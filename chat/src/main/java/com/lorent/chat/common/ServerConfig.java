package com.lorent.chat.common;

import com.lorent.chat.utils.XLog;
import com.lorent.chat.utils.XMPPServer;

import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

/**
 * 聊天的服务器配置类
 * Created by zy on 2016/8/11.
 */
public enum ServerConfig {
    instance;

    private XMPPServer server;
    private java.lang.String tag = ServerConfig.class.getSimpleName();

    public XMPPServer getServer() {
        return server;
    }

    public void setServer(XMPPServer server) {
        this.server = server;
        getConfig();
    }

    private XMPPTCPConnectionConfiguration config;

    public XMPPTCPConnectionConfiguration getConfig() {

        if (server != null && config == null)
            config = XMPPTCPConnectionConfiguration.builder()
                    //服务器IP地址
                    .setHost(server.getServerIP())
                    //服务器端口
                    .setPort(server.getServerPort())
                    //服务器名称
                    .setServiceName(server.getServerName())
                    //是否开启安全模式
                    .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled)
                    //是否开启压缩
                    .setCompressionEnabled(false)
                    //开启调试模式
                    .setDebuggerEnabled(false)
                    .build();

        if (server != null)
            XLog.i(tag, "startConnectServer : " + server);

        return config;
    }
}
