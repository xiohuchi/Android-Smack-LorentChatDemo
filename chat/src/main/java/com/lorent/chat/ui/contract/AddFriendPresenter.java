package com.lorent.chat.ui.contract;

import android.os.Handler;

import com.lorent.chat.smack.FriendsHelper;
import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.ui.base.MvpPresenter;


/**
 * 添加好友
 * Created by xtagwgj on 16/8/13.
 */
public class AddFriendPresenter extends MvpPresenter<AddFriendContract.View> implements AddFriendContract.Presenter {
    private String tag = AddFriendPresenter.class.getSimpleName();
    private Handler mHandler = new Handler();
    private FriendsHelper helper = new FriendsHelper();

    /**
     * @param searchName 用户名
     */
    @Override
    public void searchUser(final String searchName) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                getIView().showSearchResult(helper.searchUsers(searchName));

//                List<User> listUser = new ArrayList<User>();
//                try {
//                    XMPPTCPConnection connection = MXmppConnManager.getInstance().getConnection();
//                    UserSearchManager search = new UserSearchManager(connection);
//                    //此处一定要加上 search.
//                    Form searchForm = search.getSearchForm("search." + connection.getServiceName());
//                    Form answerForm = searchForm.createAnswerForm();
//                    answerForm.setAnswer("Username", true);        //账号
//                    //answerForm.setAnswer("Name", true);			//昵称
//                    answerForm.setAnswer("search", searchName);
//                    ReportedData data = search.getSearchResults(answerForm, "search." + connection.getServiceName());
//                    List<ReportedData.Row> it = data.getRows();
//                    ReportedData.Row row = null;
//                    XLog.i(tag, "search size : " + it.size());
//                    for (int i = 0; i < it.size(); i++) {
//                        row = it.get(i);
//                        XLog.i(tag, "searchUsers Username '" + searchName + "'Username : " + row.getValues("Username").get(0)
//                                + "  , Name : " + row.getValues("Name").get(0));
//
//                        User user = new User();
//                        user.setUserName(row.getValues("Username").get(0));    //只有账号，没有域
//                        user.setNickName(row.getValues("Name").get(0));
//                        user.setEmail(row.getValues("Email").get(0));
//                        listUser.add(user);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    XLog.e(tag + " searchUser:" + e);
//                }
//                getIView().showSearchResult(listUser);
            }
        });

    }

    @Override
    public void requestAddFriends(final String friendName) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                helper.subscribeByUid(friendName);

//                String userName = friendName;
//                XMPPTCPConnection connection = MXmppConnManager.getInstance().getConnection();
//                XLog.e(tag, "通过Uid添加好友 userName : " + userName);
//                if (connection == null || !connection.isConnected()) {
//                    return;
//                }
//                //wyq
//                //发送订阅
//                Presence subscription = new Presence(Presence.Type.subscribe);
//                userName += "@" + connection.getServiceName();
//                subscription.setTo(userName);
//
//                XLog.e(tag, "通过Uid添加好友 subscription.setTo : " + subscription.getTo());
//
//                try {
//                    connection.sendPacket(subscription);
//                } catch (SmackException.NotConnectedException e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                    XLog.e(tag + " requestAddFriends: " + e1);
//                }

                getIView().showToast("邀请已发送");
            }
        });


    }

    /**
     * 对方同意添加好友消息处理
     *
     * @param userName  用户账号
     * @param nickName  用户昵称
     * @param groupName 所属组名
     */
    @Override
    public void addFriendByUid(final String userName, final String nickName, final String groupName) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Boolean flag = helper.addFriendByUid(userName, nickName, groupName);

//                XLog.e(tag, "对方同意添加好友消息处理 userName : " + userName);
//                XMPPTCPConnection connection = MXmppConnManager.getInstance().getConnection();
//                if (connection == null || !connection.isConnected()) {
//                    getIView().addFriendFail("连接服务器失败");
//                    XLog.e(tag + " 对方同意添加好友消息处理 connection is null");
//                }
//
//                Roster roster = Roster.getInstanceFor(connection);
//
//                try {
//                    roster.createEntry(userName, nickName, new String[]{groupName});
//                    getIView().addFriendSuccess(nickName);
//                    XLog.i(tag + " 对方同意添加好友消息处理 成功");
//                } catch (SmackException.NotLoggedInException | SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                    getIView().addFriendFail(nickName);
//                    XLog.i(tag + " 对方同意添加好友消息处理 失败:" + e);
//                }


                if (flag)
                    getIView().addFriendSuccess(nickName);
                else
                    getIView().addFriendFail(nickName);
            }
        });

    }

    /**
     * 对方同意添加好友消息处理
     *
     * @param userName  用户账号
     * @param groupName 所属组名
     */
    @Override
    public void addFriendByUid(String userName, String groupName) {
        addFriendByUid(userName, userName, groupName);
    }
}
