package com.sok.mphone.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.TextView;

import com.sok.mphone.R;

public class ForcedOfflineActivity extends Activity {

    private Handler handler ;
    private TextView tv ;
    int time = 5;
    private final Runnable runing = new Runnable() {
        @Override
        public void run() {
            if (tv!=null && handler!=null){
                if (time > 0){
                    tv.setText("客户端被迫离线,如非本人操作请联系相关人员.");
                    time--;
                    handler.postDelayed(this,1000);
                }else{
                    ForcedOfflineActivity.this.finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forced_offline);
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
