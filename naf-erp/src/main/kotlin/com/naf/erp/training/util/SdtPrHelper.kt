package com.naf.erp.training.util

import org.docx4j.jaxb.Context
import org.docx4j.wml.SdtElement

object SdtPrHelper {
    fun setText(control: SdtElement, text: String) {
        val content = control.sdtContent
        content.content.clear()

        val factory = Context.getWmlObjectFactory()
        val run = factory.createR()
        val t = factory.createText()
        t.value = text

        run.content.add(t)
        content.content.add(run)
    }
}
