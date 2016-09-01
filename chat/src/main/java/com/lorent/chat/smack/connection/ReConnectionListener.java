package com.lorent.chat.smack.connection;

import android.os.Handler;
import android.os.Message;

import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.utils.XLog;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

public class ReConnectionListener implements ConnectionListener {

    private static final String tag = "ReConnectionListener";
    private Handler connHandler = null;

    public ReConnectionListener(Handler connHandler) {
        this.connHandler = connHandler;
    }

    @Override
    public void connectionClosed() {
        XLog.e(tag, "on connectionClosed");
        String strMsg = "连接已被关闭。。。";
        sendHandlerMessage(CustomConst.XMPP_CONNECTION_CALLBACK.XMPP_CONNECTION_CLOSED, strMsg);
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        XLog.e(tag, "on connectionClosedOnError");
        if (e.getMessage().equals("stream:error (conflict)")) {
            String strMsg = "账号在别处登录";
            sendHandlerMessage(CustomConst.XMPP_CONNECTION_CALLBACK.XMPP_CONNECTION_CLOSED_SOMEWHERE_LOGIN, strMsg);
        } else {
            String strMsg = "连接异常断开";
            sendHandlerMessage(CustomConst.XMPP_CONNECTION_CALLBACK.XMPP_CONNECTION_CLOSED_ONERROR, strMsg);
        }
    }

    @Override
    public void reconnectionFailed(Exception e) {
        XLog.e(tag, "on connectionClosedOnError");
        String strMsg = "重新连接失败，连接断开。。。";
        sendHandlerMessage(CustomConst.XMPP_CONNECTION_CALLBACK.XMPP_CONNECTION_RECONNECTIONFAILED, strMsg);
    }

    @Override
    public void reconnectionSuccessful() {
        XLog.e(tag, "on reconnectionSuccessful");
        String strMsg = "重新连接服务器成功。。。";
        sendHandlerMessage(CustomConst.XMPP_CONNECTION_CALLBACK.XMPP_CONNECTION_RECONNECTIONSUCCESSFUL, strMsg);
    }

    @Override
    public void reconnectingIn(int arg0) {
        XLog.e(tag, "on reconnectingIn");
        String strMsg = "重新连接服务器中。。。";
        sendHandlerMessage(CustomConst.XMPP_CONNECTION_CALLBACK.XMPP_CONNECTION_RECONNECTINGIN, strMsg);
    }

    @Override
    public void authenticated(XMPPConnection arg0, boolean arg1) {
        XLog.e(tag, "on authenticated");
        String strMsg = "连接认证完毕。。。";
        sendHandlerMessage(CustomConst.XMPP_CONNECTION_CALLBACK.XMPP_CONNECTION_AUTHENTICATED, strMsg);
    }

    @Override
    public void connected(XMPPConnection arg0) {
        XLog.e(tag, "on connected");
        String strMsg = "连接服务器成功。。。";
        sendHandlerMessage(CustomConst.XMPP_CONNECTION_CALLBACK.XMPP_CONNECTION_CONNECTED, strMsg);
    }

    private void sendHandlerMessage(CustomConst.XMPP_CONNECTION_CALLBACK xmppStatus, String strMsg) {
        Message msg = new Message();

        msg.what = CustomConst.XMPP_HANDLER_WHAT.XMPP_HANDLER_CONNECTION_CALLBACK.ordinal();
        msg.arg1 = xmppStatus.ordinal();
        msg.obj = strMsg;

        XLog.e(tag, "on sendHandlerMessage : " + strMsg);

        connHandler.sendMessage(msg);
    }
}
