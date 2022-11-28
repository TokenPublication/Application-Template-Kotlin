package com.tokeninc.sardis.application_template.helpers.printHelpers

import java.text.SimpleDateFormat
import java.util.*

class DateUtil {

    fun getDate(format: String?): String? {
        val calDate: Date = Calendar.getInstance().time
        return SimpleDateFormat(format, Locale.getDefault()).format(calDate)
    }

    fun getTime(format: String?): String? {
        val calDate: Date = Calendar.getInstance().time
        return SimpleDateFormat(format, Locale.getDefault()).format(calDate)
    }

}