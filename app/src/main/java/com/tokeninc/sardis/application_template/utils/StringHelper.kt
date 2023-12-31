package com.tokeninc.sardis.application_template.utils

import java.util.Locale

/**
 * This class is helper for printing some strings
 */
class StringHelper {

    fun getAmount(amount: Int): String {
        val lang = Locale.getDefault().displayLanguage
        val currency: String = if (lang == "Türkçe") { "₺" } else { "€" }
        var str = amount.toString()
        if (str.length == 1) str = "00$str" else if (str.length == 2) str = "0$str"
        val s1 = str.substring(0, str.length - 2)
        val s2 = str.substring(str.length - 2)
        return "$s1,$s2$currency"
    }

    /**
     * It forms card number as **** **** **** 4321
     */
    fun maskCardNumber(cardNo: String): String {
        // 1234 **** **** 7890
        //val prefix = cardNo.substring(0, 4)
        val suffix = cardNo.substring(cardNo.length - 4)
        val masked = StringBuilder("")
        for (i in 0 until cardNo.length - 4) {
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

    /**
     * It forms card Number as 1234 **** **** 4321
     */
    fun maskTheCardNo(cardNo: String): String {
        val cardNoFirstFour = cardNo.substring(0, 4)
        val cardNoLastFour = cardNo.substring(cardNo.length - 4)
        val masked = StringBuilder(cardNoFirstFour)
        for (i in 4 .. cardNo.length - 5) {
            if (i % 4 == 0)
                masked.append(" ")
            masked.append("*")
        }
        masked.append(" ")
        masked.append(cardNoLastFour)
        val formatted = StringBuilder(masked)
        return formatted.toString()
    }

    /**
     * It forms card number as 12345678****9876 for bundle
     */
    fun maskCardForBundle(cardNo: String): String{
        val first = cardNo.substring(0, 8)
        val last = cardNo.substring(cardNo.length - 4)
        val masked = StringBuilder(first)
        for (i in 8 .. cardNo.length - 5) {
            masked.append("*")
        }
        masked.append(last)
        val formatted = StringBuilder(masked)
        return formatted.toString()
    }

    /**
     * It generates approval code
     */
    fun generateApprovalCode(BatchNo: String, TransactionNo: String, SaleID: String): String {
        return BatchNo + TransactionNo + SaleID
    }

    fun getInstAmount(amount: Int): String {
        var str = amount.toString()
        if (str.length == 1) str = "00$str" else if (str.length == 2) str = "0$str"
        val s1 = str.substring(0, str.length - 2)
        val s2 = str.substring(str.length - 2)
        return "$s1,$s2"
    }

    /**
     * This method ensures length of random generated numbers is requested length.
     */
    fun addZeros(number: String, length: Int): String{
        val iterator = length - number.length
        var numb = number
        for (i in 1..iterator) {
            numb = "0$numb"
        }
        return numb
    }

    fun hexStringToAscii(hexString: String): String {
        val output = java.lang.StringBuilder()
        var i = 0
        while (i < hexString.length) {
            val str2 = hexString.substring(i, i + 2)
            output.append(str2.toInt(16).toChar())
            i += 2
        }
        return output.toString()
    }
}
