package com.tokeninc.sardis.application_template.helpers

import java.util.*
import java.util.stream.IntStream.range

class StringHelper {

    public fun getAmount(amount: Int): String? {
        val Lang = Locale.getDefault().displayLanguage
        val currency: String
        currency = if (Lang == "Türkçe") {
            "₺"
        } else {
            "€"
        }
        var str = amount.toString()
        if (str.length == 1) str = "00$str" else if (str.length == 2) str = "0$str"
        val s1 = str.substring(0, str.length - 2)
        val s2 = str.substring(str.length - 2)
        return "$s1,$s2$currency"
    }

    public fun GenerateApprovalCode(BatchNo: String, TransactionNo: String, SaleID: String): String? {
        var approvalCode = "0"
        approvalCode = BatchNo + TransactionNo + SaleID
        return approvalCode
    }


    public fun maskCardNumber(cardNo: String): String? {
        // 1234 **** **** 7890
        val prefix = cardNo.substring(0, 4)
        val suffix = cardNo.substring(cardNo.length - 4)
        val masked = StringBuilder(prefix)
        for (i in 4 until cardNo.length - 4) {
            masked.append("*")
        }
        masked.append(suffix)
        val formatted = StringBuilder(masked)
        var index = 4
        while (index < formatted.length) {
            formatted.insert(index, " ")
            index += 5
        }
        return formatted.toString()
    }


    public fun MaskTheCardNo(cardNo: String): String? {
        // CREATE A MASKED CARD NO
        // First 6 and Last 4 digit is visible, others are masked with '*' Card No can be 16,17,18 Digits...
        // 123456******0987
        val CardNoFirstSix = cardNo.substring(0, 6)
        val CardNoLastFour = cardNo.substring(cardNo.length - 4)
        val masked = StringBuilder(CardNoFirstSix)
        for (i in 4 .. cardNo.length - 5) {
            masked.append("*")
        }
        masked.append(CardNoLastFour)
        val formatted = StringBuilder(masked)
        return formatted.toString()
    }

}