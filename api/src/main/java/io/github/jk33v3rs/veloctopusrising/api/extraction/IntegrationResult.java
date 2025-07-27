package io.github.jk33v3rs.veloctopusrising.api.extraction;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Results of pattern integration into the Veloctopus Rising codebase.
 * 
 * <p>This immutable class contains comprehensive information about the integration
 * process, including generated files, configuration updates, and post-integration
 * verification results.</p>
 * 
 * <p><strong>Thread Safety:</strong> Immutable class, fully thread-safe.</p>
 * 
 * <p><strong>Integration Tracking:</strong> Provides detailed tracking of all
 * changes made during pattern integration for audit and rollback purposes.</p>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see ExtractionFramework
 * @see CodePattern
 */
public final class IntegrationResult {
    
    /**
     * Status of the integration process.
     */
    public enum IntegrationStatus {
        /** Integration completed successfully */
        SUCCESS,
        /** Integration completed with warnings */
        SUCCESS_WITH_WARNINGS,
        /** Integration failed but changes were rolled back */
        FAILED_ROLLED_BACK,
        /** Integration failed and manual cleanup may be required */
        FAILED_PARTIAL
    }
    
    private final IntegrationStatus status;
    private final String targetModule;
    private final List<String> generatedFiles;
    private final Map<String, String> configurationUpdates;
    private final Instant integrationTime;
    private final String integrationSummary;
    private final List<String> warnings;
    private final Exception integrationError;
    
    /**
     * Creates a new IntegrationResult instance.
     * 
     * @param builder the builder containing integration information
     */
    private IntegrationResult(Builder builder) {
        this.status = builder.status;
        this.targetModule = builder.targetModule;
        this.generatedFiles = List.copyOf(builder.generatedFiles);
        this.configurationUpdates = Map.copyOf(builder.configurationUpdates);
        this.integrationTime = builder.integrationTime;
        this.integrationSummary = builder.integrationSummary;
        this.warnings = List.copyOf(builder.warnings);
        this.integrationError = builder.integrationError;
    }
    
    /**
     * Gets the integration status.
     * 
     * @return integration status (never null)
     */
    public IntegrationStatus getStatus() {
        return status;
    }
    
    /**
     * Gets the target module where the pattern was integrated.
     * 
     * @return target module name (never null)
     */
    public String getTargetModule() {
        return targetModule;
    }
    
    /**
     * Gets the list of files generated during integration.
     * 
     * @return immutable list of file paths
     */
    public List<String> getGeneratedFiles() {
        return generatedFiles;
    }
    
    /**
     * Gets configuration updates made during integration.
     * 
     * @return immutable map of configuration changes
     */
    public Map<String, String> getConfigurationUpdates() {
        return configurationUpdates;
    }
    
    /**
     * Gets the timestamp when integration was completed.
     * 
     * @return integration timestamp (never null)
     */
    public Instant getIntegrationTime() {
        return integrationTime;
    }
    
    /**
     * Gets a summary of the integration process.
     * 
     * @return integration summary (never null)
     */
    public String getIntegrationSummary() {
        return integrationSummary;
    }
    
    /**
     * Gets warnings generated during integration.
     * 
     * @return immutable list of warning messages
     */
    public List<String> getWarnings() {
        return warnings;
    }
    
    /**
     * Gets the error that caused integration failure (if any).
     * 
     * @return integration error or null if successful
     */
    public Exception getIntegrationError() {
        return integrationError;
    }
    
    /**
     * Checks if integration was successful.
     * 
     * @return true if status is SUCCESS or SUCCESS_WITH_WARNINGS
     */
    public boolean isSuccessful() {
        return status == IntegrationStatus.SUCCESS || 
               status == IntegrationStatus.SUCCESS_WITH_WARNINGS;
    }
    
    /**
     * Creates a new builder for constructing IntegrationResult instances.
     * 
     * @return new integration result builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for creating IntegrationResult instances.
     */
    public static final class Builder {
        private IntegrationStatus status = IntegrationStatus.SUCCESS;
        private String targetModule;
        private List<String> generatedFiles = List.of();
        private Map<String, String> configurationUpdates = Map.of();
        private Instant integrationTime = Instant.now();
        private String integrationSummary = "";
        private List<String> warnings = List.of();
        private Exception integrationError;
        
        private Builder() {}
        
        public Builder status(IntegrationStatus status) {
            this.status = status != null ? status : IntegrationStatus.FAILED_PARTIAL;
            return this;
        }
        
        public Builder targetModule(String targetModule) {
            this.targetModule = targetModule;
            return this;
        }
        
        public Builder generatedFiles(List<String> generatedFiles) {
            this.generatedFiles = generatedFiles != null ? generatedFiles : List.of();
            return this;
        }
        
        public Builder configurationUpdates(Map<String, String> configurationUpdates) {
            this.configurationUpdates = configurationUpdates != null ? configurationUpdates : Map.of();
            return this;
        }
        
        public Builder integrationTime(Instant integrationTime) {
            this.integrationTime = integrationTime != null ? integrationTime : Instant.now();
            return this;
        }
        
        public Builder integrationSummary(String integrationSummary) {
            this.integrationSummary = integrationSummary != null ? integrationSummary : "";
            return this;
        }
        
        public Builder warnings(List<String> warnings) {
            this.warnings = warnings != null ? warnings : List.of();
            return this;
        }
        
        public Builder integrationError(Exception integrationError) {
            this.integrationError = integrationError;
            return this;
        }
        
        public IntegrationResult build() {
            if (targetModule == null || targetModule.trim().isEmpty()) {
                throw new IllegalStateException("Target module is required");
            }
            
            return new IntegrationResult(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "IntegrationResult{status=%s, module='%s', files=%d, warnings=%d}",
            status, targetModule, generatedFiles.size(), warnings.size());
    }
}
