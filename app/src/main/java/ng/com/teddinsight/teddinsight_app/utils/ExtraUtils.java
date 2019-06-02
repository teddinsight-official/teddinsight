package ng.com.teddinsight.teddinsight_app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExtraUtils {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

    public static Spannable getSpannableText(String text, int color, int start, int end) {
        Spannable wordtoSpan = new SpannableString(text);
        wordtoSpan.setSpan(new ForegroundColorSpan(color), start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return wordtoSpan;
    }

    public static String getHumanReadableString(long timestamp) {
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, hh:mm a", Locale.ENGLISH);
        return df.format(new Date(timestamp));
    }

    public static String getHumanReadableString(long timestamp, boolean noTime) {
        String format;

        if (noTime)
            format = "EEE, d MMM yyyy";
        else
            format = "EEE, d MMM yyyy, hh:mm a";
        DateFormat df = new SimpleDateFormat(format, Locale.ENGLISH);
        return df.format(new Date(timestamp));
    }

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    public static void createInstagramIntent(Activity activity, String mediaPath) {
        Intent share = new Intent(Intent.ACTION_SEND);
        String type = "image/*";
        share.setType(type);
        Log.e("TAG", mediaPath);
        File media = new File(mediaPath);
        Uri uri = Uri.fromFile(media);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        activity.startActivity(Intent.createChooser(share, "Select Instagram"));
    }


}
