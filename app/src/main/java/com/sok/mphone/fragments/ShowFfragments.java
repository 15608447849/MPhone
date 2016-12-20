package com.sok.mphone.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sok.mphone.R;
import com.sok.mphone.activity.BaseActivity;
import com.sok.mphone.dataEntity.SysInfo;
import com.sok.mphone.services.CommuntServer;
import com.sok.mphone.tools.CommunicationProtocol;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by user on 2016/12/19.
 */

public class ShowFfragments extends Fragment {

    private BaseActivity mActivity;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (BaseActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = mActivity.getLayoutInflater().inflate(R.layout.show_layout, null);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Bind(R.id.show_button)
    Button show_button;

    @OnClick(R.id.show_button)
    public void onClick() {
        if (SysInfo.get().isConnected()){ //连接成功的
            mActivity.showTolas("连接服务器成功");
            //发送接受请求
            mActivity.sendMessageToServers(CommunicationProtocol.ANTY+"[回执编号-200,已接受服务请求]");
            //发送停止效果
            mActivity.sendMessageToServers(CommuntServer.LocalCommand.STOP_ZX);
        }else{
            //提示 未连接
            mActivity.showTolas("未连接服务器");
        }
    }
}
