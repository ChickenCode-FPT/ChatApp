package com.example.easychat.models;


import java.util.List;
import com.google.firebase.Timestamp;
public class ChatroomModel {
    String roomId;
    List<String> userIds;
    Timestamp lastMessageStamp;
    String lastMessageSenderId;

    String lastMessage;

    boolean isGroup;
    String groupName;
    String groupAvatar;

    public ChatroomModel(){

    }

    public ChatroomModel(String roomId, List<String> userIds, Timestamp lastMessageStamp , String lastMessageSenderId) {
        this.roomId = roomId;
        this.userIds = userIds;
        this.lastMessageSenderId = lastMessageSenderId;
        this.lastMessageStamp = lastMessageStamp;
//        this.isGroup = false;
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

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isGroup() { return isGroup; }
    public void setGroup(boolean group) { isGroup = group; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGroupAvatar() { return groupAvatar; }
    public void setGroupAvatar(String groupAvatar) { this.groupAvatar = groupAvatar; }

}
