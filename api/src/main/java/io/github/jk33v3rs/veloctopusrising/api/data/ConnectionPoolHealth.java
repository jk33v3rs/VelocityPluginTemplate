package io.github.jk33v3rs.veloctopusrising.api.data;

import java.time.Instant;
import java.time.Duration;

/**
 * Comprehensive health status information for Veloctopus Rising connection pools.
 * 
 * <p>This immutable class provides detailed health metrics and status information
 * for connection pool monitoring and alerting systems. Designed to support
 * high-frequency health checks without performance impact.</p>
 * 
 * <p><strong>Thread Safety:</strong> Immutable class, fully thread-safe for
 * concurrent access across multiple threads.</p>
 * 
 * <p><strong>Performance:</strong> Lightweight value object optimized for
 * minimal memory footprint and fast serialization for monitoring systems.</p>
 * 
 * <h3>Health Status Levels:</h3>
 * <ul>
 *   <li><strong>HEALTHY:</strong> Pool operating normally within parameters</li>
 *   <li><strong>WARNING:</strong> High utilization or minor issues detected</li>
 *   <li><strong>CRITICAL:</strong> Serious problems affecting functionality</li>
 *   <li><strong>FAILED:</strong> Pool non-functional, requires intervention</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * ConnectionPoolHealth health = connectionPool.getHealth();
 * if (health.getStatus() != HealthStatus.HEALTHY) {
 *     logger.warn("Pool health issue: {} - {}", 
 *         health.getStatus(), health.getStatusMessage());
 *     
 *     // Check specific metrics
 *     if (health.getUtilizationPercentage() &gt; 90) {
 *         alertingService.sendAlert("High pool utilization");
 *     }
 * }
 * </code></pre>
 * 
 * <h3>Integration Points:</h3>
 * <ul>
 *   <li>Health monitoring and alerting systems</li>
 *   <li>Performance metrics collection</li>
 *   <li>Circuit breaker pattern implementations</li>
 *   <li>Load balancer health check endpoints</li>
 * </ul>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see VeloctopusConnectionPool
 * @see ConnectionPoolStats
 */
public final class ConnectionPoolHealth {
    
    /**
     * Health status enumeration for connection pool monitoring.
     * 
     * <p>Defines the overall health state of a connection pool, enabling
     * automated monitoring and alerting based on pool conditions.</p>
     * 
     * @since 1.0.0
     */
    public enum HealthStatus {
        /** Pool is operating normally within all parameters */
        HEALTHY,
        /** Pool has high utilization or minor issues that should be monitored */
        WARNING,
        /** Pool has serious problems that may affect functionality */
        CRITICAL,
        /** Pool is non-functional and requires immediate intervention */
        FAILED
    }
    
    private final HealthStatus status;
    private final String statusMessage;
    private final Instant lastChecked;
    private final int totalConnections;
    private final int activeConnections;
    private final int idleConnections;
    private final int failedConnections;
    private final double utilizationPercentage;
    private final Duration averageAcquisitionTime;
    private final long totalAcquisitions;
    private final long failedAcquisitions;
    private final boolean circuitBreakerOpen;
    private final Instant lastFailure;
    private final String lastFailureMessage;
    
    /**
     * Creates a new ConnectionPoolHealth instance.
     * 
     * <p>Private constructor used by the builder pattern. All health information
     * is provided through the Builder class to ensure consistency and validation.</p>
     * 
     * @param builder the builder containing all health information
     * @since 1.0.0
     */
    private ConnectionPoolHealth(Builder builder) {
        this.status = builder.status;
        this.statusMessage = builder.statusMessage;
        this.lastChecked = builder.lastChecked;
        this.totalConnections = builder.totalConnections;
        this.activeConnections = builder.activeConnections;
        this.idleConnections = builder.idleConnections;
        this.failedConnections = builder.failedConnections;
        this.utilizationPercentage = builder.utilizationPercentage;
        this.averageAcquisitionTime = builder.averageAcquisitionTime;
        this.totalAcquisitions = builder.totalAcquisitions;
        this.failedAcquisitions = builder.failedAcquisitions;
        this.circuitBreakerOpen = builder.circuitBreakerOpen;
        this.lastFailure = builder.lastFailure;
        this.lastFailureMessage = builder.lastFailureMessage;
    }
    
    /**
     * Gets the overall health status of the connection pool.
     * 
     * <p>Returns the current health classification based on pool metrics
     * and operational status. Used for high-level monitoring and alerting.</p>
     * 
     * @return current health status (never null)
     * @since 1.0.0
     */
    public HealthStatus getStatus() {
        return status;
    }
    
    /**
     * Gets a human-readable description of the current pool status.
     * 
     * <p>Provides detailed information about why the pool is in its current
     * health state. Useful for debugging and detailed monitoring.</p>
     * 
     * @return status description message (never null)
     * @since 1.0.0
     */
    public String getStatusMessage() {
        return statusMessage;
    }
    
    /**
     * Gets the timestamp when this health check was performed.
     * 
     * <p>Indicates the freshness of the health information. Health checks
     * should be performed regularly to ensure up-to-date status.</p>
     * 
     * @return health check timestamp (never null)
     * @since 1.0.0
     */
    public Instant getLastChecked() {
        return lastChecked;
    }
    
    /**
     * Gets the total number of connections currently in the pool.
     * 
     * <p>Includes both active and idle connections. This should typically
     * be between the configured minimum and maximum pool sizes.</p>
     * 
     * @return total connection count (non-negative)
     * @since 1.0.0
     */
    public int getTotalConnections() {
        return totalConnections;
    }
    
    /**
     * Gets the number of connections currently in use.
     * 
     * <p>Connections that have been acquired from the pool and are
     * actively being used for database operations.</p>
     * 
     * @return active connection count (non-negative)
     * @since 1.0.0
     */
    public int getActiveConnections() {
        return activeConnections;
    }
    
    /**
     * Gets the number of connections available for immediate use.
     * 
     * <p>Connections that are validated and ready to be acquired
     * without delay. Higher idle counts indicate better responsiveness.</p>
     * 
     * @return idle connection count (non-negative)
     * @since 1.0.0
     */
    public int getIdleConnections() {
        return idleConnections;
    }
    
    /**
     * Gets the number of connections that have failed validation.
     * 
     * <p>Connections that are no longer valid and will be replaced.
     * High values may indicate network or database server issues.</p>
     * 
     * @return failed connection count (non-negative)
     * @since 1.0.0
     */
    public int getFailedConnections() {
        return failedConnections;
    }
    
    /**
     * Gets the current pool utilization as a percentage.
     * 
     * <p>Calculated as (active connections / total connections) * 100.
     * Values above 80% may indicate the need for pool expansion.</p>
     * 
     * @return utilization percentage (0.0 to 100.0)
     * @since 1.0.0
     */
    public double getUtilizationPercentage() {
        return utilizationPercentage;
    }
    
    /**
     * Gets the average time to acquire a connection from the pool.
     * 
     * <p>Measured from request to successful acquisition. Higher values
     * may indicate pool exhaustion or performance issues.</p>
     * 
     * @return average acquisition time (never null)
     * @since 1.0.0
     */
    public Duration getAverageAcquisitionTime() {
        return averageAcquisitionTime;
    }
    
    /**
     * Gets the total number of connection acquisitions since pool creation.
     * 
     * <p>Cumulative counter of all connection requests. Used for
     * calculating success rates and throughput metrics.</p>
     * 
     * @return total acquisition count (non-negative)
     * @since 1.0.0
     */
    public long getTotalAcquisitions() {
        return totalAcquisitions;
    }
    
    /**
     * Gets the total number of failed connection acquisitions.
     * 
     * <p>Cumulative counter of acquisition failures. High failure rates
     * indicate serious pool or database connectivity issues.</p>
     * 
     * @return failed acquisition count (non-negative)
     * @since 1.0.0
     */
    public long getFailedAcquisitions() {
        return failedAcquisitions;
    }
    
    /**
     * Checks if the circuit breaker is currently open.
     * 
     * <p>When open, the circuit breaker prevents new connection attempts
     * to protect the pool from cascading failures. The breaker will
     * attempt to close after a recovery period.</p>
     * 
     * @return true if circuit breaker is open, false if closed
     * @since 1.0.0
     */
    public boolean isCircuitBreakerOpen() {
        return circuitBreakerOpen;
    }
    
    /**
     * Gets the timestamp of the last connection failure.
     * 
     * <p>Indicates when the most recent connection problem occurred.
     * May be null if no failures have occurred since pool creation.</p>
     * 
     * @return last failure timestamp, or null if no failures
     * @since 1.0.0
     */
    public Instant getLastFailure() {
        return lastFailure;
    }
    
    /**
     * Gets the error message from the last connection failure.
     * 
     * <p>Provides details about the most recent failure for debugging.
     * May be null if no failures have occurred since pool creation.</p>
     * 
     * @return last failure message, or null if no failures
     * @since 1.0.0
     */
    public String getLastFailureMessage() {
        return lastFailureMessage;
    }
    
    /**
     * Calculates the connection success rate as a percentage.
     * 
     * <p>Returns (successful acquisitions / total acquisitions) * 100.
     * Success rates below 95% typically indicate serious issues.</p>
     * 
     * @return success rate percentage (0.0 to 100.0)
     * @since 1.0.0
     */
    public double getSuccessRate() {
        if (totalAcquisitions == 0) {
            return 100.0; // No acquisitions = perfect success rate
        }
        long successful = totalAcquisitions - failedAcquisitions;
        return (successful / (double) totalAcquisitions) * 100.0;
    }
    
    /**
     * Creates a new builder for constructing ConnectionPoolHealth instances.
     * 
     * <p>The builder pattern ensures all required fields are provided and
     * validates health information for consistency.</p>
     * 
     * @return new health builder instance
     * @since 1.0.0
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for creating ConnectionPoolHealth instances.
     * 
     * <p>Provides a fluent API for constructing health objects with
     * validation and default value handling.</p>
     * 
     * @since 1.0.0
     */
    public static final class Builder {
        private HealthStatus status = HealthStatus.HEALTHY;
        private String statusMessage = "Pool operating normally";
        private Instant lastChecked = Instant.now();
        private int totalConnections;
        private int activeConnections;
        private int idleConnections;
        private int failedConnections;
        private double utilizationPercentage;
        private Duration averageAcquisitionTime = Duration.ofMillis(1);
        private long totalAcquisitions;
        private long failedAcquisitions;
        private boolean circuitBreakerOpen = false;
        private Instant lastFailure;
        private String lastFailureMessage;
        
        private Builder() {}
        
        public Builder status(HealthStatus status) {
            this.status = status != null ? status : HealthStatus.FAILED;
            return this;
        }
        
        public Builder statusMessage(String statusMessage) {
            this.statusMessage = statusMessage != null ? statusMessage : "Unknown status";
            return this;
        }
        
        public Builder lastChecked(Instant lastChecked) {
            this.lastChecked = lastChecked != null ? lastChecked : Instant.now();
            return this;
        }
        
        public Builder totalConnections(int totalConnections) {
            this.totalConnections = Math.max(0, totalConnections);
            return this;
        }
        
        public Builder activeConnections(int activeConnections) {
            this.activeConnections = Math.max(0, activeConnections);
            return this;
        }
        
        public Builder idleConnections(int idleConnections) {
            this.idleConnections = Math.max(0, idleConnections);
            return this;
        }
        
        public Builder failedConnections(int failedConnections) {
            this.failedConnections = Math.max(0, failedConnections);
            return this;
        }
        
        public Builder utilizationPercentage(double utilizationPercentage) {
            this.utilizationPercentage = Math.max(0.0, Math.min(100.0, utilizationPercentage));
            return this;
        }
        
        public Builder averageAcquisitionTime(Duration averageAcquisitionTime) {
            this.averageAcquisitionTime = averageAcquisitionTime != null ? 
                averageAcquisitionTime : Duration.ofMillis(1);
            return this;
        }
        
        public Builder totalAcquisitions(long totalAcquisitions) {
            this.totalAcquisitions = Math.max(0, totalAcquisitions);
            return this;
        }
        
        public Builder failedAcquisitions(long failedAcquisitions) {
            this.failedAcquisitions = Math.max(0, failedAcquisitions);
            return this;
        }
        
        public Builder circuitBreakerOpen(boolean circuitBreakerOpen) {
            this.circuitBreakerOpen = circuitBreakerOpen;
            return this;
        }
        
        public Builder lastFailure(Instant lastFailure) {
            this.lastFailure = lastFailure;
            return this;
        }
        
        public Builder lastFailureMessage(String lastFailureMessage) {
            this.lastFailureMessage = lastFailureMessage;
            return this;
        }
        
        /**
         * Builds the ConnectionPoolHealth instance with validation.
         * 
         * @return new health instance
         * @throws IllegalStateException if configuration is invalid
         */
        public ConnectionPoolHealth build() {
            // Validate that active + idle + failed = total
            int calculatedTotal = activeConnections + idleConnections + failedConnections;
            if (totalConnections != calculatedTotal) {
                throw new IllegalStateException(
                    "Connection counts don't add up: total=" + totalConnections + 
                    ", active=" + activeConnections + ", idle=" + idleConnections + 
                    ", failed=" + failedConnections);
            }
            
            // Validate failed acquisitions don't exceed total
            if (failedAcquisitions > totalAcquisitions) {
                throw new IllegalStateException(
                    "Failed acquisitions (" + failedAcquisitions + 
                    ") cannot exceed total acquisitions (" + totalAcquisitions + ")");
            }
            
            return new ConnectionPoolHealth(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "ConnectionPoolHealth{status=%s, message='%s', total=%d, active=%d, idle=%d, failed=%d, utilization=%.1f%%, avgAcqTime=%s}",
            status, statusMessage, totalConnections, activeConnections, idleConnections, 
            failedConnections, utilizationPercentage, averageAcquisitionTime);
    }
}
