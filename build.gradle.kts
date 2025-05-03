plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.0"
    id("org.jetbrains.changelog") version "2.2.1"
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
    implementation(libs.json)
    // GraalVM dependencies with stable version
    implementation(libs.graalvm.sdk)
    implementation(libs.graalvm.js)
    implementation(libs.graalvm.js.scriptengine)
}

// IntelliJ Plugin configuration
intellij {
    version.set(project.property("platformVersion").toString())
    type.set(project.property("platformType").toString())
    updateSinceUntilBuild.set(true)
    
    // Only include bundled plugins from the platformBundledPlugins property
    val bundledPlugins = project.property("platformBundledPlugins").toString()
    
    if (bundledPlugins.isNotEmpty()) {
        plugins.set(bundledPlugins.split(',').filter { it.isNotEmpty() })
    } else {
        plugins.set(emptyList())
    }
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

    // Configure the getChangelog task from the changelog plugin
    getChangelog {
        version = project.property("pluginVersion").toString()
        // Use this task for GitHub Actions instead of our custom task
    }

    // Test configuration
    test {
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    // Configure the wrapper task
    wrapper {
        gradleVersion = project.property("gradleVersion").toString()
    }

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
    
    buildSearchableOptions {
        enabled = false
    }
}

// Configure Changelog
changelog {
    version = project.property("pluginVersion").toString()
    groups.set(emptyList())
    repositoryUrl.set(project.property("pluginRepositoryUrl").toString())
}

// Custom task to get changelog content that's simpler and more reliable
abstract class CustomChangelogTask : DefaultTask() {
    @get:Input
    @get:Option(option = "unreleased", description = "Get unreleased changelog")
    val unreleased: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Option(option = "no-header", description = "Exclude header from output")
    val noHeader: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)

    @TaskAction
    fun run() {
        val content = if (unreleased.get()) {
            "Initial release with basic Tailwind CSS class completion"
        } else {
            "Basic Tailwind CSS class completion for HTML, JSX, and TSX files"
        }
        
        println(content)
    }
}

// Use a different name for our custom task to avoid conflicts with changelog plugin's getChangelog
tasks.register<CustomChangelogTask>("generateChangelogContent") {
    description = "Get custom changelog content"
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
