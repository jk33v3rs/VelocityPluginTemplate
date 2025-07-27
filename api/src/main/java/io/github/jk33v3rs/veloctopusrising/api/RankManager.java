package io.github.jk33v3rs.veloctopusrising.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the 175-rank system across all platforms.
 * 
 * <p>The rank system provides a unified progression pathway with 25 tiers
 * and 7 sub-ranks per tier, creating 175 total rank combinations. Ranks
 * are synchronized across Minecraft, Discord, and the web interface.</p>
 * 
 * <h2>Rank Structure:</h2>
 * <ul>
 *   <li><strong>Tiers</strong>: 25 major progression levels (I-XXV)</li>
 *   <li><strong>Sub-ranks</strong>: 7 minor levels per tier (A-G)</li>
 *   <li><strong>Total Ranks</strong>: 175 unique combinations (I-A through XXV-G)</li>
 * </ul>
 * 
 * <h2>Platform Integration:</h2>
 * <ul>
 *   <li><strong>Minecraft</strong>: Permission groups, chat prefixes, visual indicators</li>
 *   <li><strong>Discord</strong>: Role assignments, channel permissions, colored names</li>
 *   <li><strong>Web</strong>: Profile badges, leaderboards, progress tracking</li>
 * </ul>
 * 
 * <h2>Performance Requirements:</h2>
 * <ul>
 *   <li><strong>Lookup Speed</strong>: &lt;5ms for any rank operation</li>
 *   <li><strong>Sync Latency</strong>: &lt;100ms cross-platform updates</li>
 *   <li><strong>Cache Hit Rate</strong>: &gt;95% for active players</li>
 * </ul>
 * 
 * @since 1.0.0
 * @author jk33v3rs
 */
public interface RankManager {
    
    /**
     * Represents a rank in the 175-rank system.
     * 
     * <p>Ranks are immutable value objects that combine a tier (1-25)
     * with a sub-rank (A-G) to create the full rank identity.</p>
     * 
     * @since 1.0.0
     */
    interface Rank {
        
        /**
         * Gets the tier number (1-25).
         * 
         * @return The tier number in Roman numerals (I-XXV)
         * @since 1.0.0
         */
        int getTier();
        
        /**
         * Gets the sub-rank letter (A-G).
         * 
         * @return The sub-rank letter
         * @since 1.0.0
         */
        char getSubRank();
        
        /**
         * Gets the display name for this rank.
         * 
         * <p>Format: "{tier}-{subrank}" (e.g., "XII-C")</p>
         * 
         * @return The formatted rank display name
         * @since 1.0.0
         */
        String getDisplayName();
        
        /**
         * Gets the full Roman numeral representation.
         * 
         * @return The tier in Roman numerals (I, II, III, etc.)
         * @since 1.0.0
         */
        String getRomanNumeral();
        
        /**
         * Gets the ordinal position (1-175) of this rank.
         * 
         * <p>Used for numerical comparisons and progress calculations.</p>
         * 
         * @return The rank's position in the progression
         * @since 1.0.0
         */
        int getOrdinal();
        
        /**
         * Gets the minimum XP required for this rank.
         * 
         * @return The XP threshold for this rank
         * @since 1.0.0
         */
        long getMinXp();
        
        /**
         * Gets the XP required for the next rank.
         * 
         * @return The XP needed to advance, or empty if max rank
         * @since 1.0.0
         */
        Optional<Long> getNextRankXp();
        
        /**
         * Checks if this rank is higher than another.
         * 
         * @param other The rank to compare against
         * @return true if this rank is higher
         * @since 1.0.0
         */
        boolean isHigherThan(Rank other);
        
        /**
         * Gets the associated permissions for this rank.
         * 
         * @return List of permission nodes
         * @since 1.0.0
         */
        List<String> getPermissions();
        
        /**
         * Gets the Discord role ID for this rank.
         * 
         * @return Discord role ID, or empty if no role
         * @since 1.0.0
         */
        Optional<String> getDiscordRoleId();
        
        /**
         * Gets the chat prefix for this rank.
         * 
         * @return The formatted chat prefix with colors
         * @since 1.0.0
         */
        String getChatPrefix();
        
        /**
         * Gets the hex color code for this rank.
         * 
         * @return The rank's primary color as hex string
         * @since 1.0.0
         */
        String getColorHex();
    }
    
    /**
     * Gets a player's current rank.
     * 
     * <p>Returns the rank based on the player's current XP total.
     * Results are cached for performance and updated automatically
     * when XP changes.</p>
     * 
     * @param playerId The player's unique identifier
     * @return The player's current rank, or empty if player not found
     * @since 1.0.0
     */
    CompletableFuture<Optional<Rank>> getPlayerRank(UUID playerId);
    
    /**
     * Gets a rank by its tier and sub-rank.
     * 
     * @param tier The tier number (1-25)
     * @param subRank The sub-rank letter (A-G)
     * @return The rank, or empty if invalid combination
     * @since 1.0.0
     */
    Optional<Rank> getRank(int tier, char subRank);
    
    /**
     * Gets a rank by its ordinal position.
     * 
     * @param ordinal The rank position (1-175)
     * @return The rank, or empty if invalid ordinal
     * @since 1.0.0
     */
    Optional<Rank> getRankByOrdinal(int ordinal);
    
    /**
     * Gets a rank by its display name.
     * 
     * @param displayName The rank display name (e.g., "XII-C")
     * @return The rank, or empty if invalid name
     * @since 1.0.0
     */
    Optional<Rank> getRankByName(String displayName);
    
    /**
     * Gets all ranks in order from lowest to highest.
     * 
     * @return Immutable list of all 175 ranks
     * @since 1.0.0
     */
    List<Rank> getAllRanks();
    
    /**
     * Gets ranks in a specific tier.
     * 
     * @param tier The tier number (1-25)
     * @return List of 7 ranks in the tier, or empty if invalid tier
     * @since 1.0.0
     */
    List<Rank> getRanksInTier(int tier);
    
    /**
     * Calculates the appropriate rank for the given XP amount.
     * 
     * @param xp The XP amount
     * @return The rank corresponding to that XP level
     * @since 1.0.0
     */
    Rank calculateRankForXp(long xp);
    
    /**
     * Gets players currently at the specified rank.
     * 
     * @param rank The rank to query
     * @return List of player UUIDs at this rank
     * @since 1.0.0
     */
    CompletableFuture<List<UUID>> getPlayersAtRank(Rank rank);
    
    /**
     * Gets the rank distribution across all players.
     * 
     * @return Map of ranks to player counts
     * @since 1.0.0
     */
    CompletableFuture<RankDistribution> getRankDistribution();
    
    /**
     * Manually sets a player's rank (admin override).
     * 
     * <p>This bypasses normal XP requirements and should only be used
     * for administrative purposes. The change is logged and synchronized
     * across all platforms.</p>
     * 
     * @param playerId The player's unique identifier
     * @param rank The rank to assign
     * @param reason The reason for the manual assignment
     * @return CompletableFuture that completes when rank is updated
     * @since 1.0.0
     */
    CompletableFuture<Void> setPlayerRank(UUID playerId, Rank rank, String reason);
    
    /**
     * Forces a rank recalculation for a player.
     * 
     * <p>Recalculates the player's rank based on current XP and updates
     * all platform integrations if the rank has changed.</p>
     * 
     * @param playerId The player's unique identifier
     * @return The player's potentially updated rank
     * @since 1.0.0
     */
    CompletableFuture<Optional<Rank>> recalculatePlayerRank(UUID playerId);
    
    /**
     * Synchronizes rank data across all platforms for a player.
     * 
     * <p>Updates Discord roles, Minecraft permissions, and web profile
     * to match the player's current rank.</p>
     * 
     * @param playerId The player's unique identifier
     * @return CompletableFuture that completes when sync is finished
     * @since 1.0.0
     */
    CompletableFuture<Void> syncPlayerRank(UUID playerId);
    
    /**
     * Gets rank progression statistics for a player.
     * 
     * @param playerId The player's unique identifier
     * @return Progression stats including next rank info
     * @since 1.0.0
     */
    CompletableFuture<Optional<RankProgression>> getPlayerProgression(UUID playerId);
    
    /**
     * Statistics about rank distribution across players.
     * 
     * @since 1.0.0
     */
    interface RankDistribution {
        
        /** @return Total number of ranked players */
        int getTotalPlayers();
        
        /** @return Number of players at each rank */
        java.util.Map<Rank, Integer> getDistribution();
        
        /** @return The most common rank */
        Rank getMostPopularRank();
        
        /** @return The least common rank */
        Rank getLeastPopularRank();
        
        /** @return Average rank tier across all players */
        double getAverageTier();
    }
    
    /**
     * Progression information for a specific player.
     * 
     * @since 1.0.0
     */
    interface RankProgression {
        
        /** @return The player's current rank */
        Rank getCurrentRank();
        
        /** @return The next rank, if not at maximum */
        Optional<Rank> getNextRank();
        
        /** @return Current XP amount */
        long getCurrentXp();
        
        /** @return XP needed for next rank */
        Optional<Long> getXpToNextRank();
        
        /** @return Progress percentage to next rank (0.0-1.0) */
        double getProgressToNextRank();
        
        /** @return Estimated time to next rank based on recent activity */
        Optional<java.time.Duration> getEstimatedTimeToNextRank();
        
        /** @return Number of ranks gained in the last 30 days */
        int getRecentRankGains();
    }
}
