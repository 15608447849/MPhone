package com.sok.mphone.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sok.mphone.R;
import com.sok.mphone.activity.BaseActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by user on 2016/12/23.
 */

public class TitleFragments extends Fragment{
    private BaseActivity mActivity;

    @Bind(R.id.title_button)
    Button title_button;//关闭应用

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (BaseActivity) activity;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.title_layout, null);
        ButterKnife.bind(this, rootView);
        return rootView;
    }


    @OnClick({R.id.title_button})
    public void onClick(View view) {
        mActivity.stopCommunication();
        mActivity.finish();
    }



}
