package com.sok.mphone.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by user on 2016/12/19.
 */

public class CommuntBroadCasd extends BroadcastReceiver {
    private static final String TAG = "CommuntBroadCasd";
    public static final String ACTION = "com.mphone.communication.receivebroad";
    public static final String PARAM1 = "param1";
    //通讯服务
    private CommuntServer commServer;
    public CommuntBroadCasd(CommuntServer commServer){
        this.commServer = commServer;



    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (commServer!=null){
            commServer.receiveAppMsg(intent.getExtras().getString(PARAM1));
        }
    }
}
