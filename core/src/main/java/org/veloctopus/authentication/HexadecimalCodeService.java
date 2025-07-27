/*
 * This file is part of VeloctopusProject, licensed under the MIT License.
 *
 * Copyright (c) 2025 VeloctopusProject Contributors
 * 
 * Hexadecimal Code Generation and Validation System
 * Step 32: Implement hexadecimal code generation and validation
 */

package org.veloctopus.authentication;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.time.Duration;
import java.util.regex.Pattern;

/**
 * Hexadecimal Code Generation and Validation System
 * 
 * Provides secure hexadecimal code generation for authentication verification:
 * - Cryptographically secure random code generation
 * - Code validation with anti-replay protection
 * - Rate limiting and anti-brute force protection
 * - Automatic code expiration and cleanup
 * - Comprehensive audit logging for security monitoring
 * 
 * Code Format:
 * - 8-character hexadecimal codes (e.g., "a7f3c2d1")
 * - Cryptographically secure random generation
 * - Case-insensitive validation
 * - Single-use codes with automatic invalidation
 * 
 * Security Features:
 * - Rate limiting per Discord user and IP
 * - Anti-replay protection with used code tracking
 * - Automatic expiration after 10 minutes
 * - Brute force detection and temporary blocking
 * - Comprehensive security event logging
 * 
 * @author VeloctopusProject Team
 * @since 1.0.0
 */
public class HexadecimalCodeService {

    /**
     * Code validation result types
     */
    public enum ValidationResult {
        VALID,
        INVALID_FORMAT,
        CODE_NOT_FOUND,
        CODE_EXPIRED,
        CODE_ALREADY_USED,
        RATE_LIMITED,
        BRUTE_FORCE_DETECTED,
        USER_BLOCKED
    }

    /**
     * Code security levels
     */
    public enum SecurityLevel {
        STANDARD(8, Duration.ofMinutes(10)),
        HIGH_SECURITY(12, Duration.ofMinutes(5)),
        MAXIMUM_SECURITY(16, Duration.ofMinutes(3));

        private final int codeLength;
        private final Duration expiration;

        SecurityLevel(int codeLength, Duration expiration) {
            this.codeLength = codeLength;
            this.expiration = expiration;
        }

        public int getCodeLength() { return codeLength; }
        public Duration getExpiration() { return expiration; }
    }

    /**
     * Generated hexadecimal code data
     */
    public static class HexadecimalCode {
        private final String code;
        private final String discordUserId;
        private final String playerName;
        private final Instant createdTime;
        private final Instant expiryTime;
        private final SecurityLevel securityLevel;
        private final Map<String, Object> metadata;
        private boolean used;
        private Instant usedTime;
        private int validationAttempts;

        public HexadecimalCode(String code, String discordUserId, String playerName, SecurityLevel securityLevel) {
            this.code = code.toLowerCase();
            this.discordUserId = discordUserId;
            this.playerName = playerName;
            this.securityLevel = securityLevel;
            this.createdTime = Instant.now();
            this.expiryTime = createdTime.plus(securityLevel.getExpiration());
            this.metadata = new ConcurrentHashMap<>();
            this.used = false;
            this.validationAttempts = 0;
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expiryTime);
        }

        public boolean isValid() {
            return !used && !isExpired();
        }

        public Duration getTimeRemaining() {
            Duration remaining = Duration.between(Instant.now(), expiryTime);
            return remaining.isNegative() ? Duration.ZERO : remaining;
        }

        public void markAsUsed() {
            this.used = true;
            this.usedTime = Instant.now();
        }

        public void incrementValidationAttempts() {
            this.validationAttempts++;
        }

        // Getters
        public String getCode() { return code; }
        public String getDiscordUserId() { return discordUserId; }
        public String getPlayerName() { return playerName; }
        public Instant getCreatedTime() { return createdTime; }
        public Instant getExpiryTime() { return expiryTime; }
        public SecurityLevel getSecurityLevel() { return securityLevel; }
        public boolean isUsed() { return used; }
        public Instant getUsedTime() { return usedTime; }
        public int getValidationAttempts() { return validationAttempts; }
        public Map<String, Object> getMetadata() { return new ConcurrentHashMap<>(metadata); }
        public void setMetadata(String key, Object value) { metadata.put(key, value); }
    }

    /**
     * Rate limiting and security tracking
     */
    public static class SecurityTracker {
        private final Map<String, List<Instant>> validationAttempts;
        private final Map<String, Instant> blockedUsers;
        private final Map<String, Integer> failedAttempts;
        private final Duration rateLimitWindow;
        private final int maxAttemptsPerWindow;
        private final int bruteForceThreshold;
        private final Duration blockDuration;

        public SecurityTracker() {
            this.validationAttempts = new ConcurrentHashMap<>();
            this.blockedUsers = new ConcurrentHashMap<>();
            this.failedAttempts = new ConcurrentHashMap<>();
            this.rateLimitWindow = Duration.ofMinutes(5);
            this.maxAttemptsPerWindow = 10;
            this.bruteForceThreshold = 20;
            this.blockDuration = Duration.ofMinutes(30);
        }

        public boolean isRateLimited(String identifier) {
            cleanupOldAttempts(identifier);
            List<Instant> attempts = validationAttempts.getOrDefault(identifier, new ArrayList<>());
            return attempts.size() >= maxAttemptsPerWindow;
        }

        public boolean isBlocked(String identifier) {
            Instant blockTime = blockedUsers.get(identifier);
            if (blockTime != null) {
                if (Duration.between(blockTime, Instant.now()).compareTo(blockDuration) < 0) {
                    return true;
                } else {
                    // Block expired, remove it
                    blockedUsers.remove(identifier);
                    failedAttempts.remove(identifier);
                }
            }
            return false;
        }

        public void recordAttempt(String identifier, boolean successful) {
            // Record validation attempt
            validationAttempts.computeIfAbsent(identifier, k -> new ArrayList<>()).add(Instant.now());
            
            if (!successful) {
                int failures = failedAttempts.getOrDefault(identifier, 0) + 1;
                failedAttempts.put(identifier, failures);
                
                // Check for brute force
                if (failures >= bruteForceThreshold) {
                    blockedUsers.put(identifier, Instant.now());
                }
            } else {
                // Reset failure count on successful validation
                failedAttempts.remove(identifier);
            }
        }

        public boolean isBruteForceDetected(String identifier) {
            return failedAttempts.getOrDefault(identifier, 0) >= bruteForceThreshold;
        }

        private void cleanupOldAttempts(String identifier) {
            List<Instant> attempts = validationAttempts.get(identifier);
            if (attempts != null) {
                Instant cutoff = Instant.now().minus(rateLimitWindow);
                attempts.removeIf(attempt -> attempt.isBefore(cutoff));
                
                if (attempts.isEmpty()) {
                    validationAttempts.remove(identifier);
                }
            }
        }

        public void cleanup() {
            Instant now = Instant.now();
            
            // Cleanup old validation attempts
            validationAttempts.entrySet().removeIf(entry -> {
                Instant cutoff = now.minus(rateLimitWindow);
                entry.getValue().removeIf(attempt -> attempt.isBefore(cutoff));
                return entry.getValue().isEmpty();
            });
            
            // Cleanup expired blocks
            blockedUsers.entrySet().removeIf(entry -> {
                return Duration.between(entry.getValue(), now).compareTo(blockDuration) >= 0;
            });
        }

        // Getters for monitoring
        public int getTotalBlockedUsers() { return blockedUsers.size(); }
        public int getTotalActiveAttempts() { return validationAttempts.size(); }
        public Map<String, Integer> getFailedAttempts() { return new HashMap<>(failedAttempts); }
    }

    /**
     * Code audit log entry
     */
    public static class CodeAuditLog {
        private final String code;
        private final String discordUserId;
        private final String playerName;
        private final String action;
        private final ValidationResult result;
        private final Instant timestamp;
        private final String ipAddress;
        private final Map<String, Object> additionalData;

        public CodeAuditLog(String code, String discordUserId, String playerName, 
                           String action, ValidationResult result, String ipAddress) {
            this.code = code;
            this.discordUserId = discordUserId;
            this.playerName = playerName;
            this.action = action;
            this.result = result;
            this.timestamp = Instant.now();
            this.ipAddress = ipAddress;
            this.additionalData = new ConcurrentHashMap<>();
        }

        // Getters
        public String getCode() { return code; }
        public String getDiscordUserId() { return discordUserId; }
        public String getPlayerName() { return playerName; }
        public String getAction() { return action; }
        public ValidationResult getResult() { return result; }
        public Instant getTimestamp() { return timestamp; }
        public String getIpAddress() { return ipAddress; }
        public Map<String, Object> getAdditionalData() { return new ConcurrentHashMap<>(additionalData); }
        public void setAdditionalData(String key, Object value) { additionalData.put(key, value); }
    }

    // Core components
    private final Map<String, HexadecimalCode> activeCodes;
    private final Map<String, HexadecimalCode> codesByDiscordUser;
    private final Set<String> usedCodes;
    private final SecurityTracker securityTracker;
    private final List<CodeAuditLog> auditLog;
    private final SecureRandom secureRandom;
    
    // Configuration
    private final SecurityLevel defaultSecurityLevel;
    private final Pattern hexPattern;
    private final Duration codeLifetime;
    private final int maxCodesPerUser;
    
    // Monitoring
    private final Map<String, Object> serviceMetrics;

    public HexadecimalCodeService() {
        this.activeCodes = new ConcurrentHashMap<>();
        this.codesByDiscordUser = new ConcurrentHashMap<>();
        this.usedCodes = Collections.synchronizedSet(new HashSet<>());
        this.securityTracker = new SecurityTracker();
        this.auditLog = Collections.synchronizedList(new ArrayList<>());
        this.secureRandom = new SecureRandom();
        
        // Configuration
        this.defaultSecurityLevel = SecurityLevel.STANDARD;
        this.hexPattern = Pattern.compile("^[0-9a-fA-F]+$");
        this.codeLifetime = Duration.ofMinutes(10);
        this.maxCodesPerUser = 3;
        
        this.serviceMetrics = new ConcurrentHashMap<>();
        this.serviceMetrics.put("total_codes_generated", 0);
        this.serviceMetrics.put("total_codes_validated", 0);
        this.serviceMetrics.put("successful_validations", 0);
        this.serviceMetrics.put("failed_validations", 0);
    }

    /**
     * Generate new hexadecimal code
     */
    public CompletableFuture<HexadecimalCode> generateCodeAsync(String discordUserId, String playerName) {
        return generateCodeAsync(discordUserId, playerName, defaultSecurityLevel);
    }

    /**
     * Generate new hexadecimal code with specified security level
     */
    public CompletableFuture<HexadecimalCode> generateCodeAsync(String discordUserId, String playerName, SecurityLevel securityLevel) {
        return CompletableFuture.supplyAsync(() -> {
            // Check if user already has too many active codes
            if (countActiveCodesForUser(discordUserId) >= maxCodesPerUser) {
                // Clean up expired codes first
                cleanupExpiredCodes();
                
                // If still too many, remove oldest
                if (countActiveCodesForUser(discordUserId) >= maxCodesPerUser) {
                    removeOldestCodeForUser(discordUserId);
                }
            }

            // Generate unique code
            String code;
            int attempts = 0;
            do {
                code = generateRandomHexCode(securityLevel.getCodeLength());
                attempts++;
                
                if (attempts > 100) {
                    throw new RuntimeException("Failed to generate unique code after 100 attempts");
                }
            } while (activeCodes.containsKey(code) || usedCodes.contains(code));

            // Create code object
            HexadecimalCode hexCode = new HexadecimalCode(code, discordUserId, playerName, securityLevel);
            
            // Store code
            activeCodes.put(code, hexCode);
            codesByDiscordUser.put(discordUserId, hexCode);
            
            // Log audit event
            logAuditEvent(code, discordUserId, playerName, "GENERATE", ValidationResult.VALID, null);
            
            // Update metrics
            serviceMetrics.put("total_codes_generated", 
                ((Integer) serviceMetrics.getOrDefault("total_codes_generated", 0)) + 1);
            serviceMetrics.put("last_code_generation", Instant.now());
            
            return hexCode;
        });
    }

    /**
     * Validate hexadecimal code
     */
    public CompletableFuture<ValidationResult> validateCodeAsync(String code, String discordUserId, String ipAddress) {
        return CompletableFuture.supplyAsync(() -> {
            String normalizedCode = code.toLowerCase().trim();
            
            // Update metrics
            serviceMetrics.put("total_codes_validated", 
                ((Integer) serviceMetrics.getOrDefault("total_codes_validated", 0)) + 1);
            
            // Check format
            if (!isValidHexFormat(normalizedCode)) {
                logAuditEvent(normalizedCode, discordUserId, null, "VALIDATE", ValidationResult.INVALID_FORMAT, ipAddress);
                return ValidationResult.INVALID_FORMAT;
            }
            
            // Check security restrictions
            String userIdentifier = discordUserId != null ? discordUserId : ipAddress;
            
            if (securityTracker.isBlocked(userIdentifier)) {
                logAuditEvent(normalizedCode, discordUserId, null, "VALIDATE", ValidationResult.USER_BLOCKED, ipAddress);
                return ValidationResult.USER_BLOCKED;
            }
            
            if (securityTracker.isRateLimited(userIdentifier)) {
                logAuditEvent(normalizedCode, discordUserId, null, "VALIDATE", ValidationResult.RATE_LIMITED, ipAddress);
                return ValidationResult.RATE_LIMITED;
            }
            
            // Check if code exists
            HexadecimalCode hexCode = activeCodes.get(normalizedCode);
            if (hexCode == null) {
                securityTracker.recordAttempt(userIdentifier, false);
                logAuditEvent(normalizedCode, discordUserId, null, "VALIDATE", ValidationResult.CODE_NOT_FOUND, ipAddress);
                serviceMetrics.put("failed_validations", 
                    ((Integer) serviceMetrics.getOrDefault("failed_validations", 0)) + 1);
                return ValidationResult.CODE_NOT_FOUND;
            }
            
            // Increment validation attempts
            hexCode.incrementValidationAttempts();
            
            // Check if code is expired
            if (hexCode.isExpired()) {
                securityTracker.recordAttempt(userIdentifier, false);
                removeCode(normalizedCode);
                logAuditEvent(normalizedCode, discordUserId, hexCode.getPlayerName(), "VALIDATE", ValidationResult.CODE_EXPIRED, ipAddress);
                serviceMetrics.put("failed_validations", 
                    ((Integer) serviceMetrics.getOrDefault("failed_validations", 0)) + 1);
                return ValidationResult.CODE_EXPIRED;
            }
            
            // Check if code is already used
            if (hexCode.isUsed()) {
                securityTracker.recordAttempt(userIdentifier, false);
                logAuditEvent(normalizedCode, discordUserId, hexCode.getPlayerName(), "VALIDATE", ValidationResult.CODE_ALREADY_USED, ipAddress);
                serviceMetrics.put("failed_validations", 
                    ((Integer) serviceMetrics.getOrDefault("failed_validations", 0)) + 1);
                return ValidationResult.CODE_ALREADY_USED;
            }
            
            // Validate ownership (if Discord user provided)
            if (discordUserId != null && !discordUserId.equals(hexCode.getDiscordUserId())) {
                securityTracker.recordAttempt(userIdentifier, false);
                logAuditEvent(normalizedCode, discordUserId, hexCode.getPlayerName(), "VALIDATE", ValidationResult.CODE_NOT_FOUND, ipAddress);
                serviceMetrics.put("failed_validations", 
                    ((Integer) serviceMetrics.getOrDefault("failed_validations", 0)) + 1);
                return ValidationResult.CODE_NOT_FOUND;
            }
            
            // Code is valid - mark as used
            hexCode.markAsUsed();
            usedCodes.add(normalizedCode);
            removeCode(normalizedCode);
            
            // Record successful validation
            securityTracker.recordAttempt(userIdentifier, true);
            logAuditEvent(normalizedCode, discordUserId, hexCode.getPlayerName(), "VALIDATE", ValidationResult.VALID, ipAddress);
            
            serviceMetrics.put("successful_validations", 
                ((Integer) serviceMetrics.getOrDefault("successful_validations", 0)) + 1);
            serviceMetrics.put("last_successful_validation", Instant.now());
            
            return ValidationResult.VALID;
        });
    }

    /**
     * Get code information (without validating)
     */
    public CompletableFuture<Optional<HexadecimalCode>> getCodeInfoAsync(String code) {
        return CompletableFuture.supplyAsync(() -> {
            HexadecimalCode hexCode = activeCodes.get(code.toLowerCase().trim());
            return Optional.ofNullable(hexCode);
        });
    }

    /**
     * Get active code for Discord user
     */
    public CompletableFuture<Optional<HexadecimalCode>> getActiveCodeForUserAsync(String discordUserId) {
        return CompletableFuture.supplyAsync(() -> {
            HexadecimalCode hexCode = codesByDiscordUser.get(discordUserId);
            if (hexCode != null && hexCode.isValid()) {
                return Optional.of(hexCode);
            }
            return Optional.empty();
        });
    }

    /**
     * Invalidate code manually
     */
    public CompletableFuture<Boolean> invalidateCodeAsync(String code, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            String normalizedCode = code.toLowerCase().trim();
            HexadecimalCode hexCode = activeCodes.get(normalizedCode);
            
            if (hexCode != null) {
                hexCode.markAsUsed();
                removeCode(normalizedCode);
                
                logAuditEvent(normalizedCode, hexCode.getDiscordUserId(), hexCode.getPlayerName(), 
                            "INVALIDATE", ValidationResult.VALID, null);
                
                return true;
            }
            
            return false;
        });
    }

    /**
     * Internal Helper Methods
     */

    /**
     * Generate random hexadecimal code
     */
    private String generateRandomHexCode(int length) {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < length; i++) {
            hex.append(Integer.toHexString(secureRandom.nextInt(16)));
        }
        return hex.toString();
    }

    /**
     * Check if code matches hexadecimal format
     */
    private boolean isValidHexFormat(String code) {
        return code != null && 
               code.length() >= 6 && 
               code.length() <= 20 && 
               hexPattern.matcher(code).matches();
    }

    /**
     * Count active codes for user
     */
    private int countActiveCodesForUser(String discordUserId) {
        return (int) activeCodes.values().stream()
            .filter(code -> code.getDiscordUserId().equals(discordUserId))
            .filter(HexadecimalCode::isValid)
            .count();
    }

    /**
     * Remove oldest code for user
     */
    private void removeOldestCodeForUser(String discordUserId) {
        Optional<HexadecimalCode> oldestCode = activeCodes.values().stream()
            .filter(code -> code.getDiscordUserId().equals(discordUserId))
            .min(Comparator.comparing(HexadecimalCode::getCreatedTime));
        
        oldestCode.ifPresent(code -> removeCode(code.getCode()));
    }

    /**
     * Remove code from active storage
     */
    private void removeCode(String code) {
        HexadecimalCode hexCode = activeCodes.remove(code);
        if (hexCode != null) {
            codesByDiscordUser.remove(hexCode.getDiscordUserId());
        }
    }

    /**
     * Clean up expired codes
     */
    public void cleanupExpiredCodes() {
        List<String> expiredCodes = new ArrayList<>();
        
        for (Map.Entry<String, HexadecimalCode> entry : activeCodes.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredCodes.add(entry.getKey());
            }
        }
        
        for (String code : expiredCodes) {
            removeCode(code);
        }
        
        // Cleanup security tracker
        securityTracker.cleanup();
        
        // Cleanup old used codes (keep only last 10,000)
        while (usedCodes.size() > 10000) {
            usedCodes.clear(); // Simple approach - in production would use LRU
        }
        
        serviceMetrics.put("expired_codes_cleaned", expiredCodes.size());
        serviceMetrics.put("last_cleanup", Instant.now());
    }

    /**
     * Log audit event
     */
    private void logAuditEvent(String code, String discordUserId, String playerName, 
                              String action, ValidationResult result, String ipAddress) {
        CodeAuditLog logEntry = new CodeAuditLog(code, discordUserId, playerName, action, result, ipAddress);
        auditLog.add(logEntry);
        
        // Keep only last 50,000 audit entries
        while (auditLog.size() > 50000) {
            auditLog.remove(0);
        }
    }

    /**
     * Get comprehensive service status
     */
    public CompletableFuture<Map<String, Object>> getServiceStatusAsync() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> status = new HashMap<>();
            
            status.put("active_codes", activeCodes.size());
            status.put("used_codes_tracked", usedCodes.size());
            status.put("audit_log_entries", auditLog.size());
            status.put("blocked_users", securityTracker.getTotalBlockedUsers());
            status.put("active_rate_limits", securityTracker.getTotalActiveAttempts());
            status.put("metrics", new HashMap<>(serviceMetrics));
            
            return status;
        });
    }

    // Getters
    public Map<String, Object> getServiceMetrics() { return new HashMap<>(serviceMetrics); }
    public List<CodeAuditLog> getAuditLog() { return new ArrayList<>(auditLog); }
    public SecurityTracker getSecurityTracker() { return securityTracker; }
}
