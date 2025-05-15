package com.github.dilika.tailwindsmartplugin.services

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.file.Paths

/**
 * Service avancé pour analyser les configurations Tailwind CSS
 * Prend en charge les extensions, les plugins et les thèmes personnalisés
 */
class TailwindConfigAnalyzer(private val project: Project) {
    private val logger = Logger.getInstance(TailwindConfigAnalyzer::class.java)
    private val configParser = TailwindConfigParser()
    
    // Cache pour les configurations analysées
    private val configCache = mutableMapOf<String, Map<String, Any>>()
    
    /**
     * Analyse complète d'une configuration Tailwind avec prise en charge des extensions
     * @param configPath Chemin vers le fichier de configuration
     * @return Map contenant la configuration complète fusionnée
     */
    fun analyzeConfig(configPath: String): Map<String, Any> {
        // Vérifier si la configuration est déjà en cache
        if (configCache.containsKey(configPath)) {
            return configCache[configPath] ?: emptyMap()
        }
        
        val baseConfig = configParser.parseConfig(configPath)
        val result = baseConfig.toMutableMap()
        
        try {
            // Traiter les extensions (extends)
            processExtends(configPath, baseConfig, result)
            
            // Analyser les plugins pour extraire les classes personnalisées
            processPlugins(baseConfig, result)
            
            // Traiter les présets (presets)
            processPresets(configPath, baseConfig, result)
            
            // Mettre en cache le résultat
            configCache[configPath] = result
            
            return result
        } catch (e: Exception) {
            logger.error("Erreur lors de l'analyse de la configuration Tailwind: ${e.message}")
            e.printStackTrace()
            return baseConfig
        }
    }
    
    /**
     * Traite les extensions (extends) dans la configuration Tailwind
     */
    private fun processExtends(configPath: String, baseConfig: Map<String, Any>, result: MutableMap<String, Any>) {
        if (baseConfig.containsKey("extends")) {
            val extends = baseConfig["extends"]
            when (extends) {
                is String -> {
                    // Résoudre le chemin relatif
                    val resolvedPath = resolveExtendPath(configPath, extends)
                    if (resolvedPath.isNotEmpty()) {
                        val extendedConfig = configParser.parseConfig(resolvedPath)
                        // Fusionner les configurations
                        mergeConfigurations(result, extendedConfig)
                    }
                }
                is List<*> -> {
                    extends.filterIsInstance<String>().forEach { extendPath ->
                        // Résoudre le chemin relatif
                        val resolvedPath = resolveExtendPath(configPath, extendPath)
                        if (resolvedPath.isNotEmpty()) {
                            val extendedConfig = configParser.parseConfig(resolvedPath)
                            // Fusionner les configurations
                            mergeConfigurations(result, extendedConfig)
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Traite les présets dans la configuration Tailwind
     */
    private fun processPresets(configPath: String, baseConfig: Map<String, Any>, result: MutableMap<String, Any>) {
        if (baseConfig.containsKey("presets")) {
            val presets = baseConfig["presets"]
            when (presets) {
                is List<*> -> {
                    presets.filterIsInstance<String>().forEach { presetPath ->
                        // Résoudre le chemin relatif
                        val resolvedPath = resolveExtendPath(configPath, presetPath)
                        if (resolvedPath.isNotEmpty()) {
                            val presetConfig = configParser.parseConfig(resolvedPath)
                            // Fusionner les configurations
                            mergeConfigurations(result, presetConfig)
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Traite les plugins dans la configuration Tailwind
     */
    private fun processPlugins(baseConfig: Map<String, Any>, result: MutableMap<String, Any>) {
        if (baseConfig.containsKey("plugins")) {
            val plugins = baseConfig["plugins"]
            val pluginClasses = mutableListOf<String>()
            
            when (plugins) {
                is List<*> -> {
                    plugins.forEach { plugin ->
                        when (plugin) {
                            is Map<*, *> -> {
                                if (plugin.containsKey("handler") && plugin["handler"] is String) {
                                    // Extraire les classes du plugin
                                    extractPluginClasses(plugin, pluginClasses)
                                }
                            }
                            is String -> {
                                // Ajouter le nom du plugin comme préfixe potentiel
                                val pluginName = plugin.substringAfterLast("/").substringBefore(".")
                                pluginClasses.add(pluginName)
                                
                                // Ajouter des classes spécifiques en fonction du nom du plugin connu
                                addKnownPluginClasses(pluginName, pluginClasses)
                            }
                        }
                    }
                }
            }
            
            if (pluginClasses.isNotEmpty()) {
                result["pluginClasses"] = pluginClasses
            }
        }
    }
    
    /**
     * Extrait les classes d'un plugin
     */
    private fun extractPluginClasses(plugin: Map<*, *>, pluginClasses: MutableList<String>) {
        // Extraire le nom du plugin si disponible
        val pluginName = plugin["name"] as? String ?: "plugin"
        pluginClasses.add(pluginName)
        
        // Extraire les classes spécifiques si disponibles
        val classes = plugin["classes"] as? List<*>
        classes?.filterIsInstance<String>()?.forEach { className ->
            pluginClasses.add(className)
        }
        
        // Ajouter des classes spécifiques en fonction du nom du plugin connu
        addKnownPluginClasses(pluginName, pluginClasses)
    }
    
    /**
     * Ajoute des classes connues pour des plugins spécifiques
     */
    private fun addKnownPluginClasses(pluginName: String, pluginClasses: MutableList<String>) {
        when {
            pluginName.contains("typography") -> {
                pluginClasses.addAll(listOf("prose", "prose-sm", "prose-lg", "prose-xl", "prose-2xl", 
                                           "prose-gray", "prose-slate", "prose-zinc", "prose-neutral", 
                                           "prose-stone", "prose-red", "prose-orange", "prose-amber", 
                                           "prose-yellow", "prose-lime", "prose-green", "prose-emerald", 
                                           "prose-teal", "prose-cyan", "prose-sky", "prose-blue", 
                                           "prose-indigo", "prose-violet", "prose-purple", "prose-fuchsia", 
                                           "prose-pink", "prose-rose"))
            }
            pluginName.contains("forms") -> {
                pluginClasses.addAll(listOf("form-input", "form-textarea", "form-select", "form-multiselect", 
                                           "form-checkbox", "form-radio"))
            }
            pluginName.contains("aspect-ratio") -> {
                pluginClasses.addAll(listOf("aspect-auto", "aspect-square", "aspect-video", "aspect-none"))
            }
            pluginName.contains("container-queries") -> {
                pluginClasses.addAll(listOf("@container", "@sm", "@md", "@lg", "@xl", "@2xl"))
            }
        }
    }
    
    /**
     * Fusionne deux configurations Tailwind
     */
    private fun mergeConfigurations(base: MutableMap<String, Any>, extension: Map<String, Any>) {
        for ((key, value) in extension) {
            if (key == "theme") {
                // Fusion spéciale pour le thème
                val baseTheme = base.getOrPut("theme") { mutableMapOf<String, Any>() } as MutableMap<String, Any>
                val extensionTheme = value as? Map<String, Any> ?: continue
                
                mergeThemes(baseTheme, extensionTheme)
            } else {
                // Fusion simple pour les autres clés
                base[key] = value
            }
        }
    }
    
    /**
     * Fusionne deux thèmes Tailwind
     */
    private fun mergeThemes(baseTheme: MutableMap<String, Any>, extensionTheme: Map<String, Any>) {
        for ((key, value) in extensionTheme) {
            if (key == "extend") {
                // Traiter les extensions de thème
                val baseExtend = baseTheme.getOrPut("extend") { mutableMapOf<String, Any>() } as MutableMap<String, Any>
                val extensionExtend = value as? Map<String, Any> ?: continue
                
                for ((extendKey, extendValue) in extensionExtend) {
                    baseExtend[extendKey] = extendValue
                }
            } else if (baseTheme.containsKey(key) && baseTheme[key] is Map<*, *> && value is Map<*, *>) {
                // Fusion récursive pour les objets imbriqués
                val baseNested = baseTheme[key] as MutableMap<String, Any>
                val extensionNested = value as Map<String, Any>
                
                for ((nestedKey, nestedValue) in extensionNested) {
                    baseNested[nestedKey] = nestedValue
                }
            } else {
                // Remplacement simple pour les autres valeurs
                baseTheme[key] = value
            }
        }
    }
    
    /**
     * Résout le chemin d'une extension relative au fichier de configuration principal
     */
    private fun resolveExtendPath(configPath: String, extendPath: String): String {
        try {
            // Si c'est un chemin absolu, l'utiliser directement
            if (File(extendPath).isAbsolute) {
                return extendPath
            }
            
            // Si c'est un module npm, essayer de le résoudre dans node_modules
            if (extendPath.startsWith("@") || !extendPath.contains("/")) {
                val projectDir = File(configPath).parentFile
                val nodeModulesPath = findNodeModulesDir(projectDir)
                
                if (nodeModulesPath != null) {
                    val modulePath = Paths.get(nodeModulesPath.absolutePath, extendPath).toString()
                    if (File(modulePath).exists()) {
                        return modulePath
                    }
                    
                    // Essayer avec /index.js ou /tailwind.config.js
                    val indexPath = Paths.get(modulePath, "index.js").toString()
                    if (File(indexPath).exists()) {
                        return indexPath
                    }
                    
                    val tailwindConfigPath = Paths.get(modulePath, "tailwind.config.js").toString()
                    if (File(tailwindConfigPath).exists()) {
                        return tailwindConfigPath
                    }
                }
            }
            
            // Sinon, résoudre comme un chemin relatif
            val configDir = File(configPath).parentFile
            val resolvedPath = Paths.get(configDir.absolutePath, extendPath).toString()
            
            return if (File(resolvedPath).exists()) resolvedPath else ""
        } catch (e: Exception) {
            logger.error("Erreur lors de la résolution du chemin d'extension: ${e.message}")
            return ""
        }
    }
    
    /**
     * Trouve le répertoire node_modules le plus proche
     */
    private fun findNodeModulesDir(startDir: File): File? {
        var currentDir = startDir
        
        while (currentDir != null) {
            val nodeModules = File(currentDir, "node_modules")
            if (nodeModules.exists() && nodeModules.isDirectory) {
                return nodeModules
            }
            
            currentDir = currentDir.parentFile ?: break
        }
        
        return null
    }
    
    /**
     * Extrait les classes personnalisées définies dans la configuration
     */
    fun extractCustomClasses(config: Map<String, Any>): List<String> {
        val customClasses = mutableListOf<String>()
        
        // Extraire les classes des plugins
        if (config.containsKey("pluginClasses")) {
            val pluginClasses = config["pluginClasses"]
            if (pluginClasses is List<*>) {
                customClasses.addAll(pluginClasses.filterIsInstance<String>())
            }
        }
        
        // Extraire les classes du thème personnalisé
        if (config.containsKey("theme")) {
            val theme = config["theme"] as? Map<String, Any>
            if (theme != null) {
                extractCustomClassesFromTheme(theme, customClasses)
            }
        }
        
        return customClasses
    }
    
    /**
     * Extrait les classes personnalisées du thème
     */
    private fun extractCustomClassesFromTheme(theme: Map<String, Any>, customClasses: MutableList<String>) {
        // Extraire les couleurs personnalisées
        if (theme.containsKey("colors")) {
            val colors = theme["colors"] as? Map<String, Any>
            if (colors != null) {
                for (colorName in colors.keys) {
                    customClasses.add("text-$colorName")
                    customClasses.add("bg-$colorName")
                    customClasses.add("border-$colorName")
                }
            }
        }
        
        // Extraire les tailles d'écran personnalisées
        if (theme.containsKey("screens")) {
            val screens = theme["screens"] as? Map<String, Any>
            if (screens != null) {
                for (screenName in screens.keys) {
                    customClasses.add("$screenName:")
                }
            }
        }
        
        // Extraire les espacements personnalisés
        if (theme.containsKey("spacing")) {
            val spacing = theme["spacing"] as? Map<String, Any>
            if (spacing != null) {
                for (spacingName in spacing.keys) {
                    customClasses.add("p-$spacingName")
                    customClasses.add("m-$spacingName")
                    customClasses.add("gap-$spacingName")
                }
            }
        }
    }
    
    /**
     * Nettoie le cache de configuration
     */
    fun clearCache() {
        configCache.clear()
    }
}
