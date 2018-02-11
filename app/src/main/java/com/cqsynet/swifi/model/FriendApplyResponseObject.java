package com.cqsynet.swifi.model;

import java.util.ArrayList;

/**
 * Author: sayaki
 * Date: 2017/11/29
 */
public class FriendApplyResponseObject extends BaseResponseObject {

    public FriendApplyResponseBody body;

    public class FriendApplyResponseBody {
        public ArrayList<FriendApplyInfo> friendApplyInfos;
    }
}
