package com.xtagwgj.chatdemo;

import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.common.LorentChatApplication;
import com.lorent.chat.utils.XMPPServer;

/**
 * Created by zy on 2016/8/30.
 */
public class App extends LorentChatApplication {
    private static App mInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        LcUserManager.instance.init(mInstance, new XMPPServer("10.168.100.254", 5222, "lntdev"));
    }

    public static App getmInstance() {
        return mInstance;
    }
}