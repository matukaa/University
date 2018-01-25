package model.examen.com.events.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by iulia on 1/24/2017.
 */

public class DateUtils {
    private static final String FORMAT1 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String FORMAT2 = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'";
    private static final String FORMAT3 = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private static final String SIMPLE_OUT_DATE_FORMAT = "yyyy-MM-dd HH:mm";

    public static Date parseDate(String dateString) throws ParseException {
        SimpleDateFormat format1 = new SimpleDateFormat(FORMAT1);
        SimpleDateFormat format2 = new SimpleDateFormat(FORMAT2);
        SimpleDateFormat format3 = new SimpleDateFormat(FORMAT3);

        try {
            return format1.parse(dateString);
        } catch (Exception ignored) {
        }
        try {
            return format2.parse(dateString);
        } catch (Exception ignored) {
        }
        try {
            return format3.parse(dateString);
        } catch (Exception e) {
            throw e;
        }
    }

    public static String changeDateFormat(Date date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(SIMPLE_OUT_DATE_FORMAT);
        String dateString = dateFormat.format(date);
        return dateString;
    }
}
