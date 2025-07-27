/*
 * This file is part of VeloctopusProject, licensed under the MIT License.
 *
 * Copyright (c) 2025 VeloctopusProject Contributors
 * 
 * Lobby/Hub Server Whitelisting with Transfer Packet Handling
 * Step 36: Implement lobby/hub server whitelisting with transfer packet handling
 */

package org.veloctopus.authentication.server;

import org.veloctopus.authentication.AuthenticationSystem;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.time.Duration;

/**
 * Lobby/Hub Server Whitelisting with Transfer Packet Handling
 * 
 * Comprehensive server access control system for authentication workflow:
 * 
 * Access Control Logic:
 * - Unverified players: Hub/lobby servers only
 * - Purgatory state: Hub/lobby servers only
 * - Quarantine state: Hub/lobby servers only with adventure mode
 * - Verified/Member state: Full network access
 * 
 * Server Categories:
 * - Hub servers: Always accessible for unverified players
 * - Lobby servers: Accessible for unverified players
 * - Game servers: Requires verification
 * - Staff servers: Requires verification + permission
 * - VIP servers: Requires verification + VIP permission
 * 
 * Features:
 * - Real-time server access validation
 * - Automatic fallback to hub server on denied access
 * - Comprehensive transfer packet handling
 * - Player state-based server routing
 * - Kick protection with automatic hub transfer
 * - Rich player feedback with reason explanations
 * - Integration with authentication system
 * 
 * @author VeloctopusProject Team
 * @since 1.0.0
 */
public class ServerWhitelistingSystem {

    /**
     * Server access levels
     */
    public enum ServerAccessLevel {
        PUBLIC,        // Accessible to all players
        HUB_ONLY,      // Hub/lobby servers only
        VERIFIED_ONLY, // Requires verification
        STAFF_ONLY,    // Requires staff permission
        VIP_ONLY,      // Requires VIP permission
        ADMIN_ONLY     // Requires admin permission
    }

    /**
     * Server categories for access control
     */
    public enum ServerCategory {
        HUB,           // Main hub servers
        LOBBY,         // Lobby/waiting servers
        GAME,          // Game servers
        STAFF,         // Staff-only servers
        VIP,           // VIP servers
        MAINTENANCE    // Maintenance/development servers
    }

    /**
     * Transfer attempt result
     */
    public enum TransferResult {
        SUCCESS,
        DENIED_NOT_VERIFIED,
        DENIED_INSUFFICIENT_PERMISSION,
        DENIED_SERVER_OFFLINE,
        DENIED_SERVER_FULL,
        DENIED_MAINTENANCE_MODE,
        FALLBACK_TO_HUB,
        ERROR
    }

    /**
     * Server configuration
     */
    public static class ServerConfiguration {
        private final String serverName;
        private final ServerCategory category;
        private final ServerAccessLevel accessLevel;
        private final Set<String> requiredPermissions;
        private final int maxPlayers;
        private final boolean maintenanceMode;
        private final String fallbackServer;
        private final Map<String, Object> metadata;

        public ServerConfiguration(String serverName, ServerCategory category, ServerAccessLevel accessLevel) {
            this.serverName = serverName;
            this.category = category;
            this.accessLevel = accessLevel;
            this.requiredPermissions = new HashSet<>();
            this.maxPlayers = -1; // Unlimited
            this.maintenanceMode = false;
            this.fallbackServer = null;
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters
        public String getServerName() { return serverName; }
        public ServerCategory getCategory() { return category; }
        public ServerAccessLevel getAccessLevel() { return accessLevel; }
        public Set<String> getRequiredPermissions() { return new HashSet<>(requiredPermissions); }
        public int getMaxPlayers() { return maxPlayers; }
        public boolean isMaintenanceMode() { return maintenanceMode; }
        public String getFallbackServer() { return fallbackServer; }
        public Map<String, Object> getMetadata() { return new ConcurrentHashMap<>(metadata); }
    }

    /**
     * Transfer attempt audit log
     */
    public static class TransferAuditLog {
        private final String playerName;
        private final String playerId;
        private final String fromServer;
        private final String toServer;
        private final TransferResult result;
        private final AuthenticationSystem.AuthenticationState playerState;
        private final Instant timestamp;
        private final String reason;
        private final Map<String, Object> additionalData;

        public TransferAuditLog(String playerName, String playerId, String fromServer, String toServer,
                              TransferResult result, AuthenticationSystem.AuthenticationState playerState,
                              String reason) {
            this.playerName = playerName;
            this.playerId = playerId;
            this.fromServer = fromServer;
            this.toServer = toServer;
            this.result = result;
            this.playerState = playerState;
            this.timestamp = Instant.now();
            this.reason = reason;
            this.additionalData = new ConcurrentHashMap<>();
        }

        // Getters
        public String getPlayerName() { return playerName; }
        public String getPlayerId() { return playerId; }
        public String getFromServer() { return fromServer; }
        public String getToServer() { return toServer; }
        public TransferResult getResult() { return result; }
        public AuthenticationSystem.AuthenticationState getPlayerState() { return playerState; }
        public Instant getTimestamp() { return timestamp; }
        public String getReason() { return reason; }
        public Map<String, Object> getAdditionalData() { return new ConcurrentHashMap<>(additionalData); }
        public void setAdditionalData(String key, Object value) { additionalData.put(key, value); }
    }

    // Core components
    private final ProxyServer proxyServer;
    private final AuthenticationSystem authenticationSystem;
    private final Map<String, ServerConfiguration> serverConfigurations;
    private final List<TransferAuditLog> auditLog;
    
    // Configuration
    private final Set<String> hubServers;
    private final Set<String> lobbyServers;
    private final String defaultHubServer;
    private final Map<String, Component> messages;
    
    // Monitoring
    private final Map<String, Object> systemMetrics;

    public ServerWhitelistingSystem(ProxyServer proxyServer, AuthenticationSystem authenticationSystem) {
        this.proxyServer = proxyServer;
        this.authenticationSystem = authenticationSystem;
        this.serverConfigurations = new ConcurrentHashMap<>();
        this.auditLog = Collections.synchronizedList(new ArrayList<>());
        
        // Configuration
        this.hubServers = new HashSet<>(Arrays.asList("hub", "main-hub", "lobby"));
        this.lobbyServers = new HashSet<>(Arrays.asList("lobby", "lobby1", "lobby2", "waiting-room"));
        this.defaultHubServer = "hub";
        this.messages = new ConcurrentHashMap<>();
        
        this.systemMetrics = new ConcurrentHashMap<>();
        this.systemMetrics.put("total_transfer_attempts", 0);
        this.systemMetrics.put("successful_transfers", 0);
        this.systemMetrics.put("denied_transfers", 0);
        this.systemMetrics.put("fallback_transfers", 0);
        
        initializeServerConfigurations();
        initializeMessages();
    }

    /**
     * Initialize default server configurations
     */
    private void initializeServerConfigurations() {
        // Hub servers
        for (String hubServer : hubServers) {
            ServerConfiguration config = new ServerConfiguration(hubServer, ServerCategory.HUB, ServerAccessLevel.PUBLIC);
            serverConfigurations.put(hubServer, config);
        }
        
        // Lobby servers
        for (String lobbyServer : lobbyServers) {
            ServerConfiguration config = new ServerConfiguration(lobbyServer, ServerCategory.LOBBY, ServerAccessLevel.PUBLIC);
            serverConfigurations.put(lobbyServer, config);
        }
        
        // Example game servers
        ServerConfiguration survival = new ServerConfiguration("survival", ServerCategory.GAME, ServerAccessLevel.VERIFIED_ONLY);
        serverConfigurations.put("survival", survival);
        
        ServerConfiguration creative = new ServerConfiguration("creative", ServerCategory.GAME, ServerAccessLevel.VERIFIED_ONLY);
        serverConfigurations.put("creative", creative);
        
        // VIP server
        ServerConfiguration vip = new ServerConfiguration("vip", ServerCategory.VIP, ServerAccessLevel.VIP_ONLY);
        vip.getRequiredPermissions().add("server.vip");
        serverConfigurations.put("vip", vip);
        
        // Staff server
        ServerConfiguration staff = new ServerConfiguration("staff", ServerCategory.STAFF, ServerAccessLevel.STAFF_ONLY);
        staff.getRequiredPermissions().add("server.staff");
        serverConfigurations.put("staff", staff);
    }

    /**
     * Initialize messages
     */
    private void initializeMessages() {
        messages.put("verification_required", Component.text()
            .append(Component.text("🔒 ", NamedTextColor.RED))
            .append(Component.text("Verification Required", NamedTextColor.RED, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("You must verify your account in Discord to access this server.", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text("Use ", NamedTextColor.GRAY))
            .append(Component.text("/mc <username>", NamedTextColor.AQUA, TextDecoration.UNDERLINED))
            .append(Component.text(" in Discord to start verification.", NamedTextColor.GRAY))
            .build());

        messages.put("insufficient_permission", Component.text()
            .append(Component.text("⛔ ", NamedTextColor.RED))
            .append(Component.text("Access Denied", NamedTextColor.RED, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("You don't have permission to access this server.", NamedTextColor.GRAY))
            .build());

        messages.put("server_offline", Component.text()
            .append(Component.text("📡 ", NamedTextColor.YELLOW))
            .append(Component.text("Server Offline", NamedTextColor.YELLOW, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("The requested server is currently offline.", NamedTextColor.GRAY))
            .build());

        messages.put("fallback_to_hub", Component.text()
            .append(Component.text("🏠 ", NamedTextColor.AQUA))
            .append(Component.text("Redirected to Hub", NamedTextColor.AQUA, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("You've been redirected to the hub server.", NamedTextColor.GRAY))
            .build());

        messages.put("quarantine_notification", Component.text()
            .append(Component.text("⏳ ", NamedTextColor.YELLOW))
            .append(Component.text("Verification in Progress", NamedTextColor.YELLOW, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("Please wait while your verification is being processed.", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text("You'll have full access once verification is complete.", NamedTextColor.GREEN))
            .build());
    }

    /**
     * Handle server pre-connect event
     */
    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer targetServer = event.getResult().getServer().orElse(null);
        
        if (targetServer == null) {
            return;
        }
        
        String targetServerName = targetServer.getServerInfo().getName();
        
        // Check server access
        checkServerAccess(player, targetServerName)
            .thenAccept(accessResult -> {
                if (accessResult != TransferResult.SUCCESS) {
                    handleAccessDenied(event, player, targetServerName, accessResult);
                }
            });
    }

    /**
     * Handle kicked from server event
     */
    @Subscribe
    public void onKickedFromServer(KickedFromServerEvent event) {
        Player player = event.getPlayer();
        RegisteredServer fromServer = event.getServer();
        
        // Automatically transfer to hub if kicked
        RegisteredServer hubServer = proxyServer.getServer(defaultHubServer).orElse(null);
        if (hubServer != null) {
            event.setResult(KickedFromServerEvent.RedirectPlayer.create(hubServer, messages.get("fallback_to_hub")));
            
            logTransferAttempt(player, fromServer.getServerInfo().getName(), defaultHubServer, 
                             TransferResult.FALLBACK_TO_HUB, "Automatic hub transfer after kick");
            
            systemMetrics.put("fallback_transfers",
                ((Integer) systemMetrics.getOrDefault("fallback_transfers", 0)) + 1);
        }
    }

    /**
     * Check server access for player
     */
    public CompletableFuture<TransferResult> checkServerAccess(Player player, String serverName) {
        return authenticationSystem.handlePlayerJoinAsync(player.getUsername())
            .thenApply(authState -> {
                ServerConfiguration serverConfig = serverConfigurations.get(serverName);
                
                if (serverConfig == null) {
                    // Unknown server, allow if it's in hub/lobby list
                    if (hubServers.contains(serverName) || lobbyServers.contains(serverName)) {
                        return TransferResult.SUCCESS;
                    }
                    return TransferResult.DENIED_NOT_VERIFIED;
                }
                
                // Check maintenance mode
                if (serverConfig.isMaintenanceMode()) {
                    return TransferResult.DENIED_MAINTENANCE_MODE;
                }
                
                // Check based on authentication state
                switch (authState) {
                    case UNVERIFIED:
                    case PURGATORY:
                    case QUARANTINE:
                        return checkUnverifiedAccess(serverConfig);
                    
                    case VERIFIED:
                    case MEMBER:
                        return checkVerifiedAccess(player, serverConfig);
                    
                    case EXPIRED:
                    case BANNED:
                        return TransferResult.DENIED_NOT_VERIFIED;
                    
                    default:
                        return TransferResult.DENIED_NOT_VERIFIED;
                }
            });
    }

    /**
     * Check access for unverified players
     */
    private TransferResult checkUnverifiedAccess(ServerConfiguration serverConfig) {
        switch (serverConfig.getCategory()) {
            case HUB:
            case LOBBY:
                return TransferResult.SUCCESS;
            
            case GAME:
            case VIP:
            case STAFF:
            default:
                return TransferResult.DENIED_NOT_VERIFIED;
        }
    }

    /**
     * Check access for verified players
     */
    private TransferResult checkVerifiedAccess(Player player, ServerConfiguration serverConfig) {
        switch (serverConfig.getAccessLevel()) {
            case PUBLIC:
            case HUB_ONLY:
            case VERIFIED_ONLY:
                return TransferResult.SUCCESS;
            
            case STAFF_ONLY:
            case VIP_ONLY:
            case ADMIN_ONLY:
                return checkPermissions(player, serverConfig);
            
            default:
                return TransferResult.SUCCESS;
        }
    }

    /**
     * Check player permissions for server
     */
    private TransferResult checkPermissions(Player player, ServerConfiguration serverConfig) {
        for (String permission : serverConfig.getRequiredPermissions()) {
            if (!player.hasPermission(permission)) {
                return TransferResult.DENIED_INSUFFICIENT_PERMISSION;
            }
        }
        return TransferResult.SUCCESS;
    }

    /**
     * Handle access denied
     */
    private void handleAccessDenied(ServerPreConnectEvent event, Player player, String targetServerName, TransferResult result) {
        Component message;
        
        switch (result) {
            case DENIED_NOT_VERIFIED:
                message = messages.get("verification_required");
                break;
            case DENIED_INSUFFICIENT_PERMISSION:
                message = messages.get("insufficient_permission");
                break;
            case DENIED_SERVER_OFFLINE:
                message = messages.get("server_offline");
                break;
            default:
                message = messages.get("verification_required");
                break;
        }
        
        // Redirect to hub
        RegisteredServer hubServer = proxyServer.getServer(defaultHubServer).orElse(null);
        if (hubServer != null) {
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(hubServer));
            player.sendMessage(message);
            
            // Send quarantine notification if in quarantine
            authenticationSystem.handlePlayerJoinAsync(player.getUsername())
                .thenAccept(authState -> {
                    if (authState == AuthenticationSystem.AuthenticationState.QUARANTINE) {
                        player.sendMessage(messages.get("quarantine_notification"));
                    }
                });
        } else {
            event.setResult(ServerPreConnectEvent.ServerResult.denied(message));
        }
        
        // Log attempt
        logTransferAttempt(player, player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("unknown"), 
                         targetServerName, result, "Access denied: " + result);
        
        systemMetrics.put("denied_transfers",
            ((Integer) systemMetrics.getOrDefault("denied_transfers", 0)) + 1);
    }

    /**
     * Log transfer attempt
     */
    private void logTransferAttempt(Player player, String fromServer, String toServer, 
                                  TransferResult result, String reason) {
        authenticationSystem.handlePlayerJoinAsync(player.getUsername())
            .thenAccept(authState -> {
                TransferAuditLog logEntry = new TransferAuditLog(
                    player.getUsername(),
                    player.getUniqueId().toString(),
                    fromServer,
                    toServer,
                    result,
                    authState,
                    reason
                );
                
                auditLog.add(logEntry);
                
                // Keep only last 10,000 entries
                while (auditLog.size() > 10000) {
                    auditLog.remove(0);
                }
            });
        
        systemMetrics.put("total_transfer_attempts",
            ((Integer) systemMetrics.getOrDefault("total_transfer_attempts", 0)) + 1);
        systemMetrics.put("last_transfer_attempt", Instant.now());
    }

    /**
     * Transfer player to server with validation
     */
    public CompletableFuture<TransferResult> transferPlayerToServer(Player player, String serverName) {
        return checkServerAccess(player, serverName)
            .thenCompose(accessResult -> {
                if (accessResult == TransferResult.SUCCESS) {
                    RegisteredServer targetServer = proxyServer.getServer(serverName).orElse(null);
                    if (targetServer != null) {
                        return player.createConnectionRequest(targetServer).connect()
                            .thenApply(connectionResult -> {
                                if (connectionResult.isSuccessful()) {
                                    logTransferAttempt(player, 
                                        player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("unknown"),
                                        serverName, TransferResult.SUCCESS, "Manual transfer");
                                    
                                    systemMetrics.put("successful_transfers",
                                        ((Integer) systemMetrics.getOrDefault("successful_transfers", 0)) + 1);
                                    
                                    return TransferResult.SUCCESS;
                                } else {
                                    return TransferResult.DENIED_SERVER_OFFLINE;
                                }
                            });
                    } else {
                        return CompletableFuture.completedFuture(TransferResult.DENIED_SERVER_OFFLINE);
                    }
                } else {
                    return CompletableFuture.completedFuture(accessResult);
                }
            });
    }

    /**
     * Get server whitelist status
     */
    public CompletableFuture<Map<String, Object>> getWhitelistStatusAsync() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> status = new HashMap<>();
            
            status.put("total_servers_configured", serverConfigurations.size());
            status.put("hub_servers", hubServers);
            status.put("lobby_servers", lobbyServers);
            status.put("default_hub_server", defaultHubServer);
            status.put("audit_log_entries", auditLog.size());
            status.put("metrics", new HashMap<>(systemMetrics));
            
            return status;
        });
    }

    // Getters
    public Map<String, Object> getSystemMetrics() { return new HashMap<>(systemMetrics); }
    public List<TransferAuditLog> getAuditLog() { return new ArrayList<>(auditLog); }
    public Map<String, ServerConfiguration> getServerConfigurations() { return new HashMap<>(serverConfigurations); }
}
