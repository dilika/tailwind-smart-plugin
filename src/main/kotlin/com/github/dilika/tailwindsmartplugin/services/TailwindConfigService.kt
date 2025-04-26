package com.github.dilika.tailwindsmartplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VfsUtil
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

@Service(Service.Level.PROJECT)
class TailwindConfigService(private val project: Project) {
    private val LOG = logger<TailwindConfigService>()
    private var tailwindConfigPath: String? = null
    private var projectCacheMap: MutableMap<String, Any> = mutableMapOf()

    init {
        findTailwindConfig()
    }

    /**
     * Returns the path to the tailwind.config.js file
     */
    fun getTailwindConfigPath(): String? = tailwindConfigPath

    /**
     * Finds the tailwind.config.js file in the project
     */
    private fun findTailwindConfig() {
        val basePath = project.basePath ?: return
        val baseDir = File(basePath)
        
        // Common config file names
        val configFileNames = listOf(
            "tailwind.config.js",
            "tailwind.config.cjs",
            "tailwind.config.mjs",
            "tailwind.config.ts"
        )
        
        for (configFileName in configFileNames) {
            val configFile = File(baseDir, configFileName)
            if (configFile.exists()) {
                tailwindConfigPath = configFile.absolutePath
                LOG.info("Found Tailwind config at: $tailwindConfigPath")
                return
            }
        }
        
        // If not found in root, try searching in common directories
        val commonDirs = listOf("config", "src", "app")
        for (dir in commonDirs) {
            val subDir = File(baseDir, dir)
            if (subDir.exists() && subDir.isDirectory) {
                for (configFileName in configFileNames) {
                    val configFile = File(subDir, configFileName)
                    if (configFile.exists()) {
                        tailwindConfigPath = configFile.absolutePath
                        LOG.info("Found Tailwind config at: $tailwindConfigPath")
                        return
                    }
                }
            }
        }
        
        LOG.info("No Tailwind config found in project ${project.name}")
    }

    /**
     * Clears the cache for the project
     */
    fun clearProjectCache() {
        LOG.info("Clearing project cache for ${project.name}")
        projectCacheMap.clear()
    }

    /**
     * Updates the Tailwind config path
     */
    fun updateTailwindConfigPath(path: String?) {
        tailwindConfigPath = path
        clearProjectCache()
    }
}