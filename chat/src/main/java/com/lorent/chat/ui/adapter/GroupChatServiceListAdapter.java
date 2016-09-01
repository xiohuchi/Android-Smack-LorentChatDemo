package com.lorent.chat.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lorent.chat.R;
import com.lorent.chat.ui.entity.GroupChatService;

import java.util.List;

/**
 * ExpandListView的适配器，继承自BaseExpandableListAdapter
 */
public class GroupChatServiceListAdapter extends BaseExpandableListAdapter implements OnClickListener {

    private Context mContext;
    //子项是一个map，key是group的id，每一个group对应一个ChildItem的list
    private List<GroupChatService> gcsLists;
    private Button groupButton;//group上的按钮

    public GroupChatServiceListAdapter(Context context, List<GroupChatService> gcsLists) {
        this.mContext = context;
        this.gcsLists = gcsLists;
    }

    /*
     *  Gets the data associated with the given child within the given group
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        //我们这里返回一下每个item的名称，以便单击item时显示
        //return childMap.get(groupPosition).get(childPosition).getTitle();
        return gcsLists.get(groupPosition).getChatRooms().get(childPosition).getRoomName();
    }

    /*
     * 取得给定分组中给定子视图的ID. 该组ID必须在组中是唯一的.必须不同于其他所有ID（分组及子项目的ID）
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /*
     *  Gets a View that displays the data for the given child within the given group
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        ChildHolder childHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.multiuserroom_item, null);
            childHolder = new ChildHolder();
            childHolder.childImg = (ImageView) convertView.findViewById(R.id.img_child);
            childHolder.childText = (TextView) convertView.findViewById(R.id.tv_child_text);
            childHolder.roomButton = (Button) convertView.findViewById(R.id.btn_child_function);
            childHolder.roomButton.setOnClickListener(this);
            convertView.setTag(childHolder);
        } else {
            childHolder = (ChildHolder) convertView.getTag();
        }
/*		
        childHolder.childImg.setBackgroundResource(childMap.get(groupPosition).get(childPosition).getMarkerImgId());
		childHolder.childText.setText(childMap.get(groupPosition).get(childPosition).getTitle());
*/
        childHolder.childImg.setBackgroundResource(R.drawable.group);
        childHolder.childText.setText(gcsLists.get(groupPosition).getChatRooms().get(childPosition).getRoomName());
        if (gcsLists.get(groupPosition).getChatRooms().get(childPosition).getJoinedFlag())
            childHolder.roomButton.setBackgroundResource(R.drawable.gochatroom);
        return convertView;
    }

    /*
     * 取得指定分组的子元素数
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        // TODO Auto-generated method stub
        return gcsLists.get(groupPosition).getChatRooms().size();
        //return childMap.get(groupPosition).size();
    }

    /**
     * 取得与给定分组关联的数据
     */
    @Override
    public Object getGroup(int groupPosition) {
        return gcsLists.get(groupPosition).getGcsName();
        //return groupTitle.get(groupPosition);
    }

    /**
     * 取得分组数
     */
    @Override
    public int getGroupCount() {
        return gcsLists.size();
        //return groupTitle.size();
    }

    /**
     * 取得指定分组的ID.该组ID必须在组中是唯一的.必须不同于其他所有ID（分组及子项目的ID）
     */
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    /*
     *Gets a View that displays the given group
     *return: the View corresponding to the group at the specified position
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder groupHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.gcsitem, null);
            groupHolder = new GroupHolder();
            groupHolder.groupImg = (ImageView) convertView.findViewById(R.id.img_indicator);
            groupHolder.groupText = (TextView) convertView.findViewById(R.id.tv_group_text);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }
        if (isExpanded) {
            groupHolder.groupImg.setBackgroundResource(R.drawable.downarrow);
        } else {
            groupHolder.groupImg.setBackgroundResource(R.drawable.nav_left);
        }
        //groupHolder.groupText.setText(groupTitle.get(groupPosition));
        groupHolder.groupText.setText(gcsLists.get(groupPosition).getGcsName());
        groupButton = (Button) convertView.findViewById(R.id.btn_group_function);
        groupButton.setOnClickListener(this);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        // Indicates whether the child and group IDs are stable across changes to the underlying data
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // Whether the child at the specified position is selectable
        return true;
    }

    /**
     * show the text on the child and group item
     */
    private class GroupHolder {
        ImageView groupImg;
        TextView groupText;
    }

    private class ChildHolder {
        ImageView childImg;
        TextView childText;
        Button roomButton;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//		case R.id.btn_group_function:
//			Log.d("GroupChatServiceListAdapter", "onClick group button");
//		case R.id.btn_child_function:
//			{
//				//Log.d("GroupChatServiceListAdapter", "onClick child button : " + );
//				//Toast.makeText(this, "加入 '" + roomName + "'讨论组!" , Toast.LENGTH_SHORT).show();
//				//gcsLists.get(groupPosition).getChatRooms().get(childPosition).getJoinedFlag();
//				//MXmppRoomManager.getInstance().joinMultiUserChat(MySelf, listGroupRoom.get(groupIndex).getChatRooms().get(childIndex).getRoomJID(), "");
//			}
//			break;
//		default:
//			break;
//		}

        }
    }
}