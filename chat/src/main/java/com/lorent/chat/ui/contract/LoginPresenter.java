package com.lorent.chat.ui.contract;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.common.LorentChatApplication;
import com.lorent.chat.smack.LoginAndRegisterHelper;
import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.ui.base.MvpPresenter;
import com.lorent.chat.utils.SharedPreferences;
import com.lorent.chat.utils.XLog;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.util.concurrent.ExecutionException;

/**
 * 登录的处理器
 * Created by zy on 2016/8/11.
 */
public class LoginPresenter extends MvpPresenter<LoginContract.View> implements LoginContract.Presenter {

    public static final String tag = "LoginPresenter";

    @Override
    public void toLogin(String account, String password) {
        XMPPTCPConnection connection = MXmppConnManager.getInstance().getConnection();
        if (connection == null || !connection.isConnected()) {
            getIView().loginFail("toLogin() connection null");
            return ;
        }

        if (!checkParameter(account, password)) {
            return;
        }
        XLog.d("正在登录");
//        boolean success = MXmppConnManager.getInstance().feXmppLogin(account, password, AppManager.instance.currentActivity(), handler);
//        if (success) {
//            loginSuccessAndSaveInfo();
//        }
        handler.postDelayed(new LoginRunnable(account, password), 100);
    }

    class LoginRunnable implements Runnable {
        String name;
        String pwd;

        public LoginRunnable(String username, String password) {
            this.name = username;
            this.pwd = password;
        }

        @Override
        public void run() {

            try {
                MXmppConnManager.getInstance().new InitXmppConnectionTask(
                        handler, tag).execute().get();
            } catch (InterruptedException | ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            boolean success =  new LoginAndRegisterHelper().feXmppLogin(name, pwd,
                    LorentChatApplication.getInstance(), handler);
            Message msg = new Message();
            msg.what = CustomConst.XMPP_HANDLER_WHAT.XMPP_HANDLER_ERROR.ordinal();
            if (success) {
                LcUserManager.instance.setUpUserInfo(name, pwd);
                msg.arg1 = CustomConst.XMPP_HANDLER_ERROR.XMPP_ERROR_LOGINSUCCESSED.ordinal();
            } else
                msg.arg1 = CustomConst.XMPP_HANDLER_ERROR.XMPP_ERROR_LOGINFAIL.ordinal();

            handler.sendMessage(msg);
        }
    }

    /**
     * 检查输入参数的有效性
     *
     * @return 参数的有效性
     */
    private boolean checkParameter(String username, String password) {
        XLog.d("检查输入用户名密码是否有效" + username + "-->" + password);
        try {
            if (TextUtils.isEmpty(username)) {
                getIView().loginFail("用户名不正确");
                return false;
            }

            if (TextUtils.isEmpty(password)) {
                getIView().loginFail("密码不正确");
                return false;
            }
            XLog.d("用户名密码输入正确");
        } catch (Exception e) {
            e.printStackTrace();
            XLog.e("检查输入的用户名和密码出错: " + e);
        }
        return true;
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what < 0 || msg.what >= CustomConst.XMPP_HANDLER_WHAT.XMPP_HANDLER_WHAT_MAX.ordinal()) {
                XLog.i(tag, "XMPP_HANDLER_WHAT msg.what error : " + msg.what
                        + ", [" + 0
                        + ", " + CustomConst.XMPP_HANDLER_WHAT.XMPP_HANDLER_WHAT_MAX.ordinal() + "]");
                return;
            }

            CustomConst.XMPP_HANDLER_WHAT what = CustomConst.XMPP_HANDLER_WHAT.values()[msg.what];
            if (what == null) {
                XLog.i(tag, "connHandler.handleMessage : " + msg.what + " : " + " what null");
                return;
            }

            if (what == CustomConst.XMPP_HANDLER_WHAT.XMPP_HANDLER_ERROR) {
                processHanderErrorMessage(msg);
            }
        }

        private void processHanderErrorMessage(android.os.Message msg) {
            if (msg.arg1 < 0 || msg.arg1 >= CustomConst.XMPP_HANDLER_ERROR.XMPP_HANDLER_ERROR_MAX.ordinal()) {
                XLog.i(tag, "XMPP_HANDLER_PRESENCE msg.arg1 error : " + msg.arg1
                        + ", [" + 0
                        + ", " + CustomConst.XMPP_HANDLER_ERROR.XMPP_HANDLER_ERROR_MAX.ordinal() + "]");
                return;
            }

            CustomConst.XMPP_HANDLER_ERROR handlerError = CustomConst.XMPP_HANDLER_ERROR.values()[msg.arg1];
            if (handlerError == null) {
                XLog.i(tag, "processHanderErrorMessage : " + msg.arg1 + " : " + " handlerError null");
                return;
            }

            XLog.i(tag, "connHandler.handleMessage : " + handlerError.toString() + " : " + msg.obj);

            if (handlerError == CustomConst.XMPP_HANDLER_ERROR.XMPP_ERROR_CONNETERROR) {
                getIView().loginFail("网络存在异常");
            } else if (handlerError == CustomConst.XMPP_HANDLER_ERROR.XMPP_ERROR_LOGINFAIL) {
                getIView().loginFail("账号/密码错误");
            } else if (handlerError == CustomConst.XMPP_HANDLER_ERROR.XMPP_ERROR_LOGINSUCCESSED) {
                loginSuccessAndSaveInfo();
            }
        }
    };

    /**
     * 登录成功，保存账号信息
     */
    private void loginSuccessAndSaveInfo() {
        SharedPreferences.getInstance().putString("name", LcUserManager.instance.getUserName());
        SharedPreferences.getInstance().putString("pwd", LcUserManager.instance.getUserPwd());
        XLog.d(tag, "xmppConnection login succed and save");

        getIView().loginSuccess();
    }
}