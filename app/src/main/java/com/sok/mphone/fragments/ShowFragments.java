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

    private static final String TAG = "ShowFrag";


    private BaseActivity mActivity;

    @Bind(R.id.show_button_sure)
    Button show_button_sure; //接受

    @Bind(R.id.show_button_refuse)
    Button show_button_refuse;//拒绝

    @Bind(R.id.show_button_over)
    Button show_button_over;//结束服务

    @Bind(R.id.show_button_exit)
    Button show_button_exit;//结束通讯

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
        setButtonClick(1,false);
        setNoShowButton(1);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        log.e(TAG, "onResume");
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


    @OnClick({R.id.show_button_sure, R.id.show_button_refuse, R.id.show_button_over, R.id.show_button_exit})
    public void onClick(View view) {
        if (SysInfo.get(SysInfo.COMUNICATION).isConnected()) { //正在链接中的
            swiBtn(view.getId());
        } else {
            setConnectFailt();
        }
    }

    private void swiBtn(int id) {

        if (id == R.id.show_button_sure) {
            //发送接受请求
            mActivity.sendMessageToServers(CommunicationProtocol.ANTY + CommunicationProtocol.RECIPT_ACCEPT_SERVER);
            setMessageSendSuccess(0);
        }
        if (id == R.id.show_button_refuse) {
            //发送拒绝请求
            mActivity.sendMessageToServers(CommunicationProtocol.ANTY + CommunicationProtocol.RECIPT_REFUSE_SERVER);
            setMessageSendSuccess(0);
        }
        if (id == R.id.show_button_over) {
            mActivity.showTolas("已告知完成服务");
            //发送服务完成请求
            mActivity.sendMessageToServers(CommunicationProtocol.ANTY + CommunicationProtocol.RECIPT_OVER_SERVER);
            setMessageSendSuccess(1);
        }
        if (id == R.id.show_button_exit) { //结束通讯服务
            //如果正在呼叫中 并且 处于空闲
            if (SysInfo.get(SysInfo.COMUNICATION).isHasMessage() &&
                    SysInfo.get(SysInfo.COMUNICATION).isMessageTask()) {
                //发送拒绝请求
                mActivity.sendMessageToServers(CommunicationProtocol.ANTY + CommunicationProtocol.RECIPT_REFUSE_SERVER);
                setMessageSendSuccess(0);
            }

            //设置不连接标识
            SysInfo sifo = SysInfo.get(SysInfo.CONFIG);
            sifo.setLocalConnect(SysInfo.LOCAL_CONNECT.LOCAL_CONNECT_UNENABLE);
            //写入文件
            if (sifo.writeInfo(SysInfo.CONFIG)){
                mActivity.showTolas("已断开连接服务");//+SysInfo.get(SysInfo.CONFIG).isLocalConnect()
                mActivity.startCommunication();
            }
        }
        mActivity.finish();
    }
    //消息已发送出去
    public void setMessageSendSuccess(int type) {
        //设置 接受拒接 不可点击 - 消失
        setButtonClick(type,false);
        setNoShowButton(type);
    }


    //设置按钮是否可点击
    public void setButtonClick(int type, boolean isClick) {
        if (type == 0) {  //拒绝 - 接受
            if (show_button_sure != null) {
                show_button_sure.setEnabled(isClick);
            }
            if (show_button_refuse != null) {
                show_button_refuse.setEnabled(isClick);
            }
        }
        if (type == 1) {//结束服务
            if (show_button_over != null) {
                show_button_over.setEnabled(isClick);
            }
        }

    }


    public void initState() {
        //存在 呼叫任务 并且 有消息发来
        if (SysInfo.get(SysInfo.COMUNICATION).isHasMessage() &&
                SysInfo.get(SysInfo.COMUNICATION).isMessageTask()) {
            //有消息 - 按钮显示 - 可点击
            setShowButton(0);
            setButtonClick(0, true);
        } else {
            setNoShowButton(0);
            //没消息 - 按钮不可点击
            setButtonClick(0, false);
        }
    }

    //显示按钮
    private void setShowButton(int type) {
        if (type == 0) {
            if (show_button_sure != null) {
                show_button_sure.setVisibility(View.VISIBLE);
            }
            if (show_button_refuse != null) {
                show_button_refuse.setVisibility(View.VISIBLE);
            }
        }
        if (type == 1) {
            show_button_over.setVisibility(View.VISIBLE);
        }
    }

    //不显示按钮 - 接受拒绝按钮
    private void setNoShowButton(int type) {
        if (type == 0) {
            if (show_button_sure != null) {
                show_button_sure.setVisibility(View.INVISIBLE);
            }
            if (show_button_refuse != null) {
                show_button_refuse.setVisibility(View.INVISIBLE);
            }
        }

        if (type == 1) {
            show_button_over.setVisibility(View.INVISIBLE);
        }
    }


    public void setConnectFailt() {
        setNoShowButton(0);
        setButtonClick(0, false);
        setNoShowButton(1);
        setButtonClick(1, false);
        //提示
        mActivity.showTolas("连接服务器失败,请检查ip或端口是否正确");
        mActivity.initFragments(true);
    }

    public void showOverButton(){
        //存在呼叫任务 并且 没有 消息发来
        if (!SysInfo.get(SysInfo.COMUNICATION).isHasMessage() &&
                SysInfo.get(SysInfo.COMUNICATION).isMessageTask()){
            setShowButton(1);
            setButtonClick(1,true);
        }
    }


}
