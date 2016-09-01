package com.lorent.chat.smack.connection;

import android.util.Log;

import com.lorent.chat.common.LcUserManager;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;

import java.util.Map;

public class MChatManager {
	
	private static final String tag = "MChatManager";

	private ChatManager chatManager;
	
	private Map<String, Object> mJIDChats;
	
	public MChatManager(ChatManager chatManager){
		
		this.chatManager = chatManager;
		
		this.mJIDChats = LcUserManager.instance.mJIDChats;
		
	}
	/**
	 * 创建一个会话
	 * @param threadId 会话线程ID
	 * @param jid 会话对象JID
	 * @param listener 会话消息监听器
	 * @return 返回获取的会话
	 */
	public Chat createChat(String threadId,String jid,ChatMessageListener listener){
		if(threadId == null || chatManager.getThreadChat(threadId) == null){
			return chatManager.createChat(jid, threadId, listener);
			//return chatManager.createChat(jid, threadId, listener);
		}else{
			return chatManager.getThreadChat(threadId);
		}
	}
	
	/**
	 * 创建一个会话
	 * @param jid 会话对象JID
	 * @param listener 会话消息监听器
	 * @return 返回获取的会话
	 */
	public Chat createChat(String jid, ChatMessageListener listener){
		if(mJIDChats.get(jid) == null){
			Log.i(tag, "creat signel chat to : " + jid);
			Chat chat = chatManager.createChat(jid, (ChatMessageListener)listener);
			LcUserManager.instance.mJIDChats.put(jid, chat);
			return chat;
		}else{
			listener = null;
			return (Chat)mJIDChats.get(jid);
		}
	}
}
