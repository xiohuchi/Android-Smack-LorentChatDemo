package com.lorent.chat.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.lorent.chat.R;
import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.message.ChatCommonMessage;
import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.smack.engien.MsgEume.MSG_DERATION;
import com.lorent.chat.smack.engien.MsgEume.MSG_STATE;
import com.lorent.chat.ui.db.dao.MessageDAO;
import com.lorent.chat.ui.entity.CommonMessage;
import com.lorent.chat.utils.cache.CacheUtils;
import com.lorent.chat.utils.cache.ImageFetcher;

import java.util.List;

public class ChatAdapter extends BaseAdapter {

    private static final String tag = "ChatAdapter";
    private Context context;
    private List<CommonMessage> messages;
    private MessageDAO messageDAO;
    private String uid;
    private ImageFetcher mImageFetcher;

    public ChatAdapter(Context context, List<CommonMessage> messages) {
        messageDAO = (MessageDAO) LcUserManager.instance.dabatases.get(CustomConst.DAO_MESSAGE);
        uid = MXmppConnManager.hostUid;
        this.context = context;
        this.messages = messages;
        //initImageFetcher();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        CommonMessage msgs = messages.get(position);

        MSG_STATE state = msgs.getState();
        if (!state.equals(MSG_STATE.READED) &&
                !state.equals(MSG_STATE.RECEIVEING) &&
                !state.equals(MSG_STATE.SENDDING) &&
                msgs.getMsgComeFromType().equals(MSG_DERATION.RECEIVE)) {
            messageDAO.updateById(msgs.getId(), uid, 0);
        }

        return msgs;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommonMessage msg = (CommonMessage) getItem(position);
        ChatCommonMessage commonMessage = ChatCommonMessage.getInstance(msg, context, mImageFetcher);
        commonMessage.fillContent();
        View view = commonMessage.getRootView();

        return view;
    }

    protected void initImageFetcher() {
        mImageFetcher = new ImageFetcher(context, 40, 40);
        mImageFetcher.setImageCache(CacheUtils.getImageCache(context, "imageCache/"));
        mImageFetcher.setLoadingImage(R.drawable.people_icon_selector);
        mImageFetcher.setImageFadeIn(true);
    }

}