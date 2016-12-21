package com.sok.mphone.threads.interfaceImp;

import com.sok.mphone.entity.SocketBeads;
import com.sok.mphone.threads.interfaceDef.IThread;

/**
 * Created by lzp on 2016/12/19.
 * 发送服务器消息
 */

public class SendMsgThread extends IThread {
    private SocketBeads sBean;

    public SendMsgThread(SocketBeads sBean) {
        this.sBean = sBean;
    }

    @Override
    public void run() {
        if (sBean == null) return;

        while (!isStop) {
            if (sBean.isConnected()){
                //已经连接
                //获取一个消息
                try {
                    sBean.sendMessage();
                } catch (Exception e) {
                    mError(e);
                    //关闭连接
                    sBean.desConnection();
                }
            }
        }
    }
}
