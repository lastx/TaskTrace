package com.deemix.tasktrace;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.deemix.tasktrace.provider.TaskTrace;

public class TasksActivity extends Activity implements OnItemClickListener{

    private ListView mList;
    private Cursor mCursor;
    private HistoryListAdapter mAdapter;
    private TasksContentObserver mObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_list);
        mList = (ListView) findViewById(android.R.id.list);
        TextView tv = (TextView) findViewById(R.id.fragment_info);
        tv.setText(R.string.fragment_info_tasks);

        mCursor = getContentResolver().query(TaskTrace.Tasks.CONTENT_URI, null, null, null, null);
        mAdapter = new HistoryListAdapter(this, mCursor);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
        mObserver = new TasksContentObserver();
        registerContentObservers();
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        final int id = cursor.getInt(0);
        final int state = cursor.getInt(7);
        int resId = 0;
        if (state == 1) {
            resId = R.string.reopen_task_title;
        } else if (state == 0) {
            resId = R.string.finish_task_title;
        }

        new AlertDialog.Builder(this)
        .setMessage(resId)
        .setPositiveButton(android.R.string.ok,  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ContentValues values = new ContentValues();
                if (state == 0) {
                    values.put(TaskTrace.Tasks.ENDED, 1);
                } else {
                    values.put(TaskTrace.Tasks.ENDED, 0);
                }
                getContentResolver().update(
                        Uri.withAppendedPath(TaskTrace.Tasks.CONTENT_URI, id + ""), values,
                        null, null);
           }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
    }

    private class HistoryListAdapter extends ResourceCursorAdapter {
        private Context mContext;

        public HistoryListAdapter(Context context, Cursor cursor) {
            super(context, R.layout.task_overall_item, cursor, true);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            String name = cursor.getString(1);
            long start = cursor.getLong(2);
            long end = cursor.getLong(3);
            String last = cursor.getString(5);
            long events = cursor.getLong(6);
            int state = cursor.getInt(7);

            String range = DateUtils.formatDateRange(context, start, end, DateUtils.FORMAT_SHOW_DATE);
            holder.name.setText(name);
            holder.range.setText(range);
            holder.last.setText(last);
            holder.events.setText(events + "");
            if (state == 0) {
                holder.state.setText(R.string.task_state_going);
                holder.state.setTextColor(Color.GRAY);
            } else {
                holder.state.setText(R.string.task_state_finish);
                holder.state.setTextColor(Color.RED);
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = super.newView(context, cursor, parent);
            view.setTag(new ViewHolder(view));
            return view;
        }

        private class ViewHolder {
            public final TextView name;
            public final TextView range;
            public final TextView events;
            public final TextView last;
            public final TextView state;

            public ViewHolder(View view) {
                name = (TextView) view.findViewById(R.id.task_overall_name);
                range = (TextView) view.findViewById(R.id.task_overall_range);
                last = (TextView) view.findViewById(R.id.task_overall_last);
                events = (TextView) view.findViewById(R.id.task_overall_event_count);
                state = (TextView) view.findViewById(R.id.task_overall_state);
            }
        }
    }

    private class TasksContentObserver extends ContentObserver {
        public TasksContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            if (mCursor.isClosed()) {
                return;
            }
            mCursor.requery();
        }
    }

    private void registerContentObservers() {
        getContentResolver().registerContentObserver(TaskTrace.Tasks.CONTENT_URI, true, mObserver);
    }

    private void unRegisterContentObservers() {
        getContentResolver().unregisterContentObserver(mObserver);
    }
}
