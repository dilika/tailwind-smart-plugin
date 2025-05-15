package com.github.dilika.tailwindsmartplugin.documentation

import com.intellij.psi.PsiElement
import com.intellij.openapi.diagnostic.Logger

/**
 * Provides enhanced documentation for Tailwind classes
 * with examples and CSS equivalent values
 */
class TailwindEnhancedDocumentation {
    private val logger = Logger.getInstance(TailwindEnhancedDocumentation::class.java)
    
    /**
     * Generates enhanced documentation for a Tailwind class
     */
    fun generateDocumentation(className: String): String {
        return buildString {
            append("<div class='tailwind-doc'>")
            
            // Title and description
            append("<h3>${formatClassName(className)}</h3>")
            append("<p>${getClassDescription(className)}</p>")
            
            // CSS equivalent value
            val cssValue = getCssEquivalent(className)
            if (cssValue.isNotEmpty()) {
                append("<div class='css-equivalent'>")
                append("<h4>CSS equivalent:</h4>")
                append("<pre>$cssValue</pre>")
                append("</div>")
            }
            
            // Usage example
            val example = getClassExample(className)
            if (example.isNotEmpty()) {
                append("<div class='example'>")
                append("<h4>Example:</h4>")
                append("<pre>$example</pre>")
                append("</div>")
            }
            
            append("</div>")
        }
    }
    
    /**
     * Formats the class name for display
     */
    private fun formatClassName(className: String): String {
        // Class name formatting logic
        val parts = className.split(":")
        return if (parts.size > 1) {
            "<span class='variant'>${parts[0]}:</span><span class='utility'>${parts.subList(1, parts.size).joinToString(":")}</span>"
        } else {
            "<span class='utility'>$className</span>"
        }
    }
    
    /**
     * Gets the description of a Tailwind class
     */
    private fun getClassDescription(className: String): String {
        val baseClass = className.split(":").last()
        
        return when {
            // Layout
            baseClass == "container" -> "Sets a container with a maximum width based on the screen breakpoints."
            baseClass.startsWith("columns-") -> "Sets the number of columns for a multi-column element."
            baseClass.startsWith("break-") -> "Controls the line-breaking behavior of an element."
            
            // Flexbox
            baseClass == "flex" -> "Sets a flexible container (display: flex)."
            baseClass.startsWith("flex-") -> "Configures flexbox properties like direction, wrapping, etc."
            baseClass.startsWith("justify-") -> "Controls the alignment of items along the main axis."
            baseClass.startsWith("items-") -> "Controls the alignment of items along the cross axis."
            
            // Grid
            baseClass == "grid" -> "Sets a grid container (display: grid)."
            baseClass.startsWith("grid-") -> "Configures grid properties like columns, rows, etc."
            baseClass.startsWith("gap-") -> "Sets the spacing between grid or flexbox elements."
            
            // Spacing
            baseClass.startsWith("p-") || baseClass.startsWith("px-") || baseClass.startsWith("py-") || 
            baseClass.startsWith("pt-") || baseClass.startsWith("pr-") || baseClass.startsWith("pb-") || baseClass.startsWith("pl-") -> 
                "Sets the padding (inner spacing) of an element."
            baseClass.startsWith("m-") || baseClass.startsWith("mx-") || baseClass.startsWith("my-") || 
            baseClass.startsWith("mt-") || baseClass.startsWith("mr-") || baseClass.startsWith("mb-") || baseClass.startsWith("ml-") -> 
                "Sets the margin (outer spacing) of an element."
            
            // Sizing
            baseClass.startsWith("w-") -> "Sets the width of an element."
            baseClass.startsWith("h-") -> "Sets the height of an element."
            baseClass.startsWith("min-") -> "Sets the minimum size of an element."
            baseClass.startsWith("max-") -> "Sets the maximum size of an element."
            
            // Typography
            baseClass.startsWith("text-") && !baseClass.contains("[") -> "Sets the text size or color."
            baseClass.startsWith("font-") -> "Sets the font family or font weight."
            baseClass.startsWith("leading-") -> "Sets the line height."
            baseClass.startsWith("tracking-") -> "Sets the letter spacing."
            
            // Backgrounds
            baseClass.startsWith("bg-") && !baseClass.contains("[") -> "Sets the background color or image."
            baseClass.startsWith("bg-gradient-") -> "Applies a gradient as background."
            
            // Borders
            baseClass.startsWith("border") -> "Sets the borders of an element."
            baseClass.startsWith("rounded") -> "Sets the border radius."
            
            // Effects
            baseClass.startsWith("shadow") -> "Applies a shadow to an element."
            baseClass.startsWith("opacity") -> "Sets the opacity of an element."
            
            // Transitions & Animation
            baseClass.startsWith("transition") -> "Configures CSS transitions."
            baseClass.startsWith("animate") -> "Applies a predefined animation."
            
            // Interactivity
            baseClass.startsWith("cursor") -> "Sets the cursor style."
            baseClass.startsWith("pointer-events") -> "Controls how an element responds to pointer events."
            
            // Arbitrary values
            baseClass.contains("[") && baseClass.contains("]") -> "Class with custom arbitrary value."
            
            // Variants
            className.contains(":") && !baseClass.contains("[") -> {
                val variant = className.split(":").first()
                "Applies conditional styles with the '$variant' variant."
            }
            
            else -> "Tailwind CSS utility class."
        }
    }
    
    /**
     * Gets the CSS equivalent of a Tailwind class
     */
    private fun getCssEquivalent(className: String): String {
        val baseClass = className.split(":").last()
        
        return when {
            // Layout
            baseClass == "container" -> "width: 100%;\nmax-width: [responsive];"
            baseClass == "block" -> "display: block;"
            baseClass == "inline-block" -> "display: inline-block;"
            baseClass == "inline" -> "display: inline;"
            baseClass == "hidden" -> "display: none;"
            
            // Flexbox
            baseClass == "flex" -> "display: flex;"
            baseClass == "inline-flex" -> "display: inline-flex;"
            baseClass == "flex-row" -> "flex-direction: row;"
            baseClass == "flex-row-reverse" -> "flex-direction: row-reverse;"
            baseClass == "flex-col" -> "flex-direction: column;"
            baseClass == "flex-col-reverse" -> "flex-direction: column-reverse;"
            baseClass == "flex-wrap" -> "flex-wrap: wrap;"
            baseClass == "flex-nowrap" -> "flex-wrap: nowrap;"
            baseClass == "items-center" -> "align-items: center;"
            baseClass == "items-start" -> "align-items: flex-start;"
            baseClass == "items-end" -> "align-items: flex-end;"
            baseClass == "justify-center" -> "justify-content: center;"
            baseClass == "justify-start" -> "justify-content: flex-start;"
            baseClass == "justify-end" -> "justify-content: flex-end;"
            baseClass == "justify-between" -> "justify-content: space-between;"
            
            // Grid
            baseClass == "grid" -> "display: grid;"
            baseClass == "grid-cols-1" -> "grid-template-columns: repeat(1, minmax(0, 1fr));"
            baseClass == "grid-cols-2" -> "grid-template-columns: repeat(2, minmax(0, 1fr));"
            baseClass == "grid-cols-3" -> "grid-template-columns: repeat(3, minmax(0, 1fr));"
            baseClass == "grid-cols-4" -> "grid-template-columns: repeat(4, minmax(0, 1fr));"
            
            // Spacing
            baseClass == "p-0" -> "padding: 0px;"
            baseClass == "p-1" -> "padding: 0.25rem; /* 4px */"
            baseClass == "p-2" -> "padding: 0.5rem; /* 8px */"
            baseClass == "p-4" -> "padding: 1rem; /* 16px */"
            baseClass == "px-4" -> "padding-left: 1rem; /* 16px */\npadding-right: 1rem; /* 16px */"
            baseClass == "py-2" -> "padding-top: 0.5rem; /* 8px */\npadding-bottom: 0.5rem; /* 8px */"
            
            baseClass == "m-0" -> "margin: 0px;"
            baseClass == "m-1" -> "margin: 0.25rem; /* 4px */"
            baseClass == "m-2" -> "margin: 0.5rem; /* 8px */"
            baseClass == "m-4" -> "margin: 1rem; /* 16px */"
            baseClass == "mx-auto" -> "margin-left: auto;\nmargin-right: auto;"
            
            // Sizing
            baseClass == "w-full" -> "width: 100%;"
            baseClass == "w-auto" -> "width: auto;"
            baseClass == "w-screen" -> "width: 100vw;"
            baseClass == "h-full" -> "height: 100%;"
            baseClass == "h-screen" -> "height: 100vh;"
            
            // Typography
            baseClass == "text-xs" -> "font-size: 0.75rem; /* 12px */\nline-height: 1rem; /* 16px */"
            baseClass == "text-sm" -> "font-size: 0.875rem; /* 14px */\nline-height: 1.25rem; /* 20px */"
            baseClass == "text-base" -> "font-size: 1rem; /* 16px */\nline-height: 1.5rem; /* 24px */"
            baseClass == "text-lg" -> "font-size: 1.125rem; /* 18px */\nline-height: 1.75rem; /* 28px */"
            baseClass == "text-xl" -> "font-size: 1.25rem; /* 20px */\nline-height: 1.75rem; /* 28px */"
            baseClass == "text-2xl" -> "font-size: 1.5rem; /* 24px */\nline-height: 2rem; /* 32px */"
            
            baseClass == "font-bold" -> "font-weight: 700;"
            baseClass == "font-semibold" -> "font-weight: 600;"
            baseClass == "font-medium" -> "font-weight: 500;"
            baseClass == "font-normal" -> "font-weight: 400;"
            baseClass == "font-light" -> "font-weight: 300;"
            
            baseClass == "text-center" -> "text-align: center;"
            baseClass == "text-left" -> "text-align: left;"
            baseClass == "text-right" -> "text-align: right;"
            baseClass == "text-justify" -> "text-align: justify;"
            
            // Colors (examples)
            baseClass == "text-white" -> "color: rgb(255 255 255);"
            baseClass == "text-black" -> "color: rgb(0 0 0);"
            baseClass == "text-red-500" -> "color: rgb(239 68 68);"
            baseClass == "text-blue-500" -> "color: rgb(59 130 246);"
            
            baseClass == "bg-white" -> "background-color: rgb(255 255 255);"
            baseClass == "bg-black" -> "background-color: rgb(0 0 0);"
            baseClass == "bg-red-500" -> "background-color: rgb(239 68 68);"
            baseClass == "bg-blue-500" -> "background-color: rgb(59 130 246);"
            
            // Borders
            baseClass == "border" -> "border-width: 1px;"
            baseClass == "border-2" -> "border-width: 2px;"
            baseClass == "border-4" -> "border-width: 4px;"
            baseClass == "border-t" -> "border-top-width: 1px;"
            
            baseClass == "rounded" -> "border-radius: 0.25rem; /* 4px */"
            baseClass == "rounded-md" -> "border-radius: 0.375rem; /* 6px */"
            baseClass == "rounded-lg" -> "border-radius: 0.5rem; /* 8px */"
            baseClass == "rounded-full" -> "border-radius: 9999px;"
            
            // Effects
            baseClass == "shadow" -> "box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1);"
            baseClass == "shadow-md" -> "box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);"
            baseClass == "shadow-lg" -> "box-shadow: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);"
            
            baseClass == "opacity-0" -> "opacity: 0;"
            baseClass == "opacity-50" -> "opacity: 0.5;"
            baseClass == "opacity-100" -> "opacity: 1;"
            
            // Transitions
            baseClass == "transition" -> "transition-property: color, background-color, border-color, text-decoration-color, fill, stroke, opacity, box-shadow, transform, filter, backdrop-filter;\ntransition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);\ntransition-duration: 150ms;"
            baseClass == "duration-300" -> "transition-duration: 300ms;"
            baseClass == "ease-in-out" -> "transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);"
            
            // Valeurs arbitraires
            baseClass.contains("[") && baseClass.contains("]") -> {
                val property = baseClass.substringBefore("[")
                val value = baseClass.substringAfter("[").substringBefore("]")
                
                when (property) {
                    "w" -> "width: $value;"
                    "h" -> "height: $value;"
                    "text" -> "color: $value;"
                    "bg" -> "background-color: $value;"
                    "p" -> "padding: $value;"
                    "m" -> "margin: $value;"
                    else -> "$property: $value;"
                }
            }
            
            else -> ""
        }
    }
    
    /**
     * Gets a usage example of a Tailwind class
     */
    private fun getClassExample(className: String): String {
        val baseClass = className.split(":").last()
        
        return when {
            // Layout
            baseClass == "container" -> "<div class=\"container mx-auto px-4\">\n  <!-- Centered content with margins -->\n</div>"
            
            // Flexbox
            baseClass.startsWith("flex") || baseClass.startsWith("items-") || baseClass.startsWith("justify-") -> 
                "<div class=\"flex items-center justify-between p-4\">\n  <div>Item 1</div>\n  <div>Item 2</div>\n</div>"
            
            // Grid
            baseClass.startsWith("grid") -> 
                "<div class=\"grid grid-cols-3 gap-4\">\n  <div>1</div>\n  <div>2</div>\n  <div>3</div>\n</div>"
            
            // Spacing
            baseClass.startsWith("p-") || baseClass.startsWith("px-") || baseClass.startsWith("py-") -> 
                "<div class=\"$className bg-gray-200\">\n  Element with padding\n</div>"
            
            baseClass.startsWith("m-") || baseClass.startsWith("mx-") || baseClass.startsWith("my-") -> 
                "<div class=\"$className bg-gray-200\">\n  Element with margin\n</div>"
            
            // Typography
            baseClass.startsWith("text-") && baseClass.length <= 7 -> 
                "<p class=\"$className font-medium\">\n  Text with size $baseClass\n</p>"
            
            baseClass.startsWith("font-") -> 
                "<p class=\"$className text-lg\">\n  Text with font style $baseClass\n</p>"
            
            // Colors
            baseClass.startsWith("text-") && baseClass.length > 7 -> 
                "<p class=\"$className\">\n  Colored text\n</p>"
            
            baseClass.startsWith("bg-") -> 
                "<div class=\"$className p-4 rounded\">\n  Element with background\n</div>"
            
            // Borders
            baseClass.startsWith("border") && !baseClass.startsWith("border-radius") -> 
                "<div class=\"$className border-gray-300 p-4\">\n  Element with border\n</div>"
            
            baseClass.startsWith("rounded") -> 
                "<div class=\"$className bg-blue-500 p-4 text-white\">\n  Element with rounded corners\n</div>"
            
            // Effects
            baseClass.startsWith("shadow") -> 
                "<div class=\"$className bg-white p-4 rounded\">\n  Element with shadow\n</div>"
            
            baseClass.startsWith("opacity") -> 
                "<div class=\"$className bg-blue-500 p-4 text-white\">\n  Element with opacity\n</div>"
            
            // Transitions
            baseClass.startsWith("transition") || baseClass.startsWith("duration") || baseClass.startsWith("ease") -> 
                "<button class=\"bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded $className\">\n  Button with transition\n</button>"
            
            // Hover and other variants
            className.contains(":") -> {
                val variant = className.split(":").first()
                val utility = className.split(":").last()
                
                when (variant) {
                    "hover" -> "<button class=\"bg-blue-500 $className text-white font-bold py-2 px-4 rounded\">\n  Hover me\n</button>"
                    "focus" -> "<input class=\"border $className p-2 rounded\" placeholder=\"Click here\" />"
                    "active" -> "<button class=\"bg-blue-500 $className text-white font-bold py-2 px-4 rounded\">\n  Click me\n</button>"
                    "dark" -> "<div class=\"bg-white dark:bg-gray-800 text-gray-900 $className p-4 rounded\">\n  Dark/light mode\n</div>"
                    "md", "lg", "xl" -> "<div class=\"bg-blue-500 $className p-4 rounded\">\n  Responsive from $variant breakpoint\n</div>"
                    "group-hover" -> "<div class=\"group bg-white hover:bg-blue-100 p-4 rounded\">\n  <span class=\"$className\">Effect when parent is hovered</span>\n</div>"
                    else -> "<div class=\"$className p-4\">\n  Example with $variant variant\n</div>"
                }
            }
            
            else -> ""
        }
    }
}