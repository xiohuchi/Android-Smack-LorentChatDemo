package com.lorent.chat.common;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import com.lorent.chat.utils.XLog;

import java.util.LinkedList;

/**
 * 应用程序Activity管理类：用于Activity管理和应用程序退出
 */
public enum AppManager {
    instance;
    private static LinkedList<Activity> activityStack;
    private String TAG = AppManager.class.getSimpleName();

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new LinkedList<>();
        }
        XLog.i(TAG, "addActivity: " + activity);
        activityStack.add(activity);
    }


    /**
     * 获取指定的activity
     */
    public Activity getActivity(Class<?> cls) {
        if (activityStack != null) {
            for (Activity activity : activityStack) {
                if (activity.getClass().equals(cls)) {
                    return  activity;
                }
            }
        }
        return null;
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public Activity currentActivity() {
        return activityStack.getLast();
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    public void finishActivity() {
        if (activityStackSize() > 0) {
            Activity activity = activityStack.getLast();
            finishActivity(activity);
        }
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            if (activityStack != null) {
                activityStack.remove(activity);
            }
            XLog.e(TAG, "finishActivity: " + activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls) {
        Activity temp = null;
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                temp = activity;
                break;
            }
        }
        finishActivity(temp);
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }

    /**
     * 退出应用程序
     */
    public void AppExit(Context context) {
        try {
            finishAllActivity();
            ActivityManager activityMgr = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);

            activityMgr.restartPackage(context.getPackageName());
            System.exit(0);
        } catch (Exception e) {
            XLog.e(TAG, "AppExit: " + e);
        }
    }

    public int activityStackSize() {
        return activityStack == null ? 0 : activityStack.size();
    }
}