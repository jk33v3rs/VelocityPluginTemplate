# Veloctopus Rising

[![Discord](https://img.shields.io/discord/899740810956910683?color=7289da&label=Discord)](https://discord.gg/5NMMzK5mAn)

**Communication and Translation Hub for Multi-Platform Communities**

Veloctopus Rising is a comprehensive communication and translation API that serves as the central nervous system for multi-platform gaming communities. Built as a Velocity proxy plugin, it seamlessly connects Minecraft servers, Discord bots, AI tools, Matrix bridges, Redis caching, and MariaDB persistence through a unified, concurrent architecture.

## Features

### Core Architecture
- **Event-Driven Design** - All components communicate via events
- **Async-First** - Zero main thread blocking operations  
- **Modular Structure** - Independent, testable modules
- **Hot-Reload Support** - Configuration changes without restart

### Key Systems
- **Multi-Platform Messaging** - Minecraft ↔ Discord ↔ Matrix
- **VeloctopusProject Compatibility** - Exact rank and whitelist workflow
- **Four Discord Bot Personalities** - Specialized bot functions
- **175-Rank System** - 25 main ranks × 7 sub-ranks
- **4000-Endpoint XP System** - Community-weighted progression
- **AI Integration** - VelemonAId Python bridge support
- **Redis Caching** - High-performance data layer
- **MariaDB Persistence** - Cross-continental SQL support

### Bot Personalities
- **Security Bard** - Law enforcement, moderation, security monitoring
- **Flora** - Celebration, rewards, achievements, LLM-enhanced interactions
- **May** - Communication hub, global chat bridge, status monitoring
- **Librarian** - Knowledge management, AI queries, wiki integration

## Installation
- Download Veloctopus Rising from releases
- Place in your Velocity proxy plugins folder
- Configure in `config/VeloctopusRising.yml`
- Start the proxy

## Documentation
Complete documentation is available in the `docs/` directory:
- [00-PROJECT-OVERVIEW.md](docs/00-PROJECT-OVERVIEW.md) - High-level architecture
- [01-DEVELOPMENT-PLAN.md](docs/01-DEVELOPMENT-PLAN.md) - Development roadmap
- [02-CONFIGURATION-KEYS.md](docs/02-CONFIGURATION-KEYS.md) - All configuration options
- [03-JAVADOC-STANDARDS.md](docs/03-JAVADOC-STANDARDS.md) - API documentation
- [04-AI-GUIDELINES.md](docs/04-AI-GUIDELINES.md) - AI development guidelines
- [05-DEVELOPMENT-CHECKLIST.md](docs/05-DEVELOPMENT-CHECKLIST.md) - Progress tracking
