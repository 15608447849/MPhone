package com.sok.mphone.tools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

        String mac = null;
        if ( Integer.parseInt(Build.VERSION.SDK)>=23){
            mac = getMacAddrForSDK23();
        }
        if (mac==null || mac.equals("")){
            mac = getLocalMacAddressFromWifiInfo(context);
            if (mac==null || mac.equals(""))
                mac = getMacAddress();
            if (mac==null || mac.equals(""))
                mac = getLocalMacAddressFromBusybox();

        }
        StringBuilder result = new StringBuilder();
        if(mac.length()>1){
            mac = mac.replaceAll("\\s+", "");
            String[] tmp = mac.trim().split(":");
            for(int i = 0;i<tmp.length;++i){
                result.append(tmp[i]);
                if (i<tmp.length-1){
                   result.append("-");
                }
            }
        }else{
            result.append("00-00-00-00-00-00");
        }
        mac = result.toString();
        if (mac.equals("02-00-00-00-00-00")){
            mac = getBuildInfo();
        }

        return mac.toUpperCase();
    }
    //根据Wifi信息获取本地Mac
    public static String getLocalMacAddressFromWifiInfo(Context context){
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress()==null?"":info.getMacAddress();
    }
    //android 6.0 获取mac地址 02-00-00-00-00 解决
    public static String getMacAddrForSDK23() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    //本地以太网mac地址文件
    private static String getMacAddress()
    {
        String strMacAddr = "";
        byte[] b;
        try
        {
            NetworkInterface NIC = NetworkInterface.getByName("eth0");
            b = NIC.getHardwareAddress();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < b.length; i++)
            {
                if (i != 0 || i!=b.length-1)
                {
                    buffer.append('-');
                }
                String str = Integer.toHexString(b[i] & 0xFF);
                buffer.append(str.length() == 1 ? 0 + str : str);
            }
            strMacAddr = buffer.toString().toUpperCase();
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        return strMacAddr;
    }

    //获取mac 地址 函数
    /**
     * get mac
     */
    public static String getLocalMacAddressFromBusybox(){
        String result = callCmd("busybox ifconfig","HWaddr");
        if(result.equals("")){
            return "";
        }

        //例如：eth0    Link encap:Ethernet  HWaddr 00:16:E8:3E:DF:67
        if(result.length()>0 && result.contains("HWaddr")){
            result = result.substring(result.indexOf("HWaddr")+6, result.length()-1);
          }
        return result;
    }
    private static String callCmd(String cmd,String filter) {
        String result ="";
        try {
            String line ;
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStreamReader is = new InputStreamReader(proc.getInputStream());
            BufferedReader br = new BufferedReader(is);
            //执行命令cmd，只取结果中含有filter的这一行
            while ((line = br.readLine ()) != null && !line.contains(filter)) {
//                System.out.println("line: "+line);
            }
            result = line;
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static String getBuildInfo() {
        String m_szDevIDShort = "35" + //we make this look like a valid IMEI

                Build.BOARD.length()%10 +
                Build.BRAND.length()%10 +
                Build.CPU_ABI.length()%10 +
                Build.DEVICE.length()%10 +
                Build.DISPLAY.length()%10 +
                Build.HOST.length()%10 +
                Build.ID.length()%10 +
                Build.MANUFACTURER.length()%10 +
                Build.MODEL.length()%10 +
                Build.PRODUCT.length()%10 +
                Build.TAGS.length()%10 +
                Build.TYPE.length()%10 +
                Build.USER.length()%10 ;
        return m_szDevIDShort;
    }


    /**
     * 对网络连接状态进行判断
     * @return  true, 可用； false， 不可用
     */
    public static boolean isOpenNetwork(Context context) {
        ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if(networkInfo!= null) {
            return networkInfo.isAvailable();
        }
        return false;
    }

    //2.获取当前网络连接的类型信息
    public static int getNetworkType(Context context) {
        ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if(networkInfo!= null && networkInfo.isAvailable()) {
            int networkType = networkInfo.getType();
            return networkType;
//            if(ConnectivityManager.TYPE_WIFI == networkType){
//                //当前为wifi网络
//
//            }else if(ConnectivityManager.TYPE_MOBILE == networkType){
//                //当前为mobile网络
//
//            }
        }
        return -1;
    }

}
