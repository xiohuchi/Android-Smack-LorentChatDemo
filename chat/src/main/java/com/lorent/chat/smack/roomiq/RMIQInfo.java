package com.lorent.chat.smack.roomiq;

public class RMIQInfo {
	public static final String ELEMENT_NAME = "query";
	public static final String ELEMENT_NAMESPACE = "lorent:iq:roommember";
	public static final String RMPluginSubdomain = "roommember";
	
	//MUCRole.Affiliation
	public enum RmAffiliation
	{
		owner,		//设置为所有者
		admin,		//设置为管理员
		member,		//设置为成员
		outcast,	//设置为白名单
		none,		//设置为游客
		quit		//退出该群
	}
	
	public enum RmRequestType
	{
		UpdateAffiliation,	//更新角色，具体类型看RmAffiliation
		GetRoomJoined		//获取所加入、拥有、管理的讨论室
	}
	
}
