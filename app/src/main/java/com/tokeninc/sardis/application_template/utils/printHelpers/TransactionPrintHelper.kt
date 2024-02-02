package com.tokeninc.sardis.application_template.utils.printHelpers

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import com.token.printerlib.PrinterDefinitions
import com.token.printerlib.StyledString
import com.tokeninc.deviceinfo.DeviceInfo
import com.tokeninc.sardis.application_template.AppTemp
import com.tokeninc.sardis.application_template.data.database.transaction.TransactionCols
import com.tokeninc.sardis.application_template.data.model.resultCode.TransactionCode
import com.tokeninc.sardis.application_template.data.model.type.CardReadType
import com.tokeninc.sardis.application_template.data.model.type.SlipType
import com.tokeninc.sardis.application_template.utils.ExtraKeys
import com.tokeninc.sardis.application_template.utils.StringHelper
import com.tokeninc.sardis.application_template.utils.objects.SampleReceipt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * This class forms slips with respect to type of Transaction, it can arrange slip for
 * Sale, Void and all types of refund.
 */
class TransactionPrintHelper:BasePrintHelper() {
    fun getFormattedText(receipt: SampleReceipt, slipType: SlipType, contentValues: ContentValues,
                         transactionCode: Int, context: Context, ZNO: String?, ReceiptNo: Int?, isCopy: Boolean): String {
        val styledText = StyledString()
        val stringHelper = StringHelper()

        if (slipType === SlipType.CARDHOLDER_SLIP) {
            if ((context.applicationContext as AppTemp).getCurrentDeviceMode() == DeviceInfo.PosModeEnum.GIB.name)  {
                printSlipHeader(styledText, receipt)
            }
        } else {
            printSlipHeader(styledText, receipt)
        }

        if (isCopy) {
            addTextToNewLine(styledText, "İKİNCİ KOPYA", PrinterDefinitions.Alignment.Center)
        }

        styledText.newLine()
        if (slipType === SlipType.CARDHOLDER_SLIP) {
            styledText.addTextToLine("MÜŞTERİ NÜSHASI", PrinterDefinitions.Alignment.Center)
            styledText.newLine()
        } else if (slipType === SlipType.MERCHANT_SLIP) {
            styledText.addTextToLine("İŞYERİ NÜSHASI", PrinterDefinitions.Alignment.Center)
            styledText.newLine()
        }

        if (transactionCode == TransactionCode.VOID.type){
            var transactionType = ""
            when(receipt.transactionCode){
                1 -> transactionType = "SATIŞ İPTALİ"
                2 -> transactionType = "TAKSİTLİ SATIŞ"
                3 -> transactionType = "İPTAL İŞLEMİ"
                4 -> transactionType = "E. İADE İPTALİ"
                5 -> transactionType = "N. İADE İPTALİ"
                6 -> transactionType = "T. İADE İPTALİ"
            }
            styledText.addTextToLine(transactionType, PrinterDefinitions.Alignment.Center)
        }
        when (transactionCode) {
            TransactionCode.SALE.type -> styledText.addTextToLine("SATIŞ", PrinterDefinitions.Alignment.Center)
            TransactionCode.MATCHED_REFUND.type -> styledText.addTextToLine("E. İADE", PrinterDefinitions.Alignment.Center)
            TransactionCode.INSTALLMENT_REFUND.type -> {
                styledText.addTextToLine("T. SATIŞ İADE", PrinterDefinitions.Alignment.Center)
                styledText.newLine()
                styledText.addTextToLine("${contentValues.getAsString(TransactionCols.Col_InstCnt)} TAKSİT", PrinterDefinitions.Alignment.Center)
            }
            TransactionCode.INSTALLMENT_SALE.type -> {
                styledText.addTextToLine("T. SATIŞ", PrinterDefinitions.Alignment.Center)
                styledText.newLine()
                styledText.addTextToLine("${contentValues.getAsString(TransactionCols.Col_InstCnt)} TAKSİT", PrinterDefinitions.Alignment.Center)
            }
            TransactionCode.CASH_REFUND.type -> styledText.addTextToLine("NAKİT İADE", PrinterDefinitions.Alignment.Center)
        }

        val sdf: SimpleDateFormat
        = if (slipType === SlipType.CARDHOLDER_SLIP && (context.applicationContext as AppTemp).getCurrentDeviceMode() == (DeviceInfo.PosModeEnum.GIB.name)
            || slipType === SlipType.MERCHANT_SLIP) {
            SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.getDefault())
        } else{
            SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        }
        val dateTime = sdf.format(Calendar.getInstance().time)
        var lineTime = dateTime

        if (slipType == SlipType.CARDHOLDER_SLIP){
            lineTime += if (receipt.isOffline == 1) " C OFFLINE" else " C ONLINE"
        } else if (slipType == SlipType.MERCHANT_SLIP){
            lineTime += if (receipt.isOffline == 1) " M OFFLINE" else " M ONLINE"
        }

        styledText.newLine()
        styledText.addTextToLine(lineTime, PrinterDefinitions.Alignment.Center )
        styledText.newLine()
        styledText.addTextToLine(receipt.cardNo, PrinterDefinitions.Alignment.Center)
        styledText.newLine()

        if (receipt.fullName != null) {
            styledText.newLine()
            styledText.addTextToLine(receipt.fullName, PrinterDefinitions.Alignment.Center)
        }

        if (transactionCode == TransactionCode.VOID.type && !receipt.tranDate.isNullOrEmpty()) {
            styledText.newLine()
            styledText.addTextToLine(DateUtil().getFormattedDate(receipt.tranDate!!.substring(0, 8)))
            styledText.addTextToLine(DateUtil().getFormattedTime(receipt.tranDate!!.substring(8)), PrinterDefinitions.Alignment.Right)
        }

        styledText.setLineSpacing(1f)
        styledText.setFontSize(14)
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Semi_Bold)
        styledText.newLine()
        styledText.addTextToLine("TUTAR:")

        if (transactionCode == TransactionCode.MATCHED_REFUND.type || transactionCode == TransactionCode.INSTALLMENT_REFUND.type || transactionCode == TransactionCode.CASH_REFUND.type){
            styledText.addTextToLine(stringHelper.getAmount(contentValues.getAsInteger(TransactionCols.Col_Amount2)), PrinterDefinitions.Alignment.Right)
        }
        else{
            styledText.addTextToLine(receipt.amount, PrinterDefinitions.Alignment.Right)
        }

        if (transactionCode == TransactionCode.INSTALLMENT_SALE.type) {
            styledText.setFontSize(11)
            styledText.newLine()
            val transInsCnt = contentValues.getAsInteger(TransactionCols.Col_InstCnt)
            styledText.addTextToLine(transInsCnt.toString() + " x " + StringHelper().getInstAmount(contentValues.getAsInteger(TransactionCols.Col_Amount) / transInsCnt ), PrinterDefinitions.Alignment.Center)
        }

        styledText.setLineSpacing(0.5f)
        styledText.setFontSize(10)
        styledText.newLine()

        if (transactionCode == TransactionCode.VOID.type){
            styledText.addTextToLine("İPTAL EDİLMİŞTİR", PrinterDefinitions.Alignment.Center)
            styledText.newLine()
            styledText.addTextToLine("===========================",PrinterDefinitions.Alignment.Center)
            styledText.newLine()
            styledText.addTextToLine("İŞLEM TEMASSIZ TAMAMLANMIŞTIR", PrinterDefinitions.Alignment.Center)
            styledText.newLine()
            styledText.addTextToLine("MASTERCARD CONTACTLESS", PrinterDefinitions.Alignment.Center)
            if (slipType === SlipType.CARDHOLDER_SLIP) {
                styledText.newLine()
                val signature = "İŞ YERİ İMZA: _ _ _ _ _ _ _ _ _ _ _ _ _ _"
                styledText.addTextToLine(signature,PrinterDefinitions.Alignment.Center)
                styledText.newLine()
                styledText.addTextToLine("===========================",PrinterDefinitions.Alignment.Center)
            }
        }

        else if (transactionCode == TransactionCode.MATCHED_REFUND.type || transactionCode == TransactionCode.INSTALLMENT_REFUND.type || transactionCode == TransactionCode.CASH_REFUND.type){
            styledText.addTextToLine("MAL/HİZM İADE EDİLMİŞTİR", PrinterDefinitions.Alignment.Center)
            styledText.newLine()
            if (transactionCode == TransactionCode.CASH_REFUND.type){
                styledText.addTextToLine("ORJ. İŞLEM TARİHİ: " + DateUtil().getCashRefundDate(receipt.tranDate!!), PrinterDefinitions.Alignment.Center)
            } else{
                styledText.addTextToLine("ORJ. İŞLEM TARİHİ: ${contentValues.getAsString(TransactionCols.Col_Ext_RefundDateTime)}",PrinterDefinitions.Alignment.Center)
            }
            styledText.newLine()
            styledText.addTextToLine("ORJ. İŞ YERİ NO: ${receipt.merchantID }",PrinterDefinitions.Alignment.Center)
            styledText.newLine()
            styledText.addTextToLine("İŞLEM TEMASSIZ TAMAMLANMIŞTIR", PrinterDefinitions.Alignment.Center)
            styledText.newLine()
            styledText.addTextToLine("MASTERCARD CONTACTLESS", PrinterDefinitions.Alignment.Center)
            if (slipType === SlipType.CARDHOLDER_SLIP) {
                styledText.newLine()
                val signature = "İŞ YERİ İMZA: _ _ _ _ _ _ _ _ _ _ _ _ _ _"
                styledText.addTextToLine(signature,PrinterDefinitions.Alignment.Center)
            }
        }
        if (transactionCode == TransactionCode.SALE.type || transactionCode == TransactionCode.INSTALLMENT_SALE.type){
            if (slipType === SlipType.CARDHOLDER_SLIP) {
                styledText.addTextToLine("KARŞILIĞI MAL/HİZM ALDIM", PrinterDefinitions.Alignment.Center)
            } else {
                if (receipt.cardType == CardReadType.ICC.type || receipt.cardType == CardReadType.MSR.type || receipt.cardType == CardReadType.ICC2MSR.type){
                    styledText.addTextToLine("İşlem Şifre Girilerek Yapılmıştır", PrinterDefinitions.Alignment.Center)
                    styledText.newLine()
                }
                styledText.addTextToLine("İMZAYA GEREK YOKTUR", PrinterDefinitions.Alignment.Center)
            }
        }

        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Bold)
        styledText.setFontSize(12)
        styledText.newLine()
        styledText.addTextToLine("SN: " + receipt.groupSerialNo)
        styledText.addTextToLine("ONAY KODU: " + receipt.authCode, PrinterDefinitions.Alignment.Right)

        styledText.setFontSize(8)
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Semi_Bold)
        styledText.newLine()

        styledText.addTextToLine("GRUP NO: " + receipt.batchNo)
        styledText.addTextToLine("REF NO: " + receipt.refNo, PrinterDefinitions.Alignment.Right)
        styledText.newLine()

        if (!receipt.aid.isNullOrEmpty()){
            styledText.addTextToLine("AID: " + receipt.aid)
        }
        if (!receipt.aidLabel.isNullOrEmpty()){
            styledText.addTextToLine(receipt.aidLabel,PrinterDefinitions.Alignment.Right)
        }
        styledText.newLine()
        styledText.addTextToLine("Ver: 92.12.05")

        if (transactionCode == TransactionCode.SALE.type || transactionCode == TransactionCode.INSTALLMENT_SALE.type){
            if (slipType === SlipType.MERCHANT_SLIP) {
                if ((context.applicationContext as AppTemp).getCurrentDeviceMode() == DeviceInfo.PosModeEnum.ECR.name) {
                    if ((context.applicationContext as AppTemp).getCurrentDeviceMode() == DeviceInfo.PosModeEnum.VUK507.name){
                        addTextToNewLine(styledText, "*MALİ DEĞERİ YOKTUR*", PrinterDefinitions.Alignment.Center, 8)
                    }
                    styledText.newLine()
                    styledText.addTextToLine("Z NO: ${ZNO ?: 1}", PrinterDefinitions.Alignment.Right) //if null return 1 else return itself
                    styledText.addTextToLine("FİŞ NO: ${ReceiptNo ?: 1}", PrinterDefinitions.Alignment.Left)
                    if ((context.applicationContext as AppTemp).getCurrentDeviceMode() == DeviceInfo.PosModeEnum.VUK507.name){
                        addTextToNewLine(styledText, (context.applicationContext as AppTemp).getCurrentFiscalID(), PrinterDefinitions.Alignment.Center, 8)
                    }
                }
            }
        }
        styledText.newLine()
        addTextToNewLine(styledText,"BU İŞLEM YURT İÇİ KARTLA YAPILMIŞTIR", PrinterDefinitions.Alignment.Center, 8)
        styledText.newLine()
        addTextToNewLine(styledText,"BU BELGEYİ SAKLAYINIZ", PrinterDefinitions.Alignment.Center, 8)
        styledText.newLine()
        styledText.printLogo(context)
        styledText.addSpace(100)
        return styledText.toString()
    }
}
