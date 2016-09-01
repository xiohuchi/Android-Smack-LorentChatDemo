package com.lorent.chat.common;

import android.app.Application;

import com.lorent.chat.utils.XMPPServer;


public class LorentChatApplication extends Application {

    private static LorentChatApplication mInstance = null;
    private static final String tag = "LorentChatApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        LcUserManager.instance.init(mInstance, new XMPPServer("10.168.100.254", 5222, "lntdev"));

    }

    public static LorentChatApplication getInstance() {
        return mInstance;
    }

}