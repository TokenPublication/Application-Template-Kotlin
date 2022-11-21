package com.tokeninc.sardis.application_template.helpers.PrintHelpers;
/*
import android.content.Context;

import com.token.printerlib.PrinterDefinitions;
import com.token.printerlib.PrinterDefinitions.Alignment;
import com.token.printerlib.StyledString;
import com.tokeninc.deviceinfo.DeviceInfo;
import com.tokeninc.sardis.application_template.AppTemp;
import com.tokeninc.sardis.application_template.Entity.SampleReceipt;
import com.tokeninc.sardis.application_template.Entity.SlipType;
import com.tokeninc.sardis.application_template.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SalePrintHelper extends BasePrintHelper{

    public static String getFormattedText(SampleReceipt receipt, SlipType slipType, Context context, Integer ZNO, Integer ReceiptNo)
    {
        StyledString styledText = new StyledString();


        if(slipType == SlipType.CARDHOLDER_SLIP){
            if(!((AppTemp) context.getApplicationContext()).getCurrentDeviceMode().equals(DeviceInfo.PosModeEnum.ECR.name())  && !((AppTemp) context.getApplicationContext()).getCurrentDeviceMode().equals(DeviceInfo.PosModeEnum.VUK507.name())){
                printSlipHeader(styledText, receipt);
            }
        }
        else {
            printSlipHeader(styledText, receipt);
        }

        styledText.newLine();
        if (slipType == SlipType.CARDHOLDER_SLIP) {
            styledText.addTextToLine("MÜŞTERİ NÜSHASI", Alignment.Center);
            styledText.newLine();
        }
        else if (slipType == SlipType.MERCHANT_SLIP) {
            styledText.addTextToLine("İŞYERİ NÜSHASI", Alignment.Center);
            styledText.newLine();
        }
        styledText.addTextToLine("SATIŞ", Alignment.Center);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.getDefault());
        String time = sdf.format(Calendar.getInstance().getTime());

        styledText.newLine();

        if(slipType == SlipType.CARDHOLDER_SLIP){
            if(((AppTemp) context.getApplicationContext()).getCurrentDeviceMode().equals(DeviceInfo.PosModeEnum.ECR.name())|| ((AppTemp) context.getApplicationContext()).getCurrentDeviceMode().equals(DeviceInfo.PosModeEnum.VUK507.name())){
                styledText.addTextToLine("C ONLINE", Alignment.Center);
            }
            else{
                styledText.addTextToLine(time + " " + "C ONLINE", Alignment.Center);
            }
        }
        else if(slipType == SlipType.MERCHANT_SLIP){
            styledText.addTextToLine(time + " " + "C ONLINE", Alignment.Center);
        }

        styledText.newLine();
        styledText.addTextToLine(receipt.getCardNo(), Alignment.Center);

        styledText.newLine();
        styledText.addTextToLine(receipt.getFullName(), Alignment.Center);

        styledText.setLineSpacing(1f);
        styledText.setFontSize(14);
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Semi_Bold);
        styledText.newLine();
        styledText.addTextToLine("TUTAR:");
        styledText.addTextToLine(receipt.getAmount(), Alignment.Right);

        styledText.setLineSpacing(0.5f);
        styledText.setFontSize(10);
        styledText.newLine();
        if (slipType == SlipType.CARDHOLDER_SLIP) {
            styledText.addTextToLine("KARŞILIĞI MAL/HİZM ALDIM", Alignment.Center);
        }
        else {
            styledText.addTextToLine("İşlem Şifre Girilerek Yapılmıştır", Alignment.Center);
            styledText.newLine();
            styledText.addTextToLine("İMZAYA GEREK YOKTUR", Alignment.Center);
        }

        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Bold);
        styledText.setFontSize(12);
        styledText.newLine();
        styledText.addTextToLine("SN: " + receipt.getSerialNo());
        styledText.addTextToLine("ONAY KODU: " + receipt.getApprovalCode(), Alignment.Right);

        styledText.setFontSize(8);
        styledText.setFontFace(PrinterDefinitions.Font_E.Sans_Semi_Bold);
        styledText.newLine();
        styledText.addTextToLine("GRUP NO:" + receipt.getGroupNo());

        styledText.newLine();
        styledText.addTextToLine("AID: " + receipt.getAid());

        if (slipType == SlipType.MERCHANT_SLIP) {
            addTextToNewLine(styledText, "*MALİ DEĞERİ YOKTUR*", Alignment.Center, 8);
        }

        if (slipType == SlipType.MERCHANT_SLIP) {
            if(((AppTemp) context.getApplicationContext()).getCurrentDeviceMode().equals(DeviceInfo.PosModeEnum.ECR.name())) {
                styledText.newLine();
                styledText.addTextToLine("Z NO: " +ZNO, Alignment.Right);
                styledText.addTextToLine("FİŞ NO: " +ReceiptNo, Alignment.Left);
            }
        }

        if (slipType == SlipType.MERCHANT_SLIP) {
            if(((AppTemp) context.getApplicationContext()).getCurrentDeviceMode().equals(DeviceInfo.PosModeEnum.ECR.name())
                    || ((AppTemp) context.getApplicationContext()).getCurrentDeviceMode().equals(DeviceInfo.PosModeEnum.VUK507.name())) {

            addTextToNewLine(styledText, ((AppTemp) context.getApplicationContext()).getCurrentFiscalID(), Alignment.Center, 8);

            }
        }

        styledText.newLine();
        styledText.addTextToLine("BU İŞLEM YURT İÇİ KARTLA YAPILMIŞTIR", Alignment.Center);
        styledText.newLine();
        styledText.addTextToLine("BU BELGEYİ SAKLAYINIZ", Alignment.Center);
        styledText.newLine();

        styledText.printBitmap("ykb", 20);
        styledText.addSpace(100);
        return styledText.toString();
    }

    public static byte[] getBitmap(Context context) {
        return getBitmapReceiptArray(context, R.raw.ziraat_fis);
    }

    public static byte[] getBitmapReceiptArray(Context context, int resourceId) {

        byte[] bitmap;
        try
        {
            InputStream inStream = context.getResources().openRawResource(resourceId);
            bitmap = new byte[inStream.available()];

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buff = new byte[10240];
            int i = Integer.MAX_VALUE;

            while ((i = inStream.read(buff, 0, buff.length)) > 0) {
                baos.write(buff, 0, i);
            }

            bitmap = baos.toByteArray(); // be sure to close InputStream in calling function

            inStream.close();
        }
        catch (IOException e)
        {
            bitmap = null;
            e.printStackTrace();
        }

        return bitmap;
    }

}

 */
