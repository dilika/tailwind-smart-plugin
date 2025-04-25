package com.github.dilika.tailwindsmartplugin.listeners

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.messages.MessageBusConnection
import com.github.dilika.tailwindsmartplugin.utils.TailwindConfigDetector
import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils
import com.intellij.execution.ExecutionListener
import com.intellij.execution.ExecutionManager
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.task.ProjectTaskListener
import com.intellij.task.ProjectTaskManager

/**
 * Project event listener for Tailwind CSS
 * Resets the class cache when a project is opened or closed
 * Also listens for build and test events to refresh the cache
 */
@Suppress("OVERRIDE_DEPRECATION")
class TailwindProjectListener : ProjectManagerListener {
    private val logger = Logger.getInstance(TailwindProjectListener::class.java)
    private val connections = mutableMapOf<String, MessageBusConnection>()

    override fun projectOpened(project: Project) {
        logger.info("Project opened: ${project.name}")

        // Check if the project uses Tailwind
        try {
            // Preload Tailwind classes for this project
            val classes = TailwindUtils.getTailwindClasses(project)
            logger.info("Tailwind project detected: ${classes.size} classes loaded")

            // Register a file listener for changes
            registerFileListener(project)

            // Register build and test listeners
            registerBuildListener(project)
            registerExecutionListener(project)
        } catch (e: Exception) {
            logger.error("Error detecting Tailwind: ${e.message}")
        }
    }

    override fun projectClosing(project: Project) {
        logger.info("Project closing: ${project.name}")

        // Remove the file listener
        unregisterFileListener(project)

        // Clean the cache for this project
        TailwindUtils.clearProjectCache(project)
    }

    private fun registerFileListener(project: Project) {
        val projectId = project.locationHash

        if (connections.containsKey(projectId)) {
            // Already registered
            return
        }

        val connection = project.messageBus.connect()
        connections[projectId] = connection

        // Listen for file changes
        connection.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                // Check if a Tailwind configuration file has been modified
                val detector = TailwindConfigDetector(project)
                val configFiles = detector.findConfigFiles().map { it.path }

                if (events.any { event -> 
                    val filePath = event.file?.path
                    filePath != null && (
                        configFiles.contains(filePath) || 
                        filePath.endsWith("tailwind.config.js") ||
                        filePath.endsWith("tailwind.config.mjs") || 
                        filePath.endsWith("postcss.config.js") ||
                        filePath.endsWith("postcss.config.mjs")
                    )
                }) {
                    logger.info("Tailwind configuration modified, refreshing cache")

                    // Clear the cache for this project
                    TailwindUtils.clearProjectCache(project)

                    // Reload the classes
                    TailwindUtils.getTailwindClasses(project)
                }
            }
        })
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
            override fun finished(result: com.intellij.task.ProjectTaskManager.Result) {
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
        connection.subscribe(com.intellij.execution.ExecutionManager.EXECUTION_TOPIC, object : ExecutionListener {
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
