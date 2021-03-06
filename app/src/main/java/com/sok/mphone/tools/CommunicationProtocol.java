package com.sok.mphone.tools;

/**
 * Created by user on 2016/12/19.
 */

public interface CommunicationProtocol {
    String SYM = "#";
    String PSM =":";
    //app 上线
    String AHOL = "AHOL";
    // app 心跳 -  AHBT:macaddress
    String AHBT = "AHBT";
    // 服务器 ->app 通知
    String SNTY = "SNTY";
    //String app->服务器
    String ANTY = "ANTY";
    String RECIPT_ACCEPT_SERVER = "[202]";
    String RECIPT_REFUSE_SERVER = "[406]";
    String RECIPT_OVER_SERVER = "[205]";
    //用按钮点击接收呼叫
    String Receive_Calls_With_Out_The_Click_Of_A_Button = "[300]";

    String CMD_CALL_ING = "true";//呼叫中
    String CMD_NOT_ACCESS = "false";//无权限
    String CMD_FREE = "free";//空闲
    String CMD_NOT_FREE = "nofree";//繁忙
    String CMD_OFLE = "close";//通知客户端自行下线
    //本地回执
    String APP_REFUSE = "app_refuse";
    String APP_RECEIVE = "app_receive";

    String APP_LOCAL_SERVER_OVER = "app_local_server_over";
    String APP_CONNECT_CLOSE = "app_connect_end";



}
