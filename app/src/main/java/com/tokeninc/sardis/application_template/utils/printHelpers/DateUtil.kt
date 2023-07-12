package com.tokeninc.sardis.application_template.utils.printHelpers

import java.text.SimpleDateFormat
import java.util.*

/**
 * This class is for date functions
 */
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
