package com.deemix.tasktrace;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.deemix.tasktrace.provider.TaskTrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class OneDayActivity extends FragmentActivity implements ActionBar.TabListener {

    private static final String TAG                                         = "OneDayActivity";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    private static long mDayStart;
    private static long mDayEnd;

    private static final String[] TODAY_PROJECTION                          = new String[] {
        "events." + TaskTrace.Events._ID,
        "events." + TaskTrace.Events.START_TIME,
        "events." + TaskTrace.Events.END_TIME,
        "events." + TaskTrace.Events.LAST_TIME,
        TaskTrace.Tasks.NAME,
    };
    private static final int TODAY_EVENT_ID_COLUMN                           = 0;
    private static final int TODAY_EVENT_START_TIME_COLUMN                   = 1;
    private static final int TODAY_EVENT_END_TIME_COLUMN                     = 2;
    private static final int TODAY_ENVET_LAST_TIME_COLUMN                    = 3;
    private static final int TODAY_TASK_NAME_COLUMN                          = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_day);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        mDayStart = intent.getLongExtra(TaskTraceUtils.INTENT_DAY_START, 0);
        mDayEnd = intent.getLongExtra(TaskTraceUtils.INTENT_DAY_END, 0);
        if (mDayStart == 0 || mDayEnd == 0) {
            Log.e(TAG, "Invalid Day");
            finish();
        }
        setTitle(DateUtils.formatDateTime(this, mDayStart, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY));

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding tab.
        // We can also use ActionBar.Tab#select() to do this if we have a reference to the
        // Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new DummySectionFragment();
            Bundle args = new Bundle();
            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.title_section1).toUpperCase();
                case 1: return getString(R.string.title_section2).toUpperCase();
            }
            return null;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        public DummySectionFragment() {
        }

        public static final String ARG_SECTION_NUMBER = "section_number";

        private int mIndex;
        private Activity mActivity;
        private ListView mList;
        private Cursor mCursor;
        private ListAdapter mListAdapter;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_list, null);
            mActivity = getActivity();
            Bundle args = getArguments();
            mIndex = args.getInt(ARG_SECTION_NUMBER);
            mList = (ListView) rootView.findViewById(android.R.id.list);
            TextView tv = (TextView) rootView.findViewById(R.id.fragment_info);

            mCursor = getCursor();
            if (mIndex == 1) {
                mListAdapter = convertAdapter(mCursor);
                tv.setText(R.string.fragment_info_overall);
            } else if (mIndex == 2) {
                tv.setText(R.string.fragment_info_detail);
                mListAdapter = new TodayListAdapter(mActivity, mCursor);
            }
            mList.setAdapter(mListAdapter);
            return rootView;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (mCursor != null) {
                mCursor.close();
            }
        }

        private Cursor getCursor() {
            return mActivity.getContentResolver().query(
                    TaskTrace.EVENT_TASK_CONTENT_URI,
                    TODAY_PROJECTION,
                    "events." + TaskTrace.Events.START_TIME + ">" + mDayStart + " AND events."
                            + TaskTrace.Events.START_TIME + "<" + mDayEnd + " AND events." + TaskTrace.Events.ENDED + "=1", null,
                    "events." + TaskTrace.Events.START_TIME + " DESC");
        }

        private ListAdapter convertAdapter(Cursor c) {
            if (c == null) {
                return null;
            }
            return new SimpleAdapter(mActivity, getData(c), R.layout.task_list_item,
                    new String[] {
                            "task_name", "range", "count", "last"
                    },
                    new int[] {
                            R.id.this_task_name, R.id.task_range, R.id.task_event_count, R.id.task_last
                    });
        }

        private List<HashMap<String, String>> getData(Cursor c) {
            List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> map;

            HashMap<String, TaskData> taskMap = new HashMap<String, TaskData>();
            TaskData td = null;

            try {
                while (c.moveToNext()) {
                    String name = c.getString(TODAY_TASK_NAME_COLUMN);
                    long startTime = c.getLong(TODAY_EVENT_START_TIME_COLUMN);
                    long endTime = c.getLong(TODAY_EVENT_END_TIME_COLUMN);
                    String last = c.getString(TODAY_ENVET_LAST_TIME_COLUMN);
                    EventData ed = new EventData(mActivity, name, startTime, endTime, last);
                    if (taskMap.containsKey(name)) {
                        td = taskMap.get(name);
                        td.addEvent(ed);
                    } else {
                        td = new TaskData(mActivity, ed);
                    }
                    taskMap.put(name, td);
                }
            } finally {
                c.close();
            }

            if (taskMap.size() > 0) {
                Iterator<Entry<String, TaskData>> iter = taskMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, TaskData> entry = iter.next();
                    String key = entry.getKey();
                    TaskData value = entry.getValue();
                    map = new HashMap<String, String>();
                    map.put("task_name", value.getName());
                    map.put("range", value.getRange());
                    map.put("count", value.getEventCount() + "");
                    map.put("last", value.getLast());
                    list.add(map);
                } 
            }
            return list;
        }
    }
}
