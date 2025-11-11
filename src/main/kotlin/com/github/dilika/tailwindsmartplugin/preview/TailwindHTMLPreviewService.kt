package com.github.dilika.tailwindsmartplugin.preview

import com.intellij.openapi.diagnostic.Logger
import java.util.regex.Pattern

/**
 * Service de preview HTML réel avec rendu Tailwind CSS
 * Génère du HTML avec Tailwind CDN pour un rendu visuel parfait
 */
class TailwindHTMLPreviewService {
    
    private val logger = Logger.getInstance(TailwindHTMLPreviewService::class.java)
    
    /**
     * Génère du HTML avec Tailwind CDN pour un preview réel
     */
    fun generateHTMLPreview(
        classes: List<String>,
        width: Int = 400,
        height: Int = 300,
        darkMode: Boolean = false,
        breakpoint: String = ""
    ): String {
        val classesString = classes.joinToString(" ")
        val darkModeClass = if (darkMode) "dark" else ""
        val breakpointPrefix = if (breakpoint.isNotEmpty()) "$breakpoint:" else ""
        
        // Appliquer le breakpoint aux classes qui n'ont pas déjà un variant
        val classesWithBreakpoint = classes.map { className ->
            if (!className.contains(":") && breakpoint.isNotEmpty()) {
                "$breakpointPrefix$className"
            } else {
                className
            }
        }
        
        val finalClasses = if (darkMode) {
            classesWithBreakpoint + "dark:bg-gray-900 dark:text-white"
        } else {
            classesWithBreakpoint
        }
        
        return """
        <!DOCTYPE html>
        <html lang="en" class="$darkModeClass">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <script src="https://cdn.tailwindcss.com"></script>
            <style>
                body {
                    margin: 0;
                    padding: 20px;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                    background: ${if (darkMode) "#111827" else "#f9fafb"};
                    color: ${if (darkMode) "#ffffff" else "#000000"};
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    min-height: 100vh;
                }
                .preview-container {
                    width: ${width}px;
                    min-height: ${height}px;
                    border: 2px dashed ${if (darkMode) "#374151" else "#d1d5db"};
                    border-radius: 8px;
                    padding: 20px;
                    background: ${if (darkMode) "#1f2937" else "#ffffff"};
                    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
                }
                .preview-element {
                    ${finalClasses.joinToString(" ")}
                }
                .info-panel {
                    margin-top: 20px;
                    padding: 12px;
                    background: ${if (darkMode) "#374151" else "#f3f4f6"};
                    border-radius: 6px;
                    font-size: 12px;
                    font-family: 'SF Mono', Monaco, monospace;
                }
                .class-badge {
                    display: inline-block;
                    padding: 2px 6px;
                    margin: 2px;
                    background: ${if (darkMode) "#4b5563" else "#e5e7eb"};
                    border-radius: 4px;
                    font-size: 11px;
                }
            </style>
        </head>
        <body>
            <div class="preview-container">
                <div class="preview-element">
                    ${generatePreviewContent(classes)}
                </div>
                <div class="info-panel">
                    <strong>Classes:</strong><br>
                    ${finalClasses.joinToString("<br>") { "<span class='class-badge'>$it</span>" }}
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
    
    /**
     * Génère le contenu du preview basé sur les classes
     */
    private fun generatePreviewContent(classes: List<String>): String {
        // Détecter le type de composant
        val isButton = classes.any { it.contains("button") || it.contains("btn") || 
                                    (it.contains("px-") && it.contains("py-") && it.contains("bg-")) }
        val isCard = classes.any { it.contains("card") || 
                                  (it.contains("bg-white") && it.contains("shadow") && it.contains("rounded")) }
        val isBadge = classes.any { it.contains("badge") || 
                                   (it.contains("px-") && it.contains("py-") && it.contains("rounded-full")) }
        val isInput = classes.any { it.contains("input") || it.contains("border") && it.contains("px-") && it.contains("py-") }
        
        return when {
            isButton -> {
                val text = if (classes.any { it.contains("text-") }) "Click me" else "Button"
                "<button>$text</button>"
            }
            isCard -> {
                """
                <div>
                    <h3 class="text-lg font-bold mb-2">Card Title</h3>
                    <p class="text-gray-600">This is a card component with Tailwind CSS classes.</p>
                </div>
                """.trimIndent()
            }
            isBadge -> {
                "<span>Badge</span>"
            }
            isInput -> {
                "<input type='text' placeholder='Enter text...' />"
            }
            else -> {
                // Générer un preview générique basé sur les classes
                val hasText = classes.any { it.startsWith("text-") }
                val hasBg = classes.any { it.startsWith("bg-") }
                val hasPadding = classes.any { it.startsWith("p-") }
                
                when {
                    hasText && hasBg && hasPadding -> "Preview Text"
                    hasText -> "Text Preview"
                    hasBg -> "Background Preview"
                    else -> "Tailwind Preview"
                }
            }
        }
    }
    
    /**
     * Génère un preview HTML simplifié pour JEditorPane
     */
    fun generateSimpleHTMLPreview(classes: List<String>): String {
        val classesString = classes.joinToString(" ")
        
        return """
        <html>
        <head>
            <style>
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                    margin: 0;
                    padding: 20px;
                    background: #f9fafb;
                }
                .preview-box {
                    ${convertClassesToInlineCSS(classes)}
                    min-height: 100px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    border: 1px solid #e5e7eb;
                    border-radius: 8px;
                    padding: 16px;
                }
            </style>
        </head>
        <body>
            <div class="preview-box">
                Preview
            </div>
            <div style="margin-top: 12px; font-size: 12px; color: #6b7280;">
                <strong>Classes:</strong> $classesString
            </div>
        </body>
        </html>
        """.trimIndent()
    }
    
    /**
     * Convertit les classes Tailwind en CSS inline (approximation)
     */
    private fun convertClassesToInlineCSS(classes: List<String>): String {
        val css = StringBuilder()
        
        classes.forEach { className ->
            when {
                className.startsWith("bg-") -> {
                    val color = parseTailwindColor(className.substring(3))
                    css.append("background-color: $color; ")
                }
                className.startsWith("text-") -> {
                    val color = parseTailwindColor(className.substring(5))
                    css.append("color: $color; ")
                }
                className.startsWith("p-") -> {
                    val padding = parseSpacing(className.substring(2))
                    css.append("padding: ${padding}px; ")
                }
                className.startsWith("m-") -> {
                    val margin = parseSpacing(className.substring(2))
                    css.append("margin: ${margin}px; ")
                }
                className.startsWith("rounded") -> {
                    val radius = parseBorderRadius(className)
                    css.append("border-radius: ${radius}px; ")
                }
                className.contains("font-bold") -> {
                    css.append("font-weight: bold; ")
                }
                className.contains("text-center") -> {
                    css.append("text-align: center; ")
                }
                className.contains("flex") -> {
                    css.append("display: flex; ")
                }
            }
        }
        
        return css.toString()
    }
    
    /**
     * Parse une couleur Tailwind en valeur CSS
     */
    private fun parseTailwindColor(colorName: String): String {
        // Mapping des couleurs Tailwind communes
        val colorMap = mapOf(
            "red-500" to "#ef4444",
            "blue-500" to "#3b82f6",
            "green-500" to "#22c55e",
            "yellow-500" to "#eab308",
            "purple-500" to "#9333ea",
            "pink-500" to "#ec4899",
            "indigo-500" to "#6366f1",
            "gray-500" to "#6b7280",
            "gray-800" to "#1f2937",
            "gray-900" to "#111827",
            "black" to "#000000",
            "white" to "#ffffff",
            "transparent" to "transparent"
        )
        
        return colorMap[colorName] ?: "#6b7280"
    }
    
    /**
     * Parse l'espacement Tailwind
     */
    private fun parseSpacing(spacing: String): Int {
        return when (spacing) {
            "0" -> 0
            "1" -> 4
            "2" -> 8
            "3" -> 12
            "4" -> 16
            "5" -> 20
            "6" -> 24
            "8" -> 32
            "10" -> 40
            "12" -> 48
            "16" -> 64
            else -> 8
        }
    }
    
    /**
     * Parse le border radius
     */
    private fun parseBorderRadius(className: String): Int {
        return when {
            className.contains("none") -> 0
            className.contains("sm") -> 2
            className.contains("md") -> 6
            className.contains("lg") -> 8
            className.contains("xl") -> 12
            className.contains("2xl") -> 16
            className.contains("full") -> 9999
            else -> 4
        }
    }
}

