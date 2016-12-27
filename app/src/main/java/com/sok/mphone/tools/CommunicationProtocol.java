package com.sok.mphone.tools;

/**
 * Created by user on 2016/12/19.
 */

public interface CommunicationProtocol {
    //app 上线
    String AHOL = "AHOL:";

    // app 心跳 -  AHBT:macaddress
    String AHBT = "AHBT:";
    // 服务器 ->app 通知
    String SNTY = "SNTY:";
    //String app->服务器
    String ANTY = "ANTY:";
    String RECIPT_ACCEPT_SERVER = "[202]";
    String RECIPT_REFUSE_SERVER = "[406]";
    String RECIPT_OVER_SERVER = "[205]";
}
