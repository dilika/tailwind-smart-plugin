package com.github.dilika.tailwindsmartplugin.listeners

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.ex.FoldingModelEx
import com.intellij.openapi.editor.impl.FoldingModelImpl
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue

/**
 * Écouteur qui s'attache à chaque nouvel éditeur pour plier automatiquement 
 * les attributs class et className de Tailwind CSS.
 */
class TailwindEditorListener : EditorFactoryListener {

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        val project = editor.project ?: return
        val document = editor.document
        
        // Récupérer le fichier associé au document
        val file = FileDocumentManager.getInstance().getFile(document) ?: return
        
        // Ne traiter que les fichiers pertinents
        val fileType = file.fileType.name.lowercase()
        if (!isRelevantFileType(fileType) && !isRelevantFileExtension(file.extension)) {
            return
        }
        
        // Exécuter avec un délai pour s'assurer que l'éditeur est entièrement initialisé
        javax.swing.SwingUtilities.invokeLater {
            applyTailwindFolding(editor, project)
        }
    }
    
    /**
     * Vérifie si le type de fichier est pertinent pour le pliage d'attributs Tailwind
     */
    private fun isRelevantFileType(fileType: String): Boolean {
        return fileType.contains("html") || 
               fileType.contains("xml") || 
               fileType.contains("jsx") ||
               fileType.contains("tsx") ||
               fileType.contains("javascript") ||
               fileType.contains("typescript") ||
               fileType.contains("vue") ||
               fileType.contains("php")
    }
    
    /**
     * Vérifie si l'extension du fichier est pertinente
     */
    private fun isRelevantFileExtension(extension: String?): Boolean {
        if (extension == null) return false
        
        return extension == "html" ||
               extension == "htm" ||
               extension == "xml" ||
               extension == "jsx" ||
               extension == "tsx" ||
               extension == "js" ||
               extension == "ts" ||
               extension == "vue" ||
               extension == "php"
    }
    
    /**
     * Applique le pliage pour tous les attributs class/className dans le document
     */
    private fun applyTailwindFolding(editor: Editor, project: com.intellij.openapi.project.Project) {
        val document = editor.document
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return
        
        // Trouver tous les attributs dans le fichier
        val tailwindAttributes = mutableListOf<Pair<TextRange, String>>()
        
        PsiTreeUtil.processElements(psiFile, PsiElementProcessor {
            if (it is XmlAttribute && (it.name == "class" || it.name == "className")) {
                val valueElement = it.valueElement
                if (valueElement != null && valueElement.value.isNotBlank() && valueElement.value.length > 10) {
                    val valueRange = valueElement.textRange
                    val startOffset = valueRange.startOffset + 1 // +1 pour sauter le guillemet ouvrant
                    val endOffset = valueRange.endOffset - 1 // -1 pour exclure le guillemet fermant
                    
                    if (startOffset < endOffset) {
                        tailwindAttributes.add(Pair(
                            TextRange(startOffset, endOffset),
                            "..."
                        ))
                    }
                }
            }
            true
        })
        
        // Appliquer le pliage sur tous les attributs trouvés
        if (tailwindAttributes.isNotEmpty()) {
            val foldingModel = editor.foldingModel as FoldingModelEx
            
            foldingModel.runBatchFoldingOperation {
                for ((range, placeholder) in tailwindAttributes) {
                    foldingModel.createFoldRegion(
                        range.startOffset,
                        range.endOffset,
                        placeholder,
                        null,
                        true // collapsed par défaut
                    )
                }
            }
        }
    }
}
