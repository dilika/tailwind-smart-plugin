package com.github.dilika.tailwindsmartplugin.settings

import com.github.dilika.tailwindsmartplugin.license.TailwindLicenseService
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JButton
import javax.swing.BoxLayout
import javax.swing.BorderFactory
import java.awt.FlowLayout
import java.awt.Desktop
import java.net.URI

/**
 * Configurable for Tailwind CSS Smart Plugin settings.
 */
class TailwindSettingsConfigurable : Configurable {
    private var settings = TailwindPluginSettings.getInstance()
    private val licenseService = TailwindLicenseService.getInstance()
    
    // UI components
    private var showIconsCheckBox: JBCheckBox? = null
    private var useRoundedIconsCheckBox: JBCheckBox? = null
    
    // License UI components
    private var licenseStatusLabel: JBLabel? = null
    private var trialDaysLabel: JBLabel? = null
    private var startTrialButton: JButton? = null
    private var upgradeButton: JButton? = null
    
    override fun getDisplayName(): String = "Tailwind CSS Smart Plugin"
    
    override fun createComponent(): JComponent {
        showIconsCheckBox = JBCheckBox("Show icons in completion popup", settings.showIcons)
        useRoundedIconsCheckBox = JBCheckBox("Use rounded icons for color classes", settings.useRoundedColorIcons)
        
        // License section
        val licensePanel = createLicensePanel()
        
        // Main panel with vertical layout
        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        mainPanel.border = JBUI.Borders.empty(10)
        
        // Icon settings section
        val iconSettingsPanel = JPanel()
        iconSettingsPanel.layout = BoxLayout(iconSettingsPanel, BoxLayout.Y_AXIS)
        iconSettingsPanel.border = BorderFactory.createTitledBorder("Icon Settings")
        iconSettingsPanel.add(showIconsCheckBox)
        iconSettingsPanel.add(useRoundedIconsCheckBox)
        
        mainPanel.add(iconSettingsPanel)
        mainPanel.add(JPanel().apply { preferredSize = java.awt.Dimension(0, 10) }) // Spacer
        mainPanel.add(licensePanel)
        
        return mainPanel
    }
    
    /**
     * Creates the license management panel
     */
    private fun createLicensePanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createTitledBorder("License & Premium")
        
        // Status label
        licenseStatusLabel = JBLabel()
        updateLicenseStatus()
        if (licenseStatusLabel != null) {
            panel.add(licenseStatusLabel as JComponent)
        }
        
        // Trial days label (only if in trial)
        trialDaysLabel = JBLabel()
        updateTrialDays()
        if (licenseService.isTrialActive() && trialDaysLabel != null) {
            panel.add(trialDaysLabel as JComponent)
        }
        
        // Buttons panel
        val buttonsPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        
        // Start trial button (only if not premium and not in trial)
        if (!licenseService.isPremiumActive() && !licenseService.isTrialActive()) {
            startTrialButton = JButton("Start 7-Day Free Trial")
            startTrialButton?.addActionListener {
                if (licenseService.startTrial()) {
                    Messages.showInfoMessage(
                        "Trial started! You have 7 days to try all premium features.",
                        "Trial Activated"
                    )
                    updateLicenseStatus()
                    updateTrialDays()
                    // Show trial days label
                    if (licenseService.isTrialActive() && trialDaysLabel != null) {
                        panel.add(trialDaysLabel as JComponent)
                        panel.revalidate()
                        panel.repaint()
                    }
                } else {
                    Messages.showErrorDialog(
                        "Unable to start trial. You may already have an active trial or premium license.",
                        "Error"
                    )
                }
            }
            if (startTrialButton != null) {
                buttonsPanel.add(startTrialButton as JComponent)
            }
        }
        
        // Upgrade button (always show if not premium)
        if (!licenseService.isPremiumActive()) {
            upgradeButton = JButton("Upgrade to Premium")
            upgradeButton?.addActionListener {
                try {
                    val url = licenseService.getUpgradeUrl()
                    Desktop.getDesktop().browse(URI(url))
                } catch (e: Exception) {
                    Messages.showErrorDialog(
                        "Could not open upgrade page. Please visit:\n${licenseService.getUpgradeUrl()}",
                        "Error"
                    )
                }
            }
            if (upgradeButton != null) {
                buttonsPanel.add(upgradeButton as JComponent)
            }
        }
        
        panel.add(buttonsPanel)
        
        // Premium features list (always show)
        val featuresLabel = JBLabel("<html><b>Premium Features:</b><br/>" +
                "• Premium icon system with real colors<br/>" +
                "• Advanced validation (5 quick fixes)<br/>" +
                "• Visual preview with dark mode & breakpoints<br/>" +
                "• Intelligent completion with history learning<br/>" +
                "• Enhanced documentation with examples<br/>" +
                "• Refactoring & code audit tools<br/>" +
                "• And much more!</html>")
        featuresLabel.border = JBUI.Borders.empty(10, 0)
        panel.add(featuresLabel)
        
        return panel
    }
    
    /**
     * Updates the license status label
     */
    private fun updateLicenseStatus() {
        val status = when {
            licenseService.hasPremiumLicense() -> "Status: <b>Premium Active</b> ✨"
            licenseService.isTrialActive() -> "Status: <b>Trial Active</b> 🎉"
            else -> "Status: <b>Free Version</b>"
        }
        licenseStatusLabel?.text = "<html>$status</html>"
    }
    
    /**
     * Updates the trial days label
     */
    private fun updateTrialDays() {
        val daysRemaining = licenseService.getTrialDaysRemaining()
        if (daysRemaining > 0) {
            trialDaysLabel?.text = "<html>Trial expires in <b>$daysRemaining day${if (daysRemaining > 1) "s" else ""}</b></html>"
            trialDaysLabel?.foreground = java.awt.Color(0x3b82f6) // Blue
        } else {
            trialDaysLabel?.text = ""
        }
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
        updateLicenseStatus()
        updateTrialDays()
    }
    
    override fun disposeUIResources() {
        showIconsCheckBox = null
        useRoundedIconsCheckBox = null
        licenseStatusLabel = null
        trialDaysLabel = null
        startTrialButton = null
        upgradeButton = null
    }
}
