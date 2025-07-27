package io.github.jk33v3rs.veloctopusrising.api;

import java.util.Optional;

/**
 * Main API provider interface for external plugin integration.
 * 
 * <p>This interface provides access to all major Veloctopus Rising systems
 * for other plugins that want to integrate with the communication hub.</p>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Get the API provider
 * Optional<CommAPIProvider> apiOpt = CommAPIProvider.get();
 * if (apiOpt.isPresent()) {
 *     CommAPIProvider api = apiOpt.get();
 *     
 *     // Send a cross-platform message
 *     api.getMessageTranslator()
 *        .translateAndSend(message, Platform.DISCORD, Platform.MINECRAFT)
 *        .thenAccept(success -> logger.info("Message sent: " + success));
 *     
 *     // Check player rank
 *     api.getRankManager()
 *        .getPlayerRank(playerUUID)
 *        .thenAccept(rank -> logger.info("Player rank: " + rank.getDisplayName()));
 * }
 * }</pre>
 * 
 * <h2>Thread Safety:</h2>
 * <p>All methods in this interface are thread-safe and return CompletableFuture
 * for non-blocking operations. The API guarantees zero main thread blocking.</p>
 * 
 * @since 1.0.0
 * @author jk33v3rs
 */
public interface CommAPIProvider {
    
    /**
     * Gets the singleton instance of the API provider.
     * 
     * <p>This method returns the current active API provider instance.
     * It will return empty if the plugin is not fully initialized or
     * has been shut down.</p>
     * 
     * @return Optional containing the API provider if available
     * @since 1.0.0
     */
    static Optional<CommAPIProvider> get() {
        return VeloctopusAPIRegistry.getInstance();
    }
    
    /**
     * Gets the message translation engine for cross-platform communication.
     * 
     * <p>The message translator handles format conversion between different
     * platforms (Minecraft, Discord, Matrix) while preserving rich formatting,
     * attachments, and interactive components.</p>
     * 
     * @return MessageTranslator instance for platform message conversion
     * @since 1.0.0
     */
    MessageTranslator getMessageTranslator();
    
    /**
     * Gets the event manager for plugin event integration.
     * 
     * <p>The event manager provides access to the internal event system
     * for listening to player events, chat events, rank changes, and
     * system status events.</p>
     * 
     * @return EventManager instance for event handling
     * @since 1.0.0
     */
    EventManager getEventManager();
    
    /**
     * Gets the rank manager for the 175-rank system.
     * 
     * <p>Provides access to the complete VeloctopusProject-compatible
     * rank system with 25 main ranks × 7 sub-ranks = 175 combinations.</p>
     * 
     * @return RankManager instance for rank operations
     * @since 1.0.0
     */
    RankManager getRankManager();
    
    /**
     * Gets the XP manager for the 4000-endpoint achievement system.
     * 
     * <p>Manages experience points, achievements, and community-weighted
     * progression with support for peer recognition and community contributions.</p>
     * 
     * @return XPManager instance for experience and achievement operations
     * @since 1.0.0
     */
    XPManager getXPManager();
    
    /**
     * Gets the permission manager for the permission system.
     * 
     * <p>Provides access to the permission system with rank-based inheritance,
     * Redis caching, and LuckPerms integration.</p>
     * 
     * @return PermissionManager instance for permission operations
     * @since 1.0.0
     */
    PermissionManager getPermissionManager();
    
    /**
     * Gets the whitelist manager for verification workflows.
     * 
     * <p>Manages the complete VeloctopusProject whitelist verification workflow
     * including Discord verification, purgatory states, and Mojang API integration.</p>
     * 
     * @return WhitelistManager instance for verification operations
     * @since 1.0.0
     */
    WhitelistManager getWhitelistManager();
    
    /**
     * Gets the Discord integration manager for bot operations.
     * 
     * <p>Provides access to all four Discord bot personalities:
     * Security Bard, Flora, May, and Librarian with their specialized functions.</p>
     * 
     * @return DiscordManager instance for Discord bot control
     * @since 1.0.0
     */
    DiscordManager getDiscordManager();
}
