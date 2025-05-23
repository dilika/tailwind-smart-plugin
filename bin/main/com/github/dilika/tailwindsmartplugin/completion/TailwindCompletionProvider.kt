package com.github.dilika.tailwindsmartplugin.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.github.dilika.tailwindsmartplugin.util.TailwindCategoryUtils
import com.github.dilika.tailwindsmartplugin.util.TailwindCategoryIcon
import com.github.dilika.tailwindsmartplugin.util.SmartClassGroupUtils
import com.github.dilika.tailwindsmartplugin.utils.ColorIcon
import com.github.dilika.tailwindsmartplugin.utils.TailwindIconRegistry
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.intellij.openapi.project.Project
import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.JBColor
import com.github.dilika.tailwindsmartplugin.services.TailwindConfigAnalyzer
import com.github.dilika.tailwindsmartplugin.jit.TailwindJitSupport

/**
 * Enhanced Tailwind CSS completion provider.
 * Provides intelligent code completion for all Tailwind CSS classes and variants.
 */
class TailwindCompletionProvider(
    private val configAnalyzer: TailwindConfigAnalyzer,
    private val jitSupport: TailwindJitSupport
) : CompletionProvider<CompletionParameters>() {
    private val logger = Logger.getInstance(TailwindCompletionProvider::class.java)
    
    // All Tailwind variants for responsive, state, and other modifiers
    private val tailwindVariants = listOf(
        // Responsive breakpoints
        "sm:", "md:", "lg:", "xl:", "2xl:", "3xl:",
        // Pseudo-class variants
        "hover:", "focus:", "focus-within:", "focus-visible:", "active:", "visited:", 
        "target:", "first:", "last:", "only:", "odd:", "even:", "first-of-type:", 
        "last-of-type:", "only-of-type:", "empty:", "disabled:", "enabled:", "checked:", 
        "indeterminate:", "default:", "required:", "valid:", "invalid:", "in-range:", 
        "out-of-range:", "placeholder-shown:", "autofill:", "read-only:",
        // State variants
        "group-hover:", "group-focus:", "group-active:", "peer-hover:", "peer-focus:", 
        "peer-active:", "peer-checked:", "peer-focus-within:", "peer-focus-visible:",
        // Dark mode and other theme variants  
        "dark:", "light:", "contrast-more:", "contrast-less:", "portrait:", "landscape:",
        "motion-safe:", "motion-reduce:", "print:", "rtl:", "ltr:",
        // Custom media queries
        "supports-[", "container-type-[", "@media-[", "has-[", "where-[",
        // Arbitrary values
        "[&>*]:", "[&:nth-child(3)]:", "[&_p]:", "[&::before]:", "[.parent_&]:",
        // Directional variants (LTR/RTL)
        "rtl:", "ltr:", "dir-", "horizontal-",
        // New Tailwind v4 utilities
        "grid-rows-", "grid-areas-", "grid-area-", "subgrid", "columns-", "column-", "page-break-",
        "text-wrap-", "font-synthesis-", "diagonal-", "writing-", "text-orientation-", "content-visibility-",
        "hint-",
    )
    
    // Comprehensive list of all common Tailwind utility prefixes
    private val commonPrefixes = listOf(
        // Layout
        "w-", "h-", "min-w-", "min-h-", "max-w-", "max-h-", "aspect-", "container", "columns-",
        // Flexbox
        "flex-", "flex", "grow-", "shrink-", "order-", "justify-", "items-", "content-", "self-", "place-",
        // Grid
        "grid-", "grid", "col-", "row-", "auto-", "gap-", 
        // Box Model
        "p-", "px-", "py-", "pt-", "pr-", "pb-", "pl-", "m-", "mx-", "my-", "mt-", "mr-", "mb-", "ml-",
        "space-x-", "space-y-", "space-",
        // Sizing
        "w-", "h-", "size-",
        // Typography
        "text-", "font-", "tracking-", "leading-", "list-", "text-", "indent-", "align-", "whitespace-", "break-",
        "hyphens-", "content-", "line-clamp-", "truncate", "italic", "not-italic", "antialiased", "subpixel-antialiased",
        // Backgrounds
        "bg-", "bg-gradient-", "from-", "to-", "via-", "bg-clip-", "bg-origin-", "bg-repeat-", "bg-size-", "bg-position-",
        "bg-blend-", "bg-attachment-",
        // Borders
        "border-", "border", "rounded-", "outline-", "ring-", "shadow-", "divide-", "divide", "outline",
        // Effects
        "opacity-", "mix-blend-", "filter", "blur-", "brightness-", "contrast-", "drop-shadow-", "grayscale-", 
        "hue-rotate-", "invert-", "saturate-", "sepia-", "backdrop-",
        // Transforms 
        "scale-", "rotate-", "translate-", "skew-", "transform", "transform-origin-", "perspective-",
        // Transitions & Animation
        "transition-", "duration-", "ease-", "delay-", "animate-", "motion-",
        // Interactivity
        "cursor-", "pointer-events-", "resize-", "select-", "touch-", "scroll-", "snap-", "overscroll-", "accent-",
        "appearance-", "caret-", "will-change-", "user-",
        // Tables
        "table-", "caption-", "border-spacing-", "border-collapse",
        // SVG
        "fill-", "stroke-", "stroke-w-", "object-",
        // Accessibility
        "sr-", "not-sr-", "focus:", "focus-visible:", "focus-within:", "forced-color-adjust-",
        // Arbitrary properties
        "[&", "[", 
        // Nested variant notation in Tailwind v4
        "-:",
        // Directional variants (LTR/RTL)
        "rtl:", "ltr:", "dir-", "horizontal-",
        // New Tailwind v4 utilities
        "grid-rows-", "grid-areas-", "grid-area-", "subgrid", "columns-", "column-", "page-break-",
        "text-wrap-", "font-synthesis-", "diagonal-", "writing-", "text-orientation-", "content-visibility-",
        "hint-",
    )

    /**
     * Extracts the current prefix at cursor position
     */
    private fun getCurrentPrefix(position: PsiElement): String {
        // Get the text content of the current file
        val fileText = position.containingFile.text
        
        // Get the caret offset within the file
        val caretOffset = position.textOffset
        
        // Extract the prefix at the caret position
        return extractTailwindPrefix(fileText, caretOffset)
    }
    
    /**
     * Extracts variant prefix and base prefix from a complete prefix
     * e.g., "hover:bg-blue" -> ("hover:", "bg-blue")
     */
    private fun extractVariantAndBasePrefix(prefix: String): Pair<String, String> {
        // Handle variant prefixes (e.g. hover:, sm:, focus:)
        val prefixParts = prefix.split(":")
        
        return if (prefixParts.size > 1) {
            val variant = prefixParts.subList(0, prefixParts.size - 1).joinToString(":") + ":"
            val base = prefixParts.last()
            Pair(variant, base)
        } else {
            Pair("", prefix)
        }
    }
    
    /**
     * Find the tailwind.config.js file in the project
     */
    private fun findTailwindConfigPath(project: Project): String {
        // This is a simplified version, in practice we would scan the project directories
        val baseDir = project.basePath ?: return ""
        val configFile = "$baseDir/tailwind.config.js"
        
        return configFile
    }
    
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val prefix = getCurrentPrefix(position)
        val project = parameters.originalFile.project
        
        // Skip if prefix is empty or doesn't look like a Tailwind class
        if (prefix.isEmpty() || prefix.startsWith(".") || prefix.startsWith("#")) {
            return
        }
        
        // Traiter les valeurs arbitraires JIT
        if (prefix.contains("[") && !prefix.endsWith("]")) {
            jitSupport.processArbitraryValue(prefix, result)
            return
        }
        
        // Process the prefix to extract any variant parts (e.g., "hover:bg-" -> variant="hover:", basePrefix="bg-")
        val prefixParts = extractVariantAndBasePrefix(prefix)
        val variantPrefix = prefixParts.first
        val basePrefix = prefixParts.second
        
        // Espace pour les suggestions contextuelles si nécessaire dans le futur
        // Actuellement non implémenté
        
        // Ajouter les classes personnalisées de la configuration
        val configPath = findTailwindConfigPath(project)
        if (configPath.isNotEmpty()) {
            val config = configAnalyzer.analyzeConfig(configPath)
            val customClasses = configAnalyzer.extractCustomClasses(config)
            
            for (customClass in customClasses) {
                if (customClass.startsWith(basePrefix)) {
                    val fullClass = variantPrefix + customClass
                    val element = LookupElementBuilder.create(fullClass as String)
                        .withPresentableText(fullClass)
                        .withTypeText("Custom Tailwind Class", true)
                        .withIcon(TailwindIconRegistry.getIconForClass(customClass))
                    
                    result.addElement(PrioritizedLookupElement.withPriority(element, 150.0))
                }
            }
        }
        
        // Special case for colon: show all available variants
        if (prefix.endsWith(":") || prefix == ":") {
            addVariantCompletions(prefix, result)
            return
        }
        
        // Determine if we should show completions based on prefix
        val minPrefixLength = 1 // Reduced to 1 character to be more responsive
        
        // Handle various completion triggers
        val isPrefixLongEnough = prefix.length >= minPrefixLength
        val isCommonPrefix = commonPrefixes.any { prefix.startsWith(it.toString(), ignoreCase = true) }
        val isEmptyAfterSpace = prefix.isEmpty() || prefix.isBlank()
        val hasVariantPrefix = prefix.contains(":")
        val containsHyphen = prefix.contains("-") // Check for hyphen anywhere in the prefix
        
        if (!isPrefixLongEnough && !isCommonPrefix && !isEmptyAfterSpace && !hasVariantPrefix && !containsHyphen) return
        
        logger.debug("Processing completions for prefix: '$prefix', variant: '$variantPrefix', base: '$basePrefix'")
        
        // Only limit to 500 items to balance performance and completeness
        val completionLimit = 500
        
        // Get all applicable Tailwind classes from the utility
        TailwindUtils.getTailwindClasses(project)
            .asSequence()
            .filter { 
                // Advanced filtering for hyphen-based completions
                when {
                    // For hyphen-ended prefixes (e.g., "text-")
                    basePrefix.endsWith("-") -> {
                        it.startsWith(basePrefix, ignoreCase = true)
                    }
                    
                    // For prefixes with internal hyphens (e.g., "text-red-")
                    basePrefix.contains("-") -> {
                        val lastHyphenIndex = basePrefix.lastIndexOf("-")
                        
                        if (lastHyphenIndex < basePrefix.length - 1) {
                            // Check if this is a partial completion after a hyphen
                            // e.g. "text-re" should match "text-red-500"
                            val prefixBeforeLastHyphen = basePrefix.substring(0, lastHyphenIndex + 1)
                            val partialAfterLastHyphen = basePrefix.substring(lastHyphenIndex + 1)
                            
                            // Show completions that match the prefix up to the last hyphen,
                            // and then have the partial text after the hyphen
                            it.startsWith(basePrefix, ignoreCase = true) || 
                            (it.startsWith(prefixBeforeLastHyphen, ignoreCase = true) && 
                             it.substring(prefixBeforeLastHyphen.length).startsWith(partialAfterLastHyphen, ignoreCase = true))
                        } else {
                            // Normal prefix matching
                            it.startsWith(basePrefix, ignoreCase = true)
                        }
                    }
                    
                    // For normal prefixes, do normal prefix matching
                    else -> it.startsWith(basePrefix, ignoreCase = true)
                }
            }
            .take(completionLimit) 
            .forEach { cls ->
                // Use the new icon registry to get appropriate icons for each class
                val icon = TailwindIconRegistry.getIconForClass(cls, 14)
                val (category, _) = TailwindCategoryUtils.getCategoryAndColor(cls)
                val suggestion = variantPrefix + cls
                
                val element = LookupElementBuilder.create(suggestion)
                    .withIcon(icon)
                    .withTypeText(category, true)
                    .withBoldness(true)
                
                // Prioritize exact matches and common utilities higher
                val exactMatchBonus = if (cls == basePrefix) 50.0 else 0.0
                val commonPrefixBonus = if (commonPrefixes.any { cls.startsWith(it) }) 25.0 else 0.0
                val shorterBonus = 20.0 - (cls.length * 0.5).coerceAtMost(20.0) // Shorter = higher priority
                
                val priority = 100.0 + exactMatchBonus + commonPrefixBonus + shorterBonus
                
                result.addElement(PrioritizedLookupElement.withPriority(element, priority))
            }

        // Smart Class Group suggestions (buttons, alerts, etc.)
        SmartClassGroupUtils.GROUPS
            .filter { it.key.startsWith(basePrefix, ignoreCase = true) }
            .forEach { group ->
                // Use round color icons for groups for better UX
                val icon = com.github.dilika.tailwindsmartplugin.utils.RoundColorIcon(14, group.color)
                val element = LookupElementBuilder.create(group.classes)
                    .withLookupString(variantPrefix + group.key)
                    .withPresentableText(group.key)
                    .withTailText(" (" + group.description + ")", true)
                    .withIcon(icon)
                    .withTypeText("Group", true)
                    .withInsertHandler(InsertHandler<LookupElement> { context, _ ->
                        // Replace the current prefix with full class list
                        context.document.replaceString(context.startOffset, context.tailOffset, group.classes)
                    })
                result.addElement(PrioritizedLookupElement.withPriority(element, 200.0))
            }
        
        // If the prefix appears to be a complete class and there might be need for a variant, suggest common variants
        if (prefix.isNotEmpty() && !prefix.contains(":") && !prefix.endsWith("-") && 
            !prefix.endsWith("[") && !prefix.startsWith(".") && !prefix.startsWith("#")) {
            suggestVariantsForClass(prefix, result)
        }
    }
    
    /**
     * Add variant completions when the user types a colon
     */
    private fun addVariantCompletions(prefix: String, resultSet: CompletionResultSet) {
        // When user types ":" or "variant:", show all available variants
        // Split by colon to handle nested variants
        val parts = prefix.split(":")
        val currentPrefix = if (parts.isEmpty()) "" else parts.last()
        
        tailwindVariants.forEach { variant ->
            // Skip variants that don't match the current prefix
            if (!variant.startsWith(currentPrefix, ignoreCase = true)) return@forEach
            
            // Build the full suggestion with preceding variants
            val suggestion = if (prefix.endsWith(":") || prefix == ":") {
                if (prefix == ":") variant else prefix + variant.removePrefix(":")
            } else {
                // Handle partial variant name like "hov" -> should complete to "hover:"
                val precedingVariants = parts.dropLast(1).joinToString(":") 
                if (precedingVariants.isNotEmpty()) "$precedingVariants:$variant" else variant
            }
            
            val element = LookupElementBuilder.create(suggestion)
                .withPresentableText(variant)
                .withTypeText("Variant", true)
                .withIcon(TailwindCategoryIcon("variant", JBColor(0x6B7280, 0x6B7280), 14))
                .withBoldness(true)
            
            // Prioritize common variants higher
            val priority = when {
                variant.startsWith("sm:") || variant.startsWith("md:") || variant.startsWith("lg:") -> 195.0
                variant.startsWith("hover:") || variant.startsWith("focus:") -> 190.0
                variant.startsWith("dark:") -> 185.0
                else -> 180.0
            }
            
            resultSet.addElement(PrioritizedLookupElement.withPriority(element, priority))
        }
    }
    
    /**
     * Suggest variants for a complete class
     */
    private fun suggestVariantsForClass(className: String, resultSet: CompletionResultSet) {
        // Suggest common variants for existing classes
        val commonVariantsForSuggestion = listOf(
            "hover:", "focus:", "active:", "dark:", 
            "sm:", "md:", "lg:", "disabled:"
        )
        
        commonVariantsForSuggestion.forEach { variant ->
            val suggestion = variant + className
            val element = LookupElementBuilder.create(suggestion)
                .withPresentableText(variant + className)
                .withLookupString(variant + className)
                .withTypeText("Variant + Class", true)
                .withIcon(TailwindCategoryIcon("variant", JBColor(0x6B7280, 0x6B7280), 14))
            
            resultSet.addElement(PrioritizedLookupElement.withPriority(element, 150.0))
        }
    }

    // Extracts the class prefix at the caret position, supporting variants, arbitrary values, and partials
    private fun extractTailwindPrefix(fileText: String, caretOffset: Int): String {
        // Find the start of the current class name
        var startOffset = caretOffset
        while (startOffset > 0 && !isWhitespaceOrDelimiter(fileText[startOffset - 1])) {
            startOffset--
        }
        
        // Get the text from the identified start to the caret position
        return fileText.substring(startOffset, caretOffset)
    }
    private fun isWhitespaceOrDelimiter(char: Char): Boolean {
        return char.isWhitespace() || char == '"' || char == '\'' || char == '>' || char == '<' || char == '=' || char == '{' || char == '}'
    }
        
    // isClassAttribute checks if an element is a class attribute in HTML/JSX/etc.
    private fun isClassAttribute(element: PsiElement): Boolean {
        // Check parent elements to find the attribute name
        var current: PsiElement? = element
        while (current != null) {
            // For XML-like files (HTML, JSX, Vue templates, etc.)
            if (current is XmlAttributeValue) {
                val attributeName = (current.parent as? com.intellij.psi.xml.XmlAttribute)?.name?.lowercase() ?: ""
                return attributeName == "class" || attributeName == "classname"
            }
            
            // Check for JS/TS/JSX/TSX: look for class or className in the context
            val text = current.text
            if (text.contains("class=") || text.contains("className=") || text.contains("className`")) {
                return true
            }
            
            // Move up in the hierarchy
            current = current.parent
        }
        
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
