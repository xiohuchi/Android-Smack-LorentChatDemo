package com.lorent.chat.ui.activitys;

import android.os.Handler;

import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.ui.base.MvpPresenter;
import com.lorent.chat.ui.contract.SingleChatContract;

import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;

/**
 * Created by zy on 2016/8/15.
 */
public class SingleChatPresenter extends MvpPresenter<SingleChatContract.View> implements SingleChatContract.Presenter {

    @Override
    public String getHostUid() {
        return MXmppConnManager.hostUid;
    }

    @Override
    public ChatManager getChatManager() {
        return MXmppConnManager.getInstance().getChatManager();
    }

    @Override
    public ChatMessageListener getChatMessageListener() {
        return MXmppConnManager.getInstance().getChatMListener().new MsgProcessListener();
    }

    @Override
    public void sendFile(long mills, long rowid, String type, String file, Handler handler, String toUserId) {
        MXmppConnManager.getInstance().sendFile(mills, rowid, type, file, handler, toUserId);
    }
}
