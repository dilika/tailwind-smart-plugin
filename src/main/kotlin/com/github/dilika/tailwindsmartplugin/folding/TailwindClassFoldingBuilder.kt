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

/**
 * Folding builder pour les attributs class/className de Tailwind CSS.
 * Fournit une fonctionnalité de pliage pour masquer les longues listes de classes Tailwind.
 */
class TailwindClassFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        
        // Trouver tous les attributs XML dans l'arbre PSI
        val xmlAttributes = PsiTreeUtil.findChildrenOfType(root, XmlAttribute::class.java)
        
        for (attribute in xmlAttributes) {
            // Vérifier si c'est un attribut class ou className
            val attributeName = attribute.name
            if (attributeName == "class" || attributeName == "className") {
                val attributeValue = attribute.valueElement ?: continue
                val valueText = attributeValue.value
                
                // Ne pas plier les attributs courts ou vides
                if (valueText.isEmpty() || valueText.length < 15) continue
                
                // Calculer les plages pour le pliage
                val valueRange = attributeValue.textRange
                val startOffset = valueRange.startOffset + 1 // +1 pour sauter le guillemet ouvrant
                val endOffset = valueRange.endOffset - 1 // -1 pour exclure le guillemet fermant
                
                // Ne pas plier si la plage est invalide
                if (startOffset >= endOffset) continue
                
                // Créer un descripteur de pliage
                val group = FoldingGroup.newGroup("tailwind-classes")
                descriptors.add(
                    FoldingDescriptor(
                        attributeValue.node,
                        TextRange(startOffset, endOffset),
                        group,
                        "..."
                    )
                )
            }
        }
        
        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return "..." // Texte affiché quand l'attribut est plié
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        // Toujours retourner true pour plier tous les attributs class/className dès l'ouverture du fichier
        return true
    }
}
