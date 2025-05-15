package com.github.dilika.tailwindsmartplugin.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.XmlPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.components.service
import com.intellij.util.ProcessingContext
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.completion.CompletionUtilCore
import com.github.dilika.tailwindsmartplugin.services.TailwindConfigAnalyzer
import com.github.dilika.tailwindsmartplugin.jit.TailwindJitSupport
import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils
import com.github.dilika.tailwindsmartplugin.utils.TailwindIconRegistry

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
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        try {
            // Get the project from the parameters
            val project = parameters.originalFile.project
            
            // Get project services when needed - this is the key part that fixes the issue
            val configAnalyzer = project.service<TailwindConfigAnalyzer>()
            val jitSupport = project.service<TailwindJitSupport>()
            
            // Extract the prefix from the current position
            val position = parameters.position
            val prefix = extractPrefix(parameters)
            
            logger.info("[Tailwind] Extracted prefix: '$prefix'")
            
            // Skip if prefix is empty or doesn't look like a Tailwind class
            if (prefix.isEmpty() || prefix.startsWith(".") || prefix.startsWith("#")) {
                logger.info("[Tailwind] Skipping completion for prefix: '$prefix'")
                return
            }
            
            // Get Tailwind classes and add them to completion results
            val classes = TailwindUtils.getTailwindClasses(project)
            logger.info("[Tailwind] Found ${classes.size} Tailwind classes")
            
            val matchingClasses = classes.filter { cls ->
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
                    val icon = TailwindIconRegistry.getIconForClass(cls)
                    val element = LookupElementBuilder.create(cls)
                        .withPresentableText(cls)
                        .withTypeText("Tailwind CSS")
                        .withIcon(icon)
                    
                    result.addElement(PrioritizedLookupElement.withPriority(element, 100.0))
                } catch (e: Exception) {
                    logger.error("[Tailwind] Error creating lookup element for $cls: ${e.message}")
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
}
