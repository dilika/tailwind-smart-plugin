package com.github.dilika.tailwindsmartplugin.validation

/**
 * Détecte les conflits entre classes Tailwind CSS
 * Par exemple: p-4 et px-2 (padding général vs padding horizontal)
 */
class ClassConflictDetector {
    
    /**
     * Détecte les conflits dans une liste de classes
     */
    fun detectConflicts(classes: List<String>): List<ClassConflict> {
        val conflicts = mutableListOf<ClassConflict>()
        
        // Extraire les classes de base (sans variants)
        val baseClasses = classes.map { extractBaseClass(it) }
        
        // Détecter les conflits de padding
        conflicts.addAll(detectPaddingConflicts(baseClasses, classes))
        
        // Détecter les conflits de margin
        conflicts.addAll(detectMarginConflicts(baseClasses, classes))
        
        // Détecter les conflits de width/height
        conflicts.addAll(detectSizingConflicts(baseClasses, classes))
        
        // Détecter les conflits de display
        conflicts.addAll(detectDisplayConflicts(baseClasses, classes))
        
        // Détecter les conflits de position
        conflicts.addAll(detectPositionConflicts(baseClasses, classes))
        
        return conflicts
    }
    
    /**
     * Détecte les conflits de padding
     */
    private fun detectPaddingConflicts(baseClasses: List<String>, originalClasses: List<String>): List<ClassConflict> {
        val conflicts = mutableListOf<ClassConflict>()
        
        val hasGeneralPadding = baseClasses.any { it.matches(Regex("^p-\\d+$|^p-\\w+$")) }
        val hasHorizontalPadding = baseClasses.any { it.matches(Regex("^px-\\d+$|^px-\\w+$")) }
        val hasVerticalPadding = baseClasses.any { it.matches(Regex("^py-\\d+$|^py-\\w+$")) }
        val hasTopPadding = baseClasses.any { it.matches(Regex("^pt-\\d+$|^pt-\\w+$")) }
        val hasRightPadding = baseClasses.any { it.matches(Regex("^pr-\\d+$|^pr-\\w+$")) }
        val hasBottomPadding = baseClasses.any { it.matches(Regex("^pb-\\d+$|^pb-\\w+$")) }
        val hasLeftPadding = baseClasses.any { it.matches(Regex("^pl-\\d+$|^pl-\\w+$")) }
        
        if (hasGeneralPadding) {
            if (hasHorizontalPadding || hasVerticalPadding || hasTopPadding || 
                hasRightPadding || hasBottomPadding || hasLeftPadding) {
                val conflictingClasses = originalClasses.filter { 
                    it.contains("px-") || it.contains("py-") || it.contains("pt-") || 
                    it.contains("pr-") || it.contains("pb-") || it.contains("pl-")
                }
                if (conflictingClasses.isNotEmpty()) {
                    conflicts.add(ClassConflict(
                        type = ConflictType.PADDING,
                        message = "General padding class conflicts with specific padding classes",
                        conflictingClasses = conflictingClasses,
                        suggestion = "Remove general padding class or specific padding classes"
                    ))
                }
            }
        }
        
        return conflicts
    }
    
    /**
     * Détecte les conflits de margin
     */
    private fun detectMarginConflicts(baseClasses: List<String>, originalClasses: List<String>): List<ClassConflict> {
        val conflicts = mutableListOf<ClassConflict>()
        
        val hasGeneralMargin = baseClasses.any { it.matches(Regex("^m-\\d+$|^m-\\w+$")) }
        val hasHorizontalMargin = baseClasses.any { it.matches(Regex("^mx-\\d+$|^mx-\\w+$")) }
        val hasVerticalMargin = baseClasses.any { it.matches(Regex("^my-\\d+$|^my-\\w+$")) }
        val hasTopMargin = baseClasses.any { it.matches(Regex("^mt-\\d+$|^mt-\\w+$")) }
        val hasRightMargin = baseClasses.any { it.matches(Regex("^mr-\\d+$|^mr-\\w+$")) }
        val hasBottomMargin = baseClasses.any { it.matches(Regex("^mb-\\d+$|^mb-\\w+$")) }
        val hasLeftMargin = baseClasses.any { it.matches(Regex("^ml-\\d+$|^ml-\\w+$")) }
        
        if (hasGeneralMargin) {
            if (hasHorizontalMargin || hasVerticalMargin || hasTopMargin || 
                hasRightMargin || hasBottomMargin || hasLeftMargin) {
                val conflictingClasses = originalClasses.filter { 
                    it.contains("mx-") || it.contains("my-") || it.contains("mt-") || 
                    it.contains("mr-") || it.contains("mb-") || it.contains("ml-")
                }
                if (conflictingClasses.isNotEmpty()) {
                    conflicts.add(ClassConflict(
                        type = ConflictType.MARGIN,
                        message = "General margin class conflicts with specific margin classes",
                        conflictingClasses = conflictingClasses,
                        suggestion = "Remove general margin class or specific margin classes"
                    ))
                }
            }
        }
        
        return conflicts
    }
    
    /**
     * Détecte les conflits de sizing (width/height)
     */
    private fun detectSizingConflicts(baseClasses: List<String>, originalClasses: List<String>): List<ClassConflict> {
        val conflicts = mutableListOf<ClassConflict>()
        
        val widthClasses = baseClasses.filter { it.matches(Regex("^w-\\w+$")) }
        val minWidthClasses = baseClasses.filter { it.matches(Regex("^min-w-\\w+$")) }
        val maxWidthClasses = baseClasses.filter { it.matches(Regex("^max-w-\\w+$")) }
        
        // Détecter si min-w est plus grand que max-w (logiquement)
        // Note: Ceci nécessiterait de parser les valeurs, donc on se contente de détecter la présence
        
        val heightClasses = baseClasses.filter { it.matches(Regex("^h-\\w+$")) }
        val minHeightClasses = baseClasses.filter { it.matches(Regex("^min-h-\\w+$")) }
        val maxHeightClasses = baseClasses.filter { it.matches(Regex("^max-h-\\w+$")) }
        
        // Pour l'instant, on ne détecte que les cas évidents
        // On pourrait améliorer en comparant les valeurs numériques
        
        return conflicts
    }
    
    /**
     * Détecte les conflits de display
     */
    private fun detectDisplayConflicts(baseClasses: List<String>, originalClasses: List<String>): List<ClassConflict> {
        val conflicts = mutableListOf<ClassConflict>()
        
        val displayClasses = baseClasses.filter { 
            it == "block" || it == "inline" || it == "inline-block" || 
            it == "flex" || it == "inline-flex" || 
            it == "grid" || it == "inline-grid" ||
            it == "table" || it == "inline-table" ||
            it == "hidden"
        }
        
        if (displayClasses.size > 1) {
            conflicts.add(ClassConflict(
                type = ConflictType.DISPLAY,
                message = "Multiple display classes conflict with each other",
                conflictingClasses = originalClasses.filter { displayClasses.contains(extractBaseClass(it)) },
                suggestion = "Use only one display class"
            ))
        }
        
        return conflicts
    }
    
    /**
     * Détecte les conflits de position
     */
    private fun detectPositionConflicts(baseClasses: List<String>, originalClasses: List<String>): List<ClassConflict> {
        val conflicts = mutableListOf<ClassConflict>()
        
        val positionClasses = baseClasses.filter { 
            it == "static" || it == "fixed" || it == "absolute" || 
            it == "relative" || it == "sticky"
        }
        
        if (positionClasses.size > 1) {
            conflicts.add(ClassConflict(
                type = ConflictType.POSITION,
                message = "Multiple position classes conflict with each other",
                conflictingClasses = originalClasses.filter { positionClasses.contains(extractBaseClass(it)) },
                suggestion = "Use only one position class"
            ))
        }
        
        return conflicts
    }
    
    /**
     * Extrait la classe de base (sans variant)
     */
    private fun extractBaseClass(className: String): String {
        return if (className.contains(":")) {
            className.split(":").last()
        } else {
            className
        }
    }
}

/**
 * Représente un conflit entre classes
 */
data class ClassConflict(
    val type: ConflictType,
    val message: String,
    val conflictingClasses: List<String>,
    val suggestion: String
)

/**
 * Types de conflits
 */
enum class ConflictType {
    PADDING,
    MARGIN,
    SIZING,
    DISPLAY,
    POSITION
}

