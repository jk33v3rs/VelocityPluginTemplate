# Veloctopus Rising - Development Plan

## Development Phases Overview

### Phase 1: Foundation & Core Infrastructure (Hours 1-4)
**Goal**: Establish the core API, basic chat system, and essential infrastructure

### Phase 2: Integration & Features (Hours 5-8)
**Goal**: Add Discord integration, whitelisting, and advanced features

### Phase 3: Ecosystem & Polish (Hours 9-12)
**Goal**: Complete the ecosystem with AI bridges, Matrix, and optimization

---

## Phase 1: Foundation & Core Infrastructure

### Hours 1-4: Project Structure & Core API

#### Hour 1-2: Project Foundation
1. **Module Structure Setup**
   - Create all module directories per these docs and user instructions
   - Set up core API interfaces and event system
   - Configure Gradle build scripts with proper dependencies
   - Establish logging framework and configuration system

2. **Core Event System**
   - Design and implement central EventBus (inspired by Velocity's native events)
   - Create base Event classes for all communication types
   - Implement async event handling with CompletableFuture
   - Add event priority and cancellation support

3. **Configuration Framework**
   - Single YAML configuration file structure
   - Hot-reload capability for configuration changes
   - Validation system for configuration values
   - Environment-specific configuration overlays

#### Hour 3-4: Message Translation Engine
1. **Message Format Abstraction**
   - Common message format for internal routing
   - Conversion interfaces for different platforms (Minecraft, Discord, Matrix)
   - Support for rich text, embeds, and attachments
   - Message metadata handling (sender, timestamp, channel)

2. **Connection Pool Framework**
   - Redis connection pool with health monitoring
   - MariaDB connection pool with failover support
   - Connection lifecycle management
   - Circuit breaker pattern implementation

#### Hour 5-6: Basic Storage Layer
1. **Data Access Layer**
   - Repository pattern for all data operations
   - Async-first database operations
   - Connection pool integration
   - Migration system for database schema

2. **Caching Strategy**
   - Redis cache abstraction layer
   - Cache invalidation strategies
   - Fallback to database when cache unavailable
   - Cache warming for frequently accessed data

### Hours 7 to 10: Chat System Foundation

#### Hours 7-8: Core Chat Infrastructure
1. **Chat Message Model**
   - Message entity with sender, content, metadata
   - Channel abstraction (server-specific, global, private)
   - Message routing logic between servers
   - Rate limiting and anti-spam measures

2. **HuskChat-Inspired Architecture**
   - Use as much of HuskChat's proxy-only implementation as possible
   - Adapt cross-server chat bridging logic
   - Implement channel management system
   - Add permission-based channel access

#### Hours 9-10: Velocity Integration
1. **Velocity Event Handlers**
   - PlayerChatEvent processing
   - Server switching event handling
   - Permission integration points
   - Command registration system

2. **Adventure Component Integration**
   - MiniMessage format support
   - Rich text rendering
   - Click/hover event handling
   - Placeholder integration

#### Hours 11-12: Basic Discord Bridge
1. **JDA Foundation Setup**
   - Spicord's bot instanceds in quadrouplicate for testing
   - Slash commands & aLL message bridging (Minecraft <-> Discord)
   - Channel mapping configuration
   - Error handling and reconnection logic

### 2nd Half of Day: Whitelist System

#### Hours 12-13: VeloctopusProject Analysis & Adaptation - Complete Whitelist Workflow Implementation
1. **Discord-to-Minecraft Verification Pipeline**
   - **Discord command implementation**: `/mc <playername>` command with comprehensive input validation and Minecraft username format checking
   - **Mojang API integration**: Real-time username verification with rate limiting, retry logic, and caching for 24-hour periods
   - **Geyser/Floodgate support**: Intelligent prefix detection and stripping (default "." prefix) for Bedrock Edition players while preserving display prefix
   - **10-minute verification window**: Precise timer management with countdown warnings at 8-minute, 2-minute, and 30-second marks
   - **State persistence**: Redis-backed verification state with MariaDB fallback ensuring no verification loss during system restarts

2. **Purgatory State Management - Hub-Only Quarantine System**
   - **Server restriction enforcement**: Block all server transfers except to designated hub server during purgatory period
   - **Adventure mode quarantine**: Force adventure mode for first 5 minutes with gradual permission restoration
   - **Allowed command whitelist**: Limited command access (`/spawn`, `/help`, `/rules`, `/discord`, `/verify`) during quarantine period
   - **Movement and interaction restrictions**: Prevent item pickup, block interaction, entity interaction with configurable exemptions
   - **State transition logic**: Automatic progression from purgatory → verified → member status with comprehensive audit logging

#### Hours 14-15: Database Schema
1. **Player State Tracking**
   - Verification status tracking
   - Purgatory timer management
   - Member status progression
   - Audit logging for security

#### Hours 16-17: Integration Testing
1. **End-to-End Verification Flow**
   - Discord command -> Velocity permission update
   - State persistence across proxy restarts
   - Error handling for edge cases
   - Performance optimization

### Hours 18-19: Rank System Foundation

#### Hour 18: VeloctopusProject Rank System - EXACT Implementation (25 Main Ranks × 7 Sub-Ranks = 175 Total Combinations)
1. **Complete 25-Rank Hierarchy Implementation**
   - **EXACT rank structure implementation from VeloctopusProject**: 25 main ranks (bystander → deity) with progressive XP requirements
   - **7 Sub-rank progression system**: novice, apprentice, journeyman, expert, master, grandmaster, immortal with XP multipliers
   - **Progressive permission escalation**: Each rank tier grants specific capabilities from basic chat to full administrative control
   - **Dynamic rank display formatting**: Color-coded rank prefixes with sub-rank suffixes (e.g., `<green>Guardian</green> ★★` for Grandmaster Guardian)
   - **175 total rank combinations**: Mathematical certainty - 25 main × 7 sub = 175 possible rank states
   
2. **XP-Based Progression Logic Implementation**
   - **Base XP requirements**: Starting at 0 (Bystander) scaling to 1,000,000 XP (Deity) with exponential curve progression
   - **Sub-rank multiplier formula**: base_xp × (1.1 ^ sub_rank_level) for internal progression within each main rank
   - **Automatic promotion system**: Real-time XP monitoring with immediate rank advancement upon threshold achievement
   - **XP source integration**: Chat activity, playtime, community contribution, peer recognition weighted according to VeloctopusProject specifications
   - **Community contribution emphasis**: 60% of optimal progression comes from community engagement, 40% from individual achievement
   
3. **Rank Management Command Suite**
   - **Player progression commands**: `/rank check [player]`, `/rank progress`, `/rank leaderboard` with detailed XP breakdown and next-rank requirements
   - **Administrative tools**: `/rank set <player> <rank> [surank]`, `/rank add-xp <player> <amount>`, `/rank audit <player>` with comprehensive logging
   - **Bulk operation support**: `/rank promote-eligible`, `/rank recalculate-all` for mass rank updates and maintenance
   - **Permission validation**: Automatic permission node assignment and removal based on rank progression
   - **Discord role synchronization**: Real-time Discord role updates reflecting in-game rank changes with configurable role mapping 

#### Hour 20-21: Phase 1 Integration Testing
1. **System Integration**
   - Chat + Whitelist + Ranks working together
   - Performance testing under load
   - Memory usage optimization
   - Bug fixes and stability improvements

---

## Phase 2: Integration & Features

### Day 2: Multi-Bot Discord System

#### Hours 1-3: Spicord-Inspired Multi-Bot Architecture - Four Specialized Discord Bots
1. **Four-Bot Implementation with Distinct Personalities**
   - **Security Bard configuration**: Authoritative moderation bot with manual-only responses, comprehensive audit logging, and direct Velocity integration for immediate enforcement
   - **Flora reward system integration**: Semi-automated celebration bot with LLM enhancement (qwen2.5-coder-14b), timer-based community engagement, and promotional campaign management
   - **May communication hub setup**: Professional cross-platform message router with HuskChat-style bridging, server status monitoring, and social media integration
   - **Librarian AI bridge preparation**: Scholarly knowledge bot with VelemonAId backend integration, wiki management capabilities, and educational content generation

2. **Bot Role Separation and Coordination**
   - **Command routing by bot function**: Intelligent message distribution based on content type, user intent, and command context
   - **Separate permission systems**: Independent Discord role management for each bot with cross-bot coordination protocols
   - **Event distribution logic**: Centralized event bus with bot-specific filtering and priority handling
   - **Cross-bot communication protocols**: Internal messaging system for coordination without user-visible interaction

#### Hours 4-7: Discord Feature Enhancement - DiscordSRV-Quality Aesthetic Integration
1. **Rich Embed System Implementation**
   - **DiscordSRV-inspired beautiful formatting**: Professional embedded message templates with dynamic color schemes per bot personality
   - **Custom embed templates**: Configurable templates for different message types (join/leave, achievements, rank promotions, moderation actions)
   - **Dynamic content generation**: Real-time embed population with player statistics, server status, and contextual information
   - **Interactive component support**: Button-based interfaces for moderation actions, reaction-based voting systems, select menus for server navigation
   - **Attachment handling and media integration**: Image upload support for player builds, automated screenshot generation for achievements

2. **Bot-Specific Communication Enhancement**
   - **Security Bard professional responses**: Formal moderation embeds with severity levels, violation history, and appeal procedures
   - **Flora celebration embeds**: Vibrant, animated-style embeds with congratulatory messages, achievement artwork, and social sharing features
   - **May status monitoring embeds**: Clean, informational server status displays with real-time metrics and performance graphs
   - **Librarian educational embeds**: Structured knowledge presentations with categorized information, helpful links, and follow-up suggestions

### Hours 8-10: XP System - VeloctopusProject 4000-Endpoint Achievement Architecture

#### Hours 8-9: XP Tracking Implementation - Community-First Progression
1. **Dual-Track Activity Detection System**
   - **Individual achievement tracking**: Chat activity (1 XP/message with 60s cooldown), playtime (2 XP/minute with AFK detection), block interaction, mob kills, advancement unlocks
   - **Community contribution emphasis**: 60% of optimal progression from community engagement including mentoring new players, teaching sessions, conflict resolution, helpful building projects
   - **Peer recognition system**: Daily nominations (3 per player), weekly peer votes (10 per player), community validation requirements with 48-hour timeout
   - **Event-based XP awards**: Community events (25-75 XP), leadership bonuses (1.5× multiplier), special occasion multipliers (weekends 1.5×, holidays 2.0×)
   - **Anti-gaming protection**: Rate limiting, maximum XP caps (5000/day, 25000/week, 75000/month), quality-based bonus multipliers

2. **XP Storage and Calculation - High-Performance Architecture**
   - **Efficient XP storage in MariaDB**: Denormalized player XP tables with indexed lookups, batch update procedures, and transaction safety
   - **Real-time XP calculation**: Redis-cached current XP with write-through persistence, sub-second calculation response times
   - **Progressive rank calculation**: Dynamic next-rank progress tracking with percentage completion and estimated time to promotion
   - **Leaderboard generation**: Cached ranking tables with hourly updates, seasonal leaderboards, and category-specific rankings (individual vs community contribution)
   - **Historical XP tracking**: Complete XP gain history with source attribution, daily/weekly/monthly summaries, and trend analysis

#### Hour 10: Flora Integration - Celebration and Recognition Automation
1. **Reward Notifications and Celebration Management**
   - **XP milestone celebrations**: Automated Flora announcements for major XP achievements (every 10,000 XP) with personalized congratulation messages
   - **Rank promotion announcements**: Dramatic celebration embeds with promotion graphics, permission unlock notifications, and community recognition
   - **Achievement unlock notifications**: Real-time achievement tracking with custom celebration messages for each of the 4000+ unique achievements in VeloctopusProject system
   - **Peer recognition celebrations**: Flora-hosted recognition ceremonies for community nominations and peer validation achievements
   - **Special event rewards**: Holiday bonuses, community milestone celebrations, and anniversary recognition with enhanced Flora enthusiasm

2. **LLM-Enhanced Celebration System**
   - **Contextual celebration generation**: AI-powered personalized celebration messages based on achievement type, player history, and community impact
   - **Daily conversation starters**: Timer-triggered community engagement prompts with Flora's enthusiastic personality (configurable intervals)
   - **Administrative celebration tools**: `/floranews <message>` for major announcements, `/floraannounce <channel> <message>` for promotional embeds
   - **AI-assisted responses**: `/florasay <channel> <llm_prompt>` for custom Flora responses with personality-consistent tone and enthusiasm level

### Hour 11: Command System

#### Hour 12: Unified Command Framework
1. **Brigadier Integration**
   - Command tree structure
   - Permission-based command access
   - Tab completion support
   - Error handling and user feedback

2. **Discord Slash Commands**
   - Mirror Minecraft commands in Discord
   - Context-aware command availability
   - Permission synchronization
   - Response formatting

#### Hours 13-14: Advanced Commands
1. **Administrative Commands**
   - Player management commands
   - System status commands
   - Configuration reload commands
   - Debugging and diagnostics

### Hours 15-18: Permission System (VaultUnlocked-Inspired)

#### Hour 15: Permission Framework
1. **Permission Node System**
   - Hierarchical permission structure
   - Group-based permissions
   - User-specific overrides
   - Inheritance and negation

2. **Storage and Caching**
   - MariaDB permission storage
   - Redis permission caching
   - Real-time permission updates
   - Cross-server synchronization

#### Hour 16: Integration Testing
1. **System-Wide Permission Integration**
   - Chat command permissions
   - Discord command permissions
   - Feature access control
   - Performance optimization

---

## Phase 3: Ecosystem & Polish

### Hour 17: AI Integration Bridge

#### Hour 17-18: Python Bridge Implementation
1. **VelemonAId Integration**
   - HTTP API for AI communication
   - Message queue for async processing
   - Authentication and security
   - Error handling and timeouts

2. **Librarian Bot Enhancement**
   - Wiki query integration
   - AI-powered responses
   - Knowledge base search
   - Response formatting

#### Hour 19: AI Feature Integration
1. **Smart Features**
   - Auto-moderation suggestions
   - Player assistance automation
   - Content generation helpers
   - Analytics and insights

### Hour 20-22: Matrix/Matterbridge Integration

#### Hour 20: Matrix Bridge Implementation
1. **Matrix Protocol Integration**
   - Matrix SDK integration
   - Room management
   - Message bridging
   - User mapping

2. #### Hour 21: **Matterbridge Compatibility**
   - Matterbridge API integration
   - Multi-platform message routing
   - Format preservation
   - Error handling

#### Hour 22: Cross-Platform Testing
1. **Multi-Platform Communication**
   - Minecraft <-> Discord <-> Matrix
   - Message format consistency
   - Feature parity across platforms
   - Performance optimization

### Hour 23-24, Day 2: Economy Integration

#### Hour 23: TheNewEconomy Integration
1. **Economy API Integration**
   - Balance checking and modification
   - Transaction logging
   - Economy event handling
   - Multi-currency support

2. **QuickShop Integration**
   - Shop creation and management
   - Transaction notifications
   - Inventory synchronization
   - Price tracking

#### Hour 24: Economy Features
1. **Advanced Economy Features**
   - Discord balance checking
   - Cross-server transactions
   - Economic analytics
   - Fraud protection

### Day 3: Polish & Optimization

#### Hour 1-3: Performance Optimization
1. **System Performance**
   - Database query optimization
   - Cache hit ratio improvement
   - Memory usage optimization
   - Thread pool tuning

2. **Monitoring and Metrics**
   - Performance metrics collection
   - Health check endpoints
   - Alert system for issues
   - Diagnostic tools

#### Hours 4-7: Final Testing & Documentation
1. **Comprehensive Testing**
   - Load testing with 1000+ players
   - Failure scenario testing
   - Security penetration testing
   - User acceptance testing

2. **Documentation Completion**
   - Administrator setup guide
   - User manual
   - API documentation
   - Troubleshooting guide

---

## Development Standards & Practices

### Code Review Process
1. **Feature Branch Development** - All features developed in separate branches
2. **Peer Review Required** - All code must be reviewed before merging
3. **Automated Testing** - Unit tests required for all new functionality
4. **Integration Testing** - End-to-end tests for critical workflows
5. **Take As Much Working Code As You Can**: We're here for a working plugin, not status. Use the open source licencing to its advantage. 

### Quality Gates
1. **Performance Benchmarks** - All features must meet performance requirements
2. **Memory Usage Limits** - Memory usage must stay within defined bounds
3. **Error Handling Coverage** - All error scenarios must be handled gracefully
4. **Documentation Requirements** - All public APIs must be documented

### Risk Mitigation
1. **External Dependency Monitoring** - Health checks for all external services
2. **Graceful Degradation** - System must function when subsystems are unavailable
3. **Data Backup Strategy** - Regular automated backups of all critical data
4. **Security Review** - Regular security audits and vulnerability assessments

This development plan ensures systematic progress toward a production-ready system while maintaining code quality and reliability throughout the development process.
