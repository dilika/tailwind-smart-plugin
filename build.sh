#!/bin/bash

echo "🚀 Building Tailwind Smart Plugin..."

# Nettoyer
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Tests
echo "🧪 Running tests..."
./gradlew test

# Build
echo "🔨 Building plugin..."
./gradlew buildPlugin

# Vérification
echo "✅ Verifying build..."
./gradlew verifyPlugin

echo "🎉 Build completed successfully!"




