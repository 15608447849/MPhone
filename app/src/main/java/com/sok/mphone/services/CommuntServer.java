package com.sok.mphone.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;

import com.sok.mphone.R;
import com.sok.mphone.activity.BaseActivity;
import com.sok.mphone.activity.BaseBroad;
import com.sok.mphone.dataEntity.SocketBeads;
import com.sok.mphone.dataEntity.SysInfo;
import com.sok.mphone.threads.interfaceDef.IActvityCommunication;
import com.sok.mphone.threads.interfaceImp.CommunicationThread;
import com.sok.mphone.tools.CommunicationProtocol;
import com.sok.mphone.tools.log;

/**
 * Created by user on 2016/12/19.
 */

public class CommuntServer extends Service implements IActvityCommunication {
    private static String TAG = "CommuntServer";

    public interface LocalCommand {
        String STOP_ZX = "TING_ZHI_ZHENG_DONG_LING_SHENG";
    }


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
        log.e(TAG, "----------------------------------------onStartCommand() flags =" + flags);


        if (SysInfo.get().isConfig()) {
            try {
                if (communicationThread != null && communicationThread.isAlive()) {
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

            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log.e(TAG, "----------------------------------------onDestroy()");
        unregistBroad();
        stopCommThread();
    }

    public void receiveAppMsg(String message) {
        if (message != null && !"".equals(message) && communicationThread != null) {
            if (message.equals(LocalCommand.STOP_ZX)) {
                stopAlarm();
                stopVibrator();
            } else {
                communicationThread.sendMessageToThread(message);
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
            SysInfo.get().setConnectState(SysInfo.ConnectStates.connectSuccess, true);
            sendActivityMessage();
        }
        if (type == IActvityCommunication.CONNECT_ING) {
            //发送 mac 地址 ...
            communicationThread.sendMessageToThread(CommunicationProtocol.AHBT + SysInfo.get().getAppMac());
        }
        if (type == IActvityCommunication.CONNECT_NO_ING || type == IActvityCommunication.CONNECT_FAILT) {
            // 连接失败
            SysInfo.get().setConnectState(SysInfo.ConnectStates.connectFailt,true);
        }
    }

    private void sendActivityMessage() {
        //发送给activity
        Intent intent = new Intent();
        intent.setAction(BaseBroad.ACTION);
        Bundle bundle = new Bundle();
        bundle.putInt(BaseBroad.PARAM1, 1);
        intent.putExtras(bundle);
        getApplication().sendBroadcast(intent);
    }

    @Override
    public void sendMessageToActivity(String message) {
        postTask(message.substring(0, 5), message.substring(5));
    }

    private void postTask(String cmd, String command) {
        log.i(" 收到 服务器 命令 - [" + cmd + "] - [" + command + "]");
        handler.post(runTGP);
    }

    private Handler handler = new Handler();
    private final Runnable runTGP = new
            Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(CommuntServer.this, BaseActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    CommuntServer.this.getApplicationContext().startActivity(intent);
                    startAlarm();
                    startVibrator();
                }
            };

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

    private MediaPlayer mMediaPlayer;

    private void startAlarm() {
        if (mMediaPlayer == null) {
            try {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setLooping(true);
                AssetFileDescriptor file = getApplication().getResources().openRawResourceFd(R.raw.a);
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
}
//            Vibrator vibrator = (Vibrator)mActivity.getSystemService(Context.VIBRATOR_SERVICE);
//            vibrator.vibrate(new long[]{300,500},0);
  /*  MediaPlayer mediaPlayer = new MediaPlayer();
mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
@Override
public void onCompletion(MediaPlayer player) {
        player.seekTo(0);
        }
        });
        AssetFileDescriptor file = mActivity.getResources().openRawResourceFd(R.raw.a);
        try{

        mediaPlayer.setDataSource(file.getFileDescriptor(),

        file.getStartOffset(), file.getLength());

        file.close();

        mediaPlayer.setVolume(100,100);

        mediaPlayer.prepare();
        mediaPlayer.start();
        }catch (IOException ioe) {
        mediaPlayer = null;
        }*/