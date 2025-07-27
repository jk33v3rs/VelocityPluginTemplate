package io.github.jk33v3rs.veloctopusrising.api.extraction;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Comprehensive statistics for extraction framework usage and performance.
 * 
 * <p>This immutable class provides detailed metrics about extraction framework
 * operations, including pattern extraction counts, adaptation success rates,
 * performance metrics, and system health indicators.</p>
 * 
 * <p><strong>Thread Safety:</strong> Immutable class, fully thread-safe.</p>
 * 
 * <p><strong>Metrics Collection:</strong> Statistics are collected continuously
 * and provide insights into framework usage patterns and performance trends.</p>
 * 
 * @author VelocityCommAPI Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see ExtractionFramework
 */
public final class ExtractionStatistics {
    
    private final long totalExtractions;
    private final long successfulExtractions;
    private final long failedExtractions;
    private final long totalAdaptations;
    private final long successfulAdaptations;
    private final long totalIntegrations;
    private final long successfulIntegrations;
    private final Duration averageExtractionTime;
    private final Duration averageAdaptationTime;
    private final Duration averageIntegrationTime;
    private final Map<ReferenceProject, Long> extractionsByProject;
    private final Map<AdaptationTarget, Long> adaptationsByTarget;
    private final Instant statisticsStartTime;
    private final Instant lastUpdated;
    private final long cacheHits;
    private final long cacheMisses;
    
    /**
     * Creates a new ExtractionStatistics instance.
     * 
     * @param builder the builder containing statistics information
     */
    private ExtractionStatistics(Builder builder) {
        this.totalExtractions = builder.totalExtractions;
        this.successfulExtractions = builder.successfulExtractions;
        this.failedExtractions = builder.failedExtractions;
        this.totalAdaptations = builder.totalAdaptations;
        this.successfulAdaptations = builder.successfulAdaptations;
        this.totalIntegrations = builder.totalIntegrations;
        this.successfulIntegrations = builder.successfulIntegrations;
        this.averageExtractionTime = builder.averageExtractionTime;
        this.averageAdaptationTime = builder.averageAdaptationTime;
        this.averageIntegrationTime = builder.averageIntegrationTime;
        this.extractionsByProject = Map.copyOf(builder.extractionsByProject);
        this.adaptationsByTarget = Map.copyOf(builder.adaptationsByTarget);
        this.statisticsStartTime = builder.statisticsStartTime;
        this.lastUpdated = builder.lastUpdated;
        this.cacheHits = builder.cacheHits;
        this.cacheMisses = builder.cacheMisses;
    }
    
    /**
     * Gets the total number of extraction attempts.
     * 
     * @return total extraction count
     */
    public long getTotalExtractions() {
        return totalExtractions;
    }
    
    /**
     * Gets the number of successful extractions.
     * 
     * @return successful extraction count
     */
    public long getSuccessfulExtractions() {
        return successfulExtractions;
    }
    
    /**
     * Gets the number of failed extractions.
     * 
     * @return failed extraction count
     */
    public long getFailedExtractions() {
        return failedExtractions;
    }
    
    /**
     * Gets the extraction success rate as a percentage.
     * 
     * @return success rate from 0.0 to 100.0
     */
    public double getExtractionSuccessRate() {
        if (totalExtractions == 0) return 100.0;
        return (successfulExtractions / (double) totalExtractions) * 100.0;
    }
    
    /**
     * Gets the total number of adaptation attempts.
     * 
     * @return total adaptation count
     */
    public long getTotalAdaptations() {
        return totalAdaptations;
    }
    
    /**
     * Gets the number of successful adaptations.
     * 
     * @return successful adaptation count
     */
    public long getSuccessfulAdaptations() {
        return successfulAdaptations;
    }
    
    /**
     * Gets the adaptation success rate as a percentage.
     * 
     * @return success rate from 0.0 to 100.0
     */
    public double getAdaptationSuccessRate() {
        if (totalAdaptations == 0) return 100.0;
        return (successfulAdaptations / (double) totalAdaptations) * 100.0;
    }
    
    /**
     * Gets the total number of integration attempts.
     * 
     * @return total integration count
     */
    public long getTotalIntegrations() {
        return totalIntegrations;
    }
    
    /**
     * Gets the number of successful integrations.
     * 
     * @return successful integration count
     */
    public long getSuccessfulIntegrations() {
        return successfulIntegrations;
    }
    
    /**
     * Gets the integration success rate as a percentage.
     * 
     * @return success rate from 0.0 to 100.0
     */
    public double getIntegrationSuccessRate() {
        if (totalIntegrations == 0) return 100.0;
        return (successfulIntegrations / (double) totalIntegrations) * 100.0;
    }
    
    /**
     * Gets the average time for pattern extraction.
     * 
     * @return average extraction time
     */
    public Duration getAverageExtractionTime() {
        return averageExtractionTime;
    }
    
    /**
     * Gets the average time for pattern adaptation.
     * 
     * @return average adaptation time
     */
    public Duration getAverageAdaptationTime() {
        return averageAdaptationTime;
    }
    
    /**
     * Gets the average time for pattern integration.
     * 
     * @return average integration time
     */
    public Duration getAverageIntegrationTime() {
        return averageIntegrationTime;
    }
    
    /**
     * Gets extraction counts by reference project.
     * 
     * @return immutable map of project to extraction count
     */
    public Map<ReferenceProject, Long> getExtractionsByProject() {
        return extractionsByProject;
    }
    
    /**
     * Gets adaptation counts by target system.
     * 
     * @return immutable map of target to adaptation count
     */
    public Map<AdaptationTarget, Long> getAdaptationsByTarget() {
        return adaptationsByTarget;
    }
    
    /**
     * Gets the timestamp when statistics collection started.
     * 
     * @return statistics start time
     */
    public Instant getStatisticsStartTime() {
        return statisticsStartTime;
    }
    
    /**
     * Gets the timestamp of the last statistics update.
     * 
     * @return last update timestamp
     */
    public Instant getLastUpdated() {
        return lastUpdated;
    }
    
    /**
     * Gets the number of cache hits for pattern storage.
     * 
     * @return cache hit count
     */
    public long getCacheHits() {
        return cacheHits;
    }
    
    /**
     * Gets the number of cache misses for pattern storage.
     * 
     * @return cache miss count
     */
    public long getCacheMisses() {
        return cacheMisses;
    }
    
    /**
     * Gets the cache hit rate as a percentage.
     * 
     * @return cache hit rate from 0.0 to 100.0
     */
    public double getCacheHitRate() {
        long totalCacheRequests = cacheHits + cacheMisses;
        if (totalCacheRequests == 0) return 100.0;
        return (cacheHits / (double) totalCacheRequests) * 100.0;
    }
    
    /**
     * Gets the duration since statistics collection started.
     * 
     * @return uptime duration
     */
    public Duration getUptime() {
        return Duration.between(statisticsStartTime, lastUpdated);
    }
    
    /**
     * Creates a new builder for constructing ExtractionStatistics instances.
     * 
     * @return new statistics builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for creating ExtractionStatistics instances.
     */
    public static final class Builder {
        private long totalExtractions = 0;
        private long successfulExtractions = 0;
        private long failedExtractions = 0;
        private long totalAdaptations = 0;
        private long successfulAdaptations = 0;
        private long totalIntegrations = 0;
        private long successfulIntegrations = 0;
        private Duration averageExtractionTime = Duration.ZERO;
        private Duration averageAdaptationTime = Duration.ZERO;
        private Duration averageIntegrationTime = Duration.ZERO;
        private Map<ReferenceProject, Long> extractionsByProject = Map.of();
        private Map<AdaptationTarget, Long> adaptationsByTarget = Map.of();
        private Instant statisticsStartTime = Instant.now();
        private Instant lastUpdated = Instant.now();
        private long cacheHits = 0;
        private long cacheMisses = 0;
        
        private Builder() {}
        
        public Builder totalExtractions(long totalExtractions) {
            this.totalExtractions = Math.max(0, totalExtractions);
            return this;
        }
        
        public Builder successfulExtractions(long successfulExtractions) {
            this.successfulExtractions = Math.max(0, successfulExtractions);
            return this;
        }
        
        public Builder failedExtractions(long failedExtractions) {
            this.failedExtractions = Math.max(0, failedExtractions);
            return this;
        }
        
        public Builder totalAdaptations(long totalAdaptations) {
            this.totalAdaptations = Math.max(0, totalAdaptations);
            return this;
        }
        
        public Builder successfulAdaptations(long successfulAdaptations) {
            this.successfulAdaptations = Math.max(0, successfulAdaptations);
            return this;
        }
        
        public Builder totalIntegrations(long totalIntegrations) {
            this.totalIntegrations = Math.max(0, totalIntegrations);
            return this;
        }
        
        public Builder successfulIntegrations(long successfulIntegrations) {
            this.successfulIntegrations = Math.max(0, successfulIntegrations);
            return this;
        }
        
        public Builder averageExtractionTime(Duration averageExtractionTime) {
            this.averageExtractionTime = averageExtractionTime != null ? 
                averageExtractionTime : Duration.ZERO;
            return this;
        }
        
        public Builder averageAdaptationTime(Duration averageAdaptationTime) {
            this.averageAdaptationTime = averageAdaptationTime != null ? 
                averageAdaptationTime : Duration.ZERO;
            return this;
        }
        
        public Builder averageIntegrationTime(Duration averageIntegrationTime) {
            this.averageIntegrationTime = averageIntegrationTime != null ? 
                averageIntegrationTime : Duration.ZERO;
            return this;
        }
        
        public Builder extractionsByProject(Map<ReferenceProject, Long> extractionsByProject) {
            this.extractionsByProject = extractionsByProject != null ? 
                extractionsByProject : Map.of();
            return this;
        }
        
        public Builder adaptationsByTarget(Map<AdaptationTarget, Long> adaptationsByTarget) {
            this.adaptationsByTarget = adaptationsByTarget != null ? 
                adaptationsByTarget : Map.of();
            return this;
        }
        
        public Builder statisticsStartTime(Instant statisticsStartTime) {
            this.statisticsStartTime = statisticsStartTime != null ? 
                statisticsStartTime : Instant.now();
            return this;
        }
        
        public Builder lastUpdated(Instant lastUpdated) {
            this.lastUpdated = lastUpdated != null ? lastUpdated : Instant.now();
            return this;
        }
        
        public Builder cacheHits(long cacheHits) {
            this.cacheHits = Math.max(0, cacheHits);
            return this;
        }
        
        public Builder cacheMisses(long cacheMisses) {
            this.cacheMisses = Math.max(0, cacheMisses);
            return this;
        }
        
        public ExtractionStatistics build() {
            return new ExtractionStatistics(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "ExtractionStatistics{extractions=%d/%d (%.1f%%), adaptations=%d/%d (%.1f%%), integrations=%d/%d (%.1f%%), uptime=%s}",
            successfulExtractions, totalExtractions, getExtractionSuccessRate(),
            successfulAdaptations, totalAdaptations, getAdaptationSuccessRate(),
            successfulIntegrations, totalIntegrations, getIntegrationSuccessRate(),
            getUptime());
    }
}
