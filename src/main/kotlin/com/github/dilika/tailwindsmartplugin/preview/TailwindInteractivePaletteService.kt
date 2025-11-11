package com.github.dilika.tailwindsmartplugin.preview

import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import java.awt.Color
import java.awt.GridLayout
import javax.swing.*

/**
 * Service de palette interactive pour Tailwind CSS
 */
class TailwindInteractivePaletteService {
    
    private val logger = Logger.getInstance(TailwindInteractivePaletteService::class.java)
    
    /**
     * Affiche le sélecteur de couleurs
     */
    fun showColorPicker(project: Project, onColorSelected: (String) -> Unit) {
        try {
            val dialog = ColorPickerDialog(project, onColorSelected)
            dialog.show()
        } catch (e: Exception) {
            logger.warn("Error showing color picker", e)
        }
    }
    
    /**
     * Affiche l'outil d'espacement
     */
    fun showSpacingTool(project: Project, onSpacingSelected: (String) -> Unit) {
        try {
            val dialog = SpacingToolDialog(project, onSpacingSelected)
            dialog.show()
        } catch (e: Exception) {
            logger.warn("Error showing spacing tool", e)
        }
    }
    
    /**
     * Affiche l'outil de typographie
     */
    fun showTypographyTool(project: Project, onTypographySelected: (String) -> Unit) {
        try {
            val dialog = TypographyToolDialog(project, onTypographySelected)
            dialog.show()
        } catch (e: Exception) {
            logger.warn("Error showing typography tool", e)
        }
    }
    
    /**
     * Affiche le créateur de gradients
     */
    fun showGradientCreator(project: Project, onGradientSelected: (String) -> Unit) {
        try {
            val dialog = GradientCreatorDialog(project, onGradientSelected)
            dialog.show()
        } catch (e: Exception) {
            logger.warn("Error showing gradient creator", e)
        }
    }
}

/**
 * Dialog pour le sélecteur de couleurs
 */
class ColorPickerDialog(
    project: Project,
    private val onColorSelected: (String) -> Unit
) : DialogWrapper(project) {
    
    private val colorButtons = mutableListOf<JButton>()
    
    init {
        title = "Tailwind Color Picker"
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridLayout(0, 8, 5, 5))
        
        val colors = listOf(
            "red" to Color(239, 68, 68),
            "blue" to Color(59, 130, 246),
            "green" to Color(34, 197, 94),
            "yellow" to Color(234, 179, 8),
            "purple" to Color(147, 51, 234),
            "pink" to Color(236, 72, 153),
            "indigo" to Color(99, 102, 241),
            "gray" to Color(107, 114, 128),
            "orange" to Color(249, 115, 22),
            "teal" to Color(20, 184, 166),
            "cyan" to Color(6, 182, 212),
            "lime" to Color(132, 204, 22),
            "emerald" to Color(16, 185, 129),
            "sky" to Color(14, 165, 233),
            "violet" to Color(139, 92, 246),
            "fuchsia" to Color(217, 70, 239)
        )
        
        colors.forEach { (name, color) ->
            val button = JButton().apply {
                background = color
                preferredSize = java.awt.Dimension(40, 40)
                toolTipText = name
                addActionListener {
                    onColorSelected("bg-$name-500")
                    close(OK_EXIT_CODE)
                }
            }
            colorButtons.add(button)
            panel.add(button)
        }
        
        return panel
    }
}

/**
 * Dialog pour l'outil d'espacement
 */
class SpacingToolDialog(
    project: Project,
    private val onSpacingSelected: (String) -> Unit
) : DialogWrapper(project) {
    
    init {
        title = "Tailwind Spacing Tool"
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        // Padding
        val paddingPanel = JPanel()
        paddingPanel.border = BorderFactory.createTitledBorder("Padding")
        val paddingButtons = createSpacingButtons("p") { onSpacingSelected(it) }
        paddingButtons.forEach { paddingPanel.add(it) }
        
        // Margin
        val marginPanel = JPanel()
        marginPanel.border = BorderFactory.createTitledBorder("Margin")
        val marginButtons = createSpacingButtons("m") { onSpacingSelected(it) }
        marginButtons.forEach { marginPanel.add(it) }
        
        panel.add(paddingPanel)
        panel.add(marginPanel)
        
        return panel
    }
    
    private fun createSpacingButtons(prefix: String, onSelected: (String) -> Unit): List<JButton> {
        val spacings = listOf("0", "1", "2", "3", "4", "5", "6", "8", "10", "12", "16", "20", "24")
        return spacings.map { spacing ->
            JButton("$prefix-$spacing").apply {
                addActionListener {
                    onSelected("$prefix-$spacing")
                    close(OK_EXIT_CODE)
                }
            }
        }
    }
}

/**
 * Dialog pour l'outil de typographie
 */
class TypographyToolDialog(
    project: Project,
    private val onTypographySelected: (String) -> Unit
) : DialogWrapper(project) {
    
    init {
        title = "Tailwind Typography Tool"
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        // Font sizes
        val sizePanel = JPanel()
        sizePanel.border = BorderFactory.createTitledBorder("Font Size")
        val sizes = listOf("text-xs", "text-sm", "text-base", "text-lg", "text-xl", "text-2xl", "text-3xl")
        sizes.forEach { size ->
            val button = JButton(size)
            button.addActionListener {
                onTypographySelected(size)
                close(OK_EXIT_CODE)
            }
            sizePanel.add(button)
        }
        
        // Font weights
        val weightPanel = JPanel()
        weightPanel.border = BorderFactory.createTitledBorder("Font Weight")
        val weights = listOf("font-thin", "font-light", "font-normal", "font-medium", "font-semibold", "font-bold", "font-extrabold")
        weights.forEach { weight ->
            val button = JButton(weight)
            button.addActionListener {
                onTypographySelected(weight)
                close(OK_EXIT_CODE)
            }
            weightPanel.add(button)
        }
        
        panel.add(sizePanel)
        panel.add(weightPanel)
        
        return panel
    }
}

/**
 * Dialog pour le créateur de gradients
 */
class GradientCreatorDialog(
    project: Project,
    private val onGradientSelected: (String) -> Unit
) : DialogWrapper(project) {
    
    init {
        title = "Tailwind Gradient Creator"
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        val gradients = listOf(
            "bg-gradient-to-r from-blue-500 to-purple-600",
            "bg-gradient-to-r from-green-400 to-blue-500",
            "bg-gradient-to-r from-pink-500 to-red-500",
            "bg-gradient-to-r from-yellow-400 to-orange-500",
            "bg-gradient-to-r from-purple-400 to-pink-400",
            "bg-gradient-to-r from-indigo-500 to-purple-600",
            "bg-gradient-to-r from-teal-400 to-blue-500",
            "bg-gradient-to-r from-red-500 to-pink-500"
        )
        
        gradients.forEach { gradient ->
            val button = JButton(gradient)
            button.addActionListener {
                onGradientSelected(gradient)
                close(OK_EXIT_CODE)
            }
            panel.add(button)
        }
        
        return panel
    }
}