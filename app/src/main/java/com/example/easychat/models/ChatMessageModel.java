package com.example.easychat.models;

import com.google.firebase.Timestamp;

public class ChatMessageModel {
    private String message;
    private String senderId;
    private Timestamp timesStamp;

    public ChatMessageModel(){

    }

    public ChatMessageModel(String message, String senderId , Timestamp timesStamp) {
        this.message = message;
        this.timesStamp = timesStamp;
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Timestamp getTimesStamp() {
        return timesStamp;
    }

    public void setTimesStamp(Timestamp timesStamp) {
        this.timesStamp = timesStamp;
    }
}
