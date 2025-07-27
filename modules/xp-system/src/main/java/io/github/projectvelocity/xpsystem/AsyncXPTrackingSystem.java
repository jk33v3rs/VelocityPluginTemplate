package io.github.projectvelocity.xpsystem;

import io.github.projectvelocity.api.AsyncPattern;
import io.github.projectvelocity.api.EventBus;
import io.github.projectvelocity.api.storage.DataManager;
import io.github.projectvelocity.api.storage.RedisManager;
import io.github.projectvelocity.api.configuration.ConfigurationManager;
import io.github.projectvelocity.api.events.player.PlayerXPGainEvent;
import io.github.projectvelocity.api.events.player.PlayerRankPromotionEvent;
import io.github.projectvelocity.api.events.community.CommunityContributionEvent;
import io.github.projectvelocity.api.monitoring.MetricsCollector;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AsyncXPTrackingSystem - VeloctopusProject 4000-Endpoint Achievement Architecture
 * 
 * Implements community-first progression with dual-track activity detection:
 * - Individual achievement tracking (40% optimal progression)
 * - Community contribution emphasis (60% optimal progression)
 * 
 * Features:
 * - 10 different XP sources with intelligent cooldown management
 * - Anti-gaming protection with rate limiting and quality-based multipliers
 * - Real-time XP calculation with Redis caching and MariaDB persistence
 * - High-performance architecture supporting 4000+ unique achievements
 * - Peer recognition system with community validation
 * - Event-based XP awards with seasonal multipliers
 * 
 * Performance Targets:
 * - XP calculation response time: <100ms
 * - Concurrent player support: >1000 players
 * - XP storage efficiency: Denormalized tables with indexed lookups
 * - Real-time leaderboard updates: <1 second latency
 * 
 * Integration Points:
 * - AsyncRankSystem: Automatic rank promotion triggers
 * - FloraBot: Celebration notifications and milestone announcements
 * - DiscordBridge: Real-time XP notifications and leaderboards
 * - CommunitySystem: Peer recognition and validation workflows
 * 
 * @author VeloctopusProject Team
 * @version 1.0.0
 * @since 2024-01-20
 */
public class AsyncXPTrackingSystem implements AsyncPattern {
    
    // Core Components
    private final DataManager dataManager;
    private final RedisManager redisManager;
    private final EventBus eventBus;
    private final ConfigurationManager configManager;
    private final MetricsCollector metricsCollector;
    
    // XP Source Management
    private final Map<UUID, PlayerXPProfile> playerProfiles;
    private final Map<XPSource, XPSourceConfig> xpSourceConfigs;
    private final ConcurrentHashMap<UUID, Map<XPSource, LocalDateTime>> cooldownTracker;
    
    // Rate Limiting and Anti-Gaming
    private final Map<UUID, DailyXPTracker> dailyXPTrackers;
    private final Map<UUID, WeeklyXPTracker> weeklyXPTrackers;
    private final Map<UUID, MonthlyXPTracker> monthlyXPTrackers;
    
    // Leaderboard Management
    private final AtomicReference<List<LeaderboardEntry>> cachedLeaderboard;
    private final ScheduledExecutorService leaderboardUpdateScheduler;
    
    // Performance Monitoring
    private final AtomicLong totalXPProcessed;
    private final AtomicLong averageCalculationTime;
    private final Map<XPSource, AtomicLong> xpSourceMetrics;
    
    /**
     * XP Source Types - 10 Different XP Sources with Community Emphasis
     */
    public enum XPSource {
        // Individual Achievement Sources (40% optimal progression)
        CHAT_ACTIVITY(1, 60, "Chat participation and communication", 0.8f),
        PLAYTIME(2, 60, "Active time spent on server", 1.0f),
        BLOCK_INTERACTION(1, 30, "Building and world interaction", 0.9f),
        MOB_INTERACTION(3, 45, "Combat and mob farming", 0.7f),
        ADVANCEMENT_UNLOCK(25, 300, "Achievement completion", 1.2f),
        
        // Community Contribution Sources (60% optimal progression)
        MENTORING_NEW_PLAYERS(50, 1800, "Teaching and helping new members", 1.8f),
        TEACHING_SESSIONS(75, 3600, "Organized educational activities", 2.0f),
        CONFLICT_RESOLUTION(100, 7200, "Mediating disputes and problem-solving", 2.2f),
        COMMUNITY_PROJECTS(40, 1200, "Collaborative building and events", 1.6f),
        PEER_RECOGNITION(30, 900, "Recognition from other community members", 1.4f);
        
        private final int baseXP;
        private final int cooldownSeconds;
        private final String description;
        private final float qualityMultiplier;
        
        XPSource(int baseXP, int cooldownSeconds, String description, float qualityMultiplier) {
            this.baseXP = baseXP;
            this.cooldownSeconds = cooldownSeconds;
            this.description = description;
            this.qualityMultiplier = qualityMultiplier;
        }
        
        public int getBaseXP() { return baseXP; }
        public int getCooldownSeconds() { return cooldownSeconds; }
        public String getDescription() { return description; }
        public float getQualityMultiplier() { return qualityMultiplier; }
        public boolean isCommunitySource() {
            return this.ordinal() >= 5; // Community sources start at index 5
        }
    }
    
    /**
     * XP Source Configuration
     */
    public static class XPSourceConfig {
        private final boolean enabled;
        private final int maxDailyGains;
        private final float seasonalMultiplier;
        private final Set<String> requiredPermissions;
        
        public XPSourceConfig(boolean enabled, int maxDailyGains, float seasonalMultiplier, Set<String> requiredPermissions) {
            this.enabled = enabled;
            this.maxDailyGains = maxDailyGains;
            this.seasonalMultiplier = seasonalMultiplier;
            this.requiredPermissions = requiredPermissions;
        }
        
        public boolean isEnabled() { return enabled; }
        public int getMaxDailyGains() { return maxDailyGains; }
        public float getSeasonalMultiplier() { return seasonalMultiplier; }
        public Set<String> getRequiredPermissions() { return requiredPermissions; }
    }
    
    /**
     * Player XP Profile with Historical Tracking
     */
    public static class PlayerXPProfile {
        private final UUID playerId;
        private volatile long totalXP;
        private volatile long todayXP;
        private volatile long weekXP;
        private volatile long monthXP;
        private final Map<XPSource, Long> xpBySource;
        private final List<XPGainRecord> recentGains;
        private final AtomicLong lastUpdated;
        
        public PlayerXPProfile(UUID playerId) {
            this.playerId = playerId;
            this.totalXP = 0L;
            this.todayXP = 0L;
            this.weekXP = 0L;
            this.monthXP = 0L;
            this.xpBySource = new ConcurrentHashMap<>();
            this.recentGains = new CopyOnWriteArrayList<>();
            this.lastUpdated = new AtomicLong(System.currentTimeMillis());
        }
        
        public UUID getPlayerId() { return playerId; }
        public long getTotalXP() { return totalXP; }
        public long getTodayXP() { return todayXP; }
        public long getWeekXP() { return weekXP; }
        public long getMonthXP() { return monthXP; }
        public Map<XPSource, Long> getXPBySource() { return new HashMap<>(xpBySource); }
        public List<XPGainRecord> getRecentGains() { return new ArrayList<>(recentGains); }
        public long getLastUpdated() { return lastUpdated.get(); }
        
        public synchronized void addXP(XPSource source, long amount, String reason) {
            this.totalXP += amount;
            this.todayXP += amount;
            this.weekXP += amount;
            this.monthXP += amount;
            this.xpBySource.merge(source, amount, Long::sum);
            this.recentGains.add(new XPGainRecord(source, amount, reason, LocalDateTime.now()));
            this.lastUpdated.set(System.currentTimeMillis());
            
            // Maintain recent gains list size (keep last 100 entries)
            while (recentGains.size() > 100) {
                recentGains.remove(0);
            }
        }
        
        public void resetDailyXP() { this.todayXP = 0L; }
        public void resetWeeklyXP() { this.weekXP = 0L; }
        public void resetMonthlyXP() { this.monthXP = 0L; }
    }
    
    /**
     * XP Gain Record for Historical Tracking
     */
    public static class XPGainRecord {
        private final XPSource source;
        private final long amount;
        private final String reason;
        private final LocalDateTime timestamp;
        
        public XPGainRecord(XPSource source, long amount, String reason, LocalDateTime timestamp) {
            this.source = source;
            this.amount = amount;
            this.reason = reason;
            this.timestamp = timestamp;
        }
        
        public XPSource getSource() { return source; }
        public long getAmount() { return amount; }
        public String getReason() { return reason; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    /**
     * Daily XP Tracking with Rate Limiting
     */
    public static class DailyXPTracker {
        private final Map<XPSource, Integer> sourceGainCounts;
        private volatile long totalDailyXP;
        private volatile LocalDate lastReset;
        
        public DailyXPTracker() {
            this.sourceGainCounts = new ConcurrentHashMap<>();
            this.totalDailyXP = 0L;
            this.lastReset = LocalDate.now();
        }
        
        public boolean canGainXP(XPSource source, XPSourceConfig config) {
            checkDailyReset();
            return sourceGainCounts.getOrDefault(source, 0) < config.getMaxDailyGains()
                && totalDailyXP < getMaxDailyXP();
        }
        
        public void recordXPGain(XPSource source, long amount) {
            checkDailyReset();
            sourceGainCounts.merge(source, 1, Integer::sum);
            totalDailyXP += amount;
        }
        
        private void checkDailyReset() {
            LocalDate today = LocalDate.now();
            if (!today.equals(lastReset)) {
                sourceGainCounts.clear();
                totalDailyXP = 0L;
                lastReset = today;
            }
        }
        
        private long getMaxDailyXP() {
            return 5000L; // Configurable max daily XP
        }
    }
    
    /**
     * Weekly XP Tracking
     */
    public static class WeeklyXPTracker {
        private volatile long totalWeeklyXP;
        private volatile LocalDate weekStart;
        
        public WeeklyXPTracker() {
            this.totalWeeklyXP = 0L;
            this.weekStart = getWeekStart(LocalDate.now());
        }
        
        public boolean canGainXP(long amount) {
            checkWeeklyReset();
            return (totalWeeklyXP + amount) <= getMaxWeeklyXP();
        }
        
        public void recordXPGain(long amount) {
            checkWeeklyReset();
            totalWeeklyXP += amount;
        }
        
        private void checkWeeklyReset() {
            LocalDate currentWeekStart = getWeekStart(LocalDate.now());
            if (!currentWeekStart.equals(weekStart)) {
                totalWeeklyXP = 0L;
                weekStart = currentWeekStart;
            }
        }
        
        private LocalDate getWeekStart(LocalDate date) {
            return date.minusDays(date.getDayOfWeek().getValue() - 1);
        }
        
        private long getMaxWeeklyXP() {
            return 25000L; // Configurable max weekly XP
        }
    }
    
    /**
     * Monthly XP Tracking
     */
    public static class MonthlyXPTracker {
        private volatile long totalMonthlyXP;
        private volatile int currentMonth;
        private volatile int currentYear;
        
        public MonthlyXPTracker() {
            this.totalMonthlyXP = 0L;
            LocalDate now = LocalDate.now();
            this.currentMonth = now.getMonthValue();
            this.currentYear = now.getYear();
        }
        
        public boolean canGainXP(long amount) {
            checkMonthlyReset();
            return (totalMonthlyXP + amount) <= getMaxMonthlyXP();
        }
        
        public void recordXPGain(long amount) {
            checkMonthlyReset();
            totalMonthlyXP += amount;
        }
        
        private void checkMonthlyReset() {
            LocalDate now = LocalDate.now();
            if (now.getMonthValue() != currentMonth || now.getYear() != currentYear) {
                totalMonthlyXP = 0L;
                currentMonth = now.getMonthValue();
                currentYear = now.getYear();
            }
        }
        
        private long getMaxMonthlyXP() {
            return 75000L; // Configurable max monthly XP
        }
    }
    
    /**
     * Leaderboard Entry
     */
    public static class LeaderboardEntry {
        private final UUID playerId;
        private final String playerName;
        private final long totalXP;
        private final long todayXP;
        private final long weekXP;
        private final Map<XPSource, Long> xpBreakdown;
        private final int position;
        
        public LeaderboardEntry(UUID playerId, String playerName, long totalXP, long todayXP, 
                              long weekXP, Map<XPSource, Long> xpBreakdown, int position) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.totalXP = totalXP;
            this.todayXP = todayXP;
            this.weekXP = weekXP;
            this.xpBreakdown = xpBreakdown;
            this.position = position;
        }
        
        public UUID getPlayerId() { return playerId; }
        public String getPlayerName() { return playerName; }
        public long getTotalXP() { return totalXP; }
        public long getTodayXP() { return todayXP; }
        public long getWeekXP() { return weekXP; }
        public Map<XPSource, Long> getXPBreakdown() { return xpBreakdown; }
        public int getPosition() { return position; }
    }
    
    /**
     * Constructor
     */
    public AsyncXPTrackingSystem(DataManager dataManager, RedisManager redisManager, 
                               EventBus eventBus, ConfigurationManager configManager,
                               MetricsCollector metricsCollector) {
        this.dataManager = dataManager;
        this.redisManager = redisManager;
        this.eventBus = eventBus;
        this.configManager = configManager;
        this.metricsCollector = metricsCollector;
        
        this.playerProfiles = new ConcurrentHashMap<>();
        this.xpSourceConfigs = new ConcurrentHashMap<>();
        this.cooldownTracker = new ConcurrentHashMap<>();
        this.dailyXPTrackers = new ConcurrentHashMap<>();
        this.weeklyXPTrackers = new ConcurrentHashMap<>();
        this.monthlyXPTrackers = new ConcurrentHashMap<>();
        
        this.cachedLeaderboard = new AtomicReference<>(new ArrayList<>());
        this.leaderboardUpdateScheduler = Executors.newScheduledThreadPool(2);
        
        this.totalXPProcessed = new AtomicLong(0L);
        this.averageCalculationTime = new AtomicLong(0L);
        this.xpSourceMetrics = new ConcurrentHashMap<>();
        
        initializeXPSourceConfigs();
        initializeSystemComponents();
    }
    
    /**
     * Initialize XP source configurations
     */
    private void initializeXPSourceConfigs() {
        // Individual Achievement Sources
        xpSourceConfigs.put(XPSource.CHAT_ACTIVITY, new XPSourceConfig(true, 100, 1.0f, Set.of()));
        xpSourceConfigs.put(XPSource.PLAYTIME, new XPSourceConfig(true, 720, 1.0f, Set.of())); // 12 hours max
        xpSourceConfigs.put(XPSource.BLOCK_INTERACTION, new XPSourceConfig(true, 500, 1.0f, Set.of()));
        xpSourceConfigs.put(XPSource.MOB_INTERACTION, new XPSourceConfig(true, 200, 1.0f, Set.of()));
        xpSourceConfigs.put(XPSource.ADVANCEMENT_UNLOCK, new XPSourceConfig(true, 50, 1.2f, Set.of()));
        
        // Community Contribution Sources
        xpSourceConfigs.put(XPSource.MENTORING_NEW_PLAYERS, new XPSourceConfig(true, 10, 1.5f, Set.of("veloctopus.mentor")));
        xpSourceConfigs.put(XPSource.TEACHING_SESSIONS, new XPSourceConfig(true, 5, 1.8f, Set.of("veloctopus.teacher")));
        xpSourceConfigs.put(XPSource.CONFLICT_RESOLUTION, new XPSourceConfig(true, 3, 2.0f, Set.of("veloctopus.mediator")));
        xpSourceConfigs.put(XPSource.COMMUNITY_PROJECTS, new XPSourceConfig(true, 15, 1.6f, Set.of()));
        xpSourceConfigs.put(XPSource.PEER_RECOGNITION, new XPSourceConfig(true, 20, 1.4f, Set.of()));
        
        // Initialize metrics for each source
        for (XPSource source : XPSource.values()) {
            xpSourceMetrics.put(source, new AtomicLong(0L));
        }
    }
    
    /**
     * Initialize system components
     */
    private void initializeSystemComponents() {
        // Start leaderboard update scheduler
        leaderboardUpdateScheduler.scheduleAtFixedRate(
            this::updateLeaderboardCache, 
            0, 
            60, 
            TimeUnit.SECONDS
        );
        
        // Start daily reset scheduler
        leaderboardUpdateScheduler.scheduleAtFixedRate(
            this::performDailyReset,
            calculateSecondsUntilMidnight(),
            24 * 60 * 60,
            TimeUnit.SECONDS
        );
        
        // Register event listeners
        eventBus.subscribe(PlayerXPGainEvent.class, this::handlePlayerXPGain);
        eventBus.subscribe(CommunityContributionEvent.class, this::handleCommunityContribution);
        
        metricsCollector.gauge("xp_system.active_players", () -> playerProfiles.size());
        metricsCollector.gauge("xp_system.total_xp_processed", totalXPProcessed::get);
        metricsCollector.gauge("xp_system.average_calculation_time", averageCalculationTime::get);
    }
    
    /**
     * Award XP to a player
     */
    @Override
    public CompletableFuture<Void> executeAsync() {
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Award XP to a player with full validation and processing
     */
    public CompletableFuture<Boolean> awardXP(UUID playerId, XPSource source, String reason, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                // Validate XP source and configuration
                XPSourceConfig sourceConfig = xpSourceConfigs.get(source);
                if (sourceConfig == null || !sourceConfig.isEnabled()) {
                    return false;
                }
                
                // Check cooldown
                if (!checkCooldown(playerId, source)) {
                    return false;
                }
                
                // Get or create player profile
                PlayerXPProfile profile = getOrCreatePlayerProfile(playerId);
                
                // Check rate limits
                DailyXPTracker dailyTracker = dailyXPTrackers.computeIfAbsent(playerId, k -> new DailyXPTracker());
                WeeklyXPTracker weeklyTracker = weeklyXPTrackers.computeIfAbsent(playerId, k -> new WeeklyXPTracker());
                MonthlyXPTracker monthlyTracker = monthlyXPTrackers.computeIfAbsent(playerId, k -> new MonthlyXPTracker());
                
                // Calculate XP amount with multipliers
                long baseXP = source.getBaseXP();
                float multiplier = calculateXPMultiplier(source, sourceConfig, context);
                long finalXP = Math.round(baseXP * multiplier);
                
                // Check if player can gain this XP
                if (!dailyTracker.canGainXP(source, sourceConfig) ||
                    !weeklyTracker.canGainXP(finalXP) ||
                    !monthlyTracker.canGainXP(finalXP)) {
                    return false;
                }
                
                // Award the XP
                profile.addXP(source, finalXP, reason);
                dailyTracker.recordXPGain(source, finalXP);
                weeklyTracker.recordXPGain(finalXP);
                monthlyTracker.recordXPGain(finalXP);
                
                // Update cooldown
                updateCooldown(playerId, source);
                
                // Persist to Redis and database
                persistXPGain(playerId, profile, source, finalXP, reason);
                
                // Fire events
                PlayerXPGainEvent xpEvent = new PlayerXPGainEvent(playerId, source, finalXP, reason, profile.getTotalXP());
                eventBus.publish(xpEvent);
                
                // Check for rank promotion
                checkRankPromotion(playerId, profile.getTotalXP());
                
                // Update metrics
                totalXPProcessed.addAndGet(finalXP);
                xpSourceMetrics.get(source).incrementAndGet();
                
                long processingTime = System.currentTimeMillis() - startTime;
                averageCalculationTime.set((averageCalculationTime.get() + processingTime) / 2);
                
                metricsCollector.timer("xp_system.award_time", Duration.ofMillis(processingTime));
                metricsCollector.counter("xp_system.awards_total").increment();
                metricsCollector.counter("xp_system.awards_by_source", "source", source.name()).increment();
                
                return true;
                
            } catch (Exception e) {
                metricsCollector.counter("xp_system.award_errors").increment();
                return false;
            }
        });
    }
    
    /**
     * Calculate XP multiplier based on various factors
     */
    private float calculateXPMultiplier(XPSource source, XPSourceConfig config, Map<String, Object> context) {
        float multiplier = 1.0f;
        
        // Quality multiplier
        multiplier *= source.getQualityMultiplier();
        
        // Seasonal multiplier
        multiplier *= config.getSeasonalMultiplier();
        
        // Weekend multiplier (1.5x)
        LocalDateTime now = LocalDateTime.now();
        int dayOfWeek = now.getDayOfWeek().getValue();
        if (dayOfWeek == 6 || dayOfWeek == 7) { // Saturday or Sunday
            multiplier *= 1.5f;
        }
        
        // Community contribution bonus
        if (source.isCommunitySource()) {
            multiplier *= 1.3f; // 30% bonus for community activities
        }
        
        // Context-specific multipliers
        if (context != null) {
            // Event multiplier
            if (context.containsKey("event_multiplier")) {
                multiplier *= (Float) context.get("event_multiplier");
            }
            
            // Leadership bonus
            if (context.containsKey("leadership_bonus") && (Boolean) context.get("leadership_bonus")) {
                multiplier *= 1.5f;
            }
            
            // Quality rating from context (0.5x to 2.0x)
            if (context.containsKey("quality_rating")) {
                float qualityRating = (Float) context.get("quality_rating");
                multiplier *= Math.max(0.5f, Math.min(2.0f, qualityRating));
            }
        }
        
        return multiplier;
    }
    
    /**
     * Check XP source cooldown
     */
    private boolean checkCooldown(UUID playerId, XPSource source) {
        Map<XPSource, LocalDateTime> playerCooldowns = cooldownTracker.get(playerId);
        if (playerCooldowns == null) {
            return true;
        }
        
        LocalDateTime lastGain = playerCooldowns.get(source);
        if (lastGain == null) {
            return true;
        }
        
        LocalDateTime now = LocalDateTime.now();
        Duration timeSinceLastGain = Duration.between(lastGain, now);
        return timeSinceLastGain.getSeconds() >= source.getCooldownSeconds();
    }
    
    /**
     * Update cooldown for XP source
     */
    private void updateCooldown(UUID playerId, XPSource source) {
        cooldownTracker.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                     .put(source, LocalDateTime.now());
    }
    
    /**
     * Get or create player profile
     */
    private PlayerXPProfile getOrCreatePlayerProfile(UUID playerId) {
        return playerProfiles.computeIfAbsent(playerId, k -> {
            PlayerXPProfile profile = new PlayerXPProfile(playerId);
            loadPlayerProfileFromDatabase(profile);
            return profile;
        });
    }
    
    /**
     * Load player profile from database
     */
    private void loadPlayerProfileFromDatabase(PlayerXPProfile profile) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = dataManager.getConnection()) {
                // Load total XP and XP by source
                String sql = """
                    SELECT total_xp, today_xp, week_xp, month_xp, 
                           chat_xp, playtime_xp, block_xp, mob_xp, advancement_xp,
                           mentoring_xp, teaching_xp, conflict_xp, project_xp, peer_xp
                    FROM player_xp_profiles WHERE player_id = ?
                    """;
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, profile.getPlayerId().toString());
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        profile.totalXP = rs.getLong("total_xp");
                        profile.todayXP = rs.getLong("today_xp");
                        profile.weekXP = rs.getLong("week_xp");
                        profile.monthXP = rs.getLong("month_xp");
                        
                        // Load XP by source
                        profile.xpBySource.put(XPSource.CHAT_ACTIVITY, rs.getLong("chat_xp"));
                        profile.xpBySource.put(XPSource.PLAYTIME, rs.getLong("playtime_xp"));
                        profile.xpBySource.put(XPSource.BLOCK_INTERACTION, rs.getLong("block_xp"));
                        profile.xpBySource.put(XPSource.MOB_INTERACTION, rs.getLong("mob_xp"));
                        profile.xpBySource.put(XPSource.ADVANCEMENT_UNLOCK, rs.getLong("advancement_xp"));
                        profile.xpBySource.put(XPSource.MENTORING_NEW_PLAYERS, rs.getLong("mentoring_xp"));
                        profile.xpBySource.put(XPSource.TEACHING_SESSIONS, rs.getLong("teaching_xp"));
                        profile.xpBySource.put(XPSource.CONFLICT_RESOLUTION, rs.getLong("conflict_xp"));
                        profile.xpBySource.put(XPSource.COMMUNITY_PROJECTS, rs.getLong("project_xp"));
                        profile.xpBySource.put(XPSource.PEER_RECOGNITION, rs.getLong("peer_xp"));
                    }
                }
                
                // Load recent XP gains (last 100)
                String recentSql = """
                    SELECT source, amount, reason, timestamp
                    FROM player_xp_history 
                    WHERE player_id = ? 
                    ORDER BY timestamp DESC 
                    LIMIT 100
                    """;
                
                try (PreparedStatement stmt = connection.prepareStatement(recentSql)) {
                    stmt.setString(1, profile.getPlayerId().toString());
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        XPSource source = XPSource.valueOf(rs.getString("source"));
                        long amount = rs.getLong("amount");
                        String reason = rs.getString("reason");
                        LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                        
                        profile.recentGains.add(new XPGainRecord(source, amount, reason, timestamp));
                    }
                }
                
            } catch (SQLException e) {
                metricsCollector.counter("xp_system.database_errors").increment();
            }
        });
    }
    
    /**
     * Persist XP gain to Redis and database
     */
    private void persistXPGain(UUID playerId, PlayerXPProfile profile, XPSource source, long amount, String reason) {
        // Update Redis cache
        CompletableFuture.runAsync(() -> {
            try {
                String cacheKey = "xp:profile:" + playerId.toString();
                redisManager.hset(cacheKey, "total_xp", String.valueOf(profile.getTotalXP()));
                redisManager.hset(cacheKey, "today_xp", String.valueOf(profile.getTodayXP()));
                redisManager.hset(cacheKey, "week_xp", String.valueOf(profile.getWeekXP()));
                redisManager.hset(cacheKey, "month_xp", String.valueOf(profile.getMonthXP()));
                redisManager.hset(cacheKey, "last_updated", String.valueOf(profile.getLastUpdated()));
                redisManager.expire(cacheKey, 86400); // 24 hour expiration
            } catch (Exception e) {
                metricsCollector.counter("xp_system.redis_errors").increment();
            }
        });
        
        // Update database
        CompletableFuture.runAsync(() -> {
            try (Connection connection = dataManager.getConnection()) {
                // Update player profile
                String updateSql = """
                    INSERT INTO player_xp_profiles 
                    (player_id, total_xp, today_xp, week_xp, month_xp, last_updated, 
                     chat_xp, playtime_xp, block_xp, mob_xp, advancement_xp,
                     mentoring_xp, teaching_xp, conflict_xp, project_xp, peer_xp)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE 
                    total_xp = VALUES(total_xp),
                    today_xp = VALUES(today_xp),
                    week_xp = VALUES(week_xp),
                    month_xp = VALUES(month_xp),
                    last_updated = VALUES(last_updated),
                    chat_xp = VALUES(chat_xp),
                    playtime_xp = VALUES(playtime_xp),
                    block_xp = VALUES(block_xp),
                    mob_xp = VALUES(mob_xp),
                    advancement_xp = VALUES(advancement_xp),
                    mentoring_xp = VALUES(mentoring_xp),
                    teaching_xp = VALUES(teaching_xp),
                    conflict_xp = VALUES(conflict_xp),
                    project_xp = VALUES(project_xp),
                    peer_xp = VALUES(peer_xp)
                    """;
                
                try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
                    stmt.setString(1, playerId.toString());
                    stmt.setLong(2, profile.getTotalXP());
                    stmt.setLong(3, profile.getTodayXP());
                    stmt.setLong(4, profile.getWeekXP());
                    stmt.setLong(5, profile.getMonthXP());
                    stmt.setLong(6, profile.getLastUpdated());
                    
                    // Set XP by source
                    stmt.setLong(7, profile.xpBySource.getOrDefault(XPSource.CHAT_ACTIVITY, 0L));
                    stmt.setLong(8, profile.xpBySource.getOrDefault(XPSource.PLAYTIME, 0L));
                    stmt.setLong(9, profile.xpBySource.getOrDefault(XPSource.BLOCK_INTERACTION, 0L));
                    stmt.setLong(10, profile.xpBySource.getOrDefault(XPSource.MOB_INTERACTION, 0L));
                    stmt.setLong(11, profile.xpBySource.getOrDefault(XPSource.ADVANCEMENT_UNLOCK, 0L));
                    stmt.setLong(12, profile.xpBySource.getOrDefault(XPSource.MENTORING_NEW_PLAYERS, 0L));
                    stmt.setLong(13, profile.xpBySource.getOrDefault(XPSource.TEACHING_SESSIONS, 0L));
                    stmt.setLong(14, profile.xpBySource.getOrDefault(XPSource.CONFLICT_RESOLUTION, 0L));
                    stmt.setLong(15, profile.xpBySource.getOrDefault(XPSource.COMMUNITY_PROJECTS, 0L));
                    stmt.setLong(16, profile.xpBySource.getOrDefault(XPSource.PEER_RECOGNITION, 0L));
                    
                    stmt.executeUpdate();
                }
                
                // Insert XP history record
                String historySql = """
                    INSERT INTO player_xp_history 
                    (player_id, source, amount, reason, timestamp)
                    VALUES (?, ?, ?, ?, ?)
                    """;
                
                try (PreparedStatement stmt = connection.prepareStatement(historySql)) {
                    stmt.setString(1, playerId.toString());
                    stmt.setString(2, source.name());
                    stmt.setLong(3, amount);
                    stmt.setString(4, reason);
                    stmt.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                    stmt.executeUpdate();
                }
                
            } catch (SQLException e) {
                metricsCollector.counter("xp_system.database_errors").increment();
            }
        });
    }
    
    /**
     * Check for rank promotion after XP gain
     */
    private void checkRankPromotion(UUID playerId, long totalXP) {
        // This would integrate with AsyncRankSystem
        // Fire rank promotion event if threshold is met
        PlayerRankPromotionEvent promotionEvent = new PlayerRankPromotionEvent(playerId, totalXP);
        eventBus.publish(promotionEvent);
    }
    
    /**
     * Update leaderboard cache
     */
    private void updateLeaderboardCache() {
        CompletableFuture.supplyAsync(() -> {
            List<LeaderboardEntry> leaderboard = new ArrayList<>();
            
            try (Connection connection = dataManager.getConnection()) {
                String sql = """
                    SELECT p.player_id, p.player_name, p.total_xp, p.today_xp, p.week_xp,
                           p.chat_xp, p.playtime_xp, p.block_xp, p.mob_xp, p.advancement_xp,
                           p.mentoring_xp, p.teaching_xp, p.conflict_xp, p.project_xp, p.peer_xp
                    FROM player_xp_profiles p
                    JOIN players pl ON p.player_id = pl.player_id
                    ORDER BY p.total_xp DESC
                    LIMIT 100
                    """;
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    ResultSet rs = stmt.executeQuery();
                    int position = 1;
                    
                    while (rs.next()) {
                        UUID playerId = UUID.fromString(rs.getString("player_id"));
                        String playerName = rs.getString("player_name");
                        long totalXP = rs.getLong("total_xp");
                        long todayXP = rs.getLong("today_xp");
                        long weekXP = rs.getLong("week_xp");
                        
                        Map<XPSource, Long> xpBreakdown = new HashMap<>();
                        xpBreakdown.put(XPSource.CHAT_ACTIVITY, rs.getLong("chat_xp"));
                        xpBreakdown.put(XPSource.PLAYTIME, rs.getLong("playtime_xp"));
                        xpBreakdown.put(XPSource.BLOCK_INTERACTION, rs.getLong("block_xp"));
                        xpBreakdown.put(XPSource.MOB_INTERACTION, rs.getLong("mob_xp"));
                        xpBreakdown.put(XPSource.ADVANCEMENT_UNLOCK, rs.getLong("advancement_xp"));
                        xpBreakdown.put(XPSource.MENTORING_NEW_PLAYERS, rs.getLong("mentoring_xp"));
                        xpBreakdown.put(XPSource.TEACHING_SESSIONS, rs.getLong("teaching_xp"));
                        xpBreakdown.put(XPSource.CONFLICT_RESOLUTION, rs.getLong("conflict_xp"));
                        xpBreakdown.put(XPSource.COMMUNITY_PROJECTS, rs.getLong("project_xp"));
                        xpBreakdown.put(XPSource.PEER_RECOGNITION, rs.getLong("peer_xp"));
                        
                        leaderboard.add(new LeaderboardEntry(playerId, playerName, totalXP, 
                                                           todayXP, weekXP, xpBreakdown, position++));
                    }
                }
            } catch (SQLException e) {
                metricsCollector.counter("xp_system.leaderboard_errors").increment();
            }
            
            return leaderboard;
        }).thenAccept(leaderboard -> {
            cachedLeaderboard.set(leaderboard);
            metricsCollector.gauge("xp_system.leaderboard_size", () -> leaderboard.size());
        });
    }
    
    /**
     * Perform daily reset
     */
    private void performDailyReset() {
        CompletableFuture.runAsync(() -> {
            // Reset daily XP for all players
            for (PlayerXPProfile profile : playerProfiles.values()) {
                profile.resetDailyXP();
            }
            
            // Clear daily trackers
            dailyXPTrackers.clear();
            
            // Update database
            try (Connection connection = dataManager.getConnection()) {
                String sql = "UPDATE player_xp_profiles SET today_xp = 0";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                metricsCollector.counter("xp_system.daily_reset_errors").increment();
            }
            
            metricsCollector.counter("xp_system.daily_resets").increment();
        });
    }
    
    /**
     * Calculate seconds until midnight
     */
    private long calculateSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, midnight).getSeconds();
    }
    
    /**
     * Handle player XP gain events
     */
    private void handlePlayerXPGain(PlayerXPGainEvent event) {
        // Additional processing for XP gain events
        metricsCollector.counter("xp_system.events_processed", "type", "xp_gain").increment();
    }
    
    /**
     * Handle community contribution events
     */
    private void handleCommunityContribution(CommunityContributionEvent event) {
        // Award XP for community contributions
        Map<String, Object> context = Map.of(
            "community_contribution", true,
            "quality_rating", event.getQualityRating(),
            "leadership_bonus", event.hasLeadershipRole()
        );
        
        awardXP(event.getPlayerId(), XPSource.COMMUNITY_PROJECTS, 
               "Community contribution: " + event.getDescription(), context);
    }
    
    /**
     * Get player XP profile
     */
    public CompletableFuture<PlayerXPProfile> getPlayerProfile(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> getOrCreatePlayerProfile(playerId));
    }
    
    /**
     * Get current leaderboard
     */
    public CompletableFuture<List<LeaderboardEntry>> getLeaderboard() {
        return CompletableFuture.completedFuture(new ArrayList<>(cachedLeaderboard.get()));
    }
    
    /**
     * Get XP breakdown for player
     */
    public CompletableFuture<Map<XPSource, Long>> getPlayerXPBreakdown(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            PlayerXPProfile profile = getOrCreatePlayerProfile(playerId);
            return profile.getXPBySource();
        });
    }
    
    /**
     * Get system metrics
     */
    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("active_players", playerProfiles.size());
        metrics.put("total_xp_processed", totalXPProcessed.get());
        metrics.put("average_calculation_time_ms", averageCalculationTime.get());
        metrics.put("xp_source_metrics", new HashMap<>(xpSourceMetrics));
        metrics.put("leaderboard_size", cachedLeaderboard.get().size());
        return metrics;
    }
    
    /**
     * Shutdown the system
     */
    @Override
    public CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
            leaderboardUpdateScheduler.shutdown();
            try {
                if (!leaderboardUpdateScheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    leaderboardUpdateScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                leaderboardUpdateScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        });
    }
}
