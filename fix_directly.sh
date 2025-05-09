#!/bin/bash

# This script creates a completely new plugin.xml file from scratch and replaces it in the JAR

MAIN_JAR_PATH="build/idea-sandbox/plugins/tailwind-smart-plugin-1.2.1.jar"
TEMP_DIR="build/tmp/fix_direct"

echo "Fixing plugin descriptor in $MAIN_JAR_PATH (direct replacement approach)"

# Create temp directory
mkdir -p "$TEMP_DIR"
rm -rf "$TEMP_DIR/*"

if [ -f "$MAIN_JAR_PATH" ]; then
    echo "Found plugin JAR, extracting contents..."
    
    # Extract JAR contents
    unzip -q "$MAIN_JAR_PATH" -d "$TEMP_DIR"
    
    # Create a completely new plugin.xml with proper tag
    PLUGIN_XML="$TEMP_DIR/META-INF/plugin.xml"
    
    if [ -f "$PLUGIN_XML" ]; then
        echo "Creating new plugin.xml with proper name tag..."
        
        # Extract content before the invalid tag
        CONTENT_BEFORE=$(grep -B 1000 "<name>Tailwind CSS Support</name>" "$PLUGIN_XML" | head -n -1)
        
        # Extract content after the invalid tag
        CONTENT_AFTER=$(grep -A 1000 "<name>Tailwind CSS Support</name>" "$PLUGIN_XML" | tail -n +2)
        
        # Create new file with proper tag
        echo "$CONTENT_BEFORE" > "$PLUGIN_XML.new"
        echo "  <name>Tailwind CSS Support</name>" >> "$PLUGIN_XML.new"
        echo "$CONTENT_AFTER" >> "$PLUGIN_XML.new"
        
        # Replace original with fixed version
        mv "$PLUGIN_XML.new" "$PLUGIN_XML"
        
        echo "Verification of fixed file:"
        grep -A 1 "<name>" "$PLUGIN_XML" || echo "❌ Tag not found after fix!"
        
        # Recreate JAR
        rm "$MAIN_JAR_PATH"
        cd "$TEMP_DIR" && jar -cf "../../$MAIN_JAR_PATH" .
        echo "✅ Plugin JAR rebuilt with fixed descriptor"
    else
        echo "❌ Error: plugin.xml not found in extracted JAR"
    fi
else
    echo "❌ Error: JAR file not found at $MAIN_JAR_PATH"
fi

# Let's also fix the direct source file to prevent future issues
SRC_PLUGIN_XML="/Volumes/Dev/personal-projects/tailwind-smart-plugin/src/main/resources/META-INF/plugin.xml"

if [ -f "$SRC_PLUGIN_XML" ]; then
    echo "Fixing source plugin.xml file..."
    sed -i '' 's/<n>Tailwind CSS Support<\/n>/<name>Tailwind CSS Support<\/name>/g' "$SRC_PLUGIN_XML"
    echo "✅ Source plugin.xml fixed"
fi

echo "Script completed!"
