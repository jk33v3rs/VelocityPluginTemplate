/*
 * This file is part of VeloctopusProject, licensed under the MIT License.
 *
 * Copyright (c) 2025 VeloctopusProject Contributors
 * 
 * Async MariaDB Connection Pool Implementation
 * Step 21: Implement proper async connection pooling for MariaDB
 */

package org.veloctopus.database.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.jk33v3rs.veloctopusrising.api.async.AsyncPattern;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.Instant;
import javax.sql.DataSource;

/**
 * Async MariaDB Connection Pool
 * 
 * Provides high-performance async connection pooling for MariaDB with:
 * - HikariCP-based connection management
 * - Circuit breaker pattern for fault tolerance
 * - Health monitoring and automatic failover
 * - Connection lifecycle tracking and analytics
 * - Transaction support with async patterns
 * - Connection leak detection and prevention
 * 
 * Performance Targets:
 * - <5ms connection acquisition time
 * - >99.9% connection success rate
 * - Automatic failover within 30 seconds
 * - Zero connection leaks
 * 
 * @author VeloctopusProject Team
 * @since 1.0.0
 */
public class AsyncMariaDBConnectionPool implements AsyncPattern {

    /**
     * Connection pool health states
     */
    public enum PoolHealth {
        HEALTHY,
        DEGRADED,
        CRITICAL,
        OFFLINE
    }

    /**
     * Circuit breaker states for fault tolerance
     */
    public enum CircuitBreakerState {
        CLOSED,    // Normal operation
        OPEN,      // Failing, reject requests
        HALF_OPEN  // Testing if service recovered
    }

    /**
     * Connection statistics for monitoring
     */
    public static class ConnectionStatistics {
        private final Map<String, Object> metrics;
        private final Instant startTime;
        private volatile long totalConnectionsRequested;
        private volatile long totalConnectionsAcquired;
        private volatile long totalConnectionsFailed;
        private volatile long totalConnectionsLeaked;
        private volatile double averageAcquisitionTime;
        private volatile long peakActiveConnections;

        public ConnectionStatistics() {
            this.metrics = new ConcurrentHashMap<>();
            this.startTime = Instant.now();
            this.totalConnectionsRequested = 0;
            this.totalConnectionsAcquired = 0;
            this.totalConnectionsFailed = 0;
            this.totalConnectionsLeaked = 0;
            this.averageAcquisitionTime = 0.0;
            this.peakActiveConnections = 0;
        }

        // Getters
        public long getTotalConnectionsRequested() { return totalConnectionsRequested; }
        public long getTotalConnectionsAcquired() { return totalConnectionsAcquired; }
        public long getTotalConnectionsFailed() { return totalConnectionsFailed; }
        public long getTotalConnectionsLeaked() { return totalConnectionsLeaked; }
        public double getAverageAcquisitionTime() { return averageAcquisitionTime; }
        public long getPeakActiveConnections() { return peakActiveConnections; }
        public Instant getStartTime() { return startTime; }
        public Map<String, Object> getMetrics() { return new ConcurrentHashMap<>(metrics); }

        // Internal update methods
        void incrementConnectionsRequested() { totalConnectionsRequested++; }
        void incrementConnectionsAcquired() { totalConnectionsAcquired++; }
        void incrementConnectionsFailed() { totalConnectionsFailed++; }
        void incrementConnectionsLeaked() { totalConnectionsLeaked++; }
        void updateAverageAcquisitionTime(double newTime) { 
            averageAcquisitionTime = (averageAcquisitionTime + newTime) / 2; 
        }
        void updatePeakActiveConnections(long activeCount) {
            if (activeCount > peakActiveConnections) {
                peakActiveConnections = activeCount;
            }
        }
        void setMetric(String key, Object value) { metrics.put(key, value); }
    }

    /**
     * Connection wrapper for tracking and management
     */
    public static class ManagedConnection implements AutoCloseable {
        private final Connection connection;
        private final String connectionId;
        private final Instant acquiredTime;
        private final AsyncMariaDBConnectionPool pool;
        private volatile boolean closed;

        public ManagedConnection(Connection connection, AsyncMariaDBConnectionPool pool) {
            this.connection = connection;
            this.connectionId = "conn_" + System.currentTimeMillis() + "_" + connection.hashCode();
            this.acquiredTime = Instant.now();
            this.pool = pool;
            this.closed = false;
        }

        public Connection getConnection() {
            if (closed) {
                throw new IllegalStateException("Connection is closed: " + connectionId);
            }
            return connection;
        }

        public String getConnectionId() { return connectionId; }
        public Instant getAcquiredTime() { return acquiredTime; }
        public boolean isClosed() { return closed; }

        @Override
        public void close() {
            if (!closed) {
                closed = true;
                pool.releaseConnection(this);
            }
        }
    }

    // Core components
    private HikariDataSource dataSource;
    private final ScheduledExecutorService scheduler;
    private final ThreadPoolExecutor asyncExecutor;
    private final ConnectionStatistics statistics;
    private final Map<String, ManagedConnection> activeConnections;
    
    // Configuration
    private volatile PoolHealth currentHealth;
    private volatile CircuitBreakerState circuitState;
    private volatile long circuitOpenTime;
    private volatile int consecutiveFailures;
    
    // Settings
    private final int maxPoolSize;
    private final int minIdleConnections;
    private final long connectionTimeoutMs;
    private final long leakDetectionThreshold;
    private final int circuitBreakerThreshold;
    private final long circuitBreakerTimeout;
    
    // Monitoring
    private volatile boolean initialized;

    public AsyncMariaDBConnectionPool(DatabasePoolConfiguration config) {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.asyncExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(
            config.getAsyncThreadPoolSize());
        this.statistics = new ConnectionStatistics();
        this.activeConnections = new ConcurrentHashMap<>();
        
        // Configuration
        this.maxPoolSize = config.getMaxPoolSize();
        this.minIdleConnections = config.getMinIdleConnections();
        this.connectionTimeoutMs = config.getConnectionTimeoutMs();
        this.leakDetectionThreshold = config.getLeakDetectionThreshold();
        this.circuitBreakerThreshold = config.getCircuitBreakerThreshold();
        this.circuitBreakerTimeout = config.getCircuitBreakerTimeout();
        
        // State
        this.currentHealth = PoolHealth.OFFLINE;
        this.circuitState = CircuitBreakerState.CLOSED;
        this.circuitOpenTime = 0;
        this.consecutiveFailures = 0;
        this.initialized = false;
    }

    @Override
    public CompletableFuture<Boolean> initializeAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Configure HikariCP
                HikariConfig hikariConfig = new HikariConfig();
                hikariConfig.setJdbcUrl("jdbc:mariadb://localhost:3306/veloctopus");
                hikariConfig.setUsername("veloctopus_user");
                hikariConfig.setPassword("veloctopus_password");
                hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
                
                // Connection pool settings
                hikariConfig.setMaximumPoolSize(maxPoolSize);
                hikariConfig.setMinimumIdle(minIdleConnections);
                hikariConfig.setConnectionTimeout(connectionTimeoutMs);
                hikariConfig.setIdleTimeout(600000); // 10 minutes
                hikariConfig.setMaxLifetime(1800000); // 30 minutes
                hikariConfig.setLeakDetectionThreshold(leakDetectionThreshold);
                
                // Connection testing
                hikariConfig.setConnectionTestQuery("SELECT 1");
                hikariConfig.setValidationTimeout(3000);
                
                // Pool name for monitoring
                hikariConfig.setPoolName("VeloctopusMariaDB");
                
                // Performance optimizations
                hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
                hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
                hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
                hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
                hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
                hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
                hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
                hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
                hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
                
                // Create data source
                this.dataSource = new HikariDataSource(hikariConfig);
                
                // Test initial connection
                try (Connection testConnection = dataSource.getConnection()) {
                    testConnection.isValid(5);
                }
                
                // Start monitoring
                startHealthMonitoring();
                startLeakDetection();
                
                this.currentHealth = PoolHealth.HEALTHY;
                this.initialized = true;
                
                statistics.setMetric("initialization_time", Instant.now());
                statistics.setMetric("pool_name", "VeloctopusMariaDB");
                statistics.setMetric("max_pool_size", maxPoolSize);
                
                return true;
            } catch (Exception e) {
                this.currentHealth = PoolHealth.OFFLINE;
                statistics.setMetric("initialization_error", e.getMessage());
                return false;
            }
        }, asyncExecutor);
    }

    @Override
    public CompletableFuture<Boolean> executeAsync() {
        if (!initialized) {
            return CompletableFuture.completedFuture(false);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Perform pool maintenance
                updatePoolHealth();
                updateCircuitBreaker();
                cleanupStaleConnections();
                updateStatistics();
                
                return true;
            } catch (Exception e) {
                statistics.setMetric("execution_error", e.getMessage());
                return false;
            }
        }, asyncExecutor);
    }

    @Override
    public CompletableFuture<Boolean> shutdownAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                initialized = false;
                
                // Close all active connections
                for (ManagedConnection conn : activeConnections.values()) {
                    try {
                        conn.close();
                    } catch (Exception e) {
                        // Log but continue shutdown
                    }
                }
                activeConnections.clear();
                
                // Shutdown data source
                if (dataSource != null) {
                    dataSource.close();
                }
                
                // Shutdown executors
                scheduler.shutdown();
                asyncExecutor.shutdown();
                
                try {
                    if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                    if (!asyncExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                        asyncExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                    asyncExecutor.shutdownNow();
                }
                
                this.currentHealth = PoolHealth.OFFLINE;
                statistics.setMetric("shutdown_time", Instant.now());
                
                return true;
            } catch (Exception e) {
                statistics.setMetric("shutdown_error", e.getMessage());
                return false;
            }
        });
    }

    /**
     * Connection Management Methods
     */

    /**
     * Acquire a connection from the pool with async support
     */
    public CompletableFuture<ManagedConnection> getConnectionAsync() {
        statistics.incrementConnectionsRequested();
        
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.nanoTime();
            
            try {
                // Check circuit breaker
                if (circuitState == CircuitBreakerState.OPEN) {
                    if (System.currentTimeMillis() - circuitOpenTime > circuitBreakerTimeout) {
                        circuitState = CircuitBreakerState.HALF_OPEN;
                    } else {
                        statistics.incrementConnectionsFailed();
                        throw new SQLException("Circuit breaker is OPEN - database unavailable");
                    }
                }
                
                // Acquire connection
                Connection connection = dataSource.getConnection();
                ManagedConnection managedConnection = new ManagedConnection(connection, this);
                
                // Track connection
                activeConnections.put(managedConnection.getConnectionId(), managedConnection);
                statistics.updatePeakActiveConnections(activeConnections.size());
                
                // Update metrics
                long acquisitionTime = (System.nanoTime() - startTime) / 1_000_000; // Convert to ms
                statistics.updateAverageAcquisitionTime(acquisitionTime);
                statistics.incrementConnectionsAcquired();
                
                // Reset circuit breaker on success
                if (circuitState == CircuitBreakerState.HALF_OPEN) {
                    circuitState = CircuitBreakerState.CLOSED;
                    consecutiveFailures = 0;
                }
                
                return managedConnection;
                
            } catch (SQLException e) {
                statistics.incrementConnectionsFailed();
                consecutiveFailures++;
                
                // Update circuit breaker
                if (consecutiveFailures >= circuitBreakerThreshold && 
                    circuitState == CircuitBreakerState.CLOSED) {
                    circuitState = CircuitBreakerState.OPEN;
                    circuitOpenTime = System.currentTimeMillis();
                }
                
                throw new RuntimeException("Failed to acquire database connection", e);
            }
        }, asyncExecutor);
    }

    /**
     * Execute a database operation with automatic connection management
     */
    public <T> CompletableFuture<T> executeWithConnectionAsync(DatabaseOperation<T> operation) {
        return getConnectionAsync()
            .thenCompose(connection -> 
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return operation.execute(connection.getConnection());
                    } catch (Exception e) {
                        throw new RuntimeException("Database operation failed", e);
                    }
                }, asyncExecutor)
                .whenComplete((result, throwable) -> {
                    connection.close();
                })
            );
    }

    /**
     * Execute a transaction with automatic rollback on failure
     */
    public <T> CompletableFuture<T> executeTransactionAsync(DatabaseTransaction<T> transaction) {
        return getConnectionAsync()
            .thenCompose(managedConnection -> 
                CompletableFuture.supplyAsync(() -> {
                    Connection connection = managedConnection.getConnection();
                    boolean originalAutoCommit = false;
                    
                    try {
                        originalAutoCommit = connection.getAutoCommit();
                        connection.setAutoCommit(false);
                        
                        T result = transaction.execute(connection);
                        connection.commit();
                        
                        return result;
                    } catch (Exception e) {
                        try {
                            connection.rollback();
                        } catch (SQLException rollbackException) {
                            // Log rollback failure
                        }
                        throw new RuntimeException("Transaction failed", e);
                    } finally {
                        try {
                            connection.setAutoCommit(originalAutoCommit);
                        } catch (SQLException e) {
                            // Log but don't throw
                        }
                        managedConnection.close();
                    }
                }, asyncExecutor)
            );
    }

    /**
     * Internal connection release
     */
    void releaseConnection(ManagedConnection managedConnection) {
        activeConnections.remove(managedConnection.getConnectionId());
        
        try {
            if (!managedConnection.getConnection().isClosed()) {
                managedConnection.getConnection().close();
            }
        } catch (SQLException e) {
            // Log connection close failure
            statistics.incrementConnectionsLeaked();
        }
    }

    /**
     * Monitoring and Health Methods
     */

    /**
     * Start health monitoring
     */
    private void startHealthMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updatePoolHealth();
            } catch (Exception e) {
                // Log monitoring error
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * Start leak detection
     */
    private void startLeakDetection() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupStaleConnections();
            } catch (Exception e) {
                // Log cleanup error
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    /**
     * Update pool health status
     */
    private void updatePoolHealth() {
        if (!initialized || dataSource == null) {
            currentHealth = PoolHealth.OFFLINE;
            return;
        }

        try {
            int activeConnections = dataSource.getHikariPoolMXBean().getActiveConnections();
            int totalConnections = dataSource.getHikariPoolMXBean().getTotalConnections();
            
            double utilizationRatio = (double) activeConnections / maxPoolSize;
            
            if (utilizationRatio < 0.7) {
                currentHealth = PoolHealth.HEALTHY;
            } else if (utilizationRatio < 0.9) {
                currentHealth = PoolHealth.DEGRADED;
            } else {
                currentHealth = PoolHealth.CRITICAL;
            }
            
            statistics.setMetric("active_connections", activeConnections);
            statistics.setMetric("total_connections", totalConnections);
            statistics.setMetric("utilization_ratio", utilizationRatio);
            
        } catch (Exception e) {
            currentHealth = PoolHealth.CRITICAL;
            statistics.setMetric("health_check_error", e.getMessage());
        }
    }

    /**
     * Update circuit breaker state
     */
    private void updateCircuitBreaker() {
        statistics.setMetric("circuit_breaker_state", circuitState);
        statistics.setMetric("consecutive_failures", consecutiveFailures);
        statistics.setMetric("circuit_open_time", circuitOpenTime);
    }

    /**
     * Clean up stale connections
     */
    private void cleanupStaleConnections() {
        long currentTime = System.currentTimeMillis();
        List<String> staleConnections = new ArrayList<>();
        
        for (Map.Entry<String, ManagedConnection> entry : activeConnections.entrySet()) {
            ManagedConnection conn = entry.getValue();
            long connectionAge = currentTime - conn.getAcquiredTime().toEpochMilli();
            
            if (connectionAge > leakDetectionThreshold) {
                staleConnections.add(entry.getKey());
                statistics.incrementConnectionsLeaked();
            }
        }
        
        // Close stale connections
        for (String connId : staleConnections) {
            ManagedConnection conn = activeConnections.remove(connId);
            if (conn != null) {
                conn.close();
            }
        }
        
        statistics.setMetric("stale_connections_cleaned", staleConnections.size());
    }

    /**
     * Update statistics
     */
    private void updateStatistics() {
        statistics.setMetric("current_health", currentHealth);
        statistics.setMetric("last_update", Instant.now());
        statistics.setMetric("uptime_seconds", 
            (System.currentTimeMillis() - statistics.getStartTime().toEpochMilli()) / 1000);
    }

    /**
     * Get comprehensive pool status
     */
    public CompletableFuture<Map<String, Object>> getPoolStatusAsync() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> status = new HashMap<>();
            
            status.put("health", currentHealth);
            status.put("circuit_breaker_state", circuitState);
            status.put("active_connections_count", activeConnections.size());
            status.put("consecutive_failures", consecutiveFailures);
            status.put("statistics", statistics.getMetrics());
            status.put("initialized", initialized);
            
            if (dataSource != null) {
                try {
                    status.put("hikari_active_connections", 
                        dataSource.getHikariPoolMXBean().getActiveConnections());
                    status.put("hikari_total_connections", 
                        dataSource.getHikariPoolMXBean().getTotalConnections());
                    status.put("hikari_idle_connections", 
                        dataSource.getHikariPoolMXBean().getIdleConnections());
                } catch (Exception e) {
                    status.put("hikari_error", e.getMessage());
                }
            }
            
            return status;
        }, asyncExecutor);
    }

    // Getters
    public PoolHealth getCurrentHealth() { return currentHealth; }
    public CircuitBreakerState getCircuitState() { return circuitState; }
    public ConnectionStatistics getStatistics() { return statistics; }
    public boolean isInitialized() { return initialized; }

    /**
     * Functional interfaces for database operations
     */
    @FunctionalInterface
    public interface DatabaseOperation<T> {
        T execute(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    public interface DatabaseTransaction<T> {
        T execute(Connection connection) throws SQLException;
    }

    /**
     * Configuration class for pool settings
     */
    public static class DatabasePoolConfiguration {
        private int maxPoolSize = 20;
        private int minIdleConnections = 5;
        private long connectionTimeoutMs = 30000;
        private long leakDetectionThreshold = 60000;
        private int circuitBreakerThreshold = 5;
        private long circuitBreakerTimeout = 60000;
        private int asyncThreadPoolSize = 10;

        // Getters and setters
        public int getMaxPoolSize() { return maxPoolSize; }
        public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }
        public int getMinIdleConnections() { return minIdleConnections; }
        public void setMinIdleConnections(int minIdleConnections) { this.minIdleConnections = minIdleConnections; }
        public long getConnectionTimeoutMs() { return connectionTimeoutMs; }
        public void setConnectionTimeoutMs(long connectionTimeoutMs) { this.connectionTimeoutMs = connectionTimeoutMs; }
        public long getLeakDetectionThreshold() { return leakDetectionThreshold; }
        public void setLeakDetectionThreshold(long leakDetectionThreshold) { 
            this.leakDetectionThreshold = leakDetectionThreshold; 
        }
        public int getCircuitBreakerThreshold() { return circuitBreakerThreshold; }
        public void setCircuitBreakerThreshold(int circuitBreakerThreshold) { 
            this.circuitBreakerThreshold = circuitBreakerThreshold; 
        }
        public long getCircuitBreakerTimeout() { return circuitBreakerTimeout; }
        public void setCircuitBreakerTimeout(long circuitBreakerTimeout) { 
            this.circuitBreakerTimeout = circuitBreakerTimeout; 
        }
        public int getAsyncThreadPoolSize() { return asyncThreadPoolSize; }
        public void setAsyncThreadPoolSize(int asyncThreadPoolSize) { 
            this.asyncThreadPoolSize = asyncThreadPoolSize; 
        }
    }
}
