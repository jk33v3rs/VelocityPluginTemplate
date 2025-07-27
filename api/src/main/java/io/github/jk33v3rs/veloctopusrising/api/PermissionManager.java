package io.github.jk33v3rs.veloctopusrising.api;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages permissions across all platforms with rank integration.
 * 
 * <p>The permission system provides unified access control across
 * Minecraft, Discord, and web platforms. Permissions are automatically
 * synchronized with the 175-rank system and support both individual
 * grants and rank-based inheritance.</p>
 * 
 * <h2>Permission Types:</h2>
 * <ul>
 *   <li><strong>Minecraft</strong>: Plugin commands, world access, build permissions</li>
 *   <li><strong>Discord</strong>: Channel access, bot commands, role management</li>
 *   <li><strong>Web</strong>: Forum sections, admin panels, API endpoints</li>
 *   <li><strong>System</strong>: Internal operations, cross-platform features</li>
 * </ul>
 * 
 * <h2>Permission Hierarchy:</h2>
 * <ul>
 *   <li><strong>Rank Permissions</strong>: Inherited from current rank</li>
 *   <li><strong>Individual Grants</strong>: Explicitly granted to specific players</li>
 *   <li><strong>Temporary Permissions</strong>: Time-limited access grants</li>
 *   <li><strong>Negative Permissions</strong>: Explicit permission denials</li>
 * </ul>
 * 
 * <h2>Performance Requirements:</h2>
 * <ul>
 *   <li><strong>Check Speed</strong>: &lt;2ms for any permission check</li>
 *   <li><strong>Cache Hit Rate</strong>: &gt;98% for active players</li>
 *   <li><strong>Sync Latency</strong>: &lt;50ms cross-platform updates</li>
 * </ul>
 * 
 * @since 1.0.0
 * @author jk33v3rs
 */
public interface PermissionManager {
    
    /**
     * Represents a permission in the system.
     * 
     * <p>Permissions use a hierarchical dot notation and support
     * wildcards for group permissions and inheritance.</p>
     * 
     * @since 1.0.0
     */
    interface Permission {
        
        /**
         * Gets the permission node identifier.
         * 
         * @return The permission node (e.g., "veloctopus.chat.color")
         * @since 1.0.0
         */
        String getNode();
        
        /**
         * Gets the human-readable description.
         * 
         * @return The permission description
         * @since 1.0.0
         */
        String getDescription();
        
        /**
         * Gets the platform this permission applies to.
         * 
         * @return The platform (minecraft, discord, web, system)
         * @since 1.0.0
         */
        String getPlatform();
        
        /**
         * Gets the category this permission belongs to.
         * 
         * @return The category (chat, admin, build, etc.)
         * @since 1.0.0
         */
        String getCategory();
        
        /**
         * Checks if this is a wildcard permission.
         * 
         * @return true if this permission ends with '*'
         * @since 1.0.0
         */
        boolean isWildcard();
        
        /**
         * Gets the minimum rank required for this permission.
         * 
         * @return Required rank, or empty if no requirement
         * @since 1.0.0
         */
        Optional<RankManager.Rank> getMinimumRank();
        
        /**
         * Checks if this permission is currently enabled.
         * 
         * @return true if the permission can be granted/checked
         * @since 1.0.0
         */
        boolean isEnabled();
        
        /**
         * Gets child permissions included by this permission.
         * 
         * <p>For wildcard permissions, returns all matching nodes.</p>
         * 
         * @return List of included permission nodes
         * @since 1.0.0
         */
        List<String> getIncludedNodes();
    }
    
    /**
     * Represents a permission grant for a specific player.
     * 
     * @since 1.0.0
     */
    interface PermissionGrant {
        
        /** @return The player this grant applies to */
        UUID getPlayerId();
        
        /** @return The permission node */
        String getPermissionNode();
        
        /** @return Whether this is a positive or negative grant */
        boolean isPositive();
        
        /** @return When this grant was created */
        java.time.Instant getGrantTime();
        
        /** @return When this grant expires, if applicable */
        Optional<java.time.Instant> getExpirationTime();
        
        /** @return Who granted this permission */
        Optional<UUID> getGrantedBy();
        
        /** @return Reason for the grant */
        Optional<String> getReason();
        
        /** @return Whether this grant is still active */
        boolean isActive();
        
        /** @return The source of this grant (rank, manual, temporary) */
        String getSource();
    }
    
    /**
     * Checks if a player has a specific permission.
     * 
     * <p>This method checks all sources: rank permissions, individual grants,
     * and temporary permissions. Results are cached for performance.</p>
     * 
     * @param playerId The player's unique identifier
     * @param permission The permission node to check
     * @return true if the player has the permission
     * @since 1.0.0
     */
    CompletableFuture<Boolean> hasPermission(UUID playerId, String permission);
    
    /**
     * Checks multiple permissions for a player efficiently.
     * 
     * @param playerId The player's unique identifier
     * @param permissions The permission nodes to check
     * @return Map of permissions to boolean results
     * @since 1.0.0
     */
    CompletableFuture<java.util.Map<String, Boolean>> hasPermissions(
        UUID playerId,
        Set<String> permissions
    );
    
    /**
     * Gets all permissions for a player from all sources.
     * 
     * @param playerId The player's unique identifier
     * @return Set of all permission nodes the player has
     * @since 1.0.0
     */
    CompletableFuture<Set<String>> getPlayerPermissions(UUID playerId);
    
    /**
     * Gets permissions by source type for a player.
     * 
     * @param playerId The player's unique identifier
     * @param source The source type (rank, manual, temporary)
     * @return Set of permissions from that source
     * @since 1.0.0
     */
    CompletableFuture<Set<String>> getPlayerPermissionsBySource(
        UUID playerId,
        String source
    );
    
    /**
     * Grants a permission to a player.
     * 
     * <p>This creates an individual permission grant that persists
     * until explicitly removed or the player's data is reset.</p>
     * 
     * @param playerId The player to grant permission to
     * @param permission The permission node to grant
     * @param grantedBy Who is granting the permission
     * @param reason The reason for the grant
     * @return CompletableFuture that completes when grant is applied
     * @since 1.0.0
     */
    CompletableFuture<Void> grantPermission(
        UUID playerId,
        String permission,
        UUID grantedBy,
        String reason
    );
    
    /**
     * Grants a temporary permission to a player.
     * 
     * <p>The permission is automatically removed when the duration expires.</p>
     * 
     * @param playerId The player to grant permission to
     * @param permission The permission node to grant
     * @param duration How long the permission should last
     * @param grantedBy Who is granting the permission
     * @param reason The reason for the grant
     * @return CompletableFuture that completes when grant is applied
     * @since 1.0.0
     */
    CompletableFuture<Void> grantTemporaryPermission(
        UUID playerId,
        String permission,
        java.time.Duration duration,
        UUID grantedBy,
        String reason
    );
    
    /**
     * Revokes a permission from a player.
     * 
     * <p>This removes an individual permission grant. Rank-based
     * permissions cannot be revoked this way.</p>
     * 
     * @param playerId The player to revoke permission from
     * @param permission The permission node to revoke
     * @param revokedBy Who is revoking the permission
     * @param reason The reason for the revocation
     * @return CompletableFuture that completes when revocation is applied
     * @since 1.0.0
     */
    CompletableFuture<Void> revokePermission(
        UUID playerId,
        String permission,
        UUID revokedBy,
        String reason
    );
    
    /**
     * Adds a negative permission to explicitly deny access.
     * 
     * <p>Negative permissions override positive permissions and
     * can be used to restrict access even with rank permissions.</p>
     * 
     * @param playerId The player to deny permission to
     * @param permission The permission node to deny
     * @param grantedBy Who is adding the denial
     * @param reason The reason for the denial
     * @return CompletableFuture that completes when denial is applied
     * @since 1.0.0
     */
    CompletableFuture<Void> denyPermission(
        UUID playerId,
        String permission,
        UUID grantedBy,
        String reason
    );
    
    /**
     * Gets a permission definition by its node.
     * 
     * @param node The permission node
     * @return The permission definition, or empty if not found
     * @since 1.0.0
     */
    Optional<Permission> getPermission(String node);
    
    /**
     * Gets all permissions in a category.
     * 
     * @param category The category name
     * @return List of permissions in the category
     * @since 1.0.0
     */
    List<Permission> getPermissionsByCategory(String category);
    
    /**
     * Gets all permissions for a platform.
     * 
     * @param platform The platform name
     * @return List of permissions for the platform
     * @since 1.0.0
     */
    List<Permission> getPermissionsByPlatform(String platform);
    
    /**
     * Gets all permission grants for a player.
     * 
     * @param playerId The player's unique identifier
     * @return List of all active and expired grants
     * @since 1.0.0
     */
    CompletableFuture<List<PermissionGrant>> getPlayerGrants(UUID playerId);
    
    /**
     * Gets permission grants that expire soon.
     * 
     * @param within Duration to check for expiring grants
     * @return List of grants expiring within the timeframe
     * @since 1.0.0
     */
    CompletableFuture<List<PermissionGrant>> getExpiringGrants(
        java.time.Duration within
    );
    
    /**
     * Synchronizes permissions across all platforms for a player.
     * 
     * <p>Updates Discord roles, Minecraft permissions, and web access
     * to match the player's current permission set.</p>
     * 
     * @param playerId The player's unique identifier
     * @return CompletableFuture that completes when sync is finished
     * @since 1.0.0
     */
    CompletableFuture<Void> syncPlayerPermissions(UUID playerId);
    
    /**
     * Forces a permission cache refresh for a player.
     * 
     * @param playerId The player's unique identifier
     * @return CompletableFuture that completes when cache is refreshed
     * @since 1.0.0
     */
    CompletableFuture<Void> refreshPlayerCache(UUID playerId);
    
    /**
     * Gets permission system statistics.
     * 
     * @return Current permission system statistics
     * @since 1.0.0
     */
    PermissionStatistics getStatistics();
    
    /**
     * Permission system performance statistics.
     * 
     * @since 1.0.0
     */
    interface PermissionStatistics {
        
        /** @return Total permission checks performed */
        long getTotalChecks();
        
        /** @return Permission checks in the last minute */
        long getRecentChecks();
        
        /** @return Average permission check time (ms) */
        double getAverageCheckTime();
        
        /** @return Cache hit rate (0.0-1.0) */
        double getCacheHitRate();
        
        /** @return Number of registered permissions */
        int getRegisteredPermissions();
        
        /** @return Number of active permission grants */
        long getActiveGrants();
        
        /** @return Number of temporary grants */
        long getTemporaryGrants();
        
        /** @return Most checked permission in the last hour */
        Optional<String> getMostCheckedPermission();
    }
}
