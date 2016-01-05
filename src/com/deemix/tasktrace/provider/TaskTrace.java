package com.deemix.tasktrace.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class TaskTrace {

    public static final String AUTHORITY                                    = "tasktrace";

    public static final class Tasks implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://tasktrace/tasks");

        public static final String NAME                                     = "name";
        public static final String START_TIME                               = "start_time";
        public static final String END_TIME                                 = "end_time";
        public static final String LAST_TIME                                = "last_time";
        public static final String LAST_STRING                              = "last_string";
        public static final String EVENT_COUNT                              = "event_count";
        public static final String ENDED                                    = "ended";
        public static final String COLOR                                    = "color";
    }

    public static final class Events implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://tasktrace/events");

        public static final String TASK_ID                                  = "task_id";
        public static final String START_TIME                               = "start_time";
        public static final String END_TIME                                 = "end_time";
        public static final String LAST_TIME                                = "last_time";
        public static final String ENDED                                    = "ended";
    }

    public static final class Daily implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://tasktrace/daily");

        public static final String DATE                                     = "date";
        public static final String TASK_COUNT                               = "task_count";
        public static final String EVENT_COUNT                              = "event_count";
        public static final String START_TIME                               = "start_time";
        public static final String END_TIME                                 = "end_time";
        public static final String LAST_TIME                                = "last_time";
        public static final String LAST_STRING                              = "last_string";
    }

    public static final Uri EVENT_TASK_CONTENT_URI = Uri.parse("content://tasktrace/event_task");
}
