#!/bin/bash

echo "🚀 Deploying Tailwind Smart Plugin to JetBrains Marketplace..."

# Vérifier les variables d'environnement
if [ -z "$PUBLISH_TOKEN" ]; then
    echo "❌ PUBLISH_TOKEN not set"
    exit 1
fi

if [ -z "$CERTIFICATE_CHAIN" ]; then
    echo "❌ CERTIFICATE_CHAIN not set"
    exit 1
fi

if [ -z "$PRIVATE_KEY" ]; then
    echo "❌ PRIVATE_KEY not set"
    exit 1
fi

if [ -z "$PRIVATE_KEY_PASSWORD" ]; then
    echo "❌ PRIVATE_KEY_PASSWORD not set"
    exit 1
fi

# Build final
echo "🔨 Building final version..."
./gradlew clean buildPlugin

# Signature
echo "🔐 Signing plugin..."
./gradlew signPlugin

# Publication
echo "📤 Publishing to JetBrains Marketplace..."
./gradlew publishPlugin

echo "🎉 Plugin deployed successfully!"
