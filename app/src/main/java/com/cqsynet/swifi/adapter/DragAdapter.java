/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：可以拖动的GridView的适配器
 *
 *
 * 创建标识：zhaosy 20150514
 */
package com.cqsynet.swifi.adapter;

import java.util.ArrayList;
import java.util.Collections;

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

public class DragAdapter extends BaseAdapter {
	private ArrayList<ChannelInfo> mList;
	private LayoutInflater mInflater;
	private int mHidePosition = -1;
	public boolean mIsVisible = true;  //用于上下两个GridView切换动画
	
	public DragAdapter(Context context, ArrayList<ChannelInfo> list){
		this.mList = list;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(R.layout.news_channel_grid_item, null);
		ImageView ivLabel = convertView.findViewById(R.id.ivLabel_grid_item);
		TextView textView = convertView.findViewById(R.id.tvChannelName_grid_item);
		textView.setText(mList.get(position).name);
		
		//固定不可拖动的位置时
		if(!mList.get(position).type.equals("2")) {
			textView.setBackgroundResource(R.drawable.channel_item_fixed);
			textView.setTextColor(0xFFFFFFFF);
		}
		
		//新增栏目
		String label = mList.get(position).label;
		if(!TextUtils.isEmpty(label) && label.equals("1")) {
			ivLabel.setVisibility(View.VISIBLE);
			ivLabel.setImageResource(R.drawable.channel_new);
		} else {
			ivLabel.setImageDrawable(null);
			ivLabel.setVisibility(View.GONE);
		}
		
		//用于动画时隐藏
		if(!mIsVisible && position == getCount() - 1) {
			convertView.setVisibility(View.INVISIBLE);
		}
		
		if(position == mHidePosition){
			convertView.setVisibility(View.INVISIBLE);
		}
		
		return convertView;
	}
	
	public void reorderItems(int oldPosition, int newPosition) {
		ChannelInfo info = mList.get(oldPosition);
		if(oldPosition < newPosition){
			for(int i=oldPosition; i<newPosition; i++){
				Collections.swap(mList, i, i+1);
			}
		}else if(oldPosition > newPosition){
			for(int i=oldPosition; i>newPosition; i--){
				Collections.swap(mList, i, i-1);
			}
		}
		mList.set(newPosition, info);
	}

	public void setHideItem(int hidePosition) {
		this.mHidePosition = hidePosition; 
		notifyDataSetChanged();
	}


}
