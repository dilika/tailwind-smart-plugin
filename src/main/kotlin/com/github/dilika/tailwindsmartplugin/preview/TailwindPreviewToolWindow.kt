package com.github.dilika.tailwindsmartplugin.preview

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManager
import com.intellij.util.Alarm
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.text.html.HTMLEditorKit
import javax.swing.text.html.StyleSheet

/**
 * Tool window factory pour le preview visuel Tailwind
 */
class TailwindPreviewToolWindowFactory : ToolWindowFactory {
    
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val previewContent = TailwindPreviewContent(project, toolWindow)
        val content = ContentFactory.getInstance().createContent(
            previewContent.getContent(),
            "Preview",
            false
        )
        toolWindow.contentManager.addContent(content)
    }
    
    override fun shouldBeAvailable(project: Project): Boolean = true
}

/**
 * Contenu de la tool window avec preview en temps réel
 * Version améliorée avec rendu HTML réel
 */
class TailwindPreviewContent(
    private val project: Project,
    private val toolWindow: ToolWindow
) {
    private val logger = Logger.getInstance(TailwindPreviewContent::class.java)
    private val previewRenderer = TailwindPreviewRenderer()
    private val visualPreviewService = TailwindVisualPreviewService()
    private var previewPanel: JPanel? = null
    private var previewImageLabel: JLabel? = null
    private var classesLabel: JBLabel? = null
    private var descriptionLabel: JBLabel? = null
    private var currentEditor: Editor? = null
    private var caretListener: CaretListener? = null
    private var darkModeEnabled: Boolean = false
    private var currentBreakpoint: String = ""
    private val updateAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, project)
    
    fun getContent(): JComponent {
        val mainPanel = JBPanel<JBPanel<*>>(BorderLayout())
        mainPanel.border = EmptyBorder(10, 10, 10, 10)
        
        // Panel de contrôle
        val controlPanel = JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.LEFT))
        val autoUpdateCheckbox = JCheckBox("Auto-update on cursor move", true)
        autoUpdateCheckbox.addActionListener {
            if (autoUpdateCheckbox.isSelected) {
                startListeningToEditor()
            } else {
                stopListeningToEditor()
            }
        }
        controlPanel.add(autoUpdateCheckbox)
        
        val refreshButton = JButton("Refresh")
        refreshButton.addActionListener {
            updatePreview()
        }
        controlPanel.add(refreshButton)
        
        // Toggle dark mode
        val darkModeCheckbox = JCheckBox("Dark mode", false)
        darkModeCheckbox.addActionListener {
            darkModeEnabled = darkModeCheckbox.isSelected
            updatePreview()
        }
        controlPanel.add(darkModeCheckbox)
        
        // Responsive breakpoint selector
        val breakpointLabel = JBLabel("Breakpoint:")
        controlPanel.add(breakpointLabel)
        val breakpointCombo = JComboBox(arrayOf("", "sm", "md", "lg", "xl", "2xl"))
        breakpointCombo.addActionListener {
            currentBreakpoint = breakpointCombo.selectedItem?.toString() ?: ""
            updatePreview()
        }
        controlPanel.add(breakpointCombo)
        
        mainPanel.add(controlPanel, BorderLayout.NORTH)
        
        // Panel de preview avec rendu image
        previewPanel = JBPanel<JBPanel<*>>(BorderLayout())
        previewPanel!!.border = EmptyBorder(10, 10, 10, 10)
        previewPanel!!.preferredSize = Dimension(400, 500)
        previewPanel!!.background = if (darkModeEnabled) Color(0x1f2937) else Color.WHITE
        
        // Label pour l'image de preview
        previewImageLabel = JLabel()
        previewImageLabel!!.horizontalAlignment = SwingConstants.CENTER
        previewImageLabel!!.verticalAlignment = SwingConstants.CENTER
        previewImageLabel!!.text = "Place your cursor on a class attribute to see a live preview"
        previewImageLabel!!.foreground = if (darkModeEnabled) Color(0x9ca3af) else Color(0x6b7280)
        previewImageLabel!!.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(if (darkModeEnabled) Color(0x374151) else Color(0xe5e7eb), 2, true),
            EmptyBorder(20, 20, 20, 20)
        )
        
        val scrollPane = JScrollPane(previewImageLabel)
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        scrollPane.border = EmptyBorder(0, 0, 0, 0)
        
        // Label pour les classes
        classesLabel = JBLabel("<html><strong>Classes:</strong> <span style='color: #6b7280;'>None</span></html>")
        classesLabel!!.border = EmptyBorder(10, 5, 5, 5)
        
        // Label pour la description
        descriptionLabel = JBLabel("<html><strong>Description:</strong> <span style='color: #6b7280;'>Hover over Tailwind classes</span></html>")
        descriptionLabel!!.border = EmptyBorder(5, 5, 10, 5)
        
        val infoPanel = JBPanel<JBPanel<*>>()
        infoPanel.layout = BoxLayout(infoPanel, BoxLayout.Y_AXIS)
        infoPanel.border = EmptyBorder(10, 0, 0, 0)
        infoPanel.add(classesLabel)
        infoPanel.add(descriptionLabel)
        
        previewPanel!!.add(scrollPane, BorderLayout.CENTER)
        previewPanel!!.add(infoPanel, BorderLayout.SOUTH)
        
        mainPanel.add(previewPanel, BorderLayout.CENTER)
        
        // Démarrer l'écoute si auto-update est activé
        if (autoUpdateCheckbox.isSelected) {
            startListeningToEditor()
        }
        
        return mainPanel
    }
    
    private fun startListeningToEditor() {
        stopListeningToEditor() // Arrêter l'ancien listener si présent
        
        val editorManager = FileEditorManager.getInstance(project)
        currentEditor = editorManager.selectedTextEditor
        
        if (currentEditor != null) {
            caretListener = object : CaretListener {
                override fun caretPositionChanged(event: CaretEvent) {
                    // Debounce pour éviter trop de mises à jour
                    updateAlarm.cancelAllRequests()
                    updateAlarm.addRequest({
                        updatePreview()
                    }, 200) // 200ms de debounce
                }
            }
            currentEditor!!.caretModel.addCaretListener(caretListener!!)
            updatePreview()
        }
    }
    
    private fun stopListeningToEditor() {
        caretListener?.let {
            currentEditor?.caretModel?.removeCaretListener(it)
        }
        caretListener = null
        currentEditor = null
    }
    
    private fun updatePreview() {
        try {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            if (editor == null || editor.isDisposed) {
                showNoEditorMessage()
                return
            }
            
            val document = editor.document
            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
            if (psiFile == null) {
                showNoEditorMessage()
                return
            }
            
            // S'assurer que le fichier PSI est à jour
            PsiDocumentManager.getInstance(project).commitDocument(document)
            
            val offset = editor.caretModel.offset
            if (offset < 0 || offset > document.textLength) {
                showNoEditorMessage()
                return
            }
            
            val element = psiFile.findElementAt(offset)
            if (element == null) {
                showNoEditorMessage()
                return
            }
            
            // Trouver l'attribut class ou className le plus proche
            val attributeValue = PsiTreeUtil.getParentOfType(element, XmlAttributeValue::class.java)
            if (attributeValue == null) {
                showNoEditorMessage()
                return
            }
            
            val attribute = PsiTreeUtil.getParentOfType(attributeValue, XmlAttribute::class.java)
            if (attribute == null) {
                showNoEditorMessage()
                return
            }
            
            val attributeName = attribute.name.lowercase()
            if (attributeName != "class" && attributeName != "classname") {
                showNoEditorMessage()
                return
            }
            
            val classString = attributeValue.value
            if (classString.isNullOrBlank()) {
                showNoEditorMessage()
                return
            }
            
            // Extraire les classes
            val classes = classString.split(Regex("\\s+")).filter { it.isNotBlank() }
            
            if (classes.isEmpty()) {
                showNoEditorMessage()
                return
            }
            
            // Générer le preview avec le renderer d'image
            val previewIcon = previewRenderer.renderPreview(
                classes = classes,
                width = 350,
                height = 250,
                darkMode = darkModeEnabled
            )
            
            // Générer aussi la description avec le service visuel
            val previewData = try {
                visualPreviewService.generatePreview(classes, width = 300, height = 200)
            } catch (e: Exception) {
                logger.warn("Error generating visual preview: ${e.message}", e)
                com.github.dilika.tailwindsmartplugin.preview.PreviewData.empty()
            }
            
            // Mettre à jour l'UI
            SwingUtilities.invokeLater {
                try {
                    previewImageLabel?.icon = previewIcon
                    previewImageLabel?.text = ""
                    classesLabel?.text = "<html><strong>Classes:</strong> <span style='color: #3b82f6; font-family: monospace;'>${classes.joinToString(" ")}</span></html>"
                    descriptionLabel?.text = "<html><strong>Description:</strong> <span style='color: #6b7280;'>${previewData.description}</span></html>"
                    previewPanel?.revalidate()
                    previewPanel?.repaint()
                } catch (e: Exception) {
                    logger.warn("Error updating UI: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            logger.warn("Error updating preview: ${e.message}", e)
            SwingUtilities.invokeLater {
                try {
                    previewImageLabel?.icon = null
                    previewImageLabel?.text = "Error: ${e.message ?: "Unknown error"}"
                    previewImageLabel?.foreground = Color(0xef4444)
                } catch (e2: Exception) {
                    logger.error("Error displaying error message: ${e2.message}", e2)
                }
            }
        }
    }
    
    private fun showNoEditorMessage() {
        SwingUtilities.invokeLater {
            try {
                previewImageLabel?.icon = null
                previewImageLabel?.text = "Place your cursor on a class or className attribute"
                previewImageLabel?.foreground = if (darkModeEnabled) Color(0x9ca3af) else Color(0x6b7280)
                classesLabel?.text = "<html><strong>Classes:</strong> <span style='color: #6b7280;'>None</span></html>"
                descriptionLabel?.text = "<html><strong>Description:</strong> <span style='color: #6b7280;'>Hover over Tailwind classes</span></html>"
            } catch (e: Exception) {
                logger.warn("Error showing no editor message: ${e.message}", e)
            }
        }
    }
}

