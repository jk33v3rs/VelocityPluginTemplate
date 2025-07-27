package io.github.jk33v3rs.veloctopusrising.api.data;

import java.time.Duration;
import java.time.Instant;

/**
 * Comprehensive statistics and performance metrics for Veloctopus Rising connection pools.
 * 
 * <p>This immutable class provides detailed performance metrics and operational
 * statistics for connection pool monitoring, optimization, and capacity planning.
 * Designed for high-frequency metrics collection without performance impact.</p>
 * 
 * <p><strong>Thread Safety:</strong> Immutable class, fully thread-safe for
 * concurrent access across multiple threads and monitoring systems.</p>
 * 
 * <p><strong>Performance:</strong> Lightweight value object optimized for
 * minimal memory footprint and efficient serialization to metrics systems.</p>
 * 
 * <h3>Metrics Categories:</h3>
 * <ul>
 *   <li><strong>Connection Counts:</strong> Current pool state and utilization</li>
 *   <li><strong>Performance:</strong> Timing metrics and throughput rates</li>
 *   <li><strong>Success/Failure:</strong> Error rates and reliability metrics</li>
 *   <li><strong>Lifecycle:</strong> Pool creation and operational duration</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * ConnectionPoolStats stats = connectionPool.getStats();
 * 
 * // Performance monitoring
 * logger.info("Pool utilization: {}%, avg acquisition: {}ms", 
 *     stats.getUtilizationPercentage(), 
 *     stats.getAverageAcquisitionTime().toMillis());
 * 
 * // Capacity planning
 * if (stats.getPeakActiveConnections() &gt; stats.getMaxPoolSize() * 0.8) {
 *     logger.warn("Consider increasing pool size for peak demand");
 * }
 * 
 * // Error rate monitoring
 * double errorRate = stats.getFailureRate();
 * if (errorRate &gt; 5.0) {
 *     alertingService.sendAlert("High connection failure rate: " + errorRate + "%");
 * }
 * </code></pre>
 * 
 * <h3>Integration Points:</h3>
 * <ul>
 *   <li>Performance monitoring and metrics collection systems</li>
 *   <li>Capacity planning and resource optimization tools</li>
 *   <li>SLA monitoring and alerting frameworks</li>
 *   <li>Database performance tuning and analysis</li>
 * </ul>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see VeloctopusConnectionPool
 * @see ConnectionPoolHealth
 */
public final class ConnectionPoolStats {
    
    // Current State Metrics
    private final int currentActiveConnections;
    private final int currentIdleConnections;
    private final int currentTotalConnections;
    private final int maxPoolSize;
    private final int minPoolSize;
    
    // Performance Metrics
    private final Duration averageAcquisitionTime;
    private final Duration minAcquisitionTime;
    private final Duration maxAcquisitionTime;
    private final Duration averageConnectionLifetime;
    private final double connectionsPerSecond;
    
    // Cumulative Counters
    private final long totalConnectionsCreated;
    private final long totalConnectionsDestroyed;
    private final long totalAcquisitionRequests;
    private final long totalSuccessfulAcquisitions;
    private final long totalFailedAcquisitions;
    private final long totalTimeouts;
    
    // Peak Usage Tracking
    private final int peakActiveConnections;
    private final int peakTotalConnections;
    private final Instant peakUsageTime;
    
    // Lifecycle Information
    private final Instant poolCreatedTime;
    private final Instant lastStatisticsReset;
    private final Duration poolUptime;
    
    // Error and Reliability Metrics
    private final long validationFailures;
    private final long circuitBreakerActivations;
    private final Instant lastFailureTime;
    private final String lastFailureReason;
    
    /**
     * Creates a new ConnectionPoolStats instance.
     * 
     * <p>Private constructor used by the builder pattern. All statistics
     * are provided through the Builder class to ensure consistency and validation.</p>
     * 
     * @param builder the builder containing all statistics information
     * @since 1.0.0
     */
    private ConnectionPoolStats(Builder builder) {
        // Current State
        this.currentActiveConnections = builder.currentActiveConnections;
        this.currentIdleConnections = builder.currentIdleConnections;
        this.currentTotalConnections = builder.currentTotalConnections;
        this.maxPoolSize = builder.maxPoolSize;
        this.minPoolSize = builder.minPoolSize;
        
        // Performance
        this.averageAcquisitionTime = builder.averageAcquisitionTime;
        this.minAcquisitionTime = builder.minAcquisitionTime;
        this.maxAcquisitionTime = builder.maxAcquisitionTime;
        this.averageConnectionLifetime = builder.averageConnectionLifetime;
        this.connectionsPerSecond = builder.connectionsPerSecond;
        
        // Cumulative
        this.totalConnectionsCreated = builder.totalConnectionsCreated;
        this.totalConnectionsDestroyed = builder.totalConnectionsDestroyed;
        this.totalAcquisitionRequests = builder.totalAcquisitionRequests;
        this.totalSuccessfulAcquisitions = builder.totalSuccessfulAcquisitions;
        this.totalFailedAcquisitions = builder.totalFailedAcquisitions;
        this.totalTimeouts = builder.totalTimeouts;
        
        // Peak Usage
        this.peakActiveConnections = builder.peakActiveConnections;
        this.peakTotalConnections = builder.peakTotalConnections;
        this.peakUsageTime = builder.peakUsageTime;
        
        // Lifecycle
        this.poolCreatedTime = builder.poolCreatedTime;
        this.lastStatisticsReset = builder.lastStatisticsReset;
        this.poolUptime = builder.poolUptime;
        
        // Error Metrics
        this.validationFailures = builder.validationFailures;
        this.circuitBreakerActivations = builder.circuitBreakerActivations;
        this.lastFailureTime = builder.lastFailureTime;
        this.lastFailureReason = builder.lastFailureReason;
    }
    
    // Current State Getters
    
    /**
     * Gets the current number of active connections in the pool.
     * 
     * @return current active connection count (non-negative)
     * @since 1.0.0
     */
    public int getCurrentActiveConnections() {
        return currentActiveConnections;
    }
    
    /**
     * Gets the current number of idle connections in the pool.
     * 
     * @return current idle connection count (non-negative)
     * @since 1.0.0
     */
    public int getCurrentIdleConnections() {
        return currentIdleConnections;
    }
    
    /**
     * Gets the current total number of connections in the pool.
     * 
     * @return current total connection count (non-negative)
     * @since 1.0.0
     */
    public int getCurrentTotalConnections() {
        return currentTotalConnections;
    }
    
    /**
     * Gets the maximum configured pool size.
     * 
     * @return maximum pool size (positive integer)
     * @since 1.0.0
     */
    public int getMaxPoolSize() {
        return maxPoolSize;
    }
    
    /**
     * Gets the minimum configured pool size.
     * 
     * @return minimum pool size (non-negative integer)
     * @since 1.0.0
     */
    public int getMinPoolSize() {
        return minPoolSize;
    }
    
    // Performance Getters
    
    /**
     * Gets the average time to acquire a connection from the pool.
     * 
     * @return average acquisition time (never null)
     * @since 1.0.0
     */
    public Duration getAverageAcquisitionTime() {
        return averageAcquisitionTime;
    }
    
    /**
     * Gets the minimum recorded connection acquisition time.
     * 
     * @return minimum acquisition time (never null)
     * @since 1.0.0
     */
    public Duration getMinAcquisitionTime() {
        return minAcquisitionTime;
    }
    
    /**
     * Gets the maximum recorded connection acquisition time.
     * 
     * @return maximum acquisition time (never null)
     * @since 1.0.0
     */
    public Duration getMaxAcquisitionTime() {
        return maxAcquisitionTime;
    }
    
    /**
     * Gets the average lifetime of connections in the pool.
     * 
     * @return average connection lifetime (never null)
     * @since 1.0.0
     */
    public Duration getAverageConnectionLifetime() {
        return averageConnectionLifetime;
    }
    
    /**
     * Gets the current rate of connection acquisitions per second.
     * 
     * @return connections per second (non-negative)
     * @since 1.0.0
     */
    public double getConnectionsPerSecond() {
        return connectionsPerSecond;
    }
    
    // Cumulative Getters
    
    /**
     * Gets the total number of connections created since pool initialization.
     * 
     * @return total connections created (non-negative)
     * @since 1.0.0
     */
    public long getTotalConnectionsCreated() {
        return totalConnectionsCreated;
    }
    
    /**
     * Gets the total number of connections destroyed since pool initialization.
     * 
     * @return total connections destroyed (non-negative)
     * @since 1.0.0
     */
    public long getTotalConnectionsDestroyed() {
        return totalConnectionsDestroyed;
    }
    
    /**
     * Gets the total number of connection acquisition requests.
     * 
     * @return total acquisition requests (non-negative)
     * @since 1.0.0
     */
    public long getTotalAcquisitionRequests() {
        return totalAcquisitionRequests;
    }
    
    /**
     * Gets the total number of successful connection acquisitions.
     * 
     * @return total successful acquisitions (non-negative)
     * @since 1.0.0
     */
    public long getTotalSuccessfulAcquisitions() {
        return totalSuccessfulAcquisitions;
    }
    
    /**
     * Gets the total number of failed connection acquisitions.
     * 
     * @return total failed acquisitions (non-negative)
     * @since 1.0.0
     */
    public long getTotalFailedAcquisitions() {
        return totalFailedAcquisitions;
    }
    
    /**
     * Gets the total number of connection acquisition timeouts.
     * 
     * @return total timeouts (non-negative)
     * @since 1.0.0
     */
    public long getTotalTimeouts() {
        return totalTimeouts;
    }
    
    // Peak Usage Getters
    
    /**
     * Gets the peak number of active connections recorded.
     * 
     * @return peak active connections (non-negative)
     * @since 1.0.0
     */
    public int getPeakActiveConnections() {
        return peakActiveConnections;
    }
    
    /**
     * Gets the peak total number of connections recorded.
     * 
     * @return peak total connections (non-negative)
     * @since 1.0.0
     */
    public int getPeakTotalConnections() {
        return peakTotalConnections;
    }
    
    /**
     * Gets the timestamp when peak usage was recorded.
     * 
     * @return peak usage timestamp, or null if no peak recorded
     * @since 1.0.0
     */
    public Instant getPeakUsageTime() {
        return peakUsageTime;
    }
    
    // Lifecycle Getters
    
    /**
     * Gets the timestamp when the pool was created.
     * 
     * @return pool creation timestamp (never null)
     * @since 1.0.0
     */
    public Instant getPoolCreatedTime() {
        return poolCreatedTime;
    }
    
    /**
     * Gets the timestamp when statistics were last reset.
     * 
     * @return last statistics reset timestamp, or null if never reset
     * @since 1.0.0
     */
    public Instant getLastStatisticsReset() {
        return lastStatisticsReset;
    }
    
    /**
     * Gets the total uptime of the connection pool.
     * 
     * @return pool uptime (never null)
     * @since 1.0.0
     */
    public Duration getPoolUptime() {
        return poolUptime;
    }
    
    // Error Metrics Getters
    
    /**
     * Gets the total number of connection validation failures.
     * 
     * @return total validation failures (non-negative)
     * @since 1.0.0
     */
    public long getValidationFailures() {
        return validationFailures;
    }
    
    /**
     * Gets the total number of circuit breaker activations.
     * 
     * @return total circuit breaker activations (non-negative)
     * @since 1.0.0
     */
    public long getCircuitBreakerActivations() {
        return circuitBreakerActivations;
    }
    
    /**
     * Gets the timestamp of the last failure.
     * 
     * @return last failure timestamp, or null if no failures
     * @since 1.0.0
     */
    public Instant getLastFailureTime() {
        return lastFailureTime;
    }
    
    /**
     * Gets the reason for the last failure.
     * 
     * @return last failure reason, or null if no failures
     * @since 1.0.0
     */
    public String getLastFailureReason() {
        return lastFailureReason;
    }
    
    // Calculated Metrics
    
    /**
     * Calculates the current pool utilization as a percentage.
     * 
     * <p>Returns (active connections / total connections) * 100.
     * Values above 80% may indicate the need for pool expansion.</p>
     * 
     * @return utilization percentage (0.0 to 100.0)
     * @since 1.0.0
     */
    public double getUtilizationPercentage() {
        if (currentTotalConnections == 0) {
            return 0.0;
        }
        return (currentActiveConnections / (double) currentTotalConnections) * 100.0;
    }
    
    /**
     * Calculates the connection success rate as a percentage.
     * 
     * <p>Returns (successful acquisitions / total requests) * 100.
     * Success rates below 95% typically indicate serious issues.</p>
     * 
     * @return success rate percentage (0.0 to 100.0)
     * @since 1.0.0
     */
    public double getSuccessRate() {
        if (totalAcquisitionRequests == 0) {
            return 100.0;
        }
        return (totalSuccessfulAcquisitions / (double) totalAcquisitionRequests) * 100.0;
    }
    
    /**
     * Calculates the connection failure rate as a percentage.
     * 
     * <p>Returns (failed acquisitions / total requests) * 100.
     * Failure rates above 5% typically indicate serious issues.</p>
     * 
     * @return failure rate percentage (0.0 to 100.0)
     * @since 1.0.0
     */
    public double getFailureRate() {
        if (totalAcquisitionRequests == 0) {
            return 0.0;
        }
        return (totalFailedAcquisitions / (double) totalAcquisitionRequests) * 100.0;
    }
    
    /**
     * Calculates the connection timeout rate as a percentage.
     * 
     * <p>Returns (timeouts / total requests) * 100.
     * High timeout rates may indicate pool exhaustion or slow connections.</p>
     * 
     * @return timeout rate percentage (0.0 to 100.0)
     * @since 1.0.0
     */
    public double getTimeoutRate() {
        if (totalAcquisitionRequests == 0) {
            return 0.0;
        }
        return (totalTimeouts / (double) totalAcquisitionRequests) * 100.0;
    }
    
    /**
     * Calculates the connection churn rate (creations + destructions per hour).
     * 
     * <p>High churn rates may indicate frequent connection failures or
     * suboptimal pool configuration.</p>
     * 
     * @return connection churn rate per hour
     * @since 1.0.0
     */
    public double getConnectionChurnRate() {
        if (poolUptime.isZero()) {
            return 0.0;
        }
        double hours = poolUptime.toMinutes() / 60.0;
        return (totalConnectionsCreated + totalConnectionsDestroyed) / hours;
    }
    
    /**
     * Creates a new builder for constructing ConnectionPoolStats instances.
     * 
     * @return new stats builder instance
     * @since 1.0.0
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for creating ConnectionPoolStats instances.
     * 
     * <p>Provides a fluent API for constructing statistics objects with
     * validation and default value handling.</p>
     * 
     * @since 1.0.0
     */
    public static final class Builder {
        // Current State
        private int currentActiveConnections;
        private int currentIdleConnections;
        private int currentTotalConnections;
        private int maxPoolSize = 20;
        private int minPoolSize = 5;
        
        // Performance
        private Duration averageAcquisitionTime = Duration.ofMillis(1);
        private Duration minAcquisitionTime = Duration.ofMillis(1);
        private Duration maxAcquisitionTime = Duration.ofMillis(1);
        private Duration averageConnectionLifetime = Duration.ofMinutes(10);
        private double connectionsPerSecond;
        
        // Cumulative
        private long totalConnectionsCreated;
        private long totalConnectionsDestroyed;
        private long totalAcquisitionRequests;
        private long totalSuccessfulAcquisitions;
        private long totalFailedAcquisitions;
        private long totalTimeouts;
        
        // Peak Usage
        private int peakActiveConnections;
        private int peakTotalConnections;
        private Instant peakUsageTime;
        
        // Lifecycle
        private Instant poolCreatedTime = Instant.now();
        private Instant lastStatisticsReset;
        private Duration poolUptime = Duration.ZERO;
        
        // Error Metrics
        private long validationFailures;
        private long circuitBreakerActivations;
        private Instant lastFailureTime;
        private String lastFailureReason;
        
        private Builder() {}
        
        // Current State Setters
        public Builder currentActiveConnections(int currentActiveConnections) {
            this.currentActiveConnections = Math.max(0, currentActiveConnections);
            return this;
        }
        
        public Builder currentIdleConnections(int currentIdleConnections) {
            this.currentIdleConnections = Math.max(0, currentIdleConnections);
            return this;
        }
        
        public Builder currentTotalConnections(int currentTotalConnections) {
            this.currentTotalConnections = Math.max(0, currentTotalConnections);
            return this;
        }
        
        public Builder maxPoolSize(int maxPoolSize) {
            this.maxPoolSize = Math.max(1, maxPoolSize);
            return this;
        }
        
        public Builder minPoolSize(int minPoolSize) {
            this.minPoolSize = Math.max(0, minPoolSize);
            return this;
        }
        
        // Performance Setters
        public Builder averageAcquisitionTime(Duration averageAcquisitionTime) {
            this.averageAcquisitionTime = averageAcquisitionTime != null ? 
                averageAcquisitionTime : Duration.ofMillis(1);
            return this;
        }
        
        public Builder minAcquisitionTime(Duration minAcquisitionTime) {
            this.minAcquisitionTime = minAcquisitionTime != null ? 
                minAcquisitionTime : Duration.ofMillis(1);
            return this;
        }
        
        public Builder maxAcquisitionTime(Duration maxAcquisitionTime) {
            this.maxAcquisitionTime = maxAcquisitionTime != null ? 
                maxAcquisitionTime : Duration.ofMillis(1);
            return this;
        }
        
        public Builder averageConnectionLifetime(Duration averageConnectionLifetime) {
            this.averageConnectionLifetime = averageConnectionLifetime != null ? 
                averageConnectionLifetime : Duration.ofMinutes(10);
            return this;
        }
        
        public Builder connectionsPerSecond(double connectionsPerSecond) {
            this.connectionsPerSecond = Math.max(0.0, connectionsPerSecond);
            return this;
        }
        
        // Cumulative Setters
        public Builder totalConnectionsCreated(long totalConnectionsCreated) {
            this.totalConnectionsCreated = Math.max(0, totalConnectionsCreated);
            return this;
        }
        
        public Builder totalConnectionsDestroyed(long totalConnectionsDestroyed) {
            this.totalConnectionsDestroyed = Math.max(0, totalConnectionsDestroyed);
            return this;
        }
        
        public Builder totalAcquisitionRequests(long totalAcquisitionRequests) {
            this.totalAcquisitionRequests = Math.max(0, totalAcquisitionRequests);
            return this;
        }
        
        public Builder totalSuccessfulAcquisitions(long totalSuccessfulAcquisitions) {
            this.totalSuccessfulAcquisitions = Math.max(0, totalSuccessfulAcquisitions);
            return this;
        }
        
        public Builder totalFailedAcquisitions(long totalFailedAcquisitions) {
            this.totalFailedAcquisitions = Math.max(0, totalFailedAcquisitions);
            return this;
        }
        
        public Builder totalTimeouts(long totalTimeouts) {
            this.totalTimeouts = Math.max(0, totalTimeouts);
            return this;
        }
        
        // Peak Usage Setters
        public Builder peakActiveConnections(int peakActiveConnections) {
            this.peakActiveConnections = Math.max(0, peakActiveConnections);
            return this;
        }
        
        public Builder peakTotalConnections(int peakTotalConnections) {
            this.peakTotalConnections = Math.max(0, peakTotalConnections);
            return this;
        }
        
        public Builder peakUsageTime(Instant peakUsageTime) {
            this.peakUsageTime = peakUsageTime;
            return this;
        }
        
        // Lifecycle Setters
        public Builder poolCreatedTime(Instant poolCreatedTime) {
            this.poolCreatedTime = poolCreatedTime != null ? poolCreatedTime : Instant.now();
            return this;
        }
        
        public Builder lastStatisticsReset(Instant lastStatisticsReset) {
            this.lastStatisticsReset = lastStatisticsReset;
            return this;
        }
        
        public Builder poolUptime(Duration poolUptime) {
            this.poolUptime = poolUptime != null ? poolUptime : Duration.ZERO;
            return this;
        }
        
        // Error Metrics Setters
        public Builder validationFailures(long validationFailures) {
            this.validationFailures = Math.max(0, validationFailures);
            return this;
        }
        
        public Builder circuitBreakerActivations(long circuitBreakerActivations) {
            this.circuitBreakerActivations = Math.max(0, circuitBreakerActivations);
            return this;
        }
        
        public Builder lastFailureTime(Instant lastFailureTime) {
            this.lastFailureTime = lastFailureTime;
            return this;
        }
        
        public Builder lastFailureReason(String lastFailureReason) {
            this.lastFailureReason = lastFailureReason;
            return this;
        }
        
        /**
         * Builds the ConnectionPoolStats instance with validation.
         * 
         * @return new stats instance
         * @throws IllegalStateException if configuration is invalid
         */
        public ConnectionPoolStats build() {
            // Validate basic constraints
            if (minPoolSize > maxPoolSize) {
                throw new IllegalStateException(
                    "Minimum pool size (" + minPoolSize + 
                    ") cannot exceed maximum pool size (" + maxPoolSize + ")");
            }
            
            if (totalFailedAcquisitions > totalAcquisitionRequests) {
                throw new IllegalStateException(
                    "Failed acquisitions cannot exceed total requests");
            }
            
            if (totalSuccessfulAcquisitions > totalAcquisitionRequests) {
                throw new IllegalStateException(
                    "Successful acquisitions cannot exceed total requests");
            }
            
            // Auto-calculate total connections if not set
            if (currentTotalConnections == 0) {
                currentTotalConnections = currentActiveConnections + currentIdleConnections;
            }
            
            return new ConnectionPoolStats(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "ConnectionPoolStats{active=%d, idle=%d, total=%d, utilization=%.1f%%, " +
            "successRate=%.1f%%, avgAcqTime=%s, uptime=%s}",
            currentActiveConnections, currentIdleConnections, currentTotalConnections,
            getUtilizationPercentage(), getSuccessRate(), averageAcquisitionTime, poolUptime);
    }
}
