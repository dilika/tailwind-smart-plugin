rootProject.name = "tailwind-smart-plugin"

// Configure Gradle Enterprise
plugins {
    id("com.gradle.enterprise") version "3.19.2"
}

// Configure Gradle Enterprise Plugin
gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}
