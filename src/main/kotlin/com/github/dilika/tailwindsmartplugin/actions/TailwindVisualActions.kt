package com.github.dilika.tailwindsmartplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.github.dilika.tailwindsmartplugin.preview.TailwindInteractivePaletteService
import com.intellij.openapi.diagnostic.Logger

/**
 * Action pour ouvrir le sélecteur de couleurs Tailwind
 */
class TailwindColorPickerAction : AnAction("Tailwind Color Picker", "Open Tailwind color picker", null), DumbAware {
    
    private val logger = Logger.getInstance(TailwindColorPickerAction::class.java)
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        
        try {
            val paletteService = TailwindInteractivePaletteService()
            paletteService.showColorPicker(project) { colorClass ->
                // Insérer la classe de couleur dans l'éditeur
                insertClassIntoEditor(editor, colorClass)
            }
        } catch (ex: Exception) {
            logger.warn("Error opening color picker", ex)
        }
    }
    
    private fun insertClassIntoEditor(editor: com.intellij.openapi.editor.Editor, className: String) {
        val document = editor.document
        val caretModel = editor.caretModel
        val offset = caretModel.offset
        
        document.insertString(offset, className)
    }
}

/**
 * Action pour ouvrir l'outil d'espacement Tailwind
 */
class TailwindSpacingToolAction : AnAction("Tailwind Spacing Tool", "Open Tailwind spacing tool", null), DumbAware {
    
    private val logger = Logger.getInstance(TailwindSpacingToolAction::class.java)
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        
        try {
            val paletteService = TailwindInteractivePaletteService()
            paletteService.showSpacingTool(project) { spacingClass ->
                insertClassIntoEditor(editor, spacingClass)
            }
        } catch (ex: Exception) {
            logger.warn("Error opening spacing tool", ex)
        }
    }
    
    private fun insertClassIntoEditor(editor: com.intellij.openapi.editor.Editor, className: String) {
        val document = editor.document
        val caretModel = editor.caretModel
        val offset = caretModel.offset
        
        document.insertString(offset, className)
    }
}

/**
 * Action pour ouvrir l'outil de typographie Tailwind
 */
class TailwindTypographyToolAction : AnAction("Tailwind Typography Tool", "Open Tailwind typography tool", null), DumbAware {
    
    private val logger = Logger.getInstance(TailwindTypographyToolAction::class.java)
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        
        try {
            val paletteService = TailwindInteractivePaletteService()
            paletteService.showTypographyTool(project) { typographyClass ->
                insertClassIntoEditor(editor, typographyClass)
            }
        } catch (ex: Exception) {
            logger.warn("Error opening typography tool", ex)
        }
    }
    
    private fun insertClassIntoEditor(editor: com.intellij.openapi.editor.Editor, className: String) {
        val document = editor.document
        val caretModel = editor.caretModel
        val offset = caretModel.offset
        
        document.insertString(offset, className)
    }
}

/**
 * Action pour ouvrir le créateur de gradients Tailwind
 */
class TailwindGradientCreatorAction : AnAction("Tailwind Gradient Creator", "Open Tailwind gradient creator", null), DumbAware {
    
    private val logger = Logger.getInstance(TailwindGradientCreatorAction::class.java)
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        
        try {
            val paletteService = TailwindInteractivePaletteService()
            paletteService.showGradientCreator(project) { gradientClass ->
                insertClassIntoEditor(editor, gradientClass)
            }
        } catch (ex: Exception) {
            logger.warn("Error opening gradient creator", ex)
        }
    }
    
    private fun insertClassIntoEditor(editor: com.intellij.openapi.editor.Editor, className: String) {
        val document = editor.document
        val caretModel = editor.caretModel
        val offset = caretModel.offset
        
        document.insertString(offset, className)
    }
}




