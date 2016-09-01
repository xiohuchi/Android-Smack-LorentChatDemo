package com.lorent.chat.ui.contract;

import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.ui.base.MvpPresenter;
import com.lorent.chat.utils.SharedPreferences;

/**
 * 退出聊天室
 * Created by zy on 2016/8/15.
 */
public class ExitPresenter extends MvpPresenter<ExitContract.View> implements ExitContract.Presenter {
    @Override
    public void logOut() {
        MXmppConnManager.getInstance().stopChatListener();
        MXmppConnManager.getInstance().feCloseConnection();
        SharedPreferences.getInstance().putBoolean("login", false);

        getIView().logOutSuccess();
    }
}
