package com.sok.mphone.threads.interfaceDef;

/**
 * Created by user on 2016/12/19.
 */

public interface IActvityCommunication {
    public int CONNECT_SUCCEND = 0x00;//成功 0x00：ox表示16进制，00表示地址。
    public int CONNECT_ING = 0x01;//连接中
    public int CONNECT_FAILT = 0x02;//失败
    public int CONNECT_NO_ING = 0x03;//未连接
    public int MESSAGE_SEND_SUCCESS = 0x04; //APP - > SERVER ,MESSAGE OK
    public int MESSAGE_RECEIVE_SUCCESS = 0x05;// SERVER -> APP . MESSAGE OK
    public int  CONNECT_IS_ACCESS = 0x08;//有权限
    public int  CONNECT_IS_NOT_ACCESS = 0x09;//无权限
    public int CONNECT_ING_NOTFREE = 0x10;// 繁忙 - 结束服务
    public int CONNECT_ING_FREE = 0x11;//空闲

    void sendMessageToActivity(int type);
    void sendMessageToActivity(String message);
}
