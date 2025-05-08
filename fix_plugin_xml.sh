#!/bin/bash

# This script fixes the plugin.xml file in the built JAR
JAR_PATH="build/idea-sandbox/plugins/tailwind-smart-plugin/lib/instrumented-tailwind-smart-plugin-1.2.1.jar"
TEMP_DIR="build/tmp/fix_plugin_xml"

echo "Fixing plugin.xml in $JAR_PATH"

# Create temp directory
mkdir -p "$TEMP_DIR"
rm -rf "$TEMP_DIR/*"

# Extract JAR contents
unzip -q "$JAR_PATH" -d "$TEMP_DIR"

# Fix plugin.xml
PLUGIN_XML="$TEMP_DIR/META-INF/plugin.xml"
if [ -f "$PLUGIN_XML" ]; then
    echo "Found plugin.xml, fixing..."
    sed -i '' 's/<n>Tailwind CSS Support<\/n>/<name>Tailwind CSS Support<\/name>/g' "$PLUGIN_XML"
    echo "Fixed content:"
    grep -A 1 "<name>" "$PLUGIN_XML"
else
    echo "Warning: Could not find plugin.xml in the extracted JAR!"
fi

# Recreate JAR
rm "$JAR_PATH"
cd "$TEMP_DIR" && jar -cf "../../$JAR_PATH" .

echo "Plugin XML fixed successfully in $JAR_PATH"

# Verify the fix
mkdir -p "build/tmp/verify_fix"
unzip -q "$JAR_PATH" META-INF/plugin.xml -d "build/tmp/verify_fix"
echo "Verification:"
grep -A 1 "<name>" "build/tmp/verify_fix/META-INF/plugin.xml"
