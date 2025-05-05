package com.github.dilika.tailwindsmartplugin.completion

import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.ui.JBColor
import com.intellij.util.ProcessingContext
import com.intellij.util.ui.ColorIcon
import org.json.JSONObject
import javax.swing.Icon

/**
 * Fournit des suggestions d'autocomplétion pour les classes Tailwind CSS
 */
class TailwindCompletionProvider : CompletionProvider<CompletionParameters>() {
    private val logger = Logger.getInstance("TailwindCompletionProvider")
    
    // Cache pour les données des classes Tailwind
    private val classInfoCache = mutableMapOf<String, JSONObject>()
    private val colorCache = mutableMapOf<String, JBColor>()
    private val iconCache = mutableMapOf<String, Icon>()
    private val lookupElementCache = mutableMapOf<String, LookupElement>()

    // Liste de classes Tailwind v4 pour l'auto-complétion
    private val tailwindV4Classes = listOf(
        "bg-primary", "text-primary", "border-primary",
        "bg-secondary", "text-secondary", "border-secondary",
        "p-1", "p-2", "p-3", "p-4",
        "m-1", "m-2", "m-3", "m-4",
        "text-xs", "text-sm", "text-base", "text-lg", "text-xl",
        "rounded-sm", "rounded", "rounded-md", "rounded-lg",
        "shadow-xs", "shadow-sm", "shadow", "shadow-md", "shadow-lg",
        "flex", "flex-row", "flex-col", "flex-wrap", "flex-nowrap",
        "grid", "grid-cols-1", "grid-cols-2", "grid-cols-3",
        "animate-bounce", "animate-pulse", "animate-spin"
    )

    // Catégories pour une meilleure organisation
    private val categoryMapping = mapOf(
        // Couleurs et arrière-plans
        "bg-" to "Background",
        "from-" to "Gradient",
        "to-" to "Gradient",
        "via-" to "Gradient",
        
        // Typographie
        "text-" to "Typography",
        "font-" to "Typography",
        "tracking-" to "Typography",
        "leading-" to "Typography",
        
        // Bordures
        "border-" to "Borders",
        "rounded-" to "Borders",
        "outline-" to "Borders",
        "ring-" to "Borders",
        
        // Espacement
        "p-" to "Spacing",
        "m-" to "Spacing",
        "gap-" to "Spacing",
        "space-" to "Spacing",
        
        // Effets
        "shadow-" to "Effects",
        "opacity-" to "Effects",
        "blur-" to "Effects",
        
        // Layout et positionnement
        "flex" to "Layout",
        "grid" to "Layout",
        "absolute" to "Position",
        "relative" to "Position",
        "fixed" to "Position",
        "sticky" to "Position",
        "inset-" to "Position",
        "z-" to "Position",
        
        // Flexbox spécifique
        "items-" to "Flexbox",
        "justify-" to "Flexbox",
        "content-" to "Flexbox",
        "self-" to "Flexbox",
        "place-" to "Flexbox",
        
        // Dimensionnement
        "w-" to "Sizing",
        "h-" to "Sizing",
        "min-" to "Sizing",
        "max-" to "Sizing",
        
        // Transformations
        "translate-" to "Transform",
        "rotate-" to "Transform",
        "scale-" to "Transform",
        "skew-" to "Transform",
        "transform-" to "Transform",
        
        // Animations
        "animate-" to "Animation",
        "transition-" to "Animation",
        "duration-" to "Animation",
        "delay-" to "Animation",
        "ease-" to "Animation"
    )

    private val tailwindBaseColorMap = mapOf(
        "slate" to 0x64748B,
        "gray" to 0x6B7280,
        "zinc" to 0x71717A,
        "neutral" to 0x737373,
        "stone" to 0x78716C,
        "red" to 0xEF4444,
        "orange" to 0xF97316,
        "amber" to 0xF59E0B,
        "yellow" to 0xEAB308,
        "lime" to 0x84CC16,
        "green" to 0x22C55E,
        "emerald" to 0x10B981,
        "teal" to 0x14B8A6,
        "cyan" to 0x06B6D4,
        "sky" to 0x0EA5E9,
        "blue" to 0x3B82F6,
        "indigo" to 0x6366F1,
        "violet" to 0x8B5CF6,
        "purple" to 0xA855F7,
        "fuchsia" to 0xD946EF,
        "pink" to 0xEC4899,
        "rose" to 0xF43F5E,
        "white" to 0xFFFFFF,
        "black" to 0x000000,
        "transparent" to 0xFFFFFF,
        "current" to 0x000000
    )

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        resultSet: CompletionResultSet
    ) {
        // Optimisé : Détection rapide de l'attribut de classe
        if (!isClassAttribute(parameters.position)) {
            return
        }

        // Obtenir le projet actuel
        val project = parameters.originalFile.project
        
        // Récupérer TOUTES les classes Tailwind (v1-v4) du projet
        val allTailwindClasses = TailwindUtils.getTailwindClasses(project)
        
        // Récupérer les données des classes Tailwind
        val tailwindData = TailwindUtils.getTailwindClassData(project)
        
        // Ajouter toutes les classes Tailwind avec mise en cache pour optimiser les performances
        allTailwindClasses.forEach { className ->
            val element = lookupElementCache[className] ?: createLookupElementWithCache(className, tailwindData).also { 
                lookupElementCache[className] = it 
            }
            resultSet.addElement(element)
        }
    }

    /**
     * Vérification optimisée pour détecter si nous sommes dans un attribut de classe
     */
    private fun isClassAttribute(element: Any): Boolean {
        val parent = when (element) {
            is XmlAttributeValue -> element.parent
            else -> (element as? com.intellij.psi.PsiElement)?.parent
        } ?: return false

        // Vérification de l'attribut de classe de manière efficace
        val attributeName = parent.firstChild?.text
        if (attributeName == "class" || attributeName == "className") {
            return true
        }
        
        // Vérification pour XmlAttributeValue
        if (parent.parent != null) {
            val grandParentAttributeName = parent.parent?.firstChild?.text
            if (grandParentAttributeName == "class" || grandParentAttributeName == "className") {
                return true
            }
        }
        
        return false
    }

    /**
     * Création d'un élément de complétion avec mise en cache pour de meilleures performances
     */
    private fun createLookupElementWithCache(className: String, tailwindData: Map<String, JSONObject>): LookupElement {
        // Utilisation du cache pour éviter de recréer les mêmes éléments
        return iconCache[className]?.let { cachedIcon ->
            LookupElementBuilder.create(className)
                .withIcon(cachedIcon)
                .withTypeText(getCategoryForClass(className), true)
                .withTailText(" ${getDescriptionForClass(className, tailwindData)}", true)
                .withBoldness(true)
                .withCaseSensitivity(false)
        } ?: createNewLookupElement(className, tailwindData)
    }

    /**
     * Création d'un nouvel élément de complétion avec stockage dans le cache
     */
    private fun createNewLookupElement(className: String, tailwindData: Map<String, JSONObject>): LookupElement {
        val description = getDescriptionForClass(className, tailwindData)
        val category = getCategoryForClass(className)
        val info = getClassInfo(className, tailwindData)
        
        // Déterminer le type d'icône et la couleur en fonction de la classe
        val (iconType, colorValue) = when {
            // === TYPOGRAPHY ===
            className.startsWith("text-") -> {
                if (extractColorName(className) != null) {
                    // Classes de couleur de texte
                    val color = extractColor(className, info) ?: JBColor(0xEF4444, 0xEF4444) // Rouge par défaut
                    Pair("Typography", color)
                } else {
                    // Classes de taille de texte
                    Pair("Typography", JBColor(0x8B5CF6, 0x8B5CF6)) // Violet pour taille de texte
                }
            }
            className.startsWith("font-") -> Pair("Typography", JBColor(0x9333EA, 0x9333EA))
            className.startsWith("tracking-") -> Pair("Typography", JBColor(0x8B5CF6, 0x8B5CF6))
            className.startsWith("leading-") -> Pair("Typography", JBColor(0xA855F7, 0xA855F7))
            className.startsWith("whitespace-") -> Pair("Typography", JBColor(0x8B5CF6, 0x8B5CF6))
            className.startsWith("break-") -> Pair("Typography", JBColor(0x8B5CF6, 0x8B5CF6))
            
            // === BACKGROUND ===
            className.startsWith("bg-") -> {
                val color = extractColor(className, info) ?: JBColor(0x3B82F6, 0x3B82F6) // Bleu par défaut
                Pair("Background", color)
            }
            className.contains("from-") || className.contains("to-") || className.contains("via-") -> {
                val color = extractColor(className, info) ?: JBColor(0x3B82F6, 0x3B82F6) // Bleu par défaut
                Pair("Background", color)
            }
            
            // === BORDERS ===
            className.startsWith("border") -> {
                // Gérer border, border-t, border-r, border-b, border-l, border-color-X...
                val colorMatch = Regex("border(-[trbl])?-([a-z]+)(-\\d+)?").find(className)
                val color = if (colorMatch != null) {
                    extractColor(className, info) ?: JBColor(0x6B7280, 0x6B7280) // Gris foncé
                } else {
                    JBColor(0x6B7280, 0x6B7280) // Gris foncé par défaut
                }
                Pair("Borders", color)
            }
            className.startsWith("divide-") -> Pair("Borders", JBColor(0x6B7280, 0x6B7280))
            className.startsWith("ring-") -> Pair("Borders", JBColor(0x6B7280, 0x6B7280))
            className.startsWith("outline-") -> Pair("Borders", JBColor(0x6B7280, 0x6B7280))
            
            // === ROUNDED ===
            className.startsWith("rounded") -> Pair("Rounded", JBColor(0x8B5CF6, 0x8B5CF6))
            
            // === SPACING ===
            className.startsWith("p") && (className == "p" || className.matches(Regex("p-.*")) || 
                    className.matches(Regex("p[txyrblh]-.*"))) -> Pair("Spacing", JBColor(0x10B981, 0x10B981)) // Vert
            className.startsWith("m") && (className == "m" || className.matches(Regex("m-.*")) || 
                    className.matches(Regex("m[txyrblh]-.*"))) -> Pair("Spacing", JBColor(0x059669, 0x059669)) // Vert foncé
            className.startsWith("space-") -> Pair("Spacing", JBColor(0x10B981, 0x10B981))
            className.startsWith("gap-") -> Pair("Spacing", JBColor(0x10B981, 0x10B981))
            
            // === SIZING ===
            className.startsWith("w-") -> Pair("Sizing", JBColor(0xF59E0B, 0xF59E0B)) // Orange
            className.startsWith("h-") -> Pair("Sizing", JBColor(0xF59E0B, 0xF59E0B)) // Orange
            className.startsWith("min-") -> Pair("Sizing", JBColor(0xF59E0B, 0xF59E0B)) // Orange
            className.startsWith("max-") -> Pair("Sizing", JBColor(0xF59E0B, 0xF59E0B)) // Orange
            className.startsWith("size-") -> Pair("Sizing", JBColor(0xF59E0B, 0xF59E0B)) // Orange
            
            // === LAYOUT ===
            className.startsWith("container") -> Pair("Layout", JBColor(0xD946EF, 0xD946EF)) // Magenta
            className == "grid" || className.startsWith("grid-") -> Pair("Layout", JBColor(0xD946EF, 0xD946EF)) // Magenta
            className.startsWith("col-") -> Pair("Layout", JBColor(0xD946EF, 0xD946EF)) // Magenta
            className.startsWith("row-") -> Pair("Layout", JBColor(0xD946EF, 0xD946EF)) // Magenta
            
            // === FLEXBOX ===
            className == "flex" || className.startsWith("flex-") -> Pair("Flexbox", JBColor(0xEC4899, 0xEC4899)) // Rose
            className.startsWith("justify-") -> Pair("Flexbox", JBColor(0xEC4899, 0xEC4899)) // Rose
            className.startsWith("items-") -> Pair("Flexbox", JBColor(0xEC4899, 0xEC4899)) // Rose
            className.startsWith("content-") -> Pair("Flexbox", JBColor(0xEC4899, 0xEC4899)) // Rose
            className.startsWith("self-") -> Pair("Flexbox", JBColor(0xEC4899, 0xEC4899)) // Rose
            className.startsWith("place-") -> Pair("Flexbox", JBColor(0xEC4899, 0xEC4899)) // Rose
            className.startsWith("order-") -> Pair("Flexbox", JBColor(0xEC4899, 0xEC4899)) // Rose
            
            // === POSITION ===
            className == "static" || className == "relative" || className == "absolute" || 
            className == "fixed" || className == "sticky" -> Pair("Position", JBColor(0x6366F1, 0x6366F1)) // Indigo
            className.startsWith("top-") -> Pair("Position", JBColor(0x6366F1, 0x6366F1)) // Indigo
            className.startsWith("right-") -> Pair("Position", JBColor(0x6366F1, 0x6366F1)) // Indigo
            className.startsWith("bottom-") -> Pair("Position", JBColor(0x6366F1, 0x6366F1)) // Indigo
            className.startsWith("left-") -> Pair("Position", JBColor(0x6366F1, 0x6366F1)) // Indigo
            className.startsWith("inset-") -> Pair("Position", JBColor(0x6366F1, 0x6366F1)) // Indigo
            className.startsWith("z-") -> Pair("Position", JBColor(0x6366F1, 0x6366F1)) // Indigo
            
            // === EFFECTS ===
            className.startsWith("shadow") -> Pair("Effects", JBColor(0x64748B, 0x64748B)) // Slate
            className.startsWith("opacity-") -> Pair("Effects", JBColor(0x64748B, 0x64748B)) // Slate
            className.startsWith("mix-blend-") -> Pair("Effects", JBColor(0x64748B, 0x64748B)) // Slate
            className.startsWith("bg-blend-") -> Pair("Effects", JBColor(0x64748B, 0x64748B)) // Slate
            className.startsWith("blur") -> Pair("Effects", JBColor(0x64748B, 0x64748B)) // Slate
            className.startsWith("brightness-") -> Pair("Effects", JBColor(0x64748B, 0x64748B)) // Slate
            className.startsWith("contrast-") -> Pair("Effects", JBColor(0x64748B, 0x64748B)) // Slate
            className.startsWith("drop-shadow") -> Pair("Effects", JBColor(0x64748B, 0x64748B)) // Slate
            className.startsWith("grayscale") -> Pair("Effects", JBColor(0x64748B, 0x64748B)) // Slate
            className.startsWith("hue-rotate") -> Pair("Effects", JBColor(0x64748B, 0x64748B)) // Slate
            className.startsWith("invert") -> Pair("Effects", JBColor(0x64748B, 0x64748B)) // Slate
            className.startsWith("saturate-") -> Pair("Effects", JBColor(0x64748B, 0x64748B)) // Slate
            className.startsWith("sepia") -> Pair("Effects", JBColor(0x64748B, 0x64748B)) // Slate
            className.startsWith("backdrop-") -> Pair("Effects", JBColor(0x64748B, 0x64748B)) // Slate
            
            // === ANIMATION ===
            className.startsWith("animate-") -> Pair("Animation", JBColor(0xF43F5E, 0xF43F5E)) // Rose vif
            className.startsWith("transition") -> Pair("Animation", JBColor(0xF43F5E, 0xF43F5E)) // Rose vif
            className.startsWith("duration-") -> Pair("Animation", JBColor(0xF43F5E, 0xF43F5E)) // Rose vif
            className.startsWith("ease-") -> Pair("Animation", JBColor(0xF43F5E, 0xF43F5E)) // Rose vif
            className.startsWith("delay-") -> Pair("Animation", JBColor(0xF43F5E, 0xF43F5E)) // Rose vif
            
            // === TRANSFORM ===
            className == "transform" -> Pair("Transform", JBColor(0x0EA5E9, 0x0EA5E9)) // Bleu ciel
            className.startsWith("scale-") -> Pair("Transform", JBColor(0x0EA5E9, 0x0EA5E9)) // Bleu ciel
            className.startsWith("rotate-") -> Pair("Transform", JBColor(0x0EA5E9, 0x0EA5E9)) // Bleu ciel
            className.startsWith("translate-") -> Pair("Transform", JBColor(0x0EA5E9, 0x0EA5E9)) // Bleu ciel
            className.startsWith("skew-") -> Pair("Transform", JBColor(0x0EA5E9, 0x0EA5E9)) // Bleu ciel
            className.startsWith("origin-") -> Pair("Transform", JBColor(0x0EA5E9, 0x0EA5E9)) // Bleu ciel
            
            // === CURSOR & INTERACTION ===
            className.startsWith("cursor-") -> Pair("Cursor", JBColor(0x22C55E, 0x22C55E)) // Vert vif
            className.startsWith("pointer-events-") -> Pair("Interaction", JBColor(0x22C55E, 0x22C55E)) // Vert vif
            className.startsWith("resize") -> Pair("Interaction", JBColor(0x22C55E, 0x22C55E)) // Vert vif
            className.startsWith("select-") -> Pair("Interaction", JBColor(0x22C55E, 0x22C55E)) // Vert vif
            className.startsWith("user-") -> Pair("Interaction", JBColor(0x22C55E, 0x22C55E)) // Vert vif
            
            // === DIVERS & DISPLAY ===
            className == "hidden" || className == "block" || className == "inline" || className == "inline-block" ||
            className == "table" || className == "contents" || className.startsWith("float-") ||
            className.startsWith("clear-") || className.startsWith("object-") ||
            className.startsWith("overflow") || className.startsWith("overscroll") -> {
                Pair("Layout", JBColor(0x7C3AED, 0x7C3AED)) // Violet
            }
            
            // Par défaut pour les autres classes
            else -> Pair("Other", JBColor(0x94A3B8, 0x94A3B8)) // Gris clair pour les autres classes
        }
        
        // Créer l'icône personnalisée
        val icon = createTailwindIcon(iconType, colorValue)
        
        // Mise en cache de l'icône pour une utilisation future
        iconCache[className] = icon
        
        return LookupElementBuilder.create(className)
            .withIcon(icon)
            .withTypeText(category, true)
            .withTailText(" $description", true)
            .withBoldness(true)
            .withCaseSensitivity(false)
    }

    /**
     * Obtention efficace de la catégorie pour une classe donnée
     */
    private fun getCategoryForClass(className: String): String {
        return categoryMapping.entries.find { className.startsWith(it.key) }?.value ?: "Other"
    }

    /**
     * Obtention efficace de la description pour une classe donnée
     */
    private fun getDescriptionForClass(className: String, tailwindData: Map<String, JSONObject>): String {
        return tailwindData[className]?.optString("description", "Tailwind CSS class") ?: "Tailwind CSS class"
    }

    /**
     * Récupération des informations de classe avec mise en cache
     */
    private fun getClassInfo(className: String, tailwindData: Map<String, JSONObject>): JSONObject {
        return tailwindData[className] ?: classInfoCache.getOrPut(className) {
            JSONObject().apply {
                put("type", "Utility")
                put("icon", "●")
                put("color", "#64748b")
                put("description", "Tailwind CSS class")
            }
        }
    }

    /**
     * Extraction optimisée de la couleur avec mise en cache
     */
    private fun extractColor(className: String, info: JSONObject): JBColor? {
        // Vérifier d'abord dans le cache
        colorCache[className]?.let { return it }
        
        // 1. Vérifier via les métadonnées Tailwind JSON
        if (info.has("color")) {
            val colorStr = info.getString("color")
            if (colorStr.isNotEmpty()) {
                val color = parseColor(colorStr)
                colorCache[className] = color
                return color
            }
        }
        
        // 2. Fallback : analyser le nom de couleur via regex (extractColorName)
        extractColorName(className)?.let { colorKey ->
            tailwindBaseColorMap[colorKey]?.let { intColor ->
                val shade = getShadeForClassName(className)
                val baseCol = java.awt.Color(intColor)
                val finalColor = shade?.let { adjustShade(baseCol, it) } ?: baseCol
                val jb = JBColor(finalColor.rgb, finalColor.rgb)
                colorCache[className] = jb
                return jb
            }
        }
        
        return null
    }

    /**
     * Crée une icône personnalisée pour un type de classe spécifique
     */
    private fun createTailwindIcon(type: String, color: JBColor): Icon {
        // Création d'une icône personnalisée selon le type de classe
        return object : Icon {
            override fun paintIcon(c: java.awt.Component, g: java.awt.Graphics, x: Int, y: Int) {
                val g2d = g.create() as java.awt.Graphics2D
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
                
                // Couleur de fond/texte en fonction du type
                val bgColor = color
                val textColor = if (isDarkColor(bgColor)) JBColor.WHITE else JBColor.BLACK
                
                when (type) {
                    "Typography" -> {
                        // T pour Typography (sans carré de fond)
                        g2d.color = bgColor // couleur de la classe
                        g2d.font = java.awt.Font("Dialog", java.awt.Font.BOLD, 12)
                        g2d.drawString("T", x + 4, y + 12)
                    }
                    "Background" -> {
                        // Carré rempli pour background
                        g2d.color = bgColor
                        g2d.fillRect(x, y, 16, 16)
                    }
                    "Borders" -> {
                        // Carré avec bordure pour border
                        g2d.color = java.awt.Color.WHITE
                        g2d.fillRect(x, y, 16, 16)
                        g2d.color = bgColor
                        g2d.drawRect(x, y, 15, 15)
                        g2d.drawRect(x + 1, y + 1, 13, 13)
                    }
                    "Rounded" -> {
                        // Cercle pour rounded
                        g2d.color = bgColor
                        g2d.fillOval(x, y, 16, 16)
                    }
                    "Spacing" -> {
                        // Flèches bidirectionnelles pour spacing
                        g2d.color = bgColor
                        g2d.fillRoundRect(x, y, 16, 16, 3, 3)  // fond arrondi
                        g2d.color = textColor
                        // Flèches horizontales
                        g2d.drawLine(x + 3, y + 8, x + 13, y + 8)
                        g2d.drawLine(x + 3, y + 8, x + 6, y + 5)
                        g2d.drawLine(x + 3, y + 8, x + 6, y + 11)
                        g2d.drawLine(x + 13, y + 8, x + 10, y + 5)
                        g2d.drawLine(x + 13, y + 8, x + 10, y + 11)
                    }
                    "Sizing" -> {
                        // Rectangle redimensionnable pour sizing
                        g2d.color = bgColor
                        g2d.fillRoundRect(x, y, 16, 16, 3, 3)  // fond arrondi
                        g2d.color = textColor
                        g2d.drawRect(x + 4, y + 4, 8, 8)
                        g2d.fillRect(x + 12, y + 12, 3, 3) // poignée de redimensionnement
                    }
                    "Layout" -> {
                        // Grille pour layout
                        g2d.color = bgColor
                        g2d.fillRoundRect(x, y, 16, 16, 3, 3)  // fond arrondi
                        g2d.color = textColor
                        g2d.drawLine(x + 4, y + 4, x + 4, y + 12) // ligne verticale
                        g2d.drawLine(x + 8, y + 4, x + 8, y + 12) // ligne verticale
                        g2d.drawLine(x + 12, y + 4, x + 12, y + 12) // ligne verticale
                        g2d.drawLine(x + 4, y + 4, x + 12, y + 4) // ligne horizontale
                        g2d.drawLine(x + 4, y + 8, x + 12, y + 8) // ligne horizontale
                        g2d.drawLine(x + 4, y + 12, x + 12, y + 12) // ligne horizontale
                    }
                    "Flexbox" -> {
                        // Boites flexibles pour flexbox
                        g2d.color = bgColor
                        g2d.fillRoundRect(x, y, 16, 16, 3, 3)  // fond arrondi
                        g2d.color = textColor
                        g2d.fillRect(x + 3, y + 6, 3, 4) // petit rectangle
                        g2d.fillRect(x + 7, y + 6, 3, 4) // petit rectangle
                        g2d.fillRect(x + 11, y + 6, 3, 4) // petit rectangle
                    }
                    "Position" -> {
                        // Repère de position
                        g2d.color = bgColor
                        g2d.fillRoundRect(x, y, 16, 16, 3, 3)  // fond arrondi
                        g2d.color = textColor
                        g2d.drawLine(x + 8, y + 3, x + 8, y + 13) // ligne verticale
                        g2d.drawLine(x + 3, y + 8, x + 13, y + 8) // ligne horizontale
                    }
                    "Effects" -> {
                        // Effet lumineux pour shadow, opacity, etc.
                        g2d.color = bgColor
                        g2d.fillRoundRect(x, y, 16, 16, 3, 3)  // fond arrondi
                        g2d.color = textColor
                        g2d.fillOval(x + 7, y + 7, 2, 2) // point central
                        g2d.drawOval(x + 4, y + 4, 8, 8) // cercle
                    }
                    "Animation" -> {
                        // Symbole de lecture pour animation
                        g2d.color = bgColor
                        g2d.fillRoundRect(x, y, 16, 16, 3, 3)  // fond arrondi
                        g2d.color = textColor
                        // Triangle de lecture
                        val xPoints = intArrayOf(x + 6, x + 12, x + 6)
                        val yPoints = intArrayOf(y + 4, y + 8, y + 12)
                        g2d.fillPolygon(xPoints, yPoints, 3)
                    }
                    "Transform" -> {
                        // Flèche courbe pour transform
                        g2d.color = bgColor
                        g2d.fillRoundRect(x, y, 16, 16, 3, 3)  // fond arrondi
                        g2d.color = textColor
                        g2d.drawArc(x + 2, y + 2, 12, 12, 0, 270)
                        g2d.drawLine(x + 11, y + 2, x + 14, y + 2)
                        g2d.drawLine(x + 11, y + 2, x + 11, y + 5)
                    }
                    "Cursor" -> {
                        // Pointeur de souris pour cursor
                        g2d.color = bgColor
                        g2d.fillRoundRect(x, y, 16, 16, 3, 3)  // fond arrondi
                        g2d.color = textColor
                        // Dessiner le pointeur
                        val xPoints = intArrayOf(x + 5, x + 10, x + 7, x + 12)
                        val yPoints = intArrayOf(y + 4, y + 9, y + 9, y + 12)
                        g2d.drawPolyline(xPoints, yPoints, 4)
                    }
                    "Interaction" -> {
                        // Main qui pointe pour pointer-events, etc.
                        g2d.color = bgColor
                        g2d.fillRoundRect(x, y, 16, 16, 3, 3)  // fond arrondi
                        g2d.color = textColor
                        g2d.drawString("i", x + 7, y + 12)
                    }
                    else -> {
                        // Icône par défaut pour autres types
                        g2d.color = bgColor
                        g2d.fillRoundRect(x, y, 16, 16, 3, 3) // carré arrondi coloré
                    }
                }
                
                g2d.dispose()
            }
            
            override fun getIconWidth(): Int = 16
            override fun getIconHeight(): Int = 16
        }
    }
    
    /**
     * Détermine si une couleur est foncée pour choisir le texte en contraste
     */
    private fun isDarkColor(color: JBColor): Boolean {
        val rgb = color.getRGB()
        val r = (rgb shr 16) and 0xFF
        val g = (rgb shr 8) and 0xFF
        val b = rgb and 0xFF
        
        // Calcul de la luminosité (formule YIQ)
        val brightness = (r * 299 + g * 587 + b * 114) / 1000
        return brightness < 128
    }
    
    /**
     * Icône par défaut pour les classes sans couleur spécifique
     */
    private fun getDefaultIcon(): Icon {
        return createTailwindIcon("Other", JBColor(0x94A3B8, 0x94A3B8))
    }

    private fun getShadeForClassName(className: String): Int? {
        val parts = className.split("-")
        val last = parts.lastOrNull() ?: return null
        return if (last.all { it.isDigit() }) last.toInt() else null
    }
    
    private fun adjustShade(baseColor: java.awt.Color, shade: Int): java.awt.Color {
        // Approximative lightening/darkening based on shade value
        val percent = when (shade) {
            50 -> 45
            100 -> 40
            200 -> 32
            300 -> 22
            400 -> 12
            500 -> 0
            600 -> -8
            700 -> -16
            800 -> -24
            900 -> -32
            else -> 0
        }
        return if (percent >= 0) lightenColor(baseColor, percent) else darkenColor(baseColor, -percent)
    }
    
    private fun lightenColor(color: java.awt.Color, percent: Int): java.awt.Color {
        val r = color.red
        val g = color.green
        val b = color.blue
        val p = percent / 100.0
        val nr = (r + ((255 - r) * p)).toInt().coerceIn(0, 255)
        val ng = (g + ((255 - g) * p)).toInt().coerceIn(0, 255)
        val nb = (b + ((255 - b) * p)).toInt().coerceIn(0, 255)
        return java.awt.Color(nr, ng, nb)
    }
    
    private fun darkenColor(color: java.awt.Color, percent: Int): java.awt.Color {
        val r = color.red
        val g = color.green
        val b = color.blue
        val p = percent / 100.0
        val nr = (r * (1 - p)).toInt().coerceIn(0, 255)
        val ng = (g * (1 - p)).toInt().coerceIn(0, 255)
        val nb = (b * (1 - p)).toInt().coerceIn(0, 255)
        return java.awt.Color(nr, ng, nb)
    }

    /**
     * Extrait le nom de couleur d'une classe Tailwind
     * Par exemple: border-gray-300 -> gray, text-blue-500 -> blue
     */
    private fun extractColorName(className: String): String? {
        // Regex pour capturer les noms de couleurs dans différentes structures de classes
        val patterns = listOf(
            Regex("(bg|text|border|ring|outline|shadow|divide|from|to|via)-([a-z]+)(?:-\\d+)?"),  // bg-blue-500, text-red-700, etc.
            Regex("(ring|outline|shadow)-([a-z]+)(?:-\\d+)?"),   // ring-blue, shadow-indigo, etc.
            Regex("(fill|stroke)-([a-z]+)(?:-\\d+)?")           // fill-amber, stroke-lime, etc.
        )
        
        for (pattern in patterns) {
            val match = pattern.find(className)
            if (match != null && match.groupValues.size > 2) {
                val colorName = match.groupValues[2]
                // Vérifier si c'est vraiment une couleur et non un autre mot-clé
                if (isColorName(colorName)) {
                    return colorName
                }
            }
        }
        
        return null
    }
    
    /**
     * Vérifie si le nom est une couleur Tailwind valide
     */
    private fun isColorName(name: String): Boolean {
        // Liste des couleurs standard de Tailwind
        val tailwindColors = setOf(
            "slate", "gray", "zinc", "neutral", "stone", "red", "orange", "amber", 
            "yellow", "lime", "green", "emerald", "teal", "cyan", "sky", "blue", 
            "indigo", "violet", "purple", "fuchsia", "pink", "rose",
            "white", "black", "transparent", "current"
        )
        
        return tailwindColors.contains(name)
    }
    
    /**
     * Analyse d'une couleur au format chaîne vers un objet JBColor
     */
    private fun parseColor(colorString: String): JBColor {
        return try {
            if (colorString.startsWith("#")) {
                val hex = colorString.substring(1)
                val rgb = hex.toLong(16).toInt()
                JBColor(rgb, rgb)
            } else {
                JBColor.GRAY
            }
        } catch (e: Exception) {
            JBColor.GRAY
        }
    }
}
