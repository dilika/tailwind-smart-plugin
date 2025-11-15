# DEVELOPER_GUIDE.md
## 👨‍💻 Guide Développeur - Tailwind Smart Plugin

### Architecture du Plugin

#### Structure du Projet
```
src/main/kotlin/com/github/dilika/tailwindsmartplugin/
├── context/           # Intelligence contextuelle
│   ├── TailwindContextAnalyzer.kt
│   ├── TailwindContextualSuggestionsProvider.kt
│   ├── TailwindPatternLibrary.kt
│   └── TailwindPreferenceLearningService.kt
├── preview/           # Preview visuel
│   ├── TailwindVisualPreviewService.kt
│   └── TailwindInteractivePaletteService.kt
├── audit/            # Audit et refactoring
│   ├── TailwindCodeAuditor.kt
│   ├── TailwindRefactoringEngine.kt
│   └── TailwindGlobalSearchService.kt
├── snippets/         # Snippets collaboratifs
│   ├── TailwindSnippetManager.kt
│   └── TailwindAnalyticsService.kt
├── figma/            # Intégration Figma
│   └── FigmaIntegrationService.kt
└── completion/       # Système de complétion
    └── TailwindCompletionContributor.kt
```

#### Services Principaux

##### TailwindContextAnalyzer
Analyse le contexte DOM pour fournir des suggestions intelligentes.

```kotlin
@Service(Service.Level.PROJECT)
class TailwindContextAnalyzer {
    fun analyzeElementContext(element: PsiElement): ComponentContext
    fun detectComponentType(xmlTag: XmlTag): String
    fun extractAttributes(xmlTag: XmlTag): Map<String, String>
}
```

##### TailwindVisualPreviewService
Génère des previews visuels en temps réel.

```kotlin
@Service(Service.Level.PROJECT)
class TailwindVisualPreviewService {
    fun showLivePreview(editor: Editor, classes: List<String>)
    fun generatePreviewHtml(classes: List<String>): String
}
```

##### TailwindCodeAuditor
Audit intelligent du code Tailwind CSS.

```kotlin
@Service(Service.Level.PROJECT)
class TailwindCodeAuditor {
    fun auditTailwindUsage(file: PsiFile): AuditReport
    fun detectUnusedClasses(project: Project, classesInFile: List<String>): List<String>
}
```

### Extension Points

#### CompletionContributor
Fournit la complétion intelligente pour les classes Tailwind.

```kotlin
class TailwindCompletionContributor : CompletionContributor() {
    override fun fillCompletionVariants(
        parameters: CompletionParameters,
        result: CompletionResultSet
    ) {
        // Logique de complétion
    }
}
```

#### DocumentationProvider
Fournit la documentation pour les classes Tailwind.

```kotlin
class TailwindDocumentationProvider : DocumentationProvider {
    override fun generateDoc(
        element: PsiElement,
        originalElement: PsiElement?
    ): String? {
        // Génération de documentation
    }
}
```

#### FoldingBuilder
Gère le pliage des classes Tailwind.

```kotlin
class TailwindFoldingBuilder : FoldingBuilder {
    override fun buildFoldRegions(
        root: PsiElement,
        document: Document,
        quick: Boolean
    ): Array<FoldingDescriptor> {
        // Logique de pliage
    }
}
```

### Configuration du Plugin

#### plugin.xml
```xml
<idea-plugin>
    <id>com.github.dilika.tailwindsmartplugin</id>
    <name>Tailwind Smart Plugin</name>
    <version>1.2.2</version>
    
    <depends>com.intellij.modules.platform</depends>
    <depends>com.intellij.modules.java</depends>
    
    <extensions defaultExtensionNs="com.intellij">
        <!-- Services -->
        <service serviceInterface="com.github.dilika.tailwindsmartplugin.context.TailwindContextAnalyzer"/>
        <service serviceInterface="com.github.dilika.tailwindsmartplugin.preview.TailwindVisualPreviewService"/>
        
        <!-- Completion -->
        <completion.contributor language="HTML" implementationClass="com.github.dilika.tailwindsmartplugin.completion.TailwindCompletionContributor"/>
        <completion.contributor language="XML" implementationClass="com.github.dilika.tailwindsmartplugin.completion.TailwindCompletionContributor"/>
        
        <!-- Documentation -->
        <documentationProvider implementation="com.github.dilika.tailwindsmartplugin.documentation.TailwindDocumentationProvider"/>
        
        <!-- Folding -->
        <foldingBuilder implementation="com.github.dilika.tailwindsmartplugin.folding.TailwindFoldingBuilder"/>
        
        <!-- Actions -->
        <action id="TailwindSortClasses" class="com.github.dilika.tailwindsmartplugin.actions.TailwindSortClassesAction" text="Sort Tailwind Classes">
            <add-to-group group-id="EditMenu" anchor="last"/>
            <keyboard-shortcut keymap="$default" first-keystroke="shift F7"/>
        </action>
    </extensions>
</idea-plugin>
```

### Développement Local

#### Configuration de l'Environnement
```bash
# Cloner le repository
git clone https://github.com/dilika/tailwind-smart-plugin.git
cd tailwind-smart-plugin

# Installer les dépendances
./gradlew build

# Lancer l'IDE de développement
./gradlew runIde
```

#### Tests
```bash
# Exécuter tous les tests
./gradlew test

# Exécuter les tests d'intégration
./gradlew integrationTest

# Exécuter les tests de performance
./gradlew performanceTest
```

#### Build
```bash
# Build du plugin
./gradlew buildPlugin

# Vérification du plugin
./gradlew verifyPlugin

# Signature du plugin
./gradlew signPlugin
```

### API Publique

#### Types de Données

##### ComponentContext
```kotlin
data class ComponentContext(
    val componentType: String = "generic",
    val attributes: Map<String, String> = emptyMap(),
    val parentTags: List<String> = emptyList(),
    val currentClasses: List<String> = emptyList()
)
```

##### TailwindSuggestion
```kotlin
data class TailwindSuggestion(
    val className: String,
    val presentableText: String,
    val category: String,
    val icon: Icon?,
    val priority: Double = 0.0
)
```

##### AuditReport
```kotlin
data class AuditReport(
    val antiPatterns: List<AntiPattern>,
    val unusedClasses: List<String>,
    val performanceSuggestions: List<String>
)
```

#### Méthodes Utilitaires

##### TailwindUtils
```kotlin
object TailwindUtils {
    fun getTailwindClasses(project: Project): List<String>
    fun isTailwindClass(className: String): Boolean
    fun extractColorFromClass(className: String): Color?
    fun getClassCategory(className: String): String
}
```

##### TailwindSortingUtils
```kotlin
object TailwindSortingUtils {
    fun sortClasses(classes: List<String>): List<String>
    fun getClassPriority(className: String): Int
    fun groupClassesByCategory(classes: List<String>): Map<String, List<String>>
}
```

### Intégration avec d'Autres Plugins

#### Plugin Tailwind CSS
```kotlin
// Détection du plugin Tailwind CSS
val tailwindPlugin = PluginManager.getPlugin("com.tailwindcss")
if (tailwindPlugin != null) {
    // Intégration avec le plugin Tailwind CSS
}
```

#### Plugin Figma
```kotlin
// Intégration avec le plugin Figma
val figmaPlugin = PluginManager.getPlugin("com.figma")
if (figmaPlugin != null) {
    // Synchronisation avec Figma
}
```

### Performance et Optimisation

#### Cache
```kotlin
// Cache des classes Tailwind
private val classCache = ConcurrentHashMap<String, List<String>>()

// Cache des suggestions contextuelles
private val suggestionCache = ConcurrentHashMap<String, List<TailwindSuggestion>>()
```

#### Lazy Loading
```kotlin
// Chargement paresseux des services
private val contextAnalyzer by lazy { project.service<TailwindContextAnalyzer>() }
private val previewService by lazy { project.service<TailwindVisualPreviewService>() }
```

#### Threading
```kotlin
// Exécution asynchrone pour les opérations lourdes
ApplicationManager.getApplication().executeOnPooledThread {
    // Opération lourde
}
```

### Debugging et Logging

#### Configuration des Logs
```kotlin
private val logger = Logger.getInstance(TailwindContextAnalyzer::class.java)

// Logging avec différents niveaux
logger.info("Analyzing context for element: $element")
logger.debug("Found ${suggestions.size} suggestions")
logger.warn("Could not analyze context: ${e.message}")
logger.error("Error in context analysis", e)
```

#### Debug dans l'IDE
```kotlin
// Points d'arrêt pour le debugging
if (DEBUG_MODE) {
    logger.info("Debug: Context analysis result: $context")
}
```

### Tests Unitaires

#### Structure des Tests
```
src/test/kotlin/com/github/dilika/tailwindsmartplugin/
├── context/
│   ├── TailwindContextAnalyzerTest.kt
│   ├── TailwindContextualSuggestionsProviderTest.kt
│   ├── TailwindPatternLibraryTest.kt
│   └── TailwindPreferenceLearningServiceTest.kt
├── preview/
│   ├── TailwindVisualPreviewServiceTest.kt
│   └── TailwindInteractivePaletteServiceTest.kt
├── audit/
│   ├── TailwindCodeAuditorTest.kt
│   ├── TailwindRefactoringEngineTest.kt
│   └── TailwindGlobalSearchServiceTest.kt
├── snippets/
│   ├── TailwindSnippetManagerTest.kt
│   └── TailwindAnalyticsServiceTest.kt
└── figma/
    └── FigmaIntegrationServiceTest.kt
```

#### Exemple de Test
```kotlin
class TailwindContextAnalyzerTest : BasePlatformTestCase() {
    private lateinit var analyzer: TailwindContextAnalyzer

    override fun setUp() {
        super.setUp()
        analyzer = TailwindContextAnalyzer()
    }

    @Test
    fun testButtonContextDetection() {
        val mockElement = createMockButtonElement()
        val context = analyzer.analyzeElementContext(mockElement)
        
        assertEquals("button", context.componentType)
        assertTrue(context.attributes.isNotEmpty())
    }
}
```

### Déploiement

#### Configuration de Publication
```kotlin
// build.gradle.kts
publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
    channels.set(listOf("default"))
}

signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
}
```

#### Script de Déploiement
```bash
#!/bin/bash
# deploy.sh

echo "🚀 Deploying Tailwind Smart Plugin..."

# Vérifier les variables d'environnement
if [ -z "$PUBLISH_TOKEN" ]; then
    echo "❌ PUBLISH_TOKEN not set"
    exit 1
fi

# Build et déploiement
./gradlew clean buildPlugin signPlugin publishPlugin

echo "🎉 Plugin deployed successfully!"
```

### Contribution

#### Guidelines de Contribution
1. **Fork** le repository
2. **Créer** une branche feature (`git checkout -b feature/amazing-feature`)
3. **Commit** les changements (`git commit -m 'Add amazing feature'`)
4. **Push** vers la branche (`git push origin feature/amazing-feature`)
5. **Ouvrir** une Pull Request

#### Standards de Code
- **Kotlin** : Suivre les conventions Kotlin
- **Tests** : Couverture de code > 80%
- **Documentation** : KotlinDoc pour toutes les fonctions publiques
- **Logging** : Utiliser le système de logging IntelliJ

#### Processus de Review
1. **Tests** : Tous les tests doivent passer
2. **Linting** : Code conforme aux standards
3. **Documentation** : Documentation à jour
4. **Performance** : Pas de régression de performance

### Ressources

#### Documentation Officielle
- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Gradle Documentation](https://docs.gradle.org/)

#### Communauté
- [JetBrains Plugin Development](https://plugins.jetbrains.com/)
- [IntelliJ Platform Slack](https://intellij-support.jetbrains.com/hc/en-us/community/posts/360000000004-IntelliJ-Platform-Slack-Community)
- [GitHub Discussions](https://github.com/dilika/tailwind-smart-plugin/discussions)

#### Outils
- [IntelliJ Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- [Plugin Verifier](https://github.com/JetBrains/intellij-plugin-verifier)
- [Gradle IntelliJ Plugin](https://github.com/JetBrains/gradle-intellij-plugin)





