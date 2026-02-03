plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.21"
    id("org.jetbrains.intellij") version "1.17.3"
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
    compilerOptions {
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }
}

// Configure Java toolchain
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

// Add dependencies needed by your plugin
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(libs.json)
    // GraalVM dependencies with stable version
    implementation(libs.graalvm.sdk)
    implementation(libs.graalvm.js)
    implementation(libs.graalvm.js.scriptengine)
    
    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.mockito:mockito-core:5.1.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("io.mockk:mockk:1.13.5")
}

// IntelliJ Plugin configuration
intellij {
    // Configure IntelliJ IDEA version and type
    version.set(
        project.findProperty("ideaVersion")?.toString()
            ?: project.property("platformVersion").toString()
    )
    type.set(
        project.findProperty("ideaType")?.toString()
            ?: project.property("platformType").toString()
    )
    updateSinceUntilBuild.set(true)
    // Always include Kotlin and JavaScript plugins
    val bundled = project.property("platformBundledPlugins").toString()
        .split(',').filter { it.isNotEmpty() }
    // Add JavaScript plugin for JSX support
    plugins.set((bundled + "JavaScript").distinct())
}

// Configure Gradle tasks
tasks {
    // Exclude backup plugin descriptor
    processResources {
        exclude("META-INF/plugin.xml.bak")
    }
    // Patch plugin.xml
    patchPluginXml {
        sinceBuild.set(project.property("pluginSinceBuild").toString())
        val untilBuildValue = project.property("pluginUntilBuild").toString()
        if (untilBuildValue.isNotBlank()) {
            untilBuild.set(untilBuildValue)
        } else {
            // Empty string means support all future versions
            untilBuild.set("")
        }

        // Plugin description
        pluginDescription.set(
            """
            <h1>Tailwind CSS Smart Plugin</h1>
<p>Elevate your Tailwind CSS development experience in IntelliJ IDEs with smart features designed for speed, precision, and enhanced readability.</p>

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
    <li>
        <strong>Class Management & Organization:</strong>
        <ul>
            <li><strong>Smart Class Sorting:</strong> Organize Tailwind classes in a logical order based on property relationships.</li>
            <li><strong>File-wide Class Sorting:</strong> Sort all Tailwind classes throughout an entire file with a single shortcut.</li>
            <li><strong>Class Folding:</strong> Collapse long class lists to improve readability of your templates and components.</li>
            <li><strong>One-click Folding/Unfolding:</strong> Toggle, fold, or unfold all Tailwind classes across an entire file.</li>
            <li><strong>Keyboard Shortcuts:</strong> Dedicated shortcuts for all operations (sorting, folding, unfolding).</li>
        </ul>
    </li>
    <li><strong>Automatic Project Setup:</strong> Detects your project's Tailwind configuration (<code>tailwind.config.js</code>) on startup to tailor suggestions and documentation.</li>
</ul>
            
            <h2>✨ Fuel the Future of This Plugin</h2>
            <div style="padding: 15px; background-color: #f0f9ff; border-radius: 8px; margin: 15px 0; border-left: 4px solid #0ea5e9;">
                <p style="font-weight: 500;">Every day you use this plugin, <strong>you save precious development time</strong>. What would you do with an <em>extra hour each week</em>?</p>
                <p>As one of the <strong>first 150 developers</strong> to discover this tool, you have a unique opportunity to influence what features get prioritized next!</p>
                <div style="text-align: center; margin: 15px 0;">
                    <a href="https://ko-fi.com/dilika" style="display: inline-block; background-color: #0ea5e9; color: white; font-weight: bold; padding: 10px 25px; text-decoration: none; border-radius: 6px; box-shadow: 0 2px 5px rgba(14, 165, 233, 0.2);">☕️ Support Development</a>
                </div>
                <p style="font-size: 0.9em;">Your contribution directly translates to:</p>
                <ul style="font-size: 0.9em; margin-top: 5px;">
                    <li><strong>New features</strong> you'll actually use (supporters get priority feature requests)</li>
                    <li><strong>Lightning-fast updates</strong> when Tailwind releases new versions</li>
                    <li><strong>Personalized support</strong> when you need assistance</li>
                </ul>
                <p style="font-size: 0.85em; font-style: italic; margin-top: 10px;">"The plugin has already saved me hours of work. Supporting its development was an easy decision." – Recent Contributor</p>
            </div>
        """.trimIndent()
        )

        // Change notes
        changeNotes.set(
            """
            <h2>1.2.2</h2>
<ul>
    <li>
        <strong>New Class Management Features:</strong>
        <ul>
            <li><strong>File-wide Class Sorting:</strong> New command to sort all Tailwind classes in a file in one action (Shift+Alt+F7)</li>
            <li><strong>Global Folding and Unfolding:</strong> New actions to fold (Shift+Alt+F8) or unfold (Shift+Ctrl+F8) all Tailwind class lists in a file</li>
            <li>Created a dedicated menu grouping all Tailwind functionality</li>
        </ul>
    </li>
    <li>
        <strong>Completion Improvements:</strong>
        <ul>
            <li>Improved accuracy for color extraction from Tailwind classes</li>
            <li>Added support for Tailwind v4 with correct color icons for all classes</li>
            <li>Performance optimization to prevent IDE slowdowns</li>
        </ul>
    </li>
    <li>
        <strong>Bug Fixes:</strong>
        <ul>
            <li>Fixed issues with class sorting not working properly</li>
            <li>Resolved problems with incorrect color icons for certain classes</li>
            <li>Fixed build errors related to JBColor values</li>
        </ul>
    </li>
</ul>

<h2>1.2.1</h2>
<ul>
  <li><strong>Enhanced Relevance:</strong> Intelligently sorted suggestions based on your typing for an optimal experience</li>
  <li><strong>Extended Coverage:</strong> Complete support for common Tailwind classes (inline-block, px-3, py-1, text-indigo-600, etc.)</li>
  <li><strong>Optimizations:</strong> Improved performance and stability of auto-completion</li>
  <li><strong>Expanded Compatibility:</strong> Better support for various frameworks and languages</li>
</ul>
        """.trimIndent()
        )
    }

    // Configure the getChangelog task from the changelog plugin
    getChangelog {
        version = project.property("pluginVersion").toString()
        // Use this task for GitHub Actions instead of our custom task
    }

    // Test configuration
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
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

// Fix plugin.xml issue with <n> tag by creating a custom task to modify the JAR file after build
tasks.register("fixPluginXml") {
    // Capture the plugin version during configuration phase
    val pluginVersion = project.property("pluginVersion").toString()
    
    dependsOn(tasks.buildPlugin)
    doLast {
        val jarFile = layout.buildDirectory.file(
            "idea-sandbox/plugins/tailwind-smart-plugin/lib/instrumented-tailwind-smart-plugin-$pluginVersion.jar"
        ).get().asFile
        
        if (jarFile.exists()) {
            println("Fixing plugin.xml in ${jarFile.absolutePath}")
            val tempDir = layout.buildDirectory.dir("tmp/fixPluginXml").get().asFile
            tempDir.deleteRecursively()
            tempDir.mkdirs()

            // Extract JAR contents using Process API to avoid deprecation warnings
            val extractProcess = ProcessBuilder("jar", "-xf", jarFile.absolutePath)
                .directory(tempDir)
                .redirectErrorStream(true)
                .start()
            extractProcess.waitFor()

            // Fix plugin.xml
            val pluginXml = file("${tempDir}/META-INF/plugin.xml")
            if (pluginXml.exists()) {
                val content = pluginXml.readText()
                val fixedContent = content.replace("<n>Tailwind CSS Support</n>", "<name>Tailwind CSS Support</name>")
                pluginXml.writeText(fixedContent)
                println("Fixed plugin.xml content: ${fixedContent.contains("<name>Tailwind CSS Support</name>")}")
            } else {
                println("Warning: Could not find plugin.xml in the extracted JAR!")
            }

            // Recreate JAR using Process API
            jarFile.delete()
            val createProcess = ProcessBuilder("jar", "-cf", jarFile.absolutePath, ".")
                .directory(tempDir)
                .redirectErrorStream(true)
                .start()
            createProcess.waitFor()

            println("Plugin XML fixed successfully in ${jarFile.absolutePath}")
        } else {
            println("Warning: Could not find JAR file to fix at ${jarFile.absolutePath}")
        }
    }
}

// Make build task depend on fixPluginXml
tasks.build {
    finalizedBy("fixPluginXml")
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
