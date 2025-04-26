package com.github.dilika.tailwindsmartplugin.startup

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.github.dilika.tailwindsmartplugin.services.TailwindConfigService

/**
 * Startup activity for initializing Tailwind CSS support when a project is opened.
 */
class TailwindStartupActivity : StartupActivity {
    private val logger = thisLogger()

    override fun runActivity(project: Project) {
        logger.info("Initializing Tailwind CSS support for project: ${project.name}")
        
        // Initialize the TailwindConfigService by requesting it from the service provider
        val configService = project.service<TailwindConfigService>()
        
        // Log the config path if found
        val configPath = configService.getTailwindConfigPath()
        if (configPath != null) {
            logger.info("Found Tailwind config at: $configPath")
        } else {
            logger.info("No Tailwind config found for project: ${project.name}")
        }
    }
}
