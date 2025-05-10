package com.github.dilika.tailwindsmartplugin.util

import com.intellij.ui.JBColor

/**
 * A collection of common UI patterns as reusable Tailwind CSS class combinations.
 * Each entry consists of a lookup key (short mnemonic), the full class string to insert,
 * and a human-readable description that will be shown in the completion popup.
 */
object SmartClassGroupUtils {
    data class Group(val key: String, val classes: String, val description: String, val color: JBColor)

    val GROUPS: List<Group> = listOf(
        Group(
            key = "btn-primary",
            classes = "inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500",
            description = "Primary Button",
            color = JBColor(0x3B82F6, 0x3B82F6)
        ),
        Group(
            key = "btn-secondary",
            classes = "inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md shadow-sm text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500",
            description = "Secondary Button",
            color = JBColor(0x6B7280, 0x6B7280)
        ),
        Group(
            key = "alert-success",
            classes = "flex p-4 mb-4 text-sm text-green-800 rounded-lg bg-green-50 dark:bg-gray-800 dark:text-green-400",
            description = "Success Alert",
            color = JBColor(0x22C55E, 0x22C55E)
        ),
        Group(
            key = "card",
            classes = "bg-white shadow-md rounded-lg p-6 dark:bg-gray-800",
            description = "Card Container",
            color = JBColor(0xFFFFFF, 0x1F2937)
        ),
        Group(
            key = "avatar",
            classes = "inline-block h-10 w-10 rounded-full ring-2 ring-white",
            description = "Avatar",
            color = JBColor(0xFFFFFF, 0xFFFFFF)
        ),
        // New grid layout patterns
        Group(
            key = "grid-2-cols",
            classes = "grid grid-cols-1 sm:grid-cols-2 gap-4",
            description = "2-Column Responsive Grid",
            color = JBColor(0x4F46E5, 0x4F46E5)
        ),
        Group(
            key = "grid-3-cols",
            classes = "grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4",
            description = "3-Column Responsive Grid",
            color = JBColor(0x4F46E5, 0x4F46E5)
        ),
        Group(
            key = "grid-4-cols",
            classes = "grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4",
            description = "4-Column Responsive Grid",
            color = JBColor(0x4F46E5, 0x4F46E5)
        ),
        Group(
            key = "grid-auto",
            classes = "grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4",
            description = "Auto-responsive Grid",
            color = JBColor(0x4F46E5, 0x4F46E5)
        ),
        Group(
            key = "grid-layout",
            classes = "grid grid-cols-12 gap-4",
            description = "12-Column Grid Layout",
            color = JBColor(0x4F46E5, 0x4F46E5)
        ),
        Group(
            key = "flex-between",
            classes = "flex justify-between items-center",
            description = "Flex with Space Between",
            color = JBColor(0xF59E0B, 0xF59E0B)
        ),
        Group(
            key = "flex-center",
            classes = "flex justify-center items-center",
            description = "Centered Flex Container",
            color = JBColor(0xF59E0B, 0xF59E0B)
        ),
        // Specialized grid patterns
        Group(
            key = "grid-gaps",
            classes = "grid gap-4 sm:gap-6 md:gap-8 lg:gap-10",
            description = "Responsive Grid Gaps",
            color = JBColor(0x4F46E5, 0x4F46E5)
        ),
        Group(
            key = "grid-responsive",
            classes = "grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4",
            description = "Fully Responsive Grid",
            color = JBColor(0x4F46E5, 0x4F46E5)
        ),
        Group(
            key = "grid-gallery",
            classes = "grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-2 sm:gap-4",
            description = "Responsive Image Gallery Grid",
            color = JBColor(0x4F46E5, 0x4F46E5)
        ),
        Group(
            key = "grid-sidebar",
            classes = "grid grid-cols-1 lg:grid-cols-[300px_1fr] gap-6",
            description = "Sidebar Layout with Main Content",
            color = JBColor(0x4F46E5, 0x4F46E5)
        ),
        Group(
            key = "grid-dashboard",
            classes = "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 auto-rows-fr",
            description = "Dashboard Grid Layout",
            color = JBColor(0x4F46E5, 0x4F46E5)
        ),
        Group(
            key = "grid-masonry",
            classes = "grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 auto-rows-auto",
            description = "Masonry-style Grid Layout",
            color = JBColor(0x4F46E5, 0x4F46E5)
        ),
        Group(
            key = "grid-featured",
            classes = "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 [&>*:first-child]:md:col-span-2 [&>*:first-child]:lg:row-span-2",
            description = "Featured Item Grid Layout",
            color = JBColor(0x4F46E5, 0x4F46E5)
        ),
        Group(
            key = "grid-cols-12",
            classes = "grid grid-cols-12 gap-4",
            description = "12-Column Grid System",
            color = JBColor(0x4F46E5, 0x4F46E5)
        ),
        Group(
            key = "grid-auto-fit",
            classes = "grid grid-cols-[repeat(auto-fit,minmax(250px,1fr))] gap-4",
            description = "Auto-fit Grid with Minimum Width",
            color = JBColor(0x4F46E5, 0x4F46E5)
        ),
        // Flex layouts
        Group(
            key = "flex-row",
            classes = "flex flex-row items-center gap-4",
            description = "Horizontal Flex Row",
            color = JBColor(0xF59E0B, 0xF59E0B)
        ),
        Group(
            key = "flex-col",
            classes = "flex flex-col space-y-4",
            description = "Vertical Flex Column",
            color = JBColor(0xF59E0B, 0xF59E0B)
        ),
        Group(
            key = "flex-wrap",
            classes = "flex flex-wrap gap-2",
            description = "Wrapping Flex Container",
            color = JBColor(0xF59E0B, 0xF59E0B)
        ),
        Group(
            key = "flex-space-between",
            classes = "flex items-center justify-between w-full",
            description = "Space Between Flex Layout",
            color = JBColor(0xF59E0B, 0xF59E0B)
        ),
        Group(
            key = "flex-card",
            classes = "flex flex-col gap-2 rounded-lg bg-white p-6 shadow-md dark:bg-gray-800",
            description = "Flex Card Component",
            color = JBColor(0xF59E0B, 0xF59E0B)
        ),
        Group(
            key = "flex-navbar",
            classes = "flex items-center justify-between px-4 py-3 bg-white shadow-md dark:bg-gray-800",
            description = "Navbar Flex Layout",
            color = JBColor(0xF59E0B, 0xF59E0B)
        ),
        Group(
            key = "flex-list-item",
            classes = "flex items-center gap-3 px-4 py-3 hover:bg-gray-50 dark:hover:bg-gray-700",
            description = "List Item with Flex",
            color = JBColor(0xF59E0B, 0xF59E0B)
        ),
        Group(
            key = "flex-responsive",
            classes = "flex flex-col sm:flex-row items-start sm:items-center gap-4",
            description = "Responsive Flex Layout",
            color = JBColor(0xF59E0B, 0xF59E0B)
        ),
        // Animations
        Group(
            key = "animate-fade-in",
            classes = "animate-[fade-in_0.3s_ease-in-out]",
            description = "Fade In Animation",
            color = JBColor(0xA855F7, 0xA855F7)
        ),
        Group(
            key = "animate-slide-in",
            classes = "animate-[slide-in_0.3s_ease-out]",
            description = "Slide In Animation",
            color = JBColor(0xA855F7, 0xA855F7)
        ),
        Group(
            key = "animate-pulse",
            classes = "animate-pulse",
            description = "Pulsing Animation",
            color = JBColor(0xA855F7, 0xA855F7)
        ),
        Group(
            key = "animate-bounce",
            classes = "animate-bounce",
            description = "Bouncing Animation",
            color = JBColor(0xA855F7, 0xA855F7)
        ),
        Group(
            key = "animate-spin",
            classes = "animate-spin",
            description = "Spinning Animation",
            color = JBColor(0xA855F7, 0xA855F7)
        ),
        Group(
            key = "animate-ping",
            classes = "animate-ping",
            description = "Ping Animation (Radar)",
            color = JBColor(0xA855F7, 0xA855F7)
        ),
        // Transitions
        Group(
            key = "transition-default",
            classes = "transition-all duration-300 ease-in-out",
            description = "Default Transition",
            color = JBColor(0xA855F7, 0xA855F7)
        ),
        Group(
            key = "transition-hover",
            classes = "transition-all duration-300 ease-in-out hover:shadow-lg hover:scale-105",
            description = "Hover Scale Transition",
            color = JBColor(0xA855F7, 0xA855F7)
        ),
        Group(
            key = "transition-colors",
            classes = "transition-colors duration-200 ease-in-out",
            description = "Color Transition",
            color = JBColor(0xA855F7, 0xA855F7)
        ),
        Group(
            key = "transition-transform",
            classes = "transition-transform duration-300 ease-out",
            description = "Transform Transition",
            color = JBColor(0xA855F7, 0xA855F7)
        ),
        // UI Components
        Group(
            key = "card-hover",
            classes = "bg-white rounded-lg shadow-md p-6 transition-all hover:shadow-xl hover:-translate-y-1 dark:bg-gray-800",
            description = "Card with Hover Effect",
            color = JBColor(0x0EA5E9, 0x0EA5E9)
        ),
        Group(
            key = "badge",
            classes = "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200",
            description = "Badge Component",
            color = JBColor(0x0EA5E9, 0x0EA5E9)
        ),
        Group(
            key = "badge-red",
            classes = "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200",
            description = "Red Badge",
            color = JBColor(0xEF4444, 0xEF4444)
        ),
        Group(
            key = "badge-green",
            classes = "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200",
            description = "Green Badge",
            color = JBColor(0x22C55E, 0x22C55E)
        ),
        Group(
            key = "tooltip",
            classes = "absolute z-10 px-3 py-2 text-sm font-medium text-white bg-gray-900 rounded-lg shadow-sm dark:bg-gray-700",
            description = "Tooltip Component",
            color = JBColor(0x111827, 0x111827)
        ),
        Group(
            key = "divider",
            classes = "h-px w-full bg-gray-200 dark:bg-gray-700 my-4",
            description = "Horizontal Divider",
            color = JBColor(0xE5E7EB, 0x374151)
        ),
        Group(
            key = "divider-vertical",
            classes = "w-px h-full bg-gray-200 dark:bg-gray-700 mx-4",
            description = "Vertical Divider",
            color = JBColor(0xE5E7EB, 0x374151)
        ),
        Group(
            key = "input",
            classes = "block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm px-3 py-2",
            description = "Form Input Field",
            color = JBColor(0xF3F4F6, 0x1F2937)
        ),
        Group(
            key = "checkbox",
            classes = "h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500",
            description = "Checkbox Input",
            color = JBColor(0x6366F1, 0x6366F1)
        ),
        Group(
            key = "select",
            classes = "block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm px-3 py-2",
            description = "Select Dropdown",
            color = JBColor(0xF3F4F6, 0x1F2937)
        ),
        Group(
            key = "modal",
            classes = "fixed inset-0 z-50 flex items-center justify-center p-4 bg-black bg-opacity-50",
            description = "Modal Container",
            color = JBColor(0x111827, 0x111827)
        ),
        Group(
            key = "modal-content",
            classes = "bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-md w-full p-6 max-h-[90vh] overflow-auto",
            description = "Modal Content Box",
            color = JBColor(0xFFFFFF, 0x1F2937)
        )
    )
}
