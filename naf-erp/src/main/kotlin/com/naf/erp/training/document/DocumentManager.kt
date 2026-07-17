package com.naf.erp.training.document

import com.naf.erp.training.repository.DocumentTemplateRepository
import org.springframework.stereotype.Service
import java.security.MessageDigest

@Service
class DocumentManager(
    private val repository: DocumentTemplateRepository,
    private val engine: DocxEngine
) {

    fun generate(
        code: String,
        fields: MutableMap<String, String>
    ): ByteArray {
        val template = repository.findByCodeAndActiveTrue(code)
            ?: throw RuntimeException("Plantilla no encontrada: $code")

        return engine.render(
            DocumentTemplate(
                template.name,
                template.file,
                fields
            )
        )
    }

    fun hash(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(bytes).joinToString("") {
            "%02x".format(it)
        }
    }

}
