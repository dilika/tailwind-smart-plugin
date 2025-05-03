package com.github.dilika.tailwindsmartplugin.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.XmlPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.openapi.diagnostic.Logger

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
        val classAttributePattern = XmlPatterns.xmlAttributeValue()
            .withParent(
                XmlPatterns.xmlAttributeValue().withName(
                    StandardPatterns.string().oneOf("class", "className")
                )
            )

        logger.info("[Tailwind] HTML/XML class attribute pattern registered")

        // Generic pattern for all PSI (to use for files JSX/TSX)
        val genericPattern = PlatformPatterns.psiElement()

        logger.info("Generic patterns saved")

        // Register completion provider only for all class attributes for all contextes
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(classAttributePattern, genericPattern),
            TailwindCompletionProvider()
        )

        logger.info("[Tailwind] Tailwind completion provider registered for class attributes")
    }
}
