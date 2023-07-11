package com.tokeninc.sardis.application_template.utils.printHelpers

import com.token.printerlib.PrinterDefinitions
import com.token.printerlib.StyledString
import com.tokeninc.deviceinfo.DeviceInfo
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.enums.CardReadType
import com.tokeninc.sardis.application_template.utils.StringHelper

/**
 * This class constructs Batch Close slip.
 */
class BatchClosePrintHelper(): BasePrintHelper() {


    fun batchText(batch_no: String, transactions: List<Transaction?>, mainActivity: MainActivity, isCopy: Boolean): String {
        val styledText = StyledString()
        val stringHelper = StringHelper()
        val printHelper = PrintHelper()
        var totalAmount = 0
        val MID = mainActivity.currentMID
        val TID = mainActivity.currentTID
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
            addTextToNewLine(styledText,it!!.Col_TranDate,PrinterDefinitions.Alignment.Left)
            var transactionType = ""
            when(it.Col_TransCode){
                1 -> transactionType = "SATIŞ "
                4 -> transactionType = "E. İADE "
                5 -> transactionType = "N. İADE "
                6 -> transactionType = "T. İADE "
            }
            if (it.Col_IsVoid == 1){
                transactionType = "İPTAL "
            }
            addText(styledText,transactionType+it.Col_GUP_SN, PrinterDefinitions.Alignment.Right)
            if (it.Col_CardReadType != CardReadType.QrPay.type) {
                addTextToNewLine(styledText,stringHelper.maskTheCardNo(it.Col_PAN),PrinterDefinitions.Alignment.Left)
                addText(styledText,it.Col_ExpDate,PrinterDefinitions.Alignment.Right)
            } else {
                addTextToNewLine(styledText, stringHelper.maskTheCardNo("5209305830592013"), PrinterDefinitions.Alignment.Left);
                addText(styledText, "290925", PrinterDefinitions.Alignment.Right);
            }
            addTextToNewLine(styledText,it.Col_HostLogKey,PrinterDefinitions.Alignment.Left)
            val amount = it.Col_Amount
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
        styledText.printLogo(mainActivity)
        styledText.addSpace(50)
        printHelper.printBatchClose(styledText,batch_no, transactions.size.toString(),totalAmount, MID, TID)!!
        addTextToNewLine(styledText,"BU BELGEYİ SAKLAYINIZ",PrinterDefinitions.Alignment.Center,8)
        styledText.newLine()
        styledText.printLogo(mainActivity)
        styledText.addSpace(50)
        return styledText.toString()
    }

}