package com.lorent.chat.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.ui.activitys.SingleChatActivity;
import com.lorent.chat.ui.adapter.ChattingMsgAdapter;
import com.lorent.chat.ui.db.dao.ChattingPeopleDAO;
import com.lorent.chat.ui.db.daoService.ChatPeopleService;
import com.lorent.chat.ui.entity.ChattingPeople;
import com.lorent.chat.ui.entity.NearByPeople;
import com.lorent.chat.utils.XLog;

/**
 * 消息fragment
 */
@SuppressLint("ValidFragment")
public class MsgFragment extends ListFragment {

    public static final String tag = MsgFragment.class.getSimpleName();

    private ChattingMsgAdapter adapter;
    private ChatPeopleService chatPeopleService;
    private ChattingPeopleDAO cPeopleDAO;
    private String hostUid = LcUserManager.instance.getHostUid();

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            //取得这个消息表明在聊天窗口，更新聊天记录
            if (msg.what == CustomConst.HANDLER_CHATPEOPLE_LIST_UPDATE) {
                String uid = (String) msg.obj;
                refreshSession(uid);
                adapter.notifyDataSetChanged();

                //取得这个消息表明原先没有这个会话，在消息列添加
            } else if (msg.what == CustomConst.HANDLER_CHATPEOPLE_LIST_ADD) {
                String uid = (String) msg.obj;
                //将该会话如果要打开聊天窗口，要获取的聊天记录页数
                LcUserManager.instance.mUsrChatMsgPage.put(uid, 1);
                cPeopleDAO.save(uid, hostUid);
                adapter.getChattingPeoples().add(chatPeopleService.findByUid(uid, hostUid));
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(adapter);
        LcUserManager.instance.putHandler(tag, handler);
        XLog.d(tag + " onCreate");
    }

    public MsgFragment(ChattingMsgAdapter adapter) {
        XLog.d(tag + " MsgFragment");
        cPeopleDAO = (ChattingPeopleDAO) LcUserManager.instance.dabatases.get(CustomConst.DAO_CHATTING);
        chatPeopleService = new ChatPeopleService();
        this.adapter = adapter;
    }

    public void onResume() {
        XLog.d(tag + " onResume" + adapter);
        super.onResume();
        if (adapter != null) {
            //获取Activity对象 更新本个Fragment的列表，因为在Activity的onResume方法中从新获取了一遍聊天列表
            adapter.getChattingPeoples().clear();
            adapter.getChattingPeoples().addAll(LcUserManager.instance.getChattingPeople());
            adapter.notifyDataSetChanged();
        }
    }

    public void refreshSession(String uid) {
        XLog.d(tag, "refreshSession :" + uid);
        for (ChattingPeople people : adapter.getChattingPeoples()) {
            if (people.getPeople().equals(uid)) {
                adapter.getChattingPeoples().remove(people);
                people = chatPeopleService.findByUid(uid, hostUid);
                adapter.getChattingPeoples().add(0, people);
                break;
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ChattingPeople people = adapter.getChattingPeoples().get(position);
        NearByPeople userInfo = new NearByPeople(people.getPeople(), people.getLastMsg().getAvatar(), 0, 0, "", 0, 0, 0, 0, 0, 0, people.getLastMsg().getName(), 0, 0, 0, 0, people.getLastMsg().getDistance(), "", "", 0);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", userInfo);
        Intent intent = new Intent(getActivity(), SingleChatActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        super.onListItemClick(l, v, position, id);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LcUserManager.instance.removeHandler(tag, handler);
    }
}
