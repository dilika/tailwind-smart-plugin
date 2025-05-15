package com.github.dilika.tailwindsmartplugin.utils

import com.github.dilika.tailwindsmartplugin.util.TailwindCategoryUtils
import java.awt.Color
import java.awt.Component
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.Icon

/**
 * Icône personnalisée qui affiche un dégradé linéaire
 * Parfait pour représenter les classes bg-gradient-to-* de Tailwind
 */
class GradientIcon(
    private val size: Int,
    private val startColor: Color,
    private val endColor: Color,
    private val direction: String = "r" // "r", "l", "t", "b", "tr", "tl", "br", "bl"
) : Icon {

    /**
     * Dessine l'icône avec un dégradé dans la direction spécifiée
     */
    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        // Définir le point de départ et d'arrivée du dégradé selon la direction
        val coords = when (direction) {
            "r" -> listOf(0f, size/2f, size.toFloat(), size/2f)  // Droite
            "l" -> listOf(size.toFloat(), size/2f, 0f, size/2f)  // Gauche
            "t" -> listOf(size/2f, size.toFloat(), size/2f, 0f)  // Haut
            "b" -> listOf(size/2f, 0f, size/2f, size.toFloat())  // Bas
            "tr" -> listOf(0f, size.toFloat(), size.toFloat(), 0f)  // Haut-Droite
            "tl" -> listOf(size.toFloat(), size.toFloat(), 0f, 0f)  // Haut-Gauche
            "br" -> listOf(0f, 0f, size.toFloat(), size.toFloat())  // Bas-Droite
            "bl" -> listOf(size.toFloat(), 0f, 0f, size.toFloat())  // Bas-Gauche
            else -> listOf(0f, size/2f, size.toFloat(), size/2f)  // Par défaut: droite
        }
        
        val x1 = coords[0]
        val y1 = coords[1]
        val x2 = coords[2]
        val y2 = coords[3]
        
        // Créer le dégradé
        val gradientPaint = GradientPaint(
            x.toFloat() + x1, y.toFloat() + y1, startColor,
            x.toFloat() + x2, y.toFloat() + y2, endColor
        )
        
        // Appliquer le dégradé et dessiner
        g2d.paint = gradientPaint
        if (direction.contains("t") || direction.contains("b")) {
            // Arrondir les coins pour les dégradés verticaux
            g2d.fillRoundRect(x, y, size, size, 4, 4)
        } else {
            // Arrondir les coins pour les dégradés horizontaux
            g2d.fillRoundRect(x, y, size, size, 4, 4)
        }
        
        // Nettoyer
        g2d.dispose()
    }

    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size

    companion object {
        /**
         * Crée une icône de dégradé à partir d'une classe Tailwind de type bg-gradient-to-*
         */
        fun fromGradientClass(className: String, size: Int): GradientIcon {
            // Extraire la direction du gradient
            val direction = if (className.startsWith("bg-gradient-to-")) {
                className.removePrefix("bg-gradient-to-")
            } else {
                "r" // Direction par défaut: vers la droite
            }
            
            // Couleurs par défaut pour le dégradé
            val defaultStartColor = Color(0x3B82F6) // blue-500
            val defaultEndColor = Color(0x8B5CF6)   // purple-500
            
            return GradientIcon(size, defaultStartColor, defaultEndColor, direction)
        }
        
        /**
         * Crée une icône de dégradé en combinant les couleurs from-* et to-*
         */
        fun fromColorStops(fromClass: String?, toClass: String?, size: Int): GradientIcon {
            // Couleurs par défaut
            var startColor = Color(0x3B82F6) // blue-500
            var endColor = Color(0x8B5CF6)   // purple-500
            
            // Extraire la couleur 'from' si elle existe
            if (fromClass != null && fromClass.startsWith("from-")) {
                val (_, colorValue) = TailwindCategoryUtils.getCategoryAndColor(fromClass)
                startColor = Color(colorValue.rgb)
            }
            
            // Extraire la couleur 'to' si elle existe
            if (toClass != null && toClass.startsWith("to-")) {
                val (_, colorValue) = TailwindCategoryUtils.getCategoryAndColor(toClass)
                endColor = Color(colorValue.rgb)
            }
            
            return GradientIcon(size, startColor, endColor, "r")
        }
    }
}
