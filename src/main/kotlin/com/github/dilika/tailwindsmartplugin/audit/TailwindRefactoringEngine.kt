package com.github.dilika.tailwindsmartplugin.audit

import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.Logger

class TailwindRefactoringEngine {
    
    private val logger = Logger.getInstance(TailwindRefactoringEngine::class.java)
    
    fun optimizeClassOrder(classes: List<String>): List<String> {
        try {
            val categorizedClasses = categorizeClasses(classes)
            val optimizedOrder = mutableListOf<String>()
            
            val categoryOrder = listOf(
                "layout", "flexbox", "grid", "spacing", "sizing",
                "typography", "colors", "backgrounds", "borders",
                "effects", "transforms", "transitions", "interactivity"
            )
            
            categoryOrder.forEach { category ->
                categorizedClasses[category]?.let { categoryClasses ->
                    optimizedOrder.addAll(categoryClasses.sorted())
                }
            }
            
            return optimizedOrder
        } catch (e: Exception) {
            logger.warn("Error optimizing class order", e)
            return classes
        }
    }
    
    fun detectRedundantClasses(classes: List<String>): List<String> {
        val redundantClasses = mutableListOf<String>()
        val categorizedClasses = categorizeClasses(classes)
        
        categorizedClasses.forEach { (category, categoryClasses) ->
            when (category) {
                "spacing" -> {
                    val paddingClasses = categoryClasses.filter { it.startsWith("p") }
                    if (paddingClasses.contains("px-4") && paddingClasses.contains("py-4")) {
                        redundantClasses.addAll(listOf("px-4", "py-4"))
                    }
                }
                "colors" -> {
                    val textColors = categoryClasses.filter { it.startsWith("text-") }
                    val bgColors = categoryClasses.filter { it.startsWith("bg-") }
                    if (textColors.contains("text-white") && bgColors.contains("bg-white")) {
                        redundantClasses.add("text-white")
                    }
                }
            }
        }
        
        return redundantClasses
    }
    
    fun suggestClassConsolidation(classes: List<String>): List<RefactoringSuggestion> {
        val suggestions = mutableListOf<RefactoringSuggestion>()
        val categorizedClasses = categorizeClasses(classes)
        
        categorizedClasses.forEach { (category, categoryClasses) ->
            when (category) {
                "spacing" -> {
                    val paddingClasses = categoryClasses.filter { it.startsWith("p") }
                    if (paddingClasses.contains("px-4") && paddingClasses.contains("py-2")) {
                        suggestions.add(RefactoringSuggestion(
                            type = "Consolidate Padding",
                            description = "Combine px-4 py-2 into p-4 py-2",
                            originalClasses = listOf("px-4", "py-2"),
                            suggestedClasses = listOf("p-4", "py-2"),
                            impact = RefactoringSuggestion.Impact.MEDIUM
                        ))
                    }
                }
            }
        }
        
        return suggestions
    }
    
    fun convertToComponentClasses(classes: List<String>): String {
        val categorizedClasses = categorizeClasses(classes)
        val componentName = generateComponentName(categorizedClasses)
        
        val componentClasses = mutableListOf<String>()
        
        categorizedClasses.forEach { (category, categoryClasses) ->
            when (category) {
                "spacing" -> {
                    val paddingClasses = categoryClasses.filter { it.startsWith("p") }
                    if (paddingClasses.isNotEmpty()) {
                        componentClasses.add("${componentName}-padding")
                    }
                }
                "colors" -> {
                    val textColors = categoryClasses.filter { it.startsWith("text-") }
                    if (textColors.isNotEmpty()) {
                        componentClasses.add("${componentName}-text")
                    }
                }
            }
        }
        
        return componentClasses.joinToString(" ")
    }
    
    private fun categorizeClasses(classes: List<String>): Map<String, List<String>> {
        val categorized = mutableMapOf<String, MutableList<String>>()
        
        classes.forEach { className ->
            val category = when {
                className.startsWith("flex") || className.startsWith("grid") -> "layout"
                className.startsWith("items-") || className.startsWith("justify-") -> "flexbox"
                className.startsWith("p") || className.startsWith("m") -> "spacing"
                className.startsWith("w-") || className.startsWith("h-") -> "sizing"
                className.startsWith("text-") -> "colors"
                className.startsWith("bg-") -> "backgrounds"
                className.startsWith("border") -> "borders"
                className.startsWith("shadow") -> "effects"
                className.startsWith("hover:") || className.startsWith("focus:") -> "interactivity"
                className.startsWith("font-") -> "typography"
                else -> "other"
            }
            
            categorized.computeIfAbsent(category) { mutableListOf() }.add(className)
        }
        
        return categorized
    }
    
    private fun generateComponentName(categorizedClasses: Map<String, List<String>>): String {
        return when {
            categorizedClasses.containsKey("layout") && categorizedClasses["layout"]?.contains("flex") == true -> "flex-container"
            categorizedClasses.containsKey("colors") && categorizedClasses["colors"]?.any { it.startsWith("bg-") } == true -> "colored-element"
            categorizedClasses.containsKey("spacing") && categorizedClasses["spacing"]?.any { it.startsWith("p") } == true -> "padded-element"
            categorizedClasses.containsKey("typography") -> "text-element"
            else -> "custom-component"
        }
    }
}

data class RefactoringSuggestion(
    val type: String,
    val description: String,
    val originalClasses: List<String>,
    val suggestedClasses: List<String>,
    val impact: Impact
) {
    enum class Impact {
        HIGH,
        MEDIUM,
        LOW
    }
}