package io.github.jk33v3rs.veloctopusrising.api.event;

import java.util.UUID;
import java.time.Instant;

/**
 * Base interface for all Veloctopus Rising events in the centralized event system.
 * 
 * <p>This interface defines the core contract for events within the Veloctopus Rising
 * communication and translation API. All events are designed with async-first principles
 * and include correlation tracking for debugging and performance monitoring.</p>
 * 
 * <p><strong>Thread Safety:</strong> All implementations must be thread-safe and 
 * support concurrent access from multiple threads without blocking operations.</p>
 * 
 * <p><strong>Performance Considerations:</strong> Events should be lightweight objects
 * with minimal object allocation. Event processing targets 1000+ events/second
 * throughput with &lt;5ms dispatch time.</p>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * // Create and fire an event
 * VeloctopusEvent event = new CustomVeloctopusEvent(data);
 * CompletableFuture&lt;Void&gt; result = eventBus.fireEvent(event);
 * result.thenRun(() -&gt; System.out.println("Event processed successfully"));
 * </code></pre>
 * 
 * <h3>Integration Points:</h3>
 * <ul>
 *   <li>EventBus for event distribution and processing</li>
 *   <li>Message Translation Engine for cross-platform events</li>
 *   <li>Discord Bridge for bot coordination events</li>
 *   <li>Database Layer for persistent event logging</li>
 * </ul>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see VeloctopusEventBus
 * @see AsyncEventHandler
 */
public interface VeloctopusEvent {
    
    /**
     * Gets the unique correlation ID for this event instance.
     * 
     * <p>The correlation ID is used for debugging, performance monitoring,
     * and tracing event flow through the system. Each event instance gets
     * a unique UUID assigned at creation time.</p>
     * 
     * <p><strong>Threading:</strong> This method is thread-safe and can be
     * called concurrently from multiple threads.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation, returns pre-computed UUID.</p>
     * 
     * @return {@code non-null} unique correlation ID for debugging and monitoring
     * @since 1.0.0
     */
    UUID getCorrelationId();
    
    /**
     * Gets the timestamp when this event was created.
     * 
     * <p>Returns the exact moment this event instance was created, using
     * system UTC time. This timestamp is used for event ordering, debugging,
     * and performance analysis.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable Instant.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation, returns pre-computed timestamp.</p>
     * 
     * @return {@code non-null} UTC timestamp of event creation
     * @since 1.0.0
     */
    Instant getTimestamp();
    
    /**
     * Gets the priority level for event processing order.
     * 
     * <p>Events with higher priority values are processed before events with
     * lower priority values. Default priority is NORMAL (1000). System events
     * typically use HIGH (2000) or CRITICAL (3000) priority.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable int value.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * <h4>Priority Levels:</h4>
     * <ul>
     *   <li>CRITICAL (3000): System shutdown, critical errors</li>
     *   <li>HIGH (2000): Security events, moderation actions</li>
     *   <li>NORMAL (1000): Standard chat, player events</li>
     *   <li>LOW (500): Background tasks, analytics</li>
     * </ul>
     * 
     * @return priority level (higher values processed first)
     * @since 1.0.0
     */
    int getPriority();
    
    /**
     * Checks if this event can be cancelled by event handlers.
     * 
     * <p>Some events support cancellation to prevent their effects from
     * taking place. Not all events are cancellable - system events and
     * notification-only events typically cannot be cancelled.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable boolean.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return {@code true} if event supports cancellation, {@code false} otherwise
     * @since 1.0.0
     */
    boolean isCancellable();
    
    /**
     * Gets the current cancellation state of this event.
     * 
     * <p>Returns {@code true} if this event has been cancelled by an event handler.
     * Cancelled events should not have their intended effects executed. Only
     * applicable if {@link #isCancellable()} returns {@code true}.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe using atomic operations.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation using AtomicBoolean.</p>
     * 
     * @return {@code true} if event has been cancelled, {@code false} otherwise
     * @since 1.0.0
     */
    boolean isCancelled();
    
    /**
     * Sets the cancellation state of this event.
     * 
     * <p>Cancels or un-cancels this event. Cancelled events should not have
     * their intended effects executed. This method has no effect if the event
     * is not cancellable.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe using atomic operations.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation using AtomicBoolean.</p>
     * 
     * <h4>Usage Example:</h4>
     * <pre><code>
     * if (event.isCancellable() && shouldBlock) {
     *     event.setCancelled(true);
     * }
     * </code></pre>
     * 
     * @param cancelled {@code true} to cancel the event, {@code false} to un-cancel
     * @throws UnsupportedOperationException if event is not cancellable
     * @since 1.0.0
     */
    void setCancelled(boolean cancelled);
    
    /**
     * Gets the source identifier for this event.
     * 
     * <p>Returns a string identifying where this event originated from.
     * This could be a module name, Discord bot ID, player UUID, or
     * system component identifier. Used for debugging and routing.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable String.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * <h4>Common Source Formats:</h4>
     * <ul>
     *   <li>Player events: "player:uuid"</li>
     *   <li>Discord events: "discord:bot_name"</li>
     *   <li>System events: "system:module_name"</li>
     *   <li>External events: "external:service_name"</li>
     * </ul>
     * 
     * @return {@code non-null} source identifier string
     * @since 1.0.0
     */
    String getSource();
    
    /**
     * Priority level constants for event processing order.
     */
    interface Priority {
        /** Critical system events (3000) - shutdown, critical errors */
        int CRITICAL = 3000;
        
        /** High priority events (2000) - security, moderation */
        int HIGH = 2000;
        
        /** Normal priority events (1000) - standard chat, player actions */
        int NORMAL = 1000;
        
        /** Low priority events (500) - background tasks, analytics */
        int LOW = 500;
    }
}
