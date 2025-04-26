package com.github.dilika.tailwindsmartplugin.documentation

import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue
import org.json.JSONObject

/**
 * Provides documentation for Tailwind CSS classes
 */
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
            val description = classData?.optJSONObject("documentation")?.optString("description") 
                ?: "Tailwind CSS class"
                
            val examples = classData?.optJSONObject("documentation")?.optJSONArray("examples")
            val exampleHtml = if (examples != null && examples.length() > 0) {
                "<br/><b>Example:</b> <code>${examples.getString(0)}</code>"
            } else {
                ""
            }
            
            return """
                <html>
                    <body>
                        <div><b>$className</b></div>
                        <div><i>$description</i></div>
                        $exampleHtml
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
            if (cursorOffset >= start && cursorOffset <= end) {
                return className
            }
            
            currentPosition = end
        }
        
        return null
    }
}