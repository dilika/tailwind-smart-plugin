package com.github.dilika.tailwindsmartplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.ex.FoldingModelEx
import com.intellij.openapi.project.DumbAware

/**
 * Simple action to fold/unfold all Tailwind CSS classes in the current editor.
 * Directly registered in plugin.xml with a keyboard shortcut.
 */
class TailwindFoldingAction : AnAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val foldingModel = editor.foldingModel as? FoldingModelEx ?: return
        
        // Find all Tailwind class fold regions
        val tailwindRegions = foldingModel.allFoldRegions.filter { region ->
            region.group?.toString()?.startsWith("tailwind-classes") == true
        }
        
        if (tailwindRegions.isEmpty()) return
        
        // Determine whether to collapse or expand based on current state
        val expandedCount = tailwindRegions.count { it.isExpanded }
        val shouldCollapse = expandedCount > tailwindRegions.size / 2
        
        // Execute folding operation
        foldingModel.runBatchFoldingOperation {
            tailwindRegions.forEach { region ->
                region.isExpanded = !shouldCollapse
            }
        }
    }
    
    override fun update(e: AnActionEvent) {
        // Enable the action only when there's an editor
        e.presentation.isEnabled = e.getData(CommonDataKeys.EDITOR) != null
    }
}
