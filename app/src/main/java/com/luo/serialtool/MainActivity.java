package com.luo.serialtool;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

public class MainActivity extends Activity {
    private Bill_Acceptor_Server mServer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("Build.CPU_ABI  = " + Build.CPU_ABI);
        mServer = new Bill_Acceptor_Server();
        mServer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mServer.stop();
    }
}
