package com.github.dilika.tailwindsmartplugin.documentation.enhanced

import com.intellij.openapi.diagnostic.logger

/**
 * Provides enhanced documentation for Tailwind CSS classes
 */
class TailwindEnhancedDocumentation {
    private val logger = logger<TailwindEnhancedDocumentation>()

    /**
     * Generates enhanced documentation for a Tailwind CSS class
     */
    fun generateDocumentation(className: String): String? {
        try {
            logger.info("Generating enhanced documentation for: $className")
            
            // For now, return a simple HTML documentation
            // In a real implementation, this would generate more detailed documentation
            // with examples, CSS equivalents, and other useful information
            return """
                <html>
                    <head>
                        <style>
                            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif; }
                            .doc-container { padding: 12px; max-width: 800px; }
                            .class-name { 
                                font-family: 'JetBrains Mono', monospace; 
                                background: #f5f5f5; 
                                padding: 2px 6px; 
                                border-radius: 3px;
                                font-size: 14px;
                            }
                            .section { margin: 16px 0; }
                            .section-title { 
                                font-weight: 600; 
                                margin-bottom: 8px;
                                color: #2c3e50;
                            }
                            .example { 
                                background: #f8f9fa; 
                                padding: 8px 12px; 
                                border-radius: 4px; 
                                margin: 8px 0;
                                font-family: 'JetBrains Mono', monospace;
                                font-size: 13px;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="doc-container">
                            <h2 style="margin-top: 0; color: #2c3e50;">${escapeHtml(className)}</h2>
                            
                            <div class="section">
                                <div class="section-title">Description</div>
                                <p>${getClassDescription(className)}</p>
                            </div>
                            
                            <div class="section">
                                <div class="section-title">Example</div>
                                <div class="example">
                                    &lt;div class="${escapeHtml(className)}"&gt;Content&lt;/div&gt;
                                </div>
                            </div>
                            
                            <div class="section">
                                <div class="section-title">CSS Equivalent</div>
                                <div class="example">
                                    ${getCssEquivalent(className) ?: "No direct CSS equivalent"}
                                </div>
                            </div>
                            
                            <div class="section" style="font-size: 12px; color: #666; margin-top: 24px;">
                                <p>Documentation provided by Tailwind Smart Plugin</p>
                            </div>
                        </div>
                    </body>
                </html>
            """.trimIndent()
            
        } catch (e: Exception) {
            logger.error("Error generating enhanced documentation: ${e.message}", e)
            return null
        }
    }
    
    private fun getClassDescription(className: String): String {
        // This is a simplified example - in a real implementation, you would have a more
        // comprehensive mapping of Tailwind classes to their descriptions
        return when {
            className.startsWith("bg-") -> "Sets the background color of an element."
            className.startsWith("text-") -> "Sets the text color of an element."
            className.startsWith("p-") -> "Sets the padding of an element."
            className.startsWith("m-") -> "Sets the margin of an element."
            className.startsWith("w-") -> "Sets the width of an element."
            className.startsWith("h-") -> "Sets the height of an element."
            className.startsWith("border") -> "Sets the border properties of an element."
            className.startsWith("rounded") -> "Sets the border radius of an element."
            className.startsWith("flex") -> "Sets a flex container and controls the flex behavior of its children."
            className.startsWith("grid") -> "Sets a grid container and controls the grid behavior of its children."
            className.startsWith("hover:") -> "Applies styles on hover."
            className.startsWith("focus:") -> "Applies styles when the element is focused."
            className.startsWith("active:") -> "Applies styles when the element is active."
            className.startsWith("dark:") -> "Applies styles when dark mode is enabled."
            className.startsWith("sm:") -> "Applies styles on small screens (640px and up)."
            className.startsWith("md:") -> "Applies styles on medium screens (768px and up)."
            className.startsWith("lg:") -> "Applies styles on large screens (1024px and up)."
            className.startsWith("xl:") -> "Applies styles on extra large screens (1280px and up)."
            className.startsWith("2xl:") -> "Applies styles on 2x extra large screens (1536px and up)."
            className.contains("[") && className.contains("]") -> "Arbitrary value: ${className.substring(className.indexOf('[') + 1, className.indexOf(']'))}"
            else -> "A Tailwind CSS utility class."
        }
    }
    
    private fun getCssEquivalent(className: String): String? {
        // This is a simplified example - in a real implementation, you would have a more
        // comprehensive mapping of Tailwind classes to their CSS equivalents
        return when {
            className.startsWith("bg-") -> "background-color: ${getColorValue(className.removePrefix("bg-"))};"
            className.startsWith("text-") -> "color: ${getColorValue(className.removePrefix("text-"))};"
            className.startsWith("p-") -> "padding: ${getSpacingValue(className.removePrefix("p-"))};"
            className.startsWith("m-") -> "margin: ${getSpacingValue(className.removePrefix("m-"))};"
            className == "flex" -> "display: flex;"
            className == "hidden" -> "display: none;"
            className.startsWith("w-") -> "width: ${getWidthValue(className.removePrefix("w-"))};"
            className.startsWith("h-") -> "height: ${getHeightValue(className.removePrefix("h-"))};"
            className.startsWith("border") -> getBorderValue(className)
            className.startsWith("rounded") -> getBorderRadiusValue(className)
            else -> null
        }
    }
    
    private fun getColorValue(colorClass: String): String {
        // Simplified color mapping - in a real implementation, you would use the full Tailwind color palette
        return when (colorClass) {
            "black" -> "#000000"
            "white" -> "#ffffff"
            "gray-100" -> "#f3f4f6"
            "gray-200" -> "#e5e7eb"
            "gray-300" -> "#d1d5db"
            "gray-400" -> "#9ca3af"
            "gray-500" -> "#6b7280"
            "gray-600" -> "#4b5563"
            "gray-700" -> "#374151"
            "gray-800" -> "#1f2937"
            "gray-900" -> "#111827"
            "red-500" -> "#ef4444"
            "blue-500" -> "#3b82f6"
            "green-500" -> "#10b981"
            "yellow-500" -> "#f59e0b"
            "purple-500" -> "#8b5cf6"
            "pink-500" -> "#ec4899"
            else -> {
                // For arbitrary values like bg-[#ff0000]
                if (colorClass.startsWith("[") && colorClass.endsWith("]")) {
                    colorClass.removeSurrounding("[", "]")
                } else {
                    "#000000" // Default black
                }
            }
        }
    }
    
    private fun getSpacingValue(size: String): String {
        return when (size) {
            "0" -> "0"
            "px" -> "1px"
            "0.5" -> "0.125rem"
            "1" -> "0.25rem"
            "1.5" -> "0.375rem"
            "2" -> "0.5rem"
            "2.5" -> "0.625rem"
            "3" -> "0.75rem"
            "3.5" -> "0.875rem"
            "4" -> "1rem"
            "5" -> "1.25rem"
            "6" -> "1.5rem"
            "8" -> "2rem"
            "10" -> "2.5rem"
            "12" -> "3rem"
            "16" -> "4rem"
            "20" -> "5rem"
            "24" -> "6rem"
            "32" -> "8rem"
            "40" -> "10rem"
            "48" -> "12rem"
            "56" -> "14rem"
            "64" -> "16rem"
            else -> {
                // For arbitrary values like p-[10px]
                if (size.startsWith("[") && size.endsWith("]")) {
                    size.removeSurrounding("[", "]")
                } else {
                    "0"
                }
            }
        }
    }
    
    private fun getWidthValue(size: String): String {
        return when (size) {
            "auto" -> "auto"
            "1/2" -> "50%"
            "1/3" -> "33.333333%"
            "2/3" -> "66.666667%"
            "1/4" -> "25%"
            "2/4" -> "50%"
            "3/4" -> "75%"
            "1/5" -> "20%"
            "2/5" -> "40%"
            "3/5" -> "60%"
            "4/5" -> "80%"
            "1/6" -> "16.666667%"
            "2/6" -> "33.333333%"
            "3/6" -> "50%"
            "4/6" -> "66.666667%"
            "5/6" -> "83.333333%"
            "full" -> "100%"
            "screen" -> "100vw"
            "min" -> "min-content"
            "max" -> "max-content"
            else -> getSpacingValue(size)
        }
    }
    
    private fun getHeightValue(size: String): String {
        return when (size) {
            "auto" -> "auto"
            "full" -> "100%"
            "screen" -> "100vh"
            else -> getSpacingValue(size)
        }
    }
    
    private fun getBorderValue(borderClass: String): String {
        return when (borderClass) {
            "border" -> "border-width: 1px; border-style: solid;"
            "border-0" -> "border-width: 0;"
            "border-2" -> "border-width: 2px; border-style: solid;"
            "border-4" -> "border-width: 4px; border-style: solid;"
            "border-8" -> "border-width: 8px; border-style: solid;"
            "border-t" -> "border-top-width: 1px; border-top-style: solid;"
            "border-r" -> "border-right-width: 1px; border-right-style: solid;"
            "border-b" -> "border-bottom-width: 1px; border-bottom-style: solid;"
            "border-l" -> "border-left-width: 1px; border-left-style: solid;"
            else -> {
                if (borderClass.startsWith("border-")) {
                    val color = borderClass.removePrefix("border-")
                    if (color.isNotBlank()) {
                        "border-color: ${getColorValue(color)};"
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        } ?: ""
    }
    
    private fun getBorderRadiusValue(radiusClass: String): String {
        return when (radiusClass) {
            "rounded" -> "border-radius: 0.25rem;"
            "rounded-none" -> "border-radius: 0;"
            "rounded-sm" -> "border-radius: 0.125rem;"
            "rounded-md" -> "border-radius: 0.375rem;"
            "rounded-lg" -> "border-radius: 0.5rem;"
            "rounded-xl" -> "border-radius: 0.75rem;"
            "rounded-2xl" -> "border-radius: 1rem;"
            "rounded-3xl" -> "border-radius: 1.5rem;"
            "rounded-full" -> "border-radius: 9999px;"
            else -> ""
        }
    }
    
    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
