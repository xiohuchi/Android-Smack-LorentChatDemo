package com.lorent.chat.ui.base;

import android.content.Context;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Created by zy on 2016/8/9.
 */
public class MvpPresenter <V extends BaseView> {
    public Context mContext;
    public Reference<V> mViewRef;

    public void initPresenter(V view){
        mViewRef = new WeakReference<V>(view);
        mContext = Mvp.getInstance().getApplictionContext();
    }

    public V getIView(){
        return mViewRef.get();
    }

    public void destory(){
        if(mViewRef != null){
            mViewRef.clear();
            mViewRef = null;
        }
    }
}
