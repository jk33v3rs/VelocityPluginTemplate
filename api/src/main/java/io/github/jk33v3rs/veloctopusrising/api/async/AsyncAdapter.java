package io.github.jk33v3rs.veloctopusrising.api.async;

import java.util.concurrent.CompletableFuture;

/**
 * Async adapter interface for integrating borrowed code with unified async patterns.
 *
 * <p>This interface provides a standardized way to wrap borrowed implementations
 * from reference projects with the VeloctopusRising async pattern framework.
 * All borrowed code components should implement or be wrapped by this interface.</p>
 *
 * <h2>Implementation Requirements</h2>
 * <ul>
 *     <li>All operations must return CompletableFuture instances</li>
 *     <li>Error handling must follow standard async patterns</li>
 *     <li>Resource cleanup must be implemented in finally blocks</li>
 *     <li>Timeout handling must be properly configured</li>
 * </ul>
 *
 * @param <T> the type of component being adapted
 * @since 1.0.0
 * @author VeloctopusRising Development Team
 */
public interface AsyncAdapter<T> {

    /**
     * Initializes the borrowed component asynchronously.
     *
     * @return CompletableFuture that completes when initialization is done
     */
    CompletableFuture<Void> initializeAsync();

    /**
     * Starts the borrowed component asynchronously.
     *
     * @return CompletableFuture containing the initialized component
     */
    CompletableFuture<T> startAsync();

    /**
     * Stops the borrowed component asynchronously.
     *
     * @return CompletableFuture that completes when shutdown is done
     */
    CompletableFuture<Void> stopAsync();

    /**
     * Checks if the component is currently running.
     *
     * @return CompletableFuture containing the running status
     */
    CompletableFuture<Boolean> isRunningAsync();

    /**
     * Gets the wrapped component instance.
     *
     * @return CompletableFuture containing the component
     */
    CompletableFuture<T> getComponentAsync();

    /**
     * Performs health check on the component.
     *
     * @return CompletableFuture containing health status
     */
    CompletableFuture<HealthStatus> healthCheckAsync();

    /**
     * Gets performance metrics from the component.
     *
     * @return CompletableFuture containing current metrics
     */
    CompletableFuture<ComponentMetrics> getMetricsAsync();

    /**
     * Health status of a component.
     */
    enum HealthStatus {
        HEALTHY("Component is operating normally"),
        DEGRADED("Component is operating with reduced functionality"),
        UNHEALTHY("Component has critical issues"),
        UNKNOWN("Component status cannot be determined");

        private final String description;

        HealthStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Performance metrics for a component.
     */
    class ComponentMetrics {
        private final long operationCount;
        private final long errorCount;
        private final double averageResponseTime;
        private final long lastOperationTime;
        private final double successRate;

        public ComponentMetrics(long operationCount, long errorCount, 
                               double averageResponseTime, long lastOperationTime) {
            this.operationCount = operationCount;
            this.errorCount = errorCount;
            this.averageResponseTime = averageResponseTime;
            this.lastOperationTime = lastOperationTime;
            this.successRate = operationCount > 0 ? 
                (double)(operationCount - errorCount) / operationCount : 0.0;
        }

        public long getOperationCount() {
            return operationCount;
        }

        public long getErrorCount() {
            return errorCount;
        }

        public double getAverageResponseTime() {
            return averageResponseTime;
        }

        public long getLastOperationTime() {
            return lastOperationTime;
        }

        public double getSuccessRate() {
            return successRate;
        }
    }
}
