package com.raymondluc.babyjournal.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.raymondluc.babyjournal.R;
import com.raymondluc.babyjournal.data.ProviderContract;

import java.text.SimpleDateFormat;
import java.util.Date;


public class JournalWidgetRemoteViewsServices extends RemoteViewsService {

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


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        // return remote view factory
        return new RemoteViewsFactory() {
            Cursor mCursor = null;

            @Override
            public void onCreate() {
                // nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (mCursor != null)
                    mCursor.close();
                long identityToken = Binder.clearCallingIdentity();
                mCursor = getContentResolver().query(ProviderContract.JournalEntry.CONTENT_URI,
                        ENTRY_COLUMNS,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (mCursor != null) {
                    mCursor.close();
                    mCursor = null;
                }
            }

            @Override
            public int getCount() {
                return mCursor == null ? 0 : mCursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_journal_entry);
                if (mCursor != null && mCursor.moveToPosition(position)) {
                    String formattedType = null;
                    String formattedDuration = null;
                    String formattedDate = null;

                    if (mCursor.getInt(COL_TYPE) == 0) formattedType = "Feed";
                    else formattedType = "Nap";

                    SimpleDateFormat twelveHourFormat = new SimpleDateFormat("h:mm a");
                    Date startTime = new Date(mCursor.getLong(COL_START));
                    Date stopTime = new Date(mCursor.getLong(COL_STOP));
                    formattedDuration = twelveHourFormat.format(startTime) + " - " + twelveHourFormat.format(stopTime);

                    SimpleDateFormat dayAndDateFormat = new SimpleDateFormat("EEE, MMM d, ''yy");
                    formattedDate = dayAndDateFormat.format(startTime);

                    remoteViews.setTextViewText(R.id.type, formattedType);
                    remoteViews.setTextViewText(R.id.start, formattedDuration);
                    remoteViews.setTextViewText(R.id.date, formattedDate);
                }
                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}