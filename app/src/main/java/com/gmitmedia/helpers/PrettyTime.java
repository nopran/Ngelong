package com.gmitmedia.helpers;

import android.content.Context;
import android.util.Log;

import com.gmitmedia.ngelong.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by melvin on 22/04/2017.
 */

public class PrettyTime {

    public static Date currentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    public static String getTimeAgo(String dateString, String format, Context ctx) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);

        Date convertedDate = new Date();

        try {
            convertedDate = dateFormat.parse(dateString);
            Log.v("date", convertedDate.toString());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return getTimeAgo(convertedDate, ctx);
    }

    public static String getTimeAgo(Date date, Context ctx) {

        SimpleDateFormat monthDateFormatter = new SimpleDateFormat("dd MMM");
        SimpleDateFormat fullDateFormatter = new SimpleDateFormat("dd MMM yyyy");
        SimpleDateFormat timeDateFormatter = new SimpleDateFormat("h:mm a");

        if (date == null) {
            return null;
        }

        long time = date.getTime();

        Date curDate = currentDate();
        long now = curDate.getTime();
        if (time > now || time <= 0) {
            return null;
        }

        int dim = getTimeDistanceInMinutes(time);

        String timeAgo = fullDateFormatter.format(date);

        if (dim == 0) {
            return "< 1 " + ctx.getResources().getString(R.string.date_util_unit_minute) + " " + ctx.getResources().getString(R.string.date_util_suffix);
        } else if (dim == 1) {
            return "1 " + ctx.getResources().getString(R.string.date_util_unit_minute) + " " + ctx.getResources().getString(R.string.date_util_suffix);
        } else if (dim >= 2 && dim <= 44) {
            //44 mins ago
            return dim + " " + ctx.getResources().getString(R.string.date_util_unit_minutes) + " " + ctx.getResources().getString(R.string.date_util_suffix);
        } else if (fullDateFormatter.format(date).equals(fullDateFormatter.format(curDate))) {
            //same day
            return timeDateFormatter.format(date);
        } else if (dim <= 525599) {
            //same year
            return monthDateFormatter.format(date);
        }

        return timeAgo;
    }

    private static int getTimeDistanceInMinutes(long time) {
        long timeDistance = currentDate().getTime() - time;
        return Math.round((Math.abs(timeDistance) / 1000) / 60);
    }
}
