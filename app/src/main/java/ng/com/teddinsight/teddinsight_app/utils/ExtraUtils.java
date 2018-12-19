package ng.com.teddinsight.teddinsight_app.utils;

import android.graphics.Color;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ExtraUtils {

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    public static String formatDate(long milliseconds) /* This is your topStory.getTime()*1000 */ {
        String days[] = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String months[] = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        Date d = new Date(milliseconds);
        return days[d.getDay()] + " " + months[d.getMonth()] + " " + d.getYear();
    }

    public static int getColor(int status) {
        switch (status) {
            case 0:
                return Color.parseColor("#F44336");
            case 1:
                return Color.parseColor("#4CAF50");
            default:
                return Color.parseColor("#F44336");
        }
    }

    public static String getStatText(int status) {
        switch (status) {
            case 0:
                return "Incomplete";
            case 1:
                return "Complete";
            default:
                return "Incomplete";
        }
    }
}
