#!/bin/bash

# This script is for local development to fix the VERSION_CATALOGS issue

echo "Creating a corrected settings.gradle.kts file..."

# Create the new settings.gradle.kts without VERSION_CATALOGS
cat > settings.gradle.kts << 'EOL'
rootProject.name = "tailwind-smart-plugin"

// Configure Gradle Enterprise
plugins {
    id("com.gradle.enterprise") version "3.16.1"
}

// Configure Gradle Enterprise Plugin
gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
EOL

echo "settings.gradle.kts has been corrected. Now running the build..."
chmod +x gradlew
./gradlew buildPlugin
