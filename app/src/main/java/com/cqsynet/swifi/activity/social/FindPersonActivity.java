package com.cqsynet.swifi.activity.social;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.HkActivity;
import com.cqsynet.swifi.model.FindPersonInfo;
import com.cqsynet.swifi.model.FindPersonRequestBody;
import com.cqsynet.swifi.model.FindPersonResponseObject;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.NetworkUtil;
import com.cqsynet.swifi.view.FilterDialog;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/11/23
 */
public class FindPersonActivity extends HkActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener {

    public static final String TYPE_TRAIN = "0";
    public static final String TYPE_STATION = "1";
    public static final String TYPE_LINE = "2";
    public static final String TYPE_NEARBY = "3";
    public static final String ACTION_REFRESH = "0";
    public static final String ACTION_LOAD_MORE = "1";

    private ImageView mIvBack;
    private TextView mTvCategory;
    private TextView mTvLocation;
    private TextView mTvFilterPerson;
    private FrameLayout mLayoutCategory;
    private LinearLayout mLlCategory;
    private TextView mTvTrain;
    private TextView mTvLine;
    private TextView mTvStation;
    private TextView mTvNearby;
    private ImageView mIvClose;
    private PullToRefreshListView mListView;
    private LinearLayout mLlHint;
    private ImageView mIvHint;
    private TextView mTvHint;
    private ProgressBar mLoadingBar;
    private FilterDialog mFilterDialog;

    private long mFreshTime = 0;
    // 是否有下一页
    private boolean mHasMore = true;

    private String mLocation;
    private String mType = TYPE_TRAIN;
    private String mAction = ACTION_REFRESH;
    private String mAge = "不限";
    private String mSex = "不限";
    private String[] mFindCategoryArray;

    private FindPersonAdapter mAdapter;
    private List<FindPersonInfo> mPersonList = new ArrayList<>();

    public static void launch(Context context, String type) {
        Intent intent = new Intent();
        intent.setClass(context, FindPersonActivity.class);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_person);

        mIvBack = findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(this);
        mTvCategory = findViewById(R.id.tv_category);
        mTvCategory.setOnClickListener(this);
        mTvLocation = findViewById(R.id.tv_location);
        mTvLocation.setOnClickListener(this);
        mTvFilterPerson = findViewById(R.id.tv_filter_person);
        mTvFilterPerson.setOnClickListener(this);
        mLayoutCategory = findViewById(R.id.layout_category);
        mLlCategory = findViewById(R.id.ll_category);
        mTvTrain = findViewById(R.id.tv_train);
        mTvTrain.setOnClickListener(this);
        mTvLine = findViewById(R.id.tv_line);
        mTvLine.setOnClickListener(this);
        mTvStation = findViewById(R.id.tv_station);
        mTvStation.setOnClickListener(this);
        mTvNearby = findViewById(R.id.tv_nearby);
        mTvNearby.setOnClickListener(this);
        mIvClose = findViewById(R.id.iv_close);
        mIvClose.setOnClickListener(this);
        mListView = findViewById(R.id.list_view);
        mAdapter = new FindPersonAdapter(this, mPersonList);
        mListView.setAdapter(mAdapter);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                mAction = ACTION_REFRESH;
                findPerson(ACTION_REFRESH);
            }
        });
        mListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                if (mHasMore) {
                    mAction = ACTION_LOAD_MORE;
                    findPerson(ACTION_LOAD_MORE);
                    mLoadingBar.setVisibility(View.VISIBLE);
                }
            }
        });
        mFreshTime = System.currentTimeMillis();
        mListView.getLoadingLayoutProxy().setLastUpdatedLabel("更新于:" + DateUtil.getRelativeTimeSpanString(mFreshTime));
        mListView.setOnItemClickListener(this);
        mLlHint = findViewById(R.id.ll_hint);
        mLlHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAction = ACTION_REFRESH;
                findPerson(ACTION_REFRESH);
                mLoadingBar.setVisibility(View.VISIBLE);
            }
        });
        mIvHint = findViewById(R.id.iv_hint);
        mTvHint = findViewById(R.id.tv_hint);
        mLoadingBar = findViewById(R.id.loading_bar);

        mFindCategoryArray = getResources().getStringArray(R.array.find_category);

        mType = getIntent().getStringExtra("type");
        findPerson(ACTION_REFRESH);
        mLoadingBar.setVisibility(View.VISIBLE);

        mTvCategory.setText(mFindCategoryArray[Integer.parseInt(mType)]);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_category:
                openCategoryLayout();
                break;
            case R.id.tv_location:
                openCategoryLayout();
                break;
            case R.id.tv_filter_person:
                showFilterDialog();
                break;
            case R.id.tv_train:
                mTvCategory.setText(mFindCategoryArray[0]);
                mType = TYPE_TRAIN;
                findPerson(ACTION_REFRESH);
                mLoadingBar.setVisibility(View.VISIBLE);
                closeCategoryLayout();
                break;
            case R.id.tv_station:
                mTvCategory.setText(mFindCategoryArray[1]);
                mType = TYPE_STATION;
                findPerson(ACTION_REFRESH);
                mLoadingBar.setVisibility(View.VISIBLE);
                closeCategoryLayout();
                break;
            case R.id.tv_line:
                mTvCategory.setText(mFindCategoryArray[2]);
                mType = TYPE_LINE;
                findPerson(ACTION_REFRESH);
                mLoadingBar.setVisibility(View.VISIBLE);
                closeCategoryLayout();
                break;
            case R.id.tv_nearby:
                mTvCategory.setText(mFindCategoryArray[3]);
                mType = TYPE_NEARBY;
                findPerson(ACTION_REFRESH);
                mLoadingBar.setVisibility(View.VISIBLE);
                closeCategoryLayout();
                break;
            case R.id.iv_close:
                closeCategoryLayout();
                break;
        }
    }

    private void openCategoryLayout() {
        mLayoutCategory.setVisibility(View.VISIBLE);
        ObjectAnimator springInAnimator = ObjectAnimator.ofFloat(mLlCategory, "translationY", -1800, 0);
        springInAnimator.setDuration(500);
        springInAnimator.setInterpolator(new SpringInterpolator(0.7f));
        springInAnimator.start();
        ObjectAnimator fadeInAnimator = ObjectAnimator.ofFloat(mLayoutCategory, "alpha", 0f, 1f);
        fadeInAnimator.setDuration(500).start();
        mIvClose.setEnabled(true);
    }

    private void closeCategoryLayout() {
        mIvClose.setEnabled(false);
        ObjectAnimator springOutAnimator = ObjectAnimator.ofFloat(mLlCategory, "translationY", 0, -1800);
        springOutAnimator.setDuration(500).start();
        ObjectAnimator fadeOutAnimator = ObjectAnimator.ofFloat(mLayoutCategory, "alpha", 1f, 0f);
        fadeOutAnimator.setDuration(500).start();
        fadeOutAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLayoutCategory.setVisibility(View.GONE);
            }
        });
    }

    private void findPerson(String action) {
        switch (mType) {
            case TYPE_TRAIN:
                if (!isNetworkOk()) return;
                if (!isHeikuaiNetwork()) return;
                break;
            case TYPE_LINE:
                if (!isNetworkOk()) return;
                if (!isHeikuaiNetwork()) return;
                break;
            case TYPE_STATION:
                if (!isNetworkOk()) return;
                if (!isHeikuaiNetwork()) return;
                break;
            case TYPE_NEARBY:
                if (!isNetworkOk()) return;
                break;
        }

        FindPersonRequestBody body = new FindPersonRequestBody();
        body.type = mType;
        body.age = mAge;
        body.sex = mSex;
        body.refresh = action;
        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                mLoadingBar.setVisibility(View.GONE);
                mListView.onRefreshComplete();
                Log.i("FindPersonActivity", "@@@#@response: " + response);
                if (!TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    FindPersonResponseObject object = gson.fromJson(response, FindPersonResponseObject.class);
                    ResponseHeader header = object.header;
                    if (AppConstants.RET_OK.equals(header.ret)) {
                        mLocation = object.body.location;
                        if (ACTION_REFRESH.equals(mAction)) {
                            refreshPerson(object.body);
                        } else {
                            loadMorePerson(object.body);
                        }
                    } else if ("36162".equals(header.errCode)) {
                        mLlHint.setVisibility(View.VISIBLE);
                        mIvHint.setImageResource(R.drawable.ic_in_train);
                        mTvHint.setText(R.string.social_in_train);
                    } else if ("36163".equals(header.errCode)) {
                        mLlHint.setVisibility(View.VISIBLE);
                        mIvHint.setImageResource(R.drawable.ic_out_train);
                        mTvHint.setText(R.string.social_out_train);
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                Log.i("FindPersonActivity", "@@@#@onErrorResponse");
                mLoadingBar.setVisibility(View.GONE);
            }
        };
        WebServiceIf.findPerson(this, body, callback);
    }

    private boolean isHeikuaiNetwork() {
        WifiManager wifiManager = (WifiManager) (this.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (!NetworkUtil.isConnectFashionWiFi(wifiInfo)) {
            mLlHint.setVisibility(View.VISIBLE);
            mIvHint.setImageResource(R.drawable.ic_no_wifi);
            mTvHint.setText(R.string.social_no_wifi);
            return false;
        }
        return true;
    }

    private boolean isNetworkOk() {
        if (!NetworkUtil.isNetAvailable(this)) {
            mLlHint.setVisibility(View.VISIBLE);
            mIvHint.setImageResource(R.drawable.ic_no_location);
            mTvHint.setText(R.string.social_no_location);
            return false;
        }
        return true;
    }

    private void refreshPerson(FindPersonResponseObject.FindPersonResponseBody body) {
        mFreshTime = System.currentTimeMillis();
        mListView.getLoadingLayoutProxy().setLastUpdatedLabel(
                "更新于：" + DateUtil.getRelativeTimeSpanString(mFreshTime));
        mPersonList.clear();
        if (body.userList != null && body.userList.size() > 0) {
            mPersonList.addAll(body.userList);
        }
        mAdapter.notifyDataSetChanged();

        updateHint();
    }

    private void updateHint() {
        if (mPersonList.size() > 0) {
            mListView.setVisibility(View.VISIBLE);
            mLlHint.setVisibility(View.GONE);
        } else {
            mListView.setVisibility(View.GONE);
            mLlHint.setVisibility(View.VISIBLE);
            mIvHint.setImageResource(R.drawable.ic_change_posture);
            mTvHint.setText(R.string.social_change_posture);
        }
        if (mType.equals(TYPE_TRAIN) || mType.equals(TYPE_NEARBY)) {
            mTvLocation.setVisibility(View.GONE);
            mTvCategory.setTextSize(16);
        } else if (mType.equals(TYPE_STATION) || mType.equals(TYPE_LINE)) {
            mTvLocation.setVisibility(View.VISIBLE);
            mTvCategory.setTextSize(14);
            if (!TextUtils.isEmpty(mLocation)) {
                mTvLocation.setBackgroundResource(R.drawable.bg_gradient_line1);
                mTvLocation.setText(mLocation);
            } else {
                mTvLocation.setBackgroundResource(R.drawable.bg_location);
                mTvLocation.setText("");
            }
        }
    }

    private void loadMorePerson(FindPersonResponseObject.FindPersonResponseBody body) {
        if (body.userList != null && body.userList.size() > 0) {
            mPersonList.addAll(body.userList);
            mAdapter.notifyDataSetChanged();
            mHasMore = true;
        } else {
            mHasMore = false;
        }
    }

    private void showFilterDialog() {
        mFilterDialog = new FilterDialog(this, R.style.round_corner_dialog, new FilterDialog.MyDialogListener() {
            @Override
            public void onClick(View view, String sex, String age) {
                switch (view.getId()) {
                    case R.id.tv_confirm:
                        mFilterDialog.dismiss();
                        mSex = sex;
                        mAge = age;
                        findPerson(ACTION_REFRESH);
                        mLoadingBar.setVisibility(View.VISIBLE);
                        break;
                    case R.id.tv_cancel:
                        mFilterDialog.dismiss();
                        break;
                }
            }
        }, mAge, mSex);
        mFilterDialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, PersonInfoActivity.class);
        intent.putExtra("person", mPersonList.get(position - 1));
        startActivity(intent);
    }

    public class SpringInterpolator implements Interpolator {
        //弹性因数
        private float factor;

        public SpringInterpolator(float factor) {
            this.factor = factor;
        }

        @Override
        public float getInterpolation(float input) {

            return (float) (Math.pow(2, -10 * input) * Math.sin((input - factor / 4) * (2 * Math.PI) / factor) + 1);
        }
    }
}
