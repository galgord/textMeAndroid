package com.example.textme.Models;

public class Friend { // Used as Friend Model
   public String uid;
   public boolean isFriend;
   public boolean isBlocked;

    public Friend(String uid, boolean isFriend, boolean isBlocked) {
        this.uid = uid;
        this.isFriend = isFriend;
        this.isBlocked = isBlocked;
    }

    public Friend() {
    }


}
