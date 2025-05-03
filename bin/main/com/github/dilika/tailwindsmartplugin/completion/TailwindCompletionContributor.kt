package com.github.dilika.tailwindsmartplugin.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.XmlPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.xml.XmlTokenType

/**
 * CompletionContributor for Tailwind CSS classes.
 * This class registers the TailwindCompletionProvider to provide completion
 * suggestions for Tailwind CSS classes in appropriate contexts.
 */
class TailwindCompletionContributor : CompletionContributor() {
    private val logger = Logger.getInstance(TailwindCompletionContributor::class.java)

    init {
        logger.info("[Tailwind] Initializing Tailwind completion contributor")

        // Pattern for targeting HTML/XML class attributes
        val classAttributePattern = PlatformPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
            .withParent(
                XmlPatterns.xmlAttributeValue().withName(
                    StandardPatterns.string().oneOf("class", "className")
                )
            )

        logger.info("[Tailwind] HTML/XML class attribute pattern registered")

        // Register completion provider only for class attributes
        extend(
            CompletionType.BASIC,
            classAttributePattern,
            TailwindCompletionProvider()
        )
        
        logger.info("[Tailwind] Tailwind completion provider registered for class attributes")
    }
}
