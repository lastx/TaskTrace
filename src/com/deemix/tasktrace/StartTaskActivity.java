package com.deemix.tasktrace;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.deemix.tasktrace.provider.TaskTrace;

import java.util.Calendar;
import java.util.HashMap;

public class StartTaskActivity extends Activity {

    private static final String TAG                                         = "StartTaskActivity";
    private static final String BASE_TIME                                   = "base_time";
    private static final String IS_TIMING                                   = "is_timing";
    private static final String TASK_NAME                                   = "task_name";
    private static final String EVENT_ID                                    = "event_id";
    private static final String TASK_ID                                     = "task_id";

    private SharedPreferences mSp;
    private Chronometer mTimer;
    private Button mStart;
    private Button mStop;
    private Button mEnd;
    private TextView mName;
    private ListView mList;
    private Cursor mCursor;
    private TodayListAdapter mListAdapter;
    private TodayContentObserver mObserver;
    private MatrixCursor matrixCursor;
    private AlertDialog mDialog;

    private boolean isTiming;
    private int mTaskCount;
    private int mCurrentTaskId;
    private long mCurrentEventId;
    private long mDayStart;
    private long mDayEnd;

    private static final int TASKS_ID_COLUMN                                = 0;
    private static final int TASKS_NAME_COLUMN                              = 1;
    private static final int TASKS_ENDED_COLUMN                             = 7;
    private static final int TASKS_COLOR_COLUMN                             = 8;
    private static final String[] MATRIXCURSOR_PROJECTION                   = new String[] {
        TaskTrace.Tasks._ID,
        TaskTrace.Tasks.NAME,
        TaskTrace.Tasks.COLOR,
    };
    private static final int TASK_ID_COLUMN                                 = 0;
    private static final int TASK_NAME_COLUMN                               = 1;
    private static final int TASK_COLOR_COLUMN                              = 2;

    private static final String[] TODAY_PROJECTION                          = new String[] {
        "events." + TaskTrace.Events._ID,
        "events." + TaskTrace.Events.START_TIME,
        "events." + TaskTrace.Events.END_TIME,
        "events." + TaskTrace.Events.LAST_TIME,
        TaskTrace.Tasks.NAME,
    };
    public static final int TODAY_EVENT_ID_COLUMN                           = 0;
    public static final int TODAY_EVENT_START_TIME_COLUMN                   = 1;
    public static final int TODAY_EVENT_END_TIME_COLUMN                     = 2;
    public static final int TODAY_ENVET_LAST_TIME_COLUMN                    = 3;
    public static final int TODAY_TASK_NAME_COLUMN                          = 4;

    private HashMap<String, Integer> mTasks = new HashMap<String, Integer>();

    private OnClickListener mStartClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            chooseTask();
        }
    };
    private OnClickListener mStopClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            stop();
        }
    };
    private OnClickListener mEndClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            end();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_task);
        mSp = PreferenceManager.getDefaultSharedPreferences(this);
        mTimer = (Chronometer) findViewById(R.id.chronometer);
        mStart = (Button) findViewById(R.id.start_btn);
        mStart.setOnClickListener(mStartClickListener);
        mStop = (Button) findViewById(R.id.pause_btn);
        mStop.setOnClickListener(mStopClickListener);
        mEnd = (Button) findViewById(R.id.stop_btn);
        mEnd.setOnClickListener(mEndClickListener);
        mName = (TextView) findViewById(R.id.task_name);
        mList = (ListView) findViewById(android.R.id.list);

        mCursor = getCursor();
        mListAdapter = new TodayListAdapter(this, mCursor);
        mList.setAdapter(mListAdapter);
        mObserver = new TodayContentObserver();
        registerContentObservers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStart.setEnabled(true);
        mStop.setEnabled(false);
        mEnd.setEnabled(false);
        isTiming = mSp.getBoolean(IS_TIMING, false);
        mCurrentEventId = mSp.getLong(EVENT_ID, 0);
        if (isTiming && mCurrentEventId != 0) {
            reStart();
        } else {
            // Reset
            mName.setText(R.string.task_tip);
            mTimer.setBase(SystemClock.elapsedRealtime());
        }
        loadTasks();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterContentObservers();
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_start_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent();
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_history:
                i.setClass(this, HistoryActivity.class);
                startActivity(i);
                break;
            case R.id.menu_tody:
                i.putExtra(TaskTraceUtils.INTENT_DAY_START, mDayStart);
                i.putExtra(TaskTraceUtils.INTENT_DAY_END, mDayEnd);
                i.setClass(this, OneDayActivity.class);
                startActivity(i);
                break;
            case R.id.menu_tasks:
                i.setClass(this, TasksActivity.class);
                startActivity(i);
            default:
                break;
        }
        return true;
    }

    private void reStart() {
        long base = mSp.getLong(BASE_TIME, 0);
        mCurrentTaskId = mSp.getInt(TASK_ID, 0);
        String name = mSp.getString(TASK_NAME, null);
        if (base != 0) {
            mName.setText(name);
            mTimer.setBase(base);
            mTimer.start();
            mStart.setEnabled(false);
            mStop.setEnabled(true);
            mEnd.setEnabled(false);
        }
    }

    private void start(int taskId, String name) {
        isTiming = true;
        long base = SystemClock.elapsedRealtime();
        mName.setText(name);
        mTimer.setBase(base);
        mTimer.start();
        mStart.setEnabled(false);
        mStop.setEnabled(true);
        mEnd.setEnabled(false);
        mCurrentTaskId = taskId;
        mCurrentEventId = startEvent(taskId);
        mSp.edit().putString(TASK_NAME, name).putLong(BASE_TIME, base).putBoolean(IS_TIMING, true)
                .putLong(EVENT_ID, mCurrentEventId).putInt(TASK_ID, mCurrentTaskId).apply();
    }

    private void stop() {
        isTiming = false;
        mTimer.stop();
        mStart.setEnabled(true);
        mStop.setEnabled(false);
        mEnd.setEnabled(true);
        endEvent();
    }

    private void end() {
        // End the task.
        mStart.setEnabled(true);
        mStop.setEnabled(false);
        mEnd.setEnabled(false);
        ContentValues values = new ContentValues();
        values.put(TaskTrace.Tasks.ENDED, 1);
        int count = getContentResolver().update(
                Uri.withAppendedPath(TaskTrace.Tasks.CONTENT_URI, mCurrentTaskId + ""), values,
                null, null);
        if (count > 0) {
            loadTasks();
        }
    }

    // Return event id.
    private long startEvent(int taskId) {
        // insert to Events table
        ContentValues values = new ContentValues();
        values.put(TaskTrace.Events.TASK_ID, taskId);
        values.put(TaskTrace.Events.START_TIME, System.currentTimeMillis());
        Uri uri = getContentResolver().insert(TaskTrace.Events.CONTENT_URI, values);
        if (uri != null) {
            return Long.parseLong(uri.getLastPathSegment());
        }
        return 0;
    }

    private void endEvent() {
        // End event
        String lastTime = mTimer.getText().toString();
        ContentValues values = new ContentValues();
        values.put(TaskTrace.Events.END_TIME, System.currentTimeMillis());
        values.put(TaskTrace.Events.LAST_TIME, lastTime);
        values.put(TaskTrace.Events.ENDED, 1);
        int count = getContentResolver().update(
                Uri.withAppendedPath(TaskTrace.Events.CONTENT_URI, mCurrentEventId + ""), values,
                null, null);
        if (count > 0) {
            mCurrentEventId = 0;
            mSp.edit().putBoolean(IS_TIMING, false).putLong(EVENT_ID, 0).apply();
        }
    }

    private void loadTasks() {
        Cursor c = getContentResolver().query(TaskTrace.Tasks.CONTENT_URI, null, null, null, null);
        matrixCursor = new MatrixCursor(MATRIXCURSOR_PROJECTION);
        if (null != c) {
            try {
                while (c.moveToNext()) {
                    int id = c.getInt(TASKS_ID_COLUMN);
                    String name = c.getString(TASKS_NAME_COLUMN);
                    int color = c.getInt(TASKS_COLOR_COLUMN);
                    int ended = c.getInt(TASKS_ENDED_COLUMN);
                    mTasks.put(name, id);
                    if (ended == 0) {
                        matrixCursor.addRow(new Object[] {id, name, color});
                    }
                }
            } finally {
                c.close();
            }
        }
        mTaskCount = matrixCursor.getCount();
        // Add
        String name = getResources().getString(R.string.add_task);
        matrixCursor.addRow(new Object[] {-1, name, 0});
    }

    private void chooseTask() {
        Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.choose_task);
        dialogBuilder.setSingleChoiceItems(matrixCursor, -1, TaskTrace.Tasks.NAME, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which < mTaskCount) {
                    matrixCursor.moveToPosition(which);
                    int taskId = matrixCursor.getInt(TASK_ID_COLUMN);
                    String name = matrixCursor.getString(TASK_NAME_COLUMN);
                    start(taskId, name);
                    if (mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                } else {
                    View inputView = (View) LayoutInflater.from(StartTaskActivity.this).inflate(R.layout.dialog_input, null);
                    final EditText nameView = (EditText) inputView.findViewById(R.id.edit_text);
                    new AlertDialog.Builder(StartTaskActivity.this)
                    .setView(inputView)
                    .setCancelable(false)
                    .setTitle(R.string.input_task_name)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String name = nameView.getText().toString();
                            if (mTasks.containsKey(name)) {
                                int taskId = mTasks.get(name);
                                ContentValues values = new ContentValues();
                                values.put(TaskTrace.Tasks.ENDED, 0);
                                int count = getContentResolver().update(
                                        Uri.withAppendedPath(TaskTrace.Tasks.CONTENT_URI, taskId + ""),
                                        values, null, null);
                                if (count > 0) {
                                    loadTasks();
                                    start(taskId, name);
                                }
                            } else if (!TextUtils.isEmpty(name)) {
                                // insert to Tasks table
                                ContentValues values = new ContentValues();
                                values.put(TaskTrace.Tasks.NAME, name);
                                values.put(TaskTrace.Tasks.COLOR, 0);
                                values.put(TaskTrace.Tasks.START_TIME, System.currentTimeMillis());
                                values.put(TaskTrace.Tasks.END_TIME, System.currentTimeMillis());
                                values.put(TaskTrace.Tasks.LAST_STRING, "00:00");
                                Uri uri = getContentResolver().insert(TaskTrace.Tasks.CONTENT_URI, values);
                                Log.d(TAG, "Insert a new Task: " + name);
                                if (uri != null) {
                                    int taskId = Integer.parseInt(uri.getLastPathSegment());
                                    loadTasks();
                                    start(taskId, name);
                                }
                            }
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }
                        }
                    }).show();
                }
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }
        });
        mDialog = dialogBuilder.show();
    }

    private Cursor getCursor() {
        Calendar currentDate = Calendar.getInstance();
        mDayStart = TaskTraceUtils.getDayStart(currentDate);
        mDayEnd = TaskTraceUtils.getDayEnd(currentDate);
        return getContentResolver().query(
                TaskTrace.EVENT_TASK_CONTENT_URI,
                TODAY_PROJECTION,
                "events." + TaskTrace.Events.START_TIME + ">" + mDayStart + " AND events."
                        + TaskTrace.Events.START_TIME + "<" + mDayEnd + " AND events." + TaskTrace.Events.ENDED + "=1", null,
                "events." + TaskTrace.Events.START_TIME + " DESC");
    }

    private class TodayContentObserver extends ContentObserver {
        public TodayContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            if (mCursor.isClosed()) {
                return;
            }
            Log.d(TAG, "TodayContentObserver onChange!");
            mCursor.requery();
        }
    }

    private void registerContentObservers() {
        getContentResolver().registerContentObserver(TaskTrace.Events.CONTENT_URI, true, mObserver);
    }

    private void unRegisterContentObservers() {
        getContentResolver().unregisterContentObserver(mObserver);
    }
}
