package com.naf.erp.training.document

import com.naf.erp.training.entity.DC3
import org.springframework.stereotype.Service

@Service
class DocumentFactory {

    fun createDC3(dc3: DC3): DocumentTemplate {
        val template = DocumentTemplate(
            name = "DC3",
            file = "templates/DC3_Oficial.docx"
        )
        
        template.fields["trabajador"] =
            dc3.training.employee.lastName +
                    " " +
                    dc3.training.employee.middleName +
                    " " +
                    dc3.training.employee.firstName

        template.fields["curp"] = dc3.training.employee.curp
        template.fields["horas"] = dc3.training.course.duration.toString()
        template.fields["instructor"] = dc3.training.instructor.fullName
        template.fields["registro"] = dc3.training.instructor.stpsNumber ?: ""
        template.fields["empresa"] = "NORTH AMERICA FLOORING"
        template.fields["representantePatron"] = "JINSONG ZHAO"
        template.fields["representanteTrabajadores"] = "LILIANA ROSALES"

        return template
    }
}
