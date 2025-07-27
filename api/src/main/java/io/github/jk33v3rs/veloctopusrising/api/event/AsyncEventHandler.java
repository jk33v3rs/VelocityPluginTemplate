package io.github.jk33v3rs.veloctopusrising.api.event;

import java.util.concurrent.CompletableFuture;

/**
 * Functional interface for asynchronous event handling in Veloctopus Rising.
 * 
 * <p>This interface defines the contract for event handlers that process
 * VeloctopusEvent instances asynchronously. All handlers must be thread-safe
 * and should not perform blocking operations.</p>
 * 
 * <p><strong>Thread Safety:</strong> Implementations must be thread-safe as
 * handlers may be called concurrently from multiple threads in the event
 * bus thread pool.</p>
 * 
 * <p><strong>Performance Considerations:</strong> Handlers should complete
 * within 30 seconds (default timeout). Long-running operations should be
 * delegated to separate thread pools to maintain event bus performance.</p>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * AsyncEventHandler&lt;PlayerChatEvent&gt; chatHandler = event -&gt; {
 *     return CompletableFuture.runAsync(() -&gt; {
 *         // Process chat message asynchronously
 *         processMessage(event.getMessage());
 *     });
 * };
 * 
 * eventBus.registerHandler(PlayerChatEvent.class, chatHandler);
 * </code></pre>
 * 
 * <h3>Integration Points:</h3>
 * <ul>
 *   <li>VeloctopusEventBus for handler registration and event processing</li>
 *   <li>All plugin modules implement handlers for relevant events</li>
 *   <li>Discord Bridge uses handlers for bot coordination</li>
 *   <li>Chat, XP, and Rank systems implement event-driven architecture</li>
 * </ul>
 * 
 * @param <T> the type of event this handler processes, must extend VeloctopusEvent
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see VeloctopusEvent
 * @see VeloctopusEventBus
 */
@FunctionalInterface
public interface AsyncEventHandler<T extends VeloctopusEvent> {
    
    /**
     * Handles the specified event asynchronously.
     * 
     * <p>This method is called by the event bus when an event of the appropriate
     * type is fired. The implementation should process the event asynchronously
     * and return a CompletableFuture that completes when processing finishes.</p>
     * 
     * <p><strong>Threading:</strong> This method may be called from any thread
     * in the event bus thread pool. Implementations must be thread-safe.</p>
     * 
     * <p><strong>Performance:</strong> Should complete within 30 seconds to avoid
     * timeout. Complex operations should use separate thread pools.</p>
     * 
     * <h4>Best Practices:</h4>
     * <ul>
     *   <li>Return immediately with CompletableFuture for async processing</li>
     *   <li>Handle exceptions gracefully and include in returned future</li>
     *   <li>Use event.isCancelled() checks for cancellable events</li>
     *   <li>Include correlation IDs in log messages for debugging</li>
     * </ul>
     * 
     * <h4>Usage Example:</h4>
     * <pre><code>
     * public CompletableFuture&lt;Void&gt; handleEvent(PlayerChatEvent event) {
     *     if (event.isCancelled()) {
     *         return CompletableFuture.completedFuture(null);
     *     }
     *     
     *     return CompletableFuture.runAsync(() -&gt; {
     *         // Process chat message
     *         String message = event.getMessage();
     *         Player player = event.getPlayer();
     *         
     *         // Log with correlation ID for debugging
     *         logger.info("Processing chat from {} [{}]", 
     *                    player.getName(), event.getCorrelationId());
     *         
     *         // Perform actual processing
     *         chatProcessor.processMessage(player, message);
     *     }, executorService);
     * }
     * </code></pre>
     * 
     * @param event the event to handle, guaranteed to be {@code non-null}
     * @return CompletableFuture that completes when event processing finishes,
     *         must not return {@code null}. If processing fails, the future
     *         should complete exceptionally with appropriate exception.
     * @throws RuntimeException if immediate processing error occurs (rare)
     * @since 1.0.0
     */
    CompletableFuture<Void> handleEvent(T event);
}
