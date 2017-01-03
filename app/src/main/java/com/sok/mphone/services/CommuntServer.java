package com.sok.mphone.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;

import com.sok.mphone.activity.BaseActivity;
import com.sok.mphone.activity.BaseBroad;
import com.sok.mphone.entity.SocketBeads;
import com.sok.mphone.entity.SysInfo;
import com.sok.mphone.threads.interfaceDef.IActvityCommunication;
import com.sok.mphone.threads.interfaceImp.CommunicationThread;
import com.sok.mphone.tools.CommunicationProtocol;
import com.sok.mphone.tools.log;

import static com.sok.mphone.R.raw.a;

/**
 * Created by user on 2016/12/19.
 */

public class CommuntServer extends Service implements IActvityCommunication {
    private static String TAG = "CommuntServer";


    private SocketBeads socBen;
    private CommunicationThread communicationThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        log.e(TAG, "----------------------------------------onCreate() pid: " + android.os.Process.myPid());
        registBroad();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        log.e(TAG, "----------------------------------------onStartCommand() intent -getAction = "+ intent.getAction()+" -getFlags = " +intent.getFlags()+" flags =" + flags +" startId = "+startId);
        try {
            //如果 可以配置
            if (SysInfo.get(true).isConfig()) {

                if (SysInfo.get().isAccess()) {
                    if (SysInfo.get().isConnected() && communicationThread != null && communicationThread.isAlive()) {
                        log.i(TAG, "  通讯 线程 正在 执行中 ... ");
                    } else {
                        int port = Integer.parseInt(SysInfo.get().getServerPort());
                        if (socBen == null) {
                            socBen = new SocketBeads();
                        }
                        socBen.setIp(SysInfo.get().getServerIp());
                        socBen.setPort(port);
                        startCommThread();
                    }
                } else {
                    log.i(TAG, "无权访问服务器,请申请授权");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregistBroad();
        stopCommThread();
        stopEffect();
        //设置状态
        setSysyinif(false);
        log.e(TAG, "----------------------------------------onDestroy()");
    }

    private void setSysyinif(boolean isFlag) {

        if (isFlag) {
            SysInfo.get().setConnectPower(SysInfo.COMUNICATE_POWER.COMMUNI_NO_ACCESS); //无连接权限
        }
        //SysInfo.get().setCommunicationState(SysInfo.COMUNICATE_STATES.COMMUNI_NO_MESSAGE);//无消息
        SysInfo.get().setConnectState(SysInfo.CONN_STATES.CONN_FAILT);//无可连接
        SysInfo.get().setCallState(SysInfo.CALL_STATE.CALL_NOT_TASK);//不存在呼叫任务
        SysInfo.get().writeInfo();
    }

    //接受 应用-activity - showpage - 发来的消息
    public void receiveAppMsg(String message) {
        if (message != null && !"".equals(message) && communicationThread != null) {
            if (message.contains(CommunicationProtocol.ANTY)) {
                communicationThread.sendMessageToThread(message + "-" + SysInfo.get().getAppMac());//发送消息给服务器
                SysInfo.get().setCommunicationState(SysInfo.COMUNICATE_STATES.COMMUNI_NO_MESSAGE, true);//已处理 呼叫的信息
                stopEffect();
                //取消延时任务
                handler.removeCallbacks(runFunc2);
                //通知activity 已发送
                sendActivityMessage(MESSAGE_SEND_SUCCESS);
            }

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


    @Override
    public void sendMessageToActivity(int type) {
        if (type == IActvityCommunication.CONNECT_SUCCEND) {
            SysInfo.get().setConnectState(SysInfo.CONN_STATES.CONN_SUCCESS,true);
//            SysInfo.get().setCommunicationState(SysInfo.COMUNICATE_STATES.COMMUNI_NO_MESSAGE, true);
            communicationThread.sendMessageToThread(CommunicationProtocol.AHOL + SysInfo.get(true).getAppMac() +(SysInfo.get().isHasMessage()?"-"+CommunicationProtocol.Receive_Calls_With_Out_The_Click_Of_A_Button:""));
            sendActivityMessage(type);//发送消息 到 activity - 连接成功
        }
        if (type == IActvityCommunication.CONNECT_ING) {
            //发送 mac 地址 ...
            communicationThread.sendMessageToThread(CommunicationProtocol.AHBT + SysInfo.get().getAppMac());
        }
        if (type == IActvityCommunication.CONNECT_NO_ING || type == IActvityCommunication.CONNECT_FAILT) {
            // 连接失败
            setSysyinif(false);
//            SysInfo.get().setCommunicationState(SysInfo.COMUNICATE_STATES.COMMUNI_NO_MESSAGE, true);//无消息状态
            //取消延时任务
            handler.removeCallbacks(runFunc2);
            stopEffect();
            //通知activity 已发送
            sendActivityMessage(type); //发送消息 到 activity - 链接失败
        }
    }


    private void sendActivityMessage(int type) {
        //发送给activity
        Intent intent = new Intent();
        intent.setAction(BaseBroad.ACTION);
        Bundle bundle = new Bundle();
        bundle.putInt(BaseBroad.PARAM1, type);
        intent.putExtras(bundle);
        getApplication().sendBroadcast(intent);
    }

    @Override
    public void sendMessageToActivity(String message) {
        try {
            postTask(message.substring(0, 5), message.substring(5));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postTask(String cmd, String command) {
        log.i(" 收到 服务器 命令 - [" + cmd + "] , 参数 -[" + command + "]");

        if (cmd.equals(CommunicationProtocol.SNTY)) {
            if (command.equalsIgnoreCase(CommunicationProtocol.CMD_NOT_ACCESS)) {
                //断开连接 - 无权限链接- 提示
                stopCommThread();
                setSysyinif(true);
                sendActivityMessage(CONNECT_IS_NOT_ACCESS);
            }
            if (command.equalsIgnoreCase(CommunicationProtocol.CMD_CALL_ING)) { // 呼叫中 -<接受服务> - <拒绝服务>

                //设置通讯状态
                SysInfo.get().setCommunicationState(SysInfo.COMUNICATE_STATES.COMMUNI_CALL, true); //有一个消息发来 - 并且 有存在任务
                SysInfo.get().setCallState(SysInfo.CALL_STATE.CALL_EXIST_TASK, true);//存在呼叫任务
                handler.post(runFunc1);
                handler.postDelayed(runFunc2, 30 * 1000);// 30秒后无回应 发送-拒绝
            }
            if (command.equalsIgnoreCase(CommunicationProtocol.CMD_NOT_FREE)) { //已经被呼叫 - 繁忙 ,并且 本地状态是呼叫中 -> 显示 <结束服务>
                SysInfo.get().setCallState(SysInfo.CALL_STATE.CALL_EXIST_TASK, true);//存在呼叫任务
                sendActivityMessage(CONNECT_ING_NOTFREE);
            }
            if (command.equalsIgnoreCase(CommunicationProtocol.CMD_FREE)) { //未被呼叫 - 空闲中 - 三个按钮全部不显示
                //设置通讯状态
                SysInfo.get().setCommunicationState(SysInfo.COMUNICATE_STATES.COMMUNI_NO_MESSAGE); //没有消息发来
                SysInfo.get().setCallState(SysInfo.CALL_STATE.CALL_NOT_TASK, true);//不存在呼叫任务
                stopEffect();
                sendActivityMessage(CONNECT_ING_FREE);
            }


        }
    }

    private Handler handler = new Handler();
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
        receiveAppMsg(CommunicationProtocol.ANTY + CommunicationProtocol.RECIPT_REFUSE_SERVER);
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
        startFlashing();//打开闪光灯
        startAlarm();
        startVibrator();
    }

    private void stopEffect() {
        stopFlashing();//关闭闪光灯
        stopAlarm();
        stopVibrator();
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

    private Camera camera = null;
    private Camera.Parameters parameters;
    //打开闪光灯
    private void startFlashing() {
        if (camera==null){
            camera =  Camera.open();
            camera.startPreview();
            handler.postDelayed(flashingRunnable,1200);
        }
    }

    //闪光灯 闪烁
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
            handler.postDelayed(flashingRunnable,1200);
        }
    };
    //开始闪光灯 run
    private void startFlashingRun() {
        if (camera!=null){
            try {
                if (parameters==null){
                    parameters = camera.getParameters();
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
//                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //关闭闪光灯run
    private void stopFlashingRun(){
        if (camera!=null && parameters!=null){
            try{
//                camera.stopPreview();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
//                camera.startPreview();
            } catch(Exception ex){}
        }
    }

    //关闭闪光灯
    private void stopFlashing() {
        if (parameters!=null){
            //关闭闪光灯
            stopFlashingRun();
            parameters=null;
        }
        if (camera!=null){
            camera.stopPreview();
            camera.release();
            camera=null;
        }
        handler.removeCallbacks(flashingRunnable);
    }

    //打开电源灯
    private void startPower() {
    }

    //关闭电源灯
    private void stopPower() {

    }
}
