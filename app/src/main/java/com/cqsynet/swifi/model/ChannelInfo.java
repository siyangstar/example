/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：新闻频道的数据类。
 *
 *
 * 创建标识：luchaowei 20141008
 */
package com.cqsynet.swifi.model;

public class ChannelInfo {

    public String id; // 频道id，用于区分不同频道
    public String name; // 频道的名称
    public String type; // 频道类型 0为首页，1 为合作类型，2为系统类型，用户可以进行频道管理操作
    public String url; // 点击跳转 url
    public String label; //标签,0:普通, 1:新增

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
}