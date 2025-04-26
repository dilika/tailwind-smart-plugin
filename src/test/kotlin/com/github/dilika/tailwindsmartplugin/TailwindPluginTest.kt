package com.github.dilika.tailwindsmartplugin

import com.intellij.openapi.components.service
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.github.dilika.tailwindsmartplugin.services.TailwindConfigService

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class TailwindPluginTest : BasePlatformTestCase() {

    fun testBasicPluginLoad() {
        // Just verify we can load the plugin and create a project
        assertNotNull(project)
        println("Test project created successfully: ${project.name}")
    }

    fun testTailwindConfigService() {
        val configService = project.service<TailwindConfigService>()
        assertNotNull(configService)
        // Config path might be null in test environment
        println("TailwindConfigService loaded, config path: ${configService.getTailwindConfigPath()}")
        // Not asserting on the config path since it will be null in test environment
    }

    override fun getTestDataPath() = "src/test/testData"
}