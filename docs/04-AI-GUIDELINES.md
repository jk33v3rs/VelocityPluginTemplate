# VelocityCommAPI - AI Guidelines & Coding Standards

## Overview
This document establishes comprehensive guidelines for AI-assisted development of VelocityCommAPI. These standards ensure consistent, high-quality code generation while maintaining the project's architectural integrity and performance requirements.

## Core AI Development Principles

### 1. Context Preservation and Memory Management
**The AI Must Constantly Refresh Context**
- Return to project documentation between every development step
- Re-read the project overview, development plan, and configuration before each coding session
- Mark completed items in development checklist as progress is made
- Validate current implementation against documented requirements
- Never assume previous context is still accurate

**Memory Management Strategy:**
```markdown
1. Before Each Code Session:
   - Read 00-PROJECT-OVERVIEW.md
   - Read current phase from 01-DEVELOPMENT-PLAN.md
   - Check 02-CONFIGURATION-KEYS.md for relevant settings
   - Review last completed checklist items

2. During Development:
   - Complete ONE feature at a time, in full
   - Test each feature before moving to next
   - Update documentation as features are completed
   - Mark checklist items as DONE when finished

3. After Each Feature:
   - Validate implementation against requirements
   - Run tests to ensure functionality works
   - Update progress tracking
   - Refresh context for next feature
```

### 2. Ordinal Development Methodology
**One Thing at a Time, In Full, In Order**
- Follow the development plan sequentially
- Complete each item fully before starting the next
- No parallel development of unrelated features
- Each feature must be tested and validated before proceeding
- Document completion status clearly

**Implementation Order:**
1. **Foundation First**: Core APIs, event system, configuration
2. **Infrastructure Second**: Database, cache, connection management
3. **Core Features Third**: Chat, whitelist, ranks (in that order)
4. **Integration Fourth**: Discord bots, external services
5. **Enhancement Fifth**: Advanced features, optimization

### 3. Assembly-First Development Strategy
**Build Working Features, Then Optimize**
- Focus on getting complete features working end-to-end
- Prefer working implementations over perfect implementations
- Optimize only after functionality is proven
- Use borrowed code extensively (67% minimum target)
- Adapt proven patterns rather than inventing new ones

## Code Quality Standards

### 1. Borrowed Code Integration
**Leverage Open Source Libraries Effectively**
- **Target**: Minimum 67% borrowed/adapted code
- **Sources**: Spicord, HuskChat, VeloctopusProject, Adrian3D ecosystem
- **Integration**: Adapt patterns, not just copy-paste
- **Attribution**: Document source and modifications clearly
- **Licensing**: Ensure compliance with Apache 2.0 and compatible licenses

**Borrowing Strategy:**
```java
/**
 * Architecture borrowed from Spicord's multi-bot management system.
 * Original: eu.mcdb.spicord.bot.BotManager
 * Adaptations: 
 * - Extended for 4-bot architecture instead of 3
 * - Added personality-based message routing
 * - Integrated LLM trigger system for Flora and Librarian
 * 
 * @see <a href="https://github.com/OopsieWoopsie/Spicord">Spicord Project</a>
 */
```

### 2. Error Prevention and Handling
**Zero-Mistake Development Protocol**
- **Plan Before Code**: All features documented before implementation
- **Validate Continuously**: Test after each component is built
- **No Infinite Testing**: Fix issues immediately, don't accumulate technical debt
- **Graceful Degradation**: System continues operating when components fail
- **Comprehensive Logging**: Every significant action logged with context

**Error Handling Pattern:**
```java
public CompletableFuture<Result<T>> operationName(Parameters params) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            // Validate inputs
            if (!isValidInput(params)) {
                return Result.failure("Invalid parameters: " + params.validate());
            }
            
            // Execute operation with logging
            logger.debug("Starting operation: {} with params: {}", operationName, params);
            T result = performOperation(params);
            logger.info("Operation completed successfully: {}", operationName);
            
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("Operation failed: {} - {}", operationName, e.getMessage(), e);
            return Result.failure("Operation failed: " + e.getMessage());
        }
    }, asyncExecutor);
}
```

### 3. Concurrency and Performance Standards
**Thread-Safe, Non-Blocking Architecture**
- **Main Thread Protection**: Never block Velocity's main thread
- **Async-First Design**: Use CompletableFuture for all operations
- **Connection Pooling**: Manage database and Redis connections efficiently
- **Circuit Breakers**: Prevent cascade failures in external service calls
- **Performance Monitoring**: Track and log performance metrics

**Concurrency Pattern:**
```java
@ThreadSafe
public class ConcurrentServiceManager {
    private final ExecutorService executor = ForkJoinPool.commonPool();
    private final Map<String, CompletableFuture<Void>> activeOperations = 
        new ConcurrentHashMap<>();
    
    public CompletableFuture<Void> executeAsync(String operationId, Runnable operation) {
        return activeOperations.computeIfAbsent(operationId, id -> 
            CompletableFuture.runAsync(operation, executor)
                .whenComplete((result, throwable) -> {
                    activeOperations.remove(id);
                    if (throwable != null) {
                        logger.error("Operation {} failed", id, throwable);
                        // Implement circuit breaker logic
                        circuitBreaker.recordFailure(id, throwable);
                    } else {
                        circuitBreaker.recordSuccess(id);
                    }
                })
        );
    }
}
```

## VelocityCommAPI-Specific Development Guidelines

### 1. Configuration Management
**Single Source of Truth Configuration**
- All configuration in single YAML file: `config/Veloctopus.yml`
- Hot-reload support for all non-critical settings
- Environment variable substitution for sensitive data
- Validation on startup and reload
- Clear error messages for configuration issues

**Configuration Implementation Pattern:**
```java
@ConfigurationProperties("veloctopus")
public class VeloctopusConfig {
    private DatabaseConfig database = new DatabaseConfig();
    private CacheConfig cache = new CacheConfig();
    private DiscordConfig discord = new DiscordConfig();
    
    @PostConstruct
    public void validate() {
        List<String> errors = new ArrayList<>();
        database.validate(errors);
        cache.validate(errors);
        discord.validate(errors);
        
        if (!errors.isEmpty()) {
            throw new ConfigurationException("Configuration validation failed: " + 
                String.join(", ", errors));
        }
    }
    
    // Configuration classes with validation
}
```

### 2. Four-Bot Discord Architecture
**Personality-Driven Bot Design**
- **Security Bard**: Authoritative, manual-only responses, moderation focus
- **Flora**: Enthusiastic, LLM-enhanced, celebration and rewards focus
- **May**: Professional, communication hub, cross-platform messaging
- **Librarian**: Scholarly, AI-integrated, knowledge and education focus

**Bot Implementation Pattern:**
```java
public abstract class BotPersonality {
    protected final PersonalityConfig config;
    protected final MessageRouter messageRouter;
    
    public abstract CompletableFuture<BotResponse> processMessage(IncomingMessage message);
    public abstract boolean shouldAutoRespond(MessageTrigger trigger);
    public abstract String getPersonalityTone();
    
    protected CompletableFuture<String> generateLLMResponse(String prompt) {
        if (!config.isLlmEnabled()) {
            return CompletableFuture.completedFuture(null);
        }
        
        return llmService.generateResponse(prompt, getPersonalityTone());
    }
}

@Component
public class FloraBot extends BotPersonality {
    @Override
    public CompletableFuture<BotResponse> processMessage(IncomingMessage message) {
        // Flora's enthusiastic, celebration-focused response logic
    }
}
```

### 3. Data Persistence Strategy
**Hybrid Cache-Database Architecture**
- **Redis**: Hot data, session state, temporary caches
- **MariaDB**: Persistent data, historical records, configuration
- **Failover**: Redis outage falls back to MariaDB hot channels
- **Consistency**: Write-through cache for critical data
- **Performance**: Batch operations where possible

**Data Layer Pattern:**
```java
@Repository
public class PlayerDataRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CircuitBreaker databaseCircuitBreaker;
    
    public CompletableFuture<Optional<PlayerData>> findPlayerData(UUID playerId) {
        String cacheKey = "player:data:" + playerId;
        
        // Try cache first
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlayerData cached = (PlayerData) redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    return Optional.of(cached);
                }
            } catch (Exception e) {
                logger.warn("Redis cache miss for player {}: {}", playerId, e.getMessage());
            }
            
            // Fallback to database with circuit breaker
            return databaseCircuitBreaker.executeSupplier(() -> {
                PlayerData data = jdbcTemplate.queryForObject(
                    "SELECT * FROM player_data WHERE player_id = ?",
                    new PlayerDataRowMapper(),
                    playerId
                );
                
                // Update cache if database read successful
                if (data != null) {
                    try {
                        redisTemplate.opsForValue().set(cacheKey, data, Duration.ofHours(1));
                    } catch (Exception e) {
                        logger.warn("Failed to update cache for player {}", playerId);
                    }
                }
                
                return Optional.ofNullable(data);
            }).orElse(Optional.empty());
        });
    }
}
```

### 4. Event System Architecture
**Async Event Processing with Typed Events**
- Event bus for internal communication
- Strongly typed event classes
- Async event processing with CompletableFuture
- Event priority and cancellation support
- Comprehensive event logging

**Event System Pattern:**
```java
public class VeloctopusEventBus {
    private final Map<Class<? extends VeloctopusEvent>, List<EventListener<?>>> listeners = 
        new ConcurrentHashMap<>();
    private final ExecutorService eventExecutor = 
        Executors.newFixedThreadPool(8, r -> new Thread(r, "Veloctopus-Event-Thread"));
    
    public <T extends VeloctopusEvent> CompletableFuture<T> fireEvent(T event) {
        return CompletableFuture.supplyAsync(() -> {
            List<EventListener<?>> eventListeners = listeners.get(event.getClass());
            if (eventListeners == null) {
                return event;
            }
            
            for (EventListener<?> listener : eventListeners) {
                if (event.isCancelled()) {
                    break;
                }
                
                try {
                    @SuppressWarnings("unchecked")
                    EventListener<T> typedListener = (EventListener<T>) listener;
                    typedListener.onEvent(event);
                } catch (Exception e) {
                    logger.error("Event listener failed for event: {}", event.getClass().getSimpleName(), e);
                }
            }
            
            return event;
        }, eventExecutor);
    }
}
```

## Testing and Validation Standards

### 1. Testing Strategy
**Comprehensive Test Coverage**
- **Unit Tests**: All public methods and critical private methods
- **Integration Tests**: Cross-component interactions
- **Performance Tests**: Load testing for concurrent operations
- **Chaos Testing**: Component failure scenarios
- **End-to-End Tests**: Complete user workflows

**Test Structure:**
```java
@ExtendWith(MockitoExtension.class)
class ChatManagerTest {
    @Mock private DatabaseService databaseService;
    @Mock private RedisService redisService;
    @Mock private DiscordService discordService;
    
    @InjectMocks private ChatManager chatManager;
    
    @Test
    @DisplayName("Should successfully send global chat message with database and Discord integration")
    void shouldSendGlobalChatMessage() {
        // Given
        ChatMessage message = ChatMessage.builder()
            .sender(TestData.createPlayer("TestPlayer"))
            .content("Test message content")
            .channel(ChatChannel.GLOBAL)
            .build();
            
        when(databaseService.saveMessage(any())).thenReturn(CompletableFuture.completedFuture(true));
        when(discordService.sendMessage(any())).thenReturn(CompletableFuture.completedFuture(true));
        
        // When
        CompletableFuture<SendResult> result = chatManager.sendMessage(message);
        
        // Then
        assertThat(result.join().isSuccess()).isTrue();
        verify(databaseService).saveMessage(message);
        verify(discordService).sendMessage(any(DiscordMessage.class));
    }
    
    @Test
    @DisplayName("Should gracefully handle database failure and continue with Discord-only mode")
    void shouldHandleDatabaseFailureGracefully() {
        // Test graceful degradation scenarios
    }
}
```

### 2. Performance Validation
**Measurable Performance Standards**
- Chat message latency < 100ms end-to-end
- Memory usage < 512MB under normal load
- Startup time < 30 seconds with all modules
- Zero main thread blocking operations
- Database connection pool efficiency > 90%

**Performance Monitoring:**
```java
@Component
public class PerformanceMonitor {
    private final MeterRegistry meterRegistry;
    private final Timer chatLatencyTimer;
    private final Counter messagesSentCounter;
    private final Gauge memoryUsageGauge;
    
    public void recordChatMessageLatency(Duration latency) {
        chatLatencyTimer.record(latency);
        
        if (latency.toMillis() > 100) {
            logger.warn("Chat message latency exceeded threshold: {}ms", latency.toMillis());
        }
    }
    
    @EventListener
    public void onChatMessage(ChatMessageSentEvent event) {
        messagesSentCounter.increment();
        recordChatMessageLatency(event.getProcessingTime());
    }
}
```

### 3. Code Review Standards
**AI-Assisted Code Review Process**
- Automated code analysis before review
- Documentation completeness validation
- Performance impact assessment
- Security vulnerability scanning
- Architecture compliance checking

## Deployment and Operations Standards

### 1. Configuration Management
**Environment-Specific Configuration**
- Development, staging, production configurations
- Secure credential management
- Feature flags for gradual rollout
- Monitoring and alerting configuration
- Backup and recovery procedures

### 2. Logging and Monitoring
**Comprehensive Observability**
- Structured logging with correlation IDs
- Performance metrics collection
- Error rate monitoring
- Resource usage tracking
- Business metrics (messages sent, users active, etc.)

**Logging Pattern:**
```java
@Slf4j
public class VeloctopusLogger {
    private static final String CORRELATION_ID = "correlationId";
    
    public void logChatMessage(String correlationId, ChatMessage message, String action) {
        try (MDCCloseable mdcCloseable = MDC.putCloseable(CORRELATION_ID, correlationId)) {
            logger.info("Chat action: {} - Player: {} - Channel: {} - Content length: {}", 
                action, 
                message.getSender().getName(), 
                message.getChannel(), 
                message.getContent().length());
        }
    }
    
    public void logPerformanceMetric(String operation, Duration duration, boolean success) {
        try (MDCCloseable mdcCloseable = MDC.putCloseable(CORRELATION_ID, UUID.randomUUID().toString())) {
            if (success) {
                logger.info("Operation completed: {} - Duration: {}ms", operation, duration.toMillis());
            } else {
                logger.error("Operation failed: {} - Duration: {}ms", operation, duration.toMillis());
            }
        }
    }
}
```

### 3. Graceful Degradation Strategies
**System Resilience Patterns**
- Circuit breaker for external services
- Retry logic with exponential backoff
- Fallback mechanisms for service failures
- Health checks for all components
- Automated recovery procedures

## Security Standards

### 1. Input Validation
**Comprehensive Input Sanitization**
- All user input validated and sanitized
- SQL injection prevention
- Command injection prevention
- XSS prevention in Discord messages
- Rate limiting on all user actions

### 2. Authentication and Authorization
**Multi-Layer Security**
- Discord OAuth integration
- Minecraft player verification
- Role-based access control
- Session management
- Audit logging for all privileged actions

### 3. Data Protection
**Privacy and Security Compliance**
- Minimal data collection
- Secure data storage
- Data retention policies
- GDPR compliance considerations
- Secure communication channels

This comprehensive guide ensures that AI-assisted development of VelocityCommAPI maintains the highest standards of quality, performance, and reliability while leveraging the efficiency benefits of automated code generation.
