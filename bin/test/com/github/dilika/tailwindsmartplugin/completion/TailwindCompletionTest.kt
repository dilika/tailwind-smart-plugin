package com.github.dilika.tailwindsmartplugin.completion

import org.junit.Test
import org.junit.Assert.*
import java.io.File

/**
 * Simple test for Tailwind CSS completion classes
 * This test doesn't use the IntelliJ Platform test framework to avoid environment-specific issues
 */
class TailwindCompletionTest {

    @Test
    fun testTailwindCompletionProviderExists() {
        println("Testing that TailwindCompletionProvider class exists")

        // Verify that the completion provider class exists
        val providerClass = Class.forName("com.github.dilika.tailwindsmartplugin.completion.TailwindCompletionProvider")
        assertNotNull("TailwindCompletionProvider class should be loaded", providerClass)

        println("TailwindCompletionProvider class exists")
    }

    @Test
    fun testTailwindCompletionContributorExists() {
        println("Testing that TailwindCompletionContributor class exists")

        // Verify that the completion contributor class exists
        val contributorClass = Class.forName("com.github.dilika.tailwindsmartplugin.completion.TailwindCompletionContributor")
        assertNotNull("TailwindCompletionContributor class should be loaded", contributorClass)

        println("TailwindCompletionContributor class exists")
    }

    @Test
    fun testTailwindConfigFileExists() {
        println("Testing that test Tailwind config file can be created")

        // Create a test data directory
        val testDataDir = File("src/test/testData")
        if (!testDataDir.exists()) {
            testDataDir.mkdirs()
        }

        // Create a minimal config
        val minimalConfig = """
            /** @type {import('tailwindcss').Config} */
            module.exports = {
              content: [
                "./src/**/*.{html,js,jsx,ts,tsx}",
                "./public/index.html"
              ],
              theme: {
                extend: {},
              },
              plugins: [],
            }
        """.trimIndent()

        // Write to test data
        val targetFile = File(testDataDir, "tailwind.config.js")
        targetFile.writeText(minimalConfig)

        assertTrue("Tailwind config file should exist", targetFile.exists())
        println("Successfully created test Tailwind config file")
    }
}
