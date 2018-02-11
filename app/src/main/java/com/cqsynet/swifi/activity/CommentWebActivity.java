/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：评论页面
 *
 *
 * 创建标识：zhaosy 20161018
 */
package com.cqsynet.swifi.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.TimeUnit;

public class CommentWebActivity extends HkActivity {

    private WebView mWebView;
    private ImageButton mBtnBack;
    private ImageButton mBtnClose;
    private ProgressBar mProgress;
    private String mUrl;
    private boolean mIsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commentweb);
        mBtnBack = findViewById(R.id.btnBack_commentweb);
        mBtnClose = findViewById(R.id.btnClose_commentweb);
        mProgress = findViewById(R.id.progress_commentweb);
        mBtnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    finish();
                }
            }
        });

        mBtnClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        mWebView = findViewById(R.id.wv_commentweb);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.getSettings().setTextZoom(100);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setDomStorageEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mProgress.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                mProgress.setVisibility(View.GONE);
            }
        });

        // 解决5.0以上手机WebView无法成功同步Cookie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(mWebView, true);
        }

        mUrl = getIntent().getStringExtra("url");
        if(Globals.DEBUG) {
            System.out.println(mUrl);
        }
        new Thread() {
            public void run() {
                //先检测页面是否报错
                mIsError = validStatusCode(mUrl);
                mHdl.sendEmptyMessage(0);
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finish();
        }
    }

    /**
     * 检查url返回页面是否成功
     *
     * @param url
     * @return
     */
    private boolean validStatusCode(String url) {
        if(!url.toLowerCase().contains("http://") && !url.toLowerCase().contains("https://")) {
            return false;
        }
        OkHttpClient mClient = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS).build();
        try {
            Request request = new Request.Builder().get().url(url).build();
            Response response = mClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    Handler mHdl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if(mIsError) {
                        mProgress.setVisibility(View.GONE);
                    } else {
                        mWebView.loadUrl(mUrl);
                    }
                    break;
            }
        }
    };
}