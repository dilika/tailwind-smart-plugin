package com.github.dilika.tailwindsmartplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Service for managing Tailwind CSS configuration in a project
 * Responsible for loading, parsing, and providing access to Tailwind configuration
 */
@Service(Service.Level.PROJECT)
class TailwindConfigService(private val project: Project) {
    private val logger = Logger.getInstance(TailwindConfigService::class.java)
    private var configFilePath: String? = null
    private var configData: Map<String, Any> = emptyMap()
    private var tailwindVersion: String = "v3.0" // Default version if not detected
    
    // Cache for generated classes and class data to improve performance
    private val classesCache = ConcurrentHashMap<String, List<String>>()
    private val classDataCache = ConcurrentHashMap<String, Map<String, JSONObject>>()
    
    companion object {
        @Suppress("unused") // Used via reflection
        fun getInstance(project: Project): TailwindConfigService {
            return project.getService(TailwindConfigService::class.java)
        }
        
        // Known default Tailwind configuration versions
        private val TAILWIND_VERSIONS = mapOf(
            "v2" to "2.0",
            "v3" to "3.0",
            "v4" to "4.0"
        )
    }
    
    init {
        logger.info("Initializing TailwindConfigService for project: ${project.name}")
        // Detect and parse config file on initialization
        detectAndParseConfig()
    }
    
    /**
     * Get the path to the Tailwind CSS configuration file, if one exists
     */
    fun getTailwindConfigPath(): String? {
        if (configFilePath == null) {
            detectAndParseConfig()
        }
        return configFilePath
    }
    
    /**
     * Get the Tailwind version detected in the project
     */
    fun getTailwindVersion(): String {
        return tailwindVersion
    }
    
    /**
     * Get the parsed Tailwind configuration data
     */
    fun getConfigData(): Map<String, Any> {
        if (configData.isEmpty() && configFilePath != null) {
            parseConfig()
        }
        return configData
    }
    
    /**
     * Detect a Tailwind CSS configuration file in the project and parse it
     */
    private fun detectAndParseConfig() {
        logger.info("Detecting Tailwind config for project: ${project.name}")
        
        try {
            val basePath = project.basePath
            if (basePath != null) {
                val possibleConfigFiles = listOf(
                    "tailwind.config.js",
                    "tailwind.config.cjs",
                    "tailwind.config.mjs",
                    "tailwind.config.ts"
                )
                
                for (configFileName in possibleConfigFiles) {
                    val configFile = File(basePath, configFileName)
                    if (configFile.exists()) {
                        configFilePath = configFile.absolutePath
                        logger.info("Found Tailwind config at: $configFilePath")
                        parseConfig()
                        return
                    }
                }
                
                // Also check for package.json to detect Tailwind version
                val packageJsonFile = File(basePath, "package.json")
                if (packageJsonFile.exists()) {
                    detectTailwindVersionFromPackageJson(packageJsonFile)
                }
                
                logger.info("No Tailwind config found in project root, will use default settings for version: $tailwindVersion")
            }
        } catch (e: Exception) {
            logger.error("Error detecting Tailwind config: ${e.message}")
        }
    }
    
    /**
     * Parse the detected Tailwind configuration file
     */
    private fun parseConfig() {
        val configPath = configFilePath ?: return
        
        try {
            val parser = TailwindConfigParser()
            configData = parser.parseConfig(configPath)
            
            // Try to extract version information
            if (configData.containsKey("tailwindVersion")) {
                tailwindVersion = configData["tailwindVersion"] as String
            } else {
                // Try to infer version from config features
                inferTailwindVersionFromConfig()
            }
            
            // Clear caches as config has been reloaded
            clearCaches()
            
            logger.info("Successfully parsed Tailwind config with detected version: $tailwindVersion")
        } catch (e: Exception) {
            logger.error("Error parsing Tailwind config: ${e.message}")
        }
    }
    
    /**
     * Try to detect Tailwind version from package.json dependencies
     */
    private fun detectTailwindVersionFromPackageJson(packageJsonFile: File) {
        try {
            val content = packageJsonFile.readText()
            val packageJson = org.json.JSONObject(content)
            
            // Check dependencies and devDependencies for tailwindcss
            val dependencies = if (packageJson.has("dependencies")) 
                               packageJson.getJSONObject("dependencies") else null
            val devDependencies = if (packageJson.has("devDependencies")) 
                                 packageJson.getJSONObject("devDependencies") else null
            
            // Extract Tailwind version
            if (dependencies?.has("tailwindcss") == true) {
                extractVersionFromDependency(dependencies.getString("tailwindcss"))
            } else if (devDependencies?.has("tailwindcss") == true) {
                extractVersionFromDependency(devDependencies.getString("tailwindcss"))
            }
        } catch (e: Exception) {
            logger.debug("Error parsing package.json: ${e.message}")
        }
    }
    
    /**
     * Extract version number from dependency string
     */
    private fun extractVersionFromDependency(versionStr: String) {
        // Handle version formats like "^3.0.0", "~3.0", "3.x", etc.
        val versionRegex = "[~^]?([0-9]+)(.[0-9]+)?(.[0-9]+)?".toRegex()
        val match = versionRegex.find(versionStr)
        
        if (match != null) {
            val majorVersion = match.groupValues[1]
            tailwindVersion = "v$majorVersion.0"
        }
    }
    
    /**
     * Try to infer Tailwind version from config features
     */
    private fun inferTailwindVersionFromConfig() {
        // Default to v3 if we can't determine version
        tailwindVersion = "v3.0"
        
        try {
            // Check for v4-specific features
            if (configData.containsKey("experimental")) {
                val experimental = configData["experimental"] as? Map<*, *>
                if (experimental?.containsKey("extractedColors") == true || 
                    experimental?.containsKey("dynamicColors") == true) {
                    tailwindVersion = "v4.0"
                    return
                }
            }
            
            // Check for v3-specific features (JIT mode is built-in)
            if (configData.containsKey("mode") && configData["mode"] == "jit") {
                tailwindVersion = "v3.0"
                return
            }
            
            // More checks could be added here
        } catch (e: Exception) {
            logger.debug("Error inferring Tailwind version: ${e.message}")
        }
    }
    
    /**
     * Get all Tailwind classes for a specific project
     * This uses the parsed config to generate classes dynamically
     */
    fun getTailwindClasses(project: Project): List<String> {
        val projectPath = project.basePath ?: return emptyList()
        
        return classesCache.computeIfAbsent(projectPath) {
            generateTailwindClasses()
        }
    }
    
    /**
     * Get detailed Tailwind class data for a specific project
     */
    fun getTailwindClassData(project: Project): Map<String, JSONObject> {
        val projectPath = project.basePath ?: return emptyMap()
        
        return classDataCache.computeIfAbsent(projectPath) {
            generateTailwindClassData()
        }
    }
    
    /**
     * Clear all caches for this project
     */
    fun clearCaches() {
        classesCache.clear()
        classDataCache.clear()
        logger.info("Cleared all caches for project: ${project.name}")
    }
    
    /**
     * Generate a list of Tailwind classes based on the parsed configuration
     */
    private fun generateTailwindClasses(): List<String> {
        val classGenerator = TailwindClassGenerator(tailwindVersion, configData)
        return classGenerator.generateClasses()
    }
    
    /**
     * Generate detailed data for Tailwind classes based on the parsed configuration
     */
    private fun generateTailwindClassData(): Map<String, JSONObject> {
        return emptyMap() // Will be implemented in TailwindClassGenerator
    }
}
