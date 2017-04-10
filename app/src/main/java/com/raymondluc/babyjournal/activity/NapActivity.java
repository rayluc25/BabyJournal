package com.raymondluc.babyjournal.activity;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.raymondluc.babyjournal.R;
import com.raymondluc.babyjournal.data.DBHelper;
import com.raymondluc.babyjournal.data.ProviderContract;
import com.raymondluc.babyjournal.databinding.ActivityNapBinding;

public class NapActivity extends AppCompatActivity {

    static final String STATE_BASE = "baseTime";
    static final String STATE_START = "startTime";
    static final String STATE_CHRONO_STARTED = "chronometerStarted";
    static final String STATE_ENTRY_RECORDED = "entryRecorded";
    static final String STATE_START_ENABLED = "startEnabled";
    static final String STATE_STOP_ENABLED = "stopEnabled";
    private static final String TAG = "NapActivity";
    long startTime, stopTime, baseTime;
    boolean entryRecorded;
    boolean chronometerStarted;
    boolean startEnabled;
    boolean stopEnabled;

    ActivityNapBinding mBinding;
    View.OnClickListener mStartListener = new View.OnClickListener() {
        public void onClick(View v) {

            // stoppedMilliseconds can save where the chronometer is paused or stopped, use for resuming
            int stoppedMilliseconds = 0;

            String chronoText = mBinding.chronometer1.getText().toString();
            String array[] = chronoText.split(":");
            if (array.length == 2) {
                stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 1000
                        + Integer.parseInt(array[1]) * 1000;
            } else if (array.length == 3) {
                stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 60 * 1000
                        + Integer.parseInt(array[1]) * 60 * 1000
                        + Integer.parseInt(array[2]) * 1000;
            }

            chronometerStarted = true;

            mBinding.chronometer1.setBase(SystemClock.elapsedRealtime() - stoppedMilliseconds);
            mBinding.chronometer1.start();

            mBinding.button1.setEnabled(false);
            startEnabled = false;

            mBinding.button2.setEnabled(true);
            stopEnabled = true;

            startTime = System.currentTimeMillis();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_nap);

        // Use saved instance values
        if (savedInstanceState != null) {
            // Restore values from saved state
            startTime = savedInstanceState.getLong(STATE_START);
            baseTime = savedInstanceState.getLong(STATE_BASE);
            entryRecorded = savedInstanceState.getBoolean(STATE_ENTRY_RECORDED);
            chronometerStarted = savedInstanceState.getBoolean(STATE_CHRONO_STARTED);
            startEnabled = savedInstanceState.getBoolean(STATE_START_ENABLED);
            stopEnabled = savedInstanceState.getBoolean(STATE_STOP_ENABLED);

            if (startEnabled) mBinding.button1.setEnabled(true);
            else mBinding.button1.setEnabled(false);
            if (stopEnabled) mBinding.button2.setEnabled(true);
            else mBinding.button2.setEnabled(false);

            if (chronometerStarted) {
                mBinding.chronometer1.setBase(baseTime);
                mBinding.chronometer1.start();
            }

        }
        // Initialize for the first instance
        else {
            startEnabled = true;
            stopEnabled = false;
            mBinding.button2.setEnabled(false);
        }

        //Set on click listeners for start and stop buttons
        mBinding.button1.setOnClickListener(mStartListener);

        mBinding.button2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mBinding.chronometer1.stop();
                mBinding.button2.setEnabled(false);
                stopEnabled = false;
                long elapsedMillis = SystemClock.elapsedRealtime() - mBinding.chronometer1.getBase();

                entryRecorded = true;
                stopTime = System.currentTimeMillis();

                // Insert the nap entry into the database
                new InsertEntryTask().execute((long) 1, startTime, stopTime);
            }
        });

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //Stop the chronometer, save the time as pauseTime
        if (chronometerStarted) {
            outState.putLong(STATE_START, startTime);
            outState.putLong(STATE_BASE, mBinding.chronometer1.getBase());
            outState.putBoolean(STATE_CHRONO_STARTED, chronometerStarted);
            outState.putBoolean(STATE_ENTRY_RECORDED, entryRecorded);
            outState.putBoolean(STATE_START_ENABLED, startEnabled);
            outState.putBoolean(STATE_STOP_ENABLED, stopEnabled);
        }
        super.onSaveInstanceState(outState);
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
    public void onBackPressed() {
        if ((mBinding.button1.isEnabled() != true) && (entryRecorded == false)) {
            FragmentManager fm = getSupportFragmentManager();
            DialogFragment dialog = new ExitFeedDialog();
            dialog.show(fm, "fragment_exit_feed_dialog");
        } else
            super.onBackPressed();
    }

    protected class InsertEntryTask extends AsyncTask<Long, Void, Long> {

        @Override
        protected Long doInBackground(Long... longs) {
            long newRowId;
            //Do the synchronous insert here
            DBHelper mDBHelper = new DBHelper(getApplicationContext());
            SQLiteDatabase db = mDBHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(ProviderContract.JournalEntry.COLUMN_NAME_TYPE, longs[0]);
            values.put(ProviderContract.JournalEntry.COLUMN_NAME_START, longs[1]);
            values.put(ProviderContract.JournalEntry.COLUMN_NAME_STOP, longs[2]);

            newRowId = db.insert(ProviderContract.JournalEntry.TABLE_NAME,
                    null, values);
            return newRowId;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            if (aLong < 0) {
                //Show toast
                Toast.makeText(NapActivity.this, "Error, nap entry not recorded :(",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(NapActivity.this, "Nap entry recorded!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
