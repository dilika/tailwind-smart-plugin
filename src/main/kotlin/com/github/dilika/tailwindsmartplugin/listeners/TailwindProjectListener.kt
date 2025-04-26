package com.github.dilika.tailwindsmartplugin.listeners

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.messages.MessageBusConnection
import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.startup.StartupActivity
import java.util.concurrent.ConcurrentHashMap

/**
 * Project startup activity for Tailwind CSS integration.
 * Monitors project opening/closing and file changes related to Tailwind configuration.
 * Also listens for build and test events to refresh the cache.
 */
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
            
            // Register build and test listeners
            registerBuildListener(project)
            registerExecutionListener(project)
            registerProjectCloseListener(project)
        } catch (e: Exception) {
            logger.warn("Failed to setup Tailwind: ${e.message}")
        }
    }
    
    /**
     * Register a listener to clean up when the project is closed
     */
    private fun registerProjectCloseListener(project: Project) {
        project.messageBus.connect().subscribe(Project.TOPIC, object : Project.ProjectListener {
            override fun projectClosing(project: Project) {
                logger.info("Project closing: ${project.name}")
                
                // Remove the file listener
                unregisterFileListener(project)
                
                // Clean the cache for this project
                TailwindUtils.clearProjectCache(project)
            }
        })
    }
    
    private fun setupFileWatchers(project: Project) {
        val connection = project.messageBus.connect()
        
        // Store connection for cleanup
        connections[project.locationHash] = connection
        
        // Listen for file changes
        connection.subscribe(
            VirtualFileManager.VFS_CHANGES,
            object : BulkFileListener {
                override fun after(events: List<VFileEvent>) {
                    for (event in events) {
                        processFileEvent(event, project)
                    }
                }
            }
        )
    }
    
    private fun processFileEvent(event: VFileEvent, project: Project) {
        val file = event.file ?: return
        
        // Check if the file is a tailwind config file
        if (isTailwindConfigFile(file)) {
            when (event) {
                is VFileContentChangeEvent -> {
                    logger.info("Tailwind config file changed: ${file.path}")
                    project.service<TailwindConfigService>().clearProjectCache()
                }
                is VFileCreateEvent -> {
                    logger.info("Tailwind config file created: ${file.path}")
                    project.service<TailwindConfigService>().clearProjectCache()
                }
                is VFileDeleteEvent -> {
                    logger.info("Tailwind config file deleted: ${file.path}")
                    project.service<TailwindConfigService>().clearProjectCache()
                }
            }
        }
        
        // Also check CSS files since they may contain Tailwind directives
        if (isCssFile(file) && event is VFileContentChangeEvent) {
            logger.info("CSS file changed: ${file.path}")
            project.service<TailwindConfigService>().clearProjectCache()
        }
    }
    
    private fun isTailwindConfigFile(file: VirtualFile): Boolean {
        return tailwindConfigFileNames.contains(file.name)
    }
    
    private fun isCssFile(file: VirtualFile): Boolean {
        return file.extension?.lowercase() in listOf("css", "scss", "sass", "less")
    }
    
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
    
    /**
     * Register a listener for build events
     */
    private fun registerBuildListener(project: Project) {
        val connection = project.messageBus.connect()
        
        // Listen for build task events
        connection.subscribe(ProjectTaskListener.TOPIC, object : ProjectTaskListener {
            override fun finished(result: ProjectTaskManager.Result) {
                logger.info("Build task finished, refreshing Tailwind classes cache")
                
                // Clear the cache for this project
                TailwindUtils.clearProjectCache(project)
                
                // Reload the classes
                TailwindUtils.getTailwindClasses(project)
            }
        })
        
        // Store the connection
        val projectId = project.locationHash
        connections[projectId] = connection
    }
    
    /**
     * Register a listener for execution events (run/debug/test)
     */
    private fun registerExecutionListener(project: Project) {
        val connection = project.messageBus.connect()
        
        // Listen for execution events
        connection.subscribe(ExecutionManager.EXECUTION_TOPIC, object : ExecutionListener {
            override fun processTerminated(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler, exitCode: Int) {
                // Check if this is a test run
                val isTestRun = executorId.contains("test", ignoreCase = true) || 
                                env.runProfile.toString().contains("test", ignoreCase = true)
                
                if (isTestRun) {
                    logger.info("Test execution finished, refreshing Tailwind classes cache")
                    
                    // Clear the cache for this project
                    TailwindUtils.clearProjectCache(project)
                    
                    // Reload the classes
                    TailwindUtils.getTailwindClasses(project)
                }
            }
        })
        
        // Store the connection
        val projectId = project.locationHash
        connections[projectId] = connection
    }
}