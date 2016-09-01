package com.lorent.chat.ui.contract;


import com.lorent.chat.ui.base.BasePresenter;
import com.lorent.chat.ui.base.BaseView;

/**
 * 登录
 * Created by zy on 2016/8/11.
 */
public class LoginContract {
    public interface View extends BaseView {
        void loginSuccess();

        void loginFail(String msg);
    }

    public interface Presenter extends BasePresenter {

        void toLogin(String account, String password);

    }
}
