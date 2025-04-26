package com.github.dilika.tailwindsmartplugin.hover

import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttributeValue
import org.json.JSONArray

@Suppress("unused") // Registered via plugin.xml
class TailwindHoverProvider : DocumentationProvider {
    private val logger = Logger.getInstance(TailwindHoverProvider::class.java)

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element == null) return null

        logger.info("Génération de documentation pour: ${element.text}")
        
        // Try to get the class name from the element
        val className = when {
            element is XmlAttributeValue -> {
                // Check if it's a class attribute
                val attributeName = element.parent?.firstChild?.text
                if (attributeName != "class") return null
                
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
                if (attributeName != "class") return null
                
                // Try to extract the class directly from the element text
                element.text.trim().removeSurrounding("\"")
            }
            else -> {
                // Try to use the element text directly
                element.text.trim().removeSurrounding("\"")
            }
        }
        
        if (className.isNullOrBlank()) {
            logger.info("Impossible d'extraire le nom de classe")
            return null
        }

        logger.info("Classe extraite: $className")

        // Get the current project
        val project = element.project
        logger.info("Projet: ${project.name}")

        // Get Tailwind classes specific to the project
        val tailwindClasses = TailwindUtils.getTailwindClasses(project)
        logger.info("Nombre de classes Tailwind trouvées: ${tailwindClasses.size}")

        if (tailwindClasses.contains(className)) {
            logger.info("Classe Tailwind trouvée: $className")
            // Get enriched class data if possible
            val classDataMap = TailwindUtils.getTailwindClassData(project)
            val classData = classDataMap[className]
            val description = classData?.optString("description")
            val examples = classData?.optJSONArray("examples")
            val exampleHtml = if (examples != null && examples.length() > 0) {
                "<br/><b>Exemple:</b> <code>${examples.getString(0)}</code>"
            } else {
                ""
            }
            val descHtml = if (!description.isNullOrBlank()) {
                "<br/><i>${description}</i>"
            } else {
                "<br/><i>Classe Tailwind CSS</i>"
            }
            return """
                <html>
                    <b>$className</b>$descHtml$exampleHtml
                </html>
            """.trimIndent()
        }

        logger.info("Classe Tailwind non trouvée: $className")
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