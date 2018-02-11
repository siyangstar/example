package com.cqsynet.swifi.activity.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.HkActivity;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.model.ModifyFriendRemarkRequestBody;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.network.WebServiceIf;

import org.json.JSONException;

/**
 * Author: sayaki
 * Date: 2017/12/5
 */
public class RemarkActivity extends HkActivity {

    private TextView mTvNickname;
    private EditText mEtRemark;

    private String mFriendAccount;
    private String mNickname;
    private String mRemark;
    private String mAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remark);

        mFriendAccount = getIntent().getStringExtra("friendAccount");
        mNickname = getIntent().getStringExtra("nickname");
        mRemark = getIntent().getStringExtra("remark");
        mAction = getIntent().getStringExtra("action");

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTvNickname = findViewById(R.id.tv_nickname);
        if (!TextUtils.isEmpty(mNickname)) {
            mTvNickname.setText(mNickname);
        }
        mEtRemark = findViewById(R.id.et_remark);
        if (!TextUtils.isEmpty(mRemark)) {
            mEtRemark.setText(mRemark);
            mEtRemark.setSelection(mRemark.length());
        }
        TextView tvConfirm = findViewById(R.id.tv_confirm);
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("modify".equals(mAction)) {
                    modifyFriendRemark();
                } else if ("set".equals(mAction)) {
                    setFriendRemark();
                }
            }
        });
    }

    private void setFriendRemark() {
        Intent intent = new Intent();
        intent.putExtra("remark", mEtRemark.getText().toString());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void modifyFriendRemark() {
        ModifyFriendRemarkRequestBody body = new ModifyFriendRemarkRequestBody();
        body.friendAccount = mFriendAccount;
        body.remark = mEtRemark.getText().toString();
        WebServiceIf.modifyFriendRemark(this, body, new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                Log.i("RemarkActivity", "@@@#@response: " + response);
                ContactDao contactDao = ContactDao.getInstance(RemarkActivity.this);
                UserInfo userInfo = contactDao.queryUser(mFriendAccount);
                if (userInfo != null) {
                    userInfo.remark = mEtRemark.getText().toString();
                    contactDao.saveUser(userInfo);
                }

                sendBroadcast(new Intent(AppConstants.ACTION_UPDATE_MSG));

                Intent intent = new Intent();
                intent.putExtra("remark", mEtRemark.getText().toString());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

            @Override
            public void onErrorResponse() {

            }
        });
    }
}
