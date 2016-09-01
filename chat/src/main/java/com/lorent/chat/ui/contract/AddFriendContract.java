package com.lorent.chat.ui.contract;

import com.lorent.chat.ui.base.BasePresenter;
import com.lorent.chat.ui.base.BaseView;
import com.lorent.chat.ui.entity.User;

import java.util.List;

/**
 * 添加好友
 * Created by xtagwgj on 16/8/13.
 */
public class AddFriendContract {

    public interface View extends BaseView {
        /**
         * 搜索的用户结果
         *
         * @param searchResults 搜索结果
         */
        void showSearchResult(List<User> searchResults);

        void addFriendSuccess(String name);

        void addFriendFail(String name);

    }

    public interface Presenter extends BasePresenter {
        /**
         * 搜索用户
         *
         * @param searchName 用户名
         */
        void searchUser(String searchName);

        /**
         * 请求添加好友
         *
         * @param friendName 好友名
         */
        void requestAddFriends(String friendName);

        /**
         * 收到对方同意后，添加好友
         *
         * @param userName  用户账号
         * @param nickName  用户昵称
         * @param groupName 所属组名
         * @return
         */
        void addFriendByUid(String userName, String nickName, String groupName);

        /**
         * 收到对方同意后，添加好友
         *
         * @param userName  用户账号
         * @param groupName 所属组名
         * @return
         */
        void addFriendByUid(String userName, String groupName);

    }

}
