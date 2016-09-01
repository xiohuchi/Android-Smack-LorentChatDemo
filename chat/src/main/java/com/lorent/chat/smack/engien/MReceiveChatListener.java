package com.lorent.chat.smack.engien;

import android.os.Handler;
import android.util.Log;

import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.smack.connection.XmppFriendManager;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.smack.engien.MsgEume.CHAT_STATES;
import com.lorent.chat.smack.engien.MsgEume.MSG_CONTENT_TYPE;
import com.lorent.chat.smack.engien.MsgEume.MSG_DERATION;
import com.lorent.chat.smack.engien.MsgEume.MSG_STATE;
import com.lorent.chat.ui.db.dao.ChattingPeopleDAO;
import com.lorent.chat.ui.db.dao.MessageDAO;
import com.lorent.chat.ui.entity.CommonMessage;
import com.lorent.chat.utils.TypeConverter;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * 接收聊天信息的监听
 */
public class MReceiveChatListener implements ChatManagerListener {

    private static final String tag = "MReceiveChatListener";
    XmppFriendManager xManager;
    MessageDAO messageDAO;
    ChattingPeopleDAO cPeopleDAO;
    String hostUid;

    public MReceiveChatListener() {
        this.xManager = XmppFriendManager.getInstance();
        this.hostUid = MXmppConnManager.hostUid;
        messageDAO = (MessageDAO) LcUserManager.instance.dabatases.get(CustomConst.DAO_MESSAGE);
        cPeopleDAO = (ChattingPeopleDAO) LcUserManager.instance.dabatases.get(CustomConst.DAO_CHATTING);
    }

    @Override
    public void chatCreated(Chat chat, boolean isExist) {
        if (chat.getListeners().isEmpty()) {
            chat.addMessageListener(new MsgProcessListener());
        }
    }

    public class MsgProcessListener implements ChatMessageListener {

        @Override
        public void processMessage(Chat chat, Message msg) {

            //与每个用户的会话只应该有一个消息监听器
            boolean bImage = false;
            if (msg.getBody().contains("<img>") && msg.getBody().contains("</img>"))
                bImage = true;

            if (msg.getType() != Type.chat) {
                Log.e(tag, "getFrom : " + msg.getFrom());
                Log.e(tag, "getTo : " + msg.getTo());

                if (!bImage)
                    Log.e(tag, "getBody : " + msg.getBody());

                Log.e(tag, "getType : " + msg.getType().toString());
                Log.e(tag, "getSubject : " + msg.getSubject());
                Log.e(tag, "getBodies : " + msg.getBodies().toString());
                if (bImage) {
                    Log.e(tag, "getXmlns : " + "收到是<图片内容>的消息");
                } else
                    Log.e(tag, "getXmlns : " + msg.toXML());

                return;
            }

            //Log.e(tag, "toXML : " + msg.toXML());
            CHAT_STATES chatstates = CHAT_STATES.STNULL;
            ChatStateExtension chatEx = msg.getExtension("composing", "http://jabber.org/protocol/chatstates");
            if (chatEx != null) {
                chatstates = CHAT_STATES.COMPOSING;
                //System.out.println("对方正在输入......" );
            }

            chatEx = msg.getExtension("active", "http://jabber.org/protocol/chatstates");
            if (chatEx != null) {
                chatstates = CHAT_STATES.ACTIVE;
                //System.out.println("对方关注中......");
            }

            chatEx = msg.getExtension("paused", "http://jabber.org/protocol/chatstates");
            if (chatEx != null) {
                chatstates = CHAT_STATES.PAUSED;
                //System.out.println("对方已暂停输入" );
            }

            chatEx = msg.getExtension("inactive", "http://jabber.org/protocol/chatstates");
            if (chatEx != null) {
                chatstates = CHAT_STATES.INACTIVE;
                //System.out.println("对方聊天窗口失去焦点" );
            }

            chatEx = msg.getExtension("gone", "http://jabber.org/protocol/chatstates");
            if (chatEx != null) {
                chatstates = CHAT_STATES.GONE;
                //System.out.println("对方聊天窗口被关闭" );
            }
            /////////////////////////////////////////////////////

            String uid = msg.getFrom().split("/")[0];
            CommonMessage mMsg = null;
            long rowid;

            //try {
            try {
                mMsg = new CommonMessage(uid.trim(), xManager
                        .getUserIconAvatar(uid),
                        System.currentTimeMillis(), "",
                        msg.getBody(), MSG_STATE.ARRIVED,
                        MSG_CONTENT_TYPE.TEXT, MSG_DERATION.RECEIVE,
                        TypeConverter.nullStringDefaultValue(
                                LcUserManager.instance.friendsNames.get(uid.trim()), uid.split("@")[0]),
                        chatstates);
            } catch (FileNotFoundException | XMPPException e) {
                e.printStackTrace();
            }

            ///messags.add(mMsg);
            /////////////////////////////////////////////////////
            //add by wyq
            if (chatstates != CHAT_STATES.STNULL) {
                //向聊天窗口发送状态消息
                //聊天对话框内刷新
                List<Handler> handlers = LcUserManager.instance.getHandlers(uid);
                for (Handler hand : handlers) {
                    Log.i(tag, hand.getClass().toString().split("$")[0]);
                    if (hand.getClass().toString().contains("SingleChatActivity")) {
                        updateChatActivityStates(hand, mMsg);
                    }

                }
                return;
            }
            /////////////////////////////////////////////////////
            //Log.i(tag, "接收到信息：" +msg.getBody());
            if (msg.getBody().contains("<img>") && msg.getBody().contains("</img>")) {
                Log.e(tag, "getXmlns : " + "收到是<图片内容>的消息");
            } else
                Log.e(tag, "getXmlns : " + msg.toXML());

            rowid = messageDAO.save(mMsg, hostUid);
            Log.e(tag, "messageDAO.save : " + chatstates + ", rowid : " + rowid);

            if (!LcUserManager.instance.mJIDChats.containsKey(uid)) {
                //LorentChatApplication.mJIDChats.put(uid, chat);
            }
            //chang wyq
            if (chatstates == CHAT_STATES.STNULL) {
                //刷新消息列表
                if (!cPeopleDAO.peopleChatting(uid, hostUid)) {
                    android.os.Message om = new android.os.Message();
                    om.what = CustomConst.HANDLER_CHATPEOPLE_LIST_ADD;
                    om.obj = uid;
                    LcUserManager.instance.getHandlers("MsgFragment").get(0).sendMessage(om);
                }
                handRefreshSession(uid);
            }

            //聊天对话框内刷新
            List<Handler> handlers = LcUserManager.instance.getHandlers(uid);

            for (Handler hand : handlers) {
                Log.i(tag, hand.getClass().toString().split("$")[0]);
                if (hand.getClass().toString().contains("SingleChatActivity")) {
                    handChatActivity(hand, rowid);
                }
            }
        }

        public void handRefreshSession(String uid) {
            android.os.Message om = new android.os.Message();
            om.what = CustomConst.HANDLER_CHATPEOPLE_LIST_UPDATE;
            om.obj = uid;
            LcUserManager.instance.getHandlers("MsgFragment").get(0).sendMessage(om);
        }

        /**
         * 刷新聊天窗口信息
         *
         * @param handler
         * @param mMsg
         */
        public void handChatActivity(Handler handler, long mMsg) {
            android.os.Message osMsg = new android.os.Message();
            osMsg.what = 0;
            osMsg.obj = mMsg;
            handler.sendMessage(osMsg);
        }

        /**
         * 刷新聊天窗口状态
         *
         * @param handler
         * @param mMsg
         */
        public void updateChatActivityStates(Handler handler, Object mMsg) {
            android.os.Message osMsg = new android.os.Message();
            osMsg.what = CustomConst.HANDLER_MSG_CHATSTATES;
            osMsg.obj = mMsg;
            handler.sendMessage(osMsg);
        }
    }
}
