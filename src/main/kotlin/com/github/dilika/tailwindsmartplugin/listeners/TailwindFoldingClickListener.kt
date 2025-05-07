package com.github.dilika.tailwindsmartplugin.listeners

import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.ex.FoldingModelEx
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.Editor

/**
 * Editor listener to toggle Tailwind CSS class folding on click.
 * Folds and unfolds entire class/className attributes when clicked.
 */
class TailwindFoldingClickListener : EditorFactoryListener {
    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        if (editor is EditorEx) {
            editor.addEditorMouseListener(object : EditorMouseListener {
                override fun mouseClicked(e: EditorMouseEvent) {
                    val point = e.mouseEvent.point
                    val logical = editor.xyToLogicalPosition(point)
                    val offset = editor.logicalPositionToOffset(logical)
                    val model = editor.foldingModel as FoldingModelEx
                    
                    // Find the fold region at the click position
                    val foldRegion = model.getFoldingPlaceholderAt(point)?.let { placeholder ->
                        model.allFoldRegions.find { region -> region.placeholderText == placeholder.toString() && offset >= region.startOffset && offset <= region.endOffset }
                    }

                    foldRegion?.let {
                        // Check group name to ensure it's a Tailwind class fold
                        if (it.group?.toString()?.startsWith("tailwind-classes") == true) {
                            model.runBatchFoldingOperation {
                                it.isExpanded = !it.isExpanded
                            }
                        }
                    }
                }
                override fun mousePressed(e: EditorMouseEvent) {}
                override fun mouseReleased(e: EditorMouseEvent) {}
                override fun mouseEntered(e: EditorMouseEvent) {}
                override fun mouseExited(e: EditorMouseEvent) {}
            })
        }
    }
}
