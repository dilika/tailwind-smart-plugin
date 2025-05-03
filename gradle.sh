#!/bin/bash
# This script redirects 'gradle' commands to use './gradlew' instead
# to ensure consistent builds

echo "Using Gradle wrapper instead of system Gradle..."
./gradlew "$@"
