package com.cqsynet.swifi.activity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.adapter.ReplyAdapter;
import com.cqsynet.swifi.model.CommentInfo;
import com.cqsynet.swifi.model.CommentRequestBody;
import com.cqsynet.swifi.model.CommentResponseObject;
import com.cqsynet.swifi.model.CommentSubmitResponseObject;
import com.cqsynet.swifi.model.ReplyListRequestBody;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.InputManagerUtil;
import com.cqsynet.swifi.util.SoftKeyboardStateHelper;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.CommentDialog;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/9/7
 */
public class ReplyActivity extends HkActivity implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener,
        ReplyAdapter.OnItemChildClickListener {

    public static final int DIALOG_SHOW_TIME = 1000;
    private static final int MSG_DISMISS_DIALOG = 0;

    private View mRootLayout;
    private ImageView mIvBack;
    private TextView mTvTitle;
    private PullToRefreshListView mListView;
    private TextView mTvNoComment;
    private LinearLayout mLlComment;
    private TextView mTvCommentHint;
    private TextView mTvCommentDisable;
    private ImageView mIvWrite;
    private EditText mEtComment;
    private TextView mTvSend;
    private View mMarkView;

    private ReplyAdapter mAdapter;
    private List<CommentInfo> mComments = new ArrayList<>();
    private String mId;
    private CommentInfo mComment;
    private CommentInfo mLevelTwoComment;
    private long mFreshTime = 0;
    // 0: 二级评论；1: 二级评论的回复
    private int mStatus = 0;
    // 是否有下一页
    private boolean mHasMore;
    private String mCommentStatus;
    private String mCommentMessage;
    private int newCommentCount; // 新增加的回复数量
    private Dialog mDialog;
    private MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {

        private WeakReference<ReplyActivity> weakReference;

        public MyHandler(ReplyActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ReplyActivity activity = weakReference.get();
            if (msg.what == MSG_DISMISS_DIALOG) {
                activity.dismissProgressDialog();
                if (activity.mDialog != null && activity.mDialog.isShowing()) {
                    activity.mDialog.dismiss();
                }
            }
        }
    }

    public static void launch(Context context, String id, CommentInfo comment, String status, String message) {
        Intent intent = new Intent();
        intent.setClass(context, ReplyActivity.class);
        intent.putExtra("KEY_EXTRA_ID", id);
        intent.putExtra("KEY_EXTRA_COMMENT", comment);
        intent.putExtra("KEY_EXTRA_STATUS", status);
        intent.putExtra("KEY_EXTRA_MESSAGE", message);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        mId = getIntent().getStringExtra("KEY_EXTRA_ID");
        mComment = getIntent().getParcelableExtra("KEY_EXTRA_COMMENT");
        mCommentStatus = getIntent().getStringExtra("KEY_EXTRA_STATUS");
        mCommentMessage = getIntent().getStringExtra("KEY_EXTRA_MESSAGE");

        mRootLayout = findViewById(R.id.root_layout);
        mIvBack = findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTvTitle = findViewById(R.id.tv_title);
        mFreshTime = System.currentTimeMillis();
        mListView = findViewById(R.id.lv_comment);
        mAdapter = new ReplyAdapter(this, mComments, this);
        mListView.setAdapter(mAdapter);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                getReplyList(mComment.id, "");
            }
        });
        mListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                if (mHasMore) {
                    CommentInfo comment = (CommentInfo) mAdapter.getItem(mAdapter.getCount() - 1);
                    getReplyList(mComment.id, comment.id);
                }
            }
        });
        mListView.getLoadingLayoutProxy().setLastUpdatedLabel(
                "更新于:" + DateUtil.getRelativeTimeSpanString(mFreshTime));
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mTvTitle.setText(mComment.replyCount + "条回复");
        mTvNoComment = findViewById(R.id.tv_no_comment);
        mLlComment = findViewById(R.id.comment_layout);
        mIvWrite = findViewById(R.id.iv_write);
        mEtComment = findViewById(R.id.et_comment);
        mEtComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable)) {
                    mIvWrite.setVisibility(View.VISIBLE);
                    mTvCommentHint.setVisibility(View.VISIBLE);
                    mTvSend.setBackgroundResource(R.drawable.btn_send_white_disable);
                } else {
                    mIvWrite.setVisibility(View.GONE);
                    mTvCommentHint.setVisibility(View.GONE);
                    mTvSend.setBackgroundResource(R.drawable.btn_send_green);
                }
            }
        });
        mTvCommentHint = findViewById(R.id.tv_comment_hint);
        mTvCommentHint.setText("回复" + mComment.nickname + "：");
        mTvCommentDisable = findViewById(R.id.tv_comment_disable);
        mTvSend = findViewById(R.id.tv_send);
        mTvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentRequestBody body = new CommentRequestBody();
                if (mStatus == 0) {
                    body.type = "1";
                    body.newsId = mId;
                    body.levelOneId = mComment.id;
                    body.levelTwoId = "";
                    body.content = mEtComment.getText().toString();
                } else {
                    body.type = "2";
                    body.newsId = mId;
                    body.levelOneId = mComment.id;
                    body.levelTwoId = mLevelTwoComment.id;
                    body.content = "回复 " + mLevelTwoComment.nickname + "：" + mEtComment.getText().toString();
                }
                submitComment(body);
            }
        });
        mMarkView = findViewById(R.id.mark_view);
        mMarkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputManagerUtil.toggleKeyboard(ReplyActivity.this);
            }
        });

        getReplyList(mComment.id, "");

        SoftKeyboardStateHelper helper = new SoftKeyboardStateHelper(mRootLayout);
        helper.addSoftKeyboardStateListener(new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                mMarkView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSoftKeyboardClosed() {
                if (TextUtils.isEmpty(mEtComment.getText())) {
                    mStatus = 0;
                    mTvCommentHint.setText("回复" + mComment.nickname + "：");
                }
                mMarkView.setVisibility(View.GONE);
            }
        });

        if ("0".equals(mCommentStatus)) {
            mLlComment.setVisibility(View.VISIBLE);
            mEtComment.setEnabled(true);
            mIvWrite.setVisibility(View.VISIBLE);
            mTvCommentHint.setVisibility(View.VISIBLE);
            mTvCommentDisable.setVisibility(View.GONE);
        } else if ("2".equals(mCommentStatus)) {
            mLlComment.setVisibility(View.VISIBLE);
            mEtComment.setEnabled(false);
            mIvWrite.setVisibility(View.GONE);
            mTvCommentHint.setVisibility(View.GONE);
            mTvCommentDisable.setVisibility(View.VISIBLE);
            mTvCommentDisable.setText(mCommentMessage);
            mAdapter.setDisable();
        } else {
            mLlComment.setVisibility(View.GONE);
            mAdapter.setDisable();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(MSG_DISMISS_DIALOG);
    }

    private void getReplyList(String id, final String start) {
        ReplyListRequestBody body = new ReplyListRequestBody();
        body.id = id;
        body.start = start;

        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                mListView.onRefreshComplete();
                Log.i("ReplyActivity", "@@@#onResponse: " + response);
                if (!TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    CommentResponseObject object = gson.fromJson(response, CommentResponseObject.class);
                    ResponseHeader header = object.header;
                    if (AppConstants.RET_OK.equals(header.ret)) {
                        if ("".equals(start)) {
                            refreshComment(object.body);
                        } else {
                            loadMoreComment(object.body);
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                dismissProgressDialog();
            }
        };
        WebServiceIf.getReplyList(this, body, callback);
    }

    private void refreshComment(CommentResponseObject.CommentResponseBody body) {
        mFreshTime = System.currentTimeMillis();
        mListView.getLoadingLayoutProxy().setLastUpdatedLabel(
                "更新于：" + DateUtil.getRelativeTimeSpanString(mFreshTime));
        mComments.clear();
        mComments.add(mComment);
        if (body.hotComment != null && body.hotComment.size() > 0) {
            mComments.addAll(body.hotComment);
        }
        if (body.newComment != null && body.newComment.size() > 0) {
            mComments.addAll(body.newComment);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void loadMoreComment(CommentResponseObject.CommentResponseBody body) {
        if (body.newComment != null && body.newComment.size() > 0) {
            mComments.addAll(body.newComment);
            mHasMore = true;
        } else {
            mHasMore = false;
        }
        mAdapter.notifyDataSetChanged();
    }

    private void submitComment(final CommentRequestBody body) {
        if (TextUtils.isEmpty(mEtComment.getText().toString().trim())) {
            return;
        }

        showProgressDialog(R.string.comment_submitting);

        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                Log.i("CommentActivity", "@@@#submitComment onResponse: " + response);
                dismissProgressDialog();
                if (!TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    CommentSubmitResponseObject object = gson.fromJson(response, CommentSubmitResponseObject.class);
                    ResponseHeader header = object.header;
                    if (AppConstants.RET_OK.equals(header.ret)) {
                        mDialog = CommentDialog.createDialog(ReplyActivity.this, R.drawable.ic_success, getString(R.string.comment_success));
                        mDialog.show();
                        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, DIALOG_SHOW_TIME);
                        if (!TextUtils.isEmpty(object.body.id)) {
                            CommentInfo commentInfo = new CommentInfo();
                            commentInfo.id = object.body.id;
                            commentInfo.userAccount = Globals.g_userInfo.userAccount;
                            commentInfo.nickname = Globals.g_userInfo.nickname;
                            commentInfo.headUrl = Globals.g_userInfo.headUrl;
                            commentInfo.content = body.content;
                            commentInfo.likeCount = "0";
                            commentInfo.like = "0";
                            commentInfo.date = String.valueOf(System.currentTimeMillis());
                            commentInfo.replyCount = "0";
                            mComments.add(commentInfo);
                            mAdapter.notifyDataSetChanged();

                            newCommentCount++;
                            if (!TextUtils.isEmpty(mComment.replyCount)) {
                                mTvTitle.setText(Integer.parseInt(mComment.replyCount) + newCommentCount + "条回复");
                            } else {
                                mTvTitle.setText(newCommentCount + "条回复");
                            }
                        }
                    } else {
                        mDialog = CommentDialog.createDialog(ReplyActivity.this, R.drawable.ic_failure, getString(R.string.comment_fail));
                        mDialog.show();
                        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, DIALOG_SHOW_TIME);
                    }
                }
                mEtComment.setText("");
            }

            @Override
            public void onErrorResponse() {
                Log.e("CommentActivity", "@@@#onErrorResponse");
                dismissProgressDialog();
                mDialog = CommentDialog.createDialog(ReplyActivity.this, R.drawable.ic_failure, getString(R.string.comment_fail));
                mDialog.show();
                mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, DIALOG_SHOW_TIME);
            }
        };
        WebServiceIf.submitComment(this, body, callback);
    }

    @Override
    public void onLikeClick(int position, boolean isSuccess) {
        if (isSuccess) {
            mComments.get(position).like = "1";
            int likeCount = Integer.parseInt(mComments.get(position).likeCount);
            mComments.get(position).likeCount = String.valueOf(likeCount + 1);
            for (int i = 0; i < mComments.size(); i++) {
                if (i != position && mComments.get(position).id.equals(mComments.get(i).id)) {
                    mComments.get(i).like = "1";
                    int count = Integer.parseInt(mComments.get(i).likeCount);
                    mComments.get(i).likeCount = String.valueOf(count + 1);
                    break;
                }
            }
        } else {
            getReplyList(mComment.id, "");
            mDialog = CommentDialog.createDialog(ReplyActivity.this, R.drawable.ic_failure, getString(R.string.comment_fail));
            mDialog.show();
            mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, DIALOG_SHOW_TIME);
        }
    }

    @Override
    public void onLikeRepeat() {
        mDialog = CommentDialog.createDialog(ReplyActivity.this, R.drawable.ic_failure, getString(R.string.like_repeat));
        mDialog.show();
        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, DIALOG_SHOW_TIME);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!"0".equals(mCommentStatus)) {
            if (!TextUtils.isEmpty(mCommentMessage)) {
                ToastUtil.showToast(this, mCommentMessage);
            }
            return;
        }
        if (position > 1) {
            mStatus = 1;
        } else if (position == 1) {
            mStatus = 0;
        }
        mLevelTwoComment = mComments.get(position - 1);
        mTvCommentHint.setText("回复" + mComments.get(position - 1).nickname + "：");
        mEtComment.setFocusable(true);
        mEtComment.setFocusableInTouchMode(true);
        mEtComment.requestFocus();
        InputManagerUtil.showKeyboardDelay(mEtComment, 100);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mLevelTwoComment = mComments.get(position - 1);
        showActionDialog(mLevelTwoComment);
        return true;
    }

    private void showActionDialog(final CommentInfo comment) {
        final String[] itemArray = getResources().getStringArray(R.array.comment_action_2);
        final CustomDialog dialog = new CustomDialog(this, R.style.round_corner_dialog, R.layout.listview_chat_popup);
        ListView listview = dialog.getCustomView().findViewById(R.id.lv_listview_chat_popup);
        listview.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return itemArray.length;
            }

            @Override
            public Object getItem(int i) {
                return itemArray[i];
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                view = View.inflate(ReplyActivity.this, R.layout.chat_popup, null);
                TextView tvTitle = view.findViewById(R.id.tvTitle_chat_popup);
                tvTitle.setText(itemArray[i]);
                return view;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (itemArray[i].equals("复制")) {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("text", comment.content);
                    clipboardManager.setPrimaryClip(clipData);
                    ToastUtil.showToast(ReplyActivity.this, "评论内容复制成功");
                } else if (itemArray[i].equals("举报")) {
                    Intent complainIntent = new Intent(ReplyActivity.this, SimpleWebActivity.class);
                    complainIntent.putExtra("title", "投诉");
                    complainIntent.putExtra("url", AppConstants.COMPLAIN_PAGE);
                    complainIntent.putExtra("commentId", comment.id);
                    complainIntent.putExtra("complainType", "comment");
                    startActivity(complainIntent);
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
