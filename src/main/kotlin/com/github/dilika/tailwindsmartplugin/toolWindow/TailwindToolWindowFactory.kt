package com.github.dilika.tailwindsmartplugin.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
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

class TailwindToolWindowFactory : ToolWindowFactory {
    private val LOG = logger<TailwindToolWindowFactory>()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowContent = TailwindToolWindowContent(project)
        val content = ContentFactory.getInstance().createContent(
            toolWindowContent.getContent(),
            "Tailwind CSS",
            false
        )
        toolWindow.contentManager.addContent(content)
    }
}

class TailwindToolWindowContent(private val project: Project) {
    private val configService = project.service<TailwindConfigService>()
    
    fun getContent(): JComponent {
        val panel = JBPanel<JBPanel<*>>()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        // Config path information
        val configPathLabel = JBLabel("Tailwind Config Path: ")
        panel.add(configPathLabel)
        
        val configPath = configService.getTailwindConfigPath() ?: "Not found"
        val configPathValue = JBLabel(configPath)
        panel.add(configPathValue)
        
        // Additional Tailwind information can be added here
        
        val scrollPane = JBScrollPane(panel)
        
        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(scrollPane, BorderLayout.CENTER)
        
        return mainPanel
    }
}
            "Tailwind CSS",
            false
        )
        toolWindow.contentManager.addContent(content)
    }
}

class TailwindToolWindowContent(private val project: Project) {
    private val configService = project.service<TailwindConfigService>()
    
    fun getContent(): JComponent {
        val panel = JBPanel<JBPanel<*>>()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        // Config path information
        val configPathLabel = JBLabel("Tailwind Config Path: ")
        panel.add(configPathLabel)
        
        val configPath = configService.getTailwindConfigPath() ?: "Not found"
        val configPathValue = JBLabel(configPath)
        panel.add(configPathValue)
        
        // Additional Tailwind information can be added here
        
        val scrollPane = JBScrollPane(panel)
        
        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(scrollPane, BorderLayout.CENTER)
        
        return mainPanel
    }
}

class TailwindToolWindowContent(private val project: Project) {
    private val configService = project.service<TailwindConfigService>()
    
    fun getContent(): JComponent {
        val panel = JBPanel<JBPanel<*>>()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        // Config path information
        val configPathLabel = JBLabel("Tailwind Config Path: ")
        panel.add(configPathLabel)
        
        val configPath = configService.getTailwindConfigPath() ?: "Not found"
        val configPathValue = JBLabel(configPath)
        panel.add(configPathValue)
        
        // Additional Tailwind information can be added here
        
        val scrollPane = JBScrollPane(panel)
        
        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(scrollPane, BorderLayout.CENTER)
        
        return mainPanel
    }
}

class TailwindToolWindowContent(private val project: Project) {
    private val configService = project.service<TailwindConfigService>()
    
    fun getContent(): JComponent {
        val panel = JBPanel<JBPanel<*>>()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        // Config path information
        val configPathLabel = JBLabel("Tailwind Config Path: ")
        panel.add(configPathLabel)
        
        val configPath = configService.getTailwindConfigPath() ?: "Not found"
        val configPathValue = JBLabel(configPath)
        panel.add(configPathValue)
        
        // Additional Tailwind information can be added here
        
        val scrollPane = JBScrollPane(panel)
        
        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(scrollPane, BorderLayout.CENTER)
        
        return mainPanel
    }
}
        val content = ContentFactory.getInstance().createContent(
            toolWindowContent.getContent(),
            "Tailwind CSS",
            false
        )
        toolWindow.contentManager.addContent(content)
    }
}

class TailwindToolWindowContent(private val project: Project) {
    private val configService = project.service<TailwindConfigService>()
    
    fun getContent(): JComponent {
        val panel = JBPanel<JBPanel<*>>()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        // Config path information
        val configPathLabel = JBLabel("Tailwind Config Path: ")
        panel.add(configPathLabel)
        
        val configPath = configService.getTailwindConfigPath() ?: "Not found"
        val configPathValue = JBLabel(configPath)
        panel.add(configPathValue)
        
        // Additional Tailwind information can be added here
        
        val scrollPane = JBScrollPane(panel)
        
        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(scrollPane, BorderLayout.CENTER)
        
        return mainPanel
    }
}
            toolWindowContent.getContent(),
            "Tailwind CSS",
            false
        )
        toolWindow.contentManager.addContent(content)
    }
}

class TailwindToolWindowContent(private val project: Project) {
    private val configService = project.service<TailwindConfigService>()
    
    fun getContent(): JComponent {
        val panel = JBPanel<JBPanel<*>>()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        // Config path information
        val configPathLabel = JBLabel("Tailwind Config Path: ")
        panel.add(configPathLabel)
        
        val configPath = configService.getTailwindConfigPath() ?: "Not found"
        val configPathValue = JBLabel(configPath)
        panel.add(configPathValue)
        
        // Additional Tailwind information can be added here
        
        val scrollPane = JBScrollPane(panel)
        
        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(scrollPane, BorderLayout.CENTER)
        
        return mainPanel
    }
}
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import javax.swing.JLabel
import javax.swing.JPanel
import com.github.dilika.tailwindsmartplugin.MyBundle
import com.github.dilika.tailwindsmartplugin.services.TailwindConfigService
import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils

class TailwindToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().info("[Tailwind] Initializing Tailwind ToolWindow")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel()
        panel.add(JLabel(MyBundle.message("toolwindow.str")))
        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
        val service = project.getService(TailwindConfigService::class.java)
        thisLogger().info("[Tailwind] ToolWindow initialized with TailwindConfigService: $service")
    }

    override fun shouldBeAvailable(project: Project) = true

    class TailwindToolWindow(private val toolWindow: ToolWindow) {
        private val service = toolWindow.project.getService(TailwindConfigService::class.java)
        private val configPath = service.getTailwindConfigPath()

        fun getContent() = JPanel().apply {
            layout = javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS)
            val label = JLabel(
                if (configPath != null)
                    "Tailwind config detected: $configPath"
                else
                    "No Tailwind config found in the project."
            )
            add(label)
            val classesArea = javax.swing.JTextArea(10, 40)
            classesArea.isEditable = false
            val scrollPane = javax.swing.JScrollPane(classesArea)
            add(scrollPane)
            val refreshButton = javax.swing.JButton("Refresh detection and classes")
            refreshButton.addActionListener {
                val refreshedPath = service.getTailwindConfigPath()
                label.text = if (refreshedPath != null)
                    "Tailwind config detected: $refreshedPath"
                else
                    "No Tailwind config found in the project."
                val classes = TailwindUtils.getTailwindClasses(toolWindow.project)
                classesArea.text = classes.joinToString("\n")
            }
            add(refreshButton)
            // Initial display of Tailwind classes
            val initialClasses = TailwindUtils.getTailwindClasses(toolWindow.project)
            classesArea.text = initialClasses.joinToString("\n")
        }
    }
}