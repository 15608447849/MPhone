package com.sok.mphone.dataEntity;

import com.sok.mphone.tools.AppsTools;

import java.util.HashMap;

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
    }

    public interface ConnectStates {
        String connectSuccess = "success";
        String connectFailt = "failt";
    }

    private static final String infos = "/mnt/sdcard/wosapp.conf";

    private String serverIp;
    private String serverPort;
    private String appMac;
    private String connectState =  ConnectStates.connectFailt;

    //构造
    private SysInfo() {
        contentEntity = new DataListEntity();
    }

    private static SysInfo sysInfo;//单例

    public static SysInfo get() {
        if (sysInfo == null) {
            sysInfo = new SysInfo();
        }
        if (!sysInfo.isConfig()) sysInfo.initRead();  //如果未配置完成 再次读取
        return sysInfo;
    }
    //是否配置
    public boolean isConfig() {
        return isConfig;
    }
    //是否可连接
    public boolean isConnected(){
                initRead();
        return connectState.equals(ConnectStates.connectSuccess);
    }

    public String getConnectState() {
        return connectState;
    }

    public void setConnectState(String connectState) {
        if (connectState.equals(ConnectStates.connectSuccess) || connectState.equals(ConnectStates.connectFailt)) {
            this.connectState = connectState;
        }
    }
    public void setConnectState(String connectState,boolean isSave) {
        if (connectState.equals(ConnectStates.connectSuccess) || connectState.equals(ConnectStates.connectFailt)) {
            this.connectState = connectState;
        }
        if (isSave) saveInfo();
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



    private void initRead() {
        if (FileUtils.isFileExist(infos)) {
            //读取  内容转成 map
            readInfo();
        } else {
            isConfig = false;
        }
    }

    //读取信息
    private void readInfo() {
        try {
            StringBuilder content = FileUtils.readFile(infos, "UTF-8");//
            String var;
            if (content != null && !"".equals(var = content.toString())) {

                var = AppsTools.justResultIsBase64decode(var);
                contentEntity.setMap(AppsTools.jsonTxtToMap(var));
                readValue();
            }
        } catch (Exception e) {
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
            this.isConfig = true;
        } catch (Exception e) {
            e.printStackTrace();
            isConfig = false;
        }
    }

    //保存信息
    public synchronized void saveInfo() {
        try {
            HashMap<String, String> map = contentEntity.getMap();
            map.clear();
            map.put(KEYS.SERVER_IP, AppsTools.justIsEnptyToString(getServerIp()));
            map.put(KEYS.SERVER_PORT, AppsTools.justIsEnptyToString(getServerPort()));
            map.put(KEYS.APP_MAC, AppsTools.justIsEnptyToString(getAppMac()));
            map.put(KEYS.CONNECT_STATE, AppsTools.justIsEnptyToString(getConnectState()));

            String content = AppsTools.mapToJson(map);//map->文本
            content = AppsTools.justResultIsBase64encode(content);//加密
            FileUtils.writeFile(infos, content);//写入数据
            isConfig = true;
        } catch (Exception e) {
            e.printStackTrace();
            isConfig = false;
            //删除文件
            FileUtils.deleteFile(infos);
        }
    }



}
