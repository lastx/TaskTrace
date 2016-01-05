package com.deemix.tasktrace;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class TodayListAdapter extends ResourceCursorAdapter {

    private Context mContext;

    public TodayListAdapter(Context context, Cursor cursor) {
        super(context, R.layout.event_list_item, cursor, true);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String taskName = cursor.getString(StartTaskActivity.TODAY_TASK_NAME_COLUMN);
        long start = cursor.getLong(StartTaskActivity.TODAY_EVENT_START_TIME_COLUMN);
        long end = cursor.getLong(StartTaskActivity.TODAY_EVENT_END_TIME_COLUMN);
        String last = cursor.getString(StartTaskActivity.TODAY_ENVET_LAST_TIME_COLUMN);

        String range = DateUtils.formatDateRange(context, start, end, DateUtils.FORMAT_SHOW_TIME);
        holder.taskName.setText(taskName);
        holder.range.setText(range);
        holder.last.setText(last);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        view.setTag(new ViewHolder(view));
        return view;
    }

    private static class ViewHolder {
        public final TextView taskName;
        public final TextView range;
        public final TextView last;

        public ViewHolder(View view) {
            taskName = (TextView) view.findViewById(R.id.today_task_name);
            range = (TextView) view.findViewById(R.id.today_range);
            last = (TextView) view.findViewById(R.id.today_last);
        }
    }
}
