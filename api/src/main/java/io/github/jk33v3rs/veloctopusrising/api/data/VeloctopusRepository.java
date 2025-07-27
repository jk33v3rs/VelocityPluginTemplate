package io.github.jk33v3rs.veloctopusrising.api.data;

import java.util.concurrent.CompletableFuture;
import java.util.Optional;
import java.util.List;

/**
 * Base repository interface for Veloctopus Rising data access layer.
 * 
 * <p>This interface defines the contract for all data repository implementations
 * in the Veloctopus Rising system, providing a consistent, async-first approach
 * to data operations with connection pool integration and performance optimization.</p>
 * 
 * <p><strong>Thread Safety:</strong> All implementations must be fully thread-safe
 * and support high-concurrency access without blocking the main thread.</p>
 * 
 * <p><strong>Performance Requirements:</strong> Target &lt;50ms query performance
 * under normal load with automatic connection pool management and caching integration.
 * Supports 1000+ concurrent operations for high-throughput scenarios.</p>
 * 
 * <h3>Repository Pattern Benefits:</h3>
 * <ul>
 *   <li><strong>Abstraction:</strong> Isolates business logic from data persistence details</li>
 *   <li><strong>Testability:</strong> Enables easy mocking and unit testing</li>
 *   <li><strong>Consistency:</strong> Standardized data access patterns across all entities</li>
 *   <li><strong>Performance:</strong> Built-in caching and connection pool optimization</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * // Basic CRUD operations
 * PlayerRepository playerRepo = dataAccessLayer.getPlayerRepository();
 * 
 * // Create new player
 * Player newPlayer = new Player(uuid, "PlayerName");
 * playerRepo.save(newPlayer)
 *     .thenRun(() -&gt; logger.info("Player saved successfully"))
 *     .exceptionally(throwable -&gt; {
 *         logger.error("Failed to save player", throwable);
 *         return null;
 *     });
 * 
 * // Find player by ID
 * playerRepo.findById(playerUuid)
 *     .thenAccept(optionalPlayer -&gt; {
 *         if (optionalPlayer.isPresent()) {
 *             Player player = optionalPlayer.get();
 *             // Use player data
 *         }
 *     });
 * 
 * // Query with criteria
 * playerRepo.findByRankRange("guardian", "deity")
 *     .thenAccept(players -&gt; {
 *         logger.info("Found {} high-rank players", players.size());
 *     });
 * </code></pre>
 * 
 * <h3>Integration Points:</h3>
 * <ul>
 *   <li>Connection pool management for optimal resource utilization</li>
 *   <li>Redis caching layer for frequently accessed data</li>
 *   <li>Transaction support for atomic operations</li>
 *   <li>Migration system for schema evolution</li>
 * </ul>
 * 
 * @param <T> the entity type managed by this repository
 * @param <ID> the type of the entity's primary key
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see VeloctopusConnectionPool
 * @see DataAccessLayer
 */
public interface VeloctopusRepository<T, ID> {
    
    /**
     * Saves an entity to the database asynchronously.
     * 
     * <p>Performs an INSERT operation for new entities or UPDATE for existing ones.
     * The operation is determined by the entity's ID - null/default IDs indicate
     * new entities that need to be inserted.</p>
     * 
     * <p><strong>Threading:</strong> Never blocks calling thread. All database
     * operations happen asynchronously on dedicated thread pools.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;50ms save time under normal
     * conditions. Includes automatic batching for bulk operations and connection
     * pool optimization.</p>
     * 
     * <h4>Transaction Handling:</h4>
     * <ul>
     *   <li>Each save operation is wrapped in a transaction</li>
     *   <li>Automatic rollback on any failure</li>
     *   <li>Connection automatically returned to pool</li>
     *   <li>Optimistic locking support for concurrent updates</li>
     * </ul>
     * 
     * <h4>Caching Integration:</h4>
     * <ul>
     *   <li>Cache entry is updated after successful save</li>
     *   <li>Related cache entries are invalidated as needed</li>
     *   <li>Cache warming for frequently accessed entities</li>
     * </ul>
     * 
     * <h4>Usage Example:</h4>
     * <pre><code>
     * Player player = new Player(uuid, "NewPlayer");
     * player.setRank("bystander");
     * player.setXp(0);
     * 
     * playerRepository.save(player)
     *     .thenCompose(savedPlayer -&gt; {
     *         logger.info("Player {} saved with ID {}", 
     *             savedPlayer.getName(), savedPlayer.getId());
     *         return rankRepository.assignDefaultPermissions(savedPlayer);
     *     })
     *     .exceptionally(throwable -&gt; {
     *         logger.error("Failed to save player", throwable);
     *         return null;
     *     });
     * </code></pre>
     * 
     * @param entity the entity to save, must not be null
     * @return CompletableFuture containing the saved entity with updated ID and timestamps
     * @throws IllegalArgumentException if entity is null or invalid
     * @since 1.0.0
     */
    CompletableFuture<T> save(T entity);
    
    /**
     * Saves multiple entities in a single batch operation.
     * 
     * <p>Performs a bulk INSERT or UPDATE operation for improved performance
     * when dealing with multiple entities. All entities are processed in a
     * single transaction for consistency.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with batch optimization.</p>
     * 
     * <p><strong>Performance:</strong> Significantly faster than individual saves
     * for large datasets. Target &lt;200ms for batches of 100 entities.</p>
     * 
     * @param entities the entities to save, must not be null or empty
     * @return CompletableFuture containing all saved entities with updated IDs
     * @throws IllegalArgumentException if entities collection is null or empty
     * @since 1.0.0
     */
    CompletableFuture<List<T>> saveAll(List<T> entities);
    
    /**
     * Finds an entity by its primary key asynchronously.
     * 
     * <p>Attempts to retrieve the entity from cache first, falling back to
     * database if not found. The retrieved entity is automatically cached
     * for future access.</p>
     * 
     * <p><strong>Threading:</strong> Never blocks calling thread. Cache lookup
     * and database operations happen asynchronously.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;5ms cache hits, &lt;50ms
     * database lookups. Includes connection pool optimization and query caching.</p>
     * 
     * <h4>Caching Strategy:</h4>
     * <ul>
     *   <li>Cache-first lookup with configurable TTL</li>
     *   <li>Cache warming for frequently accessed entities</li>
     *   <li>Cache invalidation on entity updates</li>
     *   <li>Fallback to database if cache is unavailable</li>
     * </ul>
     * 
     * <h4>Usage Example:</h4>
     * <pre><code>
     * UUID playerId = UUID.fromString("12345678-1234-1234-1234-123456789abc");
     * 
     * playerRepository.findById(playerId)
     *     .thenAccept(optionalPlayer -&gt; {
     *         if (optionalPlayer.isPresent()) {
     *             Player player = optionalPlayer.get();
     *             logger.info("Found player: {}", player.getName());
     *             // Use player data
     *         } else {
     *             logger.warn("Player not found: {}", playerId);
     *         }
     *     })
     *     .exceptionally(throwable -&gt; {
     *         logger.error("Error finding player", throwable);
     *         return null;
     *     });
     * </code></pre>
     * 
     * @param id the primary key of the entity to find, must not be null
     * @return CompletableFuture containing Optional with the entity if found,
     *         empty Optional if not found
     * @throws IllegalArgumentException if id is null
     * @since 1.0.0
     */
    CompletableFuture<Optional<T>> findById(ID id);
    
    /**
     * Finds multiple entities by their primary keys.
     * 
     * <p>Performs efficient batch lookup using cache and database operations.
     * Returns entities in the same order as the provided IDs, with null values
     * for IDs that don't exist.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with batch optimization.</p>
     * 
     * <p><strong>Performance:</strong> More efficient than individual findById
     * calls. Target &lt;100ms for batches of 50 IDs.</p>
     * 
     * @param ids the primary keys to look up, must not be null
     * @return CompletableFuture containing list of found entities
     * @throws IllegalArgumentException if ids is null
     * @since 1.0.0
     */
    CompletableFuture<List<T>> findByIds(List<ID> ids);
    
    /**
     * Retrieves all entities of this type from the database.
     * 
     * <p>Returns all entities in the repository. Use with caution on large
     * datasets - consider using pagination methods for better performance.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with streaming support
     * for large result sets.</p>
     * 
     * <p><strong>Performance:</strong> Performance depends on dataset size.
     * Automatic result streaming for datasets larger than 1000 entities.</p>
     * 
     * <h4>Memory Management:</h4>
     * <ul>
     *   <li>Result streaming for large datasets (1000+ entities)</li>
     *   <li>Automatic batching to prevent memory exhaustion</li>
     *   <li>Connection pool optimization for long-running queries</li>
     * </ul>
     * 
     * @return CompletableFuture containing list of all entities
     * @since 1.0.0
     */
    CompletableFuture<List<T>> findAll();
    
    /**
     * Retrieves entities with pagination support.
     * 
     * <p>Returns a specific page of entities for efficient handling of large
     * datasets. Includes sorting support for consistent pagination.</p>
     * 
     * <p><strong>Threading:</strong> Async operation optimized for pagination.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;100ms per page regardless
     * of total dataset size. Uses database-level LIMIT/OFFSET optimization.</p>
     * 
     * @param offset the number of entities to skip (0-based)
     * @param limit the maximum number of entities to return
     * @return CompletableFuture containing the requested page of entities
     * @throws IllegalArgumentException if offset is negative or limit is non-positive
     * @since 1.0.0
     */
    CompletableFuture<List<T>> findAll(int offset, int limit);
    
    /**
     * Counts the total number of entities in the repository.
     * 
     * <p>Returns the count of all entities of this type. Result is cached
     * for improved performance with configurable cache TTL.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with caching.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;10ms for cached results,
     * &lt;50ms for database queries.</p>
     * 
     * @return CompletableFuture containing the total entity count
     * @since 1.0.0
     */
    CompletableFuture<Long> count();
    
    /**
     * Checks if an entity with the given ID exists.
     * 
     * <p>Efficiently checks for entity existence without loading the full
     * entity data. Uses cache when available for optimal performance.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with cache optimization.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;5ms for cached checks,
     * &lt;20ms for database existence queries.</p>
     * 
     * @param id the primary key to check, must not be null
     * @return CompletableFuture containing true if entity exists, false otherwise
     * @throws IllegalArgumentException if id is null
     * @since 1.0.0
     */
    CompletableFuture<Boolean> existsById(ID id);
    
    /**
     * Deletes an entity by its primary key.
     * 
     * <p>Removes the entity from both database and cache. The operation
     * is wrapped in a transaction for consistency.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with transaction support.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;50ms deletion time with
     * automatic cache invalidation.</p>
     * 
     * <h4>Cascade Handling:</h4>
     * <ul>
     *   <li>Related entities are handled according to cascade rules</li>
     *   <li>Cache entries for related entities are invalidated</li>
     *   <li>Referential integrity is maintained</li>
     * </ul>
     * 
     * @param id the primary key of the entity to delete, must not be null
     * @return CompletableFuture that completes when deletion finishes
     * @throws IllegalArgumentException if id is null
     * @since 1.0.0
     */
    CompletableFuture<Void> deleteById(ID id);
    
    /**
     * Deletes an entity instance.
     * 
     * <p>Removes the entity from both database and cache using the entity's
     * primary key. More convenient than deleteById when you have the entity.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with transaction support.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;50ms deletion time.</p>
     * 
     * @param entity the entity to delete, must not be null
     * @return CompletableFuture that completes when deletion finishes
     * @throws IllegalArgumentException if entity is null or has no ID
     * @since 1.0.0
     */
    CompletableFuture<Void> delete(T entity);
    
    /**
     * Deletes multiple entities in a batch operation.
     * 
     * <p>Performs bulk deletion for improved performance when removing
     * multiple entities. All deletions are processed in a single transaction.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with batch optimization.</p>
     * 
     * <p><strong>Performance:</strong> More efficient than individual deletes.
     * Target &lt;200ms for batches of 100 entities.</p>
     * 
     * @param entities the entities to delete, must not be null
     * @return CompletableFuture that completes when all deletions finish
     * @throws IllegalArgumentException if entities is null
     * @since 1.0.0
     */
    CompletableFuture<Void> deleteAll(List<T> entities);
    
    /**
     * Deletes all entities of this type from the repository.
     * 
     * <p>Removes all entities and clears related cache entries. Use with
     * extreme caution as this operation cannot be undone.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with safety checks.</p>
     * 
     * <p><strong>Performance:</strong> Performance depends on dataset size.
     * Includes progress monitoring for large deletions.</p>
     * 
     * @return CompletableFuture that completes when all entities are deleted
     * @since 1.0.0
     */
    CompletableFuture<Void> deleteAll();
    
    /**
     * Flushes any pending changes to the database.
     * 
     * <p>Forces immediate persistence of any cached writes or pending
     * operations. Useful for ensuring data consistency at critical points.</p>
     * 
     * <p><strong>Threading:</strong> Async operation that may block briefly
     * for consistency.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;100ms flush time under
     * normal conditions.</p>
     * 
     * @return CompletableFuture that completes when flush finishes
     * @since 1.0.0
     */
    CompletableFuture<Void> flush();
    
    /**
     * Gets the entity class managed by this repository.
     * 
     * <p>Returns the Class object representing the entity type for reflection
     * and dynamic operations.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable class reference.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return the entity class (never null)
     * @since 1.0.0
     */
    Class<T> getEntityClass();
    
    /**
     * Gets the connection pool used by this repository.
     * 
     * <p>Returns the connection pool for advanced operations or monitoring.
     * Useful for health checks and performance optimization.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns active pool reference.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return the connection pool (never null)
     * @since 1.0.0
     */
    VeloctopusConnectionPool<?> getConnectionPool();
}
