package com.lorent.chat.smack.connection;

import com.lorent.chat.smack.roomiq.RMIQInfo;
import com.lorent.chat.smack.roomiq.RMIQInfo.RmRequestType;
import com.lorent.chat.smack.roomiq.RMReplyIQ;
import com.lorent.chat.smack.roomiq.RMRequestIQ;
import com.lorent.chat.smack.roomiq.RoomJoined;
import com.lorent.chat.ui.entity.ChatRoom;
import com.lorent.chat.ui.entity.GroupChatService;
import com.lorent.chat.utils.XLog;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MXmppRoomManager {
    private static final String tag = "MXmppRoomManager";
    private static MXmppRoomManager instance;

    MXmppRoomManager() {
        instance = this;
    }

    public static MXmppRoomManager getInstance() {
        return instance;
    }

    public static MXmppRoomManager getInstance(XMPPConnection connection) {

        if (instance == null) {
            instance = new MXmppRoomManager();
        }

        instance.setupPacketListenser(connection);
        return instance;
    }


    private boolean ruleOutRooms(String roomJid) {
        //serach.	//搜索夫妇
        //pubsub.	//广播服务
        //proxy.	//代理服务
        final String[] ruleOuts = {"search", "pubsub", "proxy"};
        //String []ruleOuts = null;
        int length = ruleOuts.length;
        for (int i = 0; i < length; i++) {
            if (roomJid.equals(ruleOuts[i])) {
                //XLog.e(tag, "非讨论室 : " + roomJid);
                return true;
            }
        }
        return false;
    }

    /**
     * 初始化会议室列表
     * HostRooms 是会议服务器
     * 如:
     * room1@conference.ubuntu		:	room1是conference服务会议上的一个聊天室
     * room2@conference.ubuntu
     * room1@wanke.ubuntu			:	room1是wanke服务会议上的一个聊天室
     * room2@wanke.ubuntu
     * <p>
     * 按需求可以设计为
     * XXX@wanke.ubuntu , 开设一个命名为wanke的服务会议, 在服务会议上再开设多个聊天室；
     *
     * @throws NotConnectedException
     * @throws XMPPErrorException
     * @throws NoResponseException
     */
    public List<GroupChatService> getHostRooms() {
        XMPPConnection connection = null;
        MultiUserChatManager mucManager = null;

        connection = MXmppConnManager.getInstance().getConnection();
        if (connection == null)
            return null;

        mucManager = MultiUserChatManager.getInstanceFor(connection);

        List<HostedRoom> hostroomsI = null, hostroomsJ = null;
        List<GroupChatService> gcsList = new ArrayList<GroupChatService>();

        try {
            //会议服务器
            hostroomsI = mucManager.getHostedRooms(connection.getServiceName());
            XLog.i(tag,
                    "getHostedRooms -------------------- : " + connection.getServiceName() + "\r\n");

            for (int i = 0; i < hostroomsI.size(); i++) {
                //会议服务的基本信息
                String roomJid = hostroomsI.get(i).getJid().split("\\." + connection.getServiceName())[0];
/*                XLog.i(tag,  
                          "会议服务 JID -------------------- : " + hostroomsI.get(i).getJid() + "\r\n");
*/
                if (!ruleOutRooms(roomJid)) {
/*                    XLog.i(tag,  
                            "会议服务  -------------------- : " + hostroomsI.get(i).getName() + "\r\n");
*/
                    XLog.i(tag,
                            "getHostedRooms -------------------- : " + hostroomsI.get(i).getJid() + "\r\n");
                    hostroomsJ = mucManager.getHostedRooms(hostroomsI.get(i).getJid());
                    //会议服务下的聊天室
                    List<ChatRoom> listChatRooms = new ArrayList<ChatRoom>();
                    for (int j = 0; j < hostroomsJ.size(); j++) {
                        RoomInfo roomInfo = null;
                        roomInfo = mucManager.getRoomInfo(hostroomsJ.get(j).getJid());
                        XLog.i(tag,
                                "房间名字 : " + hostroomsJ.get(j).getName() + "\r\n"
                                        + "聊天室的JID: " + hostroomsJ.get(j).getJid() + "\r\n"
                                        + "聊天室的描述: " + roomInfo.getDescription() + "\r\n"
                                        + "聊天室的主题: " + roomInfo.getSubject() + "\r\n"
                                        + "聊天室的人数: " + roomInfo.getOccupantsCount() + "\r\n"
                        );

                        ChatRoom croom = new ChatRoom(hostroomsJ.get(j).getJid(),
                                hostroomsJ.get(j).getName(),
                                roomInfo.getDescription(),
                                roomInfo.getSubject(),
                                roomInfo.getOccupantsCount());
                        listChatRooms.add(croom);
                    }

                    GroupChatService gcs = new GroupChatService(roomJid, hostroomsI.get(i).getName(), listChatRooms);
                    gcsList.add(gcs);

                }
            }
        } catch (XMPPException | NoResponseException | NotConnectedException e) {
            e.printStackTrace();
        }
        XLog.i(tag, "会议服务器数量:" + gcsList.size());

        return gcsList;
    }

    public List<String> getJoinedRooms() {
        XMPPConnection connection = null;
        Set<String> setJoined = null;
        List<String> roomJoined = new ArrayList<String>();

        connection = MXmppConnManager.getInstance().getConnection();
        if (connection == null) {
            XLog.e(tag, "xmppConnManager.getConnection() null!");
            return null;
        }

        setJoined = MultiUserChatManager.getInstanceFor(connection).getJoinedRooms();
        XLog.i(tag, "getJoinedRooms setJoined size : " + setJoined.size());
        for (String str : setJoined) {
            XLog.i(tag, "getJoinedRooms joined : " + str);
            roomJoined.add(str);
        }

        return roomJoined;
    }

    /**
     * 创建房间
     *
     * @param roomName 房间名称
     */
    public MultiUserChat createRoom(String nickName, String roomName, String gcsName,
                                    String password) {
        XMPPConnection connection = null;

        connection = MXmppConnManager.getInstance().getConnection();

        if (connection == null) {
            XLog.e(tag, "xmppConnManager.getConnection() null!");
            return null;
        }

        if (!connection.isConnected()) {
            throw new NullPointerException("服务器连接失败，请先连接服务器");
        }

        MultiUserChat muc = null;
        try {
            // 创建一个MultiUserChat
            muc = MultiUserChatManager.getInstanceFor(connection).getMultiUserChat(roomName + "@" + gcsName + "." + connection.getServiceName());
            // 创建聊天室
            boolean isCreated = muc.createOrJoin(nickName);
            if (isCreated) {
                // 获得聊天室的配置表单
                Form form = muc.getConfigurationForm();
                // 根据原始表单创建一个要提交的新表单。
                Form submitForm = form.createAnswerForm();
                // 向要提交的表单添加默认答复
                List<FormField> fields = form.getFields();
                for (int i = 0; fields != null && i < fields.size(); i++) {
                    if (FormField.Type.hidden != fields.get(i).getType() && fields.get(i).getVariable() != null) {
                        // 设置默认值作为答复
                        submitForm.setDefaultAnswer(fields.get(i).getVariable());
                    }
                }
                // 设置聊天室的新拥有者
                List<String> owners = new ArrayList<String>();
                owners.add(connection.getUser());// 用户JID
                submitForm.setAnswer("muc#roomconfig_roomowners", owners);
                // 设置聊天室是持久聊天室，即将要被保存下来
                submitForm.setAnswer("muc#roomconfig_persistentroom", true);
                // 房间仅对成员开放
                submitForm.setAnswer("muc#roomconfig_membersonly", false);
                // 允许占有者邀请其他人
                submitForm.setAnswer("muc#roomconfig_allowinvites", true);
                if (password != null && password.length() != 0) {
                    // 进入是否需要密码
                    submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
                    // 设置进入密码
                    submitForm.setAnswer("muc#roomconfig_roomsecret", password);
                }
                // 能够发现占有者真实 JID 的角色
                // submitForm.setAnswer("muc#roomconfig_whois", "anyone");
                // 登录房间对话
                submitForm.setAnswer("muc#roomconfig_enablelogging", true);
                // 仅允许注册的昵称登录
                submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
                // 允许使用者修改昵称
                submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
                // 允许用户注册房间
                submitForm.setAnswer("x-muc#roomconfig_registration", false);
                // 发送已完成的表单（有默认值）到服务器来配置聊天室
                muc.sendConfigurationForm(submitForm);
            }
        } catch (XMPPException | SmackException e) {
            e.printStackTrace();
            return null;
        }
        return muc;

    }

    public MultiUserChat sendMemberIQMUCRoom(String userName, String roomFullJID,
                                             String password) {

        if (userName == null || userName.isEmpty())
            return null;
        if (roomFullJID == null || roomFullJID.isEmpty())
            return null;

        XLog.i(tag, "sendJoinIQMultiUserChat : " + roomFullJID);
        XMPPConnection connection = null;

        connection = MXmppConnManager.getInstance().getConnection();

        if (connection == null) {
            XLog.e(tag, "xmppConnManager.getConnection() null!");
            return null;
        }

        if (!connection.isConnected()) {
            throw new NullPointerException("服务器连接失败，请先连接服务器");
        }

        RMRequestIQ joinIQ = new RMRequestIQ(RMIQInfo.ELEMENT_NAME, RMIQInfo.ELEMENT_NAMESPACE, RmRequestType.UpdateAffiliation);

        joinIQ.setType(IQ.Type.set);
        joinIQ.setUserJid(userName + "@" + connection.getServiceName());
        joinIQ.setRoomJid(roomFullJID);
        joinIQ.setAffiliation(RMIQInfo.RmAffiliation.member.toString());
        joinIQ.setFrom(userName + "@" + connection.getServiceName());

        joinIQ.setTo(RMIQInfo.RMPluginSubdomain + "." + connection.getServiceName());

        XLog.i(tag, "joinIQ : " + joinIQ.toXML());

        try {
            connection.sendPacket(joinIQ);
            //IQRestTest();
        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /*
     * 退群
     */
    public MultiUserChat sendQuitIQMUCRoom(String userName, String roomFullJID) {
        if (userName == null || userName.isEmpty())
            return null;
        if (roomFullJID == null || roomFullJID.isEmpty())
            return null;

        XLog.i(tag, "sendQuitIQMUCRoom : " + roomFullJID);
        XMPPConnection connection = null;

        connection = MXmppConnManager.getInstance().getConnection();

        if (connection == null) {
            XLog.e(tag, "xmppConnManager.getConnection() null!");
            return null;
        }

        if (!connection.isConnected()) {
            throw new NullPointerException("服务器连接失败，请先连接服务器");
        }

        RMRequestIQ joinIQ = new RMRequestIQ(RMIQInfo.ELEMENT_NAME, RMIQInfo.ELEMENT_NAMESPACE, RmRequestType.UpdateAffiliation);

        joinIQ.setType(IQ.Type.set);
        joinIQ.setUserJid(userName + "@" + connection.getServiceName());
        joinIQ.setRoomJid(roomFullJID);
        joinIQ.setAffiliation(RMIQInfo.RmAffiliation.quit.toString());
        joinIQ.setFrom(userName + "@" + connection.getServiceName());

        joinIQ.setTo(RMIQInfo.RMPluginSubdomain + "." + connection.getServiceName());

        XLog.i(tag, "quitIQ : " + joinIQ.toXML());

        try {
            connection.sendPacket(joinIQ);
            //IQRestTest();
        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /*
     * 异步请求，交由listener处理
     */
    public void sendRoomJoinedIQ(String userName, String roomFullJID) {
        if (userName == null || userName.isEmpty())
            return;

        XLog.i(tag, "sendRoomJoinedIQ : " + roomFullJID + ", userName : " + userName);
        XMPPConnection connection = null;

        connection = MXmppConnManager.getInstance().getConnection();

        if (connection == null) {
            XLog.e(tag, "xmppConnManager.getConnection() null!");
            return;
        }

        if (!connection.isConnected()) {
            throw new NullPointerException("服务器连接失败，请先连接服务器");
        }

        RMRequestIQ requestIQ = new RMRequestIQ(RMIQInfo.ELEMENT_NAME, RMIQInfo.ELEMENT_NAMESPACE, RmRequestType.GetRoomJoined);

        requestIQ.setType(IQ.Type.get);
        requestIQ.setUserJid(userName + "@" + connection.getServiceName());
        requestIQ.setTo(RMIQInfo.RMPluginSubdomain + "." + connection.getServiceName());

        XLog.i(tag, "getType : " + requestIQ.getType());
        XLog.i(tag, "getTo : " + requestIQ.getTo());
        XLog.i(tag, "getFrom : " + requestIQ.getFrom());
        XLog.i(tag, "getUserJid : " + requestIQ.getUserJid());
        XLog.i(tag, "getRoomJid : " + requestIQ.getRoomJid());
        XLog.i(tag, "getAffiliation : " + requestIQ.getAffiliation());

        XLog.i(tag, "sendRoomJoinedIQ : " + requestIQ.toXML());

        /////ok succeed 
        try {
            connection.sendPacket(requestIQ);
            //IQRestTest();
        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<RoomJoined> getMultiChatRoomJoined(String userName, String roomFullJID) {
        List<RoomJoined> list = new ArrayList<RoomJoined>();
        List<RoomJoined> rest = sendRoomJoinedIQAndGetRest(userName, roomFullJID);
        if (rest != null && rest.size() > 0) {
            for (int i = 0; i < rest.size(); i++) {
                list.add(new RoomJoined(rest.get(i)));
            }
        }
        return list;
    }

    /*
     * 同步请求，并返回请求结果
     */
    private List<RoomJoined> sendRoomJoinedIQAndGetRest(String userName, String roomFullJID) {

        if (userName == null || userName.isEmpty())
            return null;

        XLog.i(tag, "sendRoomJoinedIQ : " + roomFullJID + ", userName : " + userName);
        XMPPConnection connection = null;

        connection = MXmppConnManager.getInstance().getConnection();

        if (connection == null) {
            XLog.e(tag, "xmppConnManager.getConnection() null!");
            return null;
        }

        if (!connection.isConnected()) {
            throw new NullPointerException("服务器连接失败，请先连接服务器");
        }

        RMRequestIQ requestIQ = new RMRequestIQ(RMIQInfo.ELEMENT_NAME, RMIQInfo.ELEMENT_NAMESPACE, RmRequestType.GetRoomJoined);

        requestIQ.setType(IQ.Type.get);
        requestIQ.setUserJid(userName + "@" + connection.getServiceName());
        requestIQ.setTo(RMIQInfo.RMPluginSubdomain + "." + connection.getServiceName());

        XLog.i(tag, "getType : " + requestIQ.getType());
        XLog.i(tag, "getTo : " + requestIQ.getTo());
        XLog.i(tag, "getUserJid : " + requestIQ.getUserJid());
        XLog.i(tag, "getRoomJid : " + requestIQ.getRoomJid());
        XLog.i(tag, "getAffiliation : " + requestIQ.getAffiliation());

        XLog.i(tag, "sendRoomJoinedIQ : " + requestIQ.toXML());


        //
        RMReplyIQ result = null;

        try {
            result = connection.createPacketCollectorAndSend(requestIQ).nextResultOrThrow();

            if (result == null) {
                XLog.i(tag, "connection.createPacketCollectorAndSend return null : ");
            } else
                XLog.i(tag, "connection.createPacketCollectorAndSend return not null : ");

        } catch (NoResponseException | XMPPErrorException | NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (result == null)
            return null;

        XLog.e(tag, "processPacket getRequestType : " +
                "\t\t" + result.getRequestType() + "\r\n");

        if (result.getRequestType() == RmRequestType.UpdateAffiliation) {

        }

        if (result.getRequestType() == RmRequestType.GetRoomJoined) {
            List<RoomJoined> roomJoined = result.getRoomJoined();
            for (int i = 0; roomJoined != null && i < roomJoined.size(); i++) {
                RoomJoined room = roomJoined.get(i);
                XLog.e(tag, "processPacket GetRoomJoined : "
                        + "room : " + room.getRoomJid() + ", affiliation : " + room.getAffiliation() + "\r\n");

            }
            return result.getRoomJoined();
        }


        return null;
    }

    /**
     * 加入会议室
     *
     * @param nickName    昵称
     * @param password    会议室密码
     * @param roomFullJID 完整的聊天室JID如 : room@conference.ubuntu
     */
    public MultiUserChat joinMultiUserChat(String nickName, String roomFullJID,
                                           String password) {
        XMPPConnection connection = MXmppConnManager.getInstance().getConnection();

        if (connection == null) {
            XLog.e(tag, "xmppConnManager.getConnection() null!");
            return null;
        }

        if (!connection.isConnected()) {
            throw new NullPointerException("服务器连接失败，请先连接服务器");
        }

        try {
            // 使用XMPPConnection创建一个MultiUserChat窗口  

            MultiUserChat muc = MultiUserChatManager.getInstanceFor(connection).getMultiUserChat(roomFullJID);

            // 聊天室服务将会决定要接受的历史记录数量
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxChars(0);
            // history.setSince(new Date());  
            // 用户加入聊天室  
            try {
                muc.join(nickName);
            } catch (NoResponseException | NotConnectedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //muc.join(user, password, history,  
            //        SmackConfiguration.getPacketReplyTimeout());  
            XLog.i(tag, "会议室【" + roomFullJID + "】加入成功........");

            findMulitUser(muc);

            return muc;
        } catch (XMPPException e) {
            e.printStackTrace();
            XLog.i(tag, "会议室【" + roomFullJID + "】加入失败........");
            return null;
        }

    }

    /**
     * 加入聊天室
     *
     * @param user      昵称
     * @param password  会议室密码
     * @param roomsName 会议室名
     */
    public MultiUserChat joinMultiUserChat(String nickName, String roomName, String gcsName,
                                           String password) {

        XMPPConnection connection = null;

        connection = MXmppConnManager.getInstance().getConnection();

        if (connection == null) {
            XLog.e(tag, "xmppConnManager.getConnection() null!");
            return null;
        }

        if (!connection.isConnected()) {
            throw new NullPointerException("服务器连接失败，请先连接服务器");
        }

        try {
            // 使用XMPPConnection创建一个MultiUserChat窗口  

            MultiUserChat muc = MultiUserChatManager.getInstanceFor(connection).getMultiUserChat(roomName + "@" + gcsName + "." + connection.getServiceName());

            // 聊天室服务将会决定要接受的历史记录数量
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxChars(0);
            // history.setSince(new Date());  
            // 用户加入聊天室  
            try {
                muc.join(nickName);
            } catch (NoResponseException | NotConnectedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //muc.join(user, password, history,  
            //        SmackConfiguration.getPacketReplyTimeout());  
            XLog.i(tag, "会议室【" + roomName + "】加入成功........");

            findMulitUser(muc);

            return muc;
        } catch (XMPPException e) {
            e.printStackTrace();
            XLog.i(tag, "会议室【" + roomName + "】加入失败........");
            return null;
        }
    }


    /**
     * 退出群聊聊天室
     *
     * @param roomName 聊天室名字
     * @param nickName 用户在聊天室中的昵称
     * @param password 聊天室密码
     * @return
     */
    public void leaveChatRoom(List<String> roomJoined) {
        XMPPConnection connection = null;

        if (roomJoined == null)
            return;

        connection = MXmppConnManager.getInstance().getConnection();

        if (connection == null) {
            XLog.e(tag, "xmppConnManager.getConnection() null!");
            return;
        }

        if (!connection.isConnected()) {
            throw new NullPointerException("服务器连接失败，请先连接服务器");
        }

        for (int i = 0; i < roomJoined.size(); i++) {
            MultiUserChat muc = MultiUserChatManager.getInstanceFor(connection).getMultiUserChat(roomJoined.get(i));
            XLog.e(tag, "leaveChatRoom : " + roomJoined.get(i) + ", " + muc.isJoined());
            try {
                muc.leave();
            } catch (NotConnectedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 查询会议室成员名字
     *
     * @param muc
     */
    public List<String> findMulitUser(MultiUserChat muc) {

        XMPPConnection connection = null;

        connection = MXmppConnManager.getInstance().getConnection();

        if (connection == null) {
            XLog.e(tag, "xmppConnManager.getConnection() null!");
            return null;
        }

        //List<String> listUser = new ArrayList<String>();  
        List<String> listUser = muc.getOccupants();
        // 遍历出聊天室人员名称  
        XLog.i(tag, "muc : " + muc.getRoom());

        for (int i = 0; i < listUser.size(); i++) {
            XLog.i(tag, "name : " + listUser.get(i));
        }
        return listUser;
    }


    private void setupPacketListenser(XMPPConnection connection) {

        XLog.e(tag, "setupPacketListenser 当异步处理消息时，需要添加packetListener");

        //当异步处理消息时，需要添加packetListener
        //connection.addPacketListener((new RMReplyIQListener(connection)), new RMReplyIQFilter()) ; //connection is an instance of the XMPPConnection.

        XLog.i(tag, "addIQProvider : RMIQProvider ");
        ProviderManager.addIQProvider(RMIQInfo.ELEMENT_NAME, RMIQInfo.ELEMENT_NAMESPACE,
                new com.lorent.chat.smack.roomiq.RMIQProvider());

/*		try {
            xmppConnManager.getConnection().getFeature(RMIQInfo.ELEMENT_NAME, RMIQInfo.ELEMENT_NAMESPACE);
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
    }
}
