package io.github.jk33v3rs.veloctopusrising.api.extraction;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Result object containing information about a completed code extraction operation.
 * 
 * <p>This immutable class contains comprehensive information about the outcome
 * of a code extraction process, including extracted files, metadata, statistics,
 * and any issues encountered during extraction.</p>
 * 
 * <p><strong>Thread Safety:</strong> Immutable class, fully thread-safe for
 * concurrent access and processing across multiple threads.</p>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ExtractionResult {
    
    /**
     * Status of the extraction operation.
     */
    public enum Status {
        SUCCESS,
        PARTIAL_SUCCESS,
        FAILED,
        CANCELLED
    }
    
    private final String extractionId;
    private final ExtractionRequest originalRequest;
    private final Status status;
    private final String sourceProject;
    private final List<ExtractedFile> extractedFiles;
    private final List<String> extractedClasses;
    private final List<String> dependencies;
    private final Map<String, Object> metadata;
    private final ExtractionStatistics statistics;
    private final List<String> warnings;
    private final List<String> errors;
    private final Instant startedAt;
    private final Instant completedAt;
    private final Duration processingTime;
    private final String extractionPath;
    private final Map<String, String> licenseInfo;
    
    private ExtractionResult(Builder builder) {
        this.extractionId = builder.extractionId;
        this.originalRequest = builder.originalRequest;
        this.status = builder.status;
        this.sourceProject = builder.sourceProject;
        this.extractedFiles = List.copyOf(builder.extractedFiles);
        this.extractedClasses = List.copyOf(builder.extractedClasses);
        this.dependencies = List.copyOf(builder.dependencies);
        this.metadata = Map.copyOf(builder.metadata);
        this.statistics = builder.statistics;
        this.warnings = List.copyOf(builder.warnings);
        this.errors = List.copyOf(builder.errors);
        this.startedAt = builder.startedAt;
        this.completedAt = builder.completedAt;
        this.processingTime = builder.processingTime;
        this.extractionPath = builder.extractionPath;
        this.licenseInfo = Map.copyOf(builder.licenseInfo);
    }
    
    // Getters
    public String getExtractionId() { return extractionId; }
    public ExtractionRequest getOriginalRequest() { return originalRequest; }
    public Status getStatus() { return status; }
    public String getSourceProject() { return sourceProject; }
    public List<ExtractedFile> getExtractedFiles() { return extractedFiles; }
    public List<String> getExtractedClasses() { return extractedClasses; }
    public List<String> getDependencies() { return dependencies; }
    public Map<String, Object> getMetadata() { return metadata; }
    public ExtractionStatistics getStatistics() { return statistics; }
    public List<String> getWarnings() { return warnings; }
    public List<String> getErrors() { return errors; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Duration getProcessingTime() { return processingTime; }
    public String getExtractionPath() { return extractionPath; }
    public Map<String, String> getLicenseInfo() { return licenseInfo; }
    
    public boolean isSuccessful() {
        return status == Status.SUCCESS || status == Status.PARTIAL_SUCCESS;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        private String extractionId;
        private ExtractionRequest originalRequest;
        private Status status;
        private String sourceProject;
        private List<ExtractedFile> extractedFiles = List.of();
        private List<String> extractedClasses = List.of();
        private List<String> dependencies = List.of();
        private Map<String, Object> metadata = Map.of();
        private ExtractionStatistics statistics;
        private List<String> warnings = List.of();
        private List<String> errors = List.of();
        private Instant startedAt;
        private Instant completedAt;
        private Duration processingTime;
        private String extractionPath;
        private Map<String, String> licenseInfo = Map.of();
        
        public Builder extractionId(String extractionId) {
            this.extractionId = extractionId;
            return this;
        }
        
        public Builder originalRequest(ExtractionRequest originalRequest) {
            this.originalRequest = originalRequest;
            return this;
        }
        
        public Builder status(Status status) {
            this.status = status;
            return this;
        }
        
        public Builder sourceProject(String sourceProject) {
            this.sourceProject = sourceProject;
            return this;
        }
        
        public Builder extractedFiles(List<ExtractedFile> extractedFiles) {
            this.extractedFiles = extractedFiles != null ? extractedFiles : List.of();
            return this;
        }
        
        public Builder extractedClasses(List<String> extractedClasses) {
            this.extractedClasses = extractedClasses != null ? extractedClasses : List.of();
            return this;
        }
        
        public Builder dependencies(List<String> dependencies) {
            this.dependencies = dependencies != null ? dependencies : List.of();
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? metadata : Map.of();
            return this;
        }
        
        public Builder statistics(ExtractionStatistics statistics) {
            this.statistics = statistics;
            return this;
        }
        
        public Builder warnings(List<String> warnings) {
            this.warnings = warnings != null ? warnings : List.of();
            return this;
        }
        
        public Builder errors(List<String> errors) {
            this.errors = errors != null ? errors : List.of();
            return this;
        }
        
        public Builder startedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }
        
        public Builder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }
        
        public Builder processingTime(Duration processingTime) {
            this.processingTime = processingTime;
            return this;
        }
        
        public Builder extractionPath(String extractionPath) {
            this.extractionPath = extractionPath;
            return this;
        }
        
        public Builder licenseInfo(Map<String, String> licenseInfo) {
            this.licenseInfo = licenseInfo != null ? licenseInfo : Map.of();
            return this;
        }
        
        public ExtractionResult build() {
            if (extractionId == null || extractionId.trim().isEmpty()) {
                throw new IllegalStateException("Extraction ID is required");
            }
            if (originalRequest == null) {
                throw new IllegalStateException("Original request is required");
            }
            if (status == null) {
                throw new IllegalStateException("Status is required");
            }
            if (sourceProject == null || sourceProject.trim().isEmpty()) {
                throw new IllegalStateException("Source project is required");
            }
            
            return new ExtractionResult(this);
        }
    }
    
    /**
     * Information about an extracted file.
     */
    public static final class ExtractedFile {
        private final String sourceFilePath;
        private final String targetFilePath;
        private final String fileName;
        private final long fileSize;
        private final String checksum;
        private final Instant extractedAt;
        private final boolean isModified;
        
        public ExtractedFile(String sourceFilePath, String targetFilePath, String fileName,
                           long fileSize, String checksum, Instant extractedAt, boolean isModified) {
            this.sourceFilePath = sourceFilePath;
            this.targetFilePath = targetFilePath;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.checksum = checksum;
            this.extractedAt = extractedAt;
            this.isModified = isModified;
        }
        
        public String getSourceFilePath() { return sourceFilePath; }
        public String getTargetFilePath() { return targetFilePath; }
        public String getFileName() { return fileName; }
        public long getFileSize() { return fileSize; }
        public String getChecksum() { return checksum; }
        public Instant getExtractedAt() { return extractedAt; }
        public boolean isModified() { return isModified; }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ExtractedFile that = (ExtractedFile) obj;
            return Objects.equals(sourceFilePath, that.sourceFilePath) &&
                   Objects.equals(targetFilePath, that.targetFilePath);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(sourceFilePath, targetFilePath);
        }
    }
    
    /**
     * Statistics about the extraction process.
     */
    public static final class ExtractionStatistics {
        private final int totalFilesProcessed;
        private final int filesExtracted;
        private final int filesSkipped;
        private final int classesExtracted;
        private final int methodsExtracted;
        private final long totalBytesExtracted;
        private final int dependenciesFound;
        private final int warningsGenerated;
        private final int errorsEncountered;
        
        public ExtractionStatistics(int totalFilesProcessed, int filesExtracted, int filesSkipped,
                                  int classesExtracted, int methodsExtracted, long totalBytesExtracted,
                                  int dependenciesFound, int warningsGenerated, int errorsEncountered) {
            this.totalFilesProcessed = totalFilesProcessed;
            this.filesExtracted = filesExtracted;
            this.filesSkipped = filesSkipped;
            this.classesExtracted = classesExtracted;
            this.methodsExtracted = methodsExtracted;
            this.totalBytesExtracted = totalBytesExtracted;
            this.dependenciesFound = dependenciesFound;
            this.warningsGenerated = warningsGenerated;
            this.errorsEncountered = errorsEncountered;
        }
        
        public int getTotalFilesProcessed() { return totalFilesProcessed; }
        public int getFilesExtracted() { return filesExtracted; }
        public int getFilesSkipped() { return filesSkipped; }
        public int getClassesExtracted() { return classesExtracted; }
        public int getMethodsExtracted() { return methodsExtracted; }
        public long getTotalBytesExtracted() { return totalBytesExtracted; }
        public int getDependenciesFound() { return dependenciesFound; }
        public int getWarningsGenerated() { return warningsGenerated; }
        public int getErrorsEncountered() { return errorsEncountered; }
        
        public double getExtractionEfficiency() {
            return totalFilesProcessed > 0 ? (double) filesExtracted / totalFilesProcessed : 0.0;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExtractionResult that = (ExtractionResult) obj;
        return Objects.equals(extractionId, that.extractionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(extractionId);
    }
    
    @Override
    public String toString() {
        return String.format(
            "ExtractionResult{id='%s', project='%s', status=%s, files=%d, classes=%d}",
            extractionId, sourceProject, status, extractedFiles.size(), extractedClasses.size()
        );
    }
}
