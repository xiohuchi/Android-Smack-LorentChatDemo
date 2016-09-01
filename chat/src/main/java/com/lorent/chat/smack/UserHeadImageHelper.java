package com.lorent.chat.smack;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.utils.FormatTools;
import com.lorent.chat.utils.XLog;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.lang.ref.WeakReference;

/**
 * 用户头像
 * Created by zy on 2016/8/31.
 */
public class UserHeadImageHelper {
    private XMPPTCPConnection connection;
    private String tag = UserHeadImageHelper.class.getSimpleName();

    public UserHeadImageHelper() {
        connection = MXmppConnManager.getInstance().getConnection();
    }

    /**
     * 获取用户信息类
     *
     * @param user 用户UID
     * @return VCard
     */
    public VCard getVCard(String user) {
        VCardManager.getInstanceFor(connection);
        VCard vCard = new VCard();
        ProviderManager.addIQProvider("vCard", "vcard-temp",
                new org.jivesoftware.smackx.vcardtemp.provider.VCardProvider());

        try {
            vCard.load(connection, user);
        } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return vCard;
    }

    /**
     * 设置用户头像
     *
     * @param image 图片头像数据
     */
    public void setUserImage(final byte[] image) throws XMPPException {
        VCardManager vCardManager = VCardManager.getInstanceFor(connection);
        String jid = connection.getUser();
        XLog.e(tag, "jid: " + jid);

        try {
            VCard card = getVCard(jid);
//            VCard card = new VCard();
//            card.load(connection);
            card.setAvatar(image, "avatar1/jpg");
            vCardManager.saveVCard(card);
            XLog.e(tag, "保存头像成功");
            LcUserManager.instance.showDrawable.put(jid, new WeakReference<>(FormatTools.getInstance().Bytes2Drawable(card.getAvatar())));

        } catch (SmackException.NoResponseException | SmackException.NotConnectedException e3) {
            e3.printStackTrace();
        }
    }

    public Drawable getHeadDrawable() {
        return getHeadDrawable(connection.getUser());
    }

    /**
     * 获取用户自己的头像
     *
     * @param jid connection.getUser()
     * @return
     */
    public Drawable getHeadDrawable(String jid) {
        Drawable drawable = null;

        try {
            VCard vCard = new VCard();
            vCard.setJabberId(jid);
            vCard.load(connection);
            if (vCard.getAvatar() != null) {
                drawable = FormatTools.getInstance().Bytes2Drawable(vCard.getAvatar());
                LcUserManager.instance.showDrawable.put(jid, new WeakReference<>(drawable));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drawable;
    }

    /**
     * 获取他人的头像
     *
     * @param user
     * @return
     */
    public Drawable getUserDrawable(String user) {
        Drawable drawable = null;
        try {
            VCard vCard = getVCard(user);
            if (vCard != null && vCard.getAvatar() != null) {
                drawable = FormatTools.getInstance().Bytes2Drawable(vCard.getAvatar());
                LcUserManager.instance.showDrawable.put(user, new WeakReference<>(drawable));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drawable;
    }

    public Bitmap getSelfBitmap(int width, int height) {
        return drawable2BitMap(getHeadDrawable(), width, height);
    }

    public Bitmap getOtherBitmap(String key, int width, int height) {
        return drawable2BitMap(getUserDrawable(key), width, height);
    }

    private Bitmap drawable2BitMap(Drawable drawable, int width, int height) {
        if (drawable == null)
            return null;

        if (width >= 0 && height >= 0 && width <= drawable.getIntrinsicWidth() && height <= drawable.getIntrinsicHeight())
            return FormatTools.getInstance().drawable2Bitmap(drawable, width, height);
        else
            return FormatTools.getInstance().drawable2Bitmap(drawable);
    }
}