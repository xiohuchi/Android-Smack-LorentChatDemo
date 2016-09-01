package com.lorent.chat.ui.contract;

import android.os.Handler;

import com.lorent.chat.smack.connection.MXmppRoomManager;
import com.lorent.chat.smack.roomiq.RoomJoined;
import com.lorent.chat.ui.base.MvpPresenter;
import com.lorent.chat.ui.entity.GroupChatService;
import com.lorent.chat.utils.XLog;

import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.List;

/**
 * 添加聊天室
 * Created by xtagwgj on 16/8/13.
 */
public class AddChatRoomPresenter extends MvpPresenter<AddChatRoomContract.View> implements AddChatRoomContract.Presenter {


    private java.lang.String tag = AddChatRoomPresenter.class.getSimpleName();

    /**
     * 获取所有的聊天室信息
     */
    private List<GroupChatService> getAllChatRoom() {
        return MXmppRoomManager.getInstance().getHostRooms();
    }

    /**
     * 获取用户加入的聊天室信息
     *
     * @param userName    用户名
     * @param roomFullJID 房间的id
     */
    private List<RoomJoined> getJoinChatRoom(String userName, String roomFullJID) {
        return MXmppRoomManager.getInstance().getMultiChatRoomJoined(userName, roomFullJID);
    }

    /**
     * 获取聊天室
     *
     * @param userName 当前用户名
     */
    @Override
    public void getChatRoom(final String userName) {

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                //先获得所有聊天室
                List<GroupChatService> listGroupService = getAllChatRoom();

                //再获得已加入的聊天室
                List<RoomJoined> listRoomJoined = getJoinChatRoom(userName, "");

                if (listRoomJoined != null && listRoomJoined.size() > 0 && listGroupService != null && listGroupService.size() > 0) {
                    for (int i = 0; i < listGroupService.size(); i++) {
                        for (int j = 0; j < listGroupService.get(i).getChatRooms().size(); j++) {
                            for (int z = 0; z < listRoomJoined.size(); z++) {
                                if (listRoomJoined.get(z).getRoomJid().equals(listGroupService.get(i).getChatRooms().get(j).getRoomJID())) {
                                    listGroupService.get(i).getChatRooms().get(j).setJoinedFlag(true);
                                }
                            }
                        }
                    }
                }

                if (listGroupService != null && listGroupService.size() > 0) {
                    getIView().displayHostRooms(listGroupService);
                } else {
                    getIView().noGroupService();
                }


            }
        });
    }

    /**
     * 加入聊天室内
     *
     * @param userName    当前用户名
     * @param roomFullJID 房间jid
     */
    @Override
    public void joinChatRoom(String userName, String roomFullJID) {
        XLog.d(tag, "加入聊天室内 joinChatRoom" + userName + " " + roomFullJID);
        MXmppRoomManager.getInstance().sendMemberIQMUCRoom(userName, roomFullJID, "");
//        MXmppRoomManager.getInstance().joinMultiUserChat(userName, roomFullJID, "");
//        MultiUserChat muc;

    }

    /**
     * 退出聊天室
     *
     * @param userName    当前用户名
     * @param roomFullJID 聊天室的jid
     */
    @Override
    public void exitRoom(String userName, String roomFullJID) {
        XLog.d(tag, "退出聊天室 exitRoom" + userName + " " + roomFullJID);
        MXmppRoomManager.getInstance().sendQuitIQMUCRoom(userName, roomFullJID);
    }
}
