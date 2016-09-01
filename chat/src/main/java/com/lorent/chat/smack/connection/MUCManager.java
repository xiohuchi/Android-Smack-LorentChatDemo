package com.lorent.chat.smack.connection;

import com.lorent.chat.common.LcUserManager;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.util.Map;

public class MUCManager {
	
	private Map<String, Object> mJIDMutilChats;
	
	public MUCManager(){
		
		this.mJIDMutilChats = LcUserManager.instance.mJIDMutilChats;
		
	}
	
	/**
	 * 创建一个会话
	 * @param roomFullJID 会话对象JID
	 * @param listener 会话消息监听器
	 * @return 返回获取的会话
	 */
	public MultiUserChat createMultiUserChat(String roomFullJID, MessageListener listener){
		
		XMPPConnection connection;
		if(mJIDMutilChats.get(roomFullJID) == null){
			
			connection = MXmppConnManager.getInstance().getConnection();
			if(connection != null)
            {
				MultiUserChat muc = MultiUserChatManager.getInstanceFor(connection).getMultiUserChat(roomFullJID);  
	            muc.addMessageListener(listener);
				mJIDMutilChats.put(roomFullJID, muc);
				return muc;
            }
			else
				return null;
			
		}else{
			listener = null;
			return (MultiUserChat)mJIDMutilChats.get(roomFullJID);
		}
	}
}