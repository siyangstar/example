package com.cqsynet.swifi.activity.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.HkActivity;
import com.cqsynet.swifi.activity.SimpleWebActivity;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.db.FriendApplyDao;
import com.cqsynet.swifi.db.FriendsDao;
import com.cqsynet.swifi.model.FriendApplyInfo;
import com.cqsynet.swifi.model.FriendsInfo;
import com.cqsynet.swifi.model.ReplyFriendRequestBody;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;

import org.json.JSONException;

/**
 * Author: sayaki
 * Date: 2017/11/28
 */
public class FriendReplyActivity extends HkActivity {

    private static final int SET_REMARK_REQUEST = 0;

    private FriendApplyInfo mFriendApplyInfo;
    private TextView mTvRemark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_reply);

        mFriendApplyInfo = getIntent().getParcelableExtra("friendApplyInfo");

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView ivAvatar = findViewById(R.id.iv_avatar);
        if (!TextUtils.isEmpty(mFriendApplyInfo.avatar)) {
            GlideApp.with(this)
                    .load(mFriendApplyInfo.avatar)
                    .circleCrop()
                    .into(ivAvatar);
        }
        TextView tvName = findViewById(R.id.tv_name);
        if (!TextUtils.isEmpty(mFriendApplyInfo.nickname)) {
            tvName.setText(mFriendApplyInfo.nickname);
        }
        TextView tvAge = findViewById(R.id.tv_age);
        if (!TextUtils.isEmpty(mFriendApplyInfo.age)) {
            tvAge.setText(mFriendApplyInfo.age);
        }
        ImageView ivSex = findViewById(R.id.iv_sex);
        if ("男".equals(mFriendApplyInfo.sex)) {
            ivSex.setImageResource(R.drawable.ic_male);
        } else if ("女".equals(mFriendApplyInfo.sex)) {
            ivSex.setImageResource(R.drawable.ic_female);
        }
        TextView tvMessage = findViewById(R.id.tv_message);
        if (!TextUtils.isEmpty(mFriendApplyInfo.content)) {
            tvMessage.setText(mFriendApplyInfo.content);
        }
        mTvRemark = findViewById(R.id.tv_remark);
        mTvRemark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendReplyActivity.this, RemarkActivity.class);
                intent.putExtra("friendAccount", mFriendApplyInfo.userAccount);
                intent.putExtra("nickname", mFriendApplyInfo.nickname);
                intent.putExtra("action", "set");
                startActivityForResult(intent, SET_REMARK_REQUEST);
            }
        });

        TextView tvAgree = findViewById(R.id.tv_agree);
        tvAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyFriendRequest("0");
            }
        });
        TextView tvRefuse = findViewById(R.id.tv_refuse);
        tvRefuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyFriendRequest("1");
            }
        });
        TextView tvBlacklist = findViewById(R.id.tv_blacklist);
        tvBlacklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyFriendRequest("2");
            }
        });
        TextView tvComplaint = findViewById(R.id.tv_complaint);
        tvComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFriendApplyInfo != null) {
                    Intent complainIntent = new Intent(FriendReplyActivity.this, SimpleWebActivity.class);
                    complainIntent.putExtra("title", "投诉");
                    complainIntent.putExtra("url", AppConstants.COMPLAIN_PAGE);
                    complainIntent.putExtra("friendAccount", mFriendApplyInfo.userAccount);
                    complainIntent.putExtra("userAccount", SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    complainIntent.putExtra("complainType", "chat");
                    startActivity(complainIntent);
                }
            }
        });
        TextView tvHint = findViewById(R.id.tv_hint);

        if (!"0".equals(mFriendApplyInfo.replyStatus)) {
            tvAgree.setVisibility(View.GONE);
            tvRefuse.setVisibility(View.GONE);
            tvHint.setVisibility(View.VISIBLE);
            if ("1".equals(mFriendApplyInfo.replyStatus)) {
                tvHint.setText("已同意");
            } else if ("2".equals(mFriendApplyInfo.replyStatus)) {
                tvHint.setText("已拒绝");
            } else if ("3".equals(mFriendApplyInfo.replyStatus)) {
                tvHint.setText("已拉黑");
            }
        }
    }

    private void replyFriendRequest(final String type) {
        final ReplyFriendRequestBody body = new ReplyFriendRequestBody();
        body.messageId = mFriendApplyInfo.messageId;
        body.type = type;
        body.friendAccount = mFriendApplyInfo.userAccount;
        if (!TextUtils.isEmpty(mTvRemark.getText().toString())) {
            body.remark = mTvRemark.getText().toString();
        } else {
            body.remark = "";
        }
        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                if (type.equals("0")) {
                    UserInfo userInfo = new UserInfo();
                    userInfo.userAccount = mFriendApplyInfo.userAccount;
                    userInfo.nickname = mFriendApplyInfo.nickname;
                    userInfo.headUrl = mFriendApplyInfo.avatar;
                    userInfo.age = mFriendApplyInfo.age;
                    userInfo.sex = mFriendApplyInfo.sex;
                    userInfo.remark = body.remark;
                    ContactDao contactDao = ContactDao.getInstance(FriendReplyActivity.this);
                    contactDao.saveUser(userInfo);

                    FriendsDao friendsDao = FriendsDao.getInstance(FriendReplyActivity.this);
                    FriendsInfo friendsInfo = friendsDao.query(mFriendApplyInfo.userAccount,
                            SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    if (friendsInfo == null) {
                        friendsDao.insert(mFriendApplyInfo.userAccount,
                                SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    }
                    FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(FriendReplyActivity.this);
                    FriendApplyInfo friendApplyInfo = friendApplyDao.query(mFriendApplyInfo.userAccount,
                            SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    if (friendApplyInfo != null) {
                        friendApplyInfo.replyStatus = "1";
                        friendApplyDao.insert(friendApplyInfo, SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    }
                } else if (type.equals("1")) {
                    FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(FriendReplyActivity.this);
                    FriendApplyInfo friendApplyInfo = friendApplyDao.query(mFriendApplyInfo.userAccount,
                            SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    if (friendApplyInfo != null) {
                        friendApplyInfo.replyStatus = "2";
                        friendApplyDao.insert(friendApplyInfo, SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    }
                } else if (type.equals("2")) {
                    FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(FriendReplyActivity.this);
                    FriendApplyInfo friendApplyInfo = friendApplyDao.query(mFriendApplyInfo.userAccount,
                            SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    if (friendApplyInfo != null) {
                        friendApplyInfo.replyStatus = "3";
                        friendApplyDao.insert(friendApplyInfo, SharedPreferencesInfo.getTagString(FriendReplyActivity.this, SharedPreferencesInfo.ACCOUNT));
                    }
                }

                Intent intent = new Intent();
                intent.putExtra("reply", type);
                setResult(Activity.RESULT_OK, intent);
                FriendReplyActivity.this.finish();
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(FriendReplyActivity.this, "发生错误，操作未完成");
            }
        };
        WebServiceIf.replyFriendRequest(this, body, callback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SET_REMARK_REQUEST) {
                mTvRemark.setText(data.getStringExtra("remark"));
            }
        }
    }
}
