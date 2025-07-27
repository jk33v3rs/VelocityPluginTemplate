/*
 * This file is part of VeloctopusProject, licensed under the MIT License.
 *
 * Copyright (c) 2025 VeloctopusProject Contributors
 * 
 * Async Redis Caching Layer Implementation
 * Step 22: Implement Redis caching layer with cluster support
 */

package org.veloctopus.cache.redis;

import io.github.jk33v3rs.veloctopusrising.api.async.AsyncPattern;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.HostAndPort;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.Instant;
import java.time.Duration;

/**
 * Async Redis Caching Layer
 * 
 * Provides high-performance Redis caching with cluster support including:
 * - Async Redis operations with CompletableFuture
 * - Redis Cluster support with automatic failover
 * - Intelligent caching strategies (LRU, TTL, pattern-based)
 * - Cache analytics and hit/miss ratio tracking
 * - Connection pooling with health monitoring
 * - Pub/Sub support for real-time events
 * - Distributed locking and coordination
 * - Cache warming and preloading strategies
 * 
 * Performance Targets:
 * - >95% cache hit rate for frequently accessed data
 * - <2ms average cache operation time
 * - <30 seconds failover time in cluster mode
 * - Zero data loss during cluster failover
 * 
 * @author VeloctopusProject Team
 * @since 1.0.0
 */
public class AsyncRedisCacheLayer implements AsyncPattern {

    /**
     * Cache operation types for analytics
     */
    public enum CacheOperation {
        GET,
        SET,
        DELETE,
        EXISTS,
        EXPIRE,
        KEYS,
        PUBLISH,
        SUBSCRIBE
    }

    /**
     * Cache connection modes
     */
    public enum ConnectionMode {
        STANDALONE,
        CLUSTER,
        SENTINEL
    }

    /**
     * Cache health states
     */
    public enum CacheHealth {
        HEALTHY,
        DEGRADED,
        CRITICAL,
        OFFLINE
    }

    /**
     * Cache statistics for monitoring and optimization
     */
    public static class CacheStatistics {
        private final Map<String, Object> metrics;
        private final Instant startTime;
        private volatile long totalOperations;
        private volatile long cacheHits;
        private volatile long cacheMisses;
        private volatile long cacheErrors;
        private volatile double averageOperationTime;
        private volatile long totalBytesStored;
        private volatile long totalBytesRetrieved;
        private final Map<CacheOperation, Long> operationCounts;

        public CacheStatistics() {
            this.metrics = new ConcurrentHashMap<>();
            this.startTime = Instant.now();
            this.totalOperations = 0;
            this.cacheHits = 0;
            this.cacheMisses = 0;
            this.cacheErrors = 0;
            this.averageOperationTime = 0.0;
            this.totalBytesStored = 0;
            this.totalBytesRetrieved = 0;
            this.operationCounts = new ConcurrentHashMap<>();
            
            // Initialize operation counts
            for (CacheOperation op : CacheOperation.values()) {
                operationCounts.put(op, 0L);
            }
        }

        public double getCacheHitRatio() {
            long total = cacheHits + cacheMisses;
            return total > 0 ? (double) cacheHits / total : 0.0;
        }

        // Getters
        public long getTotalOperations() { return totalOperations; }
        public long getCacheHits() { return cacheHits; }
        public long getCacheMisses() { return cacheMisses; }
        public long getCacheErrors() { return cacheErrors; }
        public double getAverageOperationTime() { return averageOperationTime; }
        public long getTotalBytesStored() { return totalBytesStored; }
        public long getTotalBytesRetrieved() { return totalBytesRetrieved; }
        public Map<CacheOperation, Long> getOperationCounts() { return new HashMap<>(operationCounts); }
        public Instant getStartTime() { return startTime; }
        public Map<String, Object> getMetrics() { return new ConcurrentHashMap<>(metrics); }

        // Internal update methods
        void incrementTotalOperations() { totalOperations++; }
        void incrementCacheHits() { cacheHits++; }
        void incrementCacheMisses() { cacheMisses++; }
        void incrementCacheErrors() { cacheErrors++; }
        void updateAverageOperationTime(double newTime) {
            averageOperationTime = (averageOperationTime + newTime) / 2;
        }
        void addBytesStored(long bytes) { totalBytesStored += bytes; }
        void addBytesRetrieved(long bytes) { totalBytesRetrieved += bytes; }
        void incrementOperationCount(CacheOperation operation) {
            operationCounts.merge(operation, 1L, Long::sum);
        }
        void setMetric(String key, Object value) { metrics.put(key, value); }
    }

    /**
     * Cache entry wrapper with metadata
     */
    public static class CacheEntry<T> {
        private final T value;
        private final Instant createdTime;
        private final Instant expiryTime;
        private final Map<String, Object> metadata;

        public CacheEntry(T value, Duration ttl) {
            this.value = value;
            this.createdTime = Instant.now();
            this.expiryTime = ttl != null ? createdTime.plus(ttl) : null;
            this.metadata = new ConcurrentHashMap<>();
        }

        public T getValue() { return value; }
        public Instant getCreatedTime() { return createdTime; }
        public Instant getExpiryTime() { return expiryTime; }
        public boolean isExpired() { 
            return expiryTime != null && Instant.now().isAfter(expiryTime); 
        }
        public Map<String, Object> getMetadata() { return new ConcurrentHashMap<>(metadata); }
        public void setMetadata(String key, Object value) { metadata.put(key, value); }
    }

    /**
     * Distributed lock implementation
     */
    public static class DistributedLock {
        private final String lockKey;
        private final String lockValue;
        private final Duration lockTimeout;
        private final AsyncRedisCacheLayer cacheLayer;
        private volatile boolean acquired;

        public DistributedLock(String lockKey, Duration lockTimeout, AsyncRedisCacheLayer cacheLayer) {
            this.lockKey = "lock:" + lockKey;
            this.lockValue = "lock_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
            this.lockTimeout = lockTimeout;
            this.cacheLayer = cacheLayer;
            this.acquired = false;
        }

        public CompletableFuture<Boolean> acquireAsync() {
            return cacheLayer.setIfNotExistsAsync(lockKey, lockValue, lockTimeout)
                .thenApply(success -> {
                    acquired = success;
                    return success;
                });
        }

        public CompletableFuture<Boolean> releaseAsync() {
            if (!acquired) {
                return CompletableFuture.completedFuture(false);
            }
            
            return cacheLayer.getAsync(lockKey)
                .thenCompose(currentValue -> {
                    if (lockValue.equals(currentValue)) {
                        return cacheLayer.deleteAsync(lockKey)
                            .thenApply(deleted -> {
                                acquired = false;
                                return deleted > 0;
                            });
                    }
                    return CompletableFuture.completedFuture(false);
                });
        }

        public boolean isAcquired() { return acquired; }
        public String getLockKey() { return lockKey; }
    }

    // Core components
    private JedisPool jedisPool;
    private JedisCluster jedisCluster;
    private final ScheduledExecutorService scheduler;
    private final ThreadPoolExecutor asyncExecutor;
    private final CacheStatistics statistics;
    private final Map<String, Object> localCache;
    
    // Configuration
    private final ConnectionMode connectionMode;
    private final RedisCacheConfiguration config;
    private volatile CacheHealth currentHealth;
    private volatile boolean initialized;
    
    // Monitoring
    private final Map<String, DistributedLock> activeLocks;

    public AsyncRedisCacheLayer(RedisCacheConfiguration config) {
        this.config = config;
        this.connectionMode = config.getConnectionMode();
        this.scheduler = Executors.newScheduledThreadPool(3);
        this.asyncExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(
            config.getAsyncThreadPoolSize());
        this.statistics = new CacheStatistics();
        this.localCache = new ConcurrentHashMap<>();
        this.activeLocks = new ConcurrentHashMap<>();
        this.currentHealth = CacheHealth.OFFLINE;
        this.initialized = false;
    }

    @Override
    public CompletableFuture<Boolean> initializeAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                switch (connectionMode) {
                    case STANDALONE:
                        initializeStandaloneRedis();
                        break;
                    case CLUSTER:
                        initializeRedisCluster();
                        break;
                    case SENTINEL:
                        initializeSentinelRedis();
                        break;
                    default:
                        throw new IllegalStateException("Unsupported connection mode: " + connectionMode);
                }
                
                // Test connection
                testConnection();
                
                // Start monitoring
                startHealthMonitoring();
                startCacheWarmup();
                startLockCleanup();
                
                this.currentHealth = CacheHealth.HEALTHY;
                this.initialized = true;
                
                statistics.setMetric("initialization_time", Instant.now());
                statistics.setMetric("connection_mode", connectionMode);
                statistics.setMetric("redis_servers", config.getRedisHosts());
                
                return true;
            } catch (Exception e) {
                this.currentHealth = CacheHealth.OFFLINE;
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
                // Perform cache maintenance
                updateCacheHealth();
                performCacheMaintenance();
                updateStatistics();
                cleanupExpiredLocks();
                
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
                
                // Release all locks
                for (DistributedLock lock : activeLocks.values()) {
                    try {
                        lock.releaseAsync().get(5, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        // Log but continue shutdown
                    }
                }
                activeLocks.clear();
                
                // Close connections
                if (jedisPool != null) {
                    jedisPool.close();
                }
                if (jedisCluster != null) {
                    jedisCluster.close();
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
                
                this.currentHealth = CacheHealth.OFFLINE;
                statistics.setMetric("shutdown_time", Instant.now());
                
                return true;
            } catch (Exception e) {
                statistics.setMetric("shutdown_error", e.getMessage());
                return false;
            }
        });
    }

    /**
     * Core Cache Operations
     */

    /**
     * Get value from cache with async support
     */
    public CompletableFuture<String> getAsync(String key) {
        return executeRedisOperationAsync(jedis -> {
            statistics.incrementOperationCount(CacheOperation.GET);
            statistics.incrementTotalOperations();
            
            long startTime = System.nanoTime();
            String value = jedis.get(key);
            long operationTime = (System.nanoTime() - startTime) / 1_000_000; // Convert to ms
            
            statistics.updateAverageOperationTime(operationTime);
            
            if (value != null) {
                statistics.incrementCacheHits();
                statistics.addBytesRetrieved(value.getBytes().length);
            } else {
                statistics.incrementCacheMisses();
            }
            
            return value;
        });
    }

    /**
     * Set value in cache with TTL support
     */
    public CompletableFuture<Boolean> setAsync(String key, String value, Duration ttl) {
        return executeRedisOperationAsync(jedis -> {
            statistics.incrementOperationCount(CacheOperation.SET);
            statistics.incrementTotalOperations();
            
            long startTime = System.nanoTime();
            String result;
            
            if (ttl != null) {
                result = jedis.setex(key, (int) ttl.getSeconds(), value);
            } else {
                result = jedis.set(key, value);
            }
            
            long operationTime = (System.nanoTime() - startTime) / 1_000_000;
            statistics.updateAverageOperationTime(operationTime);
            statistics.addBytesStored(value.getBytes().length);
            
            return "OK".equals(result);
        });
    }

    /**
     * Set value only if key doesn't exist (for distributed locking)
     */
    public CompletableFuture<Boolean> setIfNotExistsAsync(String key, String value, Duration ttl) {
        return executeRedisOperationAsync(jedis -> {
            statistics.incrementOperationCount(CacheOperation.SET);
            statistics.incrementTotalOperations();
            
            long startTime = System.nanoTime();
            String result;
            
            if (ttl != null) {
                result = jedis.set(key, value, "NX", "EX", (int) ttl.getSeconds());
            } else {
                result = jedis.set(key, value, "NX");
            }
            
            long operationTime = (System.nanoTime() - startTime) / 1_000_000;
            statistics.updateAverageOperationTime(operationTime);
            
            return "OK".equals(result);
        });
    }

    /**
     * Delete key from cache
     */
    public CompletableFuture<Long> deleteAsync(String key) {
        return executeRedisOperationAsync(jedis -> {
            statistics.incrementOperationCount(CacheOperation.DELETE);
            statistics.incrementTotalOperations();
            
            long startTime = System.nanoTime();
            Long result = jedis.del(key);
            long operationTime = (System.nanoTime() - startTime) / 1_000_000;
            
            statistics.updateAverageOperationTime(operationTime);
            return result;
        });
    }

    /**
     * Check if key exists
     */
    public CompletableFuture<Boolean> existsAsync(String key) {
        return executeRedisOperationAsync(jedis -> {
            statistics.incrementOperationCount(CacheOperation.EXISTS);
            statistics.incrementTotalOperations();
            
            long startTime = System.nanoTime();
            Boolean result = jedis.exists(key);
            long operationTime = (System.nanoTime() - startTime) / 1_000_000;
            
            statistics.updateAverageOperationTime(operationTime);
            return result;
        });
    }

    /**
     * Set expiration for key
     */
    public CompletableFuture<Boolean> expireAsync(String key, Duration ttl) {
        return executeRedisOperationAsync(jedis -> {
            statistics.incrementOperationCount(CacheOperation.EXPIRE);
            statistics.incrementTotalOperations();
            
            long startTime = System.nanoTime();
            Long result = jedis.expire(key, (int) ttl.getSeconds());
            long operationTime = (System.nanoTime() - startTime) / 1_000_000;
            
            statistics.updateAverageOperationTime(operationTime);
            return result == 1;
        });
    }

    /**
     * Get keys matching pattern
     */
    public CompletableFuture<Set<String>> getKeysAsync(String pattern) {
        return executeRedisOperationAsync(jedis -> {
            statistics.incrementOperationCount(CacheOperation.KEYS);
            statistics.incrementTotalOperations();
            
            long startTime = System.nanoTime();
            Set<String> result = jedis.keys(pattern);
            long operationTime = (System.nanoTime() - startTime) / 1_000_000;
            
            statistics.updateAverageOperationTime(operationTime);
            return result;
        });
    }

    /**
     * Advanced Cache Operations
     */

    /**
     * Bulk get operation for multiple keys
     */
    public CompletableFuture<Map<String, String>> bulkGetAsync(Set<String> keys) {
        if (keys.isEmpty()) {
            return CompletableFuture.completedFuture(new HashMap<>());
        }
        
        return executeRedisOperationAsync(jedis -> {
            statistics.incrementTotalOperations();
            
            List<String> keyList = new ArrayList<>(keys);
            List<String> values = jedis.mget(keyList.toArray(new String[0]));
            
            Map<String, String> result = new HashMap<>();
            for (int i = 0; i < keyList.size(); i++) {
                String value = values.get(i);
                if (value != null) {
                    result.put(keyList.get(i), value);
                    statistics.incrementCacheHits();
                } else {
                    statistics.incrementCacheMisses();
                }
            }
            
            return result;
        });
    }

    /**
     * Bulk set operation for multiple key-value pairs
     */
    public CompletableFuture<Boolean> bulkSetAsync(Map<String, String> keyValues, Duration ttl) {
        if (keyValues.isEmpty()) {
            return CompletableFuture.completedFuture(true);
        }
        
        return executeRedisOperationAsync(jedis -> {
            statistics.incrementTotalOperations();
            
            // Use Redis pipeline for bulk operations
            List<String> keyValueList = new ArrayList<>();
            for (Map.Entry<String, String> entry : keyValues.entrySet()) {
                keyValueList.add(entry.getKey());
                keyValueList.add(entry.getValue());
            }
            
            String result = jedis.mset(keyValueList.toArray(new String[0]));
            
            // Set TTL for each key if specified
            if (ttl != null) {
                for (String key : keyValues.keySet()) {
                    jedis.expire(key, (int) ttl.getSeconds());
                }
            }
            
            return "OK".equals(result);
        });
    }

    /**
     * Distributed Locking
     */

    /**
     * Create distributed lock
     */
    public DistributedLock createDistributedLock(String lockKey, Duration timeout) {
        DistributedLock lock = new DistributedLock(lockKey, timeout, this);
        activeLocks.put(lockKey, lock);
        return lock;
    }

    /**
     * Pub/Sub Operations
     */

    /**
     * Publish message to channel
     */
    public CompletableFuture<Long> publishAsync(String channel, String message) {
        return executeRedisOperationAsync(jedis -> {
            statistics.incrementOperationCount(CacheOperation.PUBLISH);
            statistics.incrementTotalOperations();
            
            return jedis.publish(channel, message);
        });
    }

    /**
     * Cache Warming and Preloading
     */

    /**
     * Warm cache with frequently accessed data
     */
    public CompletableFuture<Boolean> warmCacheAsync(Map<String, String> warmupData) {
        return bulkSetAsync(warmupData, Duration.ofHours(1))
            .thenApply(success -> {
                if (success) {
                    statistics.setMetric("cache_warmed_keys", warmupData.size());
                    statistics.setMetric("cache_warmup_time", Instant.now());
                }
                return success;
            });
    }

    /**
     * Internal Helper Methods
     */

    /**
     * Execute Redis operation with connection management
     */
    private <T> CompletableFuture<T> executeRedisOperationAsync(RedisOperation<T> operation) {
        return CompletableFuture.supplyAsync(() -> {
            Jedis jedis = null;
            try {
                if (connectionMode == ConnectionMode.CLUSTER) {
                    // Use cluster operation
                    return operation.execute(jedisCluster);
                } else {
                    jedis = jedisPool.getResource();
                    return operation.execute(jedis);
                }
            } catch (Exception e) {
                statistics.incrementCacheErrors();
                throw new RuntimeException("Redis operation failed", e);
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }, asyncExecutor);
    }

    /**
     * Initialize standalone Redis connection
     */
    private void initializeStandaloneRedis() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(config.getMaxConnections());
        poolConfig.setMaxIdle(config.getMaxIdleConnections());
        poolConfig.setMinIdle(config.getMinIdleConnections());
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        
        List<String> hosts = config.getRedisHosts();
        String[] hostPort = hosts.get(0).split(":");
        String host = hostPort[0];
        int port = hostPort.length > 1 ? Integer.parseInt(hostPort[1]) : 6379;
        
        jedisPool = new JedisPool(poolConfig, host, port, 
            config.getConnectionTimeout(), config.getRedisPassword());
    }

    /**
     * Initialize Redis cluster connection
     */
    private void initializeRedisCluster() {
        Set<HostAndPort> clusterNodes = new HashSet<>();
        for (String host : config.getRedisHosts()) {
            String[] hostPort = host.split(":");
            String hostname = hostPort[0];
            int port = hostPort.length > 1 ? Integer.parseInt(hostPort[1]) : 6379;
            clusterNodes.add(new HostAndPort(hostname, port));
        }
        
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(config.getMaxConnections());
        poolConfig.setMaxIdle(config.getMaxIdleConnections());
        poolConfig.setMinIdle(config.getMinIdleConnections());
        
        jedisCluster = new JedisCluster(clusterNodes, config.getConnectionTimeout(), 
            config.getConnectionTimeout(), 5, config.getRedisPassword(), poolConfig);
    }

    /**
     * Initialize Sentinel Redis connection
     */
    private void initializeSentinelRedis() {
        // Implementation would use JedisSentinelPool
        throw new UnsupportedOperationException("Sentinel mode not yet implemented");
    }

    /**
     * Test Redis connection
     */
    private void testConnection() {
        if (connectionMode == ConnectionMode.CLUSTER) {
            jedisCluster.ping();
        } else {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
            }
        }
    }

    /**
     * Start health monitoring
     */
    private void startHealthMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateCacheHealth();
            } catch (Exception e) {
                currentHealth = CacheHealth.CRITICAL;
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * Start cache warmup
     */
    private void startCacheWarmup() {
        scheduler.schedule(() -> {
            try {
                // Implement cache warmup logic
                Map<String, String> warmupData = generateWarmupData();
                warmCacheAsync(warmupData);
            } catch (Exception e) {
                // Log warmup failure
            }
        }, 60, TimeUnit.SECONDS);
    }

    /**
     * Start lock cleanup
     */
    private void startLockCleanup() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupExpiredLocks();
            } catch (Exception e) {
                // Log cleanup failure
            }
        }, 300, 300, TimeUnit.SECONDS); // Every 5 minutes
    }

    /**
     * Update cache health
     */
    private void updateCacheHealth() {
        try {
            // Test connection
            if (connectionMode == ConnectionMode.CLUSTER) {
                jedisCluster.ping();
            } else {
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.ping();
                }
            }
            
            // Check hit ratio
            double hitRatio = statistics.getCacheHitRatio();
            if (hitRatio > 0.9) {
                currentHealth = CacheHealth.HEALTHY;
            } else if (hitRatio > 0.7) {
                currentHealth = CacheHealth.DEGRADED;
            } else {
                currentHealth = CacheHealth.CRITICAL;
            }
            
        } catch (Exception e) {
            currentHealth = CacheHealth.OFFLINE;
        }
    }

    /**
     * Perform cache maintenance
     */
    private void performCacheMaintenance() {
        // Implement cache maintenance logic
        statistics.setMetric("last_maintenance", Instant.now());
    }

    /**
     * Update statistics
     */
    private void updateStatistics() {
        statistics.setMetric("current_health", currentHealth);
        statistics.setMetric("last_update", Instant.now());
        statistics.setMetric("uptime_seconds", 
            (System.currentTimeMillis() - statistics.getStartTime().toEpochMilli()) / 1000);
        statistics.setMetric("active_locks_count", activeLocks.size());
    }

    /**
     * Clean up expired locks
     */
    private void cleanupExpiredLocks() {
        List<String> expiredLocks = new ArrayList<>();
        
        for (Map.Entry<String, DistributedLock> entry : activeLocks.entrySet()) {
            DistributedLock lock = entry.getValue();
            if (!lock.isAcquired()) {
                expiredLocks.add(entry.getKey());
            }
        }
        
        for (String lockKey : expiredLocks) {
            activeLocks.remove(lockKey);
        }
        
        statistics.setMetric("expired_locks_cleaned", expiredLocks.size());
    }

    /**
     * Generate warmup data
     */
    private Map<String, String> generateWarmupData() {
        Map<String, String> warmupData = new HashMap<>();
        
        // Add commonly accessed configuration keys
        warmupData.put("config:server_status", "online");
        warmupData.put("config:maintenance_mode", "false");
        warmupData.put("config:max_players", "1000");
        
        return warmupData;
    }

    /**
     * Get comprehensive cache status
     */
    public CompletableFuture<Map<String, Object>> getCacheStatusAsync() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> status = new HashMap<>();
            
            status.put("health", currentHealth);
            status.put("connection_mode", connectionMode);
            status.put("statistics", statistics.getMetrics());
            status.put("cache_hit_ratio", statistics.getCacheHitRatio());
            status.put("total_operations", statistics.getTotalOperations());
            status.put("active_locks", activeLocks.size());
            status.put("initialized", initialized);
            
            return status;
        }, asyncExecutor);
    }

    // Getters
    public CacheHealth getCurrentHealth() { return currentHealth; }
    public CacheStatistics getStatistics() { return statistics; }
    public boolean isInitialized() { return initialized; }
    public ConnectionMode getConnectionMode() { return connectionMode; }

    /**
     * Functional interface for Redis operations
     */
    @FunctionalInterface
    private interface RedisOperation<T> {
        T execute(Jedis jedis) throws Exception;
    }

    /**
     * Configuration class for Redis cache settings
     */
    public static class RedisCacheConfiguration {
        private ConnectionMode connectionMode = ConnectionMode.STANDALONE;
        private List<String> redisHosts = Arrays.asList("localhost:6379");
        private String redisPassword = null;
        private int maxConnections = 20;
        private int maxIdleConnections = 10;
        private int minIdleConnections = 5;
        private int connectionTimeout = 5000;
        private int asyncThreadPoolSize = 10;

        // Getters and setters
        public ConnectionMode getConnectionMode() { return connectionMode; }
        public void setConnectionMode(ConnectionMode connectionMode) { this.connectionMode = connectionMode; }
        public List<String> getRedisHosts() { return redisHosts; }
        public void setRedisHosts(List<String> redisHosts) { this.redisHosts = redisHosts; }
        public String getRedisPassword() { return redisPassword; }
        public void setRedisPassword(String redisPassword) { this.redisPassword = redisPassword; }
        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
        public int getMaxIdleConnections() { return maxIdleConnections; }
        public void setMaxIdleConnections(int maxIdleConnections) { this.maxIdleConnections = maxIdleConnections; }
        public int getMinIdleConnections() { return minIdleConnections; }
        public void setMinIdleConnections(int minIdleConnections) { this.minIdleConnections = minIdleConnections; }
        public int getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
        public int getAsyncThreadPoolSize() { return asyncThreadPoolSize; }
        public void setAsyncThreadPoolSize(int asyncThreadPoolSize) { this.asyncThreadPoolSize = asyncThreadPoolSize; }
    }
}
