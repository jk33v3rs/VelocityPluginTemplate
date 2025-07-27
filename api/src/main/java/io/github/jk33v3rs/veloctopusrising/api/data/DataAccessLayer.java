package io.github.jk33v3rs.veloctopusrising.api.data;

import java.util.concurrent.CompletableFuture;

/**
 * Central data access layer interface for Veloctopus Rising persistence operations.
 * 
 * <p>This interface provides the main entry point for all data access operations
 * in the Veloctopus Rising system, managing repositories, transactions, migrations,
 * and connection pool lifecycle with enterprise-grade reliability and performance.</p>
 * 
 * <p><strong>Thread Safety:</strong> All implementations must be fully thread-safe
 * and support high-concurrency access without blocking the main thread.</p>
 * 
 * <p><strong>Performance Requirements:</strong> Target &lt;30 second startup time
 * with full schema migration and connection pool initialization. Supports 1000+
 * concurrent operations under normal load.</p>
 * 
 * <h3>Data Access Layer Benefits:</h3>
 * <ul>
 *   <li><strong>Centralized Management:</strong> Single point for all data operations</li>
 *   <li><strong>Transaction Support:</strong> Cross-repository transaction management</li>
 *   <li><strong>Migration System:</strong> Automatic schema evolution and versioning</li>
 *   <li><strong>Connection Pooling:</strong> Optimized database connection management</li>
 *   <li><strong>Caching Integration:</strong> Redis cache with database fallback</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * // Initialize data access layer
 * DataAccessLayer dataLayer = new DataAccessLayerImpl(configuration);
 * dataLayer.initialize()
 *     .thenRun(() -&gt; logger.info("Data layer ready"))
 *     .exceptionally(throwable -&gt; {
 *         logger.error("Failed to initialize data layer", throwable);
 *         return null;
 *     });
 * 
 * // Use repositories
 * PlayerRepository playerRepo = dataLayer.getPlayerRepository();
 * RankRepository rankRepo = dataLayer.getRankRepository();
 * 
 * // Transaction example
 * dataLayer.executeInTransaction(() -&gt; {
 *     return playerRepo.save(player)
 *         .thenCompose(savedPlayer -&gt; rankRepo.assignRank(savedPlayer, newRank));
 * }).thenAccept(result -&gt; {
 *     logger.info("Player and rank updated successfully");
 * });
 * </code></pre>
 * 
 * <h3>Integration Points:</h3>
 * <ul>
 *   <li>Connection pool management for MariaDB and Redis</li>
 *   <li>Schema migration system for database evolution</li>
 *   <li>Repository factory for type-safe data access</li>
 *   <li>Transaction management for atomic operations</li>
 * </ul>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see VeloctopusRepository
 * @see VeloctopusConnectionPool
 */
public interface DataAccessLayer {
    
    /**
     * Initializes the data access layer asynchronously.
     * 
     * <p>Performs complete initialization including connection pool setup,
     * schema migration, cache warming, and repository initialization.
     * This method must be called before any other operations.</p>
     * 
     * <p><strong>Threading:</strong> Never blocks calling thread. All initialization
     * happens asynchronously with progress monitoring.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;30 second initialization time
     * including full schema migration and connection pool warmup.</p>
     * 
     * <h4>Initialization Sequence:</h4>
     * <ol>
     *   <li>Database connection pool initialization</li>
     *   <li>Redis cache connection and health check</li>
     *   <li>Schema migration and validation</li>
     *   <li>Repository factory setup</li>
     *   <li>Cache warming for critical data</li>
     * </ol>
     * 
     * <h4>Error Handling:</h4>
     * <ul>
     *   <li>Automatic retry logic for transient failures</li>
     *   <li>Graceful degradation when Redis is unavailable</li>
     *   <li>Detailed error reporting for troubleshooting</li>
     *   <li>Health monitoring setup for ongoing operations</li>
     * </ul>
     * 
     * <h4>Usage Example:</h4>
     * <pre><code>
     * DataAccessLayer dataLayer = new DataAccessLayerImpl(config);
     * 
     * dataLayer.initialize()
     *     .thenRun(() -&gt; {
     *         logger.info("Data access layer initialized successfully");
     *         // Start application services
     *     })
     *     .exceptionally(throwable -&gt; {
     *         logger.error("Critical: Failed to initialize data layer", throwable);
     *         // Shutdown application gracefully
     *         return null;
     *     });
     * </code></pre>
     * 
     * @return CompletableFuture that completes when initialization finishes
     * @throws IllegalStateException if already initialized
     * @since 1.0.0
     */
    CompletableFuture<Void> initialize();
    
    /**
     * Gracefully shuts down the data access layer.
     * 
     * <p>Closes all repositories, connection pools, and cache connections.
     * Ensures all pending operations complete before shutdown.</p>
     * 
     * <p><strong>Threading:</strong> May block briefly to ensure clean shutdown.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;10 second shutdown time
     * with graceful completion of pending operations.</p>
     * 
     * @return CompletableFuture that completes when shutdown finishes
     * @since 1.0.0
     */
    CompletableFuture<Void> shutdown();
    
    /**
     * Checks if the data access layer is currently active and operational.
     * 
     * <p>Returns false if not initialized, shutting down, or in an error state
     * that prevents normal operation.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns current state.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return true if layer is active and operational, false otherwise
     * @since 1.0.0
     */
    boolean isActive();
    
    /**
     * Executes a function within a database transaction.
     * 
     * <p>Provides transaction management for operations that span multiple
     * repositories or require atomic consistency. Automatically handles
     * commit, rollback, and connection management.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with transaction isolation.</p>
     * 
     * <p><strong>Performance:</strong> Transaction overhead typically &lt;10ms
     * for simple operations, varies with complexity.</p>
     * 
     * <h4>Transaction Properties:</h4>
     * <ul>
     *   <li>ACID compliance with configurable isolation levels</li>
     *   <li>Automatic rollback on any exception</li>
     *   <li>Connection pool integration for resource management</li>
     *   <li>Deadlock detection and retry logic</li>
     * </ul>
     * 
     * <h4>Usage Example:</h4>
     * <pre><code>
     * // Transfer XP between players atomically
     * dataLayer.executeInTransaction(() -&gt; {
     *     return playerRepo.findById(fromPlayer)
     *         .thenCompose(fromOpt -&gt; playerRepo.findById(toPlayer)
     *             .thenCompose(toOpt -&gt; {
     *                 if (fromOpt.isPresent() &amp;&amp; toOpt.isPresent()) {
     *                     Player from = fromOpt.get();
     *                     Player to = toOpt.get();
     *                     
     *                     from.setXp(from.getXp() - amount);
     *                     to.setXp(to.getXp() + amount);
     *                     
     *                     return playerRepo.save(from)
     *                         .thenCompose(saved -&gt; playerRepo.save(to));
     *                 }
     *                 throw new IllegalArgumentException("Player not found");
     *             }));
     * }).thenAccept(result -&gt; {
     *     logger.info("XP transfer completed successfully");
     * }).exceptionally(throwable -&gt; {
     *     logger.error("XP transfer failed, rolled back", throwable);
     *     return null;
     * });
     * </code></pre>
     * 
     * @param <T> the return type of the transaction function
     * @param transactionFunction the function to execute within the transaction
     * @return CompletableFuture containing the function result
     * @throws IllegalArgumentException if transactionFunction is null
     * @since 1.0.0
     */
    <T> CompletableFuture<T> executeInTransaction(TransactionFunction<T> transactionFunction);
    
    /**
     * Runs database schema migrations asynchronously.
     * 
     * <p>Applies all pending schema migrations to bring the database up to
     * the current version. Includes backup creation and rollback capability.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with progress monitoring.</p>
     * 
     * <p><strong>Performance:</strong> Time varies with migration complexity,
     * typically &lt;60 seconds for standard operations.</p>
     * 
     * <h4>Migration Features:</h4>
     * <ul>
     *   <li>Automatic backup before migration execution</li>
     *   <li>Version tracking in schema_migrations table</li>
     *   <li>Rollback capability for failed migrations</li>
     *   <li>Progress monitoring for long-running migrations</li>
     * </ul>
     * 
     * @return CompletableFuture that completes when migrations finish,
     *         result indicates number of migrations applied
     * @since 1.0.0
     */
    CompletableFuture<Integer> runMigrations();
    
    /**
     * Gets the MariaDB connection pool for direct access.
     * 
     * <p>Provides access to the MariaDB connection pool for advanced operations
     * or monitoring. Use with caution as direct pool access bypasses
     * repository abstractions.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns active pool reference.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return the MariaDB connection pool (never null after initialization)
     * @throws IllegalStateException if not initialized
     * @since 1.0.0
     */
    VeloctopusConnectionPool<?> getMariaDbPool();
    
    /**
     * Gets the Redis connection pool for direct access.
     * 
     * <p>Provides access to the Redis connection pool for advanced caching
     * operations or monitoring. Returns null if Redis is unavailable.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns active pool reference.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return the Redis connection pool, or null if Redis unavailable
     * @since 1.0.0
     */
    VeloctopusConnectionPool<?> getRedisPool();
    
    /**
     * Gets a repository instance for the specified entity type.
     * 
     * <p>Returns a type-safe repository instance for the given entity class.
     * Repositories are cached and reused for performance.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns cached repository.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation with internal caching.</p>
     * 
     * <h4>Supported Entity Types:</h4>
     * <ul>
     *   <li>Player entities for user data management</li>
     *   <li>Rank entities for permission and progression</li>
     *   <li>Whitelist entities for access control</li>
     *   <li>Chat entities for message history</li>
     *   <li>Achievement entities for XP system</li>
     * </ul>
     * 
     * <h4>Usage Example:</h4>
     * <pre><code>
     * // Get typed repository
     * VeloctopusRepository&lt;Player, UUID&gt; playerRepo = 
     *     dataLayer.getRepository(Player.class);
     * 
     * // Use repository
     * playerRepo.findById(playerId)
     *     .thenAccept(player -&gt; {
     *         // Handle player data
     *     });
     * </code></pre>
     * 
     * @param <T> the entity type
     * @param <ID> the entity ID type
     * @param entityClass the entity class to get repository for
     * @return repository instance for the entity type (never null)
     * @throws IllegalArgumentException if entityClass is null or unsupported
     * @throws IllegalStateException if not initialized
     * @since 1.0.0
     */
    <T, ID> VeloctopusRepository<T, ID> getRepository(Class<T> entityClass);
    
    /**
     * Functional interface for transaction operations.
     * 
     * <p>Represents a function that executes within a database transaction,
     * returning a CompletableFuture for async operations.</p>
     * 
     * @param <T> the return type of the transaction function
     * @since 1.0.0
     */
    @FunctionalInterface
    interface TransactionFunction<T> {
        /**
         * Executes the transaction function.
         * 
         * @return CompletableFuture containing the function result
         * @throws Exception if the transaction should be rolled back
         */
        CompletableFuture<T> execute() throws Exception;
    }
}
