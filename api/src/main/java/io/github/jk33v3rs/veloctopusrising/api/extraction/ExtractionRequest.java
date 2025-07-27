package io.github.jk33v3rs.veloctopusrising.api.extraction;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Request object for code extraction operations.
 * 
 * <p>This immutable class contains all necessary information for extracting
 * code from a reference project, including source identification, target
 * configuration, and adaptation requirements.</p>
 * 
 * <p><strong>Thread Safety:</strong> Immutable class, fully thread-safe for
 * concurrent access across multiple extraction operations.</p>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ExtractionRequest {
    
    private final String sourceProject;
    private final String sourceRepository;
    private final String targetPackage;
    private final ExtractionFramework.ExtractionType extractionType;
    private final ExtractionFramework.AdaptationLevel adaptationLevel;
    private final List<String> sourceClasses;
    private final List<String> excludePatterns;
    private final Map<String, String> packageMappings;
    private final Map<String, Object> extractionOptions;
    private final String description;
    private final String requestedBy;
    private final Instant requestedAt;
    private final int priority;
    private final boolean includeTests;
    private final boolean includeDependencies;
    private final String licenseOverride;
    
    private ExtractionRequest(Builder builder) {
        this.sourceProject = builder.sourceProject;
        this.sourceRepository = builder.sourceRepository;
        this.targetPackage = builder.targetPackage;
        this.extractionType = builder.extractionType;
        this.adaptationLevel = builder.adaptationLevel;
        this.sourceClasses = List.copyOf(builder.sourceClasses);
        this.excludePatterns = List.copyOf(builder.excludePatterns);
        this.packageMappings = Map.copyOf(builder.packageMappings);
        this.extractionOptions = Map.copyOf(builder.extractionOptions);
        this.description = builder.description;
        this.requestedBy = builder.requestedBy;
        this.requestedAt = builder.requestedAt;
        this.priority = builder.priority;
        this.includeTests = builder.includeTests;
        this.includeDependencies = builder.includeDependencies;
        this.licenseOverride = builder.licenseOverride;
    }
    
    // Getters
    public String getSourceProject() { return sourceProject; }
    public String getSourceRepository() { return sourceRepository; }
    public String getTargetPackage() { return targetPackage; }
    public ExtractionFramework.ExtractionType getExtractionType() { return extractionType; }
    public ExtractionFramework.AdaptationLevel getAdaptationLevel() { return adaptationLevel; }
    public List<String> getSourceClasses() { return sourceClasses; }
    public List<String> getExcludePatterns() { return excludePatterns; }
    public Map<String, String> getPackageMappings() { return packageMappings; }
    public Map<String, Object> getExtractionOptions() { return extractionOptions; }
    public String getDescription() { return description; }
    public String getRequestedBy() { return requestedBy; }
    public Instant getRequestedAt() { return requestedAt; }
    public int getPriority() { return priority; }
    public boolean isIncludeTests() { return includeTests; }
    public boolean isIncludeDependencies() { return includeDependencies; }
    public String getLicenseOverride() { return licenseOverride; }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        private String sourceProject;
        private String sourceRepository;
        private String targetPackage;
        private ExtractionFramework.ExtractionType extractionType;
        private ExtractionFramework.AdaptationLevel adaptationLevel;
        private List<String> sourceClasses = List.of();
        private List<String> excludePatterns = List.of();
        private Map<String, String> packageMappings = Map.of();
        private Map<String, Object> extractionOptions = Map.of();
        private String description;
        private String requestedBy;
        private Instant requestedAt = Instant.now();
        private int priority = 5;
        private boolean includeTests = false;
        private boolean includeDependencies = true;
        private String licenseOverride;
        
        public Builder sourceProject(String sourceProject) {
            this.sourceProject = sourceProject;
            return this;
        }
        
        public Builder sourceRepository(String sourceRepository) {
            this.sourceRepository = sourceRepository;
            return this;
        }
        
        public Builder targetPackage(String targetPackage) {
            this.targetPackage = targetPackage;
            return this;
        }
        
        public Builder extractionType(ExtractionFramework.ExtractionType extractionType) {
            this.extractionType = extractionType;
            return this;
        }
        
        public Builder adaptationLevel(ExtractionFramework.AdaptationLevel adaptationLevel) {
            this.adaptationLevel = adaptationLevel;
            return this;
        }
        
        public Builder sourceClasses(List<String> sourceClasses) {
            this.sourceClasses = sourceClasses != null ? sourceClasses : List.of();
            return this;
        }
        
        public Builder excludePatterns(List<String> excludePatterns) {
            this.excludePatterns = excludePatterns != null ? excludePatterns : List.of();
            return this;
        }
        
        public Builder packageMappings(Map<String, String> packageMappings) {
            this.packageMappings = packageMappings != null ? packageMappings : Map.of();
            return this;
        }
        
        public Builder extractionOptions(Map<String, Object> extractionOptions) {
            this.extractionOptions = extractionOptions != null ? extractionOptions : Map.of();
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder requestedBy(String requestedBy) {
            this.requestedBy = requestedBy;
            return this;
        }
        
        public Builder requestedAt(Instant requestedAt) {
            this.requestedAt = requestedAt != null ? requestedAt : Instant.now();
            return this;
        }
        
        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder includeTests(boolean includeTests) {
            this.includeTests = includeTests;
            return this;
        }
        
        public Builder includeDependencies(boolean includeDependencies) {
            this.includeDependencies = includeDependencies;
            return this;
        }
        
        public Builder licenseOverride(String licenseOverride) {
            this.licenseOverride = licenseOverride;
            return this;
        }
        
        public ExtractionRequest build() {
            if (sourceProject == null || sourceProject.trim().isEmpty()) {
                throw new IllegalStateException("Source project is required");
            }
            if (targetPackage == null || targetPackage.trim().isEmpty()) {
                throw new IllegalStateException("Target package is required");
            }
            if (extractionType == null) {
                throw new IllegalStateException("Extraction type is required");
            }
            if (adaptationLevel == null) {
                throw new IllegalStateException("Adaptation level is required");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalStateException("Description is required");
            }
            
            return new ExtractionRequest(this);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExtractionRequest that = (ExtractionRequest) obj;
        return Objects.equals(sourceProject, that.sourceProject) &&
               Objects.equals(targetPackage, that.targetPackage) &&
               Objects.equals(extractionType, that.extractionType) &&
               Objects.equals(description, that.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sourceProject, targetPackage, extractionType, description);
    }
    
    @Override
    public String toString() {
        return String.format(
            "ExtractionRequest{project='%s', target='%s', type=%s, adaptation=%s}",
            sourceProject, targetPackage, extractionType, adaptationLevel
        );
    }
}
