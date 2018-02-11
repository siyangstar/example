package com.cqsynet.swifi.model;

import java.util.ArrayList;

/**
 * Author: Arturia
 * Date: 2017/12/18
 */
public class FindPersonResponseObject extends BaseResponseObject {

    public FindPersonResponseBody body;

    public class FindPersonResponseBody {
        public String location; // 用户所在地; 按线路查找时显示线路名，按站台查找时显示站台名
        public ArrayList<FindPersonInfo> userList;
    }
}
