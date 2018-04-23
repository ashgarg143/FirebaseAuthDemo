package com.example.ashis.firebaseauthdemo;

public class UserInformation {

    public String userName;
    public String address;
    public String url;

    public String getUser() {
        return userName;
    }

    public String getAddress() {
        return address;
    }

    public String getUrl() {
        return url;
    }

    public UserInformation() {
    }

    public UserInformation(String user, String address, String url) {
        this.userName = user;
        this.address = address;
        this.url=url;

    }


}
