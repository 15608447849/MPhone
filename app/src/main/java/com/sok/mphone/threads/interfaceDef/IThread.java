package com.sok.mphone.threads.interfaceDef;

import android.util.Log;

import com.sok.mphone.tools.log;

/**
 * Created by user on 2016/12/19.
 */

public abstract class IThread extends Thread implements IThreadCommunication{
    protected static final String TAG = "IThread";


    protected volatile boolean isStop = true;


    //是否开始中?
    public boolean isStart(){
        return !isStop;
    }

    //开始
    public void mStart() {
        isStop = false;
    }

    //结束
    public void mStop() {
        isStop = true;
    }

    //重新开始
    public void mRestart() {

    }
    public void mError(Exception e) {
        Log.e(TAG, log.ERR_LOG + e.getMessage());
    }
    @Override
    public void sendMessageToThread(Object data) {
        //收到别人给我的消息
        if (data==null)  return;
//        log.i(TAG,"收到消息 -> 到服务器 : [ "+ data.toString()+ " ]");
    }
}
