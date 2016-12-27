package com.sok.mphone.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.sok.mphone.R;
import com.sok.mphone.entity.SysInfo;
import com.sok.mphone.fragments.IFragmentsFactory;
import com.sok.mphone.fragments.LoginFragments;
import com.sok.mphone.fragments.ShowFragments;
import com.sok.mphone.fragments.TitleFragments;
import com.sok.mphone.services.CommuntBroadCasd;
import com.sok.mphone.services.CommuntServer;
import com.sok.mphone.threads.interfaceDef.IActvityCommunication;
import com.sok.mphone.tools.log;

public class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity容器";

    private LoginFragments loginPage;
    private ShowFragments showPage;
    private TitleFragments titlePage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.activity_open, R.anim.activity_close);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        registBroad();
        initFragments(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        log.e(TAG, "onStart - ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        log.e(TAG, "onRestart - ");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        log.e(TAG, "onResume - ");
    }

    @Override
    public void finish() {
        super.finish();
        //关闭窗体动画显示
        overridePendingTransition(R.anim.activity_open, R.anim.activity_close);
    }

    // 返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 如果是手机上的返回键
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        log.e(TAG, "onNewIntent - " + intent);
    }

    public void initFragments(boolean isflag) {
        deleteAllPage(isflag);
        swithPage();
    }


    //删除所有 page
    private void deleteAllPage(boolean isflag) {
        if (titlePage != null) {
//            IFragmentsFactory.removeFragment(getFragmentManager().beginTransaction(), titlePage);
//            titlePage = null;
        }
        if (loginPage != null) {
            IFragmentsFactory.removeFragment(getFragmentManager().beginTransaction(), loginPage);
//            loginPage = null;
        }
        if (showPage != null) {
            IFragmentsFactory.removeFragment(getFragmentManager().beginTransaction(), showPage);
//            showPage = null;
        }
//        if (isflag){
//        titlePage = null;
        loginPage = null;
        showPage = null;
//        }
    }

    //选择页面
    private void swithPage() {

        //标题界面
        if (titlePage == null) {
            titlePage = (TitleFragments) IFragmentsFactory.getInstans(IFragmentsFactory.Type.title_page);
            IFragmentsFactory.repeateFragment(getFragmentManager().beginTransaction(), R.id.base_layout_1, titlePage);
        }


        //如果 已经 1配置 并且 2可连接状态
        if (SysInfo.get(true).isConfig() && SysInfo.get().isConnected()) {  //显示 show页面 ,关闭 login页面 -> 已配置 并且 可连接
            if (showPage == null) {
                showPage = (ShowFragments) IFragmentsFactory.getInstans(IFragmentsFactory.Type.show_page);
            }
            IFragmentsFactory.repeateFragment(getFragmentManager().beginTransaction(), R.id.base_layout_3, showPage);
        } else {
            if (loginPage == null) {
                loginPage = (LoginFragments) IFragmentsFactory.getInstans(IFragmentsFactory.Type.login_page);
            }
            IFragmentsFactory.repeateFragment(getFragmentManager().beginTransaction(), R.id.base_layout_2, loginPage);
        }

        startCommunication();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregistBroad();
    }

    private BaseBroad appReceive = null;

    /**
     * 停止广播 destory call
     */
    private void unregistBroad() {
        if (appReceive != null) {
            try {
                this.unregisterReceiver(appReceive);
            } catch (Exception e) {
                e.printStackTrace();
            }
            appReceive = null;
            log.i(TAG, "注销 基础activity 广播");
        }
    }

    /**
     * 注册广播  create call
     */
    private void registBroad() {
        unregistBroad();
        appReceive = new BaseBroad(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BaseBroad.ACTION);
        this.registerReceiver(appReceive, filter); //只需要注册一次
        log.i(TAG, "已注册 基础activity 广播");
    }

    //打开通讯服务
    public void startCommunication() {
        this.startService(new Intent(this, CommuntServer.class));
    }

    //关闭通讯服务
    public void stopCommunication() {
        try {
            this.stopService(new Intent(this, CommuntServer.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送消息到 通讯服务中
    public void sendMessageToServers(String msg) {
        Intent intent = new Intent();
        intent.setAction(CommuntBroadCasd.ACTION);
        Bundle bundle = new Bundle();
        bundle.putString(CommuntBroadCasd.PARAM1, msg);
        intent.putExtras(bundle);
        this.sendBroadcast(intent);
    }

    public void receiveServerMessage(int type) {
        log.i(TAG, "activity 收到 - 连接返回值类型 - type [" + type + "]");
        if (type == IActvityCommunication.CONNECT_SUCCEND ) {
            //连接成功
            if (loginPage != null) {//如果现在是登陆页面
                loginPage.setConnectSuccess();
            }
        }
        if (type == IActvityCommunication.MESSAGE_SEND_SUCCESS){
            // 消息发送成功
            if (showPage != null) {//现在是显示状态
                showPage.setMessageSendSuccess();
            }
        }
        if (type == IActvityCommunication.CONNECT_FAILT) {
            // 连接失败
            if (showPage != null) {//现在是显示状态
                showPage.setConnectFailt();
            }
        }
    }



    //吐司
    public void showTolas(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


}
