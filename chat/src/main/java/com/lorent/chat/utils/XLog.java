package com.lorent.chat.utils;

import android.util.Log;

import com.lorent.chat.BuildConfig;


/**
 * Created by zy on 2016/8/11.
 */
public class XLog {
    private static String TAG = "xLog";

    public static void setTAG(String tag) {
        TAG = tag;
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void d(String msg) {
        d(TAG, msg);
    }


    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void i(String msg) {
        i(TAG, msg);
    }

    public static void e(String msg) {
        e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg);
        }
    }

}
