package com.lorent.chat.ui.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.lorent.chat.common.AppManager;
import com.lorent.chat.ui.activitys.MainFragmentActivity;


/**
 * activity基类
 * Created by Administrator on 2016/4/24.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.instance.addActivity(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        setIntent(intent);
    }

    protected void initView() {
    }


    public void startActivity(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        startActivitys(intent);
    }

    public void startActivitys(Intent intent) {
        startActivity(intent);
    }

    public void finishActivity() {
        AppManager.instance.finishActivity();
    }

    public void finishActivity(Activity activity) {
        AppManager.instance.finishActivity(activity);
    }

    @Override
    protected void onDestroy() {
        AppManager.instance.finishActivity(this);
        super.onDestroy();
    }

    private long exitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Activity activity = AppManager.instance.currentActivity();
            if (activity instanceof MainFragmentActivity) {
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
            } else {
                finishActivity();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    //=====================================================================

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null
                    && getCurrentFocus().getWindowToken() != null) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return super.onTouchEvent(event);
    }

    protected void closeInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (isOpen) {
            // imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);//没有显示则显示
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
