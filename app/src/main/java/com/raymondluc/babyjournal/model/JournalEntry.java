package com.raymondluc.babyjournal.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by raymond on 8/22/2016.
 */
public class JournalEntry implements Parcelable {

    public static final Creator<JournalEntry> CREATOR = new Creator<JournalEntry>() {
        @Override
        public JournalEntry createFromParcel(Parcel in) {
            return new JournalEntry(in);
        }

        @Override
        public JournalEntry[] newArray(int size) {
            return new JournalEntry[size];
        }
    };

    public int id;
    public int type;
    public long start;
    public long stop;

    public JournalEntry() {

    }

    public JournalEntry(int id, int type, long start, long stop) {
        this.id = id;
        this.type = type;
        this.start = start;
        this.stop = stop;
    }

    protected JournalEntry(Parcel in) {
        id = in.readInt();
        type = in.readInt();
        start = in.readLong();
        stop = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(type);
        parcel.writeLong(start);
        parcel.writeLong(stop);
    }
}
