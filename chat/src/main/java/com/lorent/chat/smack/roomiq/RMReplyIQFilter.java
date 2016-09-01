package com.lorent.chat.smack.roomiq;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Stanza;

public class RMReplyIQFilter implements PacketFilter{

	@Override
	public boolean accept(Stanza packet) {
		// TODO Auto-generated method stub
		   boolean RMReplyIQReceived = false;
           if (packet instanceof RMReplyIQ){
        	   RMReplyIQReceived = true;
           }
           return RMReplyIQReceived;
    }
}
