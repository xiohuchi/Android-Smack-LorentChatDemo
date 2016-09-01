package com.lorent.chat.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.lorent.chat.R;
import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.ui.activitys.SingleChatActivity;
import com.lorent.chat.ui.adapter.FriendListAdapter;
import com.lorent.chat.ui.entity.NearByPeople;
import com.lorent.chat.ui.fragments.FriendsSliderView.OnTouchLetterChangedListener;
import com.lorent.chat.utils.XLog;

/**
 * 好友的fragment
 */
@SuppressLint("ValidFragment")
public class FriendsFragment extends Fragment implements OnTouchLetterChangedListener, OnItemClickListener {
    private FriendListAdapter adapter;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private ListView lv_friends;
    private TextView tv_overlay;
    private OverLayThread overLayThread = new OverLayThread();

    private String tag = FriendsFragment.class.getSimpleName();

    public FriendsFragment() {
    }

    public FriendsFragment(FriendListAdapter adapter, int sectionNumber) {
        this.adapter = adapter;
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(bundle);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LcUserManager.instance.putHandler(tag, getHandler());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_friends_list_layout, null);
        lv_friends = (ListView) view.findViewById(R.id.lv_friends);
        FriendsSliderView sliderView = (FriendsSliderView) view.findViewById(R.id.fsv_sidebar);
        tv_overlay = (TextView) view.findViewById(R.id.tv_letter);
        lv_friends.setTextFilterEnabled(true);
        tv_overlay.setVisibility(View.INVISIBLE);
        lv_friends.setAdapter(adapter);
        lv_friends.setOnItemClickListener(this);
        sliderView.setOnTouchLetterChangedListener(this);

        return view;
    }

    public Handler getHandler() {
        return handler;
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                int type = msg.arg1;

                String uid = (String) msg.obj;
                for (NearByPeople people : adapter.getFriends()) {
                    if (people.getUid().equals(uid)) {
                        people.setState(type);
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }

    };

    private class OverLayThread implements Runnable {
        @Override
        public void run() {
            tv_overlay.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTouchLetterChanged(String letter) {
        tv_overlay.setText(letter);
        tv_overlay.setVisibility(View.VISIBLE);
        handler.removeCallbacks(overLayThread);
        handler.postDelayed(overLayThread, 1000);
        if (alphaIndexer(letter) > 0) {
            int position = alphaIndexer(letter);
            lv_friends.setSelection(position);
        }
    }

    public int alphaIndexer(String s) {
        int position = 0;
        for (int i = 0; i < adapter.getFriends().size(); i++) {
            if (adapter.getFriends().get(i).getPy().startsWith(s)) {
                position = i;
                break;
            }
        }
        return position;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View convertView, int position, long mills) {
        NearByPeople userInfo = adapter.getFriends().get(position);

        XLog.d(tag, "onItemClick: " + userInfo);

        Bundle bundle = new Bundle();
        bundle.putSerializable("user", userInfo);
        Intent intent = new Intent(getActivity(), SingleChatActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LcUserManager.instance.removeHandler(tag, getHandler());
    }
}
