package com.github.dilika.tailwindsmartplugin.performance

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Service de cache intelligent pour optimiser les performances
 * Implémente un cache avec lazy loading et invalidation intelligente
 */
@Service(Service.Level.PROJECT)
class TailwindCacheService(private val project: Project) {
    
    private val logger = Logger.getInstance(TailwindCacheService::class.java)
    
    companion object {
        fun getInstance(project: Project): TailwindCacheService {
            return project.getService(TailwindCacheService::class.java)
        }
    }
    
    // Cache pour les classes Tailwind par préfixe (lazy loading)
    private val classPrefixCache = ConcurrentHashMap<String, List<String>>()
    
    // Cache pour les validations
    private val validationCache = ConcurrentHashMap<String, com.github.dilika.tailwindsmartplugin.validation.ValidationResult>()
    
    // Cache pour les suggestions
    private val suggestionCache = ConcurrentHashMap<String, List<String>>()
    
    // Cache pour les métadonnées de classes
    private val classMetadataCache = ConcurrentHashMap<String, Map<String, Any>>()
    
    // Lock pour les opérations de cache
    private val cacheLock = ReentrantReadWriteLock()
    
    // Taille maximale du cache
    private val maxCacheSize = 10000
    
    /**
     * Obtient les classes pour un préfixe donné (lazy loading)
     */
    fun getClassesByPrefix(prefix: String, allClasses: List<String>): List<String> {
        return cacheLock.read {
            classPrefixCache.getOrPut(prefix) {
                // Charger uniquement les classes qui commencent par ce préfixe
                allClasses.filter { it.startsWith(prefix) }
            }
        }
    }
    
    /**
     * Obtient le résultat de validation pour une classe (avec cache)
     */
    fun getCachedValidation(className: String): com.github.dilika.tailwindsmartplugin.validation.ValidationResult? {
        return cacheLock.read {
            validationCache[className]
        }
    }
    
    /**
     * Met en cache le résultat de validation
     */
    fun cacheValidation(className: String, result: com.github.dilika.tailwindsmartplugin.validation.ValidationResult) {
        cacheLock.write {
            // Limiter la taille du cache
            if (validationCache.size >= maxCacheSize) {
                // Supprimer les entrées les plus anciennes (FIFO simple)
                val keysToRemove = validationCache.keys.take(validationCache.size - maxCacheSize + 1000)
                keysToRemove.forEach { validationCache.remove(it) }
            }
            validationCache[className] = result
        }
    }
    
    /**
     * Obtient les suggestions pour une classe (avec cache)
     */
    fun getCachedSuggestions(className: String): List<String>? {
        return cacheLock.read {
            suggestionCache[className]
        }
    }
    
    /**
     * Met en cache les suggestions
     */
    fun cacheSuggestions(className: String, suggestions: List<String>) {
        cacheLock.write {
            if (suggestionCache.size >= maxCacheSize) {
                val keysToRemove = suggestionCache.keys.take(suggestionCache.size - maxCacheSize + 1000)
                keysToRemove.forEach { suggestionCache.remove(it) }
            }
            suggestionCache[className] = suggestions
        }
    }
    
    /**
     * Obtient les métadonnées pour une classe
     */
    fun getCachedMetadata(className: String): Map<String, Any>? {
        return cacheLock.read {
            classMetadataCache[className]
        }
    }
    
    /**
     * Met en cache les métadonnées
     */
    fun cacheMetadata(className: String, metadata: Map<String, Any>) {
        cacheLock.write {
            if (classMetadataCache.size >= maxCacheSize) {
                val keysToRemove = classMetadataCache.keys.take(classMetadataCache.size - maxCacheSize + 1000)
                keysToRemove.forEach { classMetadataCache.remove(it) }
            }
            classMetadataCache[className] = metadata
        }
    }
    
    /**
     * Invalide le cache pour un préfixe spécifique
     */
    fun invalidatePrefix(prefix: String) {
        cacheLock.write {
            classPrefixCache.remove(prefix)
        }
    }
    
    /**
     * Invalide tout le cache
     */
    fun invalidateAll() {
        cacheLock.write {
            classPrefixCache.clear()
            validationCache.clear()
            suggestionCache.clear()
            classMetadataCache.clear()
            logger.info("Cache invalidated for project: ${project.name}")
        }
    }
    
    /**
     * Obtient les statistiques du cache
     */
    fun getCacheStats(): CacheStats {
        return cacheLock.read {
            CacheStats(
                prefixCacheSize = classPrefixCache.size,
                validationCacheSize = validationCache.size,
                suggestionCacheSize = suggestionCache.size,
                metadataCacheSize = classMetadataCache.size,
                totalSize = classPrefixCache.size + validationCache.size + suggestionCache.size + classMetadataCache.size
            )
        }
    }
}

/**
 * Statistiques du cache
 */
data class CacheStats(
    val prefixCacheSize: Int,
    val validationCacheSize: Int,
    val suggestionCacheSize: Int,
    val metadataCacheSize: Int,
    val totalSize: Int
)

