package com.github.dilika.tailwindsmartplugin.inspection

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.xml.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.openapi.util.TextRange
import com.github.dilika.tailwindsmartplugin.validation.TailwindValidationService
import com.github.dilika.tailwindsmartplugin.validation.ValidationResult

/**
 * Inspection IntelliJ pour détecter les classes Tailwind invalides
 * Version améliorée avec highlights précis et quick fixes intelligents
 */
class TailwindInvalidClassInspection : LocalInspectionTool() {
    
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
                
                // Obtenir le service de validation
                val project = value.project
                val validationService = try {
                    TailwindValidationService.getInstance(project)
                } catch (e: Exception) {
                    // Service pas encore initialisé, ignorer
                    return
                }
                
                // Extraire les classes individuelles avec leurs positions
                val classesWithPositions = extractClassesWithPositions(classString, value)
                
                // Valider chaque classe individuellement avec highlight précis
                classesWithPositions.forEach { (className, startOffset, endOffset) ->
                    // Ignorer les classes vides ou qui commencent par . ou #
                    if (className.isBlank() || className.startsWith(".") || className.startsWith("#")) {
                        return@forEach
                    }
                    
                    val result = validationService.validateClass(className)
                    
                    if (result is ValidationResult.Invalid) {
                        // Créer le problème avec highlight précis sur la classe invalide
                        val problemDescription = buildString {
                            append("⚠️ Invalid Tailwind class: ")
                            append(className)
                            append(" - ")
                            append(result.reason)
                            if (result.suggestions.isNotEmpty()) {
                                append("\n💡 Suggestions: ${result.suggestions.take(3).joinToString(", ")}")
                            }
                        }
                        
                        // Calculer le TextRange pour la classe spécifique
                        val textRange = TextRange.create(startOffset, endOffset)
                        
                        // Créer les quick fixes pour chaque suggestion
                        val quickFixes = result.suggestions.take(5).map { suggestion ->
                            TailwindClassQuickFix(className, suggestion, textRange)
                        }.toTypedArray()
                        
                        // Enregistrer le problème avec highlight précis
                        // Signature: registerProblem(element, textRange, description, *quickFixes)
                        holder.registerProblem(
                            value,
                            textRange,
                            problemDescription,
                            *quickFixes
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Extrait les classes avec leurs positions dans l'attribut
     * Les offsets sont relatifs à l'élément entier (XmlAttributeValue), incluant les guillemets
     */
    private fun extractClassesWithPositions(classString: String, attributeValue: XmlAttributeValue): List<Triple<String, Int, Int>> {
        val classes = mutableListOf<Triple<String, Int, Int>>()
        var currentIndex = 0
        
        // Obtenir les TextRanges
        val elementTextRange = attributeValue.textRange  // Plage de l'élément entier (avec guillemets)
        val valueTextRange = attributeValue.valueTextRange  // Plage du contenu (sans guillemets)
        
        // Calculer l'offset du début du contenu relatif à l'élément entier
        // C'est la différence entre le début du contenu et le début de l'élément
        val contentStartInElement = valueTextRange.startOffset - elementTextRange.startOffset
        
        // Parser les classes en tenant compte des espaces
        val parts = classString.split(Regex("\\s+")).filter { it.isNotBlank() }
        
        parts.forEach { className ->
            val classNameStartInString = classString.indexOf(className, currentIndex)
            if (classNameStartInString != -1) {
                val classNameEndInString = classNameStartInString + className.length
                
                // Calculer les offsets relatifs à l'élément entier
                // Position dans classString + offset du début du contenu dans l'élément
                val startOffsetRelative = contentStartInElement + classNameStartInString
                val endOffsetRelative = contentStartInElement + classNameEndInString
                
                // Vérifier que les offsets sont dans les limites de l'élément
                val elementLength = elementTextRange.length
                if (startOffsetRelative >= 0 && endOffsetRelative <= elementLength && 
                    startOffsetRelative < endOffsetRelative) {
                    classes.add(Triple(
                        className,
                        startOffsetRelative,  // Offset relatif à l'élément entier
                        endOffsetRelative     // Offset relatif à l'élément entier
                    ))
                }
                
                currentIndex = classNameEndInString
            }
        }
        
        return classes
    }
    
    override fun getDisplayName(): String {
        return "Invalid Tailwind CSS class"
    }
    
    override fun getGroupDisplayName(): String {
        return "Tailwind CSS"
    }
    
    override fun getShortName(): String {
        return "TailwindInvalidClass"
    }
    
    override fun isEnabledByDefault(): Boolean = true
}

/**
 * Quick fix pour corriger une classe Tailwind invalide
 * Version améliorée avec remplacement précis
 */
class TailwindClassQuickFix(
    private val invalidClass: String,
    private val suggestion: String,
    private val textRange: TextRange? = null
) : LocalQuickFix {
    
    override fun getName(): String {
        return "✨ Replace '$invalidClass' with '$suggestion'"
    }
    
    override fun getFamilyName(): String {
        return "Tailwind CSS Quick Fixes"
    }
    
    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement as? XmlAttributeValue ?: return
        val attribute = PsiTreeUtil.getParentOfType(element, XmlAttribute::class.java) ?: return
        
        val currentValue = element.value ?: return
        
        // Remplacer la classe invalide par la suggestion
        val newValue = if (textRange != null) {
            // Le textRange est relatif à l'élément entier (avec guillemets)
            // Nous devons le convertir en position dans la valeur (sans guillemets)
            val elementTextRange = element.textRange
            val valueTextRange = element.valueTextRange
            val contentStartInElement = valueTextRange.startOffset - elementTextRange.startOffset
            
            // Convertir les offsets relatifs à l'élément en offsets relatifs à la valeur
            val startInValue = textRange.startOffset - contentStartInElement
            val endInValue = textRange.endOffset - contentStartInElement
            
            // Vérifier que les offsets sont valides
            if (startInValue >= 0 && endInValue <= currentValue.length && startInValue < endInValue) {
                currentValue.substring(0, startInValue) + suggestion + currentValue.substring(endInValue)
            } else {
                // Fallback: remplacement simple
                currentValue.replace(invalidClass, suggestion)
            }
        } else {
            // Remplacement simple
            currentValue.replace(invalidClass, suggestion)
        }
        
        // Nettoyer les espaces multiples
        val cleanedValue = newValue.replace(Regex("\\s+"), " ").trim()
        
        // Mettre à jour la valeur de l'attribut
        com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) {
            attribute.setValue(cleanedValue)
        }
    }
}

