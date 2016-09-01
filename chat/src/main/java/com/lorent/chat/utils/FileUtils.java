package com.lorent.chat.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    private static java.lang.String tag = FileUtils.class.getSimpleName();

    /**
     * 创建文件夹
     *
     * @param file
     * @return
     */
    public static boolean mkdirs(String file) {
        String path = file.substring(0, file.lastIndexOf("/") + 1);
        File fi = new File(path);
        if (!fi.exists()) {
            return fi.mkdir();
        } else {
            return true;
        }
    }

    /*
     * 将byte数组写到文件
     */
    public static void writeFileByByteArray(byte[] array, String path) throws IOException {

        File file = new File(path);
        File p = new File(path.substring(0, path.lastIndexOf("/") + 1));
        if (!p.exists()) {
            System.out.println(p.mkdir());
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(array, 0, array.length);
        fos.close();
    }

    /**
     * 获取打开相册获取的图片的路径
     *
     * @param intent
     * @param activity
     * @return
     */
    public static String getPictureSelectedPath(Intent intent, Activity activity) {
        Uri uri = intent.getData();
        Cursor cursor = activity.managedQuery(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToNext();
        String path = cursor.getString(index);
        return path;
    }

    /**
     * 将传进来的Bitmap经过压缩处理并写到缓存目录，再将压缩的图片返回其字节数组
     *
     * @param bitmap  传进的图片
     * @param context 上下文
     * @return
     * @throws IOException
     */
    public static byte[] compressAndWriteFile(Bitmap bitmap, Context context, String path) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 40, baos);
        FileUtils.writeFileByByteArray(baos.toByteArray(), path);
        return baos.toByteArray();
    }

    /**
     * 文件转字节
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] getFileBytes(File file) throws IOException {
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            int bytes = (int) file.length();
            byte[] buffer = new byte[bytes];
            int readBytes = bis.read(buffer);
            if (readBytes != buffer.length) {
                throw new IOException("Entire file not read");
            }
            return buffer;
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
    }

    public static File byte2File(byte[] buf, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            file = File.createTempFile(fileName, ".jpg", new File(filePath));
            XLog.e(tag, file.getAbsolutePath());

            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    public static String image2String(File f) throws Exception {
        FileInputStream fis = new FileInputStream(f);
        byte[] bytes = new byte[fis.available()];
        fis.read(bytes);
        fis.close();

        // 生成字符串  
        String imgStr = byte2hex(bytes);
        return imgStr;

    }

    private static String byte2hex(byte[] b) {
        StringBuffer sb = new StringBuffer();
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1) {
                sb.append("0" + stmp);
            } else {
                sb.append(stmp);
            }

        }
        return sb.toString();
    }


    public static String getImageContent(String textFound) {
        int start = textFound.indexOf("<img>") + 5;
        int end = textFound.indexOf("</img>");
        String str = textFound.substring(start, end);
        if (str == null || str.isEmpty())
            return null;
        str = str.trim();
        return str;
    }

    public static byte[] hex2byte(String textHex) {

        int len = textHex.length();
        if (len == 0 || len % 2 == 1)
            return null;
        byte[] b = new byte[len / 2];
        try {
            for (int i = 0; i < textHex.length(); i += 2) {
                b[i / 2] = (byte) Integer.decode("0X" + textHex.substring(i, i + 2)).intValue();
            }
            return b;
        } catch (Exception e) {
            return null;
        }
    }
}
