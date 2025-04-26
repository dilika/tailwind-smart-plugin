// lib/parsers/ClassParser.js
const TYPE_METADATA = {
    spacing: {
        color: '#38bdf8',
        icon: '⇨',
        keywords: ['m', 'p', 'gap', 'space', 'margin', 'padding']
    },
    color: {
        color: '#f472b6',
        icon: '■',
        keywords: ['text', 'bg', 'border', 'fill', 'stroke']
    },
    typography: {
        color: '#a3e635',
        icon: 'T',
        keywords: ['font', 'text', 'leading', 'tracking']
    }
};

const PROPERTY_MAP = {
    // Spacing
    m: { property: 'margin', type: 'spacing' },
    mt: { property: 'margin-top', type: 'spacing' },
    mb: { property: 'margin-bottom', type: 'spacing' },
    ml: { property: 'margin-left', type: 'spacing' },
    mr: { property: 'margin-right', type: 'spacing' },
    mx: { property: ['margin-left', 'margin-right'], type: 'spacing' },
    my: { property: ['margin-top', 'margin-bottom'], type: 'spacing' },
    p: { property: 'padding', type: 'spacing' },
    pt: { property: 'padding-top', type: 'spacing' },
    pb: { property: 'padding-bottom', type: 'spacing' },
    pl: { property: 'padding-left', type: 'spacing' },
    pr: { property: 'padding-right', type: 'spacing' },
    px: { property: ['padding-left', 'padding-right'], type: 'spacing' },
    py: { property: ['padding-top', 'padding-bottom'], type: 'spacing' },

    // Color
    text: { property: 'color', type: 'color' },
    bg: { property: 'background-color', type: 'color' },
    border: { property: 'border-color', type: 'color' },
    fill: { property: 'fill', type: 'color' },
    stroke: { property: 'stroke', type: 'color' }
};

export class FastClassParser {
    constructor() {
        this.cache = new Map();
    }

    parseClass(className) {
        if (this.cache.has(className)) {
            return this.cache.get(className);
        }

        const [variant, baseClass] = this._extractVariant(className);
        const [prefix, value] = this._splitClassParts(baseClass);
        const typeMeta = this._getTypeMetadata(prefix);
        const css = this._generateCss(className, prefix, value, variant);

        const result = {
            displayText: `${typeMeta.icon} ${className}`,
            style: `color: ${typeMeta.color}`,
            css,
            variant
        };

        this.cache.set(className, result);
        return result;
    }

    _extractVariant(className) {
        const match = className.match(/^(hover|focus|active|dark):(.+)$/);
        return match ? [match[1], match[2]] : [null, className];
    }

    _splitClassParts(className) {
        const dashIndex = className.indexOf('-');
        return dashIndex > 0
            ? [className.slice(0, dashIndex), className.slice(dashIndex + 1)]
            : [className, null];
    }

    _getTypeMetadata(prefix) {
        for (const [type, meta] of Object.entries(TYPE_METADATA)) {
            if (meta.keywords.includes(prefix)) return meta;
        }
        return { color: '#94a3b8', icon: '•' };
    }

    _generateCss(className, prefix, value, variant) {
        const property = PROPERTY_MAP[prefix]?.property || prefix;
        const properties = Array.isArray(property) ? property : [property];
        const cssValue = this._getCssValue(prefix, value);

        let css = properties.map(p => `${p}: ${cssValue};`).join(' ');
        if (variant) css = `&:${variant} { ${css} }`;

        return `.${className} { ${css} }`;
    }

    _getCssValue(prefix, value) {
        if (PROPERTY_MAP[prefix]?.type === 'spacing') {
            return this._getSpacingValue(value);
        }
        return value ? (value.startsWith('#') ? value : `var(--${value})`) : '';
    }

    _getSpacingValue(value) {
        const spacing = {
            '0': '0', '0.5': '0.125rem', '1': '0.25rem',
            '1.5': '0.375rem', '2': '0.5rem', '2.5': '0.625rem',
            '3': '0.75rem', '3.5': '0.875rem', '4': '1rem'
        };
        return spacing[value] || value;
    }
}