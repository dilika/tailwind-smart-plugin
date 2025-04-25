// lib/parsers/ClassParser.js
// lib/parsers/ClassParser.js

// Configurations par défaut
const DEFAULT_TYPE_METADATA = Object.freeze({
    spacing: Object.freeze({
        color: '#38bdf8', // blue-400
        icon: '⇨',
        category: 'Spacing',
        keywords: Object.freeze(['margin', 'padding', 'gap', 'space']),
        examples: Object.freeze([
            'mt-4 for top margin',
            'px-2 for horizontal padding'
        ])
    }),
    color: Object.freeze({
        color: '#f472b6', // pink-400
        icon: '■',
        category: 'Color',
        keywords: Object.freeze(['text', 'bg', 'border', 'color']),
        examples: Object.freeze([
            'text-red-500 for red text',
            'bg-blue-200 for light blue background'
        ])
    }),
    utility: Object.freeze({
        color: '#94a3b8', // slate-400
        icon: '•',
        category: 'Utility',
        keywords: Object.freeze([]),
        examples: Object.freeze([])
    })
});

const DEFAULT_PROPERTY_MAP = Object.freeze({
    // Spacing
    m: Object.freeze({
        property: 'margin',
        axis: 'xy',
        keywords: ['margin']
    }),
    mt: Object.freeze({
        property: 'margin-top',
        axis: 'y',
        keywords: ['margin', 'top']
    }),
    mb: Object.freeze({
        property: 'margin-bottom',
        axis: 'y',
        keywords: ['margin', 'bottom']
    }),
    ml: Object.freeze({
        property: 'margin-left',
        axis: 'x',
        keywords: ['margin', 'left']
    }),
    mr: Object.freeze({
        property: 'margin-right',
        axis: 'x',
        keywords: ['margin', 'right']
    }),
    mx: Object.freeze({
        property: ['margin-left', 'margin-right'],
        axis: 'x',
        keywords: ['margin', 'horizontal']
    }),
    my: Object.freeze({
        property: ['margin-top', 'margin-bottom'],
        axis: 'y',
        keywords: ['margin', 'vertical']
    }),
    p: Object.freeze({
        property: 'padding',
        axis: 'xy',
        keywords: ['padding']
    }),
    pt: Object.freeze({
        property: 'padding-top',
        axis: 'y',
        keywords: ['padding', 'top']
    }),
    pb: Object.freeze({
        property: 'padding-bottom',
        axis: 'y',
        keywords: ['padding', 'bottom']
    }),
    pl: Object.freeze({
        property: 'padding-left',
        axis: 'x',
        keywords: ['padding', 'left']
    }),
    pr: Object.freeze({
        property: 'padding-right',
        axis: 'x',
        keywords: ['padding', 'right']
    }),
    px: Object.freeze({
        property: ['padding-left', 'padding-right'],
        axis: 'x',
        keywords: ['padding', 'horizontal']
    }),
    py: Object.freeze({
        property: ['padding-top', 'padding-bottom'],
        axis: 'y',
        keywords: ['padding', 'vertical']
    }),
    gap: Object.freeze({
        property: 'gap',
        axis: 'xy',
        keywords: ['gap']
    }),
    // Color
    text: Object.freeze({
        property: 'color',
        axis: null,
        keywords: ['text', 'color']
    }),
    bg: Object.freeze({
        property: 'background-color',
        axis: null,
        keywords: ['background', 'bg']
    }),
    border: Object.freeze({
        property: 'border-color',
        axis: null,
        keywords: ['border']
    })
});

const DEFAULT_SIZE_VALUES = Object.freeze({
    '0': '0', '0.5': '0.125rem', '1': '0.25rem',
    '1.5': '0.375rem', '2': '0.5rem', '2.5': '0.625rem',
    '3': '0.75rem', '3.5': '0.875rem', '4': '1rem',
    '5': '1.25rem', '6': '1.5rem', '7': '1.75rem',
    '8': '2rem', '9': '2.25rem', '10': '2.5rem'
});

const DEFAULT_VARIANTS = Object.freeze({
    hover: { color: '#4f46e5', icon: 'H' },
    focus: { color: '#7c3aed', icon: 'F' },
    active: { color: '#9333ea', icon: 'A' },
    dark: { color: '#475569', icon: 'D' },
    sm: { color: '#64748b', icon: 'S' },
    md: { color: '#64748b', icon: 'M' },
    lg: { color: '#64748b', icon: 'L' }
});

export class ClassParser {
    constructor(config = {}) {
        // Configurations
        this._typeMetadata = Object.freeze({
            ...DEFAULT_TYPE_METADATA,
            ...config.types
        });
        this._propertyMap = Object.freeze({
            ...DEFAULT_PROPERTY_MAP,
            ...config.properties
        });
        this._sizeValues = Object.freeze({
            ...DEFAULT_SIZE_VALUES,
            ...config.sizes
        });
        this._variants = Object.freeze({
            ...DEFAULT_VARIANTS,
            ...config.variants
        });
        this._i18n = config.i18n || {};

        // Caches
        this._classCache = new Map();
        this._cssCache = new Map();
        this._resultCache = new Map();
        this._usageStats = new Map();
    }

    parse(css, classes) {
        const cssRules = this._extractCssRules(css);
        const result = {};

        // Mise à jour des statistiques d'utilisation
        classes.forEach(cls => {
            this._usageStats.set(cls, (this._usageStats.get(cls) || 0) + 1);
        });

        for (const className of classes) {
            if (this._resultCache.has(className)) {
                result[className] = this._resultCache.get(className);
                continue;
            }

            const parsed = this._parseClass(className);
            const cssRule = cssRules.get(className) || this._generateCss(parsed);
            const formatted = this._formatResult(parsed, cssRule);

            this._resultCache.set(className, formatted);
            result[className] = formatted;
        }

        return result;
    }

    _extractCssRules(css) {
        if (this._cssCache.has(css)) {
            return this._cssCache.get(css);
        }

        const rules = new Map();
        const ruleRegex = /\.([^{]+)\{([^}]+)\}/g;
        let match;

        while ((match = ruleRegex.exec(css))) {
            const className = match[1].trim();
            if (!rules.has(className)) {
                rules.set(className, `.${className} { ${match[2].trim()} }`);
            }
        }

        this._cssCache.set(css, rules);
        return rules;
    }

    _parseClass(className) {
        const variantMatch = className.match(/^([a-z-]+):(.+)$/);
        const baseClass = variantMatch ? variantMatch[2] : className;
        const [prefix, value] = this._splitClassParts(baseClass);

        const parsed = {
            base: baseClass,
            variant: variantMatch?.[1] || null,
            arbitrary: /^\[.+\]$/.test(baseClass),
            type: this._determineType(prefix),
            property: this._propertyMap[prefix] || {
                property: prefix,
                axis: null,
                keywords: []
            },
            value,
            tokens: value ? [prefix, value] : [prefix]
        };

        // Traitement spécial pour les valeurs arbitraires
        if (parsed.arbitrary) {
            parsed.arbitraryValue = this._parseArbitraryValue(baseClass);
        }

        return parsed;
    }

    _generateCss(parsed) {
        const cacheKey = `${parsed.base}|${parsed.type}`;
        if (this._cssCache.has(cacheKey)) {
            return this._cssCache.get(cacheKey);
        }

        let css;
        if (parsed.arbitrary) {
            css = this._generateArbitraryCss(parsed);
        } else {
            switch (parsed.type) {
                case 'spacing':
                    css = this._generateSpacingCss(parsed);
                    break;
                case 'color':
                    css = this._generateColorCss(parsed);
                    break;
                default:
                    css = `.${parsed.base} { /* ${parsed.type} utility */ }`;
            }
        }

        this._cssCache.set(cacheKey, css);
        return css;
    }

    _formatResult(parsed, cssRule) {
        const meta = this._getTypeMetadata(parsed.type);
        const variantMeta = parsed.variant ? this._variants[parsed.variant] : null;
        const allKeywords = [
            ...meta.keywords,
            ...parsed.property.keywords,
            parsed.base,
            ...parsed.tokens
        ];

        return {
            completion: {
                text: parsed.base,
                displayText: this._getDisplayText(parsed, meta, variantMeta),
                style: this._getCompletionStyle(parsed, meta, variantMeta),
                relevance: this._calculateRelevance(parsed),
                meta: parsed
            },
            documentation: {
                css: this._formatCss(cssRule),
                markdown: this._generateMarkdown(parsed, meta, cssRule),
                examples: this._generateExamples(parsed, meta)
            },
            keywords: [...new Set(allKeywords)],
            technical: {
                axis: parsed.property.axis,
                properties: Array.isArray(parsed.property.property)
                    ? parsed.property.property
                    : [parsed.property.property],
                value: parsed.arbitrary ? parsed.arbitraryValue : this._sizeValues[parsed.value] || parsed.value
            }
        };
    }

    // Helper methods
    _splitClassParts(className) {
        const dashIndex = className.indexOf('-');
        return dashIndex > 0
            ? [className.slice(0, dashIndex), className.slice(dashIndex + 1)]
            : [className, null];
    }

    _determineType(prefix) {
        if (this._propertyMap[prefix]) {
            return this._propertyMap[prefix].keywords.includes('color')
                ? 'color'
                : 'spacing';
        }
        return 'utility';
    }

    _getTypeMetadata(type) {
        return this._typeMetadata[type] || this._typeMetadata.utility;
    }

    _calculateRelevance(parsed) {
        let score = 0;
        if (parsed.variant) score += 10;
        if (parsed.arbitrary) score += 5;

        // Score basé sur l'utilisation
        if (this._usageStats.has(parsed.base)) {
            score += Math.min(20, Math.log2(this._usageStats.get(parsed.base)) * 2);
        }

        return score;
    }

    _formatCss(css) {
        return css
            .replace(/\{/g, ' {\n  ')
            .replace(/\}/g, '\n}')
            .replace(/;/g, ';\n  ');
    }

    _generateMarkdown(parsed, meta, css) {
        const translatedCategory = this._i18n.category?.[meta.category] || meta.category;

        return [
            `### \`${parsed.base}\``,
            `**${this._i18n.categoryLabel || 'Category'}**: ${translatedCategory}`,
            '',
            '#### CSS:',
            '```css',
            this._formatCss(css),
            '```',
            '',
            '#### Examples:',
            ...this._generateExamples(parsed, meta).map(ex => `- ${ex}`),
            '',
            parsed.variant && `**${this._i18n.variant || 'Variant'}**: \`${parsed.variant}\``,
            parsed.arbitrary && `**${this._i18n.arbitraryValue || 'Arbitrary value'}**`
        ].filter(Boolean).join('\n');
    }

    _generateExamples(parsed, meta) {
        const examples = [...(meta.examples || [])];

        if (parsed.type === 'spacing') {
            examples.push(this._generateSpacingExample(parsed));
        } else if (parsed.type === 'color') {
            examples.push(this._generateColorExample(parsed));
        } else {
            examples.push(`<div class="${parsed.base}">${meta.category.toLowerCase()} example</div>`);
        }

        return examples;
    }

    // Méthodes spécifiques au type
    _generateSpacingCss(parsed) {
        const value = this._sizeValues[parsed.value] || parsed.value;
        const properties = Array.isArray(parsed.property.property)
            ? parsed.property.property
            : [parsed.property.property];

        const declarations = properties.map(prop => `${prop}: ${value};`).join(' ');
        return `.${parsed.base} { ${declarations} }`;
    }

    _generateColorCss(parsed) {
        return `.${parsed.base} { ${parsed.property.property}: var(--${parsed.base.replace('-', '-')}); }`;
    }

    _generateArbitraryCss(parsed) {
        if (parsed.arbitraryValue?.type === 'css-value') {
            return `.${parsed.base} { ${parsed.property.property || parsed.prefix}: ${parsed.arbitraryValue.value}; }`;
        }
        return `.${parsed.base} { /* arbitrary value: ${parsed.base} */ }`;
    }

    _parseArbitraryValue(className) {
        const match = className.match(/^\[(.+)\]$/);
        if (!match) return null;

        const content = match[1];

        // Détection des valeurs CSS simples
        if (/^([a-z-]+):\s*.+;?$/.test(content)) {
            return { type: 'css-declaration', value: content };
        }

        // Valeurs CSS simples
        if (/^([0-9a-z%#(),. ]+)$/.test(content)) {
            return { type: 'css-value', value: content };
        }

        return { type: 'unknown', value: content };
    }

    _generateSpacingExample(parsed) {
        const direction = parsed.property.axis === 'y' ? 'vertical' : 'horizontal';
        const exampleClass = parsed.variant ? `${parsed.variant}:${parsed.base}` : parsed.base;

        return `
<div class="container ${exampleClass}" style="display: flex; flex-direction: ${direction === 'vertical' ? 'column' : 'row'}; border: 1px dashed #ccc; padding: 0.5rem;">
    <div style="background: #e2e8f0; padding: 0.5rem; border: 1px solid #cbd5e1;">Item 1</div>
    <div style="background: #e2e8f0; padding: 0.5rem; border: 1px solid #cbd5e1;">Item 2</div>
</div>
`.trim();
    }

    _generateColorExample(parsed) {
        const exampleClass = parsed.variant ? `${parsed.variant}:${parsed.base}` : parsed.base;
        const propertyName = parsed.property.property.replace('-', ' ');

        return `
<div class="${exampleClass}" style="padding: 1rem; border: 1px solid #ccc;">
    ${propertyName} example
</div>
`.trim();
    }

    _getDisplayText(parsed, meta, variantMeta) {
        let display = meta.icon;

        if (variantMeta) {
            display += ` ${variantMeta.icon}`;
        }

        display += ` ${parsed.base}`;

        return display;
    }

    _getCompletionStyle(parsed, meta, variantMeta) {
        const color = variantMeta?.color || meta.color;
        return `color: ${color}`;
    }

    // Méthodes utilitaires publiques
    isValidClass(className) {
        if (!className) return false;

        // Vérifier les variants
        const variantMatch = className.match(/^([a-z-]+):(.+)$/);
        if (variantMatch && !this._variants[variantMatch[1]]) {
            return false;
        }

        const baseClass = variantMatch ? variantMatch[2] : className;

        // Vérifier les classes arbitraires
        if (/^\[.+\]$/.test(baseClass)) {
            return true; // Accepte toutes les valeurs arbitraires
        }

        // Vérifier les classes régulières
        const [prefix, value] = this._splitClassParts(baseClass);

        // Le préfixe doit exister dans propertyMap ou être une classe utilitaire connue
        if (!this._propertyMap[prefix] && !this._isKnownUtility(prefix)) {
            return false;
        }

        // Si une valeur est présente, elle doit être valide
        if (value && !this._isValidValue(prefix, value)) {
            return false;
        }

        return true;
    }

    _isKnownUtility(prefix) {
        // Implémentez cette méthode selon vos besoins
        return false;
    }

    _isValidValue(prefix, value) {
        // Pour les propriétés de spacing, vérifie les tailles connues
        if (this._propertyMap[prefix]?.keywords.includes('margin') ||
            this._propertyMap[prefix]?.keywords.includes('padding')) {
            return value in this._sizeValues || /^(\d+\/\d+|[0-9.]+px|auto|full)$/.test(value);
        }

        // Pour les couleurs, vérifie les formats de couleur
        if (this._propertyMap[prefix]?.keywords.includes('color')) {
            return /^([a-z]+-\d+|#[0-9a-f]{3,6}|rgb\(|hsl\()/.test(value);
        }

        return true;
    }

    clearCache() {
        this._classCache.clear();
        this._cssCache.clear();
        this._resultCache.clear();
    }

    // Méthode de benchmark
    parseBenchmark(classes, iterations = 1000) {
        const start = performance.now();
        for (let i = 0; i < iterations; i++) {
            this.parse('', classes);
        }
        return performance.now() - start;
    }
}