package com.tokeninc.sardis.application_template.helpers.printHelpers

import android.content.ContentValues
import android.content.Context
import com.token.printerlib.PrinterDefinitions
import com.token.printerlib.StyledString
import com.tokeninc.deviceinfo.DeviceInfo
import com.tokeninc.sardis.application_template.AppTemp
import com.tokeninc.sardis.application_template.OnlineTransactionResponse
import com.tokeninc.sardis.application_template.SampleReceipt
import com.tokeninc.sardis.application_template.database.transaction.TransactionCol
import com.tokeninc.sardis.application_template.enums.SlipType
import com.tokeninc.sardis.application_template.helpers.StringHelper
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SalePrintHelper: BasePrintHelper() {
    //TODO  MASKCARDNUMBER yap, YKB slibi aynısı, application conxtext currentDevice'a göre slip


    fun getFormattedText(slipType: SlipType, contentValues: ContentValues, onlineTransactionResponse: OnlineTransactionResponse,
                         context: Context, ZNO: Int, ReceiptNo: Int, isCopy: Boolean): String {
        val styledText = StyledString()
        val stringHelper = StringHelper()
        if (slipType === SlipType.CARDHOLDER_SLIP) {
        if (!(context.applicationContext as AppTemp).getCurrentDeviceMode().equals(DeviceInfo.PosModeEnum.ECR.name) &&
            !(context.applicationContext as AppTemp).getCurrentDeviceMode().equals(DeviceInfo.PosModeEnum.VUK507.name))  {
                //printSlipHeader(styledText, contentValues) //TODO Burada merchantID, TerminalID, PosID parametre olarak gitmeli
            }
        } else {
            //printSlipHeader(styledText, receipt)
        }
        styledText.newLine()
        if (slipType === SlipType.CARDHOLDER_SLIP) {
            styledText.addTextToLine("MÜŞTERİ NÜSHASI", PrinterDefinitions.Alignment.Center)
            styledText.newLine()
        } else if (slipType === SlipType.MERCHANT_SLIP) {
            styledText.addTextToLine("İŞYERİ NÜSHASI", PrinterDefinitions.Alignment.Center)
            styledText.newLine()
        }
        styledText.addTextToLine("SATIŞ", PrinterDefinitions.Alignment.Center)
        val sdf = SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.getDefault())
        val time = sdf.format(Calendar.getInstance().time)
        styledText.newLine()
        if (slipType === SlipType.CARDHOLDER_SLIP) {
         if ((context.applicationContext as AppTemp).getCurrentDeviceMode().equals(DeviceInfo.PosModeEnum.ECR.name) ||
             (context.applicationContext as AppTemp).getCurrentDeviceMode().equals(DeviceInfo.PosModeEnum.VUK507.name))
         {
                styledText.addTextToLine("C ONLINE", PrinterDefinitions.Alignment.Center)
            } else {
                styledText.addTextToLine("$time C ONLINE", PrinterDefinitions.Alignment.Center)
            }
        } else if (slipType === SlipType.MERCHANT_SLIP) {
            styledText.addTextToLine("$time C ONLINE", PrinterDefinitions.Alignment.Center)
        }

        styledText.newLine()
        styledText.addTextToLine(stringHelper.MaskTheCardNo(contentValues.getAsString(TransactionCol.Col_PAN.name)), PrinterDefinitions.Alignment.Center)
        styledText.newLine()
        if (contentValues.get(TransactionCol.Col_CustName.name) != null){
            styledText.addTextToLine(contentValues.getAsString(TransactionCol.Col_CustName.name), PrinterDefinitions.Alignment.Center)
        }
        styledText.setLineSpacing(1f)
        styledText.setFontSize(14)
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Semi_Bold)
        styledText.newLine()
        styledText.addTextToLine("TUTAR:")
        styledText.addTextToLine(stringHelper.getAmount(contentValues.getAsString(TransactionCol.Col_Amount.name).toInt()), PrinterDefinitions.Alignment.Right)
        styledText.setLineSpacing(0.5f)
        styledText.setFontSize(10)
        styledText.newLine()
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
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Bold)
        styledText.setFontSize(12)
        styledText.newLine()
        styledText.addTextToLine("SN: " + contentValues.getAsString(TransactionCol.Col_STN.name))
        styledText.addTextToLine(
            "ONAY KODU: " + contentValues.getAsString(TransactionCol.Col_AuthCode.name),
            PrinterDefinitions.Alignment.Right
        )
        styledText.setFontSize(8)
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Semi_Bold)
        styledText.newLine()
        styledText.addTextToLine("GRUP NO:" + contentValues.getAsString(TransactionCol.Col_GUP_SN.name))
        styledText.newLine()
        styledText.addTextToLine("AID: " + contentValues.getAsString(TransactionCol.Col_Aid.name))
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
        styledText.newLine()
        styledText.addTextToLine(
            "BU İŞLEM YURT İÇİ KARTLA YAPILMIŞTIR",
            PrinterDefinitions.Alignment.Center
        )
        styledText.newLine()
        styledText.addTextToLine("BU BELGEYİ SAKLAYINIZ", PrinterDefinitions.Alignment.Center)
        styledText.newLine()
        styledText.printBitmap("ykb", 20)
        styledText.addSpace(100)
        return styledText.toString()
    }
}