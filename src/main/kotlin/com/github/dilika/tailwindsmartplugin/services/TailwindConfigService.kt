package com.github.dilika.tailwindsmartplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.json.JSONObject
import java.io.File

/**
 * Service for managing Tailwind CSS configuration in a project
 */
@Service(Service.Level.PROJECT)
class TailwindConfigService(private val project: Project) {
    private val logger = Logger.getInstance(TailwindConfigService::class.java)
    private var configFilePath: String? = null
    private var projectCacheMap: MutableMap<String, Any> = mutableMapOf()
    
    companion object {
        @Suppress("unused") // Used via reflection
        fun getInstance(project: Project): TailwindConfigService {
            return project.getService(TailwindConfigService::class.java)
        }
    }
    
    init {
        logger.info("Initializing TailwindConfigService for project: ${project.name}")
        // Detect config file on initialization
        detectTailwindConfig()
    }
    
    /**
     * Get the path to the Tailwind CSS configuration file, if one exists
     */
    fun getTailwindConfigPath(): String? {
        if (configFilePath == null) {
            detectTailwindConfig()
        }
        return configFilePath
    }
    
    /**
     * Detect a Tailwind CSS configuration file in the project
     */
    private fun detectTailwindConfig() {
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
                        return
                    }
                }
                
                logger.info("No Tailwind config found in project root, will use default settings")
            }
        } catch (e: Exception) {
            logger.error("Error detecting Tailwind config: ${e.message}")
        }
    }
    
    /**
     * Get Tailwind classes for a specific project path
     */
    @Suppress("unused") // May be used in future implementations
    fun getTailwindClasses(projectPath: String): List<String> {
        // In a real implementation, this would parse the Tailwind config
        // and generate classes based on it. For now, return an empty list
        // as the actual classes will be provided by TailwindUtils.
        return emptyList()
    }
    
    /**
     * Get detailed Tailwind class data for a specific project path
     */
    @Suppress("unused") // May be used in future implementations
    fun getTailwindClassData(projectPath: String): Map<String, JSONObject> {
        // In a real implementation, this would parse the Tailwind config
        // and generate detailed class data. For now, return an empty map
        // as the actual data will be provided by TailwindUtils.
        return emptyMap()
    }
    
    /**
     * Clear the cache for this project
     */
    fun clearProjectCache() {
        projectCacheMap.clear()
        logger.info("Cleared project cache for: ${project.name}")
    }
}
