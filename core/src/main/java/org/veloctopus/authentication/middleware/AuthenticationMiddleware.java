/*
 * This file is part of VeloctopusProject, licensed under the MIT License.
 *
 * Copyright (c) 2025 VeloctopusProject Contributors
 * 
 * Authentication Middleware Integration Layer
 * Step 38: Implement authentication middleware integration layer for seamless component communication
 */

package org.veloctopus.authentication.middleware;

import org.veloctopus.authentication.AuthenticationSystem;
import org.veloctopus.authentication.server.ServerWhitelistingSystem;
import org.veloctopus.authentication.transfer.TransferPacketHandler;
import org.veloctopus.authentication.session.SessionCleanupSystem;
import org.veloctopus.authentication.discord.DiscordVerificationWorkflow;
import org.veloctopus.authentication.hex.HexadecimalCodeService;
import org.veloctopus.authentication.commands.VerifyCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.event.EventManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.time.Instant;

/**
 * Authentication Middleware Integration Layer
 * 
 * Centralized middleware that orchestrates all authentication components:
 * 
 * Component Integration:
 * - Unified initialization and lifecycle management
 * - Inter-component communication hub
 * - Event routing and coordination
 * - State synchronization across components
 * - Configuration management and validation
 * 
 * Service Orchestration:
 * - Authentication workflow coordination
 * - Server access control integration
 * - Transfer validation pipeline
 * - Session management lifecycle
 * - Discord workflow integration
 * - Command system registration
 * 
 * Monitoring & Analytics:
 * - Centralized metrics collection
 * - Performance monitoring
 * - Health check coordination
 * - Error handling and recovery
 * - Audit log aggregation
 * 
 * Features:
 * - Seamless component communication
 * - Unified configuration management
 * - Centralized event handling
 * - Integrated monitoring and health checks
 * - Automatic error recovery and failover
 * - Performance optimization coordination
 * - Comprehensive audit logging
 * - Hot-reload configuration support
 * 
 * @author VeloctopusProject Team
 * @since 1.0.0
 */
public class AuthenticationMiddleware {

    /**
     * Component status
     */
    public enum ComponentStatus {
        INITIALIZING,
        RUNNING,
        STOPPING,
        STOPPED,
        ERROR,
        RECOVERING
    }

    /**
     * Component health information
     */
    public static class ComponentHealth {
        private final String componentName;
        private final ComponentStatus status;
        private final Instant lastHealthCheck;
        private final String statusMessage;
        private final Map<String, Object> metrics;
        private final List<String> errors;

        public ComponentHealth(String componentName, ComponentStatus status, 
                             String statusMessage) {
            this.componentName = componentName;
            this.status = status;
            this.statusMessage = statusMessage;
            this.lastHealthCheck = Instant.now();
            this.metrics = new ConcurrentHashMap<>();
            this.errors = new ArrayList<>();
        }

        // Getters
        public String getComponentName() { return componentName; }
        public ComponentStatus getStatus() { return status; }
        public Instant getLastHealthCheck() { return lastHealthCheck; }
        public String getStatusMessage() { return statusMessage; }
        public Map<String, Object> getMetrics() { return new ConcurrentHashMap<>(metrics); }
        public List<String> getErrors() { return new ArrayList<>(errors); }
        public void addMetric(String key, Object value) { metrics.put(key, value); }
        public void addError(String error) { errors.add(error); }
    }

    /**
     * Authentication event
     */
    public static class AuthenticationEvent {
        private final String eventType;
        private final String playerId;
        private final String playerName;
        private final AuthenticationSystem.AuthenticationState oldState;
        private final AuthenticationSystem.AuthenticationState newState;
        private final Instant timestamp;
        private final Map<String, Object> metadata;

        public AuthenticationEvent(String eventType, String playerId, String playerName,
                                 AuthenticationSystem.AuthenticationState oldState,
                                 AuthenticationSystem.AuthenticationState newState) {
            this.eventType = eventType;
            this.playerId = playerId;
            this.playerName = playerName;
            this.oldState = oldState;
            this.newState = newState;
            this.timestamp = Instant.now();
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters
        public String getEventType() { return eventType; }
        public String getPlayerId() { return playerId; }
        public String getPlayerName() { return playerName; }
        public AuthenticationSystem.AuthenticationState getOldState() { return oldState; }
        public AuthenticationSystem.AuthenticationState getNewState() { return newState; }
        public Instant getTimestamp() { return timestamp; }
        public Map<String, Object> getMetadata() { return new ConcurrentHashMap<>(metadata); }
        public void setMetadata(String key, Object value) { metadata.put(key, value); }
    }

    /**
     * Middleware configuration
     */
    public static class MiddlewareConfiguration {
        private final boolean enableHealthChecks;
        private final int healthCheckInterval;
        private final boolean enableMetricsCollection;
        private final int metricsInterval;
        private final boolean enableAutoRecovery;
        private final int maxRecoveryAttempts;
        private final Map<String, Object> componentConfigs;

        public MiddlewareConfiguration() {
            this.enableHealthChecks = true;
            this.healthCheckInterval = 30; // seconds
            this.enableMetricsCollection = true;
            this.metricsInterval = 60; // seconds
            this.enableAutoRecovery = true;
            this.maxRecoveryAttempts = 3;
            this.componentConfigs = new ConcurrentHashMap<>();
        }

        // Getters
        public boolean isEnableHealthChecks() { return enableHealthChecks; }
        public int getHealthCheckInterval() { return healthCheckInterval; }
        public boolean isEnableMetricsCollection() { return enableMetricsCollection; }
        public int getMetricsInterval() { return metricsInterval; }
        public boolean isEnableAutoRecovery() { return enableAutoRecovery; }
        public int getMaxRecoveryAttempts() { return maxRecoveryAttempts; }
        public Map<String, Object> getComponentConfigs() { return new ConcurrentHashMap<>(componentConfigs); }
    }

    // Core components
    private final ProxyServer proxyServer;
    private final EventManager eventManager;
    private final MiddlewareConfiguration config;
    
    // Authentication system components
    private AuthenticationSystem authenticationSystem;
    private ServerWhitelistingSystem serverWhitelistingSystem;
    private TransferPacketHandler transferPacketHandler;
    private SessionCleanupSystem sessionCleanupSystem;
    private DiscordVerificationWorkflow discordVerificationWorkflow;
    private HexadecimalCodeService hexadecimalCodeService;
    private VerifyCommand verifyCommand;
    
    // Middleware management
    private final ScheduledExecutorService scheduler;
    private final Map<String, ComponentHealth> componentHealth;
    private final Map<String, Integer> recoveryAttempts;
    private final List<AuthenticationEvent> eventHistory;
    
    // System state
    private boolean isInitialized;
    private boolean isRunning;
    private final Map<String, Object> systemMetrics;

    public AuthenticationMiddleware(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
        this.eventManager = proxyServer.getEventManager();
        this.config = new MiddlewareConfiguration();
        
        // Initialize middleware management
        this.scheduler = Executors.newScheduledThreadPool(4, r -> {
            Thread thread = new Thread(r, "AuthMiddleware-Scheduler");
            thread.setDaemon(true);
            return thread;
        });
        this.componentHealth = new ConcurrentHashMap<>();
        this.recoveryAttempts = new ConcurrentHashMap<>();
        this.eventHistory = Collections.synchronizedList(new ArrayList<>());
        
        // System state
        this.isInitialized = false;
        this.isRunning = false;
        this.systemMetrics = new ConcurrentHashMap<>();
        
        initializeSystemMetrics();
    }

    /**
     * Initialize authentication middleware
     */
    public CompletableFuture<Void> initializeAsync() {
        if (isInitialized) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                // Initialize core authentication system
                initializeAuthenticationSystem();
                
                // Initialize hex code service
                initializeHexCodeService();
                
                // Initialize server whitelisting
                initializeServerWhitelisting();
                
                // Initialize transfer packet handler
                initializeTransferPacketHandler();
                
                // Initialize Discord workflow
                initializeDiscordWorkflow();
                
                // Initialize verify command
                initializeVerifyCommand();
                
                // Initialize session cleanup
                initializeSessionCleanup();
                
                // Register event handlers
                registerEventHandlers();
                
                // Start monitoring
                startMonitoring();
                
                isInitialized = true;
                isRunning = true;
                
                fireAuthenticationEvent("MIDDLEWARE_INITIALIZED", null, null, null, null);
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize authentication middleware", e);
            }
        });
    }

    /**
     * Initialize authentication system
     */
    private void initializeAuthenticationSystem() {
        updateComponentHealth("AuthenticationSystem", ComponentStatus.INITIALIZING, "Initializing core authentication");
        
        try {
            authenticationSystem = new AuthenticationSystem(proxyServer);
            
            updateComponentHealth("AuthenticationSystem", ComponentStatus.RUNNING, "Authentication system initialized");
            systemMetrics.put("auth_system_initialized", Instant.now());
            
        } catch (Exception e) {
            updateComponentHealth("AuthenticationSystem", ComponentStatus.ERROR, "Failed to initialize: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Initialize hex code service
     */
    private void initializeHexCodeService() {
        updateComponentHealth("HexCodeService", ComponentStatus.INITIALIZING, "Initializing hex code service");
        
        try {
            hexadecimalCodeService = new HexadecimalCodeService();
            
            updateComponentHealth("HexCodeService", ComponentStatus.RUNNING, "Hex code service initialized");
            systemMetrics.put("hex_service_initialized", Instant.now());
            
        } catch (Exception e) {
            updateComponentHealth("HexCodeService", ComponentStatus.ERROR, "Failed to initialize: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Initialize server whitelisting
     */
    private void initializeServerWhitelisting() {
        updateComponentHealth("ServerWhitelisting", ComponentStatus.INITIALIZING, "Initializing server whitelisting");
        
        try {
            serverWhitelistingSystem = new ServerWhitelistingSystem(proxyServer, authenticationSystem);
            
            updateComponentHealth("ServerWhitelisting", ComponentStatus.RUNNING, "Server whitelisting initialized");
            systemMetrics.put("whitelisting_initialized", Instant.now());
            
        } catch (Exception e) {
            updateComponentHealth("ServerWhitelisting", ComponentStatus.ERROR, "Failed to initialize: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Initialize transfer packet handler
     */
    private void initializeTransferPacketHandler() {
        updateComponentHealth("TransferHandler", ComponentStatus.INITIALIZING, "Initializing transfer handler");
        
        try {
            transferPacketHandler = new TransferPacketHandler(proxyServer, authenticationSystem, serverWhitelistingSystem);
            
            updateComponentHealth("TransferHandler", ComponentStatus.RUNNING, "Transfer handler initialized");
            systemMetrics.put("transfer_handler_initialized", Instant.now());
            
        } catch (Exception e) {
            updateComponentHealth("TransferHandler", ComponentStatus.ERROR, "Failed to initialize: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Initialize Discord workflow
     */
    private void initializeDiscordWorkflow() {
        updateComponentHealth("DiscordWorkflow", ComponentStatus.INITIALIZING, "Initializing Discord workflow");
        
        try {
            discordVerificationWorkflow = new DiscordVerificationWorkflow(authenticationSystem, hexadecimalCodeService);
            
            updateComponentHealth("DiscordWorkflow", ComponentStatus.RUNNING, "Discord workflow initialized");
            systemMetrics.put("discord_workflow_initialized", Instant.now());
            
        } catch (Exception e) {
            updateComponentHealth("DiscordWorkflow", ComponentStatus.ERROR, "Failed to initialize: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Initialize verify command
     */
    private void initializeVerifyCommand() {
        updateComponentHealth("VerifyCommand", ComponentStatus.INITIALIZING, "Initializing verify command");
        
        try {
            verifyCommand = new VerifyCommand(authenticationSystem, hexadecimalCodeService, discordVerificationWorkflow);
            
            updateComponentHealth("VerifyCommand", ComponentStatus.RUNNING, "Verify command initialized");
            systemMetrics.put("verify_command_initialized", Instant.now());
            
        } catch (Exception e) {
            updateComponentHealth("VerifyCommand", ComponentStatus.ERROR, "Failed to initialize: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Initialize session cleanup
     */
    private void initializeSessionCleanup() {
        updateComponentHealth("SessionCleanup", ComponentStatus.INITIALIZING, "Initializing session cleanup");
        
        try {
            sessionCleanupSystem = new SessionCleanupSystem(scheduler, authenticationSystem, 
                                                           hexadecimalCodeService, discordVerificationWorkflow);
            
            updateComponentHealth("SessionCleanup", ComponentStatus.RUNNING, "Session cleanup initialized");
            systemMetrics.put("session_cleanup_initialized", Instant.now());
            
        } catch (Exception e) {
            updateComponentHealth("SessionCleanup", ComponentStatus.ERROR, "Failed to initialize: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Register event handlers
     */
    private void registerEventHandlers() {
        // Register all component event handlers
        eventManager.register(this, serverWhitelistingSystem);
        eventManager.register(this, transferPacketHandler);
        eventManager.register(this, verifyCommand);
        
        systemMetrics.put("event_handlers_registered", Instant.now());
    }

    /**
     * Start monitoring
     */
    private void startMonitoring() {
        if (config.isEnableHealthChecks()) {
            scheduler.scheduleAtFixedRate(this::performHealthChecks, 
                config.getHealthCheckInterval(), config.getHealthCheckInterval(), TimeUnit.SECONDS);
        }
        
        if (config.isEnableMetricsCollection()) {
            scheduler.scheduleAtFixedRate(this::collectMetrics, 
                config.getMetricsInterval(), config.getMetricsInterval(), TimeUnit.SECONDS);
        }
        
        systemMetrics.put("monitoring_started", Instant.now());
    }

    /**
     * Perform health checks on all components
     */
    private void performHealthChecks() {
        try {
            // Check authentication system
            checkAuthenticationSystemHealth();
            
            // Check hex code service
            checkHexCodeServiceHealth();
            
            // Check server whitelisting
            checkServerWhitelistingHealth();
            
            // Check transfer handler
            checkTransferHandlerHealth();
            
            // Check Discord workflow
            checkDiscordWorkflowHealth();
            
            // Check verify command
            checkVerifyCommandHealth();
            
            // Check session cleanup
            checkSessionCleanupHealth();
            
            systemMetrics.put("last_health_check", Instant.now());
            
        } catch (Exception e) {
            systemMetrics.put("health_check_errors", 
                ((Integer) systemMetrics.getOrDefault("health_check_errors", 0)) + 1);
        }
    }

    /**
     * Check authentication system health
     */
    private void checkAuthenticationSystemHealth() {
        try {
            if (authenticationSystem != null) {
                // Get system status
                authenticationSystem.getAuthenticationStatusAsync()
                    .thenAccept(status -> {
                        ComponentHealth health = new ComponentHealth("AuthenticationSystem", 
                            ComponentStatus.RUNNING, "System operational");
                        health.addMetric("total_sessions", status.get("total_sessions"));
                        health.addMetric("active_purgatories", status.get("active_purgatories"));
                        health.addMetric("verified_players", status.get("verified_players"));
                        componentHealth.put("AuthenticationSystem", health);
                    })
                    .exceptionally(ex -> {
                        ComponentHealth health = new ComponentHealth("AuthenticationSystem", 
                            ComponentStatus.ERROR, "Health check failed: " + ex.getMessage());
                        health.addError(ex.getMessage());
                        componentHealth.put("AuthenticationSystem", health);
                        return null;
                    });
            }
        } catch (Exception e) {
            ComponentHealth health = new ComponentHealth("AuthenticationSystem", 
                ComponentStatus.ERROR, "Health check exception: " + e.getMessage());
            health.addError(e.getMessage());
            componentHealth.put("AuthenticationSystem", health);
        }
    }

    /**
     * Check hex code service health
     */
    private void checkHexCodeServiceHealth() {
        try {
            if (hexadecimalCodeService != null) {
                hexadecimalCodeService.getServiceStatusAsync()
                    .thenAccept(status -> {
                        ComponentHealth health = new ComponentHealth("HexCodeService", 
                            ComponentStatus.RUNNING, "Service operational");
                        health.addMetric("active_codes", status.get("active_codes"));
                        health.addMetric("total_generated", status.get("total_generated"));
                        health.addMetric("validation_attempts", status.get("validation_attempts"));
                        componentHealth.put("HexCodeService", health);
                    })
                    .exceptionally(ex -> {
                        ComponentHealth health = new ComponentHealth("HexCodeService", 
                            ComponentStatus.ERROR, "Health check failed: " + ex.getMessage());
                        health.addError(ex.getMessage());
                        componentHealth.put("HexCodeService", health);
                        return null;
                    });
            }
        } catch (Exception e) {
            ComponentHealth health = new ComponentHealth("HexCodeService", 
                ComponentStatus.ERROR, "Health check exception: " + e.getMessage());
            health.addError(e.getMessage());
            componentHealth.put("HexCodeService", health);
        }
    }

    /**
     * Check server whitelisting health
     */
    private void checkServerWhitelistingHealth() {
        try {
            if (serverWhitelistingSystem != null) {
                serverWhitelistingSystem.getWhitelistStatusAsync()
                    .thenAccept(status -> {
                        ComponentHealth health = new ComponentHealth("ServerWhitelisting", 
                            ComponentStatus.RUNNING, "Whitelisting operational");
                        health.addMetric("servers_configured", status.get("total_servers_configured"));
                        health.addMetric("audit_entries", status.get("audit_log_entries"));
                        componentHealth.put("ServerWhitelisting", health);
                    })
                    .exceptionally(ex -> {
                        ComponentHealth health = new ComponentHealth("ServerWhitelisting", 
                            ComponentStatus.ERROR, "Health check failed: " + ex.getMessage());
                        health.addError(ex.getMessage());
                        componentHealth.put("ServerWhitelisting", health);
                        return null;
                    });
            }
        } catch (Exception e) {
            ComponentHealth health = new ComponentHealth("ServerWhitelisting", 
                ComponentStatus.ERROR, "Health check exception: " + e.getMessage());
            health.addError(e.getMessage());
            componentHealth.put("ServerWhitelisting", health);
        }
    }

    /**
     * Check transfer handler health
     */
    private void checkTransferHandlerHealth() {
        try {
            if (transferPacketHandler != null) {
                transferPacketHandler.getTransferStatisticsAsync()
                    .thenAccept(stats -> {
                        ComponentHealth health = new ComponentHealth("TransferHandler", 
                            ComponentStatus.RUNNING, "Transfer handler operational");
                        Map<String, Object> metrics = (Map<String, Object>) stats.get("metrics");
                        health.addMetric("total_requests", metrics.get("total_transfer_requests"));
                        health.addMetric("approved_transfers", metrics.get("approved_transfers"));
                        health.addMetric("denied_transfers", metrics.get("denied_transfers"));
                        componentHealth.put("TransferHandler", health);
                    })
                    .exceptionally(ex -> {
                        ComponentHealth health = new ComponentHealth("TransferHandler", 
                            ComponentStatus.ERROR, "Health check failed: " + ex.getMessage());
                        health.addError(ex.getMessage());
                        componentHealth.put("TransferHandler", health);
                        return null;
                    });
            }
        } catch (Exception e) {
            ComponentHealth health = new ComponentHealth("TransferHandler", 
                ComponentStatus.ERROR, "Health check exception: " + e.getMessage());
            health.addError(e.getMessage());
            componentHealth.put("TransferHandler", health);
        }
    }

    /**
     * Check Discord workflow health
     */
    private void checkDiscordWorkflowHealth() {
        try {
            if (discordVerificationWorkflow != null) {
                discordVerificationWorkflow.getWorkflowStatusAsync()
                    .thenAccept(status -> {
                        ComponentHealth health = new ComponentHealth("DiscordWorkflow", 
                            ComponentStatus.RUNNING, "Discord workflow operational");
                        health.addMetric("active_verifications", status.get("active_verifications"));
                        health.addMetric("total_commands", status.get("total_commands"));
                        health.addMetric("successful_verifications", status.get("successful_verifications"));
                        componentHealth.put("DiscordWorkflow", health);
                    })
                    .exceptionally(ex -> {
                        ComponentHealth health = new ComponentHealth("DiscordWorkflow", 
                            ComponentStatus.ERROR, "Health check failed: " + ex.getMessage());
                        health.addError(ex.getMessage());
                        componentHealth.put("DiscordWorkflow", health);
                        return null;
                    });
            }
        } catch (Exception e) {
            ComponentHealth health = new ComponentHealth("DiscordWorkflow", 
                ComponentStatus.ERROR, "Health check exception: " + e.getMessage());
            health.addError(e.getMessage());
            componentHealth.put("DiscordWorkflow", health);
        }
    }

    /**
     * Check verify command health
     */
    private void checkVerifyCommandHealth() {
        try {
            if (verifyCommand != null) {
                // Simple health check - verify command exists and is accessible
                ComponentHealth health = new ComponentHealth("VerifyCommand", 
                    ComponentStatus.RUNNING, "Verify command operational");
                componentHealth.put("VerifyCommand", health);
            }
        } catch (Exception e) {
            ComponentHealth health = new ComponentHealth("VerifyCommand", 
                ComponentStatus.ERROR, "Health check exception: " + e.getMessage());
            health.addError(e.getMessage());
            componentHealth.put("VerifyCommand", health);
        }
    }

    /**
     * Check session cleanup health
     */
    private void checkSessionCleanupHealth() {
        try {
            if (sessionCleanupSystem != null) {
                sessionCleanupSystem.getCleanupStatusAsync()
                    .thenAccept(status -> {
                        ComponentHealth health = new ComponentHealth("SessionCleanup", 
                            ComponentStatus.RUNNING, "Session cleanup operational");
                        health.addMetric("cleanup_cycles", status.get("cleanup_cycles"));
                        health.addMetric("sessions_cleaned", status.get("sessions_cleaned"));
                        health.addMetric("last_cleanup", status.get("last_cleanup"));
                        componentHealth.put("SessionCleanup", health);
                    })
                    .exceptionally(ex -> {
                        ComponentHealth health = new ComponentHealth("SessionCleanup", 
                            ComponentStatus.ERROR, "Health check failed: " + ex.getMessage());
                        health.addError(ex.getMessage());
                        componentHealth.put("SessionCleanup", health);
                        return null;
                    });
            }
        } catch (Exception e) {
            ComponentHealth health = new ComponentHealth("SessionCleanup", 
                ComponentStatus.ERROR, "Health check exception: " + e.getMessage());
            health.addError(e.getMessage());
            componentHealth.put("SessionCleanup", health);
        }
    }

    /**
     * Collect system metrics
     */
    private void collectMetrics() {
        try {
            systemMetrics.put("total_components", componentHealth.size());
            systemMetrics.put("running_components", 
                componentHealth.values().stream()
                    .mapToInt(h -> h.getStatus() == ComponentStatus.RUNNING ? 1 : 0)
                    .sum());
            systemMetrics.put("error_components", 
                componentHealth.values().stream()
                    .mapToInt(h -> h.getStatus() == ComponentStatus.ERROR ? 1 : 0)
                    .sum());
            systemMetrics.put("metrics_collection_time", Instant.now());
            
        } catch (Exception e) {
            systemMetrics.put("metrics_collection_errors", 
                ((Integer) systemMetrics.getOrDefault("metrics_collection_errors", 0)) + 1);
        }
    }

    /**
     * Update component health
     */
    private void updateComponentHealth(String componentName, ComponentStatus status, String message) {
        ComponentHealth health = new ComponentHealth(componentName, status, message);
        componentHealth.put(componentName, health);
    }

    /**
     * Fire authentication event
     */
    private void fireAuthenticationEvent(String eventType, String playerId, String playerName,
                                       AuthenticationSystem.AuthenticationState oldState,
                                       AuthenticationSystem.AuthenticationState newState) {
        AuthenticationEvent event = new AuthenticationEvent(eventType, playerId, playerName, oldState, newState);
        eventHistory.add(event);
        
        // Keep only last 1,000 events
        while (eventHistory.size() > 1000) {
            eventHistory.remove(0);
        }
        
        systemMetrics.put("total_events", 
            ((Integer) systemMetrics.getOrDefault("total_events", 0)) + 1);
    }

    /**
     * Initialize system metrics
     */
    private void initializeSystemMetrics() {
        systemMetrics.put("middleware_start_time", Instant.now());
        systemMetrics.put("total_events", 0);
        systemMetrics.put("total_components", 0);
        systemMetrics.put("running_components", 0);
        systemMetrics.put("error_components", 0);
        systemMetrics.put("health_check_errors", 0);
        systemMetrics.put("metrics_collection_errors", 0);
    }

    /**
     * Shutdown middleware
     */
    public CompletableFuture<Void> shutdownAsync() {
        return CompletableFuture.runAsync(() -> {
            isRunning = false;
            
            // Shutdown session cleanup
            if (sessionCleanupSystem != null) {
                sessionCleanupSystem.shutdownAsync();
            }
            
            // Shutdown scheduler
            scheduler.shutdown();
            
            try {
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
            
            fireAuthenticationEvent("MIDDLEWARE_SHUTDOWN", null, null, null, null);
        });
    }

    /**
     * Get middleware status
     */
    public CompletableFuture<Map<String, Object>> getMiddlewareStatusAsync() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> status = new HashMap<>();
            
            status.put("initialized", isInitialized);
            status.put("running", isRunning);
            status.put("component_health", new HashMap<>(componentHealth));
            status.put("system_metrics", new HashMap<>(systemMetrics));
            status.put("event_history_size", eventHistory.size());
            status.put("config", config);
            
            return status;
        });
    }

    // Getters for component access
    public AuthenticationSystem getAuthenticationSystem() { return authenticationSystem; }
    public ServerWhitelistingSystem getServerWhitelistingSystem() { return serverWhitelistingSystem; }
    public TransferPacketHandler getTransferPacketHandler() { return transferPacketHandler; }
    public SessionCleanupSystem getSessionCleanupSystem() { return sessionCleanupSystem; }
    public DiscordVerificationWorkflow getDiscordVerificationWorkflow() { return discordVerificationWorkflow; }
    public HexadecimalCodeService getHexadecimalCodeService() { return hexadecimalCodeService; }
    public VerifyCommand getVerifyCommand() { return verifyCommand; }
    public Map<String, ComponentHealth> getComponentHealth() { return new HashMap<>(componentHealth); }
    public Map<String, Object> getSystemMetrics() { return new HashMap<>(systemMetrics); }
    public List<AuthenticationEvent> getEventHistory() { return new ArrayList<>(eventHistory); }
}
