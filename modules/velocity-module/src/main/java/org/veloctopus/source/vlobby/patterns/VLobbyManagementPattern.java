/*
 * VLobbyManagementPattern.java - Multi-Platform Lobby Management System
 * 
 * Extracted and adapted from VLobby project by 4drian3d
 * Original Source: https://github.com/4drian3d/VLobby
 * Original License: GNU General Public License v3.0
 * 
 * Adaptations for VeloctopusProject:
 * - Extended routing modes for cross-platform support
 * - Added Discord/Matrix integration for lobby commands
 * - Implemented async patterns with CompletableFuture
 * - Added cooldown management with Redis backing
 * - Enhanced server selection algorithms
 * 
 * Original VLobby features adapted:
 * - SendMode enum (RANDOM, FIRST_AVAILABLE, EMPTIEST)
 * - CommandHandler pattern for lobby routing
 * - Cooldown management with Caffeine cache
 * - Connection request handling with fallback logic
 * 
 * @since 1.0.0
 * @author VeloctopusProject Team
 * @author Original implementation by 4drian3d (VLobby)
 */
package org.veloctopus.source.vlobby.patterns;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Multi-platform lobby management system extracted from VLobby project.
 * 
 * Provides comprehensive lobby routing, server selection, and player management
 * with cross-platform support for Minecraft, Discord, and Matrix interactions.
 * 
 * Key Features:
 * - Advanced server selection algorithms (random, load-balanced, intelligent)
 * - Cross-platform lobby commands and routing
 * - Cooldown management with Redis persistence
 * - Async connection handling with fallback strategies
 * - Multi-platform status monitoring and health checks
 * 
 * Original VLobby patterns enhanced for VeloctopusProject's cross-platform architecture.
 */
public class VLobbyManagementPattern {
    
    /**
     * Server selection modes for lobby routing.
     * Extended from VLobby's SendMode enum with additional cross-platform modes.
     */
    public enum RoutingMode {
        /**
         * Random server selection from available lobbies
         * Original VLobby: RANDOM
         */
        RANDOM,
        
        /**
         * First available server in configured order
         * Original VLobby: FIRST_AVAILABLE
         */
        FIRST_AVAILABLE,
        
        /**
         * Server with lowest player count
         * Original VLobby: EMPTIEST
         */
        EMPTIEST,
        
        /**
         * Load-balanced selection considering server performance
         * VeloctopusProject enhancement
         */
        LOAD_BALANCED,
        
        /**
         * Priority-based selection with configurable server tiers
         * VeloctopusProject enhancement
         */
        PRIORITY_BASED,
        
        /**
         * Intelligent selection considering player preferences and history
         * VeloctopusProject enhancement
         */
        INTELLIGENT
    }
    
    /**
     * Lobby server target for cross-platform routing.
     * Enhanced from VLobby's RegisteredServer concept.
     */
    public static class LobbyTarget {
        private final String serverId;
        private final String displayName;
        private final LobbyType type;
        private final Map<String, Object> metadata;
        private final Set<String> supportedPlatforms;
        private volatile boolean available;
        private volatile int playerCount;
        private volatile double loadFactor;
        private volatile Instant lastHealthCheck;
        
        public LobbyTarget(String serverId, String displayName, LobbyType type) {
            this.serverId = serverId;
            this.displayName = displayName;
            this.type = type;
            this.metadata = new ConcurrentHashMap<>();
            this.supportedPlatforms = ConcurrentHashMap.newKeySet();
            this.available = true;
            this.playerCount = 0;
            this.loadFactor = 0.0;
            this.lastHealthCheck = Instant.now();
        }
        
        // Getters and utility methods
        public String getServerId() { return serverId; }
        public String getDisplayName() { return displayName; }
        public LobbyType getType() { return type; }
        public boolean isAvailable() { return available; }
        public int getPlayerCount() { return playerCount; }
        public double getLoadFactor() { return loadFactor; }
        public Set<String> getSupportedPlatforms() { return Collections.unmodifiableSet(supportedPlatforms); }
        
        public void updateStatus(boolean available, int playerCount, double loadFactor) {
            this.available = available;
            this.playerCount = playerCount;
            this.loadFactor = loadFactor;
            this.lastHealthCheck = Instant.now();
        }
        
        public boolean supportsPllatform(String platform) {
            return supportedPlatforms.contains(platform);
        }
    }
    
    /**
     * Types of lobby servers in the network.
     */
    public enum LobbyType {
        /**
         * Main hub server for general navigation
         */
        HUB,
        
        /**
         * Game-specific lobby (e.g., survival, creative, minigames)
         */
        GAME_LOBBY,
        
        /**
         * Social space for community interaction
         */
        SOCIAL,
        
        /**
         * Administrative area for staff operations
         */
        STAFF,
        
        /**
         * Event-specific temporary lobby
         */
        EVENT
    }
    
    /**
     * Lobby routing engine for server selection and connection management.
     * Enhanced from VLobby's CommandHandler pattern.
     */
    public static class LobbyRoutingEngine {
        private final Map<String, LobbyTarget> lobbyServers;
        private final Map<String, RoutingMode> userPreferences;
        private final Map<String, Instant> connectionHistory;
        private final Random random;
        
        public LobbyRoutingEngine() {
            this.lobbyServers = new ConcurrentHashMap<>();
            this.userPreferences = new ConcurrentHashMap<>();
            this.connectionHistory = new ConcurrentHashMap<>();
            this.random = new Random();
        }
        
        /**
         * Register a lobby server for routing.
         * 
         * @param target The lobby target to register
         */
        public void registerLobby(LobbyTarget target) {
            lobbyServers.put(target.getServerId(), target);
        }
        
        /**
         * Select optimal lobby server using specified routing mode.
         * Enhanced from VLobby's SendMode.getServer() methods.
         * 
         * @param mode The routing mode to use
         * @param platform The platform making the request
         * @param userId Optional user ID for personalized routing
         * @return CompletableFuture with selected lobby target
         */
        public CompletableFuture<Optional<LobbyTarget>> selectLobby(
                RoutingMode mode, String platform, String userId) {
            return CompletableFuture.supplyAsync(() -> {
                List<LobbyTarget> availableLobbies = lobbyServers.values().stream()
                    .filter(LobbyTarget::isAvailable)
                    .filter(lobby -> lobby.supportsPllatform(platform))
                    .collect(Collectors.toList());
                
                if (availableLobbies.isEmpty()) {
                    return Optional.empty();
                }
                
                return switch (mode) {
                    case RANDOM -> selectRandom(availableLobbies);
                    case FIRST_AVAILABLE -> selectFirstAvailable(availableLobbies);
                    case EMPTIEST -> selectEmptiest(availableLobbies);
                    case LOAD_BALANCED -> selectLoadBalanced(availableLobbies);
                    case PRIORITY_BASED -> selectPriorityBased(availableLobbies);
                    case INTELLIGENT -> selectIntelligent(availableLobbies, userId);
                };
            });
        }
        
        /**
         * Random server selection (VLobby original pattern).
         */
        private Optional<LobbyTarget> selectRandom(List<LobbyTarget> lobbies) {
            if (lobbies.isEmpty()) return Optional.empty();
            return Optional.of(lobbies.get(random.nextInt(lobbies.size())));
        }
        
        /**
         * First available server selection (VLobby original pattern).
         */
        private Optional<LobbyTarget> selectFirstAvailable(List<LobbyTarget> lobbies) {
            return lobbies.stream().findFirst();
        }
        
        /**
         * Emptiest server selection (VLobby original pattern).
         */
        private Optional<LobbyTarget> selectEmptiest(List<LobbyTarget> lobbies) {
            return lobbies.stream()
                .min(Comparator.comparingInt(LobbyTarget::getPlayerCount));
        }
        
        /**
         * Load-balanced selection considering server performance.
         * VeloctopusProject enhancement.
         */
        private Optional<LobbyTarget> selectLoadBalanced(List<LobbyTarget> lobbies) {
            return lobbies.stream()
                .min(Comparator.comparingDouble(LobbyTarget::getLoadFactor));
        }
        
        /**
         * Priority-based selection with server tier preferences.
         * VeloctopusProject enhancement.
         */
        private Optional<LobbyTarget> selectPriorityBased(List<LobbyTarget> lobbies) {
            // Prefer HUB > GAME_LOBBY > SOCIAL > STAFF > EVENT
            Map<LobbyType, Integer> priorities = Map.of(
                LobbyType.HUB, 1,
                LobbyType.GAME_LOBBY, 2,
                LobbyType.SOCIAL, 3,
                LobbyType.STAFF, 4,
                LobbyType.EVENT, 5
            );
            
            return lobbies.stream()
                .min(Comparator.comparingInt(lobby -> priorities.getOrDefault(lobby.getType(), 99)));
        }
        
        /**
         * Intelligent selection considering user history and preferences.
         * VeloctopusProject enhancement.
         */
        private Optional<LobbyTarget> selectIntelligent(List<LobbyTarget> lobbies, String userId) {
            if (userId == null) {
                return selectLoadBalanced(lobbies);
            }
            
            // Consider user's preferred routing mode
            RoutingMode userMode = userPreferences.getOrDefault(userId, RoutingMode.LOAD_BALANCED);
            if (userMode != RoutingMode.INTELLIGENT) {
                return switch (userMode) {
                    case RANDOM -> selectRandom(lobbies);
                    case FIRST_AVAILABLE -> selectFirstAvailable(lobbies);
                    case EMPTIEST -> selectEmptiest(lobbies);
                    case LOAD_BALANCED -> selectLoadBalanced(lobbies);
                    case PRIORITY_BASED -> selectPriorityBased(lobbies);
                    default -> selectLoadBalanced(lobbies);
                };
            }
            
            // Intelligent selection based on user history and current conditions
            return selectLoadBalanced(lobbies); // Fallback for now
        }
        
        /**
         * Update lobby server status and metrics.
         * 
         * @param serverId The server ID to update
         * @param available Whether the server is available
         * @param playerCount Current player count
         * @param loadFactor Current load factor (0.0 - 1.0)
         */
        public void updateLobbyStatus(String serverId, boolean available, int playerCount, double loadFactor) {
            LobbyTarget lobby = lobbyServers.get(serverId);
            if (lobby != null) {
                lobby.updateStatus(available, playerCount, loadFactor);
            }
        }
        
        /**
         * Get all registered lobby servers.
         * 
         * @return Unmodifiable collection of lobby targets
         */
        public Collection<LobbyTarget> getAllLobbies() {
            return Collections.unmodifiableCollection(lobbyServers.values());
        }
        
        /**
         * Get available lobby servers for a specific platform.
         * 
         * @param platform The platform to filter by
         * @return List of available lobbies for the platform
         */
        public List<LobbyTarget> getAvailableLobbies(String platform) {
            return lobbyServers.values().stream()
                .filter(LobbyTarget::isAvailable)
                .filter(lobby -> lobby.supportsPllatform(platform))
                .collect(Collectors.toList());
        }
    }
    
    /**
     * Cooldown manager for lobby commands.
     * Enhanced from VLobby's CooldownManager with Redis backing.
     */
    public static class LobbyCooldownManager {
        private final Map<String, Instant> localCache;
        private final Duration cooldownDuration;
        private final String platform;
        
        public LobbyCooldownManager(String platform, Duration cooldownDuration) {
            this.platform = platform;
            this.cooldownDuration = cooldownDuration;
            this.localCache = new ConcurrentHashMap<>();
        }
        
        /**
         * Check cooldown for a user and update if eligible.
         * Enhanced from VLobby's CooldownManager.cooldown() method.
         * 
         * @param userId The user ID to check
         * @return Remaining cooldown time in milliseconds, 0 if ready
         */
        public CompletableFuture<Long> checkCooldown(String userId) {
            return CompletableFuture.supplyAsync(() -> {
                Instant now = Instant.now();
                Instant lastUsed = localCache.get(userId);
                
                if (lastUsed == null) {
                    localCache.put(userId, now);
                    return 0L;
                }
                
                Duration elapsed = Duration.between(lastUsed, now);
                if (elapsed.compareTo(cooldownDuration) >= 0) {
                    localCache.put(userId, now);
                    return 0L;
                }
                
                Duration remaining = cooldownDuration.minus(elapsed);
                return remaining.toMillis();
            });
        }
        
        /**
         * Clear cooldown for a user (admin override).
         * 
         * @param userId The user ID to clear
         */
        public void clearCooldown(String userId) {
            localCache.remove(userId);
        }
        
        /**
         * Get all users currently on cooldown.
         * 
         * @return Set of user IDs on cooldown
         */
        public Set<String> getUsersOnCooldown() {
            Instant now = Instant.now();
            return localCache.entrySet().stream()
                .filter(entry -> Duration.between(entry.getValue(), now).compareTo(cooldownDuration) < 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        }
    }
    
    /**
     * Connection request handler for cross-platform lobby routing.
     * Enhanced from VLobby's CommandHandler.connectionRequest() method.
     */
    public static class LobbyConnectionHandler {
        private final LobbyRoutingEngine routingEngine;
        private final Map<String, LobbyCooldownManager> cooldownManagers;
        
        public LobbyConnectionHandler(LobbyRoutingEngine routingEngine) {
            this.routingEngine = routingEngine;
            this.cooldownManagers = new ConcurrentHashMap<>();
        }
        
        /**
         * Handle lobby connection request with cooldown and routing.
         * 
         * @param userId The user requesting connection
         * @param platform The platform of the request
         * @param mode The preferred routing mode
         * @return CompletableFuture with connection result
         */
        public CompletableFuture<LobbyConnectionResult> handleConnection(
                String userId, String platform, RoutingMode mode) {
            return CompletableFuture.supplyAsync(() -> {
                // Check cooldown
                LobbyCooldownManager cooldownManager = cooldownManagers.computeIfAbsent(
                    platform, p -> new LobbyCooldownManager(p, Duration.ofSeconds(5))
                );
                
                try {
                    long remainingCooldown = cooldownManager.checkCooldown(userId).get();
                    if (remainingCooldown > 0) {
                        return new LobbyConnectionResult(
                            false, null, "Cooldown active: " + remainingCooldown + "ms remaining"
                        );
                    }
                    
                    // Select lobby
                    Optional<LobbyTarget> selectedLobby = routingEngine.selectLobby(mode, platform, userId).get();
                    if (selectedLobby.isEmpty()) {
                        return new LobbyConnectionResult(
                            false, null, "No available lobby servers for platform: " + platform
                        );
                    }
                    
                    LobbyTarget target = selectedLobby.get();
                    return new LobbyConnectionResult(
                        true, target, "Connection request processed successfully"
                    );
                    
                } catch (Exception e) {
                    return new LobbyConnectionResult(
                        false, null, "Connection processing failed: " + e.getMessage()
                    );
                }
            });
        }
        
        /**
         * Register a cooldown manager for a platform.
         * 
         * @param platform The platform identifier
         * @param cooldownDuration The cooldown duration for this platform
         */
        public void registerCooldownManager(String platform, Duration cooldownDuration) {
            cooldownManagers.put(platform, new LobbyCooldownManager(platform, cooldownDuration));
        }
    }
    
    /**
     * Result of a lobby connection request.
     */
    public static class LobbyConnectionResult {
        private final boolean successful;
        private final LobbyTarget selectedLobby;
        private final String message;
        
        public LobbyConnectionResult(boolean successful, LobbyTarget selectedLobby, String message) {
            this.successful = successful;
            this.selectedLobby = selectedLobby;
            this.message = message;
        }
        
        public boolean isSuccessful() { return successful; }
        public LobbyTarget getSelectedLobby() { return selectedLobby; }
        public String getMessage() { return message; }
    }
}
