package com.sok.mphone.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sok.mphone.R;
import com.sok.mphone.activity.BaseActivity;
import com.sok.mphone.dataEntity.SysInfo;
import com.sok.mphone.tools.AppsTools;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by user on 2016/12/19.
 * 连接服务器登陆界面
 */

public class LoginFragments extends Fragment {


    private BaseActivity mActivity;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (BaseActivity) activity;
    }

    @Bind(R.id.login_server_ip)
    EditText login_server_ip;

    @Bind(R.id.login_server_port)
    EditText login_server_port;

    @Bind(R.id.login_state)
    TextView login_state;

    @Bind(R.id.login_button)
    Button login_button;


    //在实现代码中，你可以初始化想要在fragment中保持的那些必要组件(这里的组件是指除了view之外的东西,比如需要进行界面展示的关键数据)，当fragment处于暂停或者停止状态之后可重新启用它们。
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //在第一次为fragment绘制用户界面时系统会调用此方法。为fragment绘制用户界面，这个函数必须要返回所绘出的fragment的根View。如果fragment没有用户界面可以返回空
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = mActivity.getLayoutInflater().inflate(R.layout.login_layout, null);
        ButterKnife.bind(this, rootView);
        initData();
        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    //系统回调用该函数作为用户离开fragment的第一个预兆（尽管这并不总意味着fragment被销毁）。在当前用户会话结束之前，通常要在这里提交任何应该持久化的变化（因为用户可能不再返回）。
    @Override
    public void onPause() {
        super.onPause();
    }


    private void initData() {
        String ip = SysInfo.get().getServerIp();
        String port = SysInfo.get().getServerPort();
        login_server_ip.setHint(ip);
        login_server_port.setHint(port);
        if (SysInfo.get().isConnected()) {
            mActivity.startCommunication();
            login_state.setText(mActivity.getString(R.string.login_state_connect_ing));//连接中
        } else {
            login_state.setText(mActivity.getString(R.string.login_state_connect_not));//未连接
        }
    }


    @OnClick(R.id.login_button)
    public void onClick() {
        if (!SysInfo.get().isConnected()) {
            String ip = login_server_ip.getText().toString();
            String port = login_server_port.getText().toString();
            if (!"".equals(ip) && !"".equals(port)) {
                SysInfo.get().setServerIp(ip);
                SysInfo.get().setServerPort(port);
                SysInfo.get().setAppMac(AppsTools.getLocalMacAddressFromBusybox());
                SysInfo.get().saveInfo();
                login_state.setText(mActivity.getString(R.string.login_state_connect_ing));//连接中
                mActivity.startCommunication();
            }
        } else {
            mActivity.showTolas("请勿重复连接");
        }

    }


    private void setEnable(boolean t) {
        login_state.setEnabled(t);
        login_button.setEnabled(t);
        login_server_ip.setEnabled(t);
        login_server_port.setEnabled(t);
    }

    public void setConnectSuccess() {
        login_state.setText(mActivity.getString(R.string.login_state_connect_success));
        setEnable(false);
    }


}
