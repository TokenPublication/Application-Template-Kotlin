package com.tokeninc.sardis.application_template.helpers.printHelpers

import com.token.printerlib.PrinterDefinitions
import com.token.printerlib.StyledString
import com.tokeninc.sardis.application_template.helpers.StringHelper

/**
 * This class is for printing some default prints
 */
class PrintHelper: BasePrintHelper() {

    val dateUtil = DateUtil()

    fun PrintSuccess(): String? {   // Print the success message
        val styledText = StyledString()

        // Strings must be max 29 digits
        addTextToNewLine(styledText, "Banka uygulaması", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "başarıyla kurulmuştur", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "kullanımı için", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "https://www.tokeninc.com/", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "adresini ziyaret edin", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "-----------------------------", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "Banka uygulaması sorularınız", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "için, Banka Pos", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "Destek Hattı 0850 000 0 000", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "-----------------------------", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "YazarkasaPos sorularınız için", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "Beko YazarkasaPos", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "Çözüm Merkezi", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "0850 250 0 767", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, dateUtil.getDate("dd-MM-yy"),
            PrinterDefinitions.Alignment.Left
        )
        addText(styledText, dateUtil.getTime("HH:mm"), PrinterDefinitions.Alignment.Right)
        styledText.newLine()
        styledText.addSpace(100)
        return styledText.toString()
    }

    fun PrintError(): String? {   // Print the error message if necessary
        val styledText = StyledString()
        addTextToNewLine(
            styledText,
            "Uygulama kurulumunda hata",
            PrinterDefinitions.Alignment.Center
        )
        addTextToNewLine(styledText, "ile karşılaşılmıştır", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(
            styledText,
            "Lütfen Beko YazarkasaPos",
            PrinterDefinitions.Alignment.Center
        )
        addTextToNewLine(styledText, "Çözüm Merkezi'ni arayın", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "0850 250 0 767", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(
            styledText,
            dateUtil.getDate("dd-MM-yy"),
            PrinterDefinitions.Alignment.Left
        )
        addText(styledText, dateUtil.getTime("HH:mm"), PrinterDefinitions.Alignment.Right)
        styledText.newLine()
        styledText.addSpace(100)
        return styledText.toString()
    }

    fun PrintBatchClose(styledText: StyledString,batch_no: String, tx_no: String, totalAmount: Int, MID: String?, TID: String?): String? {
        addTextToNewLine(styledText, "TOKEN", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "FINTECH", PrinterDefinitions.Alignment.Center)
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Bold)
        addTextToNewLine(styledText, "İŞYERİ NO: ", PrinterDefinitions.Alignment.Left)
        addText(styledText, MID, PrinterDefinitions.Alignment.Right)
        addTextToNewLine(styledText, "TERMİNAL NO: ", PrinterDefinitions.Alignment.Left)
        addText(styledText, TID, PrinterDefinitions.Alignment.Right)
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Semi_Bold)
        if (batch_no == "") {
            addTextToNewLine(styledText, "Grup Yok", PrinterDefinitions.Alignment.Center)
        } else {
            addTextToNewLine(styledText, "GRUP NO: ", PrinterDefinitions.Alignment.Center)
            addText(styledText, batch_no, PrinterDefinitions.Alignment.Right)
        }
        addTextToNewLine(
            styledText,
            dateUtil.getDate("dd/MM/yy"),
            PrinterDefinitions.Alignment.Left
        )
        addText(styledText, dateUtil.getTime("HH:mm"), PrinterDefinitions.Alignment.Right)
        if (tx_no == "" || tx_no == "null" || tx_no == "0") {
            addTextToNewLine(styledText, "İşlem Yok", PrinterDefinitions.Alignment.Center)
        } else {
            addTextToNewLine(styledText, "İşlem Sayısı: ", PrinterDefinitions.Alignment.Left)
            addText(styledText, tx_no, PrinterDefinitions.Alignment.Right)
        }
        addTextToNewLine(styledText,"TOPLAM:",PrinterDefinitions.Alignment.Left)
        addText(styledText,StringHelper().getAmount(totalAmount),PrinterDefinitions.Alignment.Right)
        addTextToNewLine(styledText, " ", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "Grup Kapama Başarılı", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, " ", PrinterDefinitions.Alignment.Center)
        styledText.newLine()
        return styledText.toString()
    }

}