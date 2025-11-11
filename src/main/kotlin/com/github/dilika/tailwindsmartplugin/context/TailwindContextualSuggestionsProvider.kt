package com.github.dilika.tailwindsmartplugin.context

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag

/**
 * Fournisseur de suggestions contextuelles intelligentes
 */
class TailwindContextualSuggestionsProvider {
    
    private val logger = Logger.getInstance(TailwindContextualSuggestionsProvider::class.java)
    private val contextAnalyzer = TailwindContextAnalyzer()
    
    /**
     * Génère des suggestions contextuelles basées sur l'élément courant
     */
    fun generateContextualSuggestions(
        parameters: CompletionParameters,
        result: CompletionResultSet
    ) {
        try {
            val position = parameters.position
            val context = contextAnalyzer.analyzeElementContext(position)
            
            if (context.confidence > 0.3f) {
                addContextualSuggestions(context, result)
            }
        } catch (e: Exception) {
            logger.warn("Error generating contextual suggestions", e)
        }
    }
    
    /**
     * Ajoute les suggestions contextuelles au résultat
     */
    private fun addContextualSuggestions(
        context: ComponentContext,
        result: CompletionResultSet
    ) {
        context.suggestedPatterns.forEach { pattern ->
            val classes = pattern.split(" ")
            classes.forEach { className ->
                val lookupElement = LookupElementBuilder.create(className)
                    .withTypeText("Contextual: ${context.elementType}")
                    .withTailText(" (${context.confidence.takeIf { it > 0.5f }?.let { "High confidence" } ?: "Medium confidence"})")
                    .withIcon(com.intellij.icons.AllIcons.General.Information)
                
                result.addElement(lookupElement)
            }
        }
    }
    
    /**
     * Suggère des classes pour un élément spécifique
     */
    fun suggestClassesForElement(element: PsiElement): List<TailwindSuggestion> {
        val context = contextAnalyzer.analyzeElementContext(element)
        return context.suggestedPatterns.map { pattern ->
            TailwindSuggestion(
                classes = pattern.split(" "),
                context = context.elementType,
                confidence = context.confidence,
                description = "Suggested for ${context.elementType} component"
            )
        }
    }
    
    /**
     * Détecte les patterns de design dans le code
     */
    fun detectDesignPatterns(code: String): List<DesignPattern> {
        val patterns = mutableListOf<DesignPattern>()
        
        // Détection de patterns communs
        when {
            code.contains("flex") && code.contains("items-center") -> {
                patterns.add(DesignPattern(
                    name = "Centered Flex Container",
                    description = "A flex container with centered items",
                    classes = listOf("flex", "items-center", "justify-center")
                ))
            }
            code.contains("grid") && code.contains("gap") -> {
                patterns.add(DesignPattern(
                    name = "Grid Layout",
                    description = "A responsive grid layout",
                    classes = listOf("grid", "grid-cols-3", "gap-4")
                ))
            }
            code.contains("shadow") && code.contains("rounded") -> {
                patterns.add(DesignPattern(
                    name = "Card Component",
                    description = "A card-like component with shadow and rounded corners",
                    classes = listOf("bg-white", "shadow-md", "rounded-lg", "p-6")
                ))
            }
            code.contains("hover:") -> {
                patterns.add(DesignPattern(
                    name = "Interactive Element",
                    description = "An element with hover effects",
                    classes = listOf("hover:bg-blue-500", "transition-colors")
                ))
            }
        }
        
        return patterns
    }
}

/**
 * Suggestion Tailwind contextuelle
 */
data class TailwindSuggestion(
    val classes: List<String>,
    val context: String,
    val confidence: Float,
    val description: String
)

/**
 * Pattern de design détecté
 */
data class DesignPattern(
    val name: String,
    val description: String,
    val classes: List<String>
)