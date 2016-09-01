package com.lorent.chat.ui.contract;

import com.lorent.chat.ui.base.BasePresenter;
import com.lorent.chat.ui.base.BaseView;
import com.lorent.chat.ui.entity.GroupChatService;

import java.util.List;

/**
 * 添加讨论组
 * Created by zy on 2016/8/12.
 */
public class AddChatRoomContract {
    public interface View extends BaseView{
        /**
         * 显示所有的会议室列表
         * @param groupChats
         */
        void displayHostRooms(List<GroupChatService> groupChats);

        /**
         * 没有会议服务器及开设的聊天室
         */
        void noGroupService();
    }


    public interface Presenter extends BasePresenter{

        /**
         * 获取用户加入的聊天室
         * @param userName 用户名
         * @param userName
         */
        void getChatRoom(String userName);

        /**
         * 加入房间
         * @param userName 当前用户名
         * @param roomFullJID 房间jid
         */
        void joinChatRoom(String userName, String roomFullJID);

        /**
         * 退出聊天室
         * @param userName 当前用户名
         * @param roomFullJID 聊天室的jid
         */
        void exitRoom(String userName, String roomFullJID);
    }
}