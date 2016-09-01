package com.lorent.chat.smack.roomiq;

import android.util.Log;

import com.lorent.chat.smack.roomiq.RMIQInfo.RmRequestType;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Stanza;

import java.util.List;

/*
 * 接收服务器返回的IQ消息
 */
public class RMReplyIQListener implements PacketListener{

	private static final String tag = "RMReplyIQListener";
	XMPPConnection connection;
	
	public RMReplyIQListener(XMPPConnection con) {
        connection = con;
	}

	
	@Override
	public void processPacket(Stanza packet) throws NotConnectedException {
		// TODO Auto-generated method stub
		
		Log.e(tag, "processPacket : " 
				+ "packetId : " + packet.getPacketID()  + "\r\n"
				+ "fromjid : " + packet.getFrom()   + "\r\n" 
				+ "tojid : " + packet.getTo()   + "\r\n" 
				);
		
		if(packet instanceof RMReplyIQ)
		{
			RMReplyIQ iq = (RMReplyIQ)packet;
			if(iq.getRequestType() == RmRequestType.UpdateAffiliation)
			{
				
			}

			if(iq.getRequestType() == RmRequestType.GetRoomJoined)
			{
				List<RoomJoined> roomJoined = iq.getRoomJoined();
				for(int i  = 0 ; roomJoined != null && i < roomJoined.size(); i++)
				{
					RoomJoined room = roomJoined.get(i);
					Log.e(tag, "processPacket GetRoomJoined : \r\n" 
							+ "room : " + room.getRoomJid()  + ", affiliation : " + room.getAffiliation() + "\r\n");
					
				}
			}
			
		}
	}

}
