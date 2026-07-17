package com.naf.erp.training.document

import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.springframework.stereotype.Service

import org.docx4j.TraversalUtil
import org.docx4j.finders.ClassFinder
import org.docx4j.jaxb.Context
import org.docx4j.wml.SdtElement

@Service
class ContentControlEngine {

    fun fill(
        word: WordprocessingMLPackage,
        values: Map<String, String>
    ) {
        val finder = ClassFinder(SdtElement::class.java)

        TraversalUtil(
            word.mainDocumentPart.content,
            finder
        )

        finder.results.forEach {
            val control = it as SdtElement
            val tag = control.sdtPr?.tag?.`val`
            
            if (tag != null && values.containsKey(tag)) {
                setText(
                    control,
                    values[tag]!!
                )
            }
        }
    }

    private fun setText(control: SdtElement, text: String) {
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
