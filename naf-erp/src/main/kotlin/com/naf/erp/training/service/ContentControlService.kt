package com.naf.erp.training.service

import com.naf.erp.training.util.SdtFinder
import com.naf.erp.training.util.SdtPrHelper
import org.docx4j.TraversalUtil
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.springframework.stereotype.Service
import java.io.File

/**
 * Servicio para el llenado de plantillas Word utilizando Controles de Contenido (SDT).
 * Esta técnica es superior al reemplazo de texto simple (${VAR}) ya que protege la estructura del documento.
 */
@Service
class ContentControlService {

    fun fillTemplate(
        template: File,
        output: File,
        values: Map<String, String>
    ) {
        val word = WordprocessingMLPackage.load(template)

        // Nota: SdtFinder y SdtPrHelper deben ser implementados como utilidades de docx4j
        val finder = SdtFinder() 

        TraversalUtil(
            word.mainDocumentPart.content,
            finder
        )

        finder.sdtList.forEach { sdt ->
            val tag = sdt.sdtPr?.tag?.`val`

            if (tag != null && values.containsKey(tag)) {
                SdtPrHelper.setText(
                    sdt,
                    values[tag]!!
                )
            }
        }

        word.save(output)
    }
}
