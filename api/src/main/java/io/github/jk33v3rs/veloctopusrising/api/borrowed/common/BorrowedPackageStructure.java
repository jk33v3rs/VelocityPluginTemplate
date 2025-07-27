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

package io.github.jk33v3rs.veloctopusrising.api.borrowed.common;

import io.github.jk33v3rs.veloctopusrising.api.extraction.ReferenceProject;
import java.util.Map;
import java.util.Set;

/**
 * Defines standard package structures for borrowed implementations.
 * 
 * <p>This class provides standardized package mappings and naming conventions
 * for organizing borrowed code from reference projects within the VeloctopusRising
 * ecosystem.
 * 
 * <p>Package naming follows the pattern:
 * <pre>
 * io.github.jk33v3rs.veloctopusrising.api.borrowed.{project}.{category}
 * </pre>
 * 
 * Where categories include:
 * <ul>
 *   <li><code>core</code> - Essential functionality</li>
 *   <li><code>integration</code> - VeloctopusRising integration adapters</li>
 *   <li><code>util</code> - Utility classes and helpers</li>
 *   <li><code>config</code> - Configuration and settings</li>
 *   <li><code>event</code> - Event handling and listeners</li>
 *   <li><code>command</code> - Command implementations</li>
 *   <li><code>data</code> - Data models and persistence</li>
 * </ul>
 * 
 * @author VeloctopusRising Team
 * @since 1.0.0
 */
public final class BorrowedPackageStructure {
    
    /**
     * Base package for all borrowed implementations.
     */
    public static final String BASE_PACKAGE = "io.github.jk33v3rs.veloctopusrising.api.borrowed";
    
    /**
     * Package categories for organizing borrowed code.
     */
    public enum PackageCategory {
        CORE("core", "Essential functionality and main logic"),
        INTEGRATION("integration", "VeloctopusRising API integration adapters"),
        UTIL("util", "Utility classes and helper methods"),
        CONFIG("config", "Configuration and settings management"),
        EVENT("event", "Event handling and listener implementations"),
        COMMAND("command", "Command implementations and processors"),
        DATA("data", "Data models and persistence layers"),
        API("api", "External API interfaces and contracts"),
        SECURITY("security", "Security and authentication components"),
        NETWORK("network", "Network communication and protocols");
        
        private final String packageName;
        private final String description;
        
        PackageCategory(String packageName, String description) {
            this.packageName = packageName;
            this.description = description;
        }
        
        /**
         * @return the package name segment
         */
        public String getPackageName() {
            return packageName;
        }
        
        /**
         * @return the description of this category
         */
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Standard package mappings for reference projects.
     * Maps original package patterns to VeloctopusRising categories.
     */
    private static final Map<ReferenceProject, Map<String, PackageCategory>> PROJECT_MAPPINGS = Map.of(
        ReferenceProject.SPICORD, Map.of(
            "sh.okx.spicord.bot", PackageCategory.CORE,
            "sh.okx.spicord.discord", PackageCategory.INTEGRATION,
            "sh.okx.spicord.util", PackageCategory.UTIL,
            "sh.okx.spicord.config", PackageCategory.CONFIG,
            "sh.okx.spicord.api", PackageCategory.API
        ),
        
        ReferenceProject.CHATREGULATOR, Map.of(
            "me.whereareiam.chatregulator.core", PackageCategory.CORE,
            "me.whereareiam.chatregulator.filter", PackageCategory.SECURITY,
            "me.whereareiam.chatregulator.command", PackageCategory.COMMAND,
            "me.whereareiam.chatregulator.config", PackageCategory.CONFIG,
            "me.whereareiam.chatregulator.util", PackageCategory.UTIL
        ),
        
        ReferenceProject.EPICGUARD, Map.of(
            "me.xneox.epicguard.core", PackageCategory.CORE,
            "me.xneox.epicguard.velocity", PackageCategory.INTEGRATION,
            "me.xneox.epicguard.common", PackageCategory.UTIL,
            "me.xneox.epicguard.config", PackageCategory.CONFIG,
            "me.xneox.epicguard.security", PackageCategory.SECURITY
        ),
        
        ReferenceProject.HUSKCHAT, Map.of(
            "net.william278.huskchat.velocity", PackageCategory.INTEGRATION,
            "net.william278.huskchat.channel", PackageCategory.CORE,
            "net.william278.huskchat.message", PackageCategory.DATA,
            "net.william278.huskchat.user", PackageCategory.DATA,
            "net.william278.huskchat.config", PackageCategory.CONFIG
        )
    );
    
    /**
     * Class naming conventions for borrowed implementations.
     */
    private static final Map<ReferenceProject, String> CLASS_PREFIXES = Map.of(
        ReferenceProject.SPICORD, "VR",
        ReferenceProject.CHATREGULATOR, "VR",
        ReferenceProject.EPICGUARD, "VR",
        ReferenceProject.HUSKCHAT, "VR", 
        ReferenceProject.KICKREDIRECT, "VR",
        ReferenceProject.SIGNEDVELOCITY, "VR",
        ReferenceProject.VLOBBY, "VR",
        ReferenceProject.VPACKETEVENTS, "VR",
        ReferenceProject.VELEMONAID, "VR",
        ReferenceProject.DISCORD_AI_BOT, "VR"
    );
    
    /**
     * Gets the target package name for borrowed code.
     * 
     * @param project the reference project
     * @param originalPackage the original package name
     * @return the target package name in VeloctopusRising structure
     */
    public static String getTargetPackage(ReferenceProject project, String originalPackage) {
        Map<String, PackageCategory> mappings = PROJECT_MAPPINGS.get(project);
        if (mappings == null) {
            return BASE_PACKAGE + "." + project.name().toLowerCase() + ".core";
        }
        
        PackageCategory category = mappings.entrySet().stream()
            .filter(entry -> originalPackage.startsWith(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(PackageCategory.UTIL);
        
        return BASE_PACKAGE + "." + project.name().toLowerCase() + "." + category.getPackageName();
    }
    
    /**
     * Gets the renamed class name for borrowed implementations.
     * 
     * @param project the reference project
     * @param originalClassName the original class name
     * @return the renamed class name with VeloctopusRising prefix
     */
    public static String getRenamedClassName(ReferenceProject project, String originalClassName) {
        String prefix = CLASS_PREFIXES.get(project);
        if (prefix == null) {
            prefix = "VR";
        }
        
        // Don't double-prefix if already prefixed
        if (originalClassName.startsWith(prefix)) {
            return originalClassName;
        }
        
        return prefix + originalClassName;
    }
    
    /**
     * Gets all available package categories.
     * 
     * @return set of all package categories
     */
    public static Set<PackageCategory> getAllCategories() {
        return Set.of(PackageCategory.values());
    }
    
    /**
     * Gets the package mapping for a reference project.
     * 
     * @param project the reference project
     * @return mapping of original packages to categories, or empty map if not found
     */
    public static Map<String, PackageCategory> getPackageMapping(ReferenceProject project) {
        return PROJECT_MAPPINGS.getOrDefault(project, Map.of());
    }
    
    /**
     * Validates a package name against VeloctopusRising conventions.
     * 
     * @param packageName the package name to validate
     * @return true if the package name follows conventions
     */
    public static boolean isValidPackageName(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        
        if (!packageName.startsWith(BASE_PACKAGE)) {
            return false;
        }
        
        String[] parts = packageName.split("\\.");
        if (parts.length < 8) { // base + project + category
            return false;
        }
        
        String categoryName = parts[7]; // After base package
        return Set.of(PackageCategory.values()).stream()
            .anyMatch(cat -> cat.getPackageName().equals(categoryName));
    }
    
    /**
     * Gets the recommended package structure for a reference project.
     * 
     * @param project the reference project
     * @return recommended package structure
     */
    public static PackageStructureRecommendation getRecommendedStructure(ReferenceProject project) {
        return new PackageStructureRecommendation(project);
    }
    
    /**
     * Represents a recommended package structure for a reference project.
     */
    public static final class PackageStructureRecommendation {
        private final ReferenceProject project;
        
        private PackageStructureRecommendation(ReferenceProject project) {
            this.project = project;
        }
        
        /**
         * @return the reference project
         */
        public ReferenceProject getProject() {
            return project;
        }
        
        /**
         * @return base package for this project
         */
        public String getBasePackage() {
            return BASE_PACKAGE + "." + project.name().toLowerCase();
        }
        
        /**
         * @return recommended packages for this project
         */
        public Set<String> getRecommendedPackages() {
            return Set.of(PackageCategory.values()).stream()
                .map(cat -> getBasePackage() + "." + cat.getPackageName())
                .collect(java.util.stream.Collectors.toSet());
        }
        
        /**
         * @return package mappings for this project
         */
        public Map<String, PackageCategory> getMappings() {
            return getPackageMapping(project);
        }
    }
    
    private BorrowedPackageStructure() {
        throw new UnsupportedOperationException("Utility class");
    }
}
