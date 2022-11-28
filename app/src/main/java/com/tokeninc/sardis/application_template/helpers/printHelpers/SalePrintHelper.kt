package com.tokeninc.sardis.application_template.helpers.printHelpers

import android.content.Context
import com.token.printerlib.PrinterDefinitions
import com.token.printerlib.StyledString
import com.tokeninc.deviceinfo.DeviceInfo
import com.tokeninc.sardis.application_template.AppTemp
import com.tokeninc.sardis.application_template.SampleReceipt
import com.tokeninc.sardis.application_template.enums.SlipType
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SalePrintHelper: BasePrintHelper() {

    fun getFormattedText(
        receipt: SampleReceipt,
        slipType: SlipType,
        context: Context,
        ZNO: Int,
        ReceiptNo: Int
    ): String {
        val styledText = StyledString()
        if (slipType === SlipType.CARDHOLDER_SLIP) {

            if (!(context.applicationContext as AppTemp).getCurrentDeviceMode()
                    .equals(DeviceInfo.PosModeEnum.ECR.name) && !(context.applicationContext as AppTemp).getCurrentDeviceMode()
                    .equals(DeviceInfo.PosModeEnum.VUK507.name)
            ) {
                printSlipHeader(styledText, receipt)
            }
        } else {
            printSlipHeader(styledText, receipt)
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
            if ((context.applicationContext as AppTemp).getCurrentDeviceMode()
                    .equals(DeviceInfo.PosModeEnum.ECR.name) || (context.applicationContext as AppTemp).getCurrentDeviceMode()
                    .equals(DeviceInfo.PosModeEnum.VUK507.name)
            ) {
                styledText.addTextToLine("C ONLINE", PrinterDefinitions.Alignment.Center)
            } else {
                styledText.addTextToLine("$time C ONLINE", PrinterDefinitions.Alignment.Center)
            }
        } else if (slipType === SlipType.MERCHANT_SLIP) {
            styledText.addTextToLine("$time C ONLINE", PrinterDefinitions.Alignment.Center)
        }
        styledText.newLine()
        styledText.addTextToLine(receipt.cardNo, PrinterDefinitions.Alignment.Center)
        styledText.newLine()
        styledText.addTextToLine(receipt.fullName, PrinterDefinitions.Alignment.Center)
        styledText.setLineSpacing(1f)
        styledText.setFontSize(14)
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Semi_Bold)
        styledText.newLine()
        styledText.addTextToLine("TUTAR:")
        styledText.addTextToLine(receipt.amount, PrinterDefinitions.Alignment.Right)
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
        styledText.addTextToLine("SN: " + receipt.serialNo)
        styledText.addTextToLine(
            "ONAY KODU: " + receipt.approvalCode,
            PrinterDefinitions.Alignment.Right
        )
        styledText.setFontSize(8)
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Semi_Bold)
        styledText.newLine()
        styledText.addTextToLine("GRUP NO:" + receipt.groupNo)
        styledText.newLine()
        styledText.addTextToLine("AID: " + receipt.aid)
        if (slipType === SlipType.MERCHANT_SLIP) {
            addTextToNewLine(
                styledText,
                "*MALİ DEĞERİ YOKTUR*",
                PrinterDefinitions.Alignment.Center,
                8
            )
        }
        if (slipType === SlipType.MERCHANT_SLIP) {
            if ((context.applicationContext as AppTemp).getCurrentDeviceMode()
                    .equals(DeviceInfo.PosModeEnum.ECR.name)
            ) {
                styledText.newLine()
                styledText.addTextToLine("Z NO: $ZNO", PrinterDefinitions.Alignment.Right)
                styledText.addTextToLine("FİŞ NO: $ReceiptNo", PrinterDefinitions.Alignment.Left)
            }
        }
        if (slipType === SlipType.MERCHANT_SLIP) {
            if ((context.applicationContext as AppTemp).getCurrentDeviceMode()
                    .equals(DeviceInfo.PosModeEnum.ECR.name)
                || (context.applicationContext as AppTemp).getCurrentDeviceMode()
                    .equals(DeviceInfo.PosModeEnum.VUK507.name)
            ) {
                addTextToNewLine(
                    styledText,
                    (context.applicationContext as AppTemp).getCurrentFiscalID(),
                    PrinterDefinitions.Alignment.Center,
                    8
                )
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



    fun getBitmapReceiptArray(context: Context, resourceId: Int): ByteArray? {
        var bitmap: ByteArray?
        try {
            val inStream = context.resources.openRawResource(resourceId)
            bitmap = ByteArray(inStream.available())
            val baos = ByteArrayOutputStream()
            val buff = ByteArray(10240)
            var i = Int.MAX_VALUE
            while (inStream.read(buff, 0, buff.size).also { i = it } > 0) {
                baos.write(buff, 0, i)
            }
            bitmap = baos.toByteArray() // be sure to close InputStream in calling function
            inStream.close()
        } catch (e: IOException) {
            bitmap = null
            e.printStackTrace()
        }
        return bitmap
    }

}