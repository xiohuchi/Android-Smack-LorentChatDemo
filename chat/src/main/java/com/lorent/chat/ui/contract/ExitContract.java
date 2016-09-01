package com.lorent.chat.ui.contract;

import com.lorent.chat.ui.base.BasePresenter;
import com.lorent.chat.ui.base.BaseView;

/**
 * Created by zy on 2016/8/15.
 */
public class ExitContract {
    public interface View extends BaseView {
        /**
         * 退出成功
         */
        void logOutSuccess();
    }

    public interface Presenter extends BasePresenter {
        /**
         * 退出
         */
        void logOut();
    }


}
