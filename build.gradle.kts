import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

plugins {
    id("java") // Java support
    id("org.jetbrains.kotlin.jvm") version "1.9.22" // Kotlin support
    id("org.jetbrains.intellij") version "1.16.1" // IntelliJ Plugin
    id("org.jetbrains.changelog") version "2.2.0" // Changelog Plugin
    id("org.jetbrains.qodana") version "2023.3.1" // Qodana Plugin
    id("org.jetbrains.kotlinx.kover") version "0.7.5" // Kover Plugin
}

group = project.property("pluginGroup").toString()
version = project.property("pluginVersion").toString()

repositories {
    mavenCentral()
    maven { url = uri("https://cache-redirector.jetbrains.com/intellij-dependencies") }
}

// Configure JVM target for Kotlin
kotlin {
    jvmToolchain(17)
}

// Add dependencies needed by your plugin
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.json:json:20231013")
    // GraalVM dependencies with stable version
    implementation("org.graalvm.sdk:graal-sdk:22.3.0")
    implementation("org.graalvm.js:js:22.3.0")
    implementation("org.graalvm.js:js-scriptengine:22.3.0")
}

// IntelliJ Plugin configuration
intellij {
    version.set(project.property("platformVersion").toString())
    type.set(project.property("platformType").toString())

    // Configure plugins
    val bundledPlugins = project.property("platformBundledPlugins").toString()
    val platformPlugins = project.property("platformPlugins").toString()

    val allPlugins = mutableListOf<String>()
    if (bundledPlugins.isNotEmpty()) {
        allPlugins.addAll(bundledPlugins.split(',').filter { it.isNotEmpty() })
    }
    if (platformPlugins.isNotEmpty()) {
        allPlugins.addAll(platformPlugins.split(',').filter { it.isNotEmpty() })
    }

    plugins.set(allPlugins)
}

// Define our copy task for parser scripts
val copyParserScripts by tasks.registering(Copy::class) {
    from("src/main/resources/parser")
    into("${layout.buildDirectory.get()}/classes/kotlin/main/parser")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Configure Gradle tasks
tasks {
    // Patch plugin.xml
    patchPluginXml {
        sinceBuild.set(project.property("pluginSinceBuild").toString())
        untilBuild.set(project.property("pluginUntilBuild").toString())

        // Plugin description
        pluginDescription.set("""
            <p>Tailwind CSS Smart Plugin provides intelligent completion for Tailwind CSS classes.</p>
            <p>Features:</p>
            <ul>
              <li>Smart auto-completion for Tailwind CSS classes</li>
              <li>Visual indicators for color-related classes</li>
              <li>Compatible with HTML, JSX, and TSX files</li>
            </ul>
        """.trimIndent())

        // Change notes
        changeNotes.set("""
            <h2>0.0.1</h2>
            <ul>
              <li>Initial release</li>
              <li>Basic Tailwind CSS class completion</li>
            </ul>
        """.trimIndent())
    }

    // Process resources
    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        finalizedBy(copyParserScripts)
    }

    // Make compileKotlin finalize with our copy task
    compileKotlin {
        finalizedBy(copyParserScripts)
    }

    // Configure JAR task
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(copyParserScripts)
    }

    // Test configuration
    test {
        systemProperty("java.util.logging.config.file", "${project.projectDir}/src/test/resources/test-log.properties")
    }

    // Configure the wrapper task
    wrapper {
        gradleVersion = project.property("gradleVersion").toString()
    }

    // Configure duplicate resource handling
    withType<Copy>().configureEach {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    withType<Jar>().configureEach {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

// Ensure instrumentCode depends on copyParserScripts
afterEvaluate {
    tasks.findByName("instrumentCode")?.dependsOn(copyParserScripts)
}

// Configure Gradle tasks
tasks {
    // Configure signing
    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN") ?: "")
        privateKey.set(System.getenv("PRIVATE_KEY") ?: "")
        password.set(System.getenv("PRIVATE_KEY_PASSWORD") ?: "")
    }

    // Configure publishing
    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN") ?: "")
        channels.set(listOf("default"))
    }
}

// Configure Changelog
changelog {
    version.set(project.property("pluginVersion").toString())
    groups.set(emptyList())
    repositoryUrl.set(project.property("pluginRepositoryUrl").toString())
}

// Custom task to get changelog content
abstract class GetChangelogTask : DefaultTask() {
    @get:Input
    @get:Option(option = "unreleased", description = "Get unreleased changelog")
    val unreleased: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Option(option = "no-header", description = "Exclude header from output")
    val noHeader: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)

    @TaskAction
    fun run() {
        try {
            println("Starting getChangelog task")
            println("Options: unreleased=${unreleased.get()}, noHeader=${noHeader.get()}")

            val changelog = project.extensions.getByType<org.jetbrains.changelog.ChangelogPluginExtension>()
            println("Changelog extension obtained")

            val content = try {
                if (unreleased.get()) {
                    println("Getting unreleased changelog")
                    val unreleasedContent = changelog.getUnreleased().toText()
                    println("Unreleased content length: ${unreleasedContent.length}")
                    unreleasedContent
                } else {
                    println("Getting latest changelog")
                    val latestContent = changelog.getLatest().toText()
                    println("Latest content length: ${latestContent.length}")
                    latestContent
                }
            } catch (e: Exception) {
                println("Failed to get changelog content: ${e.message}")
                logger.warn("Failed to get changelog content: ${e.message}")
                // Return empty content if section doesn't exist or there's an error
                ""
            }

            println("Content obtained, processing header")
            val output = if (noHeader.get() && content.isNotEmpty()) {
                // Remove the header (first line) from the content
                val lines = content.lines()
                println("Content has ${lines.size} lines")
                lines.drop(1).joinToString("\n")
            } else {
                content
            }

            println("Final output length: ${output.length}")
            println(output)
            println("getChangelog task completed")
        } catch (e: Exception) {
            println("Error in getChangelog task: ${e.message}")
            logger.error("Error in getChangelog task: ${e.message}")
            e.printStackTrace()
            // Print empty string to ensure the task doesn't fail
            println("")
        }
    }
}

tasks.register<GetChangelogTask>("customGetChangelog") {
    description = "Get changelog content"
    group = "documentation"
}

// Register the task with a different name to avoid conflicts with the changelog plugin
tasks.register<GetChangelogTask>("customGetChangelogForGitHub") {
    description = "Get changelog content (for GitHub workflow)"
    group = "documentation"
}

// Simple test task to verify task execution
tasks.register("testTask") {
    description = "Test task to verify task execution"
    group = "documentation"
    doLast {
        println("Test task executed successfully")
    }
}
