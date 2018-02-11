/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：“未添加频道”gridView对应的Adapter。
 *
 *
 * 创建标识：luchaowei 20141008
 */
package com.cqsynet.swifi.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.ChannelInfo;

public class OtherChannelAdapter extends BaseAdapter {
    private Context mContext;
    private List<ChannelInfo> mChannelList;
    private TextView mTvItemText;
    public boolean mIsVisible = true;

    public OtherChannelAdapter(Context context, List<ChannelInfo> channelList) {
        this.mContext = context;
        this.mChannelList = channelList;
    }

    /**
     * 
     * @Description:设置adapter数据
     * @param myChannels   需要显示的数据
     * @return: void
     */
    public void setData(List<ChannelInfo> otherChannels) {
        mChannelList = otherChannels;
    }

    @Override
    public int getCount() {
        return mChannelList == null ? 0 : mChannelList.size();
    }

    @Override
    public ChannelInfo getItem(int position) {
        if (mChannelList != null && mChannelList.size() != 0) {
            return mChannelList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.news_channel_grid_item, null);
        mTvItemText = view.findViewById(R.id.tvChannelName_grid_item);
        ImageView ivLabel = view.findViewById(R.id.ivLabel_grid_item);
        ChannelInfo channel = getItem(position);
        mTvItemText.setText(channel.getName());
        if (!mIsVisible && (position == -1 + mChannelList.size())) {
            mTvItemText.setText("");
            mTvItemText.setVisibility(View.INVISIBLE);
        }
		// 新增栏目
        String label = getItem(position).label;
		if (!TextUtils.isEmpty(label) && label.equals("1")) {
			ivLabel.setVisibility(View.VISIBLE);
			ivLabel.setImageResource(R.drawable.channel_new);
		} else {
			ivLabel.setImageDrawable(null);
			ivLabel.setVisibility(View.GONE);
		}
        return view;
    }

}