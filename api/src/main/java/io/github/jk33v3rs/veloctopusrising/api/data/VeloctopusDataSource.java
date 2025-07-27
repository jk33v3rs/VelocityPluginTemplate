package io.github.jk33v3rs.veloctopusrising.api.data;

/**
 * Marker interface for Veloctopus Rising data sources.
 * 
 * <p>This interface serves as a common base for all data source implementations
 * in the Veloctopus Rising system, providing type safety and integration points
 * for the data access layer.</p>
 * 
 * <p><strong>Thread Safety:</strong> Implementations must be fully thread-safe
 * for concurrent access across multiple threads.</p>
 * 
 * <p><strong>Performance:</strong> Implementations should provide high-performance
 * data access with minimal overhead and optimal resource utilization.</p>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * VeloctopusDataSource dataSource = dataSourceProvider.getDataSource("main");
 * // Use data source for connection pool creation or direct access
 * </code></pre>
 * 
 * <h3>Integration Points:</h3>
 * <ul>
 *   <li>Connection pool implementations for resource management</li>
 *   <li>Data access layer for repository pattern support</li>
 *   <li>Configuration system for data source setup</li>
 *   <li>Health monitoring for operational status tracking</li>
 * </ul>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see VeloctopusConnectionPool
 */
public interface VeloctopusDataSource {
    
    /**
     * Gets the unique identifier for this data source.
     * 
     * <p>Returns a unique name or identifier that can be used to distinguish
     * this data source from others in the system. This is typically used
     * for configuration, logging, and monitoring purposes.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable identifier.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return unique data source identifier (never null or empty)
     * @since 1.0.0
     */
    String getId();
    
    /**
     * Gets the type of this data source.
     * 
     * <p>Returns a string indicating the type of data source (e.g., "mariadb",
     * "redis", "memory"). This information is useful for type-specific
     * handling and configuration.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable type.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return data source type (never null or empty)
     * @since 1.0.0
     */
    String getType();
    
    /**
     * Checks if this data source is currently available and operational.
     * 
     * <p>Returns true if the data source is ready for use, false if it's
     * offline, unreachable, or in an error state.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, may perform quick health check.</p>
     * 
     * <p><strong>Performance:</strong> Should complete quickly, avoid blocking operations.</p>
     * 
     * @return true if data source is available, false otherwise
     * @since 1.0.0
     */
    boolean isAvailable();
}
