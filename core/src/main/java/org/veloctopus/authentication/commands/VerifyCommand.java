/*
 * This file is part of VeloctopusProject, licensed under the MIT License.
 *
 * Copyright (c) 2025 VeloctopusProject Contributors
 * 
 * Brigadier Command Listener for /verify
 * Step 34: Implement Brigadier command listener for /verify
 */

package org.veloctopus.authentication.commands;

import org.veloctopus.authentication.AuthenticationSystem;
import org.veloctopus.authentication.HexadecimalCodeService;
import org.veloctopus.authentication.discord.DiscordVerificationWorkflow;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.time.Duration;
import java.util.regex.Pattern;

/**
 * Brigadier Command Listener for /verify
 * 
 * Implements the `/verify <hexcode>` command for Minecraft client verification:
 * 
 * Command: /verify <hexcode>
 * - Validates hexadecimal verification code
 * - Integrates with Discord verification workflow
 * - Handles authentication state transitions
 * - Provides real-time feedback and error handling
 * - Supports Geyser/Floodgate prefix detection
 * 
 * Features:
 * - Brigadier command registration with auto-completion
 * - Comprehensive input validation and sanitization
 * - Rate limiting and anti-spam protection
 * - Rich Adventure Component messages with formatting
 * - Integration with authentication and hex code systems
 * - Audit logging for all verification attempts
 * - Support for multiple verification contexts
 * 
 * Command Flow:
 * 1. Player executes `/verify <hexcode>`
 * 2. Command validates hex code format
 * 3. System checks authentication state
 * 4. Code validation against Discord session
 * 5. Authentication state transition
 * 6. Player notification and audit logging
 * 
 * @author VeloctopusProject Team
 * @since 1.0.0
 */
public class VerifyCommand implements SimpleCommand {

    /**
     * Command execution result types
     */
    public enum CommandResult {
        SUCCESS,
        INVALID_CODE_FORMAT,
        CODE_NOT_FOUND,
        CODE_EXPIRED,
        ALREADY_VERIFIED,
        RATE_LIMITED,
        PLAYER_NOT_FOUND,
        AUTHENTICATION_ERROR,
        SYSTEM_ERROR
    }

    /**
     * Command audit log entry
     */
    public static class CommandAuditLog {
        private final String playerName;
        private final String playerId;
        private final String hexCode;
        private final CommandResult result;
        private final Instant timestamp;
        private final String ipAddress;
        private final Map<String, Object> additionalData;

        public CommandAuditLog(String playerName, String playerId, String hexCode, 
                             CommandResult result, String ipAddress) {
            this.playerName = playerName;
            this.playerId = playerId;
            this.hexCode = hexCode;
            this.result = result;
            this.timestamp = Instant.now();
            this.ipAddress = ipAddress;
            this.additionalData = new ConcurrentHashMap<>();
        }

        // Getters
        public String getPlayerName() { return playerName; }
        public String getPlayerId() { return playerId; }
        public String getHexCode() { return hexCode; }
        public CommandResult getResult() { return result; }
        public Instant getTimestamp() { return timestamp; }
        public String getIpAddress() { return ipAddress; }
        public Map<String, Object> getAdditionalData() { return new ConcurrentHashMap<>(additionalData); }
        public void setAdditionalData(String key, Object value) { additionalData.put(key, value); }
    }

    /**
     * Rate limiting tracker
     */
    public static class CommandRateLimiter {
        private final Map<String, List<Instant>> commandAttempts;
        private final Duration rateLimitWindow;
        private final int maxAttemptsPerWindow;

        public CommandRateLimiter() {
            this.commandAttempts = new ConcurrentHashMap<>();
            this.rateLimitWindow = Duration.ofMinutes(2);
            this.maxAttemptsPerWindow = 5;
        }

        public boolean isRateLimited(String playerId) {
            cleanupOldAttempts(playerId);
            List<Instant> attempts = commandAttempts.getOrDefault(playerId, new ArrayList<>());
            return attempts.size() >= maxAttemptsPerWindow;
        }

        public void recordAttempt(String playerId) {
            commandAttempts.computeIfAbsent(playerId, k -> new ArrayList<>()).add(Instant.now());
        }

        private void cleanupOldAttempts(String playerId) {
            List<Instant> attempts = commandAttempts.get(playerId);
            if (attempts != null) {
                Instant cutoff = Instant.now().minus(rateLimitWindow);
                attempts.removeIf(attempt -> attempt.isBefore(cutoff));
                
                if (attempts.isEmpty()) {
                    commandAttempts.remove(playerId);
                }
            }
        }

        public void cleanup() {
            Instant cutoff = Instant.now().minus(rateLimitWindow);
            commandAttempts.entrySet().removeIf(entry -> {
                entry.getValue().removeIf(attempt -> attempt.isBefore(cutoff));
                return entry.getValue().isEmpty();
            });
        }
    }

    // Core components
    private final ProxyServer proxyServer;
    private final AuthenticationSystem authenticationSystem;
    private final HexadecimalCodeService hexCodeService;
    private final DiscordVerificationWorkflow discordWorkflow;
    private final CommandRateLimiter rateLimiter;
    private final List<CommandAuditLog> auditLog;
    
    // Configuration
    private final Pattern hexCodePattern;
    private final Map<String, Component> messages;
    
    // Monitoring
    private final Map<String, Object> commandMetrics;

    public VerifyCommand(ProxyServer proxyServer, AuthenticationSystem authenticationSystem, 
                        HexadecimalCodeService hexCodeService, DiscordVerificationWorkflow discordWorkflow) {
        this.proxyServer = proxyServer;
        this.authenticationSystem = authenticationSystem;
        this.hexCodeService = hexCodeService;
        this.discordWorkflow = discordWorkflow;
        this.rateLimiter = new CommandRateLimiter();
        this.auditLog = Collections.synchronizedList(new ArrayList<>());
        
        // Configuration
        this.hexCodePattern = Pattern.compile("^[0-9a-fA-F]{6,20}$");
        this.messages = new ConcurrentHashMap<>();
        
        this.commandMetrics = new ConcurrentHashMap<>();
        this.commandMetrics.put("total_commands_executed", 0);
        this.commandMetrics.put("successful_verifications", 0);
        this.commandMetrics.put("failed_verifications", 0);
        this.commandMetrics.put("rate_limited_attempts", 0);
        
        initializeMessages();
    }

    /**
     * Initialize command messages
     */
    private void initializeMessages() {
        messages.put("verification_success", Component.text()
            .append(Component.text("✅ ", NamedTextColor.GREEN))
            .append(Component.text("Verification Successful!", NamedTextColor.GREEN, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("Welcome to the server! You now have full access.", NamedTextColor.GRAY))
            .build());

        messages.put("invalid_code_format", Component.text()
            .append(Component.text("❌ ", NamedTextColor.RED))
            .append(Component.text("Invalid Code Format", NamedTextColor.RED, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("Please provide a valid hexadecimal verification code.", NamedTextColor.GRAY))
            .build());

        messages.put("code_not_found", Component.text()
            .append(Component.text("❌ ", NamedTextColor.RED))
            .append(Component.text("Code Not Found", NamedTextColor.RED, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("The verification code was not found or has already been used.", NamedTextColor.GRAY))
            .build());

        messages.put("code_expired", Component.text()
            .append(Component.text("⏰ ", NamedTextColor.YELLOW))
            .append(Component.text("Code Expired", NamedTextColor.YELLOW, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("Your verification code has expired. Please request a new one in Discord.", NamedTextColor.GRAY))
            .build());

        messages.put("already_verified", Component.text()
            .append(Component.text("ℹ️ ", NamedTextColor.AQUA))
            .append(Component.text("Already Verified", NamedTextColor.AQUA, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("You are already verified and have full server access.", NamedTextColor.GRAY))
            .build());

        messages.put("rate_limited", Component.text()
            .append(Component.text("⏱️ ", NamedTextColor.YELLOW))
            .append(Component.text("Rate Limited", NamedTextColor.YELLOW, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("Please wait before attempting verification again.", NamedTextColor.GRAY))
            .build());

        messages.put("usage", Component.text()
            .append(Component.text("Usage: ", NamedTextColor.YELLOW))
            .append(Component.text("/verify <hexcode>", NamedTextColor.WHITE, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("Use the verification code from Discord to complete authentication.", NamedTextColor.GRAY))
            .build());

        messages.put("system_error", Component.text()
            .append(Component.text("⚠️ ", NamedTextColor.RED))
            .append(Component.text("System Error", NamedTextColor.RED, TextDecoration.BOLD))
            .append(Component.newline())
            .append(Component.text("An error occurred during verification. Please try again later.", NamedTextColor.GRAY))
            .build());
    }

    /**
     * Execute /verify command
     */
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        // Only players can use this command
        if (!(source instanceof Player)) {
            source.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return;
        }

        Player player = (Player) source;
        
        // Update metrics
        commandMetrics.put("total_commands_executed",
            ((Integer) commandMetrics.getOrDefault("total_commands_executed", 0)) + 1);
        commandMetrics.put("last_command_execution", Instant.now());

        // Check arguments
        if (args.length != 1) {
            player.sendMessage(messages.get("usage"));
            logCommandExecution(player, "", CommandResult.INVALID_CODE_FORMAT);
            return;
        }

        String hexCode = args[0].trim().toLowerCase();
        
        // Validate hex code format
        if (!hexCodePattern.matcher(hexCode).matches()) {
            player.sendMessage(messages.get("invalid_code_format"));
            logCommandExecution(player, hexCode, CommandResult.INVALID_CODE_FORMAT);
            commandMetrics.put("failed_verifications",
                ((Integer) commandMetrics.getOrDefault("failed_verifications", 0)) + 1);
            return;
        }

        // Check rate limiting
        if (rateLimiter.isRateLimited(player.getUniqueId().toString())) {
            player.sendMessage(messages.get("rate_limited"));
            logCommandExecution(player, hexCode, CommandResult.RATE_LIMITED);
            commandMetrics.put("rate_limited_attempts",
                ((Integer) commandMetrics.getOrDefault("rate_limited_attempts", 0)) + 1);
            return;
        }

        // Record attempt
        rateLimiter.recordAttempt(player.getUniqueId().toString());

        // Process verification
        processVerification(player, hexCode);
    }

    /**
     * Process verification with hex code
     */
    private void processVerification(Player player, String hexCode) {
        // Get player name (handle Geyser prefix)
        String playerName = player.getUsername();
        String cleanPlayerName = playerName.startsWith(".") ? playerName.substring(1) : playerName;

        // Check current authentication state
        authenticationSystem.handlePlayerJoinAsync(cleanPlayerName)
            .thenCompose(authState -> {
                // Check if already verified
                if (authState == AuthenticationSystem.AuthenticationState.MEMBER || 
                    authState == AuthenticationSystem.AuthenticationState.VERIFIED) {
                    player.sendMessage(messages.get("already_verified"));
                    logCommandExecution(player, hexCode, CommandResult.ALREADY_VERIFIED);
                    return CompletableFuture.completedFuture(false);
                }

                // Validate hex code
                return hexCodeService.validateCodeAsync(hexCode, null, 
                    player.getRemoteAddress().getAddress().getHostAddress())
                    .thenCompose(validationResult -> {
                        switch (validationResult) {
                            case VALID:
                                return completeVerification(player, hexCode, cleanPlayerName);
                            case CODE_NOT_FOUND:
                            case CODE_ALREADY_USED:
                                player.sendMessage(messages.get("code_not_found"));
                                logCommandExecution(player, hexCode, CommandResult.CODE_NOT_FOUND);
                                return CompletableFuture.completedFuture(false);
                            case CODE_EXPIRED:
                                player.sendMessage(messages.get("code_expired"));
                                logCommandExecution(player, hexCode, CommandResult.CODE_EXPIRED);
                                return CompletableFuture.completedFuture(false);
                            case RATE_LIMITED:
                                player.sendMessage(messages.get("rate_limited"));
                                logCommandExecution(player, hexCode, CommandResult.RATE_LIMITED);
                                return CompletableFuture.completedFuture(false);
                            default:
                                player.sendMessage(messages.get("system_error"));
                                logCommandExecution(player, hexCode, CommandResult.SYSTEM_ERROR);
                                return CompletableFuture.completedFuture(false);
                        }
                    });
            })
            .exceptionally(throwable -> {
                player.sendMessage(messages.get("system_error"));
                logCommandExecution(player, hexCode, CommandResult.SYSTEM_ERROR);
                commandMetrics.put("failed_verifications",
                    ((Integer) commandMetrics.getOrDefault("failed_verifications", 0)) + 1);
                return false;
            });
    }

    /**
     * Complete verification process
     */
    private CompletableFuture<Boolean> completeVerification(Player player, String hexCode, String cleanPlayerName) {
        // Get hex code info to find Discord user
        return hexCodeService.getCodeInfoAsync(hexCode)
            .thenCompose(optionalCode -> {
                if (optionalCode.isPresent()) {
                    String discordUserId = optionalCode.get().getDiscordUserId();
                    
                    // Complete Discord workflow
                    return discordWorkflow.completeVerificationAsync(discordUserId, cleanPlayerName)
                        .thenApply(discordCompleted -> {
                            if (discordCompleted) {
                                // Send success message
                                player.sendMessage(messages.get("verification_success"));
                                
                                // Log successful verification
                                logCommandExecution(player, hexCode, CommandResult.SUCCESS);
                                
                                // Update metrics
                                commandMetrics.put("successful_verifications",
                                    ((Integer) commandMetrics.getOrDefault("successful_verifications", 0)) + 1);
                                commandMetrics.put("last_successful_verification", Instant.now());
                                
                                // Send welcome message with additional info
                                Component welcomeMessage = Component.text()
                                    .append(Component.newline())
                                    .append(Component.text("🎉 ", NamedTextColor.GOLD))
                                    .append(Component.text("Welcome to the VeloctopusProject Network!", 
                                           NamedTextColor.GOLD, TextDecoration.BOLD))
                                    .append(Component.newline())
                                    .append(Component.text("• You now have access to all servers", NamedTextColor.GREEN))
                                    .append(Component.newline())
                                    .append(Component.text("• Check out our features with ", NamedTextColor.GRAY))
                                    .append(Component.text("/help", NamedTextColor.AQUA, TextDecoration.UNDERLINED))
                                    .append(Component.newline())
                                    .append(Component.text("• Join our Discord community for updates and support", NamedTextColor.GRAY))
                                    .build();
                                
                                player.sendMessage(welcomeMessage);
                                
                                return true;
                            } else {
                                player.sendMessage(messages.get("system_error"));
                                logCommandExecution(player, hexCode, CommandResult.AUTHENTICATION_ERROR);
                                return false;
                            }
                        });
                } else {
                    player.sendMessage(messages.get("code_not_found"));
                    logCommandExecution(player, hexCode, CommandResult.CODE_NOT_FOUND);
                    return CompletableFuture.completedFuture(false);
                }
            });
    }

    /**
     * Log command execution
     */
    private void logCommandExecution(Player player, String hexCode, CommandResult result) {
        String ipAddress = player.getRemoteAddress().getAddress().getHostAddress();
        CommandAuditLog logEntry = new CommandAuditLog(
            player.getUsername(), 
            player.getUniqueId().toString(), 
            hexCode, 
            result, 
            ipAddress
        );
        
        auditLog.add(logEntry);
        
        // Keep only last 10,000 entries
        while (auditLog.size() > 10000) {
            auditLog.remove(0);
        }
    }

    /**
     * Register Brigadier command
     */
    public static void registerBrigadierCommand(CommandDispatcher<CommandSource> dispatcher, VerifyCommand command) {
        LiteralArgumentBuilder<CommandSource> verifyCommand = LiteralArgumentBuilder.<CommandSource>literal("verify")
            .then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<CommandSource, String>argument("hexcode", StringArgumentType.string())
                .suggests((context, builder) -> {
                    // Provide example suggestions
                    builder.suggest("a1b2c3d4", Component.text("Enter your verification code"));
                    return builder.buildFuture();
                })
                .executes(context -> {
                    String hexCode = StringArgumentType.getString(context, "hexcode");
                    CommandSource source = context.getSource();
                    
                    // Create invocation and execute
                    SimpleCommand.Invocation invocation = new SimpleCommand.Invocation() {
                        @Override
                        public CommandSource source() { return source; }
                        
                        @Override
                        public String[] arguments() { return new String[]{hexCode}; }
                    };
                    
                    command.execute(invocation);
                    return 1;
                }));

        dispatcher.register(verifyCommand);
    }

    /**
     * Get command suggestions
     */
    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length == 0) {
            return Arrays.asList("a1b2c3d4");
        }
        return Collections.emptyList();
    }

    /**
     * Check command permission
     */
    @Override
    public boolean hasPermission(Invocation invocation) {
        // All players can use the verify command
        return invocation.source() instanceof Player;
    }

    /**
     * Cleanup expired data
     */
    public void cleanup() {
        rateLimiter.cleanup();
        
        // Remove old audit logs (keep 30 days)
        Instant cutoff = Instant.now().minus(Duration.ofDays(30));
        auditLog.removeIf(log -> log.getTimestamp().isBefore(cutoff));
    }

    /**
     * Get command status
     */
    public CompletableFuture<Map<String, Object>> getCommandStatusAsync() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> status = new HashMap<>();
            
            status.put("metrics", new HashMap<>(commandMetrics));
            status.put("audit_log_entries", auditLog.size());
            status.put("rate_limiter_active_players", rateLimiter.commandAttempts.size());
            
            return status;
        });
    }

    // Getters
    public Map<String, Object> getCommandMetrics() { return new HashMap<>(commandMetrics); }
    public List<CommandAuditLog> getAuditLog() { return new ArrayList<>(auditLog); }
}
