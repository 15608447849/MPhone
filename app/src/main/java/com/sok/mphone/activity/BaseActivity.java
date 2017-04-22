package com.sok.mphone.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.widget.Toast;

import com.sok.mphone.R;
import com.sok.mphone.entity.SysInfo;
import com.sok.mphone.fragments.IFragmentsFactory;
import com.sok.mphone.fragments.LoginFragments;
import com.sok.mphone.fragments.ShowFragments;
import com.sok.mphone.services.CommuntBroadCasd;
import com.sok.mphone.services.CommuntServer;
import com.sok.mphone.threads.interfaceDef.IActvityCommunication;
import com.sok.mphone.tools.log;

public class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity容器";

    private LoginFragments loginPage;
    private ShowFragments showPage;


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.activity_open, R.anim.activity_close);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        //如果6.0 - 23 以上
        if (Integer.parseInt(Build.VERSION.SDK)>=23){
            if ( ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ){
                startActivity(new Intent(this,PermissionActivity.class));
                finish();
                return;
            }
        }

        registBroad();
        initFragments(true);
    }


    @Override
    protected void onResume() {
        super.onResume();
//        log.e(TAG, "onResume - ");
    }

    @Override
    protected void onStart() {
        super.onStart();
//        log.e(TAG, "onStart - ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        log.e(TAG, "onRestart - ");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void finish() {
        super.finish();
        //关闭窗体动画显示
        overridePendingTransition(R.anim.activity_open, R.anim.activity_close);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//      deleteAllPage(true);
        unregistBroad();
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
//        log.e(TAG, "onNewIntent - " + intent);
    }

    public void initFragments(boolean isflag) {
        deleteAllPage(false);
        swithPage();
    }


    //删除所有 page
    private void deleteAllPage(boolean flag) {
        log.i(TAG,"deleteAllPage() - "+flag);

        if (loginPage != null) {
            IFragmentsFactory.removeFragment(getFragmentManager().beginTransaction(), loginPage);
            loginPage = null;
        }
        if (showPage != null) {
            IFragmentsFactory.removeFragment(getFragmentManager().beginTransaction(), showPage);
            showPage = null;
        }
    }

    //选择页面
    private void swithPage() {
        boolean flag =SysInfo.get(SysInfo.COMUNICATION).isConnected();
        log.i(TAG," swithPage() 当前连接状态:"+flag);
        //如果 已经 --> 1配置 2本地允许接入 3正在链接中 4.有权限访问
        if (SysInfo.get(SysInfo.CONFIG).isConfig() &&
                SysInfo.get(SysInfo.CONFIG).isLocalConnect() &&
                flag //&& SysInfo.get(SysInfo.COMUNICATION).isAccess()
               ) {// 已配置 并且 可连接  - > 显示 show页面 ,关闭 login页面 ->
            log.i(TAG,"  show page - -");
            if (showPage == null) {
                showPage = (ShowFragments) IFragmentsFactory.getInstans(IFragmentsFactory.Type.show_page);
            }
            IFragmentsFactory.repeateFragment(getFragmentManager().beginTransaction(), R.id.base_layout_3, showPage);
        } else {
            log.i(TAG,"  login page - -");
            if (loginPage == null) {
                loginPage = (LoginFragments) IFragmentsFactory.getInstans(IFragmentsFactory.Type.login_page);
            }
            IFragmentsFactory.repeateFragment(getFragmentManager().beginTransaction(), R.id.base_layout_2, loginPage);
        }
        startCommunication();//再次尝试打开服务
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

        if (type == IActvityCommunication.CONNECT_SUCCEND) {
            //连接成功
            log.i(TAG, "activity 收到 - 连接返回值类型 - type [" + type + "] - 连接成功");
            if (loginPage != null) {//如果现在是登陆页面
                loginPage.setConnectSuccess();
            }
        }
        if (type == IActvityCommunication.MESSAGE_SEND_SUCCESS) {
            // 消息发送成功
//            log.i(TAG, "activity 收到 - 连接返回值类型 - type [" + type + "] - 消息发送成功");
//            if (showPage != null) {//现在 - 显示状态
//                showPage.setMessageSendSuccess();
//            }
        }
        if (type == IActvityCommunication.CONNECT_FAILT) {
            // 连接失败
            log.i(TAG, "activity 收到 - 连接返回值类型 - type [" + type + "] - 连接失败");
            if (showPage != null) {//现在是显示状态
                showPage.setConnectFailt();
            }
            if (loginPage!=null){
                loginPage.setConnectFailt();
            }
        }
        if (type == IActvityCommunication.CONNECT_IS_NOT_ACCESS) {
            // 无权访问
            log.i(TAG, "activity 收到 - 连接返回值类型 - type [" + type + "] - 无权访问服务器");
//            showTolas("无访问权限,请联系客服");
            stopCommunication();
            //设置没有权限访问服务器(在server中设置)
            startActivity(new Intent(this,ServerNotPermissionActivity.class));
            //弹出提示窗口
            this.finish();
        }
        if (type == IActvityCommunication.CONNECT_ING_FREE){
            //空闲
            log.i(TAG, "activity 收到 - 连接返回值类型 - type [" + type + "] - 空闲");
            if (showPage != null) {//现在 - 显示状态
                showPage.setMessageSendSuccess(0);
                showPage.setMessageSendSuccess(1);
            }
        }

        if (type == IActvityCommunication.CONNECT_ING_NOTFREE){ //繁忙
            log.i(TAG, "activity 收到 - 连接返回值类型 - type [" + type + "] - 繁忙");
            if (showPage != null) {//显示 - 结束服务
                showPage.showOverButton();
            }
        }






    }


    //吐司
    public void showTolas(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}
