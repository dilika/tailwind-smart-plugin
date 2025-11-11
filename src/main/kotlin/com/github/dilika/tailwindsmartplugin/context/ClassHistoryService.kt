package com.github.dilika.tailwindsmartplugin.context

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Service pour suivre l'historique des classes utilisées et suggérer basé sur l'historique
 */
@Service(Service.Level.PROJECT)
class ClassHistoryService(private val project: Project) {
    
    private val logger = Logger.getInstance(ClassHistoryService::class.java)
    
    companion object {
        fun getInstance(project: Project): ClassHistoryService {
            return project.getService(ClassHistoryService::class.java)
        }
        
        private const val MAX_HISTORY_SIZE = 1000
    }
    
    // Historique des classes utilisées (FIFO)
    private val classHistory = ConcurrentLinkedQueue<String>()
    
    // Compteur de fréquence des classes
    private val classFrequency = mutableMapOf<String, Int>()
    
    // Historique des combinaisons de classes
    private val classCombinations = mutableMapOf<String, MutableList<String>>()
    
    /**
     * Enregistre l'utilisation d'une classe
     */
    fun recordClassUsage(className: String) {
        if (className.isBlank()) return
        
        // Ajouter à l'historique
        classHistory.offer(className)
        if (classHistory.size > MAX_HISTORY_SIZE) {
            val removed = classHistory.poll()
            // Décrémenter la fréquence de la classe retirée
            classFrequency[removed] = (classFrequency[removed] ?: 1) - 1
        }
        
        // Incrémenter la fréquence
        classFrequency[className] = (classFrequency[className] ?: 0) + 1
    }
    
    /**
     * Enregistre l'utilisation d'une combinaison de classes
     */
    fun recordClassCombination(classes: List<String>) {
        if (classes.size < 2) return
        
        val sortedClasses = classes.sorted()
        val key = sortedClasses.first()
        
        val combinations = classCombinations.getOrPut(key) { mutableListOf() }
        val combination = sortedClasses.drop(1).joinToString(" ")
        
        if (!combinations.contains(combination)) {
            combinations.add(combination)
            // Limiter à 20 combinaisons par classe
            if (combinations.size > 20) {
                combinations.removeAt(0)
            }
        }
    }
    
    /**
     * Obtient les classes les plus fréquemment utilisées
     */
    fun getMostUsedClasses(limit: Int = 10): List<String> {
        return classFrequency.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    }
    
    /**
     * Obtient les suggestions basées sur l'historique pour un préfixe
     */
    fun getHistoricalSuggestions(prefix: String, limit: Int = 5): List<String> {
        return classFrequency.entries
            .filter { it.key.startsWith(prefix) }
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    }
    
    /**
     * Obtient les combinaisons de classes suggérées pour une classe donnée
     */
    fun getSuggestedCombinations(className: String): List<String> {
        return classCombinations[className]?.take(5) ?: emptyList()
    }
    
    /**
     * Obtient les statistiques d'utilisation
     */
    fun getUsageStats(): UsageStats {
        return UsageStats(
            totalClassesUsed = classFrequency.size,
            mostUsedClasses = getMostUsedClasses(10),
            historySize = classHistory.size
        )
    }
    
    /**
     * Nettoie l'historique
     */
    fun clearHistory() {
        classHistory.clear()
        classFrequency.clear()
        classCombinations.clear()
        logger.info("Class history cleared for project: ${project.name}")
    }
}

/**
 * Statistiques d'utilisation
 */
data class UsageStats(
    val totalClassesUsed: Int,
    val mostUsedClasses: List<String>,
    val historySize: Int
)

