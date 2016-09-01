package com.lorent.chat.smack.connection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.common.LorentChatApplication;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.ui.entity.NearByPeople;
import com.lorent.chat.utils.FormatTools;
import com.lorent.chat.utils.TypeConverter;
import com.lorent.chat.utils.XLog;
import com.lorent.chat.utils.cache.CacheUtils;
import com.lorent.chat.utils.cache.ImageCache;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class XmppFriendManager {

    private static final String tag = XmppFriendManager.class.getSimpleName();

    XMPPConnection connection = MXmppConnManager.getInstance().getConnection();

    Roster roster;
    VCard vCard;

    ImageCache imageCache;

    static XmppFriendManager xmppFriendManager;

    private XmppFriendManager() {
        xmppFriendManager = this;
    }

    public static XmppFriendManager getInstance() {
        if (xmppFriendManager == null) {
            xmppFriendManager = new XmppFriendManager();
        }
        return xmppFriendManager;
    }

    /**
     * 获取好友列表
     *
     * @return
     * @throws FileNotFoundException
     * @throws XMPPException
     */
    public List<NearByPeople> getFriends() {
        XLog.i(tag, "getFriends: 获取好友列表");
        List<NearByPeople> friends = new ArrayList<>();
//        connection = MXmppConnManager.getInstance().getConnection();
        if (connection != null) {
            friends = getFriendOnHasNetWork(friends);
        }
        return friends;
    }

    /**
     * 获取群组
     *
     * @param roster
     * @return
     */
    public List<RosterGroup> getGroups(Roster roster) {

        List<RosterGroup> groups = new ArrayList<RosterGroup>();
        Collection<RosterGroup> rosterGroups = roster.getGroups();
        Iterator<RosterGroup> ite = rosterGroups.iterator();
        while (ite.hasNext()) {
            groups.add(ite.next());
        }

        return groups;
    }

    /**
     * 获取某个群组的用户
     *
     * @param roster    角色
     * @param groupName 群组名
     * @return
     */
    public List<RosterEntry> getEntiesByGroup(Roster roster, String groupName) {
        List<RosterEntry> enties = new ArrayList<>();
        RosterGroup group = roster.getGroup(groupName);
        Collection<RosterEntry> ent = group.getEntries();
        Iterator<RosterEntry> ite = ent.iterator();
        while (ite.hasNext()) {
            enties.add(ite.next());
        }
        return enties;
    }

    /**
     * 获取用户信息
     *
     * @param friends
     * @return
     */
    public List<NearByPeople> getFriendOnHasNetWork(List<NearByPeople> friends) {
        XLog.i(tag, "getFriendOnHasNetWork: 获取用户信息");
        roster = Roster.getInstanceFor(connection);
        roster.addRosterListener(new MRosterListener());

        List<RosterGroup> groups = getGroups(roster);
        NearByPeople friend;
        for (RosterGroup group : groups) {
            List<RosterEntry> entries = getEntiesByGroup(roster, group.getName());
            for (RosterEntry entry : entries) {

                String avatar;
                try {
                    avatar = getUserIconAvatar(entry.getUser());
                    Log.e(tag, "getFriendOnHasNetWork avatar : " + avatar);
                } catch (XMPPException | FileNotFoundException e) {
                    e.printStackTrace();
                    return friends;
                }

                Log.i(tag, "getFriendOnHasNetWork getUser : " + entry.getUser());
/*				Log.e(tag, "getFriendOnHasNetWork getName : " + entry.getName());
                Log.e(tag, "getFriendOnHasNetWork getNickName : " + vCard.getNickName());
*/
                LcUserManager.instance.friendsNames.put(entry.getUser(), TypeConverter.nullStringDefaultValue(vCard.getNickName(), entry.getName()));

                friend = new NearByPeople(entry.getUser(),
                        avatar,
                        0,
                        0,
                        "",
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        TypeConverter.nullStringDefaultValue(vCard.getNickName(), entry.getUser()),
                        0,
                        0,
                        0,
                        25,
                        "0.12km",
                        "",
                        TypeConverter.nullStringDefaultValue(entry.getStatus() == null ? null : entry.getStatus().toString(), "正在YY一个NB的签名..."),
                        roster.getPresence(entry.getUser()).isAvailable() ? 0 : 1
                );
                friends.add(friend);
            }

        }
        return friends;
    }

    /**
     * 获取用户信息类
     *
     * @param user 用户UID
     * @return
     */
    public VCard getVCard(String user) {
        VCardManager.getInstanceFor(connection);
        vCard = new VCard();
        ProviderManager.addIQProvider("vCard", "vcard-temp",
                new org.jivesoftware.smackx.vcardtemp.provider.VCardProvider());

        try {
            vCard.load(connection, user);
        } catch (NoResponseException | XMPPErrorException | NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return vCard;
    }

    /**
     * 获取指定好友ID的头像，保存到缓存目录
     *
     * @param user
     * @return
     * @throws XMPPException
     * @throws FileNotFoundException
     */
    public String getUserIconAvatar(String user) throws XMPPException, FileNotFoundException {
        vCard = getVCard(user);
        if (vCard == null || vCard.getAvatar() == null) {
            return null;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(vCard.getAvatar());
        String baseDir = CacheUtils.getImagePath(LorentChatApplication.getInstance(), CustomConst.IMAGE_CACHE_PATH);
        String fileName = TypeConverter.getUUID() + ".jpg";
        String path = baseDir + "/" + fileName;
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(path)));

        TypeConverter.writeFile(bos, bis);
        Log.i(tag, "getUserIconAvatar user : " + user + "\r\npath : " + path);
        return fileName;
    }

    //by wyq, 以后放在map里
    public Drawable getUserImage(String user) {
//		Log.i(tag, "getUserImage user : " + user);
        ByteArrayInputStream bais = null;
        try {
            vCard = getVCard(user);
            if (vCard == null || vCard.getAvatar() == null) {
                return null;
            }
            bais = new ByteArrayInputStream(vCard.getAvatar());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (bais == null)
            return null;
        return FormatTools.getInstance().InputStream2Drawable(bais);
    }

    private String myUserHeadFileName() {
        String baseDir = CacheUtils.getImagePath(LorentChatApplication.getInstance(), CustomConst.USERHEAD_PATH);
        String fileName = baseDir + "/" + CustomConst.USERHEAD_FILENAME;

        File tmp = new File(fileName);
        if (tmp.exists()) {
            return fileName;
        } else
            return null;
    }

    //by wyq, 以后放在map里
    public Bitmap getMyHeadImage() {
        String fileName = myUserHeadFileName();
        if (fileName != null && !fileName.isEmpty()) {
            Bitmap bm = BitmapFactory.decodeFile(fileName);
            return bm;
        }
        return null;
    }

    public class MRosterListener implements RosterListener {

        @Override
        public void presenceChanged(Presence presence) {
            String uid = presence.getFrom().split("/")[0];
            int type = 0;
            if (presence.getType().equals(Presence.Type.unavailable)) {
                //删除与该人的当前会话，防止下次再通讯时导致会话不一致
                LcUserManager.instance.mJIDChats.remove(uid);
                type = CustomConst.USER_STATE_OFFLINE;
            } else if (presence.getType().equals(Presence.Type.available)) {
                Presence.Mode mode = presence.getMode();
                if (mode.equals(Presence.Mode.chat)) {
                    type = CustomConst.USER_STATE_Q_ME;
                } else if (mode.equals(Presence.Mode.dnd)) {
                    type = CustomConst.USER_STATE_BUSY;
                } else if (mode.equals(Presence.Mode.away)) {
                    type = CustomConst.USER_STATE_AWAY;
                } else {
                    type = CustomConst.USER_STATE_ONLINE;
                }
            }

//			FriendsFragment fragment = (FriendsFragment)((MainFragmentActivity)context).getFragments()[2];
//			
//			Message message = new Message();
//			
//			message.what = 0;
//			
//			message.obj = uid;
//			
//			message.arg1 = type;
//			
//			fragment.getHandler().sendMessage(message);

        }

        @Override
        public void entriesUpdated(Collection<String> arg0) {

        }

        @Override
        public void entriesDeleted(Collection<String> arg0) {

        }

        @Override
        public void entriesAdded(Collection<String> arg0) {
        }
    }

}
