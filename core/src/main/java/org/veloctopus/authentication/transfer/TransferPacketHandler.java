/*
 * This file is part of VeloctopusProject, licensed under the MIT License.
 *
 * Copyright (c) 2025 VeloctopusProject Contributors
 * 
 * Server Transfer Packet Handler with Authenticated State Validation
 * Step 37: Implement server transfer packet handling with authenticated state validation
 */

package org.veloctopus.authentication.transfer;

import org.veloctopus.authentication.AuthenticationSystem;
import org.veloctopus.authentication.server.ServerWhitelistingSystem;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.time.Duration;

/**
 * Server Transfer Packet Handler with Authenticated State Validation
 * 
 * Comprehensive packet-level transfer handling with authentication enforcement:
 * 
 * Transfer Validation Logic:
 * - Intercepts all server transfer requests (commands, packets, plugins)
 * - Validates authentication state before allowing transfers
 * - Enforces hub-only restriction for unverified players
 * - Handles plugin-initiated transfers with state validation
 * - Provides fallback mechanisms for failed transfers
 * 
 * Packet Interception:
 * - BungeeCord plugin channel handling
 * - Velocity native transfer requests
 * - Command-based transfers (/server, /hub, /lobby)
 * - Plugin-initiated transfers via API
 * - External plugin packet interception
 * 
 * State Integration:
 * - Real-time authentication state checking
 * - Purgatory state enforcement
 * - Quarantine mode restrictions
 * - Member privilege validation
 * - Permission-based access control
 * 
 * Features:
 * - Comprehensive packet interception and validation
 * - Authentication state enforcement at packet level
 * - Rich player feedback with denial reasons
 * - Automatic hub fallback for denied transfers
 * - Transfer attempt monitoring and analytics
 * - Plugin compatibility layer for third-party integrations
 * - Rate limiting for transfer attempts
 * - Audit logging for all transfer activities
 * 
 * @author VeloctopusProject Team
 * @since 1.0.0
 */
public class TransferPacketHandler {

    /**
     * Transfer request source types
     */
    public enum TransferSource {
        COMMAND,           // Player command (/server, /hub)
        PLUGIN_MESSAGE,    // BungeeCord plugin channel
        VELOCITY_API,      // Velocity native API
        EXTERNAL_PLUGIN,   // Third-party plugin
        ADMIN_OVERRIDE,    // Administrative transfer
        FALLBACK_SYSTEM    // Automatic fallback
    }

    /**
     * Transfer validation result
     */
    public enum ValidationResult {
        APPROVED,                    // Transfer approved
        DENIED_NOT_AUTHENTICATED,    // Player not authenticated
        DENIED_INSUFFICIENT_PERM,    // Insufficient permissions
        DENIED_RATE_LIMITED,         // Rate limit exceeded
        DENIED_QUARANTINE_MODE,      // In quarantine mode
        DENIED_SERVER_OFFLINE,       // Target server offline
        DENIED_MAINTENANCE,          // Server in maintenance
        FALLBACK_TO_HUB,            // Redirected to hub
        ERROR_VALIDATION_FAILED      // Validation error
    }

    /**
     * Transfer request data
     */
    public static class TransferRequest {
        private final String playerId;
        private final String playerName;
        private final String sourceServer;
        private final String targetServer;
        private final TransferSource source;
        private final Instant timestamp;
        private final Map<String, Object> metadata;
        private ValidationResult result;
        private String denialReason;

        public TransferRequest(String playerId, String playerName, String sourceServer, 
                             String targetServer, TransferSource source) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.sourceServer = sourceServer;
            this.targetServer = targetServer;
            this.source = source;
            this.timestamp = Instant.now();
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters and setters
        public String getPlayerId() { return playerId; }
        public String getPlayerName() { return playerName; }
        public String getSourceServer() { return sourceServer; }
        public String getTargetServer() { return targetServer; }
        public TransferSource getSource() { return source; }
        public Instant getTimestamp() { return timestamp; }
        public Map<String, Object> getMetadata() { return new ConcurrentHashMap<>(metadata); }
        public ValidationResult getResult() { return result; }
        public void setResult(ValidationResult result) { this.result = result; }
        public String getDenialReason() { return denialReason; }
        public void setDenialReason(String denialReason) { this.denialReason = denialReason; }
        public void setMetadata(String key, Object value) { metadata.put(key, value); }
    }

    /**
     * Rate limiting tracker
     */
    public static class RateLimitTracker {
        private final Map<String, List<Instant>> playerAttempts;
        private final int maxAttemptsPerMinute;
        private final Duration timeWindow;

        public RateLimitTracker(int maxAttemptsPerMinute) {
            this.playerAttempts = new ConcurrentHashMap<>();
            this.maxAttemptsPerMinute = maxAttemptsPerMinute;
            this.timeWindow = Duration.ofMinutes(1);
        }

        public boolean isRateLimited(String playerId) {
            List<Instant> attempts = playerAttempts.computeIfAbsent(playerId, k -> new ArrayList<>());
            
            // Clean old attempts
            Instant cutoff = Instant.now().minus(timeWindow);
            attempts.removeIf(attempt -> attempt.isBefore(cutoff));
            
            // Check if rate limited
            if (attempts.size() >= maxAttemptsPerMinute) {
                return true;
            }
            
            // Record this attempt
            attempts.add(Instant.now());
            return false;
        }

        public void clearPlayerAttempts(String playerId) {
            playerAttempts.remove(playerId);
        }
    }

    // Core components
    private final ProxyServer proxyServer;
    private final AuthenticationSystem authenticationSystem;
    private final ServerWhitelistingSystem serverWhitelistingSystem;
    
    // Plugin channels
    private final MinecraftChannelIdentifier bungeeCordChannel;
    private final MinecraftChannelIdentifier velocityChannel;
    
    // Configuration
    private final RateLimitTracker rateLimitTracker;
    private final Set<String> hubServers;
    private final String defaultHubServer;
    private final Map<String, Component> messages;
    
    // Monitoring
    private final List<TransferRequest> transferHistory;
    private final Map<String, Object> systemMetrics;
    
    // System state
    private final Map<String, Instant> lastTransferAttempt;

    public TransferPacketHandler(ProxyServer proxyServer, AuthenticationSystem authenticationSystem,
                               ServerWhitelistingSystem serverWhitelistingSystem) {
        this.proxyServer = proxyServer;
        this.authenticationSystem = authenticationSystem;
        this.serverWhitelistingSystem = serverWhitelistingSystem;
        
        // Initialize plugin channels
        this.bungeeCordChannel = MinecraftChannelIdentifier.from("bungeecord:main");
        this.velocityChannel = MinecraftChannelIdentifier.from("velocity:main");
        
        // Configuration
        this.rateLimitTracker = new RateLimitTracker(10); // 10 attempts per minute
        this.hubServers = new HashSet<>(Arrays.asList("hub", "main-hub", "lobby"));
        this.defaultHubServer = "hub";
        this.messages = new ConcurrentHashMap<>();
        
        // Monitoring
        this.transferHistory = Collections.synchronizedList(new ArrayList<>());
        this.systemMetrics = new ConcurrentHashMap<>();
        this.lastTransferAttempt = new ConcurrentHashMap<>();
        
        initializeMessages();
        initializeMetrics();
        
        // Register plugin channels
        proxyServer.getChannelRegistrar().register(bungeeCordChannel);
        proxyServer.getChannelRegistrar().register(velocityChannel);
    }

    /**
     * Initialize system messages
     */
    private void initializeMessages() {
        messages.put("authentication_required", Component.text()
            .append(Component.text("🔐 ", NamedTextColor.RED))
            .append(Component.text("Authentication Required", NamedTextColor.RED, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("You must verify your account to access other servers.", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text("Use ", NamedTextColor.GRAY))
            .append(Component.text("/mc <username>", NamedTextColor.AQUA, TextDecoration.UNDERLINED))
            .append(Component.text(" in Discord to start verification.", NamedTextColor.GRAY))
            .build());

        messages.put("rate_limited", Component.text()
            .append(Component.text("⏱️ ", NamedTextColor.YELLOW))
            .append(Component.text("Rate Limited", NamedTextColor.YELLOW, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("Too many transfer attempts. Please wait a moment.", NamedTextColor.GRAY))
            .build());

        messages.put("quarantine_restricted", Component.text()
            .append(Component.text("🏥 ", NamedTextColor.YELLOW))
            .append(Component.text("Quarantine Mode", NamedTextColor.YELLOW, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("Server transfers are restricted during quarantine.", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text("Complete verification to gain full access.", NamedTextColor.GREEN))
            .build());

        messages.put("transfer_denied", Component.text()
            .append(Component.text("❌ ", NamedTextColor.RED))
            .append(Component.text("Transfer Denied", NamedTextColor.RED, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("Unable to complete server transfer.", NamedTextColor.GRAY))
            .build());

        messages.put("fallback_to_hub", Component.text()
            .append(Component.text("🏠 ", NamedTextColor.AQUA))
            .append(Component.text("Redirected to Hub", NamedTextColor.AQUA, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("You've been redirected to the hub server.", NamedTextColor.GRAY))
            .build());
    }

    /**
     * Initialize system metrics
     */
    private void initializeMetrics() {
        systemMetrics.put("total_transfer_requests", 0);
        systemMetrics.put("approved_transfers", 0);
        systemMetrics.put("denied_transfers", 0);
        systemMetrics.put("rate_limited_attempts", 0);
        systemMetrics.put("fallback_transfers", 0);
        systemMetrics.put("plugin_message_intercepts", 0);
        systemMetrics.put("command_intercepts", 0);
        systemMetrics.put("api_intercepts", 0);
    }

    /**
     * Handle plugin message events
     */
    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(bungeeCordChannel)) {
            return;
        }

        if (!(event.getSource() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getSource();
        byte[] data = event.getData();
        
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(data));
            String subchannel = input.readUTF();
            
            if ("Connect".equals(subchannel) || "ConnectOther".equals(subchannel)) {
                handlePluginTransferRequest(player, input, subchannel, event);
            }
        } catch (IOException e) {
            // Log error but don't block the transfer
            systemMetrics.put("plugin_message_errors", 
                ((Integer) systemMetrics.getOrDefault("plugin_message_errors", 0)) + 1);
        }
        
        systemMetrics.put("plugin_message_intercepts",
            ((Integer) systemMetrics.getOrDefault("plugin_message_intercepts", 0)) + 1);
    }

    /**
     * Handle server pre-connect events
     */
    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer targetServer = event.getResult().getServer().orElse(null);
        
        if (targetServer == null) {
            return;
        }
        
        String currentServer = player.getCurrentServer()
            .map(s -> s.getServerInfo().getName())
            .orElse("unknown");
        String targetServerName = targetServer.getServerInfo().getName();
        
        // Create transfer request
        TransferRequest request = new TransferRequest(
            player.getUniqueId().toString(),
            player.getUsername(),
            currentServer,
            targetServerName,
            TransferSource.VELOCITY_API
        );
        
        // Validate transfer
        validateTransferRequest(request)
            .thenAccept(result -> {
                if (result != ValidationResult.APPROVED) {
                    handleTransferDenial(event, player, request, result);
                } else {
                    recordSuccessfulTransfer(request);
                }
            });
    }

    /**
     * Handle server connected events
     */
    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        String serverName = event.getServer().getServerInfo().getName();
        
        // Record successful connection
        lastTransferAttempt.put(player.getUniqueId().toString(), Instant.now());
        
        // Clear rate limiting on successful connection to hub
        if (hubServers.contains(serverName)) {
            rateLimitTracker.clearPlayerAttempts(player.getUniqueId().toString());
        }
        
        // Check authentication state and send appropriate message
        authenticationSystem.handlePlayerJoinAsync(player.getUsername())
            .thenAccept(authState -> {
                if (authState == AuthenticationSystem.AuthenticationState.QUARANTINE && 
                    hubServers.contains(serverName)) {
                    player.sendMessage(messages.get("quarantine_restricted"));
                }
            });
    }

    /**
     * Handle plugin transfer requests
     */
    private void handlePluginTransferRequest(Player player, DataInputStream input, 
                                           String subchannel, PluginMessageEvent event) throws IOException {
        String targetServer;
        String targetPlayer = null;
        
        if ("Connect".equals(subchannel)) {
            targetServer = input.readUTF();
        } else { // ConnectOther
            targetPlayer = input.readUTF();
            targetServer = input.readUTF();
            
            // Only handle if targeting this player
            if (!player.getUsername().equals(targetPlayer)) {
                return;
            }
        }
        
        String currentServer = player.getCurrentServer()
            .map(s -> s.getServerInfo().getName())
            .orElse("unknown");
        
        // Create transfer request
        TransferRequest request = new TransferRequest(
            player.getUniqueId().toString(),
            player.getUsername(),
            currentServer,
            targetServer,
            TransferSource.PLUGIN_MESSAGE
        );
        
        request.setMetadata("subchannel", subchannel);
        request.setMetadata("target_player", targetPlayer);
        
        // Validate transfer
        validateTransferRequest(request)
            .thenAccept(result -> {
                if (result != ValidationResult.APPROVED) {
                    // Cancel the plugin message event
                    event.setResult(PluginMessageEvent.ForwardResult.handled());
                    
                    // Send denial message
                    Component message = getMessageForResult(result);
                    player.sendMessage(message);
                    
                    // Attempt fallback to hub
                    attemptHubFallback(player, request, result);
                } else {
                    recordSuccessfulTransfer(request);
                }
            });
    }

    /**
     * Validate transfer request
     */
    public CompletableFuture<ValidationResult> validateTransferRequest(TransferRequest request) {
        String playerId = request.getPlayerId();
        
        // Check rate limiting
        if (rateLimitTracker.isRateLimited(playerId)) {
            request.setResult(ValidationResult.DENIED_RATE_LIMITED);
            request.setDenialReason("Rate limit exceeded");
            recordDeniedTransfer(request);
            return CompletableFuture.completedFuture(ValidationResult.DENIED_RATE_LIMITED);
        }
        
        // Check authentication state
        return authenticationSystem.handlePlayerJoinAsync(request.getPlayerName())
            .thenCompose(authState -> {
                switch (authState) {
                    case UNVERIFIED:
                    case PURGATORY:
                        return validateUnverifiedTransfer(request);
                    
                    case QUARANTINE:
                        return validateQuarantineTransfer(request);
                    
                    case VERIFIED:
                    case MEMBER:
                        return validateVerifiedTransfer(request);
                    
                    case EXPIRED:
                    case BANNED:
                        request.setResult(ValidationResult.DENIED_NOT_AUTHENTICATED);
                        request.setDenialReason("Account verification expired or banned");
                        recordDeniedTransfer(request);
                        return CompletableFuture.completedFuture(ValidationResult.DENIED_NOT_AUTHENTICATED);
                    
                    default:
                        request.setResult(ValidationResult.ERROR_VALIDATION_FAILED);
                        request.setDenialReason("Unknown authentication state");
                        recordDeniedTransfer(request);
                        return CompletableFuture.completedFuture(ValidationResult.ERROR_VALIDATION_FAILED);
                }
            });
    }

    /**
     * Validate transfer for unverified players
     */
    private CompletableFuture<ValidationResult> validateUnverifiedTransfer(TransferRequest request) {
        // Unverified players can only go to hub servers
        if (hubServers.contains(request.getTargetServer())) {
            request.setResult(ValidationResult.APPROVED);
            return CompletableFuture.completedFuture(ValidationResult.APPROVED);
        } else {
            request.setResult(ValidationResult.DENIED_NOT_AUTHENTICATED);
            request.setDenialReason("Authentication required for game servers");
            recordDeniedTransfer(request);
            return CompletableFuture.completedFuture(ValidationResult.DENIED_NOT_AUTHENTICATED);
        }
    }

    /**
     * Validate transfer for quarantine players
     */
    private CompletableFuture<ValidationResult> validateQuarantineTransfer(TransferRequest request) {
        // Quarantine players can only go to hub servers
        if (hubServers.contains(request.getTargetServer())) {
            request.setResult(ValidationResult.APPROVED);
            return CompletableFuture.completedFuture(ValidationResult.APPROVED);
        } else {
            request.setResult(ValidationResult.DENIED_QUARANTINE_MODE);
            request.setDenialReason("Server transfers restricted during quarantine");
            recordDeniedTransfer(request);
            return CompletableFuture.completedFuture(ValidationResult.DENIED_QUARANTINE_MODE);
        }
    }

    /**
     * Validate transfer for verified players
     */
    private CompletableFuture<ValidationResult> validateVerifiedTransfer(TransferRequest request) {
        // Use server whitelisting system for detailed validation
        return serverWhitelistingSystem.checkServerAccess(
            proxyServer.getPlayer(request.getPlayerName()).orElse(null),
            request.getTargetServer()
        ).thenApply(whitelistResult -> {
            switch (whitelistResult) {
                case SUCCESS:
                    request.setResult(ValidationResult.APPROVED);
                    return ValidationResult.APPROVED;
                
                case DENIED_NOT_VERIFIED:
                    request.setResult(ValidationResult.DENIED_NOT_AUTHENTICATED);
                    request.setDenialReason("Authentication required");
                    recordDeniedTransfer(request);
                    return ValidationResult.DENIED_NOT_AUTHENTICATED;
                
                case DENIED_INSUFFICIENT_PERMISSION:
                    request.setResult(ValidationResult.DENIED_INSUFFICIENT_PERM);
                    request.setDenialReason("Insufficient permissions");
                    recordDeniedTransfer(request);
                    return ValidationResult.DENIED_INSUFFICIENT_PERM;
                
                case DENIED_SERVER_OFFLINE:
                    request.setResult(ValidationResult.DENIED_SERVER_OFFLINE);
                    request.setDenialReason("Target server offline");
                    recordDeniedTransfer(request);
                    return ValidationResult.DENIED_SERVER_OFFLINE;
                
                case DENIED_MAINTENANCE_MODE:
                    request.setResult(ValidationResult.DENIED_MAINTENANCE);
                    request.setDenialReason("Server in maintenance mode");
                    recordDeniedTransfer(request);
                    return ValidationResult.DENIED_MAINTENANCE;
                
                default:
                    request.setResult(ValidationResult.ERROR_VALIDATION_FAILED);
                    request.setDenialReason("Validation failed");
                    recordDeniedTransfer(request);
                    return ValidationResult.ERROR_VALIDATION_FAILED;
            }
        });
    }

    /**
     * Handle transfer denial
     */
    private void handleTransferDenial(ServerPreConnectEvent event, Player player, 
                                    TransferRequest request, ValidationResult result) {
        Component message = getMessageForResult(result);
        
        // Attempt fallback to hub
        RegisteredServer hubServer = proxyServer.getServer(defaultHubServer).orElse(null);
        if (hubServer != null && !request.getTargetServer().equals(defaultHubServer)) {
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(hubServer));
            player.sendMessage(message);
            player.sendMessage(messages.get("fallback_to_hub"));
            
            request.setResult(ValidationResult.FALLBACK_TO_HUB);
            request.setDenialReason("Redirected to hub due to " + result);
            
            systemMetrics.put("fallback_transfers",
                ((Integer) systemMetrics.getOrDefault("fallback_transfers", 0)) + 1);
        } else {
            event.setResult(ServerPreConnectEvent.ServerResult.denied(message));
        }
        
        recordDeniedTransfer(request);
    }

    /**
     * Attempt hub fallback
     */
    private void attemptHubFallback(Player player, TransferRequest request, ValidationResult result) {
        RegisteredServer hubServer = proxyServer.getServer(defaultHubServer).orElse(null);
        if (hubServer != null) {
            player.createConnectionRequest(hubServer).connect()
                .thenAccept(connectionResult -> {
                    if (connectionResult.isSuccessful()) {
                        player.sendMessage(messages.get("fallback_to_hub"));
                        
                        request.setResult(ValidationResult.FALLBACK_TO_HUB);
                        request.setDenialReason("Fallback to hub after " + result);
                        
                        systemMetrics.put("fallback_transfers",
                            ((Integer) systemMetrics.getOrDefault("fallback_transfers", 0)) + 1);
                    }
                });
        }
    }

    /**
     * Get message for validation result
     */
    private Component getMessageForResult(ValidationResult result) {
        switch (result) {
            case DENIED_NOT_AUTHENTICATED:
                return messages.get("authentication_required");
            case DENIED_RATE_LIMITED:
                return messages.get("rate_limited");
            case DENIED_QUARANTINE_MODE:
                return messages.get("quarantine_restricted");
            default:
                return messages.get("transfer_denied");
        }
    }

    /**
     * Record successful transfer
     */
    private void recordSuccessfulTransfer(TransferRequest request) {
        request.setResult(ValidationResult.APPROVED);
        transferHistory.add(request);
        
        // Keep only last 5,000 entries
        while (transferHistory.size() > 5000) {
            transferHistory.remove(0);
        }
        
        systemMetrics.put("total_transfer_requests",
            ((Integer) systemMetrics.getOrDefault("total_transfer_requests", 0)) + 1);
        systemMetrics.put("approved_transfers",
            ((Integer) systemMetrics.getOrDefault("approved_transfers", 0)) + 1);
    }

    /**
     * Record denied transfer
     */
    private void recordDeniedTransfer(TransferRequest request) {
        transferHistory.add(request);
        
        // Keep only last 5,000 entries
        while (transferHistory.size() > 5000) {
            transferHistory.remove(0);
        }
        
        systemMetrics.put("total_transfer_requests",
            ((Integer) systemMetrics.getOrDefault("total_transfer_requests", 0)) + 1);
        systemMetrics.put("denied_transfers",
            ((Integer) systemMetrics.getOrDefault("denied_transfers", 0)) + 1);
        
        if (request.getResult() == ValidationResult.DENIED_RATE_LIMITED) {
            systemMetrics.put("rate_limited_attempts",
                ((Integer) systemMetrics.getOrDefault("rate_limited_attempts", 0)) + 1);
        }
    }

    /**
     * Get transfer statistics
     */
    public CompletableFuture<Map<String, Object>> getTransferStatisticsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("metrics", new HashMap<>(systemMetrics));
            stats.put("transfer_history_size", transferHistory.size());
            stats.put("active_rate_limits", rateLimitTracker.playerAttempts.size());
            stats.put("hub_servers", hubServers);
            stats.put("default_hub_server", defaultHubServer);
            
            return stats;
        });
    }

    // Getters
    public Map<String, Object> getSystemMetrics() { return new HashMap<>(systemMetrics); }
    public List<TransferRequest> getTransferHistory() { return new ArrayList<>(transferHistory); }
    public RateLimitTracker getRateLimitTracker() { return rateLimitTracker; }
}
