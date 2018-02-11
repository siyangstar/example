/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：新闻频道管理对应的Activity。
 *
 *
 * 创建标识：luchaowei 20141008
 */
package com.cqsynet.swifi.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.adapter.DragAdapter;
import com.cqsynet.swifi.adapter.OtherChannelAdapter;
import com.cqsynet.swifi.model.ChannelInfo;
import com.cqsynet.swifi.model.ChannelListResponseBody;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.ResponseObject;
import com.cqsynet.swifi.model.SetChannelListRequestBody;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.DragGridView;
import com.cqsynet.swifi.view.TitleBar;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;

public class ChannelManagerActivity extends HkActivity  implements OnClickListener, OnItemClickListener {
	
    private DragGridView mGvMine; // 我关注的频道
    private GridView mGvOther; // 等待添加的频道
    private DragAdapter mMyChannelAdapter; // 我关注的频道adapter
    private OtherChannelAdapter mOtherChannelAdapter; // 待添加的频道adapter
//    private ArrayList<ChannelInfo> mFixedChannelList = new ArrayList<ChannelInfo>(); // 固定频道，不可编辑
    private ArrayList<ChannelInfo> mMyChannelList = new ArrayList<ChannelInfo>(); // 我关注的频道列表
    private ArrayList<ChannelInfo> mOtherChannelList = new ArrayList<ChannelInfo>(); // 待添加的频道列表
    private boolean isMove = false; // 频道Item 是否正在做Animation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);        
        setContentView(R.layout.activity_channel_manager);
        
        TitleBar titleBar = findViewById(R.id.titlebar_channel_manager_activity);
        titleBar.setLeftIconClickListener(this);
        mGvMine = findViewById(R.id.gvMine_channel_manager);
        mGvOther = findViewById(R.id.gvOther_channel_manager);
        // 从Shareinfo里面拿到之前保存的Json格式的频道列表字符串
        String channelInfo = SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.CHANNELS);
        if (!TextUtils.isEmpty(channelInfo)) {
            Gson gson = new Gson();
            // 将Json格式的频道列表数据反序列化
            ChannelListResponseBody channels = gson.fromJson(channelInfo, ChannelListResponseBody.class);
            mMyChannelList = channels.add;
            mOtherChannelList = channels.notAdd;
        }
        int fixedCount = 0;
        for(ChannelInfo info : mMyChannelList) {
        	if(!info.type.equals("2")) {
        		fixedCount++;
        	}
        }
        mMyChannelAdapter = new DragAdapter(this, mMyChannelList);
        mGvMine.setAdapter(mMyChannelAdapter);
        mGvMine.mFixedCount = fixedCount;
        mOtherChannelAdapter = new OtherChannelAdapter(this, mOtherChannelList);
        mGvOther.setAdapter(mOtherChannelAdapter);
        mGvOther.setOnItemClickListener(this);
        mGvMine.setOnItemClickListener(this);
        
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
        if (isMove) {
            return;
        }
        switch (parent.getId()) {
            case R.id.gvMine_channel_manager :
                final ChannelInfo myChannel = (ChannelInfo) mMyChannelAdapter.getItem(position);
                if ("2".equals(myChannel.getType())) {
                    final ImageView moveImageView = getView(view);
                    if (moveImageView != null) {
                        TextView newTextView = view.findViewById(R.id.tvChannelName_grid_item);
                        final int[] startLocation = new int[2];
                        newTextView.getLocationInWindow(startLocation);
                        mOtherChannelAdapter.mIsVisible = false;
                        mOtherChannelList.add(myChannel);
                        mOtherChannelAdapter.notifyDataSetChanged();
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                try {
                                    int[] endLocation = new int[2];
                                    mGvOther.getChildAt(mGvOther.getLastVisiblePosition()).getLocationInWindow(endLocation);
                                    MoveAnim(moveImageView, startLocation, endLocation, myChannel, mGvMine);
                                } catch (Exception localException) {
                                }
                            }
                        }, 50L);
                    }
                }
                break;
            case R.id.gvOther_channel_manager :
            	final ChannelInfo otherChannel = mOtherChannelAdapter.getItem(position);
                final ImageView moveImageView = getView(view);
                if (moveImageView != null) {
                    TextView newTextView = view.findViewById(R.id.tvChannelName_grid_item);
                    final int[] startLocation = new int[2];
                    newTextView.getLocationInWindow(startLocation);
                    mMyChannelAdapter.mIsVisible = false;
                    mMyChannelList.add(otherChannel);
                    mMyChannelAdapter.notifyDataSetChanged();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            try {
                                int[] endLocation = new int[2];
                                mGvMine.getChildAt(mGvMine.getLastVisiblePosition()).getLocationInWindow(endLocation);
                                MoveAnim(moveImageView, startLocation, endLocation, otherChannel, mGvOther);
                            } catch (Exception localException) {
                            }
                        }
                    }, 50L);
                }
                break;
            default :
                break;
        }
    }

    /**
     * 设置新闻频道图标动画。
     * 
     * @param moveView
     * @param startLocation
     * @param endLocation
     * @param moveChannel
     * @param clickGridView
     */
    private void MoveAnim(View moveView, int[] startLocation, int[] endLocation, final ChannelInfo moveChannel, final GridView clickGridView) {
        int[] initLocation = new int[2];
        moveView.getLocationInWindow(initLocation);
        final ViewGroup moveViewGroup = getMoveViewGroup();
        final View mMoveView = getMoveView(moveViewGroup, moveView, initLocation);
        TranslateAnimation moveAnimation = new TranslateAnimation(startLocation[0], endLocation[0], startLocation[1], endLocation[1]);
        moveAnimation.setDuration(300L);
        AnimationSet moveAnimationSet = new AnimationSet(true);
        moveAnimationSet.setFillAfter(false);
        moveAnimationSet.addAnimation(moveAnimation);
        mMoveView.startAnimation(moveAnimationSet);
        moveAnimationSet.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                isMove = true;
                if (clickGridView instanceof DragGridView) {
                	mMyChannelList.remove(moveChannel);
                	mMyChannelAdapter.notifyDataSetChanged();
                } else {
                	mOtherChannelList.remove(moveChannel);
                	mOtherChannelAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                moveViewGroup.removeView(mMoveView);
                if (clickGridView instanceof DragGridView) {
                    mOtherChannelAdapter.mIsVisible = true;
                    mOtherChannelAdapter.notifyDataSetChanged();
                } else {
                	mMyChannelAdapter.mIsVisible = true;
                    mMyChannelAdapter.notifyDataSetChanged();
                }
                isMove = false;
            }
        });
    }

    /**
     * @param viewGroup
     * @param view
     * @param initLocation
     * @return
     */
    private View getMoveView(ViewGroup viewGroup, View view, int[] initLocation) {
        int x = initLocation[0];
        int y = initLocation[1];
        viewGroup.addView(view);
        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mLayoutParams.leftMargin = x;
        mLayoutParams.topMargin = y;
        view.setLayoutParams(mLayoutParams);
        return view;
    }

    private ViewGroup getMoveViewGroup() {
        ViewGroup moveViewGroup = (ViewGroup) getWindow().getDecorView();
        LinearLayout moveLinearLayout = new LinearLayout(this);
        moveLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        moveViewGroup.addView(moveLinearLayout);
        return moveLinearLayout;
    }

    private ImageView getView(View view) {
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        Bitmap cache = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(cache);
        return iv;
    } 


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ivBack_titlebar_layout) {
            saveNewsChannels();
            finish();
        }
    }

    /**
     * 保存新闻列表
     */
    private void saveNewsChannels() {
        if (mMyChannelList != null) {
            Gson gson = new Gson();
            ChannelListResponseBody channelBody = new ChannelListResponseBody();
            channelBody.add = mMyChannelList;
            channelBody.notAdd = mOtherChannelList;
            
            //把所有新增栏目变成普通
            Iterator<ChannelInfo> it = channelBody.add.iterator();
            while(it.hasNext()) {
            	ChannelInfo info = it.next();
            	if(!TextUtils.isEmpty(info.label) && info.label.equals("1")) {
            		info.label = "0";
            	} 
            }
            
            // 序列化得到当前频道列表的Json字符串
            String channels = gson.toJson(channelBody);
            // 拿到Shareinfo里保存的Json格式的频道列表
            String savedChannels = SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.CHANNELS);
            // 如果不相等，就保存一次
            if (!savedChannels.equals(channels)) {
                SharedPreferencesInfo.setTagString(this, SharedPreferencesInfo.CHANNELS, channels);
                Intent result = new Intent();
                result.putExtra("channels", channels);
                setResult(11, result); // 自定义返回码为11
                // 仅提交用户可编辑频道
                setNewsChannel(this, mMyChannelList);
            }
        }
    }


    /**
     * 上传频道列表到服务器
     */
    public static void setNewsChannel(final Context ctx, ArrayList<ChannelInfo> channelList) {
        SetChannelListRequestBody requestBody = new SetChannelListRequestBody();
        // 仅上传用户已关注的频道列表到服务器
        ArrayList<String> list = new ArrayList<String>();
        for (ChannelInfo info : channelList) {
            list.add(info.getId());
        }
        requestBody.add = list;

        IResponseCallback callbackIf = new IResponseCallback() {
            @Override
            public void onResponse(String response) {
                try {
                    Gson gson = new Gson();
                    ResponseObject responseObject = gson.fromJson(response, ResponseObject.class);
                    ResponseHeader header = responseObject.header;
                    if (AppConstants.RET_OK.equals(header.ret)) {
                        SharedPreferencesInfo.setTagBoolean(ctx, SharedPreferencesInfo.SAVE_CHANNEL_FAIL, false);
                    } else {
                        ToastUtil.showToast(ctx, R.string.set_channel_list_fail);
                    }
                } catch (Exception e) {
                    ToastUtil.showToast(ctx, R.string.request_fail_warning);
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(ctx, R.string.request_fail_warning);
                SharedPreferencesInfo.setTagBoolean(ctx, SharedPreferencesInfo.SAVE_CHANNEL_FAIL, true);
            }
        };
        // 调用接口发送频道列表到服务器
        WebServiceIf.setNewsChannels(ctx, requestBody, callbackIf);
    }

    @Override
    public void onBackPressed() {
        saveNewsChannels();
        finish();
    }
}
