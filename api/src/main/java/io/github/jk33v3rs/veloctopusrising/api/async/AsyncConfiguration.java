/*
 * Copyright (C) 2025 VeloctopusRising
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.jk33v3rs.veloctopusrising.api.async;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

/**
 * Configuration manager for unified async patterns across borrowed code.
 * 
 * <p>This class provides centralized configuration for all asynchronous operations
 * in the VeloctopusRising ecosystem, with optimization for 8-core Zen 5 systems
 * and cross-continental database operations.
 * 
 * <p>Thread pool configurations are optimized for:
 * <ul>
 *   <li><strong>Memory efficiency</strong> - Target &lt;512MB under load</li>
 *   <li><strong>Startup speed</strong> - &lt;30 seconds initialization time</li>
 *   <li><strong>Throughput</strong> - 1000+ concurrent players support</li>
 *   <li><strong>Latency</strong> - &lt;100ms chat message delivery</li>
 * </ul>
 * 
 * @author VeloctopusRising Team
 * @since 1.0.0
 */
public final class AsyncConfiguration {
    
    /**
     * Default thread pool configurations for each operation type.
     */
    private static final Map<UnifiedAsyncFramework.AsyncOperationType, ThreadPoolConfig> DEFAULT_CONFIGS = Map.of(
        
        UnifiedAsyncFramework.AsyncOperationType.DATABASE, ThreadPoolConfig.builder()
            .corePoolSize(2)
            .maximumPoolSize(4)
            .keepAliveTime(Duration.ofMinutes(2))
            .queueCapacity(100)
            .threadNamePrefix("VR-Database-")
            .description("Database operations with connection pooling")
            .build(),
            
        UnifiedAsyncFramework.AsyncOperationType.CACHE, ThreadPoolConfig.builder()
            .corePoolSize(2)
            .maximumPoolSize(3)
            .keepAliveTime(Duration.ofMinutes(1))
            .queueCapacity(200)
            .threadNamePrefix("VR-Cache-")
            .description("High-speed Redis cache operations")
            .build(),
            
        UnifiedAsyncFramework.AsyncOperationType.DISCORD, ThreadPoolConfig.builder()
            .corePoolSize(4) // One per bot minimum
            .maximumPoolSize(8) // Two per bot maximum
            .keepAliveTime(Duration.ofMinutes(5))
            .queueCapacity(500)
            .threadNamePrefix("VR-Discord-")
            .description("Discord API operations with rate limiting")
            .build(),
            
        UnifiedAsyncFramework.AsyncOperationType.CHAT, ThreadPoolConfig.builder()
            .corePoolSize(2)
            .maximumPoolSize(3)
            .keepAliveTime(Duration.ofMinutes(3))
            .queueCapacity(1000)
            .threadNamePrefix("VR-Chat-")
            .description("Cross-server message processing")
            .build(),
            
        UnifiedAsyncFramework.AsyncOperationType.AI, ThreadPoolConfig.builder()
            .corePoolSize(1)
            .maximumPoolSize(2)
            .keepAliveTime(Duration.ofMinutes(10))
            .queueCapacity(50)
            .threadNamePrefix("VR-AI-")
            .description("Python bridge and AI model operations")
            .build()
    );
    
    /**
     * Timeout configurations for different operation types.
     */
    private static final Map<UnifiedAsyncFramework.AsyncOperationType, Duration> DEFAULT_TIMEOUTS = Map.of(
        UnifiedAsyncFramework.AsyncOperationType.DATABASE, Duration.ofSeconds(30),
        UnifiedAsyncFramework.AsyncOperationType.CACHE, Duration.ofSeconds(5),
        UnifiedAsyncFramework.AsyncOperationType.DISCORD, Duration.ofSeconds(15),
        UnifiedAsyncFramework.AsyncOperationType.CHAT, Duration.ofSeconds(10),
        UnifiedAsyncFramework.AsyncOperationType.AI, Duration.ofMinutes(2)
    );
    
    /**
     * Retry configurations for different operation types.
     */
    private static final Map<UnifiedAsyncFramework.AsyncOperationType, RetryConfig> DEFAULT_RETRIES = Map.of(
        UnifiedAsyncFramework.AsyncOperationType.DATABASE, RetryConfig.builder()
            .maxRetries(3)
            .baseDelay(Duration.ofMillis(500))
            .maxDelay(Duration.ofSeconds(5))
            .backoffMultiplier(2.0)
            .build(),
            
        UnifiedAsyncFramework.AsyncOperationType.CACHE, RetryConfig.builder()
            .maxRetries(2)
            .baseDelay(Duration.ofMillis(100))
            .maxDelay(Duration.ofSeconds(1))
            .backoffMultiplier(1.5)
            .build(),
            
        UnifiedAsyncFramework.AsyncOperationType.DISCORD, RetryConfig.builder()
            .maxRetries(5)
            .baseDelay(Duration.ofSeconds(1))
            .maxDelay(Duration.ofMinutes(1))
            .backoffMultiplier(2.0)
            .build()
    );
    
    /**
     * Gets the thread pool configuration for an operation type.
     * 
     * @param operationType the operation type
     * @return the thread pool configuration
     */
    public static ThreadPoolConfig getThreadPoolConfig(UnifiedAsyncFramework.AsyncOperationType operationType) {
        return DEFAULT_CONFIGS.getOrDefault(operationType, getDefaultConfig());
    }
    
    /**
     * Gets the timeout duration for an operation type.
     * 
     * @param operationType the operation type
     * @return the timeout duration
     */
    public static Duration getTimeout(UnifiedAsyncFramework.AsyncOperationType operationType) {
        return DEFAULT_TIMEOUTS.getOrDefault(operationType, Duration.ofSeconds(30));
    }
    
    /**
     * Gets the retry configuration for an operation type.
     * 
     * @param operationType the operation type
     * @return the retry configuration
     */
    public static RetryConfig getRetryConfig(UnifiedAsyncFramework.AsyncOperationType operationType) {
        return DEFAULT_RETRIES.getOrDefault(operationType, getDefaultRetryConfig());
    }
    
    /**
     * Creates a thread factory for an operation type.
     * 
     * @param operationType the operation type
     * @return configured thread factory
     */
    public static ThreadFactory createThreadFactory(UnifiedAsyncFramework.AsyncOperationType operationType) {
        ThreadPoolConfig config = getThreadPoolConfig(operationType);
        return new VeloctopusThreadFactory(config.getThreadNamePrefix(), operationType);
    }
    
    /**
     * Gets the default thread pool configuration.
     * 
     * @return default configuration
     */
    private static ThreadPoolConfig getDefaultConfig() {
        return ThreadPoolConfig.builder()
            .corePoolSize(1)
            .maximumPoolSize(2)
            .keepAliveTime(Duration.ofMinutes(1))
            .queueCapacity(100)
            .threadNamePrefix("VR-Generic-")
            .description("Generic async operations")
            .build();
    }
    
    /**
     * Gets the default retry configuration.
     * 
     * @return default retry configuration
     */
    private static RetryConfig getDefaultRetryConfig() {
        return RetryConfig.builder()
            .maxRetries(3)
            .baseDelay(Duration.ofMillis(500))
            .maxDelay(Duration.ofSeconds(10))
            .backoffMultiplier(2.0)
            .build();
    }
    
    /**
     * Thread pool configuration for async operations.
     */
    public static final class ThreadPoolConfig {
        private final int corePoolSize;
        private final int maximumPoolSize;
        private final Duration keepAliveTime;
        private final int queueCapacity;
        private final String threadNamePrefix;
        private final String description;
        
        private ThreadPoolConfig(Builder builder) {
            this.corePoolSize = builder.corePoolSize;
            this.maximumPoolSize = builder.maximumPoolSize;
            this.keepAliveTime = builder.keepAliveTime;
            this.queueCapacity = builder.queueCapacity;
            this.threadNamePrefix = builder.threadNamePrefix;
            this.description = builder.description;
        }
        
        public int getCorePoolSize() { return corePoolSize; }
        public int getMaximumPoolSize() { return maximumPoolSize; }
        public Duration getKeepAliveTime() { return keepAliveTime; }
        public int getQueueCapacity() { return queueCapacity; }
        public String getThreadNamePrefix() { return threadNamePrefix; }
        public String getDescription() { return description; }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static final class Builder {
            private int corePoolSize = 1;
            private int maximumPoolSize = 2;
            private Duration keepAliveTime = Duration.ofMinutes(1);
            private int queueCapacity = 100;
            private String threadNamePrefix = "VR-";
            private String description = "";
            
            public Builder corePoolSize(int corePoolSize) {
                this.corePoolSize = corePoolSize;
                return this;
            }
            
            public Builder maximumPoolSize(int maximumPoolSize) {
                this.maximumPoolSize = maximumPoolSize;
                return this;
            }
            
            public Builder keepAliveTime(Duration keepAliveTime) {
                this.keepAliveTime = keepAliveTime;
                return this;
            }
            
            public Builder queueCapacity(int queueCapacity) {
                this.queueCapacity = queueCapacity;
                return this;
            }
            
            public Builder threadNamePrefix(String threadNamePrefix) {
                this.threadNamePrefix = threadNamePrefix;
                return this;
            }
            
            public Builder description(String description) {
                this.description = description;
                return this;
            }
            
            public ThreadPoolConfig build() {
                return new ThreadPoolConfig(this);
            }
        }
    }
    
    /**
     * Retry configuration for async operations.
     */
    public static final class RetryConfig {
        private final int maxRetries;
        private final Duration baseDelay;
        private final Duration maxDelay;
        private final double backoffMultiplier;
        
        private RetryConfig(Builder builder) {
            this.maxRetries = builder.maxRetries;
            this.baseDelay = builder.baseDelay;
            this.maxDelay = builder.maxDelay;
            this.backoffMultiplier = builder.backoffMultiplier;
        }
        
        public int getMaxRetries() { return maxRetries; }
        public Duration getBaseDelay() { return baseDelay; }
        public Duration getMaxDelay() { return maxDelay; }
        public double getBackoffMultiplier() { return backoffMultiplier; }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static final class Builder {
            private int maxRetries = 3;
            private Duration baseDelay = Duration.ofMillis(500);
            private Duration maxDelay = Duration.ofSeconds(10);
            private double backoffMultiplier = 2.0;
            
            public Builder maxRetries(int maxRetries) {
                this.maxRetries = maxRetries;
                return this;
            }
            
            public Builder baseDelay(Duration baseDelay) {
                this.baseDelay = baseDelay;
                return this;
            }
            
            public Builder maxDelay(Duration maxDelay) {
                this.maxDelay = maxDelay;
                return this;
            }
            
            public Builder backoffMultiplier(double backoffMultiplier) {
                this.backoffMultiplier = backoffMultiplier;
                return this;
            }
            
            public RetryConfig build() {
                return new RetryConfig(this);
            }
        }
    }
    
    /**
     * Custom thread factory for VeloctopusRising async operations.
     */
    private static final class VeloctopusThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final UnifiedAsyncFramework.AsyncOperationType operationType;
        private volatile int threadNumber = 1;
        
        VeloctopusThreadFactory(String namePrefix, UnifiedAsyncFramework.AsyncOperationType operationType) {
            this.namePrefix = namePrefix;
            this.operationType = operationType;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, namePrefix + threadNumber++);
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY);
            
            // Set appropriate thread priorities based on operation type
            switch (operationType) {
                case CHAT -> thread.setPriority(Thread.NORM_PRIORITY + 1); // Higher priority for chat
                case DISCORD -> thread.setPriority(Thread.NORM_PRIORITY);
                case DATABASE -> thread.setPriority(Thread.NORM_PRIORITY - 1); // Lower priority for database
                case CACHE -> thread.setPriority(Thread.NORM_PRIORITY + 1); // Higher priority for cache
                case AI -> thread.setPriority(Thread.NORM_PRIORITY - 2); // Lowest priority for AI
                default -> thread.setPriority(Thread.NORM_PRIORITY);
            }
            
            return thread;
        }
    }
    
    private AsyncConfiguration() {
        throw new UnsupportedOperationException("Utility class");
    }
}
