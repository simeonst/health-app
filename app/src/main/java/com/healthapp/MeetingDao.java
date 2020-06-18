package com.healthapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MeetingDao {
    @Query("SELECT * FROM meetings ORDER BY meetingID DESC")
    List<Meeting> getAllMeetings();

    @Insert
    void InsertMeeting(Meeting meeting);

    @Delete
    void deleteMeeting(Meeting meeting);
}
