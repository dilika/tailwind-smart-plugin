package com.github.dilika.tailwindsmartplugin.util

import com.intellij.ui.JBColor
import javax.swing.Icon
import java.awt.*

/**
 * Icône personnalisée qui représente visuellement une catégorie de classe Tailwind
 * L'icône utilise la couleur fournie et dessine un symbole spécifique à la catégorie
 */
class TailwindCategoryIcon(private val category: String, private val color: JBColor, private val size: Int = 14) : Icon {
    // Garantir une taille minimale pour la lisibilité
    override fun getIconWidth() = size
    override fun getIconHeight() = size

    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val bgColor = color
        // Couleur de texte contrastée pour la lisibilité
        val textColor = if (isDark(bgColor)) JBColor.WHITE else JBColor.BLACK
        when (category) {
            "Typography" -> {
                // Fond de couleur légère
                g2d.color = bgColor
                g2d.fillRoundRect(x, y, size, size, 4, 4)
                
                // Lettre T en contraste
                g2d.color = textColor
                g2d.font = Font("Dialog", Font.BOLD, size - 4)
                val fm = g2d.fontMetrics
                val str = "T"
                val tx = x + (size - fm.stringWidth(str)) / 2
                val ty = y + (size + fm.ascent - fm.descent) / 2 - 1
                g2d.drawString(str, tx, ty)
            }
            "Background" -> {
                // Rectangle rempli avec la couleur exacte de la classe
                g2d.color = bgColor
                g2d.fillRoundRect(x, y, size, size, 4, 4)
            }
            "Border" -> {
                // Rectangle avec bordure distincte de la couleur exacte
                val borderWidth = size / 7
                
                // Fond clair
                g2d.color = JBColor(Color.WHITE, Color(50, 50, 50))
                g2d.fillRoundRect(x, y, size, size, 4, 4)
                
                // Bordure de la couleur de la classe
                g2d.color = bgColor
                g2d.setStroke(BasicStroke(borderWidth.toFloat()))
                g2d.drawRoundRect(x + borderWidth/2, y + borderWidth/2, 
                                 size - borderWidth, size - borderWidth, 2, 2)
            }
            "Rounded" -> {
                // Cercle parfait de la couleur exacte
                g2d.color = bgColor
                g2d.fillOval(x, y, size, size)
            }
            "Flexbox" -> {
                // Fond arrondi avec la couleur exacte
                g2d.color = bgColor.brighter()
                g2d.fillRoundRect(x, y, size, size, 4, 4)
                
                // Barres flex distinctives
                g2d.color = bgColor
                val barW = size / 4
                val gap = size / 10
                for (i in 0..2) {
                    g2d.fillRoundRect(x + gap + i * (barW + gap), y + size / 3, barW, size / 3, 2, 2)
                }
            }
            "Grid" -> {
                // Fond avec la couleur exacte mais plus légère
                g2d.color = bgColor.brighter()
                g2d.fillRoundRect(x, y, size, size, 4, 4)
                
                // Grille claire représentant la structure grid
                g2d.color = bgColor
                val cellSize = size / 3
                val padding = size / 15
                
                // Dessiner une grille 2x2 avec des cellules remplies
                for (i in 0..1) {
                    for (j in 0..1) {
                        g2d.fillRoundRect(
                            x + padding + i * (cellSize + padding),
                            y + padding + j * (cellSize + padding),
                            cellSize, cellSize, 2, 2
                        )
                    }
                }
            }
            "Animation" -> {
                g2d.color = bgColor
                g2d.fillRoundRect(x, y, size, size, 3, 3)
                g2d.color = textColor
                val xPoints = intArrayOf(x + size/3, x + 2*size/3, x + size/3)
                val yPoints = intArrayOf(y + size/4, y + size/2, y + 3*size/4)
                g2d.fillPolygon(xPoints, yPoints, 3)
            }
            "Effects" -> {
                g2d.color = bgColor
                g2d.fillRoundRect(x, y, size, size, 5, 5)
                g2d.color = textColor
                g2d.drawString("fx", x + 2, y + size - 3)
            }
            "Spacing" -> {
                // Fond arrondi avec la couleur exacte mais plus légère
                g2d.color = bgColor.brighter()
                g2d.fillRoundRect(x, y, size, size, 4, 4)
                
                // Flèches qui s'étendent vers l'extérieur pour représenter le spacing
                g2d.color = bgColor
                val arrowSize = size / 5
                val center = size / 2
                g2d.strokeWidth = 1.5f
                
                // Dessiner quatre flèches pointant vers l'extérieur
                drawArrow(g2d, x + center, y + center, x + center, y + arrowSize, arrowSize)
                drawArrow(g2d, x + center, y + center, x + center, y + size - arrowSize, arrowSize)
                drawArrow(g2d, x + center, y + center, x + arrowSize, y + center, arrowSize)
                drawArrow(g2d, x + center, y + center, x + size - arrowSize, y + center, arrowSize)
            }
            "Sizing" -> {
                // Fond avec couleur exacte mais plus légère
                g2d.color = bgColor.brighter()
                g2d.fillRoundRect(x, y, size, size, 4, 4)
                
                // Indicateur de taille avec flèches bidirectionnelles
                g2d.color = bgColor
                g2d.strokeWidth = 1.5f
                val arrowSize = size / 5
                val margin = size / 6
                
                // Ligne horizontale avec flèches aux deux extrémités
                g2d.drawLine(x + margin, y + size / 2, x + size - margin, y + size / 2)
                drawArrow(g2d, x + margin, y + size / 2, x + margin + arrowSize, y + size / 2, arrowSize/2)
                drawArrow(g2d, x + size - margin, y + size / 2, x + size - margin - arrowSize, y + size / 2, arrowSize/2)
                
                // Ligne verticale avec flèches aux deux extrémités
                g2d.drawLine(x + size / 2, y + margin, x + size / 2, y + size - margin)
                drawArrow(g2d, x + size / 2, y + margin, x + size / 2, y + margin + arrowSize, arrowSize/2)
                drawArrow(g2d, x + size / 2, y + size - margin, x + size / 2, y + size - margin - arrowSize, arrowSize/2)
            }
            "Transform" -> {
                g2d.color = bgColor
                g2d.fillRoundRect(x, y, size, size, 3, 3)
                g2d.color = textColor
                g2d.drawArc(x + 2, y + 2, size - 4, size - 4, 0, 270)
                g2d.drawLine(x + size - 4, y + 2, x + size - 2, y + 2)
                g2d.drawLine(x + size - 4, y + 2, x + size - 4, y + 5)
            }
            "Transition" -> {
                g2d.color = bgColor
                g2d.fillRoundRect(x, y, size, size, 3, 3)
                g2d.color = textColor
                g2d.drawString("tr", x + 2, y + size - 3)
            }
            "Interactivity" -> {
                g2d.color = bgColor
                g2d.fillRoundRect(x, y, size, size, 3, 3)
                g2d.color = textColor
                g2d.drawString("i", x + size/3, y + size - 3)
            }
            "Accessibility" -> {
                g2d.color = bgColor
                g2d.fillOval(x, y, size, size)
                g2d.color = textColor
                g2d.drawString("a", x + size/3, y + size - 3)
            }
            "Position" -> {
                g2d.color = bgColor
                g2d.fillRoundRect(x, y, size, size, 3, 3)
                g2d.color = textColor
                g2d.drawString("p", x + size/3, y + size - 3)
            }
            else -> {
                g2d.color = bgColor
                g2d.fillRoundRect(x, y, size, size, 4, 4)
            }
        }
        g2d.dispose()
    }

    /**
     * Détermine si une couleur est sombre ou claire
     * Utilisé pour choisir une couleur de texte contrastée
     */
    private fun isDark(color: Color): Boolean {
        val brightness = (color.red * 299 + color.green * 587 + color.blue * 114) / 1000
        return brightness < 128
    }
    
    /**
     * Dessine une flèche de x1,y1 vers x2,y2 avec une pointe de taille size
     */
    private fun drawArrow(g2d: Graphics2D, x1: Int, y1: Int, x2: Int, y2: Int, size: Int) {
        val origStroke = g2d.stroke
        g2d.stroke = BasicStroke(1.5f)
        g2d.drawLine(x1, y1, x2, y2)
        
        // Calculer le vecteur de direction
        val dx = x2 - x1
        val dy = y2 - y1
        val length = Math.sqrt(dx * dx + dy * dy.toDouble())
        val udx = dx / length
        val udy = dy / length
        
        // Calculer le vecteur perpendiculaire
        val perpX = -udy
        val perpY = udx
        
        // Dessiner la pointe de flèche
        val arrowSize = size
        
        // Créer un petit triangle rempli pour la pointe de flèche
        val xPoints = intArrayOf(
            x2,
            (x2 - arrowSize * udx + arrowSize/2 * perpX).toInt(),
            (x2 - arrowSize * udx - arrowSize/2 * perpX).toInt()
        )
        
        val yPoints = intArrayOf(
            y2,
            (y2 - arrowSize * udy + arrowSize/2 * perpY).toInt(),
            (y2 - arrowSize * udy - arrowSize/2 * perpY).toInt()
        )
        
        g2d.fillPolygon(xPoints, yPoints, 3)
        g2d.stroke = origStroke
    }
    
    /**
     * Extension permettant de définir l'épaisseur du trait dans Graphics2D
     */
    private var Graphics2D.strokeWidth: Float
        get() = (this.stroke as? BasicStroke)?.lineWidth ?: 1.0f
        set(width) { this.stroke = BasicStroke(width) }
}
