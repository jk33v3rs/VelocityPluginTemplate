package org.veloctopus.source.kickredirect.patterns;

import org.veloctopus.api.patterns.AsyncPattern;
import org.veloctopus.adaptation.kickredirect.KickRedirectAsyncAdapter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * Extracted and adapted server management and routing patterns from KickRedirect.
 * 
 * Original source: io.github._4drian3d.kickredirect.* (KickRedirect)
 * License: GNU General Public License v3.0
 * Author: 4drian3d
 * 
 * Adaptations:
 * - Unified async pattern using CompletableFuture
 * - Multi-platform server routing (Minecraft servers + Discord bots + Matrix rooms)
 * - VeloctopusProject-compatible player flow management
 * - Advanced routing strategies for different scenarios
 * 
 * @since VeloctopusProject Phase 1
 */
public class KickRedirectRoutingPattern implements AsyncPattern<ServerRoutingEngine> {
    
    private static final Logger log = Logger.getLogger(KickRedirectRoutingPattern.class.getName());
    
    private final KickRedirectAsyncAdapter adapter;
    private final Map<String, RoutingTarget> availableTargets;
    private final Map<String, PlayerRoutingSession> activeSessions;
    private final RoutingStatistics statistics;
    
    public KickRedirectRoutingPattern(KickRedirectAsyncAdapter adapter) {
        this.adapter = adapter;
        this.availableTargets = new ConcurrentHashMap<>();
        this.activeSessions = new ConcurrentHashMap<>();
        this.statistics = new RoutingStatistics();
    }
    
    /**
     * Extracted routing modes from KickRedirect's SendMode enum
     */
    public enum RoutingMode {
        TO_FIRST("Route to first available target"),
        TO_EMPTIEST("Route to target with least players/users"),
        RANDOM("Route to random available target"),
        LOAD_BALANCED("Distribute load evenly across targets"),
        PRIORITY_BASED("Route based on target priority weights"),
        INTELLIGENT("AI-assisted routing based on player behavior");
        
        private final String description;
        
        RoutingMode(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    /**
     * Extended target types for multi-platform routing
     */
    public enum TargetType {
        MINECRAFT_SERVER("Minecraft/Velocity backend server"),
        DISCORD_VOICE_CHANNEL("Discord voice channel"),
        DISCORD_TEXT_CHANNEL("Discord text channel"),
        MATRIX_ROOM("Matrix bridge room"),
        FALLBACK_LOBBY("Emergency fallback lobby");
        
        private final String description;
        
        TargetType(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    /**
     * Extracted kick/disconnect reasons from KickRedirect patterns
     */
    public enum DisconnectReason {
        SERVER_SHUTDOWN("Server is shutting down"),
        SERVER_RESTART("Server is restarting"),
        PLAYER_KICKED("Player was kicked by staff"),
        CONNECTION_LOST("Connection lost"),
        TIMEOUT("Connection timeout"),
        MAINTENANCE("Server under maintenance"),
        FULL_SERVER("Server is full"),
        WHITELIST_VIOLATION("Player not whitelisted"),
        PERMISSION_DENIED("Insufficient permissions");
        
        private final String description;
        
        DisconnectReason(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    /**
     * Routing target representation (server, Discord channel, Matrix room, etc.)
     */
    public static class RoutingTarget {
        private final String id;
        private final String name;
        private final TargetType type;
        private final int priority;
        private final int maxCapacity;
        private final boolean isAvailable;
        private final Map<String, Object> metadata;
        private int currentLoad;
        
        public RoutingTarget(String id, String name, TargetType type, int priority, int maxCapacity) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.priority = priority;
            this.maxCapacity = maxCapacity;
            this.isAvailable = true;
            this.metadata = new ConcurrentHashMap<>();
            this.currentLoad = 0;
        }
        
        public boolean canAcceptPlayer() {
            return isAvailable && (maxCapacity <= 0 || currentLoad < maxCapacity);
        }
        
        public void addPlayer() {
            currentLoad++;
        }
        
        public void removePlayer() {
            if (currentLoad > 0) currentLoad--;
        }
        
        public double getLoadPercentage() {
            if (maxCapacity <= 0) return 0.0;
            return (double) currentLoad / maxCapacity * 100.0;
        }
        
        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public TargetType getType() { return type; }
        public int getPriority() { return priority; }
        public int getMaxCapacity() { return maxCapacity; }
        public boolean isAvailable() { return isAvailable; }
        public int getCurrentLoad() { return currentLoad; }
        public Map<String, Object> getMetadata() { return metadata; }
    }
    
    /**
     * Player routing session tracking
     */
    public static class PlayerRoutingSession {
        private final String playerId;
        private final String playerName;
        private final long sessionStart;
        private final List<RoutingAttempt> attempts;
        private RoutingTarget currentTarget;
        
        public PlayerRoutingSession(String playerId, String playerName) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.sessionStart = System.currentTimeMillis();
            this.attempts = new ArrayList<>();
        }
        
        public void addAttempt(RoutingTarget target, DisconnectReason reason, boolean successful) {
            attempts.add(new RoutingAttempt(target, reason, successful, System.currentTimeMillis()));
            if (successful) {
                currentTarget = target;
            }
        }
        
        public int getAttemptCount() {
            return attempts.size();
        }
        
        public long getSessionDuration() {
            return System.currentTimeMillis() - sessionStart;
        }
        
        // Getters
        public String getPlayerId() { return playerId; }
        public String getPlayerName() { return playerName; }
        public List<RoutingAttempt> getAttempts() { return new ArrayList<>(attempts); }
        public RoutingTarget getCurrentTarget() { return currentTarget; }
    }
    
    /**
     * Individual routing attempt record
     */
    public static class RoutingAttempt {
        private final RoutingTarget target;
        private final DisconnectReason reason;
        private final boolean successful;
        private final long timestamp;
        
        public RoutingAttempt(RoutingTarget target, DisconnectReason reason, boolean successful, long timestamp) {
            this.target = target;
            this.reason = reason;
            this.successful = successful;
            this.timestamp = timestamp;
        }
        
        // Getters
        public RoutingTarget getTarget() { return target; }
        public DisconnectReason getReason() { return reason; }
        public boolean isSuccessful() { return successful; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Routing result from KickRedirect patterns
     */
    public static class RoutingResult {
        private final RoutingTarget target;
        private final boolean successful;
        private final String message;
        private final RoutingMode modeUsed;
        
        private RoutingResult(RoutingTarget target, boolean successful, String message, RoutingMode modeUsed) {
            this.target = target;
            this.successful = successful;
            this.message = message;
            this.modeUsed = modeUsed;
        }
        
        public static RoutingResult success(RoutingTarget target, RoutingMode mode) {
            return new RoutingResult(target, true, "Successfully routed to " + target.getName(), mode);
        }
        
        public static RoutingResult failure(String reason) {
            return new RoutingResult(null, false, reason, null);
        }
        
        // Getters
        public RoutingTarget getTarget() { return target; }
        public boolean isSuccessful() { return successful; }
        public String getMessage() { return message; }
        public RoutingMode getModeUsed() { return modeUsed; }
    }
    
    /**
     * Routing engine with KickRedirect patterns adapted for multi-platform use
     */
    public static class ServerRoutingEngine {
        private final Map<String, RoutingTarget> targets;
        private final Map<String, PlayerRoutingSession> sessions;
        private final RoutingStatistics statistics;
        private RoutingMode defaultMode;
        private final Map<TargetType, List<String>> priorityLists;
        
        public ServerRoutingEngine() {
            this.targets = new ConcurrentHashMap<>();
            this.sessions = new ConcurrentHashMap<>();
            this.statistics = new RoutingStatistics();
            this.defaultMode = RoutingMode.TO_EMPTIEST;
            this.priorityLists = new ConcurrentHashMap<>();
            
            // Initialize priority lists for each target type
            for (TargetType type : TargetType.values()) {
                priorityLists.put(type, new ArrayList<>());
            }
        }
        
        public void registerTarget(RoutingTarget target) {
            targets.put(target.getId(), target);
            priorityLists.get(target.getType()).add(target.getId());
            log.info("Registered routing target: " + target.getName() + " (" + target.getType() + ")");
        }
        
        /**
         * Route player using specified mode - extracted from KickRedirect's SendMode logic
         */
        public CompletableFuture<RoutingResult> routePlayer(String playerId, String playerName, 
                                                          DisconnectReason reason, TargetType preferredType, 
                                                          RoutingMode mode) {
            return CompletableFuture.supplyAsync(() -> {
                PlayerRoutingSession session = sessions.computeIfAbsent(playerId, 
                    id -> new PlayerRoutingSession(id, playerName));
                
                List<RoutingTarget> candidateTargets = getCandidateTargets(preferredType);
                if (candidateTargets.isEmpty()) {
                    RoutingResult result = RoutingResult.failure("No available targets for type: " + preferredType);
                    session.addAttempt(null, reason, false);
                    statistics.recordFailedRouting(preferredType, reason);
                    return result;
                }
                
                RoutingTarget selected = selectTarget(candidateTargets, mode);
                if (selected == null) {
                    RoutingResult result = RoutingResult.failure("All targets are full or unavailable");
                    session.addAttempt(null, reason, false);
                    statistics.recordFailedRouting(preferredType, reason);
                    return result;
                }
                
                // Update target load
                selected.addPlayer();
                session.addAttempt(selected, reason, true);
                statistics.recordSuccessfulRouting(selected.getType(), mode);
                
                log.info("Routed player " + playerName + " to " + selected.getName() + " using " + mode);
                return RoutingResult.success(selected, mode);
            });
        }
        
        private List<RoutingTarget> getCandidateTargets(TargetType type) {
            return priorityLists.get(type).stream()
                .map(targets::get)
                .filter(Objects::nonNull)
                .filter(RoutingTarget::canAcceptPlayer)
                .toList();
        }
        
        /**
         * Select target using routing mode - adapted from KickRedirect's SendMode implementations
         */
        private RoutingTarget selectTarget(List<RoutingTarget> candidates, RoutingMode mode) {
            return switch (mode) {
                case TO_FIRST -> candidates.get(0);
                case TO_EMPTIEST -> candidates.stream()
                    .min(Comparator.comparingInt(RoutingTarget::getCurrentLoad))
                    .orElse(null);
                case RANDOM -> candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
                case LOAD_BALANCED -> selectLoadBalanced(candidates);
                case PRIORITY_BASED -> candidates.stream()
                    .max(Comparator.comparingInt(RoutingTarget::getPriority))
                    .orElse(null);
                case INTELLIGENT -> selectIntelligent(candidates);
            };
        }
        
        private RoutingTarget selectLoadBalanced(List<RoutingTarget> candidates) {
            // Select target with lowest load percentage
            return candidates.stream()
                .min(Comparator.comparingDouble(RoutingTarget::getLoadPercentage))
                .orElse(candidates.get(0));
        }
        
        private RoutingTarget selectIntelligent(List<RoutingTarget> candidates) {
            // Placeholder for AI-assisted selection - would analyze player behavior patterns
            // For now, use load-balanced approach
            return selectLoadBalanced(candidates);
        }
        
        public void setDefaultMode(RoutingMode mode) {
            this.defaultMode = mode;
        }
        
        public RoutingMode getDefaultMode() {
            return defaultMode;
        }
        
        public PlayerRoutingSession getPlayerSession(String playerId) {
            return sessions.get(playerId);
        }
        
        public RoutingStatistics getStatistics() {
            return statistics;
        }
        
        public Collection<RoutingTarget> getTargets() {
            return targets.values();
        }
    }
    
    /**
     * Routing statistics tracking
     */
    public static class RoutingStatistics {
        private final Map<TargetType, Long> successfulRoutings;
        private final Map<TargetType, Long> failedRoutings;
        private final Map<RoutingMode, Long> modeUsage;
        private final Map<DisconnectReason, Long> reasonCounts;
        
        public RoutingStatistics() {
            this.successfulRoutings = new ConcurrentHashMap<>();
            this.failedRoutings = new ConcurrentHashMap<>();
            this.modeUsage = new ConcurrentHashMap<>();
            this.reasonCounts = new ConcurrentHashMap<>();
            
            // Initialize counters
            for (TargetType type : TargetType.values()) {
                successfulRoutings.put(type, 0L);
                failedRoutings.put(type, 0L);
            }
            for (RoutingMode mode : RoutingMode.values()) {
                modeUsage.put(mode, 0L);
            }
            for (DisconnectReason reason : DisconnectReason.values()) {
                reasonCounts.put(reason, 0L);
            }
        }
        
        public void recordSuccessfulRouting(TargetType type, RoutingMode mode) {
            successfulRoutings.merge(type, 1L, Long::sum);
            modeUsage.merge(mode, 1L, Long::sum);
        }
        
        public void recordFailedRouting(TargetType type, DisconnectReason reason) {
            failedRoutings.merge(type, 1L, Long::sum);
            reasonCounts.merge(reason, 1L, Long::sum);
        }
        
        public double getSuccessRate(TargetType type) {
            long successful = successfulRoutings.get(type);
            long failed = failedRoutings.get(type);
            long total = successful + failed;
            return total > 0 ? (double) successful / total * 100.0 : 0.0;
        }
        
        public long getSuccessfulRoutings(TargetType type) {
            return successfulRoutings.get(type);
        }
        
        public long getFailedRoutings(TargetType type) {
            return failedRoutings.get(type);
        }
        
        public long getModeUsage(RoutingMode mode) {
            return modeUsage.get(mode);
        }
        
        public long getReasonCount(DisconnectReason reason) {
            return reasonCounts.get(reason);
        }
    }
    
    @Override
    public CompletableFuture<ServerRoutingEngine> executeAsync() {
        log.info("Initializing KickRedirect routing patterns for multi-platform server management");
        
        return adapter.loadRoutingConfiguration()
            .thenCompose(config -> buildRoutingEngine(config))
            .thenApply(this::configureKickRedirectPatterns);
    }
    
    private CompletableFuture<ServerRoutingEngine> buildRoutingEngine(Map<String, Object> config) {
        return CompletableFuture.supplyAsync(() -> {
            ServerRoutingEngine engine = new ServerRoutingEngine();
            
            // Configure default routing mode
            String defaultModeStr = (String) config.getOrDefault("default_routing_mode", "TO_EMPTIEST");
            try {
                RoutingMode defaultMode = RoutingMode.valueOf(defaultModeStr);
                engine.setDefaultMode(defaultMode);
                log.info("Set default routing mode: " + defaultMode);
            } catch (IllegalArgumentException e) {
                log.warning("Invalid routing mode: " + defaultModeStr + ", using TO_EMPTIEST");
                engine.setDefaultMode(RoutingMode.TO_EMPTIEST);
            }
            
            // Register routing targets from configuration
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> targets = (List<Map<String, Object>>) config.getOrDefault("routing_targets", 
                List.of());
            
            for (Map<String, Object> targetConfig : targets) {
                String id = (String) targetConfig.get("id");
                String name = (String) targetConfig.get("name");
                String typeStr = (String) targetConfig.get("type");
                int priority = (Integer) targetConfig.getOrDefault("priority", 1);
                int maxCapacity = (Integer) targetConfig.getOrDefault("max_capacity", 0);
                
                try {
                    TargetType type = TargetType.valueOf(typeStr);
                    RoutingTarget target = new RoutingTarget(id, name, type, priority, maxCapacity);
                    engine.registerTarget(target);
                } catch (IllegalArgumentException e) {
                    log.warning("Invalid target type: " + typeStr + " for target: " + name);
                }
            }
            
            return engine;
        });
    }
    
    private ServerRoutingEngine configureKickRedirectPatterns(ServerRoutingEngine engine) {
        log.info("KickRedirect routing engine configured with " + engine.getTargets().size() + " targets");
        return engine;
    }
}
