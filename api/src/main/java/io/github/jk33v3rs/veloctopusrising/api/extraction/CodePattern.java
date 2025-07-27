package io.github.jk33v3rs.veloctopusrising.api.extraction;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Represents an extracted code pattern from a reference project.
 * 
 * <p>This immutable class contains all information about a code pattern that has
 * been extracted from a reference project, including the source code, metadata,
 * attribution information, and adaptation requirements.</p>
 * 
 * <p><strong>Thread Safety:</strong> Immutable class, fully thread-safe for
 * concurrent access across multiple threads.</p>
 * 
 * <p><strong>Performance:</strong> Lightweight value object optimized for
 * serialization and caching. Source code is stored as compressed strings
 * when possible to minimize memory footprint.</p>
 * 
 * <h3>Pattern Types:</h3>
 * <ul>
 *   <li><strong>Class Pattern:</strong> Complete class implementations</li>
 *   <li><strong>Method Pattern:</strong> Specific method implementations</li>
 *   <li><strong>Interface Pattern:</strong> API contracts and interfaces</li>
 *   <li><strong>Architecture Pattern:</strong> Structural designs and frameworks</li>
 *   <li><strong>Configuration Pattern:</strong> Configuration and setup logic</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * CodePattern pattern = CodePattern.builder()
 *     .name("multi-bot-management")
 *     .source(ReferenceProject.SPICORD)
 *     .sourceCode(extractedCode)
 *     .patternType(PatternType.ARCHITECTURE_PATTERN)
 *     .dependencies(Set.of("net.dv8tion.jda", "com.github.opsie.spicord"))
 *     .adaptationLevel(AdaptationLevel.HEAVY_MODIFICATION)
 *     .build();
 * 
 * // Use pattern for adaptation and integration
 * CompletableFuture&lt;CodePattern&gt; adapted = 
 *     extractionFramework.adaptPattern(pattern, AdaptationTarget.FOUR_BOT_SYSTEM);
 * </code></pre>
 * 
 * <h3>Integration Points:</h3>
 * <ul>
 *   <li>Extraction framework for pattern storage and retrieval</li>
 *   <li>Adaptation engine for transformation and integration</li>
 *   <li>Attribution system for license compliance tracking</li>
 *   <li>Code generation pipeline for final implementation</li>
 * </ul>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see ExtractionFramework
 * @see ReferenceProject
 * @see AdaptationTarget
 */
public final class CodePattern {
    
    /**
     * Type of code pattern extracted from reference project.
     */
    public enum PatternType {
        /** Complete class implementation with all methods */
        CLASS_PATTERN,
        /** Specific method or function implementation */
        METHOD_PATTERN,
        /** Interface or API contract definition */
        INTERFACE_PATTERN,
        /** Architectural design or framework structure */
        ARCHITECTURE_PATTERN,
        /** Configuration setup or initialization logic */
        CONFIGURATION_PATTERN,
        /** Event handling or callback pattern */
        EVENT_PATTERN,
        /** Data access or persistence pattern */
        DATA_ACCESS_PATTERN,
        /** Utility or helper function collection */
        UTILITY_PATTERN
    }
    
    /**
     * Level of adaptation required for integration.
     */
    public enum AdaptationLevel {
        /** Can be used with minimal changes (imports, package names) */
        MINIMAL_MODIFICATION,
        /** Requires moderate changes (API alignment, async conversion) */
        MODERATE_MODIFICATION,
        /** Requires heavy modification (architecture changes, major refactoring) */
        HEAVY_MODIFICATION,
        /** Patterns only - complete reimplementation required */
        PATTERN_ONLY
    }
    
    private final String name;
    private final ReferenceProject source;
    private final String sourceCode;
    private final PatternType patternType;
    private final AdaptationLevel adaptationLevel;
    private final Set<String> dependencies;
    private final Map<String, String> metadata;
    private final String description;
    private final Instant extractedAt;
    private final String extractorVersion;
    private final PatternAttribution attribution;
    
    /**
     * Creates a new CodePattern instance.
     * 
     * @param builder the builder containing all pattern information
     */
    private CodePattern(Builder builder) {
        this.name = builder.name;
        this.source = builder.source;
        this.sourceCode = builder.sourceCode;
        this.patternType = builder.patternType;
        this.adaptationLevel = builder.adaptationLevel;
        this.dependencies = Set.copyOf(builder.dependencies);
        this.metadata = Map.copyOf(builder.metadata);
        this.description = builder.description;
        this.extractedAt = builder.extractedAt;
        this.extractorVersion = builder.extractorVersion;
        this.attribution = builder.attribution;
    }
    
    /**
     * Gets the unique name of this code pattern.
     * 
     * @return pattern name (never null)
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the reference project this pattern was extracted from.
     * 
     * @return source project (never null)
     */
    public ReferenceProject getSource() {
        return source;
    }
    
    /**
     * Gets the extracted source code.
     * 
     * @return source code as string (never null)
     */
    public String getSourceCode() {
        return sourceCode;
    }
    
    /**
     * Gets the type of this code pattern.
     * 
     * @return pattern type (never null)
     */
    public PatternType getPatternType() {
        return patternType;
    }
    
    /**
     * Gets the level of adaptation required for integration.
     * 
     * @return adaptation level (never null)
     */
    public AdaptationLevel getAdaptationLevel() {
        return adaptationLevel;
    }
    
    /**
     * Gets the set of dependencies required by this pattern.
     * 
     * @return immutable set of dependency identifiers
     */
    public Set<String> getDependencies() {
        return dependencies;
    }
    
    /**
     * Gets metadata associated with this pattern.
     * 
     * @return immutable map of metadata key-value pairs
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    /**
     * Gets a human-readable description of this pattern.
     * 
     * @return pattern description (never null)
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the timestamp when this pattern was extracted.
     * 
     * @return extraction timestamp (never null)
     */
    public Instant getExtractedAt() {
        return extractedAt;
    }
    
    /**
     * Gets the version of the extractor that created this pattern.
     * 
     * @return extractor version string (never null)
     */
    public String getExtractorVersion() {
        return extractorVersion;
    }
    
    /**
     * Gets the attribution information for this pattern.
     * 
     * @return attribution details (never null)
     */
    public PatternAttribution getAttribution() {
        return attribution;
    }
    
    /**
     * Estimates the complexity of adapting this pattern.
     * 
     * @return complexity score from 1 (simple) to 10 (very complex)
     */
    public int getComplexityScore() {
        int baseScore = switch (adaptationLevel) {
            case MINIMAL_MODIFICATION -> 2;
            case MODERATE_MODIFICATION -> 5;
            case HEAVY_MODIFICATION -> 8;
            case PATTERN_ONLY -> 10;
        };
        
        // Adjust based on dependencies and source code size
        int dependencyAdjustment = Math.min(dependencies.size() / 3, 2);
        int sizeAdjustment = sourceCode.length() > 10000 ? 1 : 0;
        
        return Math.min(10, Math.max(1, baseScore + dependencyAdjustment + sizeAdjustment));
    }
    
    /**
     * Checks if this pattern is compatible with GPL-3.0 licensing.
     * 
     * @return true if pattern can be integrated under GPL-3.0
     */
    public boolean isGPLCompatible() {
        return source.isGPLCompatible();
    }
    
    /**
     * Gets a metadata value by key.
     * 
     * @param key the metadata key
     * @return metadata value, or null if not found
     */
    public String getMetadata(String key) {
        return metadata.get(key);
    }
    
    /**
     * Creates a new builder for constructing CodePattern instances.
     * 
     * @return new pattern builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for creating CodePattern instances.
     */
    public static final class Builder {
        private String name;
        private ReferenceProject source;
        private String sourceCode;
        private PatternType patternType;
        private AdaptationLevel adaptationLevel;
        private Set<String> dependencies = Set.of();
        private Map<String, String> metadata = Map.of();
        private String description = "";
        private Instant extractedAt = Instant.now();
        private String extractorVersion = "1.0.0";
        private PatternAttribution attribution;
        
        private Builder() {}
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder source(ReferenceProject source) {
            this.source = source;
            return this;
        }
        
        public Builder sourceCode(String sourceCode) {
            this.sourceCode = sourceCode;
            return this;
        }
        
        public Builder patternType(PatternType patternType) {
            this.patternType = patternType;
            return this;
        }
        
        public Builder adaptationLevel(AdaptationLevel adaptationLevel) {
            this.adaptationLevel = adaptationLevel;
            return this;
        }
        
        public Builder dependencies(Set<String> dependencies) {
            this.dependencies = dependencies != null ? dependencies : Set.of();
            return this;
        }
        
        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata != null ? metadata : Map.of();
            return this;
        }
        
        public Builder description(String description) {
            this.description = description != null ? description : "";
            return this;
        }
        
        public Builder extractedAt(Instant extractedAt) {
            this.extractedAt = extractedAt != null ? extractedAt : Instant.now();
            return this;
        }
        
        public Builder extractorVersion(String extractorVersion) {
            this.extractorVersion = extractorVersion != null ? extractorVersion : "1.0.0";
            return this;
        }
        
        public Builder attribution(PatternAttribution attribution) {
            this.attribution = attribution;
            return this;
        }
        
        /**
         * Builds the CodePattern instance with validation.
         * 
         * @return new pattern instance
         * @throws IllegalStateException if required fields are missing
         */
        public CodePattern build() {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalStateException("Pattern name is required");
            }
            if (source == null) {
                throw new IllegalStateException("Source project is required");
            }
            if (sourceCode == null) {
                throw new IllegalStateException("Source code is required");
            }
            if (patternType == null) {
                throw new IllegalStateException("Pattern type is required");
            }
            if (adaptationLevel == null) {
                throw new IllegalStateException("Adaptation level is required");
            }
            if (attribution == null) {
                throw new IllegalStateException("Attribution information is required");
            }
            
            return new CodePattern(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "CodePattern{name='%s', source=%s, type=%s, adaptation=%s, dependencies=%d}",
            name, source.getProjectName(), patternType, adaptationLevel, dependencies.size());
    }
}
