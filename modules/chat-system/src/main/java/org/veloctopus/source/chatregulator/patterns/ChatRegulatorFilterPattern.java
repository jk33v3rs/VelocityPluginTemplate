package org.veloctopus.source.chatregulator.patterns;

import org.veloctopus.api.patterns.AsyncPattern;
import org.veloctopus.adaptation.chatregulator.ChatRegulatorAsyncAdapter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.logging.Logger;

/**
 * Extracted and adapted message filtering and moderation patterns from ChatRegulator.
 * 
 * Original source: io.github._4drian3d.chatregulator.api.checks.* (ChatRegulator)
 * License: GNU General Public License v3.0
 * Author: 4drian3d
 * 
 * Adaptations:
 * - Unified async pattern using CompletableFuture
 * - Cross-platform message filtering (Minecraft ↔ Discord ↔ Matrix)
 * - VeloctopusProject-compatible event system
 * - Modular check system for different chat platforms
 * 
 * @since VeloctopusProject Phase 1
 */
public class ChatRegulatorFilterPattern implements AsyncPattern<ChatModerationEngine> {
    
    private static final Logger log = Logger.getLogger(ChatRegulatorFilterPattern.class.getName());
    
    private final ChatRegulatorAsyncAdapter adapter;
    private final Map<String, InfractionPlayer> playerData;
    private final Map<CheckType, MessageCheck> activeChecks;
    private final GlobalStatistics statistics;
    
    public ChatRegulatorFilterPattern(ChatRegulatorAsyncAdapter adapter) {
        this.adapter = adapter;
        this.playerData = new ConcurrentHashMap<>();
        this.activeChecks = new ConcurrentHashMap<>();
        this.statistics = new GlobalStatistics();
    }
    
    /**
     * Extracted check types from ChatRegulator's check system
     */
    public enum CheckType {
        SPAM("Consecutive identical messages", InfractionSeverity.MEDIUM),
        FLOOD("Message rate limiting", InfractionSeverity.HIGH),
        CAPS("Excessive capitalization", InfractionSeverity.LOW),
        REGEX("Pattern-based content filtering", InfractionSeverity.HIGH),
        UNICODE("Unicode character restrictions", InfractionSeverity.MEDIUM),
        SYNTAX("Command syntax violations", InfractionSeverity.LOW),
        COOLDOWN("Message frequency limiting", InfractionSeverity.MEDIUM);
        
        private final String description;
        private final InfractionSeverity severity;
        
        CheckType(String description, InfractionSeverity severity) {
            this.description = description;
            this.severity = severity;
        }
        
        public String getDescription() { return description; }
        public InfractionSeverity getSeverity() { return severity; }
    }
    
    /**
     * Extracted infraction severity from ChatRegulator patterns
     */
    public enum InfractionSeverity {
        LOW(1), MEDIUM(3), HIGH(5), CRITICAL(10);
        
        private final int weight;
        
        InfractionSeverity(int weight) {
            this.weight = weight;
        }
        
        public int getWeight() { return weight; }
    }
    
    /**
     * Extracted source types from ChatRegulator for cross-platform support
     */
    public enum SourceType {
        MINECRAFT_CHAT("Minecraft server chat"),
        MINECRAFT_COMMAND("Minecraft server commands"),
        DISCORD_MESSAGE("Discord bot messages"),
        MATRIX_MESSAGE("Matrix bridge messages");
        
        private final String description;
        
        SourceType(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    /**
     * Extracted check result pattern from ChatRegulator's CheckResult
     */
    public static class CheckResult {
        private final boolean allowed;
        private final CheckType violatedCheck;
        private final String reason;
        private final InfractionSeverity severity;
        
        private CheckResult(boolean allowed, CheckType violatedCheck, String reason, InfractionSeverity severity) {
            this.allowed = allowed;
            this.violatedCheck = violatedCheck;
            this.reason = reason;
            this.severity = severity;
        }
        
        public static CheckResult allowed() {
            return new CheckResult(true, null, null, null);
        }
        
        public static CheckResult denied(CheckType checkType, String reason) {
            return new CheckResult(false, checkType, reason, checkType.getSeverity());
        }
        
        public boolean isAllowed() { return allowed; }
        public CheckType getViolatedCheck() { return violatedCheck; }
        public String getReason() { return reason; }
        public InfractionSeverity getSeverity() { return severity; }
    }
    
    /**
     * Extracted player infraction tracking from ChatRegulator's InfractionPlayer
     */
    public static class InfractionPlayer {
        private final String playerId;
        private final String playerName;
        private final Map<SourceType, List<String>> messageChains;
        private final Map<CheckType, Integer> infractionCounts;
        private final long firstSeen;
        private long lastMessage;
        
        public InfractionPlayer(String playerId, String playerName) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.messageChains = new ConcurrentHashMap<>();
            this.infractionCounts = new ConcurrentHashMap<>();
            this.firstSeen = System.currentTimeMillis();
            this.lastMessage = firstSeen;
            
            // Initialize message chains for all source types
            for (SourceType type : SourceType.values()) {
                messageChains.put(type, new ArrayList<>());
            }
        }
        
        public void recordMessage(SourceType source, String message) {
            List<String> chain = messageChains.get(source);
            chain.add(message);
            
            // Limit chain size (ChatRegulator pattern - keep last N messages)
            if (chain.size() > 10) {
                chain.remove(0);
            }
            
            lastMessage = System.currentTimeMillis();
        }
        
        public void recordInfraction(CheckType checkType) {
            infractionCounts.merge(checkType, 1, Integer::sum);
        }
        
        public List<String> getMessageChain(SourceType source) {
            return new ArrayList<>(messageChains.get(source));
        }
        
        public int getInfractionCount(CheckType checkType) {
            return infractionCounts.getOrDefault(checkType, 0);
        }
        
        public long getTimeSinceLastMessage() {
            return System.currentTimeMillis() - lastMessage;
        }
        
        public String getPlayerId() { return playerId; }
        public String getPlayerName() { return playerName; }
    }
    
    /**
     * Extracted message check interface from ChatRegulator's Check interface
     */
    public interface MessageCheck {
        CheckResult check(InfractionPlayer player, String message, SourceType source);
        CheckType getType();
        boolean isEnabled();
    }
    
    /**
     * Spam detection check extracted from ChatRegulator's SpamCheck
     */
    public static class SpamCheck implements MessageCheck {
        private final int similarLimit;
        private final SourceType[] supportedSources;
        
        public SpamCheck(int similarLimit, SourceType... supportedSources) {
            this.similarLimit = similarLimit;
            this.supportedSources = supportedSources.length > 0 ? supportedSources : SourceType.values();
        }
        
        @Override
        public CheckResult check(InfractionPlayer player, String message, SourceType source) {
            if (!Arrays.asList(supportedSources).contains(source)) {
                return CheckResult.allowed();
            }
            
            List<String> chain = player.getMessageChain(source);
            if (chain.size() < similarLimit) {
                return CheckResult.allowed();
            }
            
            // Check if recent messages are identical (ChatRegulator pattern)
            boolean allSimilar = true;
            for (int i = chain.size() - similarLimit; i < chain.size(); i++) {
                if (!chain.get(i).equalsIgnoreCase(message)) {
                    allSimilar = false;
                    break;
                }
            }
            
            if (allSimilar) {
                return CheckResult.denied(CheckType.SPAM, 
                    String.format("Repeated message %d times in a row", similarLimit));
            }
            
            return CheckResult.allowed();
        }
        
        @Override
        public CheckType getType() { return CheckType.SPAM; }
        
        @Override
        public boolean isEnabled() { return true; }
    }
    
    /**
     * Regex-based content filtering extracted from ChatRegulator's RegexCheck
     */
    public static class RegexCheck implements MessageCheck {
        private final List<Pattern> forbiddenPatterns;
        private final SourceType[] supportedSources;
        
        public RegexCheck(List<String> regexPatterns, SourceType... supportedSources) {
            this.forbiddenPatterns = regexPatterns.stream()
                .map(Pattern::compile)
                .toList();
            this.supportedSources = supportedSources.length > 0 ? supportedSources : SourceType.values();
        }
        
        @Override
        public CheckResult check(InfractionPlayer player, String message, SourceType source) {
            if (!Arrays.asList(supportedSources).contains(source)) {
                return CheckResult.allowed();
            }
            
            for (Pattern pattern : forbiddenPatterns) {
                Matcher matcher = pattern.matcher(message);
                if (matcher.find()) {
                    return CheckResult.denied(CheckType.REGEX, 
                        String.format("Message contains forbidden pattern: %s", pattern.pattern()));
                }
            }
            
            return CheckResult.allowed();
        }
        
        @Override
        public CheckType getType() { return CheckType.REGEX; }
        
        @Override
        public boolean isEnabled() { return !forbiddenPatterns.isEmpty(); }
    }
    
    /**
     * Global statistics tracking extracted from ChatRegulator's Statistics
     */
    public static class GlobalStatistics {
        private final Map<CheckType, Long> infractionCounts;
        private final Map<SourceType, Long> messageCounts;
        private long totalMessagesProcessed;
        
        public GlobalStatistics() {
            this.infractionCounts = new ConcurrentHashMap<>();
            this.messageCounts = new ConcurrentHashMap<>();
            this.totalMessagesProcessed = 0;
            
            // Initialize counters
            for (CheckType type : CheckType.values()) {
                infractionCounts.put(type, 0L);
            }
            for (SourceType type : SourceType.values()) {
                messageCounts.put(type, 0L);
            }
        }
        
        public void recordMessage(SourceType source) {
            messageCounts.merge(source, 1L, Long::sum);
            totalMessagesProcessed++;
        }
        
        public void recordInfraction(CheckType checkType) {
            infractionCounts.merge(checkType, 1L, Long::sum);
        }
        
        public long getInfractionCount(CheckType checkType) {
            return infractionCounts.get(checkType);
        }
        
        public long getMessageCount(SourceType source) {
            return messageCounts.get(source);
        }
        
        public long getTotalMessagesProcessed() {
            return totalMessagesProcessed;
        }
    }
    
    /**
     * Main moderation engine combining all ChatRegulator patterns
     */
    public static class ChatModerationEngine {
        private final Map<CheckType, MessageCheck> checks;
        private final Map<String, InfractionPlayer> players;
        private final GlobalStatistics statistics;
        
        public ChatModerationEngine() {
            this.checks = new ConcurrentHashMap<>();
            this.players = new ConcurrentHashMap<>();
            this.statistics = new GlobalStatistics();
        }
        
        public void registerCheck(MessageCheck check) {
            checks.put(check.getType(), check);
        }
        
        public CompletableFuture<CheckResult> processMessage(String playerId, String playerName, 
                                                           String message, SourceType source) {
            return CompletableFuture.supplyAsync(() -> {
                InfractionPlayer player = players.computeIfAbsent(playerId, 
                    id -> new InfractionPlayer(id, playerName));
                
                statistics.recordMessage(source);
                player.recordMessage(source, message);
                
                // Run all enabled checks
                for (MessageCheck check : checks.values()) {
                    if (!check.isEnabled()) continue;
                    
                    CheckResult result = check.check(player, message, source);
                    if (!result.isAllowed()) {
                        statistics.recordInfraction(result.getViolatedCheck());
                        player.recordInfraction(result.getViolatedCheck());
                        return result;
                    }
                }
                
                return CheckResult.allowed();
            });
        }
        
        public InfractionPlayer getPlayer(String playerId) {
            return players.get(playerId);
        }
        
        public GlobalStatistics getStatistics() {
            return statistics;
        }
    }
    
    @Override
    public CompletableFuture<ChatModerationEngine> executeAsync() {
        log.info("Initializing ChatRegulator filtering patterns for cross-platform moderation");
        
        return adapter.loadFilterConfiguration()
            .thenCompose(config -> buildModerationEngine(config))
            .thenApply(this::configureChatRegulatorPatterns);
    }
    
    private CompletableFuture<ChatModerationEngine> buildModerationEngine(Map<String, Object> config) {
        return CompletableFuture.supplyAsync(() -> {
            ChatModerationEngine engine = new ChatModerationEngine();
            
            // Configure spam detection (ChatRegulator pattern)
            if ((Boolean) config.getOrDefault("spam_check_enabled", true)) {
                int spamLimit = (Integer) config.getOrDefault("spam_similar_limit", 3);
                engine.registerCheck(new SpamCheck(spamLimit));
                log.info("Registered spam check with limit: " + spamLimit);
            }
            
            // Configure regex filtering (ChatRegulator pattern)
            if ((Boolean) config.getOrDefault("regex_check_enabled", true)) {
                @SuppressWarnings("unchecked")
                List<String> patterns = (List<String>) config.getOrDefault("forbidden_patterns", 
                    List.of("(?i).*badword.*", "(?i).*spam.*", "(?i).*advertise.*"));
                engine.registerCheck(new RegexCheck(patterns));
                log.info("Registered regex check with " + patterns.size() + " patterns");
            }
            
            return engine;
        });
    }
    
    private ChatModerationEngine configureChatRegulatorPatterns(ChatModerationEngine engine) {
        log.info("ChatRegulator moderation engine configured with cross-platform support");
        return engine;
    }
}
