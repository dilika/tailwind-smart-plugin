package com.github.dilika.tailwindsmartplugin.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.ui.components.JBCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import java.awt.GridLayout

/**
 * Configurable for Tailwind CSS Smart Plugin settings.
 */
class TailwindSettingsConfigurable : Configurable {
    private var settings = TailwindPluginSettings.getInstance()
    
    // UI components
    private var showIconsCheckBox: JBCheckBox? = null
    private var useRoundedIconsCheckBox: JBCheckBox? = null
    
    override fun getDisplayName(): String = "Tailwind CSS Smart Plugin"
    
    override fun createComponent(): JComponent {
        showIconsCheckBox = JBCheckBox("Show icons in completion popup", settings.showIcons)
        useRoundedIconsCheckBox = JBCheckBox("Use rounded icons for color classes", settings.useRoundedColorIcons)
        
        val panel = JPanel(GridLayout(0, 1))
        panel.add(showIconsCheckBox)
        panel.add(useRoundedIconsCheckBox)
        return panel
    }
    
    override fun isModified(): Boolean {
        return showIconsCheckBox?.isSelected != settings.showIcons ||
               useRoundedIconsCheckBox?.isSelected != settings.useRoundedColorIcons
    }
    
    @Throws(ConfigurationException::class)
    override fun apply() {
        showIconsCheckBox?.let { settings.showIcons = it.isSelected }
        useRoundedIconsCheckBox?.let { settings.useRoundedColorIcons = it.isSelected }
    }
    
    override fun reset() {
        showIconsCheckBox?.isSelected = settings.showIcons
        useRoundedIconsCheckBox?.isSelected = settings.useRoundedColorIcons
    }
    
    override fun disposeUIResources() {
        showIconsCheckBox = null
        useRoundedIconsCheckBox = null
    }
}
