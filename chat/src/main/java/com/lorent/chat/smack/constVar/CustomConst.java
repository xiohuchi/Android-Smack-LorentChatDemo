package com.lorent.chat.smack.constVar;


public class CustomConst {

    public static final String BROWSER_AK_KEY = "9GuB2R52qo2d96WmZzDz3G0j";

    public static final int Handler_BaiDuConvertorTask = 2;

    public static final int Handler_MainThreadUpdateUI = 1;

    public static final int Handler_PopDateSelectWindow = 3;

    public static final boolean GeoPointType_NETWORK = true;

    public static final boolean GeoPointType_LOCAL = false;
    ///设置里的分类
    public static final int SETTIING_ITEM_TYPE_RADIO = 0;

    public static final int SETTIING_ITEM_TYPE_SWITCH = 1;

    public static final int SETTING_ITEM_LOCATION_UPDATE = 2;


    //数据连接类型
    public static String DAO_CHATTING = "ChattingPeopleDao";

    public static String DAO_MESSAGE = "MessageDao";

    public static String DAO_NEARBY = "NearByPeopleDao";

    //缓存图片
    public static final String IMAGE_CACHE_PATH = "imageCache";
    public static final String GROUP_IMAGE_CACHE_PATH = "groupImageCache";
    public static final String USERHEAD_PATH = "myhead";
    public static final String USERHEAD_FILENAME = "userhead.jpg";

    //用户状态
    public static final int USER_STATE_ONLINE = 0;
    public static final int USER_STATE_Q_ME = 1;
    public static final int USER_STATE_BUSY = 2;
    public static final int USER_STATE_AWAY = 3;
    public static final int USER_STATE_SETSELFOFFLINE = 4;
    public static final int USER_STATE_OFFLINE = 5;

    //查询聊天记录时每页最大行数
    public static final int MESSAGE_PAGESIZE = 5;

    //图片或者拍照请求参数
    /**
     * 使用选择相册图片
     */
    public static final int MEDIA_CODE_PICTURE = 1000;
    /**
     * 拍照方式
     */
    public static final int MEDIA_CODE_CAMERA = 1001;

    //发送图片返回进度Handler值
    public static final int HANDLER_MGS_ADD = 0;
    public static final int HANDLER_MSG_SENDFILE_PROCESS = 1;
    public static final int HANDLER_MSG_SENDFILE_ERROR = 2;
    public static final int HANDLER_MSG_SENDFILE_CANNELED = 3;
    public static final int HANDLER_MSG_FILE_SUCCESS = 4;
    public static final int HANDLER_MSG_RECEIVEFILE_ERROR = 5;

    public static final int HANDLER_MSG_CHATSTATES = 6;
    //信息Fragment上聊天用户列表
    /**
     * 添加
     */
    public static final int HANDLER_CHATPEOPLE_LIST_ADD = 0;
    /**
     * 更新
     */
    public static final int HANDLER_CHATPEOPLE_LIST_UPDATE = 1;

    /**
     * 发送文件类型：图片
     */
    public static final String FILETYPE_IMAGE = "IMAGE";
    /**
     * 发送文件类型：语音
     */
    public static final String FILETYPE_VOICE = "VOICE";

    //填充Mesage.what
    public enum XMPP_HANDLER_WHAT {
        XMPP_HANDLER_ERROR,                    //
        XMPP_HANDLER_PRESENCE,
        XMPP_HANDLER_CONNECTION_CALLBACK,    //对应 ReConnectionListener
        XMPP_HANDLER_WHAT_MAX
    }

	/*
	 * 填充Message.arg1 :
	 *	XMPP_HANDLER_ERROR
	 *	XMPP_HANDLER_PRESENCE
	 *	XMPP_CONNECTION_CALLBACK
	 */

    public enum XMPP_HANDLER_ERROR {
        XMPP_ERROR_COONNETSUCCESSED,
        XMPP_ERROR_CONNETERROR,
        XMPP_ERROR_LOGINSUCCESSED,
        XMPP_ERROR_LOGINFAIL,
        XMPP_HANDLER_REQUEST_CONNECTSERVER,

        XMPP_HANDLER_ERROR_MAX
    }

    //xmpp presence 状态
    public enum XMPP_HANDLER_PRESENCE {
        HANDLER_PRESENCE_AVAILABLE,            // -- (Default) indicates the user is available to receive messages.
        HANDLER_PRESENCE_UNAVAILABLE,        // -- the user is unavailable to receive messages.
        HANDLER_PRESENCE_SUBSCRIBE,            // -- request subscription to recipient's presence.
        HANDLER_PRESENCE_SUBSCRIBED,        // -- grant subscription to sender's presence.
        HANDLER_PRESENCE_UNSUBSCRIBE,        // -- request removal of subscription to sender's presence.
        HANDLER_PRESENCE_UNSUBSCRIBED,        // -- grant removal of subscription to sender's presence.
        HANDLER_PRESENCE_ERROR,
        XMPP_HANDLER_PRESENCE_MAX
    }


    public enum XMPP_CONNECTION_CALLBACK {
        XMPP_CONNECTION_CLOSED,                    //对应connectionClosed
        XMPP_CONNECTION_CLOSED_ONERROR,            //对应connectionClosedOnError
        XMPP_CONNECTION_CLOSED_SOMEWHERE_LOGIN,    //对应reconnectionFailed
        XMPP_CONNECTION_RECONNECTIONFAILED,        //对应reconnectionSuccessful
        XMPP_CONNECTION_RECONNECTIONSUCCESSFUL,    //对应reconnectingIn
        XMPP_CONNECTION_RECONNECTINGIN,            //对应authenticated
        XMPP_CONNECTION_AUTHENTICATED,            //对应connected
        XMPP_CONNECTION_CONNECTED,                //对应connectionClosed
        XMPP_CONNECTION_CALLBACK_MAX
    }
}