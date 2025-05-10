package com.github.dilika.tailwindsmartplugin.actions

import com.github.dilika.tailwindsmartplugin.util.TailwindSortingUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.util.TextRange
import java.util.regex.Pattern

/**
 * Action to sort Tailwind CSS classes in the current selection or at the cursor position.
 * This sorts classes according to a logical grouping that enhances readability.
 */
class SortTailwindClassesAction : AnAction() {
    
    // Pattern to identify class attributes in markup
    private val classAttributePattern = Pattern.compile("(class|className)\\s*=\\s*['\"]([^'\"]*)['\"]")
    
    override fun update(e: AnActionEvent) {
        // Always enable the action regardless of context
        e.presentation.isEnabled = true
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        // Use getData instead of getRequiredData to avoid exceptions
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        
        try {
        
        // Check if there's a selection first
        val selectionModel = editor.selectionModel
        if (selectionModel.hasSelection()) {
            sortSelectionClasses(selectionModel, editor, project)
            return
        }
        
        // No selection, try to find Tailwind class attributes at cursor
        val offset = editor.caretModel.offset
        val psiElement = psiFile.findElementAt(offset)
        
        if (psiElement != null) {
            // Check if we're in an XML/HTML attribute
            val xmlAttribute = PsiTreeUtil.getParentOfType(psiElement, XmlAttribute::class.java)
            if (xmlAttribute != null && (xmlAttribute.name == "class" || xmlAttribute.name == "className")) {
                sortXmlAttributeClasses(xmlAttribute, editor, project)
                return
            }
            
            // Try to find a class attribute in the text (for JSX, etc.)
            val elementText = psiElement.text
            val elementOffset = psiElement.textRange.startOffset
            val matcher = classAttributePattern.matcher(elementText)
            
            while (matcher.find()) {
                val start = elementOffset + matcher.start(2)
                val end = elementOffset + matcher.end(2)
                
                // Check if cursor is within or near the class value
                if (offset >= start - 5 && offset <= end + 5) {
                    sortTextRangeClasses(TextRange(start, end), editor, project)
                    return
                }
            }
        }
        
        // Fallback: Look at the line containing the cursor
        val document = editor.document
        val lineNumber = document.getLineNumber(offset)
        val lineStart = document.getLineStartOffset(lineNumber)
        val lineEnd = document.getLineEndOffset(lineNumber)
        val lineText = document.getText(TextRange(lineStart, lineEnd))
        
        val matcher = classAttributePattern.matcher(lineText)
        if (matcher.find()) {
            val classStart = lineStart + matcher.start(2)
            val classEnd = lineStart + matcher.end(2)
            sortTextRangeClasses(TextRange(classStart, classEnd), editor, project)
        }
        } catch (ex: Exception) {
            // Log any exception to help diagnose issues
            com.intellij.openapi.diagnostic.Logger.getInstance(javaClass)
                .warn("Error sorting Tailwind classes: ${ex.message}", ex)
        }
    }
    
    private fun sortSelectionClasses(selectionModel: SelectionModel, editor: Editor, project: Project) {
        val selectedText = selectionModel.selectedText ?: return
        val sortedText = TailwindSortingUtils.sortClasses(selectedText)
        
        if (sortedText != selectedText) {
            val startOffset = selectionModel.selectionStart
            val endOffset = selectionModel.selectionEnd
            
            WriteCommandAction.runWriteCommandAction(project) {
                editor.document.replaceString(startOffset, endOffset, sortedText)
            }
        }
    }
    
    private fun sortXmlAttributeClasses(attribute: XmlAttribute, editor: Editor, project: Project) {
        val valueElement = attribute.valueElement ?: return
        val value = valueElement.value
        val sortedValue = TailwindSortingUtils.sortClasses(value)
        
        if (sortedValue != value) {
            val valueTextRange = valueElement.valueTextRange
            
            WriteCommandAction.runWriteCommandAction(project) {
                editor.document.replaceString(valueTextRange.startOffset, valueTextRange.endOffset, sortedValue)
            }
        }
    }
    
    private fun sortTextRangeClasses(range: TextRange, editor: Editor, project: Project) {
        val text = editor.document.getText(range)
        val sortedText = TailwindSortingUtils.sortClasses(text)
        
        if (sortedText != text) {
            WriteCommandAction.runWriteCommandAction(project) {
                editor.document.replaceString(range.startOffset, range.endOffset, sortedText)
            }
        }
    }
}
