package com.github.dilika.tailwindsmartplugin.util

import com.intellij.ui.JBColor
import javax.swing.Icon
import java.awt.*

class TailwindCategoryIcon(private val category: String, private val color: JBColor, private val size: Int = 14) : Icon {
    // No init needed
    override fun getIconWidth() = size
    override fun getIconHeight() = size

    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val bgColor = color
        // Use bgColor for filling shapes
        val textColor = if (isDark(bgColor)) JBColor.WHITE else JBColor.BLACK
        when (category) {
            "Typography" -> {
                g2d.color = textColor
                g2d.font = Font("Dialog", Font.BOLD, size - 4)
                val fm = g2d.fontMetrics
                val str = "T"
                val tx = x + (size - fm.stringWidth(str)) / 2
                val ty = y + (size + fm.ascent - fm.descent) / 2 - 1
                g2d.drawString(str, tx, ty)
            }
            "Background" -> {
                g2d.color = bgColor
                g2d.fillRect(x, y, size, size)
            }
            "Border" -> {
                g2d.color = bgColor
                g2d.fillRect(x, y, size, size)
                // Draw border lines in contrasting color
                g2d.color = if (isDark(bgColor)) JBColor.WHITE else JBColor.BLACK
                g2d.drawRect(x, y, size - 1, size - 1)
                g2d.drawRect(x + 2, y + 2, size - 5, size - 5)
            }
            "Rounded" -> {
                g2d.color = bgColor
                g2d.fillOval(x, y, size, size)
            }
            "Flexbox" -> {
                g2d.color = bgColor
                val barW = size / 4
                val gap = size / 10
                for (i in 0..2) {
                    g2d.fillRect(x + gap + i * (barW + gap), y + size / 3, barW, size / 3)
                }
            }
            "Grid" -> {
                g2d.color = bgColor
                g2d.fillRoundRect(x, y, size, size, 3, 3)
                g2d.color = textColor
                val step = size / 4
                for (i in 1..2) {
                    g2d.drawLine(x + i * step, y, x + i * step, y + size)
                    g2d.drawLine(x, y + i * step, x + size, y + i * step)
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
                g2d.color = bgColor
                g2d.fillRect(x, y + size/3, size, size/3)
            }
            "Sizing" -> {
                g2d.color = bgColor
                g2d.fillRect(x + size/3, y, size/3, size)
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

    private fun isDark(color: JBColor): Boolean {
        val c = Color(color.rgb)
        val brightness = (c.red * 299 + c.green * 587 + c.blue * 114) / 1000
        return brightness < 128
    }
}
