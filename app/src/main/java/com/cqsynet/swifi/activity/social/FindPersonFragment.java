package com.cqsynet.swifi.activity.social;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.BottleActivity;
import com.cqsynet.swifi.util.ToastUtil;

/**
 * Author: sayaki
 * Date: 2017/11/23
 */
public class FindPersonFragment extends Fragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_person, container, false);

        TextView tvTrain = view.findViewById(R.id.tv_train);
        tvTrain.setOnClickListener(this);
        TextView tvLine = view.findViewById(R.id.tv_line);
        tvLine.setOnClickListener(this);
        TextView tvStation = view.findViewById(R.id.tv_station);
        tvStation.setOnClickListener(this);
        TextView tvNearby = view.findViewById(R.id.tv_nearby);
        tvNearby.setOnClickListener(this);
        TextView tvBottle = view.findViewById(R.id.tv_bottle);
        tvBottle.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_train:
                FindPersonActivity.launch(getActivity(), FindPersonActivity.TYPE_TRAIN);
                break;
            case R.id.tv_line:
                FindPersonActivity.launch(getActivity(), FindPersonActivity.TYPE_LINE);
                break;
            case R.id.tv_station:
                FindPersonActivity.launch(getActivity(), FindPersonActivity.TYPE_STATION);
                break;
            case R.id.tv_nearby:
                FindPersonActivity.launch(getActivity(), FindPersonActivity.TYPE_NEARBY);
                break;
            case R.id.tv_bottle:
                //用户是否被冻结
                if (Globals.g_userInfo.lock.equals("1")) {
                    ToastUtil.showToast(getActivity(), Globals.g_userInfo.lockMsg);
                } else {
                    Intent intent = new Intent(getActivity(), BottleActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }
}
