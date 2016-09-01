package com.xtagwgj.chatdemo.ui;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lorent.chat.ui.base.BaseView;
import com.lorent.chat.ui.base.MvpActivity;
import com.lorent.chat.ui.contract.RegisterContract;
import com.lorent.chat.ui.contract.RegisterPresenter;
import com.lorent.chat.utils.ToastUtils;
import com.lorent.chat.utils.XLog;
import com.xtagwgj.chatdemo.R;

import org.apache.commons.lang3.StringUtils;

public class RegisterActivity extends MvpActivity<RegisterPresenter> implements RegisterContract.View, View.OnClickListener {

    EditText userNameEditText;
    EditText passwordEditText;
    EditText confPasswordEditText;

    @Override

    public void registerSuccess() {
        showToast("success");
        finishActivity();
    }

    @Override
    public void registerFail(String msg) {
        showToast(msg);
    }

    @Override
    public void cancelRegister() {
        finishActivity();
    }

    @Override
    public void showToast(String msg) {
        XLog.d("显示toast信息：" + msg);
        ToastUtils.createCenterNormalToast(this, msg, Toast.LENGTH_LONG);
    }

    @Override
    public void initView() {
        setTitle("注册");

        userNameEditText = (EditText) findViewById(R.id.userNameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        confPasswordEditText = (EditText) findViewById(R.id.confPasswordEditText);
        findViewById(R.id.registerButton).setOnClickListener(this);
    }

    @Override
    public BaseView getBaseView() {
        return this;
    }

    @Override
    public int getResLayout() {
        return R.layout.activity_register;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.registerButton) {
            String username = userNameEditText.getText().toString().trim();

            if (StringUtils.isEmpty(username)) {
                registerFail("username is too short");
                return;
            }

            String password1 = passwordEditText.getText().toString().trim();
            String password2 = confPasswordEditText.getText().toString().trim();

            if (StringUtils.equals(password1, password2) && password1.length() > 0)

                mPresenter.register(
                        username,
                        username,
                        password1
                );
            else {
                registerFail("password is inCorrect");
            }
        }
    }
}
