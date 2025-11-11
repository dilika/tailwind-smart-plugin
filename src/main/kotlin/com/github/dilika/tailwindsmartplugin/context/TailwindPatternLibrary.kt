package com.github.dilika.tailwindsmartplugin.context

import com.intellij.openapi.diagnostic.Logger

class TailwindPatternLibrary {
    
    private val logger = Logger.getInstance(TailwindPatternLibrary::class.java)
    
    private val patterns = mapOf(
        "button-primary" to TailwindPattern(
            name = "Primary Button",
            description = "A primary action button",
            classes = listOf("px-4", "py-2", "bg-blue-500", "text-white", "rounded", "hover:bg-blue-600"),
            category = "Buttons"
        ),
        "card-basic" to TailwindPattern(
            name = "Basic Card",
            description = "A simple card component",
            classes = listOf("bg-white", "shadow-md", "rounded-lg", "p-6"),
            category = "Cards"
        )
    )
    
    fun getAllPatterns(): List<TailwindPattern> {
        return patterns.values.toList()
    }
    
    fun getPattern(name: String): TailwindPattern? {
        return patterns[name]
    }
}

data class TailwindPattern(
    val name: String,
    val description: String,
    val classes: List<String>,
    val category: String
)