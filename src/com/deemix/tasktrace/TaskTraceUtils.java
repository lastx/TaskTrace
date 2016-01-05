package com.deemix.tasktrace;

import java.util.Calendar;

public class TaskTraceUtils {

    public static final String INTENT_DAY_START                             = "day_start";
    public static final String INTENT_DAY_END                               = "day_end";

    /**
     * Format a milliseconds into "HH:mm:ss"
     * @param milliseconds
     * @return format String
     */
    public static String formatTimeFromMill(long milliseconds) {
        long time = milliseconds / 1000;
        int hour = (int) (time / 3600);
        int min = 0;
        int sec = 0;
        if (hour > 0) {
            time = time - hour * 3600;
            min = (int) (time / 60);
            if (min > 0) {
                sec = (int) (time - min * 60);
            } else {
                sec = (int) time;
            }
        } else {
            min = (int) (time / 60);
            if (min > 0) {
                sec = (int) (time - min * 60);
            } else {
                sec = (int) time;
            }
        }
        String result = "";
        if (hour > 0) {
            result = hour + ":";
        }
        if (min >= 10) {
            result = result + min;
        } else {
            result = result + "0" +  min;
        }
        if (sec >= 10) {
            result = result + ":" + sec;
        } else {
            result = result + ":" + "0" +  sec;
        }
        return result;
    }

    public static long getDayStart(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTimeInMillis();
    }

    public static long getDayEnd(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTimeInMillis();
    }
}
