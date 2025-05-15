package com.github.dilika.tailwindsmartplugin.utils

import com.github.dilika.tailwindsmartplugin.settings.TailwindPluginSettings
import com.github.dilika.tailwindsmartplugin.util.TailwindCategoryIcon
import com.github.dilika.tailwindsmartplugin.util.TailwindCategoryUtils
import com.github.dilika.tailwindsmartplugin.util.TextColorIcon
import com.intellij.ui.JBColor
import com.intellij.util.ui.ColorIcon
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.RenderingHints
import javax.swing.Icon
import java.util.concurrent.ConcurrentHashMap

/**
 * A registry for Tailwind CSS class icons that provides a centralized system
 * for creating and retrieving icons based on class names and categories.
 */
object TailwindIconRegistry {
    // Default icon size in pixels
    private const val DEFAULT_ICON_SIZE = 14
    
    // Default color for unknown classes
    private val DEFAULT_COLOR = JBColor(0x6B7280, 0x6B7280)
    
    // Icon cache to avoid recreating icons for the same class
    private val iconCache = ConcurrentHashMap<String, Icon>()
    
    // Cache pour les classes de gradient pour associer from/to/via ensemble
    private val gradientClasses = ConcurrentHashMap<String, MutableList<String>>()

    /**
     * Gets an appropriate icon for a Tailwind CSS class
     *
     * @param className The Tailwind CSS class name
     * @param size The size of the icon (default is 14px)
     * @return An appropriate icon based on the class type
     */
    fun getIconForClass(className: String, size: Int = DEFAULT_ICON_SIZE): Icon {
        // Check plugin settings to see if icons should be displayed
        val settings = TailwindPluginSettings.getInstance()
        if (!settings.showIcons) {
            // Return a blank icon with the correct size if icons are disabled
            return EmptyIcon(size)
        }
        
        // Check cache first for performance
        val cacheKey = "$className-$size"
        iconCache[cacheKey]?.let { return it }
        
        // Extract the base class name without variants (hover:, md:, etc.)
        val baseClass = className.split(":").last()
        
        // Gérer spécialement les classes de type couleur (text-, bg-, border-, etc.)
        if (isColorClass(baseClass)) {
            try {
                val colorValue = extractColorValue(baseClass)
                if (colorValue != null) {
                    // Créer une icône spécifique selon le type de classe
                    val icon = when {
                        baseClass.startsWith("text-") || baseClass.startsWith("font-") -> {
                            // Utiliser TextColorIcon pour un "T" directement coloré sans fond
                            TextColorIcon(colorValue, size)
                        }
                        baseClass.startsWith("border-") -> {
                            // Pour les bordures, utiliser une icône spécifique avec un cadre
                            BorderColorIcon(colorValue, size)
                        }
                        baseClass.startsWith("bg-") -> {
                            // Pour les fonds, utiliser une icône remplie
                            BackgroundColorIcon(colorValue, size)
                        }
                        else -> {
                            // Pour les autres types, utiliser une icône générique
                            createColorIcon(colorValue, size)
                        }
                    }
                    iconCache[cacheKey] = icon
                    return icon
                } else {
                    // Fallback en cas d'absence de couleur extraite
                    val (category, defaultColor) = TailwindCategoryUtils.getCategoryAndColor(baseClass)
                    
                    // Créer une icône spécifique selon le type de classe
                    val icon = when {
                        baseClass.startsWith("text-") || baseClass.startsWith("font-") -> {
                            TextColorIcon(defaultColor, size)
                        }
                        baseClass.startsWith("border-") -> {
                            BorderColorIcon(defaultColor, size)
                        }
                        baseClass.startsWith("bg-") -> {
                            BackgroundColorIcon(defaultColor, size)
                        }
                        else -> {
                            createColorIcon(defaultColor, size)
                        }
                    }
                    iconCache[cacheKey] = icon
                    return icon
                }
            } catch (e: Exception) {
                // Fallback en cas d'erreur
                val defaultColor = JBColor(0x64748B, 0x94A3B8) // slate-500/400
                
                // Créer une icône spécifique selon le type de classe, même en cas d'erreur
                val icon = when {
                    baseClass.startsWith("text-") || baseClass.startsWith("font-") -> {
                        TextColorIcon(defaultColor, size)
                    }
                    baseClass.startsWith("border-") -> {
                        BorderColorIcon(defaultColor, size)
                    }
                    baseClass.startsWith("bg-") -> {
                        BackgroundColorIcon(defaultColor, size)
                    }
                    else -> {
                        createColorIcon(defaultColor, size)
                    }
                }
                iconCache[cacheKey] = icon
                return icon
            }
        }
        
        // Gérer spécialement les classes de gradient
        if (baseClass.startsWith("bg-gradient-to-")) {
            val gradientIcon = GradientIcon.fromGradientClass(baseClass, size)
            iconCache[cacheKey] = gradientIcon
            return gradientIcon
        }
        
        // Gérer les classes from/to/via
        if (baseClass.startsWith("from-") || baseClass.startsWith("to-") || baseClass.startsWith("via-")) {
            // Stocker la classe dans le cache de gradient (pour une future utilisation combinée)
            val classPrefix = baseClass.substringBefore("-")
            val colorPart = baseClass.substringAfter("$classPrefix-")
            
            // Ajouter au cache pour une utilisation ultérieure
            gradientClasses.computeIfAbsent(colorPart) { mutableListOf() }.add(classPrefix)
            
            // Trouver une classe correspondante à combiner (from + to ou via)
            val fromClass = gradientClasses[colorPart]?.find { it == "from" }?.let { "from-$colorPart" }
            val toClass = gradientClasses[colorPart]?.find { it == "to" }?.let { "to-$colorPart" }
            
            if (fromClass != null && toClass != null) {
                // Créer une icône de dégradé avec les deux couleurs
                val gradientIcon = GradientIcon.fromColorStops(fromClass, toClass, size)
                iconCache[cacheKey] = gradientIcon
                return gradientIcon
            }
        }
        
        // Handle color classes specially
        val colorValue = extractColorValue(baseClass)
        if (colorValue != null) {
            val colorIcon = createColorIcon(colorValue, size)
            iconCache[cacheKey] = colorIcon
            return colorIcon
        }
        
        // For other classes, use category-based icons
        val (category, color) = TailwindCategoryUtils.getCategoryAndColor(baseClass)
        val icon = TailwindCategoryIcon(category.toString(), color, size)
        iconCache[cacheKey] = icon
        return icon
    }
    
    /**
     * Creates a specialized color icon for color-related classes
     */
    private fun createColorIcon(color: Color, size: Int): Icon {
        val settings = TailwindPluginSettings.getInstance()
        
        // Create a circular or square color icon based on settings
        return if (settings.useRoundedColorIcons) {
            RoundColorIcon(size, color)
        } else {
            ColorIcon(size, color)
        }
    }
    
    /**
     * Extract color value from class name if it's a color-related class
     */
    private fun extractColorValue(className: String): Color? {
        // Extraire la valeur de couleur du nom de classe s'il s'agit d'une classe liée à la couleur
        if (className.startsWith("bg-") || 
            className.startsWith("text-") || 
            className.startsWith("border-") ||
            className.startsWith("ring-") ||
            className.startsWith("shadow-") ||
            className.startsWith("divide-") ||
            className.startsWith("from-") ||
            className.startsWith("to-") ||
            className.startsWith("via-")) {
            
            // Les classes bg-gradient sont maintenant traitées séparément plus haut
            
            try {
                // Extraction de couleur à partir de la classe en utilisant TailwindCategoryUtils
                val (_, colorValue) = TailwindCategoryUtils.getCategoryAndColor(className)
                return colorValue.let { jbColor ->
                    Color(jbColor.rgb)
                }
            } catch (e: Exception) {
                // Fallback en cas d'erreur
                return when {
                    className.contains("white") -> Color.WHITE
                    className.contains("black") -> Color.BLACK
                    className.contains("transparent") -> Color(0, 0, 0, 0)
                    else -> null
                }
            }
        }
        
        // Check for arbitrary color values like "bg-[#ff0000]" or "text-[rgb(255,0,0)]"
        if (className.contains("[") && className.contains("]")) {
            val start = className.indexOf("[")
            val end = className.indexOf("]", start)
            if (end > start) {
                val colorStr = className.substring(start + 1, end).trim()
                return parseArbitraryColor(colorStr)
            }
        }
        
        return null
    }
    
    /**
     * Détermine si une classe est liée à une couleur
     */
    private fun isColorClass(className: String): Boolean {
        // Préfixes de classes qui acceptent des couleurs
        val colorPrefixes = listOf(
            "text-", "bg-", "border-", "ring-", "shadow-", "divide-", 
            "outline-", "accent-", "caret-", "fill-", "stroke-",
            "from-", "to-", "via-"
        )
        
        // Classes avec couleur normales (text-red-500, bg-blue-600, etc.)
        for (prefix in colorPrefixes) {
            if (className.startsWith(prefix)) {
                return true
            }
        }
        
        // Cas spécial pour font-color (pas font-weight/style)
        if (className.startsWith("font-")) {
            val colorNames = listOf(
                "black", "white", "transparent", "gray", "slate", "red", "blue", "green", 
                "yellow", "purple", "pink", "indigo", "emerald", "teal", "cyan", "sky", 
                "violet", "amber", "lime", "orange", "fuchsia", "rose"
            )
            
            for (colorName in colorNames) {
                if (className == "font-$colorName" || className.contains("-$colorName-")) {
                    return true
                }
            }
            
            return false // Autres classes font-* ne sont pas liées aux couleurs
        }
        
        // Classes de couleur directes (white, black, etc.)
        val directColorNames = listOf("white", "black", "transparent")
        if (directColorNames.contains(className)) {
            return true
        }
        
        return false
    }
    
    /**
     * Parse arbitrary color values from JIT syntax
     */
    
    private fun parseArbitraryColor(colorStr: String): Color? {
        return try {
            when {
                // Hex colors: #RGB, #RRGGBB, #RRGGBBAA
                colorStr.startsWith("#") -> {
                    val hex = colorStr.substring(1)
                    when (hex.length) {
                        3 -> { // #RGB format
                            val r = hex[0].toString().repeat(2).toInt(16)
                            val g = hex[1].toString().repeat(2).toInt(16)
                            val b = hex[2].toString().repeat(2).toInt(16)
                            Color(r, g, b)
                        }
                        6 -> { // #RRGGBB format
                            Color(hex.toInt(16))
                        }
                        8 -> { // #RRGGBBAA format
                            val rgb = hex.substring(0, 6).toInt(16)
                            val alpha = hex.substring(6, 8).toInt(16)
                            Color(rgb and 0xFFFFFF or (alpha shl 24), true)
                        }
                        else -> null
                    }
                }
                // RGB format: rgb(255,0,0) or rgba(255,0,0,0.5)
                colorStr.startsWith("rgb") -> {
                    val content = colorStr.substring(colorStr.indexOf("(") + 1, colorStr.indexOf(")"))
                    val parts = content.split(",").map { it.trim() }
                    
                    when (parts.size) {
                        3 -> { // rgb(r,g,b)
                            val r = parts[0].toInt()
                            val g = parts[1].toInt()
                            val b = parts[2].toInt()
                            Color(r, g, b)
                        }
                        4 -> { // rgba(r,g,b,a)
                            val r = parts[0].toInt()
                            val g = parts[1].toInt()
                            val b = parts[2].toInt()
                            val a = (parts[3].toFloat() * 255).toInt()
                            Color(r, g, b, a)
                        }
                        else -> null
                    }
                }
                // HSL format: hsl(120,100%,50%) or hsla(120,100%,50%,0.5)
                colorStr.startsWith("hsl") -> {
                    // HSL support is more complex and would be implemented here
                    // For now, we'll return a default color
                    Color(128, 128, 128)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * A circular color icon that displays a colored circle with a subtle border
 */
class RoundColorIcon(private val size: Int, private val color: Color) : Icon {
    override fun paintIcon(c: java.awt.Component, g: java.awt.Graphics, x: Int, y: Int) {
        val g2d = g.create() as java.awt.Graphics2D
        try {
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
            
            // Fill the circle
            g2d.color = color
            g2d.fillOval(x, y, size, size)
            
            // Add a subtle border if the color is very light
            val brightness = (color.red * 0.299 + color.green * 0.587 + color.blue * 0.114) / 255
            if (brightness > 0.85) {
                g2d.color = JBColor(Color(200, 200, 200), Color(100, 100, 100))
                g2d.drawOval(x, y, size - 1, size - 1)
            }
        } finally {
            g2d.dispose()
        }
    }

    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size
}

/**
 * An empty icon that just takes up space but doesn't display anything
 */
class EmptyIcon(private val size: Int) : Icon {
    override fun paintIcon(c: java.awt.Component, g: java.awt.Graphics, x: Int, y: Int) {
        // Do nothing - icon is invisible
    }

    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size
}

// EmptyIcon classe supprimée car dupliquée - utilisez celle définie plus haut

/**
 * A border color icon that displays a square with a colored border
 */
class BorderColorIcon(private val color: Color, private val size: Int) : Icon {
    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Dessiner un carré avec une bordure colorée
        g2d.color = color
        val borderThickness = maxOf(1, size / 6)
        val rect = Rectangle(x + borderThickness/2, y + borderThickness/2, size - borderThickness, size - borderThickness)
        g2d.stroke = BasicStroke(borderThickness.toFloat())
        g2d.draw(rect)

        g2d.dispose()
    }

    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size
}

/**
 * A background color icon that displays a filled square with the color
 */
class BackgroundColorIcon(private val color: Color, private val size: Int) : Icon {
    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Dessiner un carré rempli
        g2d.color = color
        g2d.fillRect(x, y, size, size)

        // Ajouter une bordure subtile
        g2d.color = color.darker()
        g2d.drawRect(x, y, size - 1, size - 1)

        g2d.dispose()
    }

    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size
}
