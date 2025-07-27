package io.github.jk33v3rs.veloctopusrising.api;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages cross-platform whitelist system with Discord integration.
 * 
 * <p>The whitelist system controls access to Minecraft servers through
 * Discord verification, requiring players to link their accounts and
 * maintain active Discord membership for continued access.</p>
 * 
 * <h2>Whitelist Types:</h2>
 * <ul>
 *   <li><strong>Discord Required</strong>: Must be in Discord server to join</li>
 *   <li><strong>Application Based</strong>: Requires approval process</li>
 *   <li><strong>Invite Only</strong>: Added by existing members</li>
 *   <li><strong>Open Registration</strong>: Self-service with verification</li>
 * </ul>
 * 
 * <h2>Verification Levels:</h2>
 * <ul>
 *   <li><strong>Basic</strong>: Discord account linked and in server</li>
 *   <li><strong>Verified</strong>: Phone number verified on Discord</li>
 *   <li><strong>Established</strong>: Account older than 30 days</li>
 *   <li><strong>Trusted</strong>: Manually approved by staff</li>
 * </ul>
 * 
 * <h2>Auto-Moderation Features:</h2>
 * <ul>
 *   <li><strong>Alt Detection</strong>: Prevent multiple accounts per person</li>
 *   <li><strong>VPN Blocking</strong>: Block known VPN/proxy connections</li>
 *   <li><strong>Ban Evasion</strong>: Detect attempts to circumvent bans</li>
 *   <li><strong>Suspicious Activity</strong>: Flag unusual connection patterns</li>
 * </ul>
 * 
 * @since 1.0.0
 * @author jk33v3rs
 */
public interface WhitelistManager {
    
    /**
     * Represents a whitelist entry for a player.
     * 
     * @since 1.0.0
     */
    interface WhitelistEntry {
        
        /**
         * Gets the player's Minecraft UUID.
         * 
         * @return The player's unique identifier
         * @since 1.0.0
         */
        UUID getPlayerId();
        
        /**
         * Gets the player's Minecraft username.
         * 
         * @return The current username
         * @since 1.0.0
         */
        String getMinecraftUsername();
        
        /**
         * Gets the linked Discord user ID.
         * 
         * @return The Discord user ID, or empty if not linked
         * @since 1.0.0
         */
        Optional<String> getDiscordUserId();
        
        /**
         * Gets the Discord username.
         * 
         * @return The Discord username, or empty if not linked
         * @since 1.0.0
         */
        Optional<String> getDiscordUsername();
        
        /**
         * Gets when this player was whitelisted.
         * 
         * @return The whitelist timestamp
         * @since 1.0.0
         */
        Instant getWhitelistTime();
        
        /**
         * Gets who whitelisted this player.
         * 
         * @return The whitelisting staff member, or empty if automatic
         * @since 1.0.0
         */
        Optional<UUID> getWhitelistedBy();
        
        /**
         * Gets the verification level for this player.
         * 
         * @return The verification level
         * @since 1.0.0
         */
        VerificationLevel getVerificationLevel();
        
        /**
         * Gets when this whitelist entry expires.
         * 
         * @return Expiration time, or empty if permanent
         * @since 1.0.0
         */
        Optional<Instant> getExpirationTime();
        
        /**
         * Checks if this whitelist entry is currently active.
         * 
         * @return true if the player can join the server
         * @since 1.0.0
         */
        boolean isActive();
        
        /**
         * Gets the reason for whitelisting.
         * 
         * @return The whitelist reason
         * @since 1.0.0
         */
        Optional<String> getReason();
        
        /**
         * Gets additional metadata for this entry.
         * 
         * @return Metadata map with entry-specific properties
         * @since 1.0.0
         */
        java.util.Map<String, Object> getMetadata();
        
        /**
         * Gets the player's IP address from last connection.
         * 
         * @return The last known IP address
         * @since 1.0.0
         */
        Optional<String> getLastKnownIP();
        
        /**
         * Gets when the player last connected.
         * 
         * @return The last connection time, or empty if never connected
         * @since 1.0.0
         */
        Optional<Instant> getLastConnectionTime();
    }
    
    /**
     * Verification levels for whitelist entries.
     * 
     * @since 1.0.0
     */
    enum VerificationLevel {
        /** Basic Discord account linking */
        BASIC(1),
        
        /** Discord account with phone verification */
        VERIFIED(2),
        
        /** Account older than 30 days with good standing */
        ESTABLISHED(3),
        
        /** Manually approved by staff */
        TRUSTED(4);
        
        private final int level;
        
        VerificationLevel(int level) {
            this.level = level;
        }
        
        /** @return Numeric level for comparison */
        public int getLevel() {
            return level;
        }
        
        /** @return Whether this level is at least the specified level */
        public boolean isAtLeast(VerificationLevel required) {
            return this.level >= required.level;
        }
    }
    
    /**
     * Represents a whitelist application from a player.
     * 
     * @since 1.0.0
     */
    interface WhitelistApplication {
        
        /** @return Unique application identifier */
        UUID getApplicationId();
        
        /** @return The applying player's UUID */
        UUID getPlayerId();
        
        /** @return The player's username */
        String getMinecraftUsername();
        
        /** @return The Discord user ID */
        String getDiscordUserId();
        
        /** @return When the application was submitted */
        Instant getSubmissionTime();
        
        /** @return Application status */
        ApplicationStatus getStatus();
        
        /** @return Player's answers to application questions */
        java.util.Map<String, String> getAnswers();
        
        /** @return Who reviewed the application */
        Optional<UUID> getReviewedBy();
        
        /** @return When the application was reviewed */
        Optional<Instant> getReviewTime();
        
        /** @return Review decision reason */
        Optional<String> getReviewReason();
        
        /** @return Staff notes on the application */
        Optional<String> getStaffNotes();
    }
    
    /**
     * Application status values.
     * 
     * @since 1.0.0
     */
    enum ApplicationStatus {
        /** Application submitted, awaiting review */
        PENDING,
        
        /** Application approved, player whitelisted */
        APPROVED,
        
        /** Application denied */
        DENIED,
        
        /** Application withdrawn by player */
        WITHDRAWN,
        
        /** Application expired without review */
        EXPIRED
    }
    
    /**
     * Checks if a player is whitelisted and can join.
     * 
     * @param playerId The player's unique identifier
     * @return true if the player is whitelisted
     * @since 1.0.0
     */
    CompletableFuture<Boolean> isWhitelisted(UUID playerId);
    
    /**
     * Checks if a player is whitelisted by username.
     * 
     * @param username The player's Minecraft username
     * @return true if the player is whitelisted
     * @since 1.0.0
     */
    CompletableFuture<Boolean> isWhitelisted(String username);
    
    /**
     * Gets a whitelist entry for a player.
     * 
     * @param playerId The player's unique identifier
     * @return The whitelist entry, or empty if not whitelisted
     * @since 1.0.0
     */
    CompletableFuture<Optional<WhitelistEntry>> getWhitelistEntry(UUID playerId);
    
    /**
     * Gets a whitelist entry by Discord user ID.
     * 
     * @param discordUserId The Discord user ID
     * @return The whitelist entry, or empty if not found
     * @since 1.0.0
     */
    CompletableFuture<Optional<WhitelistEntry>> getWhitelistEntryByDiscord(
        String discordUserId
    );
    
    /**
     * Adds a player to the whitelist.
     * 
     * @param playerId The player's unique identifier
     * @param username The player's Minecraft username
     * @param discordUserId The player's Discord user ID
     * @param whitelistedBy Who is whitelisting the player
     * @param reason The reason for whitelisting
     * @return CompletableFuture that completes when player is whitelisted
     * @since 1.0.0
     */
    CompletableFuture<Void> addToWhitelist(
        UUID playerId,
        String username,
        String discordUserId,
        UUID whitelistedBy,
        String reason
    );
    
    /**
     * Adds a player with temporary whitelist access.
     * 
     * @param playerId The player's unique identifier
     * @param username The player's Minecraft username
     * @param discordUserId The player's Discord user ID
     * @param duration How long the whitelist should last
     * @param whitelistedBy Who is granting temporary access
     * @param reason The reason for temporary access
     * @return CompletableFuture that completes when player is whitelisted
     * @since 1.0.0
     */
    CompletableFuture<Void> addTemporaryWhitelist(
        UUID playerId,
        String username,
        String discordUserId,
        java.time.Duration duration,
        UUID whitelistedBy,
        String reason
    );
    
    /**
     * Removes a player from the whitelist.
     * 
     * @param playerId The player's unique identifier
     * @param removedBy Who is removing the player
     * @param reason The reason for removal
     * @return CompletableFuture that completes when player is removed
     * @since 1.0.0
     */
    CompletableFuture<Void> removeFromWhitelist(
        UUID playerId,
        UUID removedBy,
        String reason
    );
    
    /**
     * Updates a player's verification level.
     * 
     * @param playerId The player's unique identifier
     * @param level The new verification level
     * @param updatedBy Who is updating the level
     * @param reason The reason for the update
     * @return CompletableFuture that completes when level is updated
     * @since 1.0.0
     */
    CompletableFuture<Void> updateVerificationLevel(
        UUID playerId,
        VerificationLevel level,
        UUID updatedBy,
        String reason
    );
    
    /**
     * Links a Discord account to a Minecraft player.
     * 
     * @param playerId The player's unique identifier
     * @param discordUserId The Discord user ID to link
     * @return CompletableFuture that completes when accounts are linked
     * @since 1.0.0
     */
    CompletableFuture<Void> linkDiscordAccount(UUID playerId, String discordUserId);
    
    /**
     * Unlinks a Discord account from a Minecraft player.
     * 
     * @param playerId The player's unique identifier
     * @param unlinkedBy Who is unlinking the account
     * @param reason The reason for unlinking
     * @return CompletableFuture that completes when accounts are unlinked
     * @since 1.0.0
     */
    CompletableFuture<Void> unlinkDiscordAccount(
        UUID playerId,
        UUID unlinkedBy,
        String reason
    );
    
    /**
     * Gets all current whitelist entries.
     * 
     * @return List of all whitelist entries
     * @since 1.0.0
     */
    CompletableFuture<List<WhitelistEntry>> getAllWhitelistEntries();
    
    /**
     * Gets whitelist entries by verification level.
     * 
     * @param level The verification level to filter by
     * @return List of entries with the specified level
     * @since 1.0.0
     */
    CompletableFuture<List<WhitelistEntry>> getWhitelistEntriesByLevel(
        VerificationLevel level
    );
    
    /**
     * Gets whitelist entries that expire soon.
     * 
     * @param within Duration to check for expiring entries
     * @return List of entries expiring within the timeframe
     * @since 1.0.0
     */
    CompletableFuture<List<WhitelistEntry>> getExpiringEntries(
        java.time.Duration within
    );
    
    /**
     * Submits a whitelist application.
     * 
     * @param playerId The applying player's UUID
     * @param username The player's username
     * @param discordUserId The player's Discord user ID
     * @param answers Answers to application questions
     * @return The created application
     * @since 1.0.0
     */
    CompletableFuture<WhitelistApplication> submitApplication(
        UUID playerId,
        String username,
        String discordUserId,
        java.util.Map<String, String> answers
    );
    
    /**
     * Gets pending whitelist applications.
     * 
     * @return List of applications awaiting review
     * @since 1.0.0
     */
    CompletableFuture<List<WhitelistApplication>> getPendingApplications();
    
    /**
     * Reviews a whitelist application.
     * 
     * @param applicationId The application to review
     * @param approved Whether to approve the application
     * @param reviewedBy Who is reviewing the application
     * @param reason The reason for the decision
     * @param staffNotes Additional staff notes
     * @return CompletableFuture that completes when review is processed
     * @since 1.0.0
     */
    CompletableFuture<Void> reviewApplication(
        UUID applicationId,
        boolean approved,
        UUID reviewedBy,
        String reason,
        String staffNotes
    );
    
    /**
     * Validates Discord membership for all whitelisted players.
     * 
     * <p>Removes players who are no longer in the Discord server
     * (if Discord membership is required).</p>
     * 
     * @return Number of players removed from whitelist
     * @since 1.0.0
     */
    CompletableFuture<Integer> validateDiscordMembership();
    
    /**
     * Gets whitelist system statistics.
     * 
     * @return Current whitelist statistics
     * @since 1.0.0
     */
    WhitelistStatistics getStatistics();
    
    /**
     * Whitelist system statistics.
     * 
     * @since 1.0.0
     */
    interface WhitelistStatistics {
        
        /** @return Total number of whitelisted players */
        int getTotalWhitelisted();
        
        /** @return Number of players whitelisted today */
        int getWhitelistedToday();
        
        /** @return Number of pending applications */
        int getPendingApplications();
        
        /** @return Number of linked Discord accounts */
        int getLinkedDiscordAccounts();
        
        /** @return Percentage of players with Discord linked */
        double getDiscordLinkageRate();
        
        /** @return Distribution of verification levels */
        java.util.Map<VerificationLevel, Integer> getVerificationDistribution();
        
        /** @return Number of temporary whitelist entries */
        int getTemporaryEntries();
        
        /** @return Average application review time */
        Optional<java.time.Duration> getAverageReviewTime();
    }
}
