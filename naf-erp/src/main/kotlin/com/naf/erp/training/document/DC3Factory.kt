package com.naf.erp.training.document

import com.naf.erp.training.entity.DC3
import org.springframework.stereotype.Service

@Service
class DC3Factory : DocumentFactory<DC3> {

    override fun create(data: DC3): DocumentTemplate {
        val template = DocumentTemplate(
            "DC3",
            "templates/DC3.docx"
        )

        template.fields["trabajador"] =
            data.training.employee.lastName +
                    " " +
                    data.training.employee.middleName +
                    " " +
                    data.training.employee.firstName

        template.fields["curp"] = data.training.employee.curp
        template.fields["curso"] = data.training.course.name
        template.fields["horas"] = data.training.course.duration.toString()
        template.fields["instructor"] = data.training.instructor.fullName
        template.fields["registro"] = data.training.instructor.stpsNumber ?: ""
        template.fields["empresa"] = "NORTH AMERICA FLOORING"
        template.fields["representantePatron"] = "JINSONG ZHAO"
        template.fields["representanteTrabajadores"] = "LILIANA ROSALES"

        return template
    }
}
