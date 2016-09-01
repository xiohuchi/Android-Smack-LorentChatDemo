package com.xtagwgj.chatdemo.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lorent.chat.ui.base.BaseView;
import com.lorent.chat.ui.base.MvpActivity;
import com.lorent.chat.ui.contract.LoginContract;
import com.lorent.chat.ui.contract.LoginPresenter;
import com.lorent.chat.utils.ToastUtils;
import com.xtagwgj.chatdemo.R;

public class LoginActivity extends MvpActivity<LoginPresenter> implements LoginContract.View, View.OnClickListener {

    EditText userNameEditText;

    EditText passwordEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("登录");
    }

    @Override
    public void initView() {
        userNameEditText = (EditText) findViewById(R.id.userNameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        findViewById(R.id.loginButton).setOnClickListener(this);
        findViewById(R.id.registerButton).setOnClickListener(this);
    }

    @Override
    public BaseView getBaseView() {
        return this;
    }

    @Override
    public int getResLayout() {
        return R.layout.activity_login;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginButton:
                mPresenter.toLogin(
                        userNameEditText.getText().toString().trim(),
                        passwordEditText.getText().toString().trim()
                );
                break;
            case R.id.registerButton:
                startActivity(RegisterActivity.class);
                break;
        }
    }

    @Override
    public void loginSuccess() {
        showToast("登录成功");
        startActivity(MainActivity.class);

//        Intent intent = new Intent(this, MainActivity.class);
//        intent.putExtra("friends", false);
//        intent.putExtra("message", true);
//        startActivitys(intent);

        finish();
    }

    @Override
    public void loginFail(String msg) {
        showToast(msg);
    }

    @Override
    public void showToast(String msg) {
        ToastUtils.createCenterNormalToast(this, msg, Toast.LENGTH_SHORT);
    }
}