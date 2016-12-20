package com.sok.mphone.dataEntity;

import com.sok.mphone.tools.log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by user on 2016/12/19.
 */

public class SocketBeads {
    private static final String TAG = "_SOCKET";
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private Socket socket;
    private String ip;
    private int port;// 6666
    private boolean isConnected = false;
    private MessageBeads mStore;



    public SocketBeads(String ip, int port) {
        this();
        this.ip = ip;
        this.port = port;
    }
    public SocketBeads(){
        this.isConnected = false;
        this.mStore = new MessageBeads();
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isConnected() {
        return isConnected;
    }

    //创建socket连接
    private void createrSocketConnect() {
        //如果已连接 断开连接
        if (isConnected) {
            desConnection();
        }
        try {
            if (socket == null) {
                log.i(TAG,"尝试创建socket 连接...");
                socket = new Socket(ip, port);
                dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                dataInputStream = new DataInputStream(socket.getInputStream());
                isConnected = true;
                log.i(TAG,"Communication connectToServer success >>  \n" + ip + " - " + port);
            }
        } catch (IOException e) {
            log.e("socket connection err: " + e.getMessage());
            isConnected = false;
        }
    }

    /**
     * 创建连接
     */
    public synchronized boolean createConnect() {
        log.i(TAG, "创建连接中...");
        //连接
        createrSocketConnect();
        if (!isConnected) { //如果没连接上
            //尝试重新链接
            log.i(TAG, "创建连接失败,稍后尝试");
            return false;
        }
        return true;
    }

    //结束连接
    public synchronized void desConnection() {
        log.i(TAG,"断开socket连接中....");
        if (dataOutputStream != null) {
            try {
                dataOutputStream.close();

            } catch (Exception e) {
                log.e(TAG,"Communication disconnect error : " + e.getMessage());
            } finally {
                dataOutputStream = null;
            }
        }
        if (dataInputStream != null) {
            try {
                dataInputStream.close();

            } catch (IOException e) {
                log.e(TAG,"Communication disconnect error : " + e.getMessage());
            } finally {
                dataInputStream = null;
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                log.e(TAG,"Communication disconnect error : " + e.getMessage());
            } finally {
                socket = null;
            }
        }
        isConnected = false;//不在连接中
    }

    public String getMessage(){
        return mStore.getMsg();
    }
    public void  putMessage(String message){
        if (message!=null){
            mStore.addMsgToSend(message);
        }

    }

    public void sendMessage (String msg) throws Exception{

        if (dataOutputStream!=null){
            log.i(TAG," 发送给 服务器 [" + msg+"]");
            dataOutputStream.writeUTF(msg);
            dataOutputStream.flush();
        }
    }
    public void  sendMessage() throws Exception{
        String msg = getMessage();
        if (msg!=null){
            sendMessage(msg);
        }
    }
    public String acceptMessage()throws Exception {
        String msg = null;
        if (dataInputStream!=null){

            if (dataInputStream.available() > 0) {
                msg = dataInputStream.readUTF();
                log.i(TAG," 收到 服务器 参数[" + msg+"]");
            }
        }
        return msg;
    }


}
