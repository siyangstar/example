package com.cqsynet.swifi.activity.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.HkActivity;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.db.FriendApplyDao;
import com.cqsynet.swifi.db.FriendsDao;
import com.cqsynet.swifi.model.FriendApplyInfo;
import com.cqsynet.swifi.model.FriendsInfo;
import com.cqsynet.swifi.model.ReplyFriendRequestBody;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.view.DeleteDialog;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2018/1/4
 */
public class FriendApplyListActivity extends HkActivity implements
        FriendApplyAdapter.ItemChildListener, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    private static final int FRIEND_REPLY_REQUEST = 0;

    private ListView mListView;
    private LinearLayout mLlHint;
    private TextView mTvEdit;
    private FrameLayout mFlAction;
    private TextView mTvCancel;
    private TextView mTvDelete;
    private DeleteDialog mDeleteDialog;

    private FriendApplyAdapter mAdapter;
    private List<FriendApplyInfo> mFriendApplyInfos = new ArrayList<>();
    private List<FriendApplyInfo> mSelectedFriendApplyInfos = new ArrayList<>();
    private boolean isMultiMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_apply_list);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mListView = findViewById(R.id.list_view);
        mAdapter = new FriendApplyAdapter(this, mFriendApplyInfos);
        mAdapter.setItemChildListener(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mLlHint = findViewById(R.id.ll_hint);
        mTvEdit = findViewById(R.id.tv_edit);
        mTvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMultiMode = true;
                mTvEdit.setVisibility(View.GONE);
                mFlAction.setVisibility(View.VISIBLE);
                mAdapter.setMultiMode(true);
            }
        });
        mFlAction = findViewById(R.id.fl_action);
        mTvCancel = findViewById(R.id.tv_cancel);
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMultiMode = false;
                mTvEdit.setVisibility(View.VISIBLE);
                mFlAction.setVisibility(View.GONE);
                mAdapter.setMultiMode(false);
                mSelectedFriendApplyInfos.clear();
            }
        });
        mTvDelete = findViewById(R.id.tv_delete);
        mTvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMultiMode = false;
                mTvEdit.setVisibility(View.VISIBLE);
                mFlAction.setVisibility(View.GONE);
                mAdapter.setMultiMode(false);

                FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(FriendApplyListActivity.this);
                for (FriendApplyInfo friendApplyInfo : mSelectedFriendApplyInfos) {
                    mFriendApplyInfos.remove(friendApplyInfo);
                    friendApplyDao.delete(friendApplyInfo.userAccount,
                            SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT));
                }
                mSelectedFriendApplyInfos.clear();

                mAdapter.notifyDataSetChanged();
                updateHint();
                sendBroadcast(new Intent(AppConstants.ACTION_UPDATE_MSG));
            }
        });

        FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(this);
        mFriendApplyInfos.addAll(friendApplyDao.queryList(SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT)));
        mAdapter.notifyDataSetChanged();

        for (FriendApplyInfo friendApplyInfo : mFriendApplyInfos) {
            friendApplyInfo.readStatus = "1";
            friendApplyDao.insert(friendApplyInfo, SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT));
        }
        sendBroadcast(new Intent(AppConstants.ACTION_UPDATE_MSG));
    }

    @Override
    public void onBackPressed() {
        if (isMultiMode) {
            isMultiMode = false;
            mTvEdit.setVisibility(View.VISIBLE);
            mFlAction.setVisibility(View.GONE);
            mAdapter.setMultiMode(false);
            mSelectedFriendApplyInfos.clear();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onAgreeClick(int position) {
        replyFriendRequest(position);
    }

    private void replyFriendRequest(final int position) {
        ReplyFriendRequestBody body = new ReplyFriendRequestBody();
        body.messageId = mFriendApplyInfos.get(position).messageId;
        body.type = "0";
        body.friendAccount = mFriendApplyInfos.get(position).userAccount;
        body.remark = "";
        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                Log.i("FriendApplyListActivity", "@@@#@response: " + response);
                UserInfo userInfo = new UserInfo();
                userInfo.userAccount = mFriendApplyInfos.get(position).userAccount;
                userInfo.nickname = mFriendApplyInfos.get(position).nickname;
                userInfo.headUrl = mFriendApplyInfos.get(position).avatar;
                userInfo.age = mFriendApplyInfos.get(position).age;
                userInfo.sex = mFriendApplyInfos.get(position).sex;
                userInfo.remark = "";
                ContactDao contactDao = ContactDao.getInstance(FriendApplyListActivity.this);
                contactDao.saveUser(userInfo);

                FriendsDao friendsDao = FriendsDao.getInstance(FriendApplyListActivity.this);
                FriendsInfo friendsInfo = friendsDao.query(mFriendApplyInfos.get(position).userAccount,
                        SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT));
                if (friendsInfo == null) {
                    friendsDao.insert(mFriendApplyInfos.get(position).userAccount,
                            SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT));
                }
                FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(FriendApplyListActivity.this);
                FriendApplyInfo friendApplyInfo = friendApplyDao.query(mFriendApplyInfos.get(position).userAccount,
                        SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT));
                if (friendApplyInfo != null) {
                    friendApplyInfo.replyStatus = "1";
                    friendApplyDao.insert(friendApplyInfo, SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT));
                }

                mFriendApplyInfos.clear();
                mFriendApplyInfos.addAll(friendApplyDao.queryList(SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT)));
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onErrorResponse() {

            }
        };
        WebServiceIf.replyFriendRequest(this, body, callback);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FriendApplyInfo friendApplyInfo = mFriendApplyInfos.get(position);
        if (!isMultiMode) {
            Intent intent = new Intent(this, FriendReplyActivity.class);
            intent.putExtra("friendApplyInfo", friendApplyInfo);
            startActivityForResult(intent, FRIEND_REPLY_REQUEST);
        } else {
            mAdapter.setSelectedFriendApply(friendApplyInfo);
            if (mSelectedFriendApplyInfos.contains(friendApplyInfo)) {
                mSelectedFriendApplyInfos.remove(friendApplyInfo);
            } else {
                mSelectedFriendApplyInfos.add(friendApplyInfo);
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        showDeleteDialog(mFriendApplyInfos.get(position));

        return true;
    }

    private void showDeleteDialog(final FriendApplyInfo friendApplyInfo) {
        mDeleteDialog = new DeleteDialog(this, R.style.round_corner_dialog,
                getString(R.string.social_define_delete_friends_apply), new DeleteDialog.MyDialogListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_confirm_collect:
                        mDeleteDialog.dismiss();
                        mFriendApplyInfos.remove(friendApplyInfo);
                        mAdapter.notifyDataSetChanged();
                        updateHint();
                        FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(FriendApplyListActivity.this);
                        friendApplyDao.delete(friendApplyInfo.userAccount,
                                SharedPreferencesInfo.getTagString(FriendApplyListActivity.this, SharedPreferencesInfo.ACCOUNT));
                        sendBroadcast(new Intent(AppConstants.ACTION_UPDATE_MSG));
                        break;
                    case R.id.tv_cancel_collect:
                        mDeleteDialog.dismiss();
                        break;
                }
            }
        });
        mDeleteDialog.show();
    }

    private void updateHint() {
        if (mFriendApplyInfos.size() > 0) {
            mLlHint.setVisibility(View.GONE);
        } else {
            mLlHint.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FRIEND_REPLY_REQUEST) {
                FriendApplyDao friendApplyDao = FriendApplyDao.getInstance(this);
                mFriendApplyInfos.clear();
                mFriendApplyInfos.addAll(friendApplyDao.queryList(SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT)));
                mAdapter.notifyDataSetChanged();
                String type = data.getStringExtra("reply");
                if ("1".equals(type) || "2".equals(type)) {
                    sendBroadcast(new Intent(AppConstants.ACTION_UPDATE_MSG));
                }
            }
        }
    }
}
