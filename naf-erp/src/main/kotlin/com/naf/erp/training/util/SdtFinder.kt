package com.naf.erp.training.util

import org.docx4j.wml.SdtRun
import org.docx4j.wml.SdtElement
import org.docx4j.utils.TraversalUtilVisitor

class SdtFinder : TraversalUtilVisitor<SdtElement>() {
    val sdtList = mutableListOf<SdtElement>()

    override fun apply(element: SdtElement) {
        sdtList.add(element)
    }
}
