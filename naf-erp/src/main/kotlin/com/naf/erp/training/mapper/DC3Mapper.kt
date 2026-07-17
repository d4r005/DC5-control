package com.naf.erp.training.mapper

import com.naf.erp.training.dto.DC3Response
import com.naf.erp.training.entity.DC3

object DC3Mapper {
    fun map(dc3: DC3) =
        DC3Response(
            dc3.id,
            dc3.dc3Number,
            "${dc3.training.employee.firstName} ${dc3.training.employee.lastName}",
            dc3.training.course.name,
            dc3.generated.toString(),
            dc3.pdf ?: "",
            dc3.docx ?: ""
        )
}
