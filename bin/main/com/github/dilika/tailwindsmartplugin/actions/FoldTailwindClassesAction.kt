package com.github.dilika.tailwindsmartplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.ex.FoldingModelEx
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.editor.FoldRegion
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import javax.swing.KeyStroke
import java.awt.event.KeyEvent
import java.awt.event.InputEvent

/**
 * Action to fold/unfold all Tailwind CSS class attributes in the current file.
 * Can be triggered by a keyboard shortcut.
 */
class FoldTailwindClassesAction : AnAction(), DumbAware {

    companion object {
        const val ACTION_ID = "com.github.dilika.tailwindsmartplugin.actions.FoldTailwindClasses"
        
        /**
         * Register this action with the ActionManager and assign a keyboard shortcut
         */
        fun registerAction() {
            val actionManager = ActionManager.getInstance()
            
            // Only register if not already registered
            if (actionManager.getAction(ACTION_ID) == null) {
                val action = FoldTailwindClassesAction()
                actionManager.registerAction(ACTION_ID, action)
                
                // Add keyboard shortcut programmatically
                val keymap = KeymapManager.getInstance().activeKeymap
                val firstKeyStroke = KeyStroke.getKeyStroke(
                    KeyEvent.VK_F,
                    InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK or InputEvent.ALT_DOWN_MASK
                )
                
                // Remove any existing shortcuts to avoid conflicts
                keymap.removeAllActionShortcuts(ACTION_ID)
                
                // Add the new shortcut
                val shortcut = KeyboardShortcut(firstKeyStroke, null)
                keymap.addShortcut(ACTION_ID, shortcut)
            }
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        
        // Get the folding model
        val foldingModel = editor.foldingModel as? FoldingModelEx ?: return
        
        // Find all Tailwind class fold regions
        val tailwindRegions = foldingModel.allFoldRegions.filter { region ->
            region.group?.toString()?.startsWith("tailwind-classes") == true
        }
        
        if (tailwindRegions.isEmpty()) return
        
        // Check if most are expanded or collapsed to determine operation
        val expandedCount = tailwindRegions.count { it.isExpanded }
        val shouldCollapse = expandedCount > tailwindRegions.size / 2
        
        // Perform batch folding operation
        foldingModel.runBatchFoldingOperation {
            tailwindRegions.forEach { region ->
                region.isExpanded = !shouldCollapse
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabled = editor != null
    }
}

/**
 * Service to register the action when the plugin starts
 */
@Service
class FoldTailwindClassesActionRegistrar : Disposable {
    init {
        // Register the action on service creation
        FoldTailwindClassesAction.registerAction()
    }
    
    override fun dispose() {
        // Nothing to dispose
    }
    
    companion object {
        // Get or create the service
        fun getInstance() = ApplicationManager.getApplication().service<FoldTailwindClassesActionRegistrar>()
    }
}
