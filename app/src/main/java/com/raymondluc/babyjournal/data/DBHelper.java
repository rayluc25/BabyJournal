package com.raymondluc.babyjournal.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by raymond on 8/18/2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "BabyJournal.db";

    private static final String INT_TYPE = " INTEGER NOT NULL";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ProviderContract.JournalEntry.TABLE_NAME + " (" +
                    ProviderContract.JournalEntry._ID + " INTEGER PRIMARY KEY," +
                    ProviderContract.JournalEntry.COLUMN_NAME_TYPE + INT_TYPE + COMMA_SEP +
                    ProviderContract.JournalEntry.COLUMN_NAME_START + INT_TYPE + COMMA_SEP +
                    ProviderContract.JournalEntry.COLUMN_NAME_STOP + INT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProviderContract.JournalEntry.TABLE_NAME;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ProviderContract.JournalEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
