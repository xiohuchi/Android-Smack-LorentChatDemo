package com.lorent.chat.ui.activitys;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.lorent.chat.R;
import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.smack.connection.MXmppRoomManager;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.ui.adapter.GroupChatServiceListAdapter;
import com.lorent.chat.ui.base.BaseView;
import com.lorent.chat.ui.base.MvpActivity;
import com.lorent.chat.ui.contract.AddChatRoomContract;
import com.lorent.chat.ui.contract.AddChatRoomPresenter;
import com.lorent.chat.ui.entity.ChatRoom;
import com.lorent.chat.ui.entity.GroupChatService;
import com.lorent.chat.utils.ToastUtils;
import com.lorent.chat.utils.XLog;

import org.jivesoftware.smack.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加讨论组
 *
 * @author wyq
 */
public class AddChatRoomActivity extends MvpActivity<AddChatRoomPresenter> implements AddChatRoomContract.View, OnClickListener {

    private static final String tag = "AddChatRoomActivity";
    private ImageView go_back;
    private EditText search_key;
    private Button btn_search, btn_test;
    private ExpandableListView grouplist;

    private String search_content;

    private String MySelf;

    private String sendInviteUser = "";//被邀请人
    //private List<String> roomJoined = null;

    private List<GroupChatService> listGroupService = new ArrayList<>();    //所有会议服务器及开设的聊天室
//    private List<RoomJoined> listRoomJoined;        //用户所有加入的聊天室

    //    private AddRoomAdapter addRoomAdapter;
    private GroupChatServiceListAdapter gcsAdapter;
    private SharedPreferences sharedPreferences;

//    private MultiUserChat myMuc = null;

    final int CONTEXT_MENU_GROUP_ADD = 0;//添加上下文菜单时每一个菜单项的item ID
    final int CONTEXT_MENU_GROUP_DETAIL = 1;
    final int CONTEXT_MENU_GRUOP_QUIT = 2;

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 1:
                    gcsAdapter = new GroupChatServiceListAdapter(AddChatRoomActivity.this, listGroupService);
                    grouplist.setAdapter(gcsAdapter);
                    break;
                case -1:
                    ToastUtils.createNormalToast(AddChatRoomActivity.this, "未查询到信息", Toast.LENGTH_LONG);
                    break;
                case 2:
                    ToastUtils.createNormalToast(AddChatRoomActivity.this, "邀请已发送", Toast.LENGTH_LONG);
                    break;
            }
        }
    };

    /**
     * 初始化控件
     */
    public void initView() {
        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        MySelf = sharedPreferences.getString("name", "");

        go_back = (ImageView) findViewById(R.id.img_back);//返回
        search_key = (EditText) findViewById(R.id.search_key);

        search_key.setText("201@wangke.yqpc");

        btn_search = (Button) findViewById(R.id.btn_search);
        btn_test = (Button) findViewById(R.id.btn_test);
        //listview
        grouplist = (ExpandableListView) findViewById(R.id.search_list);
        grouplist.setOverScrollMode(View.OVER_SCROLL_NEVER);
        grouplist.setGroupIndicator(null);//这里不显示系统默认的group indicator
        registerForContextMenu(grouplist);//给ExpandListView添加上下文菜单

        initEvents();
        getJoinedRooms();
    }

    @Override
    public BaseView getBaseView() {
        return this;
    }

    @Override
    public int getResLayout() {
        return R.layout.activity_charroom_search;
    }

    private void initEvents() {
        go_back.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        btn_test.setOnClickListener(this);

        grouplist.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                startGroupChat(groupPosition, childPosition);
                return true;
            }
        });

    }

    private void startGroupChat(int groupIndex, int childIndex) {

        if (!listGroupService.get(groupIndex).getChatRooms().get(childIndex).getJoinedFlag()) {
            Toast.makeText(this, "未加入 '" + listGroupService.get(groupIndex).getChatRooms().get(childIndex).getRoomName() + "'讨论组!", Toast.LENGTH_SHORT).show();
        } else {

            MXmppRoomManager.getInstance().joinMultiUserChat(MySelf, listGroupService.get(groupIndex).getChatRooms().get(childIndex).getRoomJID(), "");

            Bundle bundle = new Bundle();
            bundle.putString("groupJID", listGroupService.get(groupIndex).getChatRooms().get(childIndex).getRoomJID());
            bundle.putString("myJID", MySelf);

            Intent intent = new Intent(this, GroupChatActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    /*
     * 添加上下文菜单
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {

        }
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            menu.setHeaderTitle("Options");
            menu.add(1, CONTEXT_MENU_GROUP_ADD, 0, "加入");
            menu.add(1, CONTEXT_MENU_GROUP_DETAIL, 0, "详细");
            menu.add(1, CONTEXT_MENU_GRUOP_QUIT, 0, "退出");
        }
    }

    /*
     * 每个菜单项的具体点击事件
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
        // 获取当前长按项的下标
        final int groupIndex = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        final int childIndex = ExpandableListView.getPackedPositionChild(info.packedPosition);

        String roomName = listGroupService.get(groupIndex).getChatRooms().get(childIndex).getRoomName();

        switch (item.getItemId()) {
            case CONTEXT_MENU_GROUP_ADD:
                if (!listGroupService.get(groupIndex).getChatRooms().get(childIndex).getJoinedFlag()) {
                    ToastUtils.createCenterNormalToast(AddChatRoomActivity.this, "加入 '" + roomName + "'讨论组!", Toast.LENGTH_SHORT);
                    mPresenter.joinChatRoom(MySelf, listGroupService.get(groupIndex).getChatRooms().get(childIndex).getRoomJID());
                    getJoinedRooms();
                } else {
                    ToastUtils.createCenterNormalToast(AddChatRoomActivity.this, "已经加入过 '" + roomName + "'讨论组了!", Toast.LENGTH_SHORT);
                    startGroupChat(groupIndex, childIndex);
                }
                break;

            case CONTEXT_MENU_GROUP_DETAIL:
                onRoomDetail(listGroupService.get(groupIndex), listGroupService.get(groupIndex).getChatRooms().get(childIndex));
                break;

            case CONTEXT_MENU_GRUOP_QUIT:
                Toast.makeText(this, "退出 '" + roomName + "'讨论组了!", Toast.LENGTH_SHORT).show();
                mPresenter.exitRoom(MySelf, listGroupService.get(groupIndex).getChatRooms().get(childIndex).getRoomJID());
                getJoinedRooms();
                break;

            default:
                break;
        }

        return super.onContextItemSelected(item);
    }

    /**
     * 聊天室的详细信息
     *
     * @param gcs
     * @param room
     */
    private void onRoomDetail(final GroupChatService gcs, final ChatRoom room) {
        AlertDialog.Builder builder = new Builder(AddChatRoomActivity.this);
        String detail =
                "讨论组的名字     : " + room.getRoomName() + "\r\n"
                        + "讨论组的JID      : " + room.getRoomJID() + "\r\n"
                        + "聊天服务域         : " + gcs.getGcsJID() + "\r\n"
                        + "讨论组的描述     : " + room.getRoomDescription() + "\r\n"
                        + "讨论组的主题     : " + room.getRoomSubject() + "\r\n"
                        + "讨论组的人数     : " + room.getOccupantsCount();
        if (room.getJoinedFlag())
            detail += "\r\n" + "已加入";

        builder.setMessage(detail);
        builder.setTitle("<" + room.getRoomName() + ">的详细信息");
        builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onClick(View arg0) {
        search_content = search_key.getText().toString();

        if (arg0.getId() == R.id.img_back) {
            finishActivity();
        } else if (arg0.getId() == R.id.btn_search) {
            String keySearch = search_key.getText().toString().trim();

            if (StringUtils.isEmpty(keySearch)) {
                getJoinedRooms();
            } else {
                searchServiceByKey(keySearch);
            }
        } else if (arg0.getId() == R.id.btn_test) {
            XLog.e("room", "btn_test call  : test ");
            testFun();
        }

    }

    private void testFun() {
        //MXmppRoomManager.getInstance().getMultiChatRoomJoined(MySelf, "");

        long mills = System.currentTimeMillis();
        Log.e("SingleChatActivity", "handleMessage sendFile to : " + "201@wangke.yqpc");
        //

        String file = "/storage/sdcard0/com.lorent.chat/imageCache/8cd9991ba9e94cb88180a71eff803eea.jpg";

        MXmppConnManager.getInstance().sendFile(mills, -1, CustomConst.FILETYPE_IMAGE, file, null, "201@wanke.yqpc");
    }

    private void getJoinedRooms() {
        XLog.d("获取聊天室的信息");
        if (listGroupService != null)
            listGroupService.clear();

        mPresenter.getChatRoom(MySelf);
    }

    /**
     * 搜索聊天室信息
     *
     * @param key 搜索关键字
     */
    private void searchServiceByKey(final String key) {
        XLog.d("搜索聊天室信息" + key);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                List<GroupChatService> searchGropChatService = new ArrayList<GroupChatService>();
                if (listGroupService != null && listGroupService.size() > 0) {

                    for (GroupChatService groupChatService : listGroupService) {
                        if (groupChatService.getGcsName().contains(key))//输入的是服务器的名字,该服务器下的所有聊天室都显示
                        {
                            searchGropChatService.add(groupChatService);
                        } else {//不是服务器的名字,搜索服务器下的聊天室
                            List<ChatRoom> chatRooms = new ArrayList<>();
                            for (ChatRoom chatRoom : groupChatService.getChatRooms()) {
                                if (chatRoom.getSearchKey().contains(key)) {
                                    //搜索到匹配的聊天室
                                    chatRooms.add(chatRoom);
                                }
                            }
                            //该聊天服务器下存在匹配的聊天室,则加入
                            if (chatRooms.size() > 0) {
                                searchGropChatService.add(
                                        new GroupChatService(groupChatService.getGcsJID(), groupChatService.getGcsName(), chatRooms)
                                );
                            }

                        }
                    }
                    //显示聊天信息
                    displayHostRooms(searchGropChatService);
                }
            }
        });
    }

    private void leaveRoom() {
        List<String> roomJoined = new ArrayList<String>();
        roomJoined.add(search_key.getText().toString());
        MXmppRoomManager.getInstance().leaveChatRoom(roomJoined);
    }

    @Override
    public void displayHostRooms(List<GroupChatService> groupChats) {
        XLog.d("显示聊天室的信息");
        this.listGroupService.clear();
        this.listGroupService.addAll(groupChats);

        gcsAdapter = new GroupChatServiceListAdapter(AddChatRoomActivity.this, listGroupService);
        grouplist.setAdapter(gcsAdapter);
    }

    @Override
    public void noGroupService() {
        showToast("没有会议服务器及开设的聊天室");
    }

    @Override
    public void showToast(String msg) {
        XLog.d(msg);
        ToastUtils.createCenterNormalToast(AddChatRoomActivity.this, msg, Toast.LENGTH_SHORT);
    }
}
