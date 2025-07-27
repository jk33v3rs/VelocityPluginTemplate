package io.github.jk33v3rs.veloctopusrising.api.extraction;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Results of pattern validation for integration compliance.
 * 
 * <p>This immutable class contains comprehensive validation results for extracted
 * code patterns, including licensing compliance, API compatibility, security
 * assessment, and integration recommendations.</p>
 * 
 * <p><strong>Thread Safety:</strong> Immutable class, fully thread-safe.</p>
 * 
 * <p><strong>Validation Scope:</strong> Covers legal, technical, security, and
 * performance aspects of pattern integration to ensure safe adoption.</p>
 * 
 * <h3>Validation Categories:</h3>
 * <ul>
 *   <li><strong>Legal:</strong> License compatibility and attribution requirements</li>
 *   <li><strong>Technical:</strong> API compatibility and dependency conflicts</li>
 *   <li><strong>Security:</strong> Vulnerability scanning and risk assessment</li>
 *   <li><strong>Performance:</strong> Impact analysis and optimization recommendations</li>
 * </ul>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see ExtractionFramework
 * @see CodePattern
 */
public final class ValidationResult {
    
    /**
     * Overall validation status for the pattern.
     */
    public enum ValidationStatus {
        /** Pattern passes all validation checks and is ready for integration */
        APPROVED,
        /** Pattern has minor issues but can be integrated with warnings */
        APPROVED_WITH_WARNINGS,
        /** Pattern has significant issues that should be addressed before integration */
        REQUIRES_ATTENTION,
        /** Pattern fails critical validation checks and cannot be integrated */
        REJECTED
    }
    
    /**
     * Severity level of validation issues.
     */
    public enum IssueSeverity {
        /** Informational notice that doesn't prevent integration */
        INFO,
        /** Warning that should be reviewed but doesn't block integration */
        WARNING,
        /** Error that should be fixed before integration */
        ERROR,
        /** Critical issue that blocks integration completely */
        CRITICAL
    }
    
    private final ValidationStatus status;
    private final List<ValidationIssue> issues;
    private final Map<String, Object> validationMetrics;
    private final Duration validationTime;
    private final String summary;
    private final List<String> recommendations;
    private final boolean licenseCompliant;
    private final boolean apiCompatible;
    private final boolean securityClear;
    private final double performanceImpactScore;
    
    /**
     * Creates a new ValidationResult instance.
     * 
     * @param builder the builder containing validation information
     */
    private ValidationResult(Builder builder) {
        this.status = builder.status;
        this.issues = List.copyOf(builder.issues);
        this.validationMetrics = Map.copyOf(builder.validationMetrics);
        this.validationTime = builder.validationTime;
        this.summary = builder.summary;
        this.recommendations = List.copyOf(builder.recommendations);
        this.licenseCompliant = builder.licenseCompliant;
        this.apiCompatible = builder.apiCompatible;
        this.securityClear = builder.securityClear;
        this.performanceImpactScore = builder.performanceImpactScore;
    }
    
    /**
     * Gets the overall validation status.
     * 
     * @return validation status (never null)
     */
    public ValidationStatus getStatus() {
        return status;
    }
    
    /**
     * Gets all validation issues found during the check.
     * 
     * @return immutable list of validation issues
     */
    public List<ValidationIssue> getIssues() {
        return issues;
    }
    
    /**
     * Gets validation metrics and measurements.
     * 
     * @return immutable map of validation metrics
     */
    public Map<String, Object> getValidationMetrics() {
        return validationMetrics;
    }
    
    /**
     * Gets the time taken to complete validation.
     * 
     * @return validation duration (never null)
     */
    public Duration getValidationTime() {
        return validationTime;
    }
    
    /**
     * Gets a summary of the validation results.
     * 
     * @return validation summary (never null)
     */
    public String getSummary() {
        return summary;
    }
    
    /**
     * Gets recommendations for addressing validation issues.
     * 
     * @return immutable list of recommendations
     */
    public List<String> getRecommendations() {
        return recommendations;
    }
    
    /**
     * Checks if the pattern is compliant with licensing requirements.
     * 
     * @return true if license is compatible with project requirements
     */
    public boolean isLicenseCompliant() {
        return licenseCompliant;
    }
    
    /**
     * Checks if the pattern is compatible with existing APIs.
     * 
     * @return true if API compatibility is confirmed
     */
    public boolean isApiCompatible() {
        return apiCompatible;
    }
    
    /**
     * Checks if the pattern passes security validation.
     * 
     * @return true if no security issues were found
     */
    public boolean isSecurityClear() {
        return securityClear;
    }
    
    /**
     * Gets the estimated performance impact score.
     * 
     * @return impact score from 0.0 (no impact) to 10.0 (severe impact)
     */
    public double getPerformanceImpactScore() {
        return performanceImpactScore;
    }
    
    /**
     * Checks if the pattern is approved for integration.
     * 
     * @return true if status is APPROVED or APPROVED_WITH_WARNINGS
     */
    public boolean isApproved() {
        return status == ValidationStatus.APPROVED || 
               status == ValidationStatus.APPROVED_WITH_WARNINGS;
    }
    
    /**
     * Gets issues of a specific severity level.
     * 
     * @param severity the severity level to filter by
     * @return list of issues with the specified severity
     */
    public List<ValidationIssue> getIssuesBySeverity(IssueSeverity severity) {
        return issues.stream()
                    .filter(issue -> issue.getSeverity() == severity)
                    .toList();
    }
    
    /**
     * Gets the count of issues by severity level.
     * 
     * @return map of severity levels to issue counts
     */
    public Map<IssueSeverity, Long> getIssueCounts() {
        return issues.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                        ValidationIssue::getSeverity,
                        java.util.stream.Collectors.counting()));
    }
    
    /**
     * Creates a new builder for constructing ValidationResult instances.
     * 
     * @return new validation result builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for creating ValidationResult instances.
     */
    public static final class Builder {
        private ValidationStatus status = ValidationStatus.APPROVED;
        private List<ValidationIssue> issues = List.of();
        private Map<String, Object> validationMetrics = Map.of();
        private Duration validationTime = Duration.ZERO;
        private String summary = "";
        private List<String> recommendations = List.of();
        private boolean licenseCompliant = true;
        private boolean apiCompatible = true;
        private boolean securityClear = true;
        private double performanceImpactScore = 0.0;
        
        private Builder() {}
        
        public Builder status(ValidationStatus status) {
            this.status = status != null ? status : ValidationStatus.REJECTED;
            return this;
        }
        
        public Builder issues(List<ValidationIssue> issues) {
            this.issues = issues != null ? issues : List.of();
            return this;
        }
        
        public Builder validationMetrics(Map<String, Object> validationMetrics) {
            this.validationMetrics = validationMetrics != null ? validationMetrics : Map.of();
            return this;
        }
        
        public Builder validationTime(Duration validationTime) {
            this.validationTime = validationTime != null ? validationTime : Duration.ZERO;
            return this;
        }
        
        public Builder summary(String summary) {
            this.summary = summary != null ? summary : "";
            return this;
        }
        
        public Builder recommendations(List<String> recommendations) {
            this.recommendations = recommendations != null ? recommendations : List.of();
            return this;
        }
        
        public Builder licenseCompliant(boolean licenseCompliant) {
            this.licenseCompliant = licenseCompliant;
            return this;
        }
        
        public Builder apiCompatible(boolean apiCompatible) {
            this.apiCompatible = apiCompatible;
            return this;
        }
        
        public Builder securityClear(boolean securityClear) {
            this.securityClear = securityClear;
            return this;
        }
        
        public Builder performanceImpactScore(double performanceImpactScore) {
            this.performanceImpactScore = Math.max(0.0, Math.min(10.0, performanceImpactScore));
            return this;
        }
        
        /**
         * Adds a validation issue to the result.
         * 
         * @param issue the issue to add
         * @return this builder for chaining
         */
        public Builder addIssue(ValidationIssue issue) {
            if (issue != null) {
                List<ValidationIssue> newIssues = new java.util.ArrayList<>(this.issues);
                newIssues.add(issue);
                this.issues = newIssues;
                
                // Auto-adjust status based on issue severity
                if (issue.getSeverity() == IssueSeverity.CRITICAL && status != ValidationStatus.REJECTED) {
                    status = ValidationStatus.REJECTED;
                } else if (issue.getSeverity() == IssueSeverity.ERROR && 
                          status == ValidationStatus.APPROVED) {
                    status = ValidationStatus.REQUIRES_ATTENTION;
                } else if (issue.getSeverity() == IssueSeverity.WARNING && 
                          status == ValidationStatus.APPROVED) {
                    status = ValidationStatus.APPROVED_WITH_WARNINGS;
                }
            }
            return this;
        }
        
        /**
         * Builds the ValidationResult instance with auto-validation.
         * 
         * @return new validation result instance
         */
        public ValidationResult build() {
            // Auto-generate summary if not provided
            if (summary.isEmpty()) {
                summary = generateAutoSummary();
            }
            
            return new ValidationResult(this);
        }
        
        private String generateAutoSummary() {
            Map<IssueSeverity, Long> counts = issues.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    ValidationIssue::getSeverity,
                    java.util.stream.Collectors.counting()));
            
            StringBuilder summary = new StringBuilder();
            summary.append("Validation ").append(status.toString().toLowerCase().replace('_', ' '));
            
            if (!issues.isEmpty()) {
                summary.append(" with ");
                summary.append(counts.getOrDefault(IssueSeverity.CRITICAL, 0L)).append(" critical, ");
                summary.append(counts.getOrDefault(IssueSeverity.ERROR, 0L)).append(" error, ");
                summary.append(counts.getOrDefault(IssueSeverity.WARNING, 0L)).append(" warning, ");
                summary.append(counts.getOrDefault(IssueSeverity.INFO, 0L)).append(" info issues");
            } else {
                summary.append(" - no issues found");
            }
            
            return summary.toString();
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "ValidationResult{status=%s, issues=%d, validationTime=%s}",
            status, issues.size(), validationTime);
    }
    
    /**
     * Represents a single validation issue found during pattern validation.
     */
    public static final class ValidationIssue {
        private final IssueSeverity severity;
        private final String category;
        private final String message;
        private final String recommendation;
        
        public ValidationIssue(IssueSeverity severity, String category, String message, String recommendation) {
            this.severity = severity;
            this.category = category;
            this.message = message;
            this.recommendation = recommendation;
        }
        
        public IssueSeverity getSeverity() { return severity; }
        public String getCategory() { return category; }
        public String getMessage() { return message; }
        public String getRecommendation() { return recommendation; }
        
        @Override
        public String toString() {
            return String.format("[%s] %s: %s", severity, category, message);
        }
    }
}
