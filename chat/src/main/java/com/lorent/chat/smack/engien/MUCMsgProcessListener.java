package com.lorent.chat.smack.engien;

import android.os.Handler;
import android.util.Log;

import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.common.LorentChatApplication;
import com.lorent.chat.message.GroupChatExtType;
import com.lorent.chat.message.GroupChatExtType.ExtType;
import com.lorent.chat.message.GroupChatFileMessageBody;
import com.lorent.chat.message.GroupChatImageMessageBody;
import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.smack.engien.MsgEume.CHAT_STATES;
import com.lorent.chat.smack.engien.MsgEume.MSG_CONTENT_TYPE;
import com.lorent.chat.smack.engien.MsgEume.MSG_DERATION;
import com.lorent.chat.smack.engien.MsgEume.MSG_STATE;
import com.lorent.chat.ui.entity.CommonMessage;
import com.lorent.chat.utils.FileUtils;
import com.lorent.chat.utils.TypeConverter;
import com.lorent.chat.utils.XLog;
import com.lorent.chat.utils.cache.CacheUtils;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import java.util.List;

public class MUCMsgProcessListener implements MessageListener {
    private static final String tag = "MUCMsgProcessListener";

    ///
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
    public void updateGroupChatActivity(Handler handler, Object mMsg) {
        android.os.Message osMsg = new android.os.Message();
        osMsg.what = CustomConst.HANDLER_MGS_ADD;
        osMsg.obj = mMsg;
        handler.sendMessage(osMsg);
    }

    @Override
    public void processMessage(Message msg) {
        // TODO Auto-generated method stub
        //聊天室的信息没有记录到数据库内
//			boolean bImage = false;
        CommonMessage mChatMsg = null;
        MSG_DERATION msgType = MSG_DERATION.GROUP_RECEIVE;
        String msgBody = msg.getBody();
        ExtType msgContentType;

        msgContentType = GroupChatExtType.getExtType(msgBody);
/*			
            if(msgBody != null && msgBody.contains("<img>") && msgBody.contains("</img>"))
				bImage = true;
*/
        XLog.e(tag, "getFrom : " + msg.getFrom());
        XLog.e(tag, "getTo : " + msg.getTo());
        if (msgContentType == ExtType.GroupChatNormal)
            XLog.e(tag, "getBody : " + msg.getBody());
        //XLog.e(tag, "getType : " + msg.getType().toString());
        //XLog.e(tag, "getSubject : " + msg.getSubject());
        //XLog.e(tag, "getBodies : " + msg.getBodies().toString());

        /**
         * from fomat  : 201@wangke.yqpc/15013208165
         *				 聊天室名/用户名
         */

        String groupJID = msg.getFrom().split("/")[0];
        //这个不合理,不一定是本域成员
        String fromSender = msg.getFrom().split("/")[1] + "@" + MXmppConnManager.getInstance().getRemoteServerName();
        String localUser = LcUserManager.instance.getFullUserName().split("/")[0];

        if (msgContentType == ExtType.GroupChatExtImage) {
        }
        //else
        //	XLog.e(tag, "getXmlns : " + msg.toXML());

        if (fromSender == null || fromSender.isEmpty()) {
            //系统消息
            msgType = MSG_DERATION.GROUP_RECEIVE;
        } else if (fromSender.equals(localUser)) {
            msgType = MSG_DERATION.GROUP_SEND;
        } else {
            msgType = MSG_DERATION.GROUP_RECEIVE;
        }
        XLog.e(tag, "groupJID : " + groupJID + ", sender : " + fromSender + ", localuser : " + localUser + ", msgType : " + msgType.toString());

        if (msgContentType == ExtType.GroupChatNormal) {
            mChatMsg = new CommonMessage(groupJID, "",
                    System.currentTimeMillis(), "",
                    msg.getBody(), MSG_STATE.ARRIVED,
                    MSG_CONTENT_TYPE.TEXT, msgType,
                    fromSender,
                    CHAT_STATES.STNULL);
        } else if (msgContentType == ExtType.GroupChatExtImage) {
            XLog.e(tag, "getXmlns : " + "收到是<图片内容>的消息");
            String imageFile = saveBobyAsImage(groupJID.split("@")[0], msg.getBody());

            if (imageFile != null)
                mChatMsg = new CommonMessage(groupJID, "",
                        System.currentTimeMillis(), "",
                        imageFile, MSG_STATE.ARRIVED,
                        MSG_CONTENT_TYPE.IMAGE, msgType,
                        fromSender,
                        CHAT_STATES.STNULL);

        } else if (msgContentType == ExtType.GroupChatExtFile) {
            XLog.e(tag, "getXmlns : " + "收到是<文件共享>的消息");

            GroupChatFileMessageBody fileMsg = new GroupChatFileMessageBody();

            fileMsg.setMessageBody(msg.getBody());

            String promptMsg = "共享了<<" + fileMsg.getFileName() + ">>" + ", 于" + fileMsg.getDate();


            mChatMsg = new CommonMessage(groupJID, "",
                    System.currentTimeMillis(), "",
                    promptMsg, MSG_STATE.ARRIVED,
                    MSG_CONTENT_TYPE.TEXT, msgType,
                    fromSender,
                    CHAT_STATES.STNULL);
        }

        List<Handler> handlers = LcUserManager.instance.getHandlers(groupJID);

        for (Handler hand : handlers) {
            XLog.i("MReceiveChatListener", hand.getClass().toString().split("$")[0]);
            if (hand.getClass().toString().contains("ChatActivity")) {
                updateGroupChatActivity(hand, mChatMsg);
            }
        }
    }

    private String saveBobyAsImage(String groupJid, String imageBody) {

        String baseDir = CacheUtils.getImagePath(LorentChatApplication.getInstance(), CustomConst.IMAGE_CACHE_PATH);
        String fileName = TypeConverter.getUUID() + ".jpg";
        String fileFullName = baseDir + "/" + groupJid + "/" + fileName;

        if (FileUtils.mkdirs(fileFullName)) {
            GroupChatImageMessageBody messageBody = new GroupChatImageMessageBody();
            if (!messageBody.setMessageBody(imageBody))
                return null;
            if (!messageBody.saveImage(fileFullName))
                return null;
            return fileFullName;
        }
        return null;
    }

}
