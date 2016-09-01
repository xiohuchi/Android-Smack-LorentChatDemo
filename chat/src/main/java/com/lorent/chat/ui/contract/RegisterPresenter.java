package com.lorent.chat.ui.contract;

import android.os.Looper;

import com.lorent.chat.smack.LoginAndRegisterHelper;
import com.lorent.chat.ui.base.MvpPresenter;
import com.lorent.chat.utils.XLog;

import java.util.HashMap;
import java.util.Map;

/**
 * 注册处理
 * Created by zy on 2016/8/12.
 */
public class RegisterPresenter extends MvpPresenter<RegisterContract.View> implements RegisterContract.Presenter {
    @Override
    public void register(final String username, final String nickname, final String password) {
        XLog.d("正在进行注册:username:" + username + "--->nickname:" + nickname + "--->password:" + password);
        new Thread() {
            public void run() {
                Looper.prepare();
                Map<String, String> attributes = new HashMap<>();
                attributes.put("name", nickname);
                final boolean flag = new LoginAndRegisterHelper().registerUser(username, password, attributes);

                if (flag) {
                    getIView().registerSuccess();
                } else {
                    getIView().registerFail("注册失败");
                }
            }
        }.start();

    }

    @Override
    public void cancelRegister() {
        getIView().cancelRegister();
    }
}
