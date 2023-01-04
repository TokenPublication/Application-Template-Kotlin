package com.tokeninc.sardis.application_template.helpers.printHelpers

import android.content.Context
import com.token.printerlib.PrinterDefinitions
import com.token.printerlib.StyledString
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.SampleReceipt

open class BasePrintHelper {

    fun addText(styledText: StyledString, text: String?, alignment: PrinterDefinitions.Alignment?) {
        addText(styledText, text, alignment, 11, 0f)
    }

    fun addText(styledText: StyledString, text: String?, alignment: PrinterDefinitions.Alignment?,
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

    fun printSlipHeader(styledText: StyledString, mainActivity: MainActivity) {
        styledText.setLineSpacing(0.5f)
        styledText.setFontSize(12)
        styledText.setFontFace(PrinterDefinitions.Font_E.SourceSansPro)
        styledText.addTextToLine(mainActivity.getString(R.string.merchant_name), PrinterDefinitions.Alignment.Center)
        styledText.newLine()
        styledText.newLine()
        styledText.newLine()
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Semi_Bold)
        styledText.addTextToLine("İŞYERİ NO:", PrinterDefinitions.Alignment.Left)
        styledText.setFontFace(PrinterDefinitions.Font_E.SourceSansPro)
        styledText.addTextToLine(mainActivity.activationViewModel.getMerchantId(), PrinterDefinitions.Alignment.Right)
        styledText.newLine()
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Semi_Bold)
        styledText.addTextToLine("TERMİNAL NO:", PrinterDefinitions.Alignment.Left)
        styledText.setFontFace(PrinterDefinitions.Font_E.SourceSansPro)
        styledText.addTextToLine(mainActivity.activationViewModel.getTerminalId(), PrinterDefinitions.Alignment.Right)
    }

}