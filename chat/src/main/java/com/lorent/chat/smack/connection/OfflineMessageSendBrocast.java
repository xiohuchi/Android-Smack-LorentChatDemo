package com.lorent.chat.smack.connection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.lorent.chat.R;
import com.lorent.chat.recceiver.OfflineMsgReceiver;
import com.lorent.chat.smack.engien.MsgEume.MSG_STATE;
import com.lorent.chat.ui.entity.ChatMessage;
import com.lorent.chat.utils.TypeConverter;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OfflineMessageSendBrocast {

    public static Map<String, ArrayList<Message>> getOfflineMegs() {

        Map<String, ArrayList<Message>> offlines = new HashMap<String, ArrayList<Message>>();

        try {
            List<Message> ites = null;
            try {
                ites = MXmppConnManager.getInstance().getOffMsgManager().getMessages();
            } catch (NoResponseException | NotConnectedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //while(ites.hasNext())
            for (int i = 0; i < ites.size(); i++) {

                Message message = ites.get(i);
                String fromUser = message.getFrom().split("/")[0];

                if (offlines.containsKey(fromUser)) {
                    offlines.get(fromUser).add(message);
                } else {
                    ArrayList<Message> temp = new ArrayList<>();
                    temp.add(message);
                    offlines.put(fromUser, temp);

                }

            }

        } catch (XMPPException e) {
            e.printStackTrace();
            return null;
        }
        return offlines;
    }

    public static void sendBrocast(Context context, Map<String, ArrayList<Message>> messages) {
        if (messages != null && messages.size() > 0) {
            Set<String> keys = messages.keySet();
            Iterator<String> keyIte = keys.iterator();
            Intent intent = new Intent(context, OfflineMsgReceiver.class);
            ChatMessage chatListUserInfo = null;

            while (keyIte.hasNext()) {
                String key = keyIte.next();
                chatListUserInfo = new ChatMessage(R.drawable.people_icon_selector,
                        key,
                        TypeConverter.formatDate(new Date(), "MM-dd HH:mm:ss"),
                        messages.get(key).get(0).getBody("UTF-8"),
                        MSG_STATE.ARRIVED);

                Bundle bundle = new Bundle();
                bundle.putSerializable("user", chatListUserInfo);
                intent.putExtra("user", bundle);
                context.sendBroadcast(intent);
            }
        }
        try {
            MXmppConnManager.getInstance().getOffMsgManager().deleteMessages();
            MXmppConnManager.getInstance().setPresence(0);

        } catch (XMPPException | NoResponseException | NotConnectedException e) {
            e.printStackTrace();
        }

    }
}