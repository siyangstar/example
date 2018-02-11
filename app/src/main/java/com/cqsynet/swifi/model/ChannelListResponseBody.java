/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：获取频道列表的响应对象。
 *
 *
 * 创建标识：luchaowei 20141215
 */
package com.cqsynet.swifi.model;

import java.util.ArrayList;

public class ChannelListResponseBody {
    // 用户或系统已添加的频道列表
    public ArrayList<ChannelInfo> add;
    // 用户未添加的频道列表
    public ArrayList<ChannelInfo> notAdd;
}
