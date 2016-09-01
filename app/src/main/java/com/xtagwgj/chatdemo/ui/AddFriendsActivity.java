package com.xtagwgj.chatdemo.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.ui.base.BaseView;
import com.lorent.chat.ui.base.MvpActivity;
import com.lorent.chat.ui.contract.AddFriendContract;
import com.lorent.chat.ui.contract.AddFriendPresenter;
import com.lorent.chat.ui.entity.User;
import com.lorent.chat.ui.view.CircleImageView;
import com.lorent.chat.utils.ToastUtils;
import com.lorent.chat.utils.XLog;
import com.xtagwgj.chatdemo.DividerItemDecoration;
import com.xtagwgj.chatdemo.R;

import java.util.ArrayList;
import java.util.List;

public class AddFriendsActivity extends MvpActivity<AddFriendPresenter> implements AddFriendContract.View {

    private List<User> lists = new ArrayList<>();
    private AddFriendAdapter addFriendAdapter;
    private String tag = AddFriendsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("添加好友");
    }

    @Override
    public void initView() {

        final EditText keyEditText = (EditText) findViewById(R.id.keyEditText);
        findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.searchUser(keyEditText.getText().toString().trim());
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        addFriendAdapter = new AddFriendAdapter();
        recyclerView.setAdapter(addFriendAdapter);
    }

    @Override
    public BaseView getBaseView() {
        return this;
    }

    @Override
    public int getResLayout() {
        return R.layout.activity_add_friends;
    }

    @Override
    public void showSearchResult(List<User> searchResults) {
        lists.clear();
        lists.addAll(searchResults);
        addFriendAdapter.notifyDataSetChanged();
    }

    @Override
    public void addFriendSuccess(String name) {
        showToast("add friend " + name + " success");
    }

    @Override
    public void addFriendFail(String name) {
        showToast("add friend " + name + " fail");
    }

    @Override
    public void showToast(String msg) {
        ToastUtils.createCenterNormalToast(this, msg, Toast.LENGTH_SHORT);
    }

    class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.ViewHolder> {

        @Override
        public AddFriendAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(com.lorent.chat.R.layout.fragment_user_item, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AddFriendAdapter.ViewHolder holder, int position) {
            final User user = lists.get(position);
            holder.tv_nick.setText(user.getNickName());
            holder.tv_name.setText(user.getUserName());

            holder.rl_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String username = user.getUserName();
                    final String nickname = user.getNickName();
                    Log.i("AddFriendAdapter", "onItemClick : " + username);
                    if (username.equals(LcUserManager.instance.getUserName())) {
                        ToastUtils.createNormalToast(getApplicationContext(), "不能添加自己为好友", Toast.LENGTH_SHORT);
                        return;
                    }
                    //此处是判断是否重复邀请了某人，当然这个判断很简单，也是不科学的，至于怎么判断此人是否被你邀请过，大家自己可以想办法
                    //还有一点是，已经成为好友了，再去点邀请，这个也是需要判断的
//                    if (sendInviteUser.equals(username)) {
//                        ToastUtils.createNormalToast(getApplicationContext(), "你已经邀请过" + username + "了", Toast.LENGTH_SHORT);
//                        return;
//                    }
                    showDialog(username, nickname);
                }
            });

        }

        @Override
        public int getItemCount() {
            return lists.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public CircleImageView iv;
            public TextView tv_nick;
            public TextView tv_name;
            public TextView tv_tips;
            public TextView tv_content;
            public TextView tv_time;
            public RelativeLayout rl_content;

            public ViewHolder(View itemView) {
                super(itemView);
                iv = (CircleImageView) itemView.findViewById(com.lorent.chat.R.id.user_head);
                tv_nick = (TextView) itemView.findViewById(com.lorent.chat.R.id.nick_name);
                tv_name = (TextView) itemView.findViewById(com.lorent.chat.R.id.user_name);
                tv_tips = (TextView) itemView.findViewById(com.lorent.chat.R.id.tips);
                tv_content = (TextView) itemView.findViewById(com.lorent.chat.R.id.content);
                tv_time = (TextView) itemView.findViewById(com.lorent.chat.R.id.tv_time);
                rl_content = (RelativeLayout) itemView.findViewById(com.lorent.chat.R.id.rl_content);
            }
        }
    }

    void showDialog(final String toUser, final String nickName) {

        new AlertDialog.Builder(AddFriendsActivity.this)
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
}