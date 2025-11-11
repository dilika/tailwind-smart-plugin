package com.github.dilika.tailwindsmartplugin.icons

import com.intellij.ui.JBColor
import java.awt.*
import java.awt.geom.RoundRectangle2D
import javax.swing.Icon

/**
 * Registry d'icônes premium pour Tailwind CSS
 * Style similaire aux meilleurs plugins du marché (VS Code Tailwind IntelliSense)
 */
object TailwindPremiumIconRegistry {
    
    private const val ICON_SIZE = 16
    private val iconCache = mutableMapOf<String, Icon>()
    
    /**
     * Obtient une icône premium pour une classe Tailwind
     */
    fun getPremiumIcon(className: String): Icon {
        try {
            iconCache[className]?.let { return it }
            
            val baseClass = className.split(":").last()
            val category = getCategory(baseClass)
            val color = getCategoryColor(category, baseClass) ?: category.defaultColor
            
            val icon = try {
                when (category) {
                    TailwindCategory.COLOR -> createColorIcon(color, baseClass)
                    TailwindCategory.SPACING -> createSpacingIcon(color)
                    TailwindCategory.TYPOGRAPHY -> createTypographyIcon(color)
                    TailwindCategory.LAYOUT -> createLayoutIcon(color)
                    TailwindCategory.BORDERS -> createBorderIcon(color)
                    TailwindCategory.EFFECTS -> createEffectIcon(color)
                    TailwindCategory.TRANSFORMS -> createTransformIcon(color)
                    TailwindCategory.INTERACTIVITY -> createInteractivityIcon(color)
                    TailwindCategory.SVG -> createSVGIcon(color)
                    TailwindCategory.ACCESSIBILITY -> createAccessibilityIcon(color)
                    TailwindCategory.OTHER -> createDefaultIcon(color)
                }
            } catch (e: Exception) {
                // Fallback en cas d'erreur
                PremiumDefaultIcon(category.defaultColor, ICON_SIZE)
            }
            
            iconCache[className] = icon
            return icon
        } catch (e: Exception) {
            // Fallback ultime en cas d'erreur
            return PremiumDefaultIcon(Color(0x6b7280), ICON_SIZE)
        }
    }
    
    /**
     * Détermine la catégorie d'une classe Tailwind
     */
    private fun getCategory(className: String): TailwindCategory {
        return when {
            // Colors
            className.startsWith("bg-") || className.startsWith("text-") || 
            className.startsWith("border-") || className.startsWith("ring-") ||
            className.startsWith("divide-") || className.startsWith("outline-") ||
            className.startsWith("accent-") || className.startsWith("caret-") ||
            className.startsWith("fill-") || className.startsWith("stroke-") ||
            className.startsWith("from-") || className.startsWith("to-") ||
            className.startsWith("via-") -> TailwindCategory.COLOR
            
            // Spacing
            className.startsWith("p-") || className.startsWith("m-") ||
            className.startsWith("space-") || className.startsWith("gap-") ||
            className.startsWith("inset-") || className.startsWith("top-") ||
            className.startsWith("right-") || className.startsWith("bottom-") ||
            className.startsWith("left-") -> TailwindCategory.SPACING
            
            // Typography
            className.startsWith("font-") || className.startsWith("text-") ||
            className.startsWith("leading-") || className.startsWith("tracking-") ||
            className.startsWith("antialiased") || className.startsWith("subpixel-antialiased") -> TailwindCategory.TYPOGRAPHY
            
            // Layout
            className.startsWith("container") || className.startsWith("box-") ||
            className.startsWith("flex") || className.startsWith("grid") ||
            className.startsWith("table") || className.startsWith("inline-") ||
            className.startsWith("block") || className.startsWith("hidden") ||
            className.startsWith("float-") || className.startsWith("clear-") ||
            className.startsWith("object-") || className.startsWith("overflow-") ||
            className.startsWith("overscroll-") || className.startsWith("position-") ||
            className.startsWith("visible") || className.startsWith("invisible") ||
            className.startsWith("z-") || className.startsWith("columns-") ||
            className.startsWith("break-") -> TailwindCategory.LAYOUT
            
            // Borders
            className.startsWith("border") || className.startsWith("rounded") ||
            className.startsWith("outline") -> TailwindCategory.BORDERS
            
            // Effects
            className.startsWith("shadow-") || className.startsWith("opacity-") ||
            className.startsWith("mix-blend-") || className.startsWith("bg-blend-") -> TailwindCategory.EFFECTS
            
            // Transforms
            className.startsWith("scale-") || className.startsWith("rotate-") ||
            className.startsWith("translate-") || className.startsWith("skew-") ||
            className.startsWith("transform") || className.startsWith("origin-") -> TailwindCategory.TRANSFORMS
            
            // Interactivity
            className.startsWith("cursor-") || className.startsWith("select-") ||
            className.startsWith("resize") || className.startsWith("scroll-") ||
            className.startsWith("snap-") || className.startsWith("touch-") ||
            className.startsWith("will-change") -> TailwindCategory.INTERACTIVITY
            
            // SVG
            className.startsWith("fill-") || className.startsWith("stroke-") -> TailwindCategory.SVG
            
            // Accessibility
            className.startsWith("sr-only") || className.startsWith("not-sr-only") -> TailwindCategory.ACCESSIBILITY
            
            else -> TailwindCategory.OTHER
        }
    }
    
    /**
     * Obtient la couleur pour une catégorie
     */
    private fun getCategoryColor(category: TailwindCategory, className: String): Color {
        // Si c'est une classe de couleur, extraire la couleur réelle
        if (category == TailwindCategory.COLOR) {
            return extractColorFromClass(className) ?: category.defaultColor
        }
        
        return category.defaultColor
    }
    
    /**
     * Extrait la couleur d'une classe Tailwind
     */
    private fun extractColorFromClass(className: String): Color? {
        // Tailwind v4.1 color palette complète
        val colorMap = mapOf(
            // Slate
            "slate-50" to Color(0xf8fafc), "slate-100" to Color(0xf1f5f9), "slate-200" to Color(0xe2e8f0),
            "slate-300" to Color(0xcbd5e1), "slate-400" to Color(0x94a3b8), "slate-500" to Color(0x64748b),
            "slate-600" to Color(0x475569), "slate-700" to Color(0x334155), "slate-800" to Color(0x1e293b),
            "slate-900" to Color(0x0f172a), "slate-950" to Color(0x020617),
            // Gray
            "gray-50" to Color(0xf9fafb), "gray-100" to Color(0xf3f4f6), "gray-200" to Color(0xe5e7eb),
            "gray-300" to Color(0xd1d5db), "gray-400" to Color(0x9ca3af), "gray-500" to Color(0x6b7280),
            "gray-600" to Color(0x4b5563), "gray-700" to Color(0x374151), "gray-800" to Color(0x1f2937),
            "gray-900" to Color(0x111827), "gray-950" to Color(0x030712),
            // Zinc
            "zinc-50" to Color(0xfafafa), "zinc-100" to Color(0xf4f4f5), "zinc-200" to Color(0xe4e4e7),
            "zinc-300" to Color(0xd4d4d8), "zinc-400" to Color(0xa1a1aa), "zinc-500" to Color(0x71717a),
            "zinc-600" to Color(0x52525b), "zinc-700" to Color(0x3f3f46), "zinc-800" to Color(0x27272a),
            "zinc-900" to Color(0x18181b), "zinc-950" to Color(0x09090b),
            // Neutral
            "neutral-50" to Color(0xfafafa), "neutral-100" to Color(0xf5f5f5), "neutral-200" to Color(0xe5e5e5),
            "neutral-300" to Color(0xd4d4d4), "neutral-400" to Color(0xa3a3a3), "neutral-500" to Color(0x737373),
            "neutral-600" to Color(0x525252), "neutral-700" to Color(0x404040), "neutral-800" to Color(0x262626),
            "neutral-900" to Color(0x171717), "neutral-950" to Color(0x0a0a0a),
            // Stone
            "stone-50" to Color(0xfafaf9), "stone-100" to Color(0xf5f5f4), "stone-200" to Color(0xe7e5e4),
            "stone-300" to Color(0xd6d3d1), "stone-400" to Color(0xa8a29e), "stone-500" to Color(0x78716c),
            "stone-600" to Color(0x57534e), "stone-700" to Color(0x44403c), "stone-800" to Color(0x292524),
            "stone-900" to Color(0x1c1917), "stone-950" to Color(0x0c0a09),
            // Red
            "red-50" to Color(0xfef2f2), "red-100" to Color(0xfee2e2), "red-200" to Color(0xfecaca),
            "red-300" to Color(0xfca5a5), "red-400" to Color(0xf87171), "red-500" to Color(0xef4444),
            "red-600" to Color(0xdc2626), "red-700" to Color(0xb91c1c), "red-800" to Color(0x991b1b),
            "red-900" to Color(0x7f1d1d), "red-950" to Color(0x450a0a),
            // Orange
            "orange-50" to Color(0xfff7ed), "orange-100" to Color(0xffedd5), "orange-200" to Color(0xfed7aa),
            "orange-300" to Color(0xfdba74), "orange-400" to Color(0xfb923c), "orange-500" to Color(0xf97316),
            "orange-600" to Color(0xea580c), "orange-700" to Color(0xc2410c), "orange-800" to Color(0x9a3412),
            "orange-900" to Color(0x7c2d12), "orange-950" to Color(0x431407),
            // Amber
            "amber-50" to Color(0xfffbeb), "amber-100" to Color(0xfef3c7), "amber-200" to Color(0xfde68a),
            "amber-300" to Color(0xfcd34d), "amber-400" to Color(0xfbbf24), "amber-500" to Color(0xf59e0b),
            "amber-600" to Color(0xd97706), "amber-700" to Color(0xb45309), "amber-800" to Color(0x92400e),
            "amber-900" to Color(0x78350f), "amber-950" to Color(0x451a03),
            // Yellow
            "yellow-50" to Color(0xfefce8), "yellow-100" to Color(0xfef9c3), "yellow-200" to Color(0xfef08a),
            "yellow-300" to Color(0xfde047), "yellow-400" to Color(0xfacc15), "yellow-500" to Color(0xeab308),
            "yellow-600" to Color(0xca8a04), "yellow-700" to Color(0xa16207), "yellow-800" to Color(0x854d0e),
            "yellow-900" to Color(0x713f12), "yellow-950" to Color(0x422006),
            // Lime
            "lime-50" to Color(0xf7fee7), "lime-100" to Color(0xecfccb), "lime-200" to Color(0xd9f99d),
            "lime-300" to Color(0xbef264), "lime-400" to Color(0xa3e635), "lime-500" to Color(0x84cc16),
            "lime-600" to Color(0x65a30d), "lime-700" to Color(0x4d7c0f), "lime-800" to Color(0x365314),
            "lime-900" to Color(0x365314), "lime-950" to Color(0x1a2e05),
            // Green
            "green-50" to Color(0xf0fdf4), "green-100" to Color(0xdcfce7), "green-200" to Color(0xbbf7d0),
            "green-300" to Color(0x86efac), "green-400" to Color(0x4ade80), "green-500" to Color(0x22c55e),
            "green-600" to Color(0x16a34a), "green-700" to Color(0x15803d), "green-800" to Color(0x166534),
            "green-900" to Color(0x14532d), "green-950" to Color(0x052e16),
            // Emerald
            "emerald-50" to Color(0xecfdf5), "emerald-100" to Color(0xd1fae5), "emerald-200" to Color(0xa7f3d0),
            "emerald-300" to Color(0x6ee7b7), "emerald-400" to Color(0x34d399), "emerald-500" to Color(0x10b981),
            "emerald-600" to Color(0x059669), "emerald-700" to Color(0x047857), "emerald-800" to Color(0x065f46),
            "emerald-900" to Color(0x064e3b), "emerald-950" to Color(0x022c22),
            // Teal
            "teal-50" to Color(0xf0fdfa), "teal-100" to Color(0xccfbf1), "teal-200" to Color(0x99f6e4),
            "teal-300" to Color(0x5eead4), "teal-400" to Color(0x2dd4bf), "teal-500" to Color(0x14b8a6),
            "teal-600" to Color(0x0d9488), "teal-700" to Color(0x0f766e), "teal-800" to Color(0x115e59),
            "teal-900" to Color(0x134e4a), "teal-950" to Color(0x042f2e),
            // Cyan
            "cyan-50" to Color(0xecfeff), "cyan-100" to Color(0xcffafe), "cyan-200" to Color(0xa5f3fc),
            "cyan-300" to Color(0x67e8f9), "cyan-400" to Color(0x22d3ee), "cyan-500" to Color(0x06b6d4),
            "cyan-600" to Color(0x0891b2), "cyan-700" to Color(0x0e7490), "cyan-800" to Color(0x155e75),
            "cyan-900" to Color(0x164e63), "cyan-950" to Color(0x083344),
            // Sky
            "sky-50" to Color(0xf0f9ff), "sky-100" to Color(0xe0f2fe), "sky-200" to Color(0xbae6fd),
            "sky-300" to Color(0x7dd3fc), "sky-400" to Color(0x38bdf8), "sky-500" to Color(0x0ea5e9),
            "sky-600" to Color(0x0284c7), "sky-700" to Color(0x0369a1), "sky-800" to Color(0x075985),
            "sky-900" to Color(0x0c4a6e), "sky-950" to Color(0x082f49),
            // Blue
            "blue-50" to Color(0xeff6ff), "blue-100" to Color(0xdbeafe), "blue-200" to Color(0xbfdbfe),
            "blue-300" to Color(0x93c5fd), "blue-400" to Color(0x60a5fa), "blue-500" to Color(0x3b82f6),
            "blue-600" to Color(0x2563eb), "blue-700" to Color(0x1d4ed8), "blue-800" to Color(0x1e40af),
            "blue-900" to Color(0x1e3a8a), "blue-950" to Color(0x172554),
            // Indigo
            "indigo-50" to Color(0xeef2ff), "indigo-100" to Color(0xe0e7ff), "indigo-200" to Color(0xc7d2fe),
            "indigo-300" to Color(0xa5b4fc), "indigo-400" to Color(0x818cf8), "indigo-500" to Color(0x6366f1),
            "indigo-600" to Color(0x4f46e5), "indigo-700" to Color(0x4338ca), "indigo-800" to Color(0x3730a3),
            "indigo-900" to Color(0x312e81), "indigo-950" to Color(0x1e1b4b),
            // Violet
            "violet-50" to Color(0xf5f3ff), "violet-100" to Color(0xede9fe), "violet-200" to Color(0xddd6fe),
            "violet-300" to Color(0xc4b5fd), "violet-400" to Color(0xa78bfa), "violet-500" to Color(0x8b5cf6),
            "violet-600" to Color(0x7c3aed), "violet-700" to Color(0x6d28d9), "violet-800" to Color(0x5b21b6),
            "violet-900" to Color(0x4c1d95), "violet-950" to Color(0x2e1065),
            // Purple
            "purple-50" to Color(0xfaf5ff), "purple-100" to Color(0xf3e8ff), "purple-200" to Color(0xe9d5ff),
            "purple-300" to Color(0xd8b4fe), "purple-400" to Color(0xc084fc), "purple-500" to Color(0xa855f7),
            "purple-600" to Color(0x9333ea), "purple-700" to Color(0x7e22ce), "purple-800" to Color(0x6b21a8),
            "purple-900" to Color(0x581c87), "purple-950" to Color(0x3b0764),
            // Fuchsia
            "fuchsia-50" to Color(0xfdf4ff), "fuchsia-100" to Color(0xfae8ff), "fuchsia-200" to Color(0xf5d0fe),
            "fuchsia-300" to Color(0xf0abfc), "fuchsia-400" to Color(0xe879f9), "fuchsia-500" to Color(0xd946ef),
            "fuchsia-600" to Color(0xc026d3), "fuchsia-700" to Color(0xa21caf), "fuchsia-800" to Color(0x86198f),
            "fuchsia-900" to Color(0x701a75), "fuchsia-950" to Color(0x4a044e),
            // Pink
            "pink-50" to Color(0xfdf2f8), "pink-100" to Color(0xfce7f3), "pink-200" to Color(0xfbcfe8),
            "pink-300" to Color(0xf9a8d4), "pink-400" to Color(0xf472b6), "pink-500" to Color(0xec4899),
            "pink-600" to Color(0xdb2777), "pink-700" to Color(0xbe185d), "pink-800" to Color(0x9f1239),
            "pink-900" to Color(0x831843), "pink-950" to Color(0x500724),
            // Rose
            "rose-50" to Color(0xfff1f2), "rose-100" to Color(0xffe4e6), "rose-200" to Color(0xfecdd3),
            "rose-300" to Color(0xfda4af), "rose-400" to Color(0xfb7185), "rose-500" to Color(0xf43f5e),
            "rose-600" to Color(0xe11d48), "rose-700" to Color(0xbe123c), "rose-800" to Color(0x9f1239),
            "rose-900" to Color(0x881337), "rose-950" to Color(0x4c0519),
            // Special
            "white" to Color.WHITE, "black" to Color.BLACK, "transparent" to Color(0, 0, 0, 0),
            "current" to Color(0x3b82f6) // currentColor
        )
        
        // Extract color name (e.g., "bg-red-500" -> "red-500", "text-amber-400" -> "amber-400")
        val colorName = when {
            className.contains("-") -> {
                val parts = className.split("-")
                // Pour "text-amber-400", on veut "amber-400"
                // Pour "bg-red-500", on veut "red-500"
                when {
                    parts.size >= 3 -> {
                        // Prendre les 2 dernières parties (couleur-shade)
                        "${parts[parts.size - 2]}-${parts.last()}"
                    }
                    parts.size == 2 -> {
                        // Si seulement 2 parties, vérifier si c'est une couleur
                        val secondPart = parts.last()
                        if (secondPart in listOf("white", "black", "transparent", "current") || 
                            secondPart.matches(Regex("\\d+"))) {
                            // C'est probablement une valeur numérique, pas une couleur
                            null
                        } else {
                            // C'est peut-être une couleur simple
                            secondPart
                        }
                    }
                    else -> null
                }
            }
            className in listOf("white", "black", "transparent", "current") -> className
            else -> null
        }
        
        return colorName?.let { colorMap[it] }
    }
    
    // Icon creators
    private fun createColorIcon(color: Color, className: String): Icon = PremiumColorIcon(color, ICON_SIZE, className)
    private fun createSpacingIcon(color: Color): Icon = PremiumSpacingIcon(color, ICON_SIZE)
    private fun createTypographyIcon(color: Color): Icon = PremiumTypographyIcon(color, ICON_SIZE)
    private fun createLayoutIcon(color: Color): Icon = PremiumLayoutIcon(color, ICON_SIZE)
    private fun createBorderIcon(color: Color): Icon = PremiumBorderIcon(color, ICON_SIZE)
    private fun createEffectIcon(color: Color): Icon = PremiumEffectIcon(color, ICON_SIZE)
    private fun createTransformIcon(color: Color): Icon = PremiumTransformIcon(color, ICON_SIZE)
    private fun createInteractivityIcon(color: Color): Icon = PremiumInteractivityIcon(color, ICON_SIZE)
    private fun createSVGIcon(color: Color): Icon = PremiumSVGIcon(color, ICON_SIZE)
    private fun createAccessibilityIcon(color: Color): Icon = PremiumAccessibilityIcon(color, ICON_SIZE)
    private fun createDefaultIcon(color: Color): Icon = PremiumDefaultIcon(color, ICON_SIZE)
}

/**
 * Catégories Tailwind v4.1
 */
enum class TailwindCategory(val defaultColor: Color) {
    COLOR(Color(0x3b82f6)),           // Blue
    SPACING(Color(0x10b981)),          // Green
    TYPOGRAPHY(Color(0x8b5cf6)),       // Purple
    LAYOUT(Color(0xf59e0b)),           // Amber
    BORDERS(Color(0x64748b)),          // Slate
    EFFECTS(Color(0xec4899)),          // Pink
    TRANSFORMS(Color(0x06b6d4)),       // Cyan
    INTERACTIVITY(Color(0x6366f1)),     // Indigo
    SVG(Color(0xef4444)),              // Red
    ACCESSIBILITY(Color(0x14b8a6)),    // Teal
    OTHER(Color(0x6b7280))             // Gray
}

/**
 * Icône de couleur premium avec style moderne
 */
class PremiumColorIcon(private val color: Color, private val size: Int, private val className: String) : Icon {
    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        // Cercle coloré avec bordure subtile
        val circleSize = size - 2
        g2d.color = color
        g2d.fillOval(x + 1, y + 1, circleSize, circleSize)
        
        // Bordure subtile
        g2d.color = color.darker().darker()
        g2d.stroke = BasicStroke(0.5f)
        g2d.drawOval(x + 1, y + 1, circleSize, circleSize)
        
        g2d.dispose()
    }
    
    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size
}

/**
 * Icône de spacing (padding/margin)
 */
class PremiumSpacingIcon(private val color: Color, private val size: Int) : Icon {
    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        // Rectangle avec padding visible
        g2d.color = color
        g2d.fillRect(x + 2, y + 2, size - 4, size - 4)
        g2d.color = color.darker()
        g2d.drawRect(x + 2, y + 2, size - 4, size - 4)
        
        g2d.dispose()
    }
    
    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size
}

/**
 * Icône de typography
 */
class PremiumTypographyIcon(private val color: Color, private val size: Int) : Icon {
    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        
        g2d.color = color
        g2d.font = Font("Arial", Font.BOLD, size - 4)
        g2d.drawString("Aa", x + 2, y + size - 2)
        
        g2d.dispose()
    }
    
    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size
}

/**
 * Icône de layout
 */
class PremiumLayoutIcon(private val color: Color, private val size: Int) : Icon {
    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        // Grille 2x2
        g2d.color = color
        val cellSize = (size - 4) / 2
        g2d.fillRect(x + 2, y + 2, cellSize - 1, cellSize - 1)
        g2d.fillRect(x + 2 + cellSize, y + 2, cellSize - 1, cellSize - 1)
        g2d.fillRect(x + 2, y + 2 + cellSize, cellSize - 1, cellSize - 1)
        g2d.fillRect(x + 2 + cellSize, y + 2 + cellSize, cellSize - 1, cellSize - 1)
        
        g2d.dispose()
    }
    
    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size
}

/**
 * Icône de border
 */
class PremiumBorderIcon(private val color: Color, private val size: Int) : Icon {
    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        // Rectangle avec bordure
        g2d.color = color
        g2d.stroke = BasicStroke(2f)
        g2d.drawRoundRect(x + 1, y + 1, size - 3, size - 3, 2, 2)
        
        g2d.dispose()
    }
    
    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size
}

/**
 * Icône d'effet
 */
class PremiumEffectIcon(private val color: Color, private val size: Int) : Icon {
    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        // Ombre/glow
        val centerX = x + size / 2
        val centerY = y + size / 2
        val radius = size / 3
        
        val gradient = RadialGradientPaint(
            centerX.toFloat(), centerY.toFloat(), radius.toFloat(),
            floatArrayOf(0f, 1f),
            arrayOf(color, Color(color.red, color.green, color.blue, 0))
        )
        g2d.paint = gradient
        g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2)
        
        g2d.dispose()
    }
    
    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size
}

/**
 * Icône de transform
 */
class PremiumTransformIcon(private val color: Color, private val size: Int) : Icon {
    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        // Flèche courbée (rotation)
        g2d.color = color
        g2d.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        val centerX = x + size / 2
        val centerY = y + size / 2
        val radius = size / 3
        g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 0, 180)
        
        // Flèche
        val arrowX = centerX + radius
        val arrowY = centerY - radius
        g2d.fillPolygon(
            intArrayOf(arrowX, arrowX - 3, arrowX - 3),
            intArrayOf(arrowY, arrowY - 3, arrowY + 3),
            3
        )
        
        g2d.dispose()
    }
    
    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size
}

/**
 * Icône d'interactivité
 */
class PremiumInteractivityIcon(private val color: Color, private val size: Int) : Icon {
    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        // Curseur
        g2d.color = color
        g2d.fillPolygon(
            intArrayOf(x + 2, x + 2, x + 8),
            intArrayOf(y + 2, y + 10, y + 10),
            3
        )
        g2d.fillRect(x + 8, y + 4, 4, 6)
        
        g2d.dispose()
    }
    
    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size
}

/**
 * Icône SVG
 */
class PremiumSVGIcon(private val color: Color, private val size: Int) : Icon {
    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        // Forme SVG
        g2d.color = color
        g2d.stroke = BasicStroke(2f)
        val path = java.awt.geom.Path2D.Float()
        path.moveTo(x + 2f, y + size / 2f)
        path.lineTo(x + size / 2f, y + 2f)
        path.lineTo(x + size - 2f, y + size / 2f)
        path.lineTo(x + size / 2f, y + size - 2f)
        path.closePath()
        g2d.draw(path)
        
        g2d.dispose()
    }
    
    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size
}

/**
 * Icône d'accessibilité
 */
class PremiumAccessibilityIcon(private val color: Color, private val size: Int) : Icon {
    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        // Icône accessibilité (personne)
        g2d.color = color
        val centerX = x + size / 2
        val centerY = y + size / 2
        val headRadius = size / 6
        g2d.fillOval(centerX - headRadius, centerY - size / 3, headRadius * 2, headRadius * 2)
        g2d.fillRect(centerX - 2, centerY - size / 6, 4, size / 2)
        
        g2d.dispose()
    }
    
    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size
}

/**
 * Icône par défaut
 */
class PremiumDefaultIcon(private val color: Color, private val size: Int) : Icon {
    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        // Carré arrondi simple
        g2d.color = color
        g2d.fillRoundRect(x + 2, y + 2, size - 4, size - 4, 3, 3)
        
        g2d.dispose()
    }
    
    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size
}

