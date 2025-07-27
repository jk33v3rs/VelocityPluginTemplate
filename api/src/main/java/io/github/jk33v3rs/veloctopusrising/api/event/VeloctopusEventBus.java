package io.github.jk33v3rs.veloctopusrising.api.event;

import java.util.concurrent.CompletableFuture;

/**
 * High-performance event bus for Veloctopus Rising async-first event system.
 * 
 * <p>The VeloctopusEventBus is the central nervous system for all communication
 * within the Veloctopus Rising plugin. It provides async-first event processing
 * with guaranteed main thread protection and high throughput capabilities.</p>
 * 
 * <p><strong>Thread Safety:</strong> Fully thread-safe with concurrent event
 * registration, firing, and handling. No blocking operations on main thread.</p>
 * 
 * <p><strong>Performance Characteristics:</strong> Designed for 1000+ events/second
 * throughput with &lt;5ms dispatch time. Uses 8-thread pool matching CPU cores
 * for optimal performance on Zen 5 architecture.</p>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * // Register event handler
 * eventBus.registerHandler(PlayerChatEvent.class, this::handleChat);
 * 
 * // Fire event asynchronously
 * PlayerChatEvent event = new PlayerChatEvent(player, message);
 * CompletableFuture&lt;Void&gt; result = eventBus.fireEventAsync(event);
 * result.thenRun(() -&gt; System.out.println("Chat processed"));
 * </code></pre>
 * 
 * <h3>Integration Points:</h3>
 * <ul>
 *   <li>All Veloctopus modules register event handlers through this bus</li>
 *   <li>Discord Bridge coordinates bot events through event system</li>
 *   <li>Chat System processes all messages through events</li>
 *   <li>XP and Rank systems listen for player activity events</li>
 * </ul>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see VeloctopusEvent
 * @see AsyncEventHandler
 */
public interface VeloctopusEventBus {
    
    /**
     * Registers an event handler for the specified event type.
     * 
     * <p>Registers a handler function to be called whenever events of the
     * specified type are fired. Handlers are called asynchronously in
     * priority order with automatic error handling and timeout protection.</p>
     * 
     * <p><strong>Threading:</strong> Handler registration is thread-safe.
     * Handlers themselves are called on the event bus thread pool.</p>
     * 
     * <p><strong>Performance:</strong> O(1) registration time. Handler lookup
     * during event firing is O(log n) where n is number of registered handlers.</p>
     * 
     * <h4>Usage Example:</h4>
     * <pre><code>
     * eventBus.registerHandler(PlayerChatEvent.class, event -&gt; {
     *     if (event.getMessage().contains("spam")) {
     *         event.setCancelled(true);
     *     }
     * });
     * </code></pre>
     * 
     * @param <T> the event type to handle
     * @param eventType the class of events to handle, must extend VeloctopusEvent
     * @param handler the handler function to call for events of this type,
     *                must be {@code non-null} and thread-safe
     * @throws IllegalArgumentException if eventType or handler is null
     * @since 1.0.0
     * @see #unregisterHandler(Class, AsyncEventHandler)
     */
    <T extends VeloctopusEvent> void registerHandler(Class<T> eventType, AsyncEventHandler<T> handler);
    
    /**
     * Unregisters a previously registered event handler.
     * 
     * <p>Removes the specified handler from the event bus. The handler will
     * no longer receive events of the specified type. If the handler was
     * not registered, this method has no effect.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe unregistration with immediate effect.</p>
     * 
     * <p><strong>Performance:</strong> O(1) unregistration time.</p>
     * 
     * @param <T> the event type the handler was registered for
     * @param eventType the class of events the handler was registered for
     * @param handler the handler function to remove, must be same instance
     *                that was registered
     * @throws IllegalArgumentException if eventType or handler is null
     * @since 1.0.0
     * @see #registerHandler(Class, AsyncEventHandler)
     */
    <T extends VeloctopusEvent> void unregisterHandler(Class<T> eventType, AsyncEventHandler<T> handler);
    
    /**
     * Fires an event asynchronously through the event bus.
     * 
     * <p>Distributes the event to all registered handlers asynchronously.
     * Handlers are called in priority order with automatic timeout protection.
     * The returned CompletableFuture completes when all handlers finish.</p>
     * 
     * <p><strong>Threading:</strong> Never blocks calling thread. Event processing
     * happens on dedicated event bus thread pool (8 threads).</p>
     * 
     * <p><strong>Performance:</strong> Target &lt;5ms dispatch time. Handlers
     * have 30-second timeout protection to prevent system blocking.</p>
     * 
     * <h4>Usage Example:</h4>
     * <pre><code>
     * PlayerJoinEvent event = new PlayerJoinEvent(player);
     * eventBus.fireEventAsync(event)
     *     .thenRun(() -&gt; logger.info("Join event processed"))
     *     .exceptionally(throwable -&gt; {
     *         logger.error("Event processing failed", throwable);
     *         return null;
     *     });
     * </code></pre>
     * 
     * @param event the event to fire, must be {@code non-null}
     * @return CompletableFuture that completes when all handlers finish,
     *         never returns {@code null}
     * @throws IllegalArgumentException if event is null
     * @since 1.0.0
     */
    CompletableFuture<Void> fireEventAsync(VeloctopusEvent event);
    
    /**
     * Gets the current number of registered event handlers.
     * 
     * <p>Returns the total count of all registered event handlers across
     * all event types. Useful for debugging and monitoring the event system.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns current snapshot count.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return total number of registered handlers (non-negative)
     * @since 1.0.0
     */
    int getHandlerCount();
    
    /**
     * Gets the number of registered handlers for a specific event type.
     * 
     * <p>Returns the count of handlers registered for the specified event type.
     * Useful for debugging event distribution and verifying handler registration.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns current snapshot count.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @param eventType the event type to count handlers for
     * @return number of handlers for the specified type (non-negative)
     * @throws IllegalArgumentException if eventType is null
     * @since 1.0.0
     */
    int getHandlerCount(Class<? extends VeloctopusEvent> eventType);
    
    /**
     * Shuts down the event bus and stops all event processing.
     * 
     * <p>Gracefully shuts down the event bus thread pool and stops accepting
     * new events. Allows currently processing events to complete within
     * the specified timeout period.</p>
     * 
     * <p><strong>Threading:</strong> Blocks calling thread until shutdown completes
     * or timeout expires. Safe to call from any thread.</p>
     * 
     * <p><strong>Performance:</strong> Shutdown time depends on currently
     * processing events, up to timeout maximum.</p>
     * 
     * @return CompletableFuture that completes when shutdown finishes,
     *         completes exceptionally if timeout exceeded
     * @since 1.0.0
     */
    CompletableFuture<Void> shutdown();
    
    /**
     * Checks if the event bus is currently accepting events.
     * 
     * <p>Returns {@code false} if the event bus has been shut down and is
     * no longer processing events. Events fired after shutdown are rejected.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns current state.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return {@code true} if event bus is active, {@code false} if shut down
     * @since 1.0.0
     */
    boolean isActive();
}
