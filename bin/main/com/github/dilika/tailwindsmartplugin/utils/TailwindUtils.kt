package com.github.dilika.tailwindsmartplugin.utils

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.json.JSONObject

/**
 * Utility class for Tailwind CSS
 */
object TailwindUtils {
    private val logger = Logger.getInstance("TailwindUtils")
    
    // Cache the Tailwind classes and data for better performance
    private val tailwindClassesCache = mutableMapOf<String, List<String>>()
    private val tailwindClassDataCache = mutableMapOf<String, Map<String, JSONObject>>()
    
    /**
     * Get all Tailwind CSS classes for a project
     */
    fun getTailwindClasses(project: Project): List<String> {
        val projectId = project.basePath ?: ""
        
        return tailwindClassesCache.getOrPut(projectId) {
            try {
                generateTailwindClasses()
            } catch (e: Exception) {
                logger.error("Error generating Tailwind classes: ${e.message}")
                emptyList()
            }
        }
    }
    
    /**
     * Get detailed data for Tailwind CSS classes
     */
    fun getTailwindClassData(project: Project): Map<String, JSONObject> {
        val projectId = project.basePath ?: ""
        
        return tailwindClassDataCache.getOrPut(projectId) {
            try {
                generateTailwindClassData()
            } catch (e: Exception) {
                logger.error("Error generating Tailwind class data: ${e.message}")
                emptyMap()
            }
        }
    }
    
    /**
     * Generate a comprehensive list of Tailwind CSS classes
     */
    private fun generateTailwindClasses(): List<String> {
        val baseClasses = mutableListOf<String>()
        val allClasses = mutableListOf<String>()
        
        // Ajouter les classes spécifiques demandées
        val specificClasses = listOf(
            "h-full", "bg-white", "rounded-lg", "overflow-hidden", "px-2.5", "min-h-[48px]", "font-semibold",
            "h-5", "w-5", "text-emerald-500", "flex-shrink-0", "mr-2", "mt-0.5", "w-full", "py-4", "px-4",
            "justify-between", "items-center", "min-w-[200px]", "max-w-7xl", "mx-auto", "gap-x-4", "gap-y-3"
        )
        baseClasses.addAll(specificClasses)
        
        // Tailwind v4 color palette
        val colors = listOf(
            // Original colors
            "slate", "gray", "zinc", "neutral", "stone", 
            "red", "orange", "amber", "yellow", "lime", "green", "emerald", 
            "teal", "cyan", "sky", "blue", "indigo", "violet", "purple", 
            "fuchsia", "pink", "rose",
            
            // New Tailwind v4 colors
            "copper", "jungle", "sand", "chestnut", "midnight", "aqua", "cherry", 
            "magenta", "moss", "sapphire", "charcoal", "lava", "sunset", "marine"
        )
        
        // Tailwind v4 includes additional shade values
        val shades = listOf("50", "100", "200", "300", "400", "500", "600", "700", "800", "900", "950", "975")
        
        // Background colors
        for (color in colors) {
            for (shade in shades) {
                baseClasses.add("bg-$color-$shade")
            }
        }
        
        // Text colors
        for (color in colors) {
            for (shade in shades) {
                baseClasses.add("text-$color-$shade")
            }
        }
        
        // Border colors
        for (color in colors) {
            for (shade in shades) {
                baseClasses.add("border-$color-$shade")
            }
        }
        
        // Layout - Updated for Tailwind v4
        baseClasses.addAll(listOf(
            // Display
            "block", "inline-block", "inline", "flex", "inline-flex", "table", "inline-table", 
            "grid", "inline-grid", "flow-root", "contents", "list-item", "hidden",
            
            // Container
            "container", "container-fluid", "container-xs", "container-sm", "container-md", "container-lg", "container-xl", 
            "container-2xl", "mx-auto",
            
            // Position
            "static", "fixed", "absolute", "relative", "sticky", "inset-0", "inset-x-0", "inset-y-0",
            "inset-auto", "top-0", "right-0", "bottom-0", "left-0", "top-auto", "right-auto", "bottom-auto", "left-auto",
            
            // Flex - v4 expands flex options
            "flex-row", "flex-row-reverse", "flex-col", "flex-col-reverse",
            "flex-wrap", "flex-wrap-reverse", "flex-nowrap",
            "flex-1", "flex-auto", "flex-initial", "flex-none", "flex-grow", "flex-grow-0", "flex-shrink", "flex-shrink-0",
            "justify-normal", "justify-start", "justify-end", "justify-center", "justify-between", "justify-around", "justify-evenly", "justify-stretch",
            "items-start", "items-end", "items-center", "items-baseline", "items-stretch",
            "self-auto", "self-start", "self-end", "self-center", "self-stretch", "self-baseline",
            "place-content-center", "place-content-start", "place-content-end", "place-content-between", "place-content-around", "place-content-evenly", "place-content-stretch",
            "place-items-auto", "place-items-start", "place-items-end", "place-items-center", "place-items-stretch",
            "place-self-auto", "place-self-start", "place-self-end", "place-self-center", "place-self-stretch",
            
            // Flexbox gaps
            "gap-x-0", "gap-x-1", "gap-x-2", "gap-x-4", "gap-x-8",
            "gap-y-0", "gap-y-1", "gap-y-2", "gap-y-4", "gap-y-8",
            
            // Grid - v4 has more grid features
            "grid-cols-1", "grid-cols-2", "grid-cols-3", "grid-cols-4", "grid-cols-5", "grid-cols-6",
            "grid-cols-7", "grid-cols-8", "grid-cols-9", "grid-cols-10", "grid-cols-11", "grid-cols-12",
            "grid-cols-none", "grid-cols-subgrid",
            "grid-rows-1", "grid-rows-2", "grid-rows-3", "grid-rows-4", "grid-rows-5", "grid-rows-6", "grid-rows-none", "grid-rows-subgrid",
            "col-auto", "col-span-1", "col-span-2", "col-span-3", "col-span-4", "col-span-5", "col-span-6",
            "col-span-7", "col-span-8", "col-span-9", "col-span-10", "col-span-11", "col-span-12", "col-span-full",
            "row-auto", "row-span-1", "row-span-2", "row-span-3", "row-span-4", "row-span-5", "row-span-6", "row-span-full",
            "col-start-1", "col-start-2", "col-start-3", "col-start-4", "col-start-5", "col-start-6",
            "col-start-7", "col-start-8", "col-start-9", "col-start-10", "col-start-11", "col-start-12", "col-start-13", "col-start-auto",
            "col-end-1", "col-end-2", "col-end-3", "col-end-4", "col-end-5", "col-end-6",
            "col-end-7", "col-end-8", "col-end-9", "col-end-10", "col-end-11", "col-end-12", "col-end-13", "col-end-auto",
            "row-start-1", "row-start-2", "row-start-3", "row-start-4", "row-start-5", "row-start-6", "row-start-7", "row-start-auto",
            "row-end-1", "row-end-2", "row-end-3", "row-end-4", "row-end-5", "row-end-6", "row-end-7", "row-end-auto",
            "grid-flow-row", "grid-flow-col", "grid-flow-dense", "grid-flow-row-dense", "grid-flow-col-dense",
            "auto-cols-auto", "auto-cols-min", "auto-cols-max", "auto-cols-fr",
            "auto-rows-auto", "auto-rows-min", "auto-rows-max", "auto-rows-fr",
            "gap-0", "gap-1", "gap-2", "gap-3", "gap-4", "gap-5", "gap-6", "gap-8", "gap-10", "gap-12", "gap-16", "gap-20", "gap-24"
        ))
        
        // Tailwind v4 spacing uses rem values and has expanded range
        val spacingValues = listOf(
            // Core values
            "0", "px", "0.5", "1", "1.5", "2", "2.5", "3", "3.5", "4", "5", "6", "7", "8", "9", "10", "11", "12", "14", "16", 
            "18", "20", "24", "28", "32", "36", "40", "44", "48", "52", "56", "60", "64", "72", "80", "96",
            // Fractional values
            "1/2", "1/3", "2/3", "1/4", "2/4", "3/4", "1/5", "2/5", "3/5", "4/5",
            // Screen-based values
            "full", "screen", "min", "max", "fit", "svh", "lvh", "dvh",
            // Decimal values fréquemment utilisées
            "1.5", "2.5", "3.5", "4.5", "5.5", "6.5", "7.5", "8.5", "9.5"
        )
        
        // Expanded spacing prefixes for Tailwind v4
        val spacingPrefixes = listOf(
            // Padding
            "p", "px", "py", "pt", "pr", "pb", "pl", "ps", "pe", 
            // Margin
            "m", "mx", "my", "mt", "mr", "mb", "ml", "ms", "me",
            // Space between
            "space-x", "space-y", "space-s", "space-e"
        )
        
        // Générer toutes les combinaisons possibles de préfixes et de valeurs pour les spacings
        for (prefix in spacingPrefixes) {
            for (value in spacingValues) {
                baseClasses.add("$prefix-$value")
            }
            // Ajout des valeurs négatives pour les marges
            if (prefix.startsWith("m")) {
                for (value in spacingValues) {
                    // Ignorer les valeurs qui ne peuvent pas être négatives
                    if (value != "auto" && value != "full" && value != "screen" && value != "min" && value != "max" && value != "fit" && !value.contains("/")) {
                        baseClasses.add("-$prefix-$value")
                    }
                }
            }
        }
        
        // Ajouter explicitement les classes mentionnées par l'utilisateur pour s'assurer qu'elles sont couvertes
        val explicitClasses = listOf(
            "inline-block", "px-3", "py-1", "text-indigo-600", "font-medium", "text-xs", 
            "uppercase", "tracking-wider", "mb-3", "mb-4", "mb-5", "mt-3", "mt-4", "mt-5"
        )
        baseClasses.addAll(explicitClasses)
        
        // === SIZING (w-, h-, max/min) ===
        val sizePrefixes = listOf("w", "h", "min-w", "max-w", "min-h", "max-h")
        for (prefix in sizePrefixes) {
            for (value in spacingValues) {
                baseClasses.add("$prefix-$value")
            }
        }
        
        // === ROUNDED ===
        val radiusValues = listOf("none", "sm", "md", "lg", "xl", "2xl", "3xl", "full")
        val roundedPrefixes = listOf(
            "rounded", "rounded-t", "rounded-r", "rounded-b", "rounded-l", 
            "rounded-tl", "rounded-tr", "rounded-br", "rounded-bl"
        )
        
        // Base "rounded" sans valeur
        baseClasses.add("rounded")
        for (prefix in roundedPrefixes) {
            for (value in radiusValues) {
                baseClasses.add("$prefix-$value")
            }
        }
        
        // === OVERFLOW ===
        val overflowVariants = listOf("auto", "hidden", "visible", "scroll")
        for (variant in overflowVariants) {
            baseClasses.add("overflow-$variant")
            baseClasses.add("overflow-x-$variant")
            baseClasses.add("overflow-y-$variant")
        }
        
        // === SHADOWS ===
        val shadowVariants = listOf("shadow", "shadow-sm", "shadow-md", "shadow-lg", "shadow-xl", "shadow-2xl", "shadow-inner", "shadow-none")
        baseClasses.addAll(shadowVariants)
        
        // === RING WIDTH & INSET ===
        val ringWidths = listOf("ring", "ring-0", "ring-1", "ring-2", "ring-4", "ring-8", "ring-inset")
        baseClasses.addAll(ringWidths)
        
        // === RING COLORS ===
        for (color in colors) {
            for (shade in shades) {
                baseClasses.add("ring-$color-$shade")
            }
        }
        
        // === OUTLINE ===
        val outlineWidths = listOf("outline-none", "outline", "outline-0", "outline-1", "outline-2", "outline-4", "outline-8")
        baseClasses.addAll(outlineWidths)
        for (color in colors) {
            for (shade in shades) {
                baseClasses.add("outline-$color-$shade")
            }
        }
        
        // === PLACEHOLDER, ACCENT, CARET, FILL, STROKE COLORS ===
        val colorUtilities = listOf("placeholder", "accent", "caret", "fill", "stroke")
        for (util in colorUtilities) {
            for (color in colors) {
                for (shade in shades) {
                    baseClasses.add("$util-$color-$shade")
                }
            }
        }
        
        // === ASPECT RATIO ===
        baseClasses.addAll(listOf("aspect-auto", "aspect-square", "aspect-video"))
        
        // === ACCESSIBILITY ===
        baseClasses.addAll(listOf("sr-only", "not-sr-only"))
        
        // === LIST STYLE ===
        baseClasses.addAll(listOf("list-none", "list-disc", "list-decimal", "list-inside", "list-outside"))
        
        // === COLUMNS ===
        val columnSizes = (1..12).map { it.toString() } + listOf("auto", "3xs", "2xs", "xs", "sm", "md", "lg", "xl", "2xl", "3xl", "4xl")
        for (size in columnSizes) {
            baseClasses.add("columns-$size")
        }
        
        // Typography - Tailwind v4 additions
        baseClasses.addAll(listOf(
            // Font Family - added system fonts
            "font-sans", "font-serif", "font-mono", "font-system", "font-ui",
            
            // Font Size - enhanced with fluid sizing
            "text-xs", "text-sm", "text-base", "text-lg", "text-xl", "text-2xl", "text-3xl", 
            "text-4xl", "text-5xl", "text-6xl", "text-7xl", "text-8xl", "text-9xl", "text-10xl",
            "text-fluid-xs", "text-fluid-sm", "text-fluid-base", "text-fluid-lg", "text-fluid-xl", "text-fluid-2xl", "text-fluid-3xl",
            
            // Font Weight - more granular weights
            "font-thin", "font-extralight", "font-light", "font-normal", "font-medium", 
            "font-semibold", "font-bold", "font-extrabold", "font-black", "font-100", "font-200", "font-300",
            "font-400", "font-500", "font-600", "font-700", "font-800", "font-900",
            
            // Utilities spécifiques à Tailwind v4
            "font-leading-tight", "font-leading-normal", "font-leading-loose",
            "font-tracking-tight", "font-tracking-normal", "font-tracking-wide",
            
            // Letter Spacing
            "tracking-tighter", "tracking-tight", "tracking-normal", "tracking-wide", "tracking-wider", "tracking-widest",
            "tracking-ultratight", "tracking-ultrawide",
            
            // Line Height - expanded values
            "leading-3", "leading-4", "leading-5", "leading-6", "leading-7", "leading-8", "leading-9", "leading-10",
            "leading-11", "leading-12", "leading-14", "leading-16", "leading-20", "leading-24",
            "leading-none", "leading-tight", "leading-snug", "leading-normal", "leading-relaxed", "leading-loose",
            
            // Text Alignment and Text Decoration
            "text-left", "text-center", "text-right", "text-justify", "text-start", "text-end",
            "text-decoration-none", "text-decoration-underline", "text-decoration-overline", "text-decoration-line-through",
            "text-wrap", "text-nowrap", "text-balance", "text-pretty",
            
            // Text Transform
            "uppercase", "lowercase", "capitalize", "normal-case",
            
            // Font Variant Numeric
            "normal-nums", "ordinal", "slashed-zero", "lining-nums", "oldstyle-nums", "proportional-nums", "tabular-nums",
            "diagonal-fractions", "stacked-fractions"
        ))
        
        // Borders
        val borderWidths = listOf("", "0", "2", "4", "8")
        for (width in borderWidths) {
            val prefix = if (width.isEmpty()) "border" else "border-$width"
            baseClasses.add(prefix)
            baseClasses.add("$prefix-t")
            baseClasses.add("$prefix-r")
            baseClasses.add("$prefix-b")
            baseClasses.add("$prefix-l")
        }
        
        // Rounded corners
        val radiusSizes = listOf("", "none", "sm", "md", "lg", "xl", "2xl", "3xl", "full")
        for (size in radiusSizes) {
            val prefix = if (size.isEmpty()) "rounded" else "rounded-$size"
            baseClasses.add(prefix)
            baseClasses.add("$prefix-t")
            baseClasses.add("$prefix-r")
            baseClasses.add("$prefix-b")
            baseClasses.add("$prefix-l")
            baseClasses.add("$prefix-tl")
            baseClasses.add("$prefix-tr")
            baseClasses.add("$prefix-bl")
            baseClasses.add("$prefix-br")
        }
        
        // Effects
        baseClasses.addAll(listOf(
            // Shadows
            "shadow-none", "shadow-sm", "shadow-md", "shadow-lg", "shadow-xl", "shadow-2xl", "shadow-inner",
            
            // Opacity
            "opacity-0", "opacity-5", "opacity-10", "opacity-20", "opacity-25", "opacity-30", "opacity-40", 
            "opacity-50", "opacity-60", "opacity-70", "opacity-75", "opacity-80", "opacity-90", "opacity-95", "opacity-100",
            
            // Transitions and Animations - New in v4
            "transition", "transition-all", "transition-colors", "transition-opacity", "transition-shadow", "transition-transform",
            "duration-75", "duration-100", "duration-150", "duration-200", "duration-300", "duration-500", "duration-700", "duration-1000",
            "ease-linear", "ease-in", "ease-out", "ease-in-out",
            "animate-none", "animate-spin", "animate-ping", "animate-pulse", "animate-bounce", "animate-fade", "animate-slide"
        ))
        
        // Add specific classes mentioned by users that might not be covered by patterns
        baseClasses.addAll(listOf(
            // Direct classes from user examples
            "font-bold", "font-medium", "text-base", "text-xs", "mx-auto", "overflow-x-auto", "overflow-hidden", "py-1", "py-3", "px-1", "px-2", "px-3", "px-4", "mb-1", "mb-2", "mb-3", "mb-8", "gap-4", "scrollbar-h", "justify-center", "justify-between", "uppercase", "tracking-wider", "text-indigo-600", "text-indigo-500", "text-indigo-700",
            // Specific fixed width/max-width utilities
            "max-w-xs", "max-w-sm", "max-w-md", "max-w-lg", "max-w-xl", "max-w-2xl", "max-w-3xl", "max-w-4xl", "max-w-5xl", "max-w-6xl", "max-w-7xl", "max-w-prose", "max-w-screen-sm", "max-w-screen-md", "max-w-screen-lg", "max-w-screen-xl", "max-w-screen-2xl",
            
            // Width and height utilities
            "w-auto", "w-px", "w-0", "w-0.5", "w-1", "w-1.5", "w-2", "w-2.5", "w-3", "w-3.5", "w-4", "w-5", "w-6", "w-7", "w-8", "w-9", "w-10", "w-11", "w-12", "w-14", "w-16",
            "w-20", "w-24", "w-28", "w-32", "w-36", "w-40", "w-44", "w-48", "w-52", "w-56", "w-60", "w-64", "w-72", "w-80", "w-96", "w-full", "w-screen", "w-min", "w-max", "w-fit",
            "h-auto", "h-px", "h-0", "h-0.5", "h-1", "h-1.5", "h-2", "h-2.5", "h-3", "h-3.5", "h-4", "h-5", "h-6", "h-7", "h-8", "h-9", "h-10", "h-11", "h-12", "h-14", "h-16",
            "h-20", "h-24", "h-28", "h-32", "h-36", "h-40", "h-44", "h-48", "h-52", "h-56", "h-60", "h-64", "h-72", "h-80", "h-96", "h-full", "h-screen", "h-min", "h-max", "h-fit",
            
            // Blur utilities
            "blur-none", "blur-sm", "blur", "blur-md", "blur-lg", "blur-xl", "blur-2xl", "blur-3xl",
            
            // Negative position utilities for inset
            "-top-0", "-top-px", "-top-0.5", "-top-1", "-top-1.5", "-top-2", "-top-2.5", "-top-3", "-top-3.5", "-top-4", "-top-5", "-top-6", "-top-7", "-top-8", "-top-9", "-top-10",
            "-top-11", "-top-12", "-top-14", "-top-16", "-top-20", "-top-24", "-top-28", "-top-32", "-top-36", "-top-40", "-top-44", "-top-48", "-top-52", "-top-56", "-top-60", "-top-64", "-top-72", "-top-80", "-top-96", "-top-full",
            "-bottom-0", "-bottom-px", "-bottom-0.5", "-bottom-1", "-bottom-1.5", "-bottom-2", "-bottom-2.5", "-bottom-3", "-bottom-3.5", "-bottom-4", "-bottom-5", "-bottom-6", "-bottom-7", "-bottom-8", "-bottom-9", "-bottom-10",
            "-bottom-11", "-bottom-12", "-bottom-14", "-bottom-16", "-bottom-20", "-bottom-24", "-bottom-28", "-bottom-32", "-bottom-36", "-bottom-40", "-bottom-44", "-bottom-48", "-bottom-52", "-bottom-56", "-bottom-60", "-bottom-64", "-bottom-72", "-bottom-80", "-bottom-96", "-bottom-full",
            "-left-0", "-left-px", "-left-0.5", "-left-1", "-left-1.5", "-left-2", "-left-2.5", "-left-3", "-left-3.5", "-left-4", "-left-5", "-left-6", "-left-7", "-left-8", "-left-9", "-left-10",
            "-left-11", "-left-12", "-left-14", "-left-16", "-left-20", "-left-24", "-left-28", "-left-32", "-left-36", "-left-40", "-left-44", "-left-48", "-left-52", "-left-56", "-left-60", "-left-64", "-left-72", "-left-80", "-left-96", "-left-full",
            "-right-11", "-right-12", "-right-14", "-right-16", "-right-20", "-right-24", "-right-28", "-right-32", "-right-36", "-right-40", "-right-44", "-right-48", "-right-52", "-right-56", "-right-60", "-right-64", "-right-72", "-right-80", "-right-96", "-right-full"
        ))
        
        // Now let's handle the variants (responsive, state, etc.)
        
        // Responsive breakpoint prefixes
        val breakpointPrefixes = listOf("sm", "md", "lg", "xl", "2xl")
        
        // Pseudo-class / state variants
        val pseudoClasses = listOf(
            "hover", "focus", "active", "disabled", "visited", "checked", "first", "last", "odd", "even",
            "group-hover", "focus-within", "focus-visible", "aria-selected", "aria-disabled", "aria-hidden", "aria-expanded",
            "peer", "peer-checked", "peer-focus", "peer-hover", "before", "after", "placeholder", "selection", "marker", "file",
            "data", "dark", "motion-safe", "motion-reduce", "landscape", "portrait", "print"
        )
        
        // First, we'll add all base classes
        allClasses.addAll(baseClasses)
        
        // Support des classes avec valeurs arbitraires (notation entre crochets)
        val arbitraryProperties = listOf(
            "w", "h", "min-w", "min-h", "max-w", "max-h", "p", "px", "py", "pt", "pr", "pb", "pl",
            "m", "mx", "my", "mt", "mr", "mb", "ml", "top", "right", "bottom", "left", "inset",
            "gap", "gap-x", "gap-y", "text", "leading", "tracking", "rounded", "translate-x", "translate-y",
            "rotate", "scale", "skew-x", "skew-y", "z", "opacity"
        )
        
        // Ajouter quelques exemples de classes avec valeurs arbitraires
        val arbitraryValues = listOf(
            "[10px]", "[48px]", "[100px]", "[200px]", "[300px]", "[400px]", "[500px]", "[50%]", "[60%]", "[70%]", "[80%]", "[90%]",
            "[calc(100%-2rem)]", "[calc(100vh-80px)]", "[fit-content]", "[3em]", "[5ch]", "[min-content]", "[max-content]",
            "[#ff5500]", "[var(--color-primary)]", "[theme(colors.blue.500)]"
        )
        
        for (prop in arbitraryProperties) {
            for (value in arbitraryValues) {
                allClasses.add("$prop-$value")
            }
        }
        
        // Add opacity modifier examples for colors
        val opacityValues = listOf("5", "10", "20", "30", "40", "50", "60", "70", "80", "90", "95")
        val colorClasses = baseClasses.filter { it.startsWith("bg-") || it.startsWith("text-") || it.startsWith("border-") }
            .filter { it.contains("-") && it.split("-").size >= 3 } // Only include color classes with shades
            .take(colors.size * 3) // Take a representative subset to keep the set manageable
        
        for (colorClass in colorClasses) {
            for (opacity in opacityValues.take(5)) { // Limit to 5 opacity values per color class
                baseClasses.add("$colorClass/$opacity")
            }
        }
        
        // Also add arbitrary colors with opacity
        val hexColors = listOf("#fff", "#000", "#3b82f6", "#22c55e", "#ef4444", "#eab308", "#8b5cf6")
        for (hex in hexColors) {
            for (opacity in opacityValues.take(3)) {
                baseClasses.add("bg-[$hex]/$opacity")
                baseClasses.add("text-[$hex]/$opacity")
                baseClasses.add("border-[$hex]/$opacity")
            }
        }
        
        // Generate responsive variants
        for (breakpoint in breakpointPrefixes) {
            for (baseClass in baseClasses) {
                allClasses.add("$breakpoint:$baseClass")
            }
        }
        
        // Generate state variants (but not for all classes to keep the list manageable)
        // Focus on common state variants for interactive elements
        val interactiveClassPatterns = listOf(
            "bg-", "text-", "border", "shadow", "opacity", "transform", "translate", "scale", "rotate", "skew", 
            "cursor-", "outline", "ring", "z-", "visible", "invisible", "block", "flex", "grid", "hidden"
        )
        
        for (variant in pseudoClasses) {
            for (baseClass in baseClasses) {
                // Only add state variants for classes that commonly use them
                if (interactiveClassPatterns.any { baseClass.startsWith(it) }) {
                    allClasses.add("$variant:$baseClass")
                }
            }
        }
        
        // Add common group/peer variants
        val groupPeerPrefixes = listOf("group-hover", "group-focus", "peer-hover", "peer-focus")
        for (prefix in groupPeerPrefixes) {
            for (baseClass in baseClasses) {
                if (interactiveClassPatterns.any { baseClass.startsWith(it) }) {
                    allClasses.add("$prefix:$baseClass")
                }
            }
        }
        
        return allClasses
    }
    
    /**
     * Generate detailed metadata for Tailwind CSS classes
     */
    private fun generateTailwindClassData(): Map<String, JSONObject> {
        val classData = mutableMapOf<String, JSONObject>()
        
        val classes = generateTailwindClasses()
        for (className in classes) {
            val data = JSONObject()
            val completion = JSONObject()
            val documentation = JSONObject()
            
            // Basic completion data
            completion.put("displayText", className)
            completion.put("lookupString", className)
            
            // Documentation
            documentation.put("title", className)
            
            // Determine category, icon, and other metadata
            val (category, icon, description) = getCategoryAndIcon(className)
            documentation.put("type", category)
            documentation.put("icon", icon)
            
            // Add color information for color-related classes
            val colorValue = getColorHexForClass(className)
            if (colorValue != null) {
                val colorStyle = "color: $colorValue;"
                completion.put("style", colorStyle)
                documentation.put("colorPreview", colorValue)
            }
            documentation.put("description", description)
            
            // Add examples where appropriate
            when {
                className.startsWith("bg-") -> {
                    documentation.put("example", "<div class=\"$className p-4\">Colored background</div>")
                }
                className.startsWith("text-") && className.contains("-") -> {
                    documentation.put("example", "<p class=\"$className\">Colored text</p>")
                }
                className.startsWith("border-") && !className.endsWith("-t") && 
                        !className.endsWith("-r") && !className.endsWith("-b") && !className.endsWith("-l") -> {
                    documentation.put("example", "<div class=\"$className border-2 p-4\">Colored border</div>")
                }
                className.startsWith("flex") || className == "flex" -> {
                    documentation.put("example", "<div class=\"$className\"><div>Item 1</div><div>Item 2</div></div>")
                }
                className.startsWith("grid") || className == "grid" -> {
                    documentation.put("example", "<div class=\"$className grid-cols-3 gap-4\"><div>1</div><div>2</div><div>3</div></div>")
                }
            }
            
            // Add CSS properties if known
            if (className.startsWith("p-") || className.startsWith("px-") || className.startsWith("py-") || 
                className.startsWith("pt-") || className.startsWith("pr-") || 
                className.startsWith("pb-") || className.startsWith("pl-")) {
                documentation.put("css", "padding: X")
            } else if (className.startsWith("m-") || className.startsWith("mx-") || className.startsWith("my-") || 
                       className.startsWith("mt-") || className.startsWith("mr-") || 
                       className.startsWith("mb-") || className.startsWith("ml-")) {
                documentation.put("css", "margin: X")
            } else if (className.startsWith("flex")) {
                documentation.put("css", "display: flex")
            } else if (className.startsWith("grid")) {
                documentation.put("css", "display: grid")
            }
            
            data.put("completion", completion)
            data.put("documentation", documentation)
            
            classData[className] = data
        }
        
        return classData
    }
    
    /**
     * Get the hex color value for a Tailwind color class
     */
    /**
     * Clear the Tailwind class cache for a specific project
     */
    fun clearProjectCache(project: Project) {
        val projectId = project.basePath ?: ""
        tailwindClassesCache.remove(projectId)
        tailwindClassDataCache.remove(projectId)
        logger.info("Cleared Tailwind cache for project: $projectId")
    }

    private fun getColorHexForClass(className: String): String? {
        // Only process color-related classes
        val isColorClass = className.startsWith("bg-") || 
                          (className.startsWith("text-") && className.contains("-") && className.split("-").size >= 3) ||
                          (className.startsWith("border-") && className.contains("-") && 
                           !className.endsWith("-t") && !className.endsWith("-r") && 
                           !className.endsWith("-b") && !className.endsWith("-l"))
        
        if (!isColorClass) return null
        
        // Extract color name and shade
        val parts = className.split("-")
        if (parts.size < 3) return null
        
        // No need to use colorPrefix as we determine the type from the class name pattern above
        // val colorPrefix = parts[0] // bg, text, border
        val colorName = parts[1]
        val shade = parts.getOrNull(2) ?: "500" // Default to 500 if no shade specified
        
        // Return hex color based on color name and shade
        return when (colorName) {
            // Original Tailwind colors
            "slate" -> when (shade) {
                "50" -> "#f8fafc"
                "100" -> "#f1f5f9"
                "200" -> "#e2e8f0"
                "300" -> "#cbd5e1"
                "400" -> "#94a3b8"
                "500" -> "#64748b"
                "600" -> "#475569"
                "700" -> "#334155"
                "800" -> "#1e293b"
                "900" -> "#0f172a"
                "950" -> "#020617"
                "975" -> "#010313"
                else -> "#64748b" // Default to 500
            }
            "gray" -> when (shade) {
                "50" -> "#f9fafb"
                "100" -> "#f3f4f6"
                "200" -> "#e5e7eb"
                "300" -> "#d1d5db"
                "400" -> "#9ca3af"
                "500" -> "#6b7280"
                "600" -> "#4b5563"
                "700" -> "#374151"
                "800" -> "#1f2937"
                "900" -> "#111827"
                "950" -> "#030712"
                "975" -> "#01050e"
                else -> "#6b7280"
            }
            "red" -> when (shade) {
                "50" -> "#fef2f2"
                "100" -> "#fee2e2"
                "200" -> "#fecaca"
                "300" -> "#fca5a5"
                "400" -> "#f87171"
                "500" -> "#ef4444"
                "600" -> "#dc2626"
                "700" -> "#b91c1c"
                "800" -> "#991b1b"
                "900" -> "#7f1d1d"
                "950" -> "#450a0a"
                "975" -> "#280505"
                else -> "#ef4444"
            }
            "orange" -> when (shade) {
                "50" -> "#fff7ed"
                "100" -> "#ffedd5"
                "200" -> "#fed7aa"
                "300" -> "#fdba74"
                "400" -> "#fb923c"
                "500" -> "#f97316"
                "600" -> "#ea580c"
                "700" -> "#c2410c"
                "800" -> "#9a3412"
                "900" -> "#7c2d12"
                "950" -> "#431407"
                "975" -> "#270b04"
                else -> "#f97316"
            }
            "blue" -> when (shade) {
                "50" -> "#eff6ff"
                "100" -> "#dbeafe"
                "200" -> "#bfdbfe"
                "300" -> "#93c5fd"
                "400" -> "#60a5fa"
                "500" -> "#3b82f6"
                "600" -> "#2563eb"
                "700" -> "#1d4ed8"
                "800" -> "#1e40af"
                "900" -> "#1e3a8a"
                "950" -> "#172554"
                "975" -> "#0d1531"
                else -> "#3b82f6"
            }
            "green" -> when (shade) {
                "50" -> "#f0fdf4"
                "100" -> "#dcfce7"
                "200" -> "#bbf7d0"
                "300" -> "#86efac"
                "400" -> "#4ade80"
                "500" -> "#22c55e"
                "600" -> "#16a34a"
                "700" -> "#15803d"
                "800" -> "#166534"
                "900" -> "#14532d"
                "950" -> "#052e16"
                "975" -> "#031b0d"
                else -> "#22c55e"
            }
            // New Tailwind v4 colors
            "copper" -> when (shade) {
                "50" -> "#fcf7ee"
                "100" -> "#f8eedc"
                "200" -> "#f1dab7"
                "300" -> "#e7c18b"
                "400" -> "#dba363"
                "500" -> "#d18942"
                "600" -> "#c37535"
                "700" -> "#a25b2a"
                "800" -> "#844a27"
                "900" -> "#6e3e25"
                "950" -> "#3c2010"
                "975" -> "#221208"
                else -> "#d18942"
            }
            "jungle" -> when (shade) {
                "50" -> "#f3faf4"
                "100" -> "#e4f6e7"
                "200" -> "#caecd0"
                "300" -> "#9fdeac"
                "400" -> "#70c884"
                "500" -> "#4eb168"
                "600" -> "#3a9253"
                "700" -> "#30744b"
                "800" -> "#2c5d42"
                "900" -> "#264d39"
                "950" -> "#0e2a1e"
                "975" -> "#051910"
                else -> "#4eb168"
            }
            "cherry" -> when (shade) {
                "50" -> "#fef1f7"
                "100" -> "#fee5f0"
                "200" -> "#fecce3"
                "300" -> "#ffa2cb"
                "400" -> "#fe67a5"
                "500" -> "#f63d81"
                "600" -> "#e91f64"
                "700" -> "#ca0c4a"
                "800" -> "#a70e3d"
                "900" -> "#8a1336"
                "950" -> "#55031c"
                "975" -> "#30020f"
                else -> "#f63d81"
            }
            "magenta" -> when (shade) {
                "50" -> "#fdf4fb"
                "100" -> "#fdeaf8"
                "200" -> "#fbcef2"
                "300" -> "#f9a4e6"
                "400" -> "#f667d4"
                "500" -> "#eb3bbd"
                "600" -> "#d9199d"
                "700" -> "#bf0d7e"
                "800" -> "#9c1068"
                "900" -> "#820f58"
                "950" -> "#500536"
                "975" -> "#2e021e"
                else -> "#eb3bbd"
            }
            // Default fallback for other colors
            else -> "#64748b"
        }
    }
    
    /**
     * Determine the category, icon, and description for a Tailwind CSS class
     */
    private fun getCategoryAndIcon(className: String): Triple<String, String, String> {
        // Default icon and category
        var category = "utility"
        var icon = "🔧" // Default icon
        var description = "Tailwind CSS class $className"
        
        // Analyze the class pattern to determine its type
        when {
            // Color-related classes
            className.startsWith("bg-") -> {
                category = "color"
                icon = "🎨"
                description = "Sets the background color"
            }
            className.startsWith("text-") && className.contains("-") && className.split("-").size >= 3 -> {
                category = "color"
                icon = "🖌️"
                description = "Sets the text color"
            }
            className.startsWith("border-") && className.contains("-") && !className.endsWith("-t") && 
                    !className.endsWith("-r") && !className.endsWith("-b") && !className.endsWith("-l") -> {
                category = "color"
                icon = "🖼️"
                description = "Sets the border color"
            }
            
            // Layout categories
            className.startsWith("flex") || className == "flex" -> {
                category = "layout"
                icon = "📐"
                description = "Flexbox layout utility"
            }
            className.startsWith("grid") || className == "grid" -> {
                category = "layout"
                icon = "🧮"
                description = "Grid layout utility"
            }
            className == "block" || className == "inline-block" || className == "inline" || 
                    className == "hidden" || className.startsWith("display-") -> {
                category = "layout"
                icon = "📏"
                description = "Display utility"
            }
            className.startsWith("justify-") || className.startsWith("items-") -> {
                category = "layout"
                icon = "↔️"
                description = "Flexbox alignment utility"
            }
            
            // Spacing categories
            className.startsWith("p-") || className.startsWith("px-") || className.startsWith("py-") || 
                    className.startsWith("pt-") || className.startsWith("pr-") || 
                    className.startsWith("pb-") || className.startsWith("pl-") -> {
                category = "spacing"
                icon = "⬌"
                description = "Padding utility"
            }
            className.startsWith("m-") || className.startsWith("mx-") || className.startsWith("my-") || 
                    className.startsWith("mt-") || className.startsWith("mr-") || 
                    className.startsWith("mb-") || className.startsWith("ml-") -> {
                category = "spacing"
                icon = "↕️"
                description = "Margin utility"
            }
            className.startsWith("gap-") -> {
                category = "spacing"
                icon = "⬚"
                description = "Gap spacing utility for grid/flexbox"
            }
            
            // Typography categories
            className.startsWith("font-") -> {
                category = "typography"
                icon = "🔤"
                description = "Font family or weight utility"
            }
            className.startsWith("text-") && !className.contains("-") || 
                    (className.startsWith("text-") && className.split("-").size == 2) -> {
                category = "typography"
                icon = "📝"
                description = "Text size utility"
            }
            className.startsWith("tracking-") -> {
                category = "typography"
                icon = "📊"
                description = "Letter spacing utility"
            }
            className.startsWith("leading-") -> {
                category = "typography"
                icon = "📃"
                description = "Line height utility"
            }
            
            // Border categories
            className == "border" || className.startsWith("border-") && (className.endsWith("-t") || 
                    className.endsWith("-r") || className.endsWith("-b") || className.endsWith("-l")) -> {
                category = "border"
                icon = "📦"
                description = "Border width utility"
            }
            className.startsWith("rounded") -> {
                category = "border"
                icon = "⭕"
                description = "Border radius utility"
            }
            
            // Effects categories
            className.startsWith("shadow") -> {
                category = "effects"
                icon = "☁️"
                description = "Box shadow utility"
            }
            className.startsWith("opacity") -> {
                category = "effects"
                icon = "👁️"
                description = "Opacity utility"
            }
            className.startsWith("transition") || className.startsWith("duration") || 
                    className.startsWith("ease-") || className.startsWith("animate-") -> {
                category = "effects"
                icon = "✨"
                description = "Animation or transition utility"
            }
        }
        
        return Triple(category, icon, description)
    }
}
