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
 * Action to fold all Tailwind CSS classes in the current file.
 * This action is available from the Code menu, Editor popup menu, and via keyboard shortcut Alt+F.
 */
class SimpleFoldAction : AnAction(), DumbAware {
    private val logger = Logger.getInstance(SimpleFoldAction::class.java)
    private val tailwindFoldingService = TailwindFoldingService.getInstance()

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)

        logger.info("Folding all Tailwind classes")
        
        if (psiFile != null) {
            tailwindFoldingService.foldAllTailwindClasses(editor, psiFile)
            logger.info("Folded all Tailwind classes in file: ${psiFile.name}")
        } else {
            logger.warn("Could not get PSI file for folding action")
        }
        
        // Extremely important: consume the event to prevent additional character insertion
        // This prevents unwanted characters from appearing when using Alt+F
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
