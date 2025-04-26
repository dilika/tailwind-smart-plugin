package com.github.dilika.tailwindsmartplugin.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import com.github.dilika.tailwindsmartplugin.utils.TailwindUtils

class TailwindCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        resultSet: CompletionResultSet
    ) {
        val project = parameters.originalFile.project
        val classes = try {
            TailwindUtils.getTailwindClasses(project)
        } catch (e: Exception) {
            listOf("bg-blue-500", "text-white", "p-4")
        }

        classes.forEach { className ->
            resultSet.addElement(LookupElementBuilder.create(className))
        }
    }
}
