# API_REFERENCE.md
## 📚 Référence API

### Services Principaux

#### TailwindContextAnalyzer
```kotlin
/**
 * Analyseur de contexte intelligent pour les suggestions Tailwind CSS
 * 
 * @param element L'élément PSI à analyser
 * @return ComponentContext Le contexte analysé avec suggestions
 */
fun analyzeElementContext(element: PsiElement): ComponentContext

/**
 * Suggère des classes pour un élément spécifique
 * 
 * @param element L'élément PSI
 * @return List<TailwindSuggestion> Liste des suggestions
 */
fun suggestClassesForElement(element: PsiElement): List<TailwindSuggestion>
```

#### TailwindVisualPreviewService
```kotlin
/**
 * Génère un preview pour une liste de classes Tailwind
 * 
 * @param classes Liste des classes Tailwind
 * @return PreviewData Données du preview généré
 */
fun generatePreview(classes: List<String>): PreviewData

/**
 * Affiche un preview en temps réel
 * 
 * @param editor L'éditeur IntelliJ
 * @param classes Liste des classes à prévisualiser
 */
fun showLivePreview(editor: Editor, classes: List<String>)
```

#### TailwindCodeAuditor
```kotlin
/**
 * Audit intelligent du code Tailwind
 * 
 * @param file Fichier PSI à auditer
 * @return AuditReport Rapport d'audit complet
 */
fun auditTailwindUsage(file: PsiFile): AuditReport

/**
 * Détecte les classes inutilisées
 * 
 * @param project Projet IntelliJ
 * @param classesInFile Classes présentes dans le fichier
 * @return List<String> Classes inutilisées
 */
fun detectUnusedClasses(project: Project, classesInFile: List<String>): List<String>
```

#### TailwindRefactoringEngine
```kotlin
/**
 * Optimise l'ordre des classes Tailwind
 * 
 * @param classes Liste des classes à optimiser
 * @return List<String> Classes optimisées
 */
fun optimizeClassOrder(classes: List<String>): List<String>

/**
 * Détecte les classes redondantes
 * 
 * @param classes Liste des classes à analyser
 * @return List<String> Classes redondantes
 */
fun detectRedundantClasses(classes: List<String>): List<String>

/**
 * Suggère des consolidations de classes
 * 
 * @param classes Liste des classes à analyser
 * @return List<RefactoringSuggestion> Suggestions de refactoring
 */
fun suggestClassConsolidation(classes: List<String>): List<RefactoringSuggestion>
```

#### TailwindGlobalSearchService
```kotlin
/**
 * Recherche globale de classes Tailwind
 * 
 * @param query Requête de recherche
 * @return List<ClassOccurrence> Occurrences trouvées
 */
fun searchClassesGlobally(query: String): List<ClassOccurrence>

/**
 * Navigue vers la définition d'une classe
 * 
 * @param occurrence Occurrence de classe
 */
fun navigateToClassDefinition(occurrence: ClassOccurrence)
```

#### TailwindSnippetManager
```kotlin
/**
 * Crée un nouveau snippet
 * 
 * @param name Nom du snippet
 * @param classes Classes Tailwind
 * @param description Description du snippet
 * @param tags Tags du snippet
 * @return TailwindSnippet Snippet créé
 */
fun createSnippet(name: String, classes: String, description: String = "", tags: List<String> = emptyList()): TailwindSnippet

/**
 * Récupère un snippet par ID
 * 
 * @param id ID du snippet
 * @return TailwindSnippet? Snippet trouvé ou null
 */
fun getSnippet(id: String): TailwindSnippet?

/**
 * Partage un snippet
 * 
 * @param snippet Snippet à partager
 * @return String URL de partage
 */
fun shareSnippet(snippet: TailwindSnippet): String
```

#### TailwindAnalyticsService
```kotlin
/**
 * Track l'utilisation d'une classe
 * 
 * @param className Nom de la classe
 */
fun trackClassUsage(className: String)

/**
 * Génère un rapport d'utilisation
 * 
 * @return UsageReport Rapport d'utilisation
 */
fun generateUsageReport(): UsageReport

/**
 * Suggère des optimisations
 * 
 * @param usageData Données d'utilisation
 * @return List<OptimizationSuggestion> Suggestions d'optimisation
 */
fun suggestOptimizations(usageData: UsageReport): List<OptimizationSuggestion>
```

#### FigmaIntegrationService
```kotlin
/**
 * Importe depuis Figma
 * 
 * @param figmaUrl URL du fichier Figma
 * @param accessToken Token d'accès Figma
 * @return FigmaImportResult Résultat de l'import
 */
fun importFromFigma(figmaUrl: String, accessToken: String): FigmaImportResult

/**
 * Exporte vers la configuration Tailwind
 * 
 * @param designTokens Tokens de design
 * @return String Configuration Tailwind
 */
fun exportToTailwindConfig(designTokens: DesignTokens): String

/**
 * Synchronise avec Figma
 * 
 * @param projectId ID du projet Figma
 * @param accessToken Token d'accès
 * @return SyncResult Résultat de la synchronisation
 */
fun syncWithFigma(projectId: String, accessToken: String): SyncResult
```

### Types de Données

#### ComponentContext
```kotlin
data class ComponentContext(
    val componentType: String = "generic",
    val attributes: Map<String, String> = emptyMap(),
    val parentTags: List<String> = emptyList(),
    val currentClasses: List<String> = emptyList()
)
```

#### TailwindSuggestion
```kotlin
data class TailwindSuggestion(
    val className: String,
    val presentableText: String,
    val category: String,
    val icon: Icon?,
    val priority: Double = 0.0
)
```

#### AuditReport
```kotlin
data class AuditReport(
    val antiPatterns: List<AntiPattern>,
    val unusedClasses: List<String>,
    val performanceSuggestions: List<String>
)
```

#### RefactoringSuggestion
```kotlin
data class RefactoringSuggestion(
    val type: String,
    val description: String,
    val originalClasses: List<String>,
    val refactoredClasses: List<String>
)
```

#### ClassOccurrence
```kotlin
data class ClassOccurrence(
    val className: String,
    val filePath: String,
    val lineNumber: Int,
    val columnNumber: Int,
    val textRange: TextRange
)
```

#### TailwindSnippet
```kotlin
data class TailwindSnippet(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var classes: String,
    var description: String = "",
    var tags: List<String> = emptyList(),
    var isShared: Boolean = false,
    var shareUrl: String? = null
)
```

#### UsageReport
```kotlin
data class UsageReport(
    val totalClassesUsed: Int,
    val uniqueClasses: Int,
    val topUsedClasses: List<ClassUsage>
)
```

#### DesignTokens
```kotlin
data class DesignTokens(
    val colors: Map<String, String>,
    val spacing: Map<String, String>,
    val typography: Map<String, String>
)
```

### Exemples d'Utilisation

#### Utilisation Basique
```kotlin
// Obtenir le service d'analyse de contexte
val contextAnalyzer = project.service<TailwindContextAnalyzer>()

// Analyser le contexte d'un élément
val context = contextAnalyzer.analyzeElementContext(element)

// Obtenir des suggestions
val suggestions = contextAnalyzer.suggestClassesForElement(element)
```

#### Utilisation Avancée
```kotlin
// Audit complet d'un fichier
val auditor = project.service<TailwindCodeAuditor>()
val report = auditor.auditTailwindUsage(file)

// Refactoring automatique
val refactoringEngine = project.service<TailwindRefactoringEngine>()
val optimizedClasses = refactoringEngine.optimizeClassOrder(classes)

// Recherche globale
val searchService = project.service<TailwindGlobalSearchService>()
val occurrences = searchService.searchClassesGlobally("bg-blue")
```

### Configuration

#### Variables d'Environnement
```bash
# Figma Integration
FIGMA_ACCESS_TOKEN="your_token"
FIGMA_PROJECT_ID="your_project_id"

# Analytics
ANALYTICS_ENABLED="true"
ANALYTICS_ENDPOINT="https://analytics.example.com"

# Performance
CACHE_SIZE="1000"
CACHE_TTL="3600"
```

#### Configuration du Plugin
```kotlin
// Dans plugin.xml
<extensions defaultExtensionNs="com.intellij">
    <service serviceInterface="com.github.dilika.tailwindsmartplugin.context.TailwindContextAnalyzer"/>
    <service serviceInterface="com.github.dilika.tailwindsmartplugin.preview.TailwindVisualPreviewService"/>
    <!-- ... autres services ... -->
</extensions>
```







