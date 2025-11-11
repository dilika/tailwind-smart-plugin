package com.github.dilika.tailwindsmartplugin.refactoring

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.util.PsiTreeUtil

/**
 * Service de refactoring automatique pour Tailwind CSS
 */
@Service(Service.Level.PROJECT)
class TailwindRefactoringService(private val project: Project) {
    
    private val logger = Logger.getInstance(TailwindRefactoringService::class.java)
    
    companion object {
        fun getInstance(project: Project): TailwindRefactoringService {
            return project.getService(TailwindRefactoringService::class.java)
        }
    }
    
    /**
     * Consolide les classes redondantes
     * Ex: "p-4 px-2" -> "px-2 py-4" (px-2 remplace p-4 pour l'axe horizontal)
     */
    fun consolidateClasses(classes: List<String>): List<String> {
        val consolidated = mutableListOf<String>()
        val processed = mutableSetOf<String>()
        
        // Grouper par catégorie
        val paddingClasses = classes.filter { it.startsWith("p") }
        val marginClasses = classes.filter { it.startsWith("m") }
        val otherClasses = classes.filter { !it.startsWith("p") && !it.startsWith("m") }
        
        // Consolider padding
        if (paddingClasses.isNotEmpty()) {
            val generalPadding = paddingClasses.find { it.matches(Regex("^p-\\w+$")) }
            val specificPadding = paddingClasses.filter { 
                it.startsWith("px-") || it.startsWith("py-") || 
                it.startsWith("pt-") || it.startsWith("pr-") || 
                it.startsWith("pb-") || it.startsWith("pl-")
            }
            
            if (generalPadding != null && specificPadding.isNotEmpty()) {
                // Garder seulement les padding spécifiques
                consolidated.addAll(specificPadding)
            } else {
                consolidated.addAll(paddingClasses)
            }
        }
        
        // Consolider margin
        if (marginClasses.isNotEmpty()) {
            val generalMargin = marginClasses.find { it.matches(Regex("^m-\\w+$")) }
            val specificMargin = marginClasses.filter { 
                it.startsWith("mx-") || it.startsWith("my-") || 
                it.startsWith("mt-") || it.startsWith("mr-") || 
                it.startsWith("mb-") || it.startsWith("ml-")
            }
            
            if (generalMargin != null && specificMargin.isNotEmpty()) {
                consolidated.addAll(specificMargin)
            } else {
                consolidated.addAll(marginClasses)
            }
        }
        
        consolidated.addAll(otherClasses)
        
        return consolidated.distinct()
    }
    
    /**
     * Extrait les classes répétitives en suggestions de composants
     */
    fun extractComponentPattern(classes: List<String>, elementName: String): ComponentPattern? {
        if (classes.size < 3) return null
        
        // Détecter des patterns communs
        val hasButtonPattern = classes.contains("px-4") && 
                              classes.contains("py-2") && 
                              (classes.contains("bg-blue-500") || classes.contains("bg-primary"))
        
        if (hasButtonPattern && elementName == "button") {
            return ComponentPattern(
                name = "Button",
                suggestedClasses = "px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600",
                description = "Standard button pattern"
            )
        }
        
        val hasCardPattern = classes.contains("bg-white") && 
                            classes.contains("rounded-lg") && 
                            classes.contains("shadow")
        
        if (hasCardPattern) {
            return ComponentPattern(
                name = "Card",
                suggestedClasses = "bg-white rounded-lg shadow-md p-6",
                description = "Standard card pattern"
            )
        }
        
        return null
    }
}

/**
 * Pattern de composant suggéré
 */
data class ComponentPattern(
    val name: String,
    val suggestedClasses: String,
    val description: String
)

