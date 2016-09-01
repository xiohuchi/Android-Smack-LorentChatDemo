package com.lorent.chat.ui.activitys;

import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lorent.chat.R;
import com.lorent.chat.ui.base.BaseView;
import com.lorent.chat.ui.base.MvpActivity;
import com.lorent.chat.ui.contract.RegisterContract;
import com.lorent.chat.ui.contract.RegisterPresenter;
import com.lorent.chat.utils.ToastUtils;
import com.lorent.chat.utils.XLog;

/**
 * 注册
 */
public class RegisterActivity extends MvpActivity<RegisterPresenter> implements RegisterContract.View, View.OnClickListener {
    /**
     * 用户名
     */
    private EditText mEtUsername;
    /**
     * 昵称
     */
    private EditText mEtNickname;
    /**
     * 密码
     */
    private EditText mEtPassword;
    /**
     * 重复密码
     */
    private EditText mEtRepassword;


    @SuppressWarnings("ConstantConditions")
    @Override
    public void initView() {
        this.mEtUsername = (EditText) findViewById(R.id.cet_register_username);
        this.mEtNickname = (EditText) findViewById(R.id.cet_register_nickname);
        this.mEtPassword = (EditText) findViewById(R.id.cet_register_password);
        this.mEtRepassword = (EditText) findViewById(R.id.cet_register_repassword);
        findViewById(R.id.btn_register_ok).setOnClickListener(this);
        findViewById(R.id.btn_register_cancel).setOnClickListener(this);


        mEtUsername.setText("15013208168");
        mEtNickname.setText("15013208168");
        mEtRepassword.setText("123456");
        mEtPassword.setText("123456");
    }

    @Override
    public BaseView getBaseView() {
        return this;
    }

    @Override
    public int getResLayout() {
        return R.layout.activity_register_layout;
    }

    public void onRegisterOk() {
        final String username = mEtUsername.getText().toString();
        final String nickname = mEtNickname.getText().toString();
        String password = mEtPassword.getText().toString();
        final String repassword = mEtRepassword.getText().toString();
        if (username.isEmpty()) {
            mEtUsername.setError("用户名不能为空");
            return;
        }
        if (nickname.isEmpty()) {
            mEtNickname.setError("昵称不能为空");
            return;
        }
        if (password.isEmpty()) {
            mEtPassword.setError("密码不能为空");
            return;
        }
        if (repassword.isEmpty()) {
            mEtRepassword.setError("密码确认不能为空");
            return;
        }
        if (!password.equals(repassword)) {
            mEtRepassword.setError("两次密码不相同，请重新确认");
            mEtRepassword.setText("");
            return;
        }

        mPresenter.register(username, nickname, password);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.getId() == R.id.btn_register_ok) {
            onRegisterOk();
        }
        if (v.getId() == R.id.btn_register_cancel) {
            mPresenter.cancelRegister();
        }

    }

    @Override
    public void registerSuccess() {
        showToast("注册成功");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finishActivity();
            }
        },2000);

    }

    @Override
    public void registerFail(String msg) {
        showToast(msg);
        finishActivity();
    }

    @Override
    public void cancelRegister() {
        showToast("取消注册");
        finishActivity();
    }

    @Override
    public void showToast(String msg) {
        XLog.d(msg);
        ToastUtils.createNormalToast(this, msg, Toast.LENGTH_SHORT);
    }
}
