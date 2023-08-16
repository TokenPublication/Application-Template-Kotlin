package com.tokeninc.sardis.application_template.utils.printHelpers

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    fun getFormattedDate(dateText: String): String? {
        val year = dateText.substring(0, 4)
        val month = dateText.substring(4, 6)
        val day = dateText.substring(6, 8)
        return "$day-$month-$year"
    }

    fun getCashRefundDate(dateText: String): String {
        Log.i("getCashRefundDate",dateText)
        val year = dateText.substring(0, 4)
        Log.i("getCashRefundDate","year: $year")
        val month = dateText.substring(5, 7)
        Log.i("getCashRefundDate","month: $month")
        val day = dateText.substring(8, 10)
        Log.i("getCashRefundDate","day: $day")
        return "$day/$month/$year"
    }

    fun getFormattedTime(timeText: String): String? {
        val hour = timeText.substring(0, 2)
        val minute = timeText.substring(2, 4)
        val second = timeText.substring(4, 6)
        return "$hour:$minute:$second"
    }
}
