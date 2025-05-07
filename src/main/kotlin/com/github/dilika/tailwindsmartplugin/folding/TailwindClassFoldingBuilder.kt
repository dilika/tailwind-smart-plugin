package com.github.dilika.tailwindsmartplugin.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import java.util.regex.Pattern
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.openapi.diagnostic.Logger

/**
 * Folding builder for Tailwind CSS class/className attributes.
 * Provides folding functionality to hide long lists of Tailwind classes.
 */
class TailwindClassFoldingBuilder : FoldingBuilderEx(), DumbAware {
    private val logger = Logger.getInstance(TailwindClassFoldingBuilder::class.java)

    // Pattern to identify class and className attributes in JSX and other formats
    private val classAttributePattern = Pattern.compile("(class|className)\\s*=\\s*['\"]([^'\"]*)['\"]")

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        logger.debug("BuildFoldRegions for root: ${root.textRange}")
        // Fold XML/HTML attributes
        processXmlAttributes(root, descriptors)
        // Fold class patterns in text (JSX, TSX, etc.)
        processTextContent(root, descriptors)
        logger.debug("Found ${descriptors.size} folding descriptors.")
        return descriptors.toTypedArray()
    }

    private fun processXmlAttributes(root: PsiElement, descriptors: MutableList<FoldingDescriptor>) {
        val xmlAttributes = PsiTreeUtil.findChildrenOfType(root, XmlAttribute::class.java)
        logger.debug("Processing ${xmlAttributes.size} XML attributes.")
        for (attribute in xmlAttributes) {
            if (attribute.name == "class" || attribute.name == "className") {
                logger.debug("Found attribute: ${attribute.name} with value: ${attribute.value}")
                attribute.valueElement?.let { valueElement ->
                    if (valueElement.value.length > 15) { // Threshold for folding
                        logger.debug("Attempting to add folding for: ${attribute.name}='${valueElement.value.substring(0, 15)}...'")
                        addFoldingDescriptorForValue(valueElement, descriptors, "tailwind-classes-xml")
                    } else {
                        logger.debug("Skipping folding for ${attribute.name}, value too short: ${valueElement.value.length}")
                    }
                } ?: logger.debug("Attribute ${attribute.name} has no valueElement.")
            }
        }
    }
    
    private fun processTextContent(root: PsiElement, descriptors: MutableList<FoldingDescriptor>) {
        // Process all text elements that might contain class definitions
        PsiTreeUtil.processElements(root) { element ->
            if (element is LeafPsiElement) {
                val text = element.text
                val matcher = classAttributePattern.matcher(text)
                
                while (matcher.find()) {
                    val classValue = matcher.group(2)
                    
                    // Don't fold short class values
                    if (classValue.length < 15) continue
                    
                    // Calculate the start and end offsets for the class value
                    val startInText = matcher.start(2)
                    val endInText = matcher.end(2)
                    
                    if (startInText >= 0 && endInText > startInText) {
                        val elementStartOffset = element.textRange.startOffset
                        val startOffset = elementStartOffset + startInText
                        val endOffset = elementStartOffset + endInText
                        
                        // Create a folding descriptor
                        val group = FoldingGroup.newGroup("tailwind-classes-text")
                        descriptors.add(
                            FoldingDescriptor(
                                element.node,
                                TextRange(startOffset, endOffset),
                                group,
                                "..."
                            )
                        )
                        logger.debug("Added folding descriptor for text value: ${classValue.substring(0, Math.min(classValue.length,15))}... Range: $startOffset-$endOffset")
                    }
                }
            }
            true
        }
    }
    
    // Helper to add folding descriptor for the attribute value only
    private fun addFoldingDescriptorForValue(
        attributeValue: XmlAttributeValue,
        descriptors: MutableList<FoldingDescriptor>,
        groupName: String
    ) {
        val valueTextRange = attributeValue.valueTextRange
        
        if (valueTextRange.isEmpty) {
            logger.debug("Skipping folding, valueTextRange is empty for ${attributeValue.parent?.text}")
            return
        }
        
        val group = FoldingGroup.newGroup(groupName)
        descriptors.add(
            FoldingDescriptor(
                attributeValue.node,
                valueTextRange,
                group,
                "..."
            )
        )
        logger.debug("Added folding descriptor for value: ${attributeValue.value.substring(0, Math.min(attributeValue.value.length,15))}... Range: $valueTextRange")
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        // This method is called if FoldingDescriptor.getPlaceholderText() returns null.
        // Since our descriptors always provide "...", this can be simplified.
        // However, to be safe and cover different node types if logic changes:
        val psi = node.psi
        return when (psi) {
            is XmlAttributeValue -> "..." // For class values in XML attributes
            is LeafPsiElement -> "..."    // For class values in text/JSX
            is XmlAttribute -> "${psi.name}=\"...\"" // Fallback if entire attribute were folded
            else -> "..."
        }
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        // Always return true to fold all class/className attributes when the file is opened
        logger.debug("isCollapsedByDefault called for node: ${node.elementType}, returning true")
        return true
    }
}
