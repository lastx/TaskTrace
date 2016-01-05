package com.deemix.tasktrace.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TTDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG                                         = "TTDatabaseHelper";

    private static final String DATABASE_NAME                               = "tasktrace.db";
    private static final int DATABASE_VERSION                               = 1;

    public interface Tables {
        public static final String TASKS                                    = "tasks";
        public static final String EVENTS                                   = "events";
        public static final String DAILY                                    = "daily";
    }

    private Context mContext;

    public TTDatabaseHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate()....");
        createTasksTable(db);
        createEventsTable(db);
        createDailyTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade()....");
    }

    private void createTasksTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.TASKS +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", name TEXT DEFAULT NULL" +
                ", start_time INTEGER NOT NULL DEFAULT 0" +
                ", end_time INTEGER NOT NULL DEFAULT 0" +
                ", last_time INTEGER NOT NULL DEFAULT 0" +
                ", last_string TEXT" +
                ", event_count INTEGER NOT NULL DEFAULT 0" +
                ", ended INTEGER NOT NULL DEFAULT 0" +
                ", color INTEGER NOT NULL DEFAULT 0" +
                ");");
    }

    private void createEventsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.EVENTS +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", task_id INTEGER NOT NULL DEFAULT 0" +
                ", start_time INTEGER NOT NULL DEFAULT 0" +
                ", end_time INTEGER NOT NULL DEFAULT 0" +
                ", last_time TEXT" +
                ", ended INTEGER NOT NULL DEFAULT 0" +
                ");");
    }

    private void createDailyTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.DAILY +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", date INTEGER UNIQUE NOT NULL DEFAULT 0" +
                ", task_count INTEGER NOT NULL DEFAULT 0" +
                ", event_count INTEGER NOT NULL DEFAULT 0" +
                ", start_time INTEGER NOT NULL DEFAULT 0" +
                ", end_time INTEGER NOT NULL DEFAULT 0" +
                ", last_time INTEGER NOT NULL DEFAULT 0" +
                ", last_string TEXT" +
                ");");
    }
}
