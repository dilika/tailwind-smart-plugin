package com.github.dilika.tailwindsmartplugin.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import java.util.regex.Pattern

/**
 * Folding builder for Tailwind CSS classes.
 * Identifies class and className attributes in HTML, JSX, and other files
 * and creates folding regions for these attributes.
 */
class TailwindClassFoldingBuilder : FoldingBuilderEx() {
    private val logger = Logger.getInstance(TailwindClassFoldingBuilder::class.java)
    private val TAILWIND_GROUP_ID = "tailwind-classes"
    
    // Minimum length of class attribute value to be folded
    private val MIN_FOLD_LENGTH = 15

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (root !is PsiFile) {
            return emptyArray()
        }

        val descriptors = mutableListOf<FoldingDescriptor>()

        try {
            // First check if it's HTML/XML
            val isXmlLike = listOf("HTML", "XML", "JSX Harmony", "JavaScript").any { lang ->
                try {
                    root.language.displayName == lang || 
                    root.language.isKindOf(Language.findLanguageByID(lang) ?: return@any false)
                } catch (e: ProcessCanceledException) {
                    // Ignore cancellation exceptions - they occur during IDE write operations
                    return emptyArray()
                } catch (e: Exception) {
                    logger.debug("Exception checking language: ${e.message}")
                    false
                }
            }

            if (isXmlLike) {
                processXmlAttributes(root, descriptors)
            }

            // Also check for JSX/JS files with className
            val isJsLike = listOf("JavaScript", "TypeScript", "JSX Harmony", "TSX").any { lang ->
                try {
                    root.language.displayName == lang || 
                    root.language.isKindOf(Language.findLanguageByID(lang) ?: return@any false)
                } catch (e: ProcessCanceledException) {
                    // Ignore cancellation exceptions - they occur during IDE write operations
                    return emptyArray()
                } catch (e: Exception) {
                    logger.debug("Exception checking language: ${e.message}")
                    false
                }
            }

            if (isJsLike) {
                processJsxPatterns(root, descriptors)
            }
        } catch (e: ProcessCanceledException) {
            // Ignore ProcessCanceledException - it's thrown during IDE write operations
            return emptyArray()
        } catch (e: Exception) {
            // Log other exceptions for debugging without re-throwing
            logger.debug("Exception during folding region building: ${e.message}", e)
        }

        return descriptors.toTypedArray()
    }

    private fun processXmlAttributes(element: PsiElement, descriptors: MutableList<FoldingDescriptor>) {
        try {
            // Find all XML attributes named 'class' or 'className'
            PsiTreeUtil.processElements(element) { currentElement ->
                if (currentElement is XmlAttribute) {
                    val name = currentElement.name.lowercase()
                    if ((name == "class" || name == "classname") && currentElement.value?.length ?: 0 >= MIN_FOLD_LENGTH) {
                        val valueElement = currentElement.valueElement
                        if (valueElement != null) {
                            val originalRange = valueElement.textRange
                            val value = valueElement.text
                            
                            // The text will have quotes, so trim those when creating the region
                            // The first character is the opening quote, and the last character is the closing quote
                            if (value.length >= 3) { // Minimum: 2 quotes + 1 character
                                val adjustedRange = TextRange(
                                    originalRange.startOffset + 1, // +1 to skip opening quote
                                    originalRange.endOffset - 1    // -1 to skip closing quote
                                )
                                
                                if (adjustedRange.length >= MIN_FOLD_LENGTH) {
                                    val descriptor = FoldingDescriptor(
                                        valueElement.node,
                                        adjustedRange,
                                        FoldingGroup.newGroup(TAILWIND_GROUP_ID),
                                        " Tailwind classes... "
                                    )
                                    descriptors.add(descriptor)
                                    logger.debug("Added XML attribute folding descriptor for ${name}")
                                }
                            }
                        }
                    }
                }
                true
            }
        } catch (e: ProcessCanceledException) {
            // Ignore cancellation exceptions - they occur during IDE write operations
        } catch (e: Exception) {
            logger.debug("Exception processing XML attributes: ${e.message}", e)
        }
    }

    private fun processJsxPatterns(element: PsiElement, descriptors: MutableList<FoldingDescriptor>) {
        try {
            // Helper function to process each pattern type
            fun processPattern(element: PsiElement, pattern: String, contentGroupIndex: Int) {
                val text = element.text
                val matcher = Pattern.compile(pattern).matcher(text)
                
                while (matcher.find()) {
                    if (matcher.groupCount() >= contentGroupIndex) {
                        val classContent = matcher.group(contentGroupIndex)
                        
                        // Only fold if content is long enough
                        if (classContent != null && classContent.length >= MIN_FOLD_LENGTH) {
                            val fullMatch = matcher.group(0)
                            val contentStart = fullMatch.indexOf(classContent)
                            
                            if (contentStart >= 0) {
                                val elementStart = element.textRange.startOffset
                                val classStart = elementStart + matcher.start() + contentStart
                                val classEnd = classStart + classContent.length
                                
                                val descriptor = FoldingDescriptor(
                                    element.node,
                                    TextRange(classStart, classEnd),
                                    FoldingGroup.newGroup(TAILWIND_GROUP_ID),
                                    " Tailwind classes... "
                                )
                                descriptors.add(descriptor)
                                logger.debug("Added JSX pattern folding descriptor")
                            }
                        }
                    }
                }
            }

            PsiTreeUtil.processElements(element) { currentElement ->
                // Skip XmlAttribute elements since we've already processed them
                if (currentElement !is XmlAttribute && currentElement !is XmlAttributeValue && currentElement.textLength >= MIN_FOLD_LENGTH) {
                    // 1. Template literals: className={`...`}
                    processPattern(
                        currentElement,
                        "(class|className)\\s*=\\s*\\{\\s*`([^`]+)`\\s*\\}",
                        2 // Group index for the class content
                    )
                    
                    // 2. classNames function: className={classNames("...")}
                    processPattern(
                        currentElement,
                        "(class|className)\\s*=\\s*\\{\\s*classNames\\(\\s*[\"']([^\"']+)[\"']",
                        2 // Group index for the class content
                    )
                    
                    // 3. Standard React className: className="..."
                    processPattern(
                        currentElement,
                        "(class|className)\\s*=\\s*[\"']([^\"']+)[\"']",
                        2 // Group index for the class content
                    )
                }
                true
            }
        } catch (e: ProcessCanceledException) {
            // Ignore cancellation exceptions - they occur during IDE write operations
        } catch (e: Exception) {
            logger.debug("Exception processing JSX patterns: ${e.message}", e)
        }
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return " Tailwind classes... "
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
}
