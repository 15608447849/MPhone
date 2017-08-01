package com.sok.mphone.threads.interfaceImp;

import com.sok.mphone.entity.SocketBeads;
import com.sok.mphone.threads.interfaceDef.IActivityCommunication;
import com.sok.mphone.threads.interfaceDef.IThread;

/**
 * Created by user on 2016/12/19.
 */

public class ReceiveThread extends IThread {

    private SocketBeads sBean;

    private IActivityCommunication iActivity;

    public ReceiveThread(SocketBeads sBean,IActivityCommunication iActivity) {
        this.sBean = sBean;
        this.iActivity = iActivity;
    }

    @Override
    public void run() {
        if (sBean == null) return;

        String message;
        while (!isStop) {
            if (sBean.isConnected()){
                //已经连接
                //获取一个消息
                try {
                    message = sBean.acceptMessage();
                    if (message!=null){
                       if (iActivity!=null){
                           iActivity.sendMessageToActivity(message);
                       }
                    }

                } catch (Exception e) {
                    mError(e);
                    //关闭连接
                    sBean.desConnection();
                }
            }
        }
    }




}
