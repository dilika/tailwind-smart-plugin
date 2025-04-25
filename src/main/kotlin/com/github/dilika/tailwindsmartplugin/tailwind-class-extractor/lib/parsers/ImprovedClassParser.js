// lib/parsers/ImprovedClassParser.js
// An enhanced Tailwind CSS class parser with VSCode-like features

/**
 * Configuration for the Tailwind CSS class types
 */
var TYPE_METADATA = {
    layout: {
        color: '#38bdf8', // blue-400
        icon: '⬒',
        category: 'Layout',
        keywords: ['flex', 'grid', 'container', 'display', 'position', 'float', 'clear', 'overflow'],
        examples: ['flex for flexible layout', 'grid for grid layout']
    },
    spacing: {
        color: '#8b5cf6', // violet-500 
        icon: '↔',
        category: 'Spacing',
        keywords: ['margin', 'padding', 'gap', 'space'],
        examples: ['mt-4 for top margin', 'px-2 for horizontal padding']
    },
    sizing: {
        color: '#ec4899', // pink-500
        icon: '⟷',
        category: 'Sizing',
        keywords: ['width', 'height', 'min', 'max'],
        examples: ['w-full for 100% width', 'h-10 for height 2.5rem']
    },
    typography: {
        color: '#f59e0b', // amber-500
        icon: 'T',
        category: 'Typography',
        keywords: ['font', 'text', 'leading', 'tracking', 'indent'],
        examples: ['text-lg for large text', 'font-bold for bold text']
    },
    color: {
        color: '#ef4444', // red-500
        icon: '◕',
        category: 'Color',
        keywords: ['text', 'bg', 'border', 'from', 'to', 'via', 'ring', 'divide', 'placeholder'],
        examples: ['text-red-500 for red text', 'bg-blue-200 for light blue background']
    },
    background: {
        color: '#10b981', // emerald-500
        icon: '◱',
        category: 'Background',
        keywords: ['bg', 'bg-opacity', 'bg-clip', 'bg-gradient'],
        examples: ['bg-cover for cover background', 'bg-center for centered background']
    },
    border: {
        color: '#6366f1', // indigo-500
        icon: '⬓',
        category: 'Border',
        keywords: ['border', 'rounded', 'outline', 'ring'],
        examples: ['border-2 for 2px border', 'rounded-md for medium rounding']
    },
    effect: {
        color: '#14b8a6', // teal-500
        icon: '✧',
        category: 'Effects',
        keywords: ['shadow', 'opacity', 'blur', 'filter', 'transition'],
        examples: ['shadow-lg for large shadow', 'opacity-50 for 50% opacity']
    },
    interaction: {
        color: '#d946ef', // fuchsia-500
        icon: '⟳',
        category: 'Interaction',
        keywords: ['cursor', 'resize', 'select', 'touch'],
        examples: ['cursor-pointer for pointer cursor', 'select-none to disable selection']
    },
    utility: {
        color: '#64748b', // slate-500
        icon: '●',
        category: 'Utility',
        keywords: [],
        examples: []
    }
};

/**
 * Mapping of Tailwind class prefixes to CSS properties
 */
var PROPERTY_MAP = {}; // Abbreviated for length - we'll dynamically build this from the TW classes

/**
 * Supported Tailwind variants with their styling
 */
var VARIANTS = {
    // Pseudo-class variants
    hover: { color: '#4f46e5', icon: 'H', description: 'When hovered' },
    focus: { color: '#7c3aed', icon: 'F', description: 'When focused' },
    active: { color: '#9333ea', icon: 'A', description: 'When active' },
    disabled: { color: '#9ca3af', icon: 'D', description: 'When disabled' },
    visited: { color: '#8b5cf6', icon: 'V', description: 'When visited' },
    checked: { color: '#10b981', icon: '✓', description: 'When checked' },
    
    // Responsive variants
    sm: { color: '#64748b', icon: 'S', description: 'Small screens (640px+)' },
    md: { color: '#64748b', icon: 'M', description: 'Medium screens (768px+)' },
    lg: { color: '#64748b', icon: 'L', description: 'Large screens (1024px+)' },
    xl: { color: '#64748b', icon: 'X', description: 'Extra large screens (1280px+)' },
    '2xl': { color: '#64748b', icon: '2X', description: 'Extra extra large screens (1536px+)' },
    
    // Color mode variants
    dark: { color: '#1e293b', icon: '🌙', description: 'Dark mode' },
    light: { color: '#f8fafc', icon: '☀', description: 'Light mode' },
    
    // State variants
    group: { color: '#6366f1', icon: 'G', description: 'Within group parent' },
    peer: { color: '#6366f1', icon: 'P', description: 'Peer-related' },
    
    // Print variant
    print: { color: '#334155', icon: '🖨', description: 'Print media' }
};

/**
 * Color palette based on Tailwind CSS default colors
 */
var COLOR_PALETTE = {
    slate: ['#f8fafc', '#f1f5f9', '#e2e8f0', '#cbd5e1', '#94a3b8', '#64748b', '#475569', '#334155', '#1e293b', '#0f172a'],
    gray: ['#f9fafb', '#f3f4f6', '#e5e7eb', '#d1d5db', '#9ca3af', '#6b7280', '#4b5563', '#374151', '#1f2937', '#111827'],
    zinc: ['#fafafa', '#f4f4f5', '#e4e4e7', '#d4d4d8', '#a1a1aa', '#71717a', '#52525b', '#3f3f46', '#27272a', '#18181b'],
    red: ['#fef2f2', '#fee2e2', '#fecaca', '#fca5a5', '#f87171', '#ef4444', '#dc2626', '#b91c1c', '#991b1b', '#7f1d1d'],
    blue: ['#eff6ff', '#dbeafe', '#bfdbfe', '#93c5fd', '#60a5fa', '#3b82f6', '#2563eb', '#1d4ed8', '#1e40af', '#1e3a8a']
    // Add more colors as needed
};

/**
 * Enhanced ClassParser for Tailwind CSS classes with VSCode-like functionality
 */
var ImprovedClassParser = function(config) {
    if (config === undefined) config = {};

    // Initialize configurations
    this._typeMetadata = {
        ...TYPE_METADATA,
        ...config.types
    };

    this._variants = {
        ...VARIANTS,
        ...config.variants
    };

    this._colorPalette = {
        ...COLOR_PALETTE,
        ...config.colors
    };

    // Import the complete class list from a Tailwind installation
    this._allTailwindClasses = new Set(config.classes || []);

    // Initialize the property map based on provided class list
    this._propertyMap = this._buildPropertyMap(config.properties);

    // Cache for parsed classes and results
    this._classCache = new Map();
    this._resultCache = new Map();
    this._usageStats = new Map();

    // Track recently used classes for better suggestions
    this._recentlyUsed = [];
    this._maxRecentClasses = config.maxRecentClasses || 50;
}

// Remplacer les méthodes de classe par des méthodes de prototype
ImprovedClassParser.prototype._buildPropertyMap = function(userProperties) {
    if (userProperties === undefined) userProperties = {};
    
    const map = {
        // Layout
        flex: { property: 'display', type: 'layout', keywords: ['flex', 'display'] },
        grid: { property: 'display', type: 'layout', keywords: ['grid', 'display'] },
        hidden: { property: 'display', type: 'layout', keywords: ['hidden', 'display'] },
        block: { property: 'display', type: 'layout', keywords: ['block', 'display'] },
        inline: { property: 'display', type: 'layout', keywords: ['inline', 'display'] },
        
        // Spacing
        m: { property: 'margin', type: 'spacing', axis: 'xy', keywords: ['margin'] },
        mt: { property: 'margin-top', type: 'spacing', axis: 'y', keywords: ['margin', 'top'] },
        mb: { property: 'margin-bottom', type: 'spacing', axis: 'y', keywords: ['margin', 'bottom'] },
        ml: { property: 'margin-left', type: 'spacing', axis: 'x', keywords: ['margin', 'left'] },
        mr: { property: 'margin-right', type: 'spacing', axis: 'x', keywords: ['margin', 'right'] },
        mx: { property: ['margin-left', 'margin-right'], type: 'spacing', axis: 'x', keywords: ['margin', 'horizontal'] },
        my: { property: ['margin-top', 'margin-bottom'], type: 'spacing', axis: 'y', keywords: ['margin', 'vertical'] },
        p: { property: 'padding', type: 'spacing', axis: 'xy', keywords: ['padding'] },
        pt: { property: 'padding-top', type: 'spacing', axis: 'y', keywords: ['padding', 'top'] },
        pb: { property: 'padding-bottom', type: 'spacing', axis: 'y', keywords: ['padding', 'bottom'] },
        pl: { property: 'padding-left', type: 'spacing', axis: 'x', keywords: ['padding', 'left'] },
        pr: { property: 'padding-right', type: 'spacing', axis: 'x', keywords: ['padding', 'right'] },
        px: { property: ['padding-left', 'padding-right'], type: 'spacing', axis: 'x', keywords: ['padding', 'horizontal'] },
        py: { property: ['padding-top', 'padding-bottom'], type: 'spacing', axis: 'y', keywords: ['padding', 'vertical'] },
        
        // Sizing
        w: { property: 'width', type: 'sizing', keywords: ['width'] },
        h: { property: 'height', type: 'sizing', keywords: ['height'] },
        min: { property: 'min-width', type: 'sizing', keywords: ['min', 'width'] },
        max: { property: 'max-width', type: 'sizing', keywords: ['max', 'width'] },
        
        // Typography
        text: { property: 'font-size', type: 'typography', keywords: ['text', 'font', 'size'] },
        font: { property: 'font-weight', type: 'typography', keywords: ['font', 'weight'] },
        leading: { property: 'line-height', type: 'typography', keywords: ['leading', 'line', 'height'] },
        tracking: { property: 'letter-spacing', type: 'typography', keywords: ['tracking', 'letter', 'spacing'] },
        
        // Colors (text/background/border)
        bg: { property: 'background-color', type: 'background', keywords: ['background', 'color'] },
        text: { property: 'color', type: 'color', keywords: ['text', 'color'] },
        border: { property: 'border-color', type: 'border', keywords: ['border', 'color'] },
        
        // Border
        rounded: { property: 'border-radius', type: 'border', keywords: ['rounded', 'border', 'radius'] },
        
        // Effects
        shadow: { property: 'box-shadow', type: 'effect', keywords: ['shadow', 'box-shadow'] },
        opacity: { property: 'opacity', type: 'effect', keywords: ['opacity'] },
        
        // Additional properties can be added here
        ...userProperties
    };
    
    return map;
}

/**
 * Parse Tailwind CSS classes with enhanced features
 * @param {string} css - CSS content for extracting rules
 * @param {string[]} classes - Array of class names to parse
 * @returns {Object} - Parsed class information with enhanced metadata
 */
ImprovedClassParser.prototype.parse = function(css, classes) {
    const result = {};
    
    // Update usage stats and recently used classes
    classes.forEach(cls => {
        this._usageStats.set(cls, (this._usageStats.get(cls) || 0) + 1);
        this._addToRecentlyUsed(cls);
    });
    
    for (const className of classes) {
        if (this._resultCache.has(className)) {
            result[className] = this._resultCache.get(className);
            continue;
        }
        
        const parsed = this._parseClass(className);
        const formatted = this._formatResult(parsed);
        
        this._resultCache.set(className, formatted);
        result[className] = formatted;
    }
    
    return result;
}

/**
 * Parse a single Tailwind CSS class
 * @param {string} className - The class name to parse
 * @returns {Object} - Parsed class information
 */
ImprovedClassParser.prototype._parseClass = function(className) {
    if (this._classCache.has(className)) {
        return this._classCache.get(className);
    }
    
    // Check for variant prefixes (e.g., hover:, focus:, dark:)
    const variantMatch = className.match(/^([a-z0-9-]+):(.+)$/);
    const baseClass = variantMatch ? variantMatch[2] : className;
    const variant = variantMatch ? variantMatch[1] : null;
    
    // Check for arbitrary values (e.g., [w-10px])
    const isArbitrary = /^\[.+\]$/.test(baseClass);
    
    // Split class into prefix and value
    const [prefix, ...valueParts] = this._splitClass(baseClass);
    const value = valueParts.join('-');
    
    // Determine class type and properties
    const propertyInfo = this._getPropertyInfo(prefix);
    
    const parsed = {
        original: className,
        base: baseClass,
        prefix,
        value,
        variant,
        isArbitrary,
        isExactMatch: this._allTailwindClasses.has(className),
        type: propertyInfo.type || 'utility',
        properties: Array.isArray(propertyInfo.property) 
            ? propertyInfo.property 
            : [propertyInfo.property],
        keywords: [...(propertyInfo.keywords || [])],
        usage: this._usageStats.get(className) || 0
    };
    
    this._classCache.set(className, parsed);
    return parsed;
}

/**
 * Split class name into prefix and value parts
 * @param {string} className - The class name to split
 * @returns {string[]} - Array containing prefix and value parts
 */
ImprovedClassParser.prototype._splitClass = function(className) {
    // Handle arbitrary values
    if (/^\[.+\]$/.test(className)) {
        return [className, null];
    }
    
    // Handle utility classes without values
    if (!className.includes('-')) {
        return [className, null];
    }
    
    // Split on first dash for regular classes
    const parts = className.split('-');
    return [parts[0], ...parts.slice(1)];
}

/**
 * Get property information for a class prefix
 * @param {string} prefix - The class prefix
 * @returns {Object} - Property information
 */
ImprovedClassParser.prototype._getPropertyInfo = function(prefix) {
    if (this._propertyMap[prefix]) {
        return this._propertyMap[prefix];
    }
    
    // For unknown prefixes, try to determine the type
    for (const [type, metadata] of Object.entries(this._typeMetadata)) {
        if (metadata.keywords.some(keyword => prefix.includes(keyword))) {
            return { 
                property: prefix, 
                type, 
                keywords: [prefix, ...metadata.keywords]
            };
        }
    }
    
    return { 
        property: prefix, 
        type: 'utility', 
        keywords: [prefix] 
    };
}

/**
 * Format parsed class information for display and completion
 * @param {Object} parsed - Parsed class information
 * @returns {Object} - Formatted result
 */
ImprovedClassParser.prototype._formatResult = function(parsed) {
    const typeMetadata = this._typeMetadata[parsed.type];
    const variantMetadata = parsed.variant ? this._variants[parsed.variant] : null;
    
    return {
        completion: {
            text: parsed.original,
            displayText: this._getDisplayText(parsed, typeMetadata, variantMetadata),
            style: this._getCompletionStyle(parsed, typeMetadata, variantMetadata),
            relevance: this._calculateRelevance(parsed),
            type: parsed.type,
            variant: parsed.variant
        },
        documentation: {
            description: this._getDescription(parsed),
            cssProperties: parsed.properties,
            cssValue: this._getCssValue(parsed),
            examples: this._getExamples(parsed, typeMetadata)
        },
        keywords: [...new Set([...parsed.keywords, parsed.original, parsed.base, parsed.prefix])],
        meta: {
            isExactMatch: parsed.isExactMatch,
            isArbitrary: parsed.isArbitrary,
            usage: parsed.usage
        }
    };
}

/**
 * Get display text for completion
 * @param {Object} parsed - Parsed class information
 * @param {Object} typeMetadata - Type metadata
 * @param {Object} variantMetadata - Variant metadata
 * @returns {string} - Display text
 */
ImprovedClassParser.prototype._getDisplayText = function(parsed, typeMetadata, variantMetadata) {
    const typeIcon = typeMetadata?.icon || '●';
    const variantIcon = variantMetadata?.icon || '';
    
    return `${typeIcon}${variantIcon ? ' ' + variantIcon : ''} ${parsed.original}`;
}

/**
 * Get completion style
 * @param {Object} parsed - Parsed class information
 * @param {Object} typeMetadata - Type metadata
 * @param {Object} variantMetadata - Variant metadata
 * @returns {string} - CSS style
 */
ImprovedClassParser.prototype._getCompletionStyle = function(parsed, typeMetadata, variantMetadata) {
    const color = variantMetadata?.color || typeMetadata?.color || '#64748b';
    return `color: ${color}; ${parsed.isExactMatch ? 'font-weight: bold;' : ''}`;
}

/**
 * Calculate relevance score for sorting completions
 * @param {Object} parsed - Parsed class information
 * @returns {number} - Relevance score
 */
ImprovedClassParser.prototype._calculateRelevance = function(parsed) {
    let score = 0;
    
    // Exact matches get highest priority
    if (parsed.isExactMatch) score += 1000;
    
    // Recently used classes get boosted
    const recentIndex = this._recentlyUsed.indexOf(parsed.original);
    if (recentIndex !== -1) {
        score += 500 - Math.min(499, recentIndex * 10);
    }
    
    // Usage frequency boosts score
    score += Math.min(100, parsed.usage * 5);
    
    return score;
}

/**
 * Get human-readable description for the class
 * @param {Object} parsed - Parsed class information
 * @returns {string} - Description
 */
ImprovedClassParser.prototype._getDescription = function(parsed) {
    const typeMetadata = this._typeMetadata[parsed.type];
    const variantMetadata = parsed.variant ? this._variants[parsed.variant] : null;
    
    let description = `${typeMetadata?.category || 'Utility'} class`;
    
    if (parsed.properties && parsed.properties.length > 0) {
        const propertyList = parsed.properties.join(', ');
        description += ` for ${propertyList}`;
    }
    
    if (variantMetadata) {
        description += ` (${variantMetadata.description})`;
    }
    
    return description;
}

/**
 * Get CSS value for the class
 * @param {Object} parsed - Parsed class information
 * @returns {string} - CSS value
 */
ImprovedClassParser.prototype._getCssValue = function(parsed) {
    if (parsed.isArbitrary) {
        const match = parsed.original.match(/\[([^\]]+)\]/);
        return match ? match[1] : '';
    }
    
    if (!parsed.value) return '';
    
    // Handle color values
    if (parsed.type === 'color' || parsed.type === 'background' || parsed.type === 'border') {
        const colorMatch = parsed.value.match(/([a-z]+)-(\d+)/);
        if (colorMatch && this._colorPalette[colorMatch[1]]) {
            const colorName = colorMatch[1];
            const colorIndex = parseInt(colorMatch[2], 10) / 100 - 1;
            if (this._colorPalette[colorName][colorIndex]) {
                return this._colorPalette[colorName][colorIndex];
            }
        }
    }
    
    // Handle sizing values
    if (parsed.type === 'sizing' || parsed.type === 'spacing') {
        const sizeMap = {
            '0': '0px',
            'px': '1px',
            '0.5': '0.125rem',
            '1': '0.25rem',
            '2': '0.5rem',
            '3': '0.75rem',
            '4': '1rem',
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
            'full': '100%'
        };
        
        return sizeMap[parsed.value] || parsed.value;
    }
    
    return parsed.value;
}

/**
 * Get examples for the class
 * @param {Object} parsed - Parsed class information
 * @param {Object} typeMetadata - Type metadata
 * @returns {string[]} - Array of examples
 */
ImprovedClassParser.prototype._getExamples = function(parsed, typeMetadata) {
    const examples = [];
    
    if (typeMetadata && typeMetadata.examples) {
        examples.push(...typeMetadata.examples);
    }
    
    return examples;
}

/**
 * Add class to recently used list
 * @param {string} className - Class name
 */
ImprovedClassParser.prototype._addToRecentlyUsed = function(className) {
    // Remove if already in the list
    const index = this._recentlyUsed.indexOf(className);
    if (index > -1) {
        this._recentlyUsed.splice(index, 1);
    }
    
    // Add to the beginning
    this._recentlyUsed.unshift(className);
    
    // Trim if exceeds max size
    if (this._recentlyUsed.length > this._maxRecentClasses) {
        this._recentlyUsed.pop();
    }
}

/**
 * Check if a class is valid
 * @param {string} className - Class name to check
 * @returns {boolean} - True if valid
 */
ImprovedClassParser.prototype.isValidClass = function(className) {
    // Exact match in known classes
    if (this._allTailwindClasses.has(className)) {
        return true;
    }
    
    // Check for variants
    const variantMatch = className.match(/^([a-z0-9-]+):(.+)$/);
    if (variantMatch) {
        const variant = variantMatch[1];
        const baseClass = variantMatch[2];
        
        if (!this._variants[variant]) {
            return false;
        }
        
        // Check if the base class is valid
        return this.isValidClass(baseClass);
    }
    
    // Check for arbitrary values
    if (/^\[.+\]$/.test(className)) {
        return true; // Accept all arbitrary values
    }
    
    // For other classes, check if the prefix is known
    const [prefix] = this._splitClass(className);
    return !!this._propertyMap[prefix];
}

/**
 * Find similar Tailwind classes based on partial input
 * @param {string} partialClass - Partial class name to match against
 * @param {number} limit - Maximum number of results to return
 * @returns {Object[]} - Array of completion items
 */
ImprovedClassParser.prototype.findSimilarClasses = function(partialClass, limit) {
    if (limit === undefined) limit = 10;
    
    const results = [];
    let count = 0;
    
    // Check exact match first
    if (this._allTailwindClasses.has(partialClass)) {
        const parsed = this._parseClass(partialClass);
        results.push(this._formatResult(parsed));
        count++;
    }
    
    // Add recently used classes that match first
    for (const className of this._recentlyUsed) {
        if (count >= limit) break;
        if (className !== partialClass && className.includes(partialClass)) {
            const parsed = this._parseClass(className);
            results.push(this._formatResult(parsed));
            count++;
        }
    }
    
    // Then check other matching classes
    for (const className of this._allTailwindClasses) {
        if (count >= limit) break;
        if (!results.some(r => r.completion.text === className) && className.includes(partialClass)) {
            const parsed = this._parseClass(className);
            results.push(this._formatResult(parsed));
            count++;
        }
    }
    
    // Sort by relevance
    return results.sort((a, b) => b.completion.relevance - a.completion.relevance);
}

/**
 * Get classes for a specific prefix
 * @param {string} prefix - The prefix to match
 * @param {number} limit - Maximum number of results to return
 * @returns {string[]} - Array of valid classes
 */
ImprovedClassParser.prototype.getClassesForPrefix = function(prefix, limit) {
    if (limit === undefined) limit = 100;
    
    const result = [];
    
    for (const className of this._allTailwindClasses) {
        if (result.length >= limit) break;
        
        if (className.startsWith(prefix)) {
            result.push(className);
        }
    }
    
    return result;
}

/**
 * Clear all caches
 */
ImprovedClassParser.prototype.clearCache = function() {
    this._classCache.clear();
    this._resultCache.clear();
}

/**
 * Export the parser for use in different environments
 */
ImprovedClassParser.create = function(options) {
    if (options === undefined) options = {};
    return new ImprovedClassParser(options);
}

// Make createTailwindParser globally available for GraalVM/Polyglot
function createTailwindParser(config) {
    // The ImprovedClassParser constructor expects a config object
    // Return an instance or a wrapper with a parse method
    return new ImprovedClassParser(config);
}

// Attach both ImprovedClassParser and createTailwindParser to globalThis for GraalVM/Polyglot compatibility
if (typeof globalThis !== 'undefined') {
    globalThis.ImprovedClassParser = ImprovedClassParser;
    globalThis.createTailwindParser = createTailwindParser;
}

// Exports pour différents environnements (CommonJS, ES Modules, et global)
if (typeof module !== 'undefined' && module.exports) {
    // CommonJS / Node.js
    module.exports = ImprovedClassParser;
    module.exports.TYPE_METADATA = TYPE_METADATA;
    module.exports.VARIANTS = VARIANTS;
    module.exports.COLOR_PALETTE = COLOR_PALETTE;
    module.exports.createTailwindParser = createTailwindParser;
} else if (typeof define === 'function' && define.amd) {
    // AMD
    define(function() { return ImprovedClassParser; });
}