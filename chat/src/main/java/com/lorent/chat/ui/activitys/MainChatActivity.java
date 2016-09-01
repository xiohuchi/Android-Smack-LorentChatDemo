package com.lorent.chat.ui.activitys;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.lorent.chat.R;
import com.lorent.chat.common.AppManager;
import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.smack.FriendsHelper;
import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.ui.adapter.ChattingMsgAdapter;
import com.lorent.chat.ui.adapter.FriendListAdapter;
import com.lorent.chat.ui.adapter.MainPagerAdapter;
import com.lorent.chat.ui.entity.ChattingPeople;
import com.lorent.chat.ui.entity.NearByPeople;
import com.lorent.chat.ui.fragments.FriendsFragment;
import com.lorent.chat.ui.fragments.MsgFragment;
import com.lorent.chat.utils.ToastUtils;
import com.lorent.chat.utils.XLog;

import org.jivesoftware.smack.XMPPConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天的主页面（不是聊天页）
 * 消息、好友
 */
public class MainChatActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private List<ChattingPeople> chattingPeoples;//聊天的好友

    private List<String> mTitleList = new ArrayList<>();//页卡标题集合
    private List<Fragment> fragmentList = new ArrayList<>();//fragment集合
    private FriendsHelper helper = new FriendsHelper();
    private String tag = MainChatActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);
        AppManager.instance.addActivity(this);
        LcUserManager.instance.putHandler(tag, handler);

        mViewPager = (ViewPager) findViewById(R.id.vp_view);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        ///初始化数据库
        XMPPConnection connection = MXmppConnManager.getInstance().getConnection();
        if (connection != null) {
            LcUserManager.instance.dbHelper.CreateSelfTable(connection.getUser().split("@")[0]);
        }

        //获取好友
        resumeAction();

        initView();
    }

    /**
     * 获取传过来的标题和fragment信息
     * 如果没有值或者传过来的值不正确 ，则使用默认的值
     */
    private void initView() {
        mTitleList.clear();
        fragmentList.clear();

        if (getIntent().getBooleanExtra("friends", true)) {
            mTitleList.add("好友");
            fragmentList.add(new FriendsFragment(new FriendListAdapter(this, LcUserManager.instance.getFriends()), 2));
        }

        if (getIntent().getBooleanExtra("message", true)) {
            mTitleList.add("消息");
            fragmentList.add(new MsgFragment(new ChattingMsgAdapter(this, chattingPeoples)));
        }

        setPager(mTitleList, fragmentList);
    }

    /**
     * 设置viewpager的值和要调用的fragment
     * 如果只有一个fragment,则隐藏tablayout
     *
     * @param titles    标题集合
     * @param fragments fragment的集合
     */
    private void setPager(List<String> titles, List<Fragment> fragments) {

        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        for (String title : titles) {
            mTabLayout.addTab(mTabLayout.newTab().setText(title));
        }

        MainPagerAdapter mSectionPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), titles, fragments);
        mViewPager.setAdapter(mSectionPagerAdapter);//给ViewPager设置适配器
        mTabLayout.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来。
        mTabLayout.setTabsFromPagerAdapter(mSectionPagerAdapter);//给Tabs设置适配器

        if (titles.size() == 1) {
            mTabLayout.setVisibility(View.GONE);
            setTitle(titles.get(0));
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            String from = (String) msg.obj;
            if (msg.what < 0 || msg.what >= CustomConst.XMPP_HANDLER_WHAT.XMPP_HANDLER_WHAT_MAX.ordinal()) {
                XLog.i(tag, "XMPP_HANDLER_WHAT msg.what error : " + msg.what
                        + ", [" + 0
                        + ", " + CustomConst.XMPP_HANDLER_WHAT.XMPP_HANDLER_WHAT_MAX.ordinal() + "]");
                return;
            }

            if (msg.arg1 < 0 || msg.arg1 >= CustomConst.XMPP_HANDLER_PRESENCE.XMPP_HANDLER_PRESENCE_MAX.ordinal()) {
                XLog.i(tag, "XMPP_HANDLER_PRESENCE msg.arg1 error : " + msg.arg1
                        + ", [" + 0
                        + ", " + CustomConst.XMPP_HANDLER_PRESENCE.XMPP_HANDLER_PRESENCE_MAX.ordinal() + "]");
                return;
            }

            CustomConst.XMPP_HANDLER_PRESENCE xmpPresenc = CustomConst.XMPP_HANDLER_PRESENCE.values()[msg.arg1];

            switch (xmpPresenc) {
                case HANDLER_PRESENCE_AVAILABLE:
                    XLog.i(tag, from + " 好友上线！");
                    break;
                case HANDLER_PRESENCE_UNAVAILABLE:
                    XLog.i(tag, from + " 好友下线！");
                    break;
                case HANDLER_PRESENCE_SUBSCRIBE:
                    XLog.i(tag, "来自 :　" + from + " 添加请求！");
                    onSubscribe(from);
                    break;
                case HANDLER_PRESENCE_SUBSCRIBED:
                    XLog.i(tag, "来自 :　" + from + " 同意添加请求！");
                    onSubscribed(from);
                    break;

                case HANDLER_PRESENCE_UNSUBSCRIBE:
                    XLog.i(tag, "来自 :　" + from + " 对方拒绝添加请求！");
                    break;
                case HANDLER_PRESENCE_UNSUBSCRIBED:
                    XLog.i(tag, "来自 :　" + from + " 对方的删除请求！");
                    break;
                case HANDLER_PRESENCE_ERROR:
                    XLog.i(tag, "来自 :　" + from + " 错误！");
                    break;
            }
        }
    };

    //wyq
    //请求添加好友消息处理
    private void onSubscribe(final String fromUser) {

        new MaterialDialog.Builder(MainChatActivity.this)
                .title("提示")
                .content("来自 :　" + fromUser + " 添加请求！")
                .positiveText("确认")
                .negativeText("拒绝")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        helper.subscribeByUid(fromUser);
                        helper.addFriendByUid(fromUser, fromUser, "friends");
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //wyq
    //对方同意添加好友消息处理
    private void onSubscribed(final String fromUser) {
        ToastUtils.createCenterNormalToast(MainChatActivity.this, fromUser + "同意了你的添加请求", Toast.LENGTH_SHORT);

        new Thread(new Runnable() {
            @Override
            public void run() {
                helper.addFriendByUid(fromUser, fromUser, "friends");
            }
        }).start();
    }

    @Override
    protected void onResume() {
        resumeAction();
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 获取好友的uid
     *
     * @return uid的集合
     */
    private List<String> getUids() {
        XLog.i(tag + " getUids");
        XLog.i(tag + "friends:" + LcUserManager.instance.getFriends());

        List<String> uids = new ArrayList<>();
        for (NearByPeople cByPeople : LcUserManager.instance.getFriends()) {
            XLog.i(tag + "friends:" + cByPeople);
            uids.add(cByPeople.getUid());
        }

        return uids;
    }

    /**
     * 重新获取聊天的好友
     */
    private void resumeAction() {
        XLog.i(tag, "resumeAction");
        chattingPeoples = LcUserManager.instance.getChattingPeople(getUids());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LcUserManager.instance.removeHandler(tag, handler);
    }
}