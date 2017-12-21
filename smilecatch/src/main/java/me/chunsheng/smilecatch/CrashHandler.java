package me.chunsheng.smilecatch;

import android.content.Context;


import java.io.File;

import me.chunsheng.smilecatch.util.StorageUtil;

/**
 * Copyright © 2017 edaixi. All Rights Reserved.
 * Author: wei_spring
 * Date: 2017/12/18
 * Email:weichsh@edaixi.com
 * Function: Crash处理类
 */
public class CrashHandler {

    private Context mContext;

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    private CrashListener crashListener;

    private boolean crashSave;

    private static CrashHandler instance;

    private CrashHandler(final Context mContext) {

        this.mContext = mContext;

        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {

                if (crashSave)
                    storageCrash(thread, throwable);

                if (crashListener != null)
                    crashListener.onCrashListener(thread, throwable);

                uncaughtExceptionHandler.uncaughtException(thread, throwable);
            }
        });

    }


    public static CrashHandler getInstance(Context mContext) {

        if (instance == null) {
            instance = new CrashHandler(mContext);
        }
        return instance;
    }

    /**
     * 本地存储Crash日志
     *
     * @param throwable
     */
    public void storageCrash(Thread thread, Throwable throwable) {
        StorageUtil.storageCrash(mContext, thread, throwable);
    }

    /**
     * 读取最近本地Crash日志
     */
    public String getCrash() {
        return StorageUtil.getCrash(mContext);
    }

    /**
     * 读取本地所有Crash日志
     */
    public File getAllCrash() {
        return StorageUtil.getAllCrash(mContext);
    }


    /**
     * 自定义UncaughtExceptionHandler
     *
     * @param handler
     */
    public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler) {

        if (handler != null) {
            this.uncaughtExceptionHandler = handler;
        }
    }

    /**
     * 监听全局crash
     *
     * @param crashListener
     */
    public CrashHandler crashListener(CrashListener crashListener) {

        this.crashListener = crashListener;

        return instance;
    }

    /**
     * Crash是否存储本地
     *
     * @param crashSave
     */
    public CrashHandler crashSave(boolean crashSave) {

        this.crashSave = crashSave;

        return instance;
    }

}
