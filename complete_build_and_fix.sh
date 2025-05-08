#!/bin/bash

echo "========== Complete Clean, Build, and Fix for Tailwind Smart Plugin =========="
echo "This script will perform a complete clean build and fix all JAR files to ensure compatibility with IntelliJ IDEA Ultimate 2024.1"

# Step 1: Fix the source plugin.xml file to prevent future issues
echo "Fixing source plugin.xml file..."
sed -i '' 's/<n>Tailwind CSS Support<\/n>/<name>Tailwind CSS Support<\/name>/g' "src/main/resources/META-INF/plugin.xml"

# Step 2: Complete clean and build
echo "Performing complete clean and build..."
./gradlew clean
./gradlew buildPlugin --info

# Step 3: Find and fix all JAR files
echo "Finding all plugin JAR files to fix..."
JAR_FILES=$(find ./build -name "*tailwind*.jar" | grep -v "temp_")

TEMP_DIR="build/tmp/fix_all_jars"
mkdir -p "$TEMP_DIR"

for jar_path in $JAR_FILES; do
  echo "Processing: $jar_path"
  
  # Clean temp directory
  rm -rf "${TEMP_DIR:?}/"*
  
  # Extract JAR contents
  unzip -q "$jar_path" -d "$TEMP_DIR" || {
    echo "  Failed to extract JAR: $jar_path"
    continue
  }
  
  # Check and fix plugin.xml if it exists
  if [ -f "$TEMP_DIR/META-INF/plugin.xml" ]; then
    echo "  Found plugin.xml, checking for invalid tag..."
    
    if grep -q "<n>" "$TEMP_DIR/META-INF/plugin.xml"; then
      echo "  Fixing invalid <n> tag in: $jar_path"
      sed -i '' 's/<n>Tailwind CSS Support<\/n>/<name>Tailwind CSS Support<\/name>/g' "$TEMP_DIR/META-INF/plugin.xml"
      
      # Show the result
      grep -A 1 "<name>" "$TEMP_DIR/META-INF/plugin.xml" || echo "  WARNING: Could not confirm fix was applied"
      
      # Rebuild JAR
      rm "$jar_path"
      (cd "$TEMP_DIR" && jar -cf "$jar_path" .)
      echo "  ✅ Fixed and rebuilt: $jar_path"
    else
      echo "  ✓ No invalid tag found in: $jar_path"
    fi
  else
    echo "  ⚠️ No plugin.xml found in: $jar_path"
  fi
  
  echo "----------------------------------------"
done

# Step 4: Generate a clean distribution ZIP with fixed plugin
echo "Creating a fixed distribution ZIP..."
./gradlew buildPlugin

DIST_ZIP="build/distributions/tailwind-smart-plugin-1.2.1.zip"
if [ -f "$DIST_ZIP" ]; then
  echo "✅ Distribution ZIP created successfully: $DIST_ZIP"
  echo "You can install this ZIP file manually in IntelliJ IDEA Ultimate via:"
  echo "Settings > Plugins > ⚙️ > Install Plugin from Disk..."
else
  echo "❌ Failed to create distribution ZIP"
fi

echo "========== Build and Fix Complete =========="
echo "IMPORTANT: Please restart IntelliJ IDEA completely before installing the plugin again"
echo "If the error persists, try installing from the distribution ZIP file instead"
