package com.github.dilika.tailwindsmartplugin.utils

import com.intellij.ui.JBColor
import java.awt.Color
import javax.swing.Icon

/**
 * Icon that displays a colored square
 */
class ColorIcon(private val size: Int, private val color: Color) : Icon {
    override fun paintIcon(c: java.awt.Component, g: java.awt.Graphics, x: Int, y: Int) {
        val g2d = g.create() as java.awt.Graphics2D
        try {
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
            g2d.color = color
            g2d.fillRect(x, y, size, size)
            
            // Add a subtle border if the color is very light
            val brightness = (color.red * 0.299 + color.green * 0.587 + color.blue * 0.114) / 255
            if (brightness > 0.85) {
                g2d.color = JBColor(Color(200, 200, 200), Color(100, 100, 100))
                g2d.drawRect(x, y, size - 1, size - 1)
            }
        } finally {
            g2d.dispose()
        }
    }

    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size
}
