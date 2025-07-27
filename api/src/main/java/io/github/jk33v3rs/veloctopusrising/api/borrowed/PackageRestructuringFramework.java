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

package io.github.jk33v3rs.veloctopusrising.api.borrowed;

import io.github.jk33v3rs.veloctopusrising.api.extraction.ReferenceProject;
import io.github.jk33v3rs.veloctopusrising.api.extraction.PatternAttribution;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Package restructuring framework for organizing borrowed implementations.
 * 
 * <p>This framework provides systematic organization of borrowed code from reference projects,
 * ensuring proper attribution, license compliance, and maintainable package structure.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Automatic package mapping by reference project</li>
 *   <li>License compliance checking and attribution generation</li>
 *   <li>Dependency resolution and conflict detection</li>
 *   <li>Class renaming and namespace isolation</li>
 *   <li>Integration point mapping for VeloctopusRising API</li>
 * </ul>
 * 
 * <p>Package structure follows the pattern:
 * <pre>
 * io.github.jk33v3rs.veloctopusrising.api.borrowed.{project}/
 *   ├── core/           - Core functionality
 *   ├── integration/    - VeloctopusRising integration points
 *   ├── util/          - Utility classes
 *   └── attribution/   - License and attribution metadata
 * </pre>
 * 
 * @author VeloctopusRising Team
 * @since 1.0.0
 */
public interface PackageRestructuringFramework {
    
    /**
     * Restructures borrowed implementations into organized packages.
     * 
     * @param project the reference project to restructure
     * @param sourcePackage the original package name from the borrowed code
     * @param targetPackage the target package in VeloctopusRising structure
     * @param attribution the attribution information for license compliance
     * @return future completing with restructuring result
     */
    CompletableFuture<RestructuringResult> restructurePackage(
        ReferenceProject project,
        String sourcePackage,
        String targetPackage,
        PatternAttribution attribution
    );
    
    /**
     * Maps original class names to VeloctopusRising-specific names.
     * 
     * @param project the reference project
     * @param originalClassName the original class name
     * @return the mapped class name for VeloctopusRising
     */
    String mapClassName(ReferenceProject project, String originalClassName);
    
    /**
     * Resolves package dependencies and detects conflicts.
     * 
     * @param project the reference project
     * @param packageName the package to check dependencies for
     * @return future completing with dependency analysis
     */
    CompletableFuture<DependencyAnalysis> analyzeDependencies(
        ReferenceProject project, 
        String packageName
    );
    
    /**
     * Generates integration points between borrowed code and VeloctopusRising API.
     * 
     * @param project the reference project
     * @param borrowedClasses the set of borrowed class names
     * @return future completing with integration mapping
     */
    CompletableFuture<IntegrationMapping> generateIntegrationPoints(
        ReferenceProject project,
        Set<String> borrowedClasses
    );
    
    /**
     * Creates attribution documentation for restructured packages.
     * 
     * @param project the reference project
     * @param packageName the restructured package name
     * @param attribution the attribution information
     * @return future completing with attribution documentation
     */
    CompletableFuture<String> generateAttributionDocument(
        ReferenceProject project,
        String packageName,
        PatternAttribution attribution
    );
    
    /**
     * Validates package structure compliance with VeloctopusRising standards.
     * 
     * @param packageName the package to validate
     * @return future completing with validation results
     */
    CompletableFuture<ValidationResult> validatePackageStructure(String packageName);
    
    /**
     * Retrieves the recommended package mapping for a reference project.
     * 
     * @param project the reference project
     * @return mapping of original packages to VeloctopusRising packages
     */
    Map<String, String> getPackageMapping(ReferenceProject project);
    
    /**
     * Result of package restructuring operation.
     */
    interface RestructuringResult {
        /**
         * @return true if restructuring was successful
         */
        boolean isSuccessful();
        
        /**
         * @return the target package name
         */
        String getTargetPackage();
        
        /**
         * @return mapping of original to renamed classes
         */
        Map<String, String> getClassMappings();
        
        /**
         * @return any errors encountered during restructuring
         */
        Set<String> getErrors();
        
        /**
         * @return the attribution information
         */
        PatternAttribution getAttribution();
        
        /**
         * @return statistics about the restructuring process
         */
        RestructuringStatistics getStatistics();
    }
    
    /**
     * Analysis of package dependencies and conflicts.
     */
    interface DependencyAnalysis {
        /**
         * @return packages that this package depends on
         */
        Set<String> getDependencies();
        
        /**
         * @return packages that depend on this package
         */
        Set<String> getDependents();
        
        /**
         * @return conflicting classes found during analysis
         */
        Set<String> getConflicts();
        
        /**
         * @return suggested resolution for conflicts
         */
        Map<String, String> getConflictResolutions();
        
        /**
         * @return true if analysis detected circular dependencies
         */
        boolean hasCircularDependencies();
    }
    
    /**
     * Mapping between borrowed code and VeloctopusRising integration points.
     */
    interface IntegrationMapping {
        /**
         * @return classes that need VeloctopusRising API integration
         */
        Set<String> getIntegrationClasses();
        
        /**
         * @return mapping of borrowed interfaces to VeloctopusRising interfaces
         */
        Map<String, String> getInterfaceMappings();
        
        /**
         * @return suggested adapter classes for integration
         */
        Set<String> getAdapterClasses();
        
        /**
         * @return configuration classes needed for integration
         */
        Set<String> getConfigurationClasses();
    }
    
    /**
     * Validation result for package structure compliance.
     */
    interface ValidationResult {
        /**
         * @return true if package structure is valid
         */
        boolean isValid();
        
        /**
         * @return validation errors found
         */
        Set<String> getErrors();
        
        /**
         * @return validation warnings
         */
        Set<String> getWarnings();
        
        /**
         * @return suggested improvements
         */
        Set<String> getSuggestions();
        
        /**
         * @return compliance score (0.0 to 1.0)
         */
        double getComplianceScore();
    }
    
    /**
     * Statistics about the restructuring process.
     */
    interface RestructuringStatistics {
        /**
         * @return number of classes restructured
         */
        int getClassesRestructured();
        
        /**
         * @return number of packages created
         */
        int getPackagesCreated();
        
        /**
         * @return time taken for restructuring in milliseconds
         */
        long getRestructuringTimeMs();
        
        /**
         * @return size of borrowed code in bytes
         */
        long getBorrowedCodeSize();
        
        /**
         * @return percentage of code that was successfully adapted
         */
        double getAdaptationSuccessRate();
    }
}
