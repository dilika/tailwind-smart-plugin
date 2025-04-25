package com.github.dilika.tailwindsmartplugin.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.XmlPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.PsiElement
import com.intellij.openapi.diagnostic.Logger

class TailwindCompletionContributor : CompletionContributor() {
    private val logger = Logger.getInstance(TailwindCompletionContributor::class.java)

    init {
        logger.info("[Tailwind] Initializing Tailwind completion contributor")

        // Pattern for targeting HTML/XML class attributes
        val classAttributePattern = XmlPatterns.xmlAttributeValue()
            .withParent(
                XmlPatterns.xmlAttribute().withName(
                    StandardPatterns.string().oneOf("class", "className")
                )
            )

        logger.info("[Tailwind] HTML/XML class attribute pattern registered")

        // More specific pattern for JSX/TSX files
        val jsxClassPattern = PlatformPatterns.psiElement()
            .inside(PlatformPatterns.psiElement().withText(
                StandardPatterns.string().contains("className=")
            ))

        logger.info("[Tailwind] JSX/TSX class pattern registered")

        // Pattern for any string literal that might contain Tailwind classes
        val stringLiteralPattern = PlatformPatterns.psiElement()
            .inside(
                PlatformPatterns.psiElement().withText(
                    StandardPatterns.string().contains("class").or(
                        StandardPatterns.string().contains("className")
                    )
                )
            )

        logger.info("[Tailwind] Generic string literal pattern registered")

        // Register completion provider for class attributes in all contexts
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(classAttributePattern, jsxClassPattern, stringLiteralPattern),
            TailwindCompletionProvider()
        )

        logger.info("[Tailwind] Tailwind completion provider registered for all contexts")
    }
}
