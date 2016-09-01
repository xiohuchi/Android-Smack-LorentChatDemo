package com.lorent.chat.ui.contract;

import android.os.Handler;

import com.lorent.chat.ui.base.BasePresenter;
import com.lorent.chat.ui.base.BaseView;

import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;

/**
 * 一对一聊天
 * Created by zy on 2016/8/15.
 */
public class SingleChatContract {
    public interface View extends BaseView {

    }

    public interface Presenter extends BasePresenter {
        String getHostUid();

        ChatManager getChatManager();

        ChatMessageListener getChatMessageListener();

        void sendFile(long mills, long rowid, String type, String file, Handler handler, String toUserId);

    }
}
