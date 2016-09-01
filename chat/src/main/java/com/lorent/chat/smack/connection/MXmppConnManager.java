package com.lorent.chat.smack.connection;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.common.LorentChatApplication;
import com.lorent.chat.common.ServerConfig;
import com.lorent.chat.smack.LoginAndRegisterHelper;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.smack.constVar.CustomConst.XMPP_HANDLER_ERROR;
import com.lorent.chat.smack.constVar.CustomConst.XMPP_HANDLER_WHAT;
import com.lorent.chat.smack.engien.MReceiveChatListener;
import com.lorent.chat.ui.activitys.MainChatActivity;
import com.lorent.chat.ui.db.dao.MessageDAO;
import com.lorent.chat.utils.ToastUtils;
import com.lorent.chat.utils.XLog;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.offline.OfflineMessageManager;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MXmppConnManager {

    private static final String tag = "MXmppConnManager";
    private XMPPTCPConnection connection;
    private static MXmppConnManager instance;
    private AccountManager accountManager;
    private ConnectionConfiguration config;
    private ChatManager chatManager;
    private OfflineMessageManager offlineMessageManager;
    private ConnectionListener connectionListener;
    private MReceiveChatListener chatManagerListener;
    public FileTransferManager transferManager;
    public static String hostUid;
    public FileTransferListener fileTranListener;
//    private String mFullUserName = "";

    enum MXPPStatus {
        XDispConnect,    //无连接
        XConnecting,    //连接中
        XConnected        //已连接
    }

    //关键变量，外部copy无效
    private volatile MXPPStatus xmppstatus = MXPPStatus.XDispConnect;

    private MXmppRoomManager roomManager = null;

    private MXmppConnManager() {
        instance = this;
    }

    /**
     * 获取一个单例对象
     *
     * @return
     */
    public static MXmppConnManager getInstance() {
        if (instance == null) {
            instance = new MXmppConnManager();
        }
        return instance;
    }

    /**
     * 获取离线信息管理者
     *
     * @return
     */
    public OfflineMessageManager getOffMsgManager() {
        return offlineMessageManager;
    }

    public void setOffLineMessageManager(OfflineMessageManager offlineMessageManager) {
        this.offlineMessageManager = offlineMessageManager;
        fileTranListener = new MFileTranListener();
        transferManager.addFileTransferListener(fileTranListener);
    }

    public Handler getConnMessageHandler() {
        return connMessageHandler;
    }

    /**
     * 获取会话管理者
     *
     * @return
     */
    public ChatManager getChatManager() {
        return chatManager;
    }

    /**
     * 获取账户管理者
     *
     * @return
     */
    public AccountManager getAccountManager() {
        return accountManager;
    }

    /**
     * 获取连接配置
     *
     * @return
     */
    public ConnectionConfiguration getConnectionConfig() {
        return config;
    }

    /**
     * 获取连接状态监听器
     *
     * @return
     */
    public ConnectionListener getConnListener() {
        return connectionListener;
    }

    /**
     * 获取会话监听器
     *
     * @return
     */
    public MReceiveChatListener getChatMListener() {
        return chatManagerListener;
    }

    /**
     * 获取一个连接
     *
     * @return
     * @throws XMPPException
     */
    public XMPPTCPConnection getConnection() {
        if (xmppstatus == MXPPStatus.XConnected) {
            if (connection.isConnected()) {
                return connection;
            } else
                xmppstatus = MXPPStatus.XDispConnect;
        }
        ToastUtils.createCenterNormalToast(LorentChatApplication.getInstance().getApplicationContext(), (String) "网络异常,重连服务器中...", Toast.LENGTH_LONG);
        XLog.e("网络异常,重连服务器中...");
        requestConnectServer();
        return null;
    }

    public String getRemoteServerName() {
        return ServerConfig.instance.getServer().getServerName();
    }

    /*******************************************************************
     * 前端连接服务器、登录
     *******************************************************************/
    /*
     * 连接服务器的方法只能在InitXmppConnectionTask里后台执行
	 */
    private Boolean startConnectServer() {
        if (xmppstatus != MXPPStatus.XDispConnect) {
            XLog.e(tag, "startConnectServer error, MXPPStatus : " + xmppstatus.toString());
            return false;
        }

        feCloseConnection();

        xmppstatus = MXPPStatus.XConnecting;

        try {
            connection = new XMPPTCPConnection(ServerConfig.instance.getConfig());
            connectionListener = new ReConnectionListener(connMessageHandler);
            connection.addConnectionListener(connectionListener);

            connection.connect();

        } catch (Exception e) {
            XLog.e(tag, "连接服务器失败" + e);
            connection = null;
            xmppstatus = MXPPStatus.XDispConnect;
            return false;
        }

        accountManager = AccountManager.getInstance(connection);
        chatManager = ChatManager.getInstanceFor(connection);
        transferManager = FileTransferManager.getInstanceFor(connection);

        roomManager = MXmppRoomManager.getInstance(connection);

        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
        StanzaFilter filter = new AndFilter(new PacketTypeFilter(Presence.class));
        PacketListener packListener = createPacketListener();

        connection.addPacketListener(packListener, filter);

        xmppstatus = MXPPStatus.XConnected;

        XLog.e(tag, "连接服务器成功！ xmppstatus : " + xmppstatus.toString());

        return true;
    }

    /**
     * 关闭连接
     */
    public void feCloseConnection() {

        if (connection != null) {

            XLog.i(tag, "closeConnection : " + ServerConfig.instance.getServer());
            if (connection.isConnected()) {
                connection.removeConnectionListener(connectionListener);
                connection.disconnect();
            }
            connection = null;
            accountManager = null;
            chatManager = null;
            transferManager = null;
        }
        xmppstatus = MXPPStatus.XDispConnect;
    }


    /*******************************************************************
     * 后台网络异常情况下自动连接服务器、登录
     *******************************************************************/
    /**
     * 发送请求连接服务器消息
     */
    public void requestConnectServer() {
        Message msg = new Message();
        msg.what = CustomConst.XMPP_HANDLER_WHAT.XMPP_HANDLER_ERROR.ordinal();
        msg.arg1 = CustomConst.XMPP_HANDLER_ERROR.XMPP_HANDLER_REQUEST_CONNECTSERVER.ordinal();

        XLog.i(tag, "requestConnectServer : 发送请求连接服务器消息!");
        connMessageHandler.sendMessage(msg);
    }

    //后台请求连接服务器线程
    private ConnectThread connectThread = null;

    /**
     * 连接线程
     */
    public class ConnectThread extends Thread {
        int timer = 0;

        @Override
        public void run() {
            super.run();
            try {
                while (xmppstatus == MXPPStatus.XDispConnect) {
                    this.currentThread().sleep(6000);
                    timer++;
                    System.out.print(timer);
                    requestConnectServer();
                }
                XLog.i(tag, "ConnectThread : 停止发送请求连接服务器消息!");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


/*******************************************************************
 *							 处理添加好友消息
 *******************************************************************/

/*******************************************************************
 *添加好友的程序流程如下 :
 *A                                    S                                 B
 *searchUsers(检索用户/搜索用户)     ---->   
 *                    返回搜索结果   <----
 *subscribeByUid(订阅/添加好友)    -------->             -------------->     
 *                             <--------              <--------------     subscribedByUid(同意A订阅，并反订阅A) 
 *                             <--------              <--------------     addFriendByUid(添加A为好友)
 *
 *******************************************************************/

    /***
     * wyq
     * 监听Presence消息，判断添加好友响应(同意、拒绝)
     *
     * @return
     */
    private PacketListener createPacketListener() {

        //条件过滤器
        //packet监听器  
        PacketListener listener = new PacketListener() {

            @Override
            public void processPacket(Stanza stanza) throws NotConnectedException {
                // TODO Auto-generated method stub

                //System.out.println("PresenceService-"+stanza.toXML());
                List<Handler> handlers = LcUserManager.instance.getHandlers(MainChatActivity.class.getSimpleName());
                if (handlers == null)
                    return;
                Handler handler = handlers.get(0);

                if (stanza instanceof Presence) {
                    Presence presence = (Presence) stanza;
                    String from = presence.getFrom();//发送方
                    String to = presence.getTo();//接收方
                    if (presence.getType().equals(Presence.Type.subscribe)) {
                        refreshPreceneMessage(handler, CustomConst.XMPP_HANDLER_PRESENCE.HANDLER_PRESENCE_SUBSCRIBE, from);
/*                      
                        //发送广播传递发送方的JIDfrom及字符串
                        acceptAdd = "收到添加请求！";  
                        Intent intent = new Intent();  
                        intent.putExtra("fromName", from);  
                        intent.putExtra("acceptAdd", acceptAdd);  
                        intent.setAction("com.example.eric_jqm_chat.AddFriendActivity");  
                        sendBroadcast(intent);   
*/
                    } else if (presence.getType().equals(
                            Presence.Type.subscribed)) {
                        refreshPreceneMessage(handler, CustomConst.XMPP_HANDLER_PRESENCE.HANDLER_PRESENCE_SUBSCRIBED, from);

/*                    	 
                        //发送广播传递response字符串
                        response = "恭喜，对方同意添加好友！";  
                        Intent intent = new Intent();  
                        intent.putExtra("response", response);  
                        intent.setAction("com.example.eric_jqm_chat.AddFriendActivity");  
                        sendBroadcast(intent);   
*/
                    } else if (presence.getType().equals(
                            Presence.Type.unsubscribe)) {
                        refreshPreceneMessage(handler, CustomConst.XMPP_HANDLER_PRESENCE.HANDLER_PRESENCE_UNSUBSCRIBE, from);

/*                    	
                        //发送广播传递response字符串
                        response = "抱歉，对方拒绝添加好友，将你从好友列表移除！";  
                        Intent intent = new Intent();  
                        intent.putExtra("response", response);  
                        intent.setAction("com.example.eric_jqm_chat.AddFriendActivity");  
                        sendBroadcast(intent);   
*/
                    } else if (presence.getType().equals(
                            Presence.Type.unsubscribed)) {
                        refreshPreceneMessage(handler, CustomConst.XMPP_HANDLER_PRESENCE.HANDLER_PRESENCE_UNSUBSCRIBED, from);

                    } else if (presence.getType().equals(
                            Presence.Type.unavailable)) {
                        refreshPreceneMessage(handler, CustomConst.XMPP_HANDLER_PRESENCE.HANDLER_PRESENCE_UNAVAILABLE, from);

                    } else if (presence.getType().equals(
                            Presence.Type.available)) {
                        refreshPreceneMessage(handler, CustomConst.XMPP_HANDLER_PRESENCE.HANDLER_PRESENCE_AVAILABLE, from);
                    }
                }

            }
        };
        return listener;
    }

    private void refreshPreceneMessage(Handler handler, CustomConst.XMPP_HANDLER_PRESENCE xmpPresence, String from) {
        android.os.Message osMsg = new android.os.Message();

        osMsg.what = CustomConst.XMPP_HANDLER_WHAT.XMPP_HANDLER_PRESENCE.ordinal();
        osMsg.arg1 = xmpPresence.ordinal();
        osMsg.obj = from;

        handler.sendMessage(osMsg);
    }


/*******************************************************************
 * 							设置用户状态
 *******************************************************************/
    /**
     * 设置发送状态
     *
     * @param code
     */
    public void setPresence(int code) {

        if (connection == null) {
            return;
        }
        Presence presence = null;
        switch (code) {
            //设置在线
            case CustomConst.USER_STATE_ONLINE:
                presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.available);
                break;

            //Q我吧
            case CustomConst.USER_STATE_Q_ME:
                presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.chat);
                break;

            //忙碌
            case CustomConst.USER_STATE_BUSY:
                presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.dnd);
                break;

            //离开
            case CustomConst.USER_STATE_AWAY:
                presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.away);
                break;

            //
            case CustomConst.USER_STATE_SETSELFOFFLINE:
/*				
                Roster roster = connection.getRoster();
				
				Collection<RosterEntry> entries = roster.getEntries();
				
				for(RosterEntry entry : entries){
					
					presence = new Presence(Presence.Type.unavailable);
					
					presence.setPacketID(Packet.ID_NOT_AVAILABLE);
					
					presence.setFrom(entry.getUser());
					
					presence.setTo(entry.getUser());
					
					connection.sendPacket(presence);
				}
				// 向同一用户的其他客户端发送隐身状态  
				presence = new Presence(Presence.Type.unavailable);
				
				presence.setPacketID(Packet.ID_NOT_AVAILABLE);
				
				presence.setFrom(connection.getUser());
				
				presence.setTo(StringUtils.parseBareAddress(connection.getUser()));
				
*/
                break;

            case CustomConst.USER_STATE_OFFLINE:
                presence = new Presence(Presence.Type.unavailable);
                break;
        }

        try {
            connection.sendStanza(presence);
        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 开始会话监听
     */
    public void startChatLinstener() {
        if (chatManager.getChatListeners().isEmpty()) {
            chatManagerListener = new MReceiveChatListener();
            chatManager.addChatListener(chatManagerListener);
            XLog.e(tag, "run startChatLinstener");
        }
    }

    /**
     * 停止会话监听
     */
    public void stopChatListener() {
        chatManager.removeChatListener(chatManagerListener);
        XLog.e(tag, "run stopChatListener");
    }

    /**
     * 发送文件
     *
     * @param file    文件路径
     * @param handler 文件发送实时情况反馈
     */
    public synchronized void sendFile(final long mills, final long rowid, String type, String file, final Handler handler, final String toUserId) {

        final MessageDAO messageDAO = (MessageDAO) LcUserManager.instance.dabatases.get(CustomConst.DAO_MESSAGE);

        Presence presence = Roster.getInstanceFor(connection).getPresence(toUserId);
        String usrFrom = presence.getFrom();

        XLog.e(tag, "发送文件 来自 : " + usrFrom + ", 发到 : " + toUserId + ", 文件 : " + file);
        final OutgoingFileTransfer transfer = transferManager.createOutgoingFileTransfer(usrFrom);
        //String usrFrom = connection.getUser();
        //final OutgoingFileTransfer transfer = transferManager.createOutgoingFileTransfer(toUserId);

        try {
            transfer.sendFile(new File(file), type);
            new Thread() {

                public void run() {
                    while (!transfer.isDone()) {
//                        while (!transfer.isDone()) {
//                        }
                        if (rowid >= 0) {
                            messageDAO.updateStateByRowid(rowid, hostUid, 1);
                            Message msg = handler.obtainMessage(CustomConst.HANDLER_MSG_FILE_SUCCESS, mills);
                            handler.sendMessage(msg);
                        }
                        MFileTranListener.handRefreshSession(toUserId);
                    }

                }

                ;

            }.start();

        } catch (SmackException e) {
            e.printStackTrace();
        }
    }
/*******************************************************************
 * 							后台连接服务器任务
 *******************************************************************/
    /**
     * 异步初始化连接服务器任务
     * handler的作用是，遵循谁调用连接服务器，谁处理消息
     *
     * @author
     */
    public class InitXmppConnectionTask extends AsyncTask<String, Void, Boolean> {

        private String callfrom;
        private Handler handler;

        public InitXmppConnectionTask(Handler handler, String callfrom) {

            this.handler = handler;
            this.callfrom = callfrom;
            if (this.handler == null)
                this.handler = connMessageHandler;

        }

        @Override
        protected Boolean doInBackground(String... params) {
            Boolean bRest = false;
            XLog.e(tag, callfrom + " Call InitXmppConnectionTask start, xmppstatus : " + xmppstatus.toString());

            if (xmppstatus != MXPPStatus.XDispConnect)
                return null;
            bRest = startConnectServer();
            if (handler == null) {
                XLog.e(tag, callfrom + " InitXmppConnectionTask handler null, startConnectServer over xmppstatus : " + xmppstatus.toString());
                return bRest;
            }

            if (bRest == false) {
                XLog.e(tag, callfrom + " InitXmppConnectionTask connect false, xmppstatus : " + xmppstatus.toString());
                Message msg = new Message();
                msg.what = CustomConst.XMPP_HANDLER_WHAT.XMPP_HANDLER_ERROR.ordinal();
                msg.arg1 = CustomConst.XMPP_HANDLER_ERROR.XMPP_ERROR_CONNETERROR.ordinal();
                handler.sendMessage(msg);

                return false;
            }

            connection = getInstance().getConnection();
            if (connection != null
                    && accountManager != null
                    && chatManager != null
                    && handler != null) {

                XLog.e(tag, callfrom + " InitXmppConnectionTask connect succeed, xmppstatus : " + xmppstatus.toString());

                Message msg = new Message();
                msg.what = CustomConst.XMPP_HANDLER_WHAT.XMPP_HANDLER_ERROR.ordinal();
                msg.arg1 = CustomConst.XMPP_HANDLER_ERROR.XMPP_ERROR_COONNETSUCCESSED.ordinal();
                handler.sendMessage(msg);

                return true;
            }

            return true;
        }
    }

    /**
     * 处理连接服务器和登录服务器反馈的消息；
     *
     * @author
     */
    Handler connMessageHandler = new Handler() {
        private Context context = LorentChatApplication.getInstance().getApplicationContext();

        public void handleMessage(android.os.Message msg) {

            XMPP_HANDLER_WHAT what = XMPP_HANDLER_WHAT.values()[msg.what];
            if (what == null) {
                XLog.i(tag, "connHandler.handleMessage : " + msg.what + " : " + " what null");
                return;
            }

            if (what == XMPP_HANDLER_WHAT.XMPP_HANDLER_ERROR) {
                processHanderErrorMessage(msg);
            } else if (what == XMPP_HANDLER_WHAT.XMPP_HANDLER_CONNECTION_CALLBACK) {
                processConnectionCallBackMessage(msg);
            }
        }

        /**
         * 处理连接服务器和用户登录反馈的消息；
         *
         * @author
         *
         */
        private void processHanderErrorMessage(android.os.Message msg) {
            CustomConst.XMPP_HANDLER_ERROR handlerError = CustomConst.XMPP_HANDLER_ERROR.values()[msg.arg1];
            if (handlerError == null) {
                XLog.i(tag, "processHanderErrorMessage : " + msg.arg1 + " : " + " handlerError null");
                return;
            }

            XLog.i(tag, "connHandler.handleMessage : " + handlerError.toString() + " : " + (String) msg.obj);

            if (handlerError == XMPP_HANDLER_ERROR.XMPP_HANDLER_REQUEST_CONNECTSERVER) {
                //启动异步任务，重连服务器
                try {
                    new InitXmppConnectionTask(null, tag).execute().get();
                } catch (InterruptedException | ExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (handlerError == XMPP_HANDLER_ERROR.XMPP_ERROR_COONNETSUCCESSED) {
                if (connectThread != null) {
                    XLog.i(tag, "on XMPP_ERROR_COONNETSUCCESSED, 连接服务器成功，暂停发送请求连接请求!");
                    connectThread.interrupt();
                    //connectThread.destroy();
                    connectThread = null;
                }
            }
        }

        /**
         * 处理XMPPConnection回调反馈的消息；
         *
         * @author
         *
         */
        private void processConnectionCallBackMessage(android.os.Message msg) {
            CustomConst.XMPP_CONNECTION_CALLBACK xmppStatus = CustomConst.XMPP_CONNECTION_CALLBACK.values()[msg.arg1];

            if (xmppStatus == null) {
                XLog.i(tag, "processConnectionCallBackMessage : " + msg.arg1 + " : " + " xmppStatus null");
                return;
            }
//            ToastUtils.createCenterNormalToast(context, (String) msg.obj, Toast.LENGTH_SHORT);
            XLog.i(tag, "connHandler.handleMessage : " + xmppStatus.toString() + " : " + (String) msg.obj);
            switch (xmppStatus) {
                case XMPP_CONNECTION_CLOSED:
                    break;
                case XMPP_CONNECTION_CLOSED_ONERROR:
                    //重新连接服务器;
                    xmppstatus = MXPPStatus.XDispConnect;
                    if (connectThread == null) {
                        connectThread = new ConnectThread();
                        connectThread.start();
                    }
                    break;
                case XMPP_CONNECTION_CLOSED_SOMEWHERE_LOGIN:
                    //其他地方登陆了
                    MXmppConnManager.getInstance().feCloseConnection();
                    break;

                case XMPP_CONNECTION_RECONNECTIONFAILED:

                    break;
                case XMPP_CONNECTION_RECONNECTIONSUCCESSFUL:

                    break;
                case XMPP_CONNECTION_RECONNECTINGIN:

                    break;
                case XMPP_CONNECTION_AUTHENTICATED:

                    break;
                case XMPP_CONNECTION_CONNECTED:
                    if (connectThread != null) {
                        XLog.i(tag, "on XMPP_CONNECTION_CONNECTED, 连接服务器成功，暂停发送请求连接请求!");

                        connectThread.interrupt();
                        ///connectThread.destroy();
                        connectThread = null;

                        new LoginAndRegisterHelper().bgXmppLogin();
                    }
                    break;
                default:
                    XLog.i(tag, xmppStatus.toString() + " : " + "know xmpp connection status!");
                    break;
            }
        }
    };

}