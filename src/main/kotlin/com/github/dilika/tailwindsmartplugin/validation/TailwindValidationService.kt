package com.github.dilika.tailwindsmartplugin.validation

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils
import com.github.dilika.tailwindsmartplugin.services.TailwindConfigService

/**
 * Service de validation pour les classes Tailwind CSS
 * Valide que les classes existent et sont correctement formatées
 */
@Service(Service.Level.PROJECT)
class TailwindValidationService(private val project: Project) {
    
    private val logger = Logger.getInstance(TailwindValidationService::class.java)
    private val cacheService = com.github.dilika.tailwindsmartplugin.performance.TailwindCacheService.getInstance(project)
    
    companion object {
        fun getInstance(project: Project): TailwindValidationService {
            return project.getService(TailwindValidationService::class.java)
        }
    }
    
    /**
     * Valide une classe Tailwind et retourne le résultat de validation
     */
    fun validateClass(className: String): ValidationResult {
        if (className.isBlank()) {
            return ValidationResult.Valid
        }
        
        // Nettoyer la classe (enlever les espaces, etc.)
        val cleanClass = className.trim()
        
        // Vérifier le cache d'abord
        cacheService.getCachedValidation(cleanClass)?.let {
            return it
        }
        
        // Vérifier si c'est une classe avec variant (ex: hover:bg-blue-500 ou hover:focus:bg-blue-500)
        val (variants, baseClass) = extractVariant(cleanClass)
        
        // Vérifier si c'est une classe avec valeur arbitraire (ex: w-[100px])
        if (isArbitraryValueClass(baseClass)) {
            return validateArbitraryValueClass(baseClass)
        }
        
        // Obtenir toutes les classes valides du projet
        val validClasses = TailwindUtils.getTailwindClasses(project)
        
        // Vérifier si la classe existe
        val isValid = validClasses.contains(cleanClass) || 
                     validClasses.contains(baseClass) ||
                     isValidCustomClass(baseClass)
        
        if (!isValid) {
            // Chercher des suggestions (classes similaires) - avec cache
            val suggestions = cacheService.getCachedSuggestions(baseClass) 
                ?: findSimilarClasses(baseClass, validClasses).also {
                    cacheService.cacheSuggestions(baseClass, it)
                }
            
            val result = ValidationResult.Invalid(
                className = cleanClass,
                reason = "Class '$baseClass' does not exist in Tailwind CSS",
                suggestions = suggestions
            )
            cacheService.cacheValidation(cleanClass, result)
            return result
        }
        
        // Vérifier les variantes (supporte les variants multiples)
        if (variants.isNotEmpty()) {
            val invalidVariants = variants.filter { !isValidVariant(it) }
            if (invalidVariants.isNotEmpty()) {
                val firstInvalid = invalidVariants.first()
                return ValidationResult.Invalid(
                    className = cleanClass,
                    reason = "Invalid variant '$firstInvalid'",
                    suggestions = findSimilarVariants(firstInvalid)
                )
            }
            
            // Vérifier l'ordre des variants (les variants responsive doivent venir avant les variants d'état)
            val responsiveVariants = listOf("sm", "md", "lg", "xl", "2xl")
            val stateVariants = listOf("hover", "focus", "active", "disabled")
            
            val responsiveIndices = variants.mapIndexedNotNull { index, variant -> 
                if (responsiveVariants.contains(variant)) index else null 
            }
            val stateIndices = variants.mapIndexedNotNull { index, variant -> 
                if (stateVariants.contains(variant)) index else null 
            }
            
            // Les variants responsive doivent venir avant les variants d'état
            if (responsiveIndices.isNotEmpty() && stateIndices.isNotEmpty()) {
                val maxResponsiveIndex = responsiveIndices.maxOrNull() ?: -1
                val minStateIndex = stateIndices.minOrNull() ?: Int.MAX_VALUE
                
                if (maxResponsiveIndex > minStateIndex) {
                    return ValidationResult.Invalid(
                        className = cleanClass,
                        reason = "Responsive variants should come before state variants (e.g., md:hover:bg-blue-500)",
                        suggestions = emptyList()
                    )
                }
            }
        }
        
        val result = ValidationResult.Valid
        cacheService.cacheValidation(cleanClass, result)
        return result
    }
    
    /**
     * Valide une liste de classes et retourne les résultats
     */
    fun validateClasses(classes: List<String>): List<ValidationResult> {
        return classes.map { validateClass(it) }
    }
    
    /**
     * Extrait les variants et la classe de base
     * Ex: "hover:focus:bg-blue-500" -> (["hover", "focus"], "bg-blue-500")
     */
    private fun extractVariant(className: String): Pair<List<String>, String> {
        val variantSeparator = ":"
        if (className.contains(variantSeparator)) {
            val parts = className.split(variantSeparator)
            if (parts.size > 1) {
                val variants = parts.dropLast(1)
                val baseClass = parts.last()
                return Pair(variants, baseClass)
            }
        }
        return Pair(emptyList(), className)
    }
    
    /**
     * Vérifie si une classe utilise une valeur arbitraire (ex: w-[100px])
     */
    private fun isArbitraryValueClass(className: String): Boolean {
        return className.contains("[") && className.contains("]")
    }
    
    /**
     * Valide une classe avec valeur arbitraire
     */
    private fun validateArbitraryValueClass(className: String): ValidationResult {
        // Extraire le préfixe et la valeur
        val match = Regex("^([a-z-]+)-\\[([^\\]]+)\\]").find(className)
        if (match == null) {
            return ValidationResult.Invalid(
                className = className,
                reason = "Invalid arbitrary value syntax. Use format: prefix-[value]",
                suggestions = emptyList()
            )
        }
        
        val prefix = match.groupValues[1]
        val value = match.groupValues[2]
        
        // Vérifier que le préfixe est valide
        val validPrefixes = listOf(
            "w", "h", "min-w", "min-h", "max-w", "max-h",
            "p", "px", "py", "pt", "pr", "pb", "pl",
            "m", "mx", "my", "mt", "mr", "mb", "ml",
            "top", "right", "bottom", "left", "inset",
            "gap", "gap-x", "gap-y",
            "text", "leading", "tracking", "rounded",
            "bg", "text", "border"
        )
        
        if (!validPrefixes.contains(prefix)) {
            return ValidationResult.Invalid(
                className = className,
                reason = "Invalid prefix '$prefix' for arbitrary value",
                suggestions = validPrefixes.filter { it.startsWith(prefix) || prefix.startsWith(it) }
            )
        }
        
        // Valider la syntaxe de la valeur
        if (value.isBlank()) {
            return ValidationResult.Invalid(
                className = className,
                reason = "Arbitrary value cannot be empty",
                suggestions = emptyList()
            )
        }
        
        return ValidationResult.Valid
    }
    
    /**
     * Vérifie si un variant est valide
     */
    private fun isValidVariant(variant: String): Boolean {
        val validVariants = listOf(
            // Responsive
            "sm", "md", "lg", "xl", "2xl",
            // State
            "hover", "focus", "active", "disabled", "visited", "checked",
            "first", "last", "odd", "even",
            // Group/Peer
            "group-hover", "group-focus", "peer", "peer-checked", "peer-focus", "peer-hover",
            // Pseudo-elements
            "before", "after", "placeholder", "selection", "marker", "file",
            // Data attributes
            "data",
            // Dark mode
            "dark",
            // Motion
            "motion-safe", "motion-reduce",
            // Media
            "landscape", "portrait", "print"
        )
        
        return validVariants.contains(variant) || variant.startsWith("data-")
    }
    
    /**
     * Trouve des classes similaires pour suggérer des corrections
     */
    private fun findSimilarClasses(className: String, validClasses: List<String>): List<String> {
        if (className.length < 3) return emptyList()
        
        // Calculer la distance de Levenshtein pour trouver les classes similaires
        val similarities = validClasses.map { validClass ->
            Pair(validClass, levenshteinDistance(className, validClass))
        }.filter { it.second <= 3 } // Seulement les classes avec distance <= 3
        
        return similarities
            .sortedBy { it.second }
            .take(5)
            .map { it.first }
    }
    
    /**
     * Trouve des variants similaires
     */
    private fun findSimilarVariants(variant: String): List<String> {
        val validVariants = listOf(
            "hover", "focus", "active", "sm", "md", "lg", "xl", "2xl",
            "group-hover", "peer", "dark", "disabled"
        )
        
        return validVariants.filter { 
            it.contains(variant) || variant.contains(it) || 
            levenshteinDistance(variant, it) <= 2
        }.take(5)
    }
    
    /**
     * Calcule la distance de Levenshtein entre deux chaînes
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[m][n]
    }
    
    /**
     * Vérifie si une classe est une classe personnalisée valide
     */
    private fun isValidCustomClass(className: String): Boolean {
        try {
            val configService = TailwindConfigService.getInstance(project)
            val configData = configService.getConfigData()
            
            // Vérifier dans les classes personnalisées de la config
            // Cette logique peut être étendue selon la structure de la config
            return false // Pour l'instant, on retourne false
        } catch (e: Exception) {
            logger.warn("Error checking custom class: ${e.message}")
            return false
        }
    }
}

/**
 * Résultat de validation d'une classe Tailwind
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    
    data class Invalid(
        val className: String,
        val reason: String,
        val suggestions: List<String> = emptyList()
    ) : ValidationResult()
}

