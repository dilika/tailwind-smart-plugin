package com.github.dilika.tailwindsmartplugin.documentation

import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue

/**
 * Provides documentation for Tailwind CSS classes
 */
@Suppress("unused") // Registered via plugin.xml
class TailwindDocumentationProvider : AbstractDocumentationProvider(), DocumentationProvider {
    private val logger = Logger.getInstance(TailwindDocumentationProvider::class.java)

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element == null) return null

        logger.info("Generating documentation for: ${element.text}")
        
        // Try to get the class name from the element
        val className = when {
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
        
        if (className.isNullOrBlank()) {
            logger.info("Could not extract class name")
            return null
        }

        logger.info("Extracted class name: $className")

        // Get the current project
        val project = element.project
        logger.info("Project: ${project.name}")

        // Get Tailwind classes specific to the project
        val tailwindClasses = TailwindUtils.getTailwindClasses(project)
        logger.info("Number of Tailwind classes found: ${tailwindClasses.size}")

        if (tailwindClasses.contains(className)) {
            logger.info("Tailwind class found: $className")
            // Get enriched class data if possible
            val classDataMap = TailwindUtils.getTailwindClassData(project)
            val classData = classDataMap[className]
            val docJSON = classData?.optJSONObject("documentation") ?: return "<html><body>No documentation available</body></html>"
            
            val description = docJSON.optString("description", "Tailwind CSS class")
            val category = docJSON.optString("type", "utility")
            val icon = docJSON.optString("icon", "🔧")
            
            // Build color preview if available
            val colorHtml = if (docJSON.has("color")) {
                val color = docJSON.getString("color")
                val colorStyle = when {
                    className.startsWith("bg-") -> "background-color: $color;"
                    className.startsWith("text-") -> "color: $color;"
                    className.startsWith("border-") -> "border: 2px solid $color;"
                    else -> "background-color: $color;"
                }
                
                """
                <div style="display: inline-block; width: 20px; height: 20px; ${colorStyle} border-radius: 3px; margin-right: 5px; vertical-align: middle;"></div>
                <span><code>$color</code></span><br/>
                """
            } else ""
            
            // Get CSS property if available
            val cssPropertyHtml = if (docJSON.has("cssProperty")) {
                val cssProperty = docJSON.getString("cssProperty")
                "<br/><span><b>CSS Property:</b> <code>$cssProperty</code></span>"
            } else ""
            
            // Get state information if available
            val stateHtml = if (docJSON.has("state")) {
                val state = docJSON.getString("state")
                val baseClass = docJSON.optString("baseClass", "")
                val stateDescription = docJSON.optString("stateDescription", "")
                
                """
                <div><b>State:</b> <code>$state</code></div>
                <div><b>Applied to:</b> <code>$baseClass</code></div>
                <div><i>$stateDescription</i></div>
                """
            } else ""
            
            // Build examples section
            val examples = docJSON.optJSONArray("examples")
            val examplesHtml = if (examples != null && examples.length() > 0) {
                val exampleBuilder = StringBuilder()
                exampleBuilder.append("<br/><b>Examples:</b><br/>")
                
                for (i in 0 until examples.length()) {
                    exampleBuilder.append("<code>${examples.getString(i)}</code>")
                    if (i < examples.length() - 1) {
                        exampleBuilder.append("<br/>")
                    }
                }
                
                exampleBuilder.toString()
            } else ""
            
            return """
                <html>
                    <body>
                        <div style="display: flex; align-items: center; margin-bottom: 10px;">
                            <span style="font-size: 16px; margin-right: 8px;">$icon</span>
                            <span style="font-size: 16px; font-weight: bold;">$className</span>
                        </div>
                        <div style="margin-bottom: 10px; color: #666;">
                            <span><i>$description</i></span>
                            <br/>
                            <span><b>Category:</b> $category</span>
                        </div>
                        $colorHtml
                        $cssPropertyHtml
                        $stateHtml
                        $examplesHtml
                    </body>
                </html>
            """.trimIndent()
        }

        logger.info("Tailwind class not found: $className")
        return null
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
}