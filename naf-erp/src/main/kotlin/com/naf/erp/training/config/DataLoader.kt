package com.naf.erp.training.config

import com.naf.erp.training.entity.*
import com.naf.erp.training.repository.*
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DataLoader(
    private val employeeRepository: EmployeeRepository,
    private val instructorRepository: InstructorRepository,
    private val courseRepository: CourseRepository,
    private val trainingRepository: TrainingRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (employeeRepository.count() == 0L) {
            val employee = Employee(
                firstName = "JUAN",
                lastName = "PEREZ",
                middleName = "GARCIA",
                curp = "PERG800101HDFRRN01",
                rfc = "PERG800101XXX",
                employeeNumber = "EMP001",
                position = "OPERADOR",
                department = "ALMACEN"
            )
            employeeRepository.save(employee)
        }

        if (instructorRepository.count() == 0L) {
            val instructor = Instructor(
                fullName = "DARIO ROBLES",
                stpsNumber = "ROBD800101XXX-001",
                external = false
            )
            instructorRepository.save(instructor)
        }

        if (courseRepository.count() == 0L) {
            val courses = listOf(
                Course(name = "MANEJO SEGURO DE MONTACARGAS", duration = 8, thematicArea = "6000", occupationKey = "1"),
                Course(name = "TRABAJOS EN ALTURA (NOM-009)", duration = 4, thematicArea = "6000", occupationKey = "1"),
                Course(name = "SEGURIDAD EN SOLDADURA", duration = 6, thematicArea = "6000", occupationKey = "1"),
                Course(name = "FORMACION DE BRIGADAS", duration = 12, thematicArea = "6000", occupationKey = "1"),
                Course(name = "ESPACIOS CONFINADOS", duration = 8, thematicArea = "6000", occupationKey = "1"),
                Course(name = "BLOQUEO Y ETIQUETADO LOTO", duration = 4, thematicArea = "6000", occupationKey = "1")
            )
            courseRepository.saveAll(courses)
        }
    }
}
