package com.lorent.chat.ui.contract;

import com.lorent.chat.ui.base.BasePresenter;
import com.lorent.chat.ui.base.BaseView;

/**
 * Created by zy on 2016/8/12.
 */
public class RegisterContract {
    public interface View extends BaseView {
        void registerSuccess();

        void registerFail(String msg);

        void cancelRegister();
    }

    public interface Presenter extends BasePresenter {

        void register(String username, String nickname, String password);

        void cancelRegister();
    }
}
