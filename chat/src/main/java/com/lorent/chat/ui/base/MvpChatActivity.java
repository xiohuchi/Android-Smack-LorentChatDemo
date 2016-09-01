package com.lorent.chat.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.lorent.chat.R;
import com.lorent.chat.ui.activitys.BaseChatActivity;


/**
 * Created by zy on 2016/8/9.
 */
public abstract class MvpChatActivity<P extends MvpPresenter> extends BaseChatActivity implements MvpView {
    public P mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_main_layout);

        initMvp();
    }

    public void initMvp() {
        Mvp mvp = Mvp.getInstance();
        mvp.registerView(this.getClass(), this);
        mPresenter = (P) mvp.getPresenter(Mvp.getGenericType(this, 0));
        mPresenter.initPresenter(getBaseView());
    }

    /**
     * 确定IView类型
     *
     * @return
     */
    public abstract BaseView getBaseView();

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Mvp.getInstance().unRegister(this.getClass());
        mPresenter.destory();
    }
}
