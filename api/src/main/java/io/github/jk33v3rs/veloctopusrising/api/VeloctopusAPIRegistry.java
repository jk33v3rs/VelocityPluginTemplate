package io.github.jk33v3rs.veloctopusrising.api;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Internal registry for managing the singleton API provider instance.
 * 
 * <p>This class maintains the global reference to the active API provider
 * and ensures thread-safe access across the entire plugin ecosystem.</p>
 * 
 * <h2>Thread Safety:</h2>
 * <p>This class is fully thread-safe using atomic references and guarantees
 * consistent visibility across all threads.</p>
 * 
 * @since 1.0.0
 * @author jk33v3rs
 */
final class VeloctopusAPIRegistry {
    
    /**
     * Atomic reference to the current API provider instance.
     * Uses AtomicReference for thread-safe access without blocking.
     */
    private static final AtomicReference<CommAPIProvider> INSTANCE = 
        new AtomicReference<>();
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private VeloctopusAPIRegistry() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Gets the current API provider instance.
     * 
     * <p>Returns the active API provider if available, or empty if the
     * plugin is not initialized or has been shut down.</p>
     * 
     * @return Optional containing the current API provider
     * @since 1.0.0
     */
    static Optional<CommAPIProvider> getInstance() {
        return Optional.ofNullable(INSTANCE.get());
    }
    
    /**
     * Registers a new API provider instance.
     * 
     * <p>This method is called during plugin initialization to register
     * the main API provider implementation. Only one provider can be
     * active at a time.</p>
     * 
     * @param provider The API provider implementation to register
     * @throws IllegalStateException if a provider is already registered
     * @since 1.0.0
     */
    static void register(CommAPIProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("API provider cannot be null");
        }
        
        if (!INSTANCE.compareAndSet(null, provider)) {
            throw new IllegalStateException("API provider is already registered");
        }
    }
    
    /**
     * Unregisters the current API provider.
     * 
     * <p>This method is called during plugin shutdown to clean up
     * the API provider reference. After this call, getInstance()
     * will return empty.</p>
     * 
     * @since 1.0.0
     */
    static void unregister() {
        INSTANCE.set(null);
    }
}
