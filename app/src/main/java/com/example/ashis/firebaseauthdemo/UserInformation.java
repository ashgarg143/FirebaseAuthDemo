package com.example.ashis.firebaseauthdemo;

public class UserInformation {


    public UserInformation(String userName, String address, String url, boolean isVerified) {
        this.userName = userName;
        this.address = address;
        this.url = url;
        this.isVerified = isVerified;
    }

    public String userName;
    public String address;
    public String url;
    public boolean isVerified;


    public UserInformation() {
    }

    public UserInformation(String user, String address, String url) {
        this.userName = user;
        this.address = address;
        this.url=url;

    }


}
