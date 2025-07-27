package io.github.jk33v3rs.veloctopusrising.api;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the 4000-endpoint XP system across all platforms.
 * 
 * <p>The XP system tracks player progression through diverse activities
 * across Minecraft, Discord, and web platforms. With 4000+ unique XP
 * sources, players can earn experience through gameplay, social interaction,
 * and community participation.</p>
 * 
 * <h2>XP Categories:</h2>
 * <ul>
 *   <li><strong>Minecraft</strong>: Gameplay, building, exploration (2500+ sources)</li>
 *   <li><strong>Discord</strong>: Messages, reactions, voice participation (800+ sources)</li>
 *   <li><strong>Web</strong>: Forum posts, votes, profile completion (400+ sources)</li>
 *   <li><strong>Events</strong>: Seasonal activities, competitions (300+ sources)</li>
 * </ul>
 * 
 * <h2>XP Multipliers:</h2>
 * <ul>
 *   <li><strong>Rank Bonus</strong>: Higher ranks earn 1.1x-3.0x multipliers</li>
 *   <li><strong>Streak Bonus</strong>: Daily login streaks up to 2.5x</li>
 *   <li><strong>Event Bonus</strong>: Special events provide temporary boosts</li>
 *   <li><strong>Premium Bonus</strong>: Donor ranks get additional multipliers</li>
 * </ul>
 * 
 * <h2>Performance Requirements:</h2>
 * <ul>
 *   <li><strong>Grant Speed</strong>: &lt;10ms for any XP award</li>
 *   <li><strong>Query Speed</strong>: &lt;5ms for player XP lookups</li>
 *   <li><strong>Batch Processing</strong>: 1000+ XP grants per second</li>
 * </ul>
 * 
 * @since 1.0.0
 * @author jk33v3rs
 */
public interface XPManager {
    
    /**
     * Represents a source of XP in the system.
     * 
     * <p>XP sources define where experience can be earned, including
     * base amounts, multipliers, and cooldown restrictions.</p>
     * 
     * @since 1.0.0
     */
    interface XPSource {
        
        /**
         * Gets the unique identifier for this XP source.
         * 
         * @return The source identifier (e.g., "minecraft.block.place.stone")
         * @since 1.0.0
         */
        String getId();
        
        /**
         * Gets the human-readable name for this source.
         * 
         * @return The display name (e.g., "Placing Stone Blocks")
         * @since 1.0.0
         */
        String getDisplayName();
        
        /**
         * Gets the category this source belongs to.
         * 
         * @return The category (minecraft, discord, web, events)
         * @since 1.0.0
         */
        String getCategory();
        
        /**
         * Gets the base XP amount for this source.
         * 
         * @return The base XP value before multipliers
         * @since 1.0.0
         */
        int getBaseXp();
        
        /**
         * Gets the maximum XP that can be earned per day from this source.
         * 
         * @return Daily XP cap, or empty if unlimited
         * @since 1.0.0
         */
        Optional<Integer> getDailyCap();
        
        /**
         * Gets the cooldown between XP grants from this source.
         * 
         * @return Cooldown duration, or empty if no cooldown
         * @since 1.0.0
         */
        Optional<Duration> getCooldown();
        
        /**
         * Checks if this source is currently enabled.
         * 
         * @return true if XP can be earned from this source
         * @since 1.0.0
         */
        boolean isEnabled();
        
        /**
         * Gets the minimum rank required to earn XP from this source.
         * 
         * @return Required rank, or empty if no requirement
         * @since 1.0.0
         */
        Optional<RankManager.Rank> getMinimumRank();
        
        /**
         * Gets additional metadata for this source.
         * 
         * @return Metadata map with source-specific properties
         * @since 1.0.0
         */
        Map<String, Object> getMetadata();
    }
    
    /**
     * Represents an XP transaction in the system.
     * 
     * <p>Transactions provide a complete audit trail of all XP changes
     * with timestamps, sources, and applied multipliers.</p>
     * 
     * @since 1.0.0
     */
    interface XPTransaction {
        
        /** @return Unique transaction identifier */
        UUID getTransactionId();
        
        /** @return The player who earned/lost XP */
        UUID getPlayerId();
        
        /** @return The XP source that triggered this transaction */
        XPSource getSource();
        
        /** @return Base XP amount before multipliers */
        int getBaseAmount();
        
        /** @return Applied multiplier (e.g., 1.5 for 50% bonus) */
        double getMultiplier();
        
        /** @return Final XP amount after multipliers */
        int getFinalAmount();
        
        /** @return When this transaction occurred */
        Instant getTimestamp();
        
        /** @return Additional context about this transaction */
        String getContext();
        
        /** @return The platform where this XP was earned */
        String getPlatform();
        
        /** @return Player's total XP after this transaction */
        long getPlayerTotalAfter();
    }
    
    /**
     * Gets a player's total XP across all sources.
     * 
     * @param playerId The player's unique identifier
     * @return The player's total XP amount
     * @since 1.0.0
     */
    CompletableFuture<Long> getPlayerXP(UUID playerId);
    
    /**
     * Gets a player's XP breakdown by category.
     * 
     * @param playerId The player's unique identifier
     * @return Map of categories to XP amounts
     * @since 1.0.0
     */
    CompletableFuture<Map<String, Long>> getPlayerXPByCategory(UUID playerId);
    
    /**
     * Awards XP to a player from a specific source.
     * 
     * <p>This method applies all relevant multipliers, checks cooldowns,
     * enforces daily caps, and creates a transaction record.</p>
     * 
     * @param playerId The player to award XP to
     * @param sourceId The XP source identifier
     * @param multiplier Additional multiplier to apply
     * @param context Additional context for the transaction
     * @return The transaction created, or empty if XP was not awarded
     * @since 1.0.0
     */
    CompletableFuture<Optional<XPTransaction>> awardXP(
        UUID playerId,
        String sourceId,
        double multiplier,
        String context
    );
    
    /**
     * Awards XP with default multiplier (1.0).
     * 
     * @param playerId The player to award XP to
     * @param sourceId The XP source identifier
     * @param context Additional context for the transaction
     * @return The transaction created, or empty if XP was not awarded
     * @since 1.0.0
     */
    default CompletableFuture<Optional<XPTransaction>> awardXP(
        UUID playerId,
        String sourceId,
        String context
    ) {
        return awardXP(playerId, sourceId, 1.0, context);
    }
    
    /**
     * Manually adjusts a player's XP (admin function).
     * 
     * <p>This bypasses normal XP sources and applies a direct adjustment.
     * Used for administrative corrections or special rewards.</p>
     * 
     * @param playerId The player to adjust XP for
     * @param amount The XP amount to add (can be negative)
     * @param reason The reason for the adjustment
     * @return The transaction created
     * @since 1.0.0
     */
    CompletableFuture<XPTransaction> adjustPlayerXP(
        UUID playerId,
        long amount,
        String reason
    );
    
    /**
     * Gets an XP source by its identifier.
     * 
     * @param sourceId The source identifier
     * @return The XP source, or empty if not found
     * @since 1.0.0
     */
    Optional<XPSource> getXPSource(String sourceId);
    
    /**
     * Gets all XP sources in a category.
     * 
     * @param category The category name
     * @return List of XP sources in the category
     * @since 1.0.0
     */
    List<XPSource> getXPSourcesByCategory(String category);
    
    /**
     * Gets all available XP source categories.
     * 
     * @return List of category names
     * @since 1.0.0
     */
    List<String> getXPCategories();
    
    /**
     * Gets a player's XP transaction history.
     * 
     * @param playerId The player's unique identifier
     * @param limit Maximum number of transactions to return
     * @return List of transactions, most recent first
     * @since 1.0.0
     */
    CompletableFuture<List<XPTransaction>> getPlayerTransactions(
        UUID playerId,
        int limit
    );
    
    /**
     * Gets transactions from a specific source.
     * 
     * @param sourceId The source identifier
     * @param limit Maximum number of transactions to return
     * @return List of transactions, most recent first
     * @since 1.0.0
     */
    CompletableFuture<List<XPTransaction>> getTransactionsBySource(
        String sourceId,
        int limit
    );
    
    /**
     * Gets the current XP multiplier for a player.
     * 
     * <p>Combines rank bonus, streak bonus, event multipliers,
     * and any other active bonuses into a single multiplier.</p>
     * 
     * @param playerId The player's unique identifier
     * @return The current total multiplier
     * @since 1.0.0
     */
    CompletableFuture<Double> getPlayerMultiplier(UUID playerId);
    
    /**
     * Gets XP leaderboard rankings.
     * 
     * @param timeframe The timeframe for rankings (all-time, monthly, weekly)
     * @param limit Maximum number of entries to return
     * @return Ordered list of players with XP amounts
     * @since 1.0.0
     */
    CompletableFuture<List<LeaderboardEntry>> getLeaderboard(
        String timeframe,
        int limit
    );
    
    /**
     * Gets a player's ranking in the leaderboard.
     * 
     * @param playerId The player's unique identifier
     * @param timeframe The timeframe for rankings
     * @return The player's ranking (1-based), or empty if not ranked
     * @since 1.0.0
     */
    CompletableFuture<Optional<Integer>> getPlayerRanking(
        UUID playerId,
        String timeframe
    );
    
    /**
     * Checks if a player can earn XP from a specific source.
     * 
     * <p>Validates cooldowns, daily caps, rank requirements,
     * and source availability.</p>
     * 
     * @param playerId The player's unique identifier
     * @param sourceId The source identifier
     * @return Validation result with details
     * @since 1.0.0
     */
    CompletableFuture<XPValidation> validateXPGrant(UUID playerId, String sourceId);
    
    /**
     * Gets XP statistics for the system.
     * 
     * @return Current XP system statistics
     * @since 1.0.0
     */
    XPStatistics getStatistics();
    
    /**
     * Represents a leaderboard entry.
     * 
     * @since 1.0.0
     */
    interface LeaderboardEntry {
        
        /** @return Player's unique identifier */
        UUID getPlayerId();
        
        /** @return Player's display name */
        String getPlayerName();
        
        /** @return Player's XP amount for this timeframe */
        long getXpAmount();
        
        /** @return Player's ranking position (1-based) */
        int getRanking();
        
        /** @return Player's current rank */
        RankManager.Rank getPlayerRank();
    }
    
    /**
     * Validation result for XP grants.
     * 
     * @since 1.0.0
     */
    interface XPValidation {
        
        /** @return Whether XP can be granted */
        boolean isValid();
        
        /** @return Reason if XP cannot be granted */
        Optional<String> getFailureReason();
        
        /** @return Time until this source becomes available again */
        Optional<Duration> getCooldownRemaining();
        
        /** @return Remaining daily XP cap for this source */
        Optional<Integer> getDailyCapRemaining();
        
        /** @return XP amount that would be granted */
        int getProjectedXpAmount();
        
        /** @return Multiplier that would be applied */
        double getProjectedMultiplier();
    }
    
    /**
     * XP system performance statistics.
     * 
     * @since 1.0.0
     */
    interface XPStatistics {
        
        /** @return Total XP transactions processed */
        long getTotalTransactions();
        
        /** @return XP transactions in the last hour */
        long getRecentTransactions();
        
        /** @return Average XP grant processing time (ms) */
        double getAverageProcessingTime();
        
        /** @return Number of active XP sources */
        int getActiveSourceCount();
        
        /** @return Total XP awarded today */
        long getTotalXpToday();
        
        /** @return Most active XP source in the last hour */
        Optional<String> getMostActiveSource();
        
        /** @return Current XP grant rate (grants per second) */
        double getCurrentGrantRate();
    }
}
