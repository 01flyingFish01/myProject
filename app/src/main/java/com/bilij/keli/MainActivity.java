package com.bilij.keli;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.bilij.keli.Util.ACache;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    Button go,sound;
    private MyConn conn;
    private FloatingWindowService.ElfBinder myBinder;//我定义的中间人对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        go = findViewById(R.id.go);
        sound = findViewById(R.id.sound);
        if(ACache.get(MainActivity.this).getAsString("sound")==null||ACache.get(MainActivity.this).getAsString("sound").equals("")){
            ACache.get(this).put("sound","yes");
        }

        if(ACache.get(MainActivity.this).getAsString("sound").equals("no")){
            sound.setText("开启声音");
        }else if(ACache.get(MainActivity.this).getAsString("sound").equals("yes")){
            sound.setText("关闭声音");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M&&!Settings.canDrawOverlays(this)) {
            //没有权限，需要申请权限，因为是打开一个授权页面，所以拿不到返回状态的，所以建议是在onResume方法中从新执行一次校验
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);

            intent.setData(Uri.parse("package:" + getPackageName()));

            startActivityForResult(intent, 100);

        }
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    myBinder.callElf(PetElf.OPERATION_SHOW);
//                    go.setText("可莉再见");
//                    ACache.get(MainActivity.this).put("go","yes");
//                else {
//                    myBinder.callElf(PetElf.OPERATION_HIDE);
//                    go.setText("可莉登场");
//                    ACache.get(MainActivity.this).put("go","no");
//                }
            }
        });
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sound.getText().toString().equals("关闭声音")){
                    myBinder.setSound(false);
                    sound.setText("开启声音");
                }else {
                    myBinder.setSound(true);
                    sound.setText("关闭声音");
                }
            }
        });
        Intent intent = new Intent(this,FloatingWindowService.class);
        //连接服务
        conn = new MyConn();
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    //监视服务的状态
    private class MyConn implements ServiceConnection {

        //当服务连接成功调用
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获取中间人对象
            myBinder = (FloatingWindowService.ElfBinder) service;
        }
        //失去连接
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }


}
