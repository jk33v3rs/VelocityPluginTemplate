package io.github.jk33v3rs.veloctopusrising.api.extraction;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Result of code adaptation operations.
 */
public final class AdaptationResult {
    private final String adaptationId;
    private final ExtractionResult sourceResult;
    private final List<String> adaptedFiles;
    private final Map<String, String> adaptationChanges;
    private final Instant adaptedAt;
    
    public AdaptationResult(String adaptationId, ExtractionResult sourceResult,
                          List<String> adaptedFiles, Map<String, String> adaptationChanges,
                          Instant adaptedAt) {
        this.adaptationId = adaptationId;
        this.sourceResult = sourceResult;
        this.adaptedFiles = List.copyOf(adaptedFiles);
        this.adaptationChanges = Map.copyOf(adaptationChanges);
        this.adaptedAt = adaptedAt;
    }
    
    public String getAdaptationId() { return adaptationId; }
    public ExtractionResult getSourceResult() { return sourceResult; }
    public List<String> getAdaptedFiles() { return adaptedFiles; }
    public Map<String, String> getAdaptationChanges() { return adaptationChanges; }
    public Instant getAdaptedAt() { return adaptedAt; }
}

/**
 * Result of code integration operations.
 */
final class IntegrationResult {
    private final String integrationId;
    private final AdaptationResult sourceResult;
    private final List<String> integratedFiles;
    private final boolean compilationSuccessful;
    private final Instant integratedAt;
    
    public IntegrationResult(String integrationId, AdaptationResult sourceResult,
                           List<String> integratedFiles, boolean compilationSuccessful,
                           Instant integratedAt) {
        this.integrationId = integrationId;
        this.sourceResult = sourceResult;
        this.integratedFiles = List.copyOf(integratedFiles);
        this.compilationSuccessful = compilationSuccessful;
        this.integratedAt = integratedAt;
    }
    
    public String getIntegrationId() { return integrationId; }
    public AdaptationResult getSourceResult() { return sourceResult; }
    public List<String> getIntegratedFiles() { return integratedFiles; }
    public boolean isCompilationSuccessful() { return compilationSuccessful; }
    public Instant getIntegratedAt() { return integratedAt; }
}

/**
 * Result of validation operations.
 */
final class ValidationResult {
    private final String validationId;
    private final IntegrationResult sourceResult;
    private final boolean isValid;
    private final List<String> validationErrors;
    private final Instant validatedAt;
    
    public ValidationResult(String validationId, IntegrationResult sourceResult,
                          boolean isValid, List<String> validationErrors, Instant validatedAt) {
        this.validationId = validationId;
        this.sourceResult = sourceResult;
        this.isValid = isValid;
        this.validationErrors = List.copyOf(validationErrors);
        this.validatedAt = validatedAt;
    }
    
    public String getValidationId() { return validationId; }
    public IntegrationResult getSourceResult() { return sourceResult; }
    public boolean isValid() { return isValid; }
    public List<String> getValidationErrors() { return validationErrors; }
    public Instant getValidatedAt() { return validatedAt; }
}

/**
 * Attribution information for extracted code.
 */
final class AttributionInfo {
    private final String attributionId;
    private final ExtractionResult sourceResult;
    private final String licenseText;
    private final List<String> requiredNotices;
    private final Instant generatedAt;
    
    public AttributionInfo(String attributionId, ExtractionResult sourceResult,
                         String licenseText, List<String> requiredNotices, Instant generatedAt) {
        this.attributionId = attributionId;
        this.sourceResult = sourceResult;
        this.licenseText = licenseText;
        this.requiredNotices = List.copyOf(requiredNotices);
        this.generatedAt = generatedAt;
    }
    
    public String getAttributionId() { return attributionId; }
    public ExtractionResult getSourceResult() { return sourceResult; }
    public String getLicenseText() { return licenseText; }
    public List<String> getRequiredNotices() { return requiredNotices; }
    public Instant getGeneratedAt() { return generatedAt; }
}

/**
 * Analysis of a reference project.
 */
final class ProjectAnalysis {
    private final String projectName;
    private final String projectPath;
    private final List<String> availableClasses;
    private final Map<String, Object> projectMetadata;
    private final Instant analyzedAt;
    
    public ProjectAnalysis(String projectName, String projectPath,
                         List<String> availableClasses, Map<String, Object> projectMetadata,
                         Instant analyzedAt) {
        this.projectName = projectName;
        this.projectPath = projectPath;
        this.availableClasses = List.copyOf(availableClasses);
        this.projectMetadata = Map.copyOf(projectMetadata);
        this.analyzedAt = analyzedAt;
    }
    
    public String getProjectName() { return projectName; }
    public String getProjectPath() { return projectPath; }
    public List<String> getAvailableClasses() { return availableClasses; }
    public Map<String, Object> getProjectMetadata() { return projectMetadata; }
    public Instant getAnalyzedAt() { return analyzedAt; }
}

/**
 * Analysis options for project analysis.
 */
final class AnalysisOptions {
    private final boolean includeTests;
    private final boolean analyzeDependencies;
    private final List<String> excludePatterns;
    
    public AnalysisOptions(boolean includeTests, boolean analyzeDependencies, List<String> excludePatterns) {
        this.includeTests = includeTests;
        this.analyzeDependencies = analyzeDependencies;
        this.excludePatterns = List.copyOf(excludePatterns);
    }
    
    public boolean isIncludeTests() { return includeTests; }
    public boolean isAnalyzeDependencies() { return analyzeDependencies; }
    public List<String> getExcludePatterns() { return excludePatterns; }
}

/**
 * Information about a reference project.
 */
final class ReferenceProject {
    private final String name;
    private final String path;
    private final String repository;
    private final String version;
    private final String license;
    
    public ReferenceProject(String name, String path, String repository, String version, String license) {
        this.name = name;
        this.path = path;
        this.repository = repository;
        this.version = version;
        this.license = license;
    }
    
    public String getName() { return name; }
    public String getPath() { return path; }
    public String getRepository() { return repository; }
    public String getVersion() { return version; }
    public String getLicense() { return license; }
}

/**
 * History of extraction operations.
 */
final class ExtractionHistory {
    private final String extractionId;
    private final String sourceProject;
    private final String targetPackage;
    private final Instant extractedAt;
    private final String status;
    
    public ExtractionHistory(String extractionId, String sourceProject, String targetPackage,
                           Instant extractedAt, String status) {
        this.extractionId = extractionId;
        this.sourceProject = sourceProject;
        this.targetPackage = targetPackage;
        this.extractedAt = extractedAt;
        this.status = status;
    }
    
    public String getExtractionId() { return extractionId; }
    public String getSourceProject() { return sourceProject; }
    public String getTargetPackage() { return targetPackage; }
    public Instant getExtractedAt() { return extractedAt; }
    public String getStatus() { return status; }
}

/**
 * Recommendation for code extraction.
 */
final class ExtractionRecommendation {
    private final String sourceProject;
    private final String targetPackage;
    private final ExtractionFramework.ExtractionType recommendedType;
    private final int priority;
    private final String reasoning;
    
    public ExtractionRecommendation(String sourceProject, String targetPackage,
                                  ExtractionFramework.ExtractionType recommendedType,
                                  int priority, String reasoning) {
        this.sourceProject = sourceProject;
        this.targetPackage = targetPackage;
        this.recommendedType = recommendedType;
        this.priority = priority;
        this.reasoning = reasoning;
    }
    
    public String getSourceProject() { return sourceProject; }
    public String getTargetPackage() { return targetPackage; }
    public ExtractionFramework.ExtractionType getRecommendedType() { return recommendedType; }
    public int getPriority() { return priority; }
    public String getReasoning() { return reasoning; }
}

/**
 * Analysis of the target system.
 */
final class SystemAnalysis {
    private final String systemName;
    private final List<String> missingComponents;
    private final Map<String, Object> systemMetrics;
    private final Instant analyzedAt;
    
    public SystemAnalysis(String systemName, List<String> missingComponents,
                        Map<String, Object> systemMetrics, Instant analyzedAt) {
        this.systemName = systemName;
        this.missingComponents = List.copyOf(missingComponents);
        this.systemMetrics = Map.copyOf(systemMetrics);
        this.analyzedAt = analyzedAt;
    }
    
    public String getSystemName() { return systemName; }
    public List<String> getMissingComponents() { return missingComponents; }
    public Map<String, Object> getSystemMetrics() { return systemMetrics; }
    public Instant getAnalyzedAt() { return analyzedAt; }
}

/**
 * Configuration for the extraction framework.
 */
final class ExtractionConfiguration {
    private final String workingDirectory;
    private final Map<String, String> projectPaths;
    private final Map<String, Object> defaultOptions;
    
    public ExtractionConfiguration(String workingDirectory, Map<String, String> projectPaths,
                                 Map<String, Object> defaultOptions) {
        this.workingDirectory = workingDirectory;
        this.projectPaths = Map.copyOf(projectPaths);
        this.defaultOptions = Map.copyOf(defaultOptions);
    }
    
    public String getWorkingDirectory() { return workingDirectory; }
    public Map<String, String> getProjectPaths() { return projectPaths; }
    public Map<String, Object> getDefaultOptions() { return defaultOptions; }
}

/**
 * Status of the extraction framework.
 */
final class FrameworkStatus {
    private final boolean isRunning;
    private final int activeExtractions;
    private final Map<String, Object> statistics;
    private final Instant lastUpdated;
    
    public FrameworkStatus(boolean isRunning, int activeExtractions,
                         Map<String, Object> statistics, Instant lastUpdated) {
        this.isRunning = isRunning;
        this.activeExtractions = activeExtractions;
        this.statistics = Map.copyOf(statistics);
        this.lastUpdated = lastUpdated;
    }
    
    public boolean isRunning() { return isRunning; }
    public int getActiveExtractions() { return activeExtractions; }
    public Map<String, Object> getStatistics() { return statistics; }
    public Instant getLastUpdated() { return lastUpdated; }
}
