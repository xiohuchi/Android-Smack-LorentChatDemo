package com.lorent.chat.message;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lorent.chat.R;
import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.smack.UserHeadImageHelper;
import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.smack.engien.MsgEume.MSG_DERATION;
import com.lorent.chat.ui.activitys.SingleChatActivity;
import com.lorent.chat.ui.entity.CommonMessage;
import com.lorent.chat.utils.TypeConverter;
import com.lorent.chat.utils.cache.ImageFetcher;

import java.lang.ref.WeakReference;
import java.util.Date;


public abstract class ChatCommonMessage {

    private static final String tag = "ChatCommonMessage";
    protected Context mContext;
    protected View mRootView;

    protected ImageFetcher mImageFetcher;

    /**
     * 每条信息上提示的时间与距离
     */
    private RelativeLayout mLayoutTimeStampContainer;
    private TextView mHtvTimeStampTime;
    private TextView mHtvTimeStampDistance;

    /**
     * 左边消息送达情况
     */
    private RelativeLayout mLayoutLeftContainer;
    private LinearLayout mLayoutStatus;
    private TextView mHtvStatus;

    /**
     * 消息容器
     */
    protected LinearLayout mLayoutMessageContainer;

    /**
     * 右边消息状况
     */
    private LinearLayout mLayoutRightContainer;
    private ImageView mIvPhotoView;
    /**
     * 布局加载器
     */
    protected LayoutInflater mInflater;
    /**
     * 消息
     */
    protected CommonMessage mMsg;
    /**
     * 背景颜色
     */
    protected int mBackground;

    private TextView message_from = null;

    public ChatCommonMessage(CommonMessage message, Context context, ImageFetcher imageFetcher) {
        mMsg = message;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mImageFetcher = imageFetcher;
    }

    public static ChatCommonMessage getInstance(CommonMessage message, Context context, ImageFetcher imageFetcher) {
        ChatCommonMessage cMessage = null;
        switch (message.getContentType()) {
            case TEXT:
                cMessage = new ChatTextMessage(message, context, imageFetcher);
                break;

            case IMAGE:
                cMessage = new ChatImageMessage(message, context, imageFetcher);
                break;

            case MAP:
                cMessage = new ChatMapMessage(message, context, imageFetcher);
                break;

            case VOICE:
                cMessage = new ChatVoiceMessage(message, context, imageFetcher);
        }

        cMessage.init(message.getMsgComeFromType());
        return cMessage;
    }

    private void init(MSG_DERATION type) {
        switch (type) {
            case RECEIVE:
                //对方layout
                mBackground = R.drawable.chat_from_msg_background_selector;
                mRootView = mInflater.inflate(R.layout.message_singlechat_receive_template, null);
                break;

            case SEND:
                //本方layout
                mBackground = R.drawable.chat_to_msg_background_selector;
                mRootView = mInflater.inflate(R.layout.message_group_send_template, null);

                break;

            case GROUP_SEND:
                //本方layout
                mBackground = R.drawable.chat_to_msg_background_selector;
                mRootView = mInflater.inflate(R.layout.message_group_send_template, null);
                break;

            case GROUP_RECEIVE:
                //对方layout
                mBackground = R.drawable.chat_from_msg_background_selector;
                mRootView = mInflater.inflate(R.layout.message_group_receive_template, null);
                message_from = (TextView) mRootView.findViewById(R.id.message_from);
                message_from.setText(mMsg.getName().split("@")[0]);
                break;
        }

        if (mRootView != null) {
            initView(mRootView);
        }
    }

    protected void initView(View view) {
        mLayoutTimeStampContainer = (RelativeLayout) view.findViewById(R.id.message_layout_timecontainer);
        mHtvTimeStampTime = (TextView) view.findViewById(R.id.message_timestamp_htv_time);
        mHtvTimeStampDistance = (TextView) view.findViewById(R.id.message_timestamp_htv_distance);
        mLayoutLeftContainer = (RelativeLayout) view.findViewById(R.id.message_layout_leftcontainer);
        mLayoutStatus = (LinearLayout) view.findViewById(R.id.message_layout_status);
        mHtvStatus = (TextView) view.findViewById(R.id.message_htv_status);

        mLayoutMessageContainer = (LinearLayout) view.findViewById(R.id.message_layout_messagecontainer);
        mLayoutMessageContainer.setBackgroundResource(mBackground);
        mLayoutRightContainer = (LinearLayout) view.findViewById(R.id.message_layout_rightcontainer);
        mIvPhotoView = (ImageView) view.findViewById(R.id.message_iv_userphoto);

        onInitViews();
    }

    public void fillContent() {
        fillTimeStamp();
        fillState();
        fillMessage();
        fillPhotoView();
    }

    protected void fillMessage() {
        onFillMessage();
    }

    /**
     * 设置发送消息时间及距离
     */
    protected void fillTimeStamp() {
        if (mLayoutTimeStampContainer != null)
            mLayoutTimeStampContainer.setVisibility(View.VISIBLE);

        if (mMsg.getTime() != 0 && mHtvTimeStampTime != null) {
            mHtvTimeStampTime.setText(TypeConverter.formatDate(new Date(mMsg.getTime()), "MM-dd HH:mm:ss"));
        }
        if (mMsg.getDistance() != null) {
            mHtvTimeStampDistance.setText(mMsg.getDistance());
        } else {
            mHtvTimeStampDistance.setText("未知");
        }
    }

    /**
     * 设置消息发送状态
     */
    protected void fillState() {
        mLayoutLeftContainer.setVisibility(View.VISIBLE);
        mLayoutStatus.setBackgroundResource(R.drawable.bg_message_status_sended);
        mHtvStatus.setText("送达");
    }

    /**
     * 设置发送消息的头像
     */
    protected void fillPhotoView() {
        mLayoutRightContainer.setVisibility(View.VISIBLE);
        //Log.e(tag, "load headicon [" + mMsg.getMsgComeFromType().toString() + "] uid : " + mMsg.getUid() + ", name : " + mMsg.getName());
        //wyq, 发送人和对方的头像都从这里加载

        ///单聊
        //发送方头像
        if (mMsg.getMsgComeFromType() == MSG_DERATION.RECEIVE) {
            loadOtherHeadDrawable(mMsg.getUid());
        } else if (mMsg.getMsgComeFromType() == MSG_DERATION.SEND) {
            loadSelfHeadDrawable();
        }

        //群聊, 以Name为用户JID
        if (mMsg.getMsgComeFromType() == MSG_DERATION.GROUP_RECEIVE) {
            loadOtherHeadDrawable(mMsg.getName());
        } else if (mMsg.getMsgComeFromType() == MSG_DERATION.GROUP_SEND) {
            loadSelfHeadDrawable();
        }
    }

    UserHeadImageHelper helper = new UserHeadImageHelper();

    /**
     * 加载自己的头像
     */
    private void loadSelfHeadDrawable() {

        WeakReference<Drawable> drawableSoftReference = LcUserManager.instance.showDrawable.get(
                MXmppConnManager.getInstance().getConnection().getUser());

        if (drawableSoftReference != null && drawableSoftReference.get() != null) {
            showDrawable(drawableSoftReference.get());
        } else {
            showDrawable(helper.getHeadDrawable());
        }
    }

    /**
     * 加载其他人的头像
     * @param key
     */
    private void loadOtherHeadDrawable(String key) {
        WeakReference<Drawable> drawableSoftReference = LcUserManager.instance.showDrawable.get(key);

        if (drawableSoftReference != null && drawableSoftReference.get() != null) {
            showDrawable(drawableSoftReference.get());
        } else {
            showDrawable(helper.getUserDrawable(key));
        }
    }

    private void showDrawable(Drawable drawable){
        if (drawable != null)
            mIvPhotoView.setImageDrawable(drawable);
        else
            mIvPhotoView.setImageResource(R.drawable.qq_leba_list_seek_myfeeds);
    }

    protected void refreshAdapter() {
        ((SingleChatActivity) mContext).refreshAdapter();
    }

    /**
     * 获取最外部元素
     *
     * @return
     */
    public View getRootView() {
        return mRootView;
    }

    protected abstract void onInitViews();

    protected abstract void onFillMessage();

}