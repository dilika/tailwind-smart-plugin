package com.github.dilika.tailwindsmartplugin.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.github.dilika.tailwindsmartplugin.util.TailwindCategoryUtils
import com.github.dilika.tailwindsmartplugin.util.TailwindCategoryIcon
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.intellij.openapi.project.Project
import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils

/**
 * Minimal Tailwind CSS completion provider.
 */
class TailwindCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        resultSet: CompletionResultSet
    ) {
        val element = parameters.position
        if (!isClassAttribute(element)) return
        val project: Project = parameters.originalFile.project
        val caretOffset = parameters.offset
        val fileText = parameters.originalFile.text
        // Extract the prefix just before the caret, supporting variants and partials
        val prefix = extractTailwindPrefix(fileText, caretOffset)
        // For common prefixes like bg-, text-, etc. or empty prefix after space, show completions regardless of length
        val commonPrefixes = listOf("bg-", "text-", "border-", "flex", "grid", "p-", "m-")
        val minPrefixLength = 2
        
        // Either prefix is long enough or it's a common prefix/empty after space
        val isPrefixLongEnough = prefix.length >= minPrefixLength
        val isCommonPrefix = commonPrefixes.any { prefix.startsWith(it) }
        val isEmptyAfterSpace = prefix.isEmpty() || prefix.isBlank()
        
        if (!isPrefixLongEnough && !isCommonPrefix && !isEmptyAfterSpace) return
        TailwindUtils.getTailwindClasses(project)
            .asSequence()
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .take(100) // Limit to 100 suggestions for performance
            .forEach { cls ->
                val (category, color) = TailwindCategoryUtils.getCategoryAndColor(cls)
                val icon = TailwindCategoryIcon(category, color, 14)
                resultSet.addElement(
                    LookupElementBuilder.create(cls)
                        .withIcon(icon)
                        .withTypeText(category, true)
                        .withBoldness(true)
                )
            }
    }

    // Extracts the class prefix at the caret position, supporting variants and partials
    private fun extractTailwindPrefix(fileText: String, caretOffset: Int): String {
        val beforeCaret = fileText.substring(0, caretOffset)
        
        // First check if we're inside an attribute value by finding the last quote/space
        val lastQuote = beforeCaret.lastIndexOfAny(charArrayOf('"', '\'', '`'))
        val lastSpace = beforeCaret.lastIndexOf(' ')
        
        // If we found a quote, and it's after the last space, extract everything after that quote
        if (lastQuote >= 0 && (lastSpace < 0 || lastQuote > lastSpace)) {
            val afterQuote = beforeCaret.substring(lastQuote + 1)
            // If there's a space in that substring, take everything after the last space
            val lastSpaceAfterQuote = afterQuote.lastIndexOf(' ')
            if (lastSpaceAfterQuote >= 0) {
                return afterQuote.substring(lastSpaceAfterQuote + 1)
            }
            return afterQuote
        }
        
        // If we're here, either no quote was found or the last space is after the last quote
        // So take everything after the last space
        if (lastSpace >= 0) {
            return beforeCaret.substring(lastSpace + 1)
        }
        
        // As a fallback, split by separators and take the last segment
        val separators = Regex("[\\s`\"'{(]+")
        val segments = beforeCaret.split(separators)
        return segments.lastOrNull() ?: ""
    }

    private fun isClassAttribute(element: PsiElement): Boolean {
        // Check for XML/HTML
        val parent = (element as? XmlAttributeValue)?.parent
            ?: element.parent
        val name = parent?.firstChild?.text
        if (name == "class" || name == "className") return true
        val gpName = parent?.parent?.firstChild?.text
        if (gpName == "class" || gpName == "className") return true
        // Check for JS/TS/JSX/TSX: look for class or className in the context
        val text = element.text
        if (text.contains("class=") || text.contains("className=") || text.contains("className`")) return true
        // Fallback: search up the tree for a className or class attribute
        var e = element.parent
        repeat(3) {
            if (e == null) return@repeat
            if (e.text.contains("class=") || e.text.contains("className=")) return true
            e = e.parent
        }
        return false
    }
}
