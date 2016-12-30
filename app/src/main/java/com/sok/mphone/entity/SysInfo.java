package com.sok.mphone.entity;

import com.sok.mphone.tools.AppsTools;

import cn.trinea.android.common.util.FileUtils;

/**
 * Created by user on 2016/12/19.
 * 用户信息
 */

public class SysInfo {



    interface KEYS {
        String SERVER_IP = "SERVER_IP";
        String SERVER_PORT = "SERVER_PORT";
        String APP_MAC = "APP_MAC";
        String CONNECT_STATE = "CONNECT_STATE";
        String COMMUNICATION_STATE = "COMMUNICATION_STATE";
        String CONNECT_POWER = "CONNECT_POWER";
        String CALL_STATE = "CALL_STATE";
    }

    public interface CONN_STATES {
        String CONN_SUCCESS = "success";
        String CONN_FAILT = "failt";
    }
    public interface COMUNICATE_STATES{
        String COMMUNI_CALL = "hu_jiao_zhong";//呼叫中
        String COMMUNI_NO_MESSAGE = "mei_xiao_xi";//没消息
    }
    public interface CALL_STATE{
        String CALL_EXIST_TASK = "KONGXIAN";//空闲的
        String CALL_NOT_TASK = "FANMAN";//正在服务中的
    }


    public interface COMUNICATE_POWER{
        String COMMUNI_NO_ACCESS = "WU_QUAN_XIAN";//无权限
        String COMMUNI_ACCESS = "you_quan_xian";//有权限
    }


    private static final String infos = "/mnt/sdcard/wosapp.conf";

    private String serverIp;
    private String serverPort;
    private String appMac;
    private String connectState =  CONN_STATES.CONN_FAILT;//连接状态

    private String communicationState = COMUNICATE_STATES.COMMUNI_NO_MESSAGE;//没消息
    private String callState = CALL_STATE.CALL_EXIST_TASK;
    private String connectPower = COMUNICATE_POWER.COMMUNI_ACCESS;



    //构造
    private SysInfo() {
        contentEntity = new DataListEntity();
    }

    private static SysInfo sysInfo;//单例

    public static SysInfo get() {
        if (sysInfo == null) {
            sysInfo = new SysInfo();
        }
        return sysInfo;
    }
    public static SysInfo get(boolean isSync) {
        if (isSync) get().readInfo();
        return get();
    }



    public String getCallState() {
        return callState;
    }

    public void setCallState(String callState) {
        if (callState.equals(CALL_STATE.CALL_EXIST_TASK) || callState.equals(CALL_STATE.CALL_NOT_TASK)){
            this.callState = callState;
        }
    }
    public void setCallState(String callState,boolean flag) {
        setCallState(callState);
        if (flag) writeInfo();
    }

    //是否 有消息任务中
    public boolean isMessageTask(){
        return callState.equals(CALL_STATE.CALL_EXIST_TASK);
    }

    //是否有消息
    public boolean isHasMessage(){
        return communicationState.equals(COMUNICATE_STATES.COMMUNI_CALL);
    }

    public String getConnectPower() {
        return connectPower;
    }

    //权限
    public void setConnectPower(String connectPower) {
        if (communicationState.equals(COMUNICATE_POWER.COMMUNI_ACCESS) || communicationState.equals(COMUNICATE_POWER.COMMUNI_NO_ACCESS)){
            this.connectPower = connectPower;
        }
    }
    //是否有权限
    public boolean isAccess() {
        return connectPower.equals(COMUNICATE_POWER.COMMUNI_ACCESS);
    }

    //是否配置服务器信息
    public boolean isConfig() {
        return isConfig;
    }

    //是否可以连接服务器
    public boolean isConnected(){
        return connectState.equals(CONN_STATES.CONN_SUCCESS);
    }

    public String getCommunicationState() {
        return communicationState;
    }

    public void setCommunicationState(String communicationState) {
        if (communicationState.equals(COMUNICATE_STATES.COMMUNI_CALL) || communicationState.equals(COMUNICATE_STATES.COMMUNI_NO_MESSAGE)){
            this.communicationState = communicationState;
        }

    }
    public void setCommunicationState(String communicationState,boolean isSave){
        setCommunicationState(communicationState);
        if (isSave) writeInfo();
    }

    public String getConnectState() {
        return connectState;
    }

    public void setConnectState(String connectState) {
        if (connectState.equals(CONN_STATES.CONN_SUCCESS) || connectState.equals(CONN_STATES.CONN_FAILT)) {
            this.connectState = connectState;
        }
    }

    public void setConnectState(String connectState,boolean isSave) {
        if (connectState.equals(CONN_STATES.CONN_SUCCESS) || connectState.equals(CONN_STATES.CONN_FAILT)) {
            this.connectState = connectState;
        }
        if (isSave) writeInfo();
    }

    private boolean isConfig = false;//是否配置完成

    public String getServerIp() {
        return serverIp;
    }
    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }
    public String getServerPort() {
        return serverPort;
    }
    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }
    private DataListEntity contentEntity;
    public String getAppMac() {
        return appMac;
    }
    public void setAppMac(String appMac) {
        this.appMac = appMac;
    }





    //读取信息
    private synchronized void readInfo() {
        try {
            if (FileUtils.isFileExist(infos)) {
                StringBuilder content = FileUtils.readFile(infos, "UTF-8");//
                String var;
                if (content != null && !"".equals(var = content.toString())) {

                    var = AppsTools.justResultIsBase64decode(var);
                    contentEntity.setMap(AppsTools.jsonTxtToMap(var));
                    readValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //保存信息
    public synchronized void writeInfo() {
        try {
            writeValue();
            String content = AppsTools.mapToJson(contentEntity.getMap());//map->文本
            content = AppsTools.justResultIsBase64encode(content);//加密
            FileUtils.writeFile(infos, content);//写入数据
            isConfig = true;
        } catch (Exception e) {
            isConfig = false;
            //删除文件
            FileUtils.deleteFile(infos);
            e.printStackTrace();
        }
    }



    //初始化数据- 赋值
    private void readValue() {
        try {
            this.setServerIp(contentEntity.GetStringDefualt(KEYS.SERVER_IP));
            this.setServerPort(contentEntity.GetStringDefualt(KEYS.SERVER_PORT));
            this.setAppMac(contentEntity.GetStringDefualt(KEYS.APP_MAC));
            this.setConnectState(contentEntity.GetStringDefualt(KEYS.CONNECT_STATE));
            this.setCommunicationState(contentEntity.GetStringDefualt(KEYS.COMMUNICATION_STATE));
            this.setConnectPower(contentEntity.GetStringDefualt(KEYS.CONNECT_POWER));
            this.setCallState(contentEntity.GetStringDefualt(KEYS.CALL_STATE));
            this.isConfig = true;
        } catch (Exception e) {
            e.printStackTrace();
            isConfig = false;
            FileUtils.deleteFile(infos);
        }
    }

    private void writeValue() {
        try {
            contentEntity.clear();
            contentEntity.put(KEYS.SERVER_IP, AppsTools.justIsEnptyToString(getServerIp()));
            contentEntity.put(KEYS.SERVER_PORT, AppsTools.justIsEnptyToString(getServerPort()));
            contentEntity.put(KEYS.APP_MAC, AppsTools.justIsEnptyToString(getAppMac()));
            contentEntity.put(KEYS.CONNECT_STATE, AppsTools.justIsEnptyToString(getConnectState()));
            contentEntity.put(KEYS.COMMUNICATION_STATE, AppsTools.justIsEnptyToString(getCommunicationState()));
            contentEntity.put(KEYS.CONNECT_POWER, AppsTools.justIsEnptyToString(getConnectPower()));
            contentEntity.put(KEYS.CALL_STATE, AppsTools.justIsEnptyToString(getCallState()));
        } catch (NullPointerException e) {
            e.printStackTrace();
            FileUtils.deleteFile(infos);
        }
    }


}
