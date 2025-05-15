package com.github.dilika.tailwindsmartplugin.listeners

import com.github.dilika.tailwindsmartplugin.folding.TailwindFoldingService
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.FoldRegion
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.ex.FoldingModelEx
import com.intellij.openapi.editor.impl.EditorImpl

/**
 * Listener that attaches click handlers to editors for Tailwind class folding.
 * Handles mouse clicks on folded regions to unfold them without toggling.
 */
class TailwindFoldingClickListener : EditorFactoryListener {
    private val logger = Logger.getInstance(TailwindFoldingClickListener::class.java)
    private val tailwindFoldingService = TailwindFoldingService.getInstance()

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        
        // Add click listener to the editor
        editor.addEditorMouseListener(createMouseAdapter())
        logger.debug("Added Tailwind folding click listener to editor")
    }
    
    /**
     * Creates a mouse adapter that handles clicks on Tailwind folding regions.
     */
    private fun createMouseAdapter(): EditorMouseListener {
        return object : EditorMouseListener {
            override fun mouseClicked(e: EditorMouseEvent) {
                // Only process left-button clicks
                if (e.mouseEvent.clickCount != 1 || e.mouseEvent.button != 1) {
                    return
                }

                val editor = e.editor as? EditorImpl ?: return
                val offset = editor.logicalPositionToOffset(
                    editor.xyToLogicalPosition(e.mouseEvent.point)
                )

                // Check if click is on a Tailwind folding region
                val region = findTailwindFoldRegionAt(editor, offset)
                if (region != null) {
                    // If the region is collapsed, expand it and consume the event
                    if (!region.isExpanded) {
                        logger.info("Clicked on a folded Tailwind class region at offset $offset")
                        unfoldRegion(editor, region)
                        e.consume()
                    } else {
                        // If it's already expanded, do nothing (don't fold it on click)
                        logger.debug("Clicked on an already expanded Tailwind class region - no action taken")
                    }
                }
            }
        }
    }

    /**
     * Finds a Tailwind fold region at the given offset.
     */
    private fun findTailwindFoldRegionAt(editor: Editor, offset: Int): FoldRegion? {
        return tailwindFoldingService.findTailwindFoldRegionAt(editor, offset)
    }

    /**
     * Unfolds a single region, ensuring it expands without showing quote characters.
     */
    private fun unfoldRegion(editor: Editor, region: FoldRegion) {
        val foldingModel = editor.foldingModel as FoldingModelEx
        foldingModel.runBatchFoldingOperation {
            // Simply expand the region - the folding descriptors were created with 
            // proper boundaries excluding quotes in TailwindClassFoldingBuilder
            region.isExpanded = true
            
            logger.info("Unfolded Tailwind class region: ${region.placeholderText}")
        }
    }
}
