package com.sok.mphone.tools;

/**
 * Created by user on 2016/12/19.
 */

public class log {
    public static boolean isDebug = true;
    public static final String TAG = "颖网_传呼机:";
    public static final String ERR_LOG  = " 错误信息 : \n";
    public static void d(String tag, String msg) {
        if (isDebug) {
            android.util.Log.d(tag, msg);
        }
    }
    public  static void d(String msg){
        d(TAG,msg);
    }



    public static void i(String tag, String msg) {
        if (isDebug) {
            android.util.Log.i(tag, msg);
        }
    }
    public  static void i(String msg){
        i(TAG,msg);
    }


    public static void e(String tag, String msg) {
        if (isDebug) {
            android.util.Log.e(tag, msg);
        }
    }
    public  static void e(String msg){
        i(TAG,msg);
    }
}
