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
        overridePendingTransition(R.anim.activity_open,R.anim.activity_close);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        registBroad();
        initFragments();
    }

    @Override
    protected void onStart() {
        super.onStart();
        log.e(TAG,"onStart - ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        log.e(TAG,"onRestart - ");
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unFragments();
    }

    private void unFragments() {
        if (loginPage != null) {
            IFragmentsFactory.removeFragment(getFragmentManager().beginTransaction(), loginPage);
            loginPage = null;
        }
        if (showPage != null) {
            IFragmentsFactory.removeFragment(getFragmentManager().beginTransaction(), showPage);
            showPage = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        log.e(TAG,"onResume - ");
    }

    @Override
    public void finish() {
        super.finish();
        //关闭窗体动画显示
        overridePendingTransition(R.anim.activity_open,R.anim.activity_close);
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
        log.e(TAG,"onNewIntent - "+intent);
    }

    public void initFragments() {

        swithPage();
        addPages();
    }

    //选择页面
    private void swithPage() { //
        if (SysInfo.get(true).isConfig() && SysInfo.get().isConnected()) {  //显示 show页面 ,关闭 login页面 -> 已配置 并且 可连接
            if (loginPage != null) {
                IFragmentsFactory.removeFragment(getFragmentManager().beginTransaction(), loginPage);
                loginPage = null;
            }
            if (showPage == null)
                showPage = (ShowFragments) IFragmentsFactory.getInstans(IFragmentsFactory.Type.show_page);

        } else {

            if (showPage != null) {
                IFragmentsFactory.removeFragment(getFragmentManager().beginTransaction(), showPage);
                showPage = null;
            }
            if (loginPage == null)
                loginPage = (LoginFragments) IFragmentsFactory.getInstans(IFragmentsFactory.Type.login_page);
        }
        if (titlePage==null){
            titlePage = (TitleFragments) IFragmentsFactory.getInstans(IFragmentsFactory.Type.title_page);
        }
    }

    //添加页面
    private void addPages() {
        if (titlePage != null) {
            IFragmentsFactory.repeateFragment(getFragmentManager().beginTransaction(), R.id.base_layout_1, titlePage);
            startCommunication();
        }
        if (loginPage != null) {
            stopCommunication();
            IFragmentsFactory.repeateFragment(getFragmentManager().beginTransaction(), R.id.base_layout_2, loginPage);
        }
        if (showPage != null) {
            IFragmentsFactory.repeateFragment(getFragmentManager().beginTransaction(), R.id.base_layout_3, showPage);
            startCommunication();
        }

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
    public void stopCommunication(){
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
        log.i(TAG, "连接返回值类型 - type >> " + type);
        if (type == IActvityCommunication.CONNECT_SUCCEND || type == IActvityCommunication.MESSAGE_SEND_SUCCESS) {
            //连接成功 //消息发送成功
            fagmentsState();
        }
        if (type == IActvityCommunication.CONNECT_NO_ING || type == IActvityCommunication.CONNECT_FAILT) {
            // 连接失败
        }
    }

    //设置 fragments 状态
    private void fagmentsState() {
        if (loginPage!=null){
            loginPage.setConnectSuccess();
        }else{
            if (showPage!=null){
                showPage.switchState();
            }
        }

    }

    //吐司
    public void showTolas(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


}
