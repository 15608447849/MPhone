package com.sok.mphone.tools;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 2016/12/19.
 */

public class AppsTools {


    /**
     *
     * 函数名称: parseData
     * 函数描述: 将json字符串转换为map
     * @param data
     * @return
     */
    public static HashMap<String, String> jsonTxtToMap(String data){
        GsonBuilder gb = new GsonBuilder();
        Gson g = gb.create();
        HashMap<String, String> map = g.fromJson(data, new TypeToken<HashMap<String, String>>() {}.getType());
        return map;
    }

    /**
     * 将Map转化为Json文本
     *
     * @param map
     * @return String
     */
    public static <T> String mapToJson(Map<String, T> map) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(map);
        return jsonStr;
    }
    /**
     *  内容 base64 解密
     */
    public static String justResultIsBase64decode(String result){

        try {
            byte[] byteIcon = Base64.decode(result,Base64.DEFAULT);
            return new String(byteIcon,"UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 内容 base64 加密
     *
     */
    public static String justResultIsBase64encode(String result){

        try {
            byte[] byteIcon = Base64.encode(result.getBytes("UTF-8"),Base64.DEFAULT);
            return new String(byteIcon,"UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //判断不为空
    public static String justIsEnptyToString(String val) throws NullPointerException{

        if (val==null || val.equals("") || val.equals("null")){
            throw new NullPointerException("user-defined null Pointer exception");
        }
        return val;
    }




    public static String getMacAddress(Context context){

        String mac = getLocalMacAddressFromWifiInfo(context);
        if (mac==null)
                mac = getLocalMacAddressFromBusybox();
        return mac;
    }


    //根据Wifi信息获取本地Mac
    public static String getLocalMacAddressFromWifiInfo(Context context){
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }




    //获取mac 地址 函数
    /**
     * get mac
     */
    public static String getLocalMacAddressFromBusybox(){
        String result;

        result = callCmd("busybox ifconfig","HWaddr");

        //如果返回的result == null，则说明网络不可取
        if(result==null){
            System.out.println("网络出错，请检查网络");
            return null;
        }
        String Mac ;
        //对该行数据进行解析
        //例如：eth0      Link encap:Ethernet  HWaddr 00:16:E8:3E:DF:67
        if(result.length()>0 && result.contains("HWaddr")==true){
            Mac = result.substring(result.indexOf("HWaddr")+6, result.length()-1);

            if(Mac.length()>1){
                Mac = Mac.replaceAll(" ", "");
                result = "";
                String[] tmp = Mac.split(":");
                for(int i = 0;i<tmp.length;++i){
                    result +=tmp[i]+"-";
                }
            }
            result = Mac;
                   }
        return result;
    }
    private static String callCmd(String cmd,String filter) {
        String result =null;

        try {
            String line ;
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStreamReader is = new InputStreamReader(proc.getInputStream());
            BufferedReader br = new BufferedReader(is);
            //执行命令cmd，只取结果中含有filter的这一行
            while ((line = br.readLine ()) != null && line.contains(filter)== false) {
                System.out.println("line: "+line);
            }
            result = line;
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
