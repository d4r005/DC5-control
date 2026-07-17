package com.naf.erp.training.document

import org.springframework.stereotype.Service

import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import java.io.ByteArrayOutputStream
import java.io.File

@Service
class DocxEngine(
    private val contentControlEngine: ContentControlEngine
) : DocumentEngine {

    override fun render(
        template: DocumentTemplate
    ): ByteArray {
        val word =
            WordprocessingMLPackage.load(
                File(template.file)
            )

        contentControlEngine.fill(
            word,
            template.fields
        )

        val out =
            ByteArrayOutputStream()

        word.save(out)

        return out.toByteArray()
    }

}
