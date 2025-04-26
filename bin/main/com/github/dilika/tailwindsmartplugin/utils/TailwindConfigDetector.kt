package com.github.dilika.tailwindsmartplugin.utils

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.SystemInfo
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * Détecteur et extracteur de configuration Tailwind CSS
 * Supporte différents types de projets et formats de configuration
 */
@Suppress("UNRESOLVED_REFERENCE", "UnresolvedExtensionProperty", "unused")
class TailwindConfigDetector(private val project: Project) {
    @Suppress("unused") private val logger = Logger.getInstance(TailwindConfigDetector::class.java)
    
    // Custom non-error logging utility to prevent any Logger.error() calls
    private object SafeLogger {
        private val actualLogger = Logger.getInstance(TailwindConfigDetector::class.java)
        
        fun info(message: String) {
            // Only use info level logging
            actualLogger.info(message)
        }
        
        fun warn(message: String) {
            // Only use warn level logging (not error)
            actualLogger.warn(message)
        }
        
        @Suppress("unused")
        fun debug(message: String) {
            // Only use debug level logging
            if (actualLogger.isDebugEnabled) {
                actualLogger.debug(message)
            }
        }
    }
    
    companion object {
        // Fichiers de configuration possibles
        private val CONFIG_FILES = listOf(
            "tailwind.config.js",
            "tailwind.config.mjs",
            "tailwind.config.ts",
            "postcss.config.js",
            "postcss.config.mjs",
            "postcss.config.ts"
        )
        
        // Pour vérifier les dépendances Tailwind
        private val PACKAGE_FILES = listOf(
            "package.json"
        )
        
        // Pour vérifier l'inclusion CDN de Tailwind
        private val HTML_FILES = listOf(
            "index.html", 
            "public/index.html", 
            "src/index.html"
        )
    }
    
    // Variable pour stocker le chemin de Node.js trouvé
    private var nodePath: String = "node"
    
    /**
     * Détecter si le projet utilise Tailwind CSS
     */
    @Suppress("unused")
    fun detectTailwindUsage(): Boolean {
        SafeLogger.info("Détection de Tailwind dans le projet: ${project.name}")
        val hasConfigFiles = findConfigFiles().isNotEmpty()
        val hasDependency = hasTailwindDependency()
        val hasCDN = hasTailwindCDN()
        
        SafeLogger.info("Résultats de détection - Config: $hasConfigFiles, Dependency: $hasDependency, CDN: $hasCDN")
        return hasConfigFiles || hasDependency || hasCDN
    }
    
    /**
     * Trouver tous les fichiers de configuration Tailwind dans le projet
     */
    fun findConfigFiles(): List<VirtualFile> {
        val configFiles = mutableListOf<VirtualFile>()
        
        for (fileName in CONFIG_FILES) {
            SafeLogger.info("Recherche du fichier de configuration: $fileName")
            val files = FilenameIndex.getVirtualFilesByName(
                fileName, 
                GlobalSearchScope.projectScope(project)
            )
            SafeLogger.info("Trouvé ${files.size} fichier(s) pour $fileName")
            configFiles.addAll(files)
        }
        
        return configFiles
    }
    
    /**
     * Vérifier si le projet a une dépendance Tailwind dans package.json
     */
    private fun hasTailwindDependency(): Boolean {
        for (fileName in PACKAGE_FILES) {
            SafeLogger.info("Recherche du fichier package: $fileName")
            val files = FilenameIndex.getVirtualFilesByName(
                fileName, 
                GlobalSearchScope.projectScope(project)
            )
            
            for (file in files) {
                SafeLogger.info("Analyse de ${file.path}")
                val content = file.contentsToByteArray().toString(Charsets.UTF_8)
                if (content.contains("tailwindcss") || content.contains("tailwind-css")) {
                    SafeLogger.info("Dépendance Tailwind trouvée dans ${file.path}")
                    return true
                }
            }
        }
        
        SafeLogger.info("Aucune dépendance Tailwind trouvée")
        return false
    }
    
    /**
     * Vérifier si le projet utilise Tailwind via CDN
     */
    private fun hasTailwindCDN(): Boolean {
        for (fileName in HTML_FILES) {
            SafeLogger.info("Recherche du fichier HTML: $fileName")
            val files = FilenameIndex.getVirtualFilesByName(
                fileName, 
                GlobalSearchScope.projectScope(project)
            )
            
            for (file in files) {
                SafeLogger.info("Analyse de ${file.path}")
                val content = file.contentsToByteArray().toString(Charsets.UTF_8)
                if (content.contains("cdn.tailwindcss.com") || 
                    content.contains("tailwindcss@") || 
                    content.contains("tailwind.min.css")) {
                    SafeLogger.info("Inclusion CDN Tailwind trouvée dans ${file.path}")
                    return true
                }
            }
        }
        
        SafeLogger.info("Aucune inclusion CDN Tailwind trouvée")
        return false
    }
    
    /**
     * Extraire les classes Tailwind personnalisées de la configuration
     */
    @Suppress("unused")
    fun extractTailwindClasses(): List<String> {
        val configFiles = findConfigFiles()
        if (configFiles.isEmpty()) {
            SafeLogger.info("Aucun fichier de configuration Tailwind trouvé, utilisation des classes de base")
            return getBasicTailwindClasses()
        }
        
        // Trouver le meilleur fichier de configuration à utiliser (priorité: tailwind.config.js > tailwind.config.ts > postcss.config.js)
        val configFile = configFiles.sortedWith(compareBy { file -> 
            when (file.name) {
                "tailwind.config.js" -> 0
                "tailwind.config.mjs" -> 1
                "tailwind.config.ts" -> 2
                "postcss.config.js" -> 3
                "postcss.config.mjs" -> 4
                "postcss.config.ts" -> 5
                else -> 6
            }
        }).firstOrNull()
        
        if (configFile == null) {
            SafeLogger.info("Aucun fichier de configuration valide trouvé, utilisation des classes de base")
            return getBasicTailwindClasses()
        }
        
        SafeLogger.info("Extraction des classes depuis ${configFile.path}")
        
        return when {
            // JavaScript (ES5, ES6, CommonJS, ESM)
            configFile.name.endsWith(".js") || configFile.name.endsWith(".mjs") -> 
                extractClassesWithNode(configFile.path)
            
            // TypeScript - Compilation avant exécution
            configFile.name.endsWith(".ts") -> 
                extractClassesFromTypeScript(configFile)
            
            // Autres formats non supportés
            else -> {
                SafeLogger.info("Format de fichier non supporté: ${configFile.name}, utilisation des classes de base")
                getBasicTailwindClasses()
            }
        }
    }
    
    /**
     * Extraction spécifique pour les fichiers TypeScript
     */
    private fun extractClassesFromTypeScript(configFile: VirtualFile): List<String> {
        SafeLogger.info("Extraction depuis fichier TypeScript: ${configFile.path}")
        
        // Options de compilation TypeScript
        val tempDir = FileUtil.createTempDirectory("ts_compiler", "", true)
        val tsCompilerOptions = """
            {
                "compilerOptions": {
                    "target": "ES2015",
                    "module": "CommonJS",
                    "esModuleInterop": true,
                    "allowSyntheticDefaultImports": true,
                    "moduleResolution": "node",
                    "resolveJsonModule": true,
                    "downlevelIteration": true,
                    "lib": ["ES2015", "DOM"]
                }
            }
        """.trimIndent()
        
        try {
            // Écrire le fichier tsconfig.json
            val tsconfigFile = tempDir.resolve("tsconfig.json")
            tsconfigFile.writeText(tsCompilerOptions)
            
            // Copier le fichier TypeScript vers le répertoire temporaire
            val tempTsFile = tempDir.resolve("temp-config.ts")
            tempTsFile.writeBytes(configFile.contentsToByteArray())
            
            // Essayer de compiler le TypeScript en JavaScript
            val processBuilder = ProcessBuilder()
            
            // Déterminer si tsc (TypeScript compiler) est disponible
            val tscAvailable = try {
                val checkProcess = if (SystemInfo.isWindows) {
                    ProcessBuilder("cmd", "/c", "npx", "--no-install", "tsc", "--version").start()
                } else {
                    ProcessBuilder("npx", "--no-install", "tsc", "--version").start()
                }
                
                checkProcess.waitFor(10, TimeUnit.SECONDS) && checkProcess.exitValue() == 0
            } catch (e: Exception) {
                SafeLogger.info("TypeScript compiler not available: ${e.message}")
                false
            }
            
            if (tscAvailable) {
                SafeLogger.info("TypeScript compiler trouvé, compilation...")
                
                // Compiler le fichier TypeScript en JavaScript
                if (SystemInfo.isWindows) {
                    processBuilder.command("cmd", "/c", "npx", "--no-install", "tsc", tempTsFile.absolutePath, "--outDir", tempDir.absolutePath)
                } else {
                    processBuilder.command("npx", "--no-install", "tsc", tempTsFile.absolutePath, "--outDir", tempDir.absolutePath)
                }
                
                processBuilder.directory(tempDir)
                processBuilder.redirectErrorStream(true)
                
                val process = processBuilder.start()
                val compileOutput = process.inputStream.bufferedReader().readText()
                process.waitFor(30, TimeUnit.SECONDS)
                
                // Vérifier si la compilation a réussi
                if (process.exitValue() != 0) {
                    SafeLogger.info("Erreur lors de la compilation TypeScript: $compileOutput")
                    return extractClassesWithNode(configFile.path)
                }
                
                // Utiliser le fichier JavaScript généré
                val jsFile = tempDir.resolve("temp-config.js")
                if (jsFile.exists()) {
                    SafeLogger.info("Compilation TypeScript réussie, extraction à partir du JavaScript généré")
                    return extractClassesWithNode(jsFile.absolutePath)
                }
            }
            
            // Si la compilation échoue ou tsc n'est pas disponible, essayer directement avec le fichier TypeScript
            SafeLogger.info("Échec de la compilation TypeScript, tentative d'extraction directe...")
            return extractClassesWithNode(configFile.path)
        } catch (e: Exception) {
            SafeLogger.warn("Erreur lors de l'extraction depuis TypeScript: ${e.message}")
            return extractClassesWithNode(configFile.path)
        } finally {
            // Nettoyer
            try {
                tempDir.deleteRecursively()
            } catch (e: Exception) {
                SafeLogger.info("Impossible de supprimer le répertoire temporaire: ${e.message}")
            }
        }
    }
    
    /**
     * Vérifie si Node.js est disponible sur le système
     */
    @Suppress("unused")
    private fun isNodeAvailable(): Boolean {
        // Essayer d'abord avec le chemin par défaut
        if (tryNodeCommand("node", "--version")) {
            nodePath = "node"
            return true
        }
        
        // Si le chemin par défaut ne fonctionne pas, essayer les chemins communs où Node.js pourrait être installé
        val homeDir = System.getProperty("user.home")
        val commonPaths = listOf(
            "$homeDir/.nvm/versions/node/v14.21.3/bin/node", // Chemin NVM détecté dans les logs
            "$homeDir/.nvm/versions/node/v16.20.0/bin/node",
            "$homeDir/.nvm/versions/node/v18.18.0/bin/node", 
            "$homeDir/.nvm/versions/node/v20.10.0/bin/node",
            "$homeDir/.nvm/current/bin/node",
            "$homeDir/.nvs/default/bin/node",
            "$homeDir/.nodenv/shims/node",
            "$homeDir/.volta/bin/node",
            "/usr/local/bin/node",
            "/usr/bin/node",
            "/opt/local/bin/node",
            "/opt/bin/node",
            "C:\\Program Files\\nodejs\\node.exe",
            "C:\\Program Files (x86)\\nodejs\\node.exe"
        )
        
        for (nodePath in commonPaths) {
            if (tryNodeCommand(nodePath, "--version")) {
                SafeLogger.info("Node.js trouvé à: $nodePath")
                this.nodePath = nodePath
                return true
            }
        }
        
        // Vérifier si nous pouvons trouver un chemin NVM dynamiquement
        try {
            val nvmDir = File("$homeDir/.nvm/versions/node")
            if (nvmDir.exists() && nvmDir.isDirectory) {
                nvmDir.listFiles()?.forEach { versionDir ->
                    if (versionDir.isDirectory) {
                        val path = "${versionDir.absolutePath}/bin/node"
                        if (tryNodeCommand(path, "--version")) {
                            SafeLogger.info("Node.js trouvé via NVM à: $path")
                            this.nodePath = path
                            return true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            SafeLogger.warn("Erreur lors de la recherche des versions NVM: ${e.message}")
        }
        
        SafeLogger.warn("Node.js n'est pas disponible sur le système")
        return false
    }
    
    /**
     * Essaie d'exécuter une commande Node.js avec le chemin spécifié
     */
    private fun tryNodeCommand(nodePath: String, vararg args: String): Boolean {
        return try {
            val processBuilder = ProcessBuilder(listOf(nodePath) + args)
            val process = processBuilder.start()
            
            if (process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0) {
                val nodeVersion = BufferedReader(InputStreamReader(process.inputStream)).readText().trim()
                SafeLogger.info("Version de Node.js détectée ($nodePath): $nodeVersion")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Extraire les classes à l'aide de Node.js pour les fichiers JS/MJS
     */
    private fun extractClassesWithNode(configFilePath: String?): List<String> {
        if (configFilePath == null) {
            SafeLogger.info("No config file path provided")
            return emptyList()
        }

        val tempDir = FileUtil.createTempDirectory("tailwind_extractor", "", true)
        try {
            val extractorScript = tempDir.resolve("tailwind-extractor.js")
            extractorScript.writeBytes(getTailwindExtractorScript().toByteArray())

            SafeLogger.info("Starting extraction with Node: $configFilePath")
            
            // Détecter la meilleure version de Node.js à utiliser
            findBestNodePath()
            
            val processBuilder = ProcessBuilder()
            if (SystemInfo.isWindows) {
                processBuilder.command("cmd", "/c", nodePath, extractorScript.absolutePath, configFilePath)
            } else {
                processBuilder.command(nodePath, extractorScript.absolutePath, configFilePath)
            }

            // Redirect error stream to output stream to avoid Logger.error() being called
            processBuilder.redirectErrorStream(true)
            
            // Utiliser le répertoire du fichier de configuration comme répertoire de travail
            // pour que les imports relatifs fonctionnent correctement
            processBuilder.directory(File(configFilePath).parentFile)
            
            // Ajouter les variables d'environnement nécessaires
            val env = processBuilder.environment()
            env["NODE_PATH"] = findNodeModulesPath(configFilePath)
            
            // Démarrer le processus
            val process = processBuilder.start()

            // Set a timeout for the process
            val timeout = 30L
            val timeoutUnit = TimeUnit.SECONDS

            var extractedClasses: List<String>? = null
            
            // Collecter la sortie du processus
            val outputReader = BufferedReader(InputStreamReader(process.inputStream))
            var stdoutContent = ""
            var line: String?
            
            while (outputReader.readLine().also { line = it } != null) {
                // La sortie JSON est écrite sur stdout uniquement
                // Les logs sont sur stderr (redirigé vers stdout)
                if (line?.startsWith("[") == true || line?.startsWith("{") == true) {
                    // Cela semble être du JSON
                    stdoutContent = line ?: ""
                } else {
                    // C'est probablement un message de log
                    if (line?.startsWith("[ERROR]") == true) {
                        // Log d'erreur
                        SafeLogger.warn(line?.removePrefix("[ERROR] ") ?: "")
                    } else {
                        // Log d'information
                        SafeLogger.info(line ?: "")
                    }
                }
            }
            
            // Attendre la fin du processus avec un timeout
            val completed = process.waitFor(timeout, timeoutUnit)
            if (!completed) {
                SafeLogger.warn("Node process timed out after $timeout $timeoutUnit")
                process.destroyForcibly()
                return getBasicTailwindClasses()
            }

            val exitCode = process.exitValue()
            
            if (exitCode != 0) {
                SafeLogger.warn("Node process exited with code $exitCode")
                return getBasicTailwindClasses()
            }

            // Essayer de parser le JSON des classes extraites
            try {
                if (stdoutContent.isNotBlank()) {
                    // Utiliser une bibliothèque JSON sûre pour le parsing
                    val jsonParser = Json { ignoreUnknownKeys = true }
                    extractedClasses = jsonParser.decodeFromString<List<String>>(stdoutContent)
                    SafeLogger.info("Successfully extracted ${extractedClasses.size} classes from config")
                }
            } catch (e: Exception) {
                SafeLogger.warn("Failed to parse extracted classes JSON: ${e.message}")
                SafeLogger.warn("Raw JSON content: $stdoutContent")
                extractedClasses = null
            }

            // Si l'extraction a réussi, retourner les classes extraites
            if (!extractedClasses.isNullOrEmpty()) {
                return extractedClasses
            }

            SafeLogger.info("Extraction failed or returned empty results, using default classes")
            return getBasicTailwindClasses()
        } catch (e: Exception) {
            SafeLogger.warn("Error during class extraction: ${e.message}")
            return getBasicTailwindClasses()
        } finally {
            try {
                tempDir.deleteRecursively()
            } catch (e: Exception) {
                SafeLogger.info("Failed to delete temp directory: ${e.message}")
            }
        }
    }
    
    /**
     * Trouver le meilleur chemin pour Node.js
     */
    private fun findBestNodePath() {
        val possibleNodePaths = listOf(
            "node",                  // Dépend du PATH
            "/usr/bin/node",         // Linux et macOS
            "/usr/local/bin/node",   // macOS (Homebrew)
            "/opt/homebrew/bin/node",// macOS Apple Silicon (Homebrew)
            "C:\\Program Files\\nodejs\\node.exe",  // Windows 64-bit
            "C:\\Program Files (x86)\\nodejs\\node.exe"  // Windows 32-bit
        )
        
        // Essayer de trouver une installation Node.js qui fonctionne
        for (path in possibleNodePaths) {
            try {
                val processBuilder = ProcessBuilder()
                if (SystemInfo.isWindows) {
                    processBuilder.command("cmd", "/c", path, "--version")
                } else {
                    processBuilder.command(path, "--version")
                }
                
                processBuilder.redirectErrorStream(true)
                val process = processBuilder.start()
                
                if (process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0) {
                    val version = process.inputStream.bufferedReader().readText().trim()
                    SafeLogger.info("Found Node.js $version at $path")
                    nodePath = path
                    return
                }
            } catch (e: Exception) {
                // Ignorer et essayer le chemin suivant
            }
        }
        
        // Si aucune installation n'a été trouvée, utiliser "node" par défaut
        SafeLogger.info("No specific Node.js installation found, using 'node' from PATH")
        nodePath = "node"
    }
    
    /**
     * Trouver le chemin vers node_modules pour les imports
     */
    private fun findNodeModulesPath(configFilePath: String): String {
        val configDir = File(configFilePath).parentFile
        var currentDir: File? = configDir
        
        // Remonter dans l'arborescence à la recherche de node_modules
        while (currentDir != null) {
            val nodeModulesDir = File(currentDir, "node_modules")
            if (nodeModulesDir.exists() && nodeModulesDir.isDirectory) {
                SafeLogger.info("Found node_modules at ${nodeModulesDir.absolutePath}")
                return nodeModulesDir.absolutePath
            }
            currentDir = currentDir.parentFile
        }
        
        // Si node_modules n'est pas trouvé, retourner le répertoire du projet
        SafeLogger.info("No node_modules found, using config directory")
        return configDir.absolutePath
    }
    
    /**
     * Retourne une liste de base des classes Tailwind les plus courantes
     */
    private fun getBasicTailwindClasses(): List<String> {
        SafeLogger.info("Utilisation des classes Tailwind de base")
        return listOf(
            // Layout
            "container", "flex", "grid", "block", "inline", "hidden",
            
            // Spacing
            "p-0", "p-1", "p-2", "p-4", "p-8",
            "px-0", "px-1", "px-2", "px-4", "px-8",
            "py-0", "py-1", "py-2", "py-4", "py-8",
            "m-0", "m-1", "m-2", "m-4", "m-8",
            "mx-0", "mx-1", "mx-2", "mx-4", "mx-8",
            "my-0", "my-1", "my-2", "my-4", "my-8",
            
            // Typography
            "text-xs", "text-sm", "text-base", "text-lg", "text-xl", "text-2xl",
            "font-thin", "font-normal", "font-medium", "font-bold",
            
            // Colors
            "text-black", "text-white", 
            "text-gray-100", "text-gray-200", "text-gray-300", "text-gray-400", "text-gray-500", "text-gray-600", "text-gray-700", "text-gray-800", "text-gray-900",
            "bg-black", "bg-white", 
            "bg-gray-100", "bg-gray-200", "bg-gray-300", "bg-gray-400", "bg-gray-500", "bg-gray-600", "bg-gray-700", "bg-gray-800", "bg-gray-900",
            
            // Flexbox
            "flex-row", "flex-col", "justify-start", "justify-center", "justify-end",
            "items-start", "items-center", "items-end",
            
            // Border
            "border", "border-0", "border-2", "border-4", "border-8",
            "rounded", "rounded-sm", "rounded-md", "rounded-lg", "rounded-full"
        )
    }

    /**
     * Get the Tailwind extractor script content from resources
     */
    private fun getTailwindExtractorScript(): String {
        SafeLogger.info("Loading Tailwind extractor script content")
        val resourceStream = javaClass.classLoader.getResourceAsStream("tailwind-extractor.js")
        
        if (resourceStream == null) {
            SafeLogger.warn("Tailwind extractor script not found in resources")
            // Create a minimal script that returns an empty array instead of throwing
            return """
                // Fallback script
                console.log('[INFO] Using fallback script as extractor script was not found in resources');
                // Output empty array as JSON
                console.log("[]");
            """.trimIndent()
        }
        
        try {
            return BufferedReader(InputStreamReader(resourceStream)).use { it.readText() }
        } catch (e: Exception) {
            SafeLogger.warn("Error reading Tailwind extractor script: ${e.message}")
            // Create a minimal script that returns an empty array
            return """
                // Fallback script
                console.log('[INFO] Using fallback script due to error reading the original script');
                // Output empty array as JSON
                console.log("[]");
            """.trimIndent()
        }
    }
}