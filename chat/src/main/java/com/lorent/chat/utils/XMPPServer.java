package com.lorent.chat.utils;

/**
 * XMPP服务器
 */
public class XMPPServer {
    private String serverIP;
    private int serverPort;
    private String serverName;

    public XMPPServer(String serverIP, int serverPort, String serverName) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.serverName = serverName;
    }

    public String getServerIP() {
        return this.serverIP;
    }

    public int getServerPort() {
        return this.serverPort;
    }

    public String getServerName() {
        return this.serverName;
    }

    @Override
    public String toString() {
        return getServerIP() + ":" + getServerPort() + ", @" + getServerName();
    }
}
