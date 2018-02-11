/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：漂流瓶好友信息页面
 *
 *
 * 创建标识：zhaosy 20161109
 */
package com.cqsynet.swifi.activity;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.model.GetFriendInfoRequestBody;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.model.UserInfoResponseObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.TitleBar;
import com.google.gson.Gson;

public class FriendInfoActivity extends HkActivity {
    private TitleBar mTitleBar;
    private ImageView mIvHead; // 头像
    private ImageView mIvSex; // 生日
    private TextView mTvName; // 性别
    private TextView mTvSign; //签名
    private Button mBtnComplain; //投诉
    private String mFriendAccount; //账号
    private String mPosition; //位置
    private String mHeadUrl;
    private String mChatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);
        mTitleBar = findViewById(R.id.titlebar_activity_friend_info);
        mTitleBar.setTitle(R.string.user_info);
        mTitleBar.findViewById(R.id.ivBack_titlebar_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mIvHead = findViewById(R.id.ivHead_activity_friend_info);
        mIvSex = findViewById(R.id.ivSex_activity_friend_info);
        mTvName = findViewById(R.id.tvName_activity_friend_info);
        mTvSign = findViewById(R.id.tvSign_activity_friend_info);
        mBtnComplain = findViewById(R.id.btnComplain_activity_friend_info);

        mFriendAccount = getIntent().getStringExtra("friendAccount");
        mPosition = getIntent().getStringExtra("position");
        mChatId = getIntent().getStringExtra("chatId");
        //查询用户信息
        ContactDao contactDao = ContactDao.getInstance(this);
        final UserInfo userInfo = contactDao.queryUser(mFriendAccount);
        if (userInfo != null) {
            showFriendInfo(userInfo);
        }

        mIvHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(FriendInfoActivity.this, ImagePreviewActivity.class);
                intent.putExtra("imgUrl", mHeadUrl);
                intent.putExtra("defaultResId", R.drawable.icon_profile_default);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        //投诉
        mBtnComplain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent complainIntent = new Intent(FriendInfoActivity.this, SimpleWebActivity.class);
                complainIntent.putExtra("title", "投诉");
                complainIntent.putExtra("url", AppConstants.COMPLAIN_PAGE);
                complainIntent.putExtra("friendAccount", mFriendAccount);
                complainIntent.putExtra("chatId", mChatId);
                complainIntent.putExtra("complainType", "chat");
                startActivity(complainIntent);
            }
        });

        getFriendInfo(mFriendAccount);
    }

    /**
     * 显示用户信息
     *
     * @param userInfo
     */
    private void showFriendInfo(UserInfo userInfo) {
        //显示头像
        if (!TextUtils.isEmpty(userInfo.headUrl)) {
            GlideApp.with(this)
                    .load(userInfo.headUrl)
                    .centerCrop()
                    .error(R.drawable.image_bg)
                    .into(mIvHead);
        }

        //设置性别
        if (userInfo.sex.equals("男")) {
            mIvSex.setImageResource(R.drawable.man);
        } else {
            mIvSex.setImageResource(R.drawable.woman);
        }

        //设置昵称
        mTvName.setText(mPosition);

        //设置签名
        mTvSign.setText("个性签名：" + userInfo.sign);
    }

    /**
     * 查询用户信息
     *
     * @param userAccount
     */
    private void getFriendInfo(final String userAccount) {
        final GetFriendInfoRequestBody requestBody = new GetFriendInfoRequestBody();
        requestBody.friendAccount = userAccount;
        WebServiceIf.IResponseCallback getFriendInfoCallbackIf = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Gson gson = new Gson();
                    UserInfoResponseObject responseObj = gson.fromJson(response, UserInfoResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            UserInfo userInfo = responseObj.body;
                            mHeadUrl = userInfo.headUrl;
                            userInfo.userAccount = userAccount;
                            //将联系人数据存数据库
                            try {
                                ContactDao contactDao = ContactDao.getInstance(FriendInfoActivity.this);
                                contactDao.saveUser(userInfo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //显示联系人
                            showFriendInfo(userInfo);
                        } else {
                            ToastUtil.showToast(FriendInfoActivity.this, getResources().getString(R.string.request_fail_warning) + "(" + header.errCode + ")");
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(FriendInfoActivity.this, R.string.request_fail_warning);
            }
        };
        // 调用接口发起登陆
        WebServiceIf.getFriendInfo(this, requestBody, getFriendInfoCallbackIf);
    }
}
