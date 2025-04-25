package com.github.dilika.tailwindsmartplugin.documentation

import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils
import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlAttributeValue
import org.json.JSONObject

/**
 * Documentation provider for Tailwind CSS classes
 * Shows rich information about Tailwind classes when users hover over them
 */
class TailwindDocumentationProvider : AbstractDocumentationProvider() {
    private val logger = Logger.getInstance(TailwindDocumentationProvider::class.java)
    
    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int
    ): PsiElement? {
        if (contextElement == null) {
            logger.info("Élément de contexte NULL")
            return null
        }
        
        logger.info("Tentative de documentation pour: ${contextElement.text} (${contextElement.javaClass.simpleName})")
        
        // Récupérer l'élément parent (peut être un XmlAttributeValue)
        val attributeValue = when {
            contextElement is XmlAttributeValue -> contextElement
            contextElement.parent is XmlAttributeValue -> contextElement.parent as XmlAttributeValue
            else -> null
        }
        
        if (attributeValue == null) {
            logger.info("Pas un attribut XML valide")
            return null
        }
        
        // Vérifier si c'est un attribut class
        val attributeName = attributeValue.parent?.firstChild?.text
        logger.info("Nom de l'attribut: $attributeName")
        
        if (attributeName != "class") {
            logger.info("Pas un attribut class")
            return null
        }
        
        // Essayer d'extraire la classe Tailwind à la position du curseur
        val attributeText = attributeValue.value
        logger.info("Valeur de l'attribut: $attributeText")
        
        val cursorOffset = targetOffset - attributeValue.textRange.startOffset
        logger.info("Position du curseur dans l'attribut: $cursorOffset")
        
        // Récupérer les classes Tailwind spécifiques au projet
        val project = file.project
        val tailwindClasses = TailwindUtils.getTailwindClasses(project)
        logger.info("Classes Tailwind disponibles: ${tailwindClasses.size}")
        
        // Trouver la classe au curseur
        val className = findClassAtCursor(attributeText, cursorOffset)
        logger.info("Classe trouvée à la position du curseur: $className")
        
        if (className.isNullOrBlank()) {
            return null
        }
        
        // Vérifier si c'est une classe Tailwind connue
        val isKnownClass = tailwindClasses.contains(className)
        logger.info("Est une classe Tailwind connue: $isKnownClass")
        
        // Retourner l'élément contexte si c'est une classe Tailwind
        return if (isKnownClass) contextElement else null
    }
    
    override fun generateHoverDoc(element: PsiElement, originalElement: PsiElement?): String? {
        logger.info("Génération de hover pour: ${element.text}")
        
        // Handle both XmlAttributeValue and direct text elements
        val attributeValue = when {
            element is XmlAttributeValue -> element
            element.parent is XmlAttributeValue -> element.parent as XmlAttributeValue
            else -> {
                logger.info("Élément pas un XmlAttributeValue et parent pas un XmlAttributeValue: ${element.javaClass.simpleName}")
                return null
            }
        }
        
        // Verify it's a class attribute
        val attributeName = attributeValue.parent?.firstChild?.text
        if (attributeName != "class") {
            logger.info("Pas un attribut class: $attributeName")
            return null
        }
        
        // Extract the class name
        val className = extractClassName(attributeValue, originalElement)
        if (className.isNullOrBlank()) {
            logger.info("Nom de classe vide")
            return null
        }
        
        logger.info("Génération de documentation pour la classe: $className")
        
        // Get documentation for the class
        val project = element.project
        return generateClassDocumentation(className, project)
    }
    
    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        logger.info("Génération de doc pour: ${element.text}")
        
        if (element !is XmlAttributeValue) {
            logger.info("Élément pas un XmlAttributeValue: ${element.javaClass.simpleName}")
            return null
        }
        
        // Extraire le nom de classe
        val className = extractClassName(element, originalElement)
        if (className.isNullOrBlank()) {
            logger.info("Nom de classe vide")
            return null
        }
        
        logger.info("Génération de documentation pour la classe: $className")
        
        // Récupérer la documentation pour la classe
        val project = element.project
        return generateClassDocumentation(className, project)
    }
    
    /**
     * Extraire le nom de classe Tailwind d'un attribut XML
     */
    private fun extractClassName(element: XmlAttributeValue, originalElement: PsiElement?): String? {
        // Vérifier si c'est un attribut class
        val attributeName = element.parent?.firstChild?.text
        if (attributeName != "class") {
            logger.info("Pas un attribut class: $attributeName")
            return null
        }
        
        val attributeValue = element.value
        if (attributeValue.isBlank()) {
            logger.info("Valeur d'attribut vide")
            return null
        }
        
        // Si l'élément original est disponible, utiliser sa position pour déterminer la classe
        val cursorOffset = originalElement?.let { 
            it.textRange.startOffset - element.textRange.startOffset 
        } ?: 0
        
        logger.info("Offset du curseur: $cursorOffset, valeur de l'attribut: $attributeValue")
        
        // Trouver la classe à la position du curseur
        return findClassAtCursor(attributeValue, cursorOffset)
    }
    
    /**
     * Trouver la classe Tailwind à la position du curseur
     */
    private fun findClassAtCursor(text: String, cursorOffset: Int): String? {
        if (cursorOffset < 0 || cursorOffset > text.length) {
            logger.info("Curseur hors limites: $cursorOffset (texte: ${text.length})")
            return null
        }
        
        // Diviser en classes
        val classes = text.split(Regex("\\s+"))
        logger.info("Classes trouvées: ${classes.joinToString(", ")}")
        
        var currentPosition = 0
        
        for (className in classes) {
            if (className.isEmpty()) {
                currentPosition++
                continue
            }
            
            // Find the exact position of this class in the original text
            val start = text.indexOf(className, currentPosition)
            if (start == -1) {
                continue
            }
            
            val end = start + className.length
            
            logger.info("Classe: $className, position: $start-$end, curseur: $cursorOffset")
            
            // Check if cursor is within this class
            if (cursorOffset >= start && cursorOffset <= end) {
                return className
            }
            
            currentPosition = end
        }
        
        // If cursor is at whitespace, try to find the nearest class
        if (text[cursorOffset].isWhitespace() && cursorOffset > 0) {
            // Check if there's a class immediately before the cursor
            for (className in classes) {
                if (className.isEmpty()) continue
                
                val start = text.indexOf(className, 0)
                val end = start + className.length
                
                if (start != -1 && cursorOffset == end + 1) {
                    return className
                }
            }
        }
        
        logger.info("Aucune classe trouvée à la position du curseur")
        return null
    }
    
    /**
     * Générer la documentation riche pour une classe Tailwind
     */
    private fun generateClassDocumentation(className: String, project: com.intellij.openapi.project.Project): String {
        // Récupérer les données spécifiques au projet
        val tailwindData = TailwindUtils.getTailwindClassData(project)
        val classData = tailwindData[className]
        
        if (classData == null) {
            logger.info("Pas de données pour la classe: $className, utilisation de la documentation par défaut")
            return getDefaultDocumentation(className)
        }
        
        logger.info("Données trouvées pour la classe: $className")
        
        try {
            val completion = classData.optJSONObject("completion") ?: JSONObject()
            val documentation = classData.optJSONObject("documentation") ?: JSONObject()
            
            val type = completion.optString("type", "utility")
            val description = documentation.optString("description", "Classe Tailwind CSS")
            val cssValue = documentation.optString("cssValue", "")
            
            // Propriétés CSS
            val cssProperties = documentation.optJSONArray("cssProperties")
            val propertiesList = if (cssProperties != null) {
                (0 until cssProperties.length()).map { cssProperties.getString(it) }
            } else {
                emptyList()
            }
            
            // Exemples
            val examples = documentation.optJSONArray("examples")
            val examplesList = if (examples != null) {
                (0 until examples.length()).map { examples.getString(it) }
            } else {
                emptyList()
            }
            
            // Information sur les variantes si applicable
            val variant = completion.optString("variant", "")
            
            // Construire la documentation
            val builder = StringBuilder()
            
            builder.append(DocumentationMarkup.DEFINITION_START)
            builder.append(className)
            builder.append(DocumentationMarkup.DEFINITION_END)
            
            builder.append(DocumentationMarkup.CONTENT_START)
            builder.append("<p><b>Type:</b> ").append(type).append("</p>")
            builder.append("<p>").append(description).append("</p>")
            
            if (variant.isNotEmpty()) {
                builder.append("<p><b>Variante:</b> ").append(variant).append("</p>")
            }
            builder.append(DocumentationMarkup.CONTENT_END)
            
            if (cssProperties != null && cssProperties.length() > 0) {
                builder.append(DocumentationMarkup.SECTIONS_START)
                builder.append("<p><b>Propriétés CSS</b></p>")
                
                for (property in propertiesList) {
                    builder.append("<p>").append(property)
                    if (cssValue.isNotEmpty()) {
                        builder.append(": <code>").append(cssValue).append("</code>")
                    }
                    builder.append("</p>")
                }
                
                if (examplesList.isNotEmpty()) {
                    builder.append("<p><b>Exemples d'utilisation</b></p>")
                    for (example in examplesList) {
                        builder.append("<p><code>").append(example).append("</code></p>")
                    }
                }
                
                builder.append(DocumentationMarkup.SECTIONS_END)
            }
            
            return builder.toString()
        } catch (e: Exception) {
            logger.error("Erreur lors de la génération de la documentation: ${e.message}", e)
            return getDefaultDocumentation(className)
        }
    }
    
    /**
     * Documentation par défaut pour une classe Tailwind
     */
    private fun getDefaultDocumentation(className: String): String {
        // Analyser le nom de classe pour en déduire le type
        val type = when {
            className.startsWith("bg-") -> "Arrière-plan"
            className.startsWith("text-") -> "Typographie"
            className.startsWith("m-") -> "Marge"
            className.startsWith("p-") -> "Remplissage"
            className.startsWith("flex") -> "Flexbox"
            className.startsWith("grid") -> "Grille"
            className.startsWith("border") -> "Bordure"
            className.startsWith("rounded") -> "Arrondi"
            className.startsWith("w-") -> "Largeur"
            className.startsWith("h-") -> "Hauteur"
            else -> "Utilitaire"
        }
        
        return DocumentationMarkup.DEFINITION_START +
               className +
               DocumentationMarkup.DEFINITION_END +
               DocumentationMarkup.CONTENT_START +
               "<p><b>Type:</b> " + type + "</p>" +
               "<p>Classe Tailwind CSS</p>" +
               DocumentationMarkup.CONTENT_END
    }
} 