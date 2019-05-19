package com.example.textme.Models;

public class Story {

    String userName;
    String userProfileImage;
    String storyId;

    public Story(String userName, String userProfileImage, String storyId) {
        this.userName = userName;
        this.userProfileImage = userProfileImage;
        this.storyId = storyId;
    }

    public Story() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    @Override
    public String toString() {
        return "Story{" +
                "userName='" + userName + '\'' +
                ", userProfileImage='" + userProfileImage + '\'' +
                ", storyId='" + storyId + '\'' +
                '}';
    }
}
