package com.cqsynet.swifi.activity.social;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.db.ContactDao;
import com.cqsynet.swifi.db.FriendsDao;
import com.cqsynet.swifi.model.FriendsInfo;
import com.cqsynet.swifi.model.FriendsRequestBody;
import com.cqsynet.swifi.model.FriendsResponseObject;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.UserInfo;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.view.SideIndexBar;
import com.github.promeg.pinyinhelper.Pinyin;
import com.google.gson.Gson;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/11/23
 */
public class FriendsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView mListFriends;
    private LinearLayout mLlHint;
    private SideIndexBar mSideIndexBar;

    private FriendsAdapter mAdapter;
    private List<FriendsInfo> mFriends = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        mListFriends = view.findViewById(R.id.list_friends);
        mLlHint = view.findViewById(R.id.ll_hint);
        mAdapter = new FriendsAdapter(getActivity(), mFriends);
        mListFriends.setAdapter(mAdapter);
        mListFriends.setOnItemClickListener(this);
        mSideIndexBar = view.findViewById(R.id.index_bar);
        mSideIndexBar.setLetterChangedListener(new SideIndexBar.OnLetterChangedListener() {
            @Override
            public void onChanged(String s, int position) {
                scrollToPosition(s);
            }
        });

        return view;
    }

    private void scrollToPosition(String s) {
        int position = 0;
        for (int i = 0; i < mFriends.size(); i++) {
            String name;
            if (!TextUtils.isEmpty(mFriends.get(i).remark)) {
                name = Pinyin.toPinyin(mFriends.get(i).remark.toCharArray()[0]);
            } else {
                name = Pinyin.toPinyin(mFriends.get(i).nickname.toCharArray()[0]);
            }
            if (s.equals(name)) {
                position = i;
                break;
            }
        }
        mListFriends.setSelection(position);
    }

    @Override
    public void onStart() {
        super.onStart();
        getFriends();
    }

    private void getFriends() {
        FriendsRequestBody body = new FriendsRequestBody();
        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                if (!TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    FriendsResponseObject object = gson.fromJson(response, FriendsResponseObject.class);
                    ResponseHeader header = object.header;
                    if (AppConstants.RET_OK.equals(header.ret)) {
                        mFriends.clear();
                        mFriends.addAll(object.body.userList);

                        Collections.sort(mFriends, new FriendsComparator());

                        mAdapter.notifyDataSetChanged();
                        updateHint();
                    } else {
                        getFriendsFromDB();
                    }
                } else {
                    getFriendsFromDB();
                }
            }

            @Override
            public void onErrorResponse() {
                getFriendsFromDB();
            }
        };
        WebServiceIf.getFriends(getActivity(), body, callback);
    }

    private void getFriendsFromDB() {
        FriendsDao friendsDao = FriendsDao.getInstance(getActivity());
        List<FriendsInfo> friendsInfos = friendsDao.queryList(SharedPreferencesInfo.getTagString(getActivity(), SharedPreferencesInfo.ACCOUNT));
        if (friendsInfos != null && friendsInfos.size() > 0) {
            ContactDao contactDao = ContactDao.getInstance(getActivity());
            for (FriendsInfo friendsInfo : friendsInfos) {
                UserInfo userInfo = contactDao.queryUser(friendsInfo.userAccount);
                if (userInfo != null) {
                    friendsInfo.nickname = userInfo.nickname;
                    friendsInfo.headUrl = userInfo.headUrl;
                    friendsInfo.sex = userInfo.sex;
                    mFriends.add(friendsInfo);
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private void updateHint() {
        if (mFriends.size() == 0) {
            mListFriends.setVisibility(View.GONE);
            mLlHint.setVisibility(View.VISIBLE);
        } else {
            mListFriends.setVisibility(View.VISIBLE);
            mLlHint.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), PersonInfoActivity.class);
        intent.putExtra("friendAccount", mFriends.get(position).userAccount);
        intent.putExtra("isFriend", "1");
        startActivity(intent);
    }

    private class FriendsComparator implements Comparator<FriendsInfo> {
        @Override
        public int compare(FriendsInfo o1, FriendsInfo o2) {
            if (!TextUtils.isEmpty(o1.remark)) {
                if (!TextUtils.isEmpty(o2.remark)) {
                    return Pinyin.toPinyin(o1.remark.toCharArray()[0]).compareTo(Pinyin.toPinyin(o2.remark.toCharArray()[0]));
                } else {
                    return Pinyin.toPinyin(o1.remark.toCharArray()[0]).compareTo(Pinyin.toPinyin(o2.nickname.toCharArray()[0]));
                }
            } else {
                if (!TextUtils.isEmpty(o2.remark)) {
                    return Pinyin.toPinyin(o1.nickname.toCharArray()[0]).compareTo(Pinyin.toPinyin(o2.remark.toCharArray()[0]));
                } else {
                    return Pinyin.toPinyin(o1.nickname.toCharArray()[0]).compareTo(Pinyin.toPinyin(o2.nickname.toCharArray()[0]));
                }
            }
        }
    }
}
