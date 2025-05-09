package com.github.dilika.tailwindsmartplugin.util

/**
 * Utility for sorting Tailwind CSS classes into a logical order.
 * Based on recommended class ordering for improved readability and maintainability.
 */
object TailwindSortingUtils {
    
    // Define the priority groups for sorting (lower number = higher priority)
    private val classPriorityGroups = mapOf(
        // Layout & Positioning (highest priority)
        "layout" to 10,
        "position" to 20,
        "display" to 30,
        // Box Model
        "width" to 40,
        "height" to 50,
        "margin" to 60,
        "padding" to 70,
        "space" to 80,
        // Flexbox & Grid
        "flex" to 90,
        "grid" to 100,
        "gap" to 105,
        "place" to 110,
        "justify" to 120,
        "items" to 130,
        "content" to 140,
        // Backgrounds & Borders
        "bg" to 150,
        "border" to 160,
        "rounded" to 170,
        "shadow" to 180,
        "outline" to 190,
        "ring" to 195,
        // Typography
        "font" to 200,
        "text" to 210,
        "tracking" to 220,
        "leading" to 230,
        "list" to 240,
        // Visual Effects
        "opacity" to 250,
        "transition" to 260,
        "transform" to 270,
        "scale" to 280,
        "rotate" to 290,
        "translate" to 300,
        "animation" to 310,
        // Interactivity
        "pointer" to 320,
        "cursor" to 330,
        "select" to 340,
        "resize" to 350,
        "focus" to 360,
        "hover" to 370,
        "active" to 380,
        "disabled" to 390,
        // Responsive variants always come last
        "sm" to 400,
        "md" to 401,
        "lg" to 402,
        "xl" to 403,
        "2xl" to 404
    )
    
    // Class prefixes for each group for more accurate matching
    private val classPrefixes = mapOf(
        "layout" to listOf("container", "columns", "object"),
        "position" to listOf("static", "fixed", "absolute", "relative", "sticky", "top-", "bottom-", "left-", "right-", "inset-", "z-"),
        "display" to listOf("block", "inline", "inline-block", "hidden", "visible", "invisible"),
        "width" to listOf("w-", "min-w-", "max-w-"),
        "height" to listOf("h-", "min-h-", "max-h-", "aspect-"),
        "margin" to listOf("m-", "mt-", "mr-", "mb-", "ml-", "mx-", "my-"),
        "padding" to listOf("p-", "pt-", "pr-", "pb-", "pl-", "px-", "py-"),
        "space" to listOf("space-x-", "space-y-"),
        "flex" to listOf("flex", "flex-", "grow", "shrink", "basis", "order"),
        "grid" to listOf("grid", "grid-", "col-", "row-", "auto-rows", "auto-cols"),
        "gap" to listOf("gap-", "gap-x-", "gap-y-"),
        "place" to listOf("place-", "place-content-", "place-items-", "place-self-"),
        "justify" to listOf("justify-", "justify-items-", "justify-self-"),
        "items" to listOf("items-"),
        "content" to listOf("content-"),
        "bg" to listOf("bg-", "from-", "via-", "to-"),
        "border" to listOf("border", "border-"),
        "rounded" to listOf("rounded", "rounded-"),
        "shadow" to listOf("shadow", "shadow-"),
        "outline" to listOf("outline", "outline-"),
        "ring" to listOf("ring", "ring-"),
        "font" to listOf("font-"),
        "text" to listOf("text-", "indent-", "align-", "whitespace-", "break-", "hyphens-"),
        "tracking" to listOf("tracking-"),
        "leading" to listOf("leading-"),
        "list" to listOf("list-"),
        "opacity" to listOf("opacity-"),
        "transition" to listOf("transition", "duration-", "ease-", "delay-"),
        "transform" to listOf("transform", "transform-origin-"),
        "scale" to listOf("scale-"),
        "rotate" to listOf("rotate-"),
        "translate" to listOf("translate-"),
        "animation" to listOf("animate-"),
        "pointer" to listOf("pointer-events-"),
        "cursor" to listOf("cursor-"),
        "select" to listOf("select-"),
        "resize" to listOf("resize-"),
        "focus" to listOf("focus:"),
        "hover" to listOf("hover:"),
        "active" to listOf("active:"),
        "disabled" to listOf("disabled:"),
        "sm" to listOf("sm:"),
        "md" to listOf("md:"),
        "lg" to listOf("lg:"),
        "xl" to listOf("xl:"),
        "2xl" to listOf("2xl:")
    )
    
    /**
     * Sort Tailwind classes in a logical order to improve readability.
     * 
     * @param classString The space-separated string of Tailwind classes
     * @return Sorted space-separated string of Tailwind classes
     */
    fun sortClasses(classString: String): String {
        if (classString.isBlank()) return classString
        
        val classes = classString.trim().split(Regex("\\s+"))
        val sortedClasses = classes.sortedWith(compareBy { cls -> 
            // Determine the priority group for this class
            var priority = Int.MAX_VALUE
            
            for ((group, prefixes) in classPrefixes) {
                if (prefixes.any { cls.startsWith(it) }) {
                    priority = classPriorityGroups[group] ?: Int.MAX_VALUE
                    break
                }
            }
            
            // Handle special case for responsive variants with multiple levels (e.g., sm:hover:bg-blue-500)
            val parts = cls.split(":")
            if (parts.size > 1) {
                // Adjust priority for responsive variants
                for (part in parts) {
                    when {
                        part == "sm" -> priority = 400
                        part == "md" -> priority = 401
                        part == "lg" -> priority = 402
                        part == "xl" -> priority = 403
                        part == "2xl" -> priority = 404
                    }
                }
            }
            
            // Return the priority as the sort key
            priority
        })
        
        return sortedClasses.joinToString(" ")
    }
    
    /**
     * Group Tailwind classes by their category.
     * Useful for displaying classes in a more organized way.
     * 
     * @param classString The space-separated string of Tailwind classes
     * @return Map of category names to lists of classes in that category
     */
    fun groupClassesByCategory(classString: String): Map<String, List<String>> {
        if (classString.isBlank()) return emptyMap()
        
        val classes = classString.trim().split(Regex("\\s+"))
        val groupedClasses = mutableMapOf<String, MutableList<String>>()
        
        for (cls in classes) {
            var category = "other"
            
            for ((group, prefixes) in classPrefixes) {
                if (prefixes.any { cls.startsWith(it) }) {
                    category = group
                    break
                }
            }
            
            // Initialize list if not exists
            if (!groupedClasses.containsKey(category)) {
                groupedClasses[category] = mutableListOf()
            }
            
            // Add class to its category
            groupedClasses[category]?.add(cls)
        }
        
        return groupedClasses
    }
}
