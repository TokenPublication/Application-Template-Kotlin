package com.tokeninc.sardis.application_template.helpers.printHelpers

import android.content.ContentValues
import android.content.Context
import com.token.printerlib.PrinterDefinitions
import com.token.printerlib.StyledString
import com.tokeninc.deviceinfo.DeviceInfo
import com.tokeninc.sardis.application_template.AppTemp
import com.tokeninc.sardis.application_template.ui.MainActivity
import com.tokeninc.sardis.application_template.responses.OnlineTransactionResponse
import com.tokeninc.sardis.application_template.database.transaction.TransactionCol
import com.tokeninc.sardis.application_template.enums.ExtraKeys
import com.tokeninc.sardis.application_template.enums.SlipType
import com.tokeninc.sardis.application_template.enums.TransactionCode
import com.tokeninc.sardis.application_template.helpers.StringHelper
import java.text.SimpleDateFormat
import java.util.*

class PrintService:BasePrintHelper() {

    //TODO installment ve cash refunda göre de düzenleme yap
    fun getFormattedText(slipType: SlipType, contentValues: ContentValues, extraContentValues: ContentValues?, onlineTransactionResponse: OnlineTransactionResponse,
                         transactionCode: Int, context: Context, ZNO: Int, ReceiptNo: Int, isCopy: Boolean): String {
        val styledText = StyledString()
        val stringHelper = StringHelper()
        val mainActivity = context as MainActivity
        if (transactionCode == TransactionCode.SALE.type){
            if (slipType === SlipType.CARDHOLDER_SLIP) {
                if (!(context.applicationContext as AppTemp).getCurrentDeviceMode().equals(DeviceInfo.PosModeEnum.ECR.name) &&
                    !(context.applicationContext as AppTemp).getCurrentDeviceMode().equals(DeviceInfo.PosModeEnum.VUK507.name))  {
                    printSlipHeader(styledText, context)
                }
            } else {
                printSlipHeader(styledText, context)
            }
        }
        else
            printSlipHeader(styledText, context)
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
            when(contentValues.getAsString(TransactionCol.Col_TransCode.name).toInt()){
                1 -> transactionType = "SATIŞ İPTALİ"
                3 -> transactionType = "İPTAL İŞLEMİ"
                4 -> transactionType = "E. İADE İPTALİ"
                5 -> transactionType = "N. İADE İPTALİ"
                6 -> transactionType = "T. İADE İPTALİ"
            }
            styledText.addTextToLine(transactionType, PrinterDefinitions.Alignment.Center)
        }
        if (transactionCode == TransactionCode.SALE.type)
            styledText.addTextToLine("SATIŞ", PrinterDefinitions.Alignment.Center)
        if (transactionCode == TransactionCode.MATCHED_REFUND.type)
            styledText.addTextToLine("E. İADE", PrinterDefinitions.Alignment.Center)
        if (transactionCode == TransactionCode.INSTALLMENT_REFUND.type){
            styledText.addTextToLine("T. SATIŞ İADE", PrinterDefinitions.Alignment.Center)
            styledText.newLine()
            styledText.addTextToLine("${contentValues.getAsString(TransactionCol.Col_InstCnt.name)} TAKSİT", PrinterDefinitions.Alignment.Center)
        }
        if (transactionCode == TransactionCode.CASH_REFUND.type)
            styledText.addTextToLine("NAKİT İADE", PrinterDefinitions.Alignment.Center)
        val sdf = SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.getDefault())
        val dateTime = sdf.format(Calendar.getInstance().time)
        var lineTime = ""
        if (transactionCode == TransactionCode.VOID.type)
            lineTime = "$dateTime M OFFLINE"
        if (transactionCode == TransactionCode.SALE.type)
            lineTime = "$dateTime C ONLINE"
        if (transactionCode == TransactionCode.MATCHED_REFUND.type)
            lineTime = "$dateTime M ONLINE"
        styledText.newLine()
        styledText.addTextToLine(lineTime, PrinterDefinitions.Alignment.Center )
        styledText.newLine()
        styledText.addTextToLine(stringHelper.maskCardNumber(contentValues.getAsString(TransactionCol.Col_PAN.name)), PrinterDefinitions.Alignment.Center)
        styledText.newLine()
        val ddMMyy = SimpleDateFormat("dd-MM-yy", Locale.getDefault())
        val date = ddMMyy.format(Calendar.getInstance().time)
        val hhmmss = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val time = hhmmss.format(Calendar.getInstance().time)
        styledText.addTextToLine(date)
        styledText.addTextToLine(time, PrinterDefinitions.Alignment.Right)
        styledText.setLineSpacing(1f)
        styledText.setFontSize(14)
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Semi_Bold)
        styledText.newLine()
        styledText.addTextToLine("TUTAR:")
        if (transactionCode == TransactionCode.MATCHED_REFUND.type || transactionCode == TransactionCode.INSTALLMENT_REFUND.type)
            styledText.addTextToLine(stringHelper.getAmount(extraContentValues!!.getAsString(ExtraKeys.REFUND_AMOUNT.name).toInt()), PrinterDefinitions.Alignment.Right)
        if (transactionCode == TransactionCode.CASH_REFUND.type)
            styledText.addTextToLine(stringHelper.getAmount(contentValues.getAsString(TransactionCol.Col_Amount2.name).toInt()), PrinterDefinitions.Alignment.Right)
        else
            styledText.addTextToLine(stringHelper.getAmount(contentValues.getAsString(TransactionCol.Col_Amount.name).toInt()), PrinterDefinitions.Alignment.Right)
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
        if (transactionCode == TransactionCode.MATCHED_REFUND.type || transactionCode == TransactionCode.INSTALLMENT_REFUND.type || transactionCode == TransactionCode.CASH_REFUND.type){
            styledText.addTextToLine("MAL/HİZM İADE EDİLMİŞTİR", PrinterDefinitions.Alignment.Center)
            styledText.newLine()
            styledText.addTextToLine("İŞLEM TARİHİ: ${extraContentValues!!.getAsString(ExtraKeys.TRAN_DATE.name)}",PrinterDefinitions.Alignment.Center)
            styledText.newLine()
            styledText.addTextToLine("ORJ. İŞ YERİ NO: ${mainActivity.activationViewModel.getMerchantId() }",PrinterDefinitions.Alignment.Center)
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
        if (transactionCode == TransactionCode.SALE.type){
            if (slipType === SlipType.CARDHOLDER_SLIP) {
                styledText.addTextToLine(
                    "KARŞILIĞI MAL/HİZM ALDIM",
                    PrinterDefinitions.Alignment.Center
                )
            } else {
                styledText.addTextToLine(
                    "İşlem Şifre Girilerek Yapılmıştır",
                    PrinterDefinitions.Alignment.Center
                )
                styledText.newLine()
                styledText.addTextToLine("İMZAYA GEREK YOKTUR", PrinterDefinitions.Alignment.Center)
            }
        }
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Bold)
        styledText.setFontSize(12)
        styledText.newLine()
        if (transactionCode == TransactionCode.VOID.type)
            styledText.addTextToLine("SN: " + extraContentValues!!.getAsString(TransactionCol.Col_GUP_SN.name))
        else
            styledText.addTextToLine("SN: " + contentValues.getAsString(TransactionCol.Col_GUP_SN.name))
        styledText.addTextToLine("ONAY KODU: " + contentValues.getAsString(TransactionCol.Col_AuthCode.name), PrinterDefinitions.Alignment.Right)
        styledText.setFontSize(8)
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Semi_Bold)
        styledText.newLine()
        styledText.addTextToLine("GRUP NO: " + contentValues.getAsString(TransactionCol.Col_BatchNo.name))
        styledText.addTextToLine("REF NO: " + contentValues.getAsString(TransactionCol.Col_HostLogKey.name), PrinterDefinitions.Alignment.Right)
        styledText.newLine()
        styledText.addTextToLine("AID: " + contentValues.getAsString(TransactionCol.Col_Aid.name))
        styledText.addTextToLine(contentValues.getAsString(TransactionCol.Col_AidLabel.name), PrinterDefinitions.Alignment.Right)
        styledText.newLine()
        styledText.addTextToLine("Ver: 92.12.05")
        if (transactionCode == TransactionCode.SALE.type){
            if (slipType === SlipType.MERCHANT_SLIP) {
                addTextToNewLine(
                    styledText,
                    "*MALİ DEĞERİ YOKTUR*",
                    PrinterDefinitions.Alignment.Center,
                    8
                )
            }
            if (slipType === SlipType.MERCHANT_SLIP) {
                if ((context.applicationContext as AppTemp).getCurrentDeviceMode().equals(DeviceInfo.PosModeEnum.ECR.name)
                ) {
                    styledText.newLine()
                    styledText.addTextToLine("Z NO: $ZNO", PrinterDefinitions.Alignment.Right)
                    styledText.addTextToLine("FİŞ NO: $ReceiptNo", PrinterDefinitions.Alignment.Left)
                }
            }
            if (slipType === SlipType.MERCHANT_SLIP) {
                if ((context.applicationContext as AppTemp).getCurrentDeviceMode().equals(DeviceInfo.PosModeEnum.ECR.name)
                    || (context.applicationContext as AppTemp).getCurrentDeviceMode().equals(DeviceInfo.PosModeEnum.VUK507.name)
                ) {
                    addTextToNewLine(styledText, (context.applicationContext as AppTemp).getCurrentFiscalID(),
                        PrinterDefinitions.Alignment.Center, 8)
                }
            }
        }
        addTextToNewLine(styledText,"BU İŞLEM YURT İÇİ KARTLA YAPILMIŞTIR", PrinterDefinitions.Alignment.Center, 8)
        addTextToNewLine(styledText,"BU BELGEYİ SAKLAYINIZ", PrinterDefinitions.Alignment.Center, 8)
        styledText.newLine()
        styledText.printBitmap("ykb", 20)
        styledText.addSpace(50)
        return styledText.toString()
    }


}