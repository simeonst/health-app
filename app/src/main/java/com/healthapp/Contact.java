package com.healthapp;

public class Contact {

    private String name;
    private String phoneNumber;
    private String photoURI;

    public Contact(String name, String phoneNumber, String photo) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.photoURI = photo;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPhoto() {
        return photoURI;
    }
}

