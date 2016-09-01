package com.lorent.chat.ui.activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lorent.chat.R;
import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.smack.LoginAndRegisterHelper;
import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.smack.constVar.CustomConst.XMPP_HANDLER_WHAT;
import com.lorent.chat.utils.ToastUtils;
import com.lorent.chat.ui.view.CommonDialog;
import com.lorent.chat.utils.XLog;

import org.jivesoftware.smack.XMPPException;

import java.util.concurrent.ExecutionException;

/**
 * 登录
 */
public class LoginActivity extends BaseActivity implements OnClickListener {

    protected static final String tag = "LoginActivity";

    private EditText et_name;
    private EditText et_pwd;

    private Button btn_login;
    private Button btn_register;

    private SharedPreferences sharedPreferences;

    private CommonDialog dialog;

    private boolean success;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        try {
            if (autoLogin()) {
                mStartActivity(MainFragmentActivity.class);
                finish();
                return;
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.login_main_layout);
        //getActionBar().setTitle("登录");
        initViews();
        initEvents();
    }

    @Override
    protected void initViews() {
        et_name = (EditText) findViewById(R.id.et_username);
        et_pwd = (EditText) findViewById(R.id.et_userpwd);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_register = (Button) findViewById(R.id.btn_register);
        et_name.setText(sharedPreferences.getString("name", ""));
        et_pwd.setText(sharedPreferences.getString("pwd", ""));
    }

    @Override
    protected void initEvents() {
        btn_login.setOnClickListener(this);
        btn_register.setOnClickListener(this);
    }

    private boolean autoLogin() throws XMPPException {
        boolean bLogin = sharedPreferences.getBoolean("login", false);
        String name = sharedPreferences.getString("name", "");
        String pwd = sharedPreferences.getString("pwd", "");

        if (bLogin == false || (name.equals("")
                && pwd.equals(""))) {
            return false;
        } else {
            try {

                dialog = new CommonDialog(this, R.style.Loading_Dialog,
                        R.layout.common_loading_dialog_layout);

                dialog.show();
                XLog.d(tag, "xmppConnection connect server star ");


                if (MXmppConnManager.getInstance().getConnection() == null
                        || !MXmppConnManager.getInstance().getConnection().isConnected()) {

                    MXmppConnManager.getInstance().new InitXmppConnectionTask(
                            handler, tag).execute().get();

                    //等待返回成功连接服务器消息
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            while (!success) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }).start();

                }

                success =  new LoginAndRegisterHelper().feXmppLogin(
                        name,
                        pwd,
                        this,
                        handler);
                if (success) {
                    LcUserManager.instance.setUpUserInfo(name, pwd);
                }
                XLog.d(tag, "xmppConnection login over success : " + success);
                dialog.cancel();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "账号或者密码错误", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_login) {

            if ("".equals(et_name.getText().toString().trim())
                    || "".equals(et_pwd.getText().toString().trim())) {

                Toast.makeText(LoginActivity.this, "账号/密码不能为空",
                        Toast.LENGTH_SHORT).show();

                return;

            }

            dialog = new CommonDialog(this, R.style.Loading_Dialog,
                    R.layout.common_loading_dialog_layout);

            dialog.show();

            handler.postDelayed(new LoginRunnable(), 100);

        }

        if (v.getId() == R.id.btn_register) {
            Intent intent = new Intent(this.getApplicationContext(), RegisterActivity.class);
            startActivity(intent);

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }

    Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            if (msg.what < 0 || msg.what >= XMPP_HANDLER_WHAT.XMPP_HANDLER_WHAT_MAX.ordinal()) {
                XLog.i(tag, "XMPP_HANDLER_WHAT msg.what error : " + msg.what
                        + ", [" + 0
                        + ", " + XMPP_HANDLER_WHAT.XMPP_HANDLER_WHAT_MAX.ordinal() + "]");
                return;
            }


            XMPP_HANDLER_WHAT what = XMPP_HANDLER_WHAT.values()[msg.what];
            if (what == null) {
                XLog.i(tag, "connHandler.handleMessage : " + msg.what + " : " + " what null");
                return;
            }

            if (what == XMPP_HANDLER_WHAT.XMPP_HANDLER_ERROR) {
                processHanderErrorMessage(msg);
            }
        }

        ;

        private void processHanderErrorMessage(android.os.Message msg) {
            if (dialog != null)
                dialog.dismiss();

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

            ToastUtils.createCenterNormalToast(LoginActivity.this, (String) msg.obj, Toast.LENGTH_SHORT);
            XLog.i(tag, "connHandler.handleMessage : " + handlerError.toString() + " : " + (String) msg.obj);

            if (handlerError == CustomConst.XMPP_HANDLER_ERROR.XMPP_ERROR_CONNETERROR)
                ToastUtils.createCenterNormalToast(LoginActivity.this, "网络存在异常", Toast.LENGTH_SHORT);


            if (handlerError == CustomConst.XMPP_HANDLER_ERROR.XMPP_ERROR_LOGINFAIL)
                ToastUtils.createCenterNormalToast(LoginActivity.this, "账号/密码错误", Toast.LENGTH_SHORT);

            if (handlerError == CustomConst.XMPP_HANDLER_ERROR.XMPP_ERROR_LOGINSUCCESSED) {
                if (et_name != null) {
                    XLog.d(tag, "xmppConnection login succed");

                    Editor editor = sharedPreferences.edit();
                    editor.putString("name", et_name.getText().toString().trim());
                    editor.putString("pwd", et_pwd.getText().toString().trim());
                    editor.putBoolean("login", true);

                    editor.commit();

                    XLog.d(tag, "xmppConnection login succed and save");
                }

                XLog.d(tag, "mStartActivity MainFragmentActivity");
                mStartActivity(MainFragmentActivity.class);

                finish();
            }

        }

    };

    class LoginRunnable implements Runnable {
        @Override
        public void run() {
            String name = "";
            String pwd = "";
            if (et_name != null) {
                name = et_name.getText().toString().trim();
                pwd = et_pwd.getText().toString().trim();

            } else {
                name = sharedPreferences.getString("name", "");
                pwd = sharedPreferences.getString("pwd", "");
            }

            try {
                MXmppConnManager.getInstance().new InitXmppConnectionTask(
                        handler, tag).execute().get();
            } catch (InterruptedException | ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            success =  new LoginAndRegisterHelper().feXmppLogin(name, pwd,
                    getApplicationContext(), handler);
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

}
