package com.cqsynet.swifi.activity.social;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.BasicFragmentActivity;
import com.cqsynet.swifi.activity.UserCenterActivity;
import com.cqsynet.swifi.db.ChatMsgDao;
import com.cqsynet.swifi.db.FriendApplyDao;
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
    private TextView mTvMsgHint;
    private ImageView mIvFindPerson;
    private ImageView mIvFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);

        mTvTitle = findViewById(R.id.tv_title);
        mTvTitle.setText("找人");
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ImageView ivMine = findViewById(R.id.iv_mine);
        ivMine.setOnClickListener(new View.OnClickListener() {
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
        mTvMsgHint = findViewById(R.id.tv_msg_hint);
        LinearLayout llFindPerson = findViewById(R.id.ll_find_person);
        mIvFindPerson = findViewById(R.id.iv_find_person);
        LinearLayout llFriends = findViewById(R.id.ll_friends);
        mIvFriends = findViewById(R.id.iv_friends);

        flMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvTitle.setText("消息");
                mIvMessage.setImageResource(R.drawable.ic_social_msg_green);
                mIvFindPerson.setImageResource(R.drawable.ic_social_find_gray);
                mIvFriends.setImageResource(R.drawable.ic_social_friend_gray);
                mViewPager.setCurrentItem(0, false);
            }
        });
        llFindPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvTitle.setText("找人");
                mIvMessage.setImageResource(R.drawable.ic_social_msg_gray);
                mIvFindPerson.setImageResource(R.drawable.ic_social_find_green);
                mIvFriends.setImageResource(R.drawable.ic_social_friend_gray);
                mViewPager.setCurrentItem(1, false);
            }
        });
        llFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvTitle.setText("好友");
                mIvMessage.setImageResource(R.drawable.ic_social_msg_gray);
                mIvFindPerson.setImageResource(R.drawable.ic_social_find_gray);
                mIvFriends.setImageResource(R.drawable.ic_social_friend_green);
                mViewPager.setCurrentItem(2, false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        int count = ChatMsgDao.getInstance(this).queryAllUnReadMsgCount() + FriendApplyDao.getInstance(this).queryUnReadApplyCount();
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
}
