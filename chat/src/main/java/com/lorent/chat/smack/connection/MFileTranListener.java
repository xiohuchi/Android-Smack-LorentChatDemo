package com.lorent.chat.smack.connection;

import android.os.Handler;
import android.util.Log;

import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.common.LorentChatApplication;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.smack.engien.MsgEume.CHAT_STATES;
import com.lorent.chat.smack.engien.MsgEume.MSG_CONTENT_TYPE;
import com.lorent.chat.smack.engien.MsgEume.MSG_DERATION;
import com.lorent.chat.smack.engien.MsgEume.MSG_STATE;
import com.lorent.chat.ui.activitys.SingleChatActivity;
import com.lorent.chat.ui.db.dao.ChattingPeopleDAO;
import com.lorent.chat.ui.db.dao.MessageDAO;
import com.lorent.chat.ui.entity.CommonMessage;
import com.lorent.chat.utils.FileUtils;
import com.lorent.chat.utils.TypeConverter;
import com.lorent.chat.utils.cache.CacheUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 文件接收类
 *
 * @author WHF
 */
public class MFileTranListener implements FileTransferListener {

    private MessageDAO messageDAO = (MessageDAO) LcUserManager.instance.dabatases.get(CustomConst.DAO_MESSAGE);
    private ChattingPeopleDAO cPeopleDAO = (ChattingPeopleDAO) LcUserManager.instance.dabatases.get(CustomConst.DAO_CHATTING);

    public MFileTranListener() {
    }

    private String getHostId(){
        return LcUserManager.instance.getUserName();
    }

    @Override
    public void fileTransferRequest(FileTransferRequest request) {

        final IncomingFileTransfer accept = request.accept();
        String uid = request.getRequestor().split("/")[0];
        String content = CacheUtils.getImagePath(LorentChatApplication.getInstance(), "receiveImage/" + TypeConverter.getUUID() + ".pilin");
        File file = new File(content);
        if (FileUtils.mkdirs(content)) {
            ///创建聊天会话
            Chat chat = MXmppConnManager.getInstance().getChatManager().createChat(uid, (ChatMessageListener) MXmppConnManager.getInstance().getChatMListener().new MsgProcessListener());
            try {
                accept.recieveFile(file);
            } catch (SmackException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            long mills = System.currentTimeMillis();
            CommonMessage message = new CommonMessage(uid,
                    "",
                    mills,
                    "0.12km",
                    content,
                    MSG_STATE.RECEIVEING,
                    MSG_CONTENT_TYPE.IMAGE,
                    MSG_DERATION.RECEIVE,
                    TypeConverter.nullStringDefaultValue(
                            LcUserManager.instance.friendsNames.get(uid.trim()), uid.split("@")[0]), CHAT_STATES.STNULL);

            long rowid = messageDAO.save(message, getHostId());
            new updateStatusthread(mills, rowid, accept, uid).start();
            if (!LcUserManager.instance.mJIDChats.containsKey(uid)) {
                LcUserManager.instance.mJIDChats.put(uid, chat);
            }
            //刷新消息列表
            if (!cPeopleDAO.peopleChatting(uid, getHostId())) {
                android.os.Message om = new android.os.Message();
                om.what = CustomConst.HANDLER_CHATPEOPLE_LIST_ADD;
                om.obj = uid;
                LcUserManager.instance.getHandlers("MsgFragment").get(0).sendMessage(om);
            }
            handRefreshSession(uid);
            refreshChatDialog(uid, rowid);
        }
    }

    /**
     * 文件接收状态更新线程
     *
     * @author whf
     */
    class updateStatusthread extends Thread {

        private long rowid;
        private IncomingFileTransfer accept;
        private String uid;
        private long mills;

        public updateStatusthread(long mills, long rowid, IncomingFileTransfer accept, String uid) {
            this.rowid = rowid;
            this.accept = accept;
            this.uid = uid;
            this.mills = mills;
        }

        @Override
        public void run() {

            while (!accept.isDone()) {
            }
            messageDAO.updateStateByRowid(rowid, getHostId(), 1);
            refreshChatImageMsg(uid, mills, CustomConst.HANDLER_MSG_FILE_SUCCESS);
            handRefreshSession(uid);
        }

    }

    ///
    public static void handRefreshSession(String uid) {
        android.os.Message om = new android.os.Message();
        om.what = CustomConst.HANDLER_CHATPEOPLE_LIST_UPDATE;
        om.obj = uid;
        LcUserManager.instance.getHandlers("MsgFragment").get(0).sendMessage(om);
    }

    /**
     * 刷新聊天窗口信息
     *
     * @param uid
     */
    public void refreshChatDialog(String uid, long rowid) {

        //聊天对话框内刷新
        List<Handler> handlers = LcUserManager.instance.getHandlers(uid);

        if (handlers != null) {
            for (Handler hand : handlers) {
                Log.i("MReceiveChatListener", hand.getClass().toString().split("$")[0]);
                if (hand.getClass().toString().contains("SingleChatActivity")) {
                    handChatActivity(hand, rowid);
                }
            }
        }
    }

    public void refreshChatImageMsg(String uid, long mills, int what) {

        // 聊天对话框内刷新
        List<Handler> handlers = LcUserManager.instance.getHandlers(uid);
        if (handlers == null) return;
        for (Handler hand : handlers) {
            Log.i("MReceiveChatListener",
                    hand.getClass().toString().split("$")[0]);
            if (hand.getClass().toString().contains(SingleChatActivity.class.getSimpleName())) {
                refreshImageMsg(hand, mills, what);
            }

        }

    }

    /**
     * 更新图片消息状态
     *
     * @param handler
     * @param mills
     */
    public void refreshImageMsg(Handler handler, long mills, int what) {
        android.os.Message osMsg = new android.os.Message();
        osMsg.what = what;
        osMsg.obj = mills;
        handler.sendMessage(osMsg);
    }

    /**
     * 刷新聊天窗口信息
     *
     * @param handler
     * @param mMsg
     */
    public void handChatActivity(Handler handler, long mMsg) {
        android.os.Message osMsg = new android.os.Message();
        osMsg.what = CustomConst.HANDLER_MGS_ADD;
        osMsg.obj = mMsg;
        handler.sendMessage(osMsg);

    }


}
