package com.lorent.chat.smack.roomiq;

import com.lorent.chat.smack.roomiq.RMIQInfo.RmRequestType;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.util.XmlStringBuilder;

import java.util.List;

public class RMReplyIQ extends IQ{
	

	private String rest = null ;
	private String roomJid = null;
	private RmRequestType requestType;		//RMRequestIQ 的操作类型 
	
	private List<RoomJoined> roomJoined;
	public String getRoomJid() {
	    return roomJid;
	}

	public void setRoomJid(String roomJid) {
		this.roomJid = roomJid;
	}

	public String getRest() {
		return rest;
	}

	public void setRest(String rest) {
		this.rest = rest;
	}

	public RmRequestType getRequestType() {
	    return requestType;
	}

	public void setRequestType(RmRequestType requestType) {
		this.requestType = requestType;
	}
	
	public void setRoomJoined(List<RoomJoined> roomJoined)
	{
		this.roomJoined = roomJoined;
	}

	public List<RoomJoined> getRoomJoined()
	{
		return this.roomJoined;
	}
	
    protected RMReplyIQ(String childElementName, String childElementNamespace, RmRequestType requestType) {
		super(childElementName, childElementNamespace);
		// TODO Auto-generated constructor stub
		this.requestType = requestType;
	}

	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
		// TODO Auto-generated method stub
		XmlStringBuilder clild = xml.attribute("requestType", requestType);
		xml.rightAngleBracket();
		
		if(requestType == RmRequestType.UpdateAffiliation)
		{
			xml.element("rest", rest);
			xml.element("roomJid", roomJid);
		}
		
		if(requestType == RmRequestType.GetRoomJoined)
		{
			
		}
		return xml;
	}

}
