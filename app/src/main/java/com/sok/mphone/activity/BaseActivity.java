package com.sok.mphone.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.Toast;

import com.sok.mphone.R;
import com.sok.mphone.dataEntity.SysInfo;
import com.sok.mphone.fragments.IFragmentsFactory;
import com.sok.mphone.fragments.LoginFragments;
import com.sok.mphone.services.CommuntBroadCasd;
import com.sok.mphone.services.CommuntServer;
import com.sok.mphone.tools.log;

public class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity容器";

    private Fragment loginPage;
    private Fragment showPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        registBroad();
        initFragments();
        addPages();
    }


    private void initFragments() {

        if (showPage == null)
            showPage = IFragmentsFactory.getInstans(IFragmentsFactory.Type.show_page);

        if (!SysInfo.get().isConnected()) {
            if (loginPage == null)
                loginPage = IFragmentsFactory.getInstans(IFragmentsFactory.Type.login_page);
        } else {
            startCommunication();
        }

    }

    private void addPages() {
        if (loginPage != null) {
            IFragmentsFactory.addFragment(getFragmentManager().beginTransaction(), R.id.base_layout_2, loginPage, IFragmentsFactory.Type.login_page + "");
        }
        if (showPage != null) {
            IFragmentsFactory.addFragment(getFragmentManager().beginTransaction(), R.id.base_layout_3, showPage, IFragmentsFactory.Type.show_page + "");
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

    //打开通讯线程
    public void startCommunication() {
        this.startService(new Intent(this, CommuntServer.class));
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
        log.i(TAG, "连接成功 - " + type);
        if (loginPage != null) {
            ((LoginFragments) loginPage).setConnectSuccess();
        }
    }


    public void showTolas(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


}
