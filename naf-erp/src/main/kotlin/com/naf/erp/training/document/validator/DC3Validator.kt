package com.naf.erp.training.document.validator

import com.naf.erp.training.entity.DC3
import org.springframework.stereotype.Service

@Service
class DC3Validator : DocumentValidator<DC3> {

    override fun validate(data: DC3): ValidationResult {
        val result = ValidationResult(true)

        if (data.training.employee.curp.length != 18) {
            result.errors.add("CURP inválida")
        }

        if (data.training.course.duration <= 0) {
            result.errors.add("Duración inválida")
        }

        if (data.training.course.thematicArea.isBlank()) {
            result.errors.add("Área temática requerida")
        }

        if (data.training.instructor.stpsNumber.isNullOrBlank()) {
            result.errors.add("Registro STPS requerido")
        }

        return if (result.errors.isNotEmpty()) {
            result.copy(valid = false)
        } else {
            result
        }
    }

}
