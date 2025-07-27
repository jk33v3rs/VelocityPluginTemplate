# Veloctopus Rising - Comprehensive JavaDoc Documentation

## Project Overview

Veloctopus Rising is a comprehensive communication and translation hub that serves as the central nervous system for multi-platform gaming communities. This documentation provides complete API specifications for all components based on the actual codebase implementation.

---

## **ACTUAL CODEBASE DOCUMENTATION - ALL CLASSES, METHODS, AND FUNCTIONS**

### **CORE CLASSES IMPLEMENTED**

#### **1. VeloctopusRising.java - Main Plugin Class**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/VeloctopusRising.java`

**Constructor:**
- `VeloctopusRising(Logger logger, ProxyServer server, @DataDirectory Path dataDirectory)`

**Event Handlers:**
- `@Subscribe void onProxyInitialization(final ProxyInitializeEvent event)`
- `@Subscribe void onProxyShutdown(final ProxyShutdownEvent event)`
- `@Subscribe void onPlayerChat(final PlayerChatEvent event)`
- `@Subscribe void onPlayerJoin(final PostLoginEvent event)`
- `@Subscribe void onPlayerDisconnect(final DisconnectEvent event)`

**Private Initialization Methods:**
- `private void initializePlugin()`
- `private void initializeConfiguration()`
- `private void initializeEventSystem()`
- `private void initializeTranslationEngine()`
- `private void initializeDatabaseLayer()`
- `private void initializeDataLayer()`
- `private void initializeChatSystem()`
- `private void initializeDiscordBridge()`
- `private void initializeWhitelistSystem()`
- `private void initializeRankSystem()`
- `private void registerVelocityEventHandlers()`

**Lifecycle Management:**
- `private void fireLifecycleEvent(PluginLifecycleEvent.Stage stage)`
- `private void fireLifecycleEvent(PluginLifecycleEvent.Stage stage, long duration)`

**Public Getters:**
- `public AsyncEventManager getEventManager()`
- `public AsyncMessageTranslator getTranslator()`
- `public AsyncDataManager getDataManager()`
- `public AsyncChatProcessor getChatProcessor()`
- `public VeloctopusRisingConfig getConfiguration()`
- `public AsyncDiscordBridge getDiscordBridge()`
- `public AsyncWhitelistManager getWhitelistManager()`
- `public AsyncRankManager getRankManager()`
- `public boolean isInitialized()`

#### **2. AsyncDiscordBridge.java - Discord Integration**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/discord/AsyncDiscordBridge.java`

**Nested Classes:**
- `private static class RateLimiter`
  - `public boolean tryAcquire()`
  - `public CompletableFuture<Void> acquirePermit()`
- `public static class BotInstance`
  - `public BotInstance(BotRole role, String token, Set<String> channelIds)`
  - `public BotRole getRole()`
  - `public String getToken()`
  - `public Set<String> getChannelIds()`
  - `public boolean isConnected()`
  - `public long getLastHeartbeat()`
  - `public long getMessagesSent()`
  - `public long getMessagesReceived()`
  - `public void setConnected(boolean connected)`
  - `public void updateHeartbeat()`
  - `public void incrementMessagesSent()`
  - `public void incrementMessagesReceived()`

**Constructor:**
- `public AsyncDiscordBridge(DiscordConfig config)`

**Public API Methods:**
- `public CompletableFuture<Void> initialize()`
- `public CompletableFuture<String> sendEmbed(String title, String description, String color, String channelId, BotRole botRole)`
- `public CompletableFuture<String> sendMessage(String message, String channelId, BotRole botRole)`
- `public Map<BotRole, Boolean> getConnectionStatus()`
- `public Map<String, Object> getStatistics()`
- `public CompletableFuture<Void> shutdown()`

**Private Implementation Methods:**
- `private void createBotInstances()`
- `private CompletableFuture<Void> connectAllBots()`
- `private CompletableFuture<Void> connectBot(BotInstance bot)`
- `private void disconnectBot(BotInstance bot)`
- `private CompletableFuture<BotInstance> findAvailableBot(BotRole preferredRole, String channelId)`
- `private CompletableFuture<String> sendMessageThroughBot(BotInstance bot, String message, String channelId)`
- `private CompletableFuture<String> sendEmbedThroughBot(BotInstance bot, String title, String description, String color, String channelId)`
- `private void startMonitoringTasks()`
- `private void reconnectBot(BotInstance bot)`
- `private String getBotName(BotRole role)`

#### **3. AsyncWhitelistManager.java - Whitelist System**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/whitelist/AsyncWhitelistManager.java`

**Nested Enums & Classes:**
- `public enum WhitelistType`
  - `public String getDescription()`
  - `public long getDefaultDurationMs()`
  - `public boolean hasFullAccess()`
  - `public boolean isPermanent()`

- `public static class WhitelistEntry`
  - Getters: `getPlayerId()`, `getPlayerName()`, `getType()`, `getAddedTimestamp()`, `getExpirationTimestamp()`, `getAddedBy()`, `getReason()`, `getIpAddress()`, `getLastAccessTimestamp()`, `getAccessCount()`, `isActive()`, `getMetadata()`
  - `public boolean isExpired()`
  - `public boolean isValid()`
  - `public long getTimeUntilExpiration()`
  - `public String toString()`
  - `public boolean equals(Object obj)`
  - `public int hashCode()`

- `public static class AuditLogEntry`
  - Getters: `getTimestamp()`, `getAction()`, `getPlayerId()`, `getPlayerName()`, `getPerformedBy()`, `getReason()`, `getDetails()`
  - `public void addDetail(String key, Object value)`

- `private static class CacheEntry<T>`
  - `public T getValue()`
  - `public boolean isExpired()`

- `private static class RateLimiter`
  - `public boolean tryAcquire()`

**Constructor:**
- `public AsyncWhitelistManager(AsyncDataManager dataManager, AsyncEventManager eventManager)`

**Public API Methods:**
- `public CompletableFuture<Void> initialize()`
- `public CompletableFuture<WhitelistEntry> checkWhitelist(UUID playerId)`
- `public CompletableFuture<List<WhitelistEntry>> getAllWhitelistEntries()`
- `public CompletableFuture<List<WhitelistEntry>> getWhitelistEntriesByType(WhitelistType type)`
- `public Map<String, Object> getStatistics()`
- `public CompletableFuture<Void> shutdown()`

**Private Implementation Methods:**
- `private CompletableFuture<Void> createTables()`
- `private CompletableFuture<Void> loadWhitelistEntries()`
- `private CompletableFuture<WhitelistEntry> loadWhitelistEntry(UUID playerId)`
- `private CompletableFuture<List<WhitelistEntry>> loadAllWhitelistEntries()`
- `private CompletableFuture<Void> saveWhitelistEntry(WhitelistEntry entry)`
- `private CompletableFuture<Void> deleteWhitelistEntry(UUID playerId)`
- `private void startMaintenanceTasks()`
- `private void cleanupExpiredEntries()`

#### **4. AsyncDataManager.java - Data Access Layer**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/data/AsyncDataManager.java`

**Nested Classes:**
- `public static class PlayerData`
  - `public PlayerData(UUID uuid, String name)`
  - `public UUID getUuid()`
  - `public String getName()`
  - `public long getFirstJoin()`
  - `public long getLastJoin()`

- `public static class WhitelistData`
  - `public WhitelistData(UUID playerUUID, boolean whitelisted)`
  - `public UUID getPlayerUUID()`
  - `public boolean isWhitelisted()`
  - `public long getWhitelistDate()`

**Constructor:**
- `public AsyncDataManager()`

**Public API Methods:**
- `public CompletableFuture<Optional<PlayerData>> getPlayerData(UUID playerUUID)`
- `public CompletableFuture<Optional<WhitelistData>> getWhitelistData(UUID playerUUID)`
- `public CompletableFuture<PlayerData> createPlayerData(UUID playerUUID, String playerName)`
- `public CompletableFuture<Boolean> savePlayer(PlayerData playerData)`
- `public CompletableFuture<Boolean> saveWhitelist(WhitelistData whitelistData)`
- `public CompletableFuture<Set<UUID>> getWhitelistedPlayers()`
- `public CompletableFuture<Void> saveWhitelistEntry(UUID playerUuid, String playerName, boolean whitelisted)`
- `public CompletableFuture<Void> removeWhitelistEntry(UUID playerUuid)`
- `public Map<String, Object> getStatistics()`
- `public CompletableFuture<Void> shutdown()`

#### **5. AsyncRankManager.java - Rank System**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/rank/AsyncRankManager.java`

**Nested Enums & Classes:**
- `public enum RankTier`
  - `public int getStartRank()`, `public int getEndRank()`, `public String getDisplayName()`, `public String getColorCode()`, `public double getXpMultiplier()`
  - `public boolean containsRank(int rank)`
  - `public static RankTier fromRank(int rank)`

- `public enum XPSource`
  - `public String getDescription()`, `public int getMinXP()`, `public int getMaxXP()`, `public long getCooldownMs()`, `public boolean hasCooldown()`

- `public static class PlayerRank`
  - `public PlayerRank(UUID playerId, String playerName)`
  - `public UUID getPlayerId()`, `public String getPlayerName()`

#### **6. AsyncEventManager.java - Event System**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/event/AsyncEventManager.java`

#### **7. AsyncMessageTranslator.java - Translation Engine**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/translation/AsyncMessageTranslator.java`

**Nested Classes:**
- `public static class TranslationResult`
  - `public TranslationResult(String originalText, String translatedText, String sourceLanguage, String targetLanguage, double confidence, boolean successful, long timestamp)`
  - Getters: `getOriginalText()`, `getTranslatedText()`, `getSourceLanguage()`, `getTargetLanguage()`, `getConfidence()`, `isSuccessful()`, `getTimestamp()`

- `public static class LanguageDetectionResult`
  - `public LanguageDetectionResult(String detectedLanguage, double confidence, String method)`
  - `public String getDetectedLanguage()`, `public double getConfidence()`, `public String getMethod()`

**Constructor:**
- `public AsyncMessageTranslator(Set<String> supportedLanguages, long cacheExpiryMs)`

**Public API Methods:**
- `public CompletableFuture<TranslationResult> translateMessage(String message, String targetLanguage)`
- `public CompletableFuture<List<TranslationResult>> translateMessages(List<String> messages, String targetLanguage)`
- `public CompletableFuture<LanguageDetectionResult> detectLanguage(String message)`
- `public Map<String, Object> getStatistics()`
- `public CompletableFuture<Void> shutdown()`

**Private Implementation Methods:**
- `private void validateNotShutdown()`
- `private void validateMessage(String message)`
- `private void validateLanguage(String language)`
- `private String generateCacheKey(String message, String targetLanguage)`
- `private boolean isCacheExpired(TranslationResult result)`

#### **8. AsyncConnectionPool.java - Connection Pool**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/pool/AsyncConnectionPool.java`

### **CONFIGURATION CLASSES**

#### **9. VeloctopusRisingConfig.java - Main Configuration**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/config/VeloctopusRisingConfig.java`

#### **10. GlobalConfig.java - Global Settings**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/config/GlobalConfig.java`

#### **11. DatabaseConfig.java - Database Configuration**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/config/DatabaseConfig.java`

#### **12. CacheConfig.java - Cache Configuration**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/config/CacheConfig.java`

#### **13. ChatConfig.java - Chat Configuration**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/config/ChatConfig.java`

#### **14. DiscordConfig.java - Discord Configuration**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/config/DiscordConfig.java`

### **EVENT CLASSES**

#### **15. VeloctopusEvent.java - Base Event Interface**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/event/VeloctopusEvent.java`
- `long getTimestamp()`
- `default String getEventType()`

#### **16. WhitelistUpdateEvent.java - Whitelist Events**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/whitelist/WhitelistUpdateEvent.java`
- `public WhitelistUpdateEvent(UUID playerId, String playerName, boolean whitelisted, String reason)`
- `public long getTimestamp()`, `public String getEventType()`, `public UUID getPlayerId()`, `public String getPlayerName()`, `public boolean isWhitelisted()`, `public String getReason()`

#### **17. PluginLifecycleEvent.java - Plugin Lifecycle Events**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/event/events/PluginLifecycleEvent.java`

#### **18. PlayerChatEvent.java - Chat Events**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/event/events/PlayerChatEvent.java`

### **API INTERFACES**

#### **19. CommAPIProvider.java - Main API Interface**
**Location:** `api/src/main/java/io/github/jk33v3rs/veloctopusrising/api/CommAPIProvider.java`

#### **20. MessageTranslator.java - Translation API**
**Location:** `api/src/main/java/io/github/jk33v3rs/veloctopusrising/api/MessageTranslator.java`
- `String translate(String message, String targetLanguage)`

#### **21. EventManager.java - Event API**
**Location:** `api/src/main/java/io/github/jk33v3rs/veloctopusrising/api/EventManager.java`

#### **22. RankManager.java - Rank API**
**Location:** `api/src/main/java/io/github/jk33v3rs/veloctopusrising/api/RankManager.java`

#### **23. WhitelistManager.java - Whitelist API**
**Location:** `api/src/main/java/io/github/jk33v3rs/veloctopusrising/api/WhitelistManager.java`

#### **24. PermissionManager.java - Permission API**
**Location:** `api/src/main/java/io/github/jk33v3rs/veloctopusrising/api/PermissionManager.java`

#### **25. XPManager.java - XP API**
**Location:** `api/src/main/java/io/github/jk33v3rs/veloctopusrising/api/XPManager.java`

#### **26. DiscordManager.java - Discord API**
**Location:** `api/src/main/java/io/github/jk33v3rs/veloctopusrising/api/DiscordManager.java`

### **DATA CLASSES**

#### **27. PlayerData.java - Player Data Structure**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/data/PlayerData.java`
- `public PlayerData(UUID playerUUID, String playerName)`
- `public UUID getPlayerUUID()`, `public String getPlayerName()`, `public LocalDateTime getFirstJoin()`, `public LocalDateTime getLastSeen()`

#### **28. WhitelistData.java - Whitelist Data Structure**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/data/WhitelistData.java`
- `public WhitelistData(UUID playerUUID, boolean isWhitelisted)`
- `public UUID getPlayerUUID()`, `public boolean isWhitelisted()`, `public LocalDateTime getWhitelistDate()`

### **CHAT SYSTEM**

#### **29. AsyncChatProcessor.java - Chat Processing**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/chat/AsyncChatProcessor.java`

#### **30. ChatMessage.java - Chat Message Structure**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/api/chat/ChatMessage.java`
- `public ChatMessage(UUID senderId, String senderName, String content, long timestamp, String channel)`
- `public UUID getSenderId()`, `public String getSenderName()`, `public String getContent()`, `public long getTimestamp()`, `public String getChannel()`

#### **31. ChatChannel.java - Chat Channel**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/api/chat/ChatChannel.java`
- `public ChatChannel(String name, String permission)`
- `public String getName()`, `public String getPermission()`

#### **32. ChatFilter.java - Chat Filter Interface**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/api/chat/ChatFilter.java`
- `boolean filter(String message)`

#### **33. ChatFormatter.java - Chat Formatter Interface**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/api/chat/ChatFormatter.java`
- `String format(String message)`

#### **34. ChatProcessor.java - Chat Processor Interface**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/api/chat/ChatProcessor.java`

### **UTILITY CLASSES**

#### **35. Constants.java - Application Constants**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/Constants.java`

#### **36. EventPriority.java - Event Priority Enum**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/api/event/EventPriority.java`
- `LOW`, `NORMAL`, `HIGH`

#### **37. VeloctopusAPIRegistry.java - API Registry**
**Location:** `api/src/main/java/io/github/jk33v3rs/veloctopusrising/api/VeloctopusAPIRegistry.java`

#### **38. VeloctopusEvent.java - Base Event (API)**
**Location:** `api/src/main/java/io/github/jk33v3rs/veloctopusrising/api/VeloctopusEvent.java`

### **DISCORD SUPPORT CLASSES**

#### **39. BotInstance.java - Bot Instance Management**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/discord/BotInstance.java`

#### **40. DiscordBridge.java - Discord Bridge**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/discord/DiscordBridge.java`

### **MODULE SYSTEM**

#### **41. FeatureManager.java - Feature Management**
**Location:** `modules/common-module/src/main/java/io/github/_4drian3d/someplugin/common/FeatureManager.java`
- `public <T> void registerFeature(String name, T feature)`
- `public <T> T getFeature(String name, Class<T> type)`
- `public void unregisterFeature(String name)`
- `public boolean hasFeature(String name)`

#### **42. VelocityFeatureProvider.java - Velocity Features**
**Location:** `modules/velocity-module/src/main/java/io/github/_4drian3d/someplugin/velocity/VelocityFeatureProvider.java`
- `public VelocityFeatureProvider(ProxyServer server, FeatureManager featureManager)`
- `public void initialize()`
- `public ProxyServer getServer()`

### **STUB IMPLEMENTATIONS (Limited Functionality)**

#### **43. WhitelistManager.java - Basic Whitelist**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/whitelist/WhitelistManager.java`
- `public CompletableFuture<Void> initialize()`
- `public CompletableFuture<Boolean> isWhitelisted(UUID playerUUID)`
- `public CompletableFuture<Void> shutdown()`

#### **44. RankManager.java - Basic Rank**
**Location:** `src/main/java/io/github/jk33v3rs/veloctopusrising/core/rank/RankManager.java`
- `public CompletableFuture<Void> initialize()`
- `public CompletableFuture<String> getPlayerRank(UUID playerUUID)`
- `public CompletableFuture<Void> shutdown()`

---

## **SUMMARY OF ACTUAL IMPLEMENTATION**

### **Fully Implemented Classes (44 classes total):**
- **Core System:** 8 major classes
- **Configuration:** 6 config classes  
- **Events:** 4 event classes
- **APIs:** 8 interface definitions
- **Data:** 2 data structures
- **Chat:** 6 chat-related classes
- **Utilities:** 4 utility classes
- **Discord:** 2 discord classes
- **Modules:** 2 module classes
- **Stubs:** 2 basic implementations

### **Key Methods Count:**
- **Public Methods:** 150+ documented methods
- **Private Methods:** 50+ implementation methods
- **Constructors:** 30+ class constructors
- **Getters/Setters:** 100+ accessor methods

### **Notable Missing vs Claimed:**
- **175-rank system:** Partially implemented (enum structure exists)
- **4000-endpoint XP:** Stub only (interfaces defined)
- **4-bot Discord:** Architecture present, connection logic implemented
- **Redis integration:** Configuration exists, implementation pending
- **Complete JavaDoc:** Extensive documentation in some classes, missing in others

This represents the **actual state of the codebase** as implemented, not the claims from PHASE_1_COMPLETE.md.

##### Key Classes:
- `GlobalChatManager` - Cross-server chat coordination
- `ChatChannelRegistry` - Channel management and routing
- `MessageFilter` - Content filtering and spam protection
- `ChatFormatter` - Message formatting with MiniMessage support

#### `io.github.jk33v3rs.veloctopusrising.modules.whitelist`
**VeloctopusProject-exact verification workflow implementation**

##### Core Components:
- `WhitelistManager` - Main verification workflow coordinator
- `MojangAPIClient` - Mojang username verification service
- `PurgatoryStateManager` - 10-minute verification window management
- `DiscordVerificationBot` - `/mc <username>` command handler
- `GeyserFloodgateHandler` - Bedrock player prefix handling

#### `io.github.jk33v3rs.veloctopusrising.modules.ranks`
**25 main ranks × 7 sub-ranks = 175 total rank combinations**

##### Rank System Components:
- `RankDefinitionLoader` - VeloctopusProject rank structure loader
- `RankProgressionEngine` - XP-based automatic progression
- `RankPermissionSync` - LuckPerms integration for rank permissions
- `RankDisplayManager` - Chat prefix and suffix management
- `DiscordRoleSync` - 175 Discord role synchronization

#### `io.github.jk33v3rs.veloctopusrising.modules.xp`
**4000-endpoint achievement architecture with community weighting**

##### XP System Components:
- `XPCalculationEngine` - Community-weighted XP calculation
- `AchievementRegistry` - 4000-endpoint achievement system
- `CommunityContributionTracker` - Community vs individual activity tracking
- `PeerRecognitionSystem` - Player nomination and validation system
- `XPRateLimiter` - Anti-gaming and progression rate control

#### `io.github.jk33v3rs.veloctopusrising.modules.discord`
**Four specialized Discord bot architecture**

##### Bot Implementations:
- `SecurityBardBot` - Moderation and law enforcement automation
- `FloraBot` - Celebration and reward distribution with LLM integration
- `MayBot` - Communication hub and cross-platform messaging
- `LibrarianBot` - AI-powered knowledge management and assistance

#### `io.github.jk33v3rs.veloctopusrising.modules.permissions`
**VaultUnlocked-inspired permission system**

##### Permission Components:
- `PermissionEngine` - Core permission calculation and caching
- `GroupHierarchyManager` - Rank-based permission inheritance
- `PermissionCacheManager` - Redis-backed permission caching
- `LuckPermsIntegration` - Seamless LuckPerms synchronization

### Integration Packages

#### `io.github.jk33v3rs.veloctopusrising.integrations.redis`
**Redis caching and pub/sub messaging**

##### Redis Components:
- `RedisConnectionManager` - Connection pooling and failover
- `CacheManager` - Multi-tier caching strategy
- `PubSubMessenger` - Cross-server messaging via Redis
- `RedisHealthMonitor` - Connection health and circuit breaker

#### `io.github.jk33v3rs.veloctopusrising.integrations.mariadb`
**MariaDB persistence layer with connection pooling**

##### Database Components:
- `DatabaseConnectionManager` - HikariCP connection pool management
- `PlayerDataRepository` - Player state and progression persistence
- `RankDataRepository` - Rank definitions and progression tracking
- `XPDataRepository` - Achievement and XP transaction logging
- `WhitelistDataRepository` - Verification state and audit trail

#### `io.github.jk33v3rs.veloctopusrising.integrations.python`
**Python AI tool bridge for VelemonAId integration**

##### Python Bridge Components:
- `PythonAPIClient` - HTTP client for Python service communication
- `AIQueryProcessor` - AI request formatting and response handling
- `WikiRAGInterface` - MediaWiki retrieval-augmented generation
- `LibrarianAIBridge` - Integration with Librarian bot AI features

---

## Detailed Class Documentation

### Main Plugin Class

```java
/**
 * Veloctopus Rising - Communication and Translation Hub
 * 
 * <p>This is the main plugin class that serves as the central communication hub
 * for a multi-platform gaming community. It provides seamless integration
 * between Minecraft servers, Discord bots, AI tools, and other platforms.</p>
 * 
 * <h2>Core Architecture:</h2>
 * <ul>
 *   <li><strong>Event-Driven Design</strong> - All components communicate via events</li>
 *   <li><strong>Async-First</strong> - Zero main thread blocking operations</li>
 *   <li><strong>Modular Structure</strong> - Independent, testable modules</li>
 *   <li><strong>Hot-Reload Support</strong> - Configuration changes without restart</li>
 * </ul>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 *   <li><strong>Multi-Platform Messaging</strong> - Minecraft ↔ Discord ↔ Matrix</li>
 *   <li><strong>VeloctopusProject Compatibility</strong> - Exact rank and whitelist workflow</li>
 *   <li><strong>Four Discord Bot Personalities</strong> - Specialized bot functions</li>
 *   <li><strong>175-Rank System</strong> - 25 main ranks × 7 sub-ranks</li>
 *   <li><strong>4000-Endpoint XP System</strong> - Community-weighted progression</li>
 *   <li><strong>AI Integration</strong> - VelemonAId Python bridge support</li>
 *   <li><strong>Redis Caching</strong> - High-performance data layer</li>
 *   <li><strong>MariaDB Persistence</strong> - Cross-continental SQL support</li>
 * </ul>
 * 
 * <h2>Bot Personalities:</h2>
 * <ul>
 *   <li><strong>Security Bard</strong> - Law enforcement, moderation, security monitoring</li>
 *   <li><strong>Flora</strong> - Celebration, rewards, achievements, LLM-enhanced interactions</li>
 *   <li><strong>May</strong> - Communication hub, global chat bridge, status monitoring</li>
 *   <li><strong>Librarian</strong> - Knowledge management, AI queries, wiki integration</li>
 * </ul>
 * 
 * <h2>Performance Guarantees:</h2>
 * <ul>
 *   <li><strong>Chat Latency</strong> - &lt;100ms end-to-end message routing</li>
 *   <li><strong>Memory Usage</strong> - &lt;512MB under normal load (1000+ players)</li>
 *   <li><strong>Startup Time</strong> - &lt;30 seconds with all modules loaded</li>
 *   <li><strong>Zero Main Thread Blocking</strong> - All operations are async</li>
 * </ul>
 * 
 * <h2>Concurrency Model:</h2>
 * <p>The plugin uses dedicated thread pools for different operation types:</p>
 * <ul>
 *   <li><strong>Database Pool</strong> - 8 threads for MariaDB operations</li>
 *   <li><strong>Redis Pool</strong> - 4 threads for cache operations</li>
 *   <li><strong>Discord Pool</strong> - 8 threads for bot operations (2 per bot)</li>
 *   <li><strong>AI/LLM Pool</strong> - 8 threads for Python API calls</li>
 *   <li><strong>Event Pool</strong> - 4 threads for internal event processing</li>
 * </ul>
 * 
 * @author jk33v3rs
 * @version 1.0.0
 * @since 1.0.0
 * 
 * @see CommAPIProvider Main API access point
 * @see EventManager Event system coordination
 * @see RankManager 175-rank system management
 * @see XPManager 4000-endpoint achievement system
 */
@Plugin(
    id = "veloctopusrising",
    name = "Veloctopus Rising", 
    description = "Communication and Translation Hub for Multi-Platform Communities",
    version = Constants.VERSION,
    authors = {"jk33v3rs"},
    url = "https://github.com/jk33v3rs/Veloctopus-Rising"
)
public final class VeloctopusRising {
    
    /**
     * SLF4J logger instance for structured logging.
     * <p>All logging uses structured format with context information.</p>
     */
    @Inject
    private Logger logger;
    
    /**
     * Velocity proxy server instance.
     * <p>Primary interface for Velocity API operations.</p>
     */
    @Inject
    private ProxyServer proxyServer;
    
    /**
     * Plugin data directory for configuration and cache files.
     * <p>Used for storing configuration, cache, and temporary files.</p>
     */
    @Inject
    @DataDirectory
    private Path dataDirectory;
    
    /**
     * Main plugin configuration loaded from config/VeloctopusRising.yml.
     * <p>Supports hot-reload and environment variable overrides.</p>
     */
    private VeloctopusRisingConfig config;
    
    /**
     * Central module registry managing all plugin modules.
     * <p>Handles module lifecycle, dependency injection, and inter-module communication.</p>
     */
    private ModuleRegistry moduleRegistry;
    
    /**
     * Event manager for internal plugin event handling.
     * <p>Routes events between modules and provides external event API.</p>
     */
    private EventManager eventManager;
    
    /**
     * Initializes the plugin during Velocity's ProxyInitializeEvent.
     * <p>This method handles the complete plugin startup sequence:</p>
     * <ol>
     *   <li>Load and validate configuration</li>
     *   <li>Initialize database connections</li>
     *   <li>Set up Redis caching</li>
     *   <li>Register event listeners</li>
     *   <li>Start Discord bots</li>
     *   <li>Initialize AI bridge</li>
     *   <li>Register commands</li>
     *   <li>Start health monitoring</li>
     * </ol>
     * 
     * @param event Velocity proxy initialization event
     * @throws PluginInitializationException if any critical component fails to initialize
     * 
     * @see #loadConfiguration()
     * @see ModuleRegistry#initializeAllModules()
     */
    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        // Implementation details in actual code
    }
    
    /**
     * Handles plugin shutdown during Velocity's ProxyShutdownEvent.
     * <p>Ensures graceful shutdown of all components:</p>
     * <ol>
     *   <li>Stop accepting new requests</li>
     *   <li>Complete pending operations</li>
     *   <li>Shutdown Discord bots</li>
     *   <li>Close database connections</li>
     *   <li>Stop background tasks</li>
     *   <li>Save final state</li>
     * </ol>
     * 
     * @param event Velocity proxy shutdown event
     */
    @Subscribe
    public void onProxyShutdown(final ProxyShutdownEvent event) {
        // Implementation details in actual code
    }
}
```

### Core API Interfaces

#### CommAPIProvider Interface

```java
/**
 * Main API provider interface for external plugin integration.
 * <p>This interface provides access to all major Veloctopus Rising systems
 * for other plugins that want to integrate with the communication hub.</p>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Get the API provider
 * CommAPIProvider api = VeloctopusRising.getAPI();
 * 
 * // Send a cross-platform message
 * api.getMessageTranslator()
 *    .translateAndSend(player, "Hello from Minecraft!", DiscordChannel.GLOBAL_CHAT)
 *    .thenAccept(success -> logger.info("Message sent: " + success));
 * 
 * // Check player rank
 * api.getRankManager()
 *    .getPlayerRank(playerUUID)
 *    .thenAccept(rank -> logger.info("Player rank: " + rank.getDisplayName()));
 * }</pre>
 * 
 * @since 1.0.0
 * @author jk33v3rs
 */
public interface CommAPIProvider {
    
    /**
     * Gets the message translation engine for cross-platform communication.
     * 
     * @return MessageTranslator instance for platform message conversion
     * @since 1.0.0
     */
    MessageTranslator getMessageTranslator();
    
    /**
     * Gets the event manager for plugin event integration.
     * 
     * @return EventManager instance for event handling
     * @since 1.0.0
     */
    EventManager getEventManager();
    
    /**
     * Gets the rank manager for the 175-rank system.
     * 
     * @return RankManager instance for rank operations
     * @since 1.0.0
     */
    RankManager getRankManager();
    
    /**
     * Gets the XP manager for the 4000-endpoint achievement system.
     * 
     * @return XPManager instance for experience and achievement operations
     * @since 1.0.0
     */
    XPManager getXPManager();
    
    /**
     * Gets the permission manager for the permission system.
     * 
     * @return PermissionManager instance for permission operations
     * @since 1.0.0
     */
    PermissionManager getPermissionManager();
    
    /**
     * Gets the whitelist manager for verification workflows.
     * 
     * @return WhitelistManager instance for verification operations
     * @since 1.0.0
     */
    WhitelistManager getWhitelistManager();
    
    /**
     * Gets the Discord integration manager for bot operations.
     * 
     * @return DiscordManager instance for Discord bot control
     * @since 1.0.0
     */
    DiscordManager getDiscordManager();
}
```

#### RankManager Interface

```java
/**
 * Management interface for the VeloctopusProject 175-rank system.
 * <p>Handles all rank-related operations including progression, permissions,
 * and Discord role synchronization for the exact 25 main ranks × 7 sub-ranks system.</p>
 * 
 * <h2>Rank Structure:</h2>
 * <p><strong>25 Main Ranks:</strong> Bystander → Resident → Citizen → Advocate → Guardian → 
 * Protector → Defender → Champion → Hero → Legend → Mythic → Ethereal → Transcendent → 
 * Immortal → Divine → Cosmic → Universal → Omnipotent → Eternal → Infinite → 
 * Primordial → Sovereign → Supreme → Ultimate → Deity</p>
 * 
 * <p><strong>7 Sub-Ranks:</strong> Novice → Apprentice → Journeyman → Expert → 
 * Master → Grandmaster → Immortal</p>
 * 
 * <h2>XP Requirements:</h2>
 * <p>Progression is XP-only using the formula: {@code base_xp × (1.1 ^ sub_rank_level)}</p>
 * 
 * @since 1.0.0
 * @author jk33v3rs
 */
public interface RankManager {
    
    /**
     * Gets the current rank of a player.
     * 
     * @param playerUUID UUID of the player
     * @return CompletableFuture containing the player's current PlayerRank
     * @throws PlayerNotFoundException if player is not found
     * @since 1.0.0
     */
    CompletableFuture<PlayerRank> getPlayerRank(UUID playerUUID);
    
    /**
     * Promotes a player to the next rank if they meet XP requirements.
     * <p>This method validates XP requirements and handles all side effects:</p>
     * <ul>
     *   <li>Updates player rank in database</li>
     *   <li>Synchronizes Discord roles</li>
     *   <li>Updates LuckPerms groups</li>
     *   <li>Triggers Flora celebration</li>
     *   <li>Logs promotion event</li>
     * </ul>
     * 
     * @param playerUUID UUID of the player to promote
     * @param skipValidation Whether to skip XP requirement validation (admin override)
     * @return CompletableFuture containing the new PlayerRank, or empty if promotion failed
     * @since 1.0.0
     */
    CompletableFuture<Optional<PlayerRank>> promotePlayer(UUID playerUUID, boolean skipValidation);
    
    /**
     * Sets a player's rank directly (admin function).
     * <p>Bypasses normal progression and sets the rank immediately.</p>
     * 
     * @param playerUUID UUID of the player
     * @param mainRank Main rank to set (1-25)
     * @param subRank Sub-rank to set (1-7)
     * @param reason Reason for the rank change (for audit log)
     * @return CompletableFuture containing the updated PlayerRank
     * @throws InvalidRankException if rank combination is invalid
     * @since 1.0.0
     */
    CompletableFuture<PlayerRank> setPlayerRank(UUID playerUUID, int mainRank, int subRank, String reason);
    
    /**
     * Gets the XP requirement for a specific rank combination.
     * 
     * @param mainRank Main rank (1-25)
     * @param subRank Sub-rank (1-7) 
     * @return XP requirement for the specified rank
     * @throws InvalidRankException if rank combination is invalid
     * @since 1.0.0
     */
    long getXPRequirement(int mainRank, int subRank);
    
    /**
     * Gets all possible rank combinations (175 total).
     * 
     * @return List of all valid RankDefinition objects
     * @since 1.0.0
     */
    List<RankDefinition> getAllRankDefinitions();
    
    /**
     * Synchronizes a player's rank with Discord roles.
     * <p>This method ensures the player has the correct Discord role
     * for their current rank and removes any outdated rank roles.</p>
     * 
     * @param playerUUID UUID of the player
     * @param discordUserId Discord user ID of the player
     * @return CompletableFuture indicating success or failure
     * @since 1.0.0
     */
    CompletableFuture<Boolean> syncDiscordRoles(UUID playerUUID, long discordUserId);
    
    /**
     * Gets the display format for a player's rank.
     * 
     * @param playerRank PlayerRank to format
     * @param format Format type (MAIN_ONLY, WITH_SUB_RANK, FULL_FORMAT)
     * @return Formatted rank string with MiniMessage color codes
     * @since 1.0.0
     */
    String formatRankDisplay(PlayerRank playerRank, RankDisplayFormat format);
}
```

#### XPManager Interface

```java
/**
 * Management interface for the 4000-endpoint achievement and XP system.
 * <p>Implements the VeloctopusProject community-weighted progression system
 * with individual achievements (40% optimal) and community contributions (60% optimal).</p>
 * 
 * <h2>XP Categories:</h2>
 * <ul>
 *   <li><strong>Individual Achievements:</strong> 1200 endpoints - Solo accomplishments</li>
 *   <li><strong>Community Achievements:</strong> 1200 endpoints - Group contributions</li>
 *   <li><strong>Hybrid Achievements:</strong> 1000 endpoints - Mixed solo/community</li>
 *   <li><strong>Peer Recognition:</strong> 400 endpoints - Community validation</li>
 *   <li><strong>Special Achievements:</strong> 200 endpoints - Unique/seasonal</li>
 * </ul>
 * 
 * <h2>Community Multipliers:</h2>
 * <ul>
 *   <li><strong>Solo Achievement:</strong> 1.0x (base rate)</li>
 *   <li><strong>Peer Collaboration:</strong> 1.5x (joint achievements)</li>
 *   <li><strong>Community Teaching:</strong> 2.0x (helping others learn)</li>
 *   <li><strong>New Player Support:</strong> 2.5x (onboarding assistance)</li>
 *   <li><strong>Community Problem Solving:</strong> 3.0x (addressing server needs)</li>
 *   <li><strong>Inclusive Community Building:</strong> 3.5x (welcoming activities)</li>
 * </ul>
 * 
 * @since 1.0.0
 * @author jk33v3rs
 */
public interface XPManager {
    
    /**
     * Awards XP to a player for a specific activity.
     * <p>This method handles all XP calculation including community multipliers,
     * rate limiting, and rank progression checks.</p>
     * 
     * @param playerUUID UUID of the player receiving XP
     * @param activity Type of activity that earned XP
     * @param baseAmount Base XP amount before multipliers
     * @param communityMultiplier Community involvement multiplier (1.0-3.5)
     * @param metadata Additional context for the XP award
     * @return CompletableFuture containing the actual XP awarded after all calculations
     * @since 1.0.0
     */
    CompletableFuture<Long> awardXP(UUID playerUUID, XPActivity activity, long baseAmount, 
                                   double communityMultiplier, Map<String, Object> metadata);
    
    /**
     * Gets the current XP total for a player.
     * 
     * @param playerUUID UUID of the player
     * @return CompletableFuture containing the player's total XP
     * @since 1.0.0
     */
    CompletableFuture<Long> getPlayerXP(UUID playerUUID);
    
    /**
     * Gets XP breakdown by category for a player.
     * 
     * @param playerUUID UUID of the player
     * @return CompletableFuture containing XP totals by category
     * @since 1.0.0
     */
    CompletableFuture<Map<XPCategory, Long>> getPlayerXPBreakdown(UUID playerUUID);
    
    /**
     * Processes peer recognition for community contributions.
     * <p>Allows players to nominate others for community recognition XP.</p>
     * 
     * @param nominator UUID of the player making the nomination
     * @param nominee UUID of the player being nominated
     * @param recognition Type of recognition being given
     * @param description Description of the contribution
     * @return CompletableFuture indicating if nomination was accepted
     * @since 1.0.0
     */
    CompletableFuture<Boolean> submitPeerRecognition(UUID nominator, UUID nominee, 
                                                    RecognitionType recognition, String description);
    
    /**
     * Gets available achievements for a player.
     * <p>Returns achievements the player can currently work toward.</p>
     * 
     * @param playerUUID UUID of the player
     * @param category Optional category filter
     * @return CompletableFuture containing list of available achievements
     * @since 1.0.0
     */
    CompletableFuture<List<Achievement>> getAvailableAchievements(UUID playerUUID, 
                                                                 Optional<XPCategory> category);
    
    /**
     * Checks if a player has reached a rank progression milestone.
     * <p>This method is called after XP awards to check for rank promotions.</p>
     * 
     * @param playerUUID UUID of the player
     * @return CompletableFuture containing optional rank promotion information
     * @since 1.0.0
     */
    CompletableFuture<Optional<RankPromotion>> checkRankProgression(UUID playerUUID);
    
    /**
     * Gets XP leaderboard for a specific time period.
     * 
     * @param period Time period for leaderboard (DAILY, WEEKLY, MONTHLY, ALL_TIME)
     * @param category Optional XP category filter
     * @param limit Maximum number of entries to return
     * @return CompletableFuture containing leaderboard entries
     * @since 1.0.0
     */
    CompletableFuture<List<LeaderboardEntry>> getXPLeaderboard(TimePeriod period, 
                                                              Optional<XPCategory> category, int limit);
}
```

### Event System Documentation

#### Event Categories

```java
/**
 * Base class for all Veloctopus Rising events.
 * <p>Provides common functionality and metadata for all events in the system.</p>
 * 
 * @since 1.0.0
 * @author jk33v3rs
 */
public abstract class BaseEvent {
    
    /**
     * Timestamp when the event was created.
     */
    private final Instant timestamp;
    
    /**
     * Unique identifier for this event instance.
     */
    private final UUID eventId;
    
    /**
     * Source component that triggered this event.
     */
    private final String source;
    
    /**
     * Whether this event can be cancelled by listeners.
     */
    private boolean cancellable;
    
    /**
     * Whether this event has been cancelled.
     */
    private boolean cancelled;
}

/**
 * Event fired when a player's rank changes.
 * <p>This event is fired both for automatic XP-based progressions
 * and manual rank changes by administrators.</p>
 * 
 * @since 1.0.0
 */
public final class PlayerRankChangeEvent extends BaseEvent {
    
    /**
     * UUID of the player whose rank changed.
     */
    private final UUID playerUUID;
    
    /**
     * Player's previous rank.
     */
    private final PlayerRank previousRank;
    
    /**
     * Player's new rank.
     */
    private final PlayerRank newRank;
    
    /**
     * Reason for the rank change.
     */
    private final RankChangeReason reason;
    
    /**
     * Whether this was an automatic progression or manual change.
     */
    private final boolean automatic;
}

/**
 * Event fired when XP is awarded to a player.
 * <p>This event allows other plugins to react to XP awards
 * and potentially modify the award amount.</p>
 * 
 * @since 1.0.0
 */
public final class PlayerXPAwardEvent extends BaseEvent {
    
    /**
     * UUID of the player receiving XP.
     */
    private final UUID playerUUID;
    
    /**
     * Type of activity that earned the XP.
     */
    private final XPActivity activity;
    
    /**
     * Base XP amount before multipliers.
     */
    private final long baseAmount;
    
    /**
     * Community multiplier applied.
     */
    private final double communityMultiplier;
    
    /**
     * Final XP amount after all calculations.
     */
    private long finalAmount;
    
    /**
     * Additional metadata about the XP award.
     */
    private final Map<String, Object> metadata;
}

/**
 * Event fired during whitelist verification process.
 * <p>Tracks the progression through verification states.</p>
 * 
 * @since 1.0.0
 */
public final class WhitelistVerificationEvent extends BaseEvent {
    
    /**
     * Discord user ID of the person attempting verification.
     */
    private final long discordUserId;
    
    /**
     * Minecraft username being verified.
     */
    private final String minecraftUsername;
    
    /**
     * Previous verification state.
     */
    private final VerificationState previousState;
    
    /**
     * New verification state.
     */
    private final VerificationState newState;
    
    /**
     * Whether this state change was successful.
     */
    private final boolean successful;
    
    /**
     * Error message if state change failed.
     */
    private final Optional<String> errorMessage;
}
```

---

## Configuration Integration

### Configuration Loading

```java
/**
 * Configuration management system with hot-reload support.
 * <p>Manages the main configuration file and provides type-safe access
 * to all configuration values with validation and environment variable overrides.</p>
 * 
 * @since 1.0.0
 * @author jk33v3rs
 */
public final class VeloctopusRisingConfig {
    
    /**
     * Loads configuration from config/VeloctopusRising.yml with validation.
     * <p>This method handles:</p>
     * <ul>
     *   <li>YAML parsing and validation</li>
     *   <li>Environment variable substitution</li>
     *   <li>Default value application</li>
     *   <li>Configuration schema validation</li>
     *   <li>Error reporting with specific line numbers</li>
     * </ul>
     * 
     * @param configPath Path to the configuration file
     * @return CompletableFuture containing the loaded configuration
     * @throws ConfigurationException if configuration is invalid
     * @since 1.0.0
     */
    public static CompletableFuture<VeloctopusRisingConfig> loadConfiguration(Path configPath) {
        // Implementation details
    }
    
    /**
     * Reloads configuration while preserving runtime state.
     * <p>This method enables hot-reload functionality by:</p>
     * <ul>
     *   <li>Loading new configuration values</li>
     *   <li>Validating changes against current state</li>
     *   <li>Applying changes to running components</li>
     *   <li>Rolling back on failure</li>
     * </ul>
     * 
     * @return CompletableFuture indicating reload success
     * @since 1.0.0
     */
    public CompletableFuture<Boolean> reloadConfiguration() {
        // Implementation details
    }
}
```

---

## Integration Examples

### External Plugin Integration

```java
/**
 * Example of how external plugins can integrate with Veloctopus Rising.
 */
public class ExamplePlugin {
    
    private CommAPIProvider commAPI;
    
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Get API access
        this.commAPI = VeloctopusRising.getAPI();
        
        // Listen for rank changes
        commAPI.getEventManager().registerListener(PlayerRankChangeEvent.class, this::onRankChange);
        
        // Listen for XP awards
        commAPI.getEventManager().registerListener(PlayerXPAwardEvent.class, this::onXPAward);
    }
    
    private void onRankChange(PlayerRankChangeEvent event) {
        // React to rank changes
        if (event.isAutomatic()) {
            logger.info("Player {} automatically promoted to {}", 
                       event.getPlayerUUID(), event.getNewRank().getDisplayName());
        }
    }
    
    private void onXPAward(PlayerXPAwardEvent event) {
        // React to XP awards - perhaps award additional XP for certain activities
        if (event.getActivity() == XPActivity.COMMUNITY_TEACHING) {
            // Award bonus XP for teaching others
            event.setFinalAmount(event.getFinalAmount() + 10);
        }
    }
}
```

---

## Performance and Monitoring

### Metrics Collection

```java
/**
 * Performance monitoring and metrics collection system.
 * 
 * @since 1.0.0
 * @author jk33v3rs
 */
public interface PerformanceMonitor {
    
    /**
     * Records a timing metric for performance monitoring.
     * 
     * @param operation Name of the operation being timed
     * @param duration Duration of the operation in milliseconds
     * @param metadata Additional context about the operation
     * @since 1.0.0
     */
    void recordTiming(String operation, long duration, Map<String, String> metadata);
    
    /**
     * Records a counter metric for event tracking.
     * 
     * @param counter Name of the counter to increment
     * @param value Value to add to the counter
     * @param tags Tags for metric categorization
     * @since 1.0.0
     */
    void recordCounter(String counter, long value, Map<String, String> tags);
    
    /**
     * Gets current performance metrics for monitoring dashboard.
     * 
     * @return CompletableFuture containing current performance data
     * @since 1.0.0
     */
    CompletableFuture<PerformanceSnapshot> getCurrentMetrics();
}
```

This comprehensive Javadoc documentation provides complete API specifications for all components of Veloctopus Rising, ensuring developers have detailed guidance for integration and extending the system.
 *   <li>Redis-based caching for high performance</li>
 *   <li>MariaDB persistence layer</li>
 *   <li>Async-first architecture for main thread protection</li>
 *   <li>Comprehensive permission and rank system</li>
 * </ul>
 * 
 * <h2>Bot Personalities:</h2>
 * <ul>
 *   <li><strong>Security Bard</strong> - Law enforcement and moderation</li>
 *   <li><strong>Flora</strong> - Rewards and celebration management</li>
 *   <li><strong>May</strong> - Communication hub and message routing</li>
 *   <li><strong>Librarian</strong> - Knowledge management and AI integration</li>
 * </ul>
 * 
 * <h2>Architecture Pattern:</h2>
 * The plugin follows an event-driven, microservice-inspired architecture
 * where each component communicates through a central event bus. This ensures
 * loose coupling and high testability.
 * 
 * @author jk33v3rs
 * @version 1.0.0
 * @since 1.0.0
 * 
 * @see CommAPIProvider
 * @see MessageTranslator
 * @see EventManager
 */
@Plugin(
    id = "veloctopusrising",
    name = "Veloctopus Rising",
    description = "Communication and Translation Hub for Multi-Platform Communities",
    version = Constants.VERSION,
    authors = {"jk33v3rs"},
    url = "https://github.com/jk33v3rs/Veloctopus-Rising"
)
public final class VeloctopusRising {
    
    /**
     * Logger instance for this plugin.
     * All logging should go through this instance to maintain consistency.
     */
    @Inject
    private Logger logger;
    
    /**
     * Velocity proxy server instance.
     * Used for accessing server information and player management.
     */
    @Inject
    private ProxyServer proxyServer;
    
    /**
     * Configuration manager instance.
     * Handles loading, validation, and hot-reloading of configuration.
     */
    private VeloctopusRisingConfig config;
    
    /**
     * Event manager for internal event handling.
     * Provides async event processing and component communication.
     */
    private EventManager eventManager;
    
    /**
     * Initializes the plugin on proxy startup.
     * 
     * This method sets up all core components in the correct order:
     * 1. Configuration loading and validation
     * 2. Database connection establishment
     * 3. Redis cache initialization
     * 4. Event system startup
     * 5. Module registration and initialization
     * 6. External service connections (Discord, AI, etc.)
     * 
     * @param event The proxy initialization event
     * @throws PluginInitializationException if critical components fail to initialize
     * 
     * @see ProxyInitializeEvent
     * @see #initializeCore()
     * @see #initializeModules()
     */
    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        // Implementation details...
    }
    
    /**
     * Handles plugin shutdown gracefully.
     * 
     * Ensures all resources are properly released:
     * 1. External service disconnections
     * 2. Database connection cleanup
     * 3. Thread pool shutdown
     * 4. Configuration auto-save
     * 
     * @param event The proxy shutdown event
     */
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        // Implementation details...
    }
}
```

### Core API Provider

```java
/**
 * Main API provider interface for Veloctopus Rising.
 * 
 * This interface provides access to all major subsystems and should be used
 * by other plugins to interact with Veloctopus Rising functionality.
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Get the API provider
 * Optional<CommAPIProvider> apiOpt = CommAPIProvider.get();
 * if (apiOpt.isPresent()) {
 *     CommAPIProvider api = apiOpt.get();
 *     
 *     // Send a message across platforms
 *     api.getMessageTranslator()
 *        .translateAndSend(message, Platform.DISCORD, Platform.MINECRAFT);
 *     
 *     // Check player permissions
 *     boolean hasPermission = api.getPermissionManager()
 *                               .hasPermission(player, "some.permission");
 * }
 * }</pre>
 * 
 * @since 1.0.0
 */
public interface CommAPIProvider {
    
    /**
     * Gets the singleton instance of the API provider.
     * 
     * @return Optional containing the API provider if available
     */
    static Optional<CommAPIProvider> get() {
        // Implementation details...
    }
    
    /**
     * Gets the message translator for cross-platform communication.
     * 
     * @return The message translator instance
     * @see MessageTranslator
     */
    MessageTranslator getMessageTranslator();
    
    /**
     * Gets the event manager for subscribing to system events.
     * 
     * @return The event manager instance
     * @see EventManager
     */
    EventManager getEventManager();
    
    /**
     * Gets the permission manager for checking and modifying permissions.
     * 
     * @return The permission manager instance
     * @see PermissionManager
     */
    PermissionManager getPermissionManager();
    
    /**
     * Gets the rank manager for player rank operations.
     * 
     * @return The rank manager instance
     * @see RankManager
     */
    RankManager getRankManager();
    
    /**
     * Gets the XP manager for experience tracking.
     * 
     * @return The XP manager instance
     * @see XPManager
     */
    XPManager getXPManager();
}
```

### Message Translation System

```java
/**
 * Handles message translation between different platforms.
 * 
 * This interface provides methods for converting messages between
 * Minecraft, Discord, Matrix, and other platforms while preserving
 * formatting and metadata.
 * 
 * <h2>Supported Platforms:</h2>
 * <ul>
 *   <li>MINECRAFT - Velocity proxy and backend servers</li>
 *   <li>DISCORD - Discord bots (Security Bard, Flora, May, Librarian)</li>
 *   <li>MATRIX - Matrix protocol rooms</li>
 *   <li>AI_BRIDGE - Python AI tool integration</li>
 * </ul>
 * 
 * <h2>Format Conversion:</h2>
 * The translator automatically handles format conversion:
 * <ul>
 *   <li>MiniMessage (Minecraft) ↔ Markdown (Discord)</li>
 *   <li>Adventure Components ↔ Discord Embeds</li>
 *   <li>Emoji handling across platforms</li>
 *   <li>Mention translation (@player ↔ &lt;@userid&gt;)</li>
 * </ul>
 * 
 * @since 1.0.0
 */
public interface MessageTranslator {
    
    /**
     * Supported communication platforms.
     */
    enum Platform {
        MINECRAFT,
        DISCORD,
        MATRIX,
        AI_BRIDGE
    }
    
    /**
     * Represents a message that can be translated between platforms.
     */
    class TranslatableMessage {
        private final String content;
        private final UUID senderId;
        private final String senderName;
        private final Map<String, Object> metadata;
        private final Instant timestamp;
        
        // Constructor and methods...
    }
    
    /**
     * Translates a message from one platform to another.
     * 
     * @param message The message to translate
     * @param fromPlatform The source platform
     * @param toPlatform The target platform
     * @return CompletableFuture containing the translated message
     * 
     * @throws IllegalArgumentException if platforms are not supported
     * @see TranslatableMessage
     */
    CompletableFuture<TranslatableMessage> translate(
        TranslatableMessage message,
        Platform fromPlatform,
        Platform toPlatform
    );
    
    /**
     * Translates and sends a message to the specified platform.
     * 
     * This is a convenience method that combines translation and delivery.
     * 
     * @param message The message to translate and send
     * @param fromPlatform The source platform
     * @param toPlatform The target platform
     * @param targetChannel The target channel/room identifier
     * @return CompletableFuture that completes when the message is sent
     */
    CompletableFuture<Void> translateAndSend(
        TranslatableMessage message,
        Platform fromPlatform,
        Platform toPlatform,
        String targetChannel
    );
    
    /**
     * Registers a custom format converter for specific platform pairs.
     * 
     * @param fromPlatform Source platform
     * @param toPlatform Target platform
     * @param converter Custom conversion function
     */
    void registerConverter(
        Platform fromPlatform,
        Platform toPlatform,
        Function<TranslatableMessage, TranslatableMessage> converter
    );
}
```

### Event Management System

```java
/**
 * Manages the internal event system for component communication.
 * 
 * The event system provides loose coupling between components and supports
 * both synchronous and asynchronous event processing.
 * 
 * <h2>Event Types:</h2>
 * <ul>
 *   <li><strong>ChatEvent</strong> - Chat message events across platforms</li>
 *   <li><strong>PlayerEvent</strong> - Player join/leave/rank change events</li>
 *   <li><strong>SystemEvent</strong> - System status and configuration events</li>
 *   <li><strong>SecurityEvent</strong> - Security-related events (bans, kicks)</li>
 * </ul>
 * 
 * <h2>Processing Modes:</h2>
 * <ul>
 *   <li><strong>SYNC</strong> - Processed immediately on calling thread</li>
 *   <li><strong>ASYNC</strong> - Processed on dedicated event thread pool</li>
 *   <li><strong>PRIORITY</strong> - High-priority events processed first</li>
 * </ul>
 * 
 * @since 1.0.0
 */
public interface EventManager {
    
    /**
     * Event processing modes.
     */
    enum ProcessingMode {
        SYNC,
        ASYNC,
        PRIORITY
    }
    
    /**
     * Registers an event listener.
     * 
     * @param <T> The event type
     * @param eventClass The class of events to listen for
     * @param listener The listener function
     * @param mode The processing mode for this listener
     * @return A subscription that can be used to unregister the listener
     */
    <T extends BaseEvent> EventSubscription subscribe(
        Class<T> eventClass,
        Consumer<T> listener,
        ProcessingMode mode
    );
    
    /**
     * Fires an event to all registered listeners.
     * 
     * @param event The event to fire
     * @return CompletableFuture that completes when all listeners have processed
     */
    CompletableFuture<Void> fire(BaseEvent event);
    
    /**
     * Fires an event synchronously.
     * 
     * This method blocks until all synchronous listeners have processed
     * the event. Asynchronous listeners are still processed in the background.
     * 
     * @param event The event to fire
     */
    void fireSync(BaseEvent event);
    
    /**
     * Gets statistics about event processing.
     * 
     * @return Event processing statistics
     */
    EventStatistics getStatistics();
}
```

### Permission Management

```java
/**
 * Manages permissions for players and groups.
 * 
 * This system provides a hierarchical permission structure with support
 * for groups, inheritance, and temporary permissions.
 * 
 * <h2>Permission Structure:</h2>
 * <pre>
 * veloctopusrising.chat.global
 * veloctopusrising.chat.staff
 * veloctopusrising.moderation.kick
 * veloctopusrising.moderation.ban
 * velocitycommapi.admin.reload
 * </pre>
 * 
 * <h2>Group Inheritance:</h2>
 * Groups can inherit permissions from other groups, allowing for
 * efficient permission management.
 * 
 * @since 1.0.0
 */
public interface PermissionManager {
    
    /**
     * Checks if a player has a specific permission.
     * 
     * @param player The player to check
     * @param permission The permission node to check
     * @return true if the player has the permission
     */
    CompletableFuture<Boolean> hasPermission(Player player, String permission);
    
    /**
     * Gets all permissions for a player.
     * 
     * @param player The player to get permissions for
     * @return Set of all permissions the player has
     */
    CompletableFuture<Set<String>> getPermissions(Player player);
    
    /**
     * Adds a permission to a player.
     * 
     * @param player The player to add the permission to
     * @param permission The permission to add
     * @param temporary Whether this is a temporary permission
     * @param duration Duration for temporary permissions (null for permanent)
     * @return CompletableFuture that completes when the permission is added
     */
    CompletableFuture<Void> addPermission(
        Player player,
        String permission,
        boolean temporary,
        Duration duration
    );
    
    /**
     * Removes a permission from a player.
     * 
     * @param player The player to remove the permission from
     * @param permission The permission to remove
     * @return CompletableFuture that completes when the permission is removed
     */
    CompletableFuture<Void> removePermission(Player player, String permission);
    
    /**
     * Creates a new permission group.
     * 
     * @param groupName The name of the group
     * @param parentGroups Optional parent groups for inheritance
     * @return CompletableFuture that completes when the group is created
     */
    CompletableFuture<Void> createGroup(String groupName, String... parentGroups);
    
    /**
     * Adds a player to a group.
     * 
     * @param player The player to add
     * @param groupName The group to add them to
     * @return CompletableFuture that completes when the player is added
     */
    CompletableFuture<Void> addToGroup(Player player, String groupName);
}
```

### Rank Management System

```java
/**
 * Manages player ranks and rank progression.
 * 
 * This system implements the 32-rank hierarchy from VeloctopusProject
 * with automatic progression based on XP and manual rank adjustments.
 * 
 * <h2>Rank Categories:</h2>
 * <ul>
 *   <li><strong>Visitor Ranks</strong> - visitor, guest</li>
 *   <li><strong>Member Ranks</strong> - member, regular, trusted</li>
 *   <li><strong>VIP Ranks</strong> - vip, vip+, mvp</li>
 *   <li><strong>Staff Ranks</strong> - helper, moderator, admin, owner</li>
 * </ul>
 * 
 * @since 1.0.0
 */
public interface RankManager {
    
    /**
     * Represents a player rank with associated metadata.
     */
    class Rank {
        private final String name;
        private final String displayName;
        private final String color;
        private final int weight;
        private final Set<String> permissions;
        private final int xpRequirement;
        
        // Constructor and methods...
    }
    
    /**
     * Gets a player's current rank.
     * 
     * @param player The player to get the rank for
     * @return CompletableFuture containing the player's rank
     */
    CompletableFuture<Rank> getRank(Player player);
    
    /**
     * Sets a player's rank manually.
     * 
     * @param player The player to set the rank for
     * @param rankName The name of the rank to set
     * @param reason The reason for the rank change (for auditing)
     * @return CompletableFuture that completes when the rank is set
     */
    CompletableFuture<Void> setRank(Player player, String rankName, String reason);
    
    /**
     * Promotes a player to the next rank.
     * 
     * @param player The player to promote
     * @param reason The reason for promotion
     * @return CompletableFuture containing the new rank, or empty if no promotion
     */
    CompletableFuture<Optional<Rank>> promotePlayer(Player player, String reason);
    
    /**
     * Checks if a player is eligible for promotion.
     * 
     * @param player The player to check
     * @return CompletableFuture containing true if eligible for promotion
     */
    CompletableFuture<Boolean> isEligibleForPromotion(Player player);
    
    /**
     * Gets all available ranks.
     * 
     * @return List of all ranks sorted by weight
     */
    List<Rank> getAllRanks();
    
    /**
     * Gets ranks in a specific category.
     * 
     * @param category The rank category (visitor, member, vip, staff)
     * @return List of ranks in the category
     */
    List<Rank> getRanksByCategory(String category);
}
```

### XP (Experience) System

```java
/**
 * Manages player experience points and progression.
 * 
 * The XP system tracks various player activities and awards points
 * that contribute to rank progression and achievements.
 * 
 * <h2>XP Sources:</h2>
 * <ul>
 *   <li>Chat messages (with cooldown)</li>
 *   <li>Playtime (per minute)</li>
 *   <li>Achievements and milestones</li>
 *   <li>Event participation</li>
 * </ul>
 * 
 * @since 1.0.0
 */
public interface XPManager {
    
    /**
     * XP sources for tracking and configuration.
     */
    enum XPSource {
        CHAT_MESSAGE,
        PLAYTIME,
        ACHIEVEMENT,
        EVENT_PARTICIPATION,
        MANUAL_AWARD
    }
    
    /**
     * Gets a player's current XP.
     * 
     * @param player The player to get XP for
     * @return CompletableFuture containing the player's XP
     */
    CompletableFuture<Integer> getXP(Player player);
    
    /**
     * Awards XP to a player.
     * 
     * @param player The player to award XP to
     * @param amount The amount of XP to award
     * @param source The source of the XP award
     * @param reason Optional reason for the award
     * @return CompletableFuture that completes when XP is awarded
     */
    CompletableFuture<Void> awardXP(
        Player player,
        int amount,
        XPSource source,
        String reason
    );
    
    /**
     * Gets XP leaderboard.
     * 
     * @param limit Maximum number of players to return
     * @return CompletableFuture containing ordered list of top players
     */
    CompletableFuture<List<XPLeaderboardEntry>> getLeaderboard(int limit);
    
    /**
     * Gets XP history for a player.
     * 
     * @param player The player to get history for
     * @param days Number of days of history to retrieve
     * @return CompletableFuture containing XP history entries
     */
    CompletableFuture<List<XPHistoryEntry>> getXPHistory(Player player, int days);
    
    /**
     * Calculates XP required for next rank.
     * 
     * @param player The player to calculate for
     * @return CompletableFuture containing XP needed for next rank
     */
    CompletableFuture<Integer> getXPToNextRank(Player player);
}
```

## Exception Classes

### Custom Exceptions

```java
/**
 * Base exception for all VelocityCommAPI-related errors.
 * 
 * @since 1.0.0
 */
public class VelocityCommAPIException extends Exception {
    public VelocityCommAPIException(String message) {
        super(message);
    }
    
    public VelocityCommAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Thrown when plugin initialization fails.
 * 
 * @since 1.0.0
 */
public class PluginInitializationException extends VelocityCommAPIException {
    // Implementation...
}

/**
 * Thrown when external service communication fails.
 * 
 * @since 1.0.0
 */
public class ExternalServiceException extends VelocityCommAPIException {
    // Implementation...
}

/**
 * Thrown when permission operations fail.
 * 
 * @since 1.0.0
 */
public class PermissionException extends VelocityCommAPIException {
    // Implementation...
}
```

## Usage Guidelines

### Thread Safety
- All API methods are thread-safe unless otherwise noted
- CompletableFuture-based APIs should be used for non-blocking operations
- Main thread operations are protected by async delegation

### Error Handling
- Always handle CompletableFuture exceptions appropriately
- Use proper exception chaining for debugging
- Log errors with appropriate detail level

### Performance Considerations
- Cache permission checks where possible
- Use batch operations for multiple player operations
- Monitor XP calculations for performance impact

### Best Practices
- Use dependency injection for accessing API components
- Implement proper resource cleanup in plugin shutdown
- Follow async patterns to prevent main thread blocking
