package com.github.dilika.tailwindsmartplugin

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.components.service
import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.PsiErrorElementUtil
import com.github.dilika.tailwindsmartplugin.services.TailwindConfigService

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class TailwindPluginTest : BasePlatformTestCase() {

    fun testXMLFile() {
        val psiFile = myFixture.configureByText(XmlFileType.INSTANCE, "<foo>bar</foo>")
        val xmlFile = assertInstanceOf(psiFile, XmlFile::class.java)

        assertFalse(PsiErrorElementUtil.hasErrors(project, xmlFile.virtualFile))

        assertNotNull(xmlFile.rootTag)

        xmlFile.rootTag?.let {
            assertEquals("foo", it.name)
            assertEquals("bar", it.value.text)
        }
    }

    fun testRename() {
        myFixture.testRename("foo.xml", "foo_after.xml", "a2")
    }

    fun testTailwindConfigService() {
        val configService = project.service<TailwindConfigService>()
        assertNotNull(configService)
        // Config path might be null in test environment
        assertNull(configService.getTailwindConfigPath())
    }

    override fun getTestDataPath() = "src/test/testData/rename"
}