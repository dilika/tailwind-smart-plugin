package com.github.dilika.tailwindsmartplugin.context

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.openapi.diagnostic.Logger

/**
 * Analyseur de contexte intelligent pour les suggestions Tailwind CSS
 */
class TailwindContextAnalyzer {
    
    private val logger = Logger.getInstance(TailwindContextAnalyzer::class.java)
    
    /**
     * Analyse le contexte d'un élément PSI pour déterminer le type de composant
     */
    fun analyzeElementContext(element: PsiElement): ComponentContext {
        val tag = PsiTreeUtil.getParentOfType(element, XmlTag::class.java)
        if (tag == null) {
            return ComponentContext.unknown()
        }
        
        val elementType = detectElementType(tag)
        val attributes = extractAttributes(tag)
        val suggestedPatterns = getSuggestedPatterns(elementType, attributes)
        val frameworkContext = detectFrameworkContext(element.project)
        
        return ComponentContext(
            elementType = elementType,
            attributes = attributes,
            suggestedPatterns = suggestedPatterns,
            frameworkContext = frameworkContext,
            confidence = calculateConfidence(elementType, attributes)
        )
    }
    
    private fun detectElementType(tag: XmlTag): String {
        val tagName = tag.name.lowercase()
        val className = tag.getAttributeValue("class") ?: ""
        
        when (tagName) {
            "button" -> return "button"
            "a" -> return "link"
            "input" -> return "input"
            "textarea" -> return "textarea"
            "select" -> return "select"
            "img" -> return "image"
            "nav" -> return "navigation"
            "header" -> return "header"
            "footer" -> return "footer"
        }
        
        when {
            className.contains("btn") || className.contains("button") -> return "button"
            className.contains("card") -> return "card"
            className.contains("modal") -> return "modal"
            className.contains("nav") -> return "navigation"
            className.contains("alert") -> return "alert"
            className.contains("badge") -> return "badge"
            className.contains("avatar") -> return "avatar"
        }
        
        return "generic"
    }
    
    private fun extractAttributes(tag: XmlTag): Map<String, String> {
        val attributes = mutableMapOf<String, String>()
        
        tag.attributes.forEach { attr ->
            when (attr.name.lowercase()) {
                "class", "className" -> attributes["class"] = attr.value ?: ""
                "id" -> attributes["id"] = attr.value ?: ""
                "data-component" -> attributes["data-component"] = attr.value ?: ""
                "type" -> attributes["type"] = attr.value ?: ""
            }
        }
        
        return attributes
    }
    
    private fun getSuggestedPatterns(elementType: String, attributes: Map<String, String>): List<String> {
        return when (elementType) {
            "button" -> listOf(
                "px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600",
                "px-6 py-3 bg-green-500 text-white rounded-lg hover:bg-green-600",
                "px-3 py-1 bg-gray-200 text-gray-800 rounded hover:bg-gray-300"
            )
            "card" -> listOf(
                "bg-white shadow-md rounded-lg p-6",
                "bg-white border border-gray-200 rounded-lg p-4",
                "bg-gray-50 border border-gray-300 rounded-lg p-6"
            )
            "modal" -> listOf(
                "fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center",
                "bg-white rounded-lg shadow-xl max-w-md w-full mx-4"
            )
            "navigation" -> listOf(
                "bg-white shadow-sm border-b border-gray-200",
                "bg-gray-900 text-white shadow-lg"
            )
            "input" -> listOf(
                "w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            )
            "alert" -> listOf(
                "bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded",
                "bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded"
            )
            else -> listOf("p-4", "px-4 py-2", "m-4")
        }
    }
    
    private fun detectFrameworkContext(project: Project): FrameworkContext {
        // Détecter le framework basé sur les fichiers du projet
        val basePath = project.basePath ?: return FrameworkContext.VANILLA
        
        val packageJson = java.io.File(basePath, "package.json")
        if (packageJson.exists()) {
            try {
                val content = packageJson.readText()
                when {
                    content.contains("\"react\"") && content.contains("\"next\"") -> return FrameworkContext.NEXT_JS
                    content.contains("\"react\"") -> return FrameworkContext.REACT
                    content.contains("\"vue\"") && content.contains("\"nuxt\"") -> return FrameworkContext.NUXT
                    content.contains("\"vue\"") -> return FrameworkContext.VUE
                    content.contains("\"@astrojs") -> return FrameworkContext.ASTRO
                    content.contains("\"svelte\"") -> return FrameworkContext.SVELTE_KIT
                }
            } catch (e: Exception) {
                logger.warn("Error reading package.json: ${e.message}")
            }
        }
        
        // Détecter par extension de fichiers
        val hasReactFiles = java.io.File(basePath).walkTopDown()
            .any { it.extension == "jsx" || it.extension == "tsx" }
        if (hasReactFiles) return FrameworkContext.REACT
        
        val hasVueFiles = java.io.File(basePath).walkTopDown()
            .any { it.extension == "vue" }
        if (hasVueFiles) return FrameworkContext.VUE
        
        return FrameworkContext.VANILLA
    }
    
    private fun calculateConfidence(elementType: String, attributes: Map<String, String>): Float {
        var confidence = 0.5f
        if (attributes.containsKey("data-component")) confidence += 0.3f
        if (attributes["class"]?.isNotEmpty() == true) confidence += 0.1f
        return confidence.coerceIn(0.0f, 1.0f)
    }
}

data class ComponentContext(
    val elementType: String,
    val attributes: Map<String, String>,
    val suggestedPatterns: List<String>,
    val frameworkContext: FrameworkContext,
    val confidence: Float
) {
    companion object {
        fun unknown(): ComponentContext {
            return ComponentContext(
                elementType = "unknown",
                attributes = emptyMap(),
                suggestedPatterns = emptyList(),
                frameworkContext = FrameworkContext.VANILLA,
                confidence = 0.0f
            )
        }
    }
}

enum class FrameworkContext {
    VANILLA,
    REACT,
    VUE,
    NEXT_JS,
    NUXT,
    ASTRO,
    SVELTE_KIT
}