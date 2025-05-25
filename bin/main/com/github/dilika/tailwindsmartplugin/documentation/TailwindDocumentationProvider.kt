package com.github.dilika.tailwindsmartplugin.documentation

import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.xml.*
import com.intellij.openapi.project.Project
import org.json.JSONObject

/**
 * Provides documentation for Tailwind CSS classes
 */
@Suppress("unused") // Registered via plugin.xml
class TailwindDocumentationProvider : AbstractDocumentationProvider() {
    private val logger = Logger.getInstance(TailwindDocumentationProvider::class.java)
    private val enhancedDocumentation = TailwindEnhancedDocumentation()
    
    // Key for storing Tailwind class name in element user data
    internal val TAILWIND_CLASS_KEY = com.intellij.openapi.util.Key<String>("TAILWIND_CLASS_NAME")
    
    /**
     * Helper function to prettify values for display
     */
    internal fun prettifyValue(value: String): String {
        return when {
            value.isEmpty() -> ""
            value == "auto" -> "auto"
            value == "none" -> "none"
            value == "full" -> "100%"
            value == "screen" -> "100vh"
            value.matches(Regex("\\d+/\\d+")) -> {
                val parts = value.split("/")
                try {
                    val numerator = parts[0].toInt()
                    val denominator = parts[1].toInt()
                    if (denominator != 0) {
                        val percentage = (numerator.toDouble() / denominator) * 100
                        String.format("%.1f%%", percentage)
                    } else {
                        value
                    }
                } catch (e: Exception) {
                    value
                }
            }
            value.endsWith("%") -> value
            value.endsWith("px") -> value
            value.endsWith("rem") -> value
            value.endsWith("em") -> value
            value.startsWith("#") -> value
            value.matches(Regex("\\d+")) -> "${value}px"
            else -> value
        }
    }
    
    /**
     * Parse dimension value
     */
    internal fun parseDimension(value: String): String {
        return when {
            value.matches(Regex("\\d+")) -> "${value}px"
            value.matches(Regex("\\d+/\\d+")) -> {
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
     * Checks if a character is valid in a Tailwind class name
     */
    internal fun isTailwindWordChar(c: Char): Boolean {
        return c.isLetterOrDigit() || c == '-' || c == ':' || c == '[' || c == ']' || c == '.' || c == '/' || c == '#'
    }
    
    /**
     * Get HTML for arbitrary value classes like w-[100px]
     */
    internal fun getArbitraryValueHtml(className: String): String {
        val regex = Regex("([\\w-]+)\\[([^\\]]+)\\]")
        val match = regex.find(className) ?: return "<html><body>Unable to parse arbitrary value: $className</body></html>"
        
        val (property, value) = match.destructured
        val cssProperty = when (property) {
            "w" -> "width"
            "h" -> "height"
            "m" -> "margin"
            "p" -> "padding"
            "bg" -> "background-color"
            "text" -> "color"
            "border" -> "border-color"
            "rounded" -> "border-radius"
            else -> property
        }
        
        val categoryIcon = getCategoryIcon(property)
        val description = getDescriptionForPrefix(property, value)
        val cssHtml = """
            <table class="sections">
                <tr>
                    <td valign="top"><p>CSS:</p></td>
                    <td valign="top">
                        <p><code>.${className} { ${cssProperty}: ${value}; }</code></p>
                    </td>
                </tr>
            </table>
        """.trimIndent()
        
        val versionInfo = if (isTailwindV4Class(className)) {
            " <span class='grayed'>[Tailwind v4]</span>"
        } else ""
        
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
        
        val visualExampleHtml = when {
            property == "z" && value.isNotEmpty() -> {
                """
                <table class="sections">
                    <tr>
                        <td valign="top"><p>Example:</p></td>
                        <td valign="top">
                            <div style="position: relative; height: 50px; width: 140px; background: #eee;">
                                <div style="position: absolute; top: 5px; left: 5px; width: 30px; height: 30px; background-color: #ef4444; z-index: 0; text-align: center; line-height: 30px; color: white;">0</div>
                                <div style="position: absolute; top: 15px; left: 25px; width: 30px; height: 30px; background-color: #3b82f6; z-index: ${value}; text-align: center; line-height: 30px; color: white;">${value}</div>
                                <div style="position: absolute; top: 25px; left: 45px; width: 30px; height: 30px; background-color: #10b981; z-index: 20; text-align: center; line-height: 30px; color: white;">20</div>
                            </div>
                        </td>
                    </tr>
                </table>
                """
            }
            (className == "flex" || className.startsWith("flex-")) -> {
                """
                <table class="sections">
                    <tr>
                        <td valign="top"><p>Example:</p></td>
                        <td valign="top">
                            <div style="display: flex; background: #eee; width: 200px;">
                                <div style="background-color: #3b82f6; color: white; padding: 3px; margin: 3px; font-size: 11px;">Item 1</div>
                                <div style="background-color: #3b82f6; color: white; padding: 3px; margin: 3px; font-size: 11px;">Item 2</div>
                                <div style="background-color: #3b82f6; color: white; padding: 3px; margin: 3px; font-size: 11px;">Item 3</div>
                            </div>
                        </td>
                    </tr>
                </table>
                """
            }
            (className == "grid" || className.startsWith("grid-")) -> {
                """
                <table class="sections">
                    <tr>
                        <td valign="top"><p>Example:</p></td>
                        <td valign="top">
                            <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 6px; background: #eee; width: 100px; height: 100px;">
                                <div style="background-color: #3b82f6; color: white; display: flex; align-items: center; justify-content: center; font-size: 11px;">1</div>
                                <div style="background-color: #3b82f6; color: white; display: flex; align-items: center; justify-content: center; font-size: 11px;">2</div>
                                <div style="background-color: #3b82f6; color: white; display: flex; align-items: center; justify-content: center; font-size: 11px;">3</div>
                                <div style="background-color: #3b82f6; color: white; display: flex; align-items: center; justify-content: center; font-size: 11px;">4</div>
                            </div>
                        </td>
                    </tr>
                </table>
                """
            }
            else -> ""
        }
        
        val relatedClasses = findRelatedClasses(className)
        val relatedHtml = if (relatedClasses.isNotEmpty()) {
            val links = relatedClasses.joinToString(", ") { "<code>$it</code>" }
            """
            <table class="sections">
                <tr>
                    <td valign="top"><p>Related:</p></td>
                    <td valign="top">
                        <p>$links</p>
                    </td>
                </tr>
            </table>
            """
        } else ""
        
        return """
            <html>
                <head>
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, sans-serif; font-size: 13px; }
                        pre { font-family: monospace; }
                        code { font-family: monospace; white-space: nowrap; }
                        p { margin: 5px 0; }
                        .title { font-weight: bold; font-size: 14px; margin-bottom: 6px; }
                        .sections { margin-top: 8px; border-spacing: 0; }
                        .sections td { padding: 3px; vertical-align: top; }
                        .sections td:first-child { color: #787878; white-space: nowrap; padding-right: 10px; }
                        .grayed { color: #787878; }
                    </style>
                </head>
                <body>
                    <div class='title'>$className $versionInfo</div>
                    <div>$description</div>
                    $cssHtml
                    $visualExampleHtml
                    $variantHtml
                    $relatedHtml
                </body>
            </html>
        """.trimIndent()
    }
    
    /**
     * This method is critical for hovering documentation.
     * It's called before any other documentation method to identify what element should receive documentation.
     */
    override fun getCustomDocumentationElement(editor: com.intellij.openapi.editor.Editor, file: com.intellij.psi.PsiFile, contextElement: PsiElement?, targetOffset: Int): PsiElement? {
        try {
            logger.debug("getCustomDocumentationElement called for ${file.name} at offset $targetOffset")
            
            // Skip if contextElement is null
            if (contextElement == null) {
                logger.debug("Context element is null, exiting")
                return null
            }
            
            // Extract text around cursor for debugging
            val document = editor.document
            val lineNumber = document.getLineNumber(targetOffset)
            val lineStart = document.getLineStartOffset(lineNumber)
            val lineEnd = document.getLineEndOffset(lineNumber)
            val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStart, lineEnd))
            logger.debug("Line $lineNumber: $lineText")
            
            // Look for a class/className attribute or string that contains Tailwind classes
            var elementToCheck = contextElement
            var depth = 0
            val maxDepth = 5 // Prevent infinite loops
            
            // Check the element and its parents for Tailwind classes
            while (elementToCheck != null && depth < maxDepth) {
                // Debug info
                logger.debug("Checking element: ${elementToCheck.javaClass.simpleName}, text: '${elementToCheck.text.take(30)}'")
                
                // Check for class or className attributes
                val isClassAttribute = (elementToCheck is XmlAttributeValue && 
                    (elementToCheck.parent as? XmlAttribute)?.name?.lowercase() in listOf("class", "classname"))
                    
                // Check for JSX className attribute
                val isJsxClassName = elementToCheck.text.contains("className=") ||
                    elementToCheck.parent?.text?.contains("className=") == true
                    
                logger.debug("Is class attribute: $isClassAttribute, Is JSX className: $isJsxClassName")
                
                // Extract potential Tailwind class
                val className = extractClassName(elementToCheck, contextElement)
                logger.debug("Extracted class name: $className")
                
                if (className != null && isTailwindPattern(className, file.project)) {
                    logger.debug("Found Tailwind class: $className")
                    // Store the class name in user data for later retrieval
                    elementToCheck.putUserData(TAILWIND_CLASS_KEY, className)
                    return elementToCheck
                }
                
                // Check parent
                elementToCheck = elementToCheck.parent
                depth++
            }
            
            logger.debug("No Tailwind class found")
            return null
        } catch (e: Exception) {
            logger.error("Error in getCustomDocumentationElement: ${e.message}")
            return null
        }
    }
    
    /**
     * Override getQuickNavigateInfo to ensure Tailwind documentation is prioritized
     * This method is called first for quick documentation display
     */
    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        // Check if we've previously identified a Tailwind class in this element
        val tailwindClass = element?.getUserData(TAILWIND_CLASS_KEY)
        if (tailwindClass != null) {
            logger.debug("Found Tailwind class in user data: $tailwindClass")
            return generateDocForClass(tailwindClass, element.project)
        }
        
        return generateTailwindDoc(element, originalElement)
    }
    
    /**
     * Main documentation generation method
     */
    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        // Check if we've previously identified a Tailwind class in this element
        val tailwindClass = element?.getUserData(TAILWIND_CLASS_KEY)
        if (tailwindClass != null) {
            logger.debug("Found Tailwind class in user data: $tailwindClass")
            return generateDocForClass(tailwindClass, element.project)
        }
        
        return generateTailwindDoc(element, originalElement)
    }
    
    /**
     * Generate Tailwind documentation with high priority
     */
    internal fun generateTailwindDoc(element: PsiElement?, originalElement: PsiElement?): String? {
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
            
            // Check if this is a Tailwind class
            // 1. Get standard Tailwind classes
            val tailwindClasses = TailwindUtils.getTailwindClasses(project)
            // 2. Check if it's a CSS property or a Tailwind class pattern (bg-, text-, etc.)
            val isTailwindClass = tailwindClasses.contains(className) || 
                                 isTailwindPattern(className)
            
            if (!isTailwindClass) {
                // Not a Tailwind class, let other providers handle it
                return null
            }
            
            try {
                // Utiliser la documentation enrichie pour toutes les classes
                val enhancedDoc = enhancedDocumentation.generateDocumentation(className)
                
                if (tailwindClasses.contains(className)) {
                    try {
                        // Found in Tailwind classes - generate rich documentation with enhanced content
                        val richDoc = generateDocForClass(className, project)
                        // Combine both documentations with enhanced content first
                        return enhancedDoc + "<hr/>" + richDoc
                    } catch (e: Exception) {
                        logger.warn("Error generating rich documentation: ${e.message}")
                        // On continue avec la documentation simple si la riche échoue
                        return enhancedDoc.ifEmpty { generateBasicDocumentation(className) }
                    }
                } else {
                    // Not found in standard classes - use enhanced documentation or basic fallback
                    return enhancedDoc.ifEmpty { generateBasicDocumentation(className) }
                }
            } catch (e: Exception) {
                logger.warn("Enhanced documentation failed, using basic documentation: ${e.message}")
                // Fallback sur la documentation de base en cas d'échec de la documentation enrichie
                return generateBasicDocumentation(className)
            }
        } catch (e: Exception) {
            logger.error("Error generating documentation: ${e.message}")
            // En dernier recours seulement
            return generateBasicDocumentation(element.text ?: "unknown")
        }
    }
    
    /**
     * Check if a class name follows Tailwind naming patterns
     */
    internal fun isTailwindPattern(className: String, project: Project? = null): Boolean {
        // Common Tailwind prefixes
        val tailwindPrefixes = listOf(
            "bg-", "text-", "font-", "border-", "rounded-", "p-", "m-", "px-", "py-", "mx-", "my-",
            "flex-", "grid-", "gap-", "w-", "h-", "max-w-", "min-w-", "max-h-", "min-h-",
            "opacity-", "shadow-", "z-", "from-", "to-", "via-", "transition-", "ease-", "duration-",
            "scale-", "rotate-", "translate-", "skew-", "transform-", "space-", "divide-", "justify-",
            "align-", "list-", "float-", "overflow-", "tracking-", "leading-", "underline", "line-through",
            "italic", "not-italic", "uppercase", "lowercase", "capitalize", "normal-case", "truncate",
            "outline-", "ring-", "filter-", "blur-", "brightness-", "contrast-", "grayscale-", "invert-",
            "saturate-", "sepia-", "backdrop-"
        )
        
        // Common Tailwind responsive and state variants
        val tailwindVariants = listOf(
            "sm:", "md:", "lg:", "xl:", "2xl:", "hover:", "focus:", "active:", "disabled:",
            "visited:", "checked:", "first:", "last:", "even:", "odd:", "group-hover:", "peer-hover:",
            "dark:", "motion-safe:", "motion-reduce:", "print:", "rtl:", "ltr:"
        )
        
        // Check for variants + prefix patterns
        for (variant in tailwindVariants) {
            if (className.startsWith(variant)) {
                val baseClass = className.substring(variant.length)
                for (prefix in tailwindPrefixes) {
                    if (baseClass.startsWith(prefix)) {
                        return true
                    }
                }
            }
        }
        
        // Check for direct prefix patterns
        for (prefix in tailwindPrefixes) {
            if (className.startsWith(prefix)) {
                return true
            }
        }
        
        // Match JIT arbitrary values like p-[20px], text-[#336699], etc.
        if (className.contains("[") && className.contains("]")) {
            for (prefix in tailwindPrefixes) {
                if (className.startsWith(prefix)) {
                    return true
                }
            }
        }
        
        return false
    }
    
    /**
     * Extract class name from the PsiElement
     */
    internal fun extractClassName(element: PsiElement?, originalElement: PsiElement?): String? {
        logger.info("Extracting class name from element: ${element?.text}")
        
        if (element == null) return null
        
        try {
            return when {
                // If it's a class attribute in XML/HTML
                element is XmlAttributeValue && element.parent is XmlAttribute -> {
                    val attr = element.parent as XmlAttribute
                    val attrName = attr.name.lowercase()
                    
                    if (attrName == "class" || attrName == "classname") {
                        val text = element.value.trim()
                        val offset = originalElement?.textOffset?.minus(element.textRange.startOffset) ?: 0
                        findClassAtCursor(text, offset.toInt())
                    } else {
                        null
                    }
                }
                
                // JSX className attribute
                element is com.intellij.psi.PsiLanguageInjectionHost && element.parent?.text?.contains("className=") == true -> {
                    val text = element.text.trim().removeSurrounding("'").removeSurrounding("\"").trim()
                    // Get cursor position relative to this element
                    val offset = originalElement?.textOffset?.minus(element.textRange.startOffset) ?: 0
                    findClassAtCursor(text, offset.toInt())
                }
                
                // For plain strings (like in JavaScript)
                element.text.startsWith("\"") || element.text.startsWith("'") -> {
                    // If this looks like a quoted string containing a class name
                    val text = element.text.trim().removeSurrounding("'").removeSurrounding("\"").trim()
                    if (text.contains(" ")) {
                        val offset = originalElement?.textOffset?.minus(element.textRange.startOffset) ?: 0
                        // If there are spaces, try to find the class at cursor position
                        findClassAtCursor(text, offset.toInt() - 1) // -1 to account for opening quote
                    } else if (isTailwindPattern(text, element.project)) {
                        // Single word that matches Tailwind pattern
                        text
                    } else null
                }
                
                // Create an instance for JSP-like templates
                element is com.intellij.psi.xml.XmlToken && element.text.startsWith("class=") -> {
                    val attrValue = element.text.substring(6).trim().removeSurrounding("\"").removeSurrounding("'")
                    val offset = originalElement?.textOffset?.minus(element.textRange.startOffset) ?: 0
                    findClassAtCursor(attrValue, offset.toInt() - 6) // -6 for 'class=' prefix
                }
                
                // Embedded styles and other contexts
                element.parent?.text?.contains("class") == true || element.parent?.text?.contains("className") == true -> {
                    val text = element.text.trim().removePrefix("class=").removePrefix("className=").trim()
                    val cleanText = text.removeSurrounding("\"").removeSurrounding("'")
                    if (cleanText.contains(" ")) {
                        // Multi-class situation, find the one at cursor
                        val offset = originalElement?.textOffset?.minus(element.textRange.startOffset) ?: 0
                        findClassAtCursor(cleanText, offset.toInt())
                    } else if (cleanText.isNotEmpty()) {
                        cleanText
                    } else {
                        null
                    }
                }
                
                else -> null
            }
        } catch (e: Exception) {
            logger.error("Error extracting class name", e)
            return null
        }
    }
    
    /**
     * Creates rich documentation for a Tailwind class using class data
     */
    internal fun generateDocForClass(className: String, project: Project): String? {
        try {
            // Get enriched class data if possible
            val classDataMap = TailwindUtils.getTailwindClassData(project)
            val classDataJson = classDataMap[className]
            if (classDataJson != null) {
                val docJSON = JSONObject(classDataJson.toString()).optJSONObject("documentation") ?: return generateBasicDocumentation(className)
                
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
                        <div style="margin-bottom: 10px; display: flex; align-items: center;">
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
                        <body style="font-family: -apple-system, BlinkMacSystemFont, sans-serif;">
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
            } else {
                return generateBasicDocumentation(className)
            }
        } catch (e: Exception) {
            logger.error("Error creating rich documentation: ${e.message}")
            return generateBasicDocumentation(className)
        }
    }
    
    /**
     * Find the Tailwind class at the cursor position
     */
    internal fun findClassAtCursor(text: String, cursorOffset: Int): String? {
        logger.info("Finding class at cursor. Text length: ${text.length}, Cursor offset: $cursorOffset")
        
        if (text.isBlank() || cursorOffset < 0 || cursorOffset > text.length) {
            logger.info("Invalid text or cursor position")
            return null
        }
        
        try {
            // Handle quoted strings by removing quotes
            val cleanText = text.trim().removeSurrounding("\"").removeSurrounding("'")
            
            // Adjust cursor offset for cleaned text if needed
            var adjustedOffset = cursorOffset
            if (text != cleanText && text.startsWith("\"") || text.startsWith("'")) {
                adjustedOffset = adjustedOffset - 1
            }
            
            if (adjustedOffset < 0 || adjustedOffset > cleanText.length) {
                logger.info("Adjusted offset out of bounds: $adjustedOffset for text length ${cleanText.length}")
                return null
            }
            
            // Split by any whitespace (including multiple spaces, tabs, etc.)
            val classes = cleanText.split(Regex("\\s+"))
            logger.info("Found ${classes.size} classes in text: ${classes.joinToString(", ")}")
            
            // Fast path for single class
            if (classes.size == 1 && classes[0].isNotEmpty()) {
                logger.info("Single class detected: ${classes[0]}")
                return classes[0]
            }
            
            var currentPosition = 0
            
            for (className in classes) {
                if (className.isEmpty()) {
                    currentPosition++
                    continue
                }
                
                // Find exact position of this class in the original text
                val start = cleanText.indexOf(className, currentPosition)
                if (start == -1) {
                    logger.info("Could not find class '$className' at position $currentPosition")
                    continue
                }
                
                val end = start + className.length
                
                logger.info("Class: $className, Start: $start, End: $end, Cursor: $adjustedOffset")
                
                // Check if cursor is within or at the boundaries of this class
                if (adjustedOffset >= start && adjustedOffset <= end) {
                    logger.info("Found class at cursor: $className")
                    return className
                }
                
                currentPosition = end + 1 // Skip past this class and the following space
            }
            
            logger.info("No class found at cursor position $adjustedOffset")
            return null
            
        } catch (e: Exception) {
            logger.error("Error finding class at cursor", e)
            return null
        }
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
            " <span class='grayed'>[Tailwind v4]</span>"
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
    internal fun extractPrefixAndValue(className: String): Pair<String, String> {
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
    internal fun getCategoryIcon(prefix: String): String {
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
    internal fun getDescriptionForPrefix(prefix: String, value: String): String {
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
            "z" -> "Sets z-index to ${value}"
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
    internal fun generateErrorDocumentation(message: String, exception: Exception? = null): String {
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
    internal fun isTailwindV4Class(className: String): Boolean {
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
    internal fun getContrastingTextColor(backgroundColor: String): String {
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
    internal fun extractColorValue(className: String): String? {
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
    internal fun inferCssProperties(className: String): Map<String, String> {
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
            "z" -> properties["z-index"] = value
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
     * Find related Tailwind classes
     */
    internal fun findRelatedClasses(className: String): List<String> {
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
}
