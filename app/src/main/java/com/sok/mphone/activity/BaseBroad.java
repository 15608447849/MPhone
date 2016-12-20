package com.sok.mphone.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sok.mphone.tools.log;

/**
 * Created by user on 2016/12/19.
 */

public class BaseBroad extends BroadcastReceiver{
    private static final String TAG = "BaseBroad";
    public static final String ACTION = "com.mphone.activity.receivebroad";
    public static final String PARAM1 = "param1";
    public static final String PARAM2 = "param2";
    public static final String PARAM3 = "param3";

    private BaseActivity mActivity;

    public BaseBroad(BaseActivity mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int type = intent.getExtras().getInt(PARAM1);
        log.i(TAG," - - - - - - 收到广播");
        if (mActivity!=null){

            mActivity.receiveServerMessage(type);
        }
    }
}
