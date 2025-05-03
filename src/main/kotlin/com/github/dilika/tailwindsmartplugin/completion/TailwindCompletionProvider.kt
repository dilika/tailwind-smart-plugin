package com.github.dilika.tailwindsmartplugin.completion

import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.ui.JBColor
import com.intellij.util.ui.ColorIcon
import org.json.JSONObject
import java.awt.Color
import javax.swing.Icon

class TailwindCompletionProvider : CompletionProvider<CompletionParameters>() {
    private val logger = Logger.getInstance("TailwindCompletionProvider")

    // Caches for analyzed classes information
    private val classInfoCache = mutableMapOf<String, JSONObject>()


    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        resultSet: CompletionResultSet
    ) {
        // Obtain context elements
        val psiElement = parameters.position
        val parent = psiElement.parent

        logger.info("Completion requested for element: ${psiElement.text}, class: ${psiElement.javaClass.name}")
        logger.info("Parent: ${parent?.javaClass?.name}, node type: ${parent?.node?.elementType}")

        // Verify if we are in class attribute
        var isClassAttribute = false

        //More flexible verification to detect attribute class
        try {
            // Approche 1: Verification by direct element parent
            val parentText = parent?.parent?.firstChild?.text
            logger.info("Approche 1 - Parent text: $parentText")
            if (parentText == "class" || parentText == "className") {
                isClassAttribute = true
            }

            // Approche 2: Verification by XmlAttributeValue
            if (parent is XmlAttributeValue) {
                val xmlAttribute = parent.parent
                val xmlAttributeName = xmlAttribute?.firstChild?.text
                logger.info("Approche 2 - Xml attribute name: $xmlAttributeName")
                if (xmlAttributeName == "class" || xmlAttributeName == "className") {
                    isClassAttribute = true
                }
            }

            // Approche 3: More wide context search
            var currentParent = parent
            for (i in 0 until 5) { // Fixed Limit to 5 to avoid infinite loop
                currentParent = currentParent?.parent
                val currentText = currentParent?.firstChild?.text
                logger.info("Approche 3 - Level $i : ${currentParent?.javaClass?.name} - text: $currentText")
                if (currentText == "class" || currentText == "className") {
                    isClassAttribute = true
                    break
                }

            }
            logger.info("Attributes classes detection results: $isClassAttribute")
        } catch (e: Exception) {
            logger.error("Error during attributes classes detection: ${e.message}")
        }

        // If we're not in the attribute class, do not continue
        if (!isClassAttribute) {
            logger.info("Not in class attribute, skipping completion")
            return
        }

        // Obtain current projet
        val project = parameters.originalFile.project
        logger.info("Project detected: ${project.name}")

        // Retrieve project-specific tailwind classes
        val classes = TailwindUtils.getTailwindClasses(project)
        logger.info("Tailwind classes detected: ${classes.size}")

        val tailwindData = TailwindUtils.getTailwindClassData(project)
        logger.info("Tailwind data loaded: ${tailwindData.size}")

        // Always propose all tailwind classes, without filter by prefix
        classes.forEach { className ->
            val element = LookupElementBuilder.create(className)
            resultSet.addElement(LookupElementBuilder.create(className))
        }
        return
    }

    /**
     *Create a rich element suggestion for tailwind classes
     */
    private fun creatLookupElement(className: String, tailwindData: Map<String, JSONObject>): LookupElement {
        // Retrieve or analyze class information
        val classInfo = tailwindData[className] ?: classInfoCache.getOrPut(className) {
            // Defautl info if class not find
            JSONObject().apply {
                put("type", "Utility")
                put("icon", "●")
                put("color", "#64748b")
                put("description", "Utility class")
            }
        }

        // Extract infos for a completion element
        val completionInfo = classInfo.optJSONObject("completion") ?: JSONObject()
        val type = completionInfo.optString("type", "Utility")
        val displayText = completionInfo.optString("displayText", "$className")
        val styleString = completionInfo.optString("style", "")

        // Extract color from style (format: "color: #XXXXXX;")
        val colorString = styleString.replace("color:", "").replace(";", "").trim()
        val color = parseColor(colorString)

        // Determine the class type for visualization
        val isTextColorClass = className.startsWith("text-")
        val isBgColorClass = className.startsWith("bg-")
        val isBorderColorClass = className.startsWith("border-")
        val isOtherColorClass = colorString != "#64748b" && !(isTextColorClass || isBgColorClass || isBorderColorClass)
        val isColorClass = isTextColorClass || isBgColorClass || isBorderColorClass || isOtherColorClass

        // Choose the appropriate size for the icon
        val iconSize = if (isColorClass) 16 else 12

        // Create the appropriate icon by class type
        val icon = when {
            isTextColorClass -> {
                // For text classes (text-*) create the icone "T" with the color
                TextColorIcon(iconSize, color, "T")
            }

            isBgColorClass -> {
                // For the background maintain the plain square
                ColorIcon(iconSize, color)
            }

            isBorderColorClass -> {
                // For border classes, create a square just with border colored
                BorderColorIcon(iconSize, color)
            }

            isOtherColorClass -> {
                // For other class bound to colors (like fill-*, stroke-*, etc.)
                GradientColorIcon(iconSize, color)
            }

            else -> {
                // For the class not bound to color
                ColorIcon(iconSize, parseColor(colorString))
            }
        }

        // create a special presentation for a color class
        val tailText = if (isColorClass) {
            "(${colorString})" // Add hexadecimal for reference
        } else {
            val docInfo = classInfo.optJSONObject("documentation") ?: JSONObject()
            " (${docInfo.optString("description", "Tailwind Css class")})"
        }

        // Construct a completion element with rich information
        val builder = LookupElementBuilder.create(className)
            .withPresentableText(displayText)
            .withIcon(icon)
            .withTypeText(StringUtil.capitalize(type), true)
            .withTailText(tailText, true)
            .withCaseSensitivity(true)
            .withBoldness(isColorClass) // Put in Bold color classes

        // prioritize color class with a slight difference depending on the type
        val priority = when {
            isTextColorClass -> 102.0
            isBgColorClass -> 101.0
            isBorderColorClass -> 100.0
            isOtherColorClass -> 99.0
            else -> 80.0
        }

        // Use PrioritizedLookupElement to give the priority to color classes
        return PrioritizedLookupElement.withPriority(builder, priority)
    }

    /**
     * Analyze a String color to objet Color
     */
    private fun parseColor(colorString: String): Color {
        return try {
            if (colorString.startsWith("#")) {
                // Hexadecimal Format (#RRGGBB or #RGB)
                Color.decode(colorString)
            } else {
                // Default color if unknow
                JBColor(Color(100, 116, 139), Color(100, 116, 139)) // slate-500
            }
        } catch (e: Exception) {
            logger.warn("Impossible d'analyser la couleur: $colorString")
            JBColor(Color(100, 116, 139), Color(100, 116, 139)) // slate-500
        }
    }

    /**
     * Icon for color classes text (text-*)
     */
    private class TextColorIcon(size: Int, private val color: Color, private val text: String = "T") : Icon {
        private val size = size

        override fun paintIcon(c: java.awt.Component, g: java.awt.Graphics, x: Int, y: Int) {
            val g2d = g.create() as java.awt.Graphics2D
            try {
                g2d.setRenderingHint(
                    java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON
                )
                g2d.color = color
                g2d.font = java.awt.Font("Dialog", java.awt.Font.BOLD, size * 3 / 4)
                val metrics = g2d.fontMetrics
                val textWidth = metrics.stringWidth(text)
                val textHeight = metrics.height
                g2d.drawString(text, x + (size - textWidth) / 2, y + (size + textHeight) / 2 - metrics.descent)
            } finally {
                g2d.dispose()
            }
        }

        override fun getIconWidth(): Int = size
        override fun getIconHeight(): Int = size
    }

    /**
     * Icon for color classes border (border-*)
     */
    private class BorderColorIcon(private val size: Int, private val color: Color) : Icon {
        private val borderWidth = 2

        override fun paintIcon(c: java.awt.Component, g: java.awt.Graphics, x: Int, y: Int) {
            val g2d = g.create() as java.awt.Graphics2D
            try {
                g2d.setRenderingHint(
                    java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON
                )
                g2d.color = color
                g2d.stroke = java.awt.BasicStroke(borderWidth.toFloat())
                g2d.drawRect(x + borderWidth / 2, y + borderWidth / 2, size - borderWidth, size - borderWidth)
            } finally {
                g2d.dispose()
            }
        }

        override fun getIconWidth(): Int = size
        override fun getIconHeight(): Int = size
    }

    /**
     * Icon for other classes bound to color
     */
    private class GradientColorIcon(size: Int, private val color: Color) : Icon {
        private val size = size

        override fun paintIcon(c: java.awt.Component, g: java.awt.Graphics, x: Int, y: Int) {
            val g2d = g.create() as java.awt.Graphics2D
            try {
                g2d.setRenderingHint(
                    java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON
                )

                // Create a gradient from the color until a light version
                val lighterColor = JBColor(
                    Color(
                        255.coerceAtMost(color.red + 60),
                        255.coerceAtMost(color.green + 60),
                        255.coerceAtMost(color.blue + 60)
                    ),
                    Color(
                        Math.min(255, color.red + 30),
                        Math.min(255, color.green + 30),
                        Math.min(255, color.blue + 30)
                    )
                )

                val gradient = java.awt.GradientPaint(
                    x.toFloat(), y.toFloat(), color,
                    (x + size).toFloat(), (y + size).toFloat(), lighterColor
                )

                g2d.paint = gradient
                g2d.fillOval(x, y, size, size)
            } finally {
                g2d.dispose()
            }
        }

        override fun getIconWidth(): Int = size
        override fun getIconHeight(): Int = size
    }
}
