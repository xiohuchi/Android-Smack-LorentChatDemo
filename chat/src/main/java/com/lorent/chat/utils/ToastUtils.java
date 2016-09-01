package com.lorent.chat.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import org.jivesoftware.smack.util.StringUtils;

public class ToastUtils {
    /**
     * ������ͨĬ�ϵ�������ʾ
     *
     * @param context
     * @param textContext
     * @param time
     */
    public static void createNormalToast(Context context, String textContext, int time) {
        if (StringUtils.isNotEmpty(textContext.trim())) {
            Toast.makeText(context, textContext, time).show();
        }
    }

    /**
     * ������ͨλ�����������
     *
     * @param context
     * @param textContext
     * @param time
     */
    public static void createCenterNormalToast(Context context, String textContext, int time) {
        if (StringUtils.isNotEmpty(textContext.trim())) {
            Toast toast = Toast.makeText(context, textContext, time);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

}
