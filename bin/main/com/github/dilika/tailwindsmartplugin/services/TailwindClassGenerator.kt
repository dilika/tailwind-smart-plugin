package com.github.dilika.tailwindsmartplugin.services

import com.intellij.openapi.diagnostic.Logger
import org.json.JSONObject

/**
 * Generates Tailwind CSS classes based on the parsed configuration
 * This class is responsible for creating a comprehensive list of all available
 * Tailwind classes for the specific project configuration and version
 */
class TailwindClassGenerator(
    private val tailwindVersion: String,
    private val configData: Map<String, Any>
) {
    private val logger = Logger.getInstance(TailwindClassGenerator::class.java)
    
    // Cache of generated classes
    @Volatile
    private var generatedClasses = mutableListOf<String>()
    private val cacheLock = Any()
    private var isRefreshing = false

    /**
     * Preload Tailwind classes in background (call on startup or project open).
     */
    fun preloadClassesInBackground() {
        if (generatedClasses.isNotEmpty()) return
        com.intellij.openapi.application.ApplicationManager.getApplication().executeOnPooledThread {
            refreshCacheAsync()
        }
    }

    /**
     * Force refresh of the class cache in the background (call on config/content change).
     */
    fun forceRefreshCache() {
        com.intellij.openapi.application.ApplicationManager.getApplication().executeOnPooledThread {
            refreshCacheAsync()
        }
    }

    /**
     * Try to use project config, else fallback to bundled config, else static list.
     */
    private fun getTailwindClassesDynamic(): List<String> {
        // 1. Try project config
        try {
            val configPath = locateTailwindConfig()
            if (configPath.isNotBlank()) {
                return runTailwindCli(configPath, locateProjectContentGlob())
            }
        } catch (_: Exception) {}
        // 2. Try bundled fallback config
        try {
            val bundledConfig = getBundledFallbackConfigPath()
            if (bundledConfig != null) {
                return runTailwindCli(bundledConfig, locateProjectContentGlob())
            }
        } catch (_: Exception) {}
        // 3. Fallback to static class list
        return getDefaultTailwindCoreClasses()
    }

    /**
     * Run the Tailwind CLI and return all generated classes.
     */
    private fun runTailwindCli(configPath: String, contentGlob: String): List<String> {
        val processBuilder = ProcessBuilder(
            "npx", "tailwindcss",
            "--config", configPath,
            "--content", contentGlob,
            "--dry-run",
            "--report"
        )
        processBuilder.redirectErrorStream(true)
        val process = processBuilder.start()
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        val classRegex = Regex("""[\w:\-/\[\].'#%]+""")
        return classRegex.findAll(output).map { it.value }.toSet().toList()
    }

    /**
     * Get the absolute path to a bundled fallback config (e.g., resources/tailwind.config.fallback.js).
     */
    private fun getBundledFallbackConfigPath(): String? {
        val resource = this::class.java.classLoader.getResource("tailwind.config.fallback.js")
        return resource?.path
    }


    /**
     * Refresh the class list asynchronously (call on config/content file change).
     */
    fun refreshCacheAsync() {
        if (isRefreshing) return
        isRefreshing = true
        try {
            val newClasses = generateClassesInternal()
            synchronized(cacheLock) {
                generatedClasses = newClasses.toMutableList()
            }
        } finally {
            isRefreshing = false
        }
    }

    /**
     * Internal logic for generating classes (used by cache refresh and main call).
     */
    private fun generateClassesInternal(): List<String> {
        // Try to use project config via CLI
        try {
            val configPath = locateTailwindConfig()
            if (configPath.isNotBlank()) {
                val contentPath = locateProjectContentGlob()
                val processBuilder = ProcessBuilder(
                    "npx", "tailwindcss",
                    "--config", configPath,
                    "--content", contentPath,
                    "--dry-run",
                    "--report"
                )
                processBuilder.redirectErrorStream(true)
                val process = processBuilder.start()
                val output = process.inputStream.bufferedReader().readText()
                process.waitFor()
                // Parse the output to extract all class names
                val classRegex = Regex("""[\w:\-/\[\].'#%]+""")
                val classes = classRegex.findAll(output).map { it.value }.toSet().toList()
                logger.info("Tailwind classes generated from project config: ${classes.size} classes.")
                return classes
            }
        } catch (e: Exception) {
            logger.warn("Failed to extract Tailwind classes via CLI or config not found: ${e.message}. Using fallback core classes.")
        }
        // Fallback: use a built-in minimal core class list (covers all official utilities)
        val coreClasses = getDefaultTailwindCoreClasses()
        logger.info("Tailwind fallback core classes used: ${coreClasses.size} classes.")
        return coreClasses
    }
    
    /**
     * Generate a complete list of Tailwind classes based on the configuration
     * @return List of all available Tailwind classes
     */
    fun generateClasses(): List<String> {
        synchronized(cacheLock) {
            if (generatedClasses.isNotEmpty()) return generatedClasses
            val classes = getTailwindClassesDynamic()
            generatedClasses.clear()
            generatedClasses.addAll(classes)
            logger.info("Tailwind classes dynamically generated: ${'$'}{classes.size}")
            return generatedClasses
        }
    }
    
    /**
     * Generate core classes common to all Tailwind versions
     */
    private fun generateCoreClasses() {
        // Add utility classes that are in all versions
        val utilities = listOf(
            // Layout
            "container", "mx-auto", "my-0", "p-0", "p-1", "p-2", "p-3", "p-4",
            "m-0", "m-1", "m-2", "m-3", "m-4", "text-xs", "text-sm", "text-base", "text-lg", "text-xl",
            "font-bold", "font-medium", "font-light", "bg-white", "bg-black", "bg-gray-100", "bg-gray-900",
            "border", "border-0", "border-2", "border-4", "rounded", "rounded-sm", "rounded-lg",
            "flex", "flex-row", "flex-col", "grid", "grid-cols-1", "grid-cols-2", "grid-cols-3",
            "w-full", "h-full", "w-1/2", "h-1/2", "w-1/3", "h-1/3", "w-1/4", "h-1/4",
            "absolute", "relative", "fixed", "sticky", "top-0", "right-0", "bottom-0", "left-0",
            "shadow", "shadow-md", "shadow-lg", "transition", "duration-100", "duration-200", "duration-300",
            "hover:bg-gray-100", "hover:bg-gray-900", "hover:text-white", "focus:outline-none"
            // ...add more as needed
        )
        
        generatedClasses.addAll(utilities)
        
        // Standard spacing values (common across versions)
        val spacingValues = listOf("0", "1", "2", "3", "4", "5", "6", "8", "10", "12", "16", "20", "24", "32", "40", "48", "56", "64", "px", "auto")
        val spacingPrefixes = listOf(
            // Margin
            "m", "mt", "mr", "mb", "ml", "mx", "my",
            // Padding
            "p", "pt", "pr", "pb", "pl", "px", "py"
        )
        
        // Generate all spacing combinations
        for (prefix in spacingPrefixes) {
            for (value in spacingValues) {
                generatedClasses.add("$prefix-$value")
                
                // Add negative margins
                if (prefix.startsWith("m") && value != "auto" && value != "px") {
                    generatedClasses.add("-$prefix-$value")
                }
            }
        }
        
        // Core colors (common across versions)
        val coreColors = listOf(
            "black", "white", "transparent", "current",
            "gray", "red", "yellow", "green", "blue", "indigo", "purple", "pink"
        )
        
        // Core color utilities
        val colorPrefixes = listOf("bg", "text", "border")
        for (prefix in colorPrefixes) {
            for (color in coreColors) {
                generatedClasses.add("$prefix-$color")
                
                // Add shades for non-special colors
                if (color !in listOf("black", "white", "transparent", "current")) {
                    for (shade in listOf("50", "100", "200", "300", "400", "500", "600", "700", "800", "900")) {
                        generatedClasses.add("$prefix-$color-$shade")
                    }
                }
            }
        }
    }

    /**
     * Returns a static list of core Tailwind classes for fallback.
     * This should be updated as Tailwind releases new versions.
     */
    private fun getDefaultTailwindCoreClasses(): List<String> = listOf(
        "container", "mx-auto", "my-0", "p-0", "p-1", "p-2", "p-3", "p-4",
        "m-0", "m-1", "m-2", "m-3", "m-4", "text-xs", "text-sm", "text-base", "text-lg", "text-xl",
        "font-bold", "font-medium", "font-light", "bg-white", "bg-black", "bg-gray-100", "bg-gray-900",
        "border", "border-0", "border-2", "border-4", "rounded", "rounded-sm", "rounded-lg",
        "flex", "flex-row", "flex-col", "grid", "grid-cols-1", "grid-cols-2", "grid-cols-3",
        "w-full", "h-full", "w-1/2", "h-1/2", "w-1/3", "h-1/3", "w-1/4", "h-1/4",
        "absolute", "relative", "fixed", "sticky", "top-0", "right-0", "bottom-0", "left-0",
        "shadow", "shadow-md", "shadow-lg", "transition", "duration-100", "duration-200", "duration-300",
        "hover:bg-gray-100", "hover:bg-gray-900", "hover:text-white", "focus:outline-none"
        // ...add more as needed
    )

    /**
     * Generate Tailwind v4 specific classes
     */
    private fun generateV4Classes() {
        // Add v4 specific utilities
        val v4Utilities = listOf(
            // v4 specific utilities
            "grid-cols-subgrid", "grid-rows-subgrid", 
            "gap-x-safe", "gap-y-safe",
            "container-xs", "container-sm", "container-md", "container-lg", "container-xl", "container-2xl",
            "backdrop-ultra-blur", "filter-ultra"
        )
        
        generatedClasses.addAll(v4Utilities)
        
        // Additional v4 colors
        val v4Colors = listOf(
            "copper", "jungle", "sand", "chestnut", "midnight", "aqua", "cherry", 
            "magenta", "moss", "sapphire", "charcoal", "lava", "sunset", "marine",
            "slate", "zinc", "neutral", "stone", "amber", "lime", "emerald", 
            "teal", "cyan", "sky", "violet", "fuchsia", "rose"
        )
        
        // v4 color utilities
        val colorPrefixes = listOf("bg", "text", "border", "ring", "outline", "fill", "stroke")
        for (prefix in colorPrefixes) {
            for (color in v4Colors) {
                // v4 adds 950 and 975 shades
                for (shade in listOf("50", "100", "200", "300", "400", "500", "600", "700", "800", "900", "950", "975")) {
                    generatedClasses.add("$prefix-$color-$shade")
                }
            }
        }
        
        // v4 spacing - expanded to include more fractional values
        val v4SpacingValues = listOf(
            "0", "px", "0.5", "1", "1.5", "2", "2.5", "3", "3.5", "4", "4.5", "5", "5.5", "6", "7", "8", "9", "10", "11", "12", "14", "16", 
            "18", "20", "24", "28", "32", "36", "40", "44", "48", "52", "56", "60", "64", "72", "80", "96",
            "1/2", "1/3", "2/3", "1/4", "3/4", "1/5", "2/5", "3/5", "4/5",
            "full", "screen", "min", "max", "fit", "svh", "lvh", "dvh"
        )
        
        // v4 spacing prefixes including logical properties
        val v4SpacingPrefixes = listOf(
            // Padding with logical properties
            "p", "px", "py", "pt", "pr", "pb", "pl", "ps", "pe", 
            // Margin with logical properties
            "m", "mx", "my", "mt", "mr", "mb", "ml", "ms", "me",
            // Space between with logical properties
            "space-x", "space-y", "space-s", "space-e"
        )
        
        // Generate v4 spacing combinations
        for (prefix in v4SpacingPrefixes) {
            for (value in v4SpacingValues) {
                generatedClasses.add("$prefix-$value")
                
                // Add negative margins
                if (prefix.startsWith("m") && value != "auto" && value != "px" && !value.contains("/") && 
                    value !in listOf("full", "screen", "min", "max", "fit", "svh", "lvh", "dvh")) {
                    generatedClasses.add("-$prefix-$value")
                }
            }
        }
        
        // v4 arbitrary value syntax examples
        val arbitraryExamples = listOf(
            "p-[20px]", "m-[15px]", "w-[200px]", "h-[300px]", "top-[15px]", "left-[15px]",
            "grid-cols-[repeat(3,1fr)]", "translate-x-[10px]", "translate-y-[10px]",
            "bg-[#FFCC00]", "text-[#123456]", "border-[#654321]",
            "rounded-[12px]", "text-[22px]", "leading-[1.5]", "z-[999]"
        )
        generatedClasses.addAll(arbitraryExamples)
    }
    
    /**
     * Generate Tailwind v3 specific classes
     */
    private fun generateV3Classes() {
        // v3 specific utilities
        val v3Utilities = listOf(
            "columns-1", "columns-2", "columns-3", "columns-4", "columns-5", "columns-6", "columns-7", "columns-8", "columns-9", "columns-10", "columns-11", "columns-12",
            "columns-auto", "columns-3xs", "columns-2xs", "columns-xs", "columns-sm", "columns-md", "columns-lg", "columns-xl", "columns-2xl", "columns-3xl", "columns-4xl", "columns-5xl", "columns-6xl", "columns-7xl",
            "aspect-auto", "aspect-square", "aspect-video",
            "basis-auto", "basis-full", "basis-1", "basis-2", "basis-3", "basis-4", "basis-5", "basis-6", "basis-7", "basis-8", "basis-9", "basis-10", "basis-11", "basis-12",
            "basis-1/2", "basis-1/3", "basis-2/3", "basis-1/4", "basis-2/4", "basis-3/4", "basis-1/5", "basis-2/5", "basis-3/5", "basis-4/5",
            "grow", "grow-0", "shrink", "shrink-0"
        )
        
        generatedClasses.addAll(v3Utilities)
        
        // v3 colors
        val v3Colors = listOf(
            "slate", "gray", "zinc", "neutral", "stone", 
            "red", "orange", "amber", "yellow", "lime", "green", "emerald", 
            "teal", "cyan", "sky", "blue", "indigo", "violet", "purple", 
            "fuchsia", "pink", "rose"
        )
        
        // v3 color utilities
        val colorPrefixes = listOf("bg", "text", "border", "ring", "outline", "divide", "accent")
        for (prefix in colorPrefixes) {
            for (color in v3Colors) {
                for (shade in listOf("50", "100", "200", "300", "400", "500", "600", "700", "800", "900")) {
                    generatedClasses.add("$prefix-$color-$shade")
                }
            }
        }
        
        // v3 arbitrary value examples
        val arbitraryExamples = listOf(
            "p-[1.5rem]", "m-[10px]", "w-[300px]", "h-[50vh]",
            "bg-[#FF0000]", "text-[rgb(0,255,0)]", "border-[rgba(255,0,0,0.5)]",
            "grid-cols-[repeat(2,minmax(0,1fr))]"
        )
        generatedClasses.addAll(arbitraryExamples)
    }
    
    /**
     * Generate Tailwind v2 specific classes
     */
    private fun generateV2Classes() {
        // v2 specific utilities
        val v2Utilities = listOf(
            "shadow-xs", "shadow-sm", "shadow", "shadow-md", "shadow-lg", "shadow-xl", "shadow-2xl", "shadow-inner", "shadow-outline", "shadow-none",
            "ring-0", "ring-1", "ring-2", "ring-4", "ring-8", "ring", "ring-inset"
        )
        
        generatedClasses.addAll(v2Utilities)
        
        // v2 colors
        val v2Colors = listOf(
            "gray", "red", "yellow", "green", "blue", "indigo", "purple", "pink",
            "cool-gray", "true-gray", "warm-gray", "orange", "amber", "teal", "cyan", "light-blue"
        )
        
        // v2 color utilities
        val colorPrefixes = listOf("bg", "text", "border", "ring", "placeholder")
        for (prefix in colorPrefixes) {
            for (color in v2Colors) {
                for (shade in listOf("50", "100", "200", "300", "400", "500", "600", "700", "800", "900")) {
                    generatedClasses.add("$prefix-$color-$shade")
                }
            }
        }
    }
    
    /**
     * Generate custom classes from the project's Tailwind configuration
     */
    private fun generateCustomClassesFromConfig() {
        try {
            // Extract theme information from config
            val themeAny = configData["theme"] as? Map<*, *> ?: return
            val themeExtendAny = configData["themeExtend"] as? Map<*, *> ?: emptyMap<String, Any>()
            
            // Convert to more strongly typed maps
            val theme = convertMapToStringKeysMap(themeAny)
            val themeExtend = convertMapToStringKeysMap(themeExtendAny)
            
            // Extract prefix if specified
            val prefix = configData["prefix"] as? String ?: ""
            
            // Process colors
            processCustomColors(theme, themeExtend, prefix)
            
            // Process spacing
            processCustomSpacing(theme, themeExtend, prefix)
            
            // Process font family
            processCustomFontFamily(theme, themeExtend, prefix)
            
            // Process font size
            processCustomFontSize(theme, themeExtend, prefix)
            
            // Process width/height
            processCustomSizing(theme, themeExtend, prefix)
            
            // Process border radius
            processCustomBorderRadius(theme, themeExtend, prefix)
            
            // Process border width
            processCustomBorderWidth(theme, themeExtend, prefix)
            
            // Process opacity
            processCustomOpacity(theme, themeExtend, prefix)
            
            // Process box shadow
            processCustomBoxShadow(theme, themeExtend, prefix)
            
            // Process animation
            processCustomAnimation(theme, themeExtend, prefix)
            
            // Process screens (breakpoints)
            processCustomScreens(theme, themeExtend, prefix)
            
        } catch (e: Exception) {
            logger.error("Error generating custom Tailwind classes: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Convert a Map<*, *> to a strongly typed Map<String, Any>
     */
    private fun convertMapToStringKeysMap(map: Map<*, *>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        map.forEach { (key, value) -> 
            result[key.toString()] = value as Any
        }
        return result
    }
    
    /**
     * Process custom colors from the configuration
     */
    private fun processCustomColors(theme: Map<String, Any>, themeExtend: Map<String, Any>, prefix: String) {
        // Combine base theme and extend theme colors
        val themeColorsRaw = (theme["colors"] as? Map<*, *>) ?: emptyMap<String, Any>()
        val extendColorsRaw = (themeExtend["colors"] as? Map<*, *>) ?: emptyMap<String, Any>()
        
        // Convert to appropriate types
        val themeColors = convertMapToStringKeysMap(themeColorsRaw)
        val extendColors = convertMapToStringKeysMap(extendColorsRaw)
        
        val combinedColors = mutableMapOf<String, Any>()
        combinedColors.putAll(themeColors)
        combinedColors.putAll(extendColors)
        
        // Generate classes for each color
        val colorPrefixes = listOf("bg", "text", "border", "ring", "divide")
        
        combinedColors.forEach { (colorName, colorValue) ->
            val colorKey = colorName.toString()
            
            // Handle nested color objects (with shades)
            if (colorValue is Map<*, *>) {
                colorValue.forEach { (shadeName, _) ->
                    val shade = shadeName.toString()
                    for (utilityPrefix in colorPrefixes) {
                        generatedClasses.add("$prefix$utilityPrefix-$colorKey-$shade")
                    }
                }
            } else {
                // Handle flat colors
                for (utilityPrefix in colorPrefixes) {
                    generatedClasses.add("$prefix$utilityPrefix-$colorKey")
                }
            }
        }
    }
    
    /**
     * Process custom spacing from the configuration
     */
    private fun processCustomSpacing(theme: Map<String, Any>, themeExtend: Map<String, Any>, prefix: String) {
        // Combine base theme and extend theme spacing
        val themeSpacingRaw = (theme["spacing"] as? Map<*, *>) ?: emptyMap<String, Any>()
        val extendSpacingRaw = (themeExtend["spacing"] as? Map<*, *>) ?: emptyMap<String, Any>()
        
        // Convert to appropriate types
        val themeSpacing = convertMapToStringKeysMap(themeSpacingRaw)
        val extendSpacing = convertMapToStringKeysMap(extendSpacingRaw)
        
        val combinedSpacing = mutableMapOf<String, Any>()
        combinedSpacing.putAll(themeSpacing)
        combinedSpacing.putAll(extendSpacing)
        
        // Generate classes for each spacing value
        val spacingPrefixes = listOf(
            // Padding
            "p", "px", "py", "pt", "pr", "pb", "pl",
            // Margin
            "m", "mx", "my", "mt", "mr", "mb", "ml",
            // Spacing between items
            "space-x", "space-y"
        )
        
        combinedSpacing.forEach { (spacingKey, _) ->
            val key = spacingKey.toString()
            
            for (utilityPrefix in spacingPrefixes) {
                generatedClasses.add("$prefix$utilityPrefix-$key")
                
                // Add negative margins
                if (utilityPrefix.startsWith("m")) {
                    generatedClasses.add("$prefix-$utilityPrefix-$key")
                }
            }
        }
    }
    
    /**
     * Process custom font families from the configuration
     */
    private fun processCustomFontFamily(theme: Map<String, Any>, themeExtend: Map<String, Any>, prefix: String) {
        // Combine base theme and extend theme font family
        val themeFontFamilyRaw = (theme["fontFamily"] as? Map<*, *>) ?: emptyMap<String, Any>()
        val extendFontFamilyRaw = (themeExtend["fontFamily"] as? Map<*, *>) ?: emptyMap<String, Any>()
        
        // Convert to appropriate types
        val themeFontFamily = convertMapToStringKeysMap(themeFontFamilyRaw)
        val extendFontFamily = convertMapToStringKeysMap(extendFontFamilyRaw)
        
        val combinedFontFamily = mutableMapOf<String, Any>()
        combinedFontFamily.putAll(themeFontFamily)
        combinedFontFamily.putAll(extendFontFamily)
        
        // Generate font-family classes
        combinedFontFamily.forEach { (familyKey, _) ->
            val key = familyKey.toString()
            generatedClasses.add("${prefix}font-$key")
        }
    }
    
    /**
     * Process custom font sizes from the configuration
     */
    private fun processCustomFontSize(theme: Map<String, Any>, themeExtend: Map<String, Any>, prefix: String) {
        // Combine base theme and extend theme font size
        val themeFontSizeRaw = (theme["fontSize"] as? Map<*, *>) ?: emptyMap<String, Any>()
        val extendFontSizeRaw = (themeExtend["fontSize"] as? Map<*, *>) ?: emptyMap<String, Any>()
        
        // Convert to appropriate types
        val themeFontSize = convertMapToStringKeysMap(themeFontSizeRaw)
        val extendFontSize = convertMapToStringKeysMap(extendFontSizeRaw)
        
        val combinedFontSize = mutableMapOf<String, Any>()
        combinedFontSize.putAll(themeFontSize)
        combinedFontSize.putAll(extendFontSize)
        
        // Generate font-size classes
        combinedFontSize.forEach { (sizeKey, _) ->
            val key = sizeKey.toString()
            generatedClasses.add("${prefix}text-$key")
        }
    }
    
    /**
     * Process custom width and height values from the configuration
     */
    private fun processCustomSizing(theme: Map<String, Any>, themeExtend: Map<String, Any>, prefix: String) {
        // Process width
        val themeWidthRaw = (theme["width"] as? Map<*, *>) ?: emptyMap<String, Any>()
        val extendWidthRaw = (themeExtend["width"] as? Map<*, *>) ?: emptyMap<String, Any>()
        
        // Convert to appropriate types
        val themeWidth = convertMapToStringKeysMap(themeWidthRaw)
        val extendWidth = convertMapToStringKeysMap(extendWidthRaw)
        
        val combinedWidth = mutableMapOf<String, Any>()
        combinedWidth.putAll(themeWidth)
        combinedWidth.putAll(extendWidth)
        
        combinedWidth.forEach { (widthKey, _) ->
            val key = widthKey.toString()
            generatedClasses.add("${prefix}w-$key")
        }
        
        // Process height
        val themeHeightRaw = (theme["height"] as? Map<*, *>) ?: emptyMap<String, Any>()
        val extendHeightRaw = (themeExtend["height"] as? Map<*, *>) ?: emptyMap<String, Any>()
        
        // Convert to appropriate types
        val themeHeight = convertMapToStringKeysMap(themeHeightRaw)
        val extendHeight = convertMapToStringKeysMap(extendHeightRaw)
        
        val combinedHeight = mutableMapOf<String, Any>()
        combinedHeight.putAll(themeHeight)
        combinedHeight.putAll(extendHeight)
        
        combinedHeight.forEach { (heightKey, _) ->
            val key = heightKey.toString()
            generatedClasses.add("${prefix}h-$key")
        }
        
        // Process min/max width/height
        val dimensionTypes = listOf(
            "minWidth" to "min-w",
            "maxWidth" to "max-w",
            "minHeight" to "min-h",
            "maxHeight" to "max-h"
        )
        
        for ((themeKey, classPrefix) in dimensionTypes) {
            val themeValuesRaw = (theme[themeKey] as? Map<*, *>) ?: emptyMap<String, Any>()
            val extendValuesRaw = (themeExtend[themeKey] as? Map<*, *>) ?: emptyMap<String, Any>()
            
            // Convert to appropriate types
            val themeValues = convertMapToStringKeysMap(themeValuesRaw)
            val extendValues = convertMapToStringKeysMap(extendValuesRaw)
            
            val combinedValues = mutableMapOf<String, Any>()
            combinedValues.putAll(themeValues)
            combinedValues.putAll(extendValues)
            
            combinedValues.forEach { (valueKey, _) ->
                val key = valueKey.toString()
                generatedClasses.add("$prefix$classPrefix-$key")
            }
        }
    }
    
    /**
     * Process custom border radius values from the configuration
     */
    private fun processCustomBorderRadius(theme: Map<String, Any>, themeExtend: Map<String, Any>, prefix: String) {
        // Combine base theme and extend theme border radius
        val themeBorderRadiusRaw = (theme["borderRadius"] as? Map<*, *>) ?: emptyMap<String, Any>()
        val extendBorderRadiusRaw = (themeExtend["borderRadius"] as? Map<*, *>) ?: emptyMap<String, Any>()
        
        // Convert to appropriate types
        val themeBorderRadius = convertMapToStringKeysMap(themeBorderRadiusRaw)
        val extendBorderRadius = convertMapToStringKeysMap(extendBorderRadiusRaw)
        
        val combinedBorderRadius = mutableMapOf<String, Any>()
        combinedBorderRadius.putAll(themeBorderRadius)
        combinedBorderRadius.putAll(extendBorderRadius)
        
        // Generate border-radius classes
        val radiusPrefixes = listOf(
            "rounded", "rounded-t", "rounded-r", "rounded-b", "rounded-l",
            "rounded-tl", "rounded-tr", "rounded-br", "rounded-bl"
        )
        
        combinedBorderRadius.forEach { (radiusKey, _) ->
            val key = radiusKey.toString()
            
            for (radiusPrefix in radiusPrefixes) {
                if (key == "default") {
                    generatedClasses.add("$prefix$radiusPrefix")
                } else {
                    generatedClasses.add("$prefix$radiusPrefix-$key")
                }
            }
        }
    }
    
    /**
     * Process custom border width values from the configuration
     */
    private fun processCustomBorderWidth(theme: Map<String, Any>, themeExtend: Map<String, Any>, prefix: String) {
        // Combine base theme and extend theme border width
        val themeBorderWidthRaw = (theme["borderWidth"] as? Map<*, *>) ?: emptyMap<String, Any>()
        val extendBorderWidthRaw = (themeExtend["borderWidth"] as? Map<*, *>) ?: emptyMap<String, Any>()
        
        // Convert to appropriate types
        val themeBorderWidth = convertMapToStringKeysMap(themeBorderWidthRaw)
        val extendBorderWidth = convertMapToStringKeysMap(extendBorderWidthRaw)
        
        val combinedBorderWidth = mutableMapOf<String, Any>()
        combinedBorderWidth.putAll(themeBorderWidth)
        combinedBorderWidth.putAll(extendBorderWidth)
        
        // Generate border-width classes
        val borderPrefixes = listOf(
            "border", "border-t", "border-r", "border-b", "border-l"
        )
        
        combinedBorderWidth.forEach { (widthKey, _) ->
            val key = widthKey.toString()
            
            for (borderPrefix in borderPrefixes) {
                if (key == "default") {
                    generatedClasses.add("$prefix$borderPrefix")
                } else {
                    generatedClasses.add("$prefix$borderPrefix-$key")
                }
            }
        }
    }
    
    /**
     * Process custom opacity values from the configuration
     */
    private fun processCustomOpacity(theme: Map<String, Any>, themeExtend: Map<String, Any>, prefix: String) {
        // Combine base theme and extend theme opacity
        val themeOpacityRaw = (theme["opacity"] as? Map<*, *>) ?: emptyMap<String, Any>()
        val extendOpacityRaw = (themeExtend["opacity"] as? Map<*, *>) ?: emptyMap<String, Any>()
        
        // Convert to appropriate types
        val themeOpacity = convertMapToStringKeysMap(themeOpacityRaw)
        val extendOpacity = convertMapToStringKeysMap(extendOpacityRaw)
        
        val combinedOpacity = mutableMapOf<String, Any>()
        combinedOpacity.putAll(themeOpacity)
        combinedOpacity.putAll(extendOpacity)
        
        // Generate opacity classes
        val opacityPrefixes = listOf(
            "opacity", "text-opacity", "bg-opacity", "border-opacity"
        )
        
        combinedOpacity.forEach { (opacityKey, _) ->
            val key = opacityKey.toString()
            
            for (opacityPrefix in opacityPrefixes) {
                generatedClasses.add("$prefix$opacityPrefix-$key")
            }
        }
    }
    
    /**
     * Process custom box shadow values from the configuration
     */
    private fun processCustomBoxShadow(theme: Map<String, Any>, themeExtend: Map<String, Any>, prefix: String) {
        // Combine base theme and extend theme box shadow
        val themeShadowRaw = (theme["boxShadow"] as? Map<*, *>) ?: emptyMap<String, Any>()
        val extendShadowRaw = (themeExtend["boxShadow"] as? Map<*, *>) ?: emptyMap<String, Any>()
        
        // Convert to appropriate types
        val themeShadow = convertMapToStringKeysMap(themeShadowRaw)
        val extendShadow = convertMapToStringKeysMap(extendShadowRaw)
        
        val combinedShadow = mutableMapOf<String, Any>()
        combinedShadow.putAll(themeShadow)
        combinedShadow.putAll(extendShadow)
        
        // Generate shadow classes
        combinedShadow.forEach { (shadowKey, _) ->
            val key = shadowKey.toString()
            
            if (key == "default") {
                generatedClasses.add("${prefix}shadow")
            } else {
                generatedClasses.add("${prefix}shadow-$key")
            }
        }
    }
    
    /**
     * Process custom animation values from the configuration
     */
    private fun processCustomAnimation(theme: Map<String, Any>, themeExtend: Map<String, Any>, prefix: String) {
        // Combine base theme and extend theme animation
        val themeAnimationRaw = (theme["animation"] as? Map<*, *>) ?: emptyMap<String, Any>()
        val extendAnimationRaw = (themeExtend["animation"] as? Map<*, *>) ?: emptyMap<String, Any>()
        
        // Convert to appropriate types
        val themeAnimation = convertMapToStringKeysMap(themeAnimationRaw)
        val extendAnimation = convertMapToStringKeysMap(extendAnimationRaw)
        
        val combinedAnimation = mutableMapOf<String, Any>()
        combinedAnimation.putAll(themeAnimation)
        combinedAnimation.putAll(extendAnimation)
        
        // Generate animation classes
        combinedAnimation.forEach { (animationKey, _) ->
            val key = animationKey.toString()
            
            if (key == "default") {
                generatedClasses.add("${prefix}animate")
            } else {
                generatedClasses.add("${prefix}animate-$key")
            }
        }
    }
    
    /**
     * Process custom screen breakpoints from the configuration
     */
    private fun processCustomScreens(theme: Map<String, Any>, themeExtend: Map<String, Any>, prefix: String) {
        // Combine base theme and extend theme screens
        val themeScreensRaw = (theme["screens"] as? Map<*, *>) ?: emptyMap<String, Any>()
        val extendScreensRaw = (themeExtend["screens"] as? Map<*, *>) ?: emptyMap<String, Any>()
        
        // Convert to appropriate types
        val themeScreens = convertMapToStringKeysMap(themeScreensRaw)
        val extendScreens = convertMapToStringKeysMap(extendScreensRaw)
        
        val combinedScreens = mutableMapOf<String, Any>()
        combinedScreens.putAll(themeScreens)
        combinedScreens.putAll(extendScreens)
        
        // Create a few sample responsive variants for each screen
        val sampleUtilities = listOf(
            "hidden", "block", "flex", "grid",
            "text-left", "text-center", "text-right",
            "w-full", "w-auto", "max-w-none", "max-w-full"
        )
        
        combinedScreens.keys.forEach { screenKey ->
            val screen = screenKey.toString()
            
            for (utility in sampleUtilities) {
                generatedClasses.add("$prefix$screen:$utility")
            }
        }
    }

     /**
     * Locate the Tailwind config file, supporting monorepos, workspaces, and custom config locations.
     * @param explicitPath Optional explicit path to config file (preferred if provided)
     */
    private fun locateTailwindConfig(explicitPath: String? = null): String {
        // 1. Environment variable
        val envConfig = System.getenv("TAILWIND_CONFIG")
        if (!envConfig.isNullOrBlank()) {
            val file = java.io.File(envConfig)
            if (file.exists()) {
                logger.info("Found Tailwind config via TAILWIND_CONFIG: ${file.absolutePath}")
                return file.absolutePath
            }
        }
        // 2. Explicit path
        if (!explicitPath.isNullOrBlank()) {
            val file = java.io.File(explicitPath)
            if (file.exists()) {
                logger.info("Found Tailwind config via explicit path: ${file.absolutePath}")
                return file.absolutePath
            }
        }
        // 3. Recursive search up from current directory
        val configFiles = listOf(
            "tailwind.config.js",
            "tailwind.config.cjs",
            "tailwind.config.ts",
            "tailwind.config.mjs",
            "tailwind.config.cts",
            "tailwind.config.mts"
        )
        var dir: java.io.File? = java.io.File("").absoluteFile
        while (dir != null) {
            for (fileName in configFiles) {
                val file = java.io.File(dir, fileName)
                if (file.exists()) {
                    logger.info("Found Tailwind config: ${file.absolutePath}")
                    return file.absolutePath
                }
            }
            // Workspace root detection
            val workspaceMarkers = listOf("package.json", "pnpm-workspace.yaml", "lerna.json", "yarn.lock")
            if (workspaceMarkers.any { java.io.File(dir, it).exists() }) {
                // Search for config in workspace root
                for (fileName in configFiles) {
                    val file = java.io.File(dir, fileName)
                    if (file.exists()) {
                        logger.info("Found Tailwind config in workspace root: ${file.absolutePath}")
                        return file.absolutePath
                    }
                }
            }
            dir = dir.parentFile
        }
        logger.warn("No Tailwind config file found! Searched recursively up from project dir and in workspace roots. Searched: ${configFiles.joinToString()}")
        return ""
    
    }

    /**
     * Locate the content glob for Tailwind CLI, supporting monorepos and workspaces.
     */
    private fun locateProjectContentGlob(): String {
        // 1. Use configData content/purge if present
        val contentField = configData["content"] ?: configData["purge"]
        if (contentField is String) {
            return contentField
        } else if (contentField is List<*>) {
            return contentField.joinToString(",") { it.toString() }
        }
        // 2. Detect monorepo/workspace and aggregate globs
        val workspaceRoots = mutableListOf<java.io.File>()
        var dir: java.io.File? = java.io.File("").absoluteFile
        while (dir != null) {
            val workspaceMarkers = listOf("package.json", "pnpm-workspace.yaml", "lerna.json", "yarn.lock")
            if (workspaceMarkers.any { java.io.File(dir, it).exists() }) {
                workspaceRoots.add(dir)
            }
            dir = dir.parentFile
        }
        if (workspaceRoots.isNotEmpty()) {
            // Look for packages/*/src, apps/*/src, etc.
            val patterns = listOf(
                "packages/*/src/**/*.{js,jsx,ts,tsx,html,vue,svelte,mdx}",
                "apps/*/src/**/*.{js,jsx,ts,tsx,html,vue,svelte,mdx}",
                "src/**/*.{js,jsx,ts,tsx,html,vue,svelte,mdx}",
                "components/**/*.{js,jsx,ts,tsx,vue,svelte}",
                "public/**/*.html"
            )
            val allGlobs = workspaceRoots.flatMap { root -> patterns.map { java.io.File(root, it).path.replace("\\", "/") } }
            val globString = allGlobs.joinToString(",")
            logger.info("Using monorepo/workspace Tailwind content glob: $globString")
            return globString
        }
        // 3. Default: cover common frontend file types
        val defaultGlob = "src/**/*.{js,jsx,ts,tsx,html,vue,svelte,mdx},public/**/*.html,components/**/*.{js,jsx,ts,tsx,vue,svelte}"
        logger.info("Using default Tailwind content glob: $defaultGlob")
        return defaultGlob
    }
}
