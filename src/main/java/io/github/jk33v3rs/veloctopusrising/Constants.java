package io.github.jk33v3rs.veloctopusrising;

/**
 * Constants for the Veloctopus Rising plugin.
 * 
 * <p>This class contains version information and other compile-time constants
 * used throughout the plugin.</p>
 * 
 * @since 1.0.0
 * @author jk33v3rs
 */
public final class Constants {
    
    /**
     * The current version of the Veloctopus Rising plugin.
     */
    public static final String VERSION = "1.0.0-SNAPSHOT";
    
    /**
     * The plugin identifier used by Velocity.
     */
    public static final String PLUGIN_ID = "veloctopusrising";
    
    /**
     * The plugin name displayed to users.
     */
    public static final String PLUGIN_NAME = "Veloctopus Rising";
    
    /**
     * The plugin description.
     */
    public static final String PLUGIN_DESCRIPTION = "Communication and Translation Hub for Multi-Platform Communities";
    
    /**
     * The plugin author.
     */
    public static final String PLUGIN_AUTHOR = "jk33v3rs";
    
    /**
     * The plugin website URL.
     */
    public static final String PLUGIN_URL = "https://github.com/jk33v3rs/Veloctopus-Rising";
    
    /**
     * Minimum Java version required.
     */
    public static final int MIN_JAVA_VERSION = 21;
    
    /**
     * Minimum Velocity API version required.
     */
    public static final String MIN_VELOCITY_VERSION = "3.4.0";
    
    // Prevent instantiation
    private Constants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
}
