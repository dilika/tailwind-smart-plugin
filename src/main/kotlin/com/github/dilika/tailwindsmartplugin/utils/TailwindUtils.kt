package com.github.dilika.tailwindsmartplugin.utils
package com.github.dilika.tailwindsmartplugin.utils

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.github.dilika.tailwindsmartplugin.services.TailwindConfigService
import org.json.JSONObject

/**
 * Utility class for Tailwind CSS related operations.
 */
object TailwindUtils {
    private val logger = Logger.getInstance(TailwindUtils::class.java)
    
    /**
     * Get all Tailwind CSS classes available for the project.
     */
    fun getTailwindClasses(project: Project): List<String> {
        return try {
            val projectPath = project.basePath ?: ""
            TailwindConfigService.getInstance().getTailwindClasses(projectPath)
        } catch (e: Exception) {
            logger.error("Error getting Tailwind classes: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get detailed data for all Tailwind CSS classes.
     */
    fun getTailwindClassData(project: Project): Map<String, JSONObject> {
        return try {
            val projectPath = project.basePath ?: ""
            TailwindConfigService.getInstance().getTailwindClassData(projectPath)
        } catch (e: Exception) {
            logger.error("Error getting Tailwind class data: ${e.message}")
            emptyMap()
        }
    }
}
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.graalvm.polyglot.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.script.Invocable
import javax.script.ScriptException
import java.util.concurrent.ConcurrentHashMap

object TailwindUtils {
    private val logger = Logger.getInstance("TailwindUtils")
    private var defaultTailwindClasses: List<String>? = null

    // Project data cache
    private val projectClassesCache = ConcurrentHashMap<String, List<String>>()
    private val projectClassDataCache = ConcurrentHashMap<String, Map<String, JSONObject>>()

    // JavaScript engine for running the parser
    private val jsEngine: Context? by lazy {
        try {
            val context = Context.newBuilder("js")
                .allowAllAccess(true) // Allow access to Java from JS
                .build()
            logger.info("[Tailwind] Using GraalVM JavaScript engine")
            context
        } catch (e: Exception) {
            logger.error("[Tailwind] Failed to initialize GraalVM JavaScript engine: ${e.message}", e)
            null
        }
    }

    /**
     * Checks if an exception is or contains a ProcessCanceledException
     * In this case, we need to rethrow the exception rather than logging it
     */
    private fun isCancellationException(e: Throwable): Boolean {
        return when {
            e is com.intellij.openapi.progress.ProcessCanceledException -> true
            e.cause is com.intellij.openapi.progress.ProcessCanceledException -> true
            e.cause?.cause is com.intellij.openapi.progress.ProcessCanceledException -> true
            else -> false
        }
    }

    /**
     * Get all available Tailwind CSS classes for a project
     */
    fun getTailwindClasses(project: Project? = null): List<String> {
        // If a project is specified, try to load its specific classes
        if (project != null) {
            val projectId = project.locationHash

            // Check project cache
            if (projectClassesCache.containsKey(projectId)) {
                return projectClassesCache[projectId]!!
            }

            // Try to load custom classes from the project
            try {
                val detector = TailwindConfigDetector(project)
                val isTailwindProject = try {
                    detector.detectTailwindUsage()
                } catch (e: Exception) {
                    // Check if it's a cancellation exception
                    if (isCancellationException(e)) {
                        throw e // Rethrow ProcessCanceledException
                    }
                    logger.warn("[Tailwind] Error detecting Tailwind, assuming it's a Tailwind project: ${e.message}")
                    true // Assume it's a Tailwind project even if detection fails
                }

                if (isTailwindProject) {
                    logger.info("[Tailwind] Tailwind project detected, extracting custom classes")
                    val customClasses = try {
                        detector.extractTailwindClasses()
                    } catch (e: Exception) {
                        // Check if it's a cancellation exception
                        if (isCancellationException(e)) {
                            throw e // Rethrow ProcessCanceledException
                        }
                        logger.warn("[Tailwind] Error extracting custom classes: ${e.message}")
                        emptyList() // Return empty list if extraction fails
                    }

                    // Combine with default classes
                    val defaultClasses = getDefaultTailwindClasses()
                    val allClasses = (defaultClasses + customClasses).distinct()

                    // Cache the result
                    projectClassesCache[projectId] = allClasses

                    return allClasses
                }
            } catch (e: Exception) {
                // Check if it's a cancellation exception
                if (isCancellationException(e)) {
                    throw e // Rethrow ProcessCanceledException
                }
                logger.error("[Tailwind] Error extracting custom classes: ${e.message}")
                // Fall through to return default classes
            }
        }

        // Fallback: return default classes
        return getDefaultTailwindClasses()
    }

    /**
     * Get rich data for Tailwind CSS classes
     */
    fun getTailwindClassData(project: Project? = null): Map<String, JSONObject> {
        // If a project is specified, try to load its specific data
        if (project != null) {
            val projectId = project.locationHash

            // Check project cache
            if (projectClassDataCache.containsKey(projectId)) {
                return projectClassDataCache[projectId]!!
            }

            try {
                // Get classes for this project
                val classes = getTailwindClasses(project)

                // Initialize data for these classes
                val classData = initializeClassParser(classes)

                // Cache the result
                projectClassDataCache[projectId] = classData

                return classData
            } catch (e: Exception) {
                // Check if it's a cancellation exception
                if (isCancellationException(e)) {
                    throw e // Rethrow ProcessCanceledException
                }
                logger.error("[Tailwind] Error initializing class data for project: ${e.message}")
            }
        }

        // Fallback: generate data for default classes
        if (defaultTailwindClasses === null) {
            defaultTailwindClasses = getDefaultTailwindClasses()
        }

        try {
            return initializeClassParser(defaultTailwindClasses!!)
        } catch (e: Exception) {
            // Check if it's a cancellation exception
            if (isCancellationException(e)) {
                throw e // Rethrow ProcessCanceledException
            }
            logger.error("[Tailwind] Failed to initialize class data", e)
            return createFallbackClassData(defaultTailwindClasses!!)
        }
    }

    /**
     * Get default Tailwind classes from embedded resource
     */
    private fun getDefaultTailwindClasses(): List<String> {
        if (defaultTailwindClasses != null) return defaultTailwindClasses!!

        val inputStream = javaClass.classLoader.getResourceAsStream("tailwind-classes.json")
        if (inputStream != null) {
            val content = inputStream.bufferedReader().readText()
            val jsonArray = JSONArray(content)
            defaultTailwindClasses = (0 until jsonArray.length()).map { jsonArray.getString(it) }
        } else {
            logger.warn("tailwind-classes.json not found, using fallback class list!")
            defaultTailwindClasses = loadDefaultTailwindClasses()
        }

        return defaultTailwindClasses!!
    }

    /**
     * Initialize the ClassParser and get class data
     */
    private fun initializeClassParser(classes: List<String>): Map<String, JSONObject> {
        try {
            // Check that the JS engine is available
            val engine = jsEngine ?: return createFallbackClassData(classes)

            // Load the parser script
            val parserScript = loadParserScript()
            if (parserScript.isBlank()) {
                logger.error("Failed to load parser script")
                return createFallbackClassData(classes)
            }

            // Add polyfill for modern JS features if needed
            val polyfill = """
                if (typeof global === 'undefined') {
                    var global = this;
                }
                if (typeof console === 'undefined') {
                    var console = { 
                        log: print, 
                        warn: print, 
                        error: print 
                    };
                }

                // Object.freeze polyfill (if not already defined, replaced with a no-op function)
                if (typeof Object.freeze === 'undefined') {
                    Object.freeze = function(obj) { return obj; };
                }

                // Object.assign polyfill
                if (typeof Object.assign !== 'function') {
                    Object.assign = function(target) {
                        if (target == null) {
                            throw new TypeError('Cannot convert undefined or null to object');
                        }
                        var to = Object(target);
                        for (var i = 1; i < arguments.length; i++) {
                            var nextSource = arguments[i];
                            if (nextSource != null) {
                                for (var nextKey in nextSource) {
                                    if (Object.prototype.hasOwnProperty.call(nextSource, nextKey)) {
                                        to[nextKey] = nextSource[nextKey];
                                    }
                                }
                            }
                        }
                        return to;
                    };
                }
            """.trimIndent()

            // Evaluate the scripts
            try {
                // Evaluate polyfill first
                try {
                    engine.eval("js",polyfill)
                } catch (e: Exception) {
                    // Vérifier s'il s'agit d'une exception d'annulation
                    if (isCancellationException(e)) {
                        throw e // Relancer les exceptions de type ProcessCanceledException
                    }
                    logger.error("Error evaluating polyfill: ${e.message}")
                    return createFallbackClassData(classes)
                }

                // Then try parser script
                try {
                    // Final check for common syntax errors
                    if (parserScript.contains("function = function") ||
                        parserScript.contains("function.prototype.function")) {
                        logger.error("[Tailwind] Invalid JavaScript syntax detected, attempting correction...")
                        val correctedScript = parserScript
                            .replace("function = function", "function")
                            .replace("function.prototype.function", "function")
                        engine.eval("js",correctedScript)
                    } else {
                        engine.eval("js",parserScript)
                    }
                    logger.info("[Tailwind] Successfully evaluated parser script")
                } catch (e: Exception) {
                    // Check if it's a cancellation exception
                    if (isCancellationException(e)) {
                        throw e // Rethrow ProcessCanceledException
                    }

                    logger.error("Error evaluating parser script: ${e.message}", e)
                    // Try to log the problematic lines for debugging
                    val lines = parserScript.lines()
                    val errorLine = e.message?.let {
                        val match = Regex("<eval>:(\\d+)").find(it)
                        match?.groupValues?.get(1)?.toIntOrNull()
                    }

                    if (errorLine != null && errorLine <= lines.size) {
                        val startLine = maxOf(0, errorLine - 3)
                        val endLine = minOf(lines.size, errorLine + 3)

                        logger.error("Error context (lines ${startLine+1}-${endLine}):")
                        for (i in startLine until endLine) {
                            logger.error("${i+1}: ${lines[i]}")
                        }
                    }

                    return createFallbackClassData(classes)
                }
            } catch (e: ScriptException) {
                // Vérifier s'il s'agit d'une exception d'annulation
                if (isCancellationException(e)) {
                    throw e // Relancer les exceptions de type ProcessCanceledException
                }

                logger.error("Error during script evaluation: ${e.message}", e)
                return createFallbackClassData(classes)
            }

            // Check if engine supports Invocable
//            val invocable = engine as? Invocable
//            if (invocable == null) {
//                logger.error("JavaScript engine does not support Invocable")
//                return createFallbackClassData(classes)
//            }

            // Vérifier que la fonction createTailwindParser est bien disponible dans le contexte JS
            val bindings = engine.getBindings("js")
            val createParserFn = bindings.getMember("createTailwindParser")

            if (createParserFn == null || !createParserFn.canExecute()) {
                logger.error("JavaScript function 'createTailwindParser' is not available or not executable")
                return createFallbackClassData(classes)
            }


            // Create configuration safely using a simple string
            val classesJson = classes.joinToString(",") { "\"$it\"" }
            val configJson = "{\"classes\": [$classesJson]}"

            try {
                // Vérifier que ImprovedClassParser existe dans le contexte JS
                try {
                    // Approche plus sûre pour vérifier l'existence
                    val exists = engine.eval("js","""
                        typeof ImprovedClassParser !== 'undefined' && 
                        (typeof ImprovedClassParser === 'function' || 
                         typeof ImprovedClassParser === 'object')
                    """)

                    if (exists == null || !exists.asBoolean()) {
                        logger.error("ImprovedClassParser not properly defined: $exists")
                        return createFallbackClassData(classes)
                    }
                } catch (e: Exception) {
                    if (isCancellationException(e)) {
                        throw e // Relancer les exceptions de type ProcessCanceledException
                    }
                    logger.error("Error checking ImprovedClassParser type: ${e.message}", e)
                    return createFallbackClassData(classes)
                }

                // Créer l'instance du parser en utilisant une fonction JavaScript intermédiaire
                val parser: Any?
                try {
                    // Créons une fonction d'aide en JavaScript pour créer le parser
                    engine.eval("js","""
                        function createTailwindParser(config) {
                            try {
                                return ImprovedClassParser.create(config);
                            } catch(e) {
                                print('Error creating parser: ' + e);
                                return null;
                            }
                        }
                    """)

                    // Appel direct de la fonction helper avec notre configuration JSON
                    parser = engine.eval("js","createTailwindParser($configJson)")

                    if (parser == null) {
                        logger.error("Failed to create parser instance")
                        return createFallbackClassData(classes)
                    }
                } catch (e: Exception) {
                    if (isCancellationException(e)) {
                        throw e // Relancer les exceptions de type ProcessCanceledException
                    }
                    logger.error("Error creating parser instance: ${e.message}", e)
                    return createFallbackClassData(classes)
                }

                // Process all classes
                val result = mutableMapOf<String, JSONObject>()

                try {
                    // Créer une fonction d'aide pour appeler parse
                    engine.eval("js","""
                        function parseTailwindClass(parser, className) {
                            try {
                                return JSON.stringify(parser.parse("", [className])[className]);
                            } catch(e) {
                                print('Error parsing class ' + className + ': ' + e);
                                return null;
                            }
                        }
                    """)

                    // Parser chaque classe
                    for (className in classes) {
                        try {
                            val resultValue = engine.eval("js", "parseTailwindClass(parser, '$className')")
                            val jsonString = if (resultValue.isString) resultValue.asString() else null

                            if (jsonString != null) {
                                result[className] = JSONObject(jsonString)
                            } else {
                                result[className] = createFallbackClassEntry(className)
                            }
                        } catch (e: Exception) {
                            if (isCancellationException(e)) {
                                throw e // Relancer les exceptions de type ProcessCanceledException
                            }
                            logger.warn("Failed to parse class: $className", e)
                            result[className] = createFallbackClassEntry(className)
                        }
                    }
                } catch (e: Exception) {
                    if (isCancellationException(e)) {
                        throw e // Relancer les exceptions de type ProcessCanceledException
                    }
                    logger.error("Error setting up class parsing: ${e.message}", e)
                    return createFallbackClassData(classes)
                }

                logger.info("Successfully parsed ${result.size} Tailwind classes with JavaScript parser")
                return result
            } catch (e: Exception) {
                if (isCancellationException(e)) {
                    throw e // Relancer les exceptions de type ProcessCanceledException
                }
                logger.error("Error in class parser execution: ${e.message}", e)
                return createFallbackClassData(classes)
            }
        } catch (e: Exception) {
            if (isCancellationException(e)) {
                throw e // Relancer les exceptions de type ProcessCanceledException
            }
            logger.error("Error in class parser initialization", e)
            return createFallbackClassData(classes)
        }
    }

    /**
     * Load the parser script from resources
     */
    private fun loadParserScript(): String {
        var scriptContent = ""

        // List of possible paths where the script might be found
        val possiblePaths = listOf(
            "ImprovedClassParser.js", 
            "ml/dilika/tailwindintellijplugin/tailwind-class-extractor/lib/parsers/ImprovedClassParser.js",
            "tailwind-class-extractor/lib/parsers/ImprovedClassParser.js", 
            "parsers/ImprovedClassParser.js",
            "tailwind-parser.js"
        )

        // Try to load from resources
        for (path in possiblePaths) {
            val resourceStream = javaClass.classLoader.getResourceAsStream(path)
            if (resourceStream != null) {
                scriptContent = BufferedReader(InputStreamReader(resourceStream)).readText()
                logger.info("[Tailwind] Parser script found in resources: $path")
                break
            }
        }

        // If not found in resources, try to load from the file system
        if (scriptContent.isBlank()) {
            try {
                val pluginPath = javaClass.protectionDomain.codeSource.location.path
                val basePath = pluginPath.substringBeforeLast("/")

                for (scriptName in listOf("ImprovedClassParser.js", "tailwind-parser.js")) {
                    val scriptFile = java.io.File("$basePath/$scriptName")
                    if (scriptFile.exists()) {
                        logger.info("[Tailwind] Parser script found in plugin directory: ${scriptFile.absolutePath}")
                        scriptContent = scriptFile.readText()
                        break
                    }
                }
            } catch (e: Exception) {
                logger.warn("[Tailwind] Error searching for script in file system: ${e.message}")
            }
        }

        // If still not found, log and return an empty string
        if (scriptContent.isBlank()) {
            logger.error("[Tailwind] Parser script not found!")
            return ""
        }

        // Replace problematic patterns directly
        scriptContent = scriptContent.replace(
            "var ImprovedClassParser = function = function", 
            "var ImprovedClassParser = function"
        )

        scriptContent = scriptContent.replace(
            "var ImprovedClassParser = function.prototype.function = function", 
            "var ImprovedClassParser = function"
        )

        scriptContent = scriptContent.replace(
            "function.prototype.function", 
            "function"
        )

        // Specifically replace spread operators in objects
        // For example: { ...TYPE_METADATA, ...config.types } => Object.assign({}, TYPE_METADATA, config.types)
        scriptContent = scriptContent.replace(
            Regex("this\\._typeMetadata\\s*=\\s*\\{\\s*\\.\\.\\.([A-Z_]+),\\s*\\.\\.\\.([^}]+)\\}"),
            "this._typeMetadata = Object.assign({}, $1, $2)"
        )

        scriptContent = scriptContent.replace(
            Regex("this\\._variants\\s*=\\s*\\{\\s*\\.\\.\\.([A-Z_]+),\\s*\\.\\.\\.([^}]+)\\}"),
            "this._variants = Object.assign({}, $1, $2)"
        )

        scriptContent = scriptContent.replace(
            Regex("this\\._colorPalette\\s*=\\s*\\{\\s*\\.\\.\\.([A-Z_]+),\\s*\\.\\.\\.([^}]+)\\}"),
            "this._colorPalette = Object.assign({}, $1, $2)"
        )

        // General rule for other spread operators in objects
        scriptContent = scriptContent.replace(
            Regex("\\{\\s*\\.\\.\\.([^,}]+)\\s*,\\s*\\.\\.\\.([^}]+)\\s*\\}"),
            "Object.assign({}, $1, $2)"
        )

        return scriptContent
    }

    /**
     * Create fallback class data for when the JS engine fails
     */
    private fun createFallbackClassData(classes: List<String>): Map<String, JSONObject> {
        val result = mutableMapOf<String, JSONObject>()
        for (className in classes) {
            result[className] = createFallbackClassEntry(className)
        }
        return result
    }

    /**
     * Create a fallback entry for a single class
     */
    private fun createFallbackClassEntry(className: String): JSONObject {
        // Determine class type from name for basic styling
        val type = when {
            className.startsWith("bg-") -> "background"
            className.startsWith("text-") -> "typography"
            className.startsWith("m-") || className.startsWith("p-") -> "spacing"
            className.startsWith("flex") || className.startsWith("grid") -> "layout"
            className.startsWith("w-") || className.startsWith("h-") -> "sizing"
            className.startsWith("border") && !className.startsWith("border-") -> "border"
            className.startsWith("border-") -> "border"
            className.startsWith("rounded") -> "border"
            else -> "utility"
        }

        // Extract color from class name if it's a color-related class
        val extractedColor = extractColorFromClassName(className)

        // Determine color based on type or use extracted color
        val color = when {
            extractedColor != null -> extractedColor
            type == "background" -> "#10b981"
            type == "typography" -> "#f59e0b"
            type == "spacing" -> "#8b5cf6"
            type == "layout" -> "#38bdf8"
            type == "sizing" -> "#ec4899"
            type == "border" -> "#6366f1"
            else -> "#64748b"
        }

        // Create a simple JSON structure similar to what the JS parser would return
        return JSONObject().apply {
            put("completion", JSONObject().apply {
                put("text", className)
                put("displayText", "$className")
                put("style", "color: $color;")
                put("relevance", 100)
                put("type", type)
            })

            put("documentation", JSONObject().apply {
                put("description", "Tailwind CSS $type class")
                put("cssProperties", JSONArray().apply {
                    put(inferCssProperty(className))
                })
                put("cssValue", inferCssValue(className))
                put("examples", JSONArray().apply {
                    put("Example: <div class=\"$className\">Content</div>")
                })
            })

            put("keywords", JSONArray().apply {
                put(className)
                className.split("-").forEach { put(it) }
            })

            put("meta", JSONObject().apply {
                put("isExactMatch", true)
                put("isArbitrary", false)
                put("usage", 0)
            })
        }
    }

    /**
     * Extract color from Tailwind class name
     */
    private fun extractColorFromClassName(className: String): String? {
        // Tailwind color mapping (simplified)
        val colorMap = mapOf(
            // Slate
            "slate-50" to "#f8fafc", "slate-100" to "#f1f5f9", "slate-200" to "#e2e8f0", 
            "slate-300" to "#cbd5e1", "slate-400" to "#94a3b8", "slate-500" to "#64748b",
            "slate-600" to "#475569", "slate-700" to "#334155", "slate-800" to "#1e293b", 
            "slate-900" to "#0f172a", "slate-950" to "#020617",

            // Gray
            "gray-50" to "#f9fafb", "gray-100" to "#f3f4f6", "gray-200" to "#e5e7eb",
            "gray-300" to "#d1d5db", "gray-400" to "#9ca3af", "gray-500" to "#6b7280",
            "gray-600" to "#4b5563", "gray-700" to "#374151", "gray-800" to "#1f2937",
            "gray-900" to "#111827", "gray-950" to "#030712",

            // Red
            "red-50" to "#fef2f2", "red-100" to "#fee2e2", "red-200" to "#fecaca",
            "red-300" to "#fca5a5", "red-400" to "#f87171", "red-500" to "#ef4444",
            "red-600" to "#dc2626", "red-700" to "#b91c1c", "red-800" to "#991b1b",
            "red-900" to "#7f1d1d", "red-950" to "#450a0a",

            // Orange
            "orange-50" to "#fff7ed", "orange-100" to "#ffedd5", "orange-200" to "#fed7aa",
            "orange-300" to "#fdba74", "orange-400" to "#fb923c", "orange-500" to "#f97316",
            "orange-600" to "#ea580c", "orange-700" to "#c2410c", "orange-800" to "#9a3412",
            "orange-900" to "#7c2d12", "orange-950" to "#431407",

            // Yellow
            "yellow-50" to "#fefce8", "yellow-100" to "#fef9c3", "yellow-200" to "#fef08a",
            "yellow-300" to "#fde047", "yellow-400" to "#facc15", "yellow-500" to "#eab308",
            "yellow-600" to "#ca8a04", "yellow-700" to "#a16207", "yellow-800" to "#854d0e",
            "yellow-900" to "#713f12", "yellow-950" to "#422006",

            // Green
            "green-50" to "#f0fdf4", "green-100" to "#dcfce7", "green-200" to "#bbf7d0",
            "green-300" to "#86efac", "green-400" to "#4ade80", "green-500" to "#22c55e",
            "green-600" to "#16a34a", "green-700" to "#15803d", "green-800" to "#166534",
            "green-900" to "#14532d", "green-950" to "#052e16",

            // Blue
            "blue-50" to "#eff6ff", "blue-100" to "#dbeafe", "blue-200" to "#bfdbfe", 
            "blue-300" to "#93c5fd", "blue-400" to "#60a5fa", "blue-500" to "#3b82f6",
            "blue-600" to "#2563eb", "blue-700" to "#1d4ed8", "blue-800" to "#1e40af", 
            "blue-900" to "#1e3a8a", "blue-950" to "#172554",

            // Indigo
            "indigo-50" to "#eef2ff", "indigo-100" to "#e0e7ff", "indigo-200" to "#c7d2fe",
            "indigo-300" to "#a5b4fc", "indigo-400" to "#818cf8", "indigo-500" to "#6366f1",
            "indigo-600" to "#4f46e5", "indigo-700" to "#4338ca", "indigo-800" to "#3730a3",
            "indigo-900" to "#312e81", "indigo-950" to "#1e1b4b",

            // Purple
            "purple-50" to "#faf5ff", "purple-100" to "#f3e8ff", "purple-200" to "#e9d5ff",
            "purple-300" to "#d8b4fe", "purple-400" to "#c084fc", "purple-500" to "#a855f7",
            "purple-600" to "#9333ea", "purple-700" to "#7e22ce", "purple-800" to "#6b21a8",
            "purple-900" to "#581c87", "purple-950" to "#3b0764",

            // Pink
            "pink-50" to "#fdf2f8", "pink-100" to "#fce7f3", "pink-200" to "#fbcfe8",
            "pink-300" to "#f9a8d4", "pink-400" to "#f472b6", "pink-500" to "#ec4899",
            "pink-600" to "#db2777", "pink-700" to "#be185d", "pink-800" to "#9d174d",
            "pink-900" to "#831843", "pink-950" to "#500724",

            // Base colors
            "white" to "#ffffff", 
            "black" to "#000000",
            "transparent" to "transparent"
        )

        // Extract color part from class name (e.g., "red-500" from "bg-red-500")
        val parts = className.split("-")
        if (parts.size < 3) {
            // Check for simple colors like "bg-red", "text-blue"
            if (parts.size == 2 && (className.startsWith("bg-") || className.startsWith("text-") || className.startsWith("border-"))) {
                val colorName = parts[1]
                if (colorName == "white") return "#ffffff"
                if (colorName == "black") return "#000000"
                if (colorName == "transparent") return "transparent"
                if (colorName == "current") return "currentColor"
            }
            return null
        }

        val prefixParts = parts.subList(0, 1)
        // Check if this is a color class (bg-*, text-*, border-*)
        if (!(prefixParts.contains("bg") || prefixParts.contains("text") || prefixParts.contains("border"))) {
            return null
        }

        // Try to get color-shade (e.g., "red-500")
        val colorPart = parts.subList(1, parts.size).joinToString("-")
        return colorMap[colorPart]
    }

    /**
     * Infer a CSS property from a class name
     */
    private fun inferCssProperty(className: String): String {
        return when {
            className.startsWith("m-") -> "margin"
            className.startsWith("mt-") -> "margin-top"
            className.startsWith("mr-") -> "margin-right"
            className.startsWith("mb-") -> "margin-bottom"
            className.startsWith("ml-") -> "margin-left"
            className.startsWith("mx-") -> "margin-left, margin-right"
            className.startsWith("my-") -> "margin-top, margin-bottom"
            className.startsWith("p-") -> "padding"
            className.startsWith("pt-") -> "padding-top"
            className.startsWith("pr-") -> "padding-right"
            className.startsWith("pb-") -> "padding-bottom"
            className.startsWith("pl-") -> "padding-left"
            className.startsWith("px-") -> "padding-left, padding-right"
            className.startsWith("py-") -> "padding-top, padding-bottom"
            className.startsWith("text-") -> "color"
            className.startsWith("bg-") -> "background-color"
            className.startsWith("w-") -> "width"
            className.startsWith("h-") -> "height"
            className.startsWith("border") -> "border"
            className.startsWith("rounded") -> "border-radius"
            className.startsWith("flex") -> "display: flex"
            className.startsWith("grid") -> "display: grid"
            else -> "various"
        }
    }

    /**
     * Infer a CSS value from a class name
     */
    private fun inferCssValue(className: String): String {
        val parts = className.split("-")
        if (parts.size < 2) return ""

        val value = parts.last()

        // Size values
        return when {
            value == "0" -> "0"
            value == "px" -> "1px"
            value == "auto" -> "auto"
            value == "full" -> "100%"
            value.toIntOrNull() != null -> "${value.toInt() / 4.0}rem"
            else -> value
        }
    }

    /**
     * Load default Tailwind CSS classes if the JSON file is not found
     */
    private fun loadDefaultTailwindClasses(): List<String> {
        // Common Tailwind classes
        val layoutClasses = listOf(
            "container", "flex", "inline-flex", "grid", "inline-grid", "block", "inline-block", "inline", "table", "table-row", "table-cell",
            "hidden", "flow-root", "contents", "list-item", "relative", "absolute", "fixed", "sticky", "static", "isolate", "isolation-auto",
            "object-contain", "object-cover", "object-fill", "object-none", "object-scale-down", "overflow-auto", "overflow-hidden",
            "overflow-visible", "overflow-scroll", "overflow-x-auto", "overflow-y-auto", "overflow-x-hidden", "overflow-y-hidden",
            "z-0", "z-10", "z-20", "z-30", "z-40", "z-50", "z-auto"
        )

        val spacingValues = listOf("0", "0.5", "1", "1.5", "2", "2.5", "3", "3.5", "4", "5", "6", "7", "8", "9", "10", "11", "12", 
                                  "14", "16", "20", "24", "28", "32", "36", "40", "44", "48", "52", "56", "60", "64", "72", "80", "96", "auto", "px", "full")

        // Spacing classes with prefix-value patterns
        val spacingPrefixes = listOf("p", "px", "py", "pt", "pr", "pb", "pl", "m", "mx", "my", "mt", "mr", "mb", "ml", "space-x", "space-y", "gap", "gap-x", "gap-y")
        val spacingClasses = spacingPrefixes.flatMap { prefix -> 
            spacingValues.map { value -> "$prefix-$value" }
        }

        // Typography
        val textSizes = listOf("xs", "sm", "base", "lg", "xl", "2xl", "3xl", "4xl", "5xl", "6xl", "7xl", "8xl", "9xl")
        val fontWeights = listOf("thin", "extralight", "light", "normal", "medium", "semibold", "bold", "extrabold", "black")
        val textAligns = listOf("left", "center", "right", "justify", "start", "end")

        val typographyClasses = 
            textSizes.map { "text-$it" } +
            fontWeights.map { "font-$it" } + 
            textAligns.map { "text-$it" } +
            listOf("italic", "not-italic", "uppercase", "lowercase", "capitalize", "normal-case", "truncate", "overflow-ellipsis", "overflow-clip")

        // Colors - basic variations for most commonly used colors
        val colorNames = listOf("slate", "gray", "zinc", "neutral", "stone", "red", "orange", "amber", "yellow", "lime", "green", "emerald", "teal", "cyan", "sky", "blue", "indigo", "violet", "purple", "fuchsia", "pink", "rose")
        val colorShades = listOf("50", "100", "200", "300", "400", "500", "600", "700", "800", "900", "950")
        val colorPrefixes = listOf("text", "bg", "border")

        val colorClasses = colorPrefixes.flatMap { prefix ->
            colorNames.flatMap { color ->
                colorShades.map { shade -> "$prefix-$color-$shade" }
            }
        } + colorPrefixes.flatMap { prefix -> listOf("$prefix-black", "$prefix-white", "$prefix-transparent", "$prefix-current") }

        // Flexbox & Grid
        val flexboxClasses = listOf(
            "flex-row", "flex-row-reverse", "flex-col", "flex-col-reverse", "flex-wrap", "flex-wrap-reverse", "flex-nowrap",
            "justify-start", "justify-end", "justify-center", "justify-between", "justify-around", "justify-evenly",
            "items-start", "items-end", "items-center", "items-baseline", "items-stretch",
            "self-auto", "self-start", "self-end", "self-center", "self-stretch",
            "flex-1", "flex-auto", "flex-initial", "flex-none", "flex-grow", "flex-grow-0", "flex-shrink", "flex-shrink-0"
        )

        val gridClasses = listOf(
            "grid-cols-1", "grid-cols-2", "grid-cols-3", "grid-cols-4", "grid-cols-5", "grid-cols-6", "grid-cols-7", "grid-cols-8", "grid-cols-9", "grid-cols-12", "grid-cols-none",
            "grid-rows-1", "grid-rows-2", "grid-rows-3", "grid-rows-4", "grid-rows-5", "grid-rows-6", "grid-rows-none",
            "col-auto", "col-span-1", "col-span-2", "col-span-3", "col-span-4", "col-span-5", "col-span-6", "col-span-7", "col-span-8", "col-span-9", "col-span-10", "col-span-11", "col-span-12", "col-span-full",
            "row-auto", "row-span-1", "row-span-2", "row-span-3", "row-span-4", "row-span-5", "row-span-6", "row-span-full"
        )

        // Border
        val borderWidths = listOf("", "0", "2", "4", "8")
        val borderStyles = listOf("solid", "dashed", "dotted", "double", "none")
        val radiusSizes = listOf("none", "sm", "md", "lg", "xl", "2xl", "3xl", "full")

        val borderClasses = borderWidths.map { if(it.isEmpty()) "border" else "border-$it" } +
                            borderStyles.map { "border-$it" } +
                            radiusSizes.map { "rounded-$it" } +
                            listOf("rounded", "rounded-t", "rounded-r", "rounded-b", "rounded-l", "rounded-tl", "rounded-tr", "rounded-br", "rounded-bl")

        // Effects
        val effectsClasses = listOf(
            "shadow-sm", "shadow", "shadow-md", "shadow-lg", "shadow-xl", "shadow-2xl", "shadow-inner", "shadow-none",
            "opacity-0", "opacity-5", "opacity-10", "opacity-20", "opacity-25", "opacity-30", "opacity-40", "opacity-50",
            "opacity-60", "opacity-70", "opacity-75", "opacity-80", "opacity-90", "opacity-95", "opacity-100"
        )

        // Transitions & Animation
        val transitionClasses = listOf(
            "transition", "transition-none", "transition-all", "transition-colors", "transition-opacity", "transition-shadow", "transition-transform",
            "duration-75", "duration-100", "duration-150", "duration-200", "duration-300", "duration-500", "duration-700", "duration-1000",
            "ease-linear", "ease-in", "ease-out", "ease-in-out"
        )

        // Hover, focus, and other states
        val stateClasses = listOf("hover", "focus", "active", "disabled", "visited", "first", "last", "odd", "even")
        val commonWithStates = listOf("bg-gray-100", "bg-gray-200", "bg-blue-500", "text-gray-700", "text-white", "opacity-75", "scale-105")

        val stateAppliedClasses = stateClasses.flatMap { state ->
            commonWithStates.map { cls -> "$state:$cls" }
        }

        // Combine all classes
        return layoutClasses + spacingClasses + typographyClasses + colorClasses + 
               flexboxClasses + gridClasses + borderClasses + effectsClasses + 
               transitionClasses + stateAppliedClasses
    }

    /**
     * Clear caches for a specific project
     */
    fun clearProjectCache(project: Project) {
        val projectId = project.locationHash
        projectClassesCache.remove(projectId)
        projectClassDataCache.remove(projectId)
    }

    /**
     * Clear all caches
     */
    fun clearAllCaches() {
        projectClassesCache.clear()
        projectClassDataCache.clear()
        defaultTailwindClasses = null
    }
}
