package com.tokeninc.sardis.application_template.helpers;

import java.util.Locale;

public class StringHelper {

    public static String getAmount(int amount) {
        String Lang = Locale.getDefault().getDisplayLanguage();
        String currency;

        if(Lang.equals("Türkçe")){
         currency = "₺";
        }
        else{
            currency ="€";
        }

        String str=String.valueOf(amount);
        if (str.length() == 1) str = "00" + str;
        else if (str.length() == 2) str = "0" + str;

        String s1=str.substring(0,str.length()-2);
        String s2=str.substring(str.length()-2);
        return s1 + "," + s2 + currency;
    }

    public static String GenerateApprovalCode(String BatchNo, String TransactionNo, String SaleID){
        String approvalCode = "0";
        approvalCode = BatchNo + TransactionNo + SaleID;
        return approvalCode;
    }


    public static String maskCardNumber(String cardNo) {
        // 1234 **** **** 7890
        String prefix = cardNo.substring(0, 4);
        String suffix = cardNo.substring(cardNo.length() - 4);
        StringBuilder masked = new StringBuilder(prefix);
        for (int i = 4; i < cardNo.length() - 4; i++) {
            masked.append("*");
        }
        masked.append(suffix);
        StringBuilder formatted = new StringBuilder(masked);
        int index = 4;
        while (index < formatted.length()) {
            formatted.insert(index, " ");
            index += 5;
        }
        return formatted.toString();
    }


    public static String MaskTheCardNo(String cardNo){
        // CREATE A MASKED CARD NO
        // First 6 and Last 4 digit is visible, others are masked with '*' Card No can be 16,17,18 Digits...
        // 123456******0987
        String CardNoFirstSix = cardNo.substring(0, 6);
        String CardNoLastFour =  cardNo.substring(cardNo.length() - 4);
        StringBuilder masked = new StringBuilder(CardNoFirstSix);
        for (int i = 4; i < cardNo.length() - 4; i++) {
            masked.append("*");
        }
        masked.append(CardNoLastFour);
        StringBuilder formatted = new StringBuilder(masked);

        return formatted.toString();
    }

}
