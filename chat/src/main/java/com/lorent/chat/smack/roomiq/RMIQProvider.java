package com.lorent.chat.smack.roomiq;

import android.util.Log;

import com.lorent.chat.smack.roomiq.RMIQInfo.RmRequestType;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Element;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * 
 * 
 * IQ RECV FORMATE 1:
 * 
 * UpdateAffiliation
 * 
 * <iq type="result" id="Kzf2u-170" from="roommember.yqpc" to="wyq@yqpc/Smack">
 * 	<query xmlns="lorent:iq:roommember" requestType="UpdateAffiliation">
 * 		<rest>
 * 			succeed
 * 		</rest>
 * 		<roomJid>
 * 			201@wangke.yqpc
 * 		</roomJid>
 * 	</query>
 * </iq>
 *****************************************************8
 * FORMATE 2:
 * <iq type="result" id="Kzf2u-172" from="roommember.yqpc" to="wyq@yqpc/Smack">
 * 		<query xmlns="lorent:iq:roommember" requestType="GetRoomJoined">
 * 			<rest>
 * 				succeed
 * 			</rest>
 * 			<room affiliation="member">
 * 				201@wangke.yqpc
 * 			</room>
 * 			<room affiliation="member">
 * 				workgroup-demo@conference.yqpc
 * 			</room>
 * 		</query>
 * </iq>
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */
public class RMIQProvider extends IQProvider{

	private static final String tag = "RMIQProvider";

	@Override
	public Element parse(XmlPullParser xmlPull, int arg1) throws XmlPullParserException, IOException, SmackException {
		// TODO Auto-generated method stub		
		String requestType = xmlPull.getAttributeValue("", "requestType");  
		
		if(requestType.equals(RmRequestType.UpdateAffiliation.toString()))
		{
			return parseUpdateAffiliationReplay(xmlPull);
		}
		else
		if(requestType.equals(RmRequestType.GetRoomJoined.toString()))
		{
			return  parseGetRoomJoinedReplay(xmlPull);
		}
		else
			Log.e(tag, "parse.XmlPullParser recv RMReplyIQ unknow requestType : " + requestType);

		return null;
	}
	
	
	RMReplyIQ parseUpdateAffiliationReplay(XmlPullParser xmlPull) throws XmlPullParserException, IOException
	{
		RMReplyIQ replay = new RMReplyIQ(RMIQInfo.ELEMENT_NAME, RMIQInfo.ELEMENT_NAMESPACE, RmRequestType.UpdateAffiliation);

		int eventType = xmlPull.next();
		outerloop:
		while (eventType != XmlPullParser.END_DOCUMENT) {
	            switch (eventType) {
	                case XmlPullParser.START_TAG:
	                    String elementName = xmlPull.getName();
//	                    Log.w(tag, "parse.XmlPullParser recv RMReplyIQ elementName : " + elementName);
	                    
	                    if ("rest".equals(elementName)) {
	                    	replay.setRest(xmlPull.nextText());
	                    }
	                    else if ("roomJid".equals(elementName)) {
	                    	replay.setRoomJid(xmlPull.nextText());
	                    }
	                    
	                    break;
	                case XmlPullParser.END_TAG:
	                    if (xmlPull.getDepth() == 3) {
	                        break outerloop;
	                    }
	                    break;
	            }
	            eventType = xmlPull.next();
	    }
		
/*		Log.w(tag, "parse.XmlPullParser UpdateAffiliation : ReplayIQ" 
									+ "type : " + replay.getType()  + "\r\n"
									+ "rest : " + replay.getRest()  + "\r\n"
									+ "roomjid : " + replay.getRoomJid()   + "\r\n" 
									);
*/		
		return replay;
	}
	
	RMReplyIQ parseGetRoomJoinedReplay(XmlPullParser xmlPull) throws XmlPullParserException, IOException
	{
		List<RoomJoined> roomJoined = new ArrayList<RoomJoined>();
		RMReplyIQ replay = new RMReplyIQ(RMIQInfo.ELEMENT_NAME, RMIQInfo.ELEMENT_NAMESPACE, RmRequestType.GetRoomJoined);
		
		int eventType = xmlPull.next();
		outerloop:
		while (eventType != XmlPullParser.END_DOCUMENT) {
	            switch (eventType) {
	                case XmlPullParser.START_TAG:
	                    String elementName = xmlPull.getName();
/*	                    Log.w(tag, "parse.XmlPullParser recv RMReplyIQ elementName : " + elementName);
*/	                    
	                    if ("rest".equals(elementName)) {
	                    	replay.setRest(xmlPull.nextText());
	                    }

	                    else if("room".equals(elementName))
	                    {
	                        String affiliation = xmlPull.getAttributeValue("", "affiliation");  
	                        String roomJid = xmlPull.nextText();
	                        
	                        roomJoined.add(new RoomJoined(roomJid, affiliation));
	                		//Log.w(tag, "parse.XmlPullParser recv RMReplyIQ room : " + roomJid + ", " + affiliation);
	                    }
	                    
	                    break;
	                case XmlPullParser.END_TAG:
	                    if (xmlPull.getDepth() == 3) {
	                        break outerloop;
	                    }
	                    break;
	            }
	            eventType = xmlPull.next();
	    }
		
/*		Log.e(tag, "parse.XmlPullParser GetRoomJoined RMReplyIQ : " 
									+ "type : " + replay.getType()  + "\r\n"
									+ "rest : " + replay.getRest()  + "\r\n"
									+ "roomjid : " + replay.getRoomJid()   + "\r\n" 
									);
*/		
		replay.setRoomJoined(roomJoined);
		return replay;
	}
	
}
