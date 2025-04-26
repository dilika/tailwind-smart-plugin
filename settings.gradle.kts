plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("com.gradle.enterprise") version "3.16.1"
}

rootProject.name = "tailwind-smart-plugin"

// Configure Gradle Enterprise Plugin
gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

// Use Gradle 8.5 for this project
gradle.rootProject {
    buildscript {
        configurations.classpath {
            resolutionStrategy.capabilitiesResolution.withCapability("org.gradle:gradle-enterprise-gradle-plugin") {
                selectHighestVersion()
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}
