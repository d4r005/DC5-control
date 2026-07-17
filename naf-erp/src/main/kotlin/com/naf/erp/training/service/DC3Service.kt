package com.naf.erp.training.service

import com.naf.erp.training.document.DC3Factory
import com.naf.erp.training.document.DocxEngine
import com.naf.erp.training.document.LocalStorage
import com.naf.erp.training.document.validator.DC3Validator
import com.naf.erp.training.entity.DC3
import com.naf.erp.training.repository.DC3Repository
import com.naf.erp.training.repository.TrainingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class DC3Service(
    private val validator: DC3Validator,
    private val factory: DC3Factory,
    private val engine: DocxEngine,
    private val storage: LocalStorage,
    private val dc3Repository: DC3Repository,
    private val trainingRepository: TrainingRepository
) {

    fun generate(
        dc3: DC3
    ): String {
        val validation =
            validator.validate(dc3)

        if (validation.errors.isNotEmpty()) {
            throw RuntimeException(
                validation.errors.joinToString("\n")
            )
        }

        val template =
            factory.create(dc3)

        val bytes =
            engine.render(template)

        return storage.save(
            "DC3_${dc3.id}.docx",
            bytes
        )
    }


    private fun generateNumber(): String {
        val year = java.time.LocalDate.now().year
        val count = dc3Repository.count() + 1
        return "DC3-$year-${count.toString().padStart(6, '0')}"
    }

    fun all(): List<DC3> = dc3Repository.findAll()

    fun pdf(id: Long): java.io.File {
        val dc3 = dc3Repository.findById(id)
            .orElseThrow { RuntimeException("DC3 no encontrado") }
        return java.io.File(dc3.pdf!!)
    }

    fun docx(id: Long): java.io.File {
        val dc3 = dc3Repository.findById(id)
            .orElseThrow { RuntimeException("DC3 no encontrado") }
        return java.io.File(dc3.docx!!)
    }

    fun reprint(id: Long): DC3 {
        return dc3Repository.findById(id)
            .orElseThrow { RuntimeException("DC3 no encontrado") }
    }

    fun delete(id: Long) {
        val dc3 = dc3Repository.findById(id)
            .orElseThrow { RuntimeException("DC3 no encontrado") }

        java.io.File(dc3.pdf!!).delete()
        java.io.File(dc3.docx!!).delete()
        dc3Repository.delete(dc3)
    }
}
