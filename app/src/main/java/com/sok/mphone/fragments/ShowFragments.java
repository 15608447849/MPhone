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
import com.sok.mphone.entity.SysInfo;
import com.sok.mphone.tools.CommunicationProtocol;
import com.sok.mphone.tools.log;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by user on 2016/12/19.
 */

public class ShowFragments extends Fragment {

    private BaseActivity mActivity;

    @Bind(R.id.show_button)
    Button show_button;

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
    public void onResume() {
        super.onResume();
        log.i("showfragments","onResume");
        initState();
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



    @OnClick(R.id.show_button)
    public void onClick() {
        if (SysInfo.get(true).isConnected()) { //连接成功的
            //发送接受请求
            mActivity.sendMessageToServers(CommunicationProtocol.ANTY + "[回执编号-200,已接受服务请求]");
        } else {
            //提示 未连接
            mActivity.showTolas("未连接服务器");
//            重新选择页面
            mActivity.initFragments();
        }
    }

    //设置按钮是否可点击
    public void setButtonClick(boolean isClick){
        if (show_button!=null){
            show_button.setEnabled(isClick);
        }

    }

    public void initState(){
        if (SysInfo.get(true).getCommunicationState().equals(SysInfo.COMUNICATE_STATES.COMMUNI_CALL)) {
            //有消息 - 按钮可点击
            setButtonClick(true);
        }
        if (SysInfo.get(true).getCommunicationState().equals(SysInfo.COMUNICATE_STATES.COMMUNI_NO_MESSAGE)){
            //没消息 - 按钮不可点击
            setButtonClick(false);
        }
    }

    public void switchState() {
        initState();
    }
}
