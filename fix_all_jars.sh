#!/bin/bash

# Fix all plugin JAR files with correct plugin.xml descriptor
echo "Fixing plugin descriptor in all JAR files..."

# List of JAR files to fix
JAR_FILES=(
  "build/libs/instrumented-tailwind-smart-plugin-1.2.1.jar"
  "build/libs/tailwind-smart-plugin-1.2.1.jar"
  "build/build/idea-sandbox/plugins/tailwind-smart-plugin/lib/instrumented-tailwind-smart-plugin-1.2.1.jar"
  "build/idea-sandbox/plugins-test/tailwind-smart-plugin/lib/instrumented-tailwind-smart-plugin-1.2.1.jar"
)

# Temp directory for processing
TEMP_DIR="build/tmp/fix_plugin_xml"
mkdir -p "$TEMP_DIR"

for JAR_FILE in "${JAR_FILES[@]}"; do
  if [ -f "$JAR_FILE" ]; then
    echo "Processing $JAR_FILE..."
    
    # Clean temp directory
    rm -rf "${TEMP_DIR:?}/"*
    
    # Extract JAR contents
    unzip -q "$JAR_FILE" -d "$TEMP_DIR"
    
    # Check if plugin.xml exists
    if [ -f "$TEMP_DIR/META-INF/plugin.xml" ]; then
      echo "Found plugin.xml in $JAR_FILE"
      
      # Fix plugin.xml by replacing <n> with <name>
      sed -i '' 's/<n>Tailwind CSS Support<\/n>/<name>Tailwind CSS Support<\/name>/g' "$TEMP_DIR/META-INF/plugin.xml"
      
      # Check if the fix was applied
      if grep -q "<name>Tailwind CSS Support</name>" "$TEMP_DIR/META-INF/plugin.xml"; then
        echo "✅ Fix successfully applied to plugin.xml"
      else
        echo "❌ Fix was not applied to plugin.xml"
        grep -A 1 "<n>" "$TEMP_DIR/META-INF/plugin.xml" || echo "No <n> tag found"
      fi
      
      # Rebuild JAR file
      rm "$JAR_FILE"
      (cd "$TEMP_DIR" && jar -cf "../../$JAR_FILE" .)
      echo "Rebuilt $JAR_FILE"
    else
      echo "⚠️ No plugin.xml found in $JAR_FILE"
    fi
  else
    echo "⚠️ File $JAR_FILE does not exist, skipping"
  fi
done

# Special handling for the main JAR used by IntelliJ Ultimate
MAIN_JAR="build/idea-sandbox/plugins/tailwind-smart-plugin/lib/instrumented-tailwind-smart-plugin-1.2.1.jar"
if [ -f "$MAIN_JAR" ]; then
  echo "Processing main IntelliJ JAR: $MAIN_JAR"
  
  # Clean temp directory
  rm -rf "${TEMP_DIR:?}/"*
  
  # Extract JAR contents
  unzip -q "$MAIN_JAR" -d "$TEMP_DIR"
  
  # Check if plugin.xml exists
  if [ -f "$TEMP_DIR/META-INF/plugin.xml" ]; then
    echo "Found plugin.xml in $MAIN_JAR"
    
    # Fix plugin.xml by replacing <n> with <name>
    sed -i '' 's/<n>Tailwind CSS Support<\/n>/<name>Tailwind CSS Support<\/name>/g' "$TEMP_DIR/META-INF/plugin.xml"
    
    # Check if the fix was applied
    if grep -q "<name>Tailwind CSS Support</name>" "$TEMP_DIR/META-INF/plugin.xml"; then
      echo "✅ Fix successfully applied to plugin.xml"
    else
      echo "❌ Fix was not applied to plugin.xml"
      grep -A 1 "<n>" "$TEMP_DIR/META-INF/plugin.xml" || echo "No <n> tag found"
    fi
    
    # Rebuild JAR file
    rm "$MAIN_JAR"
    (cd "$TEMP_DIR" && jar -cf "../../$MAIN_JAR" .)
    echo "Rebuilt $MAIN_JAR"
  else
    echo "⚠️ No plugin.xml found in $MAIN_JAR"
  fi
else
  echo "⚠️ Main JAR file $MAIN_JAR does not exist, trying alternate locations..."
  
  # Look for the JAR in other locations
  MAIN_JAR_ALT=$(find build -name "instrumented-tailwind-smart-plugin-1.2.1.jar" | grep -v "temp_extract" | head -1)
  if [ -n "$MAIN_JAR_ALT" ]; then
    echo "Found alternate JAR: $MAIN_JAR_ALT"
    
    # Clean temp directory
    rm -rf "${TEMP_DIR:?}/"*
    
    # Extract JAR contents
    unzip -q "$MAIN_JAR_ALT" -d "$TEMP_DIR"
    
    # Check if plugin.xml exists
    if [ -f "$TEMP_DIR/META-INF/plugin.xml" ]; then
      echo "Found plugin.xml in $MAIN_JAR_ALT"
      
      # Fix plugin.xml by replacing <n> with <name>
      sed -i '' 's/<n>Tailwind CSS Support<\/n>/<name>Tailwind CSS Support<\/name>/g' "$TEMP_DIR/META-INF/plugin.xml"
      
      # Check if the fix was applied
      if grep -q "<name>Tailwind CSS Support</name>" "$TEMP_DIR/META-INF/plugin.xml"; then
        echo "✅ Fix successfully applied to plugin.xml"
      else
        echo "❌ Fix was not applied to plugin.xml"
        grep -A 1 "<n>" "$TEMP_DIR/META-INF/plugin.xml" || echo "No <n> tag found"
      fi
      
      # Rebuild JAR file
      rm "$MAIN_JAR_ALT"
      (cd "$TEMP_DIR" && jar -cf "../../$MAIN_JAR_ALT" .)
      echo "Rebuilt $MAIN_JAR_ALT"
    else
      echo "⚠️ No plugin.xml found in $MAIN_JAR_ALT"
    fi
  else
    echo "❌ Could not find any suitable JAR to fix"
  fi
fi

echo "Script completed!"
