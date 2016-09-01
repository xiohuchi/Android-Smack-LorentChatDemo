package com.lorent.chat.message;

import com.lorent.chat.utils.FileUtils;
import com.lorent.chat.utils.TypeConverter;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/*********
 * 群发图片消息Body格式如下:
 * <img>XXXXX</image>
 *
 * @author wuyaoquan
 */
public class GroupChatImageMessageBody implements GroupChatExtMessageBody {


    private static final String tag = "GroupChatImageMessageBody";

    private String imageContent = null;

    /**
     * @param fileFullName, 保存的文件路径或读取的文件路径
     */
    public GroupChatImageMessageBody() {

    }

    //发送

    /**
     * 把fileFullName文件读取并转换成字符串，
     */
    public boolean readImage(String fileFullName) {
        String imageByte = null;
        File file = new File(fileFullName);//本地创建截图
        if (file.exists()) {
            try {
                imageByte = FileUtils.image2String(file);//得到转码后的字符串
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (imageByte != null && !imageByte.isEmpty()) {
                imageContent = new String(imageByte);
                return true;
            }

        }
        return false;
    }

    /*
     * 将字符串的image数据转换成byte然后保存到指定的文件内；
     */
    public boolean saveImage(String fileFullName) {
        byte[] imgbyte = null;
        if (imageContent == null || imageContent.isEmpty())
            return false;

        imgbyte = FileUtils.hex2byte(imageContent);

        ByteArrayInputStream bis = new ByteArrayInputStream(imgbyte);
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(new File(fileFullName)));
            TypeConverter.writeFile(bos, bis);
            return true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 导入packet中的body数据，然后提取image hex 字符串数据,暂时不转换成byte
     */
    @Override
    public boolean setMessageBody(String body) {
        // TODO Auto-generated method stub
        String tokeStart = GroupChatExtType.IMG_TOKEN_START;
        String tokenEnd = GroupChatExtType.IMG_TOKEN_END;
        int start = body.indexOf(tokeStart) + tokeStart.length();
        int end = body.indexOf(tokenEnd);
        //image true body
        String str = body.substring(start, end);
        if (str == null || str.isEmpty())
            return false;

        imageContent = new String(str);

        return true;
    }

    @Override
    public String getMessageBody() {
        // TODO Auto-generated method stub
        String tokeStart = GroupChatExtType.IMG_TOKEN_START;
        String tokenEnd = GroupChatExtType.IMG_TOKEN_END;

        if (imageContent != null)
            return tokeStart + imageContent + tokenEnd;
        else
            return null;
    }


}
