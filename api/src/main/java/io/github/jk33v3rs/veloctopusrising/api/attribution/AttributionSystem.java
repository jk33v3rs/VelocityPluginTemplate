package io.github.jk33v3rs.veloctopusrising.api.attribution;

import java.util.List;
import java.util.Optional;

/**
 * Code borrowing guidelines and attribution system for Veloctopus Rising.
 * 
 * <p>This interface defines the contract for tracking, attributing, and managing
 * borrowed code from open-source projects in the Veloctopus Rising system, ensuring
 * proper license compliance and attribution requirements are met.</p>
 * 
 * <p><strong>Legal Compliance:</strong> All borrowed code must be properly tracked
 * and attributed according to the source project's license requirements.</p>
 * 
 * <p><strong>Performance Requirements:</strong> Attribution tracking should have
 * minimal runtime overhead - primarily used for documentation and compliance.</p>
 * 
 * <h3>Code Borrowing Strategy (67% Minimum):</h3>
 * <ul>
 *   <li><strong>Spicord:</strong> Multi-bot Discord integration patterns</li>
 *   <li><strong>HuskChat:</strong> Cross-server chat bridging implementation</li>
 *   <li><strong>VeloctopusProject:</strong> 175-rank system and verification workflow</li>
 *   <li><strong>DiscordSRV:</strong> Rich embed aesthetics and message formatting</li>
 *   <li><strong>Adrian3D Ecosystem:</strong> Lightweight Velocity patterns</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * // Register borrowed code attribution
 * AttributionSystem attribution = new AttributionSystemImpl();
 * 
 * attribution.registerBorrowedCode(
 *     "io.github.jk33v3rs.veloctopusrising.discord.BotManager",
 *     BorrowedCodeInfo.builder()
 *         .sourceProject("Spicord")
 *         .sourceClass("SpicordBot")
 *         .license("GPL-3.0")
 *         .adaptationLevel(AdaptationLevel.HEAVILY_MODIFIED)
 *         .description("Multi-bot management pattern adapted for 4-bot architecture")
 *         .build()
 * );
 * 
 * // Generate attribution report
 * attribution.generateAttributionReport()
 *     .thenAccept(report -&gt; {
 *         logger.info("Attribution report generated: {}", report.getReportPath());
 *     });
 * </code></pre>
 * 
 * <h3>Integration Points:</h3>
 * <ul>
 *   <li>Build system for automatic attribution file generation</li>
 *   <li>Documentation system for developer reference</li>
 *   <li>Legal compliance reporting and validation</li>
 *   <li>Code review process for borrowed code approval</li>
 * </ul>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see BorrowedCodeInfo
 * @see AttributionReport
 */
public interface AttributionSystem {
    
    /**
     * Levels of adaptation for borrowed code classification.
     * 
     * <p>Defines how extensively the original code has been modified
     * to help determine appropriate attribution requirements.</p>
     * 
     * @since 1.0.0
     */
    enum AdaptationLevel {
        /** Code used exactly as-is with no modifications */
        VERBATIM,
        /** Minor modifications like variable names or formatting */
        LIGHTLY_MODIFIED,
        /** Significant structural changes but core logic preserved */
        MODERATELY_MODIFIED,
        /** Extensive modifications with only patterns or concepts borrowed */
        HEAVILY_MODIFIED,
        /** Only inspiration or high-level concepts taken */
        CONCEPTUAL_INSPIRATION
    }
    
    /**
     * License types and their attribution requirements.
     * 
     * <p>Common open-source licenses and their specific requirements
     * for attribution and compliance.</p>
     * 
     * @since 1.0.0
     */
    enum LicenseType {
        /** MIT License - minimal attribution required */
        MIT("MIT", "Copyright notice and license text required"),
        /** Apache 2.0 - moderate attribution requirements */
        APACHE_2_0("Apache-2.0", "Copyright notice, license text, and changes documented"),
        /** GPL v3 - copyleft license with strong requirements */
        GPL_3_0("GPL-3.0", "Full source disclosure and license compatibility required"),
        /** BSD 3-Clause - attribution with advertising clause */
        BSD_3_CLAUSE("BSD-3-Clause", "Copyright notice and disclaimer required"),
        /** Creative Commons - various CC licenses */
        CREATIVE_COMMONS("CC-BY-SA-4.0", "Attribution and share-alike required"),
        /** Public Domain - no restrictions */
        PUBLIC_DOMAIN("Public Domain", "No attribution required but good practice"),
        /** Custom license - project-specific terms */
        CUSTOM("Custom", "Refer to original project license terms");
        
        private final String identifier;
        private final String requirements;
        
        LicenseType(String identifier, String requirements) {
            this.identifier = identifier;
            this.requirements = requirements;
        }
        
        public String getIdentifier() { return identifier; }
        public String getRequirements() { return requirements; }
    }
    
    /**
     * Registers borrowed code with full attribution information.
     * 
     * <p>Records the use of borrowed code from external projects,
     * including source information, license details, and adaptation level
     * for compliance tracking and attribution generation.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe operation for concurrent registration.</p>
     * 
     * <p><strong>Performance:</strong> O(1) registration with internal indexing.</p>
     * 
     * <h4>Registration Requirements:</h4>
     * <ul>
     *   <li>Full class/package path in Veloctopus Rising codebase</li>
     *   <li>Source project name and original class/file reference</li>
     *   <li>License type and specific attribution requirements</li>
     *   <li>Level of adaptation and modification description</li>
     * </ul>
     * 
     * <h4>Usage Example:</h4>
     * <pre><code>
     * // Register Spicord bot management pattern
     * attributionSystem.registerBorrowedCode(
     *     "io.github.jk33v3rs.veloctopusrising.discord.MultiBot",
     *     BorrowedCodeInfo.builder()
     *         .sourceProject("Spicord")
     *         .sourceRepository("https://github.com/OopsieWoopsie/Spicord")
     *         .sourceClass("me.opsie.spicord.bot.DiscordBot")
     *         .license(LicenseType.GPL_3_0)
     *         .adaptationLevel(AdaptationLevel.HEAVILY_MODIFIED)
     *         .description("Multi-bot management pattern extended for 4-bot architecture")
     *         .originalAuthor("OopsieWoopsie")
     *         .borrowedElements(List.of("Bot lifecycle management", "Configuration loading"))
     *         .build()
     * );
     * </code></pre>
     * 
     * @param targetClass the full class path where borrowed code is used
     * @param borrowedInfo complete information about the borrowed code
     * @throws IllegalArgumentException if targetClass or borrowedInfo is null
     * @throws IllegalStateException if code is already registered for this class
     * @since 1.0.0
     */
    void registerBorrowedCode(String targetClass, BorrowedCodeInfo borrowedInfo);
    
    /**
     * Updates existing borrowed code attribution information.
     * 
     * <p>Modifies the attribution information for previously registered
     * borrowed code, useful when adaptation level changes during development.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe update operation.</p>
     * 
     * <p><strong>Performance:</strong> O(1) update with versioning support.</p>
     * 
     * @param targetClass the class path to update
     * @param updatedInfo the new attribution information
     * @throws IllegalArgumentException if targetClass or updatedInfo is null
     * @throws IllegalStateException if no existing registration found
     * @since 1.0.0
     */
    void updateBorrowedCode(String targetClass, BorrowedCodeInfo updatedInfo);
    
    /**
     * Removes borrowed code attribution registration.
     * 
     * <p>Removes the attribution record when borrowed code is no longer
     * used or has been completely rewritten.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe removal operation.</p>
     * 
     * <p><strong>Performance:</strong> O(1) removal with audit trail.</p>
     * 
     * @param targetClass the class path to remove
     * @return true if registration was found and removed, false otherwise
     * @since 1.0.0
     */
    boolean removeBorrowedCode(String targetClass);
    
    /**
     * Retrieves attribution information for a specific class.
     * 
     * <p>Returns the complete attribution information for borrowed code
     * in the specified class, if any exists.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe read operation.</p>
     * 
     * <p><strong>Performance:</strong> O(1) lookup with caching.</p>
     * 
     * @param targetClass the class path to look up
     * @return attribution information if found, empty if not registered
     * @throws IllegalArgumentException if targetClass is null
     * @since 1.0.0
     */
    Optional<BorrowedCodeInfo> getBorrowedCodeInfo(String targetClass);
    
    /**
     * Gets all borrowed code attributions by source project.
     * 
     * <p>Returns all attribution records for a specific source project,
     * useful for generating project-specific attribution sections.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe read operation.</p>
     * 
     * <p><strong>Performance:</strong> O(n) where n is total registrations.</p>
     * 
     * @param sourceProject the source project name to filter by
     * @return list of attribution information for the project
     * @throws IllegalArgumentException if sourceProject is null
     * @since 1.0.0
     */
    List<BorrowedCodeInfo> getBorrowedCodeByProject(String sourceProject);
    
    /**
     * Gets all borrowed code attributions by license type.
     * 
     * <p>Returns all attribution records for a specific license type,
     * useful for license compliance reporting.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe read operation.</p>
     * 
     * <p><strong>Performance:</strong> O(n) where n is total registrations.</p>
     * 
     * @param licenseType the license type to filter by
     * @return list of attribution information for the license
     * @throws IllegalArgumentException if licenseType is null
     * @since 1.0.0
     */
    List<BorrowedCodeInfo> getBorrowedCodeByLicense(LicenseType licenseType);
    
    /**
     * Gets all registered borrowed code attributions.
     * 
     * <p>Returns the complete list of all borrowed code attributions
     * registered in the system.</p>
     * 
     * <p><strong>Threading:</strong> Thread-safe read operation.</p>
     * 
     * <p><strong>Performance:</strong> O(n) where n is total registrations.</p>
     * 
     * @return list of all attribution information
     * @since 1.0.0
     */
    List<BorrowedCodeInfo> getAllBorrowedCode();
    
    /**
     * Validates license compatibility across all borrowed code.
     * 
     * <p>Checks that all borrowed code licenses are compatible with each
     * other and with the project's overall license strategy.</p>
     * 
     * <p><strong>Threading:</strong> Async validation operation.</p>
     * 
     * <p><strong>Performance:</strong> May take several seconds for complex validation.</p>
     * 
     * @return validation result with any compatibility issues
     * @since 1.0.0
     */
    LicenseCompatibilityResult validateLicenseCompatibility();
    
    /**
     * Calculates the percentage of borrowed code in the project.
     * 
     * <p>Analyzes the codebase to determine what percentage consists of
     * borrowed code versus original implementation.</p>
     * 
     * <p><strong>Threading:</strong> Async calculation operation.</p>
     * 
     * <p><strong>Performance:</strong> May take time for full codebase analysis.</p>
     * 
     * @return borrowed code percentage analysis
     * @since 1.0.0
     */
    BorrowedCodeAnalysis calculateBorrowedCodePercentage();
    
    /**
     * Generates a comprehensive attribution report.
     * 
     * <p>Creates a detailed report of all borrowed code, organized by
     * project, license, and adaptation level for documentation purposes.</p>
     * 
     * <p><strong>Threading:</strong> Async report generation.</p>
     * 
     * <p><strong>Performance:</strong> Report generation time depends on
     * number of attributions and output format.</p>
     * 
     * @param format the desired output format (MARKDOWN, HTML, PDF, JSON)
     * @return report generation result with file path
     * @since 1.0.0
     */
    java.util.concurrent.CompletableFuture<AttributionReport> generateAttributionReport(ReportFormat format);
    
    /**
     * Generates attribution text for inclusion in documentation.
     * 
     * <p>Creates properly formatted attribution text that can be included
     * in README files, documentation, or other attribution sections.</p>
     * 
     * <p><strong>Threading:</strong> Synchronous text generation.</p>
     * 
     * <p><strong>Performance:</strong> Fast text generation, typically &lt;100ms.</p>
     * 
     * @return formatted attribution text
     * @since 1.0.0
     */
    String generateAttributionText();
    
    /**
     * Supported report formats for attribution generation.
     * 
     * @since 1.0.0
     */
    enum ReportFormat {
        MARKDOWN, HTML, PDF, JSON, PLAIN_TEXT
    }
    
    /**
     * Result of license compatibility validation.
     * 
     * @since 1.0.0
     */
    interface LicenseCompatibilityResult {
        boolean isCompatible();
        List<String> getCompatibilityIssues();
        List<String> getWarnings();
        List<String> getRecommendations();
    }
    
    /**
     * Analysis of borrowed code percentage in the project.
     * 
     * @since 1.0.0
     */
    interface BorrowedCodeAnalysis {
        double getBorrowedCodePercentage();
        double getOriginalCodePercentage();
        int getTotalClasses();
        int getBorrowedClasses();
        int getOriginalClasses();
        List<String> getLargestBorrowedComponents();
        boolean meetsMinimumBorrowedCodeTarget();
    }
    
    /**
     * Generated attribution report with metadata.
     * 
     * @since 1.0.0
     */
    interface AttributionReport {
        String getReportPath();
        ReportFormat getFormat();
        int getTotalAttributions();
        java.time.Instant getGeneratedAt();
        String getSummary();
    }
}
