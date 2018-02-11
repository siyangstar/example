/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：评论响应类
 *
 *
 * 创建标识：sayaki 20170831
 */
package com.cqsynet.swifi.model;

import java.util.ArrayList;

/**
 * Author: sayaki
 * Date: 2017/8/31
 */
public class CommentResponseObject extends BaseResponseObject {

    public CommentResponseBody body;

    public class CommentResponseBody {
        public ArrayList<CommentInfo> hotComment;
        public ArrayList<CommentInfo> newComment;
    }
}
