package com.tokeninc.sardis.application_template.helpers.PrintHelpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    public static String getDate(String format) {
        Date calDate = Calendar.getInstance().getTime();
        return new SimpleDateFormat(format, Locale.getDefault()).format(calDate);
    }

    public static String getTime(String format) {
        Date calDate = Calendar.getInstance().getTime();
        return new SimpleDateFormat(format, Locale.getDefault()).format(calDate);
    }
}
