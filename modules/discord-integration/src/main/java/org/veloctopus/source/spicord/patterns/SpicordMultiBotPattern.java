package org.veloctopus.source.spicord.patterns;

import io.github.jk33v    /**
     * Extracted bot instance pattern from Spicord's DiscordBot
     */
    public static class DiscordBotInstance {
        private final BotPersonality personality;
        private final String token;
        private final boolean enabled;
        private final Set<String> addonKeys;
        private JDA jda;
        private BotStatus status;
        private final Map<String, BotAddon> loadedAddons;
        
        public DiscordBotInstance(BotPersonality personality, String token, boolean enabled, Set<String> addonKeys) {
            this.personality = personality;
            this.token = token;
            this.enabled = enabled;
            this.addonKeys = addonKeys;
            this.status = BotStatus.OFFLINE;
            this.loadedAddons = new ConcurrentHashMap<>();
        }
        
        public CompletableFuture<Void> start() {
            return CompletableFuture.runAsync(() -> {
                try {
                    status = BotStatus.STARTING;
                    logger.info("Starting bot: {} ({})", personality.getDisplayName(), personality.getDescription());
                    
                    JDABuilder builder = JDABuilder.createDefault(token)
                        .enableIntents(personality.getRequiredIntents())
                        .setActivity(Activity.playing("VeloctopusProject"))
                        .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                        .setBulkDeleteSplittingEnabled(false);
                    
                    this.jda = builder.build().awaitReady();
                    status = BotStatus.READY;
                    logger.info("Bot {} is now ready", personality.getDisplayName());
                } catch (Exception e) {
                    status = BotStatus.FAILED;
                    logger.error("Failed to start bot {}", personality.getDisplayName(), e);
                }
            });
        }
        
        public CompletableFuture<Void> shutdown() {
            return CompletableFuture.runAsync(() -> {
                try {
                    status = BotStatus.SHUTTING_DOWN;
                    if (jda != null) {
                        jda.shutdown();
                        if (!jda.awaitShutdown(Duration.ofSeconds(10))) {
                            jda.shutdownNow();
                        }
                    }
                    status = BotStatus.OFFLINE;
                    logger.info("Bot {} shutdown completed", personality.getDisplayName());
                } catch (Exception e) {
                    logger.error("Error during shutdown of bot {}", personality.getDisplayName(), e);
                }
            });
        }

        // Getters
        public BotPersonality getPersonality() { return personality; }
        public String getToken() { return token; }
        public boolean isEnabled() { return enabled; }
        public Set<String> getAddonKeys() { return addonKeys; }
        public BotStatus getStatus() { return status; }
        public Map<String, BotAddon> getLoadedAddons() { return loadedAddons; }
        public JDA getJda() { return jda; }
    }pusrising.api.async.AsyncPattern;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Duration;

/**
 * Extracted and adapted Discord bot management pattern from Spicord.
 * 
 * Original source: org.spicord.bot.DiscordBot (Spicord)
 * License: GNU Affero General Public License v3.0
 * Author: OopsieWoopsie
 * 
 * Adaptations:
 * - Unified async pattern using CompletableFuture
 * - 4-bot architecture specialization (Security Bard, Flora, May, Librarian)
 * - Modular addon system extracted and simplified
 * - VeloctopusProject-compatible configuration
 * 
 * @since VeloctopusProject Phase 1
 */
public class SpicordMultiBotPattern implements AsyncPattern {
    
    private static final Logger logger = LoggerFactory.getLogger(SpicordMultiBotPattern.class);
    
    private final Map<BotPersonality, DiscordBotInstance> bots;
    private final Map<String, BotAddon> registeredAddons;
    
    public SpicordMultiBotPattern() {
        this.bots = new ConcurrentHashMap<>();
        this.registeredAddons = new ConcurrentHashMap<>();
    }
    
    /**
     * Four bot personalities for VeloctopusProject
     */
    public enum BotPersonality {
        SECURITY_BARD("Security Bard", "Security and law enforcement", "🛡️", GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES),
        FLORA("Flora", "Celebration and rewards system", "🌸", GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS),
        MAY("May", "Communication hub and global chat", "💬", GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES),
        LIBRARIAN("Librarian", "Knowledge management and AI queries", "📚", GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES);
        
        private final String displayName;
        private final String description;
        private final String emoji;
        private final Set<GatewayIntent> requiredIntents;
        
        BotPersonality(String displayName, String description, String emoji, GatewayIntent... intents) {
            this.displayName = displayName;
            this.description = description;
            this.emoji = emoji;
            this.requiredIntents = Set.of(intents);
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getEmoji() { return emoji; }
        public Set<GatewayIntent> getRequiredIntents() { return requiredIntents; }
    }
    
    /**
     * Extracted bot instance pattern from Spicord's DiscordBot
     */
    public static class DiscordBotInstance {
        @Getter private final BotPersonality personality;
        @Getter private final String token;
        @Getter private final boolean enabled;
        @Getter private final Set<String> addonKeys;
        @Getter private JDA jda;
        @Getter private BotStatus status;
        @Getter private final Map<String, BotAddon> loadedAddons;
        
        public DiscordBotInstance(BotPersonality personality, String token, boolean enabled, Set<String> addonKeys) {
            this.personality = personality;
            this.token = token;
            this.enabled = enabled;
            this.addonKeys = new HashSet<>(addonKeys);
            this.status = BotStatus.OFFLINE;
            this.loadedAddons = new ConcurrentHashMap<>();
        }
        
        public CompletableFuture<JDA> start() {
            if (!enabled) {
                status = BotStatus.DISABLED;
                return CompletableFuture.completedFuture(null);
            }
            
            return CompletableFuture.supplyAsync(() -> {
                try {
                    status = BotStatus.STARTING;
                    
                    JDABuilder builder = JDABuilder.createDefault(token)
                        .enableIntents(personality.getRequiredIntents())
                        .setActivity(Activity.playing(personality.getDescription()))
                        .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER)
                        .setBulkDeleteSplittingEnabled(false);
                    
                    jda = builder.build().awaitReady();
                    status = BotStatus.READY;
                    
                    logger.info("Discord bot {} ({}) started successfully", 
                        personality.getDisplayName(), jda.getSelfUser().getAsTag());
                    
                    return jda;
                    
                } catch (Exception e) {
                    status = BotStatus.FAILED;
                    logger.error("Failed to start Discord bot {}: {}", personality.getDisplayName(), e.getMessage(), e);
                    throw new RuntimeException("Bot startup failed", e);
                }
            });
        }
        
        public CompletableFuture<Void> shutdown() {
            return CompletableFuture.runAsync(() -> {
                if (jda != null) {
                    status = BotStatus.SHUTTING_DOWN;
                    jda.shutdown();
                    status = BotStatus.OFFLINE;
                    logger.info("Discord bot {} shutdown completed", personality.getDisplayName());
                }
            });
        }
    }
    
    /**
     * Extracted addon pattern from Spicord's SimpleAddon
     */
    public interface BotAddon {
        String getId();
        String getName();
        String getVersion();
        Set<BotPersonality> getSupportedPersonalities();
        
        CompletableFuture<Void> onLoad(DiscordBotInstance bot);
        CompletableFuture<Void> onUnload(DiscordBotInstance bot);
        
        default boolean isCompatible(BotPersonality personality) {
            return getSupportedPersonalities().contains(personality);
        }
    }
    
    /**
     * Bot status enumeration extracted from Spicord patterns
     */
    public enum BotStatus {
        OFFLINE, STARTING, READY, FAILED, SHUTTING_DOWN, DISABLED
    }
    
    @Override
    public CompletableFuture<Set<JDA>> executeAsync() {
        logger.info("Starting 4-bot Discord architecture using Spicord patterns");
        
        return adapter.loadConfiguration()
            .thenCompose(config -> initializeBots(config))
            .thenCompose(this::startAllBots)
            .thenApply(this::extractJDAInstances);
    }
    
    private CompletableFuture<Void> initializeBots(Map<String, Object> config) {
        return CompletableFuture.runAsync(() -> {
            for (BotPersonality personality : BotPersonality.values()) {
                String tokenKey = personality.name().toLowerCase() + "_token";
                String token = (String) config.get(tokenKey);
                boolean enabled = (Boolean) config.getOrDefault(personality.name().toLowerCase() + "_enabled", true);
                
                @SuppressWarnings("unchecked")
                Set<String> addonKeys = (Set<String>) config.getOrDefault(
                    personality.name().toLowerCase() + "_addons", 
                    Set.of()
                );
                
                DiscordBotInstance bot = new DiscordBotInstance(personality, token, enabled, addonKeys);
                bots.put(personality, bot);
                
                log.debug("Initialized bot configuration for {}", personality.getDisplayName());
            }
        });
    }
    
    private CompletableFuture<List<DiscordBotInstance>> startAllBots() {
        List<CompletableFuture<DiscordBotInstance>> startFutures = bots.values().stream()
            .map(bot -> bot.start().thenApply(jda -> bot))
            .toList();
            
        return CompletableFuture.allOf(startFutures.toArray(new CompletableFuture[0]))
            .thenApply(v -> startFutures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList());
    }
    
    private Set<JDA> extractJDAInstances(List<DiscordBotInstance> startedBots) {
        return startedBots.stream()
            .map(DiscordBotInstance::getJda)
            .filter(Objects::nonNull)
            .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }
    
    public CompletableFuture<Void> registerAddon(BotAddon addon) {
        return CompletableFuture.runAsync(() -> {
            registeredAddons.put(addon.getId(), addon);
            
            // Load addon to compatible bots
            bots.values().stream()
                .filter(bot -> addon.isCompatible(bot.getPersonality()))
                .filter(bot -> bot.getAddonKeys().contains(addon.getId()))
                .forEach(bot -> {
                    addon.onLoad(bot).thenRun(() -> {
                        bot.getLoadedAddons().put(addon.getId(), addon);
                        log.info("Loaded addon {} for bot {}", addon.getId(), bot.getPersonality().getDisplayName());
                    }).exceptionally(throwable -> {
                        log.error("Failed to load addon {} for bot {}: {}", 
                            addon.getId(), bot.getPersonality().getDisplayName(), throwable.getMessage());
                        return null;
                    });
                });
                
            log.info("Registered addon: {} v{}", addon.getName(), addon.getVersion());
        });
    }
    
    public CompletableFuture<Void> shutdownAll() {
        log.info("Shutting down all Discord bots");
        
        List<CompletableFuture<Void>> shutdownFutures = bots.values().stream()
            .map(DiscordBotInstance::shutdown)
            .toList();
            
        return CompletableFuture.allOf(shutdownFutures.toArray(new CompletableFuture[0]))
            .thenRun(() -> log.info("All Discord bots shutdown completed"));
    }
    
    public Optional<DiscordBotInstance> getBotByPersonality(BotPersonality personality) {
        return Optional.ofNullable(bots.get(personality));
    }
    
    public boolean isAllBotsReady() {
        return bots.values().stream()
            .filter(bot -> bot.isEnabled())
            .allMatch(bot -> bot.getStatus() == BotStatus.READY);
    }
}
