package com.sok.mphone.threads.interfaceImp;

import com.sok.mphone.entity.SocketBeads;
import com.sok.mphone.threads.interfaceDef.IActvityCommunication;
import com.sok.mphone.threads.interfaceDef.IThread;
import com.sok.mphone.tools.log;

/**
 * Created by lizhaoping on 2016/12/19.
 * 连接服务器线程
 */

public class CommunicationThread extends IThread {

    private SocketBeads sBean;
    private int reCreateConnTime = 3 * 1000;
    private IActvityCommunication iActivity;


    private SendMsgThread sender;
    private ReceiveThread receiver;

    public CommunicationThread(SocketBeads sBean) {
        this.sBean = sBean;
        log.i("通讯线程","创建");
    }

    public CommunicationThread(IActvityCommunication iActivity, SocketBeads sBean) {
        this(sBean);
        this.iActivity = iActivity;
    }

    public CommunicationThread(SocketBeads sBean, int reCreateConnTime, IActvityCommunication iActivity) {
        this(iActivity, sBean);
        this.reCreateConnTime = reCreateConnTime;
    }

    @Override
    public void mStart() {
        super.mStart();
        receiver = new ReceiveThread(sBean, iActivity);
        receiver.mStart();
        receiver.start();
        sender = new SendMsgThread(sBean);
        sender.mStart();
        sender.start();
    }

    @Override
    public void mStop() {
        super.mStop();
        if (sender != null) {
            sender.mStop();
            sender = null;
        }
        if (receiver != null) {
            receiver.mStop();
            receiver = null;
        }
    }

    @Override
    public void run() {
        if (sBean == null) return;
        while (!isStop) {
            if (sBean.isConnected()) {
                //连接成功发送心跳
                if (iActivity != null)
                    iActivity.sendMessageToActivity(IActvityCommunication.CONNECT_ING);

            } else {
                //创建链接  - 1 连接成功
                if (sBean.createConnect()) {
                    if (iActivity != null)
                        iActivity.sendMessageToActivity(IActvityCommunication.CONNECT_SUCCEND);
                } else {
                    if (iActivity != null)
                        iActivity.sendMessageToActivity(IActvityCommunication.CONNECT_FAILT);
                }
            }
            try {
                Thread.sleep(reCreateConnTime);
            } catch (InterruptedException e) {
                mError(e);
            }
        }
    }

    @Override
    public void sendMessageToThread(Object data) {
        super.sendMessageToThread(data);
        if (sBean != null && sBean.isConnected()) {
            sBean.putMessage(data.toString());
        }
    }

}
