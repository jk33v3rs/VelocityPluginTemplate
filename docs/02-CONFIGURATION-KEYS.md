# Veloctopus - Configuration Keys Reference

## Overview
This document provides a comprehensive map of all configuration keys required for Veloctopus. The system uses a single YAML configuration file with clearly defined sections for each component.

## Main Configuration File Structure
**File**: `config/Veloctopus.yml`

```yaml
# Veloctopus Configuration
# Version: 1.0.0

# Global Settings
global:
  version: 1.0.0
  debug_mode: false
  log_level: INFO
  startup_delay_seconds: 5
  shutdown_timeout_seconds: 30

# Core API Configuration
api:
  # Event system configuration
  event_bus:
    thread_pool_size: 8
    max_queue_size: 10000
    event_timeout_seconds: 30
  
  # Message translation engine
  translation:
    max_message_length: 4096
    rate_limit_per_minute: 60
    format_validation: true
    emoji_support: true

# Database Configuration
database:
  # MariaDB Primary Database
  mariadb:
    host: localhost
    port: 3306
    database: Veloctopus
    username: Veloctopus
    password: changeme
    
    # Connection Pool Settings
    connection_pool:
      minimum_connections: 5
      maximum_connections: 20
      connection_timeout_ms: 30000
      idle_timeout_ms: 600000
      max_lifetime_ms: 1800000
    
    # Performance Settings
    performance:
      use_ssl: false
      timezone: UTC
      charset: utf8mb4
      
  # Migration Settings
  migrations:
    auto_migrate: true
    backup_before_migrate: true
    migration_table: schema_migrations

# Redis Cache Configuration
cache:
  redis:
    host: localhost
    port: 6379
    password: ""
    database: 0
    
    # Connection Pool Settings
    connection_pool:
      max_total: 20
      max_idle: 10
      min_idle: 2
      timeout_ms: 5000
    
    # Cache Settings
    cache_settings:
      default_ttl_seconds: 3600
      key_prefix: "Veloctopus:"
      compression_enabled: true

# Chat System Configuration
chat:
  # Global Chat Settings
  global_chat:
    enabled: true
    format: "<gray>[<aqua>{server}</aqua>]</gray> <{rank_color}>{rank}</color> <white>{player}</white> <gray>></gray> {message}"
    max_message_length: 256
    cooldown_seconds: 1
    
  # Channel Configuration
  channels:
    global:
      name: "Global"
      permission: "Veloctopus.chat.global"
      rate_limit: 60
      format: "<gray>[<green>G</green>]</gray> <{rank_color}>{rank}</color> <white>{player}</white> <gray>></gray> {message}"
    
    staff:
      name: "Staff"
      permission: "Veloctopus.chat.staff"
      rate_limit: 120
      format: "<gray>[<red>STAFF</red>]</gray> <{rank_color}>{rank}</color> <white>{player}</white> <gray>></gray> {message}"
  
  # Filter Settings
  filtering:
    enabled: true
    blocked_words: []
    max_caps_percentage: 50
    max_repeated_chars: 3

# Discord Integration Configuration - Four Specialized Bot Architecture
discord:
  # Multi-Bot Configuration (Spicord-Inspired Architecture)
  bots:
    # Bot 1: Security Bard - Authority & Law Enforcement
    security_bard:
      token: "${VELOCTOPUS_DISCORD_SECURITY_TOKEN}"
      enabled: true
      guild_id: "${DISCORD_GUILD_ID}"
      command_prefix: "!sb"
      
      # Personality Configuration
      personality:
        type: "authority"
        description: "Stern, authoritative, fair but firm law enforcement"
        response_tone: "professional_strict"
        auto_llm_triggers: false  # Manual-only responses for security
        
      # Bot-Specific Channels
      channels:
        mod_log: "${DISCORD_CHANNEL_MOD_LOG}"
        ban_appeals: "${DISCORD_CHANNEL_BAN_APPEALS}"
        security_alerts: "${DISCORD_CHANNEL_SECURITY_ALERTS}"
        staff_coordination: "${DISCORD_CHANNEL_STAFF_COORD}"
        audit_trail: "${DISCORD_CHANNEL_AUDIT}"
        
      # Security Bot Commands
      commands:
        ban_management: true
        kick_management: true
        mute_management: true
        warning_system: true
        appeal_processing: true
        security_monitoring: true
        rule_enforcement: true
        
      # Integration Settings
      integrations:
        velocity_moderation: true
        luckperms_sync: true
        audit_logging: true
        real_time_alerts: true
    
    # Bot 2: Flora - Rewards & Celebration (Semi-Automated)
    flora:
      token: "${VELOCTOPUS_DISCORD_FLORA_TOKEN}"
      enabled: true
      guild_id: "${DISCORD_GUILD_ID}"
      command_prefix: "!flora"
      
      # Personality Configuration
      personality:
        type: "celebration"
        description: "Cheerful, encouraging, celebration-focused, sickly sweet positivity"
        response_tone: "enthusiastic_positive"
        auto_llm_triggers: true
        llm_model_preference: "qwen2.5-coder-14b"  # 1B parameter for quick responses
        
      # Auto-Trigger Settings
      auto_triggers:
        rank_promotions: true
        achievement_unlocks: true
        xp_milestones: true
        daily_celebrations: true
        event_celebrations: true
        community_milestones: true
        
      # LLM Response Configuration
      llm_integration:
        enabled: true
        trigger_on_timer: true
        trigger_on_events: true
        trigger_on_commands: true
        daily_conversation_starters: true
        event_specific_prompts: true
        celebration_enhancement: true
        
      # Bot-Specific Channels
      channels:
        celebrations: "${DISCORD_CHANNEL_CELEBRATIONS}"
        achievements: "${DISCORD_CHANNEL_ACHIEVEMENTS}"
        promotions: "${DISCORD_CHANNEL_PROMOTIONS}"
        daily_rewards: "${DISCORD_CHANNEL_DAILY_REWARDS}"
        community_milestones: "${DISCORD_CHANNEL_MILESTONES}"
        marketing_promos: "${DISCORD_CHANNEL_MARKETING}"
        
      # Flora Commands
      commands:
        floranews: true          # /floranews <message> - Celebration announcements
        floraannounce: true      # /floraannounce <channel> <message> - Promo embeds
        florasay: true           # /florasay <channel> <llm_prompt> - AI responses
        celebration_setup: true
        reward_distribution: true
        milestone_tracking: true
        
      # XP and Achievement Integration
      integrations:
        xp_system_events: true
        rank_promotion_broadcasts: true
        achievement_notifications: true
        community_celebration: true
        marketing_automation: true
    
    # Bot 3: May - Communications Hub
    may:
      token: "${VELOCTOPUS_DISCORD_MAY_TOKEN}"
      enabled: true
      guild_id: "${DISCORD_GUILD_ID}"
      command_prefix: "!may"
      
      # Personality Configuration
      personality:
        type: "communication"
        description: "Reliable, professional, no-nonsense, proud of efficient work"
        response_tone: "professional_efficient"
        auto_llm_triggers: false  # Focused on pure communication
        
      # Core Communication Functions
      communication_hub:
        global_chat_bridge: true
        server_status_monitoring: true
        cross_platform_routing: true
        social_media_integration: true
        webhook_monitoring: true
        
      # Bot-Specific Channels
      channels:
        global_chat: "${DISCORD_CHANNEL_GLOBAL_CHAT}"
        server_status: "${DISCORD_CHANNEL_SERVER_STATUS}"
        announcements: "${DISCORD_CHANNEL_ANNOUNCEMENTS}"
        cross_platform: "${DISCORD_CHANNEL_CROSS_PLATFORM}"
        social_media_feed: "${DISCORD_CHANNEL_SOCIAL_FEED}"
        join_leave_alerts: "${DISCORD_CHANNEL_JOIN_LEAVE}"
        
      # HuskChat-Style Integration
      chat_integration:
        bidirectional_chat: true
        message_filtering: true
        rate_limiting: true
        format_preservation: true
        mention_handling: true
        attachment_support: true
        
      # Server Monitoring
      monitoring:
        server_uptime: true
        player_counts: true
        performance_metrics: true
        error_alerts: true
        maintenance_notifications: true
        
      # Social Media Integration
      social_monitoring:
        twitter_feeds: []           # Add Twitter accounts to monitor
        reddit_feeds: []            # Add Reddit feeds to monitor
        youtube_notifications: []   # Add YouTube channels to monitor
        twitch_streams: []          # Add Twitch streams to monitor
        
      # Integration Settings
      integrations:
        huskchat_style_bridge: true
        velocity_status_sync: true
        matrix_bridge_ready: true
        matterbridge_compatibility: true
    
    # Bot 4: ArchiveSMP Librarian - Knowledge & Research
    librarian:
      token: "${VELOCTOPUS_DISCORD_LIBRARIAN_TOKEN}"
      enabled: true
      guild_id: "${DISCORD_GUILD_ID}"
      command_prefix: "!lib"
      
      # Personality Configuration
      personality:
        type: "knowledge"
        description: "Scholarly, helpful, information-focused, nerdy, excellent at simplification"
        response_tone: "educational_helpful"
        auto_llm_triggers: true
        llm_model_preference: "exaone-3.5-2.4b"  # Better for knowledge tasks
        
      # AI Integration Configuration
      ai_integration:
        velemonaid_backend: true
        wiki_rag_system: true
        article_generation: true
        knowledge_synthesis: true
        technical_explanation: true
        
      # Bot-Specific Channels
      channels:
        help_desk: "${DISCORD_CHANNEL_HELP_DESK}"
        wiki_updates: "${DISCORD_CHANNEL_WIKI_UPDATES}"
        ai_queries: "${DISCORD_CHANNEL_AI_QUERIES}"
        knowledge_base: "${DISCORD_CHANNEL_KNOWLEDGE}"
        tutorials: "${DISCORD_CHANNEL_TUTORIALS}"
        documentation: "${DISCORD_CHANNEL_DOCS}"
        
      # Knowledge Management
      knowledge_features:
        wiki_article_generation: true
        player_query_assistance: true
        command_help_system: true
        technical_documentation: true
        update_interpretation: true
        beginner_education: true
        
      # Python AI Bridge Integration
      python_bridge:
        enabled: true
        velemonaid_endpoint: "${PYTHON_API_URL}/api/wiki"
        rag_query_endpoint: "${PYTHON_API_URL}/api/chat"
        article_generation_endpoint: "${PYTHON_API_URL}/api/generate"
        knowledge_synthesis_endpoint: "${PYTHON_API_URL}/api/synthesize"
        
      # Upstream Software Monitoring
      software_monitoring:
        velocity_updates: true
        plugin_announcements: true
        minecraft_updates: true
        community_news: true
        technical_analysis: true
        
      # Integration Settings
      integrations:
        wiki_management: true
        player_education: true
        ai_query_processing: true
        documentation_generation: true
        community_knowledge_building: true
  
  # Global Discord Features
  global_features:
    # Rich Embeds (DiscordSRV-Inspired Beauty)
    rich_embeds:
      enabled: true
      custom_templates: true
      dynamic_content: true
      color_schemes_per_bot: true
      attachment_handling: true
      interactive_components: true
      
    # Message Enhancement
    message_features:
      reactions: true
      threading: true
      ephemeral_responses: true
      button_interactions: true
      select_menu_support: true
      modal_dialogs: true
      
    # Cross-Bot Communication
    inter_bot_communication:
      enabled: true
      shared_data_channels: true
      coordination_protocols: true
      conflict_resolution: true
      
    # Advanced Features
    advanced_features:
      slash_commands: true
      context_menus: true
      autocomplete: true
      voice_channel_integration: false  # Not needed for this use case
      stage_channel_support: false     # Not needed for this use case
  
  # Security and Rate Limiting
  security:
    api_rate_limiting: true
    message_content_filtering: true
    spam_protection: true
    privilege_escalation_protection: true
    audit_all_bot_actions: true
    
  # Performance Optimization
  performance:
    connection_pooling: true
    message_batching: true
    cache_optimization: true
    lazy_loading: true
    concurrent_bot_operations: true

# Whitelist System Configuration - VeloctopusProject Exact Workflow
whitelist:
  # Core Verification Settings
  enabled: true
  verification_command: "/mc"
  verification_timeout_minutes: 10
  
  # VeloctopusProject Workflow Implementation
  workflow:
    # Step 1: Discord Verification Command
    discord_verification:
      command: "/mc"
      parameter_name: "playername"
      require_exact_match: true
      case_sensitive: false
      
      # Command Validation
      validation:
        min_username_length: 3
        max_username_length: 16
        allowed_characters: "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_"
        
    # Step 2: Mojang API Verification
    mojang_verification:
      enabled: true
      api_endpoint: "https://api.mojang.com/users/profiles/minecraft/"
      rate_limit_per_minute: 60
      timeout_seconds: 10
      cache_duration_hours: 24
      retry_attempts: 3
      retry_delay_seconds: 2
      
      # Username Validation
      username_validation:
        verify_existence: true
        check_name_history: true
        validate_premium_account: false  # Allow cracked accounts
        
    # Step 3: Geyser/Floodgate Support
    geyser_support:
      enabled: true
      prefix: "."
      strip_prefix_for_verification: true
      preserve_prefix_in_game: true
      
      # Bedrock Player Handling
      bedrock_handling:
        validate_xbox_live: false      # Don't require Xbox Live verification
        allow_offline_bedrock: true    # Allow offline Bedrock players
        prefix_validation: true        # Ensure Geyser prefix is present
        
    # Step 4: Purgatory State (10-Minute Window)
    purgatory:
      enabled: true
      duration_minutes: 10
      
      # Hub Server Restriction
      hub_restriction:
        hub_server: "hub"
        block_transfers: true
        allowed_servers: ["hub"]  # Only hub server during purgatory
        
      # Adventure Mode Quarantine
      quarantine:
        gamemode: "ADVENTURE"
        duration_minutes: 5
        force_gamemode: true
        prevent_gamemode_change: true
        
      # Allowed Actions During Purgatory
      allowed_commands:
        - "/spawn"
        - "/help"
        - "/rules"
        - "/discord"
        - "/verify"
        
      # Restricted Actions
      restrictions:
        block_chat_spam: true
        limit_movement_speed: true
        prevent_item_pickup: true
        prevent_block_interaction: true
        prevent_entity_interaction: true
        
    # Step 5: Member Status Transition
    member_transition:
      auto_promote_on_join: true
      remove_purgatory_restrictions: true
      grant_full_network_access: true
      send_welcome_message: true
      log_successful_verification: true
      
      # Post-Verification Actions
      post_verification:
        assign_starting_rank: "bystander"
        grant_basic_permissions: true
        add_to_discord_role: "verified"
        send_flora_celebration: true
        update_database_status: true
  
  # State Management
  state_management:
    # Player States
    states:
      unverified: "blocked_from_game_servers"
      verification_pending: "discord_command_issued_waiting_mojang"
      purgatory: "hub_only_with_restrictions"
      verified: "full_network_access"
      member: "established_community_member"
      
    # State Persistence
    persistence:
      store_in_database: true
      cache_in_redis: true
      backup_state_changes: true
      
    # State Transitions
    transitions:
      unverified_to_pending: "discord_verification_command"
      pending_to_purgatory: "mojang_verification_success"
      purgatory_to_verified: "minecraft_join_during_window"
      verified_to_member: "community_integration_complete"
      
    # Timeout Handling
    timeouts:
      verification_expires: "return_to_unverified"
      purgatory_expires: "return_to_unverified"
      cleanup_expired_states: true
      cleanup_interval_minutes: 30
  
  # Discord Integration Settings
  discord_integration:
    # Verification Channel
    verification_channel: "${DISCORD_CHANNEL_VERIFICATION}"
    log_channel: "${DISCORD_CHANNEL_WHITELIST_LOG}"
    appeal_channel: "${DISCORD_CHANNEL_APPEALS}"
    
    # Role Management
    roles:
      unverified: "${DISCORD_ROLE_UNVERIFIED}"
      verification_pending: "${DISCORD_ROLE_PENDING}"
      purgatory: "${DISCORD_ROLE_PURGATORY}"
      verified: "${DISCORD_ROLE_VERIFIED}"
      member: "${DISCORD_ROLE_MEMBER}"
      
    # Notification Settings
    notifications:
      verification_success: true
      purgatory_entry: true
      member_promotion: true
      verification_failure: true
      timeout_warnings: true
      
    # Command Responses
    command_responses:
      success_message: "✅ Verification successful! You have 10 minutes to join the Minecraft server."
      already_verified: "❌ You are already verified for this username."
      invalid_username: "❌ Invalid Minecraft username. Please check spelling and try again."
      mojang_api_error: "⚠️ Unable to verify username with Mojang. Please try again later."
      rate_limited: "⏱️ Please wait before trying to verify again."
      timeout_warning: "⚠️ Your verification will expire in 2 minutes. Please join the server soon!"
      verification_expired: "❌ Your verification has expired. Please use `/mc <username>` again."
  
  # Security and Anti-Abuse
  security:
    # Rate Limiting
    rate_limiting:
      max_attempts_per_user_per_hour: 3
      max_attempts_per_username_per_day: 5
      cooldown_after_failure_minutes: 15
      
    # Anti-Abuse Measures
    anti_abuse:
      detect_username_cycling: true
      prevent_rapid_reverification: true
      monitor_suspicious_patterns: true
      auto_blacklist_abusers: true
      
    # Logging and Monitoring
    monitoring:
      log_all_attempts: true
      track_success_rates: true
      monitor_api_response_times: true
      alert_on_high_failure_rates: true
      
    # Blacklist System
    blacklist:
      enabled: true
      blacklisted_usernames: []
      blacklisted_discord_ids: []
      blacklist_duration_hours: 24
      permanent_blacklist: []
  
  # Performance Optimization
  performance:
    # Caching Strategy
    caching:
      cache_mojang_responses: true
      cache_duration_hours: 24
      preload_frequent_usernames: true
      
    # Database Optimization
    database:
      batch_state_updates: true
      cleanup_old_records_days: 30
      index_optimization: true
      
    # Network Optimization
    network:
      connection_pooling: true
      parallel_api_requests: false  # Respect Mojang rate limits
      timeout_optimization: true
  
  # Integration Points
  integrations:
    # Velocity Integration
    velocity:
      hook_player_login: true
      enforce_server_restrictions: true
      sync_gamemode_changes: true
      
    # LuckPerms Integration
    luckperms:
      sync_verification_status: true
      assign_groups_by_state: true
      remove_old_groups: true
      
    # Database Integration
    database:
      store_verification_history: true
      track_state_transitions: true
      maintain_audit_trail: true

# Rank System Configuration
ranks:
  # VeloctopusProject 25 Main Ranks × 7 Sub-Ranks = 175 Total Combinations
  # This is the EXACT rank structure from VeloctopusProject and MUST be maintained
  definitions:
    # 25 Main Ranks (Progression Order)
    main_ranks:
      bystander:
        display_name: "Bystander"
        color: "<dark_gray>"
        weight: 100
        base_xp_requirement: 0
        description: "New community member, observing and learning"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.basic"
      
      resident:
        display_name: "Resident"
        color: "<gray>"
        weight: 200
        base_xp_requirement: 500
        description: "Regular community participant"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.basic"
          - "Veloctopus.server.transfer"
      
      citizen:
        display_name: "Citizen"
        color: "<white>"
        weight: 300
        base_xp_requirement: 1500
        description: "Established community member"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.basic"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
      
      advocate:
        display_name: "Advocate"
        color: "<yellow>"
        weight: 400
        base_xp_requirement: 3000
        description: "Community supporter and helper"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.basic"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.basic"
      
      guardian:
        display_name: "Guardian"
        color: "<green>"
        weight: 500
        base_xp_requirement: 5000
        description: "Community protector and guide"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.basic"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.basic"
          - "Veloctopus.moderation.warn"
      
      protector:
        display_name: "Protector"
        color: "<dark_green>"
        weight: 600
        base_xp_requirement: 8000
        description: "Active community defender"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.basic"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.basic"
          - "Veloctopus.moderation.warn"
          - "Veloctopus.moderation.kick"
      
      defender:
        display_name: "Defender"
        color: "<aqua>"
        weight: 700
        base_xp_requirement: 12000
        description: "Skilled community guardian"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.basic"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.basic"
          - "Veloctopus.moderation.warn"
          - "Veloctopus.moderation.kick"
          - "Veloctopus.chat.staff"
      
      champion:
        display_name: "Champion"
        color: "<dark_aqua>"
        weight: 800
        base_xp_requirement: 17000
        description: "Community champion and leader"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.basic"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.basic"
          - "Veloctopus.moderation.warn"
          - "Veloctopus.moderation.kick"
          - "Veloctopus.chat.staff"
          - "Veloctopus.moderation.mute"
      
      hero:
        display_name: "Hero"
        color: "<blue>"
        weight: 900
        base_xp_requirement: 25000
        description: "Heroic community figure"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.basic"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.advanced"
          - "Veloctopus.moderation.warn"
          - "Veloctopus.moderation.kick"
          - "Veloctopus.chat.staff"
          - "Veloctopus.moderation.mute"
          - "Veloctopus.priority.join"
      
      legend:
        display_name: "Legend"
        color: "<dark_blue>"
        weight: 1000
        base_xp_requirement: 35000
        description: "Legendary community member"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.basic"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.advanced"
          - "Veloctopus.moderation.warn"
          - "Veloctopus.moderation.kick"
          - "Veloctopus.chat.staff"
          - "Veloctopus.moderation.mute"
          - "Veloctopus.priority.join"
          - "Veloctopus.moderation.ban.temporary"
      
      mythic:
        display_name: "Mythic"
        color: "<light_purple>"
        weight: 1100
        base_xp_requirement: 50000
        description: "Mythical community presence"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.advanced"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.advanced"
          - "Veloctopus.moderation.warn"
          - "Veloctopus.moderation.kick"
          - "Veloctopus.chat.staff"
          - "Veloctopus.moderation.mute"
          - "Veloctopus.priority.join"
          - "Veloctopus.moderation.ban.temporary"
          - "Veloctopus.admin.teleport"
      
      ethereal:
        display_name: "Ethereal"
        color: "<dark_purple>"
        weight: 1200
        base_xp_requirement: 70000
        description: "Ethereal community spirit"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.advanced"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.advanced"
          - "Veloctopus.moderation.warn"
          - "Veloctopus.moderation.kick"
          - "Veloctopus.chat.staff"
          - "Veloctopus.moderation.mute"
          - "Veloctopus.priority.join"
          - "Veloctopus.moderation.ban.temporary"
          - "Veloctopus.admin.teleport"
          - "Veloctopus.admin.time"
      
      transcendent:
        display_name: "Transcendent"
        color: "<red>"
        weight: 1300
        base_xp_requirement: 95000
        description: "Transcendent community influence"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.advanced"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.expert"
          - "Veloctopus.moderation.*"
          - "Veloctopus.chat.staff"
          - "Veloctopus.priority.join"
          - "Veloctopus.admin.teleport"
          - "Veloctopus.admin.time"
          - "Veloctopus.admin.weather"
      
      immortal:
        display_name: "Immortal"
        color: "<dark_red>"
        weight: 1400
        base_xp_requirement: 125000
        description: "Immortal community legacy"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.expert"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.expert"
          - "Veloctopus.moderation.*"
          - "Veloctopus.chat.staff"
          - "Veloctopus.priority.join"
          - "Veloctopus.admin.teleport"
          - "Veloctopus.admin.time"
          - "Veloctopus.admin.weather"
          - "Veloctopus.admin.gamemode"
      
      divine:
        display_name: "Divine"
        color: "<gold>"
        weight: 1500
        base_xp_requirement: 160000
        description: "Divine community blessing"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.expert"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.expert"
          - "Veloctopus.moderation.*"
          - "Veloctopus.chat.staff"
          - "Veloctopus.priority.join"
          - "Veloctopus.admin.*"
      
      cosmic:
        display_name: "Cosmic"
        color: "<#FF6B35>"
        weight: 1600
        base_xp_requirement: 200000
        description: "Cosmic community force"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.expert"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.master"
          - "Veloctopus.moderation.*"
          - "Veloctopus.chat.staff"
          - "Veloctopus.priority.join"
          - "Veloctopus.admin.*"
          - "Veloctopus.server.manage"
      
      universal:
        display_name: "Universal"
        color: "<#4ECDC4>"
        weight: 1700
        base_xp_requirement: 250000
        description: "Universal community connection"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.master"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.master"
          - "Veloctopus.moderation.*"
          - "Veloctopus.chat.staff"
          - "Veloctopus.priority.join"
          - "Veloctopus.admin.*"
          - "Veloctopus.server.manage"
          - "Veloctopus.plugin.reload"
      
      omnipotent:
        display_name: "Omnipotent"
        color: "<#45B7D1>"
        weight: 1800
        base_xp_requirement: 310000
        description: "Omnipotent community power"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.master"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.master"
          - "Veloctopus.moderation.*"
          - "Veloctopus.chat.staff"
          - "Veloctopus.priority.join"
          - "Veloctopus.admin.*"
          - "Veloctopus.server.manage"
          - "Veloctopus.plugin.reload"
          - "Veloctopus.database.access"
      
      eternal:
        display_name: "Eternal"
        color: "<#96CEB4>"
        weight: 1900
        base_xp_requirement: 380000
        description: "Eternal community presence"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.master"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.master"
          - "Veloctopus.moderation.*"
          - "Veloctopus.chat.staff"
          - "Veloctopus.priority.join"
          - "Veloctopus.admin.*"
          - "Veloctopus.server.manage"
          - "Veloctopus.plugin.reload"
          - "Veloctopus.database.access"
          - "Veloctopus.system.monitor"
      
      infinite:
        display_name: "Infinite"
        color: "<#FFEAA7>"
        weight: 2000
        base_xp_requirement: 460000
        description: "Infinite community wisdom"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.master"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.legendary"
          - "Veloctopus.moderation.*"
          - "Veloctopus.chat.staff"
          - "Veloctopus.priority.join"
          - "Veloctopus.admin.*"
          - "Veloctopus.server.manage"
          - "Veloctopus.plugin.reload"
          - "Veloctopus.database.access"
          - "Veloctopus.system.monitor"
          - "Veloctopus.network.manage"
      
      primordial:
        display_name: "Primordial"
        color: "<#DDA0DD>"
        weight: 2100
        base_xp_requirement: 550000
        description: "Primordial community essence"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.legendary"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.legendary"
          - "Veloctopus.moderation.*"
          - "Veloctopus.chat.staff"
          - "Veloctopus.priority.join"
          - "Veloctopus.admin.*"
          - "Veloctopus.server.manage"
          - "Veloctopus.plugin.reload"
          - "Veloctopus.database.access"
          - "Veloctopus.system.monitor"
          - "Veloctopus.network.manage"
          - "Veloctopus.security.override"
      
      sovereign:
        display_name: "Sovereign"
        color: "<#FF7675>"
        weight: 2200
        base_xp_requirement: 650000
        description: "Sovereign community authority"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.legendary"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.legendary"
          - "Veloctopus.moderation.*"
          - "Veloctopus.chat.staff"
          - "Veloctopus.priority.join"
          - "Veloctopus.admin.*"
          - "Veloctopus.server.manage"
          - "Veloctopus.plugin.reload"
          - "Veloctopus.database.access"
          - "Veloctopus.system.monitor"
          - "Veloctopus.network.manage"
          - "Veloctopus.security.override"
          - "Veloctopus.emergency.powers"
      
      supreme:
        display_name: "Supreme"
        color: "<#74B9FF>"
        weight: 2300
        base_xp_requirement: 760000
        description: "Supreme community leadership"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.legendary"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.ultimate"
          - "Veloctopus.moderation.*"
          - "Veloctopus.chat.staff"
          - "Veloctopus.priority.join"
          - "Veloctopus.admin.*"
          - "Veloctopus.server.manage"
          - "Veloctopus.plugin.reload"
          - "Veloctopus.database.access"
          - "Veloctopus.system.monitor"
          - "Veloctopus.network.manage"
          - "Veloctopus.security.override"
          - "Veloctopus.emergency.powers"
          - "Veloctopus.ai.control"
      
      ultimate:
        display_name: "Ultimate"
        color: "<#0984E3>"
        weight: 2400
        base_xp_requirement: 880000
        description: "Ultimate community mastery"
        permissions:
          - "Veloctopus.chat.global"
          - "Veloctopus.help.ultimate"
          - "Veloctopus.server.transfer"
          - "Veloctopus.chat.color"
          - "Veloctopus.chat.format.ultimate"
          - "Veloctopus.moderation.*"
          - "Veloctopus.chat.staff"
          - "Veloctopus.priority.join"
          - "Veloctopus.admin.*"
          - "Veloctopus.server.manage"
          - "Veloctopus.plugin.reload"
          - "Veloctopus.database.access"
          - "Veloctopus.system.monitor"
          - "Veloctopus.network.manage"
          - "Veloctopus.security.override"
          - "Veloctopus.emergency.powers"
          - "Veloctopus.ai.control"
          - "Veloctopus.architecture.modify"
      
      deity:
        display_name: "Deity"
        color: "<#E17055>"
        weight: 2500
        base_xp_requirement: 1000000
        description: "Divine community transcendence"
        permissions:
          - "Veloctopus.*"

    # 7 Sub-Ranks (Applied to all Main Ranks)
    sub_ranks:
      novice:
        display_name: "Novice"
        suffix: ""
        weight_modifier: 0
        xp_multiplier: 1.0
        description: "Beginning stage of the rank"
        
      apprentice:
        display_name: "Apprentice"
        suffix: "+"
        weight_modifier: 1
        xp_multiplier: 1.1
        description: "Learning and improving in the rank"
        
      journeyman:
        display_name: "Journeyman"
        suffix: "++"
        weight_modifier: 2
        xp_multiplier: 1.2
        description: "Competent practitioner of the rank"
        
      expert:
        display_name: "Expert"
        suffix: "+++"
        weight_modifier: 3
        xp_multiplier: 1.3
        description: "Skilled expert in the rank"
        
      master:
        display_name: "Master"
        suffix: "★"
        weight_modifier: 4
        xp_multiplier: 1.4
        description: "Master-level practitioner"
        
      grandmaster:
        display_name: "Grandmaster"
        suffix: "★★"
        weight_modifier: 5
        xp_multiplier: 1.5
        description: "Grandmaster-level excellence"
        
      immortal:
        display_name: "Immortal"
        suffix: "★★★"
        weight_modifier: 6
        xp_multiplier: 1.6
        description: "Immortal mastery of the rank"
  
  # Rank Progression Settings - VeloctopusProject Exact Formula
  progression:
    auto_promotion: true
    manual_promotion_allowed: true
    
    # XP-Only Progression (No Time or Achievement Requirements)
    # Formula: base_xp × (sub_rank_multiplier ^ sub_rank_level)
    # Where sub_rank_multiplier = 1.1 and sub_rank_level = 0-6
    
    # Main Rank XP Requirements (exact from VeloctopusProject)
    xp_requirements:
      bystander: 0          # Starting rank
      resident: 500         # 500 XP total
      citizen: 1500         # 1,500 XP total  
      advocate: 3000        # 3,000 XP total
      guardian: 5000        # 5,000 XP total
      protector: 8000       # 8,000 XP total
      defender: 12000       # 12,000 XP total
      champion: 17000       # 17,000 XP total
      hero: 25000           # 25,000 XP total
      legend: 35000         # 35,000 XP total
      mythic: 50000         # 50,000 XP total
      ethereal: 70000       # 70,000 XP total
      transcendent: 95000   # 95,000 XP total
      immortal: 125000      # 125,000 XP total
      divine: 160000        # 160,000 XP total
      cosmic: 200000        # 200,000 XP total
      universal: 250000     # 250,000 XP total
      omnipotent: 310000    # 310,000 XP total
      eternal: 380000       # 380,000 XP total
      infinite: 460000      # 460,000 XP total
      primordial: 550000    # 550,000 XP total
      sovereign: 650000     # 650,000 XP total
      supreme: 760000       # 760,000 XP total
      ultimate: 880000      # 880,000 XP total
      deity: 1000000        # 1,000,000 XP total
    
    # Sub-Rank Progression Within Each Main Rank
    # Each sub-rank requires: base_main_rank_xp × sub_rank_multiplier
    sub_rank_progression:
      base_multiplier: 1.1
      requirements:
        novice: 1.0         # Base requirement (no multiplier)
        apprentice: 1.1     # 10% more XP
        journeyman: 1.21    # 21% more XP  
        expert: 1.331       # 33.1% more XP
        master: 1.4641      # 46.41% more XP
        grandmaster: 1.61051 # 61.051% more XP
        immortal: 1.771561  # 77.1561% more XP
    
    # Promotion Validation Rules
    validation:
      require_all_sub_ranks: false  # Can skip sub-ranks with admin override
      allow_rank_skipping: false    # Must progress through each main rank
      validate_xp_on_promotion: true
      log_all_promotions: true
      
    # Rank Display Format
    display_format:
      main_only: "{rank_color}{main_rank_display}{rank_suffix}"
      with_sub_rank: "{rank_color}{main_rank_display} {sub_rank_display}{rank_suffix}"
      full_format: "{rank_color}[{main_rank_display} {sub_rank_display}]{rank_suffix} {player_name}"
      
    # Discord Role Mapping (175 total combinations)
    discord_integration:
      create_roles_for_all_combinations: true
      role_name_format: "{main_rank}_{sub_rank}"
      sync_on_promotion: true
      remove_old_rank_roles: true

# XP System Configuration - VeloctopusProject 4000-Endpoint Achievement Architecture
xp:
  enabled: true
  
  # Core XP System Settings
  core_settings:
    base_calculation_formula: "base_xp × community_multiplier × time_multiplier × rank_bonus"
    max_xp_per_day: 5000
    max_xp_per_week: 25000
    max_xp_per_month: 75000
    overflow_protection: true
    
  # XP Sources - Individual Achievement Track (40% of optimal progression)
  individual_sources:
    # Core Minecraft Activities
    chat_message:
      base_xp: 1
      cooldown_seconds: 60
      max_per_hour: 30
      quality_bonus_multiplier: 1.2
      
    playtime:
      xp_per_minute: 2
      bonus_multiplier_hours: 2.0
      max_per_day: 1000
      afk_detection: true
      afk_timeout_minutes: 15
      
    block_break:
      base_xp: 0.5
      valuable_blocks_multiplier: 2.0
      max_per_hour: 500
      rare_block_bonus: 5
      
    block_place:
      base_xp: 0.3
      creative_building_bonus: 1.5
      max_per_hour: 300
      
    mob_kill:
      base_xp: 2
      boss_multiplier: 10.0
      elite_mob_multiplier: 5.0
      max_per_hour: 200
      
    advancement_unlock:
      base_xp: 25
      major_advancement_multiplier: 3.0
      hidden_advancement_bonus: 50
      
    first_time_bonuses:
      first_join: 100
      first_build: 50
      first_death: 10
      first_achievement: 25
      daily_login: 25
  
  # XP Sources - Community Contribution Track (60% of optimal progression)
  community_sources:
    # Active Community Engagement (Verbal/Social)
    new_player_mentoring:
      successful_onboarding_xp: 50
      per_session_xp: 15
      graduation_bonus: 100
      max_mentees_per_month: 10
      
    community_event_participation:
      small_event_xp: 25
      large_event_xp: 40
      event_organization_xp: 75
      leadership_bonus: 1.5
      
    teaching_sessions:
      per_session_xp: 30
      skill_mastery_bonus: 20
      student_success_multiplier: 2.0
      max_sessions_per_week: 10
      
    conflict_resolution:
      successful_mediation_xp: 60
      peaceful_resolution_bonus: 25
      community_harmony_multiplier: 1.5
      
    community_problem_solving:
      solution_implementation_xp: 40
      innovation_bonus: 40
      long_term_benefit_multiplier: 2.0
      max_per_month: 8
    
    # Quiet Community Contribution (Non-Verbal/Introverted)
    helpful_building_projects:
      infrastructure_xp: 20
      community_benefit_multiplier: 2.5
      aesthetic_improvement_bonus: 30
      max_projects_per_month: 5
      
    resource_sharing:
      meaningful_donation_xp: 10
      rare_resource_multiplier: 2.0
      community_project_bonus: 15
      max_donations_per_week: 10
      
    documentation_creation:
      guide_creation_xp: 30
      tutorial_quality_bonus: 30
      community_adoption_multiplier: 2.0
      wiki_contribution_xp: 25
      
    quality_builds:
      inspiring_structure_xp: 40
      helpful_design_bonus: 25
      landmark_creation_multiplier: 2.5
      architectural_innovation_xp: 60
      
    maintenance_work:
      server_upkeep_xp: 15
      repair_work_xp: 20
      cleanup_efforts_xp: 10
      infrastructure_maintenance_bonus: 1.5
    
    # Passive Community Support (Stress-Free Options)
    consistent_presence:
      daily_online_bonus: 5
      community_time_multiplier: 1.2
      positive_atmosphere_bonus: 10
      
    peer_recognition:
      commendation_received_xp: 10
      appreciation_bonus: 15
      community_nomination_xp: 25
      
    silent_support:
      group_participation_xp: 15
      background_contribution_bonus: 20
      cultural_participation_xp: 25
      
    tradition_building:
      cultural_contribution_xp: 25
      tradition_maintenance_xp: 20
      community_identity_bonus: 35
  
  # Peer Recognition and Validation System
  peer_recognition:
    enabled: true
    daily_nominations_per_player: 3
    weekly_peer_votes: 10
    nomination_xp_value: 15
    vote_xp_value: 10
    community_validation_required: true
    minimum_validators: 2
    validation_timeout_hours: 48
    
  # XP Multipliers and Bonuses
  multipliers:
    # Time-Based Multipliers
    weekend: 1.5
    holiday: 2.0
    special_events: 2.5
    
    # Community Contribution Multipliers (EXACT from VeloctopusProject)
    solo_achievement: 1.0           # Base rate
    peer_collaboration: 1.5         # Joint achievements
    community_teaching: 2.0         # Helping others learn
    new_player_support: 2.5         # Onboarding assistance
    community_problem_solving: 3.0  # Addressing server needs
    inclusive_community_building: 3.5 # Activities welcoming everyone
    
    # Rank-Based Bonus Multipliers
    rank_bonuses:
      bystander: 1.0
      resident: 1.05
      citizen: 1.1
      advocate: 1.15
      guardian: 1.2
      protector: 1.25
      defender: 1.3
      champion: 1.35
      hero: 1.4
      legend: 1.45
      mythic: 1.5
      ethereal: 1.55
      transcendent: 1.6
      immortal: 1.65
      divine: 1.7
      cosmic: 1.75
      universal: 1.8
      omnipotent: 1.85
      eternal: 1.9
      infinite: 1.95
      primordial: 2.0
      sovereign: 2.05
      supreme: 2.1
      ultimate: 2.15
      deity: 2.2
  
  # Rate Limiting and Anti-Gaming Protection
  rate_limiting:
    # Individual Activity Limits (Prevent Grinding)
    daily_individual_achievements: 5
    weekly_individual_milestones: 20
    monthly_individual_epics: 75
    quarterly_individual_legends: 200
    
    # Community Activity Limits (Encourage Quality)
    daily_community_contributions: 3
    weekly_community_impact: 15
    monthly_community_leadership: 50
    quarterly_community_legacy: 150
    
    # Hybrid Achievement Limits
    daily_hybrid_goals: 4
    weekly_hybrid_milestones: 18
    monthly_hybrid_accomplishments: 60
    quarterly_hybrid_mastery: 175
    
    # Anti-Gaming Mechanisms
    peer_validation_required: true
    impact_assessment_enabled: true
    quality_gates_enforced: true
    collaboration_requirements: true
    time_span_validation: true
    community_benefit_verification: true
  
  # Achievement Categories (4000-Endpoint Architecture)
  achievement_categories:
    individual_achievements: 1200    # Solo accomplishments
    community_achievements: 1200     # Group contributions  
    hybrid_achievements: 1000        # Mixed solo/community
    peer_recognition_milestones: 400 # Community validation
    special_achievements: 200        # Unique/seasonal
  
  # Storage and Performance
  storage:
    batch_update_interval_seconds: 30
    cache_xp_calculations: true
    use_database_transactions: true
    backup_xp_data_daily: true
    compress_historical_data: true
    
  # Monitoring and Analytics
  monitoring:
    track_xp_sources: true
    monitor_progression_rates: true
    detect_xp_anomalies: true
    generate_progress_reports: true
    community_health_metrics: true

# Permission System Configuration
permissions:
  # Core Settings
  enabled: true
  default_group: "guest"
  
  # Storage Settings
  storage:
    type: "mariadb"  # mariadb, redis, or mixed
    cache_permissions: true
    cache_duration_minutes: 30
  
  # Group Settings
  groups:
    default_permissions: []
    inheritance_enabled: true
    weight_based_priority: true
  
  # User Settings
  users:
    store_offline_players: true
    uuid_based_storage: true
    name_change_tracking: true

# Command System Configuration
commands:
  # Core Settings
  enabled: true
  
  # Brigadier Settings
  brigadier:
    case_sensitive: false
    suggest_on_partial: true
    max_suggestions: 10
  
  # Discord Commands
  discord_commands:
    enabled: true
    guild_commands_only: false
    ephemeral_responses: false
  
  # Command Aliases
  aliases:
    "spawn": ["hub", "lobby"]
    "msg": ["tell", "whisper", "pm"]
    "r": ["reply"]

# External Integration Configuration
integrations:
  # Python AI Bridge
  python_bridge:
    enabled: false
    api_url: "http://localhost:8080"
    api_key: "changeme"
    timeout_seconds: 30
    
    # VelemonAId Integration
    velemonaid:
      enabled: false
      wiki_endpoint: "/api/wiki"
      chat_endpoint: "/api/chat"
      model_preferences:
        - "qwen2.5-coder-14b"
        - "exaone-3.5-2.4b"
  
  # Matrix Bridge
  matrix:
    enabled: false
    homeserver_url: "https://matrix.example.com"
    access_token: "changeme"
    room_mappings:
      global_chat: "!roomid:matrix.example.com"
  
  # Economy Integration
  economy:
    enabled: false
    provider: "theneweconomy"  # theneweconomy, vault
    
    # QuickShop Integration
    quickshop:
      enabled: false
      transaction_notifications: true
      discord_shop_alerts: true

# Security Configuration
security:
  # Rate Limiting
  rate_limiting:
    enabled: true
    
    # Per-player limits
    player_limits:
      commands_per_minute: 20
      messages_per_minute: 10
      api_calls_per_minute: 5
    
    # Global limits
    global_limits:
      database_queries_per_second: 100
      redis_operations_per_second: 500
  
  # Authentication
  authentication:
    require_secure_passwords: true
    session_timeout_minutes: 60
    max_failed_attempts: 5
    lockout_duration_minutes: 15
  
  # Data Protection
  data_protection:
    encrypt_sensitive_data: true
    anonymize_logs: false
    gdpr_compliance: true
    data_retention_days: 90

# Monitoring Configuration
monitoring:
  # Metrics Collection
  metrics:
    enabled: true
    collection_interval_seconds: 60
    retention_days: 30
    
    # Metric Types
    types:
      - "performance"
      - "usage"
      - "errors"
      - "security"
  
  # Health Checks
  health_checks:
    enabled: true
    interval_seconds: 30
    
    # Check Targets
    targets:
      - "database"
      - "redis"
      - "discord_bots"
      - "external_apis"
  
  # Alerting
  alerts:
    enabled: true
    discord_webhook: "https://discord.com/api/webhooks/..."
    
    # Alert Conditions
    conditions:
      high_memory_usage: 80
      high_cpu_usage: 80
      database_slow_queries: 1000
      failed_health_checks: 3

# Development Configuration
development:
  # Debug Settings
  debug:
    enabled: false
    log_sql_queries: false
    log_redis_operations: false
    performance_tracking: true
  
  # Testing
  testing:
    mock_external_services: false
    test_data_enabled: false
    integration_test_mode: false
```

## Environment Variable Overrides

The following environment variables can override configuration values:

### Database
- `Veloctopus_DB_HOST` - MariaDB host
- `Veloctopus_DB_PORT` - MariaDB port
- `Veloctopus_DB_USER` - MariaDB username
- `Veloctopus_DB_PASS` - MariaDB password
- `Veloctopus_DB_NAME` - MariaDB database name

### Redis
- `Veloctopus_REDIS_HOST` - Redis host
- `Veloctopus_REDIS_PORT` - Redis port
- `Veloctopus_REDIS_PASS` - Redis password

### Discord Bots
- `Veloctopus_DISCORD_SECURITY_TOKEN` - Security Bard token
- `Veloctopus_DISCORD_FLORA_TOKEN` - Flora token
- `Veloctopus_DISCORD_MAY_TOKEN` - May token
- `Veloctopus_DISCORD_LIBRARIAN_TOKEN` - Librarian token

### External Services
- `Veloctopus_PYTHON_API_URL` - Python bridge API URL
- `Veloctopus_PYTHON_API_KEY` - Python bridge API key
- `Veloctopus_MATRIX_TOKEN` - Matrix access token

## Configuration Validation

The system will validate all configuration values on startup:

### Required Fields
- Database connection details
- At least one Discord bot token
- Valid server names and channel IDs

### Optional Fields
- External integrations (Python, Matrix)
- Advanced features (XP system, economy)
- Monitoring and alerting

### Default Values
All configuration keys have sensible defaults to minimize required configuration. Only database and Discord tokens are strictly required for basic operation.

## Hot Reload Support

The following configuration sections support hot reloading without restart:
- Chat formats and filters
- Rate limiting settings
- Permission updates
- Monitoring thresholds
- Debug settings

Configuration changes are detected automatically and applied within 30 seconds.
