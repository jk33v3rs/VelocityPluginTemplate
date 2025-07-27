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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Unified async pattern framework for all borrowed code implementations.
 * 
 * <p>This framework provides standardized asynchronous execution patterns across
 * all borrowed code from reference projects, ensuring consistent performance,
 * error handling, and thread safety throughout the VeloctopusRising ecosystem.
 * 
 * <p>Key principles:
 * <ul>
 *   <li><strong>Main Thread Protection</strong> - Zero blocking operations on Velocity's main thread</li>
 *   <li><strong>Dedicated Thread Pools</strong> - Separate pools for different operation types</li>
 *   <li><strong>8-Core Zen 5 Optimization</strong> - Efficient core utilization patterns</li>
 *   <li><strong>Cross-Continental SQL</strong> - High-latency database operation support</li>
 *   <li><strong>Graceful Degradation</strong> - Fallback patterns for service failures</li>
 * </ul>
 * 
 * <p>Thread pool allocation:
 * <ul>
 *   <li><strong>Database Pool</strong> - HikariCP + async operations (2-4 threads)</li>
 *   <li><strong>Redis Pool</strong> - High-speed cache operations (2-3 threads)</li>
 *   <li><strong>Discord Pool</strong> - JDA bot operations (1-2 threads per bot)</li>
 *   <li><strong>Chat Pool</strong> - Message processing and routing (2-3 threads)</li>
 *   <li><strong>AI Pool</strong> - Python bridge and LLM operations (1-2 threads)</li>
 * </ul>
 * 
 * @author VeloctopusRising Team
 * @since 1.0.0
 */
public interface UnifiedAsyncFramework {
    
    /**
     * Executes database operations asynchronously with proper error handling.
     * 
     * <p>This method ensures all database operations are executed on the dedicated
     * database thread pool, with automatic connection management, retry logic,
     * and graceful degradation to fallback data sources.
     * 
     * @param <T> the type of the operation result
     * @param operation the database operation to execute
     * @return future completing with the operation result
     */
    <T> CompletableFuture<T> executeDatabase(Supplier<T> operation);
    
    /**
     * Executes Redis cache operations with fallback to database.
     * 
     * <p>This method provides high-speed cache operations with automatic fallback
     * to database sources when Redis is unavailable, ensuring system resilience.
     * 
     * @param <T> the type of the cached data
     * @param cacheKey the cache key for the operation
     * @param cacheOperation the Redis operation to execute
     * @param fallbackOperation the database fallback operation
     * @return future completing with cached or fallback data
     */
    <T> CompletableFuture<T> executeCache(
        String cacheKey,
        Supplier<T> cacheOperation,
        Supplier<T> fallbackOperation
    );
    
    /**
     * Executes Discord bot operations with rate limiting and error recovery.
     * 
     * <p>This method manages Discord API calls across multiple bot instances,
     * with automatic rate limiting, request queuing, and bot failover support.
     * 
     * @param <T> the type of the Discord operation result
     * @param botId the ID of the bot to use for the operation
     * @param operation the Discord operation to execute
     * @return future completing with the operation result
     */
    <T> CompletableFuture<T> executeDiscord(String botId, Supplier<T> operation);
    
    /**
     * Executes chat message processing with cross-server routing.
     * 
     * <p>This method handles message processing across multiple servers with
     * automatic routing, filtering, and delivery confirmation.
     * 
     * @param <T> the type of the message processing result
     * @param serverId the target server ID for the message
     * @param operation the chat operation to execute
     * @return future completing with the processing result
     */
    <T> CompletableFuture<T> executeChat(String serverId, Supplier<T> operation);
    
    /**
     * Executes AI bridge operations with timeout and retry logic.
     * 
     * <p>This method manages Python bridge communications for AI operations,
     * with automatic timeout handling, retry logic, and graceful degradation.
     * 
     * @param <T> the type of the AI operation result
     * @param operation the AI operation to execute
     * @param timeoutMs the timeout in milliseconds for the operation
     * @return future completing with the AI operation result
     */
    <T> CompletableFuture<T> executeAI(Supplier<T> operation, long timeoutMs);
    
    /**
     * Executes generic borrowed code operations on appropriate thread pools.
     * 
     * <p>This method provides automatic thread pool selection based on operation
     * type, ensuring optimal performance and resource utilization.
     * 
     * @param <T> the type of the operation result
     * @param operationType the type of operation being executed
     * @param operation the operation to execute
     * @return future completing with the operation result
     */
    <T> CompletableFuture<T> execute(AsyncOperationType operationType, Supplier<T> operation);
    
    /**
     * Chains multiple async operations with error handling and recovery.
     * 
     * <p>This method enables complex operation chains with automatic error
     * propagation, recovery mechanisms, and performance monitoring.
     * 
     * @param <T> the type of the initial operation result
     * @param <U> the type of the final operation result
     * @param initialOperation the first operation in the chain
     * @param chainedOperation the subsequent operation
     * @param errorRecovery the error recovery function
     * @return future completing with the final result
     */
    <T, U> CompletableFuture<U> chain(
        CompletableFuture<T> initialOperation,
        Function<T, CompletableFuture<U>> chainedOperation,
        Function<Throwable, CompletableFuture<U>> errorRecovery
    );
    
    /**
     * Creates a timeout-aware future with automatic cancellation.
     * 
     * @param <T> the type of the operation result
     * @param operation the operation to execute with timeout
     * @param timeoutMs the timeout in milliseconds
     * @return future that completes or times out as specified
     */
    <T> CompletableFuture<T> withTimeout(Supplier<T> operation, long timeoutMs);
    
    /**
     * Executes operations with retry logic and exponential backoff.
     * 
     * @param <T> the type of the operation result
     * @param operation the operation to retry
     * @param maxRetries the maximum number of retry attempts
     * @param baseDelayMs the base delay between retries in milliseconds
     * @return future completing with the operation result or final failure
     */
    <T> CompletableFuture<T> withRetry(
        Supplier<CompletableFuture<T>> operation,
        int maxRetries,
        long baseDelayMs
    );
    
    /**
     * Gets the appropriate executor for an operation type.
     * 
     * @param operationType the type of operation
     * @return the executor for this operation type
     */
    Executor getExecutor(AsyncOperationType operationType);
    
    /**
     * Shuts down all managed thread pools gracefully.
     * 
     * @return future completing when all pools are shut down
     */
    CompletableFuture<Void> shutdown();
    
    /**
     * Types of asynchronous operations supported by the framework.
     */
    enum AsyncOperationType {
        /**
         * Database operations (MariaDB/SQLite).
         */
        DATABASE("database", "Database operations with connection pooling"),
        
        /**
         * Redis cache operations.
         */
        CACHE("cache", "High-speed cache operations with fallback"),
        
        /**
         * Discord bot operations.
         */
        DISCORD("discord", "Discord API operations with rate limiting"),
        
        /**
         * Chat message processing.
         */
        CHAT("chat", "Cross-server message processing and routing"),
        
        /**
         * AI bridge operations.
         */
        AI("ai", "Python bridge and AI model operations"),
        
        /**
         * General borrowed code operations.
         */
        BORROWED("borrowed", "General borrowed code execution"),
        
        /**
         * Configuration and setup operations.
         */
        CONFIG("config", "Configuration loading and validation"),
        
        /**
         * Network and API operations.
         */
        NETWORK("network", "External network and API calls"),
        
        /**
         * File I/O operations.
         */
        FILE_IO("file", "File system operations and data persistence"),
        
        /**
         * Monitoring and metrics operations.
         */
        MONITORING("monitoring", "Performance monitoring and metrics collection");
        
        private final String typeName;
        private final String description;
        
        AsyncOperationType(String typeName, String description) {
            this.typeName = typeName;
            this.description = description;
        }
        
        /**
         * @return the operation type name
         */
        public String getTypeName() {
            return typeName;
        }
        
        /**
         * @return the description of this operation type
         */
        public String getDescription() {
            return description;
        }
    }
}
