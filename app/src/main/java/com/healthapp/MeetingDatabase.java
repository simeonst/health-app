package com.healthapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = { Meeting.class }, version = 1, exportSchema = false)
public abstract class MeetingDatabase extends RoomDatabase {
    private static final String DB_NAME = "meeting_db";
    private static MeetingDatabase instance;

    // TODO: remove MainThreadQueries --> make it run in background or use executors
    public static synchronized MeetingDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), MeetingDatabase.class, DB_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract MeetingDao meetingDao();
}
