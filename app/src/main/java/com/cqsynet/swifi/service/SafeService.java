/*
 * Copyright (C) 2015 重庆尚渝
 * 版权所有
 *
 * 守护进程service,保护PushService
 *
 * 创建标识：zhaosy 20170407
 */
package com.cqsynet.swifi.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.IGuardAIDL;
import com.cqsynet.swifi.util.AppUtil;

public class SafeService extends Service {

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    startPushService();
                    break;
                case 2:
                    startTimerService();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 使用aidl 启动PushService
     */
    private IGuardAIDL mIGuardAIDL = new IGuardAIDL.Stub() {
        @Override
        public void stopService() throws RemoteException {
            Intent i = new Intent(getBaseContext(), PushService.class);
            getBaseContext().stopService(i);

            Intent intent = new Intent(getBaseContext(), TimerService.class);
            getBaseContext().stopService(intent);
        }

        @Override
        public void startService() throws RemoteException {
            Intent i = new Intent(getBaseContext(), PushService.class);
            getBaseContext().startService(i);

            Intent intent = new Intent(getBaseContext(), TimerService.class);
            getBaseContext().startService(intent);
        }
    };

    /**
     * 在内存紧张的时候，系统回收内存时，会回调OnTrimMemory， 重写onTrimMemory当系统清理内存时从新启动PushService
     */
    @Override
    public void onTrimMemory(int level) {
        startPushService();
        startTimerService();
    }

    @Override
    public void onCreate() {
        if (Globals.DEBUG) {
            Log.i(this.getClass().getName(), "SafeService onCreate");
        }

        //此线程用监听PushService的状态
        new Thread() {
            public void run() {
                while (true) {
                    boolean isRun = AppUtil.isServiceWork(SafeService.this, PushService.class.getName());
                    if (!isRun) {
                        Message msg = Message.obtain();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                    }
                    if (!AppUtil.isServiceWork(SafeService.this, TimerService.class.getName())) {
                        Message msg = Message.obtain();
                        msg.what = 2;
                        mHandler.sendMessage(msg);
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * 判断PushService是否还在运行，如果不是则启动PushService
     */
    private void startPushService() {
        boolean isRun = AppUtil.isServiceWork(SafeService.this, PushService.class.getName());
        if (!isRun) {
            try {
                mIGuardAIDL.startService();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void startTimerService() {
        boolean isRun = AppUtil.isServiceWork(SafeService.this, TimerService.class.getName());
        if (!isRun) {
            try {
                mIGuardAIDL.startService();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return (IBinder) mIGuardAIDL;
    }
}
