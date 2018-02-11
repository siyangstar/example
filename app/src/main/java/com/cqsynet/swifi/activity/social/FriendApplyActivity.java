package com.cqsynet.swifi.activity.social;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.HkActivity;
import com.cqsynet.swifi.model.AddOrRemoveFriendRequestBody;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.ToastUtil;

import org.json.JSONException;

/**
 * Author: sayaki
 * Date: 2017/12/5
 */
public class FriendApplyActivity extends HkActivity {

    private EditText mEtMessage;
    private TextView mTvMessageWord;

    private String friendAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_apply);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvSend = findViewById(R.id.tv_send);
        tvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestFriend();
            }
        });
        mEtMessage = findViewById(R.id.et_message);
        mEtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mTvMessageWord.setText(s.length() + "/25");
            }
        });
        mTvMessageWord = findViewById(R.id.tv_message_word);

        friendAccount = getIntent().getStringExtra("friendAccount");
    }

    private void requestFriend() {
        AddOrRemoveFriendRequestBody body = new AddOrRemoveFriendRequestBody();
        body.type = "0";
        body.friendAccount = friendAccount;
        body.message = mEtMessage.getText().toString();
        if (TextUtils.isEmpty(body.message)) {
            body.message = getString(R.string.social_friend_apply_greetings);
        }
        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                Log.i("FriendApplyActivity", "@@@#@response: " + response);
                ToastUtil.showToast(FriendApplyActivity.this, getString(R.string.social_friend_apply_sent));
                FriendApplyActivity.this.finish();
            }

            @Override
            public void onErrorResponse() {
            }
        };
        WebServiceIf.addOrRemoveFriend(this, body, callback);
    }
}
