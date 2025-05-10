package com.github.dilika.tailwindsmartplugin.startup

import com.github.dilika.tailwindsmartplugin.actions.FoldTailwindClassesAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import javax.swing.KeyStroke
import java.awt.event.KeyEvent
import java.awt.event.InputEvent

/**
 * Ensures that keyboard shortcuts for Tailwind actions are properly registered
 * during project startup, regardless of plugin.xml configuration.
 */
class TailwindKeyboardShortcutRegistrar : StartupActivity {
    override fun runActivity(project: Project) {
        registerFoldingShortcut()
    }
    
    private fun registerFoldingShortcut() {
        val actionId = "com.github.dilika.tailwindsmartplugin.actions.FoldTailwindClasses"
        val actionManager = ActionManager.getInstance()
        
        // Make sure the action is registered
        if (actionManager.getAction(actionId) == null) {
            actionManager.registerAction(actionId, FoldTailwindClassesAction())
        }
        
        // Force register the keyboard shortcut
        try {
            val keymap = KeymapManager.getInstance().activeKeymap
            
            // Remove any existing shortcuts for this action
            keymap.removeAllActionShortcuts(actionId)
            
            // Create Ctrl+Alt+Shift+F shortcut
            val shortcutKeyStroke = KeyStroke.getKeyStroke(
                KeyEvent.VK_F, 
                InputEvent.CTRL_DOWN_MASK or InputEvent.ALT_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK
            )
            val shortcut = KeyboardShortcut(shortcutKeyStroke, null)
            
            // Add to keymap
            keymap.addShortcut(actionId, shortcut)
        } catch (e: Exception) {
            // Log error but don't crash
            println("Failed to register keyboard shortcut: ${e.message}")
        }
    }
}
