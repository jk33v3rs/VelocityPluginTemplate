package io.github.jk33v3rs.veloctopusrising.api.message;

/**
 * Enumeration of supported communication platforms in Veloctopus Rising.
 * 
 * <p>This enum identifies the various platforms that can send and receive
 * messages through the unified message translation system. Each platform
 * has specific formatting requirements and capabilities.</p>
 * 
 * <p><strong>Thread Safety:</strong> Immutable enum - completely thread-safe.</p>
 * 
 * <p><strong>Performance Considerations:</strong> Enum comparisons are O(1)
 * and highly optimized by the JVM. Used extensively in message routing.</p>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * if (message.getSource() == MessageSource.MINECRAFT) {
 *     // Handle Minecraft-specific formatting
 *     handleMinecraftMessage(message);
 * }
 * </code></pre>
 * 
 * <h3>Integration Points:</h3>
 * <ul>
 *   <li>VeloctopusMessage for source and target platform identification</li>
 *   <li>Message Translation Engine for routing decisions</li>
 *   <li>Platform-specific adapters and formatters</li>
 * </ul>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see VeloctopusMessage
 * @see MessageTranslationEngine
 */
public enum MessageSource {
    
    /**
     * Minecraft game platform - includes all Minecraft servers behind Velocity proxy.
     * 
     * <p>Supports Adventure Text Components with MiniMessage formatting,
     * click/hover events, and rich text rendering. Messages from players
     * include player context and permission information.</p>
     * 
     * <h4>Supported Features:</h4>
     * <ul>
     *   <li>MiniMessage text formatting (&lt;color:red&gt;text&lt;/color&gt;)</li>
     *   <li>Click and hover events for interactive text</li>
     *   <li>Player context (UUID, rank, permissions)</li>
     *   <li>Server context (server name, player counts)</li>
     * </ul>
     */
    MINECRAFT,
    
    /**
     * Discord platform - includes all four specialized Discord bots.
     * 
     * <p>Supports rich embeds, reactions, attachments, and interactive
     * components. Messages can be routed to specific bots based on
     * content type and target audience.</p>
     * 
     * <h4>Supported Features:</h4>
     * <ul>
     *   <li>Rich embeds with colors, thumbnails, fields</li>
     *   <li>Interactive buttons and select menus</li>
     *   <li>File attachments and media content</li>
     *   <li>Reactions and emoji responses</li>
     *   <li>Bot-specific routing (Security Bard, Flora, May, Librarian)</li>
     * </ul>
     */
    DISCORD,
    
    /**
     * Matrix protocol platform - federated messaging system.
     * 
     * <p>Supports Matrix rooms, federation, and basic formatting.
     * Prepared for future "galactic chat" expansion connecting
     * multiple communities across the Matrix federation.</p>
     * 
     * <h4>Supported Features:</h4>
     * <ul>
     *   <li>Matrix room messaging</li>
     *   <li>Basic HTML formatting</li>
     *   <li>Federation across Matrix homeservers</li>
     *   <li>User mentions and room references</li>
     * </ul>
     */
    MATRIX,
    
    /**
     * Internal system platform - for plugin-generated messages.
     * 
     * <p>Used for system notifications, automated responses, and
     * internal communication between plugin modules. These messages
     * can be translated to any external platform.</p>
     * 
     * <h4>Supported Features:</h4>
     * <ul>
     *   <li>System notifications and alerts</li>
     *   <li>Automated responses and confirmations</li>
     *   <li>Module-to-module communication</li>
     *   <li>Performance and health monitoring messages</li>
     * </ul>
     */
    SYSTEM,
    
    /**
     * External API platform - for third-party integrations.
     * 
     * <p>Used for external services like social media monitoring,
     * webhooks, and API integrations. Supports flexible JSON
     * formatting for various external systems.</p>
     * 
     * <h4>Supported Features:</h4>
     * <ul>
     *   <li>Webhook integrations</li>
     *   <li>Social media cross-posting</li>
     *   <li>Third-party API communication</li>
     *   <li>Custom JSON formatting</li>
     * </ul>
     */
    EXTERNAL;
    
    /**
     * Checks if this platform supports rich text formatting.
     * 
     * <p>Returns true if the platform can display formatted text with
     * colors, styling, and interactive elements. Used to determine
     * if complex formatting should be preserved during translation.</p>
     * 
     * @return true if platform supports rich formatting
     * @since 1.0.0
     */
    public boolean supportsRichFormatting() {
        return switch (this) {
            case MINECRAFT, DISCORD -> true;
            case MATRIX, SYSTEM, EXTERNAL -> false;
        };
    }
    
    /**
     * Checks if this platform supports interactive components.
     * 
     * <p>Returns true if the platform can display buttons, links,
     * or other interactive elements that users can click or interact with.</p>
     * 
     * @return true if platform supports interactive components
     * @since 1.0.0
     */
    public boolean supportsInteractivity() {
        return switch (this) {
            case MINECRAFT, DISCORD -> true;
            case MATRIX, SYSTEM, EXTERNAL -> false;
        };
    }
    
    /**
     * Checks if this platform supports file attachments.
     * 
     * <p>Returns true if the platform can handle file uploads,
     * images, or other media attachments alongside text messages.</p>
     * 
     * @return true if platform supports attachments
     * @since 1.0.0
     */
    public boolean supportsAttachments() {
        return switch (this) {
            case DISCORD -> true;
            case MINECRAFT, MATRIX, SYSTEM, EXTERNAL -> false;
        };
    }
}
