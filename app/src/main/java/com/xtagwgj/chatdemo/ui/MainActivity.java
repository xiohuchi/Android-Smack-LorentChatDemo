package com.xtagwgj.chatdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.lorent.chat.ui.activitys.AddChatRoomActivity;
import com.lorent.chat.ui.activitys.MainChatActivity;

public class MainActivity extends MainChatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("主页");
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(com.lorent.chat.R.menu.main, menu);
//        MenuItem menuMore = menu.findItem(com.lorent.chat.R.id.menu_more);
//        menuMore.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        ActionBarPopupWindow popupWindow = new ActionBarPopupWindow(getApplicationContext());
//        popupWindow.showAsDropDown(this.findViewById(com.lorent.chat.R.id.menu_more));
//        return super.onOptionsItemSelected(item);
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.NONE, 1, "添加好友");
        menu.add(Menu.NONE, Menu.NONE, 2, "添加讨论组");
        menu.add(Menu.NONE, Menu.NONE, 3, "设置");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getOrder()) {
            case 1:
                startActivity(new Intent(this, AddFriendsActivity.class));
                break;
            case 2:
                startActivity(new Intent(this, AddChatRoomActivity.class));
                break;
            case 3:
                startActivity(new Intent(this, ExitActivity.class));
                break;
        }

        return true;
    }
}