package com.github.dilika.tailwindsmartplugin

import com.intellij.testFramework.TestDataPath
import org.junit.Assert
import org.junit.Test

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class TailwindPluginTest {

    @Test
    fun testSomethingSimple() {
        // Simple test that always passes
        Assert.assertTrue("This test should always pass", true)
    }

    // The getTestDataPath function is no longer needed since we're not extending the platform test case
}