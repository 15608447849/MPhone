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

    @Bind(R.id.show_button_sure)
    Button show_button_sure; //接受

    @Bind(R.id.show_button_refuse)
    Button show_button_refuse;//拒绝

    @Bind(R.id.show_button_over)
    Button show_button_over;//结束

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
        View rootView = inflater.inflate(R.layout.show_layout, null);
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



    @OnClick({R.id.show_button_sure,R.id.show_button_refuse,R.id.show_button_over})
    public void onClick(View view) {
        if (SysInfo.get(true).isConnected()) { //连接成功的

            swiBtn(view.getId());

        } else {
            //提示 未连接
            mActivity.showTolas("连接服务器失败,尝试中...");
//            重新选择页面
            mActivity.initFragments();
        }
    }

    private void swiBtn(int id) {
       if (id == R.id.show_button_sure){
           //发送接受请求
           mActivity.sendMessageToServers(CommunicationProtocol.ANTY + CommunicationProtocol.RECIPT_ACCEPT_SERVER);
       }
        if (id == R.id.show_button_refuse){
            //发送拒绝请求
            mActivity.sendMessageToServers(CommunicationProtocol.ANTY +  CommunicationProtocol.RECIPT_REFUSE_SERVER);
        }
        if (id == R.id.show_button_over){
            //发送服务完成请求
            mActivity.sendMessageToServers(CommunicationProtocol.ANTY +  CommunicationProtocol.RECIPT_OVER_SERVER);
        }
        setButtonClick(false);
        mActivity.finish();
    }


    //设置按钮是否可点击
    public void setButtonClick(boolean isClick){
        if (show_button_sure!=null){
            show_button_sure.setEnabled(isClick);
        }
        if (show_button_refuse!=null){
            show_button_refuse.setEnabled(isClick);
        }
        if (show_button_over!=null){
            show_button_over.setEnabled(!isClick);
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
