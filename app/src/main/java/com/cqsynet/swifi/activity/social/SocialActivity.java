/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：社交主界面
 *
 *
 * 创建标识：sayaki 20171123
 */
package com.cqsynet.swifi.activity.social;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.BasicFragmentActivity;
import com.cqsynet.swifi.activity.UserCenterActivity;
import com.cqsynet.swifi.db.ChatMsgDao;
import com.cqsynet.swifi.db.FriendApplyDao;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.view.NoSlidingViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/11/23
 */
public class SocialActivity extends BasicFragmentActivity {

    private TextView mTvTitle;
    private NoSlidingViewPager mViewPager;
    private ImageView mIvMessage;
    private TextView mTvMessage;
    private TextView mTvMsgHint;
    private ImageView mIvFindPerson;
    private TextView mTvFindPerson;
    private ImageView mIvFriends;
    private TextView mTvFriends;
    private MessageReceiver mMessageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);

        mTvTitle = findViewById(R.id.tvTitle_activity_social);
        mTvTitle.setText("找人");
        findViewById(R.id.ivBack_activity_social).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.ivMine_activity_social).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SocialActivity.this, UserCenterActivity.class);
                startActivity(intent);
            }
        });

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new ChatListFragment());
        fragments.add(new FindPersonFragment());
        fragments.add(new FriendsFragment());

        SocialPagerAdapter adapter = new SocialPagerAdapter(getSupportFragmentManager(), fragments);
        mViewPager = findViewById(R.id.view_pager);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(1, false);
        mViewPager.setOffscreenPageLimit(2);

        FrameLayout flMessage = findViewById(R.id.fl_message);
        mIvMessage = findViewById(R.id.iv_message);
        mTvMessage = findViewById(R.id.tv_message);
        mTvMsgHint = findViewById(R.id.tv_msg_hint);
        LinearLayout llFindPerson = findViewById(R.id.ll_find_person);
        mIvFindPerson = findViewById(R.id.iv_find_person);
        mTvFindPerson = findViewById(R.id.tv_find_person);
        LinearLayout llFriends = findViewById(R.id.ll_friends);
        mIvFriends = findViewById(R.id.iv_friends);
        mTvFriends = findViewById(R.id.tv_friends);

        flMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvTitle.setText("消息");
                mIvMessage.setImageResource(R.drawable.ic_social_msg_green);
                mTvMessage.setTextColor(getResources().getColor(R.color.green));
                mIvFindPerson.setImageResource(R.drawable.ic_social_find_gray);
                mTvFindPerson.setTextColor(getResources().getColor(R.color.text3));
                mIvFriends.setImageResource(R.drawable.ic_social_friend_gray);
                mTvFriends.setTextColor(getResources().getColor(R.color.text3));
                mViewPager.setCurrentItem(0, false);
            }
        });
        llFindPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvTitle.setText("找人");
                mIvMessage.setImageResource(R.drawable.ic_social_msg_gray);
                mTvMessage.setTextColor(getResources().getColor(R.color.text3));
                mIvFindPerson.setImageResource(R.drawable.ic_social_find_green);
                mTvFindPerson.setTextColor(getResources().getColor(R.color.green));
                mIvFriends.setImageResource(R.drawable.ic_social_friend_gray);
                mTvFriends.setTextColor(getResources().getColor(R.color.text3));
                mViewPager.setCurrentItem(1, false);
            }
        });
        llFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvTitle.setText("好友");
                mIvMessage.setImageResource(R.drawable.ic_social_msg_gray);
                mTvMessage.setTextColor(getResources().getColor(R.color.text3));
                mIvFindPerson.setImageResource(R.drawable.ic_social_find_gray);
                mTvFindPerson.setTextColor(getResources().getColor(R.color.text3));
                mIvFriends.setImageResource(R.drawable.ic_social_friend_green);
                mTvFriends.setTextColor(getResources().getColor(R.color.green));
                mViewPager.setCurrentItem(2, false);
            }
        });

        mMessageReceiver = new MessageReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(AppConstants.ACTION_SOCKET_PUSH);
        registerReceiver(mMessageReceiver, filter);
        updateHint();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mMessageReceiver != null) {
            unregisterReceiver(mMessageReceiver);
        }
    }

    /**
     * 消息数量红点提示
     */
    private void updateHint() {
        int count = ChatMsgDao.getInstance(this).queryAllUnReadMsgCount("friend");
        if(SharedPreferencesInfo.getTagBoolean(this, SharedPreferencesInfo.MSG_BOTTLE, true)) {
            count += ChatMsgDao.getInstance(this).queryAllUnReadMsgCount("bottle");
        }
        if(SharedPreferencesInfo.getTagBoolean(this, SharedPreferencesInfo.MSG_FRIEND_APPLY, true)) {
            count += FriendApplyDao.getInstance(this).queryUnReadApplyCount();
        }
        if (count < 100) {
            mTvMsgHint.setText(count + "");
        } else {
            mTvMsgHint.setText("···");
        }
        if (count != 0) {
            mTvMsgHint.setVisibility(View.VISIBLE);
        } else {
            mTvMsgHint.setVisibility(View.GONE);
        }
    }

    /**
     * 广播接收消息内容
     */
    private class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(AppConstants.ACTION_SOCKET_PUSH)) {
                String type = intent.getStringExtra("type");
                if (!TextUtils.isEmpty(type) && (type.equals(AppConstants.PUSH_BOTTLE) || type.equals(AppConstants.PUSH_CHAT) || type.equals(AppConstants.PUSH_FRIEND_APPLY))) {
                    updateHint();
                }
            }
        }
    }
}
