package com.lorent.chat.ui.entity;

import android.util.Log;

public class ChatRoom {
    private static final String tag = "ChatRoom";
    private String roomJID = null;
    private String roomName = null;
    private String roomDescription = null;
    private String roomSubject = null;
    private int occupantsCount = 0;
    private boolean bJoined = false;

    public ChatRoom(String roomJID, String roomName, String roomDescription, String roomSubject, int occupantsCount) {
        this.roomJID = roomJID;
        this.roomName = roomName;
        this.roomDescription = roomDescription;
        this.roomSubject = roomSubject;
        this.occupantsCount = occupantsCount;
    }

    public String getRoomJID() {
        return this.roomJID;
    }

    public String getRoomName() {
        return this.roomName;
    }

    public String getRoomDescription() {
        return this.roomDescription;
    }

    public String getRoomSubject() {
        return this.roomSubject;
    }

    public int getOccupantsCount() {
        return this.occupantsCount;
    }

    public void setJoinedFlag(boolean joined) {
        Log.e(tag, this.roomJID + " is joined!");
        this.bJoined = joined;
    }

    public boolean getJoinedFlag() {
        return this.bJoined;
    }

    public String getSearchKey() {
        return roomName + " " + roomSubject + " " + roomDescription;
    }
}
