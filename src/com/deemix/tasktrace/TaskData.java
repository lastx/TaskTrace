package com.deemix.tasktrace;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

public class TaskData {
    private Context mContext;
    private String name;
    private long startTime;
    private long endTime;
    private long eventCount;
    private long lastTime;
    private String last;

    public TaskData(Context context, EventData event) {
        mContext = context;
        name = event.getName();
        startTime = event.getStartTime();
        endTime = event.getEndTime();
        lastTime = event.getLastTime();
        eventCount = 1;
        last = event.getLast();
    }

    public void addEvent(EventData event) {
        if (!TextUtils.equals(event.getName(), name)) {
            return;
        }
        long st = event.getStartTime();
        long et = event.getEndTime();
        if (st < startTime) {
            startTime = st;
        }
        if (et > endTime) {
            endTime = et;
        }
        eventCount++;
        lastTime += event.getLastTime();
        last = TaskTraceUtils.formatTimeFromMill(lastTime);
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

    public long getEventCount() {
        return eventCount;
    }

    public long getLastTime() {
        return lastTime;
    }

    public String getLast() {
        return last;
    }
}
