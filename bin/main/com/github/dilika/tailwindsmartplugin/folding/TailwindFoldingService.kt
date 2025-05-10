package com.github.dilika.tailwindsmartplugin.folding

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.editor.FoldRegion
import com.intellij.openapi.editor.ex.FoldingModelEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue

/**
 * Service for working with Tailwind class folding.
 * Provides methods for finding and toggling fold regions for Tailwind classes.
 */
@Service
class TailwindFoldingService {
    private val logger = Logger.getInstance(TailwindFoldingService::class.java)
    private val TAILWIND_GROUP_ID = "tailwind-classes"

    companion object {
        fun getInstance(): TailwindFoldingService = service()
    }
    
    /**
     * Toggles the folding state of Tailwind class regions in the editor.
     */
    fun toggleTailwindFoldingState(editor: Editor, project: Project) {
        val foldingModel = editor.foldingModel
        
        // Find all regions related to Tailwind classes in the current state
        val tailwindRegions = foldingModel.allFoldRegions.filter { region ->
            region.group?.toString()?.contains(TAILWIND_GROUP_ID) == true
        }
        
        if (tailwindRegions.isEmpty()) {
            logger.info("No Tailwind folding regions found, calling builder to create them")
            
            // If no regions exist, trigger a rebuild of fold regions which will create them
            editor.document.let { document ->
                val file = PsiDocumentManager.getInstance(project).getPsiFile(document)
                file?.let { psiFile ->
                    // Force an update to rebuild the folding regions
                    createFoldRegionsForFile(editor, psiFile)
                }
            }
            
            // Try again after rebuild
            val newRegions = foldingModel.allFoldRegions.filter { region -> 
                region.group?.toString()?.contains(TAILWIND_GROUP_ID) == true
            }
            
            if (newRegions.isNotEmpty()) {
                // Fold the newly created regions
                foldingModel.runBatchFoldingOperation {
                    newRegions.forEach { region -> 
                        region.isExpanded = false
                    }
                }
                logger.info("Folded ${newRegions.size} newly created Tailwind regions")
            } else {
                logger.warn("No Tailwind folding regions found after rebuild")
            }
        } else {
            // If regions exist, toggle their state (if all expanded, collapse all; if any collapsed, expand all)
            val allExpanded = tailwindRegions.all { it.isExpanded }
            
            foldingModel.runBatchFoldingOperation {
                tailwindRegions.forEach { region ->
                    region.isExpanded = !allExpanded
                }
            }
            
            logger.info("Toggled ${tailwindRegions.size} Tailwind regions to ${!allExpanded}")
        }
    }
    
    /**
     * Creates folding regions for a file directly, ensuring accurate text boundaries.
     */
    private fun createFoldRegionsForFile(editor: Editor, psiFile: PsiFile) {
        val foldingModel = editor.foldingModel as? FoldingModelEx ?: return
        
        try {
            // Process XML attributes first for structured markup
            foldingModel.runBatchFoldingOperation {
                // Find all XML attributes that are class or className
                PsiTreeUtil.processElements(psiFile) { element ->
                    if (element is XmlAttribute) {
                        val attributeName = element.name.lowercase()
                        if (attributeName == "class" || attributeName == "classname") {
                            val valueElement = element.valueElement
                            val value = element.value ?: ""
                            
                            if (value.length >= 15 && valueElement != null) {
                                val originalRange = valueElement.textRange
                                // Skip quotes - create region for content only
                                val startOffset = originalRange.startOffset + 1
                                val endOffset = originalRange.endOffset - 1
                                
                                if (endOffset > startOffset) {
                                    foldingModel.createFoldRegion(
                                        startOffset,
                                        endOffset,
                                        "...",
                                        FoldingGroup.newGroup(TAILWIND_GROUP_ID),
                                        false
                                    )
                                    
                                    logger.debug("Created fold region for ${attributeName} from ${startOffset} to ${endOffset}")
                                }
                            }
                        }
                    }
                    true
                }
                
                // Process non-XML content (JSX, TSX, etc.)
                PsiTreeUtil.processElements(psiFile) { element ->
                    if (element !is XmlAttribute && element !is XmlAttributeValue && element.textLength > 15) {
                        val text = element.text
                        val elementStart = element.textRange.startOffset
                        
                        // 1. Process template literals: className={`...`}
                        createClassRegions(
                            foldingModel,
                            text,
                            "(class|className)\\s*=\\s*\\{\\s*`([^`]+)`\\s*\\}",
                            2, // Group index for content
                            elementStart,
                            element
                        )
                        
                        // 2. Process classNames function: className={classNames("...")}
                        createClassRegions(
                            foldingModel,
                            text,
                            "(class|className)\\s*=\\s*\\{\\s*classNames\\(\\s*[\"']([^\"']+)[\"']",
                            2, // Group index for content
                            elementStart,
                            element
                        )
                        
                        // 3. Process standard attributes in JSX: className="..."
                        createClassRegions(
                            foldingModel,
                            text,
                            "(class|className)\\s*=\\s*[\"']([^\"']+)[\"']",
                            2, // Group index for content
                            elementStart,
                            element
                        )
                    }
                    true
                }
            }
        } catch (e: Exception) {
            logger.error("Error creating fold regions: ${e.message}", e)
        }
    }
    
    /**
     * Helper method to create folding regions for class content found by regex patterns.
     */
    private fun createClassRegions(
        foldingModel: FoldingModelEx,
        text: String,
        pattern: String,
        contentGroupIndex: Int,
        elementStart: Int,
        element: PsiElement
    ) {
        val regex = pattern.toRegex()
        val matches = regex.findAll(text)
        
        matches.forEach { match ->
            val classContent = match.groupValues[contentGroupIndex]
            if (classContent.length >= 15) {
                // Find the precise location of the class content
                val fullMatch = match.value
                val contentPos = fullMatch.indexOf(classContent)
                
                if (contentPos >= 0) {
                    val startOffset = elementStart + match.range.first + contentPos
                    val endOffset = startOffset + classContent.length
                    
                    foldingModel.createFoldRegion(
                        startOffset,
                        endOffset,
                        "...",
                        FoldingGroup.newGroup(TAILWIND_GROUP_ID),
                        false
                    )
                    
                    logger.debug("Created fold region for regex pattern from ${startOffset} to ${endOffset}")
                }
            }
        }
    }
    
    /**
     * Specifically folds all Tailwind class regions.
     * If regions don't exist yet, it triggers their creation first.
     */
    fun foldAllTailwindClasses(editor: Editor, psiFile: PsiFile) {
        val foldingModel = editor.foldingModel
        
        // First check if we have any regions to fold
        var tailwindRegions = foldingModel.allFoldRegions.filter { region ->
            region.group?.toString()?.contains(TAILWIND_GROUP_ID) == true
        }
        
        // If no regions exist, trigger region creation first
        if (tailwindRegions.isEmpty()) {
            logger.info("No Tailwind folding regions found, creating them first")
            createFoldRegionsForFile(editor, psiFile)
            
            // Get the newly created regions
            tailwindRegions = foldingModel.allFoldRegions.filter { region -> 
                region.group?.toString()?.contains(TAILWIND_GROUP_ID) == true
            }
        }
        
        // Now fold all regions
        if (tailwindRegions.isNotEmpty()) {
            foldingModel.runBatchFoldingOperation {
                tailwindRegions.forEach { region -> 
                    region.isExpanded = false
                }
            }
            logger.info("Folded ${tailwindRegions.size} Tailwind regions")
        } else {
            logger.warn("No Tailwind folding regions found to fold")
        }
    }
    
    /**
     * Specifically unfolds all Tailwind class regions.
     */
    fun unfoldAllTailwindClasses(editor: Editor, psiFile: PsiFile) {
        val foldingModel = editor.foldingModel
        
        // Find all Tailwind class regions
        val tailwindRegions = foldingModel.allFoldRegions.filter { region ->
            region.group?.toString()?.contains(TAILWIND_GROUP_ID) == true
        }
        
        // Unfold all regions
        if (tailwindRegions.isNotEmpty()) {
            foldingModel.runBatchFoldingOperation {
                tailwindRegions.forEach { region -> 
                    region.isExpanded = true
                }
            }
            logger.info("Unfolded ${tailwindRegions.size} Tailwind regions")
        } else {
            logger.warn("No Tailwind folding regions found to unfold")
        }
    }
    
    /**
     * Overload for unfoldAllTailwindClasses that only requires an Editor.
     * This is useful for keyboard shortcuts where we don't need the PsiFile.
     */
    fun unfoldAllInEditor(editor: Editor, project: Project) {
        val foldingModel = editor.foldingModel
        
        // Find all Tailwind class regions
        val tailwindRegions = foldingModel.allFoldRegions.filter { region ->
            region.group?.toString()?.contains(TAILWIND_GROUP_ID) == true
        }
        
        // If no regions exist, create them first
        if (tailwindRegions.isEmpty()) {
            // Try to get the PsiFile from the editor
            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
            if (psiFile != null) {
                createFoldRegionsForFile(editor, psiFile)
            }
        }
        
        // Get the regions again after potential creation
        val allRegions = foldingModel.allFoldRegions.filter { region ->
            region.group?.toString()?.contains(TAILWIND_GROUP_ID) == true
        }
        
        // Unfold all regions
        if (allRegions.isNotEmpty()) {
            foldingModel.runBatchFoldingOperation {
                allRegions.forEach { region -> 
                    region.isExpanded = true
                }
            }
            logger.info("Unfolded ${allRegions.size} Tailwind regions")
        } else {
            logger.warn("No Tailwind folding regions found to unfold")
        }
    }
    
    /**
     * Folds all Tailwind class regions in the editor. Creates regions first if needed.
     */
    fun foldAllInEditor(editor: Editor, project: Project) {
        val foldingModel = editor.foldingModel
        
        // Find all Tailwind class regions
        var tailwindRegions = foldingModel.allFoldRegions.filter { region ->
            region.group?.toString()?.contains(TAILWIND_GROUP_ID) == true
        }
        
        // If no regions exist, create them first
        if (tailwindRegions.isEmpty()) {
            // Try to get the PsiFile from the editor
            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
            if (psiFile != null) {
                createFoldRegionsForFile(editor, psiFile)
                
                // Get regions again after creation
                tailwindRegions = foldingModel.allFoldRegions.filter { region ->
                    region.group?.toString()?.contains(TAILWIND_GROUP_ID) == true
                }
            }
        }
        
        // Fold all regions
        if (tailwindRegions.isNotEmpty()) {
            foldingModel.runBatchFoldingOperation {
                tailwindRegions.forEach { region -> 
                    region.isExpanded = false
                }
            }
            logger.info("Folded ${tailwindRegions.size} Tailwind regions")
        } else {
            logger.warn("No Tailwind folding regions found to fold")
        }
    }
    
    /**
     * Find fold regions at the given offset that match Tailwind class groups.
     */
    fun findTailwindFoldRegionAt(editor: Editor, offset: Int): FoldRegion? {
        val model = editor.foldingModel
        
        return model.allFoldRegions.find { region ->
            region.startOffset <= offset && offset <= region.endOffset &&
            region.group?.toString()?.contains(TAILWIND_GROUP_ID) == true
        }
    }
}
