package com.naf.erp.training.service

import com.naf.erp.training.entity.DC3
import com.naf.erp.training.model.DC3Fields
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.time.format.DateTimeFormatter

@Service
class WordTemplateService(
    @Value("${dc3.template}")
    private val templatePath: String,

    @Value("${dc3.output}")
    private val outputPath: String,

    private val contentControlService: ContentControlService
) {

    fun generate(dc3: DC3): String {
        val training = dc3.training
        val employee = training.employee
        val course = training.course
        val instructor = training.instructor

        // 1. Transformar Training a DC3Fields (Data)
        val data = DC3Fields(
            trabajador = "${employee.lastName} ${employee.middleName} ${employee.firstName}".uppercase(),
            curp = employee.curp.uppercase(),
            ocupacion = course.occupationKey ?: "",
            puesto = employee.position.uppercase(),
            empresa = "NORTH AMERICA FLOORING SA DE CV",
            rfc = "NAFXXXXXXXXX",
            curso = course.name.uppercase(),
            horas = course.duration.toString(),
            fechaInicio = training.startDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
            fechaFin = training.endDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
            areaTematica = course.thematicArea,
            instructor = instructor.fullName.uppercase(),
            registroSTPS = instructor.stpsNumber ?: "",
            representantePatron = "JINSONG ZHAO",
            representanteTrabajadores = "LILIANA ROSALES"
        )

        // 2. Definir archivos
        val fileName = "DC3_${employee.employeeNumber}_${System.currentTimeMillis()}.docx"
        val templateFile = File(templatePath)
        val outputFile = File(outputPath, fileName)

        // 3. Ejecutar motor de ContentControls
        contentControlService.fillTemplate(
            templateFile,
            outputFile,
            data.toMap()
        )

        return outputFile.absolutePath
    }
}
