package com.lorent.chat.ui.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

/**
 * Created by zy on 2016/8/18.
 */
public class ChatPage implements Parcelable {
    private String title;
    private Fragment fragment;

    public ChatPage(String title, Fragment fragment) {
        this.title = title;
        this.fragment = fragment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public String toString() {
        return "ChatPage{" +
                "title='" + title + '\'' +
                ", fragment=" + fragment +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeParcelable((Parcelable) this.fragment, flags);
    }

    protected ChatPage(Parcel in) {
        this.title = in.readString();
        this.fragment = in.readParcelable(Fragment.class.getClassLoader());
    }

    public static final Creator<ChatPage> CREATOR = new Creator<ChatPage>() {
        @Override
        public ChatPage createFromParcel(Parcel source) {
            return new ChatPage(source);
        }

        @Override
        public ChatPage[] newArray(int size) {
            return new ChatPage[size];
        }
    };
}
