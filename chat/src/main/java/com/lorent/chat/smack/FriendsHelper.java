package com.lorent.chat.smack;

import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.smack.connection.XmppFriendManager;
import com.lorent.chat.ui.entity.NearByPeople;
import com.lorent.chat.ui.entity.User;
import com.lorent.chat.utils.XLog;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;

import java.util.ArrayList;
import java.util.List;

/**
 * 好友的工具类
 * 搜索
 * 添加
 * Created by zy on 2016/8/31.
 */
public class FriendsHelper {
    private XMPPTCPConnection connection;
    private String tag = FriendsHelper.class.getSimpleName();

    public FriendsHelper() {
        connection = MXmppConnManager.getInstance().getConnection();
    }

    /**
     * 好友列表
     *
     * @return 好友列表
     */
    public List<NearByPeople> getFriends() {
        return XmppFriendManager.getInstance().getFriends();
    }

    /**
     * 查询用户
     *
     * @param userName 用户的关键字
     * @return 符合条件的用户
     */
    public List<User> searchUsers(String userName) {
        XLog.i(tag, "查询用户: " + userName);

        List<User> listUser = new ArrayList<>();
        try {
            UserSearchManager search = new UserSearchManager(connection);
            //此处一定要加上 search.
            Form searchForm = search.getSearchForm("search." + connection.getServiceName());
            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("Username", true);        //账号
            //answerForm.setAnswer("Name", true);			//昵称
            answerForm.setAnswer("search", userName);
            ReportedData data = search.getSearchResults(answerForm, "search." + connection.getServiceName());
            List<ReportedData.Row> it = data.getRows();

            XLog.i(tag, "search size : " + it.size());
            for (ReportedData.Row row : it) {
                XLog.i(tag, "searchUsers Username '" + userName + "'Username : " + row.getValues("Username").get(0)
                        + "  , Name : " + row.getValues("Name").get(0));

                User user = new User();
                user.setUserName(row.getValues("Username").get(0));    //只有账号，没有域
                user.setNickName(row.getValues("Name").get(0));
                user.setEmail(row.getValues("Email").get(0));
                listUser.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listUser;
    }

    /**
     * 发请求添加好友
     *
     * @param userName 用户账号
     */
    public void subscribeByUid(String userName) {
        XLog.e(tag, "发请求添加好友: " + userName);
        if (connection == null || !connection.isConnected()) {
            XLog.e(tag, "connection is null or inConnect: ");
            return;
        }
        //wyq
        //发送订阅
        Presence subscription = new Presence(Presence.Type.subscribe);
        userName += "@" + connection.getServiceName();
        subscription.setTo(userName);

        XLog.e(tag, "addFriendByUid subscription.setTo : " + subscription.getTo());

        try {
            connection.sendPacket(subscription);
        } catch (SmackException.NotConnectedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    /**
     * 收到对方同意后，添加好友
     *
     * @param userName  用户账号
     * @param nickName  用户昵称
     * @param groupName 所属组名
     * @return
     */
    public boolean addFriendByUid(String userName, String nickName, String groupName) {
        XLog.e(tag, "收到对方同意后，添加好友: " + userName);
        if (connection == null || !connection.isConnected()) {
            XLog.e(tag, "connection is null or inConnect: ");
            return false;
        }

        Roster roster = Roster.getInstanceFor(connection);

        try {
            roster.createEntry(userName, nickName, new String[]{groupName});
            return true;
        } catch (SmackException.NotLoggedInException | SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }
}