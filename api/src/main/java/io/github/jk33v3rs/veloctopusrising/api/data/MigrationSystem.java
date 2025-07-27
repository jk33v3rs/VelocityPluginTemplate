package io.github.jk33v3rs.veloctopusrising.api.data;

import java.util.concurrent.CompletableFuture;
import java.util.List;

/**
 * Database schema migration system for Veloctopus Rising data layer.
 * 
 * <p>This interface defines the contract for database schema evolution and versioning
 * in the Veloctopus Rising system, providing automated migration capabilities with
 * backup creation, rollback support, and comprehensive error handling.</p>
 * 
 * <p><strong>Thread Safety:</strong> All implementations must be fully thread-safe
 * and support safe execution during system startup and maintenance.</p>
 * 
 * <p><strong>Performance Requirements:</strong> Target &lt;60 second execution time
 * for standard migrations with progress monitoring and parallel execution where safe.
 * Supports migration validation and rollback for enterprise reliability.</p>
 * 
 * <h3>Migration System Benefits:</h3>
 * <ul>
 *   <li><strong>Version Control:</strong> Tracks applied migrations with timestamps</li>
 *   <li><strong>Safety Features:</strong> Automatic backup before migration execution</li>
 *   <li><strong>Rollback Support:</strong> Ability to revert failed or problematic migrations</li>
 *   <li><strong>Progress Monitoring:</strong> Real-time progress tracking for long operations</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * // Initialize migration system
 * MigrationSystem migrationSystem = new MigrationSystemImpl(dataSource);
 * 
 * // Check current schema version
 * migrationSystem.getCurrentVersion()
 *     .thenAccept(version -&gt; {
 *         logger.info("Current schema version: {}", version);
 *     });
 * 
 * // Apply pending migrations
 * migrationSystem.migrate()
 *     .thenAccept(appliedCount -&gt; {
 *         logger.info("Applied {} migrations successfully", appliedCount);
 *     })
 *     .exceptionally(throwable -&gt; {
 *         logger.error("Migration failed", throwable);
 *         return null;
 *     });
 * 
 * // Rollback last migration if needed
 * migrationSystem.rollbackLast()
 *     .thenRun(() -&gt; logger.info("Rollback completed"));
 * </code></pre>
 * 
 * <h3>Integration Points:</h3>
 * <ul>
 *   <li>Data access layer for automatic migration during startup</li>
 *   <li>Backup system for pre-migration database snapshots</li>
 *   <li>Monitoring system for migration progress and error reporting</li>
 *   <li>Configuration system for migration settings and validation</li>
 * </ul>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see DataAccessLayer
 * @see VeloctopusDataSource
 */
public interface MigrationSystem {
    
    /**
     * Represents a single database migration with metadata.
     * 
     * <p>Contains all information needed to execute, track, and potentially
     * rollback a database schema change.</p>
     * 
     * @since 1.0.0
     */
    interface Migration {
        
        /**
         * Gets the unique version identifier for this migration.
         * 
         * @return migration version (e.g., "001", "1.2.3", "20250724_001")
         */
        String getVersion();
        
        /**
         * Gets a human-readable description of this migration.
         * 
         * @return migration description (never null)
         */
        String getDescription();
        
        /**
         * Gets the SQL statements to execute for this migration.
         * 
         * @return list of SQL statements in execution order
         */
        List<String> getUpStatements();
        
        /**
         * Gets the SQL statements to rollback this migration.
         * 
         * @return list of rollback SQL statements, or empty if not supported
         */
        List<String> getDownStatements();
        
        /**
         * Checks if this migration supports rollback.
         * 
         * @return true if rollback is supported, false otherwise
         */
        boolean isRollbackSupported();
        
        /**
         * Gets the estimated execution time for this migration.
         * 
         * @return estimated duration in milliseconds, or -1 if unknown
         */
        long getEstimatedDurationMs();
    }
    
    /**
     * Applies all pending migrations to the database.
     * 
     * <p>Executes all migrations that haven't been applied yet, in version order.
     * Creates automatic backup before execution and tracks progress for monitoring.</p>
     * 
     * <p><strong>Threading:</strong> Never blocks calling thread. All migration
     * operations happen asynchronously with progress callbacks.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;60 second execution time for
     * standard migrations. Progress monitoring available for long operations.</p>
     * 
     * <h4>Migration Process:</h4>
     * <ol>
     *   <li>Create automatic backup of current database state</li>
     *   <li>Identify all pending migrations in version order</li>
     *   <li>Execute each migration within its own transaction</li>
     *   <li>Update migration tracking table after each success</li>
     *   <li>Rollback and restore on any failure</li>
     * </ol>
     * 
     * <h4>Error Handling:</h4>
     * <ul>
     *   <li>Automatic rollback of failed migration</li>
     *   <li>Database restoration from backup on critical failures</li>
     *   <li>Detailed error logging with SQL statement context</li>
     *   <li>Health check validation after migration completion</li>
     * </ul>
     * 
     * <h4>Usage Example:</h4>
     * <pre><code>
     * migrationSystem.migrate()
     *     .thenAccept(count -&gt; {
     *         if (count &gt; 0) {
     *             logger.info("Successfully applied {} migrations", count);
     *         } else {
     *             logger.info("Database schema is up to date");
     *         }
     *     })
     *     .exceptionally(throwable -&gt; {
     *         logger.error("Migration failed - database restored to previous state", throwable);
     *         // Consider application shutdown for critical failures
     *         return null;
     *     });
     * </code></pre>
     * 
     * @return CompletableFuture containing the number of migrations applied
     * @since 1.0.0
     */
    CompletableFuture<Integer> migrate();
    
    /**
     * Applies pending migrations up to a specific version.
     * 
     * <p>Like {@link #migrate()} but stops at the specified target version.
     * Useful for controlled migration in staging environments.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with version validation.</p>
     * 
     * <p><strong>Performance:</strong> Execution time depends on number of
     * migrations between current and target version.</p>
     * 
     * @param targetVersion the version to migrate to (inclusive)
     * @return CompletableFuture containing the number of migrations applied
     * @throws IllegalArgumentException if targetVersion is null or invalid
     * @since 1.0.0
     */
    CompletableFuture<Integer> migrateTo(String targetVersion);
    
    /**
     * Rolls back the last applied migration.
     * 
     * <p>Reverts the most recently applied migration if it supports rollback.
     * Creates backup before rollback execution for safety.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with safety validation.</p>
     * 
     * <p><strong>Performance:</strong> Rollback time typically faster than
     * original migration, target &lt;30 seconds for standard operations.</p>
     * 
     * <h4>Rollback Process:</h4>
     * <ol>
     *   <li>Verify last migration supports rollback</li>
     *   <li>Create backup of current database state</li>
     *   <li>Execute rollback SQL statements in reverse order</li>
     *   <li>Update migration tracking table</li>
     *   <li>Validate database consistency after rollback</li>
     * </ol>
     * 
     * @return CompletableFuture that completes when rollback finishes
     * @throws IllegalStateException if no migrations to rollback or rollback not supported
     * @since 1.0.0
     */
    CompletableFuture<Void> rollbackLast();
    
    /**
     * Rolls back migrations to a specific version.
     * 
     * <p>Reverts all migrations after the specified target version.
     * All affected migrations must support rollback.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with extensive validation.</p>
     * 
     * <p><strong>Performance:</strong> Time depends on number of migrations
     * to rollback and their complexity.</p>
     * 
     * @param targetVersion the version to rollback to (exclusive)
     * @return CompletableFuture that completes when rollback finishes
     * @throws IllegalArgumentException if targetVersion is invalid
     * @throws IllegalStateException if any required migration doesn't support rollback
     * @since 1.0.0
     */
    CompletableFuture<Void> rollbackTo(String targetVersion);
    
    /**
     * Gets the current database schema version.
     * 
     * <p>Returns the version of the last successfully applied migration.
     * Returns "0" or empty string if no migrations have been applied.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with caching.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;10ms with cached results.</p>
     * 
     * @return CompletableFuture containing the current schema version
     * @since 1.0.0
     */
    CompletableFuture<String> getCurrentVersion();
    
    /**
     * Gets the latest available migration version.
     * 
     * <p>Returns the highest version number among all available migrations.
     * Used to determine if the database is up to date.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with caching.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;5ms with migration registry caching.</p>
     * 
     * @return CompletableFuture containing the latest available version
     * @since 1.0.0
     */
    CompletableFuture<String> getLatestVersion();
    
    /**
     * Gets all available migrations in version order.
     * 
     * <p>Returns a complete list of all migrations known to the system,
     * including both applied and pending migrations.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with registry lookup.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;20ms for typical migration counts.</p>
     * 
     * @return CompletableFuture containing list of all available migrations
     * @since 1.0.0
     */
    CompletableFuture<List<Migration>> getAllMigrations();
    
    /**
     * Gets all pending migrations that need to be applied.
     * 
     * <p>Returns migrations that are available but haven't been applied to
     * the database yet, in the order they should be executed.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with database lookup.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;30ms including database query.</p>
     * 
     * @return CompletableFuture containing list of pending migrations
     * @since 1.0.0
     */
    CompletableFuture<List<Migration>> getPendingMigrations();
    
    /**
     * Gets the history of all applied migrations.
     * 
     * <p>Returns a record of all migrations that have been successfully
     * applied to the database, including timestamps and execution details.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with database query.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;50ms including full history query.</p>
     * 
     * @return CompletableFuture containing migration history
     * @since 1.0.0
     */
    CompletableFuture<List<MigrationRecord>> getMigrationHistory();
    
    /**
     * Validates the current database schema against expected structure.
     * 
     * <p>Performs comprehensive validation of database tables, columns,
     * indexes, and constraints to ensure schema integrity.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with thorough checking.</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;10 seconds for complete validation.</p>
     * 
     * @return CompletableFuture containing validation results
     * @since 1.0.0
     */
    CompletableFuture<ValidationResult> validateSchema();
    
    /**
     * Creates a backup of the current database state.
     * 
     * <p>Generates a complete backup that can be used for restoration
     * in case of migration failures or data corruption.</p>
     * 
     * <p><strong>Threading:</strong> Async operation with progress monitoring.</p>
     * 
     * <p><strong>Performance:</strong> Time depends on database size,
     * typically &lt;2 minutes for databases under 1GB.</p>
     * 
     * @return CompletableFuture containing the backup file path or identifier
     * @since 1.0.0
     */
    CompletableFuture<String> createBackup();
    
    /**
     * Represents a record of an applied migration.
     * 
     * @since 1.0.0
     */
    interface MigrationRecord {
        
        /**
         * Gets the migration version.
         * 
         * @return migration version
         */
        String getVersion();
        
        /**
         * Gets the migration description.
         * 
         * @return migration description
         */
        String getDescription();
        
        /**
         * Gets when this migration was applied.
         * 
         * @return application timestamp
         */
        java.time.Instant getAppliedAt();
        
        /**
         * Gets how long the migration took to execute.
         * 
         * @return execution duration in milliseconds
         */
        long getExecutionTimeMs();
        
        /**
         * Gets any checksum or hash of the migration.
         * 
         * @return migration checksum for integrity verification
         */
        String getChecksum();
    }
    
    /**
     * Represents the result of schema validation.
     * 
     * @since 1.0.0
     */
    interface ValidationResult {
        
        /**
         * Checks if the schema validation passed.
         * 
         * @return true if schema is valid, false if issues found
         */
        boolean isValid();
        
        /**
         * Gets any validation errors found.
         * 
         * @return list of validation errors, empty if valid
         */
        List<String> getErrors();
        
        /**
         * Gets any validation warnings.
         * 
         * @return list of validation warnings, empty if none
         */
        List<String> getWarnings();
        
        /**
         * Gets the validation timestamp.
         * 
         * @return when validation was performed
         */
        java.time.Instant getValidatedAt();
    }
}
