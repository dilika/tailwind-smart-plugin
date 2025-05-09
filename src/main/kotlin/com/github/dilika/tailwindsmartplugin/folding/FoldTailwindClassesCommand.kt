package com.github.dilika.tailwindsmartplugin.folding

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.ex.FoldingModelEx
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.command.CommandProcessor

/**
 * Command to fold/unfold all Tailwind CSS classes in the current editor
 */
class FoldTailwindClassesCommand : AnAction(), DumbAware {
    
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        
        CommandProcessor.getInstance().executeCommand(project, {
            val foldingModel = editor.foldingModel as? FoldingModelEx ?: return@executeCommand
            
            // Find all Tailwind class fold regions
            val tailwindRegions = foldingModel.allFoldRegions.filter { region ->
                region.group?.toString()?.startsWith("tailwind-classes") == true
            }
            
            if (tailwindRegions.isEmpty()) return@executeCommand
            
            // Determine whether to collapse or expand
            val expandedCount = tailwindRegions.count { it.isExpanded }
            val shouldCollapse = expandedCount > tailwindRegions.size / 2
            
            // Perform batch folding operation
            foldingModel.runBatchFoldingOperation {
                tailwindRegions.forEach { region ->
                    region.isExpanded = !shouldCollapse
                }
            }
        }, "Fold/Unfold Tailwind Classes", null)
    }
    
    override fun update(e: AnActionEvent) {
        // Only enable this action if there's an editor available
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabled = editor != null
    }
}
