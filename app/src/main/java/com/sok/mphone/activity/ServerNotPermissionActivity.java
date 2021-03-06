package com.sok.mphone.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.TextView;

import com.sok.mphone.R;
import com.sok.mphone.tools.AppsTools;

public class ServerNotPermissionActivity extends Activity {

    private Handler handler ;
    private TextView tv ;
    int time = 10;
    private final Runnable runing = new Runnable() {
        @Override
        public void run() {
            if (tv!=null && handler!=null){
                if (time > 0){
                    tv.setText("无权限连接服务器,请联系客服. ("+time+")\n["+ AppsTools.getMacAddress(ServerNotPermissionActivity.this)+"]");
                    time--;
                    handler.postDelayed(this,1000);
                }else{
                    ServerNotPermissionActivity.this.finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_not_permission);
        tv = (TextView) findViewById(R.id.text);
        handler =  new Handler();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        closem();
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (handler!=null && tv!=null){
            handler.post(runing);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closem();
    }

    private void closem() {
        if (handler!=null){
            handler.removeCallbacks(runing);//移除
            handler = null;
            tv = null;
        }
    }
}
