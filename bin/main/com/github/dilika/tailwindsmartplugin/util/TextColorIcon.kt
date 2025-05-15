package com.github.dilika.tailwindsmartplugin.util

import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.Icon

/**
 * Icône spécifique pour les classes de texte (text- et font-)
 * Affiche une lettre T dans la couleur spécifiée, sans fond coloré
 */
class TextColorIcon(private val color: Color, private val size: Int = 14) : Icon {
    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size

    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        // Couleur du T directement celle de la classe
        g2d.color = color
        
        // Police légèrement plus grande que d'habitude pour meilleure visibilité
        g2d.font = Font("Dialog", Font.BOLD, size - 2)
        val fm = g2d.fontMetrics
        val str = "T"
        val tx = x + (size - fm.stringWidth(str)) / 2
        val ty = y + (size + fm.ascent - fm.descent) / 2 - 1
        
        // Dessiner le T directement avec la couleur
        g2d.drawString(str, tx, ty)
        
        g2d.dispose()
    }
}
