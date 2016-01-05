package com.deemix.tasktrace;

import android.content.Context;
import android.text.format.DateUtils;

public class EventData {
    private Context mContext;
    private String name;
    private long startTime;
    private long endTime;
    private long lastTime;
    private String last;

    public EventData(Context context, String name, long startTime, long endTime, String last) {
        mContext = context;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lastTime = endTime - startTime;
        this.last = last;
    }

    public String getName() {
        return name;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getRange() {
        return DateUtils.formatDateRange(mContext, startTime, endTime, DateUtils.FORMAT_SHOW_TIME);
    }

    public long getLastTime() {
        return lastTime;
    }

    public String getLast() {
        return last;
    }
}
