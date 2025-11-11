package com.github.dilika.tailwindsmartplugin.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.XmlPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.util.ProcessingContext
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.completion.CompletionUtilCore
import com.github.dilika.tailwindsmartplugin.services.TailwindConfigAnalyzer
import com.github.dilika.tailwindsmartplugin.jit.TailwindJitSupport
import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils
import com.github.dilika.tailwindsmartplugin.utils.TailwindIconRegistry
import com.github.dilika.tailwindsmartplugin.icons.TailwindPremiumIconRegistry
import com.github.dilika.tailwindsmartplugin.context.TailwindContextAnalyzer
import com.github.dilika.tailwindsmartplugin.context.TailwindContextualSuggestionsProvider
import com.github.dilika.tailwindsmartplugin.context.TailwindPreferenceLearningService
import com.github.dilika.tailwindsmartplugin.context.TailwindPatternLibrary
import com.github.dilika.tailwindsmartplugin.preview.TailwindVisualPreviewService

/**
 * CompletionContributor for Tailwind CSS classes.
 * This class registers the TailwindCompletionProvider to provide completion
 * suggestions for Tailwind CSS classes in appropriate contexts.
 */
class TailwindCompletionContributor : CompletionContributor(), DumbAware {
    private val logger = Logger.getInstance(TailwindCompletionContributor::class.java)

    init {
        logger.info("[Tailwind] Initializing Tailwind completion contributor")

        // Pattern for targeting HTML/XML class attributes
        val classAttributePattern = XmlPatterns.xmlAttributeValue()
            .withParent(
                XmlPatterns.xmlAttributeValue().withName(
                    StandardPatterns.string().oneOf("class", "className")
                )
            )

        logger.info("[Tailwind] HTML/XML class attribute pattern registered")

        // Generic pattern for all PSI (to use for files JSX/TSX)
        val genericPattern = PlatformPatterns.psiElement()

        logger.info("Generic patterns saved")

        // Register completion provider that gets project services when needed
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(classAttributePattern, genericPattern),
            ProjectAwareTailwindCompletionProvider()
        )

        logger.info("[Tailwind] Tailwind completion provider registered for class attributes")
    }
}

/**
 * Completion provider that obtains project-level services correctly when needed.
 * Cette classe implémente une version simplifiée de TailwindCompletionProvider qui
 * obtient les services au niveau du projet au bon moment.
 */
class ProjectAwareTailwindCompletionProvider : CompletionProvider<CompletionParameters>() {
    private val logger = Logger.getInstance(ProjectAwareTailwindCompletionProvider::class.java)
    
    // Common Tailwind prefixes that should trigger completion even with short input
    private val commonPrefixes = listOf(
        "bg-", "text-", "border-", "p-", "m-", "flex", "grid-", "w-", "h-", 
        "rounded", "shadow", "font-", "align-", "justify-", "items-", "content-", 
        "space-", "gap-", "opacity-", "z-", "outline-", "ring-"
    )
    
    // Services pour l'intelligence contextuelle
    private var contextAnalyzer: TailwindContextAnalyzer? = null
    private var contextualSuggestionsProvider: TailwindContextualSuggestionsProvider? = null
    private var preferenceLearningService: TailwindPreferenceLearningService? = null
    private var patternLibrary: TailwindPatternLibrary? = null
    private var visualPreviewService: TailwindVisualPreviewService? = null
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        try {
            // Get the project from the parameters
            val project = parameters.originalFile.project
            
            // Get project services when needed - this is the key part that fixes the issue
            val configAnalyzer = project.getService(TailwindConfigAnalyzer::class.java)
            val jitSupport = project.getService(TailwindJitSupport::class.java)
            
            // Initialize contextual services
            initializeContextualServices(project)
            
            // Extract the prefix from the current position
            val position = parameters.position
            val prefix = extractPrefix(parameters)
            
            logger.info("[Tailwind] Extracted prefix: '$prefix'")
            
            // Skip if prefix is empty or doesn't look like a Tailwind class
            if (prefix.isEmpty() || prefix.startsWith(".") || prefix.startsWith("#")) {
                logger.info("[Tailwind] Skipping completion for prefix: '$prefix'")
                return
            }
            
                // Add contextual suggestions first (highest priority)
                // addContextualSuggestions(parameters, context, result)
            
            // Add contextual suggestions first (highest priority)
            addContextualSuggestions(parameters, context, result)
            
            // Get Tailwind classes and add them to completion results
            val classes = TailwindUtils.getTailwindClasses(project)
            logger.info("[Tailwind] Found ${classes.size} Tailwind classes")
            
            // Enregistrer l'utilisation dans l'historique si une classe est sélectionnée
            // (sera appelé lors de la sélection)
            
            // Enhance suggestions with user preferences and history
            val historyService = com.github.dilika.tailwindsmartplugin.context.ClassHistoryService.getInstance(project)
            val enhancedClasses = enhanceSuggestionsWithHistory(classes, prefix, historyService, position)
            
            val matchingClasses = enhancedClasses.filter { cls ->
                val matches = cls.startsWith(prefix, ignoreCase = true) ||
                              commonPrefixes.any { prefix.startsWith(it, ignoreCase = true) && cls.startsWith(it, ignoreCase = true) }
                if (matches) {
                    logger.debug("[Tailwind] Class matches prefix: $cls")
                }
                matches
            }
            
            logger.info("[Tailwind] Found ${matchingClasses.size} matching classes for prefix '$prefix'")
            
            matchingClasses.forEach { cls ->
                try {
                    // Utiliser les icônes premium avec fallback
                    val icon = try {
                        TailwindPremiumIconRegistry.getPremiumIcon(cls)
                    } catch (e: Exception) {
                        logger.debug("[Tailwind] Error getting premium icon for $cls: ${e.message}")
                        // Fallback vers l'ancien système d'icônes
                        try {
                            TailwindIconRegistry.getIconForClass(cls)
                        } catch (e2: Exception) {
                            logger.debug("[Tailwind] Error getting fallback icon for $cls: ${e2.message}")
                            null // Pas d'icône si tout échoue
                        }
                    }
                    
                    // Déterminer la priorité basée sur la popularité et le contexte
                    val priority = try {
                        calculatePremiumPriority(cls, prefix, position)
                    } catch (e: Exception) {
                        logger.debug("[Tailwind] Error calculating priority for $cls: ${e.message}")
                        100.0 // Priorité par défaut
                    }
                    
                    val elementBuilder = LookupElementBuilder.create(cls)
                        .withPresentableText(cls)
                        .withTypeText("Tailwind CSS")
                    
                    // Ajouter l'icône seulement si disponible
                    val element = if (icon != null) {
                        elementBuilder.withIcon(icon)
                    } else {
                        elementBuilder
                    }
                    
                    val finalElement = element.withInsertHandler { insertionContext, lookupElement -> 
                        try {
                            trackSuggestionUsage(cls, position, true)
                            // Enregistrer l'utilisation dans l'historique (protégé)
                            try {
                                historyService.recordClassUsage(lookupElement.lookupString)
                            } catch (e: Exception) {
                                logger.debug("[Tailwind] Error recording class usage: ${e.message}")
                            }
                        } catch (e: Exception) {
                            logger.debug("[Tailwind] Error in insert handler: ${e.message}")
                        }
                    }
                    
                    // Ajouter le preview visuel si disponible
                    visualPreviewService?.let { previewService ->
                        try {
                            val preview = previewService.generatePreview(listOf(cls))
                            finalElement.withTailText(" ${preview.description}", true)
                        } catch (e: Exception) {
                            logger.debug("[Tailwind] Error generating preview for $cls: ${e.message}")
                        }
                    }
                    
                    // Ajouter avec priorité premium
                    result.addElement(PrioritizedLookupElement.withPriority(finalElement, priority))
                } catch (e: Exception) {
                    // Log mais ne pas bloquer - essayer de créer un élément simple
                    logger.debug("[Tailwind] Error creating lookup element for $cls: ${e.message}", e)
                    try {
                        val simpleElement = LookupElementBuilder.create(cls)
                            .withPresentableText(cls)
                            .withTypeText("Tailwind CSS")
                        result.addElement(PrioritizedLookupElement.withPriority(simpleElement, 50.0))
                    } catch (e2: Exception) {
                        // Si même ça échoue, on ignore cette classe
                        logger.warn("[Tailwind] Failed to create even simple element for $cls: ${e2.message}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error providing Tailwind completions", e)
        }
    }
    
    /**
     * Extrait le préfixe de texte à l'emplacement du curseur
     * Plus sophistiqué que la simple extraction de texte de PsiElement
     */
    private fun extractPrefix(parameters: CompletionParameters): String {
        try {
            // Obtenez le texte du document et l'offset du curseur
            val document = parameters.editor.document
            val caretOffset = parameters.offset
            val text = document.text
            
            if (caretOffset <= 0 || caretOffset > text.length) {
                return ""
            }
            
            // Trouve le début du mot actuel (espace, guillemet, etc.)
            var startOffset = caretOffset
            while (startOffset > 0 && isPartOfTailwindClass(text[startOffset - 1])) {
                startOffset--
            }
            
            // Extrait le texte entre le début du mot et la position du curseur
            if (startOffset < caretOffset) {
                return text.substring(startOffset, caretOffset)
            }
            
            return ""
        } catch (e: Exception) {
            logger.error("[Tailwind] Error extracting prefix: ${e.message}")
            return ""
        }
    }
    
    /**
     * Vérifie si un caractère fait partie d'un nom de classe Tailwind
     */
    private fun isPartOfTailwindClass(char: Char): Boolean {
        // Les classes Tailwind peuvent contenir des lettres, chiffres, tirets, deux-points, crochets, etc.
        return char.isLetterOrDigit() || char == '-' || char == ':' || char == '[' || char == ']' || char == '.' || char == '_'
    }
    
    /**
     * Initialise les services contextuels
     */
    private fun initializeContextualServices(project: com.intellij.openapi.project.Project) {
        try {
            contextAnalyzer = TailwindContextAnalyzer()
            contextualSuggestionsProvider = TailwindContextualSuggestionsProvider()
            preferenceLearningService = project.getService(TailwindPreferenceLearningService::class.java)
            patternLibrary = TailwindPatternLibrary()
            visualPreviewService = TailwindVisualPreviewService()
            logger.info("[Tailwind] Contextual services initialized")
        } catch (e: Exception) {
            logger.warn("[Tailwind] Error initializing contextual services: ${e.message}")
        }
    }

    /**
     * Ajoute les suggestions contextuelles
     */
    private fun addContextualSuggestions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        try {
            contextualSuggestionsProvider?.generateContextualSuggestions(parameters, result)
        } catch (e: Exception) {
            logger.warn("[Tailwind] Error adding contextual suggestions: ${e.message}")
        }
    }

    /**
     * Améliore les suggestions avec l'historique et les préférences
     */
    private fun enhanceSuggestionsWithHistory(
        suggestions: List<String>,
        prefix: String,
        historyService: com.github.dilika.tailwindsmartplugin.context.ClassHistoryService,
        element: com.intellij.psi.PsiElement
    ): List<String> {
        // Obtenir les suggestions de l'historique
        val historicalSuggestions = historyService.getHistoricalSuggestions(prefix, 5)
        
        // Combiner avec les suggestions normales, en priorisant l'historique
        val enhanced = mutableListOf<String>()
        enhanced.addAll(historicalSuggestions)
        enhanced.addAll(suggestions.filter { !enhanced.contains(it) })
        
        return enhanceSuggestionsWithPreferences(enhanced, element)
    }
    
    /**
     * Améliore les suggestions avec les préférences utilisateur
     */
    private fun enhanceSuggestionsWithPreferences(
        suggestions: List<String>,
        element: com.intellij.psi.PsiElement
    ): List<String> {
        try {
            val context = contextAnalyzer?.analyzeElementContext(element)
            if (context != null && context.confidence > 0.3f) {
                val personalizedSuggestions = preferenceLearningService?.generatePersonalizedSuggestions(context.elementType) ?: emptyList()
                val mostUsed = preferenceLearningService?.getMostUsedClasses(5) ?: emptyList()
                
                // Combine personalized suggestions with most used classes
                val enhanced = (personalizedSuggestions + mostUsed + suggestions).distinct()
                return enhanced
            }
        } catch (e: Exception) {
            logger.warn("[Tailwind] Error enhancing suggestions with preferences: ${e.message}")
        }
        return suggestions
    }

    /**
     * Enregistre l'utilisation d'une suggestion
     */
    private fun trackSuggestionUsage(
        className: String,
        element: com.intellij.psi.PsiElement,
        wasAccepted: Boolean
    ) {
        try {
            val context = contextAnalyzer?.analyzeElementContext(element)
            if (context != null) {
                preferenceLearningService?.trackClassUsage(className, context.elementType)
                logger.debug("[Tailwind] Tracked usage: $className in context: ${context.elementType}")
            }
        } catch (e: Exception) {
            logger.warn("[Tailwind] Error tracking suggestion usage: ${e.message}")
        }
    }
    
    /**
     * Calcule la priorité premium pour une classe Tailwind
     * Basé sur la popularité, le contexte et la correspondance avec le préfixe
     */
    private fun calculatePremiumPriority(className: String, prefix: String, element: com.intellij.psi.PsiElement): Double {
        var priority = 100.0
        
        // Boost pour correspondance exacte du préfixe
        if (className.startsWith(prefix, ignoreCase = true)) {
            priority += 50.0
        }
        
        // Boost pour classes populaires
        val popularClasses = listOf(
            "flex", "grid", "bg-", "text-", "p-", "m-", "rounded", "shadow",
            "hover:", "focus:", "md:", "lg:", "xl:"
        )
        if (popularClasses.any { className.contains(it) }) {
            priority += 20.0
        }
        
        // Boost pour classes de couleur communes
        val commonColors = listOf("blue-500", "gray-500", "red-500", "green-500", "white", "black")
        if (commonColors.any { className.contains(it) }) {
            priority += 15.0
        }
        
        // Boost basé sur le contexte
        try {
            val context = contextAnalyzer?.analyzeElementContext(element)
            if (context != null && context.confidence > 0.5f) {
                priority += 30.0
            }
        } catch (e: Exception) {
            // Ignore
        }
        
        return priority
    }
}
