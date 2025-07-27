package io.github.jk33v3rs.veloctopusrising.api.async;

import java.util.concurrent.CompletableFuture;

/**
 * Async lifecycle pattern interface for VeloctopusRising components.
 *
 * <p>This interface defines the standard lifecycle methods that all async components
 * in VeloctopusRising should implement. It provides a consistent pattern for
 * initialization, execution, and shutdown of system components.</p>
 *
 * <h2>Lifecycle Pattern</h2>
 * <ol>
 *     <li><strong>initializeAsync()</strong> - Set up the component and prepare for execution</li>
 *     <li><strong>executeAsync()</strong> - Perform the main component operations</li>
 *     <li><strong>shutdownAsync()</strong> - Clean up resources and shut down gracefully</li>
 * </ol>
 *
 * <h2>Implementation Requirements</h2>
 * <ul>
 *     <li>All methods must return CompletableFuture instances</li>
 *     <li>Error handling must follow standard async patterns</li>
 *     <li>Resource cleanup must be implemented in shutdownAsync()</li>
 *     <li>Components should be thread-safe</li>
 * </ul>
 *
 * @since 1.0.0
 * @author VeloctopusRising Development Team
 */
public interface AsyncLifecycle {

    /**
     * Initializes the component asynchronously.
     *
     * <p>This method should set up all necessary resources, dependencies,
     * and prepare the component for execution. It should not start the
     * main operation - that's done in executeAsync().</p>
     *
     * @return CompletableFuture that completes with true on successful initialization,
     *         false on failure
     */
    CompletableFuture<Boolean> initializeAsync();

    /**
     * Executes the main component operation asynchronously.
     *
     * <p>This method performs the primary functionality of the component.
     * It should only be called after successful initialization.</p>
     *
     * @return CompletableFuture that completes with true on successful execution,
     *         false on failure
     */
    CompletableFuture<Boolean> executeAsync();

    /**
     * Shuts down the component asynchronously.
     *
     * <p>This method should clean up all resources, close connections,
     * and perform any necessary cleanup operations. After this method
     * completes, the component should be in a state where it can be
     * safely disposed of.</p>
     *
     * @return CompletableFuture that completes with true on successful shutdown,
     *         false on failure
     */
    CompletableFuture<Boolean> shutdownAsync();
}
