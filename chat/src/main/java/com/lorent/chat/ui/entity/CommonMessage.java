package com.lorent.chat.ui.entity;

import com.lorent.chat.smack.engien.MsgEume.CHAT_STATES;
import com.lorent.chat.smack.engien.MsgEume.MSG_CONTENT_TYPE;
import com.lorent.chat.smack.engien.MsgEume.MSG_DERATION;
import com.lorent.chat.smack.engien.MsgEume.MSG_STATE;

import java.io.Serializable;

public class CommonMessage extends Entity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private int id;
    private String uid;
    private String name;
    private String avatar;
    private long time;
    private String distance;
    private String content;
    private MSG_CONTENT_TYPE contentType;
    private MSG_DERATION msgComeFromType;
    private MSG_STATE state;
    private CHAT_STATES chatstates = CHAT_STATES.STNULL;

    public CommonMessage(int id, String uid, String avatar, long time, String distance,
                         String content, MSG_STATE state, MSG_CONTENT_TYPE contentType,
                         MSG_DERATION msgComeFromType, String name) {
        super();
        this.id = id;
        this.uid = uid;
        this.avatar = avatar;
        this.time = time;
        this.distance = distance;
        this.content = content;
        this.state = state;
        this.contentType = contentType;
        this.msgComeFromType = msgComeFromType;
        this.name = name;
    }

    public CommonMessage(String uid, String avatar, long time, String distance,
                         String content, MSG_STATE state, MSG_CONTENT_TYPE contentType,
                         MSG_DERATION msgComeFromType, String name, CHAT_STATES chatstates) {
        super();
        this.uid = uid;
        this.avatar = avatar;
        this.time = time;
        this.distance = distance;
        this.content = content;
        this.state = state;
        this.contentType = contentType;
        this.msgComeFromType = msgComeFromType;
        this.name = name;
        this.chatstates = chatstates;
    }

    public CommonMessage(String avatar, long time, String distance,
                         String content, MSG_CONTENT_TYPE contentType,
                         MSG_DERATION msgComeFromType, CHAT_STATES chatstates) {
        super();
        this.avatar = avatar;
        this.time = time;
        this.distance = distance;
        this.content = content;
        this.contentType = contentType;
        this.msgComeFromType = msgComeFromType;
        this.chatstates = chatstates;
    }

    public CHAT_STATES getChatstates() {
        return chatstates;
    }

    public void setChatstates(CHAT_STATES chatstates) {
        this.chatstates = chatstates;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public MSG_CONTENT_TYPE getContentType() {
        return contentType;
    }

    public void setContentType(MSG_CONTENT_TYPE contentType) {
        this.contentType = contentType;
    }

    public MSG_DERATION getMsgComeFromType() {
        return msgComeFromType;
    }

    public void setMsgComeFromType(MSG_DERATION msgComeFromType) {
        this.msgComeFromType = msgComeFromType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public MSG_STATE getState() {
        return state;
    }

    public void setState(MSG_STATE state) {
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CommonMessage{" +
                "id=" + id +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", avatar='" + avatar + '\'' +
                ", time=" + time +
                ", distance='" + distance + '\'' +
                ", content='" + content + '\'' +
                ", contentType=" + contentType +
                ", msgComeFromType=" + msgComeFromType +
                ", state=" + state +
                ", chatstates=" + chatstates +
                '}';
    }
}