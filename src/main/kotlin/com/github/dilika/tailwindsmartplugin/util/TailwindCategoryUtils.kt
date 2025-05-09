package com.github.dilika.tailwindsmartplugin.util

import com.intellij.ui.JBColor
import java.awt.Color

object TailwindCategoryUtils {
    enum class Category(val displayName: String, val color: JBColor) {
        LAYOUT("Layout", JBColor(0x3B82F6, 0x3B82F6)),
        TYPOGRAPHY("Typography", JBColor(0xEF4444, 0xEF4444)),
        BACKGROUND("Background", JBColor(0x10B981, 0x10B981)),
        BORDER("Border", JBColor(0xF59E0B, 0xF59E0B)),
        EFFECTS("Effects", JBColor(0x8B5CF6, 0x8B5CF6)),
        SPACING("Spacing", JBColor(0xEC4899, 0xEC4899)),
        SIZING("Sizing", JBColor(0x6366F1, 0x6366F1)),
        FLEXBOX("Flexbox", JBColor(0x14B8A6, 0x14B8A6)),
        GRID("Grid", JBColor(0xF97316, 0xF97316)),
        TRANSFORM("Transform", JBColor(0x4B5563, 0x4B5563)),
        TRANSITION("Transition", JBColor(0xA78BFA, 0xA78BFA)),
        INTERACTIVITY("Interactivity", JBColor(0x06B6D4, 0x06B6D4)),
        ACCESSIBILITY("Accessibility", JBColor(0x64748B, 0x64748B)),
        ANIMATION("Animation", JBColor(0xF43F5E, 0xF43F5E)),
        POSITION("Position", JBColor(0x6366F1, 0x6366F1)),
        OTHER("Other", JBColor(0x6B7280, 0x6B7280))
    }

    private val prefixToCategory = listOf(
        "container" to Category.LAYOUT,
        "block" to Category.LAYOUT,
        "inline" to Category.LAYOUT,
        "flex" to Category.FLEXBOX,
        "grid" to Category.GRID,
        "hidden" to Category.LAYOUT,
        "text-" to Category.TYPOGRAPHY,
        "font-" to Category.TYPOGRAPHY,
        "tracking-" to Category.TYPOGRAPHY,
        "leading-" to Category.TYPOGRAPHY,
        "list-" to Category.TYPOGRAPHY,
        "bg-" to Category.BACKGROUND,
        "bg-opacity-" to Category.BACKGROUND,
        "bg-gradient-" to Category.BACKGROUND,
        "border" to Category.BORDER,
        "border-" to Category.BORDER,
        "rounded" to Category.BORDER,
        "rounded-" to Category.BORDER,
        "shadow" to Category.EFFECTS,
        "shadow-" to Category.EFFECTS,
        "opacity-" to Category.EFFECTS,
        "filter" to Category.EFFECTS,
        "blur-" to Category.EFFECTS,
        "p-" to Category.SPACING,
        "py-" to Category.SPACING,
        "px-" to Category.SPACING,
        "pl-" to Category.SPACING,
        "pr-" to Category.SPACING,
        "pt-" to Category.SPACING,
        "pb-" to Category.SPACING,
        "m-" to Category.SPACING,
        "my-" to Category.SPACING,
        "mx-" to Category.SPACING,
        "ml-" to Category.SPACING,
        "mr-" to Category.SPACING,
        "mt-" to Category.SPACING,
        "mb-" to Category.SPACING,
        "space-" to Category.SPACING,
        "gap-" to Category.SPACING,
        "w-" to Category.SIZING,
        "h-" to Category.SIZING,
        "min-" to Category.SIZING,
        "max-" to Category.SIZING,
        "justify-" to Category.FLEXBOX,
        "items-" to Category.FLEXBOX,
        "flex-" to Category.FLEXBOX,
        "col-" to Category.GRID,
        "row-" to Category.GRID,
        "scale-" to Category.TRANSFORM,
        "rotate-" to Category.TRANSFORM,
        "translate-" to Category.TRANSFORM,
        "transform" to Category.TRANSFORM,
        "transition" to Category.TRANSITION,
        "duration-" to Category.TRANSITION,
        "ease-" to Category.TRANSITION,
        "delay-" to Category.TRANSITION,
        "animate-" to Category.ANIMATION,
        "cursor-" to Category.INTERACTIVITY,
        "pointer-" to Category.INTERACTIVITY,
        "resize" to Category.INTERACTIVITY,
        "select-" to Category.INTERACTIVITY,
        "focus" to Category.INTERACTIVITY,
        "hover" to Category.INTERACTIVITY,
        "sr-" to Category.ACCESSIBILITY,
        "not-sr-" to Category.ACCESSIBILITY,
        "static" to Category.POSITION,
        "relative" to Category.POSITION,
        "absolute" to Category.POSITION,
        "fixed" to Category.POSITION,
        "sticky" to Category.POSITION,
        "top-" to Category.POSITION,
        "right-" to Category.POSITION,
        "bottom-" to Category.POSITION,
        "left-" to Category.POSITION,
        "inset-" to Category.POSITION,
        "z-" to Category.POSITION,
        // New prefixes for v1–v4 expansions
        "ring" to Category.EFFECTS,
        "ring-" to Category.EFFECTS,
        "divide" to Category.BORDER,
        "divide-" to Category.BORDER,
        "outline" to Category.EFFECTS,
        "outline-" to Category.EFFECTS,
        "placeholder-" to Category.TYPOGRAPHY,
        "mix-blend-" to Category.EFFECTS,
        "bg-gradient-" to Category.BACKGROUND,
        "from-" to Category.BACKGROUND,
        "to-" to Category.BACKGROUND,
        // Accent and caret color utilities
        "accent-" to Category.INTERACTIVITY,
        "caret-" to Category.INTERACTIVITY,
        // Container and container query utilities
        "container-query" to Category.LAYOUT,
        "@container" to Category.LAYOUT
    )

    // Extended Tailwind color palette
    private val tailwindColors = mapOf(
        // Red
        "red-50" to 0xFEF2F2, "red-100" to 0xFEE2E2, "red-200" to 0xFECACA, "red-300" to 0xFCA5A5, "red-400" to 0xF87171,
        "red-500" to 0xEF4444, "red-600" to 0xDC2626, "red-700" to 0xB91C1C, "red-800" to 0x991B1B, "red-900" to 0x7F1D1D,
        // Blue
        "blue-50" to 0xEFF6FF, "blue-100" to 0xDBEAFE, "blue-200" to 0xBFDBFE, "blue-300" to 0x93C5FD, "blue-400" to 0x60A5FA,
        "blue-500" to 0x3B82F6, "blue-600" to 0x2563EB, "blue-700" to 0x1D4ED8, "blue-800" to 0x1E40AF, "blue-900" to 0x1E3A8A,
        // Green
        "green-50" to 0xF0FDF4, "green-100" to 0xDCFCE7, "green-200" to 0xBBF7D0, "green-300" to 0x86EFAC, "green-400" to 0x4ADE80,
        "green-500" to 0x22C55E, "green-600" to 0x16A34A, "green-700" to 0x15803D, "green-800" to 0x166534, "green-900" to 0x14532D,
        // Gray
        "gray-50" to 0xF9FAFB, "gray-100" to 0xF3F4F6, "gray-200" to 0xE5E7EB, "gray-300" to 0xD1D5DB, "gray-400" to 0x9CA3AF,
        "gray-500" to 0x6B7280, "gray-600" to 0x4B5563, "gray-700" to 0x374151, "gray-800" to 0x1F2937, "gray-900" to 0x111827,
        // Yellow
        "yellow-50" to 0xFFFBEB, "yellow-100" to 0xFEF3C7, "yellow-200" to 0xFDE68A, "yellow-300" to 0xFCD34D, "yellow-400" to 0xFB923C,
        "yellow-500" to 0xF59E0B, "yellow-600" to 0xD97706, "yellow-700" to 0xB45309, "yellow-800" to 0x92400E, "yellow-900" to 0x78350F,
        // Indigo
        "indigo-50" to 0xEEF2FF, "indigo-100" to 0xE0E7FF, "indigo-200" to 0xC7D2FE, "indigo-300" to 0xA5B4FC, "indigo-400" to 0x818CF8,
        "indigo-500" to 0x6366F1, "indigo-600" to 0x4F46E5, "indigo-700" to 0x4338CA, "indigo-800" to 0x3730A3, "indigo-900" to 0x312E81,
        // Purple
        "purple-50" to 0xF5F3FF, "purple-100" to 0xEDE9FE, "purple-200" to 0xDDD6FE, "purple-300" to 0xC4B5FD, "purple-400" to 0xA78BFA,
        "purple-500" to 0x8B5CF6, "purple-600" to 0x7C3AED, "purple-700" to 0x6D28D9, "purple-800" to 0x5B21B6, "purple-900" to 0x4C1D95,
        // Pink
        "pink-50" to 0xFDF2F8, "pink-100" to 0xFCE7F3, "pink-200" to 0xFBCFE8, "pink-300" to 0xF9A8D4, "pink-400" to 0xF472B6,
        "pink-500" to 0xEC4899, "pink-600" to 0xDB2777, "pink-700" to 0xBE185D, "pink-800" to 0x9D174D, "pink-900" to 0x831843,
        // Teal
        "teal-50" to 0xF0FDFA, "teal-100" to 0xCCFBF1, "teal-200" to 0x99F6E4, "teal-300" to 0x5EEAD4, "teal-400" to 0x2DD4BF,
        "teal-500" to 0x14B8A6, "teal-600" to 0x0D9488, "teal-700" to 0x0F766E, "teal-800" to 0x115E59, "teal-900" to 0x134E4A,
        // Orange
        "orange-50" to 0xFFF7ED, "orange-100" to 0xFFEDD5, "orange-200" to 0xFED7AA, "orange-300" to 0xFDBA74, "orange-400" to 0xFB923C,
        "orange-500" to 0xF97316, "orange-600" to 0xEA580C, "orange-700" to 0xC2410C, "orange-800" to 0x9A3412, "orange-900" to 0x7C2D12,
        // Cyan
        "cyan-50" to 0xECFEFF, "cyan-100" to 0xCFFAFE, "cyan-200" to 0xA5F3FC, "cyan-300" to 0x67E8F9, "cyan-400" to 0x22D3EE,
        "cyan-500" to 0x06B6D4, "cyan-600" to 0x0891B2, "cyan-700" to 0x0E7490, "cyan-800" to 0x155E75, "cyan-900" to 0x164E63
    )

    // Direct mapping for specific class names that don't follow the standard pattern
    private val directColorMapping = mapOf(
        "bg-white" to 0xFFFFFF,
        "bg-black" to 0x000000,
        "text-white" to 0xFFFFFF,
        "text-black" to 0x000000,
        "border-white" to 0xFFFFFF,
        "border-black" to 0x000000
    )
    
    private fun extractTailwindColor(className: String): JBColor? {
        // Remove variants (hover:, focus:, etc.)
        val base = className.split(":").last()
        // Remove opacity/alpha (e.g. bg-red-500/50)
        val colorPart = base.substringBefore("/")
        
        // Check direct mapping first (exact matches like bg-white, text-black)
        val directMatch = directColorMapping[colorPart] 
        if (directMatch != null) {
            // Direct match found
            return createJBColorFromHex(directMatch)
        }
        
        // Handle common colors with explicit short names (bg-red, text-blue, etc.)
        val basicColorRegex = Regex("(bg|text|border|ring|shadow|divide|outline|accent|caret|fill|stroke)-([a-z]+)$")
        if (colorPart.matches(basicColorRegex)) {
            val parts = colorPart.split("-")
            if (parts.size >= 2) {
                val colorName = parts[1]
                // Try with default shade of 500
                val defaultShade = "$colorName-500"
                tailwindColors[defaultShade]?.let { rgb ->
                    return createJBColorFromHex(rgb)
                }
                // Try with alternate shade of 600 if 500 isn't found
                val alternateShade = "$colorName-600"
                tailwindColors[alternateShade]?.let { rgb ->
                    return createJBColorFromHex(rgb)
                }
                // Try with 400 as another fallback
                val fallbackShade = "$colorName-400"
                tailwindColors[fallbackShade]?.let { rgb ->
                    return createJBColorFromHex(rgb)
                }
            }
        }
        
        // Match standard patterns like bg-red-500, text-blue-700, border-green-300
        val standardRegex = Regex("(bg|text|border|ring|shadow|divide|outline|accent|caret|fill|stroke)-([a-z0-9-]+)-(\\d{2,3})")
        val standardMatch = standardRegex.find(colorPart)
        
        if (standardMatch != null) {
            val colorKey = "${standardMatch.groupValues[2]}-${standardMatch.groupValues[3]}"
            tailwindColors[colorKey]?.let { rgb ->
                return createJBColorFromHex(rgb)
            }
        }
        
        // Try to find the closest match by looking at the base color name if available
        val anyColorRegex = Regex("(bg|text|border|ring|shadow|divide|outline|accent|caret|fill|stroke)-([a-z0-9-]+)")
        val anyMatch = anyColorRegex.find(colorPart) 
        if (anyMatch != null && anyMatch.groupValues.size > 2) {
            val typeAndColor = anyMatch.groupValues[2]
            // Look for any entry in tailwindColors that starts with this color name
            val matchingColor = tailwindColors.entries.firstOrNull { (key, _) ->
                key.startsWith(typeAndColor)
            }
            matchingColor?.let { (_, rgb) ->
                return createJBColorFromHex(rgb)
            }
        }
        
        // Also handle color-specific utilities without prefix like red-500, blue-300, etc.
        val colorOnlyRegex = Regex("([a-z]+)-(\\d{2,3})$")
        val colorOnlyMatch = colorOnlyRegex.find(colorPart)
        if (colorOnlyMatch != null) {
            val colorKey = colorPart // e.g. "red-500"
            tailwindColors[colorKey]?.let { rgb ->
                return createJBColorFromHex(rgb)
            }
        }
        
        return null
    }
    
    /**
     * Creates a JBColor from a hexadecimal color value, ensuring it works in both light and dark themes
     */
    private fun createJBColorFromHex(hex: Int): JBColor {
        // Simpler approach: just use the integer value directly for both light and dark themes
        return JBColor(hex, hex)
    }

    fun getCategoryAndColor(className: String): Pair<String, JBColor> {
        val color = extractTailwindColor(className)
        for ((prefix, category) in prefixToCategory) {
            if (className == prefix || className.startsWith(prefix)) {
                return category.displayName to (color ?: category.color)
            }
        }
        return Category.OTHER.displayName to (color ?: Category.OTHER.color)
    }
}
