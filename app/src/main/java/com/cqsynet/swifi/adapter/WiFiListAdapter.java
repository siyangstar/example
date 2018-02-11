/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：附近WiFi列表数据适配器
 *
 *
 * 创建标识：duxl 20140924
 */
package com.cqsynet.swifi.adapter;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.WiFiObject;

import java.util.List;

public class WiFiListAdapter extends BaseAdapter {

	private Context mContext;
	private List<WiFiObject> mListData;
	private int[] mWiFiLevelLockRes = { // 未连接需要密码的WiFi信号强度的图标
			R.drawable.wifi_lock_0,
			R.drawable.wifi_lock_1,
			R.drawable.wifi_lock_2,
			R.drawable.wifi_lock_3,
			R.drawable.wifi_lock_4
		};

	private int[] mWiFiLevelUnLockRes = { // 未连接不需要密码的WiFi信号强度的图标
		R.drawable.wifi_signal0,
		R.drawable.wifi_signal1,
		R.drawable.wifi_signal2,
		R.drawable.wifi_signal3,
		R.drawable.wifi_signal4
	};
	
	private int[] mWiFiLevelConnectRes = {  // 已连接WiFi信号强度的图标
			R.drawable.wifi_0,
			R.drawable.wifi_1,
			R.drawable.wifi_2,
			R.drawable.wifi_3,
			R.drawable.wifi_4
		};
	
	public WiFiListAdapter(Context cxt, List<WiFiObject> data) {
		super();
		this.mContext = cxt;
		this.mListData = data;
	}

	@Override
	public int getCount() {
		return mListData == null ? 0 : mListData.size();
	}

	@Override
	public WiFiObject getItem(int position) {
		return mListData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HolderView holderView = null;
		if(convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_wifilist_itemlayout, null);
			holderView = new HolderView();
			holderView.tvName = convertView.findViewById(R.id.tvWifiName_actvity_wifilist_itemlayout);
			holderView.tvSWiFi = convertView.findViewById(R.id.tvSWifi_actvity_wifilist_itemlayout);
			holderView.ivSWiFi = convertView.findViewById(R.id.ivJoinIcon_actvity_wifilist_itemlayout);
			holderView.tvDesc = convertView.findViewById(R.id.tvDesc_actvity_wifilist_itemlayout);
			holderView.ivWiFiSignal = convertView.findViewById(R.id.ivWiFiSignal_actvity_wifilist_itemlayout);
			convertView.setTag(holderView);
		} else {
			holderView = (HolderView) convertView.getTag();
		}
		
		WiFiObject wiFiObj = getItem(position);
		
		holderView.ivWiFiSignal.setVisibility(View.VISIBLE);
		holderView.tvName.setText(wiFiObj.ssid);
		if(wiFiObj.status == WifiConfiguration.Status.CURRENT) {
			holderView.tvDesc.setText("已连接");
		} else if(wiFiObj.ss == -99999) {
			holderView.tvDesc.setText("不在范围内");
			holderView.ivWiFiSignal.setVisibility(View.INVISIBLE);			
		} else if(TextUtils.isEmpty(wiFiObj.capabilities) || "[ESS]".equals(wiFiObj.capabilities)) {
			holderView.tvDesc.setText("开放（WPS可用）");			
		} else {
			if(wiFiObj.networkId != -99999) {
				holderView.tvDesc.setText("已记住 通过WPA/WPA2 PSK 进行保护");
			} else {
				holderView.tvDesc.setText("通过WPA/WPA2 PSK 进行保护");
			}
		}
		
		// 设置信号图标
		int[] tempWiFiLevelRes = null;
		if(wiFiObj.status == WifiConfiguration.Status.CURRENT) { // 已连接
			tempWiFiLevelRes = mWiFiLevelConnectRes;
		} else if(TextUtils.isEmpty(getItem(position).capabilities) || getItem(position).capabilities.equals("[ESS]")) { // 不需要密码
			tempWiFiLevelRes = mWiFiLevelUnLockRes;			
		} else { // 需要密码
			tempWiFiLevelRes = mWiFiLevelLockRes;
		}
		
		int level = getItem(position).ss;
		if(level != -99999) {
			int resId = tempWiFiLevelRes[WifiManager.calculateSignalLevel(level, mWiFiLevelLockRes.length)];
			holderView.ivWiFiSignal.setImageResource(resId);
		} else {
			holderView.ivWiFiSignal.setImageResource(tempWiFiLevelRes[0]);
		}
		
		// 是否是轻轨免费WiFi
		if(wiFiObj.ssid.startsWith(AppConstants.WIFI_SSID)) { // 是
			holderView.tvSWiFi.setVisibility(View.VISIBLE);
			holderView.ivSWiFi.setImageResource(R.drawable.join_icon_free);
			holderView.ivWiFiSignal.setImageResource(R.drawable.free_wifi_icon);
		} else {
			holderView.tvSWiFi.setVisibility(View.GONE);
			holderView.ivSWiFi.setImageResource(R.drawable.join_icon);
		}
		
		return convertView;
	}

	private final class HolderView {
		public TextView tvName; // WiFi名称
		public ImageView tvSWiFi; // 轨道免费WiFi
		public ImageView ivSWiFi; // 轨道免费WiFi
		public TextView tvDesc; // 描述
		public ImageView ivWiFiSignal; // 信号强度
	}
}
