package com.github.dilika.tailwindsmartplugin.license

import com.intellij.openapi.ui.Messages
import com.intellij.openapi.diagnostic.Logger

/**
 * Utilitaire pour gérer l'accès aux fonctionnalités premium
 */
object PremiumFeatureGate {
    
    private val logger = Logger.getInstance(PremiumFeatureGate::class.java)
    private val licenseService = TailwindLicenseService.getInstance()
    
    /**
     * Vérifie si une fonctionnalité premium est accessible
     * 
     * @param featureName Nom de la fonctionnalité (pour les messages)
     * @param showUpgradeDialog Si true, affiche un dialog d'upgrade si non premium
     * @return true si premium/trial actif, false sinon
     */
    fun checkPremiumAccess(
        featureName: String,
        showUpgradeDialog: Boolean = true
    ): Boolean {
        val isPremium = licenseService.isPremiumActive()
        
        if (!isPremium && showUpgradeDialog) {
            showUpgradeDialog(featureName)
        }
        
        return isPremium
    }
    
    /**
     * Exécute une action premium avec vérification
     * 
     * @param featureName Nom de la fonctionnalité
     * @param action Action à exécuter si premium
     * @return true si l'action a été exécutée, false sinon
     */
    fun <T> executeIfPremium(
        featureName: String,
        action: () -> T
    ): T? {
        return if (checkPremiumAccess(featureName)) {
            try {
                action()
            } catch (e: Exception) {
                logger.warn("Error executing premium feature $featureName: ${e.message}", e)
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Affiche un dialog d'upgrade
     */
    private fun showUpgradeDialog(featureName: String) {
        val trialDays = licenseService.getTrialDaysRemaining()
        val message = if (trialDays > 0) {
            "You have $trialDays days remaining in your trial.\n\n" +
            "Upgrade to Premium to unlock: $featureName"
        } else {
            "This is a Premium feature: $featureName\n\n" +
            "Upgrade to Premium to unlock:\n" +
            "• Premium icon system with real colors\n" +
            "• Advanced validation with multiple quick fixes\n" +
            "• Visual preview with dark mode & breakpoints\n" +
            "• Intelligent completion with history learning\n" +
            "• Refactoring & code audit tools\n" +
            "• And much more!"
        }
        
        val options = if (trialDays == 0) {
            arrayOf("Start 7-Day Trial", "Upgrade to Premium", "Cancel")
        } else {
            arrayOf("Upgrade to Premium", "Cancel")
        }
        
        val result = Messages.showDialog(
            message,
            "Premium Feature",
            options,
            0,
            Messages.getInformationIcon()
        )
        
        when (result) {
            0 -> {
                if (trialDays == 0) {
                    // Démarrer le trial
                    licenseService.startTrial()
                    Messages.showInfoMessage(
                        "Trial started! You have 7 days to try all premium features.",
                        "Trial Activated"
                    )
                } else {
                    // Upgrade
                    openUpgradePage()
                }
            }
            1 -> {
                if (trialDays == 0) {
                    // Upgrade depuis le dialog avec trial
                    openUpgradePage()
                }
                // Sinon c'est Cancel
            }
        }
    }
    
    /**
     * Ouvre la page d'upgrade
     */
    private fun openUpgradePage() {
        val url = licenseService.getUpgradeUrl()
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI(url))
        } catch (e: Exception) {
            logger.warn("Error opening upgrade URL: ${e.message}", e)
            Messages.showErrorDialog(
                "Could not open upgrade page. Please visit:\n$url",
                "Error"
            )
        }
    }
    
    /**
     * Vérifie si premium sans afficher de dialog
     */
    fun isPremium(): Boolean {
        return licenseService.isPremiumActive()
    }
    
    /**
     * Obtient le message de limitation pour une fonctionnalité
     */
    fun getLimitationMessage(featureName: String): String {
        val trialDays = licenseService.getTrialDaysRemaining()
        return if (trialDays > 0) {
            "Premium feature (Trial: $trialDays days left)"
        } else {
            "Premium feature - Upgrade to unlock"
        }
    }
}

