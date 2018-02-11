/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：个人中心文本输入页面
 *
 *
 * 创建标识：duxl 20141222
 */
package com.cqsynet.swifi.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.LoginInputField;
import com.cqsynet.swifi.view.TitleBar;

/**
 * 个人中心文本输入页面
 * 
 * @param title - String 标题
 * @param value - String 原有文本
 * 
 * @return data - String 返回文本
 * @author duxl
 *
 */
public class UserCenterInputActivity extends HkActivity implements OnClickListener {
	
	private TitleBar mTitleBar;
	private EditText mEtValue;
	private TextView mTvHint;
	private String mTitleStr;
	private String mValueStr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mTitleStr = getIntent().getStringExtra("title");
		mValueStr = getIntent().getStringExtra("value");
		setContentView(R.layout.activity_user_center_input);
		mTitleBar = findViewById(R.id.titlebar_activity_user_center_input);
		mTitleBar.setTitle(mTitleStr + "修改");
		mTitleBar.setLeftIconClickListener(this);
		mTitleBar.setRightIconClickListener(this);
		LoginInputField loginInputField = findViewById(R.id.etValue_activity_user_center_input);
		mEtValue = loginInputField.getEditText();
        mEtValue.setHint("请输入" + mTitleStr);
		mEtValue.setText(mValueStr);
		mEtValue.setSelection(mEtValue.getText().length());
		mTvHint = findViewById(R.id.tv_hint);
		mTvHint.setText("点击当前" + mTitleStr + "，进行修改");
	}

	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.ivBack_titlebar_layout) { // 返回
			finish();
		} else if(v.getId() == R.id.ivMenu_titlebar_layout) { // 确定
			if(mEtValue.getText().toString().trim().length() == 0) {
				ToastUtil.showToast(this, "不能为空");
			} else {
				Intent intent = new Intent();
				intent.putExtra("data", mEtValue.getText().toString().trim());
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		}
	}

}
