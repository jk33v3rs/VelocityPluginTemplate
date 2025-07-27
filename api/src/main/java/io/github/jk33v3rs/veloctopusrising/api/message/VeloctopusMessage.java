package io.github.jk33v3rs.veloctopusrising.api.message;

import java.time.Instant;
import java.util.UUID;
import java.util.Map;
import java.util.Optional;

/**
 * Universal message container for cross-platform communication in Veloctopus Rising.
 * 
 * <p>The VeloctopusMessage represents a unified message format that can be translated
 * between different communication platforms (Minecraft, Discord, Matrix). It supports
 * rich text formatting, attachments, metadata, and platform-specific features while
 * maintaining compatibility across all supported platforms.</p>
 * 
 * <p><strong>Thread Safety:</strong> Immutable by design - all instances are thread-safe
 * and can be safely passed between threads without synchronization concerns.</p>
 * 
 * <p><strong>Performance Considerations:</strong> Lightweight object designed for high
 * throughput message processing. Target &lt;200ms translation time between platforms
 * with support for caching frequently translated content.</p>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * // Create a message from Minecraft chat
 * VeloctopusMessage message = VeloctopusMessage.builder()
 *     .content("Hello world!")
 *     .sender("PlayerName")
 *     .source(MessageSource.MINECRAFT)
 *     .channel("global")
 *     .build();
 * 
 * // Translate to Discord format
 * DiscordMessage discordMsg = translator.translateToDiscord(message);
 * </code></pre>
 * 
 * <h3>Integration Points:</h3>
 * <ul>
 *   <li>Message Translation Engine for cross-platform conversion</li>
 *   <li>Discord Bridge for bot message handling</li>
 *   <li>Chat System for unified message processing</li>
 *   <li>Redis Cache for message storage and retrieval</li>
 * </ul>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see MessageTranslationEngine
 * @see MessageSource
 */
public interface VeloctopusMessage {
    
    /**
     * Gets the unique correlation ID for this message.
     * 
     * <p>Each message instance receives a unique UUID for tracking through
     * the translation pipeline, debugging, and performance monitoring.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable UUID.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return {@code non-null} unique correlation ID for debugging and monitoring
     * @since 1.0.0
     */
    UUID getCorrelationId();
    
    /**
     * Gets the timestamp when this message was created.
     * 
     * <p>Returns the exact moment this message was created, used for ordering,
     * caching, and temporal correlation across platforms.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable Instant.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return {@code non-null} UTC timestamp of message creation
     * @since 1.0.0
     */
    Instant getTimestamp();
    
    /**
     * Gets the primary text content of this message.
     * 
     * <p>Returns the main text content in a platform-neutral format.
     * May contain formatting codes that can be translated to platform-specific
     * rich text representations.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable String.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return {@code non-null} message content (may be empty string)
     * @since 1.0.0
     */
    String getContent();
    
    /**
     * Gets the sender identifier for this message.
     * 
     * <p>Returns the identifier of who sent this message. Format varies by
     * source platform - Minecraft uses player names/UUIDs, Discord uses
     * user IDs, system messages use module names.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable String.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return {@code non-null} sender identifier
     * @since 1.0.0
     */
    String getSender();
    
    /**
     * Gets the source platform for this message.
     * 
     * <p>Identifies which platform this message originated from, used for
     * translation routing and platform-specific handling.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable enum.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return {@code non-null} source platform identifier
     * @since 1.0.0
     */
    MessageSource getSource();
    
    /**
     * Gets the target platform for this message translation.
     * 
     * <p>Specifies which platform this message should be translated to.
     * Optional - if not present, message can be translated to any platform.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable Optional.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return Optional target platform, empty if message is platform-agnostic
     * @since 1.0.0
     */
    Optional<MessageSource> getTarget();
    
    /**
     * Gets the channel identifier for this message.
     * 
     * <p>Returns the channel/room/destination where this message should be
     * delivered. Format is platform-specific but typically represents a
     * logical communication channel.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable String.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return {@code non-null} channel identifier
     * @since 1.0.0
     */
    String getChannel();
    
    /**
     * Gets the message type classification.
     * 
     * <p>Classifies the message for appropriate handling - chat messages,
     * system notifications, embedded content, etc. Used for routing and
     * formatting decisions.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable enum.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return {@code non-null} message type classification
     * @since 1.0.0
     */
    MessageType getType();
    
    /**
     * Gets the priority level for message processing.
     * 
     * <p>Higher priority messages are processed before lower priority messages.
     * Used for ensuring important notifications (moderation, security) are
     * delivered promptly.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable int.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * @return priority level (higher values processed first)
     * @since 1.0.0
     */
    int getPriority();
    
    /**
     * Gets additional metadata associated with this message.
     * 
     * <p>Returns a map of key-value pairs containing platform-specific
     * metadata, formatting hints, or additional context information
     * needed for proper message translation and rendering.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe, returns immutable Map.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * <h4>Common Metadata Keys:</h4>
     * <ul>
     *   <li>"player_uuid" - Minecraft player UUID</li>
     *   <li>"rank_color" - Text color for rank formatting</li>
     *   <li>"embed_color" - Discord embed color</li>
     *   <li>"avatar_url" - User avatar image URL</li>
     * </ul>
     * 
     * @return {@code non-null} immutable metadata map (may be empty)
     * @since 1.0.0
     */
    Map<String, Object> getMetadata();
    
    /**
     * Gets a specific metadata value with type safety.
     * 
     * <p>Retrieves a metadata value and casts it to the specified type.
     * Returns empty Optional if key doesn't exist or value cannot be cast.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe operation.</p>
     * 
     * <p><strong>Performance:</strong> O(1) operation.</p>
     * 
     * <h4>Usage Example:</h4>
     * <pre><code>
     * Optional&lt;String&gt; playerUuid = message.getMetadata("player_uuid", String.class);
     * Optional&lt;Integer&gt; embedColor = message.getMetadata("embed_color", Integer.class);
     * </code></pre>
     * 
     * @param <T> the expected type of the metadata value
     * @param key the metadata key to retrieve
     * @param type the expected class type for safe casting
     * @return Optional containing the typed value, empty if not found or wrong type
     * @throws IllegalArgumentException if key or type is null
     * @since 1.0.0
     */
    <T> Optional<T> getMetadata(String key, Class<T> type);
    
    /**
     * Creates a new message builder for constructing VeloctopusMessage instances.
     * 
     * <p>Returns a builder that provides a fluent API for constructing
     * VeloctopusMessage instances with validation and default values.</p>
     * 
     * <p><strong>Threading:</strong> Builders are not thread-safe and should
     * not be shared between threads.</p>
     * 
     * <p><strong>Performance:</strong> Lightweight builder creation, O(1).</p>
     * 
     * @return new message builder instance
     * @since 1.0.0
     */
    static VeloctopusMessageBuilder builder() {
        // Implementation will be provided by concrete class
        throw new UnsupportedOperationException("Builder implementation required");
    }
    
    /**
     * Message priority constants for processing order.
     */
    interface Priority {
        /** Critical system messages (3000) - security alerts, shutdowns */
        int CRITICAL = 3000;
        
        /** High priority messages (2000) - moderation actions, important notifications */
        int HIGH = 2000;
        
        /** Normal priority messages (1000) - standard chat, general notifications */
        int NORMAL = 1000;
        
        /** Low priority messages (500) - background info, analytics */
        int LOW = 500;
    }
}
