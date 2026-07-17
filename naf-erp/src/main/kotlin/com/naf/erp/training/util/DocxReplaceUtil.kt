package com.naf.erp.training.util

import org.docx4j.finders.ClassFinder
import org.docx4j.wml.Text

object DocxReplaceUtil {

    fun replace(
        packageWord: org.docx4j.openpackaging.packages.WordprocessingMLPackage,
        values: Map<String, String>
    ) {

        val finder = ClassFinder(Text::class.java)

        packageWord.mainDocumentPart.content.forEach {
            org.docx4j.TraversalUtil(it, finder)
        }

        finder.results.forEach {
            val text = it as Text
            values.forEach { (k, v) ->
                if (text.value.contains("\${$k}")) {
                    text.value = text.value.replace("\${$k}", v)
                }
            }
        }
    }
}
