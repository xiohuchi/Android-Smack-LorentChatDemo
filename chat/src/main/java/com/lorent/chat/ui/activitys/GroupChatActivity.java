package com.lorent.chat.ui.activitys;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lorent.chat.R;
import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.message.GroupChatFileMessageBody;
import com.lorent.chat.message.GroupChatImageMessageBody;
import com.lorent.chat.smack.connection.MUCManager;
import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.smack.engien.MUCMsgProcessListener;
import com.lorent.chat.smack.engien.MsgEume.CHAT_STATES;
import com.lorent.chat.smack.engien.MsgEume.MSG_CONTENT_TYPE;
import com.lorent.chat.smack.engien.MsgEume.MSG_DERATION;
import com.lorent.chat.smack.engien.MsgEume.MSG_STATE;
import com.lorent.chat.ui.adapter.ChatAdapter;
import com.lorent.chat.ui.adapter.FaceGridViewAdapter;
import com.lorent.chat.ui.entity.CommonMessage;
import com.lorent.chat.ui.view.CommonChatListView;
import com.lorent.chat.ui.view.EmotionEditText;
import com.lorent.chat.utils.FileUtils;
import com.lorent.chat.utils.PictureViewUtils;
import com.lorent.chat.utils.TypeConverter;
import com.lorent.chat.utils.cache.CacheUtils;
import com.lorent.chat.utils.cache.ImageResizer;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.io.IOException;
import java.lang.ref.SoftReference;

public class GroupChatActivity extends BaseChatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String tag = "GroupChatActivity";

    private CommonMessage message;

    private String prevMsg;

    private SwipeRefreshLayout mRfLayout;

//    private int page = 1;
//
//    private int maxPage = 1;
//
//    private long sendRowid = 0;
//
//    private String hostUid = MXmppConnManager.hostUid;

    private String myJID = null;
    private String groupJID = null;
    private MUCManager mucManager = null;
    private MultiUserChat muchat = null;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_main_layout);
        mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // 获取从上面获取来的用户数据
        Bundle bundle = getIntent().getExtras();
        groupJID = bundle.getString("groupJID");
        myJID = bundle.getString("myJID");
        Log.e(tag, "groupJID : " + groupJID + ", myJID : " + myJID);
        //需优化
        if (getActionBar() != null)
            getActionBar().setIcon(R.drawable.group);

        mucManager = new MUCManager();
        muchat = mucManager.createMultiUserChat(groupJID, new MUCMsgProcessListener());
        //wyq
        if (getActionBar() != null)
            getActionBar().setTitle("讨论组:" + groupJID);

        LcUserManager.instance.putHandler(groupJID, handler);
        initViews();
        initEvents();
        refreshAdapter();
    }

    @Override
    protected void initViews() {

        mLayoutEmotionMedia = (FrameLayout) findViewById(R.id.fl_emotion_media);

        mLoPlusBarPic = (View) findViewById(R.id.include_chat_plus_pic);

        mLoPlusBarCarema = (View) findViewById(R.id.include_chat_plus_camera);

        mLoPlusBarLocation = (View) findViewById(R.id.include_chat_plus_location);

        mIvPlusBarPic = (ImageView) mLoPlusBarPic.findViewById(R.id.iv_chat_plus_image);

        mIvPlusBarCarema = (ImageView) mLoPlusBarCarema.findViewById(R.id.iv_chat_plus_image);

        mIvPlusBarLocation = (ImageView) mLoPlusBarLocation.findViewById(R.id.iv_chat_plus_image);

        mTvPlusBarPic = (TextView) findViewById(R.id.include_chat_plus_pic).findViewById(R.id.tv_chat_plus_description);

        mTvPlusBarCarema = (TextView) findViewById(R.id.include_chat_plus_camera).findViewById(R.id.tv_chat_plus_description);

        mTvPlusBarLocation = (TextView) findViewById(R.id.include_chat_plus_location).findViewById(R.id.tv_chat_plus_description);

        mTvPlusBarCarema.setText("拍照");

        mTvPlusBarPic.setText("图片");

        mTvPlusBarLocation.setText("位置");

        mRfLayout = (SwipeRefreshLayout) findViewById(R.id.srfl_chat);
        //noinspection ResourceAsColor
        mRfLayout.setColorScheme(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mBtnMsgSend = (Button) findViewById(R.id.btn_chat_send);

        mEmtMsg = (EmotionEditText) findViewById(R.id.et_chat_msg);

        mIvEmotion = (ImageView) findViewById(R.id.iv_chat_biaoqing);

        mIvMedia = (ImageView) findViewById(R.id.iv_chat_media);

        mLvCommonMsg = (CommonChatListView) findViewById(R.id.lv_chat);

        mGvEmotion = (GridView) findViewById(R.id.gv_chat_biaoqing);

        mLayoutEmotionMedia.setVisibility(View.GONE);

        mLayoutEmotion = (LinearLayout) findViewById(R.id.ll_chat_face);

        mLayoutMedia = (LinearLayout) findViewById(R.id.ll_chat_plusbar);

        loadMediaBar();

        mAdapter = new ChatAdapter(this, messagesList);

        mLvCommonMsg.setAdapter(mAdapter);

        mGvEmotion.setAdapter(new FaceGridViewAdapter(this));

    }

    private Bitmap getBitmap(String key, int res) {

        SoftReference<Bitmap> soft = LcUserManager.instance.mSendbarCache.get(key);
        Bitmap bitmap = null;
        if (soft == null) {
            bitmap = ImageResizer.decodeSampledBitmapFromResource(getResources(), res, 50, 50);
            soft = new SoftReference<Bitmap>(bitmap);
            LcUserManager.instance.mSendbarCache.put(key, soft);
        } else {
            bitmap = soft.get();
        }
        return bitmap;
    }

    /**
     * 加载多媒体框内的图片
     */
    private void loadMediaBar() {
        //添加多媒体窗口内的多媒体图表--图片
        Bitmap bitmap = getBitmap(getString(R.string.plusbar_pic), R.drawable.ic_chat_plusbar_pic_normal);
        mIvPlusBarPic.setImageBitmap(bitmap);

        //添加多媒体窗口内的多媒体图表--拍照
        bitmap = getBitmap(getString(R.string.plusbar_camera), R.drawable.ic_chat_plusbar_camera_normal);
        mIvPlusBarCarema.setImageBitmap(bitmap);

        //添加多媒体窗口内的多媒体图表--位置
        bitmap = getBitmap(getString(R.string.plusbar_location), R.drawable.ic_chat_plusbar_location_normal);
        mIvPlusBarLocation.setImageBitmap(bitmap);
    }


    @Override
    protected void initEvents() {
        mRfLayout.setOnRefreshListener(this);
        mBtnMsgSend.setOnClickListener(this);
        mIvEmotion.setOnClickListener(this);
        mEmtMsg.setOnTouchListener(this);
        mLvCommonMsg.setOnTouchListener(this);
        mGvEmotion.setOnItemClickListener(this);
        mIvMedia.setOnClickListener(this);
        mLoPlusBarPic.setOnClickListener(this);
        mLoPlusBarCarema.setOnClickListener(this);
        mLoPlusBarLocation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.iv_chat_media) {
            chatMedia();
        } else if (v.getId() == R.id.iv_chat_biaoqing) {//点击的是表情框
            chatBiaoQing();
        } else if (v.getId() == R.id.btn_chat_send) { //点击发送按钮
            chatSend();
        } else if (v.getId() == R.id.include_chat_plus_pic) {
            Intent intent = PictureViewUtils.getPictureIntent();
            startActivityForResult(intent, CustomConst.MEDIA_CODE_PICTURE);
        } else if (v.getId() == R.id.include_chat_plus_camera) {
            GroupChatFileMessageBody gcfm = new GroupChatFileMessageBody("家长汇产品缺陷修复计划.xml",
                    "312ACAEF93123",
                    "2016-07-22 11:45:59",
                    "http://cdn.duitang.com/uploads/item/201407/30/20140730125858_APsPN.thumb.700_0.jpeg");

            //Log.e(tag, gcfm.getMessageBody());
            try {
                muchat.sendMessage(gcfm.getMessageBody());
            } catch (NotConnectedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void chatSend() {
        if ("".equals(mEmtMsg.getText().toString())) {
            Toast.makeText(this, "先输入信息", Toast.LENGTH_SHORT).show();
        } else {
            prevMsg = mEmtMsg.getText().toString();
            mEmtMsg.setText("");

            try {
                //最后服务器会重新推送一个聊天消息过来， 所以报错时才添加到本地；要么处理一下接收器/分离出历史消息和当前消息
                muchat.sendMessage(prevMsg);
            } catch (NotConnectedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

                message = new CommonMessage(
                        groupJID,
                        "nearby_people_other",
                        System.currentTimeMillis(),
                        "",
                        prevMsg,
                        MSG_STATE.ARRIVED,
                        MSG_CONTENT_TYPE.TEXT,
                        MSG_DERATION.GROUP_SEND,
                        myJID,
                        CHAT_STATES.STNULL);


                messagesList.add(message);

                refreshAdapter();

            }

        }
    }

    private void chatBiaoQing() {
    /*
     * 如果素材库显示
     */
        if (mLayoutEmotionMedia.isShown()) {
        /*
         *如果表情框显示
         *则隐藏整个素材库
         */
            if (mLayoutEmotion.isShown()) {
                mLayoutEmotionMedia.setVisibility(View.GONE);
        /*
         * 如果表情框隐藏
         * 则隐藏多媒体框
         * 设置表情库的标志
         * 显示表情框
         */
            } else {
                mLayoutMedia.setVisibility(View.GONE);
                mLayoutEmotion.setVisibility(View.VISIBLE);
            }
    /*
     * 如果素材库不显示，
     * 无论怎样先隐藏掉多媒体框；
     */
        } else {
            mLayoutMedia.setVisibility(View.GONE);
            mLvCommonMsg.setSelection(messagesList.size() - 1);
            mInputManager.hideSoftInputFromWindow(mEmtMsg.getWindowToken(),
                    0);
            mLayoutEmotion.setVisibility(View.VISIBLE);
            mLayoutEmotionMedia.setVisibility(View.VISIBLE);
        }
    }

    private void chatMedia() {
    /*
     * 如果素材库框显示
     */
        if (mLayoutEmotionMedia.isShown()) {
        /*
         * 如果表情框是可视的，这时只需跳到多媒体框来显示
         * 否则就证明当前打开的是多媒体框，就隐藏整个素材库;
         */
            if (mLayoutEmotion.isShown()) {
                mLayoutEmotion.setVisibility(View.GONE);
                mLayoutMedia.setVisibility(View.VISIBLE);
            } else {
                mLayoutEmotionMedia.setVisibility(View.GONE);
            }
    /*
     * 如果素材库当前不显示
     * 无论怎样，先把表情框隐藏，然后设置
     * 多媒体框为显示
     */
        } else {
            mLayoutEmotion.setVisibility(View.GONE);
            mLvCommonMsg.setSelection(messagesList.size() - 1);
            mInputManager.hideSoftInputFromWindow(mEmtMsg.getWindowToken(),
                    0);
            mLayoutEmotionMedia.setVisibility(View.VISIBLE);
            mLayoutMedia.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == CustomConst.MEDIA_CODE_PICTURE && resultCode == RESULT_OK) {

            String path = FileUtils.getPictureSelectedPath(intent, this);
            //String newPath = CacheUtils.getImagePath(mApplication, "sendImage/" + TypeConverter.getUUID() + ".pilin");
            String newPath = CacheUtils.getImagePath(mApplication, "sendImage/" + TypeConverter.getUUID() + ".jpg");
            try {
                Bitmap bitmap = ImageResizer.decodeSampledBitmapFromFile(path, 400, 800);
                FileUtils.compressAndWriteFile(bitmap, mApplication, newPath);
            } catch (IOException e) {
                e.printStackTrace();
            }


            if (!sendImageMessage(newPath)) {
                long mills = System.currentTimeMillis();

                message = new CommonMessage(
                        groupJID,
                        "nearby_people_other",
                        mills,
                        "",
                        newPath,
                        MSG_STATE.SENDDING,
                        MSG_CONTENT_TYPE.IMAGE,
                        MSG_DERATION.GROUP_SEND,
                        myJID,
                        CHAT_STATES.STNULL);

                //sendRowid = messageDAO.save(message, hostUid);

                messagesList.add(message);

                refreshAdapter();
            }
        } else if (resultCode == CustomConst.MEDIA_CODE_CAMERA && resultCode == RESULT_OK) {
            Bundle bundle = intent.getExtras();
            Bitmap bitmap = (Bitmap) bundle.get("data");
        }
    }

    /*****************
     * 群聊发送图片功能
     */

    //群发送图片demo
    private boolean sendImageMessage(String filePath) {
        GroupChatImageMessageBody messageBody = new GroupChatImageMessageBody();
        if (!messageBody.readImage(filePath))
            return false;
        try {
            muchat.sendMessage(messageBody.getMessageBody());
            return true;
        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }
    //群发文件demo

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.et_chat_msg) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (mLayoutEmotionMedia.isShown()) {
                    mLayoutEmotionMedia.setVisibility(View.GONE);
                }
            }
        } else if (v.getId() == R.id.lv_chat) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (mLayoutEmotionMedia.isShown()) {
                    mLayoutEmotionMedia.setVisibility(View.GONE);
                }
                mInputManager.hideSoftInputFromWindow(
                        mEmtMsg.getWindowToken(), 0);
            }
        }

        return false;
    }

    Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case CustomConst.HANDLER_MGS_ADD:
                    CommonMessage chatmsg = (CommonMessage) msg.obj;
                    if (!chatmsg.getUid().equals(myJID)) {

                        Log.e(tag, "handleMessage from : " + chatmsg);
                        Log.e(tag, "handleMessage from : " + chatmsg.getUid() + ", " + chatmsg.getName());
                        Log.e(tag, "handleMessage getContentType : " + chatmsg.getState());
                        messagesList.add(chatmsg);
                        refreshAdapter();
                    }
                    break;
            }

        }

        ;

    };

    private void updateMsgByMills(long mills) {

        for (int i = 0; i < messagesList.size(); i++) {
            CommonMessage msg = messagesList.get(i);
            if (msg.getTime() == mills) {
                msg.setState(MSG_STATE.ARRIVED);
                break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long mills) {
        String text = LcUserManager.instance.mEmotions_Zme.get(position);
        if (!TextUtils.isEmpty(text)) {
            int start = mEmtMsg.getSelectionStart();
            CharSequence content = mEmtMsg.getText().insert(start, text);
            mEmtMsg.setText(content);
            //定位光标
            CharSequence info = mEmtMsg.getText();

            if (info instanceof Spannable) {
                Spannable spanText = (Spannable) info;
                Selection.setSelection(spanText, start + text.length());
            }
        }
    }

    public void refreshAdapter() {
        mAdapter.notifyDataSetChanged();
        mLvCommonMsg.setSelection(messagesList.size() - 1);
    }

    @Override
    protected void onDestroy() {
        LcUserManager.instance.removeHandler(groupJID, handler);
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        // TODO Auto-generated method stub
    }
}