package com.lorent.chat.smack.roomiq;

public class RoomJoined {

	private String roomJid;
	private String affiliation;
	
	public RoomJoined(RoomJoined from)
	{
		this(from.roomJid, from.affiliation);
	}
	
	public RoomJoined(String roomJid, String affiliation)
	{
		this.roomJid = roomJid;
		this.affiliation = affiliation;
	}
	
	public void setRoomJid(String roomJid)
	{
		this.roomJid = roomJid;
	}
	
	public void setAffiliation(String affiliation)
	{
		this.affiliation = affiliation;
	}
	
	public String getRoomJid()
	{
		return this.roomJid;
	}
	
	public String getAffiliation()
	{
		return this.affiliation;
	}
	
	public String toXML()
	{
		String xml = "<roomJid>"+roomJid+"</roomJid>"+"<affiliation>"+affiliation+"</affiliation>";
		return xml;
	}
	
}
