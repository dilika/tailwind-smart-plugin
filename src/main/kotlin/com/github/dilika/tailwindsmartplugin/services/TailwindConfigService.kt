package com.github.dilika.tailwindsmartplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import java.io.File

@Service(Service.Level.PROJECT)
class TailwindConfigService(private val project: Project) {

    private var tailwindConfigPath: String? = null

    init {
        thisLogger().info("[Tailwind] Initializing Tailwind service for project: ${project.name}")
        tailwindConfigPath = findTailwindConfig()
        if (tailwindConfigPath != null) {
            thisLogger().info("[Tailwind] Configuration file found: $tailwindConfigPath")
        } else {
            // Check if we're in a test environment
            val isTestEnvironment = project.name.contains("light_temp") || project.name.contains("TailwindConfigService")

            // Only log a warning if we're not in a test environment
            if (!isTestEnvironment) {
                thisLogger().warn("[Tailwind] No Tailwind configuration file found in the project.")
            } else {
                thisLogger().info("[Tailwind] No Tailwind configuration file found in test environment (expected).")
            }
        }
    }

    private fun findTailwindConfig(): String? {
        val projectBase = project.basePath ?: return null
        val configNames = listOf("tailwind.config.js", "tailwind.config.cjs", "tailwind.config.ts")

        // Check if we're in a test environment
        val isTestEnvironment = project.name.contains("light_temp") || project.name.contains("TailwindConfigService")

        // First, look in the project base path
        val configInProjectBase = configNames.map { File(projectBase, it) }.firstOrNull { it.exists() }?.absolutePath
        if (configInProjectBase != null) {
            return configInProjectBase
        }

        // If not found in project base and not in a test environment, look in test resources
        if (!isTestEnvironment) {
            val testResourcesPath = "src/test/resources"
            val configInTestResources = configNames.map { File(testResourcesPath, it) }.firstOrNull { it.exists() }?.absolutePath
            if (configInTestResources != null) {
                thisLogger().info("[Tailwind] Using test configuration file: $configInTestResources")
                return configInTestResources
            }
        }

        return null
    }

    fun getTailwindConfigPath(): String? = tailwindConfigPath
}
