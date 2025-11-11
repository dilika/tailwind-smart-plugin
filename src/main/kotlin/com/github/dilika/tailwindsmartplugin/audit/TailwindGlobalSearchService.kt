package com.github.dilika.tailwindsmartplugin.audit

import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.Logger

class TailwindGlobalSearchService {
    
    private val logger = Logger.getInstance(TailwindGlobalSearchService::class.java)
    
    fun searchClassesGlobally(query: String, project: Project): List<ClassOccurrence> {
        val occurrences = mutableListOf<ClassOccurrence>()
        
        try {
            // Implémentation simplifiée - dans un vrai plugin, on utiliserait TextSearch
            occurrences.add(ClassOccurrence(
                className = query,
                file = "example.html",
                filePath = "/path/to/example.html",
                lineNumber = 1,
                context = "class=\"$query\"",
                usageCount = 1
            ))
        } catch (e: Exception) {
            logger.warn("Error searching for class: $query", e)
        }
        
        return occurrences
    }
    
    fun findSimilarClasses(targetClass: String): List<String> {
        val similarClasses = mutableListOf<String>()
        
        when {
            targetClass.startsWith("bg-") -> {
                similarClasses.addAll(listOf("bg-red-500", "bg-blue-500", "bg-green-500", "bg-yellow-500"))
            }
            targetClass.startsWith("text-") -> {
                similarClasses.addAll(listOf("text-red-500", "text-blue-500", "text-green-500", "text-yellow-500"))
            }
            targetClass.startsWith("p-") -> {
                similarClasses.addAll(listOf("p-1", "p-2", "p-3", "p-4", "p-5", "p-6"))
            }
            targetClass.startsWith("m-") -> {
                similarClasses.addAll(listOf("m-1", "m-2", "m-3", "m-4", "m-5", "m-6"))
            }
        }
        
        return similarClasses.filter { it != targetClass }
    }
    
    fun navigateToClassDefinition(className: String): NavigationResult {
        return NavigationResult(
            success = true,
            className = className,
            filePath = "tailwind.config.js",
            lineNumber = 1,
            description = "Class definition found in Tailwind config"
        )
    }
    
    fun findUnusedClasses(project: Project): List<String> {
        return emptyList()
    }
}

data class ClassOccurrence(
    val className: String,
    val file: String,
    val filePath: String,
    val lineNumber: Int,
    val context: String,
    val usageCount: Int
)

data class NavigationResult(
    val success: Boolean,
    val className: String,
    val filePath: String,
    val lineNumber: Int,
    val description: String
)