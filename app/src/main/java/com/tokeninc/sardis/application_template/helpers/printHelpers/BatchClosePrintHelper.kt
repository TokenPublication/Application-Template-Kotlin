package com.tokeninc.sardis.application_template.helpers.printHelpers

import android.content.ContentValues
import com.token.printerlib.PrinterDefinitions
import com.token.printerlib.StyledString
import com.tokeninc.deviceinfo.DeviceInfo
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.database.transaction.TransactionCol
import com.tokeninc.sardis.application_template.enums.SlipType
import com.tokeninc.sardis.application_template.helpers.StringHelper

class BatchClosePrintHelper(): BasePrintHelper() {

    fun batchText(batch_no: String, transactions: List<ContentValues?>, mainActivity: MainActivity, isCopy: Boolean): String {
        var styledText = StyledString()
        val stringHelper = StringHelper()
        val printHelper = PrintHelper()
        var totalAmount = 0
        val MID = mainActivity.activationViewModel.getMerchantId()
        val TID = mainActivity.activationViewModel.getTerminalId()
        addTextToNewLine(styledText, "TOKEN", PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText, "FINTECH", PrinterDefinitions.Alignment.Center)
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Bold)
        addTextToNewLine(styledText, "İŞYERİ NO: ", PrinterDefinitions.Alignment.Left)
        addText(styledText, MID, PrinterDefinitions.Alignment.Right)
        addTextToNewLine(styledText, "TERMİNAL NO: ", PrinterDefinitions.Alignment.Left)
        addText(styledText, TID, PrinterDefinitions.Alignment.Right)
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Semi_Bold)
        if (isCopy){
            addTextToNewLine(styledText,"İKİNCİ KOPYA",PrinterDefinitions.Alignment.Center,12)
        }
        addTextToNewLine(styledText,"Ver. :", PrinterDefinitions.Alignment.Left)
        addText(styledText,"${DeviceInfo.Field.LYNX_VERSION}", PrinterDefinitions.Alignment.Right)
        addTextToNewLine(styledText,"DETAY",PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText,"İŞLEMLER LİSTESİ",PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText,"===========================",PrinterDefinitions.Alignment.Center)
        addTextToNewLine(styledText,"PEŞİN İŞLEMLER",PrinterDefinitions.Alignment.Left)
        transactions.forEach {
            addTextToNewLine(styledText,it!!.getAsString(TransactionCol.Col_TranDate.name),PrinterDefinitions.Alignment.Left)
            addText(styledText,"SATIŞ "+it.getAsString(TransactionCol.Col_GUP_SN.name), PrinterDefinitions.Alignment.Right)
            addTextToNewLine(styledText,stringHelper.MaskTheCardNo(it.getAsString(TransactionCol.Col_PAN.name)),PrinterDefinitions.Alignment.Left)
            addText(styledText,it.getAsString(TransactionCol.Col_ExpDate.name),PrinterDefinitions.Alignment.Right)
            addTextToNewLine(styledText,it.getAsString(TransactionCol.Col_HostLogKey.name),PrinterDefinitions.Alignment.Left)
            val amount = it.getAsString(TransactionCol.Col_Amount.name).toInt()
            totalAmount += amount
            addText(styledText,stringHelper.getAmount(amount),PrinterDefinitions.Alignment.Right)
            styledText.newLine()
        }
        addTextToNewLine(styledText,"İŞLEM SAYISI:",PrinterDefinitions.Alignment.Left)
        addText(styledText,transactions.size.toString(),PrinterDefinitions.Alignment.Right)
        addTextToNewLine(styledText,"TOPLAM:",PrinterDefinitions.Alignment.Left)
        addText(styledText,stringHelper.getAmount(totalAmount),PrinterDefinitions.Alignment.Right)
        styledText.newLine()
        addTextToNewLine(styledText,"===========================",PrinterDefinitions.Alignment.Center)
        styledText.newLine()
        styledText.printBitmap("ykb", 20)
        styledText.addSpace(50)
        printHelper.PrintBatchClose(styledText,batch_no, transactions.size.toString(),totalAmount, MID, TID)!!
        addTextToNewLine(styledText,"BU BELGEYİ SAKLAYINIZ",PrinterDefinitions.Alignment.Center,8)
        styledText.newLine()
        styledText.printBitmap("ykb", 20)
        styledText.addSpace(50)
        return styledText.toString()
    }

}