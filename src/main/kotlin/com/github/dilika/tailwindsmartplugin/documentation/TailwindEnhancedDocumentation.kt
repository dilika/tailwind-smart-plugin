package com.github.dilika.tailwindsmartplugin.documentation

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap

/**
 * Provides enhanced documentation for Tailwind classes with rich interactive examples, 
 * visual previews, complete CSS equivalents, and responsive variants information.
 * 
 * This documentation provider is designed to offer a premium documentation experience
 * comparable to or better than the official Tailwind CSS plugin for IntelliJ.
 */
class TailwindEnhancedDocumentation {
    private val logger = Logger.getInstance(TailwindEnhancedDocumentation::class.java)
    
    // Cache for documentation to improve performance
    private val documentationCache = ConcurrentHashMap<String, String>()
    
    /**
     * Generates enhanced documentation for a Tailwind class
     * with interactive examples and visual previews
     */
    fun generateDocumentation(className: String, project: Project? = null): String {
        // Check cache first for performance
        documentationCache[className]?.let {
            return it
        }
        
        val doc = buildDocumentation(className)
        documentationCache[className] = doc
        return doc
    }
    
    /**
     * Builds rich documentation with interactive examples and visual previews
     */
    private fun buildDocumentation(className: String): String {
        // Split the class name to identify variants and base class
        val parts = className.split(":")
        val baseClass = parts.last()
        val variants = if (parts.size > 1) parts.dropLast(1) else emptyList()
        
        // Extract prefix and value for categorization
        val (prefix, value) = extractPrefixAndValue(baseClass)
        
        // Determine category for the class
        val category = getCategoryForPrefix(prefix)
        
        // Get description and CSS value
        val description = getClassDescription(className)
        val cssValue = getCssEquivalent(className)
        
        // Get examples and previews
        val example = getClassExample(className)
        val visualPreview = getVisualPreview(className)
        
        // Determine if class is from Tailwind v4
        val versionBadge = if (isTailwindV4Class(className)) {
            "<span class='version-badge v4'>Tailwind v4</span>"
        } else {
            "<span class='version-badge'>Tailwind v3</span>"
        }
        
        return """
        <html>
            <head>
                <style>
                    body { 
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; 
                        font-size: 13px;
                        line-height: 1.5;
                        margin: 0;
                        padding: 0;
                    }
                    h3 { 
                        margin-top: 0;
                        margin-bottom: 12px;
                        font-size: 16px;
                        display: flex;
                        align-items: center;
                    }
                    pre, code { 
                        font-family: 'SF Mono', Monaco, Menlo, Consolas, monospace;
                        font-size: 12px;
                    }
                    pre {
                        background: #f1f5f9;
                        padding: 12px;
                        border-radius: 6px;
                        overflow-x: auto;
                    }
                    .version-badge {
                        display: inline-block;
                        padding: 2px 6px;
                        border-radius: 4px;
                        font-size: 11px;
                        font-weight: 500;
                        background: #cbd5e1;
                        color: #0f172a;
                        margin-left: 8px;
                    }
                    .version-badge.v4 {
                        background: #818cf8;
                        color: white;
                    }
                    .category-badge {
                        display: inline-flex;
                        align-items: center;
                        padding: 3px 6px;
                        border-radius: 4px;
                        font-size: 11px;
                        font-weight: 500;
                        margin-left: 8px;
                        background: #e0f2fe;
                        color: #0369a1;
                    }
                    .css-section {
                        margin: 16px 0;
                        background: #f8fafc;
                        border-radius: 6px;
                        border: 1px solid #e2e8f0;
                        overflow: hidden;
                    }
                    .css-section h4 {
                        margin: 0;
                        padding: 10px 12px;
                        background: #f8fafc;
                        border-bottom: 1px solid #e2e8f0;
                        font-weight: 500;
                    }
                    .example-section {
                        margin: 16px 0;
                        border: 1px solid #e2e8f0;
                        border-radius: 6px;
                        overflow: hidden;
                    }
                    .example-section h4 {
                        margin: 0;
                        padding: 10px 12px;
                        background: #f8fafc;
                        border-bottom: 1px solid #e2e8f0;
                        font-weight: 500;
                    }
                    .example-content {
                        padding: 16px;
                    }
                    .visual-preview {
                        margin: 16px 0;
                        background: #f1f5f9;
                        border-radius: 6px;
                        padding: 16px;
                        border: 1px solid #e2e8f0;
                    }
                </style>
            </head>
            <body>
                <div class='tailwind-doc'>
                    <h3>
                        ${formatClassName(className)}
                        $versionBadge
                        <span class='category-badge'>$category</span>
                    </h3>
                    
                    <p>${description}</p>
                    
                    <!-- CSS Equivalent -->
                    <div class='css-section'>
                        <h4>CSS Equivalent</h4>
                        <pre>${cssValue}</pre>
                    </div>
                    
                    <!-- Visual Preview if available -->
                    ${if (visualPreview.isNotEmpty()) """<div class='visual-preview'>$visualPreview</div>""" else ""}
                    
                    <!-- Example if available -->
                    ${if (example.isNotEmpty()) """
                    <div class='example-section'>
                        <h4>Example</h4>
                        <div class='example-content'>
                            <pre>${example}</pre>
                        </div>
                    </div>
                    """ else ""}
                    
                    <!-- Variant information if present -->
                    ${if (variants.isNotEmpty()) buildVariantInfo(variants) else ""}
                    
                    <!-- Official Documentation Link -->
                    <div class='official-docs-section' style='margin: 16px 0; padding: 12px; background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 6px;'>
                        <h4 style='margin: 0 0 8px 0; font-size: 13px; font-weight: 600; color: #0369a1;'>📚 Official Documentation</h4>
                        <a href='${getOfficialDocUrl(className)}' target='_blank' style='color: #0284c7; text-decoration: none; font-size: 12px;'>
                            View on Tailwind CSS Docs →
                        </a>
                    </div>
                    
                    <!-- Related Classes / Cheat Sheet -->
                    ${buildCheatSheetSection(className)}
                </div>
            </body>
        </html>
        """.trimIndent()
    }
    
    /**
     * Builds variant information HTML
     */
    private fun buildVariantInfo(variants: List<String>): String {
        return buildString {
            append("<div class='variants-section' style='margin: 16px 0; border: 1px solid #e2e8f0; border-radius: 6px; overflow: hidden;'>")
            append("<h4 style='margin: 0; padding: 10px 12px; background: #f8fafc; border-bottom: 1px solid #e2e8f0; font-weight: 500;'>Variants</h4>")
            append("<div style='padding: 0;'>")
            variants.forEach { variant ->
                append("<div style='padding: 8px 12px; display: flex; border-bottom: 1px solid #e2e8f0;'>")
                append("<span style='font-weight: 500; min-width: 80px; color: #3b82f6;'>$variant</span>")
                append("<span style='color: #64748b;'>${getVariantDescription(variant)}</span>")
                append("</div>")
            }
            append("</div>")
            append("</div>")
        }
    }
    /**
     * Determines the category for a class based on its prefix
     */
    private fun getCategoryForPrefix(prefix: String): String {
        return when {
            prefix.startsWith("bg") -> "Background"
            prefix.startsWith("text") -> "Typography"
            prefix.startsWith("font") -> "Typography"
            prefix.startsWith("border") -> "Borders"
            prefix.startsWith("rounded") -> "Borders"
            prefix.startsWith("p") -> "Spacing"
            prefix.startsWith("m") -> "Spacing"
            prefix.startsWith("w") -> "Sizing"
            prefix.startsWith("h") -> "Sizing"
            prefix.startsWith("min") -> "Sizing"
            prefix.startsWith("max") -> "Sizing"
            prefix.startsWith("flex") -> "Flexbox"
            prefix.startsWith("grid") -> "Grid"
            prefix.startsWith("justify") -> "Flexbox"
            prefix.startsWith("items") -> "Flexbox"
            prefix.startsWith("overflow") -> "Effects"
            prefix.startsWith("shadow") -> "Effects"
            prefix.startsWith("opacity") -> "Effects"
            prefix.startsWith("transition") -> "Transitions"
            prefix.startsWith("animation") -> "Transitions"
            prefix.startsWith("transform") -> "Transforms"
            prefix.startsWith("rotate") -> "Transforms"
            prefix.startsWith("scale") -> "Transforms"
            prefix.startsWith("skew") -> "Transforms"
            prefix.startsWith("translate") -> "Transforms"
            prefix.startsWith("focus") -> "Interactivity"
            prefix.startsWith("hover") -> "Interactivity"
            prefix.startsWith("active") -> "Interactivity"
            prefix.startsWith("disabled") -> "Interactivity"
            prefix.startsWith("group") -> "Interactivity"
            prefix.startsWith("peer") -> "Interactivity"
            prefix.startsWith("dark") -> "Dark Mode"
            prefix.startsWith("lg") -> "Responsive"
            prefix.startsWith("md") -> "Responsive"
            prefix.startsWith("sm") -> "Responsive"
            prefix.startsWith("xl") -> "Responsive"
            prefix.startsWith("2xl") -> "Responsive"
            else -> "Other"
        }
    }
    
    /**
     * Generates a visual preview HTML for the class
     */
    private fun getVisualPreview(className: String): String {
        val baseClass = className.split(":").last()
        
        // Background color preview
        if (baseClass.startsWith("bg-")) {
            val colorValue = extractColorValue(baseClass)
            if (colorValue.isNotEmpty()) {
                val textColor = if (colorValue == "#ffffff" || colorValue == "white" || 
                                   colorValue == "#f8fafc" || colorValue == "#f1f5f9") {
                    "black"
                } else {
                    "white"
                }
                
                return """
                <div style='width: 100%; height: 40px; background-color: $colorValue; 
                     display: flex; align-items: center; justify-content: center; border-radius: 6px;
                     color: $textColor; font-weight: 500;'>
                    $colorValue
                </div>
                """.trimIndent()
            }
        }
        
        // Text color preview
        if (baseClass.startsWith("text-")) {
            val colorValue = extractColorValue(baseClass)
            if (colorValue.isNotEmpty()) {
                return """
                <div style='width: 100%; display: flex; flex-direction: column; gap: 8px;'>
                    <div style='color: $colorValue; font-size: 16px; font-weight: 600;'>
                        Sample Text - $colorValue
                    </div>
                    <div style='color: $colorValue; font-size: 13px;'>
                        Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                    </div>
                </div>
                """.trimIndent()
            }
        }
        
        // Font size preview
        if (baseClass.startsWith("text-") && !baseClass.contains("color")) {
            val sizes = mapOf(
                "text-xs" to "0.75rem",
                "text-sm" to "0.875rem",
                "text-base" to "1rem",
                "text-lg" to "1.125rem",
                "text-xl" to "1.25rem",
                "text-2xl" to "1.5rem",
                "text-3xl" to "1.875rem",
                "text-4xl" to "2.25rem",
                "text-5xl" to "3rem"
            )
            
            val size = sizes[baseClass] ?: return ""
            
            if (size.isNotEmpty()) {
                return """
                <div style='font-size: $size;'>
                    Sample Text ($size)
                </div>
                """.trimIndent()
            }
        }
        
        // Spacing preview (padding/margin)
        if (baseClass.startsWith("p-") || baseClass.startsWith("px-") || 
            baseClass.startsWith("py-") || baseClass.startsWith("pt-") || 
            baseClass.startsWith("pb-") || baseClass.startsWith("pl-") || 
            baseClass.startsWith("pr-")) {
            
            val value = extractSpacingValue(baseClass)
            if (value.isNotEmpty()) {
                val style = when {
                    baseClass.startsWith("p-") -> "padding: $value;"
                    baseClass.startsWith("px-") -> "padding-left: $value; padding-right: $value;"
                    baseClass.startsWith("py-") -> "padding-top: $value; padding-bottom: $value;"
                    baseClass.startsWith("pt-") -> "padding-top: $value;"
                    baseClass.startsWith("pb-") -> "padding-bottom: $value;"
                    baseClass.startsWith("pl-") -> "padding-left: $value;"
                    baseClass.startsWith("pr-") -> "padding-right: $value;"
                    else -> ""
                }
                
                return """
                <div style='background-color: #f1f5f9; width: 100%;'>
                    <div style='$style; background-color: #e0f2fe; border: 1px dashed #0284c7; 
                          text-align: center; font-size: 12px;'>
                        $style
                    </div>
                </div>
                """.trimIndent()
            }
        }
        
        // Border preview
        if (baseClass.startsWith("border") && !baseClass.contains("color") && !baseClass.contains("opacity")) {
            val width = when (baseClass) {
                "border" -> "1px"
                "border-0" -> "0px"
                "border-2" -> "2px"
                "border-4" -> "4px"
                "border-8" -> "8px"
                else -> ""
            }
            
            if (width.isNotEmpty()) {
                return """
                <div style='border: $width solid #94a3b8; padding: 16px; text-align: center; border-radius: 6px;'>
                    Border Width: $width
                </div>
                """.trimIndent()
            }
        }
        
        // Border radius preview
        if (baseClass.startsWith("rounded")) {
            val radius = when (baseClass) {
                "rounded-none" -> "0px"
                "rounded-sm" -> "0.125rem"
                "rounded" -> "0.25rem"
                "rounded-md" -> "0.375rem"
                "rounded-lg" -> "0.5rem"
                "rounded-xl" -> "0.75rem"
                "rounded-2xl" -> "1rem"
                "rounded-3xl" -> "1.5rem"
                "rounded-full" -> "9999px"
                else -> ""
            }
            
            if (radius.isNotEmpty()) {
                return """
                <div style='background-color: #e0f2fe; padding: 16px; text-align: center; 
                     border-radius: $radius; border: 1px solid #0284c7;'>
                    Border Radius: $radius
                </div>
                """.trimIndent()
            }
        }
        
        return ""
    }
    
    /**
     * Formats the class name for display
     */
    private fun formatClassName(className: String): String {
        val parts = className.split(":")
        val baseClass = parts.last()
        val variants = if (parts.size > 1) parts.dropLast(1) else emptyList()
        
        return if (variants.isEmpty()) {
            baseClass
        } else {
            "${variants.joinToString(":")}:<strong>$baseClass</strong>"
        }
    }
    /**
     * Gets the description of a Tailwind class
     */
    private fun getClassDescription(className: String): String {
        val baseClass = className.split(":").last()
        
        return when {
            baseClass.startsWith("bg-") -> "Sets the background color of an element."
            baseClass.startsWith("text-") && baseClass.contains("-") -> "Sets the text color of an element."
            baseClass.startsWith("text-") -> "Sets the font size and line height of an element."
            baseClass.startsWith("font-") -> "Sets the font weight of an element."
            baseClass.startsWith("p-") -> "Sets padding on all sides of an element."
            baseClass.startsWith("px-") -> "Sets horizontal (left and right) padding of an element."
            baseClass.startsWith("py-") -> "Sets vertical (top and bottom) padding of an element."
            baseClass.startsWith("m-") -> "Sets margin on all sides of an element."
            baseClass.startsWith("mx-") -> "Sets horizontal (left and right) margin of an element."
            baseClass.startsWith("my-") -> "Sets vertical (top and bottom) margin of an element."
            baseClass.startsWith("w-") -> "Sets the width of an element."
            baseClass.startsWith("h-") -> "Sets the height of an element."
            baseClass.startsWith("min-") -> "Sets the minimum width or height of an element."
            baseClass.startsWith("max-") -> "Sets the maximum width or height of an element."
            baseClass.startsWith("flex") -> "Sets how flex items are placed in the flex container."
            baseClass.startsWith("grid") -> "Creates a grid layout container."
            baseClass.startsWith("justify-") -> "Sets how flex and grid items are aligned along the main axis."
            baseClass.startsWith("items-") -> "Sets how flex and grid items are aligned along the cross axis."
            baseClass.startsWith("border") && !baseClass.contains("radius") -> "Sets the border width and style of an element."
            baseClass.startsWith("rounded") -> "Sets the border radius of an element."
            baseClass.startsWith("shadow") -> "Adds box shadow to an element."
            baseClass.startsWith("opacity") -> "Sets the opacity (transparency) of an element."
            baseClass.startsWith("transition") -> "Sets the CSS properties to include in transitions."
            baseClass.startsWith("animate") -> "Applies CSS animations to an element."
            baseClass.startsWith("scale") -> "Scales an element via CSS transform."
            baseClass.startsWith("rotate") -> "Rotates an element via CSS transform."
            baseClass.startsWith("translate") -> "Moves an element via CSS transform."
            else -> "A Tailwind CSS utility class that applies styling to an element."
        }
    }
    
    /**
     * Gets the CSS equivalent of a Tailwind class
     */
    private fun getCssEquivalent(className: String): String {
        val baseClass = className.split(":").last()
        
        return when {
            baseClass.startsWith("bg-") -> {
                val colorValue = extractColorValue(baseClass)
                if (colorValue.isNotEmpty()) {
                    "background-color: $colorValue;"
                } else {
                    "background-color: [color-value];"
                }
            }
            baseClass.startsWith("text-") && baseClass.contains("-") -> {
                val colorValue = extractColorValue(baseClass)
                if (colorValue.isNotEmpty()) {
                    "color: $colorValue;"
                } else {
                    "color: [color-value];"
                }
            }
            baseClass == "text-xs" -> "font-size: 0.75rem; /* 12px */\nline-height: 1rem; /* 16px */"
            baseClass == "text-sm" -> "font-size: 0.875rem; /* 14px */\nline-height: 1.25rem; /* 20px */"
            baseClass == "text-base" -> "font-size: 1rem; /* 16px */\nline-height: 1.5rem; /* 24px */"
            baseClass == "text-lg" -> "font-size: 1.125rem; /* 18px */\nline-height: 1.75rem; /* 28px */"
            baseClass == "text-xl" -> "font-size: 1.25rem; /* 20px */\nline-height: 1.75rem; /* 28px */"
            baseClass == "text-2xl" -> "font-size: 1.5rem; /* 24px */\nline-height: 2rem; /* 32px */"
            baseClass == "text-3xl" -> "font-size: 1.875rem; /* 30px */\nline-height: 2.25rem; /* 36px */"
            baseClass == "font-thin" -> "font-weight: 100;"
            baseClass == "font-extralight" -> "font-weight: 200;"
            baseClass == "font-light" -> "font-weight: 300;"
            baseClass == "font-normal" -> "font-weight: 400;"
            baseClass == "font-medium" -> "font-weight: 500;"
            baseClass == "font-semibold" -> "font-weight: 600;"
            baseClass == "font-bold" -> "font-weight: 700;"
            baseClass == "font-extrabold" -> "font-weight: 800;"
            baseClass == "font-black" -> "font-weight: 900;"
            baseClass.startsWith("p-") -> {
                val value = extractSpacingValue(baseClass)
                if (value.isNotEmpty()) {
                    "padding: $value;"
                } else {
                    "padding: [spacing-value];"
                }
            }
            baseClass.startsWith("px-") -> {
                val value = extractSpacingValue(baseClass)
                if (value.isNotEmpty()) {
                    "padding-left: $value;\npadding-right: $value;"
                } else {
                    "padding-left: [spacing-value];\npadding-right: [spacing-value];"
                }
            }
            baseClass.startsWith("py-") -> {
                val value = extractSpacingValue(baseClass)
                if (value.isNotEmpty()) {
                    "padding-top: $value;\npadding-bottom: $value;"
                } else {
                    "padding-top: [spacing-value];\npadding-bottom: [spacing-value];"
                }
            }
            baseClass.startsWith("m-") -> {
                val value = extractSpacingValue(baseClass)
                if (value.isNotEmpty()) {
                    "margin: $value;"
                } else {
                    "margin: [spacing-value];"
                }
            }
            baseClass.startsWith("mx-") -> {
                val value = extractSpacingValue(baseClass)
                if (value.isNotEmpty()) {
                    "margin-left: $value;\nmargin-right: $value;"
                } else {
                    "margin-left: [spacing-value];\nmargin-right: [spacing-value];"
                }
            }
            baseClass.startsWith("my-") -> {
                val value = extractSpacingValue(baseClass)
                if (value.isNotEmpty()) {
                    "margin-top: $value;\nmargin-bottom: $value;"
                } else {
                    "margin-top: [spacing-value];\nmargin-bottom: [spacing-value];"
                }
            }
            baseClass == "flex" -> "display: flex;"
            baseClass == "inline-flex" -> "display: inline-flex;"
            baseClass == "grid" -> "display: grid;"
            baseClass == "inline-grid" -> "display: inline-grid;"
            baseClass == "flex-row" -> "flex-direction: row;"
            baseClass == "flex-row-reverse" -> "flex-direction: row-reverse;"
            baseClass == "flex-col" -> "flex-direction: column;"
            baseClass == "flex-col-reverse" -> "flex-direction: column-reverse;"
            baseClass == "justify-start" -> "justify-content: flex-start;"
            baseClass == "justify-end" -> "justify-content: flex-end;"
            baseClass == "justify-center" -> "justify-content: center;"
            baseClass == "justify-between" -> "justify-content: space-between;"
            baseClass == "justify-around" -> "justify-content: space-around;"
            baseClass == "justify-evenly" -> "justify-content: space-evenly;"
            baseClass == "items-start" -> "align-items: flex-start;"
            baseClass == "items-end" -> "align-items: flex-end;"
            baseClass == "items-center" -> "align-items: center;"
            baseClass == "items-baseline" -> "align-items: baseline;"
            baseClass == "items-stretch" -> "align-items: stretch;"
            baseClass == "border" -> "border-width: 1px;\nborder-style: solid;"
            baseClass == "border-0" -> "border-width: 0px;"
            baseClass == "border-2" -> "border-width: 2px;"
            baseClass == "border-4" -> "border-width: 4px;"
            baseClass == "border-8" -> "border-width: 8px;"
            baseClass.startsWith("border-") && baseClass.split("-").size > 1 -> {
                val colorValue = extractColorValue(baseClass)
                if (colorValue.isNotEmpty()) {
                    "border-color: $colorValue;"
                } else {
                    "border-color: [color-value];"
                }
            }
            baseClass == "rounded-none" -> "border-radius: 0px;"
            baseClass == "rounded-sm" -> "border-radius: 0.125rem; /* 2px */"
            baseClass == "rounded" -> "border-radius: 0.25rem; /* 4px */"
            baseClass == "rounded-md" -> "border-radius: 0.375rem; /* 6px */"
            baseClass == "rounded-lg" -> "border-radius: 0.5rem; /* 8px */"
            baseClass == "rounded-xl" -> "border-radius: 0.75rem; /* 12px */"
            baseClass == "rounded-2xl" -> "border-radius: 1rem; /* 16px */"
            baseClass == "rounded-3xl" -> "border-radius: 1.5rem; /* 24px */"
            baseClass == "rounded-full" -> "border-radius: 9999px;"
            baseClass == "shadow-sm" -> "box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);"
            baseClass == "shadow" -> "box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);"
            baseClass == "shadow-md" -> "box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);"
            baseClass == "shadow-lg" -> "box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);"
            baseClass == "shadow-xl" -> "box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);"
            baseClass == "shadow-2xl" -> "box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);"
            baseClass == "shadow-inner" -> "box-shadow: inset 0 2px 4px 0 rgba(0, 0, 0, 0.06);"
            baseClass == "shadow-none" -> "box-shadow: none;"
            else -> "[CSS equivalent not available]"
        }
    }
    
    /**
     * Gets a usage example of a Tailwind class
     */
    private fun getClassExample(className: String): String {
        val baseClass = className.split(":").last()
        
        return when {
            baseClass.startsWith("bg-") -> """<div class="$className p-4">Element with background color</div>"""
            baseClass.startsWith("text-") && baseClass.contains("-") -> """<p class="$className">Text with color styling</p>"""
            baseClass.startsWith("text-") -> """<p class="$className">Text with size styling</p>"""
            baseClass.startsWith("font-") -> """<p class="$className">Text with font weight styling</p>"""
            baseClass.startsWith("p-") -> """<div class="$className border border-gray-200">Element with padding</div>"""
            baseClass.startsWith("m-") -> """<div class="border border-gray-200 $className">Element with margin</div>"""
            baseClass.startsWith("w-") -> """<div class="$className h-6 bg-blue-200">Element with width</div>"""
            baseClass.startsWith("h-") -> """<div class="w-24 $className bg-blue-200">Element with height</div>"""
            baseClass == "flex" -> """<div class="$className gap-2">
  <div class="bg-blue-200 p-2">Item 1</div>
  <div class="bg-blue-300 p-2">Item 2</div>
</div>"""
            baseClass == "grid" -> """<div class="$className grid-cols-3 gap-2">
  <div class="bg-blue-200 p-2">Item 1</div>
  <div class="bg-blue-300 p-2">Item 2</div>
  <div class="bg-blue-400 p-2">Item 3</div>
</div>"""
            baseClass.startsWith("border") -> """<div class="$className border-gray-400 p-4">Element with border</div>"""
            baseClass.startsWith("rounded") -> """<div class="$className bg-blue-200 p-4">Element with rounded corners</div>"""
            baseClass.startsWith("shadow") -> """<div class="$className bg-white p-4">Element with shadow</div>"""
            else -> ""
        }
    }
    /**
     * Gets interactive example HTML for a class
     */
    private fun getInteractiveExample(className: String): String {
        val baseClass = className.split(":").last()
        
        // No interactive examples for now - this is a placeholder for future implementation
        return ""
    }
    
    /**
     * Gets related Tailwind classes
     */
    private fun getRelatedClasses(className: String): String {
        val baseClass = className.split(":").last()
        
        return when {
            baseClass.startsWith("bg-") -> "text-, p-, m-, rounded-"
            baseClass.startsWith("text-") && baseClass.contains("-") -> "bg-, font-, p-"
            baseClass.startsWith("text-") -> "font-, text-[color], leading-"
            baseClass.startsWith("font-") -> "text-, tracking-, leading-"
            baseClass.startsWith("p-") -> "px-, py-, pt-, pb-, pl-, pr-"
            baseClass.startsWith("m-") -> "mx-, my-, mt-, mb-, ml-, mr-"
            baseClass.startsWith("w-") -> "h-, min-w-, max-w-"
            baseClass.startsWith("h-") -> "w-, min-h-, max-h-"
            baseClass == "flex" -> "flex-row, flex-col, justify-, items-"
            baseClass == "grid" -> "grid-cols-, gap-, grid-rows-"
            baseClass.startsWith("border") -> "border-[color], border-[width], rounded-"
            baseClass.startsWith("rounded") -> "border, overflow-hidden"
            baseClass.startsWith("shadow") -> "opacity-, z-"
            else -> ""
        }
    }
    
    /**
     * Gets description for a variant
     */
    private fun getVariantDescription(variant: String): String {
        return when (variant) {
            "hover" -> "Applied when the user hovers over the element with a pointer device"
            "focus" -> "Applied when the element has focus (e.g., form input)"
            "active" -> "Applied when the element is being activated by the user (e.g., clicked)"
            "disabled" -> "Applied when the element is disabled"
            "visited" -> "Applied when the element (usually a link) has been visited"
            "first" -> "Applied to the first element among a group of sibling elements"
            "last" -> "Applied to the last element among a group of sibling elements"
            "odd" -> "Applied to elements with an odd position among a group of sibling elements"
            "even" -> "Applied to elements with an even position among a group of sibling elements"
            "sm" -> "Applied at the small screen breakpoint (640px and above)"
            "md" -> "Applied at the medium screen breakpoint (768px and above)"
            "lg" -> "Applied at the large screen breakpoint (1024px and above)"
            "xl" -> "Applied at the extra large screen breakpoint (1280px and above)"
            "2xl" -> "Applied at the 2x extra large screen breakpoint (1536px and above)"
            "dark" -> "Applied when dark mode is enabled"
            "group-hover" -> "Applied when a parent with the 'group' class is hovered"
            "peer-hover" -> "Applied when a sibling with the 'peer' class is hovered"
            "peer-focus" -> "Applied when a sibling with the 'peer' class is focused"
            else -> "A conditional style variant"
        }
    }
    
    /**
     * Gets customization information from the Tailwind config
     */
    private fun getCustomizationInfo(className: String, project: Project?): String {
        // This would normally analyze the Tailwind config file
        // For now, we return generic customization info
        
        val baseClass = className.split(":").last()
        
        return when {
            baseClass.startsWith("bg-") -> 
                "Colors can be customized in the tailwind.config.js file under the 'theme.colors' section."
            baseClass.startsWith("text-") && baseClass.contains("-") -> 
                "Text colors can be customized in the tailwind.config.js file under the 'theme.colors' section."
            baseClass.startsWith("text-") -> 
                "Font sizes can be customized in the tailwind.config.js file under the 'theme.fontSize' section."
            baseClass.startsWith("font-") -> 
                "Font weights can be customized in the tailwind.config.js file under the 'theme.fontWeight' section."
            baseClass.startsWith("p-") || baseClass.startsWith("m-") -> 
                "Spacing values can be customized in the tailwind.config.js file under the 'theme.spacing' section."
            baseClass.startsWith("w-") || baseClass.startsWith("h-") -> 
                "Width and height values can be customized in the tailwind.config.js file under the 'theme.width' and 'theme.height' sections."
            baseClass.startsWith("rounded") -> 
                "Border radius values can be customized in the tailwind.config.js file under the 'theme.borderRadius' section."
            baseClass.startsWith("shadow") -> 
                "Box shadow values can be customized in the tailwind.config.js file under the 'theme.boxShadow' section."
            else -> ""
        }
    }
    
    /**
     * Extract color value from a Tailwind color class name
     */
    private fun extractColorValue(className: String): String {
        val parts = className.split("-")
        if (parts.size < 2) return ""
        
        // Basic colors without shades
        if (parts.size == 2) {
            return when (parts[1]) {
                "black" -> "#000000"
                "white" -> "#ffffff"
                "transparent" -> "transparent"
                "current" -> "currentColor"
                "red" -> "#ef4444"    // red-500 equivalent
                "blue" -> "#3b82f6"   // blue-500 equivalent
                "green" -> "#22c55e"  // green-500 equivalent
                "yellow" -> "#eab308" // yellow-500 equivalent
                "gray" -> "#6b7280"   // gray-500 equivalent
                else -> ""
            }
        }
        
        // For example: bg-blue-500, text-red-700
        if (parts.size >= 3) {
            val colorName = parts[1]
            val shade = parts[2]
            
            // Map common color-shade combinations to hex values
            return when {
                colorName == "slate" && shade == "50" -> "#f8fafc"
                colorName == "slate" && shade == "100" -> "#f1f5f9"
                colorName == "slate" && shade == "500" -> "#64748b"
                colorName == "slate" && shade == "900" -> "#0f172a"
                
                colorName == "gray" && shade == "50" -> "#f9fafb"
                colorName == "gray" && shade == "100" -> "#f3f4f6"
                colorName == "gray" && shade == "500" -> "#6b7280"
                colorName == "gray" && shade == "900" -> "#111827"
                
                colorName == "red" && shade == "50" -> "#fef2f2"
                colorName == "red" && shade == "100" -> "#fee2e2"
                colorName == "red" && shade == "500" -> "#ef4444"
                colorName == "red" && shade == "900" -> "#7f1d1d"
                
                colorName == "blue" && shade == "50" -> "#eff6ff"
                colorName == "blue" && shade == "100" -> "#dbeafe"
                colorName == "blue" && shade == "500" -> "#3b82f6"
                colorName == "blue" && shade == "900" -> "#1e3a8a"
                
                colorName == "green" && shade == "50" -> "#f0fdf4"
                colorName == "green" && shade == "100" -> "#dcfce7"
                colorName == "green" && shade == "500" -> "#22c55e"
                colorName == "green" && shade == "900" -> "#14532d"
                
                else -> ""
            }
        }
        
        return ""
    }
    
    /**
     * Extract spacing value from a Tailwind spacing class
     */
    private fun extractSpacingValue(className: String): String {
        val parts = className.split("-")
        if (parts.size < 2) return ""
        
        val value = parts.last()
        
        return when (value) {
            "0" -> "0px"
            "px" -> "1px"
            "0.5" -> "0.125rem" // 2px
            "1" -> "0.25rem"    // 4px
            "2" -> "0.5rem"     // 8px
            "3" -> "0.75rem"    // 12px
            "4" -> "1rem"       // 16px
            "5" -> "1.25rem"    // 20px
            "6" -> "1.5rem"     // 24px
            "8" -> "2rem"       // 32px
            "10" -> "2.5rem"    // 40px
            "12" -> "3rem"      // 48px
            "16" -> "4rem"      // 64px
            "20" -> "5rem"      // 80px
            "24" -> "6rem"      // 96px
            "32" -> "8rem"      // 128px
            "40" -> "10rem"     // 160px
            "48" -> "12rem"     // 192px
            "56" -> "14rem"     // 224px
            "64" -> "16rem"     // 256px
            else -> ""
        }
    }
    
    /**
     * Extract prefix and value from a class name
     */
    private fun extractPrefixAndValue(className: String): Pair<String, String> {
        // Regular expression to match Tailwind class patterns
        val regex = """^([a-zA-Z0-9-]+)(?:-(\d+(?:\.\d+)?|[a-zA-Z]+))?(?:\[(.*)\])?$""".toRegex()
        val match = regex.find(className) ?: return Pair(className, "")
        
        val groups = match.groupValues
        val prefix = groups[1]
        
        // Handle arbitrary values like w-[300px]
        val value = if (groups[2].isNotEmpty()) {
            groups[2]
        } else if (groups.size > 3 && groups[3].isNotEmpty()) {
            groups[3]
        } else {
            ""
        }
        
        return Pair(prefix, value)
    }
    
    /**
     * Determines if a class belongs to Tailwind v4
     */
    private fun isTailwindV4Class(className: String): Boolean {
        return className.startsWith("accent-") || 
               className.startsWith("backdrop-") ||
               className.startsWith("border-x-") ||
               className.startsWith("border-y-")
    }
    
    /**
     * Gets the official Tailwind CSS documentation URL for a class
     */
    private fun getOfficialDocUrl(className: String): String {
        val baseClass = className.split(":").last()
        val baseUrl = "https://tailwindcss.com/docs"
        
        return when {
            baseClass.startsWith("bg-") -> "$baseUrl/background-color"
            baseClass.startsWith("text-") && baseClass.contains("-") -> "$baseUrl/text-color"
            baseClass.startsWith("text-") -> "$baseUrl/font-size"
            baseClass.startsWith("font-") -> "$baseUrl/font-weight"
            baseClass.startsWith("p-") -> "$baseUrl/padding"
            baseClass.startsWith("m-") -> "$baseUrl/margin"
            baseClass.startsWith("w-") -> "$baseUrl/width"
            baseClass.startsWith("h-") -> "$baseUrl/height"
            baseClass.startsWith("flex") -> "$baseUrl/flex"
            baseClass.startsWith("grid") -> "$baseUrl/grid-template-columns"
            baseClass.startsWith("justify-") -> "$baseUrl/justify-content"
            baseClass.startsWith("items-") -> "$baseUrl/align-items"
            baseClass.startsWith("border") && !baseClass.contains("radius") -> "$baseUrl/border-width"
            baseClass.startsWith("rounded") -> "$baseUrl/border-radius"
            baseClass.startsWith("shadow") -> "$baseUrl/box-shadow"
            baseClass.startsWith("opacity") -> "$baseUrl/opacity"
            baseClass.startsWith("transition") -> "$baseUrl/transition-property"
            baseClass.startsWith("animate") -> "$baseUrl/animation"
            baseClass.startsWith("scale") -> "$baseUrl/scale"
            baseClass.startsWith("rotate") -> "$baseUrl/rotate"
            baseClass.startsWith("translate") -> "$baseUrl/translate"
            else -> "$baseUrl"
        }
    }
    
    /**
     * Builds a cheat sheet section with related classes
     */
    private fun buildCheatSheetSection(className: String): String {
        val relatedClasses = getRelatedClasses(className)
        if (relatedClasses.isEmpty()) return ""
        
        val category = getCategoryForPrefix(extractPrefixAndValue(className.split(":").last()).first)
        val cheatSheet = getCheatSheetForCategory(category)
        
        return buildString {
            append("<div class='cheatsheet-section' style='margin: 16px 0; border: 1px solid #e2e8f0; border-radius: 6px; overflow: hidden;'>")
            append("<h4 style='margin: 0; padding: 10px 12px; background: #f8fafc; border-bottom: 1px solid #e2e8f0; font-weight: 500;'>📋 Quick Reference</h4>")
            append("<div style='padding: 12px;'>")
            
            // Related classes
            append("<div style='margin-bottom: 12px;'>")
            append("<strong style='font-size: 12px; color: #475569;'>Related Classes:</strong>")
            append("<div style='margin-top: 6px; font-family: monospace; font-size: 11px; color: #64748b;'>")
            append(relatedClasses.split(", ").joinToString(" • ") { "<code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>$it</code>" })
            append("</div>")
            append("</div>")
            
            // Cheat sheet for category
            if (cheatSheet.isNotEmpty()) {
                append("<div>")
                append("<strong style='font-size: 12px; color: #475569;'>Common $category Classes:</strong>")
                append("<div style='margin-top: 6px; font-family: monospace; font-size: 11px; color: #64748b; line-height: 1.6;'>")
                append(cheatSheet)
                append("</div>")
                append("</div>")
            }
            
            append("</div>")
            append("</div>")
        }
    }
    
    /**
     * Gets a cheat sheet for a specific category
     */
    private fun getCheatSheetForCategory(category: String): String {
        return when (category) {
            "Background" -> """
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>bg-white</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>bg-black</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>bg-blue-500</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>bg-red-500</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>bg-green-500</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>bg-transparent</code>
            """.trimIndent()
            "Typography" -> """
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>text-xs</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>text-sm</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>text-base</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>text-lg</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>text-xl</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>font-bold</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>font-semibold</code>
            """.trimIndent()
            "Spacing" -> """
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>p-4</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>px-4</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>py-4</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>m-4</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>mx-auto</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>my-4</code>
            """.trimIndent()
            "Sizing" -> """
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>w-full</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>w-1/2</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>h-screen</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>min-h-screen</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>max-w-7xl</code>
            """.trimIndent()
            "Flexbox" -> """
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>flex</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>flex-col</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>justify-center</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>items-center</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>gap-4</code>
            """.trimIndent()
            "Borders" -> """
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>border</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>border-2</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>rounded</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>rounded-lg</code> 
                <code style='background: #f1f5f9; padding: 2px 4px; border-radius: 3px;'>rounded-full</code>
            """.trimIndent()
            else -> ""
        }
    }
} // End of TailwindEnhancedDocumentation class
