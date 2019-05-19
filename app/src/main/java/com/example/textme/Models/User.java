package com.example.textme.Models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable { // Used as User Model
    private String email;
    private String id;
    private String displayName;
    private String userStatus;
    private String userImageUri;

    private int likes = 0;


    public User(String email,String id,String name){
        this.email = email;
        this.displayName = name;
        this.id = id;
    }

    public User(){}


    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }

    protected User(Parcel in) {
        email = in.readString();
        id = in.readString();
        displayName = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String name) {
        this.displayName = name;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getUserImageUri() {
        return userImageUri;
    }

    public void setUserImageUri(String userImageUri) {
        this.userImageUri = userImageUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(id);
        dest.writeString(displayName);
    }
}
