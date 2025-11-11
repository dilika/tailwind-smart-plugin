package com.github.dilika.tailwindsmartplugin.context

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.Logger
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class TailwindPreferenceLearningService(private val project: Project) {
    
    private val logger = Logger.getInstance(TailwindPreferenceLearningService::class.java)
    
    // Stockage des préférences utilisateur
    private val classUsageCount = ConcurrentHashMap<String, Int>()
    private val contextPreferences = ConcurrentHashMap<String, MutableList<String>>()
    private val userPatterns = ConcurrentHashMap<String, TailwindPattern>()
    
    /**
     * Enregistre l'utilisation d'une classe Tailwind
     */
    fun trackClassUsage(className: String, context: String) {
        try {
            classUsageCount.merge(className, 1) { old, new -> old + new }
            
            contextPreferences.computeIfAbsent(context) { mutableListOf() }
                .add(className)
            
            logger.debug("Tracked usage: $className in context: $context")
        } catch (e: Exception) {
            logger.warn("Error tracking class usage", e)
        }
    }
    
    /**
     * Obtient les classes les plus utilisées
     */
    fun getMostUsedClasses(limit: Int = 10): List<String> {
        return classUsageCount.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    }
    
    /**
     * Obtient les préférences pour un contexte donné
     */
    fun getContextPreferences(context: String): List<String> {
        return contextPreferences[context]?.distinct() ?: emptyList()
    }
    
    /**
     * Enregistre un pattern personnalisé de l'utilisateur
     */
    fun saveUserPattern(name: String, pattern: TailwindPattern) {
        userPatterns[name] = pattern
        logger.debug("Saved user pattern: $name")
    }
    
    /**
     * Obtient les patterns personnalisés de l'utilisateur
     */
    fun getUserPatterns(): List<TailwindPattern> {
        return userPatterns.values.toList()
    }
    
    /**
     * Génère des suggestions basées sur les préférences
     */
    fun generatePersonalizedSuggestions(context: String): List<String> {
        val contextPrefs = getContextPreferences(context)
        val mostUsed = getMostUsedClasses(5)
        
        return (contextPrefs + mostUsed).distinct()
    }
    
    /**
     * Obtient les statistiques d'utilisation
     */
    fun getUsageStatistics(): UsageStatistics {
        val totalClasses = classUsageCount.size
        val totalUsages = classUsageCount.values.sum()
        val mostUsedClass = classUsageCount.maxByOrNull { it.value }?.key ?: ""
        
        return UsageStatistics(
            totalClasses = totalClasses,
            totalUsages = totalUsages,
            mostUsedClass = mostUsedClass,
            contextsTracked = contextPreferences.size
        )
    }
}

data class UsageStatistics(
    val totalClasses: Int,
    val totalUsages: Int,
    val mostUsedClass: String,
    val contextsTracked: Int
)




