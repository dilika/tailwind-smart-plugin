package com.github.dilika.tailwindsmartplugin.services

import com.intellij.openapi.diagnostic.Logger
import org.graalvm.polyglot.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Parser for Tailwind CSS configuration files that uses GraalVM to evaluate JavaScript
 */
class TailwindConfigParser {
    private val logger = Logger.getInstance(TailwindConfigParser::class.java)
    
    /**
     * Parse a Tailwind configuration file and extract relevant theme information
     * @param configPath Path to the Tailwind config file
     * @return Map containing the parsed configuration data
     */
    fun parseConfig(configPath: String): Map<String, Any> {
        logger.info("Parsing Tailwind config at: $configPath")
        val configFile = File(configPath)
        
        if (!configFile.exists()) {
            logger.warn("Tailwind config file not found at: $configPath")
            return emptyMap()
        }
        
        try {
            // Read the config file content
            val configContent = Files.readString(Paths.get(configPath))
            
            // For JS/TS configs, we need to use GraalVM to evaluate them
            val context = Context.newBuilder("js")
                .allowAllAccess(true)
                .build()
            
            // Create a mock module.exports object that the config file will populate
            context.eval("js", "var module = { exports: {} };")
            
            // Evaluate the config file
            context.eval("js", configContent)
            
            // Extract the config object from module.exports
            val configObj = context.eval("js", "JSON.stringify(module.exports)")
            val configJson = JSONObject(configObj.asString())
            
            // Close the GraalVM context
            context.close()
            
            // Extract and return the theme configuration
            return extractTailwindConfig(configJson)
        } catch (e: Exception) {
            logger.error("Error parsing Tailwind config: ${e.message}")
            e.printStackTrace()
            return emptyMap()
        }
    }
    
    /**
     * Extract the important parts of the Tailwind configuration
     * @param configJson The parsed Tailwind config as a JSONObject
     * @return Map containing extracted theme and plugin values
     */
    private fun extractTailwindConfig(configJson: JSONObject): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        try {
            // Extract the theme section
            if (configJson.has("theme")) {
                val theme = configJson.getJSONObject("theme")
                result["theme"] = jsonToMap(theme)
                
                // Look for extend section within theme
                if (theme.has("extend")) {
                    val extend = theme.getJSONObject("extend")
                    result["themeExtend"] = jsonToMap(extend)
                }
            }
            
            // Extract the plugins that might define custom classes
            if (configJson.has("plugins")) {
                val plugins = configJson.getJSONArray("plugins")
                val pluginsList = mutableListOf<String>()
                
                for (i in 0 until plugins.length()) {
                    try {
                        // Store plugin names if available
                        val pluginObj = plugins.get(i)
                        if (pluginObj is JSONObject && pluginObj.has("name")) {
                            pluginsList.add(pluginObj.getString("name"))
                        } else {
                            pluginsList.add("plugin-$i")
                        }
                    } catch (e: Exception) {
                        logger.debug("Error parsing plugin at index $i: ${e.message}")
                    }
                }
                
                result["plugins"] = pluginsList
            }
            
            // Extract if the config is JIT mode
            if (configJson.has("mode")) {
                result["mode"] = configJson.getString("mode")
            }
            
            // Extract prefix if it exists
            if (configJson.has("prefix")) {
                result["prefix"] = configJson.getString("prefix")
            }
            
            // Extract arbitrary values support
            if (configJson.has("experimental")) {
                val experimental = configJson.getJSONObject("experimental")
                if (experimental.has("arbitraryValues")) {
                    result["arbitraryValues"] = experimental.getBoolean("arbitraryValues")
                }
            }
            
            // Extract any custom content paths
            if (configJson.has("content")) {
                val content = configJson.get("content")
                val contentPaths = mutableListOf<String>()
                
                if (content is JSONArray) {
                    for (i in 0 until content.length()) {
                        if (content.get(i) is String) {
                            contentPaths.add(content.getString(i))
                        }
                    }
                }
                
                result["contentPaths"] = contentPaths
            }
            
            // Extract Tailwind version if specified
            if (configJson.has("tailwindVersion")) {
                result["tailwindVersion"] = configJson.getString("tailwindVersion")
            }
        } catch (e: Exception) {
            logger.error("Error extracting Tailwind config: ${e.message}")
        }
        
        return result
    }
    
    /**
     * Convert a JSONObject to a Map
     */
    private fun jsonToMap(json: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        
        json.keys().forEach { key ->
            val value = json.get(key)
            
            when (value) {
                is JSONObject -> map[key] = jsonToMap(value)
                is JSONArray -> {
                    val list = mutableListOf<Any>()
                    for (i in 0 until value.length()) {
                        when (val item = value.get(i)) {
                            is JSONObject -> list.add(jsonToMap(item))
                            is JSONArray -> list.add(jsonArrayToList(item))
                            else -> list.add(item.toString())
                        }
                    }
                    map[key] = list
                }
                else -> map[key] = value.toString()
            }
        }
        
        return map
    }
    
    /**
     * Convert a JSONArray to a List
     */
    private fun jsonArrayToList(jsonArray: JSONArray): List<Any> {
        val list = mutableListOf<Any>()
        
        for (i in 0 until jsonArray.length()) {
            val value = jsonArray.get(i)
            
            when (value) {
                is JSONObject -> list.add(jsonToMap(value))
                is JSONArray -> list.add(jsonArrayToList(value))
                else -> list.add(value.toString())
            }
        }
        
        return list
    }
}
