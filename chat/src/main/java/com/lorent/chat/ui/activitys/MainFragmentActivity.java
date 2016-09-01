package com.lorent.chat.ui.activitys;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.lorent.chat.R;
import com.lorent.chat.common.AppManager;
import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.smack.FriendsHelper;
import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.smack.constVar.CustomConst.XMPP_HANDLER_PRESENCE;
import com.lorent.chat.smack.constVar.CustomConst.XMPP_HANDLER_WHAT;
import com.lorent.chat.ui.adapter.ChattingMsgAdapter;
import com.lorent.chat.ui.adapter.FriendListAdapter;
import com.lorent.chat.ui.adapter.MainViewPagerAdapter;
import com.lorent.chat.ui.entity.ChattingPeople;
import com.lorent.chat.ui.entity.NearByPeople;
import com.lorent.chat.ui.fragments.FriendsFragment;
import com.lorent.chat.ui.fragments.MsgFragment;
import com.lorent.chat.ui.view.popwindow.ActionBarPopupWindow;
import com.lorent.chat.utils.XLog;

import org.jivesoftware.smack.XMPPConnection;

import java.util.ArrayList;
import java.util.List;


public class MainFragmentActivity extends AppCompatActivity implements ActionBar.TabListener {

    private static final String tag = "MainFragmentActivity";
    private MainViewPagerAdapter mSectionPagerAdapter;
    private ViewPager mViewPager;

    private List<ChattingPeople> chattingPeoples;
    private Fragment[] fragments;
    private FriendsHelper helper = new FriendsHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setBackgroundDrawable(new ColorDrawable(0xff47b8ff));

        ///初始化数据库
        XMPPConnection connection = MXmppConnManager.getInstance().getConnection();
        if (connection != null) {
            LcUserManager.instance.dbHelper.CreateSelfTable(connection.getUser().split("@")[0]);
        }
//        chatPeopleService = new ChatPeopleService();
        mViewPager = (ViewPager) findViewById(R.id.pager);

        //wyq
/*		List<NearFunction> nearFunctions = new ArrayList<NearFunction>();
        nearFunctions.add(new NearFunction(R.drawable.ic_discover_group, "附近用户", ""));
		nearFunctions.add(new NearFunction(R.drawable.ic_discover_feed, "附近留言板", "小菜发表了留言:[zem1]..."));
*/
        resumeAction();

        //wyq
/*		fragments = new Fragment [] {new NearFragment(new NearFunctionAdapter(this, nearFunctions)),
                                                 new MsgFragment(new ChattingMsgAdapter(this,chattingPeoples)),
											     new FriendsFragment(new FriendListAdapter(this, friends), 2)};
*/
        fragments = new Fragment[]{new MsgFragment(new ChattingMsgAdapter(this, chattingPeoples)),
                new FriendsFragment(new FriendListAdapter(this, LcUserManager.instance.getFriends()), 2)};

        mSectionPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(),
                R.array.main_tab_names,    //动态、消息、好友
                getResources(), fragments);

        initViews(actionBar);

        AppManager.instance.addActivity(this);
        LcUserManager.instance.putHandler("MainFragmentActivity", handler);
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            String from = (String) msg.obj;
            if (msg.what < 0 || msg.what >= XMPP_HANDLER_WHAT.XMPP_HANDLER_WHAT_MAX.ordinal()) {
                Log.i(tag, "XMPP_HANDLER_WHAT msg.what error : " + msg.what
                        + ", [" + 0
                        + ", " + XMPP_HANDLER_WHAT.XMPP_HANDLER_WHAT_MAX.ordinal() + "]");
                return;
            }

            if (msg.arg1 < 0 || msg.arg1 >= XMPP_HANDLER_PRESENCE.XMPP_HANDLER_PRESENCE_MAX.ordinal()) {
                Log.i(tag, "XMPP_HANDLER_PRESENCE msg.arg1 error : " + msg.arg1
                        + ", [" + 0
                        + ", " + XMPP_HANDLER_PRESENCE.XMPP_HANDLER_PRESENCE_MAX.ordinal() + "]");
                return;
            }

            XMPP_HANDLER_PRESENCE xmpPresenc = XMPP_HANDLER_PRESENCE.values()[msg.arg1];

            switch (xmpPresenc) {
                case HANDLER_PRESENCE_AVAILABLE:
                    Log.i(tag, from + " 好友上线！");
                    break;

                case HANDLER_PRESENCE_UNAVAILABLE:
                    Log.i(tag, from + " 好友下线！");
                    break;

                case HANDLER_PRESENCE_SUBSCRIBE:
                    Log.i(tag, "来自 :　" + from + " 添加请求！");
                    onSubscribe(from);

                    break;
                case HANDLER_PRESENCE_SUBSCRIBED:
                    Log.i(tag, "来自 :　" + from + " 同意添加请求！");
                    onSubscribed(from);
                    break;

                case HANDLER_PRESENCE_UNSUBSCRIBE:
                    Log.i(tag, "来自 :　" + from + " 对方拒绝添加请求！");
                    break;

                case HANDLER_PRESENCE_UNSUBSCRIBED:
                    Log.i(tag, "来自 :　" + from + " 对方的删除请求！");
                    break;

                case HANDLER_PRESENCE_ERROR:
                    Log.i(tag, "来自 :　" + from + " 错误！");
                    break;
            }
        }
    };

    //wyq
    //请求添加好友消息处理
    private void onSubscribe(final String fromUser) {
        AlertDialog.Builder builder = new Builder(MainFragmentActivity.this);

        builder.setMessage("来自 :　" + fromUser + " 添加请求！");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                helper.subscribeByUid(fromUser);
                helper.addFriendByUid(fromUser, fromUser, "friends");
                dialog.dismiss();
//				MainFragmentActivity.this.finish();
                AppManager.instance.finishActivity();
            }
        });
        builder.setNegativeButton("拒绝", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    //wyq
    //对方同意添加好友消息处理
    private void onSubscribed(final String fromUser) {
        new AlertDialog.Builder(MainFragmentActivity.this)
                .setItems(new String[]{"添加同意"}, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        arg0.dismiss();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                helper.addFriendByUid(fromUser, fromUser, "friends");
                            }
                        }).start();
                    }
                })
                .create()
                .show();
    }

    public Fragment[] getFragments() {
        return fragments;
    }

    private void initViews(final ActionBar actionBar) {
        mViewPager.setAdapter(mSectionPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mSectionPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab().setText(mSectionPagerAdapter.getPageTitle(i)).setTabListener(this));
        }
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction transaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction transaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction transaction) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem menuMore = menu.findItem(R.id.menu_more);
        menuMore.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ActionBarPopupWindow popupWindow = new ActionBarPopupWindow(getApplicationContext());
        popupWindow.showAsDropDown(this.findViewById(R.id.menu_more));
        return super.onOptionsItemSelected(item);
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
        Log.i(tag, "resumeAction");
        chattingPeoples = LcUserManager.instance.getChattingPeople(getUids());
    }
}