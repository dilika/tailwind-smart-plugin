package com.github.dilika.tailwindsmartplugin.license

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SimpleModificationTracker
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Service de gestion de licence pour le modèle FREEMIUM
 * 
 * Product Code: PTAILWINDCSSSUP
 * 
 * Ce service vérifie si l'utilisateur a une licence premium active
 * et gère les fonctionnalités premium vs free.
 */
@Service(Service.Level.APP)
class TailwindLicenseService : SimpleModificationTracker() {
    
    private val logger = Logger.getInstance(TailwindLicenseService::class.java)
    
    companion object {
        private const val PRODUCT_CODE = "PTAILWINDCSSSUP"
        private const val TRIAL_DAYS = 7
        
        @JvmStatic
        fun getInstance(): TailwindLicenseService {
            return ApplicationManager.getApplication().getService(TailwindLicenseService::class.java)
        }
    }
    
    private var isPremium: Boolean = false
    private var isTrial: Boolean = false
    private var trialEndDate: LocalDate? = null
    private var lastCheckTime: Long = 0
    private val checkInterval = 60 * 60 * 1000L // 1 heure
    
    init {
        loadLicenseState()
        checkLicense()
    }
    
    /**
     * Vérifie si l'utilisateur a une licence premium active (inclut trial)
     */
    fun isPremiumActive(): Boolean {
        // Vérifier périodiquement (toutes les heures)
        val now = System.currentTimeMillis()
        if (now - lastCheckTime > checkInterval) {
            checkLicense()
        }
        
        return isPremium || isTrialActive()
    }
    
    /**
     * Vérifie si l'utilisateur a une licence premium (sans trial)
     */
    fun hasPremiumLicense(): Boolean {
        return isPremium
    }
    
    /**
     * Vérifie si l'utilisateur est en période d'essai
     */
    fun isTrialActive(): Boolean {
        if (!isTrial) return false
        
        val endDate = trialEndDate ?: return false
        val today = LocalDate.now()
        
        if (today.isAfter(endDate)) {
            // Trial expiré
            isTrial = false
            saveLicenseState()
            return false
        }
        
        return true
    }
    
    /**
     * Active la période d'essai de 7 jours
     */
    fun startTrial(): Boolean {
        if (isTrialActive() || isPremium) {
            return false // Déjà en trial ou premium
        }
        
        isTrial = true
        trialEndDate = LocalDate.now().plusDays(TRIAL_DAYS.toLong())
        saveLicenseState()
        incModificationCount()
        
        logger.info("Trial started until $trialEndDate")
        return true
    }
    
    /**
     * Vérifie la licence via JetBrains Marketplace API
     */
    private fun checkLicense() {
        try {
            // TODO: Implémenter la vérification via JetBrains Marketplace API
            // Pour l'instant, on utilise une vérification locale
            
            // Vérifier via JetBrains License API
            // val licenseInfo = JetBrainsMarketplaceAPI.checkLicense(PRODUCT_CODE)
            // isPremium = licenseInfo?.isActive == true
            
            // Pour le développement, on peut simuler avec une propriété système
            val devPremium = System.getProperty("tailwind.premium", "false").toBoolean()
            if (devPremium) {
                isPremium = true
                saveLicenseState()
            }
            
            lastCheckTime = System.currentTimeMillis()
        } catch (e: Exception) {
            logger.warn("Error checking license: ${e.message}", e)
            // En cas d'erreur, on garde l'état précédent
        }
    }
    
    /**
     * Charge l'état de la licence depuis le stockage persistant
     */
    private fun loadLicenseState() {
        try {
            val settings = TailwindLicenseSettings.getInstance()
            isPremium = settings.isPremium
            isTrial = settings.isTrial
            trialEndDate = settings.trialEndDate?.let { LocalDate.parse(it) }
            lastCheckTime = settings.lastCheckTime
        } catch (e: Exception) {
            logger.warn("Error loading license state: ${e.message}", e)
        }
    }
    
    /**
     * Sauvegarde l'état de la licence
     */
    private fun saveLicenseState() {
        try {
            val settings = TailwindLicenseSettings.getInstance()
            settings.isPremium = isPremium
            settings.isTrial = isTrial
            settings.trialEndDate = trialEndDate?.toString()
            settings.lastCheckTime = lastCheckTime
        } catch (e: Exception) {
            logger.warn("Error saving license state: ${e.message}", e)
        }
    }
    
    /**
     * Obtient le nombre de jours restants dans la période d'essai
     */
    fun getTrialDaysRemaining(): Int {
        if (!isTrialActive()) return 0
        
        val endDate = trialEndDate ?: return 0
        val today = LocalDate.now()
        val days = ChronoUnit.DAYS.between(today, endDate).toInt()
        
        return maxOf(0, days)
    }
    
    /**
     * Obtient l'URL pour acheter/upgrader vers premium
     */
    fun getUpgradeUrl(): String {
        return "https://plugins.jetbrains.com/plugin/PTAILWINDCSSSUP"
    }
    
    /**
     * Force une vérification de licence (pour refresh manuel)
     */
    fun refreshLicense() {
        checkLicense()
        incModificationCount()
    }
}

