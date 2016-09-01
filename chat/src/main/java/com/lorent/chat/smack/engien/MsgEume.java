package com.lorent.chat.smack.engien;

public class MsgEume {
	/**
	 * 发送信息状态;
	 * ARRIVED - 0;
	 * READED  - 1;
	 * FAILED  - 2;
	 * RECEIVEING - 3;
	 * SENDDING - 4;
	 * @author WHF
	 */
	public enum MSG_STATE{
		//到达
		ARRIVED,
		//已读
		READED,
		//发送失败
		FAILED,
		//正在发送
		SENDDING,
		//正在接收
		RECEIVEING
	
	}
	/**
	 * 信息流向
	 * RECEIVE - 0;
	 * SEND - 1;
	 * @author WHF
	 *
	 */
	public enum MSG_DERATION{
		//接收
		RECEIVE,
		//发送
		SEND,
		//讨论组接受
		GROUP_RECEIVE,
		GROUP_SEND
		
	}
	/**
	 * 内容类型
	 * TEXT - 0;
	 * IMAGE - 1;
	 * VOICE - 2;
	 * MAP - 3;
	 * @author WHF
	 *
	 */
	public enum MSG_CONTENT_TYPE{
		//文本
		TEXT,
		//图片
		IMAGE,
		//语音
		VOICE,
		//地图
		MAP
		
	}
	
	/**
	 * 用户信息界面类型
	 * @author WHF
	 *
	 */
	public enum USERINFO_TYPE{
		//正在聊天
		CHATTING,
		//用户列表
		FRIEND_NEAR
		
	}
	/**
	 * 提示广播类型
	 * @author WHF
	 *
	 */
	public enum BORCAST_RECEIVER{
		//群
		GROUP,
		//单人
		SINGLE_PEOPLE
		
	}
	//WYQ
	public enum CHAT_STATES{
		STNULL,
		//某人开始一个对话，但是你还没有参与进来
		STARTING,
		//你正参与在对话中，当前你没有组织你的消息，而是在关注
		ACTIVE,
		//你正在组织一个消息
		COMPOSING,
		//你开始组织一个消息，但由于某个原因而停止组织消息
		PAUSED,
		//你一段时间里没有参与这个对话
		INACTIVE,
		//你参与的这个对话已经结束
		GONE
	} 
	
}
