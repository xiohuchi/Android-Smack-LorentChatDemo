package com.lorent.chat.smack;

import android.graphics.drawable.Drawable;

import com.lorent.chat.R;
import com.lorent.chat.common.LcUserManager;
import com.lorent.chat.common.LorentChatApplication;
import com.lorent.chat.smack.connection.MXmppConnManager;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.utils.FormatTools;
import com.lorent.chat.utils.XLog;
import com.lorent.chat.utils.cache.CacheUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.ByteArrayInputStream;
import java.lang.ref.SoftReference;

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
     * @return
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
            VCard card = new VCard();
            card.load(connection);
            // card.setJabberId(jid);
            card.setAvatar(image, "avatar1/jpg");

            vCardManager.saveVCard(card);
            XLog.e(tag, "保存头像成功");
            LcUserManager.instance.showDrawable.put(jid, new SoftReference<>(FormatTools.getInstance().Bytes2Drawable(card.getAvatar())));

//            deleteAllUserImage(jid);
        } catch (SmackException.NoResponseException | SmackException.NotConnectedException e3) {
            e3.printStackTrace();
        }
    }

    public Drawable getHeadDrawable() {
        return getHeadDrawable(connection.getUser());
    }

    public Drawable getHeadDrawable(String jid) {
        SoftReference<Drawable> drawableSoftReference = LcUserManager.instance.showDrawable.get(jid);
        Drawable drawable;

        if (drawableSoftReference != null)
            drawable = drawableSoftReference.get();
        else {
            try {
                VCard vCard = new VCard();
                vCard.setJabberId(jid);
                vCard.load(connection);
                if (vCard.getAvatar() == null) {
                    return null;
                }
                drawable = FormatTools.getInstance().Bytes2Drawable(vCard.getAvatar());
                LcUserManager.instance.showDrawable.put(userHeadFileName(jid), new SoftReference<>(drawable));
            } catch (Exception e) {
                e.printStackTrace();
                drawable = LorentChatApplication.getInstance().getResources().getDrawable(R.drawable.qq_leba_list_seek_myfeeds);
            }
        }
//        FormatTools.getInstance().Bytes2Bitmap()
        return drawable;
    }

    //by wyq, 以后放在map里
    public Drawable getUserDrawable(String user) {
//		Log.i(tag, "getUserImage user : " + user);
        SoftReference<Drawable> drawableSoftReference = LcUserManager.instance.showDrawable.get(user);
        Drawable drawable;

        if (drawableSoftReference != null)
            drawable = drawableSoftReference.get();

        ByteArrayInputStream bais = null;
        try {
            VCard vCard = getVCard(user);
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


    /**
     * 默认缓存文件夹
     *
     * @return
     */
    private String userHeadFilePath() {
        return CacheUtils.getImagePath(LorentChatApplication.getInstance(), CustomConst.USERHEAD_PATH);
    }

    /**
     * 获取文件名称
     *
     * @param fileName 15620608104@lntdev/Smack
     * @return 将/替换为.之后返回的文件名称
     */
    private String userHeadFileName(String fileName) {
        if (fileName.endsWith("Smack"))
            return fileName;
        else
            return fileName + "/Smack";
    }
}