package com.example.easychat.models;


import java.util.List;
import com.google.firebase.Timestamp;
public class ChatroomModel {
    String roomId;
    List<String> userIds;
    Timestamp lastMessageStamp;
    String lastMessageSenderId;

    public ChatroomModel(){

    }

    public ChatroomModel(String roomId, List<String> userIds, Timestamp lastMessageStamp , String lastMessageSenderId) {
        this.roomId = roomId;
        this.userIds = userIds;
        this.lastMessageSenderId = lastMessageSenderId;
        this.lastMessageStamp = lastMessageStamp;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public Timestamp getLastMessageStamp() {
        return lastMessageStamp;
    }

    public void setLastMessageStamp(Timestamp lastMessageStamp) {
        this.lastMessageStamp = lastMessageStamp;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }
}
