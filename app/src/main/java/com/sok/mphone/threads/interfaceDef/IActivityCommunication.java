package com.sok.mphone.threads.interfaceDef;

/**
 * Created by user on 2016/12/19.
 */

public interface IActivityCommunication {
    int CONNECT_SUCCEND = 0x00;//成功 0x00：ox表示16进制，00表示地址。
    int CONNECT_ING = 0x01;//连接中
    int CONNECT_FAILT = 0x02;//失败
    int CONNECT_NO_ING = 0x03;//未连接
    int MESSAGE_SEND_SUCCESS = 0x04; //APP - > SERVER ,MESSAGE OK
    int MESSAGE_RECEIVE_SUCCESS = 0x05;// SERVER -> APP . MESSAGE OK
    int  CONNECT_IS_ACCESS = 0x08;//有权限
    int  CONNECT_IS_NOT_ACCESS = 0x09;//无权限
    int CONNECT_ING_NOTFREE = 0x10;// 繁忙 - 结束服务
    int CONNECT_ING_FREE = 0x11;//空闲
    int CONNECT_STOP = 0x22;//停止连接
    void sendMessageToActivity(int type);
    void sendMessageToActivity(String message);
}
