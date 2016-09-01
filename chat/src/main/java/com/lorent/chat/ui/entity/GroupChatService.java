package com.lorent.chat.ui.entity;

import java.util.List;

public class GroupChatService {
    private String gcsJid;
    private String gcsName;
    private List<ChatRoom> listChatRoom;

    public GroupChatService(String jid, String name, List<ChatRoom> chatRooms) {
        this.gcsJid = jid;
        this.gcsName = name;
        this.listChatRoom = chatRooms;
    }

    public void setGcsJID(String jid) {
        this.gcsJid = jid;
    }

    public void setGcsName(String name) {
        this.gcsName = name;
    }

    public String getGcsJID() {
        return this.gcsJid;
    }

    public String getGcsName() {
        return this.gcsName;
    }

    public List<ChatRoom> getChatRooms() {
        return this.listChatRoom;
    }

}
