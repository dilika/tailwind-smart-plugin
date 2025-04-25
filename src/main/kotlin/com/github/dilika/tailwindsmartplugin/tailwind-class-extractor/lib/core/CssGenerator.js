// lib/core/CssGenerator.js
import postcss from 'postcss';
import tailwindcss from 'tailwindcss';
import discardComments from 'postcss-discard-comments';
import { debug } from '../utils/debug.js';

export class CssGenerator {
    constructor(options = {}) {
        this.safelist = options.safelist || this.getDefaultSafeList();
        this.minify = options.minify || false;
    }

    async generate(config) {
        const processor = this.createProcessor(config);
        const css = this.getBaseCss();

        try {
            const startTime = Date.now();
            const result = await processor.process(css, {
                from: undefined,
                to: undefined,
                map: false
            });

            debug(`CSS généré en ${Date.now() - startTime}ms`);
            return {
                css: result.css,
                classes: this.extractClasses(result.css),
                stats: {
                    size: result.css.length,
                    rules: this.countRules(result.css)
                }
            };
        } catch (error) {
            debug('Erreur de génération CSS:', error);
            throw new Error(`Échec de la génération CSS: ${error.message}`);
        }
    }

    createProcessor(config) {
        const plugins = [
            tailwindcss(this.extendConfig(config)),
            discardComments()
        ];

        if (this.minify) {
            plugins.push(require('cssnano')({ preset: 'default' }));
        }

        return postcss(plugins);
    }

    extendConfig(config) {
        return {
            ...config,
            corePlugins: config.corePlugins || {},
            safelist: [
                ...(config.safelist || []),
                ...this.safelist
            ],
            // Optimisation pour l'analyse IDE
            theme: {
                ...config.theme,
                extend: {
                    ...config.theme?.extend,
                    // Force l'inclusion des utilitaires communes
                    spacing: config.theme?.spacing || {},
                    colors: config.theme?.colors || {}
                }
            }
        };
    }

    getBaseCss() {
        return `
      @tailwind base;
      @tailwind components;
      @tailwind utilities;
      
      /* Classes forcées pour l'analyse IDE */
      .force-include { @apply sr-only container; }
    `;
    }

    extractClasses(css) {
        const classRegex = /\.([^{}:\s][^{}:]*)(?::[^{}]+)?(?=\s*\{)/g;
        const classes = new Set();
        let match;

        while ((match = classRegex.exec(css)) !== null) {
            const className = match[1].replace(/\\/g, '');
            if (className && !className.startsWith('force-include')) {
                classes.add(className);
            }
        }

        return Array.from(classes);
    }

    countRules(css) {
        return (css.match(/{[^}]*}/g) || []).length;
    }

    getDefaultSafeList() {
        return [
            { pattern: /^sr-only$/ },
            { pattern: /^container$/ },
            { pattern: /^[wh]-./ },
            { pattern: /^[mp][trblxy]?-./ },
            // Classes dynamiques communes
            { pattern: /^bg-(red|blue|green)-(\d+)$/ },
            { pattern: /^text-(xs|sm|base|lg|xl)$/ }
        ];
    }
}