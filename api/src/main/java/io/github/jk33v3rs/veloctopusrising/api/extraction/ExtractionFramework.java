package io.github.jk33v3rs.veloctopusrising.api.extraction;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Modular extraction framework for borrowing code from reference projects.
 * 
 * <p>This interface defines the contract for systematically extracting, adapting,
 * and integrating code patterns from external open-source projects into the
 * Veloctopus Rising system while maintaining proper attribution and license
 * compliance.</p>
 * 
 * <p><strong>Thread Safety:</strong> All operations are async-first with
 * CompletableFuture-based execution, ensuring zero main thread blocking
 * during extraction and integration processes.</p>
 * 
 * <p><strong>Performance Requirements:</strong> Extraction operations should
 * complete within reasonable timeframes while maintaining code quality and
 * proper attribution tracking.</p>
 * 
 * <h3>Reference Project Extraction Strategy:</h3>
 * <ul>
 *   <li><strong>Spicord:</strong> Discord integration patterns for 4-bot architecture</li>
 *   <li><strong>ChatRegulator:</strong> Message filtering and moderation systems</li>
 *   <li><strong>EpicGuard:</strong> Connection protection and anti-bot systems</li>
 *   <li><strong>KickRedirect:</strong> Server management and routing logic</li>
 *   <li><strong>SignedVelocity:</strong> Security and authentication patterns</li>
 *   <li><strong>VLobby:</strong> Lobby management and player routing systems</li>
 *   <li><strong>VPacketEvents:</strong> Packet handling and event systems</li>
 *   <li><strong>VelemonAId:</strong> AI integration and Python bridge patterns</li>
 *   <li><strong>discord-ai-bot:</strong> AI chat and LLM integration systems</li>
 *   <li><strong>HuskChat:</strong> Global chat architecture and cross-server messaging</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * // Extract Discord bot management pattern from Spicord
 * ExtractionFramework framework = createExtractionFramework();
 * 
 * // Configure extraction for Spicord's multi-bot management
 * ExtractionRequest request = ExtractionRequest.builder()
 *     .sourceProject("Spicord")
 *     .targetPackage("io.github.jk33v3rs.veloctopusrising.discord")
 *     .extractionType(ExtractionType.ARCHITECTURAL_PATTERN)
 *     .sourceClasses(List.of("me.opsie.spicord.bot.DiscordBot", 
 *                           "me.opsie.spicord.bot.BotManager"))
 *     .adaptationLevel(AdaptationLevel.HEAVILY_MODIFIED)
 *     .description("Multi-bot management extended for 4-bot architecture")
 *     .build();
 * 
 * // Execute extraction with full attribution tracking
 * CompletableFuture&lt;ExtractionResult&gt; extraction = framework
 *     .extractAsync(request)
 *     .thenCompose(result -&gt; framework.adaptAsync(result))
 *     .thenCompose(adapted -&gt; framework.integrateAsync(adapted));
 * 
 * // Handle completion with proper error handling
 * extraction.thenAccept(result -&gt; {
 *     logger.info("Successfully extracted {} classes from {}", 
 *                 result.getExtractedClasses().size(), 
 *                 result.getSourceProject());
 * }).exceptionally(throwable -&gt; {
 *     logger.error("Extraction failed", throwable);
 *     return null;
 * });
 * </code></pre>
 * 
 * <h3>Extraction Workflow:</h3>
 * <ol>
 *   <li><strong>Analysis:</strong> Analyze source project structure and identify extraction targets</li>
 *   <li><strong>Extraction:</strong> Extract code patterns, interfaces, and implementations</li>
 *   <li><strong>Adaptation:</strong> Modify extracted code for Veloctopus Rising integration</li>
 *   <li><strong>Attribution:</strong> Generate proper attribution and license compliance</li>
 *   <li><strong>Integration:</strong> Integrate adapted code into target package structure</li>
 *   <li><strong>Validation:</strong> Verify extraction quality and functional correctness</li>
 * </ol>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see io.github.jk33v3rs.veloctopusrising.api.attribution.AttributionSystem
 */
public interface ExtractionFramework {
    
    /**
     * Types of code extraction that can be performed.
     */
    enum ExtractionType {
        /**
         * Extract complete class implementations with minimal modification.
         */
        DIRECT_IMPLEMENTATION,
        
        /**
         * Extract and adapt architectural patterns and design approaches.
         */
        ARCHITECTURAL_PATTERN,
        
        /**
         * Extract interface definitions and API contracts.
         */
        INTERFACE_EXTRACTION,
        
        /**
         * Extract configuration and setup patterns.
         */
        CONFIGURATION_PATTERN,
        
        /**
         * Extract utility methods and helper functions.
         */
        UTILITY_EXTRACTION,
        
        /**
         * Extract event handling and listener patterns.
         */
        EVENT_PATTERN,
        
        /**
         * Extract database and persistence patterns.  
         */
        PERSISTENCE_PATTERN,
        
        /**
         * Extract integration and bridge patterns.
         */
        INTEGRATION_PATTERN
    }
    
    /**
     * Levels of adaptation applied during extraction.
     */
    enum AdaptationLevel {
        /**
         * Minimal changes - mostly direct copying with package/naming updates.
         */
        MINIMAL_ADAPTATION,
        
        /**
         * Moderate changes - method signatures and some logic modifications.
         */
        MODERATE_ADAPTATION,
        
        /**
         * Heavy modifications - significant architectural changes and enhancements.
         */
        HEAVILY_MODIFIED,
        
        /**
         * Complete redesign - only core concepts retained from original.
         */
        CONCEPTUAL_ONLY
    }
    
    /**
     * Analyzes a reference project to identify extraction opportunities.
     * 
     * <p>Scans the target project structure, identifies key components,
     * and generates recommendations for code extraction based on the
     * project's needs and architectural requirements.</p>
     * 
     * @param projectPath the path to the reference project source code
     * @param analysisOptions configuration options for the analysis process
     * @return future containing analysis results with extraction recommendations
     * @throws IllegalArgumentException if project path is invalid
     * @throws SecurityException if project access is restricted
     */
    CompletableFuture<ProjectAnalysis> analyzeProjectAsync(String projectPath, 
                                                           AnalysisOptions analysisOptions);
    
    /**
     * Extracts code from a reference project according to the specified request.
     * 
     * <p>Performs the actual code extraction, copying relevant source files,
     * dependencies, and documentation while tracking all borrowed elements
     * for proper attribution.</p>
     * 
     * @param request the extraction request defining what to extract
     * @return future containing the extraction results
     * @throws IllegalArgumentException if request is invalid or incomplete
     * @throws ExtractionException if extraction process fails
     */
    CompletableFuture<ExtractionResult> extractAsync(ExtractionRequest request);
    
    /**
     * Adapts extracted code for integration into Veloctopus Rising.
     * 
     * <p>Modifies package names, class names, method signatures, and
     * implementation details to ensure compatibility with the target
     * system architecture and coding standards.</p>
     * 
     * @param extractionResult the result from a previous extraction operation
     * @return future containing the adapted code ready for integration
     * @throws IllegalArgumentException if extraction result is invalid
     * @throws AdaptationException if adaptation process fails
     */
    CompletableFuture<AdaptationResult> adaptAsync(ExtractionResult extractionResult);
    
    /**
     * Integrates adapted code into the target package structure.
     * 
     * <p>Creates target files, resolves dependencies, updates imports,
     * and ensures the integrated code compiles and functions correctly
     * within the Veloctopus Rising system.</p>
     * 
     * @param adaptationResult the result from a previous adaptation operation
     * @return future containing the integration results and status
     * @throws IllegalArgumentException if adaptation result is invalid
     * @throws IntegrationException if integration process fails
     */
    CompletableFuture<IntegrationResult> integrateAsync(AdaptationResult adaptationResult);
    
    /**
     * Validates the quality and correctness of an extraction process.
     * 
     * <p>Performs compilation checks, runs tests, validates attribution
     * information, and ensures license compliance for all extracted
     * and adapted code.</p>
     * 
     * @param integrationResult the result from a previous integration operation
     * @return future containing validation results and quality metrics
     * @throws IllegalArgumentException if integration result is invalid
     * @throws ValidationException if validation process fails
     */
    CompletableFuture<ValidationResult> validateAsync(IntegrationResult integrationResult);
    
    /**
     * Generates attribution information for extracted code.
     * 
     * <p>Creates comprehensive attribution documentation including
     * source project information, license details, adaptation notes,
     * and compliance requirements for all borrowed code.</p>
     * 
     * @param extractionResult the extraction result to generate attribution for
     * @return future containing complete attribution information
     * @throws IllegalArgumentException if extraction result is invalid
     * @throws AttributionException if attribution generation fails
     */
    CompletableFuture<AttributionInfo> generateAttributionAsync(ExtractionResult extractionResult);
    
    /**
     * Gets a list of all available reference projects for extraction.
     * 
     * <p>Returns information about all reference projects that have been
     * configured for extraction, including their current status,
     * available components, and extraction recommendations.</p>
     * 
     * @return future containing list of available reference projects
     */
    CompletableFuture<List<ReferenceProject>> getAvailableProjectsAsync();
    
    /**
     * Gets the extraction history for tracking and audit purposes.
     * 
     * <p>Returns a comprehensive history of all extraction operations
     * performed, including timestamps, source projects, extraction
     * details, and attribution information.</p>
     * 
     * @return future containing complete extraction history
     */
    CompletableFuture<List<ExtractionHistory>> getExtractionHistoryAsync();
    
    /**
     * Gets extraction recommendations based on current system needs.
     * 
     * <p>Analyzes the current state of the Veloctopus Rising system
     * and recommends specific extraction operations from reference
     * projects that would most benefit the development process.</p>
     * 
     * @param systemAnalysis current analysis of the target system
     * @return future containing prioritized extraction recommendations
     * @throws IllegalArgumentException if system analysis is invalid
     */
    CompletableFuture<List<ExtractionRecommendation>> getRecommendationsAsync(SystemAnalysis systemAnalysis);
    
    /**
     * Configures the extraction framework with project-specific settings.
     * 
     * <p>Sets up extraction rules, attribution requirements, quality
     * standards, and integration patterns for the target project.</p>
     * 
     * @param configuration the framework configuration settings
     * @return future indicating configuration completion
     * @throws IllegalArgumentException if configuration is invalid
     * @throws ConfigurationException if configuration process fails
     */
    CompletableFuture<Void> configureAsync(ExtractionConfiguration configuration);
    
    /**
     * Gets the current status and statistics of the extraction framework.
     * 
     * <p>Returns real-time information about framework health,
     * performance metrics, active extractions, and system utilization.</p>
     * 
     * @return future containing current framework status
     */
    CompletableFuture<FrameworkStatus> getStatusAsync();
    
    /**
     * Shuts down the extraction framework gracefully.
     * 
     * <p>Completes any active extraction operations, saves state
     * information, and releases all system resources.</p>
     * 
     * @return future indicating shutdown completion
     */
    CompletableFuture<Void> shutdownAsync();
    
    /**
     * Exception thrown when extraction operations fail.
     */
    class ExtractionException extends RuntimeException {
        public ExtractionException(String message) {
            super(message);
        }
        
        public ExtractionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Exception thrown when adaptation operations fail.
     */
    class AdaptationException extends RuntimeException {
        public AdaptationException(String message) {
            super(message);
        }
        
        public AdaptationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Exception thrown when integration operations fail.
     */
    class IntegrationException extends RuntimeException {
        public IntegrationException(String message) {
            super(message);
        }
        
        public IntegrationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Exception thrown when validation operations fail.
     */
    class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
        
        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Exception thrown when attribution generation fails.
     */
    class AttributionException extends RuntimeException {
        public AttributionException(String message) {
            super(message);
        }
        
        public AttributionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Exception thrown when framework configuration fails.
     */
    class ConfigurationException extends RuntimeException {
        public ConfigurationException(String message) {
            super(message);
        }
        
        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
