package com.example.textme.Models;

public class Message { // USED AS MESSAGE MODEL
    private String MessageBody;
    private String Sender;
    private String mId;
    private String mType;

    public Message(String MessageBody, String Sender, String mId, String mType) {
        this.MessageBody = MessageBody;
        this.Sender = Sender;
        this.mId = mId;
        this.mType = mType;
    }

    public Message() {
    }

    public String getMessageBody() {
        return MessageBody;
    }

    public void setMessageBody(String MessageBody) {
        this.MessageBody = MessageBody;
    }

    public String getSender() {
        return Sender;
    }

    public void setSender(String Sender) {
        this.Sender = Sender;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }
}
