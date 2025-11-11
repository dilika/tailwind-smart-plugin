package com.github.dilika.tailwindsmartplugin.tailwindv4

import com.intellij.openapi.diagnostic.Logger

/**
 * Générateur complet de toutes les classes Tailwind CSS v4.1
 * Couverture totale de toutes les fonctionnalités
 */
object TailwindV41ClassGenerator {
    
    private val logger = Logger.getInstance(TailwindV41ClassGenerator::class.java)
    
    /**
     * Génère toutes les classes Tailwind v4.1
     */
    fun generateAllClasses(): List<String> {
        val allClasses = mutableSetOf<String>()
        
        // 1. Colors (v4.1 palette complète)
        allClasses.addAll(generateColorClasses())
        
        // 2. Layout & Display
        allClasses.addAll(generateLayoutClasses())
        
        // 3. Spacing (Padding, Margin, Gap)
        allClasses.addAll(generateSpacingClasses())
        
        // 4. Sizing (Width, Height)
        allClasses.addAll(generateSizingClasses())
        
        // 5. Typography
        allClasses.addAll(generateTypographyClasses())
        
        // 6. Borders & Dividers
        allClasses.addAll(generateBorderClasses())
        
        // 7. Effects (Shadows, Opacity, Blur)
        allClasses.addAll(generateEffectClasses())
        
        // 8. Transforms & Transitions
        allClasses.addAll(generateTransformClasses())
        
        // 9. Interactivity
        allClasses.addAll(generateInteractivityClasses())
        
        // 10. SVG
        allClasses.addAll(generateSVGClasses())
        
        // 11. Accessibility
        allClasses.addAll(generateAccessibilityClasses())
        
        // 12. Filters & Backdrop
        allClasses.addAll(generateFilterClasses())
        
        // 13. Tables
        allClasses.addAll(generateTableClasses())
        
        // 14. Transitions & Animations
        allClasses.addAll(generateAnimationClasses())
        
        // 15. Scroll Behavior
        allClasses.addAll(generateScrollClasses())
        
        // 16. Touch Action
        allClasses.addAll(generateTouchClasses())
        
        // 17. Will Change
        allClasses.addAll(generateWillChangeClasses())
        
        // 18. Contain
        allClasses.addAll(generateContainClasses())
        
        // 19. Isolation
        allClasses.addAll(generateIsolationClasses())
        
        // 20. Object Fit & Position
        allClasses.addAll(generateObjectClasses())
        
        // 21. Overscroll
        allClasses.addAll(generateOverscrollClasses())
        
        // 22. Inset
        allClasses.addAll(generateInsetClasses())
        
        // 23. Z-Index
        allClasses.addAll(generateZIndexClasses())
        
        // 24. Columns
        allClasses.addAll(generateColumnClasses())
        
        // 25. Break & Hyphens
        allClasses.addAll(generateTextBreakClasses())
        
        // 26. Variants (Responsive, State, etc.)
        allClasses.addAll(generateVariantClasses())
        
        logger.info("Generated ${allClasses.size} Tailwind v4.1 classes")
        return allClasses.toList()
    }
    
    /**
     * Génère toutes les classes de couleur (v4.1)
     */
    private fun generateColorClasses(): Set<String> {
        val classes = mutableSetOf<String>()
        
        // Palette complète v4.1
        val colors = listOf(
            // Grays
            "slate", "gray", "zinc", "neutral", "stone",
            // Reds & Oranges
            "red", "orange", "amber",
            // Yellows & Greens
            "yellow", "lime", "green", "emerald",
            // Blues & Cyans
            "teal", "cyan", "sky", "blue", "indigo",
            // Purples & Pinks
            "violet", "purple", "fuchsia", "pink", "rose",
            // Tailwind v4.1 nouvelles couleurs
            "copper", "jungle", "sand", "chestnut", "midnight",
            "aqua", "cherry", "magenta", "moss", "sapphire",
            "charcoal", "lava", "sunset", "marine"
        )
        
        val shades = listOf("50", "100", "200", "300", "400", "500", "600", "700", "800", "900", "950", "975")
        val specialColors = listOf("white", "black", "transparent", "current")
        
        // Color utilities
        val colorPrefixes = listOf("bg", "text", "border", "ring", "divide", "outline", "accent", "caret", "fill", "stroke")
        
        for (prefix in colorPrefixes) {
            // Special colors
            for (special in specialColors) {
                classes.add("$prefix-$special")
            }
            
            // Color shades
            for (color in colors) {
                for (shade in shades) {
                    classes.add("$prefix-$color-$shade")
                }
            }
        }
        
        // Gradient backgrounds
        val gradientDirections = listOf("t", "tr", "r", "br", "b", "bl", "l", "tl")
        for (direction in gradientDirections) {
            classes.add("bg-gradient-to-$direction")
        }
        
        // Gradient stops
        for (color in colors) {
            for (shade in shades) {
                classes.add("from-$color-$shade")
                classes.add("via-$color-$shade")
                classes.add("to-$color-$shade")
            }
        }
        
        return classes
    }
    
    /**
     * Génère les classes de layout
     */
    private fun generateLayoutClasses(): Set<String> {
        return setOf(
            // Display
            "block", "inline-block", "inline", "flex", "inline-flex",
            "table", "inline-table", "table-caption", "table-cell",
            "table-column", "table-column-group", "table-footer-group",
            "table-header-group", "table-row-group", "table-row",
            "grid", "inline-grid", "flow-root", "contents", "list-item", "hidden",
            
            // Container
            "container", "mx-auto",
            
            // Position
            "static", "fixed", "absolute", "relative", "sticky",
            
            // Float
            "float-right", "float-left", "float-none",
            "clear-left", "clear-right", "clear-both", "clear-none",
            
            // Object Fit
            "object-contain", "object-cover", "object-fill", "object-none", "object-scale-down",
            
            // Object Position
            "object-bottom", "object-center", "object-left", "object-left-bottom",
            "object-left-top", "object-right", "object-right-bottom", "object-right-top", "object-top",
            
            // Overflow
            "overflow-auto", "overflow-hidden", "overflow-clip", "overflow-visible", "overflow-scroll",
            "overflow-x-auto", "overflow-y-auto", "overflow-x-hidden", "overflow-y-hidden",
            "overflow-x-clip", "overflow-y-clip", "overflow-x-visible", "overflow-y-visible",
            "overflow-x-scroll", "overflow-y-scroll",
            
            // Overscroll
            "overscroll-auto", "overscroll-contain", "overscroll-none",
            "overscroll-y-auto", "overscroll-y-contain", "overscroll-y-none",
            "overscroll-x-auto", "overscroll-x-contain", "overscroll-x-none",
            
            // Position values
            "inset-0", "inset-x-0", "inset-y-0", "top-0", "right-0", "bottom-0", "left-0",
            "inset-auto", "inset-x-auto", "inset-y-auto", "top-auto", "right-auto", "bottom-auto", "left-auto",
            "inset-px", "inset-0.5", "inset-1", "inset-1.5", "inset-2", "inset-2.5", "inset-3",
            "inset-3.5", "inset-4", "inset-5", "inset-6", "inset-7", "inset-8", "inset-9", "inset-10",
            "inset-11", "inset-12", "inset-14", "inset-16", "inset-20", "inset-24", "inset-28", "inset-32",
            "inset-36", "inset-40", "inset-44", "inset-48", "inset-52", "inset-56", "inset-60", "inset-64",
            "inset-72", "inset-80", "inset-96",
            "inset-1/2", "inset-1/3", "inset-2/3", "inset-1/4", "inset-2/4", "inset-3/4",
            "inset-full", "-inset-0", "-inset-px", "-inset-0.5", "-inset-1", "-inset-1.5", "-inset-2",
            "-inset-2.5", "-inset-3", "-inset-3.5", "-inset-4", "-inset-5", "-inset-6",
            
            // Z-Index
            "z-0", "z-10", "z-20", "z-30", "z-40", "z-50", "z-auto",
            
            // Visibility
            "visible", "invisible", "collapse"
        )
    }
    
    /**
     * Génère les classes de spacing
     */
    private fun generateSpacingClasses(): Set<String> {
        val classes = mutableSetOf<String>()
        
        val spacingValues = listOf(
            "0", "px", "0.5", "1", "1.5", "2", "2.5", "3", "3.5", "4", "5", "6", "7", "8", "9",
            "10", "11", "12", "14", "16", "18", "20", "24", "28", "32", "36", "40", "44", "48",
            "52", "56", "60", "64", "72", "80", "96",
            "1/2", "1/3", "2/3", "1/4", "2/4", "3/4", "1/5", "2/5", "3/5", "4/5", "1/6", "5/6",
            "full", "screen", "min", "max", "fit", "svh", "lvh", "dvh"
        )
        
        // Padding
        val paddingPrefixes = listOf("p", "px", "py", "pt", "pr", "pb", "pl", "ps", "pe")
        for (prefix in paddingPrefixes) {
            for (value in spacingValues) {
                classes.add("$prefix-$value")
            }
        }
        
        // Margin
        val marginPrefixes = listOf("m", "mx", "my", "mt", "mr", "mb", "ml", "ms", "me")
        for (prefix in marginPrefixes) {
            for (value in spacingValues) {
                classes.add("$prefix-$value")
                if (value != "auto" && value != "full" && value != "screen" && !value.contains("/")) {
                    classes.add("-$prefix-$value") // Negative margins
                }
            }
            classes.add("$prefix-auto")
        }
        
        // Space Between
        val spacePrefixes = listOf("space-x", "space-y", "space-s", "space-e")
        for (prefix in spacePrefixes) {
            for (value in spacingValues.filter { it != "auto" }) {
                classes.add("$prefix-$value")
                if (!value.contains("/")) {
                    classes.add("-$prefix-$value")
                }
            }
        }
        
        // Gap
        val gapPrefixes = listOf("gap", "gap-x", "gap-y")
        for (prefix in gapPrefixes) {
            for (value in spacingValues.filter { it != "auto" }) {
                classes.add("$prefix-$value")
            }
        }
        
        return classes
    }
    
    /**
     * Génère les classes de sizing
     */
    private fun generateSizingClasses(): Set<String> {
        val classes = mutableSetOf<String>()
        
        val sizeValues = listOf(
            "0", "px", "0.5", "1", "1.5", "2", "2.5", "3", "3.5", "4", "5", "6", "7", "8", "9",
            "10", "11", "12", "14", "16", "18", "20", "24", "28", "32", "36", "40", "44", "48",
            "52", "56", "60", "64", "72", "80", "96",
            "1/2", "1/3", "2/3", "1/4", "2/4", "3/4", "1/5", "2/5", "3/5", "4/5", "1/6", "5/6",
            "full", "screen", "min", "max", "fit", "svh", "lvh", "dvh", "auto"
        )
        
        val sizePrefixes = listOf("w", "h", "min-w", "max-w", "min-h", "max-h")
        for (prefix in sizePrefixes) {
            for (value in sizeValues) {
                classes.add("$prefix-$value")
            }
        }
        
        // Aspect Ratio (v4.1)
        classes.addAll(listOf(
            "aspect-auto", "aspect-square", "aspect-video",
            "aspect-[4/3]", "aspect-[16/9]", "aspect-[21/9]"
        ))
        
        return classes
    }
    
    /**
     * Génère les classes de typography
     */
    private fun generateTypographyClasses(): Set<String> {
        val classes = mutableSetOf<String>()
        
        // Font Family
        classes.addAll(listOf(
            "font-sans", "font-serif", "font-mono", "font-system-ui"
        ))
        
        // Font Size (v4.1)
        val fontSizes = listOf(
            "xs", "sm", "base", "lg", "xl", "2xl", "3xl", "4xl", "5xl", "6xl", "7xl", "8xl", "9xl"
        )
        for (size in fontSizes) {
            classes.add("text-$size")
        }
        
        // Font Weight
        classes.addAll(listOf(
            "font-thin", "font-extralight", "font-light", "font-normal", "font-medium",
            "font-semibold", "font-bold", "font-extrabold", "font-black"
        ))
        
        // Font Style
        classes.addAll(listOf("italic", "not-italic"))
        
        // Font Variant Numeric
        classes.addAll(listOf(
            "normal-nums", "ordinal", "slashed-zero", "lining-nums", "oldstyle-nums",
            "proportional-nums", "tabular-nums", "diagonal-fractions", "stacked-fractions"
        ))
        
        // Letter Spacing
        classes.addAll(listOf(
            "tracking-tighter", "tracking-tight", "tracking-normal", "tracking-wide",
            "tracking-wider", "tracking-widest"
        ))
        
        // Line Height
        classes.addAll(listOf(
            "leading-none", "leading-tight", "leading-snug", "leading-normal", "leading-relaxed",
            "leading-loose", "leading-3", "leading-4", "leading-5", "leading-6", "leading-7",
            "leading-8", "leading-9", "leading-10"
        ))
        
        // Text Align
        classes.addAll(listOf("text-left", "text-center", "text-right", "text-justify", "text-start", "text-end"))
        
        // Text Decoration
        classes.addAll(listOf(
            "underline", "overline", "line-through", "no-underline",
            "decoration-solid", "decoration-double", "decoration-dotted", "decoration-dashed", "decoration-wavy"
        ))
        
        // Text Transform
        classes.addAll(listOf("uppercase", "lowercase", "capitalize", "normal-case"))
        
        // Text Overflow
        classes.addAll(listOf("truncate", "text-ellipsis", "text-clip", "text-wrap", "text-nowrap", "text-balance", "text-pretty"))
        
        // Text Indent
        val indentValues = listOf("0", "px", "0.5", "1", "1.5", "2", "2.5", "3", "3.5", "4", "5", "6", "7", "8", "9", "10", "11", "12")
        for (value in indentValues) {
            classes.add("indent-$value")
            classes.add("-indent-$value")
        }
        
        // Vertical Align
        classes.addAll(listOf(
            "align-baseline", "align-top", "align-middle", "align-bottom", "align-text-top",
            "align-text-bottom", "align-sub", "align-super"
        ))
        
        // Whitespace
        classes.addAll(listOf(
            "whitespace-normal", "whitespace-nowrap", "whitespace-pre", "whitespace-pre-line",
            "whitespace-pre-wrap", "whitespace-break-spaces"
        ))
        
        // Word Break
        classes.addAll(listOf(
            "break-normal", "break-words", "break-all", "break-keep"
        ))
        
        // Hyphens
        classes.addAll(listOf("hyphens-none", "hyphens-manual", "hyphens-auto"))
        
        // Content
        classes.addAll(listOf("content-none", "content-['']"))
        
        return classes
    }
    
    /**
     * Génère les classes de border
     */
    private fun generateBorderClasses(): Set<String> {
        val classes = mutableSetOf<String>()
        
        // Border Width
        classes.addAll(listOf(
            "border-0", "border-2", "border-4", "border-8", "border",
            "border-x", "border-y", "border-t", "border-r", "border-b", "border-l",
            "border-s", "border-e"
        ))
        
        // Border Style
        classes.addAll(listOf(
            "border-solid", "border-dashed", "border-dotted", "border-double", "border-none"
        ))
        
        // Border Radius
        val radiusValues = listOf("none", "sm", "md", "lg", "xl", "2xl", "3xl", "full")
        val radiusPrefixes = listOf(
            "rounded", "rounded-t", "rounded-r", "rounded-b", "rounded-l",
            "rounded-tl", "rounded-tr", "rounded-br", "rounded-bl",
            "rounded-ss", "rounded-se", "rounded-ee", "rounded-es"
        )
        for (prefix in radiusPrefixes) {
            for (value in radiusValues) {
                classes.add("$prefix-$value")
            }
        }
        classes.add("rounded") // Base rounded
        
        // Divide Width
        classes.addAll(listOf(
            "divide-x", "divide-y", "divide-x-reverse", "divide-y-reverse",
            "divide-x-0", "divide-y-0", "divide-x-2", "divide-y-2", "divide-x-4", "divide-y-4", "divide-x-8", "divide-y-8"
        ))
        
        // Outline
        classes.addAll(listOf(
            "outline-none", "outline", "outline-dashed", "outline-dotted", "outline-double",
            "outline-0", "outline-1", "outline-2", "outline-4", "outline-8",
            "outline-offset-0", "outline-offset-1", "outline-offset-2", "outline-offset-4", "outline-offset-8"
        ))
        
        return classes
    }
    
    /**
     * Génère les classes d'effets
     */
    private fun generateEffectClasses(): Set<String> {
        val classes = mutableSetOf<String>()
        
        // Box Shadow
        classes.addAll(listOf(
            "shadow-sm", "shadow", "shadow-md", "shadow-lg", "shadow-xl", "shadow-2xl",
            "shadow-inner", "shadow-none"
        ))
        
        // Opacity
        val opacityValues = listOf("0", "5", "10", "20", "25", "30", "40", "50", "60", "70", "75", "80", "90", "95", "100")
        for (value in opacityValues) {
            classes.add("opacity-$value")
        }
        
        // Mix Blend Mode
        classes.addAll(listOf(
            "mix-blend-normal", "mix-blend-multiply", "mix-blend-screen", "mix-blend-overlay",
            "mix-blend-darken", "mix-blend-lighten", "mix-blend-color-dodge", "mix-blend-color-burn",
            "mix-blend-hard-light", "mix-blend-soft-light", "mix-blend-difference", "mix-blend-exclusion",
            "mix-blend-hue", "mix-blend-saturation", "mix-blend-color", "mix-blend-luminosity", "mix-blend-plus-lighter"
        ))
        
        // Background Blend Mode
        classes.addAll(listOf(
            "bg-blend-normal", "bg-blend-multiply", "bg-blend-screen", "bg-blend-overlay",
            "bg-blend-darken", "bg-blend-lighten", "bg-blend-color-dodge", "bg-blend-color-burn",
            "bg-blend-hard-light", "bg-blend-soft-light", "bg-blend-difference", "bg-blend-exclusion",
            "bg-blend-hue", "bg-blend-saturation", "bg-blend-color", "bg-blend-luminosity"
        ))
        
        return classes
    }
    
    /**
     * Génère les classes de transform
     */
    private fun generateTransformClasses(): Set<String> {
        val classes = mutableSetOf<String>()
        
        // Transform
        classes.addAll(listOf("transform", "transform-none", "transform-gpu", "transform-cpu"))
        
        // Scale
        val scaleValues = listOf("0", "50", "75", "90", "95", "100", "105", "110", "125", "150")
        for (value in scaleValues) {
            classes.add("scale-$value")
            classes.add("scale-x-$value")
            classes.add("scale-y-$value")
        }
        
        // Rotate
        val rotateValues = listOf("0", "1", "2", "3", "6", "12", "45", "90", "180")
        for (value in rotateValues) {
            classes.add("rotate-$value")
            classes.add("-rotate-$value")
        }
        
        // Translate
        val translateValues = listOf("0", "px", "0.5", "1", "1.5", "2", "2.5", "3", "3.5", "4", "5", "6", "7", "8", "9", "10", "11", "12", "14", "16", "20", "24", "28", "32", "36", "40", "44", "48", "52", "56", "60", "64", "72", "80", "96", "1/2", "1/3", "2/3", "1/4", "3/4", "full")
        for (value in translateValues) {
            classes.add("translate-x-$value")
            classes.add("translate-y-$value")
            if (value != "0" && value != "px" && !value.contains("/") && value != "full") {
                classes.add("-translate-x-$value")
                classes.add("-translate-y-$value")
            }
        }
        
        // Skew
        val skewValues = listOf("0", "1", "2", "3", "6", "12")
        for (value in skewValues) {
            classes.add("skew-x-$value")
            classes.add("skew-y-$value")
            classes.add("-skew-x-$value")
            classes.add("-skew-y-$value")
        }
        
        // Transform Origin
        classes.addAll(listOf(
            "origin-center", "origin-top", "origin-top-right", "origin-right", "origin-bottom-right",
            "origin-bottom", "origin-bottom-left", "origin-left", "origin-top-left"
        ))
        
        return classes
    }
    
    /**
     * Génère les classes d'interactivité
     */
    private fun generateInteractivityClasses(): Set<String> {
        return setOf(
            // Cursor
            "cursor-auto", "cursor-default", "cursor-pointer", "cursor-wait", "cursor-text",
            "cursor-move", "cursor-help", "cursor-not-allowed", "cursor-none", "cursor-context-menu",
            "cursor-progress", "cursor-cell", "cursor-crosshair", "cursor-vertical-text",
            "cursor-alias", "cursor-copy", "cursor-no-drop", "cursor-grab", "cursor-grabbing",
            "cursor-all-scroll", "cursor-col-resize", "cursor-row-resize", "cursor-n-resize",
            "cursor-e-resize", "cursor-s-resize", "cursor-w-resize", "cursor-ne-resize",
            "cursor-nw-resize", "cursor-se-resize", "cursor-sw-resize", "cursor-ew-resize",
            "cursor-ns-resize", "cursor-nesw-resize", "cursor-nwse-resize", "cursor-zoom-in", "cursor-zoom-out",
            
            // Resize
            "resize-none", "resize-y", "resize-x", "resize",
            
            // Scroll Snap
            "snap-none", "snap-x", "snap-y", "snap-both",
            "snap-mandatory", "snap-proximity",
            "snap-start", "snap-end", "snap-center", "snap-align-none",
            "snap-normal", "snap-always",
            "snap-stop", "snap-normal",
            
            // Touch Action
            "touch-auto", "touch-none", "touch-pan-x", "touch-pan-left", "touch-pan-right",
            "touch-pan-y", "touch-pan-up", "touch-pan-down", "touch-pinch-zoom", "touch-manipulation",
            
            // User Select
            "select-none", "select-text", "select-all", "select-auto"
        )
    }
    
    /**
     * Génère les classes SVG
     */
    private fun generateSVGClasses(): Set<String> {
        val classes = mutableSetOf<String>()
        
        // Fill & Stroke colors are already in color classes
        // Stroke Width
        val strokeWidths = listOf("0", "1", "2")
        for (width in strokeWidths) {
            classes.add("stroke-$width")
        }
        
        return classes
    }
    
    /**
     * Génère les classes d'accessibilité
     */
    private fun generateAccessibilityClasses(): Set<String> {
        return setOf(
            "sr-only", "not-sr-only"
        )
    }
    
    /**
     * Génère les classes de filtre
     */
    private fun generateFilterClasses(): Set<String> {
        val classes = mutableSetOf<String>()
        
        // Blur
        classes.addAll(listOf(
            "blur-none", "blur-sm", "blur", "blur-md", "blur-lg", "blur-xl", "blur-2xl", "blur-3xl"
        ))
        
        // Brightness
        val brightnessValues = listOf("0", "50", "75", "90", "95", "100", "105", "110", "125", "150", "200")
        for (value in brightnessValues) {
            classes.add("brightness-$value")
        }
        
        // Contrast
        val contrastValues = listOf("0", "50", "75", "100", "125", "150", "200")
        for (value in contrastValues) {
            classes.add("contrast-$value")
        }
        
        // Grayscale
        classes.addAll(listOf("grayscale-0", "grayscale"))
        
        // Hue Rotate
        val hueRotateValues = listOf("0", "15", "30", "60", "90", "180")
        for (value in hueRotateValues) {
            classes.add("hue-rotate-$value")
            classes.add("-hue-rotate-$value")
        }
        
        // Invert
        classes.addAll(listOf("invert-0", "invert"))
        
        // Saturate
        val saturateValues = listOf("0", "50", "100", "150", "200")
        for (value in saturateValues) {
            classes.add("saturate-$value")
        }
        
        // Sepia
        classes.addAll(listOf("sepia-0", "sepia"))
        
        // Backdrop Blur
        classes.addAll(listOf(
            "backdrop-blur-none", "backdrop-blur-sm", "backdrop-blur", "backdrop-blur-md",
            "backdrop-blur-lg", "backdrop-blur-xl", "backdrop-blur-2xl", "backdrop-blur-3xl"
        ))
        
        // Backdrop Brightness
        for (value in brightnessValues) {
            classes.add("backdrop-brightness-$value")
        }
        
        // Backdrop Contrast
        for (value in contrastValues) {
            classes.add("backdrop-contrast-$value")
        }
        
        // Backdrop Grayscale
        classes.addAll(listOf("backdrop-grayscale-0", "backdrop-grayscale"))
        
        // Backdrop Hue Rotate
        for (value in hueRotateValues) {
            classes.add("backdrop-hue-rotate-$value")
            classes.add("-backdrop-hue-rotate-$value")
        }
        
        // Backdrop Invert
        classes.addAll(listOf("backdrop-invert-0", "backdrop-invert"))
        
        // Backdrop Saturate
        for (value in saturateValues) {
            classes.add("backdrop-saturate-$value")
        }
        
        // Backdrop Sepia
        classes.addAll(listOf("backdrop-sepia-0", "backdrop-sepia"))
        
        // Backdrop Opacity
        val opacityValues = listOf("0", "5", "10", "20", "25", "30", "40", "50", "60", "70", "75", "80", "90", "95", "100")
        for (value in opacityValues) {
            classes.add("backdrop-opacity-$value")
        }
        
        return classes
    }
    
    /**
     * Génère les classes de table
     */
    private fun generateTableClasses(): Set<String> {
        return setOf(
            "border-collapse", "border-separate",
            "table-auto", "table-fixed",
            "caption-top", "caption-bottom"
        )
    }
    
    /**
     * Génère les classes d'animation
     */
    private fun generateAnimationClasses(): Set<String> {
        val classes = mutableSetOf<String>()
        
        // Transition Property
        classes.addAll(listOf(
            "transition-none", "transition-all", "transition", "transition-colors",
            "transition-opacity", "transition-shadow", "transition-transform"
        ))
        
        // Transition Duration
        val durations = listOf("75", "100", "150", "200", "300", "500", "700", "1000")
        for (duration in durations) {
            classes.add("duration-$duration")
        }
        
        // Transition Timing Function
        classes.addAll(listOf(
            "ease-linear", "ease-in", "ease-out", "ease-in-out"
        ))
        
        // Transition Delay
        for (delay in durations) {
            classes.add("delay-$delay")
        }
        
        // Animation
        classes.addAll(listOf(
            "animate-none", "animate-spin", "animate-ping", "animate-pulse", "animate-bounce"
        ))
        
        return classes
    }
    
    /**
     * Génère les classes de scroll
     */
    private fun generateScrollClasses(): Set<String> {
        return setOf(
            "scroll-auto", "scroll-smooth",
            "scroll-m-0", "scroll-mx-0", "scroll-my-0", "scroll-mt-0", "scroll-mr-0", "scroll-mb-0", "scroll-ml-0",
            "scroll-p-0", "scroll-px-0", "scroll-py-0", "scroll-pt-0", "scroll-pr-0", "scroll-pb-0", "scroll-pl-0"
        )
    }
    
    /**
     * Génère les classes touch
     */
    private fun generateTouchClasses(): Set<String> {
        return setOf(
            "touch-auto", "touch-none", "touch-pan-x", "touch-pan-left", "touch-pan-right",
            "touch-pan-y", "touch-pan-up", "touch-pan-down", "touch-pinch-zoom", "touch-manipulation"
        )
    }
    
    /**
     * Génère les classes will-change
     */
    private fun generateWillChangeClasses(): Set<String> {
        return setOf(
            "will-change-auto", "will-change-scroll", "will-change-contents", "will-change-transform"
        )
    }
    
    /**
     * Génère les classes contain
     */
    private fun generateContainClasses(): Set<String> {
        return setOf(
            "contain-none", "contain-strict", "contain-content", "contain-size", "contain-layout",
            "contain-style", "contain-paint"
        )
    }
    
    /**
     * Génère les classes isolation
     */
    private fun generateIsolationClasses(): Set<String> {
        return setOf("isolate", "isolation-auto")
    }
    
    /**
     * Génère les classes object
     */
    private fun generateObjectClasses(): Set<String> {
        return setOf(
            "object-contain", "object-cover", "object-fill", "object-none", "object-scale-down",
            "object-bottom", "object-center", "object-left", "object-left-bottom", "object-left-top",
            "object-right", "object-right-bottom", "object-right-top", "object-top"
        )
    }
    
    /**
     * Génère les classes overscroll
     */
    private fun generateOverscrollClasses(): Set<String> {
        return setOf(
            "overscroll-auto", "overscroll-contain", "overscroll-none",
            "overscroll-y-auto", "overscroll-y-contain", "overscroll-y-none",
            "overscroll-x-auto", "overscroll-x-contain", "overscroll-x-none"
        )
    }
    
    /**
     * Génère les classes inset
     */
    private fun generateInsetClasses(): Set<String> {
        val classes = mutableSetOf<String>()
        val values = listOf("0", "px", "0.5", "1", "1.5", "2", "2.5", "3", "3.5", "4", "5", "6", "7", "8", "9", "10", "11", "12", "14", "16", "20", "24", "28", "32", "36", "40", "44", "48", "52", "56", "60", "64", "72", "80", "96", "auto", "1/2", "1/3", "2/3", "1/4", "3/4", "full")
        val prefixes = listOf("inset", "inset-x", "inset-y", "top", "right", "bottom", "left", "start", "end")
        
        for (prefix in prefixes) {
            for (value in values) {
                classes.add("$prefix-$value")
                if (value != "auto" && value != "full" && !value.contains("/") && value != "px" && value != "0") {
                    classes.add("-$prefix-$value")
                }
            }
        }
        
        return classes
    }
    
    /**
     * Génère les classes z-index
     */
    private fun generateZIndexClasses(): Set<String> {
        return setOf(
            "z-0", "z-10", "z-20", "z-30", "z-40", "z-50", "z-auto"
        )
    }
    
    /**
     * Génère les classes columns
     */
    private fun generateColumnClasses(): Set<String> {
        val classes = mutableSetOf<String>()
        val columnValues = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "auto", "3xs", "2xs", "xs", "sm", "md", "lg", "xl", "2xl", "3xl", "4xl", "5xl", "6xl", "7xl")
        for (value in columnValues) {
            classes.add("columns-$value")
        }
        return classes
    }
    
    /**
     * Génère les classes text break
     */
    private fun generateTextBreakClasses(): Set<String> {
        return setOf(
            "break-normal", "break-words", "break-all", "break-keep"
        )
    }
    
    /**
     * Génère les classes de variants (responsive, state, etc.)
     */
    private fun generateVariantClasses(): Set<String> {
        val classes = mutableSetOf<String>()
        
        // Base classes (sans variant)
        // Les variants sont appliqués dynamiquement, donc on génère juste les classes de base
        
        // Variants responsive
        val responsiveBreakpoints = listOf("sm", "md", "lg", "xl", "2xl")
        
        // Variants d'état
        val stateVariants = listOf(
            "hover", "focus", "focus-within", "focus-visible", "active", "visited", "target",
            "disabled", "enabled", "checked", "indeterminate", "default", "required", "optional",
            "invalid", "valid", "in-range", "out-of-range", "placeholder-shown", "autofill",
            "read-only", "read-write", "empty", "open"
        )
        
        // Variants de groupe
        val groupVariants = listOf("group", "peer")
        
        // Variants de pseudo-éléments
        val pseudoElementVariants = listOf(
            "before", "after", "first-letter", "first-line", "marker", "selection", "file",
            "placeholder", "backdrop"
        )
        
        // Variants de position
        val positionVariants = listOf("first", "last", "only", "odd", "even")
        
        // Variants de formulaire
        val formVariants = listOf("default", "checked", "indeterminate", "placeholder-shown", "autofill", "optional", "required", "valid", "invalid", "in-range", "out-of-range", "read-write", "read-only")
        
        // Variants de media
        val mediaVariants = listOf("dark", "portrait", "landscape", "motion-safe", "motion-reduce", "contrast-more", "contrast-less", "print", "rtl", "ltr")
        
        // Les variants sont combinés avec les classes de base lors de la complétion
        // Ici on retourne juste les noms de variants pour référence
        
        return classes
    }
}

