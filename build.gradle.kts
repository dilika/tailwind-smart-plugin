plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.0"
    id("org.jetbrains.changelog") version "2.0.0"
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
            <h1>Tailwind CSS Smart Plugin</h1>
            <p>Elevate your Tailwind CSS development experience in IntelliJ IDEs with smart features designed for speed and precision.</p>

            <h2>Key Features</h2>
            <ul>
                <li>
                    <strong>Advanced Auto-Completion:</strong>
                    <ul>
                        <li>Comprehensive suggestions for Tailwind CSS utility classes, <strong>including Tailwind v4 support</strong>.</li>
                        <li><strong>Dynamically prioritized suggestions</strong> that adapt to your typing for maximum relevance.</li>
                        <li><strong>Smart Class Group Suggestions:</strong> Access common UI patterns (buttons, alerts, cards, avatars, modals, etc.) with ready-to-use class combinations.</li>
                        <li><strong>Categorized popups</strong> with intuitive, <strong>color-coded icons</strong> for quick visual identification.</li>
                        <li>Smart detection of your project's <code>tailwind.config.js</code> for <strong>custom class support</strong> and theme values.</li>
                    </ul>
                </li>
                <li><strong>Automatic Project Setup:</strong> Detects your project's Tailwind configuration (<code>tailwind.config.js</code>) on startup to tailor suggestions and documentation.</li>
            </ul>

            <h2>Supported Languages & Frameworks</h2>
            <p>Enjoy a consistent experience across a wide range of web technologies:</p>
            <ul>
                <li>HTML, XML</li>
                <li>JavaScript (including JSX for React, Qwik, Solid)</li>
                <li>TypeScript (including TSX for React, Qwik, Solid)</li>
                <li>Vue.js (<code>.vue</code> files)</li>
                <li>PHP (including Blade templates)</li>
                <li>Svelte</li>
                <li>Astro</li>
                <li>Other template languages where class attributes are common.</li>
            </ul>
            
            <h2>⭐️ Support This Plugin</h2>
            <div style="padding: 12px; background-color: #f1f5f9; border-radius: 8px; margin: 15px 0;">
                <p style="font-weight: 500;">This plugin is <strong>saving developers valuable time</strong> every day. <em>Be part of its early growth story!</em></p>
                <p>Unlike many premium Tailwind tools that cost $10-15/month, this plugin is completely <strong>free</strong>. If it has improved your workflow:</p>
                <div style="text-align: center; margin: 15px 0;">
                    <a href="https://ko-fi.com/dilika" style="display: inline-block; background-color: #0ea5e9; color: white; font-weight: bold; padding: 10px 20px; text-decoration: none; border-radius: 6px;">☕️ Buy Me a Coffee</a>
                </div>
                <p style="font-size: 0.9em; font-style: italic;">Your early support is crucial! It directly enables new features, better tools, and ensures the plugin's future. <strong>Help this plugin reach its full potential</strong> and shape its development!</p>
            </div>
        """.trimIndent())

        // Change notes
        changeNotes.set("""
            <h2>1.2.0</h2>
            <ul>
              <li><strong>Enhanced Relevance:</strong> Intelligently sorted suggestions based on your typing for an optimal experience</li>
              <li><strong>Extended Coverage:</strong> Complete support for common Tailwind classes (inline-block, px-3, py-1, text-indigo-600, etc.)</li>
              <li><strong>New Icon:</strong> Clean and professional design with official Tailwind colors</li>
              <li><strong>Optimizations:</strong> Improved performance and stability of auto-completion</li>
              <li><strong>Expanded Compatibility:</strong> Better support for various frameworks and languages</li>
            </ul>
            
            <h2>0.0.2</h2>
            <ul>
              <li>Extensive support for Tailwind v1–v4 utilities</li>
              <li>Color-coded icons & improved autocompletion visuals</li>
              <li>Donation link added to plugin overview</li>
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
