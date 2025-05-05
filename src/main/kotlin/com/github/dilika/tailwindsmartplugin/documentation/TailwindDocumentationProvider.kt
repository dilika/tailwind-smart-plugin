package com.github.dilika.tailwindsmartplugin.documentation

import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue
import org.json.JSONObject

/**
 * Provides documentation for Tailwind CSS classes
 */
@Suppress("unused") // Registered via plugin.xml
class TailwindDocumentationProvider : AbstractDocumentationProvider(), DocumentationProvider {
    private val logger = Logger.getInstance(TailwindDocumentationProvider::class.java)

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element == null) return null

        try {
            logger.info("Generating documentation for: ${element.text}")
            
            // Try to get the class name from the element
            val className = extractClassName(element, originalElement)
            if (className.isNullOrBlank()) {
                logger.info("Could not extract class name")
                return null
            }
            
            // Get the current project
            val project = element.project
            
            // Get Tailwind classes specific to the project
            val tailwindClasses = TailwindUtils.getTailwindClasses(project)
            
            return if (tailwindClasses.contains(className)) {
                // Found in Tailwind classes - generate rich documentation
                createRichDocumentation(className, project)
            } else {
                // Not found - generate basic documentation
                generateBasicDocumentation(className)
            }
        } catch (e: Exception) {
            logger.error("Error generating documentation: ${e.message}")
            return generateErrorDocumentation("Failed to generate documentation", e)
        }
    }
    
    /**
     * Extract class name from the PsiElement
     */
    private fun extractClassName(element: PsiElement, originalElement: PsiElement?): String? {
        return when {
            element is XmlAttributeValue -> {
                // Check if it's a class attribute
                val attributeName = element.parent?.firstChild?.text
                if (attributeName != "class" && attributeName != "className") return null
                
                // Get the cursor position if available
                val cursorOffset = originalElement?.let {
                    it.textRange.startOffset - element.textRange.startOffset
                } ?: 0
                
                // Find the class at cursor
                findClassAtCursor(element.value, cursorOffset)
            }
            element.parent is XmlAttributeValue -> {
                val attributeValue = element.parent as XmlAttributeValue
                // Check if it's a class attribute
                val attributeName = attributeValue.parent?.firstChild?.text
                if (attributeName != "class" && attributeName != "className") return null
                
                // Try to extract the class directly from the element text
                element.text.trim().removeSurrounding("\"")
            }
            else -> {
                // Try to use the element text directly
                element.text.trim().removeSurrounding("\"")
            }
        }
    }
    
    /**
     * Creates rich documentation for a Tailwind class using class data
     */
    private fun createRichDocumentation(className: String, project: Project): String {
        try {
            // Get enriched class data if possible
            val classDataMap = TailwindUtils.getTailwindClassData(project)
            val classData = classDataMap[className]
            val docJSON = classData?.optJSONObject("documentation") ?: return generateBasicDocumentation(className)
            
            logger.info("Tailwind class found: $className")
            val description = docJSON.optString("description", "Tailwind CSS class")
            val category = docJSON.optString("type", "utility")
            val icon = docJSON.optString("icon", getCategoryIcon(extractPrefixAndValue(className).first))
            
            // Build color preview if available
            val colorHtml = if (docJSON.has("color")) {
                val color = docJSON.getString("color")
                val textColor = getContrastingTextColor(color)
                val colorStyle = when {
                    className.startsWith("bg-") -> "background-color: $color; color: $textColor;"
                    className.startsWith("text-") -> "color: $color; background-color: #f3f4f6;"
                    className.startsWith("border-") -> "border: 2px solid $color; background-color: transparent;"
                    else -> "background-color: $color; color: $textColor;"
                }
                
                """
                <div style="margin-bottom: 12px;">
                    <div style="font-weight: bold; margin-bottom: 4px;">Color Preview:</div>
                    <div style="display: flex; align-items: center;">
                        <div style="display: inline-block; width: 24px; height: 24px; ${colorStyle} border-radius: 4px; margin-right: 8px; vertical-align: middle;"></div>
                        <span><code>$color</code></span>
                    </div>
                </div>
                """
            } else ""
            
            // Get CSS property if available
            val cssPropertyHtml = if (docJSON.has("cssProperty")) {
                val cssProperty = docJSON.getString("cssProperty")
                val cssValue = docJSON.optString("cssValue", "")
                
                // Check if we have multiple properties (comma-separated)
                val properties = cssProperty.split(",").map { it.trim() }
                val values = if (cssValue.contains(",")) {
                    cssValue.split(",").map { it.trim() }
                } else {
                    List(properties.size) { cssValue }
                }
                
                val cssLines = StringBuilder()
                properties.forEachIndexed { index, prop ->
                    val value = if (index < values.size) values[index] else values.last()
                    cssLines.append("&nbsp;&nbsp;<span style=\"color: #07a;\">$prop</span>: <span style=\"color: #a67\">$value</span>;<br/>\n")
                }
                
                """
                <div style="margin-top: 10px; padding: 10px; background-color: #f8f8f8; border-radius: 4px; font-family: monospace;">
                    <div style="color: #666; font-weight: bold;">Generated CSS:</div>
                    <div style="margin-top: 5px; color: #333;">
                        <span style="color: #905;">.${className}</span> {<br/>
                        $cssLines
                        }
                    </div>
                </div>
                """
            } else {
                // Try to infer CSS properties if not provided
                val cssProps = inferCssProperties(className)
                if (cssProps.isNotEmpty()) {
                    val cssLines = StringBuilder()
                    cssProps.forEach { (prop, value) ->
                        cssLines.append("&nbsp;&nbsp;<span style=\"color: #07a;\">$prop</span>: <span style=\"color: #a67\">$value</span>;<br/>\n")
                    }
                    
                    """
                    <div style="margin-top: 10px; padding: 10px; background-color: #f8f8f8; border-radius: 4px; font-family: monospace;">
                        <div style="color: #666; font-weight: bold;">Inferred CSS:</div>
                        <div style="margin-top: 5px; color: #333;">
                            <span style="color: #905;">.${className}</span> {<br/>
                            $cssLines
                            }
                        </div>
                    </div>
                    """
                } else ""
            }
            
            // Enhanced version information
            val versionInfo = if (isTailwindV4Class(className)) {
                """
                <div style="display: inline-block; margin-top: 8px; padding: 2px 8px; background-color: #0ea5e9; color: white; font-size: 12px; border-radius: 12px;">
                    Tailwind v4
                </div>
                """
            } else ""
            
            // Check for responsive or state variants
            val stateHtml = if (className.contains(":") || docJSON.has("state")) {
                // If it has explicit state information in JSON
                if (docJSON.has("state")) {
                    val state = docJSON.getString("state")
                    val baseClass = docJSON.optString("baseClass", "")
                    val stateDescription = docJSON.optString("stateDescription", "")
                    
                    """
                    <div style="margin-top: 12px; padding: 8px; background-color: #f0f9ff; border-radius: 4px; border-left: 3px solid #3b82f6;">
                        <div style="font-weight: bold;">Variant: <code>$state</code></div>
                        <div style="margin-top: 4px;">$stateDescription</div>
                        <div style="margin-top: 4px;">Applied to: <code>$baseClass</code></div>
                    </div>
                    """
                } 
                // Infer variant information from the class name
                else if (className.contains(":")) {
                    val parts = className.split(":")
                    val variant = parts.first()
                    val baseClass = parts.last()
                    
                    val variantDescription = when (variant) {
                        "hover" -> "Applied when the element is hovered"
                        "focus" -> "Applied when the element has focus"
                        "active" -> "Applied when the element is active"
                        "disabled" -> "Applied when the element is disabled"
                        "sm" -> "Applied at small screen sizes (640px and above)"
                        "md" -> "Applied at medium screen sizes (768px and above)"
                        "lg" -> "Applied at large screen sizes (1024px and above)"
                        "xl" -> "Applied at extra large screen sizes (1280px and above)"
                        "2xl" -> "Applied at 2x extra large screen sizes (1536px and above)"
                        "dark" -> "Applied in dark mode"
                        else -> "Applied in $variant state"
                    }
                    
                    """
                    <div style="margin-top: 12px; padding: 8px; background-color: #f0f9ff; border-radius: 4px; border-left: 3px solid #3b82f6;">
                        <div style="font-weight: bold;">Variant: <code>$variant</code></div>
                        <div style="margin-top: 4px;">$variantDescription</div>
                        <div style="margin-top: 4px;">Base class: <code>$baseClass</code></div>
                    </div>
                    """
                } else ""
            } else ""
            
            // Build examples section with improved formatting
            val examples = docJSON.optJSONArray("examples")
            val examplesHtml = if (examples != null && examples.length() > 0) {
                val exampleBuilder = StringBuilder()
                exampleBuilder.append("""
                    <div style="margin-top: 12px;">
                        <div style="font-weight: bold; margin-bottom: 5px;">Examples:</div>
                        <div style="background-color: #f8f8f8; padding: 10px; border-radius: 4px;">
                """.trimIndent())
                
                for (i in 0 until examples.length()) {
                    exampleBuilder.append("<code>${examples.getString(i)}</code>")
                    if (i < examples.length() - 1) {
                        exampleBuilder.append("<br/>")
                    }
                }
                
                exampleBuilder.append("</div></div>")
                exampleBuilder.toString()
            } else ""
            
            // Create a visual example for layout classes
            val visualExampleHtml = when {
                (className == "flex" || className.startsWith("flex-")) && !docJSON.has("examples") -> {
                    """
                    <div style="margin-top: 12px;">
                        <div style="font-weight: bold; margin-bottom: 4px;">Layout Example:</div>
                        <div style="display: flex; background-color: #f3f4f6; padding: 8px; border-radius: 4px;">
                            <div style="background-color: #3b82f6; color: white; padding: 4px 8px; margin: 4px; border-radius: 2px;">Item 1</div>
                            <div style="background-color: #3b82f6; color: white; padding: 4px 8px; margin: 4px; border-radius: 2px;">Item 2</div>
                            <div style="background-color: #3b82f6; color: white; padding: 4px 8px; margin: 4px; border-radius: 2px;">Item 3</div>
                        </div>
                    </div>
                    """
                }
                (className == "grid" || className.startsWith("grid-")) && !docJSON.has("examples") -> {
                    """
                    <div style="margin-top: 12px;">
                        <div style="font-weight: bold; margin-bottom: 4px;">Layout Example:</div>
                        <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; background-color: #f3f4f6; padding: 8px; border-radius: 4px;">
                            <div style="background-color: #3b82f6; color: white; padding: 4px 8px; border-radius: 2px; text-align: center;">1</div>
                            <div style="background-color: #3b82f6; color: white; padding: 4px 8px; border-radius: 2px; text-align: center;">2</div>
                            <div style="background-color: #3b82f6; color: white; padding: 4px 8px; border-radius: 2px; text-align: center;">3</div>
                            <div style="background-color: #3b82f6; color: white; padding: 4px 8px; border-radius: 2px; text-align: center;">4</div>
                        </div>
                    </div>
                    """
                }
                else -> ""
            }
            
            // Related classes
            val relatedClasses = findRelatedClasses(className)
            val relatedHtml = if (relatedClasses.isNotEmpty()) {
                val links = relatedClasses.joinToString("") { "<code style=\"margin-right: 8px;\">$it</code>" }
                """
                <div style="margin-top: 12px;">
                    <div style="font-weight: bold; margin-bottom: 4px;">Related Classes:</div>
                    <div style="display: flex; flex-wrap: wrap; gap: 4px;">
                        $links
                    </div>
                </div>
                """
            } else ""
            
            return """
                <html>
                    <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', sans-serif;">
                        <div style="display: flex; align-items: center; margin-bottom: 10px;">
                            <span style="font-size: 16px; margin-right: 8px;">$icon</span>
                            <span style="font-size: 16px; font-weight: bold;">$className</span>
                            $versionInfo
                        </div>
                        <div style="margin-bottom: 10px; color: #374151;">
                            <span><i>$description</i></span>
                            <br/>
                            <span><b>Category:</b> $category</span>
                        </div>
                        $colorHtml
                        $cssPropertyHtml
                        $visualExampleHtml
                        $stateHtml
                        $examplesHtml
                        $relatedHtml
                    </body>
                </html>
            """.trimIndent()
        } catch (e: Exception) {
            logger.error("Error creating rich documentation: ${e.message}")
            return generateBasicDocumentation(className)
        }
    }
    
    /**
     * Find the Tailwind class at the cursor position
     */
    private fun findClassAtCursor(text: String, cursorOffset: Int): String? {
        if (text.isBlank() || cursorOffset < 0 || cursorOffset > text.length) {
            return null
        }
        
        // Split into classes
        val classes = text.split(Regex("\\s+"))
        
        var currentPosition = 0
        
        for (className in classes) {
            if (className.isEmpty()) {
                currentPosition++
                continue
            }
            
            val start = text.indexOf(className, currentPosition)
            if (start == -1) continue
            
            val end = start + className.length
            
            // Check if cursor is within this class
            if (cursorOffset in start..end) {
                return className
            }
            
            currentPosition = end
        }
        
        return null
    }
    
    /**
     * Generate basic documentation for classes that don't have specific data
     */
    private fun generateBasicDocumentation(className: String): String {
        // Extract prefix and value from class name
        val (prefix, value) = extractPrefixAndValue(className)
        
        // Get category icon
        val categoryIcon = getCategoryIcon(prefix)
        
        // Get description based on prefix
        val description = getDescriptionForPrefix(prefix, value)
        
        // Infer CSS properties
        val cssProps = inferCssProperties(className)
        val cssHtml = if (cssProps.isNotEmpty()) {
            val cssLines = StringBuilder()
            cssProps.forEach { (prop, value) ->
                cssLines.append("&nbsp;&nbsp;<span style=\"color: #07a;\">$prop</span>: <span style=\"color: #a67\">$value</span>;<br/>\n")
            }
            
            """
            <div style="margin-top: 10px; padding: 10px; background-color: #f8f8f8; border-radius: 4px; font-family: monospace;">
                <div style="color: #666; font-weight: bold;">Inferred CSS:</div>
                <div style="margin-top: 5px; color: #333;">
                    <span style="color: #905;">.${className}</span> {<br/>
                    $cssLines
                    }
                </div>
            </div>
            """
        } else ""
        
        // Check for responsive or state variants
        val variantHtml = if (className.contains(":")) {
            val parts = className.split(":")
            val variant = parts.first()
            val baseClass = parts.last()
            
            val variantDescription = when (variant) {
                "hover" -> "Applied when the element is hovered"
                "focus" -> "Applied when the element has focus"
                "active" -> "Applied when the element is active"
                "disabled" -> "Applied when the element is disabled"
                "sm" -> "Applied at small screen sizes (640px and above)"
                "md" -> "Applied at medium screen sizes (768px and above)"
                "lg" -> "Applied at large screen sizes (1024px and above)"
                "xl" -> "Applied at extra large screen sizes (1280px and above)"
                "2xl" -> "Applied at 2x extra large screen sizes (1536px and above)"
                "dark" -> "Applied in dark mode"
                else -> "Applied in $variant state"
            }
            
            """
            <div style="margin-top: 12px; padding: 8px; background-color: #f0f9ff; border-radius: 4px; border-left: 3px solid #3b82f6;">
                <div style="font-weight: bold;">Variant: <code>$variant</code></div>
                <div style="margin-top: 4px;">$variantDescription</div>
                <div style="margin-top: 4px;">Base class: <code>$baseClass</code></div>
            </div>
            """
        } else ""
        
        // Add tailwind version badge if applicable
        val versionInfo = if (isTailwindV4Class(className)) {
            """
            <div style="display: inline-block; margin-left: 8px; padding: 2px 8px; background-color: #0ea5e9; color: white; font-size: 12px; border-radius: 12px;">
                Tailwind v4
            </div>
            """
        } else ""
        
        return """
            <html>
                <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', sans-serif;">
                    <div style="display: flex; align-items: center; margin-bottom: 10px;">
                        <span style="font-size: 16px; margin-right: 8px;">$categoryIcon</span>
                        <span style="font-size: 16px; font-weight: bold;">$className</span>
                        $versionInfo
                    </div>
                    <div style="margin-bottom: 10px; color: #374151;">
                        <span><i>$description</i></span>
                    </div>
                    $cssHtml
                    $variantHtml
                </body>
            </html>
        """.trimIndent()
    }
    
    /**
     * Extract the prefix and value from a Tailwind class name
     */
    private fun extractPrefixAndValue(className: String): Pair<String, String> {
        // Handle variant prefixes (e.g., hover:, focus:, md:)
        val baseClass = if (className.contains(":")) {
            className.split(":").last()
        } else {
            className
        }
        
        // Match common patterns like bg-red-500, w-1/2, text-sm, etc.
        val regex = Regex("([\\w-]+)[-/](.+)")
        val matchResult = regex.find(baseClass)
        
        return if (matchResult != null) {
            val (prefix, value) = matchResult.destructured
            Pair(prefix, value)
        } else {
            // For classes without a clear separator (e.g., flex, hidden)
            Pair(baseClass, "")
        }
    }
    
    /**
     * Generate an appropriate icon for a category
     */
    private fun getCategoryIcon(prefix: String): String {
        return when (prefix) {
            "bg" -> "🎨" // Background
            "text" -> "🔤" // Text
            "font" -> "📝" // Font
            "m", "p", "mt", "mb", "ml", "mr", "mx", "my", 
            "pt", "pb", "pl", "pr", "px", "py" -> "📏" // Spacing
            "w", "h", "min-w", "min-h", "max-w", "max-h" -> "📐" // Width/Height
            "border" -> "🔲" // Border
            "rounded" -> "⚪" // Rounded
            "flex", "grid" -> "🧩" // Layout
            "justify", "items", "content" -> "↔️" // Alignment
            "shadow" -> "👥" // Shadow
            "opacity" -> "🌫️" // Opacity
            "z" -> "🔢" // Z-index
            "overflow" -> "📦" // Overflow
            "transition" -> "🔄" // Transition
            "animate" -> "✨" // Animation
            "transform", "rotate", "scale", "translate" -> "🔄" // Transform
            "cursor" -> "👆" // Cursor
            "select" -> "✓" // Selection
            "outline" -> "✒️" // Outline
            "ring" -> "⭕" // Ring/Focus
            "filter" -> "🔍" // Filter
            "backdrop" -> "🖼️" // Backdrop
            "table" -> "🏓" // Table
            "object" -> "🖼️" // Object-fit/position
            "mix" -> "🎭" // Mix blend
            else -> "🧰" // General utilities
        }
    }
    
    /**
     * Get description based on the prefix
     */
    private fun getDescriptionForPrefix(prefix: String, value: String): String {
        return when (prefix) {
            "bg" -> "Sets the background color to ${prettifyValue(value)}"
            "text" -> if (value.matches(Regex("\\w+\\d+"))) {
                "Sets the text color to ${prettifyValue(value)}"
            } else {
                "Sets the text size to ${prettifyValue(value)}"
            }
            "font" -> "Sets the font family or weight to ${prettifyValue(value)}"
            "m", "margin" -> "Sets margin on all sides to ${prettifyValue(value)}"
            "p", "padding" -> "Sets padding on all sides to ${prettifyValue(value)}"
            "mt" -> "Sets top margin to ${prettifyValue(value)}"
            "mb" -> "Sets bottom margin to ${prettifyValue(value)}"
            "ml" -> "Sets left margin to ${prettifyValue(value)}"
            "mr" -> "Sets right margin to ${prettifyValue(value)}"
            "mx" -> "Sets horizontal margin to ${prettifyValue(value)}"
            "my" -> "Sets vertical margin to ${prettifyValue(value)}"
            "pt" -> "Sets top padding to ${prettifyValue(value)}"
            "pb" -> "Sets bottom padding to ${prettifyValue(value)}"
            "pl" -> "Sets left padding to ${prettifyValue(value)}"
            "pr" -> "Sets right padding to ${prettifyValue(value)}"
            "px" -> "Sets horizontal padding to ${prettifyValue(value)}"
            "py" -> "Sets vertical padding to ${prettifyValue(value)}"
            "w" -> "Sets width to ${prettifyValue(value)}"
            "h" -> "Sets height to ${prettifyValue(value)}"
            "min-w" -> "Sets minimum width to ${prettifyValue(value)}"
            "min-h" -> "Sets minimum height to ${prettifyValue(value)}"
            "max-w" -> "Sets maximum width to ${prettifyValue(value)}"
            "max-h" -> "Sets maximum height to ${prettifyValue(value)}"
            "border" -> if (value.isEmpty()) "Adds a border with default width" else "Sets border color to ${prettifyValue(value)}"
            "border-t" -> "Adds a top border"
            "border-b" -> "Adds a bottom border"
            "border-l" -> "Adds a left border"
            "border-r" -> "Adds a right border"
            "rounded" -> if (value.isEmpty()) "Adds default border radius" else "Sets border radius to ${prettifyValue(value)}"
            "flex" -> if (value.isEmpty()) "Sets display to flex" else "Sets flex properties to ${prettifyValue(value)}"
            "grid" -> "Sets display to grid"
            "justify" -> "Sets justify-content to ${prettifyValue(value)}"
            "items" -> "Sets align-items to ${prettifyValue(value)}"
            "content" -> "Sets align-content to ${prettifyValue(value)}"
            "shadow" -> if (value.isEmpty()) "Adds default shadow" else "Sets shadow to ${prettifyValue(value)}"
            "opacity" -> "Sets opacity to ${prettifyValue(value)}"
            "z" -> "Sets z-index to ${prettifyValue(value)}"
            "overflow" -> "Sets overflow to ${prettifyValue(value)}"
            "transition" -> if (value.isEmpty()) "Adds default transition" else "Sets transition property to ${prettifyValue(value)}"
            "animate" -> "Applies animation ${prettifyValue(value)}"
            "transform" -> "Applies CSS transform"
            "rotate" -> "Rotates element by ${prettifyValue(value)}"
            "scale" -> "Scales element by ${prettifyValue(value)}"
            "translate" -> "Translates element by ${prettifyValue(value)}"
            "cursor" -> "Sets cursor to ${prettifyValue(value)}"
            "select" -> "Sets user-select to ${prettifyValue(value)}"
            "outline" -> "Controls the outline"
            "hidden" -> "Sets display to none"
            "block" -> "Sets display to block"
            "inline" -> "Sets display to inline"
            "inline-block" -> "Sets display to inline-block"
            "relative" -> "Sets position to relative"
            "absolute" -> "Sets position to absolute"
            "fixed" -> "Sets position to fixed"
            "sticky" -> "Sets position to sticky"
            else -> if (value.isEmpty()) "Tailwind utility class '$prefix'" else "Tailwind utility class '$prefix-$value'"
        }
    }
    
    /**
     * Generate error documentation to show to users when an exception occurs
     */
    private fun generateErrorDocumentation(message: String, exception: Exception? = null): String {
        val errorMessage = message.takeIf { it.isNotBlank() } ?: "An error occurred"
        
        val stackTraceHtml = exception?.let {
            val stackTrace = it.stackTraceToString()
            
            """
            <div style="margin-top: 10px;">
                <details>
                    <summary style="color: #666; cursor: pointer; font-weight: bold;">See Details</summary>
                    <div style="margin-top: 5px; padding: 10px; background-color: #f8f8f8; border-radius: 4px; font-family: monospace; font-size: 12px; white-space: pre-wrap; overflow-x: auto;">
                        ${it.message ?: ""}
                        $stackTrace
                    </div>
                </details>
            </div>
            """
        } ?: ""
        
        return """
            <html>
                <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', sans-serif;">
                    <div style="padding: 10px; background-color: #fee2e2; border-radius: 4px; border-left: 4px solid #ef4444;">
                        <div style="font-weight: bold; color: #b91c1c;">Error</div>
                        <div style="margin-top: 5px; color: #374151;">$errorMessage</div>
                        $stackTraceHtml
                    </div>
                </body>
            </html>
        """.trimIndent()
    }
    
    /**
     * Identify if a class name belongs to Tailwind v4
     */
    private fun isTailwindV4Class(className: String): Boolean {
        // This is a simplistic check - in a real implementation you might have a more comprehensive list
        val v4PrefixList = listOf(
            "plc-", // placeholder color classes
            "shadow-", // updated shadow utility
            "text-balance", // text balancing
            "text-pretty", // improved text wrapping
            "font-", // new font variants
            "size-", // size utility (replaces w-* + h-*)
            "grid-flow-", // updated grid flow utilities
            "backdrop-", // updated backdrop utilities
            "text-wrap-", // text wrapping utilities
            "columns-" // updated column utilities
        )
        
        return v4PrefixList.any { className.startsWith(it) }
    }
    
    /**
     * Get contrasting text color for a background
     */
    private fun getContrastingTextColor(backgroundColor: String): String {
        // Simple algorithm: for hex colors, calculate brightness and return white for dark backgrounds, black for light
        if (backgroundColor.startsWith("#")) {
            try {
                val hexColor = backgroundColor.substring(1)
                val r = hexColor.substring(0, 2).toInt(16)
                val g = hexColor.substring(2, 4).toInt(16)
                val b = hexColor.substring(4, 6).toInt(16)
                
                // Calculate perceived brightness using YIQ formula
                val brightness = (r * 299 + g * 587 + b * 114) / 1000
                
                return if (brightness > 128) "#000000" else "#ffffff"
            } catch (e: Exception) {
                return "#000000"
            }
        }
        
        // Fallback
        return "#000000"
    }
    
    /**
     * Extract color value from a Tailwind color class name
     */
    private fun extractColorValue(className: String): String? {
        // This is a simplified mapping - a real implementation would have a comprehensive color map
        val colorMap = mapOf(
            "red" to "#ef4444",
            "blue" to "#3b82f6",
            "green" to "#10b981",
            "yellow" to "#f59e0b",
            "purple" to "#8b5cf6",
            "pink" to "#ec4899",
            "indigo" to "#6366f1",
            "gray" to "#9ca3af",
            "white" to "#ffffff",
            "black" to "#000000"
        )
        
        // Try to match color name and shade (e.g., "bg-red-500")
        val regex = Regex("(bg|text|border)-([\\w-]+)(?:-(\\d+))?")
        val match = regex.find(className)
        
        if (match != null) {
            val colorName = match.groupValues[2]
            val shade = match.groupValues.getOrNull(3)
            
            // Return matching color or null if not found
            return colorMap[colorName]
        }
        
        return null
    }
    
    /**
     * Infer CSS properties based on the class name
     */
    private fun inferCssProperties(className: String): Map<String, String> {
        val properties = mutableMapOf<String, String>()
        val (prefix, value) = extractPrefixAndValue(className)
        
        when (prefix) {
            "bg" -> properties["background-color"] = "var(--tw-${value}, #ccc)"
            "text" -> {
                if (value.matches(Regex("\\w+\\d+"))) {
                    properties["color"] = "var(--tw-${value}, #333)"
                } else {
                    properties["font-size"] = when(value) {
                        "xs" -> "0.75rem"
                        "sm" -> "0.875rem"
                        "md" -> "1rem"
                        "lg" -> "1.125rem"
                        "xl" -> "1.25rem"
                        "2xl" -> "1.5rem"
                        "3xl" -> "1.875rem"
                        "4xl" -> "2.25rem"
                        "5xl" -> "3rem"
                        else -> value
                    }
                }
            }
            "font" -> {
                if (value.matches(Regex("\\d+"))) {
                    properties["font-weight"] = value
                } else {
                    properties["font-family"] = "var(--tw-font-${value}, sans-serif)"
                }
            }
            "w" -> properties["width"] = parseDimension(value)
            "h" -> properties["height"] = parseDimension(value)
            "m" -> properties["margin"] = parseDimension(value)
            "p" -> properties["padding"] = parseDimension(value)
            "mt" -> properties["margin-top"] = parseDimension(value)
            "mb" -> properties["margin-bottom"] = parseDimension(value)
            "ml" -> properties["margin-left"] = parseDimension(value)
            "mr" -> properties["margin-right"] = parseDimension(value)
            "mx" -> {
                properties["margin-left"] = parseDimension(value)
                properties["margin-right"] = parseDimension(value)
            }
            "my" -> {
                properties["margin-top"] = parseDimension(value)
                properties["margin-bottom"] = parseDimension(value)
            }
            "pt" -> properties["padding-top"] = parseDimension(value)
            "pb" -> properties["padding-bottom"] = parseDimension(value)
            "pl" -> properties["padding-left"] = parseDimension(value)
            "pr" -> properties["padding-right"] = parseDimension(value)
            "px" -> {
                properties["padding-left"] = parseDimension(value)
                properties["padding-right"] = parseDimension(value)
            }
            "py" -> {
                properties["padding-top"] = parseDimension(value)
                properties["padding-bottom"] = parseDimension(value)
            }
            "border" -> {
                if (value.isEmpty()) {
                    properties["border-width"] = "1px"
                    properties["border-style"] = "solid"
                } else {
                    properties["border-color"] = "var(--tw-${value}, #ccc)"
                }
            }
            "rounded" -> {
                properties["border-radius"] = if (value.isEmpty()) "0.25rem" else parseDimension(value)
            }
            "flex" -> {
                properties["display"] = "flex"
                if (value.isNotEmpty()) {
                    properties["flex"] = value
                }
            }
            "grid" -> properties["display"] = "grid"
            "justify" -> properties["justify-content"] = value
            "items" -> properties["align-items"] = value
            "shadow" -> properties["box-shadow"] = if (value.isEmpty()) "0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06)" else "var(--tw-shadow-${value})"
            "opacity" -> properties["opacity"] = value
            "z" -> properties["z-index"] = value
            "overflow" -> properties["overflow"] = value
            "hidden" -> properties["display"] = "none"
            "block" -> properties["display"] = "block"
            "inline" -> properties["display"] = "inline"
            "inline-block" -> properties["display"] = "inline-block"
            "relative" -> properties["position"] = "relative"
            "absolute" -> properties["position"] = "absolute"
            "fixed" -> properties["position"] = "fixed"
            "sticky" -> properties["position"] = "sticky"
        }
        
        return properties
    }
    
    /**
     * Parse dimension value
     */
    private fun parseDimension(value: String): String {
        return when {
            value.matches(Regex("\\d+")) -> "${value}px"
            value.matches(Regex("\\d+/\\d+")) -> {
                // Handle fractions like 1/2, 3/4, etc.
                val parts = value.split("/")
                val numerator = parts[0].toDoubleOrNull() ?: 1.0
                val denominator = parts[1].toDoubleOrNull() ?: 1.0
                "${(numerator / denominator) * 100}%"
            }
            value == "full" -> "100%"
            value == "screen" -> "100vh"
            value == "auto" -> "auto"
            else -> value
        }
    }
    
    /**
     * Find related Tailwind classes
     */
    private fun findRelatedClasses(className: String): List<String> {
        val (prefix, value) = extractPrefixAndValue(className)
        val related = mutableListOf<String>()
        
        // For color utilities, suggest other color utilities with same color
        if (prefix in listOf("bg", "text", "border") && value.isNotEmpty()) {
            val colorPrefixes = listOf("bg", "text", "border")
            for (p in colorPrefixes) {
                if (p != prefix) {
                    related.add("$p-$value")
                }
            }
        }
        
        // For spacing utilities, suggest related ones
        if (prefix in listOf("m", "p", "mt", "mb", "ml", "mr", "mx", "my",
                "pt", "pb", "pl", "pr", "px", "py") && value.isNotEmpty()) {
            when (prefix) {
                "m" -> related.addAll(listOf("mx-$value", "my-$value"))
                "p" -> related.addAll(listOf("px-$value", "py-$value"))
                "mx" -> related.addAll(listOf("ml-$value", "mr-$value"))
                "my" -> related.addAll(listOf("mt-$value", "mb-$value"))
                "px" -> related.addAll(listOf("pl-$value", "pr-$value"))
                "py" -> related.addAll(listOf("pt-$value", "pb-$value"))
            }
        }
        
        // For dimension utilities
        if (prefix in listOf("w", "h") && value.isNotEmpty()) {
            if (prefix == "w") related.add("h-$value")
            if (prefix == "h") related.add("w-$value")
        }
        
        return related
    }
    
    /**
     * Helper function to prettify values for display
     */
    private fun prettifyValue(value: String): String {
        return when {
            value.isEmpty() -> ""
            value == "auto" -> "auto"
            value.matches(Regex("\\d+")) -> "${value}px"
            value.matches(Regex("\\d+/\\d+")) -> {
                val parts = value.split("/")
                "${parts[0]}/${parts[1]}"
            }
            else -> value
        }
    }
}
