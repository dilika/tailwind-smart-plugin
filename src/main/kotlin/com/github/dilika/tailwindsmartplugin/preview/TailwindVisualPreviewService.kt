package com.github.dilika.tailwindsmartplugin.preview

import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.JBColor
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.swing.Icon
import javax.swing.ImageIcon

/**
 * Service de preview visuel en temps réel pour les classes Tailwind CSS
 */
class TailwindVisualPreviewService {
    
    private val logger = Logger.getInstance(TailwindVisualPreviewService::class.java)
    
    /**
     * Génère un aperçu visuel pour une liste de classes Tailwind
     */
    fun generatePreview(classes: List<String>, width: Int = 200, height: Int = 100): PreviewData {
        try {
            val image = createPreviewImage(classes, width, height)
            val icon = ImageIcon(image)
            
            return PreviewData(
                image = image,
                icon = icon,
                classes = classes,
                cssProperties = extractCSSProperties(classes),
                description = generateDescription(classes)
            )
        } catch (e: Exception) {
            logger.warn("Error generating preview for classes: $classes", e)
            return PreviewData.empty()
        }
    }
    
    /**
     * Crée une image de preview basée sur les classes Tailwind
     */
    private fun createPreviewImage(classes: List<String>, width: Int, height: Int): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.graphics as Graphics2D
        
        // Configuration de l'antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        
        // Appliquer les styles basés sur les classes
        applyTailwindStyles(g2d, classes, width, height)
        
        g2d.dispose()
        return image
    }
    
    /**
     * Applique les styles Tailwind à l'image
     */
    private fun applyTailwindStyles(g2d: Graphics2D, classes: List<String>, width: Int, height: Int) {
        var backgroundColor = Color.WHITE
        var textColor = Color.BLACK
        var borderColor: Color? = null
        var borderRadius = 0
        var padding = 8
        var margin = 0
        var fontSize = 14
        var fontWeight = "normal"
        var textAlign = "left"
        var display = "block"
        
        // Analyser les classes pour extraire les styles
        classes.forEach { className ->
            when {
                className.startsWith("bg-") -> {
                    backgroundColor = parseColor(className.substring(3))
                }
                className.startsWith("text-") -> {
                    textColor = parseColor(className.substring(5))
                }
                className.startsWith("border-") -> {
                    borderColor = parseColor(className.substring(7))
                }
                className.startsWith("rounded") -> {
                    borderRadius = parseBorderRadius(className)
                }
                className.startsWith("p-") -> {
                    padding = parseSpacing(className.substring(2))
                }
                className.startsWith("m-") -> {
                    margin = parseSpacing(className.substring(2))
                }
                className.startsWith("text-") && className.contains("xl") -> {
                    fontSize = 20
                }
                className.startsWith("text-") && className.contains("lg") -> {
                    fontSize = 18
                }
                className.startsWith("text-") && className.contains("sm") -> {
                    fontSize = 12
                }
                className.contains("font-bold") -> {
                    fontWeight = "bold"
                }
                className.contains("text-center") -> {
                    textAlign = "center"
                }
                className.contains("flex") -> {
                    display = "flex"
                }
            }
        }
        
        // Dessiner le background
        g2d.color = backgroundColor
        g2d.fillRoundRect(margin, margin, width - margin * 2, height - margin * 2, borderRadius, borderRadius)
        
        // Dessiner la bordure si présente
        if (borderColor != null) {
            g2d.color = borderColor
            g2d.drawRoundRect(margin, margin, width - margin * 2, height - margin * 2, borderRadius, borderRadius)
        }
        
        // Dessiner le texte
        g2d.color = textColor
        g2d.font = java.awt.Font("Arial", if (fontWeight == "bold") java.awt.Font.BOLD else java.awt.Font.PLAIN, fontSize)
        
        val text = generatePreviewText(classes)
        val fontMetrics = g2d.fontMetrics
        val textWidth = fontMetrics.stringWidth(text)
        val textHeight = fontMetrics.height
        
        val x = when (textAlign) {
            "center" -> (width - textWidth) / 2
            "right" -> width - textWidth - padding
            else -> padding
        }
        val y = (height + textHeight) / 2 - fontMetrics.descent
        
        g2d.drawString(text, x, y)
    }
    
    /**
     * Parse une couleur Tailwind
     */
    private fun parseColor(colorName: String): Color {
        return when (colorName) {
            "red-500" -> Color(239, 68, 68)
            "blue-500" -> Color(59, 130, 246)
            "green-500" -> Color(34, 197, 94)
            "yellow-500" -> Color(234, 179, 8)
            "purple-500" -> Color(147, 51, 234)
            "pink-500" -> Color(236, 72, 153)
            "indigo-500" -> Color(99, 102, 241)
            "gray-500" -> Color(107, 114, 128)
            "black" -> Color.BLACK
            "white" -> Color.WHITE
            "transparent" -> Color(0, 0, 0, 0)
            else -> Color.GRAY
        }
    }
    
    /**
     * Parse le border radius
     */
    private fun parseBorderRadius(className: String): Int {
        return when (className) {
            "rounded-none" -> 0
            "rounded-sm" -> 2
            "rounded" -> 4
            "rounded-md" -> 6
            "rounded-lg" -> 8
            "rounded-xl" -> 12
            "rounded-2xl" -> 16
            "rounded-full" -> 50
            else -> 4
        }
    }
    
    /**
     * Parse l'espacement
     */
    private fun parseSpacing(spacing: String): Int {
        return when (spacing) {
            "0" -> 0
            "1" -> 4
            "2" -> 8
            "3" -> 12
            "4" -> 16
            "5" -> 20
            "6" -> 24
            "8" -> 32
            "10" -> 40
            "12" -> 48
            "16" -> 64
            "20" -> 80
            "24" -> 96
            else -> 8
        }
    }
    
    /**
     * Génère un texte de preview basé sur les classes
     */
    private fun generatePreviewText(classes: List<String>): String {
        return when {
            classes.any { it.contains("button") || it.contains("btn") } -> "Button"
            classes.any { it.contains("card") } -> "Card"
            classes.any { it.contains("modal") } -> "Modal"
            classes.any { it.contains("alert") } -> "Alert"
            classes.any { it.contains("badge") } -> "Badge"
            classes.any { it.contains("avatar") } -> "Avatar"
            else -> "Preview"
        }
    }
    
    /**
     * Extrait les propriétés CSS des classes
     */
    private fun extractCSSProperties(classes: List<String>): Map<String, String> {
        val properties = mutableMapOf<String, String>()
        
        classes.forEach { className ->
            when {
                className.startsWith("bg-") -> {
                    properties["background-color"] = parseColor(className.substring(3)).toString()
                }
                className.startsWith("text-") -> {
                    properties["color"] = parseColor(className.substring(5)).toString()
                }
                className.startsWith("p-") -> {
                    val padding = parseSpacing(className.substring(2))
                    properties["padding"] = "${padding}px"
                }
                className.startsWith("m-") -> {
                    val margin = parseSpacing(className.substring(2))
                    properties["margin"] = "${margin}px"
                }
                className.startsWith("rounded") -> {
                    val radius = parseBorderRadius(className)
                    properties["border-radius"] = "${radius}px"
                }
                className.contains("font-bold") -> {
                    properties["font-weight"] = "bold"
                }
                className.contains("text-center") -> {
                    properties["text-align"] = "center"
                }
                className.contains("flex") -> {
                    properties["display"] = "flex"
                }
            }
        }
        
        return properties
    }
    
    /**
     * Génère une description des classes
     */
    private fun generateDescription(classes: List<String>): String {
        val descriptions = mutableListOf<String>()
        
        classes.forEach { className ->
            when {
                className.startsWith("bg-") -> descriptions.add("Background: ${className.substring(3)}")
                className.startsWith("text-") -> descriptions.add("Text color: ${className.substring(5)}")
                className.startsWith("p-") -> descriptions.add("Padding: ${className.substring(2)}")
                className.startsWith("m-") -> descriptions.add("Margin: ${className.substring(2)}")
                className.startsWith("rounded") -> descriptions.add("Border radius: $className")
                className.contains("font-bold") -> descriptions.add("Font weight: bold")
                className.contains("text-center") -> descriptions.add("Text align: center")
                className.contains("flex") -> descriptions.add("Display: flex")
            }
        }
        
        return descriptions.joinToString(", ")
    }
}

/**
 * Données de preview
 */
data class PreviewData(
    val image: BufferedImage,
    val icon: Icon,
    val classes: List<String>,
    val cssProperties: Map<String, String>,
    val description: String
) {
    companion object {
        fun empty(): PreviewData {
            val emptyImage = BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB)
            return PreviewData(
                image = emptyImage,
                icon = ImageIcon(emptyImage),
                classes = emptyList(),
                cssProperties = emptyMap(),
                description = "No preview available"
            )
        }
    }
}