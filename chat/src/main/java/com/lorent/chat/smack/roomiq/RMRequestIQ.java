package com.lorent.chat.smack.roomiq;

import com.lorent.chat.smack.roomiq.RMIQInfo.RmRequestType;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.util.XmlStringBuilder;

/*
 * 讨论组操作:
 * 			加入
 * 			退出
 * 			设置为所有者
 * 			设置为管理者
 * 			设置为白名单
 * 
 */
public class RMRequestIQ extends IQ{
	
	private String roomJid;
	private String userJid;
	private String affiliation;
	private RmRequestType requestType;		//RMRequestIQ 的操作类型 
	
	public String getRoomJid() {
	    return roomJid;
	}

	public void setRoomJid(String roomJid) {
		this.roomJid = roomJid;
	}

	public String getUserJid() {
		return userJid;
	}

	public void setUserJid(String userJid) {
		this.userJid = userJid;
	}

	public String getAffiliation() {
		return this.affiliation;
	}
	  
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}
	
	

	public RMRequestIQ(String childElementName, String childElementNamespace, RmRequestType requestType) {
		super(childElementName, childElementNamespace);
		// TODO Auto-generated constructor stub
		this.requestType = requestType;
	}
	
	public RmRequestType getRequestType() {
	    return requestType;
	}

	public void setRequestType(RmRequestType requestType) {
		this.requestType = requestType;
	}
	

	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
		// TODO Auto-generated method stub
		XmlStringBuilder clild = xml.attribute("requestType", requestType);
		xml.rightAngleBracket();		
		if(requestType == RmRequestType.UpdateAffiliation)
		{
			clild.element("roomJid", roomJid);
			clild.element("userJid", userJid);
			clild.element("affiliation", affiliation);
		}
	     
		if(requestType == RmRequestType.GetRoomJoined)
		{
			clild.element("userJid", userJid);
		}
		return xml;
	}
	
}
