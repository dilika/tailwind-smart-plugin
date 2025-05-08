#!/bin/bash

# This script specifically fixes the main plugin JAR file
# The error message points to a different location than our previous fixes

MAIN_JAR_PATH="build/idea-sandbox/plugins/tailwind-smart-plugin-1.2.1.jar"
TEMP_DIR="build/tmp/fix_main_jar"

echo "Fixing plugin descriptor in main JAR: $MAIN_JAR_PATH"

# Create temp directory
mkdir -p "$TEMP_DIR"
rm -rf "$TEMP_DIR/*"

if [ -f "$MAIN_JAR_PATH" ]; then
    echo "Found main plugin JAR, extracting contents..."
    
    # Extract JAR contents
    unzip -q "$MAIN_JAR_PATH" -d "$TEMP_DIR"
    
    # Fix plugin.xml
    PLUGIN_XML="$TEMP_DIR/META-INF/plugin.xml"
    if [ -f "$PLUGIN_XML" ]; then
        echo "Found plugin.xml, fixing tag..."
        sed -i '' 's/<n>Tailwind CSS Support<\/n>/<name>Tailwind CSS Support<\/name>/g' "$PLUGIN_XML"
        echo "Fixed content:"
        grep -A 1 "<name>" "$PLUGIN_XML" || grep -A 1 "<n>" "$PLUGIN_XML"
        
        # Recreate JAR
        rm "$MAIN_JAR_PATH"
        cd "$TEMP_DIR" && jar -cf "../../$MAIN_JAR_PATH" .
        echo "✅ Plugin descriptor fixed successfully in $MAIN_JAR_PATH"
    else
        echo "❌ Error: Could not find plugin.xml in the extracted JAR"
    fi
else
    echo "❌ Error: Main JAR file not found at $MAIN_JAR_PATH"
    
    # Try to locate it
    echo "Searching for plugin JAR files..."
    find build -name "tailwind-smart-plugin-*.jar" -type f
fi

echo "Script completed!"
