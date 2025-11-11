package com.github.dilika.tailwindsmartplugin.context

import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.Logger
import java.io.File

/**
 * Détecte le design system utilisé dans le projet
 */
class DesignSystemDetector(private val project: Project) {
    
    private val logger = Logger.getInstance(DesignSystemDetector::class.java)
    
    /**
     * Détecte le design system du projet
     */
    fun detectDesignSystem(): DesignSystemInfo {
        val basePath = project.basePath ?: return DesignSystemInfo.unknown()
        
        // Détecter Tailwind UI
        if (detectTailwindUI(basePath)) {
            return DesignSystemInfo(
                name = "Tailwind UI",
                type = DesignSystemType.TAILWIND_UI,
                confidence = 0.9f,
                patterns = getTailwindUIPatterns()
            )
        }
        
        // Détecter Headless UI
        if (detectHeadlessUI(basePath)) {
            return DesignSystemInfo(
                name = "Headless UI",
                type = DesignSystemType.HEADLESS_UI,
                confidence = 0.8f,
                patterns = getHeadlessUIPatterns()
            )
        }
        
        // Détecter Shadcn/ui
        if (detectShadcnUI(basePath)) {
            return DesignSystemInfo(
                name = "shadcn/ui",
                type = DesignSystemType.SHADCN_UI,
                confidence = 0.9f,
                patterns = getShadcnUIPatterns()
            )
        }
        
        // Détecter Radix UI
        if (detectRadixUI(basePath)) {
            return DesignSystemInfo(
                name = "Radix UI",
                type = DesignSystemType.RADIX_UI,
                confidence = 0.8f,
                patterns = getRadixUIPatterns()
            )
        }
        
        // Détecter Mantine
        if (detectMantine(basePath)) {
            return DesignSystemInfo(
                name = "Mantine",
                type = DesignSystemType.MANTINE,
                confidence = 0.8f,
                patterns = getMantinePatterns()
            )
        }
        
        return DesignSystemInfo.unknown()
    }
    
    private fun detectTailwindUI(basePath: String): Boolean {
        val packageJson = File(basePath, "package.json")
        if (packageJson.exists()) {
            val content = packageJson.readText()
            if (content.contains("@tailwindcss/ui") || content.contains("tailwindui")) {
                return true
            }
        }
        return false
    }
    
    private fun detectHeadlessUI(basePath: String): Boolean {
        val packageJson = File(basePath, "package.json")
        if (packageJson.exists()) {
            val content = packageJson.readText()
            if (content.contains("@headlessui/react") || content.contains("@headlessui/vue")) {
                return true
            }
        }
        return false
    }
    
    private fun detectShadcnUI(basePath: String): Boolean {
        val componentsDir = File(basePath, "components/ui")
        if (componentsDir.exists() && componentsDir.isDirectory) {
            return true
        }
        val packageJson = File(basePath, "package.json")
        if (packageJson.exists()) {
            val content = packageJson.readText()
            if (content.contains("shadcn") || content.contains("@radix-ui")) {
                return true
            }
        }
        return false
    }
    
    private fun detectRadixUI(basePath: String): Boolean {
        val packageJson = File(basePath, "package.json")
        if (packageJson.exists()) {
            val content = packageJson.readText()
            if (content.contains("@radix-ui/")) {
                return true
            }
        }
        return false
    }
    
    private fun detectMantine(basePath: String): Boolean {
        val packageJson = File(basePath, "package.json")
        if (packageJson.exists()) {
            val content = packageJson.readText()
            if (content.contains("@mantine/")) {
                return true
            }
        }
        return false
    }
    
    private fun getTailwindUIPatterns(): List<String> {
        return listOf(
            "bg-white shadow-lg rounded-lg",
            "flex items-center justify-between",
            "px-6 py-4 border-b border-gray-200"
        )
    }
    
    private fun getHeadlessUIPatterns(): List<String> {
        return listOf(
            "relative inline-flex items-center",
            "absolute inset-0",
            "transition-opacity duration-200"
        )
    }
    
    private fun getShadcnUIPatterns(): List<String> {
        return listOf(
            "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2",
            "flex items-center justify-center rounded-md",
            "h-4 w-4 text-muted-foreground"
        )
    }
    
    private fun getRadixUIPatterns(): List<String> {
        return listOf(
            "inline-flex items-center justify-center",
            "absolute top-0 left-0",
            "data-[state=open]:animate-in"
        )
    }
    
    private fun getMantinePatterns(): List<String> {
        return listOf(
            "flex items-center gap-2",
            "rounded-md px-4 py-2",
            "border border-gray-300"
        )
    }
}

/**
 * Informations sur le design system
 */
data class DesignSystemInfo(
    val name: String,
    val type: DesignSystemType,
    val confidence: Float,
    val patterns: List<String>
) {
    companion object {
        fun unknown(): DesignSystemInfo {
            return DesignSystemInfo(
                name = "Unknown",
                type = DesignSystemType.UNKNOWN,
                confidence = 0.0f,
                patterns = emptyList()
            )
        }
    }
}

enum class DesignSystemType {
    UNKNOWN,
    TAILWIND_UI,
    HEADLESS_UI,
    SHADCN_UI,
    RADIX_UI,
    MANTINE
}

