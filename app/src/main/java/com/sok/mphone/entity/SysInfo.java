package com.sok.mphone.entity;

import com.sok.mphone.tools.AppsTools;

import java.util.HashMap;

import cn.trinea.android.common.util.FileUtils;

/**
 * Created by user on 2016/12/19.
 * 用户信息
 */

public class SysInfo {
    public static final String TAG = "系统配置";
    interface KEYS {
        String CONFIG_INFO = "CONFIG_INFO";
        String SERVER_IP = "SERVER_IP";
        String SERVER_PORT = "SERVER_PORT";
        String JOB_NUMBER = "JOB_NUMBER";
        String APP_MAC = "APP_MAC";
        String CONNECT_STATE = "CONNECT_STATE";
        String COMMUNICATION_STATE = "COMMUNICATION_STATE";
        String CONNECT_POWER = "CONNECT_POWER";
        String CALL_STATE = "CALL_STATE";
        String LOCAL_CONNECT = "LOCAL_CONNECT";
    }
    public interface IFCONFIG{
        String CONFIG_SUCCESS = "config_success";
        String CONFIG_FAILT = "config_failt";
    }
    public interface CONN_STATES {
        String CONN_SUCCESS = "connect_success";
        String CONN_FAILT = "connect_failt";
    }
    public interface COMUNICATE_STATES{
        String COMMUNI_CALL = "calling";//呼叫中
        String COMMUNI_NO_MESSAGE = "numessage";//没消息
    }
    public interface CALL_STATE{
        String CALL_EXIST_TASK = "free";//空闲的
        String CALL_NOT_TASK = "busy";//正在服务中的
    }
    public interface COMUNICATE_POWER{
        String COMMUNI_NO_ACCESS = "connect_server_not_access";//无权限
        String COMMUNI_ACCESS = "connect_server_access";//有权限
    }
    public interface LOCAL_CONNECT{
        String LOCAL_CONNECT_ENABLE="enable_connect_server"; //本地允许链接服务器
        String LOCAL_CONNECT_UNENABLE="unenable_connect_server";//本地不连接服务器
    }

    private static final String CONFIG_FILE = "/mnt/sdcard/mphone.config";
    private static final String COMMUNICATION_FILE = "/mnt/sdcard/mphone.communication";

    public static final int CONFIG = 1;
    public static final int COMUNICATION = 2;

    //配置
    private DataListEntity configEntity;//配置信息
    private DataListEntity comnicationEntity;//通讯信息
    private String configInfo = IFCONFIG.CONFIG_FAILT;
    private String serverIp = "";
    private String serverPort = "";
    private String jobNumber = "";
    private String appMac = "";
    private String localConnect = LOCAL_CONNECT.LOCAL_CONNECT_UNENABLE;//默认不允许
    //通讯
    private String connectState =  CONN_STATES.CONN_FAILT;//连接状态
    private String communicationState = COMUNICATE_STATES.COMMUNI_NO_MESSAGE;//没消息
    private String callState = CALL_STATE.CALL_EXIST_TASK;
    private String connectPower = COMUNICATE_POWER.COMMUNI_ACCESS;//默认有权限接入服务器



    //构造
    private SysInfo() {
        configEntity = new DataListEntity();
        comnicationEntity = new DataListEntity();
    }
    private static SysInfo sysInfo;//单例

    //1 - 本地配置信息
    //2 - 通讯信息
    public static SysInfo get(int type) {
        if (sysInfo == null) {
            sysInfo = new SysInfo();
        }
        sysInfo.readInfo(type);
        return sysInfo;
    }


    public String getConfigInfo() {
        return configInfo;
    }

    public void setConfigInfo(String configInfo) {
        if (configInfo.equals(IFCONFIG.CONFIG_FAILT) || configInfo.equals(IFCONFIG.CONFIG_SUCCESS)){
            this.configInfo = configInfo;
        }
    }




    public String getLocalConnect() {
        return localConnect;
    }
    public void setLocalConnect(String localConnect) {
        if (localConnect.equals(LOCAL_CONNECT.LOCAL_CONNECT_ENABLE) || localConnect.equals(LOCAL_CONNECT.LOCAL_CONNECT_UNENABLE)){
            this.localConnect = localConnect;
        }
    }
    public void setLocalConnect(String localConnect,boolean flag) {
        setLocalConnect(localConnect);
        if (flag) writeInfo(CONFIG);
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
        if (flag) writeInfo(COMUNICATION);
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
    public void setConnectPower(String connectPower,boolean flag) {
        setConnectPower(connectPower);
        if (flag) writeInfo(COMUNICATION);
    }
    //是否有权限
    public boolean isAccess() {
        return connectPower.equals(COMUNICATE_POWER.COMMUNI_ACCESS);
    }
    //是否配置服务器信息
    public boolean isConfig() {
        return configInfo.equals(IFCONFIG.CONFIG_SUCCESS);
    }
    //是否可以连接服务器
    public boolean isConnected(){
        return connectState.equals(CONN_STATES.CONN_SUCCESS);
    }
    public boolean isLocalConnect() {
        return localConnect.equals(LOCAL_CONNECT.LOCAL_CONNECT_ENABLE);
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
        if (isSave) writeInfo(COMUNICATION);
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
        if (isSave) writeInfo(COMUNICATION);
    }
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
    public String getAppMac() {
        return appMac;
    }
    public void setAppMac(String appMac) {
        this.appMac = appMac;
    }

    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }

    //读取信息
    private synchronized void readInfo(int type) {
        String file = null;
        if (type == CONFIG){
            file = CONFIG_FILE;
        }
        if (type == COMUNICATION){
            file = COMMUNICATION_FILE;
        }
//        Log.e(TAG,"准备读取文件:"+file);
        if (file==null) return;
        try {
            if (FileUtils.isFileExist(file)) {
                StringBuilder content = FileUtils.readFile(file, "UTF-8");
                String var;
                if (content != null && !"".equals(var = content.toString())) {
                    var = AppsTools.justResultIsBase64decode(var);
                    //Log.e(TAG,"读取内容:\n"+var);
                    readValue(type,AppsTools.jsonTxtToMap(var));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //保存信息
    public  synchronized boolean writeInfo(int type) {
        DataListEntity entity = writeValue(type);
        String file = null;
        if (type == CONFIG){
            file = CONFIG_FILE;
        }
        if (type == COMUNICATION){
            file = COMMUNICATION_FILE;
        }
        if (file==null || entity==null) return false;
        try {
            String content = AppsTools.mapToJson(entity.getMap());//map->文本
            content = AppsTools.justResultIsBase64encode(content);//加密
            return FileUtils.writeFile(file, content);//写入数据
        } catch (Exception e) {
            //删除文件
            FileUtils.deleteFile(CONFIG_FILE);
            FileUtils.deleteFile(COMMUNICATION_FILE);
            e.printStackTrace();
        }
        return false;
    }


    //初始化数据- 赋值
    private void readValue(int type, HashMap<String,String> map) {
        try {
            if (type == CONFIG){
                configEntity.setMap(map);
                this.setServerIp(configEntity.GetStringDefualt(KEYS.SERVER_IP));//ip
                this.setServerPort(configEntity.GetStringDefualt(KEYS.SERVER_PORT));//port
                this.setJobNumber(configEntity.GetStringDefualt(KEYS.JOB_NUMBER));//job number
                this.setAppMac(configEntity.GetStringDefualt(KEYS.APP_MAC));
                this.setLocalConnect(configEntity.GetStringDefualt(KEYS.LOCAL_CONNECT));//本地授权
                this.setConfigInfo(configEntity.GetStringDefualt(KEYS.CONFIG_INFO));
            }
            if (type == COMUNICATION){
                comnicationEntity.setMap(map);
                this.setConnectState(comnicationEntity.GetStringDefualt(KEYS.CONNECT_STATE));//连接状态
                this.setCommunicationState(comnicationEntity.GetStringDefualt(KEYS.COMMUNICATION_STATE));//通讯状态
                this.setConnectPower(comnicationEntity.GetStringDefualt(KEYS.CONNECT_POWER));//通讯权限
                this.setCallState(comnicationEntity.GetStringDefualt(KEYS.CALL_STATE));//呼叫状态
            }
        } catch (Exception e) {
            e.printStackTrace();
            FileUtils.deleteFile(CONFIG_FILE);
            FileUtils.deleteFile(COMMUNICATION_FILE);
        }

    }

    private DataListEntity writeValue(int type) {

        try {
            if (type == COMUNICATION){
                comnicationEntity.clear();
                comnicationEntity.put(KEYS.CONNECT_STATE, AppsTools.justIsEnptyToString(getConnectState()));
                comnicationEntity.put(KEYS.COMMUNICATION_STATE, AppsTools.justIsEnptyToString(getCommunicationState()));
                comnicationEntity.put(KEYS.CONNECT_POWER, AppsTools.justIsEnptyToString(getConnectPower()));
                comnicationEntity.put(KEYS.CALL_STATE, AppsTools.justIsEnptyToString(getCallState()));
                return comnicationEntity;
            }
            if (type == CONFIG){
                configEntity.clear();
                configEntity.put(KEYS.SERVER_IP, AppsTools.justIsEnptyToString(getServerIp()));
                configEntity.put(KEYS.SERVER_PORT, AppsTools.justIsEnptyToString(getServerPort()));
                configEntity.put(KEYS.JOB_NUMBER, AppsTools.justIsEnptyToString(getJobNumber()));
                configEntity.put(KEYS.APP_MAC, AppsTools.justIsEnptyToString(getAppMac()));
                configEntity.put(KEYS.LOCAL_CONNECT, AppsTools.justIsEnptyToString(getLocalConnect()));
                configEntity.put(KEYS.CONFIG_INFO,AppsTools.justIsEnptyToString(getConfigInfo()));
                return configEntity;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            FileUtils.deleteFile(CONFIG_FILE);
            FileUtils.deleteFile(COMMUNICATION_FILE);
        }
        return null;
    }

}
