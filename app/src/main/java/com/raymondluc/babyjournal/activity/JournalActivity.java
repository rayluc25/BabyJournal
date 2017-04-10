package com.raymondluc.babyjournal.activity;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.raymondluc.babyjournal.R;
import com.raymondluc.babyjournal.data.ProviderContract;
import com.raymondluc.babyjournal.databinding.ActivityJournalBinding;
import com.raymondluc.babyjournal.databinding.CardJournalEntryBinding;
import com.raymondluc.babyjournal.model.JournalEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class JournalActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Database items
    static final int COL_ID = 0;
    static final int COL_TYPE = 1;
    static final int COL_START = 2;
    static final int COL_STOP = 3;
    private static final String[] ENTRY_COLUMNS = {
            ProviderContract.JournalEntry.TABLE_NAME + "." + ProviderContract.JournalEntry._ID,
            ProviderContract.JournalEntry.COLUMN_NAME_TYPE,
            ProviderContract.JournalEntry.COLUMN_NAME_START,
            ProviderContract.JournalEntry.COLUMN_NAME_STOP
    };
    ActivityJournalBinding mBinding;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_journal);

        getLoaderManager().initLoader(0, null, this);

        mRecyclerView = (RecyclerView) mBinding.journalRecyclerView;
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.grid_items), LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Hook up an adapter to the recycler view
        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                ProviderContract.JournalEntry.CONTENT_URI,
                ENTRY_COLUMNS,
                null,
                null,
                ProviderContract.JournalEntry.COLUMN_NAME_START + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() == 0) {
            mBinding.journalRecyclerView.setVisibility(View.GONE);
            mBinding.emptyView.setVisibility(View.VISIBLE);

        } else {
            mBinding.journalRecyclerView.setVisibility(View.VISIBLE);
            mBinding.emptyView.setVisibility(View.GONE);
            mAdapter.setData(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setData(null);
    }

    protected class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        protected CardJournalEntryBinding cardJournalEntryBinding;
        private ArrayList<JournalEntry> mDataSet;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_journal_entry, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            JournalEntry entry = mDataSet.get(position);
            String formattedType = null;
            String formattedDuration = null;
            String formattedDate = null;

            if (entry.type == 0)
                formattedType = "Feed";
            else formattedType = "Nap";

            SimpleDateFormat twelveHourFormat = new SimpleDateFormat("h:mm a");
            Date startTime = new Date(entry.start);
            Date stopTime = new Date(entry.stop);
            formattedDuration = twelveHourFormat.format(startTime) + " - " + twelveHourFormat.format(stopTime);

            SimpleDateFormat dayAndDateFormat = new SimpleDateFormat("EEE, MMM d, ''yy");
            formattedDate = dayAndDateFormat.format(startTime);

            cardJournalEntryBinding.activityType.setText(formattedType);
            cardJournalEntryBinding.duration.setText(formattedDuration);
            cardJournalEntryBinding.activityDate.setText(formattedDate);
        }

        @Override
        public int getItemCount() {
            return mDataSet != null ? mDataSet.size() : 0;
        }

        public void setData(Cursor cursor) {
            mDataSet = new ArrayList<>();
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    mDataSet.add(getEntryFromCursor(cursor));
                } while (cursor.moveToNext());
            }

        }

        public JournalEntry getEntryFromCursor(Cursor data) {
            return new JournalEntry(data.getInt(COL_ID),
                    data.getInt(COL_TYPE),
                    data.getLong(COL_START),
                    data.getLong(COL_STOP));
        }

        protected class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(View entryView) {
                super(entryView);
                cardJournalEntryBinding = DataBindingUtil.bind(entryView);
            }

        }


    }
}
