package com.lorent.chat.smack;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.common.LorentChatApplication;
import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.smack.connection.OfflineMessageSendBrocast;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.utils.XLog;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.offline.OfflineMessageManager;

import java.util.Map;

/**
 * 注册和登录工具
 * Created by zy on 2016/8/31.
 */
public class LoginAndRegisterHelper {
    private XMPPTCPConnection connection;
    private String tag = LoginAndRegisterHelper.class.getSimpleName();

    public LoginAndRegisterHelper() {
        connection = MXmppConnManager.getInstance().getConnection();
    }

    /**
     * 注册用户
     *
     * @param username   账号
     * @param password   账号密码
     * @param attributes 账号其他属性，参考AccountManager.getAccountAttributes()的属性介绍
     * @return
     */
    public boolean registerUser(String username, String password, Map<String, String> attributes) {
        XLog.e(tag, "注册用户信息");
        if (connection == null || !connection.isConnected()) {
            XLog.e(tag, "connection 为空或未连接");
            return false;
        }

        try {
            AccountManager.getInstance(connection).createAccount(username, password, attributes);
            return true;
        } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException
                | SmackException.NotConnectedException e) {
            XLog.e(tag, "注册失败");
            return false;
        }
    }

    /****
     * 用户登录
     *
     * @param userName 账号
     * @param passwd   密码
     * @return 登录是否成功
     */
    public boolean feXmppLogin(String userName, String passwd, Context context, Handler handler) {
        XLog.e(tag, "用户登录");
        if (connection == null || !connection.isConnected()) {
            XLog.e(tag, "connection 为空或未连接");
            return false;
        }

        try {
            XLog.e(tag, "login : " + userName + ":" + passwd);
            connection.login(userName, passwd);
            if (connection.getUser() == null) {
                XLog.e(tag, "login connection.getUser()  : " + connection.getUser());
                return false;
            }
            MXmppConnManager.getInstance().setOffLineMessageManager(new OfflineMessageManager(connection));
            MXmppConnManager.hostUid = connection.getUser();
            MXmppConnManager.getInstance().startChatLinstener();

            OfflineMessageSendBrocast.sendBrocast(context, OfflineMessageSendBrocast.getOfflineMegs());
            LcUserManager.instance.setUpUserInfo(userName, passwd);
            return true;

        } catch (Exception e) {
            if (e instanceof SmackException.AlreadyLoggedInException) {
                LcUserManager.instance.setUpUserInfo(userName, passwd);
                return true;
            }

            e.printStackTrace();
            if (e.getMessage().equals("not-authorized(401)")) {
                Message msg = new Message();
                msg.what = CustomConst.XMPP_HANDLER_WHAT.XMPP_HANDLER_ERROR.ordinal();
                msg.arg1 = CustomConst.XMPP_HANDLER_ERROR.XMPP_ERROR_LOGINFAIL.ordinal();
                handler.sendMessage(msg);
            }
            return false;
        }
    }

    /**
     * 后台自动登录
     */
    public void bgXmppLogin() {
        XLog.e(tag, "后台登录，无法获取离线信息，待处理优化");
        feXmppLogin(
                LcUserManager.instance.getUserName(),
                LcUserManager.instance.getUserPwd(),
                LorentChatApplication.getInstance(),
                MXmppConnManager.getInstance().getConnMessageHandler());
    }
}