package com.tokeninc.sardis.application_template.utils.printHelpers

import com.token.printerlib.PrinterDefinitions
import com.token.printerlib.StyledString
import com.tokeninc.sardis.application_template.utils.objects.SampleReceipt

/**
 * This class includes styledText methods.
 */
open class BasePrintHelper {
    fun addText(styledText: StyledString, text: String?, alignment: PrinterDefinitions.Alignment?) {
        addText(styledText, text, alignment, 11, 0f)
    }

    private fun addText(styledText: StyledString, text: String?, alignment: PrinterDefinitions.Alignment?,
                        fontSize: Int, lineSpacing: Float
    ) {
        styledText.setFontFace(PrinterDefinitions.Font_E.SourceCodePro)
        styledText.setFontSize(fontSize)
        styledText.setLineSpacing(lineSpacing)
        styledText.addTextToLine(text, alignment)
    }

    fun addTextToNewLine(styledText: StyledString, text: String?, alignment: PrinterDefinitions.Alignment?
    ) {
        styledText.newLine()
        addText(styledText, text, alignment)
    }

    fun addTextToNewLine(styledText: StyledString, text: String?,
        alignment: PrinterDefinitions.Alignment?, fontSize: Int
    ) {
        styledText.newLine()
        addText(styledText, text, alignment, fontSize, 0f)
    }

    fun printSlipHeader(styledText: StyledString, receipt: SampleReceipt) {
        styledText.setLineSpacing(0.5f)
        styledText.setFontSize(12)
        styledText.setFontFace(PrinterDefinitions.Font_E.SourceSansPro)
        styledText.addTextToLine(receipt.merchantName, PrinterDefinitions.Alignment.Center)
        styledText.newLine()
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Semi_Bold)
        styledText.addTextToLine("İŞYERİ NO:", PrinterDefinitions.Alignment.Left)
        styledText.setFontFace(PrinterDefinitions.Font_E.SourceSansPro)
        styledText.addTextToLine(receipt.merchantID, PrinterDefinitions.Alignment.Right)
        styledText.newLine()
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Semi_Bold)
        styledText.addTextToLine("TERMİNAL NO:", PrinterDefinitions.Alignment.Left)
        styledText.setFontFace(PrinterDefinitions.Font_E.SourceSansPro)
        styledText.addTextToLine(receipt.terminalID, PrinterDefinitions.Alignment.Right)
    }
}
