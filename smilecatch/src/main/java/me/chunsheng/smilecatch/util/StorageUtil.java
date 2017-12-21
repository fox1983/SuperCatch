package me.chunsheng.smilecatch.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Copyright © 2017 edaixi. All Rights Reserved.
 * Author: wei_spring
 * Date: 2017/12/18
 * Email:weichsh@edaixi.com
 * Function: 本地存储工具类
 */

public class StorageUtil {


    public static void storageCrash(Context mContext, Thread thread, Throwable throwable) {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly())
            return;

        ApplicationInfo applicationInfo = mContext.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        String appName = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : mContext.getString(stringId);

        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("=======Crash日志开始记录=======");
        stringBuilder.append("\nCrash Time: " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance(Locale.CHINA).getTime()));
        stringBuilder.append("\nCrash Thread: " + thread.getName());
        stringBuilder.append("\nCrash Digest: " + throwable.toString());
        stringBuilder.append("\nCrash StackTrace: " + sw.toString());
        stringBuilder.append("\n******Crash设备当前信息******");
        stringBuilder.append("\n" + DeviceSnapshotUtil.snapshot(mContext));
        stringBuilder.append("=======Crash日志结束记录=======");
        String logName = new SimpleDateFormat("yyyy-MM-dd-hhmmss").format(Calendar.getInstance(Locale.CHINA).getTime());
        savaCrash("/" + appName + "/crashlogs", logName + ".txt", stringBuilder.toString());
    }

    /**
     * 本地存储crash日志
     *
     * @param dirName
     * @param fileName
     * @param crashInfo
     */
    public static void savaCrash(String dirName, String fileName, String crashInfo) {
        File path = new File(Environment.getExternalStorageDirectory().toString() + dirName);
        if (!path.exists()) {
            path.mkdirs();
        }
        File file = new File(path.toString(), fileName);
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(crashInfo.getBytes("UTF-8"));
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取最近一次的crash
     *
     * @param mContext
     */
    public static String getCrash(Context mContext) {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly())
            return "";

        ApplicationInfo applicationInfo = mContext.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        String appName = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : mContext.getString(stringId);

        try {
            File path = new File(Environment.getExternalStorageDirectory().toString() + "/" + appName + "/crashlogs");
            if (path.exists()) {
                File[] files = path.listFiles();
                StringBuilder text = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(files[files.length - 1]));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
                return text.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

    /**
     * 读取本地所有的crash
     *
     * @param mContext
     */
    public static File getAllCrash(Context mContext) {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly())
            return null;

        ApplicationInfo applicationInfo = mContext.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        String appName = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : mContext.getString(stringId);

        try {
            File path = new File(Environment.getExternalStorageDirectory().toString() + "/" + appName + "/crashlogs");
            if (path.exists()) {
                return path;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;

    }


    /**
     * Checks if external storage is available for read and write
     *
     * @return
     */
    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }


    /**
     * Checks if external storage is available to at least read
     *
     * @return
     */
    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

}
