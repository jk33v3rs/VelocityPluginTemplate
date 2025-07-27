package io.github.jk33v3rs.veloctopusrising.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the 4 specialized Discord bots and cross-platform integration.
 * 
 * <p>The Discord integration features 4 specialized bots working together
 * to provide comprehensive community management, security, and automation
 * across Discord and Minecraft platforms.</p>
 * 
 * <h2>Bot Specializations:</h2>
 * <ul>
 *   <li><strong>Security Bard</strong>: Moderation, anti-spam, raid protection</li>
 *   <li><strong>Flora</strong>: Welcome messages, role management, utilities</li>
 *   <li><strong>May</strong>: Games, events, community engagement</li>
 *   <li><strong>Librarian</strong>: Documentation, help system, knowledge base</li>
 * </ul>
 * 
 * <h2>Integration Features:</h2>
 * <ul>
 *   <li><strong>Chat Bridge</strong>: Bidirectional Minecraft-Discord messaging</li>
 *   <li><strong>Status Sync</strong>: Real-time server status and player counts</li>
 *   <li><strong>Rank Sync</strong>: Automatic Discord role assignment from ranks</li>
 *   <li><strong>Whitelist Integration</strong>: Discord-based access control</li>
 * </ul>
 * 
 * <h2>Performance Requirements:</h2>
 * <ul>
 *   <li><strong>Message Latency</strong>: &lt;100ms Discord-Minecraft bridge</li>
 *   <li><strong>Command Response</strong>: &lt;500ms for bot commands</li>
 *   <li><strong>Status Updates</strong>: Every 30 seconds maximum</li>
 * </ul>
 * 
 * @since 1.0.0
 * @author jk33v3rs
 */
public interface DiscordManager {
    
    /**
     * Represents a Discord bot in the system.
     * 
     * @since 1.0.0
     */
    interface DiscordBot {
        
        /**
         * Gets the bot's unique identifier.
         * 
         * @return The bot identifier (security-bard, flora, may, librarian)
         * @since 1.0.0
         */
        String getBotId();
        
        /**
         * Gets the bot's display name.
         * 
         * @return The bot's display name
         * @since 1.0.0
         */
        String getDisplayName();
        
        /**
         * Gets the bot's specialization.
         * 
         * @return The bot's primary function
         * @since 1.0.0
         */
        String getSpecialization();
        
        /**
         * Gets the Discord application ID.
         * 
         * @return The Discord application ID
         * @since 1.0.0
         */
        String getApplicationId();
        
        /**
         * Checks if the bot is currently online.
         * 
         * @return true if the bot is connected and operational
         * @since 1.0.0
         */
        boolean isOnline();
        
        /**
         * Gets the bot's current status.
         * 
         * @return The bot status (ONLINE, OFFLINE, CONNECTING, ERROR)
         * @since 1.0.0
         */
        BotStatus getStatus();
        
        /**
         * Gets when the bot last came online.
         * 
         * @return The last connection time
         * @since 1.0.0
         */
        Optional<java.time.Instant> getLastOnlineTime();
        
        /**
         * Gets the guilds this bot is active in.
         * 
         * @return List of Discord guild IDs
         * @since 1.0.0
         */
        List<String> getActiveGuilds();
        
        /**
         * Gets bot performance statistics.
         * 
         * @return Current performance metrics
         * @since 1.0.0
         */
        BotStatistics getStatistics();
    }
    
    /**
     * Bot status values.
     * 
     * @since 1.0.0
     */
    enum BotStatus {
        /** Bot is online and processing commands */
        ONLINE,
        
        /** Bot is offline */
        OFFLINE,
        
        /** Bot is attempting to connect */
        CONNECTING,
        
        /** Bot encountered an error */
        ERROR,
        
        /** Bot is shutting down */
        SHUTTING_DOWN,
        
        /** Bot is in maintenance mode */
        MAINTENANCE
    }
    
    /**
     * Represents a Discord message to be sent.
     * 
     * @since 1.0.0
     */
    interface DiscordMessage {
        
        /**
         * Gets the message content.
         * 
         * @return The message text
         * @since 1.0.0
         */
        String getContent();
        
        /**
         * Gets the target channel ID.
         * 
         * @return The Discord channel ID
         * @since 1.0.0
         */
        String getChannelId();
        
        /**
         * Gets embeds attached to this message.
         * 
         * @return List of Discord embeds
         * @since 1.0.0
         */
        List<DiscordEmbed> getEmbeds();
        
        /**
         * Checks if this message should be sent as ephemeral.
         * 
         * @return true if only the command user should see the response
         * @since 1.0.0
         */
        boolean isEphemeral();
        
        /**
         * Gets the message priority.
         * 
         * @return Priority level for message processing
         * @since 1.0.0
         */
        MessagePriority getPriority();
    }
    
    /**
     * Message priority levels.
     * 
     * @since 1.0.0
     */
    enum MessagePriority {
        /** Critical system messages */
        CRITICAL,
        
        /** High priority notifications */
        HIGH,
        
        /** Normal chat messages */
        NORMAL,
        
        /** Low priority updates */
        LOW
    }
    
    /**
     * Represents a Discord embed.
     * 
     * @since 1.0.0
     */
    interface DiscordEmbed {
        
        /** @return Embed title */
        Optional<String> getTitle();
        
        /** @return Embed description */
        Optional<String> getDescription();
        
        /** @return Embed color as hex string */
        Optional<String> getColor();
        
        /** @return Embed fields */
        List<EmbedField> getFields();
        
        /** @return Footer text */
        Optional<String> getFooter();
        
        /** @return Thumbnail URL */
        Optional<String> getThumbnail();
        
        /** @return Image URL */
        Optional<String> getImage();
        
        /** @return Timestamp for the embed */
        Optional<java.time.Instant> getTimestamp();
    }
    
    /**
     * Represents an embed field.
     * 
     * @since 1.0.0
     */
    interface EmbedField {
        
        /** @return Field name */
        String getName();
        
        /** @return Field value */
        String getValue();
        
        /** @return Whether field should display inline */
        boolean isInline();
    }
    
    /**
     * Gets a Discord bot by its identifier.
     * 
     * @param botId The bot identifier
     * @return The Discord bot, or empty if not found
     * @since 1.0.0
     */
    Optional<DiscordBot> getBot(String botId);
    
    /**
     * Gets all Discord bots.
     * 
     * @return List of all Discord bots
     * @since 1.0.0
     */
    List<DiscordBot> getAllBots();
    
    /**
     * Gets online Discord bots.
     * 
     * @return List of currently online bots
     * @since 1.0.0
     */
    List<DiscordBot> getOnlineBots();
    
    /**
     * Sends a message through a specific bot.
     * 
     * @param botId The bot to send the message through
     * @param message The message to send
     * @return CompletableFuture that completes when message is sent
     * @since 1.0.0
     */
    CompletableFuture<Void> sendMessage(String botId, DiscordMessage message);
    
    /**
     * Sends a message through the most appropriate bot.
     * 
     * <p>Automatically selects the best bot based on message type
     * and current bot availability.</p>
     * 
     * @param message The message to send
     * @return CompletableFuture that completes when message is sent
     * @since 1.0.0
     */
    CompletableFuture<Void> sendMessage(DiscordMessage message);
    
    /**
     * Sends a simple text message to a channel.
     * 
     * @param channelId The Discord channel ID
     * @param content The message content
     * @return CompletableFuture that completes when message is sent
     * @since 1.0.0
     */
    CompletableFuture<Void> sendSimpleMessage(String channelId, String content);
    
    /**
     * Bridges a Minecraft chat message to Discord.
     * 
     * @param playerName The Minecraft player's name
     * @param message The chat message
     * @param serverName The server the message came from
     * @return CompletableFuture that completes when message is bridged
     * @since 1.0.0
     */
    CompletableFuture<Void> bridgeMinecraftMessage(
        String playerName,
        String message,
        String serverName
    );
    
    /**
     * Updates Discord server status information.
     * 
     * @param serverName The server name
     * @param playerCount Current player count
     * @param maxPlayers Maximum player capacity
     * @param isOnline Whether the server is online
     * @return CompletableFuture that completes when status is updated
     * @since 1.0.0
     */
    CompletableFuture<Void> updateServerStatus(
        String serverName,
        int playerCount,
        int maxPlayers,
        boolean isOnline
    );
    
    /**
     * Synchronizes Discord roles for a player based on their rank.
     * 
     * @param discordUserId The Discord user ID
     * @param currentRank The player's current rank
     * @return CompletableFuture that completes when roles are synchronized
     * @since 1.0.0
     */
    CompletableFuture<Void> syncPlayerRoles(
        String discordUserId,
        RankManager.Rank currentRank
    );
    
    /**
     * Notifies Discord about a player's rank advancement.
     * 
     * @param playerId The player's UUID
     * @param playerName The player's name
     * @param oldRank The previous rank
     * @param newRank The new rank
     * @return CompletableFuture that completes when notification is sent
     * @since 1.0.0
     */
    CompletableFuture<Void> notifyRankAdvancement(
        UUID playerId,
        String playerName,
        RankManager.Rank oldRank,
        RankManager.Rank newRank
    );
    
    /**
     * Sends a security alert to designated Discord channels.
     * 
     * @param alertType The type of security alert
     * @param message The alert message
     * @param severity The severity level
     * @return CompletableFuture that completes when alert is sent
     * @since 1.0.0
     */
    CompletableFuture<Void> sendSecurityAlert(
        String alertType,
        String message,
        AlertSeverity severity
    );
    
    /**
     * Alert severity levels.
     * 
     * @since 1.0.0
     */
    enum AlertSeverity {
        /** Informational alert */
        INFO,
        
        /** Warning alert */
        WARNING,
        
        /** Critical alert requiring immediate attention */
        CRITICAL,
        
        /** Emergency alert */
        EMERGENCY
    }
    
    /**
     * Gets Discord user information by user ID.
     * 
     * @param userId The Discord user ID
     * @return User information, or empty if not found
     * @since 1.0.0
     */
    CompletableFuture<Optional<DiscordUser>> getDiscordUser(String userId);
    
    /**
     * Gets Discord guild member information.
     * 
     * @param guildId The Discord guild ID
     * @param userId The Discord user ID
     * @return Member information, or empty if not found
     * @since 1.0.0
     */
    CompletableFuture<Optional<DiscordMember>> getGuildMember(
        String guildId,
        String userId
    );
    
    /**
     * Checks if a user is in a specific Discord guild.
     * 
     * @param guildId The Discord guild ID
     * @param userId The Discord user ID
     * @return true if the user is a member of the guild
     * @since 1.0.0
     */
    CompletableFuture<Boolean> isUserInGuild(String guildId, String userId);
    
    /**
     * Gets the primary Discord guild ID.
     * 
     * @return The main Discord server ID
     * @since 1.0.0
     */
    String getPrimaryGuildId();
    
    /**
     * Gets Discord integration statistics.
     * 
     * @return Current Discord system statistics
     * @since 1.0.0
     */
    DiscordStatistics getStatistics();
    
    /**
     * Represents Discord user information.
     * 
     * @since 1.0.0
     */
    interface DiscordUser {
        
        /** @return Discord user ID */
        String getUserId();
        
        /** @return Username */
        String getUsername();
        
        /** @return Display name */
        String getDisplayName();
        
        /** @return Avatar URL */
        Optional<String> getAvatarUrl();
        
        /** @return Whether the account is a bot */
        boolean isBot();
        
        /** @return Account creation timestamp */
        java.time.Instant getCreationTime();
    }
    
    /**
     * Represents Discord guild member information.
     * 
     * @since 1.0.0
     */
    interface DiscordMember {
        
        /** @return Discord user information */
        DiscordUser getUser();
        
        /** @return Guild-specific nickname */
        Optional<String> getNickname();
        
        /** @return When the user joined the guild */
        java.time.Instant getJoinTime();
        
        /** @return List of role IDs the member has */
        List<String> getRoleIds();
        
        /** @return Whether the member can be mentioned */
        boolean isMentionable();
        
        /** @return Member's permissions in the guild */
        List<String> getPermissions();
    }
    
    /**
     * Bot performance statistics.
     * 
     * @since 1.0.0
     */
    interface BotStatistics {
        
        /** @return Total commands processed */
        long getTotalCommands();
        
        /** @return Commands processed in the last hour */
        long getRecentCommands();
        
        /** @return Average command response time (ms) */
        double getAverageResponseTime();
        
        /** @return Number of guilds the bot is in */
        int getGuildCount();
        
        /** @return Current uptime */
        java.time.Duration getUptime();
        
        /** @return Memory usage percentage */
        double getMemoryUsage();
        
        /** @return Number of recent errors */
        int getRecentErrors();
    }
    
    /**
     * Discord system statistics.
     * 
     * @since 1.0.0
     */
    interface DiscordStatistics {
        
        /** @return Number of online bots */
        int getOnlineBots();
        
        /** @return Total messages sent today */
        long getMessagesSentToday();
        
        /** @return Total bridged messages today */
        long getBridgedMessagesToday();
        
        /** @return Average message delivery time (ms) */
        double getAverageDeliveryTime();
        
        /** @return Number of linked Discord accounts */
        long getLinkedAccounts();
        
        /** @return Number of synchronized roles today */
        long getRolesSyncedToday();
        
        /** @return Current Discord API rate limit remaining */
        int getRateLimitRemaining();
    }
}
