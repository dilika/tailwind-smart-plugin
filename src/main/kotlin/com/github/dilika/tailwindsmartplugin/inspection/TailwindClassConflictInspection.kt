package com.github.dilika.tailwindsmartplugin.inspection

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.xml.*
import com.intellij.psi.util.PsiTreeUtil
import com.github.dilika.tailwindsmartplugin.validation.ClassConflictDetector
import com.github.dilika.tailwindsmartplugin.validation.ClassConflict

/**
 * Inspection pour détecter les conflits entre classes Tailwind CSS
 */
class TailwindClassConflictInspection : LocalInspectionTool() {
    
    private val conflictDetector = ClassConflictDetector()
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                // Visiter récursivement tous les enfants
                element.acceptChildren(this)
                
                // Visiter uniquement les XmlAttributeValue
                if (element !is XmlAttributeValue) return
                
                val value = element
                
                // Vérifier si c'est un attribut class ou className
                val attribute = PsiTreeUtil.getParentOfType(value, XmlAttribute::class.java)
                if (attribute == null) return
                
                val attributeName = attribute.name.lowercase()
                if (attributeName != "class" && attributeName != "classname") return
                
                // Obtenir la valeur de l'attribut
                val classString = value.value ?: return
                
                if (classString.isBlank()) return
                
                // Extraire les classes individuelles
                val classes = classString.split(Regex("\\s+")).filter { it.isNotBlank() }
                
                if (classes.size < 2) return // Pas de conflit possible avec une seule classe
                
                // Détecter les conflits
                val conflicts = conflictDetector.detectConflicts(classes)
                
                // Enregistrer les problèmes pour chaque conflit
                conflicts.forEach { conflict ->
                    val problemDescription = buildString {
                        append("Tailwind: ")
                        append(conflict.message)
                        append(". ${conflict.suggestion}")
                    }
                    
                    holder.registerProblem(
                        value,
                        problemDescription,
                        ProblemHighlightType.WEAK_WARNING,
                        TailwindConflictQuickFix(conflict)
                    )
                }
            }
        }
    }
    
    override fun getDisplayName(): String {
        return "Tailwind CSS class conflicts"
    }
    
    override fun getGroupDisplayName(): String {
        return "Tailwind CSS"
    }
    
    override fun getShortName(): String {
        return "TailwindClassConflict"
    }
    
    override fun getStaticDescription(): String? {
        return "Detects conflicts between Tailwind CSS classes (e.g., p-4 and px-2)."
    }
}

/**
 * Quick fix pour résoudre les conflits de classes
 */
class TailwindConflictQuickFix(
    private val conflict: ClassConflict
) : LocalQuickFix {
    
    override fun getName(): String {
        return when (conflict.type) {
            com.github.dilika.tailwindsmartplugin.validation.ConflictType.PADDING -> "Remove conflicting padding classes"
            com.github.dilika.tailwindsmartplugin.validation.ConflictType.MARGIN -> "Remove conflicting margin classes"
            com.github.dilika.tailwindsmartplugin.validation.ConflictType.DISPLAY -> "Remove conflicting display classes"
            com.github.dilika.tailwindsmartplugin.validation.ConflictType.POSITION -> "Remove conflicting position classes"
            com.github.dilika.tailwindsmartplugin.validation.ConflictType.SIZING -> "Remove conflicting sizing classes"
        }
    }
    
    override fun getFamilyName(): String {
        return "Tailwind CSS"
    }
    
    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement as? XmlAttributeValue ?: return
        val attribute = PsiTreeUtil.getParentOfType(element, XmlAttribute::class.java) ?: return
        
        val currentValue = element.value ?: return
        val classes = currentValue.split(Regex("\\s+")).toMutableList()
        
        // Retirer les classes en conflit
        conflict.conflictingClasses.forEach { conflictingClass ->
            classes.remove(conflictingClass)
        }
        
        // Mettre à jour la valeur de l'attribut
        val newValue = classes.joinToString(" ")
        attribute.setValue(newValue)
    }
}

