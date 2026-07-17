package com.naf.erp.training.service

import com.naf.erp.training.entity.DC3
import com.naf.erp.training.repository.DC3Repository
import com.naf.erp.training.repository.TrainingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class DC3Service(
    private val dc3Repository: DC3Repository,
    private val trainingRepository: TrainingRepository,
    private val wordTemplateService: WordTemplateService,
    private val pdfService: PdfService
) {

    fun generate(trainingId: Long): DC3 {
        val training = trainingRepository.findById(trainingId)
            .orElseThrow { RuntimeException("Capacitación no encontrada") }

        if (!training.approved)
            throw RuntimeException("El trabajador aún no aprobó el curso")

        if (dc3Repository.existsByTraining(training))
            throw RuntimeException("Ya existe un DC3 para esta capacitación")

        val dc3 = DC3()
        dc3.training = training
        dc3.generated = LocalDateTime.now()
        dc3.dc3Number = generateNumber()

        validate(training)

        val docx = wordTemplateService.generate(dc3)
        val pdf = pdfService.convert(docx)

        dc3.docx = docx
        dc3.pdf = pdf

        return dc3Repository.save(dc3)
    }

    private fun validate(training: Training) {
        require(training.employee.curp.length == 18) { "CURP inválida (debe ser de 18 caracteres)" }
        require(training.course.duration > 0) { "La duración del curso debe ser mayor a 0" }
        require(!training.course.thematicArea.isNullOrBlank()) { "Falta área temática en el curso" }
        require(!training.course.occupationKey.isNullOrBlank()) { "Falta clave de ocupación en el curso" }
        require(!training.instructor.stpsNumber.isNullOrBlank()) { "El instructor no tiene registro STPS" }
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
