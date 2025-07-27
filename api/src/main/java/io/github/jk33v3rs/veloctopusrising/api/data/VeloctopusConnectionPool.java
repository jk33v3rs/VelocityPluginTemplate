package io.github.jk33v3rs.veloctopusrising.api.data;

import java.util.concurrent.CompletableFuture;
import java.time.Duration;

/**
 * High-performance connection pool interface for Veloctopus Rising data layer.
 * 
 * <p>This interface defines the contract for connection pool management across
 * different data sources (MariaDB, Redis). Designed for async-first operations
 * with health monitoring, circuit breaker patterns, and graceful degradation.</p>
 * 
 * <p><strong>Thread Safety:</strong> All implementations must be fully thread-safe
 * and support high-concurrency access without blocking the main thread.</p>
 * 
 * <p><strong>Performance Requirements:</strong> Target &lt;5ms connection acquisition
 * time under normal load with automatic failover and recovery capabilities.
 * Supports 1000+ concurrent connections for high-throughput scenarios.</p>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * // Acquire connection asynchronously
 * connectionPool.acquireConnection()
 *     .thenCompose(connection -&gt; {
 *         // Use connection for database operations
 *         return performDatabaseOperation(connection);
 *     })
 *     .whenComplete((result, throwable) -&gt; {
 *         // Connection automatically returned to pool
 *         if (throwable != null) {
 *             logger.error("Database operation failed", throwable);
 *         }
 *     });
 * </code></pre>
 * 
 * <h3>Integration Points:</h3>
 * <ul>
 *   <li>Data Access Layer for repository pattern implementation</li>
 *   <li>Connection Pool Providers (HikariCP for MariaDB, Jedis for Redis)</li>
 *   <li>Health monitoring and metrics collection systems</li>
 *   <li>Circuit breaker pattern for fault tolerance</li>
 * </ul>
 * 
 * @param <T> the connection type (e.g., Connection for JDBC, Jedis for Redis)
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see VeloctopusDataSource
 * @see ConnectionHealth
 */
public interface VeloctopusConnectionPool<T> {
    
    /**
     * Acquires a connection from the pool asynchronously.
     * 
     * <p>Returns a CompletableFuture that will complete with a connection
     * from the pool. The connection is guaranteed to be valid and ready
     * for use. If no connections are available, the request will wait
     * according to the configured timeout policy.</p>
     * 
     * <p><strong>Threading:</strong> Never blocks calling thread. All pool
     * management happens asynchronously on dedicated thread pools.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;5ms acquisition time under
     * normal conditions. Includes automatic retry logic for transient failures.</p>
     * 
     * <h4>Connection Management:</h4>
     * <ul>
     *   <li>Connections are automatically validated before return</li>
     *   <li>Failed connections are discarded and replaced</li>
     *   <li>Connections auto-return to pool when closed or on timeout</li>
     *   <li>Pool automatically grows/shrinks based on demand</li>
     * </ul>
     * 
     * <h4>Usage Example:</h4>
     * <pre><code>
     * connectionPool.acquireConnection()
     *     .thenApply(connection -&gt; {
     *         try {
     *             return performQuery(connection);
     *         } finally {
     *             // Connection automatically returned to pool
     *             connection.close();
     *         }
     *     })
     *     .exceptionally(throwable -&gt; {
     *         logger.error("Failed to acquire connection", throwable);
     *         return null;
     *     });
     * </code></pre>
     * 
     * @return CompletableFuture containing a valid connection from the pool,
     *         never returns {@code null}
     * @throws IllegalStateException if pool is shutdown or not initialized
     * @since 1.0.0
     */
    CompletableFuture<T> acquireConnection();
    
    /**
     * Acquires a connection with a custom timeout.
     * 
     * <p>Like {@link #acquireConnection()} but with a specific timeout
     * for connection acquisition. Useful for operations with strict
     * timing requirements or to prevent indefinite waiting.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with timeout protection.</p>
     * 
     * <p><strong>Performance:</strong> Timeout is enforced precisely to prevent
     * resource starvation and maintain system responsiveness.</p>
     * 
     * @param timeout maximum time to wait for a connection
     * @return CompletableFuture containing a connection or completing exceptionally
     *         if timeout is exceeded
     * @throws IllegalArgumentException if timeout is null or negative
     * @throws IllegalStateException if pool is shutdown
     * @since 1.0.0
     */
    CompletableFuture<T> acquireConnection(Duration timeout);
    
    /**
     * Returns a connection to the pool for reuse.
     * 
     * <p>Manually returns a connection to the pool. Normally connections
     * are returned automatically when closed, but this method allows
     * explicit return for special use cases.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, can be called from any thread.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation with validation checks.</p>
     * 
     * @param connection the connection to return, must be from this pool
     * @throws IllegalArgumentException if connection is null or not from this pool
     * @since 1.0.0
     */
    void returnConnection(T connection);
    
    /**
     * Gets the current health status of the connection pool.
     * 
     * <p>Returns comprehensive health information including active connections,
     * pool utilization, error rates, and overall pool health status.
     * Used for monitoring and alerting systems.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns current snapshot.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation using cached metrics.</p>
     * 
     * @return current pool health status and metrics
     * @since 1.0.0
     */
    ConnectionPoolHealth getHealth();
    
    /**
     * Gets current pool statistics and metrics.
     * 
     * <p>Returns detailed statistics about pool performance including
     * connection counts, acquisition times, success rates, and resource
     * utilization. Useful for performance monitoring and optimization.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns current snapshot.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation using atomic counters.</p>
     * 
     * @return current pool statistics and performance metrics
     * @since 1.0.0
     */
    ConnectionPoolStats getStats();
    
    /**
     * Validates all connections in the pool.
     * 
     * <p>Asynchronously validates all connections currently in the pool,
     * removing any that are no longer valid. This is typically done
     * automatically but can be triggered manually for maintenance.</p>
     * 
     * <p><strong>Threading:</strong> Async operation that doesn't block callers.</p>
     * 
     * <p><strong>Performance:</strong> May take several seconds for large pools.
     * Validation runs in background without affecting active connections.</p>
     * 
     * @return CompletableFuture that completes when validation finishes,
     *         result indicates number of invalid connections removed
     * @since 1.0.0
     */
    CompletableFuture<Integer> validateAllConnections();
    
    /**
     * Gracefully shuts down the connection pool.
     * 
     * <p>Closes all connections in the pool and prevents new connection
     * acquisitions. Allows currently active connections to complete their
     * operations within a reasonable timeout period.</p>
     * 
     * <p><strong>Threading:</strong> Blocks calling thread until shutdown
     * completes or timeout expires.</p>
     * 
     * <p><strong>Performance:</strong> Shutdown time depends on active
     * connections and configured timeout values.</p>
     * 
     * @return CompletableFuture that completes when shutdown finishes
     * @since 1.0.0
     */
    CompletableFuture<Void> shutdown();
    
    /**
     * Checks if the connection pool is currently active and accepting requests.
     * 
     * <p>Returns false if the pool has been shut down or is in an error state
     * that prevents normal operation.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns current state.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return true if pool is active and operational, false if shut down
     * @since 1.0.0
     */
    boolean isActive();
    
    /**
     * Gets the maximum number of connections this pool can maintain.
     * 
     * <p>Returns the configured maximum pool size. The pool may temporarily
     * exceed this limit during high demand but will return to this size
     * when demand decreases.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable configuration.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return maximum pool size (positive integer)
     * @since 1.0.0
     */
    int getMaxPoolSize();
    
    /**
     * Gets the minimum number of connections this pool maintains.
     * 
     * <p>Returns the configured minimum pool size. The pool will always
     * maintain at least this many connections when active, ensuring
     * quick response times for connection requests.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable configuration.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return minimum pool size (non-negative integer)
     * @since 1.0.0
     */
    int getMinPoolSize();
    
    /**
     * Gets the current number of active connections in the pool.
     * 
     * <p>Returns the count of connections currently in use by active
     * operations. This includes connections that have been acquired
     * but not yet returned to the pool.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns current snapshot.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation using atomic counters.</p>
     * 
     * @return current active connection count (non-negative)
     * @since 1.0.0
     */
    int getActiveConnectionCount();
    
    /**
     * Gets the current number of idle connections in the pool.
     * 
     * <p>Returns the count of connections that are available for immediate
     * use. These connections are validated and ready to be acquired.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns current snapshot.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation using atomic counters.</p>
     * 
     * @return current idle connection count (non-negative)
     * @since 1.0.0
     */
    int getIdleConnectionCount();
}
