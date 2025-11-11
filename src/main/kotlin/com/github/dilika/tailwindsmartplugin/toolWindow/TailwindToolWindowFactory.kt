package com.github.dilika.tailwindsmartplugin.toolWindow

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.github.dilika.tailwindsmartplugin.services.TailwindConfigService
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JTextArea

/**
 * Factory for the Tailwind CSS tool window
 */
@Suppress("unused") // Registered via plugin.xml
class TailwindToolWindowFactory : ToolWindowFactory {
    private val logger = Logger.getInstance(TailwindToolWindowFactory::class.java)

    init {
        logger.info("[Tailwind] Initializing Tailwind ToolWindow")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowContent = TailwindToolWindowContent(project)
        val content = ContentFactory.getInstance().createContent(
            toolWindowContent.getContent(),
            "Tailwind CSS",
            false
        )
        toolWindow.contentManager.addContent(content)
        logger.info("[Tailwind] ToolWindow content added")
    }
    
    override fun shouldBeAvailable(project: Project) = true
}

/**
 * Content for the Tailwind CSS tool window
 */
class TailwindToolWindowContent(private val project: Project) {
    private val configService = project.getService(TailwindConfigService::class.java)
    
    fun getContent(): JComponent {
        val panel = JBPanel<JBPanel<*>>()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        // Config path information
        val configPathLabel = JBLabel("Tailwind config path: ")
        panel.add(configPathLabel)
        
        val configPath = configService.getTailwindConfigPath() ?: "Not found"
        val configPathValue = JBLabel(configPath)
        panel.add(configPathValue)
        
        // Additional Tailwind information can be added here
        val classesArea = JTextArea(10, 40)
        classesArea.isEditable = false
        classesArea.text = "Tailwind CSS classes will appear here"
        panel.add(JBScrollPane(classesArea))
        
        // Add refresh button
        val refreshButton = JButton("Refresh Config")
        refreshButton.addActionListener {
            val refreshedPath = configService.getTailwindConfigPath() ?: "Not found"
            configPathValue.text = refreshedPath
            
            // Simulate fetching Tailwind classes
            val classes = listOf(
                "bg-blue-500",
                "text-white", 
                "p-4", 
                "rounded", 
                "hover:bg-blue-700"
            )
            classesArea.text = classes.joinToString("\n")
        }
        panel.add(refreshButton)
        
        val scrollPane = JBScrollPane(panel)
        
        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(scrollPane, BorderLayout.CENTER)
        
        return mainPanel
    }
}