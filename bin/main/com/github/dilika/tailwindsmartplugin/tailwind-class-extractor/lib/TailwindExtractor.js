import path from "path";
import fs from "fs/promises";
// lib/TailwindExtractor.js
import { ConfigLoader } from './core/ConfigLoader.js';
import { CssGenerator } from './core/CssGenerator.js';
import { FileWatcher } from './watchers/FileWatcher.js';
import {ClassParser} from './parsers/ClassParser.js';

export class TailwindExtractor {
    constructor(options = {}) {
        this.options = {
            projectRoot: process.cwd(),
            watch: false,
            debug: false,
            safeContentList: [
                './src/**/*.{html,js,jsx,ts,tsx}',
                './*.html'
            ],
            ...options
        };

        this.parser = new ClassParser();
        this.configLoader = new ConfigLoader(this.options.projectRoot);
        this.cssGenerator = new CssGenerator();
        this.fileWatcher = null;
    }

    async run() {
        try {
            // 1. Chargement sécurisé de la config
            const config = await this.loadConfig();

            // 2. Génération CSS
            const { css, classes } = await this.cssGenerator.generate(config);

            // 3. Parsing des classes
            const parsed = this.parser.parse(css, classes);

            // 4. Formatage du résultat
            return {
                classes: parsed || {},
                stats: {
                    total: parsed ? Object.keys(parsed).length : 0,
                    generatedAt: new Date().toISOString()
                }
            };

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

    setupWatchMode(config) {
        this.fileWatcher = new FileWatcher(
            this.options.projectRoot,
            config.content,
            () => this.run().catch(console.error)
        );
    }
}