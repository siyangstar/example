/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：个性签名
 *
 *
 * 创建标识：zhaosy 20161109
 */
package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.view.TitleBar;

public class UserSignActivity extends HkActivity implements OnClickListener {

    private TitleBar mTitleBar;
    private EditText mEtContent;
    private TextView mTvCount;
    private String mTitleStr;
    private String mValueStr;
    private int mCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mTitleStr = getIntent().getStringExtra("title");
        mValueStr = getIntent().getStringExtra("value");
        setContentView(R.layout.activity_user_sign);
        mTitleBar = findViewById(R.id.titlebar_activity_user_sign);
        mTitleBar.setTitle(mTitleStr);
        mTitleBar.setLeftIconClickListener(this);
        mTvCount = findViewById(R.id.tvCount_activity_user_sign);
        mEtContent = findViewById(R.id.etContent_activity_user_sign);
        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mCount = editable.length();
                mTvCount.setText(mCount + "/50");
            }
        });
        mEtContent.setText(mValueStr);
        mEtContent.setSelection(mEtContent.getText().length());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ivBack_titlebar_layout) { // 返回
            Intent intent = new Intent();
            intent.putExtra("data", mEtContent.getText().toString().trim());
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("data", mEtContent.getText().toString().trim());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
