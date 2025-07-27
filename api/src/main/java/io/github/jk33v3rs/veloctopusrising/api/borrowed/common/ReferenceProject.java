/*
 * This file is part of VeloctopusProject, licensed under the MIT License.
 *
 * Copyright (c) 2025 VeloctopusProject Contributors
 * 
 * Reference Project Enumeration
 * Defines all the reference projects that have been analyzed and borrowed from
 */

package io.github.jk33v3rs.veloctopusrising.api.borrowed.common;

/**
 * Enumeration of reference projects used for pattern extraction and code borrowing.
 * Each project represents a plugin or system that has been analyzed for useful patterns.
 */
public enum ReferenceProject {
    /**
     * Spicord - Discord integration plugin for Minecraft servers
     * Used for Discord bot patterns and JDA integration techniques
     */
    SPICORD("Spicord", "Discord integration plugin", "sh.okx.spicord"),
    
    /**
     * ChatRegulator - Advanced chat filtering and moderation system
     * Used for chat filtering patterns and moderation workflows
     */
    CHATREGULATOR("ChatRegulator", "Chat filtering and moderation", "me.whereareiam.chatregulator"),
    
    /**
     * EpicGuard - Advanced bot protection and anti-grief system
     * Used for security patterns and protection mechanisms
     */
    EPICGUARD("EpicGuard", "Bot protection and security", "me.xneox.epicguard"),
    
    /**
     * HuskChat - Cross-server chat system for Velocity
     * Used for cross-server messaging and chat bridging patterns
     */
    HUSKCHAT("HuskChat", "Cross-server chat system", "net.william278.huskchat"),
    
    /**
     * KickRedirect - Server kick redirection system
     * Used for server routing and redirection patterns
     */
    KICKREDIRECT("KickRedirect", "Server kick redirection", "com.github._4drian3d.kickredirect"),
    
    /**
     * SignedVelocity - Chat signing and security for Velocity
     * Used for security and authentication patterns
     */
    SIGNEDVELOCITY("SignedVelocity", "Chat signing and security", "com.github._4drian3d.signedvelocity"),
    
    /**
     * VLobby - Lobby management system for Velocity
     * Used for lobby management and player routing patterns
     */
    VLOBBY("VLobby", "Lobby management system", "com.github._4drian3d.vlobby"),
    
    /**
     * VPacketEvents - Packet handling system for Velocity
     * Used for packet manipulation and event handling patterns
     */
    VPACKETEVENTS("VPacketEvents", "Packet handling system", "com.github._4drian3d.vpacketevents"),
    
    /**
     * VelemonAId - AI integration and Python bridge
     * Used for AI integration patterns and Python bridge techniques
     */
    VELEMONAID("VelemonAId", "AI integration and Python bridge", "org.veloctopus.velemonaid"),
    
    /**
     * Discord AI Bot - AI-powered Discord bot system
     * Used for Discord AI integration and conversation patterns
     */
    DISCORD_AI_BOT("Discord AI Bot", "AI-powered Discord bot", "org.veloctopus.discordaibot");
    
    private final String displayName;
    private final String description;
    private final String basePackage;
    
    ReferenceProject(String displayName, String description, String basePackage) {
        this.displayName = displayName;
        this.description = description;
        this.basePackage = basePackage;
    }
    
    /**
     * @return the human-readable display name of the project
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * @return a brief description of what this project provides
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @return the base package name for this project
     */
    public String getBasePackage() {
        return basePackage;
    }
    
    /**
     * @return the lowercase name suitable for package naming
     */
    public String getPackageName() {
        return name().toLowerCase();
    }
}
