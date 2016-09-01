package com.lorent.chat.ui.db.daoService;

import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.ui.db.dao.MessageDAO;
import com.lorent.chat.ui.entity.ChattingPeople;
import com.lorent.chat.ui.entity.CommonMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天的服务
 */
public class ChatPeopleService {

    private MessageDAO messageDAO;

    public ChatPeopleService() {
        messageDAO = (MessageDAO) LcUserManager.instance.dabatases.get(CustomConst.DAO_MESSAGE);
    }

    /**
     * 获取消息列表的成员
     *
     * @param uids 成员用户名
     * @return 聊天的好友
     */
    public List<ChattingPeople> findAll(List<String> uids, String hostUid) {
        List<ChattingPeople> cPeoples = new ArrayList<>();
        for (String uid : uids) {
            ChattingPeople people = findByUid(uid, hostUid);
            if (people != null) {
                cPeoples.add(people);
            }
        }
        return cPeoples;
    }

    public ChattingPeople findByUid(String uid, String hostUid) {
        CommonMessage message = messageDAO.findLastMesgByUid(uid, hostUid);
        long count = messageDAO.findReceiveButNotReadByUid(uid, hostUid);
        ChattingPeople chattingPeople = null;
        if (message != null) {
            chattingPeople = new ChattingPeople(uid, message, count);
        }
        return chattingPeople;
    }

}
