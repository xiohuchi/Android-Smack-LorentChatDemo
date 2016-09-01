package com.lorent.chat.ui.activitys;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.lorent.chat.R;
import com.lorent.chat.ui.adapter.AddFriendAdapter;
import com.lorent.chat.ui.base.BaseView;
import com.lorent.chat.ui.base.MvpActivity;
import com.lorent.chat.ui.contract.AddFriendContract;
import com.lorent.chat.ui.contract.AddFriendPresenter;
import com.lorent.chat.ui.entity.User;
import com.lorent.chat.utils.ToastUtils;
import com.lorent.chat.utils.XLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加好友
 *
 * @author wyq
 */
public class AddFriendActivity extends MvpActivity<AddFriendPresenter> implements AddFriendContract.View, OnClickListener {

    private static final String tag = "AddFriendActivity";
    private ImageView go_back;
    private EditText search_key;
    private Button btn_search;
    private ListView search_list;

    private String I;

    private String sendInviteUser = "";//被邀请人
    private List<User> listUser = new ArrayList<>();
    private AddFriendAdapter addFriendAdapter;
    private SharedPreferences sharedPreferences;


    @Override
    public BaseView getBaseView() {
        return this;
    }

    @Override
    public int getResLayout() {
        return R.layout.activity_friend_search;
    }

    /**
     * 初始化控件
     */
    public void initView() {

        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        I = sharedPreferences.getString("name", "");
        Log.i(tag, "I : " + I);

        go_back = (ImageView) findViewById(R.id.img_back);//返回
        search_key = (EditText) findViewById(R.id.search_key);

        search_key.setText("test");

        btn_search = (Button) findViewById(R.id.btn_search);
        //listview
        search_list = (ListView) findViewById(R.id.search_list);
        search_list.setOverScrollMode(View.OVER_SCROLL_NEVER);
        search_list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
                final String username = listUser.get(arg2).getUserName();
                final String nickname = listUser.get(arg2).getNickName();
                Log.i(tag, "onItemClick : " + username);
                if (username.equals(I)) {
                    ToastUtils.createNormalToast(getApplicationContext(), "不能添加自己为好友", Toast.LENGTH_SHORT);
                    return;
                }
                //此处是判断是否重复邀请了某人，当然这个判断很简单，也是不科学的，至于怎么判断此人是否被你邀请过，大家自己可以想办法
                //还有一点是，已经成为好友了，再去点邀请，这个也是需要判断的
                if (sendInviteUser.equals(username)) {
                    ToastUtils.createNormalToast(getApplicationContext(), "你已经邀请过" + username + "了", Toast.LENGTH_SHORT);
                    return;
                }
                showDialog(username, nickname);
            }
        });

        go_back.setOnClickListener(this);
        btn_search.setOnClickListener(this);
    }


    /**
     * 添加好友
     */
    void showDialog(final String toUser, final String nickName) {

        new AlertDialog.Builder(AddFriendActivity.this)
                .setItems(new String[]{"添加为好友"}
                        , new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                XLog.i(tag, "toUser : " + toUser);
                                XLog.i(tag, "nickName : " + nickName);

                                mPresenter.requestAddFriends(toUser);
                            }
                        })
                .create()
                .show();

    }

    @Override
    public void onClick(View arg0) {
        if(arg0.getId()==R.id.btn_search){
            String search_content = search_key.getText().toString();
            if (TextUtils.isEmpty(search_content)) {
                return;
            }
            searchByUseName(search_content);
        }else if(arg0.getId()==R.id.img_back){
            finishActivity();
        }
    }


    private void searchByUseName(String name) {
        mPresenter.searchUser(name);
    }

    @Override
    public void showSearchResult(List<User> searchResults) {
        if (searchResults != null && searchResults.size() > 0) {
            this.listUser.clear();
            this.listUser.addAll(searchResults);
            addFriendAdapter = new AddFriendAdapter(AddFriendActivity.this, listUser);
            search_list.setAdapter(addFriendAdapter);
        } else {
            showToast("未查询到信息");
        }
    }

    @Override
    public void addFriendSuccess(String name) {
        showToast("添加 : " + name + "成功!");
    }

    @Override
    public void addFriendFail(String name) {
        showToast("添加 : " + name + "失败!");
    }

    @Override
    public void showToast(String msg) {
        XLog.d(msg);
        ToastUtils.createCenterNormalToast(AddFriendActivity.this, msg, Toast.LENGTH_SHORT);
    }
}