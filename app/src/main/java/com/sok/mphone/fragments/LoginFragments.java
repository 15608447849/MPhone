package com.sok.mphone.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sok.mphone.R;
import com.sok.mphone.activity.BaseActivity;
import com.sok.mphone.entity.SysInfo;
import com.sok.mphone.tools.AppsTools;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

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
        View rootView = inflater.inflate(R.layout.login_layout, null);
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
        String ip = SysInfo.get(SysInfo.CONFIG).getServerIp();
        String port = SysInfo.get(SysInfo.CONFIG).getServerPort();
        if (!"".equals(ip))
                login_server_ip.setText(ip);
        if (!"".equals(port))
                login_server_port.setText(port);
        if (SysInfo.get(SysInfo.COMUNICATION).isConnected()) {
            mActivity.startCommunication();
            login_state.setText(mActivity.getString(R.string.login_state_connect_ing));//连接中
        } else {
            login_state.setText(mActivity.getString(R.string.login_state_connect_not));//未连接
        }
    }


    @OnClick(R.id.login_button)
    public void onClick(View view) {
       // mActivity.showTolas("连接服务器" +(SysInfo.get(true).isConnected()?"成功":"失败,请检查网络或ip是否正确"));
        if (mActivity==null) return;
        if (!AppsTools.isOpenNetwork(mActivity)){
            mActivity.showTolas("网络连接不可用");
            //进入网络设置
            if(android.os.Build.VERSION.SDK_INT > 10 ){
                //3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
            } else {
                startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
            }
            return;
        }

        //如果6.0 - 23 以上
        if (Integer.parseInt(Build.VERSION.SDK)>=23){
            if ( ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                  || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    ){
                mActivity.showTolas("无法读取写入文件,请设置应用权限.");
                return;
            }
        }

        //                SysInfo.get().setConnectPower(SysInfo.COMUNICATE_POWER.COMMUNI_ACCESS);//设置有权限
        //输出的ip 地址 和保存的 ip地址 不同 -> 也需要连接
        if (!SysInfo.get(SysInfo.COMUNICATION).isConnected()) { //判断不在连接中
            String ip = login_server_ip.getText().toString().trim();
            String port = login_server_port.getText().toString().trim();
            if (!"".equals(ip) && !"".equals(port)) {
                login_state.setText(mActivity.getString(R.string.login_state_connect_ing));//连接中
                SysInfo sifo = SysInfo.get(SysInfo.CONFIG);
                sifo.setServerIp(ip);
                sifo.setServerPort(port);
                sifo.setAppMac(AppsTools.getMacAddress(mActivity));//获取mac地址
                sifo.setLocalConnect(SysInfo.LOCAL_CONNECT.LOCAL_CONNECT_ENABLE);//本地允许
                sifo.setConfigInfo(SysInfo.IFCONFIG.CONFIG_SUCCESS);
                //写入文件
                if (sifo.writeInfo(SysInfo.CONFIG)){
                    mActivity.startCommunication();//打开后台通讯进程
                }else{
                    mActivity.showTolas("写入配置信息失败");
                    login_state.setText(mActivity.getString(R.string.login_state_connect_failt));//连接失败
                }
            }
        }
    }

    @OnLongClick(R.id.login_button)
    public boolean OnLongClick(View view){
        login_state.setText(AppsTools.getMacAddress(mActivity));//显示mac地址
        return true;
    }

    private void setEnable(boolean t) {

        login_button.setEnabled(t);
        login_state.setEnabled(t);
        login_server_ip.setEnabled(t);
        login_server_port.setEnabled(t);
    }

    public void setConnectSuccess() {

        if (login_state!=null){
            login_state.setText(mActivity.getString(R.string.login_state_connect_success));
            setEnable(false);//不可点击
        }
//        mActivity.showTolas("成功连接服务器");

        //转变页面
        mActivity.initFragments(false);//转变页面
    }


    public void setConnectFailt() {
        if (login_state!=null){
            login_state.setText(mActivity.getString(R.string.login_state_connect_failt));
        }
    }
}
