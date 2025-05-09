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
                    
                    // Find the fold region at the click offset with improved logic
                    // First check for exact matches, then expand search area if needed
                    var foldRegion = model.allFoldRegions.find { region -> 
                        region.startOffset <= offset && offset <= region.endOffset && 
                        region.group?.toString()?.startsWith("tailwind-classes") == true
                    }
                    
                    // If no region found, try to look for nearby regions (within 5 characters)
                    if (foldRegion == null) {
                        // Try a slightly wider range to make clicking easier
                        val rangeStart = Math.max(0, offset - 5)
                        val rangeEnd = Math.min(editor.document.textLength, offset + 5)
                        
                        foldRegion = model.allFoldRegions.find { region ->
                            ((region.startOffset <= rangeEnd && region.startOffset >= rangeStart) ||
                             (region.endOffset <= rangeEnd && region.endOffset >= rangeStart) ||
                             (region.startOffset <= rangeStart && region.endOffset >= rangeEnd)) &&
                            region.group?.toString()?.startsWith("tailwind-classes") == true
                        }
                    }

                    foldRegion?.let {
                        model.runBatchFoldingOperation {
                            it.isExpanded = !it.isExpanded
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
