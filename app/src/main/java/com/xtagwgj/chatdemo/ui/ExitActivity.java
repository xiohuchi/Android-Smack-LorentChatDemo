package com.xtagwgj.chatdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lorent.chat.smack.UserHeadImageHelper;
import com.lorent.chat.ui.base.BaseView;
import com.lorent.chat.ui.base.MvpActivity;
import com.lorent.chat.ui.contract.ExitContract;
import com.lorent.chat.ui.contract.ExitPresenter;
import com.lorent.chat.utils.FileUtils;
import com.lorent.chat.utils.ToastUtils;
import com.lorent.chat.utils.XLog;
import com.xtagwgj.chatdemo.GlideLoader;
import com.xtagwgj.chatdemo.R;
import com.yancy.imageselector.ImageConfig;
import com.yancy.imageselector.ImageSelector;
import com.yancy.imageselector.ImageSelectorActivity;

import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ExitActivity extends MvpActivity<ExitPresenter> implements ExitContract.View {

    ImageView headIcoImageView;
    UserHeadImageHelper userHeadImage = new UserHeadImageHelper();
    private java.lang.String tag = ExitActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("退出app");

        showHeadImg();
    }

    @Override
    public void initView() {
        headIcoImageView = (ImageView) findViewById(R.id.headIcoImageView);
        findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.logOut();
            }
        });
        headIcoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //noinspection deprecation
                ImageConfig imageConfig
                        = new ImageConfig.Builder(new GlideLoader())
                        .steepToolBarColor(getResources().getColor(R.color.blue))
                        .titleBgColor(getResources().getColor(R.color.blue))
                        .titleSubmitTextColor(getResources().getColor(R.color.white))
                        .titleTextColor(getResources().getColor(R.color.white))
                        // (截图默认配置：关闭    比例 1：1    输出分辨率  500*500)
                        .crop(1, 1, 500, 500)
                        // 开启单选   （默认为多选）
                        .singleSelect()
                        // 开启拍照功能 （默认关闭）
                        .showCamera()
                        // 拍照后存放的图片路径（默认 /temp/picture） （会自动创建）
//                        .filePath("/ImageSelector/Pictures")
                        .build();
                ImageSelector.open(ExitActivity.this, imageConfig);   // 开启图片选择器
            }
        });
    }

    @Override
    public BaseView getBaseView() {
        return this;
    }

    @Override
    public int getResLayout() {
        return R.layout.activity_exit;
    }

    @Override
    public void logOutSuccess() {
        showToast("log out success");
    }

    @Override
    public void showToast(String msg) {
        ToastUtils.createCenterNormalToast(this, msg, Toast.LENGTH_SHORT);
    }

    /**
     * 更新头像文件
     *
     * @param filePath 文件地址
     */
    private void updateUserHeadImg(final String filePath) {
        final File file = new File(filePath);

        if (!file.exists()) {
            XLog.e(tag, "头像文件丢失");
            return;
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] image = FileUtils.getFileBytes(file);
                    userHeadImage.setUserImage(image);
                } catch (IOException | XMPPException e) {
                    e.printStackTrace();
                } finally {
                    file.delete();
                    showHeadImg();
                }
            }
        });
    }

    private void showHeadImg() {
        headIcoImageView.setImageDrawable(userHeadImage.getHeadDrawable());
//        Glide.with(this)
//                .load(userHeadImage.getHeadImageFile())
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .into(headIcoImageView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ImageSelector.IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            List<String> pathList = data.getStringArrayListExtra(ImageSelectorActivity.EXTRA_RESULT);

            for (String path : pathList) {
                XLog.i(tag, "单选获取的头像地址" + path);
                updateUserHeadImg(path);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userHeadImage = null;
    }
}