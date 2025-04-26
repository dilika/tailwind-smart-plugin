package com.github.dilika.tailwindsmartplugin.listeners

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.messages.MessageBusConnection
import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils
import com.github.dilika.tailwindsmartplugin.services.TailwindConfigService
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.startup.StartupActivity
import java.util.concurrent.ConcurrentHashMap

/**
 * Project startup activity for Tailwind CSS integration.
 * Monitors project opening/closing and file changes related to Tailwind configuration.
 */
@Suppress("unused") // Used via plugin.xml
class TailwindProjectListener : StartupActivity, DumbAware {
    private val logger = Logger.getInstance(TailwindProjectListener::class.java)
    private val connections = ConcurrentHashMap<String, MessageBusConnection>()
    private val tailwindConfigFileNames = listOf(
        "tailwind.config.js",
        "tailwind.config.cjs",
        "tailwind.config.mjs",
        "tailwind.config.ts"
    )
    
    override fun runActivity(project: Project) {
        logger.info("Project opened: ${project.name}")
        
        // Check if the project uses Tailwind
        try {
            // Preload Tailwind classes for this project
            val classes = TailwindUtils.getTailwindClasses(project)
            logger.info("Tailwind project detected: ${classes.size} classes loaded")
            
            // Register a file listener for changes
            setupFileWatchers(project)
            
        } catch (e: Exception) {
            logger.warn("Failed to setup Tailwind: ${e.message}")
        }
    }
    
    private fun setupFileWatchers(project: Project) {
        val connection = project.messageBus.connect()
        
        // Store connection for cleanup
        connections[project.locationHash] = connection
        
        // Listen for file changes
        connection.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                // Process events
                for (event in events) {
                    processFileEvent(project, event)
                }
            }
        })
        
        logger.info("File watchers setup for project: ${project.name}")
    }
    
    private fun processFileEvent(project: Project, event: VFileEvent) {
        val file = event.file ?: return
        
        // Check if it's a Tailwind config file
        if (isTailwindConfigFile(file) && event is VFileContentChangeEvent) {
            logger.info("Tailwind config file changed: ${file.path}")
            TailwindUtils.clearProjectCache(project)
            return
        }
        
        // Also check CSS files since they may contain Tailwind directives
        if (isCssFile(file) && event is VFileContentChangeEvent) {
            logger.info("CSS file changed, may contain Tailwind directives: ${file.path}")
            TailwindUtils.clearProjectCache(project)
        }
    }
    
    private fun isTailwindConfigFile(file: VirtualFile): Boolean {
        return file.name in tailwindConfigFileNames
    }
    
    private fun isCssFile(file: VirtualFile): Boolean {
        return file.extension?.lowercase() in listOf("css", "scss", "sass", "less")
    }
    
    @Suppress("unused")
    private fun registerFileListener(project: Project) {
        setupFileWatchers(project)
    }
    
    private fun unregisterFileListener(project: Project) {
        val projectId = project.locationHash
        val connection = connections[projectId]
        
        if (connection != null) {
            connection.disconnect()
            connections.remove(projectId)
        }
    }
}
