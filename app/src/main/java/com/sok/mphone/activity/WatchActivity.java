package com.sok.mphone.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.sok.mphone.R;
import com.wos.play.rootdir.model_monitor.soexcute.WatchServerHelp;

public class WatchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);
//        if (Integer.parseInt(Build.VERSION.SDK)==19){
//            startActivity(new Intent(this,WatchActivity.class));
//            finish();
//            return;
//        }
    }
    public void watchServer(View view){
        WatchServerHelp.openDeams(getApplication());
    }
}
