package io.github.jk33v3rs.veloctopusrising.api.attribution;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Comprehensive information about borrowed code from external projects.
 * 
 * <p>This immutable class contains all necessary information for proper attribution
 * and license compliance when using code borrowed from open-source projects
 * in the Veloctopus Rising system.</p>
 * 
 * <p><strong>Thread Safety:</strong> Immutable class, fully thread-safe for
 * concurrent access across multiple threads.</p>
 * 
 * <p><strong>Performance:</strong> Lightweight value object optimized for
 * minimal memory footprint and fast serialization for reporting systems.</p>
 * 
 * <h3>Usage Example:</h3>
 * <pre><code>
 * // Create attribution info for borrowed Spicord code
 * BorrowedCodeInfo spicordAttribution = BorrowedCodeInfo.builder()
 *     .sourceProject("Spicord")
 *     .sourceRepository("https://github.com/OopsieWoopsie/Spicord")
 *     .sourceClass("me.opsie.spicord.bot.DiscordBot")
 *     .originalAuthor("OopsieWoopsie")
 *     .license(AttributionSystem.LicenseType.GPL_3_0)
 *     .adaptationLevel(AttributionSystem.AdaptationLevel.HEAVILY_MODIFIED)
 *     .description("Multi-bot management pattern extended for 4-bot architecture")
 *     .borrowedElements(List.of("Bot lifecycle management", "Event distribution"))
 *     .build();
 * 
 * // Use in attribution system
 * attributionSystem.registerBorrowedCode(
 *     "io.github.jk33v3rs.veloctopusrising.discord.BotManager", 
 *     spicordAttribution
 * );
 * </code></pre>
 * 
 * <h3>Builder Pattern Benefits:</h3>
 * <ul>
 *   <li><strong>Validation:</strong> Ensures all required fields are provided</li>
 *   <li><strong>Immutability:</strong> Creates immutable objects for thread safety</li>
 *   <li><strong>Flexibility:</strong> Optional fields with sensible defaults</li>
 *   <li><strong>Clarity:</strong> Self-documenting API for attribution creation</li>
 * </ul>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see AttributionSystem
 */
public final class BorrowedCodeInfo {
    
    private final String sourceProject;
    private final String sourceRepository;
    private final String sourceClass;
    private final String sourceFile;
    private final String originalAuthor;
    private final String originalLicenseText;
    private final AttributionSystem.LicenseType license;
    private final AttributionSystem.AdaptationLevel adaptationLevel;
    private final String description;
    private final List<String> borrowedElements;
    private final List<String> modifications;
    private final String justification;
    private final Instant borrowedAt;
    private final String borrowedBy;
    private final String version;
    private final String commitHash;
    private final List<String> dependencies;
    private final String notes;
    
    /**
     * Private constructor for builder pattern.
     * 
     * @param builder the builder containing all attribution information
     */
    private BorrowedCodeInfo(Builder builder) {
        this.sourceProject = builder.sourceProject;
        this.sourceRepository = builder.sourceRepository;
        this.sourceClass = builder.sourceClass;
        this.sourceFile = builder.sourceFile;
        this.originalAuthor = builder.originalAuthor;
        this.originalLicenseText = builder.originalLicenseText;
        this.license = builder.license;
        this.adaptationLevel = builder.adaptationLevel;
        this.description = builder.description;
        this.borrowedElements = List.copyOf(builder.borrowedElements);
        this.modifications = List.copyOf(builder.modifications);
        this.justification = builder.justification;
        this.borrowedAt = builder.borrowedAt;
        this.borrowedBy = builder.borrowedBy;
        this.version = builder.version;
        this.commitHash = builder.commitHash;
        this.dependencies = List.copyOf(builder.dependencies);
        this.notes = builder.notes;
    }
    
    /**
     * Gets the name of the source project.
     * 
     * @return source project name (never null)
     */
    public String getSourceProject() {
        return sourceProject;
    }
    
    /**
     * Gets the repository URL of the source project.
     * 
     * @return source repository URL (may be null)
     */
    public String getSourceRepository() {
        return sourceRepository;
    }
    
    /**
     * Gets the specific class or file borrowed from the source.
     * 
     * @return source class/file reference (may be null)
     */
    public String getSourceClass() {
        return sourceClass;
    }
    
    /**
     * Gets the source file path if different from class.
     * 
     * @return source file path (may be null)
     */
    public String getSourceFile() {
        return sourceFile;
    }
    
    /**
     * Gets the original author(s) of the borrowed code.
     * 
     * @return original author information (may be null)
     */
    public String getOriginalAuthor() {
        return originalAuthor;
    }
    
    /**
     * Gets the full license text from the original project.
     * 
     * @return original license text (may be null)
     */
    public String getOriginalLicenseText() {
        return originalLicenseText;
    }
    
    /**
     * Gets the license type of the borrowed code.
     * 
     * @return license type (never null)
     */
    public AttributionSystem.LicenseType getLicense() {
        return license;
    }
    
    /**
     * Gets the level of adaptation applied to the borrowed code.
     * 
     * @return adaptation level (never null)
     */
    public AttributionSystem.AdaptationLevel getAdaptationLevel() {
        return adaptationLevel;
    }
    
    /**
     * Gets a description of how the code was borrowed and adapted.
     * 
     * @return description of borrowing and adaptation (never null)
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the specific elements borrowed from the original code.
     * 
     * @return list of borrowed elements (never null, may be empty)
     */
    public List<String> getBorrowedElements() {
        return borrowedElements;
    }
    
    /**
     * Gets the modifications made to the borrowed code.
     * 
     * @return list of modifications (never null, may be empty)
     */
    public List<String> getModifications() {
        return modifications;
    }
    
    /**
     * Gets the justification for borrowing this code.
     * 
     * @return justification text (may be null)
     */
    public String getJustification() {
        return justification;
    }
    
    /**
     * Gets when the code was borrowed.
     * 
     * @return borrowing timestamp (never null)
     */
    public Instant getBorrowedAt() {
        return borrowedAt;
    }
    
    /**
     * Gets who borrowed the code.
     * 
     * @return borrower identifier (may be null)
     */
    public String getBorrowedBy() {
        return borrowedBy;
    }
    
    /**
     * Gets the version of the source project when borrowed.
     * 
     * @return source version (may be null)
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Gets the commit hash from the source project.
     * 
     * @return source commit hash (may be null)
     */
    public String getCommitHash() {
        return commitHash;
    }
    
    /**
     * Gets any additional dependencies introduced by the borrowed code.
     * 
     * @return list of dependencies (never null, may be empty)
     */
    public List<String> getDependencies() {
        return dependencies;
    }
    
    /**
     * Gets additional notes about the borrowed code.
     * 
     * @return notes text (may be null)
     */
    public String getNotes() {
        return notes;
    }
    
    /**
     * Creates a new builder for constructing BorrowedCodeInfo instances.
     * 
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for creating BorrowedCodeInfo instances.
     * 
     * <p>Provides a fluent API for constructing attribution information with
     * validation and default value handling.</p>
     */
    public static final class Builder {
        private String sourceProject;
        private String sourceRepository;
        private String sourceClass;
        private String sourceFile;
        private String originalAuthor;
        private String originalLicenseText;
        private AttributionSystem.LicenseType license;
        private AttributionSystem.AdaptationLevel adaptationLevel;
        private String description;
        private List<String> borrowedElements = List.of();
        private List<String> modifications = List.of();
        private String justification;
        private Instant borrowedAt = Instant.now();
        private String borrowedBy;
        private String version;
        private String commitHash;
        private List<String> dependencies = List.of();
        private String notes;
        
        private Builder() {}
        
        /**
         * Sets the source project name (required).
         * 
         * @param sourceProject the name of the source project
         * @return this builder for method chaining
         */
        public Builder sourceProject(String sourceProject) {
            this.sourceProject = sourceProject;
            return this;
        }
        
        /**
         * Sets the source repository URL.
         * 
         * @param sourceRepository the repository URL
         * @return this builder for method chaining
         */
        public Builder sourceRepository(String sourceRepository) {
            this.sourceRepository = sourceRepository;
            return this;
        }
        
        /**
         * Sets the source class or file reference.
         * 
         * @param sourceClass the class or file name
         * @return this builder for method chaining
         */
        public Builder sourceClass(String sourceClass) {
            this.sourceClass = sourceClass;
            return this;
        }
        
        /**
         * Sets the source file path.
         * 
         * @param sourceFile the file path
         * @return this builder for method chaining
         */
        public Builder sourceFile(String sourceFile) {
            this.sourceFile = sourceFile;
            return this;
        }
        
        /**
         * Sets the original author information.
         * 
         * @param originalAuthor the author information
         * @return this builder for method chaining
         */
        public Builder originalAuthor(String originalAuthor) {
            this.originalAuthor = originalAuthor;
            return this;
        }
        
        /**
         * Sets the original license text.
         * 
         * @param originalLicenseText the license text
         * @return this builder for method chaining
         */
        public Builder originalLicenseText(String originalLicenseText) {
            this.originalLicenseText = originalLicenseText;
            return this;
        }
        
        /**
         * Sets the license type (required).
         * 
         * @param license the license type
         * @return this builder for method chaining
         */
        public Builder license(AttributionSystem.LicenseType license) {
            this.license = license;
            return this;
        }
        
        /**
         * Sets the adaptation level (required).
         * 
         * @param adaptationLevel the level of adaptation
         * @return this builder for method chaining
         */
        public Builder adaptationLevel(AttributionSystem.AdaptationLevel adaptationLevel) {
            this.adaptationLevel = adaptationLevel;
            return this;
        }
        
        /**
         * Sets the description (required).
         * 
         * @param description the borrowing description
         * @return this builder for method chaining
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        /**
         * Sets the borrowed elements list.
         * 
         * @param borrowedElements the elements borrowed
         * @return this builder for method chaining
         */
        public Builder borrowedElements(List<String> borrowedElements) {
            this.borrowedElements = borrowedElements != null ? borrowedElements : List.of();
            return this;
        }
        
        /**
         * Sets the modifications list.
         * 
         * @param modifications the modifications made
         * @return this builder for method chaining
         */
        public Builder modifications(List<String> modifications) {
            this.modifications = modifications != null ? modifications : List.of();
            return this;
        }
        
        /**
         * Sets the justification for borrowing.
         * 
         * @param justification the justification text
         * @return this builder for method chaining
         */
        public Builder justification(String justification) {
            this.justification = justification;
            return this;
        }
        
        /**
         * Sets when the code was borrowed.
         * 
         * @param borrowedAt the borrowing timestamp
         * @return this builder for method chaining
         */
        public Builder borrowedAt(Instant borrowedAt) {
            this.borrowedAt = borrowedAt != null ? borrowedAt : Instant.now();
            return this;
        }
        
        /**
         * Sets who borrowed the code.
         * 
         * @param borrowedBy the borrower identifier
         * @return this builder for method chaining
         */
        public Builder borrowedBy(String borrowedBy) {
            this.borrowedBy = borrowedBy;
            return this;
        }
        
        /**
         * Sets the source version.
         * 
         * @param version the source version
         * @return this builder for method chaining
         */
        public Builder version(String version) {
            this.version = version;
            return this;
        }
        
        /**
         * Sets the commit hash.
         * 
         * @param commitHash the commit hash
         * @return this builder for method chaining
         */
        public Builder commitHash(String commitHash) {
            this.commitHash = commitHash;
            return this;
        }
        
        /**
         * Sets the dependencies list.
         * 
         * @param dependencies the additional dependencies
         * @return this builder for method chaining
         */
        public Builder dependencies(List<String> dependencies) {
            this.dependencies = dependencies != null ? dependencies : List.of();
            return this;
        }
        
        /**
         * Sets additional notes.
         * 
         * @param notes the notes text
         * @return this builder for method chaining
         */
        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }
        
        /**
         * Builds the BorrowedCodeInfo instance with validation.
         * 
         * @return new BorrowedCodeInfo instance
         * @throws IllegalStateException if required fields are missing
         */
        public BorrowedCodeInfo build() {
            if (sourceProject == null || sourceProject.trim().isEmpty()) {
                throw new IllegalStateException("Source project is required");
            }
            if (license == null) {
                throw new IllegalStateException("License type is required");
            }
            if (adaptationLevel == null) {
                throw new IllegalStateException("Adaptation level is required");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalStateException("Description is required");
            }
            
            return new BorrowedCodeInfo(this);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BorrowedCodeInfo that = (BorrowedCodeInfo) obj;
        return Objects.equals(sourceProject, that.sourceProject) &&
               Objects.equals(sourceClass, that.sourceClass) &&
               Objects.equals(license, that.license) &&
               Objects.equals(description, that.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sourceProject, sourceClass, license, description);
    }
    
    @Override
    public String toString() {
        return String.format(
            "BorrowedCodeInfo{project='%s', class='%s', license=%s, adaptation=%s}",
            sourceProject, sourceClass, license, adaptationLevel
        );
    }
}
