package com.sok.mphone.services;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.sok.mphone.R;
import com.sok.mphone.activity.BaseActivity;
import com.sok.mphone.activity.BaseBroad;
import com.sok.mphone.activity.PermissionActivity;
import com.sok.mphone.entity.SocketBeads;
import com.sok.mphone.entity.SysInfo;
import com.sok.mphone.threads.interfaceDef.IActivityCommunication;
import com.sok.mphone.threads.interfaceImp.CommunicationThread;
import com.sok.mphone.tools.CommunicationProtocol;
import com.sok.mphone.tools.log;
import com.wos.play.rootdir.model_monitor.soexcute.WatchServerHelp;

import static com.sok.mphone.R.raw.a;

/**
 * Created by user on 2016/12/19.
 */

public class CommuntServer extends Service implements IActivityCommunication {
    private static String TAG = "CommuntServer";
    private SocketBeads socBen;
    private CommunicationThread communicationThread;
    private CommunicationMessage communicationMessage = new CommunicationMessage();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        log.e(TAG, "----------------------------------------onCreate() pid: " + android.os.Process.myPid());

//        Notification.Builder builder = new Notification.Builder(getApplicationContext());
//        builder.setSmallIcon(R.drawable.title_icon);
//        builder.setContentTitle(getString(R.string.notify_server_title));
//        builder.setContentInfo(getString(R.string.notify_server_info));
//        Intent intent = new Intent(getApplicationContext(), CommuntServer.class);
//        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0,intent, 0);
//        builder.setContentIntent(pendingIntent);
//
//        //把该service创建为前台service
//        Notification notification = builder.build();
//        startForeground(1, notification);


        registBroad();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log.e(TAG, "onStartCommand() startId = "+startId);
//        log.i("CLibs", "颖网终端呼叫机 后台通讯服务 -  onStartCommand >>> "+startId);
        try {
            //如果6.0以上的系统 - 先检查权限
            if (Integer.parseInt(Build.VERSION.SDK) >= 21 &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    ) {
                //通知activity 系统权限不足 - 无法连接
                sendMessageToActivity(CONNECT_FAILT);//连接失败
                startActivity(new Intent(this, PermissionActivity.class));
                return START_NOT_STICKY;
            }

            boolean ifg = SysInfo.get(SysInfo.CONFIG).isConfig();
            boolean iflg = SysInfo.get(SysInfo.CONFIG).isLocalConnect();
            //如果 可以配置
            if (ifg && iflg) {

                        if (SysInfo.get(SysInfo.COMUNICATION).isConnected() && communicationThread != null && communicationThread.isAlive()) {
                            //打开服务
                            startNotify();
                        }else{
                            if (socBen == null) {
                                socBen = new SocketBeads();
                            }
                            socBen.setIp(SysInfo.get(SysInfo.CONFIG).getServerIp());
                            socBen.setPort(Integer.parseInt(SysInfo.get(SysInfo.CONFIG).getServerPort()));
                            startCommThread();
                            communicationMessage.setType(SysInfo.get(SysInfo.CONFIG).getServerPort());//根据端口设置类型 - ()
                            //打开监听
                            WatchServerHelp.openDeams(getApplication());
                        }
            }else{
                //关闭监听
                WatchServerHelp.closeDeams(getApplication());
                //结束自己
                stopSelf();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;//super.onStartCommand(intent, flags, startId);
    }

    private void startNotify() {
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setSmallIcon(R.drawable.title_icon);
        builder.setContentTitle(getString(R.string.notify_server_communication_title));
        builder.setContentInfo(getString(R.string.notify_server_info));
        Intent intent = new Intent(getApplicationContext(), BaseActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,intent, 0);
        builder.setContentIntent(pendingIntent);
        //把该service创建为前台service
        Notification notification = builder.build();
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregistBroad();//注销广播
        stopCommThread();//结束通讯
        stopEffect();//停止
        //设置状态
        setCommuncationFailt();
        log.e(TAG, "----------------------------------------onDestroy()");
    }

    //设置了 是否有权限 - true >> 设置没有连接后台的权限
    private void setCommuncationFailt() {
        SysInfo.get(SysInfo.COMUNICATION).setConnectState(SysInfo.CONN_STATES.CONN_FAILT,true);//未连接
    }

    //接受 appUI-activity - showpage - 发来的消息
    public void receiveAppMsg(String message) {
        if (message != null && !"".equals(message) && communicationThread != null) {
                communicationThread.sendMessageToThread(communicationMessage.RESP(message));//发送消息给服务器  :  xxxx-mac地址
                SysInfo.get(SysInfo.COMUNICATION).setCommunicationState(SysInfo.COMUNICATE_STATES.COMMUNI_NO_MESSAGE, true);//已处理 呼叫的信息
                stopEffect();
                //取消延时任务
                handler.removeCallbacks(runFunc2);
                //通知activity 已发送
                sendActivityMessage(MESSAGE_SEND_SUCCESS);
        }
    }

    /**
     * 通过 广播 接受 其他 进程 发来的消息的,
     */
    private CommuntBroadCasd appReceive = null;

    /**
     * 停止广播 destory call
     */
    private void unregistBroad() {
        if (appReceive != null) {
            try {
                getApplicationContext().unregisterReceiver(appReceive);
            } catch (Exception e) {
                e.printStackTrace();
            }
            appReceive = null;
            log.i(TAG, "注销 通讯 广播");
        }
    }

    /**
     * 注册广播  create call
     */
    private void registBroad() {
        unregistBroad();
        appReceive = new CommuntBroadCasd(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(CommuntBroadCasd.ACTION);
        getApplicationContext().registerReceiver(appReceive, filter); //只需要注册一次
        log.i(TAG, "已注册 通讯 广播");
    }

    //打开通讯线程
    private void startCommThread() {
        stopCommThread();
        if (socBen == null) {
            return;
        }
        log.i(TAG, "开启通讯线程中...");
        communicationThread = new CommunicationThread(this, socBen);
        communicationThread.mStart();
        communicationThread.start();
    }


    //关闭通讯线程
    private void stopCommThread() {
        if (communicationThread != null) {
            communicationThread.mStop();
            communicationThread = null;
        }
    }

    //接受到服务器的消息 - 后台处理 - 到activity中
    @Override
    public void sendMessageToActivity(int type) {
        if (type == IActivityCommunication.CONNECT_SUCCEND) {//连接成功 -> 上线
            SysInfo.get(SysInfo.COMUNICATION).setConnectState(SysInfo.CONN_STATES.CONN_SUCCESS, true); //设置连接成功
            communicationThread.sendMessageToThread(communicationMessage.ONLI(this));
            sendActivityMessage(type);//发送消息 到 activity - 连接成功
        }
        if (type == IActivityCommunication.CONNECT_ING) {
            //发送 mac 地址 ...
            communicationThread.sendMessageToThread(communicationMessage.HRBT());
        }
        if (type == IActivityCommunication.CONNECT_NO_ING || type == IActivityCommunication.CONNECT_FAILT) {
            // 未连接 或者 连接失败
            setCommuncationFailt();
            //取消延时任务
            handler.removeCallbacks(runFunc2);
            stopEffect();
            //通知activity 已发送
            sendActivityMessage(type); //发送消息 到 activity - 链接失败
        }
    }

    //本地发送消息到UI界面
    protected void sendActivityMessage(int type) {
        //发送给activity
        Intent intent = new Intent();
        intent.setAction(BaseBroad.ACTION);
        Bundle bundle = new Bundle();
        bundle.putInt(BaseBroad.PARAM1, type);
        intent.putExtras(bundle);
        getApplication().sendBroadcast(intent);
    }
    //接受到服务器的消息发送到本地处理
    @Override
    public void sendMessageToActivity(String message) {
        try {
            String[] messageArr = communicationMessage.RECV(message);
            if (messageArr==null) return;
            postTask(messageArr[0],messageArr[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void postTask(String cmd, String command) {
//        log.i(" 收到 服务器 命令 - [" + cmd + "] , 参数 -[" + command + "]");
        if (cmd.equals(CommunicationProtocol.SNTY)) {
            if (command.equals(CommunicationProtocol.CMD_OFLE)){
                //通知UI-发送下线, 断开连接,界面跳转
                sendActivityMessage(CONNECT_STOP);
            }else
            if (command.equalsIgnoreCase(CommunicationProtocol.CMD_NOT_ACCESS)) {
                //1断开连接 -> 2 设置无权限链接 -> 3.提示
                stopCommThread();
                setCommuncationFailt();
                SysInfo.get(SysInfo.COMUNICATION).setConnectPower(SysInfo.COMUNICATE_POWER.COMMUNI_NO_ACCESS,true); //无连接权限
                sendActivityMessage(CONNECT_IS_NOT_ACCESS);
            }else
            if (command.equalsIgnoreCase(CommunicationProtocol.CMD_CALL_ING)) { // 呼叫中 - 选择 : 1<接受服务> - 2<拒绝服务>
                //设置通讯状态
                if (setCommunicationMessage(true)){ //写入成功
                    handler.post(runFunc1);
                    handler.postDelayed(runFunc2, 30 * 1000);// 设置 30秒后无回应 发送-拒绝
                }
            }else
            if (command.equalsIgnoreCase(CommunicationProtocol.CMD_NOT_FREE)) { //已经被呼叫 - 繁忙 ,并且 本地状态是呼叫中 -> 显示 <结束服务>
                SysInfo.get(SysInfo.COMUNICATION).setCallState(SysInfo.CALL_STATE.CALL_EXIST_TASK, true);//存在任务待执行
                sendActivityMessage(CONNECT_ING_NOTFREE);//告知activity ,有任务进行中~ 繁忙
            }else
            if (command.equalsIgnoreCase(CommunicationProtocol.CMD_FREE)) {//未被呼叫 - 空闲中 - 三个按钮全部不显示
                //设置通讯状态
                if (setCommunicationMessage(false)){
                    stopEffect();
                    sendActivityMessage(CONNECT_ING_FREE);
                }
            }
        }
    }

    //设置通讯状态
    private boolean setCommunicationMessage(boolean flag){
        SysInfo sysInfo = SysInfo.get(SysInfo.COMUNICATION);
        if (flag){
            sysInfo.setCommunicationState(SysInfo.COMUNICATE_STATES.COMMUNI_CALL); //有一个消息发来
            sysInfo.setCallState(SysInfo.CALL_STATE.CALL_EXIST_TASK);//存在任务
        }else{
            sysInfo.setCommunicationState(SysInfo.COMUNICATE_STATES.COMMUNI_NO_MESSAGE); //没有消息发来
            sysInfo.setCallState(SysInfo.CALL_STATE.CALL_NOT_TASK);//不存在呼叫任务
        }
       return sysInfo.writeInfo(SysInfo.COMUNICATION);
    }

    private Handler handler = new Handler();
    //打开activity 开始响铃震动
    private final Runnable runFunc1 = new
            Runnable() {
                @Override
                public void run() {
                    callCmds();
                }
            };
    private final Runnable runFunc2 = new
            Runnable() {
                @Override
                public void run() {
                    sendRefuse();
                }
            };

    //发送拒绝请求
    private void sendRefuse() {
        //告知ui
        postTask(CommunicationProtocol.SNTY,CommunicationProtocol.CMD_FREE);
        receiveAppMsg(CommunicationProtocol.APP_REFUSE);
    }

    //收到传呼命令
    private void callCmds() {
        startActivitys();
        startEffect();
    }

    //开启activity界面
    private void startActivitys() {
        Intent intent = new Intent(CommuntServer.this, BaseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        CommuntServer.this.getApplicationContext().startActivity(intent);
    }
//--效果

    private void startEffect() {
        startVibrator();
        startAlarm();
        startFlashing();//打开闪光灯
    }

    private void stopEffect() {
        stopVibrator();
        stopAlarm();
        stopFlashing();//关闭闪光灯
    }

    //震动需要的
    private Vibrator vibrator;

    //开始震动
    private void startVibrator() {
        if (vibrator == null) {
            vibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
            //long型数组内的数组依次表示：等待1秒、震动2秒、等待3秒、震动4秒，0表示无限循环long型数组内定义的震动规则；如果是-1则表示不循环震动
            vibrator.vibrate(new long[]{1000, 3000, 2000, 6000}, 0);
        }
    }

    //结束震动
    private void stopVibrator() {
        if (vibrator != null) {
            if (vibrator.hasVibrator()) {
                vibrator.cancel();
            }
            vibrator = null;
        }
    }

    //音频播放的
    private MediaPlayer mMediaPlayer;

    private void startAlarm() {
        if (mMediaPlayer == null) {
            //调节媒体音量
            setVolumes();
            try {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setLooping(true);
                AssetFileDescriptor file = getApplication().getResources().openRawResourceFd(a);
                mMediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mMediaPlayer.setVolume(1, 1);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
                stopAlarm();
            }
        }
    }

    //设置媒体音量
    private void setVolumes() {
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
    }

    //关闭音乐
    private void stopAlarm() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /*****************************************
     * 闪光灯
     ****************************************/
    //sdk<23
    private Camera camera = null;
    private Camera.Parameters parameters;

    //打开闪光灯
    private void startFlashing() {
        if (!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
            return;
        if (Integer.parseInt(Build.VERSION.SDK) >= 21) {
            startFlashingSDK23();
        } else {
            startFlashingSDK19();
        }

    }

    private void startFlashingSDK19() {
        if (camera == null) {
            camera = Camera.open();
            parameters = camera.getParameters();
            handler.post(flashingRunnable);
        }
    }

    //sdk>=23
    CameraManager mCameraManager = null;
    String mCameraId = null;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startFlashingSDK23() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            mCameraId = mCameraManager.getCameraIdList()[0];
            if (mCameraManager != null && mCameraId != null) {
                int flag = mCameraManager.getCameraCharacteristics(mCameraId).get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                log.e(TAG,"是不是支持:"+flag);
                if (flag == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                    //调用旧版本
                    throw new IllegalStateException("plase laegacy camera api");
                }
                handler.post(SDK_M_flashingRunnable);
            }
        } catch (Exception e) {
//            e.printStackTrace();
            mCameraId = null;
            mCameraManager = null;
            startFlashingSDK19();
        }
    }

    //打开闪光灯
    @TargetApi(Build.VERSION_CODES.M)
    public void turnOnFlashLight() {
        try {
            if (mCameraManager != null && mCameraId != null) {
                mCameraManager.setTorchMode(mCameraId, true);
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    //关闭闪光灯
    @TargetApi(Build.VERSION_CODES.M)
    public void turnOffFlashLight() {
        try {
            if (mCameraManager != null && mCameraId != null) {
                mCameraManager.setTorchMode(mCameraId, false);
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    private final Runnable SDK_M_flashingRunnable = new Runnable() {
        @Override
        public void run() {
            //休眠
            try {
                //打开闪光灯
                turnOnFlashLight();
                Thread.sleep(500);
                //关闭闪关灯
                turnOffFlashLight();
            } catch (InterruptedException e) {
//                e.printStackTrace();
            }
            handler.postDelayed(this, 1200);
        }
    };

    @TargetApi(Build.VERSION_CODES.M)
    private void stopFlashingSDK23() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        handler.removeCallbacks(SDK_M_flashingRunnable);
        turnOffFlashLight();//关闭闪关灯
        mCameraManager = null;
        mCameraId = null;
    }


    //闪光灯闪烁
    private final Runnable flashingRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                startFlashingRun();
                Thread.sleep(500);
                stopFlashingRun();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.postDelayed(this, 1200);
        }
    };

    //开始闪光灯 run
    private void startFlashingRun() {
        if (camera != null && parameters != null) {
            try {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                camera.startPreview();
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }

    //关闭闪光灯run
    private void stopFlashingRun() {
        if (camera != null && parameters != null) {
            try {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                camera.startPreview();
            } catch (Exception ex) {
            }
        }
    }

    //关闭闪光灯
    private void stopFlashing() {
        if (!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
            return;
        stopFlashingSDK23();
        stopFlashingSDK19();

    }

    private void stopFlashingSDK19() {
        handler.removeCallbacks(flashingRunnable);
        if (parameters != null) {
            //关闭闪光灯
            stopFlashingRun();
            parameters = null;
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

}
