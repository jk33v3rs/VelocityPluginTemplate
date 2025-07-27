# VelocityCommAPI - AI Guidelines and Coding Standards

## Overview
This document establishes comprehensive guidelines for AI-assisted development of VelocityCommAPI, ensuring consistent code quality, maintainability, and adherence to the project requirements. These standards are mandatory and must be followed without compromise.

---

## Core Development Philosophy

### 1. VeloctopusProject Adherence Principle
- **Exact Implementation**: All VeloctopusProject specifications must be implemented exactly as documented
- **25 Main Ranks × 7 Sub-Ranks**: The 175-rank system is non-negotiable and immutable
- **4000-Endpoint XP System**: Community-weighted progression must match specifications exactly
- **Discord Verification Workflow**: The `/mc <username>` → Purgatory → Member flow is mandatory
- **Four Bot Personalities**: Security Bard, Flora, May, and Librarian roles are fixed

### 2. Efficiency Over Perfection Principle  
- **67% Borrowed Code Minimum**: Leverage open-source implementations wherever possible
- **Spicord Architecture**: Use proven Discord integration patterns from Spicord ecosystem
- **HuskChat Patterns**: Adapt cross-server chat implementation from HuskChat
- **Working Code First**: Prioritize functionality over theoretical perfection
- **Tested Solutions**: Prefer established patterns over innovative but untested approaches

### 3. Lightweight Performance Architecture
- **Minimal Dependencies**: Every external dependency must be justified and documented
- **Self-Contained Design**: Prefer internal implementations when external libs add bloat
- **Performance Over Features**: Choose efficient solutions over feature-rich alternatives
- **Memory Constraints**: Target <512MB memory usage under load (1000+ players)
- **Startup Speed**: <30 seconds startup time with all modules loaded

### 4. Concurrency Native Design
- **Async-First Architecture**: All operations that could block must be asynchronous
- **Main Thread Protection**: Zero blocking operations on Velocity's main thread
- **Thread Pool Management**: Dedicated pools for different operation types
- **8-Core Zen 5 Optimization**: Leverage all available cores efficiently
- **Cross-Continental SQL**: Design for high-latency database operations

---

## AI Development Guidelines

### 1. Context Management and Memory
- **Constantly Cycle Back**: Return to documentation between every step to refresh context
- **Mark Progress**: Track completed items and always work ordinally, one thing at a time
- **Full Implementation**: Complete each feature entirely before moving to the next
- **Assembly First**: Adapt/write code so all features exist, then test comprehensively

### 2. Error Prevention and Quality
- **Zero Mistakes**: All things planned before enactment, follow planned procedures
- **No Infinite Testing**: Comprehensive planning prevents testing loops
- **Server Startup Time**: Expect measurable startup time, don't expect instant feedback
- **Graceful Degradation**: Continue operating when subsystems fail
- **Comprehensive Logging**: Structured logging with context for debugging

### 3. Implementation Strategy
- **Study Reference Code**: Examine Spicord, HuskChat, VeloctopusProject patterns first
- **Adapt Proven Solutions**: Modify existing code rather than writing from scratch
- **Maintain Compatibility**: Ensure integration with existing Velocity/Discord ecosystems
- **Performance Testing**: Validate all code under realistic load conditions

---

## Code Quality Standards

### Class Design Principles

#### 1. Single Responsibility with Module Focus
```java
// GOOD: Clear, focused responsibility
public final class XPCalculationEngine {
    public long calculateCommunityWeightedXP(XPActivity activity, double communityMultiplier) {
        return applyFormula(activity.getBaseXP(), communityMultiplier);
    }
    
    private long applyFormula(long baseXP, double multiplier) {
        return Math.round(baseXP * multiplier);
    }
}

// BAD: Multiple responsibilities in one class
public class PlayerManager {
    public void updateXP() { ... }
    public void sendDiscordMessage() { ... }
    public void validatePermissions() { ... }
    public void formatChatMessage() { ... }
}
```

#### 2. Immutability and Thread Safety
```java
// GOOD: Immutable data structures for thread safety
public final class PlayerRank {
    private final int mainRank;
    private final int subRank;
    private final String displayName;
    private final String color;
    private final long xpRequirement;
    
    public PlayerRank(int mainRank, int subRank, String displayName, String color, long xpRequirement) {
        this.mainRank = mainRank;
        this.subRank = subRank;
        this.displayName = Objects.requireNonNull(displayName);
        this.color = Objects.requireNonNull(color);
        this.xpRequirement = xpRequirement;
    }
    
    // Only getters, no setters
    public int getMainRank() { return mainRank; }
    public int getSubRank() { return subRank; }
    // ... other getters
}

// BAD: Mutable state causing thread safety issues
public class PlayerRank {
    public int mainRank;
    public int subRank;
    public String displayName;
    // Direct field access and mutation
}
```

#### 3. CompletableFuture for Async Operations
```java
// GOOD: Proper async pattern with error handling
public CompletableFuture<PlayerRank> getPlayerRank(UUID playerUUID) {
    return CompletableFuture
        .supplyAsync(() -> {
            try {
                return databaseRepository.getPlayerRank(playerUUID);
            } catch (SQLException e) {
                throw new RuntimeException("Database error getting player rank", e);
            }
        }, databaseExecutor)
        .exceptionally(throwable -> {
            logger.error("Failed to get player rank for {}", playerUUID, throwable);
            return getDefaultRank(); // Graceful fallback
        });
}

// BAD: Blocking operation on caller thread
public PlayerRank getPlayerRank(UUID playerUUID) {
    try {
        return databaseRepository.getPlayerRank(playerUUID); // BLOCKS THREAD
    } catch (SQLException e) {
        throw new RuntimeException(e); // Poor error handling
    }
}
```

### Interface Design Standards

#### 1. Focused Interface Segregation
```java
// GOOD: Focused interfaces for specific needs
public interface RankProgression {
    CompletableFuture<Optional<PlayerRank>> promotePlayer(UUID playerUUID);
    CompletableFuture<Boolean> validateXPRequirement(UUID playerUUID, int targetRank);
}

public interface RankDisplay {
    String formatRankDisplay(PlayerRank rank, RankDisplayFormat format);
    String getRankColor(PlayerRank rank);
}

public interface RankPersistence {
    CompletableFuture<PlayerRank> loadPlayerRank(UUID playerUUID);
    CompletableFuture<Boolean> savePlayerRank(UUID playerUUID, PlayerRank rank);
}

// BAD: Monolithic interface
public interface RankManager {
    // Too many responsibilities in one interface
    CompletableFuture<PlayerRank> getPlayerRank(UUID playerUUID);
    String formatRankDisplay(PlayerRank rank);
    CompletableFuture<Boolean> promotePlayer(UUID playerUUID);
    void sendDiscordNotification(UUID playerUUID);
    void updateDatabase(UUID playerUUID);
    boolean validatePermissions(UUID playerUUID);
}
```

#### 2. Builder Pattern for Complex Objects
```java
// GOOD: Builder pattern for complex configuration
public final class DiscordBotConfig {
    private final String token;
    private final String guildId;
    private final Map<String, String> channels;
    private final BotPersonality personality;
    private final Set<BotFeature> enabledFeatures;
    
    private DiscordBotConfig(Builder builder) {
        this.token = Objects.requireNonNull(builder.token);
        this.guildId = Objects.requireNonNull(builder.guildId);
        this.channels = Map.copyOf(builder.channels);
        this.personality = Objects.requireNonNull(builder.personality);
        this.enabledFeatures = Set.copyOf(builder.enabledFeatures);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        private String token;
        private String guildId;
        private Map<String, String> channels = new HashMap<>();
        private BotPersonality personality;
        private Set<BotFeature> enabledFeatures = EnumSet.noneOf(BotFeature.class);
        
        public Builder token(String token) {
            this.token = token;
            return this;
        }
        
        public Builder guildId(String guildId) {
            this.guildId = guildId;
            return this;
        }
        
        public Builder addChannel(String name, String id) {
            this.channels.put(name, id);
            return this;
        }
        
        public Builder personality(BotPersonality personality) {
            this.personality = personality;
            return this;
        }
        
        public Builder enableFeature(BotFeature feature) {
            this.enabledFeatures.add(feature);
            return this;
        }
        
        public DiscordBotConfig build() {
            return new DiscordBotConfig(this);
        }
    }
}
```

---

## VeloctopusProject Specific Standards

### 1. Rank System Implementation
```java
// MANDATORY: Exact rank definitions from VeloctopusProject
public enum MainRank {
    BYSTANDER(1, "Bystander", "<dark_gray>", 0),
    RESIDENT(2, "Resident", "<gray>", 500),
    CITIZEN(3, "Citizen", "<white>", 1500),
    ADVOCATE(4, "Advocate", "<yellow>", 3000),
    GUARDIAN(5, "Guardian", "<green>", 5000),
    PROTECTOR(6, "Protector", "<dark_green>", 8000),
    DEFENDER(7, "Defender", "<aqua>", 12000),
    CHAMPION(8, "Champion", "<dark_aqua>", 17000),
    HERO(9, "Hero", "<blue>", 25000),
    LEGEND(10, "Legend", "<dark_blue>", 35000),
    MYTHIC(11, "Mythic", "<light_purple>", 50000),
    ETHEREAL(12, "Ethereal", "<dark_purple>", 70000),
    TRANSCENDENT(13, "Transcendent", "<red>", 95000),
    IMMORTAL(14, "Immortal", "<dark_red>", 125000),
    DIVINE(15, "Divine", "<gold>", 160000),
    COSMIC(16, "Cosmic", "<#FF6B35>", 200000),
    UNIVERSAL(17, "Universal", "<#4ECDC4>", 250000),
    OMNIPOTENT(18, "Omnipotent", "<#45B7D1>", 310000),
    ETERNAL(19, "Eternal", "<#96CEB4>", 380000),
    INFINITE(20, "Infinite", "<#FFEAA7>", 460000),
    PRIMORDIAL(21, "Primordial", "<#DDA0DD>", 550000),
    SOVEREIGN(22, "Sovereign", "<#FF7675>", 650000),
    SUPREME(23, "Supreme", "<#74B9FF>", 760000),
    ULTIMATE(24, "Ultimate", "<#0984E3>", 880000),
    DEITY(25, "Deity", "<#E17055>", 1000000);
    
    private final int id;
    private final String displayName;
    private final String color;
    private final long baseXPRequirement;
    
    MainRank(int id, String displayName, String color, long baseXPRequirement) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.baseXPRequirement = baseXPRequirement;
    }
    
    // Standard getters...
}

public enum SubRank {
    NOVICE(1, "Novice", "", 1.0),
    APPRENTICE(2, "Apprentice", "+", 1.1),
    JOURNEYMAN(3, "Journeyman", "++", 1.21),
    EXPERT(4, "Expert", "+++", 1.331),
    MASTER(5, "Master", "★", 1.4641),
    GRANDMASTER(6, "Grandmaster", "★★", 1.61051),
    IMMORTAL(7, "Immortal", "★★★", 1.771561);
    
    private final int id;
    private final String displayName;
    private final String suffix;
    private final double xpMultiplier;
    
    SubRank(int id, String displayName, String suffix, double xpMultiplier) {
        this.id = id;
        this.displayName = displayName;
        this.suffix = suffix;
        this.xpMultiplier = xpMultiplier;
    }
    
    // Standard getters...
}
```

### 2. XP Calculation Implementation
```java
// MANDATORY: Exact community multiplier formula
public final class XPCalculationEngine {
    
    private static final Map<CommunityContributionType, Double> COMMUNITY_MULTIPLIERS = Map.of(
        CommunityContributionType.SOLO_ACHIEVEMENT, 1.0,
        CommunityContributionType.PEER_COLLABORATION, 1.5,
        CommunityContributionType.COMMUNITY_TEACHING, 2.0,
        CommunityContributionType.NEW_PLAYER_SUPPORT, 2.5,
        CommunityContributionType.COMMUNITY_PROBLEM_SOLVING, 3.0,
        CommunityContributionType.INCLUSIVE_COMMUNITY_BUILDING, 3.5
    );
    
    public long calculateFinalXP(long baseXP, CommunityContributionType contributionType, 
                                PlayerRank currentRank, boolean isWeekend) {
        double communityMultiplier = COMMUNITY_MULTIPLIERS.get(contributionType);
        double rankBonus = calculateRankBonus(currentRank);
        double timeMultiplier = isWeekend ? 1.5 : 1.0;
        
        return Math.round(baseXP * communityMultiplier * rankBonus * timeMultiplier);
    }
    
    private double calculateRankBonus(PlayerRank currentRank) {
        // Rank bonus: 1.0 + (main_rank - 1) * 0.05
        return 1.0 + (currentRank.getMainRank() - 1) * 0.05;
    }
    
    public long calculateRankXPRequirement(MainRank mainRank, SubRank subRank) {
        return Math.round(mainRank.getBaseXPRequirement() * subRank.getXpMultiplier());
    }
}
```

### 3. Discord Integration Standards
```java
// MANDATORY: Four bot personality implementation
public enum BotPersonality {
    SECURITY_BARD("stern_authoritative", "Law enforcement and security"),
    FLORA("cheerful_enthusiastic", "Celebration and rewards"),
    MAY("professional_efficient", "Communication and reliability"), 
    LIBRARIAN("scholarly_helpful", "Knowledge and education");
    
    private final String responseStyle;
    private final String primaryFunction;
    
    BotPersonality(String responseStyle, String primaryFunction) {
        this.responseStyle = responseStyle;
        this.primaryFunction = primaryFunction;
    }
}

// Bot command routing must respect personality boundaries
public final class DiscordCommandRouter {
    
    public void routeCommand(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        String botId = event.getJDA().getSelfUser().getId();
        
        BotPersonality personality = getBotPersonality(botId);
        
        switch (personality) {
            case SECURITY_BARD -> handleSecurityCommands(event, commandName);
            case FLORA -> handleCelebrationCommands(event, commandName);  
            case MAY -> handleCommunicationCommands(event, commandName);
            case LIBRARIAN -> handleKnowledgeCommands(event, commandName);
            default -> event.reply("Command not recognized for this bot.").setEphemeral(true).queue();
        }
    }
}
```

---

## Performance Standards

### 1. Database Operations
```java
// GOOD: Proper connection pooling and async operations
@Component
public final class PlayerDataRepository {
    
    private final HikariDataSource dataSource;
    private final Executor databaseExecutor;
    
    public CompletableFuture<Optional<PlayerRank>> getPlayerRank(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT main_rank, sub_rank FROM player_ranks WHERE player_uuid = ?";
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, playerUUID.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new PlayerRank(
                            rs.getInt("main_rank"),
                            rs.getInt("sub_rank")
                        ));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                logger.error("Database error getting player rank for {}", playerUUID, e);
                throw new DatabaseException("Failed to get player rank", e);
            }
        }, databaseExecutor);
    }
    
    // Batch operations for performance
    public CompletableFuture<Boolean> batchUpdatePlayerRanks(Map<UUID, PlayerRank> updates) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE player_ranks SET main_rank = ?, sub_rank = ? WHERE player_uuid = ?";
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                conn.setAutoCommit(false);
                
                for (Map.Entry<UUID, PlayerRank> entry : updates.entrySet()) {
                    stmt.setInt(1, entry.getValue().getMainRank());
                    stmt.setInt(2, entry.getValue().getSubRank());
                    stmt.setString(3, entry.getKey().toString());
                    stmt.addBatch();
                }
                
                stmt.executeBatch();
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                logger.error("Batch update failed", e);
                return false;
            }
        }, databaseExecutor);
    }
}
```

### 2. Redis Caching Patterns
```java
// GOOD: Redis caching with fallback and TTL
@Component
public final class PlayerRankCache {
    
    private final JedisPool jedisPool;
    private final PlayerDataRepository repository;
    private static final int DEFAULT_TTL_SECONDS = 3600; // 1 hour
    
    public CompletableFuture<PlayerRank> getPlayerRank(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            String cacheKey = "player_rank:" + playerUUID;
            
            // Try cache first
            try (Jedis jedis = jedisPool.getResource()) {
                String cached = jedis.get(cacheKey);
                if (cached != null) {
                    return deserializePlayerRank(cached);
                }
            } catch (Exception e) {
                logger.warn("Redis cache error for player {}, falling back to database", playerUUID, e);
            }
            
            // Fallback to database
            return repository.getPlayerRank(playerUUID)
                .thenApply(optionalRank -> {
                    if (optionalRank.isPresent()) {
                        PlayerRank rank = optionalRank.get();
                        // Cache the result
                        cachePlayerRank(playerUUID, rank);
                        return rank;
                    }
                    throw new PlayerNotFoundException("Player not found: " + playerUUID);
                })
                .join(); // Safe because we're already in async context
        });
    }
    
    private void cachePlayerRank(UUID playerUUID, PlayerRank rank) {
        CompletableFuture.runAsync(() -> {
            String cacheKey = "player_rank:" + playerUUID;
            String serialized = serializePlayerRank(rank);
            
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.setex(cacheKey, DEFAULT_TTL_SECONDS, serialized);
            } catch (Exception e) {
                logger.warn("Failed to cache player rank for {}", playerUUID, e);
                // Don't throw - caching failure shouldn't break the operation
            }
        });
    }
}
```

### 3. Thread Pool Management
```java
// MANDATORY: Proper thread pool configuration
@Configuration
public class ThreadPoolConfiguration {
    
    @Bean("databaseExecutor")
    public Executor databaseExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("database-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    @Bean("redisExecutor") 
    public Executor redisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("redis-");
        executor.initialize();
        return executor;
    }
    
    @Bean("discordExecutor")
    public Executor discordExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("discord-");
        executor.initialize();
        return executor;
    }
}
```

---

## Error Handling Standards

### 1. Exception Hierarchy
```java
// GOOD: Specific exception types for different error cases
public abstract class VelocityCommAPIException extends Exception {
    protected VelocityCommAPIException(String message) {
        super(message);
    }
    
    protected VelocityCommAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}

public final class PlayerNotFoundException extends VelocityCommAPIException {
    public PlayerNotFoundException(String message) {
        super(message);
    }
}

public final class RankProgressionException extends VelocityCommAPIException {
    public RankProgressionException(String message, Throwable cause) {
        super(message, cause);
    }
}

public final class DiscordIntegrationException extends VelocityCommAPIException {
    public DiscordIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### 2. Circuit Breaker Pattern
```java
// MANDATORY: Circuit breaker for external services
public final class CircuitBreaker {
    
    private final int failureThreshold;
    private final long recoveryTimeoutMs;
    private volatile CircuitState state = CircuitState.CLOSED;
    private volatile int failureCount = 0;
    private volatile long lastFailureTime = 0;
    
    public <T> CompletableFuture<T> execute(Supplier<CompletableFuture<T>> operation) {
        if (state == CircuitState.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime > recoveryTimeoutMs) {
                state = CircuitState.HALF_OPEN;
            } else {
                return CompletableFuture.failedFuture(
                    new CircuitBreakerOpenException("Circuit breaker is open")
                );
            }
        }
        
        return operation.get()
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    recordFailure();
                } else {
                    recordSuccess();
                }
            });
    }
    
    private synchronized void recordFailure() {
        failureCount++;
        lastFailureTime = System.currentTimeMillis();
        if (failureCount >= failureThreshold) {
            state = CircuitState.OPEN;
        }
    }
    
    private synchronized void recordSuccess() {
        failureCount = 0;
        state = CircuitState.CLOSED;
    }
    
    private enum CircuitState {
        CLOSED, OPEN, HALF_OPEN
    }
}
```

---

## Testing Standards

### 1. Unit Testing Requirements
```java
// MANDATORY: Comprehensive unit tests for all core functionality
@ExtendWith(MockitoExtension.class)
class XPCalculationEngineTest {
    
    @Mock
    private PlayerRankCache rankCache;
    
    @InjectMocks
    private XPCalculationEngine xpEngine;
    
    @Test
    void shouldCalculateCorrectXPForSoloAchievement() {
        // Given
        long baseXP = 100;
        CommunityContributionType type = CommunityContributionType.SOLO_ACHIEVEMENT;
        PlayerRank rank = new PlayerRank(1, 1); // Bystander Novice
        
        // When
        long result = xpEngine.calculateFinalXP(baseXP, type, rank, false);
        
        // Then
        assertThat(result).isEqualTo(100); // 100 * 1.0 * 1.0 * 1.0
    }
    
    @Test
    void shouldApplyCommunityMultiplierCorrectly() {
        // Given
        long baseXP = 100;
        CommunityContributionType type = CommunityContributionType.COMMUNITY_TEACHING;
        PlayerRank rank = new PlayerRank(5, 3); // Guardian Journeyman
        
        // When
        long result = xpEngine.calculateFinalXP(baseXP, type, rank, false);
        
        // Then
        // 100 * 2.0 (teaching) * 1.2 (rank 5 bonus) * 1.0 (not weekend) = 240
        assertThat(result).isEqualTo(240);
    }
    
    @Test
    void shouldApplyWeekendBonusCorrectly() {
        // Given
        long baseXP = 100;
        CommunityContributionType type = CommunityContributionType.PEER_COLLABORATION;
        PlayerRank rank = new PlayerRank(1, 1);
        
        // When
        long result = xpEngine.calculateFinalXP(baseXP, type, rank, true);
        
        // Then
        // 100 * 1.5 (collaboration) * 1.0 (rank 1) * 1.5 (weekend) = 225
        assertThat(result).isEqualTo(225);
    }
}
```

### 2. Integration Testing
```java
// MANDATORY: Integration tests for critical workflows
@SpringBootTest
@Testcontainers
class WhitelistIntegrationTest {
    
    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:10.6")
        .withDatabaseName("veloctopus_test")
        .withUsername("test")
        .withPassword("test");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);
    
    @Autowired
    private WhitelistManager whitelistManager;
    
    @Test
    void shouldCompleteFullVerificationWorkflow() {
        // Given
        String discordUserId = "123456789";
        String minecraftUsername = "TestPlayer";
        
        // When & Then - Test complete workflow
        // Step 1: Discord verification command
        CompletableFuture<VerificationResult> verificationFuture = 
            whitelistManager.processDiscordVerification(discordUserId, minecraftUsername);
        
        assertThat(verificationFuture.join().getState())
            .isEqualTo(VerificationState.PURGATORY);
        
        // Step 2: Player joins during purgatory window
        UUID playerUUID = UUID.randomUUID();
        CompletableFuture<Boolean> joinResult = 
            whitelistManager.handlePlayerJoin(playerUUID, minecraftUsername);
        
        assertThat(joinResult.join()).isTrue();
        
        // Step 3: Verify member status
        CompletableFuture<VerificationState> finalState = 
            whitelistManager.getPlayerVerificationState(playerUUID);
        
        assertThat(finalState.join()).isEqualTo(VerificationState.VERIFIED);
    }
}
```

---

## Documentation Requirements

### 1. Javadoc Standards
- **All public methods**: Must have comprehensive Javadoc with examples
- **Complex algorithms**: Detailed explanation of logic and performance characteristics
- **Integration points**: Clear documentation of external dependencies and error cases
- **Configuration**: All configuration options must be documented with valid examples

### 2. Code Comments
```java
// GOOD: Explanatory comments for complex logic
public long calculateRankXPRequirement(MainRank mainRank, SubRank subRank) {
    // XP requirement formula from VeloctopusProject:
    // base_main_rank_xp × (1.1 ^ sub_rank_level)
    // This ensures exponential growth within each main rank
    double subRankMultiplier = Math.pow(1.1, subRank.getId() - 1);
    return Math.round(mainRank.getBaseXPRequirement() * subRankMultiplier);
}

// BAD: Obvious or redundant comments
public int getMainRank() {
    return mainRank; // Returns the main rank
}
```

---

## Mandatory Compliance Checklist

Before any code submission, verify:

- [ ] **VeloctopusProject Compliance**: All specifications implemented exactly
- [ ] **Performance Requirements**: Memory usage <512MB, startup <30s, chat latency <100ms
- [ ] **Thread Safety**: No blocking operations on main thread
- [ ] **Error Handling**: Comprehensive exception handling with graceful degradation
- [ ] **Testing Coverage**: Unit tests for all public methods, integration tests for workflows
- [ ] **Documentation**: Complete Javadoc for all public APIs
- [ ] **Code Review**: All code reviewed against these standards
- [ ] **Integration Testing**: End-to-end testing with actual Discord/Velocity/database components

These standards are non-negotiable and ensure the successful delivery of a production-ready VelocityCommAPI that meets all project requirements and performance targets.
    private final UUID senderId;
    private final String content;
    private final Instant timestamp;
    
    public ChatMessage(UUID senderId, String content, Instant timestamp) {
        this.senderId = Objects.requireNonNull(senderId);
        this.content = Objects.requireNonNull(content);
        this.timestamp = Objects.requireNonNull(timestamp);
    }
    
    // Only getters, no setters
    public UUID getSenderId() { return senderId; }
    public String getContent() { return content; }
    public Instant getTimestamp() { return timestamp; }
}

// BAD: Mutable state
public class ChatMessage {
    private UUID senderId;
    private String content;
    // Setters allow mutation
}
```

#### 3. Builder Pattern for Complex Objects
```java
// GOOD: Builder pattern for complex configuration
public class DiscordBotConfig {
    private final String token;
    private final String guildId;
    private final Map<String, String> channels;
    private final boolean enabled;
    
    private DiscordBotConfig(Builder builder) {
        this.token = builder.token;
        this.guildId = builder.guildId;
        this.channels = Map.copyOf(builder.channels);
        this.enabled = builder.enabled;
    }
    
    public static class Builder {
        private String token;
        private String guildId;
        private Map<String, String> channels = new HashMap<>();
        private boolean enabled = true;
        
        public Builder token(String token) {
            this.token = token;
            return this;
        }
        
        public Builder guildId(String guildId) {
            this.guildId = guildId;
            return this;
        }
        
        public Builder channel(String name, String id) {
            this.channels.put(name, id);
            return this;
        }
        
        public DiscordBotConfig build() {
            return new DiscordBotConfig(this);
        }
    }
}
```

### Async Programming Standards

#### 1. CompletableFuture Usage
```java
// GOOD: Proper async chain with error handling
public CompletableFuture<Void> sendChatMessage(ChatMessage message) {
    return validateMessage(message)
        .thenCompose(this::translateMessage)
        .thenCompose(this::deliverMessage)
        .thenRun(() -> logMessageSent(message))
        .exceptionally(throwable -> {
            logger.error("Failed to send message: {}", message.getContent(), throwable);
            return null;
        });
}

// BAD: Blocking in async method
public CompletableFuture<Void> sendChatMessage(ChatMessage message) {
    return CompletableFuture.supplyAsync(() -> {
        // This blocks the thread pool!
        String translated = translateMessage(message).join();
        deliverMessage(translated).join();
        return null;
    });
}
```

#### 2. Thread Pool Management
```java
// GOOD: Dedicated thread pools
public class ThreadPoolManager {
    private final ExecutorService databasePool = 
        Executors.newFixedThreadPool(5, 
            new ThreadFactoryBuilder()
                .setNameFormat("commapi-db-%d")
                .setDaemon(true)
                .build());
    
    private final ExecutorService discordPool = 
        Executors.newFixedThreadPool(3,
            new ThreadFactoryBuilder()
                .setNameFormat("commapi-discord-%d")
                .setDaemon(true)
                .build());
    
    public ExecutorService getDatabasePool() { return databasePool; }
    public ExecutorService getDiscordPool() { return discordPool; }
}

// BAD: Using ForkJoinPool.commonPool() for everything
CompletableFuture.supplyAsync(() -> databaseOperation(), 
    ForkJoinPool.commonPool()); // Don't do this!
```

### Error Handling Standards

#### 1. Exception Hierarchy
```java
// GOOD: Clear exception hierarchy
public class VelocityCommAPIException extends Exception {
    public VelocityCommAPIException(String message) { super(message); }
    public VelocityCommAPIException(String message, Throwable cause) { 
        super(message, cause); 
    }
}

public class DatabaseException extends VelocityCommAPIException {
    public DatabaseException(String operation, Throwable cause) {
        super("Database operation failed: " + operation, cause);
    }
}

public class DiscordException extends VelocityCommAPIException {
    private final String botName;
    
    public DiscordException(String botName, String operation, Throwable cause) {
        super(String.format("Discord bot '%s' failed: %s", botName, operation), cause);
        this.botName = botName;
    }
    
    public String getBotName() { return botName; }
}
```

#### 2. Graceful Degradation
```java
// GOOD: Graceful degradation with fallbacks
public CompletableFuture<Void> sendGlobalMessage(String message) {
    return sendToDiscord(message)
        .exceptionally(throwable -> {
            logger.warn("Discord delivery failed, continuing with Minecraft only", throwable);
            return null;
        })
        .thenCompose(ignored -> sendToMinecraft(message))
        .exceptionally(throwable -> {
            logger.error("Critical: All message delivery failed", throwable);
            return null;
        });
}

// BAD: Failing fast without fallbacks
public CompletableFuture<Void> sendGlobalMessage(String message) {
    return sendToDiscord(message)
        .thenCompose(ignored -> sendToMinecraft(message));
    // If Discord fails, Minecraft never gets the message
}
```

### Database Interaction Standards

#### 1. Repository Pattern
```java
// GOOD: Repository pattern with async operations
public interface PlayerRepository {
    CompletableFuture<Optional<Player>> findById(UUID playerId);
    CompletableFuture<List<Player>> findByRank(String rankName);
    CompletableFuture<Void> save(Player player);
    CompletableFuture<Void> delete(UUID playerId);
}

@Singleton
public class MariaDBPlayerRepository implements PlayerRepository {
    private final HikariDataSource dataSource;
    private final ExecutorService dbExecutor;
    
    @Inject
    public MariaDBPlayerRepository(HikariDataSource dataSource, 
                                  @Named("database") ExecutorService dbExecutor) {
        this.dataSource = dataSource;
        this.dbExecutor = dbExecutor;
    }
    
    @Override
    public CompletableFuture<Optional<Player>> findById(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM players WHERE id = ?")) {
                stmt.setString(1, playerId.toString());
                // ... implementation
            } catch (SQLException e) {
                throw new DatabaseException("findById", e);
            }
        }, dbExecutor);
    }
}
```

#### 2. Connection Pool Management
```java
// GOOD: Proper connection pool configuration
@Provides
@Singleton
public HikariDataSource provideDataSource(VelocityCommAPIConfig config) {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(config.getDatabaseUrl());
    hikariConfig.setUsername(config.getDatabaseUsername());
    hikariConfig.setPassword(config.getDatabasePassword());
    
    // Performance tuning
    hikariConfig.setMinimumIdle(5);
    hikariConfig.setMaximumPoolSize(20);
    hikariConfig.setConnectionTimeout(30000);
    hikariConfig.setIdleTimeout(600000);
    hikariConfig.setMaxLifetime(1800000);
    
    // Monitoring
    hikariConfig.setLeakDetectionThreshold(60000);
    
    return new HikariDataSource(hikariConfig);
}
```

### Logging Standards

#### 1. Structured Logging
```java
// GOOD: Structured logging with context
public class ChatMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageHandler.class);
    
    public void handleMessage(ChatMessage message) {
        MDC.put("playerId", message.getSenderId().toString());
        MDC.put("playerName", message.getSenderName());
        MDC.put("messageLength", String.valueOf(message.getContent().length()));
        
        try {
            logger.info("Processing chat message from player {}", message.getSenderName());
            processMessage(message);
            logger.debug("Message processed successfully");
        } catch (Exception e) {
            logger.error("Failed to process message", e);
        } finally {
            MDC.clear();
        }
    }
}

// BAD: Unstructured logging
public void handleMessage(ChatMessage message) {
    System.out.println("Got message: " + message.getContent());
    // Processing...
    System.out.println("Done");
}
```

#### 2. Log Level Guidelines
```java
// Log level usage guidelines:

// ERROR: System failures, exceptions that prevent normal operation
logger.error("Failed to connect to database", exception);

// WARN: Recoverable errors, degraded functionality
logger.warn("Discord bot disconnected, will retry in 30 seconds");

// INFO: Important business events, system lifecycle
logger.info("Player {} promoted to rank {}", playerName, newRank);

// DEBUG: Detailed flow information for troubleshooting
logger.debug("Processing message: length={}, channels={}", 
    message.length(), targetChannels.size());

// TRACE: Very detailed information, performance data
logger.trace("Database query took {}ms: {}", duration, query);
```

### Testing Standards

#### 1. Unit Test Structure
```java
// GOOD: Comprehensive unit test
@ExtendWith(MockitoExtension.class)
class MessageTranslatorTest {
    
    @Mock
    private DiscordFormatter discordFormatter;
    
    @Mock
    private MinecraftFormatter minecraftFormatter;
    
    @InjectMocks
    private MessageTranslator messageTranslator;
    
    @Test
    @DisplayName("Should translate Minecraft message to Discord format")
    void shouldTranslateMinecraftToDiscord() {
        // Given
        TranslatableMessage minecraftMessage = TranslatableMessage.builder()
            .content("<red>Hello World</red>")
            .senderId(UUID.randomUUID())
            .senderName("TestPlayer")
            .platform(Platform.MINECRAFT)
            .build();
        
        TranslatableMessage expectedDiscord = TranslatableMessage.builder()
            .content("**Hello World**")
            .senderId(minecraftMessage.getSenderId())
            .senderName("TestPlayer")
            .platform(Platform.DISCORD)
            .build();
        
        when(discordFormatter.format(any())).thenReturn(expectedDiscord);
        
        // When
        CompletableFuture<TranslatableMessage> result = 
            messageTranslator.translate(minecraftMessage, 
                Platform.MINECRAFT, Platform.DISCORD);
        
        // Then
        assertThat(result).succeedsWithin(Duration.ofSeconds(1));
        assertThat(result.join().getContent()).isEqualTo("**Hello World**");
        verify(discordFormatter).format(minecraftMessage);
    }
    
    @Test
    @DisplayName("Should handle translation errors gracefully")
    void shouldHandleTranslationErrors() {
        // Given
        TranslatableMessage message = createTestMessage();
        when(discordFormatter.format(any()))
            .thenThrow(new RuntimeException("Format error"));
        
        // When & Then
        assertThatThrownBy(() -> 
            messageTranslator.translate(message, Platform.MINECRAFT, Platform.DISCORD)
                .join())
            .isInstanceOf(CompletionException.class)
            .hasCauseInstanceOf(RuntimeException.class);
    }
}
```

#### 2. Integration Test Patterns
```java
// GOOD: Integration test with test containers
@Testcontainers
@ExtendWith(GuiceExtension.class)
class DatabaseIntegrationTest {
    
    @Container
    static MariaDBContainer<?> mariaDB = new MariaDBContainer<>("mariadb:10.6")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
    
    @Inject
    private PlayerRepository playerRepository;
    
    @Test
    @DisplayName("Should persist and retrieve player data")
    void shouldPersistAndRetrievePlayerData() {
        // Given
        Player player = Player.builder()
            .id(UUID.randomUUID())
            .name("TestPlayer")
            .rank("member")
            .xp(1000)
            .build();
        
        // When
        playerRepository.save(player).join();
        Optional<Player> retrieved = playerRepository.findById(player.getId()).join();
        
        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get()).isEqualTo(player);
    }
}
```

## Performance Guidelines

### 1. Memory Management
```java
// GOOD: Efficient collection usage
public class MessageCache {
    private final Map<UUID, List<ChatMessage>> cache = 
        Collections.synchronizedMap(new LinkedHashMap<UUID, List<ChatMessage>>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<UUID, List<ChatMessage>> eldest) {
                return size() > 1000; // Limit cache size
            }
        });
    
    public void addMessage(UUID playerId, ChatMessage message) {
        cache.computeIfAbsent(playerId, k -> new ArrayList<>()).add(message);
    }
}

// BAD: Memory leak potential
public class MessageCache {
    private final Map<UUID, List<ChatMessage>> cache = new ConcurrentHashMap<>();
    
    public void addMessage(UUID playerId, ChatMessage message) {
        cache.computeIfAbsent(playerId, k -> new ArrayList<>()).add(message);
        // Cache grows indefinitely!
    }
}
```

### 2. Database Query Optimization
```java
// GOOD: Batch operations
public CompletableFuture<Void> updatePlayerXP(Map<UUID, Integer> xpUpdates) {
    return CompletableFuture.runAsync(() -> {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE players SET xp = xp + ? WHERE id = ?")) {
            
            for (Map.Entry<UUID, Integer> entry : xpUpdates.entrySet()) {
                stmt.setInt(1, entry.getValue());
                stmt.setString(2, entry.getKey().toString());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new DatabaseException("batchUpdateXP", e);
        }
    }, databaseExecutor);
}

// BAD: Individual queries
public CompletableFuture<Void> updatePlayerXP(Map<UUID, Integer> xpUpdates) {
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    for (Map.Entry<UUID, Integer> entry : xpUpdates.entrySet()) {
        // This creates N database connections!
        futures.add(updateSinglePlayerXP(entry.getKey(), entry.getValue()));
    }
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
}
```

## Security Guidelines

### 1. Input Validation
```java
// GOOD: Comprehensive input validation
public class ChatMessageValidator {
    private static final int MAX_MESSAGE_LENGTH = 256;
    private static final Pattern VALID_MESSAGE_PATTERN = 
        Pattern.compile("^[\\p{L}\\p{N}\\p{P}\\p{Z}]*$");
    
    public ValidationResult validate(String message) {
        if (message == null || message.trim().isEmpty()) {
            return ValidationResult.error("Message cannot be empty");
        }
        
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return ValidationResult.error("Message too long");
        }
        
        if (!VALID_MESSAGE_PATTERN.matcher(message).matches()) {
            return ValidationResult.error("Message contains invalid characters");
        }
        
        return ValidationResult.success();
    }
}

// BAD: No validation
public void sendMessage(String message) {
    // Directly processing user input without validation
    processMessage(message);
}
```

### 2. Permission Checking
```java
// GOOD: Consistent permission checking
public CompletableFuture<Void> executeCommand(Player player, String command, String[] args) {
    String permission = "velocitycommapi.command." + command;
    
    return permissionManager.hasPermission(player, permission)
        .thenCompose(hasPermission -> {
            if (!hasPermission) {
                return CompletableFuture.failedFuture(
                    new PermissionException("Insufficient permissions for command: " + command));
            }
            return executeCommandInternal(player, command, args);
        });
}

// BAD: Inconsistent or missing permission checks
public void executeCommand(Player player, String command, String[] args) {
    // Sometimes checks permissions, sometimes doesn't
    if (command.equals("ban")) {
        // Check permissions for ban
    }
    // But not for other commands!
    executeCommandInternal(player, command, args);
}
```

## Documentation Standards

### 1. Javadoc Requirements
```java
/**
 * Manages player experience points and rank progression.
 * 
 * <p>This service handles all XP-related operations including awarding points,
 * calculating rank eligibility, and maintaining leaderboards. All operations
 * are asynchronous to prevent blocking the main server thread.
 * 
 * <h3>XP Sources:</h3>
 * <ul>
 *   <li>Chat messages (1 XP per message, 60-second cooldown)</li>
 *   <li>Playtime (2 XP per minute)</li>
 *   <li>Achievements (variable amounts)</li>
 * </ul>
 * 
 * <h3>Thread Safety:</h3>
 * All methods in this class are thread-safe and can be called from any thread.
 * 
 * @author jk33v3rs
 * @since 1.0.0
 * @see RankManager
 * @see PermissionManager
 */
public interface XPManager {
    
    /**
     * Awards experience points to a player.
     * 
     * <p>This method will automatically check for rank eligibility after
     * awarding XP and trigger promotion events if applicable.
     * 
     * @param player the player to award XP to, must not be null
     * @param amount the amount of XP to award, must be positive
     * @param source the source of the XP award, must not be null
     * @param reason optional reason for audit logging, may be null
     * @return a CompletableFuture that completes when XP is awarded
     * @throws IllegalArgumentException if amount is negative or zero
     * @throws NullPointerException if player or source is null
     * 
     * @see XPSource
     * @see #getXP(Player)
     */
    CompletableFuture<Void> awardXP(Player player, int amount, XPSource source, String reason);
}
```

### 2. Configuration Documentation
```yaml
# XP System Configuration
xp:
  # Whether the XP system is enabled globally
  # Default: true
  enabled: true
  
  # XP award sources and their configuration
  sources:
    # XP awarded for chat messages
    chat_message:
      # Base XP per message
      # Range: 1-10, Default: 1
      base_xp: 1
      
      # Cooldown between XP awards from chat
      # Prevents spam, Default: 60 seconds
      cooldown_seconds: 60
      
      # Maximum XP from chat per hour
      # Prevents excessive grinding, Default: 30
      max_per_hour: 30
```

## AI Assistant Guidelines

### 1. Code Generation Principles
- **Always follow the established patterns** in the codebase
- **Use CompletableFuture** for all potentially blocking operations
- **Include comprehensive error handling** with proper exception types
- **Add appropriate logging** at the correct levels
- **Include Javadoc** for all public methods and classes
- **Write corresponding unit tests** for new functionality

### 2. Refactoring Guidelines
- **Preserve existing functionality** unless explicitly asked to change it
- **Maintain backward compatibility** for public APIs
- **Update tests** when changing implementation details
- **Document breaking changes** clearly

### 3. Architecture Decisions
- **Prefer composition over inheritance** for flexibility
- **Use dependency injection** for testability
- **Keep modules loosely coupled** through well-defined interfaces
- **Design for horizontal scaling** where applicable

### 4. Performance Considerations
- **Profile before optimizing** - don't guess at bottlenecks
- **Use async patterns** to prevent thread blocking
- **Cache strategically** with appropriate TTL values
- **Monitor memory usage** in long-running operations

This document serves as the definitive guide for maintaining code quality and consistency throughout the VelocityCommAPI project. All contributors, whether human or AI, should adhere to these standards to ensure a maintainable and professional codebase.
