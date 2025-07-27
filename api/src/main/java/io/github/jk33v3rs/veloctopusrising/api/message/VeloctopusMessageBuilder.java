package io.github.jk33v3rs.veloctopusrising.api.message;

import java.time.Instant;
import java.util.UUID;
import java.util.Map;

/**
 * Builder interface for constructing VeloctopusMessage instances with validation.
 * 
 * <p>Provides a fluent API for building VeloctopusMessage instances with
 * proper validation, default values, and type safety. The builder pattern
 * ensures all required fields are set and validates message content.</p>
 * 
 * <p><strong>Thread Safety:</strong> Builders are NOT thread-safe and should
 * not be shared between threads. Create separate builder instances for
 * concurrent message construction.</p>
 * 
 * <p><strong>Performance Considerations:</strong> Builders are lightweight
 * and designed for quick message construction. Validation is performed
 * at build time to ensure message integrity.</p>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * VeloctopusMessage message = VeloctopusMessage.builder()
 *     .content("Hello, world!")
 *     .sender("PlayerName")
 *     .source(MessageSource.MINECRAFT)
 *     .target(MessageSource.DISCORD)
 *     .channel("global")
 *     .type(MessageType.CHAT)
 *     .metadata("player_uuid", "12345678-1234-1234-1234-123456789abc")
 *     .metadata("rank_color", "#00FF00")
 *     .build();
 * </code></pre>
 * 
 * <h3>Integration Points:</h3>
 * <ul>
 *   <li>VeloctopusMessage.builder() factory method</li>
 *   <li>Message Translation Engine for message construction</li>
 *   <li>Platform adapters for converting external messages</li>
 * </ul>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see VeloctopusMessage
 */
public interface VeloctopusMessageBuilder {
    
    /**
     * Sets the primary text content for the message.
     * 
     * <p>The content should be in a platform-neutral format that can be
     * translated to platform-specific representations. Rich formatting
     * should use standardized markup that can be converted as needed.</p>
     * 
     * @param content the message content, must be {@code non-null}
     * @return this builder for method chaining
     * @throws IllegalArgumentException if content is null
     * @since 1.0.0
     */
    VeloctopusMessageBuilder content(String content);
    
    /**
     * Sets the sender identifier for the message.
     * 
     * <p>Should uniquely identify the message sender within the context
     * of the source platform. Format varies by platform but should be
     * consistent for translation purposes.</p>
     * 
     * @param sender the sender identifier, must be {@code non-null}
     * @return this builder for method chaining
     * @throws IllegalArgumentException if sender is null
     * @since 1.0.0
     */
    VeloctopusMessageBuilder sender(String sender);
    
    /**
     * Sets the source platform for the message.
     * 
     * <p>Identifies which platform this message originated from.
     * Required for proper message translation and routing.</p>
     * 
     * @param source the source platform, must be {@code non-null}
     * @return this builder for method chaining
     * @throws IllegalArgumentException if source is null
     * @since 1.0.0
     */
    VeloctopusMessageBuilder source(MessageSource source);
    
    /**
     * Sets the target platform for message translation.
     * 
     * <p>Optionally specifies which platform this message should be
     * translated to. If not set, message can be translated to any platform.</p>
     * 
     * @param target the target platform, may be {@code null}
     * @return this builder for method chaining
     * @since 1.0.0
     */
    VeloctopusMessageBuilder target(MessageSource target);
    
    /**
     * Sets the channel identifier for the message.
     * 
     * <p>Specifies the destination channel, room, or communication endpoint
     * where this message should be delivered.</p>
     * 
     * @param channel the channel identifier, must be {@code non-null}
     * @return this builder for method chaining
     * @throws IllegalArgumentException if channel is null
     * @since 1.0.0
     */
    VeloctopusMessageBuilder channel(String channel);
    
    /**
     * Sets the message type classification.
     * 
     * <p>Classifies the message for appropriate handling, formatting,
     * and routing decisions. If not set, defaults to CHAT type.</p>
     * 
     * @param type the message type, must be {@code non-null}
     * @return this builder for method chaining
     * @throws IllegalArgumentException if type is null
     * @since 1.0.0
     */
    VeloctopusMessageBuilder type(MessageType type);
    
    /**
     * Sets the priority level for message processing.
     * 
     * <p>Higher priority messages are processed before lower priority messages.
     * If not set, defaults to the message type's default priority.</p>
     * 
     * @param priority the priority level (higher values processed first)
     * @return this builder for method chaining
     * @since 1.0.0
     */
    VeloctopusMessageBuilder priority(int priority);
    
    /**
     * Sets a metadata key-value pair for the message.
     * 
     * <p>Adds platform-specific metadata, formatting hints, or additional
     * context information. Metadata is preserved during translation when
     * compatible with the target platform.</p>
     * 
     * @param key the metadata key, must be {@code non-null}
     * @param value the metadata value, may be {@code null}
     * @return this builder for method chaining
     * @throws IllegalArgumentException if key is null
     * @since 1.0.0
     */
    VeloctopusMessageBuilder metadata(String key, Object value);
    
    /**
     * Sets multiple metadata key-value pairs for the message.
     * 
     * <p>Replaces any existing metadata with the provided map.
     * Use this for bulk metadata assignment from external sources.</p>
     * 
     * @param metadata the metadata map, must be {@code non-null}
     * @return this builder for method chaining
     * @throws IllegalArgumentException if metadata is null
     * @since 1.0.0
     */
    VeloctopusMessageBuilder metadata(Map<String, Object> metadata);
    
    /**
     * Sets a custom correlation ID for the message.
     * 
     * <p>Normally correlation IDs are generated automatically, but this
     * allows setting a specific ID for message tracking or testing purposes.</p>
     * 
     * @param correlationId the correlation ID, must be {@code non-null}
     * @return this builder for method chaining
     * @throws IllegalArgumentException if correlationId is null
     * @since 1.0.0
     */
    VeloctopusMessageBuilder correlationId(UUID correlationId);
    
    /**
     * Sets a custom timestamp for the message.
     * 
     * <p>Normally timestamps are generated automatically at creation time,
     * but this allows setting a specific timestamp for historical messages
     * or testing purposes.</p>
     * 
     * @param timestamp the message timestamp, must be {@code non-null}
     * @return this builder for method chaining
     * @throws IllegalArgumentException if timestamp is null
     * @since 1.0.0
     */
    VeloctopusMessageBuilder timestamp(Instant timestamp);
    
    /**
     * Builds and returns the VeloctopusMessage instance.
     * 
     * <p>Validates all required fields are set, applies default values
     * for optional fields, and constructs an immutable VeloctopusMessage
     * instance.</p>
     * 
     * <p><strong>Required Fields:</strong></p>
     * <ul>
     *   <li>content - message text content</li>
     *   <li>sender - message sender identifier</li>
     *   <li>source - source platform</li>
     *   <li>channel - destination channel</li>
     * </ul>
     * 
     * <p><strong>Default Values:</strong></p>
     * <ul>
     *   <li>type - MessageType.CHAT</li>
     *   <li>priority - type.getDefaultPriority()</li>
     *   <li>correlationId - randomly generated UUID</li>
     *   <li>timestamp - Instant.now()</li>
     * </ul>
     * 
     * @return new immutable VeloctopusMessage instance
     * @throws IllegalStateException if required fields are missing
     * @throws IllegalArgumentException if field values are invalid
     * @since 1.0.0
     */
    VeloctopusMessage build();
}
