package me.chunsheng.supercatch;

import android.app.Application;
import android.content.Intent;

import me.chunsheng.smilecatch.CrashHandler;
import me.chunsheng.smilecatch.CrashListener;

/**
 * Copyright © 2017 edaixi. All Rights Reserved.
 * Author: wei_spring
 * Date: 2017/12/19
 * Email:weichsh@edaixi.com
 * Function: 自定义Application
 */
public class SuperCatchApplication extends Application implements CrashListener {

    public static final int DefaultRestart = 1;
    public static final int DefaultCrashPage = 2;
    public static final int CustomerCrashPage = 3;

    @Override
    public void onCreate() {
        super.onCreate();

        CrashHandler.getInstance(this).crashListener(this).crashSave(true);

    }

    /**
     * 监听crash，可以在这里处理crash，重启APP或者上报crash日志
     *
     * @param thread
     * @param throwable
     */
    @Override
    public void onCrashListener(Thread thread, Throwable throwable) {

        int catchType = 2;
        switch (catchType) {
            case DefaultRestart:
                try {
                    Intent intent = new Intent(this, Class.forName("me.chunsheng.supercatch.MainActivity"));
                    intent.putExtra("crash", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DefaultCrashPage:
                try {
                    Intent intent = new Intent(this, Class.forName("me.chunsheng.supercatch.CrashActivity"));
                    intent.putExtra("crash", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case CustomerCrashPage:
                //自定义Crash提示页面
                break;

        }

    }
}

