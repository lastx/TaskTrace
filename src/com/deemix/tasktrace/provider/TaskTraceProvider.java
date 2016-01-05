package com.deemix.tasktrace.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.deemix.tasktrace.TaskTraceUtils;

import java.util.Calendar;

public class TaskTraceProvider extends ContentProvider {

    private static final String TAG                                         = "TaskTraceProvider";

    private SQLiteOpenHelper mOpenHelper;

    private static final int URI_TASKS                                      = 1;
    private static final int URI_TASKS_ID                                   = 2;
    private static final int URI_EVENTS                                     = 3;
    private static final int URI_EVENTS_ID                                  = 4;
    private static final int URI_DAILY                                      = 5;
    private static final int URI_DAILY_ID                                   = 6;
    private static final int URI_EVENT_TASK                                 = 7;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(TaskTrace.AUTHORITY, "tasks", URI_TASKS);
        sURIMatcher.addURI(TaskTrace.AUTHORITY, "tasks/*", URI_TASKS_ID);
        sURIMatcher.addURI(TaskTrace.AUTHORITY, "events", URI_EVENTS);
        sURIMatcher.addURI(TaskTrace.AUTHORITY, "events/*", URI_EVENTS_ID);
        sURIMatcher.addURI(TaskTrace.AUTHORITY, "daily", URI_DAILY);
        sURIMatcher.addURI(TaskTrace.AUTHORITY, "daily/*", URI_DAILY_ID);
        sURIMatcher.addURI(TaskTrace.AUTHORITY, "event_task", URI_EVENT_TASK);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new TTDatabaseHelper(getContext());
        Log.d(TAG, "onCreate()....");
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return "*/*";
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete()....");
        int count = 0;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sURIMatcher.match(uri);
        String id = "";
        switch (match) {
            case URI_TASKS:
                count = db.delete(TTDatabaseHelper.Tables.TASKS, selection, selectionArgs);
                break;
            case URI_TASKS_ID:
                id = uri.getLastPathSegment();
                count = db.delete(TTDatabaseHelper.Tables.TASKS, "_id=?", new String[] {id});
                break;
            case URI_EVENTS:
                count = db.delete(TTDatabaseHelper.Tables.EVENTS, selection, selectionArgs);
                break;
            case URI_EVENTS_ID:
                id = uri.getLastPathSegment();
                count = db.delete(TTDatabaseHelper.Tables.EVENTS, "_id=?", new String[] {id});
                break;
            case URI_DAILY:
                count = db.delete(TTDatabaseHelper.Tables.DAILY, selection, selectionArgs);
                break;
            case URI_DAILY_ID:
                id = uri.getLastPathSegment();
                count = db.delete(TTDatabaseHelper.Tables.DAILY, "_id=?", new String[] {id});
                break;
            default:
                break;
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri result = null;
        long id = 0;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sURIMatcher.match(uri);
        switch (match) {
            case URI_TASKS:
                id = db.insert(TTDatabaseHelper.Tables.TASKS, null, values);
                break;
            case URI_EVENTS:
                id = db.insert(TTDatabaseHelper.Tables.EVENTS, null, values);
                break;
            case URI_DAILY:
                id = db.insert(TTDatabaseHelper.Tables.DAILY, null, values);
                break;
            default:
                break;
        }
        if (id > 0) {
            result = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return result;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        int match = sURIMatcher.match(uri);
        switch (match) {
            case URI_TASKS:
                qb.setTables(TTDatabaseHelper.Tables.TASKS);
                break;
            case URI_TASKS_ID:
                qb.setTables(TTDatabaseHelper.Tables.TASKS);
                qb.appendWhere("_id=" + uri.getLastPathSegment());
                break;
            case URI_EVENTS:
                qb.setTables(TTDatabaseHelper.Tables.EVENTS);
                break;
            case URI_EVENTS_ID:
                qb.setTables(TTDatabaseHelper.Tables.EVENTS);
                qb.appendWhere("_id=" + uri.getLastPathSegment());
                break;
            case URI_DAILY:
                qb.setTables(TTDatabaseHelper.Tables.DAILY);
                break;
            case URI_DAILY_ID:
                qb.setTables(TTDatabaseHelper.Tables.DAILY);
                qb.appendWhere("_id=" + uri.getLastPathSegment());
                break;
            case URI_EVENT_TASK:
                qb.setTables(TTDatabaseHelper.Tables.EVENTS + " JOIN " + TTDatabaseHelper.Tables.TASKS + " ON events.task_id=tasks._id");
            default:
                break;
        }
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor ret = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        return ret;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        boolean needUpdateTask = false;
        int taskId = 0;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sURIMatcher.match(uri);
        String table = null;
        switch (match) {
            case URI_TASKS:
                table = TTDatabaseHelper.Tables.TASKS;
                if (values.containsKey(TaskTrace.Tasks._ID)) {
                    needUpdateTask = true;
                    taskId = values.getAsInteger(TaskTrace.Tasks._ID);
                }
                break;
            case URI_TASKS_ID:
                table = TTDatabaseHelper.Tables.TASKS;
                selection = "_id=" + uri.getLastPathSegment();
                needUpdateTask = true;
                break;
            case URI_EVENTS:
                table = TTDatabaseHelper.Tables.EVENTS;
                break;
            case URI_EVENTS_ID:
                table = TTDatabaseHelper.Tables.EVENTS;
                selection = "_id=" + uri.getLastPathSegment();
                needUpdateTask = true;
                taskId = getTaskId(uri.getLastPathSegment());
                break;
            case URI_DAILY:
                table = TTDatabaseHelper.Tables.DAILY;
                break;
            case URI_DAILY_ID:
                table = TTDatabaseHelper.Tables.DAILY;
                selection = "_id=" + uri.getLastPathSegment();
                break;
            default:
                break;
        }
        count = db.update(table, values, selection, selectionArgs);
        if (count > 0 && needUpdateTask) {
            updateTask(taskId);
            updateDaily(Long.parseLong(uri.getLastPathSegment()));
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    private void updateTask(int taskId) {
        Cursor c = getContext().getContentResolver().query(TaskTrace.Events.CONTENT_URI, null,
                TaskTrace.Events.TASK_ID + "=" + taskId + " AND " + TaskTrace.Events.ENDED + "=1", null, null);
        String taskLast = "";
        long last = 0;
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    last += c.getLong(3) - c.getLong(2);
//                    String range = DateUtils.formatDateRange(getContext(), c.getLong(2), c.getLong(3), DateUtils.FORMAT_SHOW_TIME);
                }
                taskLast = TaskTraceUtils.formatTimeFromMill(last); 
            } finally {
                c.close();
            }
        }
        c = getContext().getContentResolver().query(
                Uri.withAppendedPath(TaskTrace.Tasks.CONTENT_URI, taskId + ""), null, null, null,
                null);
        int eventCount = 0;
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    eventCount = c.getInt(6);
                }
            } finally {
                c.close();
            }
        }
        ContentValues values = new ContentValues();
        values.put(TaskTrace.Tasks.END_TIME, System.currentTimeMillis());
        values.put(TaskTrace.Tasks.LAST_TIME, last);
        values.put(TaskTrace.Tasks.LAST_STRING, taskLast);
        values.put(TaskTrace.Tasks.EVENT_COUNT, eventCount + 1);
        getContext().getContentResolver().update(
                Uri.withAppendedPath(TaskTrace.Tasks.CONTENT_URI, taskId + ""), values, null, null);
    }

    private int getTaskId(String eventId) {
        Cursor c = getContext().getContentResolver()
                .query(Uri.withAppendedPath(TaskTrace.Events.CONTENT_URI, eventId), null, null,
                        null, null);
        if (c != null) {
            try {
                c.moveToFirst();
                int taskId = c.getInt(1);
                return taskId;
            } finally {
                c.close();
            }
        }
        return 0;
    }

    private void updateDaily(long eventId) {
        Cursor cursor = getContext().getContentResolver().query(
                Uri.withAppendedPath(TaskTrace.Events.CONTENT_URI, eventId + ""), null, null, null,
                null);
        long time = 0;
        if (cursor != null) {
            try {
                if (cursor.getCount() == 0) {
                    return;
                }
                cursor.moveToFirst();
                time = cursor.getLong(2);
            } finally {
                cursor.close();
            }
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        long st = TaskTraceUtils.getDayStart(c);
        long et = TaskTraceUtils.getDayEnd(c);
        cursor = getContext().getContentResolver().query(
                TaskTrace.Events.CONTENT_URI,
                null,
                TaskTrace.Events.START_TIME + ">" + st + " AND " + TaskTrace.Daily.START_TIME + "<"
                        + et, null, TaskTrace.Daily.START_TIME + " DESC");
        Cursor taskCurosr = null;
        if (cursor != null) {
            try {
                int eventCount = cursor.getCount();
                int taskCount = 0;
                long startTime = 0;
                long endTime = 0;
                long lastTime = 0;
                if (eventCount == 0) {
                    return;
                }
                if (cursor.moveToFirst()) {
                    startTime = cursor.getLong(2);
                    endTime = cursor.getLong(3);
                    lastTime = endTime - startTime;
                }
                while (cursor.moveToNext()) {
                    startTime = cursor.getLong(2);
                    long tmpEndTime = cursor.getLong(3);
                    lastTime += tmpEndTime - startTime;
                }
                String lastString = TaskTraceUtils.formatTimeFromMill(lastTime);
                // get task count
                taskCurosr = getContext().getContentResolver().query(
                        TaskTrace.Events.CONTENT_URI,
                        null,
                        TaskTrace.Events.START_TIME + ">" + st + " AND " + TaskTrace.Daily.START_TIME + "<"
                                + et + ") AND (0==0) GROUP BY (" + TaskTrace.Events.TASK_ID, null, null);
                if (taskCurosr != null) {
                    taskCount = taskCurosr.getCount();
                }
                ContentValues values = new ContentValues();
                values.put(TaskTrace.Daily.END_TIME, endTime);
                values.put(TaskTrace.Daily.TASK_COUNT, taskCount);
                values.put(TaskTrace.Daily.EVENT_COUNT, eventCount);
                values.put(TaskTrace.Daily.LAST_TIME, lastTime);
                values.put(TaskTrace.Daily.LAST_STRING, lastString);
                int count = getContext().getContentResolver().update(
                        TaskTrace.Daily.CONTENT_URI,
                        values,
                        TaskTrace.Daily.DATE + ">" + (st - 1000) + " AND " + TaskTrace.Daily.DATE
                                + "<" + (st + 1000), null);
                if (count == 0) {
                    values.put(TaskTrace.Daily.DATE, st);
                    values.put(TaskTrace.Daily.START_TIME, startTime);
                    getContext().getContentResolver().insert(TaskTrace.Daily.CONTENT_URI, values);
                }
            } finally {
                cursor.close();
                if (taskCurosr != null) {
                    taskCurosr.close();
                }
            }
        }
    }
}
