package com.deemix.tasktrace;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.deemix.tasktrace.provider.TaskTrace;

import java.util.Calendar;

public class HistoryActivity extends Activity implements OnItemClickListener{

    private ListView mList;
    private Cursor mCursor;
    private HistoryListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_list);
        mList = (ListView) findViewById(android.R.id.list);
        TextView tv = (TextView) findViewById(R.id.fragment_info);
        tv.setText(R.string.fragment_info_history);

        mCursor = getContentResolver().query(TaskTrace.Daily.CONTENT_URI, null, null, null,
                TaskTrace.Daily.START_TIME + " DESC");
        mAdapter = new HistoryListAdapter(this, mCursor);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        long time = cursor.getLong(4);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        long st = TaskTraceUtils.getDayStart(c);
        long et = TaskTraceUtils.getDayEnd(c);
        Intent i = new Intent();
        i.putExtra(TaskTraceUtils.INTENT_DAY_START, st);
        i.putExtra(TaskTraceUtils.INTENT_DAY_END, et);
        i.setClass(this, OneDayActivity.class);
        startActivity(i);
    }

    private class HistoryListAdapter extends ResourceCursorAdapter {
        private Context mContext;

        public HistoryListAdapter(Context context, Cursor cursor) {
            super(context, R.layout.daily_list_item, cursor, true);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            long start = cursor.getLong(4);
            long end = cursor.getLong(5);
            String last = cursor.getString(7);
            int tasks = cursor.getInt(2);
            long events = cursor.getLong(3);

            String date = DateUtils.formatDateTime(mContext, start, DateUtils.FORMAT_SHOW_DATE);
            String range = DateUtils.formatDateRange(context, start, end, DateUtils.FORMAT_SHOW_TIME);
            holder.date.setText(date);
            holder.range.setText(range);
            holder.last.setText(last);
            holder.tasks.setText(tasks + "");
            holder.events.setText(events + "");
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = super.newView(context, cursor, parent);
            view.setTag(new ViewHolder(view));
            return view;
        }

        private class ViewHolder {
            public final TextView date;
            public final TextView range;
            public final TextView tasks;
            public final TextView events;
            public final TextView last;

            public ViewHolder(View view) {
                date = (TextView) view.findViewById(R.id.daily_name);
                range = (TextView) view.findViewById(R.id.daily_range);
                last = (TextView) view.findViewById(R.id.daily_last);
                tasks = (TextView) view.findViewById(R.id.daily_task_count);
                events = (TextView) view.findViewById(R.id.daily_event_count);
            }
        }
    }
}
