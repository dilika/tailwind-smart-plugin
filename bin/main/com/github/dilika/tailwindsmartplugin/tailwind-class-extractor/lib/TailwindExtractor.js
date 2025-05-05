import path from "path";
import fs from "fs/promises";
// lib/TailwindExtractor.js - Version optimisée pour Tailwind v4
import { ConfigLoader } from './core/ConfigLoader.js';
import { CssGenerator } from './core/CssGenerator.js';
import { FileWatcher } from './watchers/FileWatcher.js';
import { ClassParser } from './parsers/ClassParser.js';
import { performance } from 'perf_hooks';

export class TailwindExtractor {
    constructor(options = {}) {
        this.options = {
            projectRoot: process.cwd(),
            watch: false,
            debug: false,
            safeContentList: [
                './src/**/*.{html,js,jsx,ts,tsx,vue}',
                './components/**/*.{js,jsx,ts,tsx,vue}',
                './pages/**/*.{js,jsx,ts,tsx,vue}',
                './*.html'
            ],
            tailwindVersion: 'v4',
            enableCache: true,
            cacheTimeout: 3600000, // 1 heure en millisecondes
            ...options
        };

        // Instanciation avec configuration adaptée pour Tailwind v4
        this.parser = new ClassParser({
            types: {
                // Ajout de types spécifiques à Tailwind v4
                gradient: {
                    color: '#f59e0b', // amber-500
                    icon: '⬤', // cercle plein
                    category: 'Gradient',
                    keywords: ['gradient', 'from', 'to', 'via'],
                    examples: [
                        'from-blue-500 to-purple-500',
                        'bg-gradient-to-r from-primary to-secondary'
                    ]
                },
                transform: {
                    color: '#8b5cf6', // violet-500
                    icon: '⟳', // flèche circulaire
                    category: 'Transform',
                    keywords: ['transform', 'rotate', 'scale', 'translate', 'skew'],
                    examples: [
                        'rotate-45 scale-110',
                        'translate-x-4 -translate-y-2'
                    ]
                }
            }
        });
        
        this.configLoader = new ConfigLoader(this.options.projectRoot);
        this.cssGenerator = new CssGenerator();
        this.fileWatcher = null;
        
        // Caches pour les performances
        this.classCache = new Map();
        this.lastCacheUpdate = 0;
        this.cacheHits = 0;
        this.cacheMisses = 0;
    }

    /**
     * Exécute l'extracteur et renvoie les classes avec métadonnées
     * @returns {Promise<Object>} Objet contenant les classes, métadonnées et statistiques
     */
    async run() {
        const startTime = performance.now();
        
        try {
            // Vérifier si nous pouvons utiliser le cache
            if (this.options.enableCache && this._isValidCache()) {
                this.cacheHits++;
                if (this.options.debug) {
                    console.log(`[TailwindExtractor] Using cached data (${this.classCache.size} classes, hit rate: ${(this.cacheHits / (this.cacheHits + this.cacheMisses) * 100).toFixed(2)}%)`);
                }
                return this._formatCachedResult();
            }
            
            this.cacheMisses++;
            // 1. Chargement sécurisé de la config
            const config = await this.loadConfig();

            // 2. Génération CSS
            const { css, classes } = await this.cssGenerator.generate(config);

            // 3. Parsing des classes avec support amélioré pour Tailwind v4
            const parsed = this.parser.parse(css, classes);
            
            // 4. Mise en cache du résultat
            if (this.options.enableCache) {
                this._updateCache(parsed);
            }
            
            // 5. Formatage du résultat avec informations de performances
            const endTime = performance.now();
            const executionTime = endTime - startTime;
            
            const result = {
                classes: parsed || {},
                stats: {
                    total: parsed ? Object.keys(parsed).length : 0,
                    generatedAt: new Date().toISOString(),
                    executionTime: `${executionTime.toFixed(2)}ms`,
                    tailwindVersion: this.options.tailwindVersion,
                    cacheInfo: {
                        enabled: this.options.enableCache,
                        hits: this.cacheHits,
                        misses: this.cacheMisses,
                        hitRate: (this.cacheHits / (this.cacheHits + this.cacheMisses) * 100).toFixed(2) + '%'
                    }
                }
            };
            
            if (this.options.debug) {
                console.log(`[TailwindExtractor] Extracted ${result.stats.total} classes in ${result.stats.executionTime}`);
            }
            
            return result;

        } catch (error) {
            throw new Error(`Extraction failed: ${error.message}`);
        }
    }

    async loadConfig() {
        const config = await this.configLoader.load();

        // Normalisation du content (solutions aux différents formats)
        config.content = this.normalizeContent(config.content);

        return config;
    }

    normalizeContent(content) {
        // Cas 1: content est un array
        if (Array.isArray(content)) {
            return content;
        }

        // Cas 2: content est un objet avec fichiers (v3+)
        if (content && content.files) {
            return Array.isArray(content.files) ? content.files : [content.files];
        }

        // Cas 3: Format inconnu - retourne la valeur sécurisée
        return this.options.safeContentList;
    }

    /**
     * Configure le mode observation des fichiers pour la mise à jour automatique
     * @param {Object} config Configuration Tailwind
     */
    setupWatchMode(config) {
        this.fileWatcher = new FileWatcher(
            this.options.projectRoot,
            config.content,
            () => {
                if (this.options.debug) {
                    console.log('[TailwindExtractor] File changes detected, updating classes...');
                }
                this.clearCache(); // Invalider le cache lors des changements
                return this.run().catch(console.error);
            }
        );
    }
    
    /**
     * Vérifie si le cache est valide (non expiré)
     * @returns {boolean} True si le cache est valide
     * @private
     */
    _isValidCache() {
        const now = Date.now();
        return (
            this.classCache.size > 0 &&
            (now - this.lastCacheUpdate) < this.options.cacheTimeout
        );
    }
    
    /**
     * Met à jour le cache avec les nouvelles données
     * @param {Object} classData Données de classe parsées
     * @private
     */
    _updateCache(classData) {
        this.classCache.clear();
        for (const [className, data] of Object.entries(classData)) {
            this.classCache.set(className, data);
        }
        this.lastCacheUpdate = Date.now();
    }
    
    /**
     * Convertit les données en cache en format de résultat
     * @returns {Object} Résultat formaté
     * @private
     */
    _formatCachedResult() {
        const classes = {};
        for (const [className, data] of this.classCache) {
            classes[className] = data;
        }
        
        return {
            classes,
            stats: {
                total: this.classCache.size,
                generatedAt: new Date().toISOString(),
                fromCache: true,
                cacheAge: `${((Date.now() - this.lastCacheUpdate) / 1000).toFixed(1)}s`,
                tailwindVersion: this.options.tailwindVersion
            }
        };
    }
    
    /**
     * Efface le cache actuel
     */
    clearCache() {
        if (this.options.debug) {
            console.log('[TailwindExtractor] Cache cleared');
        }
        this.classCache.clear();
        this.lastCacheUpdate = 0;
    }
}