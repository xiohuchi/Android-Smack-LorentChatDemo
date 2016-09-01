package com.lorent.chat.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;

import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.smack.connection.XmppFriendManager;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.ui.adapter.FaceGridViewAdapter;
import com.lorent.chat.ui.db.DBHelper;
import com.lorent.chat.ui.db.dao.BaseDAO;
import com.lorent.chat.ui.db.dao.impl.ChattingPeopleDaoImpl;
import com.lorent.chat.ui.db.dao.impl.MessageDaoImpl;
import com.lorent.chat.ui.db.dao.impl.NearByPeopleDaoImpl;
import com.lorent.chat.ui.db.daoService.ChatPeopleService;
import com.lorent.chat.ui.entity.ChattingPeople;
import com.lorent.chat.ui.entity.NearByPeople;
import com.lorent.chat.utils.XLog;
import com.lorent.chat.utils.XMPPServer;

import org.jivesoftware.smack.XMPPConnection;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * LorentChat用户的管理类
 * Created by zy on 2016/8/15.
 */
public enum LcUserManager {
    instance;
    public static final String tag = LcUserManager.class.getSimpleName();
    private Context mContext;

    public Map<String, SoftReference<Bitmap>> mPhotoOriginalCache = new HashMap<>();
    public Map<String, SoftReference<Bitmap>> mSendbarCache = new HashMap<>();
    public Map<String, SoftReference<Drawable>> showDrawable = new HashMap<>();

    // 连接管理者
    //public static XMPPTCPConnection xmppConnection;

    //用户名
    public Map<String, String> friendsNames = new HashMap<>();

    //所有会话，以对话用户JID命名
    public Map<String, Object> mJIDChats = new TreeMap<>();
    public Map<String, Object> mJIDMutilChats = new TreeMap<>();

    private String userName;
    private String userPwd;

    public void setUpUserInfo(String userName, String userPwd) {
        this.userName = userName;
        this.userPwd = userPwd;
    }

    public void init(Context context, XMPPServer xmppServer) {
        this.mContext = context;
        initDataBase();
        initEmotions();
        dbHelper = new DBHelper(mContext);
        ServerConfig.instance.setServer(xmppServer);
        //连接服务器
        MXmppConnManager.getInstance().requestConnectServer();
    }

    public String getUserName() {
        return this.userName;
    }

    public String getUserPwd() {
        return this.userPwd;
    }

    public String getFullUserName() {
        XLog.d(tag, "getFullUserName: " + MXmppConnManager.getInstance().getConnection().getUser());
        return MXmppConnManager.getInstance().getConnection().getUser();
    }

    //用户聊天界面当前滚动到的信息在数据库中的页数,String-uid;Integer-页码
    public Map<String, Integer> mUsrChatMsgPage = new HashMap<>();

    /***************************************************************************
     * 数据库
     **************************************************************************/
    // 数据库连接
    public Map<String, BaseDAO> dabatases;

    public DBHelper dbHelper;

    private void initDataBase() {
        dabatases = new HashMap<>();
        dabatases.put(CustomConst.DAO_MESSAGE, new MessageDaoImpl(mContext));
        dabatases.put(CustomConst.DAO_NEARBY, new NearByPeopleDaoImpl(mContext));
        dabatases.put(CustomConst.DAO_CHATTING, new ChattingPeopleDaoImpl(mContext));
    }

    /***************************************************************************
     * 表情
     **************************************************************************/
    //表情名
    public List<String> mEmotions_Zme = new ArrayList<>();
    //备用表情名
    public List<String> mEmotions = new ArrayList<>();
    //表情名与其对应的图片ID
    public Map<String, Integer> mEmotions_Id = new HashMap<>();

    // 初始化表情
    private void initEmotions() {
        for (int i = 1; i < 66; i++) {
            String emotionName = "[zme" + i + "]";
            //int emotionId = getResources().getIdentifier("zme" + i, "drawable", getPackageName());
            mEmotions.add(emotionName);
            mEmotions_Zme.add(emotionName);
            mEmotions_Id.put(emotionName, FaceGridViewAdapter.faces[i - 1]);
        }
    }

    /*************************************************************************
     * Handler
     ************************************************************************/
    private Map<String, List<Handler>> handlers = new HashMap<>();

    public List<Handler> getHandlers(String uid) {
        XLog.i(tag, "====>GETUID====Handler : " + uid);
        return handlers.get(uid);
    }

    public void removeHandler(String key, Handler handler) {
        handlers.get(key).remove(handler);
        XLog.i(tag, "====>Remove====Handler : " + key);
    }

    public void putHandler(String key, Handler handler) {
        XLog.i(tag, "====>Add====Handler : " + key);
        if (handlers.get(key) == null) {
            handlers.put(key, new ArrayList<Handler>());
        }
        handlers.get(key).add(handler);
    }

    /*************************************************************************
     * 聊天
     ************************************************************************/
    private ChatPeopleService chatPeopleService;
    //
    private List<ChattingPeople> chattingPeoples;

    public ChatPeopleService getChatPeopleService() {
        if (chatPeopleService == null)
            chatPeopleService = new ChatPeopleService();

        return chatPeopleService;
    }

    public List<ChattingPeople> getChattingPeople(List<String> uids) {
        XMPPConnection connection = MXmppConnManager.getInstance().getConnection();

        if (chatPeopleService == null)
            getChatPeopleService();

        if (connection != null)
            chattingPeoples = chatPeopleService.findAll(uids, connection.getUser());

        return chattingPeoples;
    }

    public List<ChattingPeople> getChattingPeople() {
        return chattingPeoples;
    }

    public List<NearByPeople> getFriends() {
        return XmppFriendManager.getInstance().getFriends();
    }
}
