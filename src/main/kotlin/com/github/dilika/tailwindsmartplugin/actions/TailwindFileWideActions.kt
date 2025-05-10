package com.github.dilika.tailwindsmartplugin.actions

import com.github.dilika.tailwindsmartplugin.folding.TailwindFoldingService
import com.github.dilika.tailwindsmartplugin.util.TailwindSortingUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.openapi.util.TextRange
import java.util.regex.Pattern

/**
 * Actions for applying operations to all Tailwind classes in a file:
 * - Sorting classes in a logical order for all class attributes
 * - Folding all class attributes
 * - Unfolding all class attributes
 */
abstract class BaseTailwindFileWideAction : AnAction() {
    protected val logger = Logger.getInstance(javaClass)
    protected val classAttributePattern = Pattern.compile("(class|className)\\s*=\\s*['\"]([^'\"]*)['\"]")
    
    override fun update(e: AnActionEvent) {
        // Always enable the action
        e.presentation.isEnabled = true
    }
    
    /**
     * Process all class attributes in a file
     */
    protected fun processFileClassAttributes(e: AnActionEvent, processor: (TextRange, String, Editor, Project) -> Unit) {
        try {
            val editor = e.getData(CommonDataKeys.EDITOR) ?: return
            val project = e.getData(CommonDataKeys.PROJECT) ?: return
            val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
            
            // Find all XML/HTML class attributes
            val classAttributes = findClassAttributes(psiFile)
            
            // Process each class attribute
            for (attribute in classAttributes) {
                val valueElement = attribute.valueElement ?: continue
                val value = valueElement.value
                val range = valueElement.valueTextRange
                
                processor(range, value, editor, project)
            }
            
            // Also look for JSX/JS className patterns
            val fileText = editor.document.text
            val matcher = classAttributePattern.matcher(fileText)
            
            while (matcher.find()) {
                val start = matcher.start(2)
                val end = matcher.end(2)
                val range = TextRange(start, end)
                val value = matcher.group(2) ?: continue
                
                processor(range, value, editor, project)
            }
        } catch (ex: Exception) {
            logger.warn("Error processing file-wide Tailwind classes: ${ex.message}", ex)
        }
    }
    
    private fun findClassAttributes(psiFile: PsiFile): List<XmlAttribute> {
        val result = mutableListOf<XmlAttribute>()
        
        try {
            PsiTreeUtil.processElements(psiFile) { element ->
                if (element is XmlAttribute) {
                    val name = element.name.lowercase()
                    if (name == "class" || name == "classname") {
                        result.add(element)
                    }
                }
                true
            }
        } catch (ex: Exception) {
            logger.warn("Error finding class attributes: ${ex.message}", ex)
        }
        
        return result
    }
}

/**
 * Action to sort all Tailwind classes in the entire file
 */
class SortAllTailwindClassesAction : BaseTailwindFileWideAction() {
    override fun actionPerformed(e: AnActionEvent) {
        processFileClassAttributes(e) { range, value, editor, project ->
            val sortedValue = TailwindSortingUtils.sortClasses(value)
            
            if (sortedValue != value) {
                WriteCommandAction.runWriteCommandAction(project) {
                    editor.document.replaceString(range.startOffset, range.endOffset, sortedValue)
                }
            }
        }
    }
}

/**
 * Action to fold all Tailwind class attributes in the file
 */
class FoldAllTailwindClassesAction : BaseTailwindFileWideAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        
        // Use the folding service to fold all regions
        val foldingService = TailwindFoldingService.getInstance()
        foldingService.foldAllInEditor(editor, project)
    }
}

/**
 * Action to unfold all Tailwind class attributes in the file
 */
class UnfoldAllTailwindClassesAction : BaseTailwindFileWideAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        
        // Use the folding service to unfold all regions
        val foldingService = TailwindFoldingService.getInstance()
        foldingService.unfoldAllInEditor(editor, project)
    }
}
