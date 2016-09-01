package com.lorent.chat.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.lorent.chat.common.AppManager;


/**
 * Created by zy on 2016/8/9.
 */
public abstract class MvpActivity<P extends MvpPresenter> extends BaseActivity implements MvpView {
    public P mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResLayout());
        AppManager.instance.addActivity(this);
        initMvp();
        initView();
    }

    public void initMvp() {
        Mvp mvp = Mvp.getInstance();
        mvp.registerView(this.getClass(), this);
        mPresenter = (P) mvp.getPresenter(Mvp.getGenericType(this, 0));
        mPresenter.initPresenter(getBaseView());
    }

    public abstract void initView();

    /**
     * 确定IView类型
     *
     * @return
     */
    public abstract BaseView getBaseView();

    public abstract int getResLayout();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.instance.finishActivity(this);
        Mvp.getInstance().unRegister(this.getClass());
        mPresenter.destory();
    }
}
