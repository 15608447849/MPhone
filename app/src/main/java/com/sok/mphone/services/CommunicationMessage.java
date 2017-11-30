package com.sok.mphone.services;

import com.sok.mphone.entity.SysInfo;

import static com.sok.mphone.entity.SysInfo.*;
import static com.sok.mphone.entity.SysInfo.CONFIG;
import static com.sok.mphone.tools.CommunicationProtocol.*;

/**
 * Created by user on 2017/11/30.
 */

class CommunicationMessage {
    private int type = -1;

    public void setType(String paramString)
    {
        if (paramString == null || paramString.trim().length()==0) return;

        if (paramString.equals("6660")) {//奇景
            type = 1;
        } else if(paramString.equals("23333")){//颖网
            type = 0;
        }

    }

    //心跳
    public String HRBT()
    {
        if (this.type == 0) {
            return AHBT+PSM;
        }
        if (this.type == 1) {
            return "0";
        }
        return null;
    }

    //上线
    public String ONLI(CommuntServer paramCommuntServer)
    {
        if (this.type == 0)
        {

            if (SysInfo.get(COMUNICATION).isHasMessage()) {
                return SYM+Receive_Calls_With_Out_The_Click_Of_A_Button;
            }else{
                return AHOL+PSM+SysInfo.get(CONFIG).getAppMac()+SYM+SysInfo.get(CONFIG).getJobNumber();
            }
        }else
        if (this.type == 1)
        {
            paramCommuntServer.postTask(SNTY, CMD_FREE);
            return "1|" + SysInfo.get(CONFIG).getJobNumber() + "|1|" + SysInfo.get(CONFIG).getAppMac();
        }
        return null;
    }

    public String[] RECV(String paramString)
    {
        if (paramString!=null && paramString.trim().length()>0) {
            if (type == 0){
                return paramString.split(PSM);
            }else if (type == 1){
                String[] array = new String[2];
                array[0] = SNTY;
                if (paramString.equals("2")) {
                    array[1] = CMD_CALL_ING;
                }else
                if (paramString.equals("3")) {
                    array[1] = CMD_FREE;
                }else if (paramString.equals("0")){
                    array[1] = "0";
                }
                return array;
            }
        }

      return null;
    }

    public String RESP(String paramString)
    {
        int i = -1;
        if (paramString.equals(APP_REFUSE)) {
            i = 0;
        }
        if (paramString.equals(APP_RECEIVE)) {
            i = 1;
        }
        if (paramString.equals(APP_LOCAL_SERVER_OVER)) {
            i = 2;
        }
        if (paramString.equals(APP_CONNECT_CLOSE)) {
            i = 3;
        }
        if (this.type == 0)
        {
            if (i == 0) {
                return ANTY+PSM+RECIPT_REFUSE_SERVER+"-" + SysInfo.get(CONFIG).getAppMac();
            }
            if (i == 1) {
                return ANTY+PSM+RECIPT_ACCEPT_SERVER+"-" + SysInfo.get(CONFIG).getAppMac();
            }
            if (i == 2) {
                return ANTY+PSM+RECIPT_OVER_SERVER+"-" + SysInfo.get(CONFIG).getAppMac();
            }
        }
        if (this.type == 1)
        {
            if (i == 0) {
                return "4";
            }
            if (i == 1) {
                return "5";
            }
            if (i == 3) {
                return "1|" + SysInfo.get(CONFIG).getJobNumber() + "|0|" + SysInfo.get(CONFIG).getAppMac();
            }
        }
        return null;
    }


}
