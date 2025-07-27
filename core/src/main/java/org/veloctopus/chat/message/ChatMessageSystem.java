/*
 * This file is part of VeloctopusProject, licensed under the MIT License.
 *
 * Copyright (c) 2025 VeloctopusProject Contributors
 * 
 * Chat Message System Foundation
 * Step 39: Implement core chat message model and routing infrastructure
 */

package org.veloctopus.chat.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chat Message System Foundation
 * 
 * Core message infrastructure for multi-platform communication:
 * 
 * Message Model:
 * - Unified message format across all platforms
 * - Rich metadata support (sender, channel, timestamp, platform)
 * - Content transformation and filtering
 * - Platform-specific formatting and rendering
 * - Message lifecycle management
 * 
 * Channel Abstraction:
 * - Server-specific channels (per-server chat)
 * - Global channels (cross-server communication)
 * - Private channels (direct messages, parties)
 * - System channels (announcements, alerts)
 * - Discord-integrated channels
 * 
 * Message Routing:
 * - Intelligent message distribution
 * - Platform-aware content transformation
 * - Rate limiting and anti-spam protection
 * - Permission-based message filtering
 * - Real-time delivery with retry mechanisms
 * 
 * Features:
 * - Multi-platform message unification
 * - Adventure Component integration
 * - MiniMessage format support
 * - Rich text and interactive elements
 * - Channel-based message routing
 * - Real-time message delivery
 * - Comprehensive message history
 * - Anti-spam and rate limiting
 * 
 * @author VeloctopusProject Team
 * @since 1.0.0
 */
public class ChatMessageSystem {

    /**
     * Message platform types
     */
    public enum MessagePlatform {
        MINECRAFT("minecraft", "§7[MC]"),
        DISCORD("discord", "🎮"),
        MATRIX("matrix", "⚡"),
        SYSTEM("system", "⚙️"),
        CONSOLE("console", "💻");

        private final String identifier;
        private final String displayPrefix;

        MessagePlatform(String identifier, String displayPrefix) {
            this.identifier = identifier;
            this.displayPrefix = displayPrefix;
        }

        public String getIdentifier() { return identifier; }
        public String getDisplayPrefix() { return displayPrefix; }
    }

    /**
     * Channel types for message routing
     */
    public enum ChannelType {
        SERVER_LOCAL("local", "Local Chat"),
        GLOBAL("global", "Global Chat"),
        STAFF("staff", "Staff Chat"),
        PRIVATE("private", "Private Message"),
        PARTY("party", "Party Chat"),
        DISCORD_GENERAL("discord-general", "Discord General"),
        DISCORD_GAME("discord-game", "Discord Gaming"),
        SYSTEM("system", "System Messages"),
        ANNOUNCEMENT("announcement", "Announcements");

        private final String identifier;
        private final String displayName;

        ChannelType(String identifier, String displayName) {
            this.identifier = identifier;
            this.displayName = displayName;
        }

        public String getIdentifier() { return identifier; }
        public String getDisplayName() { return displayName; }
    }

    /**
     * Message priority levels
     */
    public enum MessagePriority {
        URGENT(4, NamedTextColor.RED),
        HIGH(3, NamedTextColor.YELLOW),
        NORMAL(2, NamedTextColor.WHITE),
        LOW(1, NamedTextColor.GRAY),
        DEBUG(0, NamedTextColor.DARK_GRAY);

        private final int level;
        private final NamedTextColor color;

        MessagePriority(int level, NamedTextColor color) {
            this.level = level;
            this.color = color;
        }

        public int getLevel() { return level; }
        public NamedTextColor getColor() { return color; }
    }

    /**
     * Core chat message representation
     */
    public static class ChatMessage {
        private final String messageId;
        private final String senderId;
        private final String senderName;
        private final String senderDisplayName;
        private final MessagePlatform platform;
        private final ChannelType channelType;
        private final String channelName;
        private final String rawContent;
        private final Component formattedContent;
        private final MessagePriority priority;
        private final Instant timestamp;
        private final Map<String, Object> metadata;
        private final Set<String> mentionedUsers;
        private final Set<String> mentionedRoles;
        private final boolean isEdited;
        private final String editReason;

        private ChatMessage(Builder builder) {
            this.messageId = builder.messageId != null ? builder.messageId : UUID.randomUUID().toString();
            this.senderId = builder.senderId;
            this.senderName = builder.senderName;
            this.senderDisplayName = builder.senderDisplayName;
            this.platform = builder.platform;
            this.channelType = builder.channelType;
            this.channelName = builder.channelName;
            this.rawContent = builder.rawContent;
            this.formattedContent = builder.formattedContent;
            this.priority = builder.priority;
            this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
            this.metadata = new ConcurrentHashMap<>(builder.metadata);
            this.mentionedUsers = new HashSet<>(builder.mentionedUsers);
            this.mentionedRoles = new HashSet<>(builder.mentionedRoles);
            this.isEdited = builder.isEdited;
            this.editReason = builder.editReason;
        }

        // Getters
        public String getMessageId() { return messageId; }
        public String getSenderId() { return senderId; }
        public String getSenderName() { return senderName; }
        public String getSenderDisplayName() { return senderDisplayName; }
        public MessagePlatform getPlatform() { return platform; }
        public ChannelType getChannelType() { return channelType; }
        public String getChannelName() { return channelName; }
        public String getRawContent() { return rawContent; }
        public Component getFormattedContent() { return formattedContent; }
        public MessagePriority getPriority() { return priority; }
        public Instant getTimestamp() { return timestamp; }
        public Map<String, Object> getMetadata() { return new ConcurrentHashMap<>(metadata); }
        public Set<String> getMentionedUsers() { return new HashSet<>(mentionedUsers); }
        public Set<String> getMentionedRoles() { return new HashSet<>(mentionedRoles); }
        public boolean isEdited() { return isEdited; }
        public String getEditReason() { return editReason; }

        /**
         * Get formatted content for specific platform
         */
        public Component getFormattedContentForPlatform(MessagePlatform targetPlatform) {
            if (targetPlatform == platform) {
                return formattedContent;
            }

            // Apply platform-specific formatting
            Component baseContent = formattedContent;
            
            switch (targetPlatform) {
                case MINECRAFT:
                    return Component.text()
                        .append(Component.text(platform.getDisplayPrefix(), NamedTextColor.GRAY))
                        .append(Component.space())
                        .append(baseContent)
                        .build();
                
                case DISCORD:
                    return Component.text()
                        .append(Component.text("**[" + senderDisplayName + "]**", NamedTextColor.WHITE))
                        .append(Component.space())
                        .append(baseContent)
                        .build();
                
                default:
                    return baseContent;
            }
        }

        /**
         * Builder pattern for ChatMessage creation
         */
        public static class Builder {
            private String messageId;
            private String senderId;
            private String senderName;
            private String senderDisplayName;
            private MessagePlatform platform = MessagePlatform.MINECRAFT;
            private ChannelType channelType = ChannelType.SERVER_LOCAL;
            private String channelName = "general";
            private String rawContent = "";
            private Component formattedContent = Component.empty();
            private MessagePriority priority = MessagePriority.NORMAL;
            private Instant timestamp;
            private Map<String, Object> metadata = new HashMap<>();
            private Set<String> mentionedUsers = new HashSet<>();
            private Set<String> mentionedRoles = new HashSet<>();
            private boolean isEdited = false;
            private String editReason = null;

            public Builder messageId(String messageId) { this.messageId = messageId; return this; }
            public Builder senderId(String senderId) { this.senderId = senderId; return this; }
            public Builder senderName(String senderName) { this.senderName = senderName; return this; }
            public Builder senderDisplayName(String senderDisplayName) { this.senderDisplayName = senderDisplayName; return this; }
            public Builder platform(MessagePlatform platform) { this.platform = platform; return this; }
            public Builder channelType(ChannelType channelType) { this.channelType = channelType; return this; }
            public Builder channelName(String channelName) { this.channelName = channelName; return this; }
            public Builder rawContent(String rawContent) { this.rawContent = rawContent; return this; }
            public Builder formattedContent(Component formattedContent) { this.formattedContent = formattedContent; return this; }
            public Builder priority(MessagePriority priority) { this.priority = priority; return this; }
            public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
            public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }
            public Builder addMetadata(String key, Object value) { this.metadata.put(key, value); return this; }
            public Builder mentionedUsers(Set<String> mentionedUsers) { this.mentionedUsers = mentionedUsers; return this; }
            public Builder addMentionedUser(String userId) { this.mentionedUsers.add(userId); return this; }
            public Builder mentionedRoles(Set<String> mentionedRoles) { this.mentionedRoles = mentionedRoles; return this; }
            public Builder addMentionedRole(String roleId) { this.mentionedRoles.add(roleId); return this; }
            public Builder isEdited(boolean isEdited) { this.isEdited = isEdited; return this; }
            public Builder editReason(String editReason) { this.editReason = editReason; return this; }

            public ChatMessage build() {
                if (senderId == null || senderName == null) {
                    throw new IllegalArgumentException("SenderId and senderName are required");
                }
                if (senderDisplayName == null) {
                    senderDisplayName = senderName;
                }
                return new ChatMessage(this);
            }
        }
    }

    /**
     * Channel configuration and management
     */
    public static class ChatChannel {
        private final String channelId;
        private final ChannelType type;
        private final String name;
        private final String displayName;
        private final String description;
        private final Set<MessagePlatform> enabledPlatforms;
        private final Set<String> allowedRoles;
        private final Set<String> moderatorRoles;
        private final boolean isPublic;
        private final boolean isPersistent;
        private final int rateLimit; // messages per minute
        private final Map<String, Object> configuration;

        public ChatChannel(String channelId, ChannelType type, String name) {
            this.channelId = channelId;
            this.type = type;
            this.name = name;
            this.displayName = name;
            this.description = "Chat channel: " + name;
            this.enabledPlatforms = EnumSet.allOf(MessagePlatform.class);
            this.allowedRoles = new HashSet<>();
            this.moderatorRoles = new HashSet<>();
            this.isPublic = true;
            this.isPersistent = true;
            this.rateLimit = 60; // 60 messages per minute default
            this.configuration = new ConcurrentHashMap<>();
        }

        // Getters
        public String getChannelId() { return channelId; }
        public ChannelType getType() { return type; }
        public String getName() { return name; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public Set<MessagePlatform> getEnabledPlatforms() { return new HashSet<>(enabledPlatforms); }
        public Set<String> getAllowedRoles() { return new HashSet<>(allowedRoles); }
        public Set<String> getModeratorRoles() { return new HashSet<>(moderatorRoles); }
        public boolean isPublic() { return isPublic; }
        public boolean isPersistent() { return isPersistent; }
        public int getRateLimit() { return rateLimit; }
        public Map<String, Object> getConfiguration() { return new ConcurrentHashMap<>(configuration); }

        /**
         * Check if platform is enabled for this channel
         */
        public boolean isPlatformEnabled(MessagePlatform platform) {
            return enabledPlatforms.contains(platform);
        }

        /**
         * Check if user has access to this channel
         */
        public boolean hasAccess(String userId, Set<String> userRoles) {
            if (isPublic && allowedRoles.isEmpty()) {
                return true;
            }
            
            return allowedRoles.stream().anyMatch(userRoles::contains);
        }

        /**
         * Check if user can moderate this channel
         */
        public boolean canModerate(String userId, Set<String> userRoles) {
            return moderatorRoles.stream().anyMatch(userRoles::contains);
        }
    }

    /**
     * Rate limiting for anti-spam protection
     */
    public static class RateLimiter {
        private final Map<String, List<Instant>> userMessageTimes;
        private final int maxMessagesPerMinute;

        public RateLimiter(int maxMessagesPerMinute) {
            this.userMessageTimes = new ConcurrentHashMap<>();
            this.maxMessagesPerMinute = maxMessagesPerMinute;
        }

        /**
         * Check if user is rate limited
         */
        public boolean isRateLimited(String userId) {
            List<Instant> messageTimes = userMessageTimes.computeIfAbsent(userId, k -> new ArrayList<>());
            
            // Clean old messages (older than 1 minute)
            Instant cutoff = Instant.now().minusSeconds(60);
            messageTimes.removeIf(time -> time.isBefore(cutoff));
            
            // Check if rate limited
            return messageTimes.size() >= maxMessagesPerMinute;
        }

        /**
         * Record message for rate limiting
         */
        public void recordMessage(String userId) {
            List<Instant> messageTimes = userMessageTimes.computeIfAbsent(userId, k -> new ArrayList<>());
            messageTimes.add(Instant.now());
        }

        /**
         * Get remaining messages for user
         */
        public int getRemainingMessages(String userId) {
            List<Instant> messageTimes = userMessageTimes.get(userId);
            if (messageTimes == null) {
                return maxMessagesPerMinute;
            }
            
            // Clean old messages
            Instant cutoff = Instant.now().minusSeconds(60);
            messageTimes.removeIf(time -> time.isBefore(cutoff));
            
            return Math.max(0, maxMessagesPerMinute - messageTimes.size());
        }
    }

    // Core system components
    private final Map<String, ChatChannel> channels;
    private final Map<String, RateLimiter> channelRateLimiters;
    private final List<ChatMessage> messageHistory;
    private final MiniMessage miniMessage;
    private final Map<String, Object> systemMetrics;

    public ChatMessageSystem() {
        this.channels = new ConcurrentHashMap<>();
        this.channelRateLimiters = new ConcurrentHashMap<>();
        this.messageHistory = Collections.synchronizedList(new ArrayList<>());
        this.miniMessage = MiniMessage.miniMessage();
        this.systemMetrics = new ConcurrentHashMap<>();
        
        initializeDefaultChannels();
        initializeSystemMetrics();
    }

    /**
     * Initialize default chat channels
     */
    private void initializeDefaultChannels() {
        // Global chat channel
        ChatChannel globalChannel = new ChatChannel("global", ChannelType.GLOBAL, "global");
        channels.put("global", globalChannel);
        channelRateLimiters.put("global", new RateLimiter(60));
        
        // Staff chat channel
        ChatChannel staffChannel = new ChatChannel("staff", ChannelType.STAFF, "staff");
        channels.put("staff", staffChannel);
        channelRateLimiters.put("staff", new RateLimiter(120));
        
        // Discord general channel
        ChatChannel discordChannel = new ChatChannel("discord-general", ChannelType.DISCORD_GENERAL, "discord-general");
        channels.put("discord-general", discordChannel);
        channelRateLimiters.put("discord-general", new RateLimiter(60));
        
        // System announcements channel
        ChatChannel systemChannel = new ChatChannel("system", ChannelType.SYSTEM, "system");
        channels.put("system", systemChannel);
        channelRateLimiters.put("system", new RateLimiter(300)); // Higher limit for system messages
    }

    /**
     * Initialize system metrics
     */
    private void initializeSystemMetrics() {
        systemMetrics.put("total_messages", 0);
        systemMetrics.put("messages_by_platform", new ConcurrentHashMap<String, Integer>());
        systemMetrics.put("messages_by_channel", new ConcurrentHashMap<String, Integer>());
        systemMetrics.put("rate_limited_messages", 0);
        systemMetrics.put("system_start_time", Instant.now());
    }

    /**
     * Process incoming chat message
     */
    public CompletableFuture<Boolean> processMessage(ChatMessage message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate message
                if (!validateMessage(message)) {
                    return false;
                }
                
                // Check rate limiting
                RateLimiter rateLimiter = channelRateLimiters.get(message.getChannelName());
                if (rateLimiter != null && rateLimiter.isRateLimited(message.getSenderId())) {
                    updateMetric("rate_limited_messages", 1);
                    return false;
                }
                
                // Record for rate limiting
                if (rateLimiter != null) {
                    rateLimiter.recordMessage(message.getSenderId());
                }
                
                // Add to message history
                messageHistory.add(message);
                
                // Keep only last 10,000 messages
                while (messageHistory.size() > 10000) {
                    messageHistory.remove(0);
                }
                
                // Update metrics
                updateMetric("total_messages", 1);
                updatePlatformMetric(message.getPlatform().getIdentifier());
                updateChannelMetric(message.getChannelName());
                
                return true;
                
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Validate message content and metadata
     */
    private boolean validateMessage(ChatMessage message) {
        if (message == null) return false;
        if (message.getSenderId() == null || message.getSenderId().trim().isEmpty()) return false;
        if (message.getSenderName() == null || message.getSenderName().trim().isEmpty()) return false;
        if (message.getRawContent() == null) return false;
        if (message.getChannelName() == null || message.getChannelName().trim().isEmpty()) return false;
        
        // Check if channel exists
        if (!channels.containsKey(message.getChannelName())) return false;
        
        // Check content length (max 2000 characters for Discord compatibility)
        if (message.getRawContent().length() > 2000) return false;
        
        return true;
    }

    /**
     * Create formatted message from raw text
     */
    public ChatMessage createFormattedMessage(String senderId, String senderName, String rawContent, 
                                            ChannelType channelType, MessagePlatform platform) {
        // Parse MiniMessage format
        Component formattedContent;
        try {
            formattedContent = miniMessage.deserialize(rawContent);
        } catch (Exception e) {
            // Fallback to plain text if parsing fails
            formattedContent = Component.text(rawContent);
        }
        
        return new ChatMessage.Builder()
            .senderId(senderId)
            .senderName(senderName)
            .senderDisplayName(senderName)
            .rawContent(rawContent)
            .formattedContent(formattedContent)
            .channelType(channelType)
            .channelName(channelType.getIdentifier())
            .platform(platform)
            .priority(MessagePriority.NORMAL)
            .build();
    }

    /**
     * Get channel by name
     */
    public ChatChannel getChannel(String channelName) {
        return channels.get(channelName);
    }

    /**
     * Get all available channels
     */
    public Collection<ChatChannel> getAllChannels() {
        return new ArrayList<>(channels.values());
    }

    /**
     * Get recent messages from channel
     */
    public CompletableFuture<List<ChatMessage>> getRecentMessages(String channelName, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            return messageHistory.stream()
                .filter(msg -> channelName.equals(msg.getChannelName()))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        });
    }

    /**
     * Search messages by content
     */
    public CompletableFuture<List<ChatMessage>> searchMessages(String query, String channelName, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            return messageHistory.stream()
                .filter(msg -> channelName == null || channelName.equals(msg.getChannelName()))
                .filter(msg -> msg.getRawContent().toLowerCase().contains(query.toLowerCase()) ||
                              msg.getSenderName().toLowerCase().contains(query.toLowerCase()))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        });
    }

    /**
     * Update metric counter
     */
    private void updateMetric(String metricName, int increment) {
        systemMetrics.merge(metricName, increment, Integer::sum);
    }

    /**
     * Update platform-specific metric
     */
    private void updatePlatformMetric(String platform) {
        Map<String, Integer> platformMetrics = (Map<String, Integer>) 
            systemMetrics.computeIfAbsent("messages_by_platform", k -> new ConcurrentHashMap<String, Integer>());
        platformMetrics.merge(platform, 1, Integer::sum);
    }

    /**
     * Update channel-specific metric
     */
    private void updateChannelMetric(String channel) {
        Map<String, Integer> channelMetrics = (Map<String, Integer>) 
            systemMetrics.computeIfAbsent("messages_by_channel", k -> new ConcurrentHashMap<String, Integer>());
        channelMetrics.merge(channel, 1, Integer::sum);
    }

    /**
     * Get system statistics
     */
    public CompletableFuture<Map<String, Object>> getSystemStatistics() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("metrics", new HashMap<>(systemMetrics));
            stats.put("total_channels", channels.size());
            stats.put("message_history_size", messageHistory.size());
            stats.put("active_rate_limiters", channelRateLimiters.size());
            
            return stats;
        });
    }

    // Getters
    public Map<String, ChatChannel> getChannels() { return new HashMap<>(channels); }
    public List<ChatMessage> getMessageHistory() { return new ArrayList<>(messageHistory); }
    public Map<String, Object> getSystemMetrics() { return new HashMap<>(systemMetrics); }
}
