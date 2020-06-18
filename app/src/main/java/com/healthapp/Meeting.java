package com.healthapp;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "meetings")
public class Meeting {
    @PrimaryKey(autoGenerate = true)
    private int meetingID;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "phone_number")
    private String phoneNumber;

    @ColumnInfo(name = "latitude")
    private String latitude;

    @ColumnInfo(name = "longitude")
    private String longitude;

    @ColumnInfo(name = "timeDate")
    private String timeDate;

    @ColumnInfo(name = "photoURI")
    private String photoURI;

    public Meeting(String name, String phoneNumber, String latitude, String longitude, String timeDate, String photoURI) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeDate = timeDate;
        this.photoURI = photoURI;
    }

    public int getMeetingID() {
        return meetingID;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getTimeDate() {
        return timeDate;
    }

    public String getPhotoURI() {
        return photoURI;
    }

    public void setMeetingID(int meetingID) {
        this.meetingID = meetingID;
    }
}
