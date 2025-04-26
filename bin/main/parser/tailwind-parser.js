/**
 * Tailwind CSS Class Parser
 * 
 * This script parses Tailwind CSS classes and provides data for the IntelliJ plugin.
 */

(function() {
  // Main function to parse Tailwind classes
  function parseTailwindClass(className) {
    // Default info structure
    const result = {
      type: "utility",
      icon: "●",
      color: "#64748b", // slate-500 default
      description: "Tailwind CSS utility class",
      completion: {
        displayText: className,
        type: "utility",
        style: "color: #64748b;"
      },
      documentation: {
        description: "Tailwind CSS utility class",
        link: "https://tailwindcss.com/docs"
      }
    };
    
    // Color utilities
    if (className.startsWith("text-")) {
      parseColorClass(result, className, "text");
    } else if (className.startsWith("bg-")) {
      parseColorClass(result, className, "background");
    } else if (className.startsWith("border-")) {
      parseColorClass(result, className, "border");
    } else if (className.startsWith("fill-")) {
      parseColorClass(result, className, "fill");
    } else if (className.startsWith("stroke-")) {
      parseColorClass(result, className, "stroke");
    }
    
    // Spacing utilities
    else if (className.match(/^(p|m)[txblry]?-/)) {
      parseSpacingClass(result, className);
    }
    
    // Layout utilities
    else if (["flex", "grid", "block", "inline", "hidden"].includes(className)) {
      parseDisplayClass(result, className);
    }
    
    return result;
  }
  
  // Parse color classes (text-*, bg-*, border-*, etc.)
  function parseColorClass(result, className, type) {
    // Common color values (simplified for example)
    const colorMap = {
      "slate": { "50": "#f8fafc", "100": "#f1f5f9", "500": "#64748b", "900": "#0f172a" },
      "gray": { "50": "#f9fafb", "100": "#f3f4f6", "500": "#6b7280", "900": "#111827" },
      "red": { "50": "#fef2f2", "100": "#fee2e2", "500": "#ef4444", "900": "#7f1d1d" },
      "blue": { "50": "#eff6ff", "100": "#dbeafe", "500": "#3b82f6", "900": "#1e3a8a" },
      "green": { "50": "#f0fdf4", "100": "#dcfce7", "500": "#22c55e", "900": "#14532d" },
      "yellow": { "50": "#fefce8", "100": "#fef9c3", "500": "#eab308", "900": "#713f12" },
      "purple": { "50": "#faf5ff", "100": "#f3e8ff", "500": "#a855f7", "900": "#581c87" },
      "pink": { "50": "#fdf2f8", "100": "#fce7f3", "500": "#ec4899", "900": "#831843" },
    };
    
    // Extract color name and shade
    const parts = className.split('-');
    if (parts.length >= 2) {
      const colorName = parts[1].split('/')[0]; // Handle opacity notation like text-red-500/75
      let colorShade = parts[2] || "500";
      
      // Handle special cases like text-white, text-black
      if (colorName === "white") {
        result.color = "#ffffff";
        result.completion.style = "color: #ffffff;";
      } else if (colorName === "black") {
        result.color = "#000000";
        result.completion.style = "color: #000000;";
      } else if (colorMap[colorName] && colorMap[colorName][colorShade]) {
        // Use the color from our map
        result.color = colorMap[colorName][colorShade];
        result.completion.style = `color: ${colorMap[colorName][colorShade]};`;
      }
      
      result.type = type;
      result.icon = "■";
      result.description = `${type} color: ${colorName}-${colorShade}`;
      result.completion.type = "color";
      result.completion.displayText = className;
      result.documentation.description = `Sets the ${type} color to ${colorName}-${colorShade}`;
      result.documentation.link = `https://tailwindcss.com/docs/text-color`;
    }
  }
  
  // Parse spacing classes (p-*, m-*, etc.)
  function parseSpacingClass(result, className) {
    const prefix = className.charAt(0);
    const type = prefix === 'p' ? 'padding' : 'margin';
    
    let direction = '';
    if (className.charAt(1) === 't') direction = 'top';
    else if (className.charAt(1) === 'b') direction = 'bottom';
    else if (className.charAt(1) === 'l') direction = 'left';
    else if (className.charAt(1) === 'r') direction = 'right';
    else if (className.charAt(1) === 'x') direction = 'horizontal';
    else if (className.charAt(1) === 'y') direction = 'vertical';
    
    result.type = "spacing";
    result.icon = "◫";
    result.color = "#8b5cf6"; // purple-500
    result.completion.style = "color: #8b5cf6;";
    result.completion.type = "spacing";
    
    const dirText = direction ? ` ${direction}` : '';
    result.description = `${type}${dirText}`;
    result.documentation.description = `Sets the ${type}${dirText}`;
    result.documentation.link = `https://tailwindcss.com/docs/${type}`;
  }
  
  // Parse display classes (flex, grid, etc.)
  function parseDisplayClass(result, className) {
    result.type = "layout";
    result.icon = "⊞";
    result.color = "#0ea5e9"; // sky-500
    result.completion.style = "color: #0ea5e9;";
    result.completion.type = "layout";
    result.description = `display: ${className}`;
    result.documentation.description = `Sets the display property to ${className}`;
    result.documentation.link = `https://tailwindcss.com/docs/display`;
  }
  
  // Expose the parser function globally
  return {
    parseTailwindClass: parseTailwindClass
  };
})();
