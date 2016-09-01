package com.lorent.chat.ui.activitys;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lorent.chat.R;
import com.lorent.chat.ui.base.BaseView;
import com.lorent.chat.ui.base.MvpActivity;
import com.lorent.chat.ui.contract.LoginContract;
import com.lorent.chat.ui.contract.LoginPresenter;
import com.lorent.chat.ui.view.CommonDialog;
import com.lorent.chat.utils.ToastUtils;
import com.lorent.chat.utils.XLog;


public class XLoginActivity extends MvpActivity<LoginPresenter> implements LoginContract.View, View.OnClickListener {
    /**
     * 用户名
     */
    private EditText et_name;
    /**
     * 密码
     */
    private EditText et_pwd;

    private CommonDialog dialog;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void initView() {
        et_name = (EditText) findViewById(R.id.et_username);
        et_pwd = (EditText) findViewById(R.id.et_userpwd);
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.btn_register).setOnClickListener(this);
    }

    @Override
    public BaseView getBaseView() {
        return this;
    }

    @Override
    public int getResLayout() {
        return R.layout.login_main_layout;
    }


    @Override
    public void loginSuccess() {
        showToast("登录成功");
        dialog.cancel();
        startActivity(MainChatActivity.class);
//        startActivity(MainFragmentActivity.class);
        finish();
    }

    @Override
    public void loginFail(String msg) {
        showToast("登录失败:" + msg);
        dialog.cancel();
    }

    public String getAccount() {
        return et_name.getText().toString().trim();
    }

    public String getPassword() {
        return et_pwd.getText().toString().trim();
    }

    @Override
    public void showToast(String msg) {
        XLog.d("显示toast信息：" + msg);
        ToastUtils.createCenterNormalToast(this, msg, Toast.LENGTH_LONG);
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_login) {
            dialog = new CommonDialog(XLoginActivity.this, R.style.Loading_Dialog,
                    R.layout.common_loading_dialog_layout);
            dialog.show();
            mPresenter.toLogin(getAccount(), getPassword());
        } else if (v.getId() == R.id.btn_register) {
            startActivity(RegisterActivity.class);
        }

    }
}
