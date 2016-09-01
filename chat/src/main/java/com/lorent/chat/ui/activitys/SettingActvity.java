package com.lorent.chat.ui.activitys;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.lorent.chat.R;
import com.lorent.chat.common.AppManager;
import com.lorent.chat.common.LorentChatApplication;
import com.lorent.chat.smack.UserHeadImageHelper;
import com.lorent.chat.smack.constVar.CustomConst;
import com.lorent.chat.ui.base.BaseView;
import com.lorent.chat.ui.base.MvpActivity;
import com.lorent.chat.ui.contract.ExitContract;
import com.lorent.chat.ui.contract.ExitPresenter;
import com.lorent.chat.utils.FileUtils;
import com.lorent.chat.utils.ToastUtils;
import com.lorent.chat.utils.cache.CacheUtils;

import org.jivesoftware.smack.XMPPException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class SettingActvity extends MvpActivity<ExitPresenter> implements ExitContract.View, OnClickListener, OnCheckedChangeListener {

    private SharedPreferences sharedPreferences;


    private Dialog dialogSetLocationHz;

    private View dialogView;
    /**
     * 设置定位频率参数
     */
    private Button btn_commit_ok;

    private Button btn_commit_cancel;

    private EditText ed_dialog;

    private Button btn_logout;

    private ImageView userIcon;


    @Override
    public void initView() {
        if (getActionBar() != null) {
            getActionBar().setBackgroundDrawable(new ColorDrawable(0xff47b8ff));
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);

        TextView tv_logout = (TextView) findViewById(R.id.line_logout).findViewById(R.id.tv_line);
        userIcon = (ImageView) findViewById(R.id.iv_usr_icon);


        btn_logout = (Button) findViewById(R.id.btn_logout);


        btn_logout.setOnClickListener(this);

        tv_logout.setText("账号");
        tv_logout.setText(sharedPreferences.getString("name", ""));

        userIcon.setOnClickListener(this);

        File fhead = new File(userHeadFilePath() + "/" + userHeadFileName());
        if (fhead.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(userHeadFilePath() + "/" + userHeadFileName());
            userIcon.setImageBitmap(bm);
        }

    }

    @Override
    public BaseView getBaseView() {
        return this;
    }

    @Override
    public int getResLayout() {
        return R.layout.setting_tableview_layout;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }


    @Override
    public void onClick(View v) {

        Editor editor = sharedPreferences.edit();

        if (v.getId() == R.id.btn_commit_cancel) {
            dialogSetLocationHz.dismiss();
            ed_dialog.setText("");
        } else if (v.getId() == R.id.btn_commit_ok) {
            dialogSetLocationHz.dismiss();
            ed_dialog.setText("");
        } else if (v.getId() == R.id.btn_logout) {
            mPresenter.logOut();
        } else if (v.getId() == R.id.iv_usr_icon) {
            choseHeadImageFromGallery();
        }

        editor.commit();
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        Editor editor = sharedPreferences.edit();
        switch (checkedId) {

        }
        editor.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * wyq
     * 测试设置头像
     */
    private void updateUserHeadImg(String fileName) {
        byte[] imag;
        File file = new File(fileName);
        if (file.exists()) {
            try {
                imag = FileUtils.getFileBytes(file);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
            Bitmap bm = BitmapFactory.decodeFile(fileName);
            userIcon.setImageBitmap(bm);
        } else {
            Resources res = getResources();
            Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.userhead);
            userIcon.setBackgroundResource(R.drawable.userhead);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
            imag = baos.toByteArray();
        }


        try {
            new UserHeadImageHelper().setUserImage(imag);
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /***************************************
     * 8
     *///////////////////////////

    /* 请求识别码 */
    private static final int CODE_GALLERY_REQUEST = 0xa0;//本地
    //private static final int CODE_CAMERA_REQUEST = 0xa1;//拍照
    private static final int CODE_RESULT_REQUEST = 0xa2;//最终裁剪后的结果	

    // 裁剪后图片的宽(X)和高(Y),480 X 480的正方形。
    private final static int output_X = 600;
    private final static int output_Y = 600;

    // 从本地相册选取图片作为头像
    private void choseHeadImageFromGallery() {

        Intent intentFromGallery = new Intent();
        // 设置文件类型
        intentFromGallery.setType("image/*");//选择图片
        intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
        //如果你想在Activity中得到新打开Activity关闭后返回的数据，
        //你需要使用系统提供的startActivityForResult(Intent intent,int requestCode)方法打开新的Activity
        startActivityForResult(intentFromGallery, CODE_GALLERY_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent intent) {
        // 用户没有进行有效的设置操作，返回
        if (resultCode == RESULT_CANCELED) {//取消
            Toast.makeText(getApplication(), "取消", Toast.LENGTH_LONG).show();
            return;
        }

        switch (requestCode) {
            case CODE_GALLERY_REQUEST://如果是来自本地的
                cropRawPhoto(intent.getData());//直接裁剪图片
                break;


            case CODE_RESULT_REQUEST:
                if (intent != null) {
                    setImageToHeadView(intent);//设置图片框
                }

                break;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * 裁剪原始的图片
     */
    public void cropRawPhoto(Uri uri) {

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");

        //把裁剪的数据填入里面

        // 设置裁剪
        intent.putExtra("crop", "true");

        // aspectX , aspectY :宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX , outputY : 裁剪图片宽高
        intent.putExtra("outputX", output_X);
        intent.putExtra("outputY", output_Y);
        //intent.putExtra("return-data", true);


        //uritempFile为Uri类变量，实例化uritempFile
        Uri uritempFile = Uri.parse("file://" + "/" + userHeadFilePath() + "/" + userHeadFileName());
        FileUtils.mkdirs(uritempFile.toString());
        Log.e("", "file name : " + uritempFile.toString());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        startActivityForResult(intent, CODE_RESULT_REQUEST);
    }

    private String userHeadFilePath() {
        return CacheUtils.getImagePath(LorentChatApplication.getInstance(), CustomConst.USERHEAD_PATH);
    }

    private String userHeadFileName() {
        return CustomConst.USERHEAD_FILENAME;
    }

    /**
     * 提取保存裁剪之后的图片数据，并设置头像部分的View
     */
    private void setImageToHeadView(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            updateUserHeadImg(userHeadFilePath() + "/" + userHeadFileName());
        }
    }

    @Override
    public void logOutSuccess() {
        AppManager.instance.AppExit(LorentChatApplication.getInstance());
        System.gc();
    }

    @Override
    public void showToast(String msg) {
        ToastUtils.createCenterNormalToast(this, msg, Toast.LENGTH_SHORT);
    }
}
