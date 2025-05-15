package com.github.dilika.tailwindsmartplugin.jit

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.github.dilika.tailwindsmartplugin.utils.RoundColorIcon
import com.github.dilika.tailwindsmartplugin.utils.ColorIcon
import com.intellij.ui.JBColor
import java.awt.Color
import com.intellij.openapi.diagnostic.Logger
import java.util.regex.Pattern

/**
 * Support avancé pour les fonctionnalités JIT (Just-In-Time) de Tailwind CSS
 * Permet la complétion des valeurs arbitraires et des classes dynamiques
 */
class TailwindJitSupport {
    private val logger = Logger.getInstance(TailwindJitSupport::class.java)
    
    // Modèles de regex pour détecter différents types de valeurs arbitraires
    private val colorPattern = Pattern.compile("(bg|text|border|fill|stroke|ring|shadow|accent|caret|decoration)-\\[(#|rgb|rgba|hsl|hsla)?.*?\\]?")
    private val sizePattern = Pattern.compile("(w|h|min-w|min-h|max-w|max-h|p|px|py|pt|pr|pb|pl|m|mx|my|mt|mr|mb|ml|gap|space-x|space-y|inset|top|right|bottom|left)-\\[.*?\\]?")
    private val cssPropertyPattern = Pattern.compile("\\[(.*?):(.*?)\\]?")
    private val mediaQueryPattern = Pattern.compile("(@media|@container|@supports)-\\[.*?\\]?")
    
    /**
     * Analyse une classe JIT arbitraire et génère des suggestions appropriées
     * @param prefix Préfixe de saisie actuel
     * @param result Ensemble de résultats de complétion à remplir
     */
    fun processArbitraryValue(prefix: String, result: CompletionResultSet) {
        try {
            when {
                // Valeur de couleur arbitraire
                isColorValue(prefix) -> {
                    suggestArbitraryColors(result, prefix)
                }
                
                // Valeur de taille arbitraire
                isSizeValue(prefix) -> {
                    suggestArbitrarySizes(result, prefix)
                }
                
                // Valeur CSS personnalisée
                isCssPropertyValue(prefix) -> {
                    suggestCssProperties(result, prefix)
                }
                
                // Valeur de média query
                isMediaQueryValue(prefix) -> {
                    suggestMediaQueries(result, prefix)
                }
                
                // Sélecteur CSS arbitraire
                isCssSelectorValue(prefix) -> {
                    suggestCssSelectors(result, prefix)
                }
            }
        } catch (e: Exception) {
            logger.error("Erreur lors du traitement de la valeur arbitraire: $prefix", e)
        }
    }
    
    /**
     * Vérifie si la valeur est une couleur arbitraire
     */
    private fun isColorValue(value: String): Boolean {
        return colorPattern.matcher(value).find()
    }
    
    /**
     * Vérifie si la valeur est une taille arbitraire
     */
    private fun isSizeValue(value: String): Boolean {
        return sizePattern.matcher(value).find()
    }
    
    /**
     * Vérifie si la valeur est une propriété CSS arbitraire
     */
    private fun isCssPropertyValue(value: String): Boolean {
        return cssPropertyPattern.matcher(value).find()
    }
    
    /**
     * Vérifie si la valeur est une media query arbitraire
     */
    private fun isMediaQueryValue(value: String): Boolean {
        return mediaQueryPattern.matcher(value).find()
    }
    
    /**
     * Vérifie si la valeur est un sélecteur CSS arbitraire
     */
    private fun isCssSelectorValue(value: String): Boolean {
        return value.contains("[&") || value.contains("[.")
    }
    
    /**
     * Suggère des couleurs arbitraires
     */
    private fun suggestArbitraryColors(result: CompletionResultSet, prefix: String) {
        // Extraire le préfixe de la propriété (bg, text, etc.)
        val propertyPrefix = extractPropertyPrefix(prefix)
        
        // Couleurs hexadécimales courantes
        val commonColors = listOf(
            "#000000", "#ffffff", "#f8fafc", "#f1f5f9", "#e2e8f0", "#cbd5e1", "#94a3b8", 
            "#64748b", "#475569", "#334155", "#1e293b", "#0f172a", "#ef4444", "#3b82f6", "#22c55e"
        )
        
        // Couleurs RGB/RGBA
        val rgbColors = listOf(
            "rgb(0, 0, 0)", "rgb(255, 255, 255)", "rgb(239, 68, 68)", "rgb(59, 130, 246)", 
            "rgba(0, 0, 0, 0.5)", "rgba(255, 255, 255, 0.8)", "rgba(239, 68, 68, 0.75)"
        )
        
        // Couleurs HSL/HSLA
        val hslColors = listOf(
            "hsl(0, 0%, 0%)", "hsl(0, 0%, 100%)", "hsl(0, 84%, 60%)", "hsl(217, 91%, 60%)",
            "hsla(0, 0%, 0%, 0.5)", "hsla(0, 0%, 100%, 0.8)", "hsla(0, 84%, 60%, 0.75)"
        )
        
        // Ajouter les suggestions de couleurs
        commonColors.forEach { color ->
            addColorSuggestion(result, propertyPrefix, color, 1000.0)
        }
        
        rgbColors.forEach { color ->
            addColorSuggestion(result, propertyPrefix, color, 900.0)
        }
        
        hslColors.forEach { color ->
            addColorSuggestion(result, propertyPrefix, color, 800.0)
        }
        
        // Variables CSS
        listOf("var(--color-primary)", "var(--color-secondary)", "var(--color-accent)").forEach { color ->
            addColorSuggestion(result, propertyPrefix, color, 700.0)
        }
    }
    
    /**
     * Ajoute une suggestion de couleur au résultat
     */
    private fun addColorSuggestion(result: CompletionResultSet, propertyPrefix: String, color: String, priority: Double) {
        val lookupString = "$propertyPrefix-[$color]"
        val presentableText = "$propertyPrefix-[$color]"
        
        val element = LookupElementBuilder.create(lookupString)
            .withPresentableText(presentableText)
            .withTypeText("Couleur arbitraire")
            .withIcon(createColorIcon(color))
        
        result.addElement(PrioritizedLookupElement.withPriority(element, priority))
    }
    
    /**
     * Creates a color icon from a color value
     */
    private fun createColorIcon(colorValue: String): RoundColorIcon {
        // Default icon size
        val iconSize = 14
        
        try {
            return when {
                colorValue.startsWith("#") -> {
                    RoundColorIcon(iconSize, parseHexColor(colorValue))
                }
                colorValue.startsWith("rgb") -> {
                    RoundColorIcon(iconSize, parseRgbColor(colorValue))
                }
                colorValue.startsWith("hsl") -> {
                    RoundColorIcon(iconSize, parseHslColor(colorValue))
                }
                else -> {
                    RoundColorIcon(iconSize, Color(JBColor(0x000000, 0x000000).getRGB()))
                }
            }
        } catch (e: Exception) {
            logger.warn("Unable to create color icon for: $colorValue", e)
            return RoundColorIcon(iconSize, Color(JBColor(0x000000, 0x000000).getRGB()))
        }
    }
    
    /**
     * Parse une couleur hexadécimale
     */
    private fun parseHexColor(hexColor: String): Color {
        try {
            val colorStr = hexColor.trim().removePrefix("#")
            return when (colorStr.length) {
                3 -> {
                    // Format #RGB
                    val r = colorStr.substring(0, 1).repeat(2).toInt(16)
                    val g = colorStr.substring(1, 2).repeat(2).toInt(16)
                    val b = colorStr.substring(2, 3).repeat(2).toInt(16)
                    Color(r, g, b)
                }
                6 -> {
                    // Format #RRGGBB
                    Color(colorStr.toInt(16))
                }
                8 -> {
                    // Format #RRGGBBAA
                    val r = colorStr.substring(0, 2).toInt(16)
                    val g = colorStr.substring(2, 4).toInt(16)
                    val b = colorStr.substring(4, 6).toInt(16)
                    val a = colorStr.substring(6, 8).toInt(16)
                    Color(r, g, b, a)
                }
                else -> JBColor(0x000000, 0x000000)
            }
        } catch (e: Exception) {
            logger.warn("Impossible de parser la couleur hex: $hexColor", e)
            return JBColor(0x000000, 0x000000)
        }
    }
    
    /**
     * Parse une couleur RGB/RGBA
     */
    private fun parseRgbColor(rgbColor: String): Color {
        try {
            val isRgba = rgbColor.startsWith("rgba")
            val valuesStr = rgbColor.substringAfter("(").substringBefore(")")
            val values = valuesStr.split(",").map { it.trim() }
            
            return when {
                isRgba && values.size >= 4 -> {
                    val r = values[0].toInt().coerceIn(0, 255)
                    val g = values[1].toInt().coerceIn(0, 255)
                    val b = values[2].toInt().coerceIn(0, 255)
                    val a = (values[3].toFloat().coerceIn(0f, 1f) * 255).toInt()
                    Color(r, g, b, a)
                }
                values.size >= 3 -> {
                    val r = values[0].toInt().coerceIn(0, 255)
                    val g = values[1].toInt().coerceIn(0, 255)
                    val b = values[2].toInt().coerceIn(0, 255)
                    Color(r, g, b)
                }
                else -> JBColor(0x000000, 0x000000)
            }
        } catch (e: Exception) {
            logger.warn("Impossible de parser la couleur RGB: $rgbColor", e)
            return JBColor(0x000000, 0x000000)
        }
    }
    
    /**
     * Parse une couleur HSL/HSLA
     */
    private fun parseHslColor(hslColor: String): Color {
        try {
            val isHsla = hslColor.startsWith("hsla")
            val valuesStr = hslColor.substringAfter("(").substringBefore(")")
            val values = valuesStr.split(",").map { it.trim() }
            
            if (values.size >= 3) {
                val h = values[0].toFloat() / 360f
                val s = values[1].removeSuffix("%").toFloat() / 100f
                val l = values[2].removeSuffix("%").toFloat() / 100f
                
                val alpha = if (isHsla && values.size >= 4) {
                    values[3].toFloat().coerceIn(0f, 1f)
                } else {
                    1.0f
                }
                
                return hslToRgb(h, s, l, alpha)
            }
            
            return JBColor(0x000000, 0x000000)
        } catch (e: Exception) {
            logger.warn("Impossible de parser la couleur HSL: $hslColor", e)
            return JBColor(0x000000, 0x000000)
        }
    }
    
    /**
     * Convertit HSL en RGB
     */
    private fun hslToRgb(h: Float, s: Float, l: Float, alpha: Float = 1.0f): Color {
        val c = (1 - Math.abs(2 * l - 1)) * s
        val x = c * (1 - Math.abs((h * 6) % 2 - 1))
        val m = l - c / 2
        
        val (r1, g1, b1) = when {
            h < 1/6f -> Triple(c, x, 0f)
            h < 2/6f -> Triple(x, c, 0f)
            h < 3/6f -> Triple(0f, c, x)
            h < 4/6f -> Triple(0f, x, c)
            h < 5/6f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        
        val r = ((r1 + m) * 255).toInt().coerceIn(0, 255)
        val g = ((g1 + m) * 255).toInt().coerceIn(0, 255)
        val b = ((b1 + m) * 255).toInt().coerceIn(0, 255)
        val a = (alpha * 255).toInt().coerceIn(0, 255)
        
        return Color(r, g, b, a)
    }
    
    /**
     * Extrait le préfixe de propriété d'une valeur arbitraire
     */
    private fun extractPropertyPrefix(value: String): String {
        val bracketIndex = value.indexOf("[")
        return if (bracketIndex > 0) {
            value.substring(0, bracketIndex)
        } else {
            value
        }
    }
    
    /**
     * Suggère des tailles arbitraires
     */
    private fun suggestArbitrarySizes(result: CompletionResultSet, prefix: String) {
        val propertyPrefix = extractPropertyPrefix(prefix)
        
        // Unités communes pour les tailles
        val commonUnits = listOf("px", "rem", "em", "%", "vh", "vw", "vmin", "vmax", "ch")
        
        // Valeurs communes pour chaque unité
        val commonValues = listOf(
            // Pixels
            "0px", "1px", "2px", "4px", "8px", "12px", "16px", "20px", "24px", "32px", "40px", "48px", "56px", "64px", "80px", "96px", "128px", "160px", "192px", "224px", "256px", "320px", "384px", "448px", "512px", "640px", "768px", "896px", "1024px", "1152px", "1280px", "1536px",
            
            // REM
            "0rem", "0.25rem", "0.5rem", "0.75rem", "1rem", "1.25rem", "1.5rem", "1.75rem", "2rem", "2.25rem", "2.5rem", "2.75rem", "3rem", "3.5rem", "4rem", "5rem", "6rem", "7rem", "8rem", "9rem", "10rem", "11rem", "12rem", "13rem", "14rem", "15rem", "16rem",
            
            // EM
            "0em", "0.25em", "0.5em", "0.75em", "1em", "1.25em", "1.5em", "1.75em", "2em", "2.5em", "3em", "4em", "5em", "6em", "7em", "8em", "9em", "10em",
            
            // Pourcentage
            "0%", "5%", "10%", "15%", "20%", "25%", "30%", "33%", "40%", "50%", "60%", "66%", "70%", "75%", "80%", "90%", "95%", "100%",
            
            // Viewport
            "10vh", "20vh", "30vh", "40vh", "50vh", "60vh", "70vh", "80vh", "90vh", "100vh",
            "10vw", "20vw", "30vw", "40vw", "50vw", "60vw", "70vw", "80vw", "90vw", "100vw",
            
            // Caractères
            "1ch", "2ch", "3ch", "4ch", "5ch", "10ch", "15ch", "20ch", "25ch", "30ch", "35ch", "40ch", "45ch", "50ch", "60ch", "70ch", "80ch",
            
            // Fractions
            "1/2", "1/3", "2/3", "1/4", "2/4", "3/4", "1/5", "2/5", "3/5", "4/5", "1/6", "5/6", "1/12", "5/12", "7/12", "11/12",
            
            // Valeurs spéciales
            "auto", "min-content", "max-content", "fit-content", "calc(100% - 2rem)", "calc(100vh - 80px)", "clamp(10rem, 5vw, 20rem)"
        )
        
        // Ajouter les suggestions de tailles
        commonValues.forEach { value ->
            val lookupString = "$propertyPrefix-[$value]"
            val presentableText = "$propertyPrefix-[$value]"
            
            val element = LookupElementBuilder.create(lookupString)
                .withPresentableText(presentableText)
                .withTypeText("Taille arbitraire")
            
            result.addElement(PrioritizedLookupElement.withPriority(element, 900.0))
        }
        
        // Suggérer des modèles pour des valeurs personnalisées
        if (propertyPrefix.contains("w") || propertyPrefix.contains("h") || propertyPrefix.contains("size")) {
            val customSizeTemplates = listOf(
                "calc(100% - 2rem)", 
                "calc(100vh - 80px)", 
                "clamp(10rem, 5vw, 20rem)",
                "min(100px, 10vw)",
                "max(50%, 300px)"
            )
            
            customSizeTemplates.forEach { template ->
                val lookupString = "$propertyPrefix-[$template]"
                val presentableText = "$propertyPrefix-[$template]"
                
                val element = LookupElementBuilder.create(lookupString)
                    .withPresentableText(presentableText)
                    .withTypeText("Fonction de taille")
                
                result.addElement(PrioritizedLookupElement.withPriority(element, 950.0))
            }
        }
    }
    
    /**
     * Suggère des propriétés CSS arbitraires
     */
    private fun suggestCssProperties(result: CompletionResultSet, prefix: String) {
        // Propriétés CSS courantes
        val commonCssProperties = listOf(
            "display: flex", "display: grid", "display: block", "display: inline-block", "display: none",
            "position: relative", "position: absolute", "position: fixed", "position: sticky",
            "overflow: hidden", "overflow: auto", "overflow: scroll", "overflow: visible",
            "z-index: 10", "z-index: 20", "z-index: 30", "z-index: 40", "z-index: 50",
            "opacity: 0", "opacity: 0.5", "opacity: 1",
            "cursor: pointer", "cursor: not-allowed", "cursor: wait", "cursor: move",
            "transform: rotate(45deg)", "transform: scale(1.5)", "transform: translateY(-10px)",
            "filter: blur(4px)", "filter: brightness(1.2)", "filter: contrast(1.5)",
            "transition: all 300ms ease", "animation: spin 1s linear infinite",
            "grid-template-columns: repeat(3, 1fr)", "grid-template-rows: auto 1fr auto",
            "aspect-ratio: 16/9", "object-fit: cover", "object-position: center",
            "user-select: none", "pointer-events: none", "touch-action: manipulation",
            "backdrop-filter: blur(10px)", "mix-blend-mode: multiply",
            "text-decoration: underline", "text-transform: uppercase", "white-space: nowrap",
            "writing-mode: vertical-rl", "direction: rtl", "unicode-bidi: isolate",
            "clip-path: circle(50%)", "mask-image: linear-gradient(to bottom, black, transparent)",
            "scroll-behavior: smooth", "scroll-snap-type: x mandatory",
            "content-visibility: auto", "contain: layout paint",
            "gap: 1rem", "column-gap: 2rem", "row-gap: 1.5rem",
            "font-variant-numeric: tabular-nums", "font-feature-settings: 'tnum'",
            "box-decoration-break: clone", "print-color-adjust: exact"
        )
        
        // Ajouter les suggestions de propriétés CSS
        commonCssProperties.forEach { property ->
            val lookupString = "[$property]"
            val presentableText = "[$property]"
            
            val element = LookupElementBuilder.create(lookupString)
                .withPresentableText(presentableText)
                .withTypeText("Propriété CSS arbitraire")
            
            result.addElement(PrioritizedLookupElement.withPriority(element, 850.0))
        }
    }
    
    /**
     * Suggère des media queries arbitraires
     */
    private fun suggestMediaQueries(result: CompletionResultSet, prefix: String) {
        val propertyPrefix = extractPropertyPrefix(prefix)
        
        // Media queries courantes
        val commonMediaQueries = listOf(
            // Responsive
            "min-width: 640px", "min-width: 768px", "min-width: 1024px", "min-width: 1280px", "min-width: 1536px",
            "max-width: 639px", "max-width: 767px", "max-width: 1023px", "max-width: 1279px", "max-width: 1535px",
            
            // Orientation
            "orientation: portrait", "orientation: landscape",
            
            // Préférences
            "prefers-color-scheme: dark", "prefers-color-scheme: light",
            "prefers-reduced-motion: reduce", "prefers-reduced-motion: no-preference",
            "prefers-contrast: more", "prefers-contrast: less",
            
            // Impression
            "print", "screen", "print and (min-resolution: 300dpi)",
            
            // Dispositifs
            "hover: hover", "pointer: fine", "pointer: coarse",
            "any-hover: hover", "any-pointer: fine",
            
            // Container queries
            "container-type: inline-size", "container-type: size",
            
            // Supports
            "supports(display: grid)", "supports(display: flex)",
            "supports(backdrop-filter: blur(10px))", "supports(mask-image: linear-gradient(black, transparent))"
        )
        
        // Ajouter les suggestions de media queries
        commonMediaQueries.forEach { query ->
            val lookupString = "$propertyPrefix-[$query]"
            val presentableText = "$propertyPrefix-[$query]"
            
            val element = LookupElementBuilder.create(lookupString)
                .withPresentableText(presentableText)
                .withTypeText("Media query arbitraire")
            
            result.addElement(PrioritizedLookupElement.withPriority(element, 800.0))
        }
    }
    
    /**
     * Suggère des sélecteurs CSS arbitraires
     */
    private fun suggestCssSelectors(result: CompletionResultSet, prefix: String) {
        // Sélecteurs CSS courants
        val commonCssSelectors = listOf(
            // Sélecteurs d'enfants
            "&>*", "&>div", "&>p", "&>span", "&>a", "&>img", "&>ul", "&>li",
            
            // Sélecteurs de pseudo-classes
            "&:hover", "&:focus", "&:active", "&:visited", "&:first-child", "&:last-child",
            "&:nth-child(odd)", "&:nth-child(even)", "&:nth-child(3)", "&:nth-of-type(2)",
            
            // Sélecteurs de pseudo-éléments
            "&::before", "&::after", "&::placeholder", "&::selection", "&::first-line", "&::first-letter",
            
            // Sélecteurs d'attributs
            "&[disabled]", "&[type=\"text\"]", "&[aria-hidden=\"true\"]", "&[data-state=\"active\"]",
            
            // Sélecteurs de combinaison
            "&+div", "&~p", "&:not(:last-child)", "&:is(:hover, :focus)",
            
            // Sélecteurs de descendants
            "& div", "& p", "& a", "& img", "& .class-name", "& #id-name",
            
            // Sélecteurs de parent
            ".parent &", "div>&", "h1~&", ".sibling+&",
            
            // Sélecteurs complexes
            "&:where(:not(:first-child))", "&:has(> img)", "&:focus-within:not([disabled])"
        )
        
        // Ajouter les suggestions de sélecteurs CSS
        commonCssSelectors.forEach { selector ->
            val lookupString = "[$selector]:"
            val presentableText = "[$selector]:"
            
            val element = LookupElementBuilder.create(lookupString)
                .withPresentableText(presentableText)
                .withTypeText("Sélecteur CSS arbitraire")
            
            result.addElement(PrioritizedLookupElement.withPriority(element, 750.0))
        }
    }
}
