package com.wos.play.rootdir.model_monitor.soexcute;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by 79306 on 2017/3/8.
 */

public class WatchServerHelp extends IntentService{
    private static final String TAG = "CLibs";
    public WatchServerHelp() {
        super(TAG);
    }
    public static final String DEAMS_KEY = "keys";
    public static final int OPEN_DEAMS = 666;
    public static final int CLOSE_DEAMS = 777;
    public static final int CLOSE_DEAMS_ALL = 888;
    public static final int RESET_DEAMS = 999;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
            int type = intent.getIntExtra(DEAMS_KEY,-1);
            if (type == OPEN_DEAMS){
                open();
            }
            if (type == CLOSE_DEAMS){
                close();
            }
            if (type == CLOSE_DEAMS_ALL){
                closeAll();
            }
            if (type == RESET_DEAMS){
                openAll();
            }
    }

    private void open() {
        //获取包名
        String packageName = this.getPackageName();
        //String activityComd = "am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n "+packageName+"/com.sok.mphone.activity.BaseActivity";
        String watchServerPath = "am startservice --user 0 "+packageName+"/com.sok.mphone.services.CommuntServer";
        RunJniHelper.getInstance().startMservice(watchServerPath,"null","","",10);
    }
    private void close() {
        RunJniHelper.getInstance().stopMservice("");
    }
    private void openAll() {
        RunJniHelper.getInstance().liveAll("");
    }
    private void closeAll() {
        RunJniHelper.getInstance().killAll("");
    }


    /**
     * sd卡是否可用
     *
     * @return
     */
    private static boolean isSdCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    /**
     * 创建根缓存目录
     *
     * @return
     */
    public static String createRootPath(Context context ) {
        String cacheRootPath = "/mnt/sdcard/";
        if (isSdCardAvailable()) {
            // /sdcard/Android/data/<application package>/cache
            cacheRootPath = context.getExternalCacheDir().getPath();
        } else {
            // /data/data/<application package>/cache
            cacheRootPath = context.getCacheDir().getPath();
        }
        return cacheRootPath;
    }

    public static void openDeams(Context content) {
        Log.e(TAG,"准备打开守护进程服务!");
        Intent intent = new Intent(content, WatchServerHelp.class);
        intent.putExtra(WatchServerHelp.DEAMS_KEY,WatchServerHelp.OPEN_DEAMS);
        content.startService(intent);
    }
    public static void closeDeams(Context content) {
        Log.e(TAG,"准备关闭守护进程服务!");
        Intent intent = new Intent(content, WatchServerHelp.class);
        intent.putExtra(WatchServerHelp.DEAMS_KEY,WatchServerHelp.CLOSE_DEAMS);
        content.startService(intent);
    }
}
