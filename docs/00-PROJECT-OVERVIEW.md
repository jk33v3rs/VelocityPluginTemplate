# Veloctopus Rising - Project Overview (1000ft View)

## Vision Statement
Veloctopus Rising is a lightweight, high-performance communication and translation API designed to serve as the central nervous system for a multi-platform gaming community. Built as a Velocity proxy plugin, it seamlessly connects Minecraft servers, Discord bots, AI tools, Matrix bridges, Redis caching, and MariaDB persistence through a unified, concurrent architecture.

## Core Philosophy
- **Lightweight First**: Every component must justify its existence - no bloat
- **Concurrency Native**: Built from the ground up for multi-threaded, non-blocking operations
- **Minimal External Dependencies**: Self-contained where possible, strategic dependencies only
- **Clear Interface Contracts**: Well-defined APIs between all components
- **Translation Layer Focus**: Convert between different communication protocols seamlessly

## High-Level Architecture

### Communication Hub Model
```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                Veloctopus Core                              │
├─────────────────────────────────────────────────────────────────────────────┤
│  Event Bus │ Message Router │ Translation Engine │ Cache | Player Handler   │
└─────────────────────────────────────────────────────────────────────────────┘   
     ↕              ↕               ↕                 ↕                ↕
┌─────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌────────────────────┐
│Brigadier│  │   Discord   │  │   Discord   │  │    Redis    │  |  Multi - Currency  |
│Listener │  │ JDA (4 Bots)│  │  JDA (Bots) │  │    Cache    │  | Serverwide Economy |
└─────────┘  └─────────────┘  └─────────────┘  └─────────────┘  └────────────────────┘
     ↕              ↕                ↕                ↕                ↕
┌─────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  Global │  │ Whitelist   │  │   Matrix &  │  │   MariaDB   │
│Game Chat|  │   System    │  │ MatterBridge│  │ Persistence │
└─────────┘  └─────────────┘  └─────────────┘  └─────────────┘
     ↕              ↕                ↕                ↕                ↕
┌─────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
|Universal|  | Role+Perms  |  |  Python AI  |  |Backup, Log &|
|XP+Ranks |  | Manager     |  |   Bridge    |  |Update Runner|  
└─────────┘  └─────────────┘  └─────────────┘  └─────────────┘
```

## Four Discord Bot Personas - Specialized Communication Architecture

### 1. Security Bard - Authority & Law Enforcement (Authoritative Moderation Bot)
- **Primary Role**: Comprehensive moderation, rule enforcement, security monitoring, and community discipline management
- **Personality Profile**: Stern 6'11" rugby front-rower archetype - authoritative yet fair, firm but not cruel, commanding respect through competence
- **Core Responsibilities**: 
  - Ban, kick, mute, and warning management with full audit trails
  - Security event monitoring and real-time threat response
  - Rule violation detection and enforcement procedures
  - Appeal processing and moderation review workflows
  - Cross-platform security coordination (Discord + Minecraft integration)
- **Integration Architecture**: Direct Velocity proxy integration for immediate enforcement actions, LuckPerms synchronization for permission management, comprehensive security logging with correlation IDs
- **Response Characteristics**: Manual-only responses (no LLM integration), professional and authoritative tone, detailed violation explanations with clear remediation steps

### 2. Flora - Rewards & Celebration (Semi-Automated Mascot & Marketing Bot)
- **Primary Role**: Achievement celebration, rank promotions, positive reinforcement, community engagement, and promotional activities
- **Personality Profile**: Energetic manga-trope mascot with boundless enthusiasm - all smiles, peace signs, and sickly-sweet positivity designed to boost community morale
- **Core Responsibilities**:
  - XP milestone celebrations and rank promotion announcements with custom embedded messages
  - Achievement unlock notifications with personalized congratulations
  - Daily conversation starters and community engagement prompts
  - Marketing and promotional campaign management (/floranews, /floraannounce commands)
  - Event celebration coordination and prize distribution management
- **Advanced LLM Integration**: 
  - Triggered responses using 1B-parameter models (qwen2.5-coder-14b preference) for organic conversation
  - Timer-based daily engagement with community-appropriate prompts
  - Event-specific celebration enhancement with contextual enthusiasm
  - `/florasay <channel> <llm_prompt>` command for administrative-triggered AI responses
- **Integration Architecture**: Deep XP system integration, rank progression monitoring, achievement tracking system, promotional embed generation with configurable channel targeting

### 3. May - Communications Hub (Cross-Platform Message Router)
- **Primary Role**: Cross-platform message routing, server status monitoring, connectivity management, and social media integration
- **Personality Profile**: Reliable, professional, no-nonsense efficiency expert - proud of her hard work connecting all corners of the network, serious but not unfriendly
- **Core Responsibilities**:
  - HuskChat-style global chat bridging between Minecraft servers and Discord
  - Server uptime monitoring with intelligent status reporting (up/down alerts, performance metrics)
  - Cross-platform message routing with format preservation and attachment support
  - Social media feed monitoring and cross-posting (Twitter, Reddit, YouTube, Twitch integration)
  - Join/leave notifications and player connectivity status reporting
- **Communication Infrastructure**: 
  - Bidirectional chat bridge with advanced message filtering and rate limiting
  - Server performance monitoring with automated alerts and status dashboards
  - Social media webhook integration with intelligent content filtering
  - Matrix and Matterbridge compatibility preparation for future "galactic" chat expansion
- **Integration Architecture**: Velocity server event hooks, Redis pub/sub for real-time messaging, social media API integration, comprehensive message logging and analytics

### 4. ArchiveSMP Librarian - Knowledge & Research (AI-Enhanced Documentation Bot)
- **Primary Role**: Wiki management, player education, information retrieval, technical documentation, and community knowledge building
- **Personality Profile**: Scholarly, helpful, information-focused academic - nerdy enthusiasm for learning combined with exceptional ability to simplify complex topics for accessibility
- **Core Responsibilities**:
  - Dynamic wiki article generation and maintenance using AI assistance
  - Player query assistance with intelligent information retrieval
  - Command help system with contextual documentation
  - Technical update interpretation and community education
  - Beginner-friendly tutorials and educational content creation
- **Advanced AI Integration**:
  - VelemonAId backend integration for wiki RAG (Retrieval-Augmented Generation) system
  - Intelligent knowledge synthesis from multiple sources
  - Technical documentation generation with accuracy validation
  - Community question answering with source attribution and confidence scoring
- **Technical Infrastructure**: 
  - Python AI bridge for seamless VelemonAId integration
  - MediaWiki RAG system for intelligent document retrieval
  - Knowledge base indexing and semantic search capabilities
  - Automated article generation with human review workflows 

## Technology Stack & Integration Architecture

### Core Platform & Runtime Environment
- **Velocity 3.4.0-SNAPSHOT** - High-performance proxy server platform with native async architecture
- **Java 21 LTS** - Modern runtime environment with advanced concurrency primitives, virtual threads, and memory management improvements
- **Gradle Kotlin DSL** - Type-safe build system with advanced dependency management and plugin ecosystem
- **Adventure API 4.17.0** - Modern Minecraft text component system with comprehensive formatting and interaction support
- **Brigadier 1.2.9** - Advanced command framework with tree-structured commands and contextual auto-completion

### External Service Integrations & Communication Layer
- **JDA (Discord) 5.0.0** - Robust Discord bot framework supporting multiple concurrent bot instances with comprehensive API coverage
  - Four simultaneous bot instances with distinct personalities and responsibilities
  - Advanced embedded message support with interactive components
  - Comprehensive webhook integration and real-time event processing
- **Redis (Jedis 5.1.0)** - High-speed caching and pub/sub messaging system
  - Hot data caching with configurable TTL policies
  - Cross-server message routing and real-time synchronization
  - Session state management and temporary data storage
  - Circuit breaker pattern for Redis failures with MariaDB fallback
- **MariaDB (Connector 3.3.2 + HikariCP 5.1.0)** - Persistent data storage with enterprise-grade connection pooling
  - Player data persistence, whitelist management, rank progression tracking
  - Comprehensive audit logging and historical data retention
  - Cross-continental query optimization with connection pooling
  - Database migration system with automatic schema versioning
- **Python Bridge Integration** - AI tool connectivity layer
  - JEP (Java Embedded Python) 4.2.0 for in-process Python execution
  - Py4J 0.10.9.7 for robust inter-process communication
  - HTTP API integration for VelemonAId backend services
  - Asynchronous request handling with timeout and retry logic

### Internal Dependencies & Open Source Integration Strategy
**Spicord Architecture Foundation** - Multi-bot Discord integration patterns
- Proven multi-bot management system adapted for 4-bot architecture
- Event-driven bot coordination and message routing
- Configuration management and bot lifecycle handling
- Performance optimization patterns for concurrent bot operations

**HuskChat Communication Patterns** - Cross-server chat implementation excellence
- Proxy-only chat system architecture (no backend server plugins required)
- Advanced message routing and channel management
- Permission-based access control with integration points
- Rich text formatting and MiniMessage component support
- Rate limiting and anti-spam protection mechanisms

**VeloctopusProject Foundation** - Comprehensive whitelist workflow and rank system
- Discord-to-Minecraft verification pipeline with Mojang API integration
- 25 main ranks × 7 sub-ranks = 175 total rank combinations (EXACT implementation required)
- Purgatory state management with hub-only restrictions and adventure mode quarantine
- XP tracking and progression system with activity-based advancement
- Advanced permission system with rank-based privilege escalation

**DiscordSRV Aesthetic Integration** - Beautiful Discord message formatting
- Rich embedded message templates with dynamic content generation
- Advanced color schemes and formatting options
- Interactive component support (buttons, select menus, modals)
- Attachment handling and media integration capabilities

**Adrian3D Ecosystem Utilities** - Lightweight Velocity patterns and optimizations
- **VLobby**: Server hub management and player routing optimization
- **SignedVelocity**: Internal authentication and security patterns
- **EpicGuard**: Advanced anti-bot and security protection systems
- **VPacketEvents**: Proxy-level packet manipulation and optimization
- **ChatRegulator**: Advanced content filtering and moderation automation
- **KickRedirect**: Intelligent connection management and failover handling

**Permission and Economy Integration Foundations**
- **LuckPerms**: Advanced permission management integration (external dependency during Phase 1-2)
- **VaultUnlocked**: Permission system architecture patterns for internal implementation
- **TheNewEconomy EconomyCore**: Multi-currency economy system integration patterns
- **QuickShop-Hikari**: Comprehensive shop system with unified player accounts and cross-server inventory management

### Development and Build Infrastructure
- **ACF (Annotation Command Framework) 0.5.1-SNAPSHOT** - Annotation-driven command system with automatic help generation
- **Caffeine 3.1.8** - High-performance in-memory caching for frequently accessed data
- **Gson 2.10.1** - JSON serialization and configuration management
- **bStats 3.0.2** - Anonymous usage statistics and performance monitoring
- **Gradle Plugins**:
  - Shadow 8.3.3 - Fat JAR generation with dependency shading
  - Blossom 2.1.0 - Template processing and code generation
  - RunVelocity 2.3.1 - Development environment automation

### Cross-Platform Compatibility & Future Expansion
**Current Platform Support:**
- **Velocity Proxy** - Primary platform with full feature support
- **Paper/Spigot** - Backend server compatibility for future expansion
- **NeoForge/Fabric** - Mod platform support for client-side enhancements
- **Geyser/Floodgate** - Bedrock Edition cross-play support (mandatory feature)
- **ViaVersion/ViaBackwards** - Multi-version client support for legacy compatibility

**Future Integration Targets:**
- **Matrix Protocol** - Federated messaging for "galactic" chat expansion
- **Matterbridge** - Multi-platform bridge for Slack, Teams, IRC integration
- **Social Media APIs** - Twitter, Reddit, YouTube, Twitch monitoring and cross-posting
- **MediaWiki** - Comprehensive wiki integration with AI-powered content generation 

## Key Features

### Phase 1 - Foundation (MVP)
1. **Global Chat System** - HuskChat-inspired cross-server communication
2. **Whitelist System** - VeloctopusProject-inspired Discord verification
3. **Basic Rank System** - 32-rank hierarchy from VeloctopusProject
4. **Core API** - Internal event system and module communication

### Phase 2 - Enhancement
1. **XP System** - Activity tracking and progression
2. **Advanced Permissions** - VaultUnlocked-inspired permission management
3. **Command System** - Unified slash commands (Minecraft + Discord)
4. **AI Integration** - Python bridge for VelemonAId

### Phase 3 - Ecosystem & Advanced Features
1. **Matrix Bridge** - Mattermost/Matrix connectivity for "galactic" chat spanning Game + Discord + Matrix
2. **Economy Integration** - TheNewEconomy-compatible multi-currency system with cross-server synchronization
3. **Shop System** - QuickShop-Hikari-inspired serverwide ledger + inventory manager with unified player accounts per currency (servers defined by comma-separated list in config YAML, with addon functions built-in)
4. **Advanced Analytics** - Performance monitoring, player behavior analytics, and business intelligence dashboards
5. **Social Media Integration** - Automated monitoring and cross-posting from Twitter, Reddit, YouTube, Twitch
6. **Advanced AI Features** - Context-aware responses, automated moderation suggestions, community insights

## Performance Requirements & Architecture Constraints

### Concurrency Model - Multi-Threading Excellence
- **Main Thread Protection** - ZERO blocking operations on Velocity main thread (non-negotiable requirement)
- **Async-First Design** - CompletableFuture-based architecture throughout entire system, Pebble-template-based implementation for dynamic content
- **Thread Pool Management** - Dedicated thread pools for different operation types:
  - Database operations pool (5-10 threads)
  - Redis cache operations pool (3-5 threads)  
  - Discord API operations pool (4 threads, one per bot)
  - Python AI bridge pool (2-3 threads)
  - Event processing pool (8 threads matching CPU cores)
- **Connection Pooling** - Sophisticated Redis and MariaDB connection management with health monitoring, automatic failover, and connection lifecycle optimization

### Hardware Targets & Resource Management
- **Primary Infrastructure**: Single 8-core Zen 5 processor with 64GB RAM hosting entire game network (proxy + game servers + this plugin)
- **Network Architecture**: Cross-continental SQL queries with high latency expectation (500ms+ round-trip times to database server)
- **Scaling Requirements**: Support 1000+ concurrent players across multiple backend Minecraft servers
- **Memory Budget**: Plugin must operate within 512MB memory allocation under normal load
- **CPU Efficiency**: Maximum 15% CPU utilization under normal load, 40% maximum during peak activity

## Development Principles & Quality Standards

### Code Quality - Zero-Compromise Standards
- **Constantly Cycle Back To Refresh Context** - AI systems suffer from functional operational memory limitations; therefore MANDATORY return to documentation between every development step to re-learn project aims and current progress. Mark off completed items systematically, always complete one task at a time, in ordinal sequence, and in full completion before proceeding
- **Immutable Data Structures** - Prefer immutability wherever possible to prevent concurrent modification issues and improve thread safety
- **Interface Segregation** - Design small, focused interfaces with single responsibilities rather than monolithic service contracts
- **Dependency Injection** - Guice-based component management for clean separation of concerns and testability
- **Assembly First Methodology** - Adapt existing proven code or write new code such that ALL full features exist in working form before optimization. Complete features end-to-end before refinement

### Error Handling & System Resilience 
- **Development Reality Check** - Servers require measurable startup time in both testing and production environments; never expect instantaneous responses from complex operations or database queries spanning continents
- **Graceful Degradation Architecture** - System MUST continue operating when subsystems fail (e.g., Redis outage falls back to MariaDB hot channels - slower but functional)
- **Circuit Breaker Pattern Implementation** - Prevent cascade failures through intelligent failure detection and automatic service isolation
- **Comprehensive Structured Logging** - Every significant operation logged with correlation IDs, performance metrics, and contextual information for debugging
- **Proactive Health Checks** - Continuous monitoring of all external dependencies with automatic recovery procedures and alerting

## Success Metrics & Quality Gates

### Development Efficiency Requirements
- **Minimum 67% Borrowed Code** - Leverage existing open-source solutions wherever licensing permits. Focus on delivery of working systems, not reinventing proven patterns
- **Minimal Human Intervention Required** - Code must be "done in one" - complete, tested, and deployable without extensive manual debugging or refinement iterations
- **Zero Infinite Testing Loops** - All features planned and documented before implementation, with clear acceptance criteria and automated validation

### Performance Benchmarks (Non-Negotiable)
- **Chat Message Latency**: < 100ms end-to-end (Minecraft player -> processing -> Discord/Matrix delivery)
- **Main Thread Protection**: ZERO blocking operations on Velocity main thread (monitored and enforced)
- **Memory Efficiency**: < 512MB memory usage under normal load (1000 concurrent players)
- **Startup Performance**: < 30 seconds complete system initialization with all modules loaded and connected
- **Database Performance**: Connection pool efficiency > 90%, query response times logged and optimized
- **Redis Performance**: Cache hit ratio > 85%, fallback to database seamless and logged

### Reliability Standards (Mission-Critical)
- **AI Development Precision**: Zero coding mistakes through comprehensive planning, documentation-driven development, and systematic validation
- **System Uptime**: 99.9% availability for core communication features (chat, whitelist, Discord integration)
- **Automatic Recovery**: Intelligent failover mechanisms with recovery procedures (Redis outage → MariaDB hot channels as documented failover path)
- **Data Integrity**: Zero data loss under any circumstances - all critical operations must be transactional and logged
- **Network Resilience**: Graceful handling of network partitions, timeouts, and external service outages with appropriate user feedback

### Operational Excellence Requirements
- **Single Configuration Management**: One YAML file (`config/Veloctopus.yml`) controls all system settings with comprehensive validation
- **Hot-Reload Capability**: Configuration changes applied without service restart for all non-critical settings
- **Error Communication**: Clear, actionable error messages with troubleshooting guidance and resolution steps
- **Administrative Efficiency**: Minimal ongoing administrative overhead through automation and intelligent defaults
- **Monitoring Integration**: Built-in performance metrics, health dashboards, and proactive alerting systems

This overview provides the strategic foundation for all subsequent implementation decisions and ensures alignment with the core vision throughout development.
