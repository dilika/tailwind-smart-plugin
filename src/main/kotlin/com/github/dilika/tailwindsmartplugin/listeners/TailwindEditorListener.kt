package com.github.dilika.tailwindsmartplugin.listeners

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.FoldingModelEx
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.Alarm
import java.util.regex.Pattern
import com.intellij.openapi.diagnostic.Logger

/**
 * Listener that attaches to each new editor to automatically fold
 * Tailwind CSS class and className attributes.
 * This listener helps ensure folding is applied correctly, especially after indexing.
 */
class TailwindEditorListener : EditorFactoryListener {
    private val logger = Logger.getInstance(TailwindEditorListener::class.java)
    
    // Pattern to identify class and className attributes in text content
    private val classAttributePattern = Pattern.compile("(class|className)\\s*=\\s*['\"]([^'\"]*)['\"]")

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        val project = editor.project ?: return
        logger.debug("TailwindEditorListener: editorCreated for project ${project.name}")
        val document = editor.document
        
        // Get the file associated with the document
        val file = FileDocumentManager.getInstance().getFile(document) ?: return
        
        // Only process relevant files
        val fileType = file.fileType.name.lowercase()
        if (!isRelevantFileType(fileType) && !isRelevantFileExtension(file.extension)) {
            return
        }
        
        // Use a delayed execution with alarm to ensure proper timing
        val alarm = Alarm(Alarm.ThreadToUse.SWING_THREAD)
        alarm.addRequest({
            // Execute when dumb mode is finished (indexing is complete)
            DumbService.getInstance(project).runWhenSmart {
                ApplicationManager.getApplication().invokeLater({
                    if (!editor.isDisposed) {
                        logger.debug("TailwindEditorListener: About to applyTailwindFolding for ${file.name}")
                        applyTailwindFolding(editor, project)
                    }
                }, ModalityState.defaultModalityState())
            }
        }, 3000) // Delay of 3000ms
    }
    
    /**
     * Checks if the file type is relevant for Tailwind attribute folding
     */
    private fun isRelevantFileType(fileType: String): Boolean {
        return fileType.contains("html") || 
               fileType.contains("xml") || 
               fileType.contains("jsx") ||
               fileType.contains("tsx") ||
               fileType.contains("javascript") ||
               fileType.contains("typescript") ||
               fileType.contains("vue") ||
               fileType.contains("php")
    }
    
    /**
     * Checks if the file extension is relevant
     */
    private fun isRelevantFileExtension(extension: String?): Boolean {
        if (extension == null) return false
        
        return extension == "html" ||
               extension == "htm" ||
               extension == "xml" ||
               extension == "jsx" ||
               extension == "tsx" ||
               extension == "js" ||
               extension == "ts" ||
               extension == "vue" ||
               extension == "php"
    }
    
    /**
     * Applies folding for all class/className attributes in the document
     */
    private fun applyTailwindFolding(editor: Editor, project: com.intellij.openapi.project.Project) {
        try {
            logger.debug("TailwindEditorListener: applyTailwindFolding started.")
            val document = editor.document
            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return
            
            val rangesToFold = mutableListOf<Pair<TextRange, String>>()
            
            // Process XML attributes
            processXmlAttributes(psiFile, rangesToFold)
            
            // Process text content (e.g., in JSX/TSX)
            processTextContent(document.text, rangesToFold) // Always process text content
            
            if (rangesToFold.isNotEmpty()) {
                ApplicationManager.getApplication().runWriteAction {
                    try {
                        val foldingModel = editor.foldingModel as? FoldingModelEx ?: return@runWriteAction
                        
                        foldingModel.runBatchFoldingOperation {
                            for ((range, placeholder) in rangesToFold) {
                                if (range.startOffset < range.endOffset && 
                                    foldingModel.getFoldRegion(range.startOffset, range.endOffset) == null) {
                                    foldingModel.createFoldRegion(
                                        range.startOffset,
                                        range.endOffset,
                                        placeholder, // This will be "..." from processing methods
                                        null, 
                                        true // collapsed by default
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Catch exceptions during folding operation
                    }
                }
            }
        } catch (e: Exception) {
            // Safety catch-all
        }
    }
    
    /**
     * Process XML attributes to find class/className attributes
     */
    private fun processXmlAttributes(psiFile: com.intellij.psi.PsiFile, ranges: MutableList<Pair<TextRange, String>>) {
        PsiTreeUtil.processElements(psiFile, PsiElementProcessor { element ->
            if (element is XmlAttribute && (element.name == "class" || element.name == "className")) {
                element.valueElement?.let { valueElement ->
                    val classValue = valueElement.value
                    // Fold if class value is not blank and sufficiently long
                    if (classValue.isNotBlank() && classValue.length > 15) { 
                        val valueTextRange = valueElement.valueTextRange
                        if (!valueTextRange.isEmpty) {
                            ranges.add(Pair(valueTextRange, "...")) // Fold only value, placeholder is "..."
                        }
                    }
                }
            }
            true
        })
        logger.debug("TailwindEditorListener: processXmlAttributes found ${ranges.size} XML attributes to fold.")
    }
    
    /**
     * Process text content to find class/className attributes using regex
     */
    private fun processTextContent(text: String, ranges: MutableList<Pair<TextRange, String>>) {
        val matcher = classAttributePattern.matcher(text)
        val initialSize = ranges.size
        while (matcher.find()) {
            val classAttributeName = matcher.group(1) // class or className
            val classValue = matcher.group(2)
            
            // Fold if class value is not blank and sufficiently long
            if (classValue.isNotBlank() && classValue.length > 15) {
                val valueStartPos = matcher.start(2) // Start of the value itself
                val valueEndPos = matcher.end(2)   // End of the value itself
            
                if (valueStartPos >= 0 && valueEndPos > valueStartPos) {
                    ranges.add(Pair(
                        TextRange(valueStartPos, valueEndPos),
                        "..." // Placeholder is just "..." for the value
                    ))
                }
            }
        }
        logger.debug("TailwindEditorListener: processTextContent found ${ranges.size - initialSize} text attributes to fold.")
    }
}
