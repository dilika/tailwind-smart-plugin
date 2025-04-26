package com.github.dilika.tailwindsmartplugin.utils

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/**
 * Utility class for working with Tailwind CSS.
 */
object TailwindUtils {
    private val LOG = Logger.getInstance(TailwindUtils::class.java)
    private val classCache = ConcurrentHashMap<String, List<String>>()
    private val classDataCache = ConcurrentHashMap<String, Map<String, JSONObject>>()

    /**
     * Get all available Tailwind CSS classes for a project.
     */
    fun getTailwindClasses(project: Project): List<String> {
        val projectId = project.locationHash
        return classCache.getOrPut(projectId) {
            try {
                // For now, return a basic set of Tailwind classes
                LOG.info("Loading Tailwind classes for project: ${project.name}")
                generateBasicTailwindClasses()
            } catch (e: Exception) {
                LOG.error("Error loading Tailwind classes: ${e.message}", e)
                emptyList()
            }
        }
    }
    
    /**
     * Get detailed information about Tailwind CSS classes for a project.
     */
    fun getTailwindClassData(project: Project): Map<String, JSONObject> {
        val projectId = project.locationHash
        return classDataCache.getOrPut(projectId) {
            try {
                LOG.info("Loading Tailwind class data for project: ${project.name}")
                generateBasicTailwindClassData()
            } catch (e: Exception) {
                LOG.error("Error loading Tailwind class data: ${e.message}", e)
                emptyMap()
            }
        }
    }
    
    /**
     * Clear the Tailwind cache for a project.
     */
    fun clearProjectCache(project: Project) {
        val projectId = project.locationHash
        LOG.info("Clearing Tailwind cache for project: ${project.name}")
        classCache.remove(projectId)
        classDataCache.remove(projectId)
    }

    /**
     * Generate a basic set of Tailwind CSS classes.
     */
    private fun generateBasicTailwindClasses(): List<String> {
        // This is a simplified list - in a real implementation, these would be generated
        // from parsing the Tailwind config and CSS files
        return listOf(
            // Background colors
            "bg-slate-50", "bg-slate-100", "bg-slate-200", "bg-slate-300", "bg-slate-400",
            "bg-slate-500", "bg-slate-600", "bg-slate-700", "bg-slate-800", "bg-slate-900",
            "bg-gray-50", "bg-gray-100", "bg-gray-200", "bg-gray-300", "bg-gray-400",
            "bg-gray-500", "bg-gray-600", "bg-gray-700", "bg-gray-800", "bg-gray-900",
            "bg-red-50", "bg-red-100", "bg-red-200", "bg-red-300", "bg-red-400",
            "bg-red-500", "bg-red-600", "bg-red-700", "bg-red-800", "bg-red-900",
            "bg-blue-50", "bg-blue-100", "bg-blue-200", "bg-blue-300", "bg-blue-400",
            "bg-blue-500", "bg-blue-600", "bg-blue-700", "bg-blue-800", "bg-blue-900",
            "bg-green-50", "bg-green-100", "bg-green-200", "bg-green-300", "bg-green-400",
            "bg-green-500", "bg-green-600", "bg-green-700", "bg-green-800", "bg-green-900",
            
            // Text colors
            "text-slate-50", "text-slate-100", "text-slate-200", "text-slate-300", "text-slate-400",
            "text-slate-500", "text-slate-600", "text-slate-700", "text-slate-800", "text-slate-900",
            "text-gray-50", "text-gray-100", "text-gray-200", "text-gray-300", "text-gray-400",
            "text-gray-500", "text-gray-600", "text-gray-700", "text-gray-800", "text-gray-900",
            "text-red-50", "text-red-100", "text-red-200", "text-red-300", "text-red-400",
            "text-red-500", "text-red-600", "text-red-700", "text-red-800", "text-red-900",
            "text-blue-50", "text-blue-100", "text-blue-200", "text-blue-300", "text-blue-400",
            "text-blue-500", "text-blue-600", "text-blue-700", "text-blue-800", "text-blue-900",
            "text-green-50", "text-green-100", "text-green-200", "text-green-300", "text-green-400",
            "text-green-500", "text-green-600", "text-green-700", "text-green-800", "text-green-900",
            
            // Layout
            "flex", "inline-flex", "grid", "inline-grid", "block", "inline-block", "inline",
            "hidden", "container", "relative", "absolute", "fixed", "sticky",
            
            // Spacing
            "p-0", "p-1", "p-2", "p-3", "p-4", "p-5", "p-6", "p-8", "p-10", "p-12", "p-16", "p-20",
            "px-0", "px-1", "px-2", "px-3", "px-4", "px-5", "px-6", "px-8", "px-10", "px-12", "px-16", "px-20",
            "py-0", "py-1", "py-2", "py-3", "py-4", "py-5", "py-6", "py-8", "py-10", "py-12", "py-16", "py-20",
            "m-0", "m-1", "m-2", "m-3", "m-4", "m-5", "m-6", "m-8", "m-10", "m-12", "m-16", "m-20",
            "mx-0", "mx-1", "mx-2", "mx-3", "mx-4", "mx-5", "mx-6", "mx-8", "mx-10", "mx-12", "mx-16", "mx-20",
            "my-0", "my-1", "my-2", "my-3", "my-4", "my-5", "my-6", "my-8", "my-10", "my-12", "my-16", "my-20",
            
            // Typography
            "font-sans", "font-serif", "font-mono", "font-light", "font-normal", "font-medium", "font-bold",
            "text-xs", "text-sm", "text-base", "text-lg", "text-xl", "text-2xl", "text-3xl", "text-4xl",
            "tracking-tight", "tracking-normal", "tracking-wide",
            "leading-none", "leading-tight", "leading-snug", "leading-normal", "leading-loose"
        )
    }

    /**
     * Generate basic data about Tailwind CSS classes.
     */
    private fun generateBasicTailwindClassData(): Map<String, JSONObject> {
        val classData = mutableMapOf<String, JSONObject>()
        
        val classes = generateBasicTailwindClasses()
        for (className in classes) {
            val data = JSONObject()
            val completion = JSONObject()
            val documentation = JSONObject()
            
            // Basic completion data
            completion.put("displayText", className)
            completion.put("lookupString", className)
            
            // Documentation
            documentation.put("title", className)
            documentation.put("description", "Tailwind CSS class $className")
            
            // Type inference based on prefix
            val type = when {
                className.startsWith("bg-") -> "background"
                className.startsWith("text-") -> "text"
                className.startsWith("p-") || className.startsWith("px-") || className.startsWith("py-") -> "padding"
                className.startsWith("m-") || className.startsWith("mx-") || className.startsWith("my-") -> "margin"
                className.startsWith("flex") -> "display"
                className.startsWith("font-") -> "font"
                else -> "utility"
            }
            
            documentation.put("type", type)
            
            // If it's a color, add color information
            if (className.contains("-")) {
                val parts = className.split("-")
                if (parts.size >= 3) {
                    val colorName = parts[1]
                    val shade = parts.getOrNull(2) ?: ""
                    
                    if (colorName in listOf("slate", "gray", "red", "blue", "green")) {
                        val hexColor = getColorHex(colorName, shade)
                        documentation.put("color", hexColor)
                        if (className.startsWith("bg-")) {
                            completion.put("style", "background-color: $hexColor;")
                        } else if (className.startsWith("text-")) {
                            completion.put("style", "color: $hexColor;")
                        }
                    }
                }
            }
            
            data.put("completion", completion)
            data.put("documentation", documentation)
            
            classData[className] = data
        }
        
        return classData
    }
    
    /**
     * Get a hex color for a given color name and shade.
     */
    private fun getColorHex(colorName: String, shade: String): String {
        // This is a simplified color mapping
        val colorMap = mapOf(
            "slate" to mapOf(
                "50" to "#f8fafc", "100" to "#f1f5f9", "200" to "#e2e8f0",
                "300" to "#cbd5e1", "400" to "#94a3b8", "500" to "#64748b",
                "600" to "#475569", "700" to "#334155", "800" to "#1e293b",
                "900" to "#0f172a"
            ),
            "gray" to mapOf(
                "50" to "#f9fafb", "100" to "#f3f4f6", "200" to "#e5e7eb",
                "300" to "#d1d5db", "400" to "#9ca3af", "500" to "#6b7280",
                "600" to "#4b5563", "700" to "#374151", "800" to "#1f2937",
                "900" to "#111827"
            ),
            "red" to mapOf(
                "50" to "#fef2f2", "100" to "#fee2e2", "200" to "#fecaca",
                "300" to "#fca5a5", "400" to "#f87171", "500" to "#ef4444",
                "600" to "#dc2626", "700" to "#b91c1c", "800" to "#991b1b",
                "900" to "#7f1d1d"
            ),
            "blue" to mapOf(
                "50" to "#eff6ff", "100" to "#dbeafe", "200" to "#bfdbfe",
                "300" to "#93c5fd", "400" to "#60a5fa", "500" to "#3b82f6",
                "600" to "#2563eb", "700" to "#1d4ed8", "800" to "#1e40af",
                "900" to "#1e3a8a"
            ),
            "green" to mapOf(
                "50" to "#f0fdf4", "100" to "#dcfce7", "200" to "#bbf7d0",
                "300" to "#86efac", "400" to "#4ade80", "500" to "#22c55e",
                "600" to "#16a34a", "700" to "#15803d", "800" to "#166534",
                "900" to "#14532d"
            )
        )
        
        return colorMap[colorName]?.get(shade) ?: "#64748b" // Default to slate-500
    }
}
