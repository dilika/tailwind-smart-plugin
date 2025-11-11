package com.github.dilika.tailwindsmartplugin.audit

import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlAttribute
import java.util.regex.Pattern

/**
 * Service d'audit intelligent pour le code Tailwind CSS
 */
class TailwindCodeAuditor {
    
    private val logger = Logger.getInstance(TailwindCodeAuditor::class.java)
    
    /**
     * Effectue un audit complet d'un fichier
     */
    fun auditTailwindUsage(file: PsiFile): AuditReport {
        val issues = mutableListOf<AuditIssue>()
        val statistics = TailwindStatistics()
        
        try {
            // Analyser tous les éléments avec des classes Tailwind
            val elements = PsiTreeUtil.findChildrenOfType(file, XmlTag::class.java)
            
            elements.forEach { element ->
                val classAttribute = element.getAttribute("class") ?: element.getAttribute("className")
                if (classAttribute != null) {
                    val classes = extractClasses(classAttribute.value ?: "")
                    analyzeClasses(classes, element, issues, statistics)
                }
            }
            
            // Générer des suggestions d'optimisation
            val optimizations = generateOptimizations(statistics)
            
            return AuditReport(
                file = file.name,
                issues = issues,
                statistics = statistics,
                optimizations = optimizations,
                score = calculateScore(issues)
            )
        } catch (e: Exception) {
            logger.warn("Error auditing file: ${file.name}", e)
            return AuditReport.empty(file.name)
        }
    }
    
    /**
     * Détecte les anti-patterns Tailwind
     */
    fun detectAntiPatterns(classes: List<String>): List<AntiPattern> {
        val antiPatterns = mutableListOf<AntiPattern>()
        
        classes.forEach { className ->
            when {
                // Classes redondantes
                className.contains("px-4") && className.contains("px-6") -> {
                    antiPatterns.add(AntiPattern(
                        type = "Redundant Padding",
                        description = "Multiple horizontal padding values detected",
                        classes = classes.filter { it.startsWith("px-") },
                        severity = AntiPattern.Severity.WARNING,
                        suggestion = "Use a single px- value"
                    ))
                }
                
                // Classes contradictoires
                className.contains("hidden") && className.contains("block") -> {
                    antiPatterns.add(AntiPattern(
                        type = "Contradictory Display",
                        description = "Hidden and block classes used together",
                        classes = listOf("hidden", "block"),
                        severity = AntiPattern.Severity.ERROR,
                        suggestion = "Remove one of the conflicting display classes"
                    ))
                }
                
                // Classes obsolètes
                className.contains("text-xs") && className.contains("text-sm") -> {
                    antiPatterns.add(AntiPattern(
                        type = "Multiple Text Sizes",
                        description = "Multiple text size classes detected",
                        classes = classes.filter { it.startsWith("text-") && it.contains("xs|sm|base|lg|xl".toRegex()) },
                        severity = AntiPattern.Severity.WARNING,
                        suggestion = "Use a single text size class"
                    ))
                }
                
                // Espacement incohérent
                className.contains("m-4") && className.contains("mx-2") -> {
                    antiPatterns.add(AntiPattern(
                        type = "Inconsistent Spacing",
                        description = "Margin and margin-x classes conflict",
                        classes = classes.filter { it.startsWith("m") },
                        severity = AntiPattern.Severity.WARNING,
                        suggestion = "Use consistent margin classes"
                    ))
                }
            }
        }
        
        return antiPatterns
    }
    
    /**
     * Suggère des bonnes pratiques
     */
    fun suggestBestPractices(context: CodeContext): List<BestPractice> {
        val practices = mutableListOf<BestPractice>()
        
        when (context.elementType) {
            "button" -> {
                practices.add(BestPractice(
                    title = "Button Accessibility",
                    description = "Ensure buttons have proper focus states and ARIA attributes",
                    suggestion = "Add focus:outline-none focus:ring-2 focus:ring-blue-500",
                    priority = BestPractice.Priority.HIGH
                ))
                practices.add(BestPractice(
                    title = "Button States",
                    description = "Include hover and active states for better UX",
                    suggestion = "Add hover:bg-blue-600 active:bg-blue-700",
                    priority = BestPractice.Priority.MEDIUM
                ))
            }
            "card" -> {
                practices.add(BestPractice(
                    title = "Card Shadows",
                    description = "Use consistent shadow depth for visual hierarchy",
                    suggestion = "Use shadow-sm for subtle cards, shadow-lg for prominent ones",
                    priority = BestPractice.Priority.MEDIUM
                ))
            }
            "form" -> {
                practices.add(BestPractice(
                    title = "Form Validation",
                    description = "Add visual feedback for form validation states",
                    suggestion = "Use border-red-500 for errors, border-green-500 for success",
                    priority = BestPractice.Priority.HIGH
                ))
            }
        }
        
        return practices
    }
    
    /**
     * Génère un rapport de performance
     */
    fun generatePerformanceReport(project: Project): PerformanceReport {
        val unusedClasses = findUnusedClasses(project)
        val duplicateClasses = findDuplicateClasses(project)
        val largeClassLists = findLargeClassLists(project)
        
        return PerformanceReport(
            unusedClasses = unusedClasses,
            duplicateClasses = duplicateClasses,
            largeClassLists = largeClassLists,
            recommendations = generatePerformanceRecommendations(unusedClasses, duplicateClasses, largeClassLists)
        )
    }
    
    private fun extractClasses(classString: String): List<String> {
        return classString.split("\\s+".toRegex()).filter { it.isNotEmpty() }
    }
    
    private fun analyzeClasses(classes: List<String>, element: XmlTag, issues: MutableList<AuditIssue>, statistics: TailwindStatistics) {
        // Détecter les anti-patterns
        val antiPatterns = detectAntiPatterns(classes)
        antiPatterns.forEach { antiPattern ->
            issues.add(AuditIssue(
                type = AuditIssue.Type.ANTI_PATTERN,
                message = antiPattern.description,
                element = element,
                severity = when (antiPattern.severity) {
                    AntiPattern.Severity.ERROR -> AuditIssue.Severity.ERROR
                    AntiPattern.Severity.WARNING -> AuditIssue.Severity.WARNING
                    AntiPattern.Severity.INFO -> AuditIssue.Severity.INFO
                },
                suggestion = antiPattern.suggestion
            ))
        }
        
        // Mettre à jour les statistiques
        statistics.totalClasses += classes.size
        statistics.totalElements++
        classes.forEach { className ->
            statistics.classUsage[className] = statistics.classUsage.getOrDefault(className, 0) + 1
        }
    }
    
    private fun generateOptimizations(statistics: TailwindStatistics): List<Optimization> {
        val optimizations = mutableListOf<Optimization>()
        
        // Optimisations basées sur l'usage
        val mostUsedClasses = statistics.classUsage.entries.sortedByDescending { it.value }.take(5)
        mostUsedClasses.forEach { (className, count) ->
            if (count > 10) {
                optimizations.add(Optimization(
                    type = "Extract Common Classes",
                    description = "Class '$className' is used $count times",
                    suggestion = "Consider creating a component class or CSS custom property",
                    impact = Optimization.Impact.MEDIUM
                ))
            }
        }
        
        return optimizations
    }
    
    private fun calculateScore(issues: List<AuditIssue>): Int {
        val errorCount = issues.count { it.severity == AuditIssue.Severity.ERROR }
        val warningCount = issues.count { it.severity == AuditIssue.Severity.WARNING }
        
        return (100 - (errorCount * 10) - (warningCount * 5)).coerceAtLeast(0)
    }
    
    private fun findUnusedClasses(project: Project): List<String> {
        // Implémentation simplifiée - dans un vrai plugin, on analyserait tous les fichiers
        return emptyList()
    }
    
    private fun findDuplicateClasses(project: Project): List<String> {
        // Implémentation simplifiée
        return emptyList()
    }
    
    private fun findLargeClassLists(project: Project): List<String> {
        // Implémentation simplifiée
        return emptyList()
    }
    
    private fun generatePerformanceRecommendations(
        unusedClasses: List<String>,
        duplicateClasses: List<String>,
        largeClassLists: List<String>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (unusedClasses.isNotEmpty()) {
            recommendations.add("Remove ${unusedClasses.size} unused classes to reduce bundle size")
        }
        
        if (duplicateClasses.isNotEmpty()) {
            recommendations.add("Consolidate duplicate class usage")
        }
        
        if (largeClassLists.isNotEmpty()) {
            recommendations.add("Consider breaking down large class lists into components")
        }
        
        return recommendations
    }
}

/**
 * Rapport d'audit
 */
data class AuditReport(
    val file: String,
    val issues: List<AuditIssue>,
    val statistics: TailwindStatistics,
    val optimizations: List<Optimization>,
    val score: Int
) {
    companion object {
        fun empty(fileName: String): AuditReport {
            return AuditReport(
                file = fileName,
                issues = emptyList(),
                statistics = TailwindStatistics(),
                optimizations = emptyList(),
                score = 100
            )
        }
    }
}

/**
 * Problème d'audit
 */
data class AuditIssue(
    val type: Type,
    val message: String,
    val element: XmlTag,
    val severity: Severity,
    val suggestion: String
) {
    enum class Type {
        ANTI_PATTERN,
        PERFORMANCE,
        ACCESSIBILITY,
        BEST_PRACTICE
    }
    
    enum class Severity {
        ERROR,
        WARNING,
        INFO
    }
}

/**
 * Anti-pattern détecté
 */
data class AntiPattern(
    val type: String,
    val description: String,
    val classes: List<String>,
    val severity: Severity,
    val suggestion: String
) {
    enum class Severity {
        ERROR,
        WARNING,
        INFO
    }
}

/**
 * Bonne pratique suggérée
 */
data class BestPractice(
    val title: String,
    val description: String,
    val suggestion: String,
    val priority: Priority
) {
    enum class Priority {
        HIGH,
        MEDIUM,
        LOW
    }
}

/**
 * Optimisation suggérée
 */
data class Optimization(
    val type: String,
    val description: String,
    val suggestion: String,
    val impact: Impact
) {
    enum class Impact {
        HIGH,
        MEDIUM,
        LOW
    }
}

/**
 * Statistiques Tailwind
 */
data class TailwindStatistics(
    var totalClasses: Int = 0,
    var totalElements: Int = 0,
    val classUsage: MutableMap<String, Int> = mutableMapOf()
)

/**
 * Rapport de performance
 */
data class PerformanceReport(
    val unusedClasses: List<String>,
    val duplicateClasses: List<String>,
    val largeClassLists: List<String>,
    val recommendations: List<String>
)

/**
 * Contexte de code
 */
data class CodeContext(
    val elementType: String,
    val framework: String,
    val fileType: String
)