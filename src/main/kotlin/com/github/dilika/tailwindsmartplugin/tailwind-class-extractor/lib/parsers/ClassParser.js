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
        const result = {};

        // Parsing de base des règles CSS
        const cssRules = this._extractCssRules(css);

        // Support pour les classes combinées (séparation par espace)
        const allClasses = new Set();
        for (const classOrGroup of classes) {
            // Ignorer les classes vides
            if (!classOrGroup || classOrGroup.trim() === '') continue;

            // Détecter si c'est une combinaison de classes
            if (classOrGroup.includes(' ')) {
                // Fractionner en classes individuelles
                const individualClasses = classOrGroup.split(/\s+/);
                individualClasses.forEach(cls => allClasses.add(cls.trim()));

                // Ajouter aussi la combinaison comme une entité unique pour l'autocomplétion
                allClasses.add(classOrGroup.trim());
            } else {
                allClasses.add(classOrGroup.trim());
            }
        }

        // Parse chaque classe
        for (const className of allClasses) {
            // Skip if empty (check again after splitting)
            if (!className || className.trim() === '') continue;

            // Détection des variantes (comme hover:, focus:, dark:)
            const hasVariant = className.includes(':');
            let baseClass = className;
            let variant = null;

            if (hasVariant) {
                const parts = className.split(':');
                variant = parts[0];
                baseClass = parts.slice(1).join(':');
            }

            // Mémoisation pour éviter le duplicate parsing
            if (!this._classCache.has(baseClass)) {
                // Détection des valeurs arbitraires
                if (baseClass.includes('[') && baseClass.includes(']')) {
                    this._classCache.set(baseClass, this._parseArbitraryValue(baseClass));
                } else {
                    this._classCache.set(baseClass, this._parseClass(baseClass));
                }
            }

            const parsed = this._classCache.get(baseClass);
            if (!parsed) continue;  // Skip invalid classes

            // Ajouter les infos de variante si présentes
            if (variant) {
                parsed.variant = variant;
                parsed.hasVariant = true;
            }

            // Génération du CSS
            if (!this._cssCache.has(baseClass)) {
                this._cssCache.set(baseClass, this._generateCss(parsed));
            }

            // Calcul de la pertinence pour le tri
            const relevance = this._calculateRelevance(parsed);

            // Déterminer si c'est une classe combinée
            const isCombined = className.includes(' ');

            // Format final de l'objet pour cette classe
            result[className] = {
                ...this._formatResult(parsed, cssRules[baseClass] || this._cssCache.get(baseClass) || ''),
                relevance,
                variant: variant,
                hasVariant: !!variant,
                isCombination: isCombined,
                components: isCombined ? className.split(/\s+/).map(c => c.trim()) : null
            };
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
        // Valeurs standard de Tailwind CSS
        const valueMap = {
            '0': '0px',
            'px': '1px',
            '0.5': '0.125rem',
            '1': '0.25rem',
            '1.5': '0.375rem',
            '2': '0.5rem',
            '2.5': '0.625rem',
            '3': '0.75rem',
            '3.5': '0.875rem',
            '4': '1rem',
            '5': '1.25rem',
            '6': '1.5rem',
            '8': '2rem',
            '10': '2.5rem',
            '12': '3rem',
            '16': '4rem',
            '20': '5rem',
            '24': '6rem',
            '32': '8rem',
            '40': '10rem',
            '48': '12rem',
            '56': '14rem',
            '64': '16rem',
            '72': '18rem',
            '80': '20rem',
            '96': '24rem',
            'full': '100%'
        };

        // Convertir la valeur Tailwind en valeur CSS
        const cssValue = valueMap[parsed.value] || parsed.value || '1rem';
        return `${parsed.prefix}: ${cssValue};`;
    }

    _generateColorCss(parsed) {
        // Map des couleurs Tailwind v4
        const colorMap = {
            'slate': { 50: '#f8fafc', 100: '#f1f5f9', 500: '#64748b', 900: '#0f172a' },
            'gray': { 50: '#f9fafb', 100: '#f3f4f6', 500: '#6b7280', 900: '#111827' },
            'zinc': { 50: '#fafafa', 100: '#f4f4f5', 500: '#71717a', 900: '#18181b' },
            'red': { 50: '#fef2f2', 100: '#fee2e2', 500: '#ef4444', 900: '#7f1d1d' },
            'orange': { 50: '#fff7ed', 100: '#ffedd5', 500: '#f97316', 900: '#7c2d12' },
            'amber': { 50: '#fffbeb', 100: '#fef3c7', 500: '#f59e0b', 900: '#78350f' },
            'yellow': { 50: '#fefce8', 100: '#fef9c3', 500: '#eab308', 900: '#713f12' },
            'lime': { 50: '#f7fee7', 100: '#ecfccb', 500: '#84cc16', 900: '#365314' },
            'green': { 50: '#f0fdf4', 100: '#dcfce7', 500: '#22c55e', 900: '#14532d' },
            'emerald': { 50: '#ecfdf5', 100: '#d1fae5', 500: '#10b981', 900: '#064e3b' },
            'teal': { 50: '#f0fdfa', 100: '#ccfbf1', 500: '#14b8a6', 900: '#134e4a' },
            'cyan': { 50: '#ecfeff', 100: '#cffafe', 500: '#06b6d4', 900: '#164e63' },
            'sky': { 50: '#f0f9ff', 100: '#e0f2fe', 500: '#0ea5e9', 900: '#0c4a6e' },
            'blue': { 50: '#eff6ff', 100: '#dbeafe', 500: '#3b82f6', 900: '#1e3a8a' },
            'indigo': { 50: '#eef2ff', 100: '#e0e7ff', 500: '#6366f1', 900: '#312e81' },
            'violet': { 50: '#f5f3ff', 100: '#ede9fe', 500: '#8b5cf6', 900: '#4c1d95' },
            'purple': { 50: '#faf5ff', 100: '#f3e8ff', 500: '#a855f7', 900: '#581c87' },
            'fuchsia': { 50: '#fdf4ff', 100: '#fae8ff', 500: '#d946ef', 900: '#701a75' },
            'pink': { 50: '#fdf2f8', 100: '#fce7f3', 500: '#ec4899', 900: '#831843' },
            'rose': { 50: '#fff1f2', 100: '#ffe4e6', 500: '#f43f5e', 900: '#881337' },
            // Nouvelles couleurs Tailwind v4
            'copper': { 500: '#c25d45' },
            'jungle': { 500: '#2c9f4b' },
            'midnight': { 500: '#1e293b' },
            'sunset': { 500: '#ff7e33' },
            'marine': { 500: '#105a8c' }
        };

        // Extraire la couleur et la nuance
        let colorBase = '';
        let shade = '500'; // Nuance par défaut

        if (parsed.colorValue) {
            const parts = parsed.colorValue.split('-');
            if (parts.length >= 2) {
                colorBase = parts[0];
                shade = parts[1];
            } else {
                colorBase = parsed.colorValue;
            }
        }

        // Déterminer la propriété CSS basée sur le préfixe
        let property = 'color';
        if (parsed.prefix.startsWith('bg')) {
            property = 'background-color';
        } else if (parsed.prefix.startsWith('border')) {
            property = 'border-color';
        } else if (parsed.prefix.startsWith('text')) {
            property = 'color';
        }

        // Récupérer la couleur du map ou utiliser la valeur hexadécimale directe
        const hexColor = colorMap[colorBase]?.[shade] || parsed.hexColor || '#38bdf8';

        return `${property}: ${hexColor};`;
    }

    _generateArbitraryCss(parsed) {
        if (parsed.arbitraryValue?.type === 'css-value') {
            return `.${parsed.base} { ${parsed.property.property || parsed.prefix}: ${parsed.arbitraryValue.value}; }`;
        }
        return `.${parsed.base} { /* arbitrary value: ${parsed.base} */ }`;
    }

    _parseArbitraryValue(className) {
        const match = className.match(/\[(.*?)\]/);
        if (!match) return null;

        const value = match[1];
        const prefix = className.split('[')[0];

        return {
            isArbitrary: true,
            value: value,
            // Récupère le préfixe correctement sans supprimer tous les tirets
            prefix: prefix.endsWith('-') ? prefix.slice(0, -1) : prefix,
            rawClassName: className,
            // Déterminer le type en fonction du préfixe
            type: this._determineType(prefix),
            // Identifier si c'est une couleur arbitraire
            isColor: value.startsWith('#') || value.startsWith('rgb') || value.includes('var(--color'),
            // Propriété CSS probable
            property: this._getPropertyFromPrefix(prefix)
        };
    }

    // Nouvelle méthode pour déduire la propriété CSS à partir du préfixe
    _getPropertyFromPrefix(prefix) {
        const propertyMap = {
            'w': 'width',
            'h': 'height',
            'min-w': 'min-width',
            'min-h': 'min-height',
            'max-w': 'max-width',
            'max-h': 'max-height',
            'p': 'padding',
            'px': 'padding-inline',
            'py': 'padding-block',
            'pt': 'padding-top',
            'pr': 'padding-right',
            'pb': 'padding-bottom',
            'pl': 'padding-left',
            'm': 'margin',
            'mx': 'margin-inline',
            'my': 'margin-block',
            'mt': 'margin-top',
            'mr': 'margin-right',
            'mb': 'margin-bottom',
            'ml': 'margin-left',
            'bg': 'background-color',
            'text': 'color',
            'border': 'border-color',
            'rounded': 'border-radius',
            'gap': 'gap',
            'gap-x': 'column-gap',
            'gap-y': 'row-gap',
            'opacity': 'opacity',
            'z': 'z-index',
            'top': 'top',
            'right': 'right',
            'bottom': 'bottom',
            'left': 'left',
            'inset': 'inset',
            'translate-x': 'transform: translateX',
            'translate-y': 'transform: translateY',
            'rotate': 'transform: rotate',
            'scale': 'transform: scale',
            'skew-x': 'transform: skewX',
            'skew-y': 'transform: skewY'
        };

        return propertyMap[prefix] || prefix;
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