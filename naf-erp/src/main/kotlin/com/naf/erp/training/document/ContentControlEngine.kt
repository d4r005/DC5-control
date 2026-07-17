package com.naf.erp.training.document

import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.springframework.stereotype.Service

import org.docx4j.TraversalUtil
import org.docx4j.finders.ClassFinder
import org.docx4j.jaxb.Context
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
import org.docx4j.wml.SdtElement
import java.io.File

@Service
class ContentControlEngine {

    fun fill(
        word: WordprocessingMLPackage,
        values: Map<String, String>,
        images: Map<String, String> = emptyMap()
    ) {
        val finder = ClassFinder(SdtElement::class.java)

        TraversalUtil(
            word.mainDocumentPart.content,
            finder
        )

        finder.results.forEach {
            val control = it as SdtElement
            val tag = control.sdtPr?.tag?.`val`
            
            if (tag != null) {
                if (values.containsKey(tag)) {
                    setText(control, values[tag]!!)
                } else if (images.containsKey(tag)) {
                    setImage(control, word, images[tag]!!)
                }
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

    private fun setImage(control: SdtElement, word: WordprocessingMLPackage, imagePath: String) {
        val file = File(imagePath)
        if (!file.exists()) return

        val imageBytes = file.readBytes()
        val imagePart = BinaryPartAbstractImage.createImagePart(word, imageBytes)
        val inline = imagePart.createImageInline(
            "Signature", 
            "Electronic Signature", 
            1, 
            2, 
            false
        )

        val factory = Context.getWmlObjectFactory()
        val run = factory.createR()
        val drawing = factory.createDrawing()
        drawing.anchorOrInline.add(inline)
        run.content.add(drawing)

        val content = control.sdtContent
        content.content.clear()
        content.content.add(run)
    }
}
