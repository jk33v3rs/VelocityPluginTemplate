package io.github.jk33v3rs.veloctopusrising.api.async;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Central coordination manager for all async operations in borrowed code.
 *
 * <p>This manager coordinates async operations across all borrowed implementations,
 * ensuring consistent patterns, proper resource management, and centralized
 * monitoring. It serves as the control hub for the unified async pattern
 * implementation in Step 9.</p>
 *
 * <h2>Key Responsibilities</h2>
 * <ul>
 *     <li>Coordinates async operations across borrowed components</li>
 *     <li>Manages shared executor services and thread pools</li>
 *     <li>Provides centralized error handling and recovery</li>
 *     <li>Monitors performance and resource utilization</li>
 * </ul>
 *
 * @since 1.0.0
 * @author VeloctopusRising Development Team
 */
public class AsyncCoordinationManager {

    private final Map<String, AsyncAdapter<?>> adapters = new ConcurrentHashMap<>();
    private final ExecutorService coordinationExecutor;
    private final Duration defaultTimeout;

    /**
     * Creates a new coordination manager with default settings.
     */
    public AsyncCoordinationManager() {
        this(Executors.newCachedThreadPool(), Duration.ofSeconds(30));
    }

    /**
     * Creates a new coordination manager with custom settings.
     *
     * @param executor the executor for coordination operations
     * @param defaultTimeout the default timeout for operations
     */
    public AsyncCoordinationManager(ExecutorService executor, Duration defaultTimeout) {
        this.coordinationExecutor = executor;
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * Registers an async adapter with the coordination manager.
     *
     * @param <T> the type of component being adapted
     * @param name the unique name for the adapter
     * @param adapter the async adapter to register
     * @return CompletableFuture that completes when registration is done
     */
    public <T> CompletableFuture<Void> registerAdapter(String name, AsyncAdapter<T> adapter) {
        return CompletableFuture.runAsync(() -> {
            adapters.put(name, adapter);
        }, coordinationExecutor);
    }

    /**
     * Unregisters an async adapter from the coordination manager.
     *
     * @param name the name of the adapter to unregister
     * @return CompletableFuture that completes when unregistration is done
     */
    public CompletableFuture<Void> unregisterAdapter(String name) {
        return CompletableFuture.runAsync(() -> {
            AsyncAdapter<?> adapter = adapters.remove(name);
            if (adapter != null) {
                // Attempt graceful shutdown
                adapter.stopAsync().join();
            }
        }, coordinationExecutor);
    }

    /**
     * Initializes all registered adapters.
     *
     * @return CompletableFuture that completes when all adapters are initialized
     */
    public CompletableFuture<Map<String, Boolean>> initializeAllAsync() {
        return AsyncPattern.execute(() -> {
            Map<String, Boolean> results = new ConcurrentHashMap<>();
            
            CompletableFuture<?>[] futures = adapters.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    AsyncAdapter<?> adapter = entry.getValue();
                    
                    return adapter.initializeAsync()
                        .thenRun(() -> results.put(name, true))
                        .exceptionally(throwable -> {
                            results.put(name, false);
                            return null;
                        });
                })
                .toArray(CompletableFuture[]::new);
            
            CompletableFuture.allOf(futures).join();
            return results;
        }).withTimeout(defaultTimeout.multipliedBy(2)).start();
    }

    /**
     * Starts all registered adapters.
     *
     * @return CompletableFuture that completes when all adapters are started
     */
    public CompletableFuture<Map<String, Boolean>> startAllAsync() {
        return AsyncPattern.execute(() -> {
            Map<String, Boolean> results = new ConcurrentHashMap<>();
            
            CompletableFuture<?>[] futures = adapters.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    AsyncAdapter<?> adapter = entry.getValue();
                    
                    return adapter.startAsync()
                        .thenRun(() -> results.put(name, true))
                        .exceptionally(throwable -> {
                            results.put(name, false);
                            return null;
                        });
                })
                .toArray(CompletableFuture[]::new);
            
            CompletableFuture.allOf(futures).join();
            return results;
        }).withTimeout(defaultTimeout.multipliedBy(3)).start();
    }

    /**
     * Stops all registered adapters.
     *
     * @return CompletableFuture that completes when all adapters are stopped
     */
    public CompletableFuture<Map<String, Boolean>> stopAllAsync() {
        return AsyncPattern.execute(() -> {
            Map<String, Boolean> results = new ConcurrentHashMap<>();
            
            CompletableFuture<?>[] futures = adapters.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    AsyncAdapter<?> adapter = entry.getValue();
                    
                    return adapter.stopAsync()
                        .thenRun(() -> results.put(name, true))
                        .exceptionally(throwable -> {
                            results.put(name, false);
                            return null;
                        });
                })
                .toArray(CompletableFuture[]::new);
            
            CompletableFuture.allOf(futures).join();
            return results;
        }).withTimeout(defaultTimeout.multipliedBy(2)).start();
    }

    /**
     * Performs health check on all registered adapters.
     *
     * @return CompletableFuture containing health status for each adapter
     */
    public CompletableFuture<Map<String, AsyncAdapter.HealthStatus>> healthCheckAllAsync() {
        return AsyncPattern.execute(() -> {
            Map<String, AsyncAdapter.HealthStatus> results = new ConcurrentHashMap<>();
            
            CompletableFuture<?>[] futures = adapters.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    AsyncAdapter<?> adapter = entry.getValue();
                    
                    return adapter.healthCheckAsync()
                        .thenAccept(status -> results.put(name, status))
                        .exceptionally(throwable -> {
                            results.put(name, AsyncAdapter.HealthStatus.UNKNOWN);
                            return null;
                        });
                })
                .toArray(CompletableFuture[]::new);
            
            CompletableFuture.allOf(futures).join();
            return results;
        }).withTimeout(defaultTimeout).start();
    }

    /**
     * Gets performance metrics from all registered adapters.
     *
     * @return CompletableFuture containing metrics for each adapter
     */
    public CompletableFuture<Map<String, AsyncAdapter.ComponentMetrics>> getMetricsAllAsync() {
        return AsyncPattern.execute(() -> {
            Map<String, AsyncAdapter.ComponentMetrics> results = new ConcurrentHashMap<>();
            
            CompletableFuture<?>[] futures = adapters.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    AsyncAdapter<?> adapter = entry.getValue();
                    
                    return adapter.getMetricsAsync()
                        .thenAccept(metrics -> results.put(name, metrics))
                        .exceptionally(throwable -> {
                            // Create error metrics
                            AsyncAdapter.ComponentMetrics errorMetrics = 
                                new AsyncAdapter.ComponentMetrics(0, 1, 0.0, System.currentTimeMillis());
                            results.put(name, errorMetrics);
                            return null;
                        });
                })
                .toArray(CompletableFuture[]::new);
            
            CompletableFuture.allOf(futures).join();
            return results;
        }).withTimeout(defaultTimeout).start();
    }

    /**
     * Gets a specific adapter by name.
     *
     * @param <T> the type of component
     * @param name the adapter name
     * @param type the expected adapter type
     * @return CompletableFuture containing the adapter, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<AsyncAdapter<T>> getAdapter(String name, Class<T> type) {
        return CompletableFuture.supplyAsync(() -> {
            AsyncAdapter<?> adapter = adapters.get(name);
            if (adapter != null) {
                return (AsyncAdapter<T>) adapter;
            }
            return null;
        }, coordinationExecutor);
    }

    /**
     * Gets all registered adapter names.
     *
     * @return CompletableFuture containing the set of adapter names
     */
    public CompletableFuture<Set<String>> getAdapterNames() {
        return CompletableFuture.supplyAsync(() -> 
            Set.copyOf(adapters.keySet()), coordinationExecutor);
    }

    /**
     * Executes an operation with error recovery across all adapters.
     *
     * @param <T> the type of operation result
     * @param operation the operation to execute
     * @param recovery the recovery function for errors
     * @return CompletableFuture containing the operation result
     */
    public <T> CompletableFuture<T> executeWithRecovery(
            Function<Map<String, AsyncAdapter<?>>, T> operation,
            Function<Throwable, T> recovery) {
        
        return AsyncPattern.chain(() -> Map.copyOf(adapters))
            .then(operation)
            .withErrorRecovery(recovery)
            .withTimeout(defaultTimeout)
            .execute();
    }

    /**
     * Shuts down the coordination manager and all resources.
     *
     * @return CompletableFuture that completes when shutdown is done
     */
    public CompletableFuture<Void> shutdownAsync() {
        return stopAllAsync()
            .thenRun(() -> {
                adapters.clear();
                coordinationExecutor.shutdown();
            });
    }
}
