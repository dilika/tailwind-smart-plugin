package com.github.dilika.tailwindsmartplugin.actions

import com.github.dilika.tailwindsmartplugin.folding.TailwindFoldingService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import java.awt.event.KeyEvent

/**
 * Action to unfold all Tailwind CSS classes in the current file.
 * This action is available from the Code menu, Editor popup menu, and via keyboard shortcut Alt+U.
 */
class UnfoldAction : AnAction(), DumbAware {
    private val logger = Logger.getInstance(UnfoldAction::class.java)
    private val tailwindFoldingService = TailwindFoldingService.getInstance()

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return

        logger.info("Unfolding all tailwind classes")
        tailwindFoldingService.unfoldAllInEditor(editor, project)
        
        // Extremely important: consume the event to prevent additional character insertion
        // This prevents the ¨ character from appearing when using Alt+U
        val inputEvent = e.inputEvent
        if (inputEvent is KeyEvent) {
            inputEvent.consume()
            // Stop event propagation completely
            e.presentation.isEnabledAndVisible = false
        }
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabled = editor != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
