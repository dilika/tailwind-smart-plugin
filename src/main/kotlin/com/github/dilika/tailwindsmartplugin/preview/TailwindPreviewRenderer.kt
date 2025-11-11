package com.github.dilika.tailwindsmartplugin.preview

import com.intellij.openapi.diagnostic.Logger
import java.awt.*
import java.awt.image.BufferedImage
import javax.swing.Icon
import javax.swing.ImageIcon

/**
 * Renderer de preview qui génère des images réelles au lieu d'HTML
 * car JEditorPane ne peut pas exécuter JavaScript (Tailwind CDN)
 */
class TailwindPreviewRenderer {
    
    private val logger = Logger.getInstance(TailwindPreviewRenderer::class.java)
    
    /**
     * Génère une image de preview pour les classes Tailwind
     */
    fun renderPreview(
        classes: List<String>,
        width: Int = 350,
        height: Int = 250,
        darkMode: Boolean = false
    ): Icon {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.graphics as Graphics2D
        
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            
            // Background
            val bgColor = if (darkMode) Color(0x1f2937) else Color(0xffffff)
            g2d.color = bgColor
            g2d.fillRect(0, 0, width, height)
            
            // Border
            g2d.color = if (darkMode) Color(0x374151) else Color(0xd1d5db)
            g2d.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0f, floatArrayOf(5f, 5f), 0f)
            g2d.drawRect(5, 5, width - 11, height - 11)
            
            // Parse et applique les classes Tailwind
            val styles = parseTailwindClasses(classes, darkMode)
            renderStyles(g2d, styles, width, height)
            
        } finally {
            g2d.dispose()
        }
        
        return ImageIcon(image)
    }
    
    /**
     * Parse les classes Tailwind en styles
     */
    private fun parseTailwindClasses(classes: List<String>, darkMode: Boolean): PreviewStyles {
        val styles = PreviewStyles()
        
        classes.forEach { className ->
            when {
                className.startsWith("bg-") -> {
                    styles.backgroundColor = parseColor(className.substring(3), darkMode)
                }
                className.startsWith("text-") -> {
                    styles.textColor = parseColor(className.substring(5), darkMode)
                }
                className.startsWith("border-") -> {
                    styles.borderColor = parseColor(className.substring(7), darkMode)
                    styles.borderWidth = 1
                }
                className.startsWith("p-") -> {
                    styles.padding = parseSpacing(className.substring(2))
                }
                className.startsWith("px-") -> {
                    styles.paddingX = parseSpacing(className.substring(3))
                }
                className.startsWith("py-") -> {
                    styles.paddingY = parseSpacing(className.substring(3))
                }
                className.startsWith("m-") -> {
                    styles.margin = parseSpacing(className.substring(2))
                }
                className.startsWith("rounded") -> {
                    styles.borderRadius = parseBorderRadius(className)
                }
                className.contains("font-bold") -> {
                    styles.fontWeight = Font.BOLD
                }
                className.contains("text-center") -> {
                    styles.textAlign = "center"
                }
                className.contains("flex") -> {
                    styles.display = "flex"
                }
                className.contains("grid") -> {
                    styles.display = "grid"
                }
                className.startsWith("w-") -> {
                    styles.width = parseSize(className.substring(2))
                }
                className.startsWith("h-") -> {
                    styles.height = parseSize(className.substring(2))
                }
            }
        }
        
        return styles
    }
    
    /**
     * Rend les styles sur le canvas
     */
    private fun renderStyles(g2d: Graphics2D, styles: PreviewStyles, width: Int, height: Int) {
        val margin = styles.margin
        val padding = styles.padding
        val x = margin
        val y = margin
        val w = (styles.width ?: (width - margin * 2 - padding * 2)).coerceAtMost(width - margin * 2 - padding * 2)
        val h = (styles.height ?: (height - margin * 2 - padding * 2)).coerceAtMost(height - margin * 2 - padding * 2)
        
        // Background
        if (styles.backgroundColor != null) {
            g2d.color = styles.backgroundColor
            if (styles.borderRadius > 0) {
                g2d.fillRoundRect(x + padding, y + padding, w, h, styles.borderRadius, styles.borderRadius)
            } else {
                g2d.fillRect(x + padding, y + padding, w, h)
            }
        }
        
        // Border
        if (styles.borderColor != null && styles.borderWidth > 0) {
            g2d.color = styles.borderColor
            g2d.stroke = BasicStroke(styles.borderWidth.toFloat())
            if (styles.borderRadius > 0) {
                g2d.drawRoundRect(x + padding, y + padding, w, h, styles.borderRadius, styles.borderRadius)
            } else {
                g2d.drawRect(x + padding, y + padding, w, h)
            }
        }
        
        // Text
        if (styles.textColor != null) {
            g2d.color = styles.textColor
            val fontSize = 14
            val font = Font("Arial", styles.fontWeight, fontSize)
            g2d.font = font
            
            val text = "Preview"
            val metrics = g2d.fontMetrics
            val textWidth = metrics.stringWidth(text)
            val textHeight = metrics.height
            
            val textX = when (styles.textAlign) {
                "center" -> x + padding + (w - textWidth) / 2
                "right" -> x + padding + w - textWidth - 10
                else -> x + padding + 10
            }
            val textY = y + padding + (h + textHeight) / 2 - metrics.descent
            
            g2d.drawString(text, textX, textY)
        }
    }
    
    /**
     * Parse une couleur Tailwind
     */
    private fun parseColor(colorName: String, darkMode: Boolean): Color {
        // Tailwind v4.1 color palette
        val colorMap = mapOf(
            // Grays
            "gray-50" to Color(0xf9fafb), "gray-100" to Color(0xf3f4f6), "gray-200" to Color(0xe5e7eb),
            "gray-300" to Color(0xd1d5db), "gray-400" to Color(0x9ca3af), "gray-500" to Color(0x6b7280),
            "gray-600" to Color(0x4b5563), "gray-700" to Color(0x374151), "gray-800" to Color(0x1f2937),
            "gray-900" to Color(0x111827), "gray-950" to Color(0x030712),
            // Reds
            "red-50" to Color(0xfef2f2), "red-100" to Color(0xfee2e2), "red-200" to Color(0xfecaca),
            "red-300" to Color(0xfca5a5), "red-400" to Color(0xf87171), "red-500" to Color(0xef4444),
            "red-600" to Color(0xdc2626), "red-700" to Color(0xb91c1c), "red-800" to Color(0x991b1b),
            "red-900" to Color(0x7f1d1d), "red-950" to Color(0x450a0a),
            // Blues
            "blue-50" to Color(0xeff6ff), "blue-100" to Color(0xdbeafe), "blue-200" to Color(0xbfdbfe),
            "blue-300" to Color(0x93c5fd), "blue-400" to Color(0x60a5fa), "blue-500" to Color(0x3b82f6),
            "blue-600" to Color(0x2563eb), "blue-700" to Color(0x1d4ed8), "blue-800" to Color(0x1e40af),
            "blue-900" to Color(0x1e3a8a), "blue-950" to Color(0x172554),
            // Greens
            "green-50" to Color(0xf0fdf4), "green-100" to Color(0xdcfce7), "green-200" to Color(0xbbf7d0),
            "green-300" to Color(0x86efac), "green-400" to Color(0x4ade80), "green-500" to Color(0x22c55e),
            "green-600" to Color(0x16a34a), "green-700" to Color(0x15803d), "green-800" to Color(0x166534),
            "green-900" to Color(0x14532d), "green-950" to Color(0x052e16),
            // Yellows
            "yellow-50" to Color(0xfefce8), "yellow-100" to Color(0xfef9c3), "yellow-200" to Color(0xfef08a),
            "yellow-300" to Color(0xfde047), "yellow-400" to Color(0xfacc15), "yellow-500" to Color(0xeab308),
            "yellow-600" to Color(0xca8a04), "yellow-700" to Color(0xa16207), "yellow-800" to Color(0x854d0e),
            "yellow-900" to Color(0x713f12), "yellow-950" to Color(0x422006),
            // Purples
            "purple-50" to Color(0xfaf5ff), "purple-100" to Color(0xf3e8ff), "purple-200" to Color(0xe9d5ff),
            "purple-300" to Color(0xd8b4fe), "purple-400" to Color(0xc084fc), "purple-500" to Color(0xa855f7),
            "purple-600" to Color(0x9333ea), "purple-700" to Color(0x7e22ce), "purple-800" to Color(0x6b21a8),
            "purple-900" to Color(0x581c87), "purple-950" to Color(0x3b0764),
            // Pinks
            "pink-50" to Color(0xfdf2f8), "pink-100" to Color(0xfce7f3), "pink-200" to Color(0xfbcfe8),
            "pink-300" to Color(0xf9a8d4), "pink-400" to Color(0xf472b6), "pink-500" to Color(0xec4899),
            "pink-600" to Color(0xdb2777), "pink-700" to Color(0xbe185d), "pink-800" to Color(0x9f1239),
            "pink-900" to Color(0x831843), "pink-950" to Color(0x500724),
            // Indigos
            "indigo-50" to Color(0xeef2ff), "indigo-100" to Color(0xe0e7ff), "indigo-200" to Color(0xc7d2fe),
            "indigo-300" to Color(0xa5b4fc), "indigo-400" to Color(0x818cf8), "indigo-500" to Color(0x6366f1),
            "indigo-600" to Color(0x4f46e5), "indigo-700" to Color(0x4338ca), "indigo-800" to Color(0x3730a3),
            "indigo-900" to Color(0x312e81), "indigo-950" to Color(0x1e1b4b),
            // Special
            "white" to Color.WHITE, "black" to Color.BLACK, "transparent" to Color(0, 0, 0, 0)
        )
        
        // Extract color and shade (e.g., "red-500" -> "red-500", "blue" -> "blue-500")
        val normalizedColor = when {
            colorName.contains("-") -> colorName
            colorName in listOf("white", "black", "transparent") -> colorName
            else -> "$colorName-500" // Default shade
        }
        
        return colorMap[normalizedColor] ?: Color(0x6b7280) // Default gray
    }
    
    private fun parseSpacing(value: String): Int {
        return when (value) {
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
    
    private fun parseBorderRadius(className: String): Int {
        return when {
            className.contains("none") -> 0
            className.contains("sm") -> 2
            className.contains("md") -> 6
            className.contains("lg") -> 8
            className.contains("xl") -> 12
            className.contains("2xl") -> 16
            className.contains("full") -> 9999
            else -> 4
        }
    }
    
    private fun parseSize(value: String): Int? {
        return when (value) {
            "full" -> null // 100%
            "auto" -> null
            "screen" -> null
            else -> parseSpacing(value)
        }
    }
}

/**
 * Styles de preview
 */
private data class PreviewStyles(
    var backgroundColor: Color? = null,
    var textColor: Color? = null,
    var borderColor: Color? = null,
    var borderWidth: Int = 0,
    var borderRadius: Int = 0,
    var padding: Int = 8,
    var paddingX: Int = 0,
    var paddingY: Int = 0,
    var margin: Int = 10,
    var fontWeight: Int = Font.PLAIN,
    var textAlign: String = "left",
    var display: String = "block",
    var width: Int? = null,
    var height: Int? = null
)

