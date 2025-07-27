package io.github.jk33v3rs.veloctopusrising.api.extraction;

import java.time.Instant;
import java.util.List;

/**
 * Attribution information for extracted code patterns.
 * 
 * <p>This immutable class tracks the origin, licensing, and attribution
 * requirements for code patterns extracted from reference projects.
 * Essential for maintaining proper open-source license compliance.</p>
 * 
 * <p><strong>Thread Safety:</strong> Immutable class, fully thread-safe.</p>
 * 
 * <p><strong>Legal Compliance:</strong> This class ensures we maintain proper
 * attribution for all borrowed code, meeting GPL-3.0 and other license
 * requirements for the Veloctopus Rising project.</p>
 * 
 * <h3>Attribution Requirements:</h3>
 * <ul>
 *   <li><strong>GPL-3.0:</strong> Source disclosure, license compatibility</li>
 *   <li><strong>Apache/MIT:</strong> Attribution in documentation and credits</li>
 *   <li><strong>Custom:</strong> Follow specific project requirements</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * PatternAttribution attribution = PatternAttribution.builder()
 *     .originalProject("Spicord")
 *     .originalAuthors(List.of("OpsieGuy"))
 *     .licenseType("GPL-3.0")
 *     .repositoryUrl("https://github.com/Spicord/Spicord")
 *     .extractedFiles(List.of("src/main/java/me/opsie/spicord/bot/BotManager.java"))
 *     .adaptationNotes("Modified for 4-bot architecture with async patterns")
 *     .build();
 * </code></pre>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see CodePattern
 * @see ExtractionFramework
 */
public final class PatternAttribution {
    
    private final String originalProject;
    private final List<String> originalAuthors;
    private final String licenseType;
    private final String repositoryUrl;
    private final List<String> extractedFiles;
    private final String adaptationNotes;
    private final Instant attributionCreated;
    private final String copyright;
    private final boolean requiresSourceDisclosure;
    private final boolean requiresAttribution;
    private final String licenseUrl;
    
    /**
     * Creates a new PatternAttribution instance.
     * 
     * @param builder the builder containing attribution information
     */
    private PatternAttribution(Builder builder) {
        this.originalProject = builder.originalProject;
        this.originalAuthors = List.copyOf(builder.originalAuthors);
        this.licenseType = builder.licenseType;
        this.repositoryUrl = builder.repositoryUrl;
        this.extractedFiles = List.copyOf(builder.extractedFiles);
        this.adaptationNotes = builder.adaptationNotes;
        this.attributionCreated = builder.attributionCreated;
        this.copyright = builder.copyright;
        this.requiresSourceDisclosure = builder.requiresSourceDisclosure;
        this.requiresAttribution = builder.requiresAttribution;
        this.licenseUrl = builder.licenseUrl;
    }
    
    /**
     * Gets the name of the original project.
     * 
     * @return original project name (never null)
     */
    public String getOriginalProject() {
        return originalProject;
    }
    
    /**
     * Gets the list of original authors/contributors.
     * 
     * @return immutable list of author names
     */
    public List<String> getOriginalAuthors() {
        return originalAuthors;
    }
    
    /**
     * Gets the license type of the original code.
     * 
     * @return license identifier (GPL-3.0, MIT, Apache-2.0, etc.)
     */
    public String getLicenseType() {
        return licenseType;
    }
    
    /**
     * Gets the repository URL of the original project.
     * 
     * @return repository URL or null if not available
     */
    public String getRepositoryUrl() {
        return repositoryUrl;
    }
    
    /**
     * Gets the list of files that were extracted from the original project.
     * 
     * @return immutable list of file paths
     */
    public List<String> getExtractedFiles() {
        return extractedFiles;
    }
    
    /**
     * Gets notes about how the pattern was adapted.
     * 
     * @return adaptation notes (never null)
     */
    public String getAdaptationNotes() {
        return adaptationNotes;
    }
    
    /**
     * Gets the timestamp when this attribution was created.
     * 
     * @return attribution creation timestamp (never null)
     */
    public Instant getAttributionCreated() {
        return attributionCreated;
    }
    
    /**
     * Gets the copyright information for the original code.
     * 
     * @return copyright string or null if not specified
     */
    public String getCopyright() {
        return copyright;
    }
    
    /**
     * Checks if the license requires source code disclosure.
     * 
     * @return true if source disclosure is required (e.g., GPL licenses)
     */
    public boolean requiresSourceDisclosure() {
        return requiresSourceDisclosure;
    }
    
    /**
     * Checks if the license requires attribution in documentation.
     * 
     * @return true if attribution is required
     */
    public boolean requiresAttribution() {
        return requiresAttribution;
    }
    
    /**
     * Gets the URL to the full license text.
     * 
     * @return license URL or null if not available
     */
    public String getLicenseUrl() {
        return licenseUrl;
    }
    
    /**
     * Generates a standard attribution notice for documentation.
     * 
     * @return formatted attribution notice
     */
    public String generateAttributionNotice() {
        StringBuilder notice = new StringBuilder();
        notice.append("Portions of this code are derived from ").append(originalProject);
        
        if (!originalAuthors.isEmpty()) {
            notice.append(" by ").append(String.join(", ", originalAuthors));
        }
        
        notice.append(" (").append(licenseType).append(")");
        
        if (repositoryUrl != null) {
            notice.append(" - ").append(repositoryUrl);
        }
        
        if (!adaptationNotes.isEmpty()) {
            notice.append("\nAdaptations: ").append(adaptationNotes);
        }
        
        return notice.toString();
    }
    
    /**
     * Checks if this attribution is compatible with GPL-3.0.
     * 
     * @return true if compatible with GPL-3.0 licensing
     */
    public boolean isGPLCompatible() {
        return "GPL-3.0".equals(licenseType) || 
               "Apache-2.0".equals(licenseType) || 
               "MIT".equals(licenseType) ||
               "BSD-3-Clause".equals(licenseType);
    }
    
    /**
     * Creates a new builder for constructing PatternAttribution instances.
     * 
     * @return new attribution builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for creating PatternAttribution instances.
     */
    public static final class Builder {
        private String originalProject;
        private List<String> originalAuthors = List.of();
        private String licenseType;
        private String repositoryUrl;
        private List<String> extractedFiles = List.of();
        private String adaptationNotes = "";
        private Instant attributionCreated = Instant.now();
        private String copyright;
        private boolean requiresSourceDisclosure = false;
        private boolean requiresAttribution = true;
        private String licenseUrl;
        
        private Builder() {}
        
        public Builder originalProject(String originalProject) {
            this.originalProject = originalProject;
            return this;
        }
        
        public Builder originalAuthors(List<String> originalAuthors) {
            this.originalAuthors = originalAuthors != null ? originalAuthors : List.of();
            return this;
        }
        
        public Builder licenseType(String licenseType) {
            this.licenseType = licenseType;
            // Auto-configure requirements based on license type
            if ("GPL-3.0".equals(licenseType) || "GPL-2.0".equals(licenseType)) {
                this.requiresSourceDisclosure = true;
                this.requiresAttribution = true;
            } else if ("MIT".equals(licenseType) || "Apache-2.0".equals(licenseType)) {
                this.requiresSourceDisclosure = false;
                this.requiresAttribution = true;
            }
            return this;
        }
        
        public Builder repositoryUrl(String repositoryUrl) {
            this.repositoryUrl = repositoryUrl;
            return this;
        }
        
        public Builder extractedFiles(List<String> extractedFiles) {
            this.extractedFiles = extractedFiles != null ? extractedFiles : List.of();
            return this;
        }
        
        public Builder adaptationNotes(String adaptationNotes) {
            this.adaptationNotes = adaptationNotes != null ? adaptationNotes : "";
            return this;
        }
        
        public Builder attributionCreated(Instant attributionCreated) {
            this.attributionCreated = attributionCreated != null ? attributionCreated : Instant.now();
            return this;
        }
        
        public Builder copyright(String copyright) {
            this.copyright = copyright;
            return this;
        }
        
        public Builder requiresSourceDisclosure(boolean requiresSourceDisclosure) {
            this.requiresSourceDisclosure = requiresSourceDisclosure;
            return this;
        }
        
        public Builder requiresAttribution(boolean requiresAttribution) {
            this.requiresAttribution = requiresAttribution;
            return this;
        }
        
        public Builder licenseUrl(String licenseUrl) {
            this.licenseUrl = licenseUrl;
            return this;
        }
        
        /**
         * Builds the PatternAttribution instance with validation.
         * 
         * @return new attribution instance
         * @throws IllegalStateException if required fields are missing
         */
        public PatternAttribution build() {
            if (originalProject == null || originalProject.trim().isEmpty()) {
                throw new IllegalStateException("Original project name is required");
            }
            if (licenseType == null || licenseType.trim().isEmpty()) {
                throw new IllegalStateException("License type is required");
            }
            
            return new PatternAttribution(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "PatternAttribution{project='%s', license='%s', authors=%d, files=%d}",
            originalProject, licenseType, originalAuthors.size(), extractedFiles.size());
    }
}
