package com.luo.bsdiff;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bzip.DiffPatchUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void click1(View view){
        DiffPatchUtils.genDiff("/mnt/sdcard/old.apk","/mnt/sdcard/new.apk","/mnt/sdcard/patch");

    }

    public void click2(View view){
        DiffPatchUtils.patch("/mnt/sdcard/old.apk","/mnt/sdcard/new2.apk","/mnt/sdcard/patch");

    }
}
