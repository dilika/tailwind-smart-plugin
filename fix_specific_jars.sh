#!/bin/bash

# This script fixes specific JAR files that are causing the "invalid plugin descriptor" error

echo "=== Fixing Tailwind Smart Plugin JAR files for IntelliJ IDEA Ultimate 2024.1 ==="

# Define all potential locations of the plugin JAR files that might need fixing
PLUGIN_JARS=(
  "/Volumes/Dev/personal-projects/tailwind-smart-plugin/build/idea-sandbox/plugins/tailwind-smart-plugin-1.2.1.jar"
  "build/build/idea-sandbox/plugins/tailwind-smart-plugin-1.2.1.jar" 
  "build/idea-sandbox/plugins/tailwind-smart-plugin-1.2.1.jar"
  "build/libs/tailwind-smart-plugin-1.2.1.jar"
  "build/libs/instrumented-tailwind-smart-plugin-1.2.1.jar"
)

TEMP_DIR="build/tmp/fix_jars"
mkdir -p "$TEMP_DIR"

# Function to fix a single JAR file
fix_jar() {
  local jar_path="$1"
  
  echo "Processing: $jar_path"
  
  if [ -f "$jar_path" ]; then
    echo "✓ File exists, extracting..."
    
    # Clean temp dir
    rm -rf "${TEMP_DIR:?}/"*
    
    # Extract JAR
    unzip -q "$jar_path" -d "$TEMP_DIR"
    
    # Check if plugin.xml exists
    local plugin_xml="$TEMP_DIR/META-INF/plugin.xml"
    if [ -f "$plugin_xml" ]; then
      # Check if it contains the invalid tag
      if grep -q "<n>Tailwind CSS Support</n>" "$plugin_xml"; then
        echo "✓ Found invalid tag, fixing..."
        
        # Replace the invalid tag
        sed -i '' 's/<n>Tailwind CSS Support<\/n>/<name>Tailwind CSS Support<\/name>/g' "$plugin_xml"
        
        # Verify fix
        if grep -q "<name>Tailwind CSS Support</name>" "$plugin_xml"; then
          echo "✓ Tag successfully replaced"
          
          # Rebuild JAR
          rm "$jar_path"
          (cd "$TEMP_DIR" && jar -cf "$jar_path" .)
          echo "✅ Successfully fixed: $jar_path"
        else
          echo "❌ Failed to replace tag"
          return 1
        fi
      else
        echo "ℹ️ No invalid tag found in plugin.xml, checking if valid tag exists"
        if grep -q "<name>Tailwind CSS Support</name>" "$plugin_xml"; then
          echo "✓ Valid tag already exists"
        else 
          echo "❌ Neither invalid nor valid tag found - unexpected plugin.xml format"
          return 1
        fi
      fi
    else
      echo "❌ No plugin.xml found in JAR"
      return 1
    fi
  else
    echo "ℹ️ File does not exist: $jar_path"
    return 0
  fi
}

# Fix each JAR
for jar in "${PLUGIN_JARS[@]}"; do
  fix_jar "$jar"
  echo "-----------------------------------------"
done

# Also fix the source plugin.xml to prevent future issues
SRC_PLUGIN_XML="src/main/resources/META-INF/plugin.xml"
if [ -f "$SRC_PLUGIN_XML" ]; then
  echo "Fixing source plugin.xml file..."
  sed -i '' 's/<n>Tailwind CSS Support<\/n>/<name>Tailwind CSS Support<\/name>/g' "$SRC_PLUGIN_XML"
  if grep -q "<name>Tailwind CSS Support</name>" "$SRC_PLUGIN_XML"; then
    echo "✅ Source plugin.xml fixed"
  else
    echo "❌ Failed to fix source plugin.xml"
  fi
fi

echo "=== Script completed! ==="
echo "If you continue to see 'invalid plugin descriptor' errors, please restart IntelliJ IDEA completely"
echo "and make sure to build the plugin with './gradlew clean buildPlugin' before running this script again."
