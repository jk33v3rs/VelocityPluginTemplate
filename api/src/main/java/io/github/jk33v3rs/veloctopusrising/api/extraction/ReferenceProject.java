package io.github.jk33v3rs.veloctopusrising.api.extraction;

/**
 * Enumeration of reference projects available for pattern extraction.
 * 
 * <p>Defines all reference projects in the workspace that contain proven
 * implementations suitable for extraction and adaptation into Veloctopus Rising.
 * Each project includes license information and extraction compatibility notes.</p>
 * 
 * <p><strong>Extraction Philosophy:</strong> Leverage the "67% Borrowed Code Minimum"
 * principle by identifying and reusing battle-tested implementations rather than
 * rebuilding from scratch.</p>
 * 
 * <h3>License Compatibility:</h3>
 * <ul>
 *   <li><strong>GPL-3.0 Compatible:</strong> Can be directly integrated</li>
 *   <li><strong>Apache/MIT:</strong> Can be integrated with attribution</li>
 *   <li><strong>Study Only:</strong> Patterns can be learned but not copied</li>
 * </ul>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see ExtractionFramework
 */
public enum ReferenceProject {
    
    /**
     * Spicord - Multi-bot Discord integration framework.
     * 
     * <p><strong>Primary Extractions:</strong> Multi-bot architecture, Discord event
     * routing, bot configuration management, addon system patterns.</p>
     * 
     * <p><strong>License:</strong> GPL-3.0 (Fully compatible)</p>
     * <p><strong>Repository:</strong> references/Spicord</p>
     * <p><strong>Adaptation Level:</strong> Heavy modification for 4-bot system</p>
     */
    SPICORD("Spicord", "references/Spicord", "GPL-3.0", true,
        "Multi-bot Discord architecture and event routing patterns"),
    
    /**
     * ChatRegulator - Message filtering and moderation system.
     * 
     * <p><strong>Primary Extractions:</strong> Message filtering algorithms,
     * moderation automation, chat monitoring, violation tracking.</p>
     * 
     * <p><strong>License:</strong> GPL-3.0 (Fully compatible)</p>
     * <p><strong>Repository:</strong> references/ChatRegulator</p>
     * <p><strong>Adaptation Level:</strong> Moderate modification for integration</p>
     */
    CHATREGULATOR("ChatRegulator", "references/ChatRegulator", "GPL-3.0", true,
        "Message filtering and moderation automation systems"),
    
    /**
     * EpicGuard - Connection protection and anti-bot systems.
     * 
     * <p><strong>Primary Extractions:</strong> Connection limiting, bot detection,
     * IP filtering, attack mitigation, security monitoring.</p>
     * 
     * <p><strong>License:</strong> GPL-3.0 (Fully compatible)</p>
     * <p><strong>Repository:</strong> references/EpicGuard</p>
     * <p><strong>Adaptation Level:</strong> Light modification for Velocity integration</p>
     */
    EPICGUARD("EpicGuard", "references/EpicGuard", "GPL-3.0", true,
        "Connection protection and anti-bot detection systems"),
    
    /**
     * KickRedirect - Server management and player routing.
     * 
     * <p><strong>Primary Extractions:</strong> Player routing logic, server
     * management, kick handling, connection state management.</p>
     * 
     * <p><strong>License:</strong> GPL-3.0 (Fully compatible)</p>
     * <p><strong>Repository:</strong> references/KickRedirect</p>
     * <p><strong>Adaptation Level:</strong> Moderate modification for integration</p>
     */
    KICKREDIRECT("KickRedirect", "references/KickRedirect", "GPL-3.0", true,
        "Server management and player routing systems"),
    
    /**
     * SignedVelocity - Security and authentication patterns.
     * 
     * <p><strong>Primary Extractions:</strong> Authentication systems, security
     * validation, player verification, session management.</p>
     * 
     * <p><strong>License:</strong> GPL-3.0 (Fully compatible)</p>
     * <p><strong>Repository:</strong> references/SignedVelocity</p>
     * <p><strong>Adaptation Level:</strong> Heavy modification for Discord integration</p>
     */
    SIGNEDVELOCITY("SignedVelocity", "references/SignedVelocity", "GPL-3.0", true,
        "Security and authentication pattern implementations"),
    
    /**
     * VLobby - Lobby management and player routing systems.
     * 
     * <p><strong>Primary Extractions:</strong> Lobby management, player teleportation,
     * server routing, queue management, load balancing.</p>
     * 
     * <p><strong>License:</strong> GPL-3.0 (Fully compatible)</p>
     * <p><strong>Repository:</strong> references/VLobby</p>
     * <p><strong>Adaptation Level:</strong> Moderate modification for integration</p>
     */
    VLOBBY("VLobby", "references/VLobby", "GPL-3.0", true,
        "Lobby management and player routing systems"),
    
    /**
     * VPacketEvents - Packet handling and event systems.
     * 
     * <p><strong>Primary Extractions:</strong> Packet processing, event handling,
     * network communication, protocol management.</p>
     * 
     * <p><strong>License:</strong> GPL-3.0 (Fully compatible)</p>
     * <p><strong>Repository:</strong> references/VPacketEvents</p>
     * <p><strong>Adaptation Level:</strong> Light modification for event integration</p>
     */
    VPACKETEVENTS("VPacketEvents", "references/VPacketEvents", "GPL-3.0", true,
        "Packet handling and event system implementations"),
    
    /**
     * VelemonAId - AI integration and Python bridge patterns.
     * 
     * <p><strong>Primary Extractions:</strong> Python bridge implementation,
     * AI service integration, HTTP API patterns, async communication.</p>
     * 
     * <p><strong>License:</strong> Study Only (Custom implementation required)</p>
     * <p><strong>Repository:</strong> references/VelemonAId</p>
     * <p><strong>Adaptation Level:</strong> Complete reimplementation based on patterns</p>
     */
    VELEMONAID("VelemonAId", "references/VelemonAId", "Custom", false,
        "AI integration and Python bridge implementation patterns"),
    
    /**
     * discord-ai-bot - AI chat and LLM integration systems.
     * 
     * <p><strong>Primary Extractions:</strong> LLM integration patterns, AI chat
     * handling, conversation management, response generation.</p>
     * 
     * <p><strong>License:</strong> MIT (Compatible with attribution)</p>
     * <p><strong>Repository:</strong> references/discord-ai-bot</p>
     * <p><strong>Adaptation Level:</strong> Moderate modification for bot integration</p>
     */
    DISCORD_AI_BOT("discord-ai-bot", "references/discord-ai-bot", "MIT", true,
        "AI chat and LLM integration implementation patterns"),
    
    /**
     * HuskChat - Global chat architecture and cross-server messaging.
     * 
     * <p><strong>Primary Extractions:</strong> Cross-server chat bridging,
     * proxy-only architecture, channel management, message routing.</p>
     * 
     * <p><strong>License:</strong> Apache 2.0 (Fully compatible)</p>
     * <p><strong>Repository:</strong> External reference (adapted patterns)</p>
     * <p><strong>Adaptation Level:</strong> Heavy modification for Discord integration</p>
     */
    HUSKCHAT("HuskChat", "references/HuskChat-patterns", "Apache-2.0", true,
        "Cross-server chat bridging and proxy-only architecture patterns");
    
    private final String projectName;
    private final String repositoryPath;
    private final String license;
    private final boolean directExtractionAllowed;
    private final String description;
    
    /**
     * Creates a new reference project enumeration entry.
     * 
     * @param projectName the display name of the project
     * @param repositoryPath the workspace path to the project
     * @param license the license type of the project
     * @param directExtractionAllowed whether code can be directly extracted
     * @param description a description of the project's key extraction targets
     */
    ReferenceProject(String projectName, String repositoryPath, String license, 
                    boolean directExtractionAllowed, String description) {
        this.projectName = projectName;
        this.repositoryPath = repositoryPath;
        this.license = license;
        this.directExtractionAllowed = directExtractionAllowed;
        this.description = description;
    }
    
    /**
     * Gets the display name of the reference project.
     * 
     * @return project display name
     */
    public String getProjectName() {
        return projectName;
    }
    
    /**
     * Gets the workspace path to the reference project.
     * 
     * @return repository path within the workspace
     */
    public String getRepositoryPath() {
        return repositoryPath;
    }
    
    /**
     * Gets the license type of the reference project.
     * 
     * @return license identifier (GPL-3.0, MIT, Apache-2.0, etc.)
     */
    public String getLicense() {
        return license;
    }
    
    /**
     * Checks if direct code extraction is allowed from this project.
     * 
     * @return true if code can be directly extracted, false if patterns only
     */
    public boolean isDirectExtractionAllowed() {
        return directExtractionAllowed;
    }
    
    /**
     * Gets a description of the project's key extraction targets.
     * 
     * @return description of extractable patterns and implementations
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if the project is compatible with GPL-3.0 licensing.
     * 
     * @return true if license is compatible with our project
     */
    public boolean isGPLCompatible() {
        return "GPL-3.0".equals(license) || "Apache-2.0".equals(license) || "MIT".equals(license);
    }
}
