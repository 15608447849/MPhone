package com.sok.mphone.dataEntity;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2016/12/19.
 */

public class MessageBeads {
    private ReentrantLock msgStoreLock = new ReentrantLock();//消息队列锁
    private List<String> msgSendingList = Collections.synchronizedList(new LinkedList<String>()); //消息待发送队列

    //添加一个消息
    public void addMsgToSend(String msg){
        try {
            msgStoreLock.lock();
            //如果发送队列消息过多 进入存储
            if (msgSendingList!=null ){
                msgSendingList.add(msg);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            msgStoreLock.unlock();
        }
    }

    //获取一个消息
    public String getMsg(){
        String message = null;
        try{
            msgStoreLock.lock();
            if (msgSendingList!=null && msgSendingList.size()>0){
                Iterator<String> itr = msgSendingList.iterator();
                if (itr.hasNext()){
                    message = itr.next();
                    itr.remove();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            msgStoreLock.unlock();
        }
        return message;
    }

}
