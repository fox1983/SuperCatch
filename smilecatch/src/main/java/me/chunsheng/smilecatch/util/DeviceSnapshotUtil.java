package me.chunsheng.smilecatch.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 获取设备的当前信息
 * eg: 设备名称，系统版本，电量等信息
 */

public class DeviceSnapshotUtil {

    private static long mTotalMemory = -1;

    /**
     * 检测手机是否Rooted
     *
     * @return
     */
    private static boolean isRooted(Context context) {
        boolean isSdk = isGoogleSdk(context);
        Object tags = Build.TAGS;
        if ((!isSdk) && (tags != null)
                && (((String) tags).contains("test-keys"))) {
            return true;
        }
        if (new File("/system/app/Superuser.apk").exists()) {
            return true;
        }
        if ((!isSdk) && (new File("/system/xbin/su").exists())) {
            return true;
        }
        return false;
    }

    private static boolean isGoogleSdk(Context context) {
        String str = Settings.Secure.getString(context
                .getContentResolver(), Secure.ANDROID_ID);
        return ("sdk".equals(Build.PRODUCT))
                || ("google_sdk".equals(Build.PRODUCT)) || (str == null);
    }

    /**
     * 获取手机剩余电量
     *
     * @return
     */
    private static String battery(Context mContext) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = mContext.registerReceiver(null, filter);
        int level = intent.getIntExtra("level", -1);
        int scale = intent.getIntExtra("scale", -1);
        if (scale == -1) {
            return "--";
        } else {
            return String.format(Locale.US, "%d %%", (level * 100) / scale);
        }
    }

    private static long getAvailMemory(Context mContext) {
        ActivityManager am = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    private static String parseFile(File file, String filter) {
        String str = null;
        if (file.exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file), 1024);
                String line;
                while ((line = br.readLine()) != null) {
                    Pattern pattern = Pattern.compile("\\s*:\\s*");
                    String[] ret = pattern.split(line, 2);
                    if (ret != null && ret.length > 1 && ret[0].equals(filter)) {
                        str = ret[1];
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return str;
    }

    private static long getSize(String size, String uint, int factor) {
        return Long.parseLong(size.split(uint)[0].trim()) * factor;
    }

    private static synchronized long getTotalMemory(Context mContext) {
        if (mTotalMemory == -1) {
            long total = 0L;
            String str;
            try {
                if (!TextUtils.isEmpty(str = parseFile(
                        new File("/proc/meminfo"), "MemTotal"))) {
                    str = str.toUpperCase(Locale.US);
                    if (str.endsWith("KB")) {
                        total = getSize(str, "KB", 1024);
                    } else if (str.endsWith("MB")) {
                        total = getSize(str, "MB", 1048576);
                    } else if (str.endsWith("GB")) {
                        total = getSize(str, "GB", 1073741824);
                    } else {
                        total = -1;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mTotalMemory = total;
        }
        return mTotalMemory;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private static long[] getSdCardMemory() {
        long[] sdCardInfo = new long[2];
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            if (Build.VERSION.SDK_INT >= 18) {
                long bSize = sf.getBlockSizeLong();
                long bCount = sf.getBlockCountLong();
                long availBlocks = sf.getAvailableBlocksLong();
                sdCardInfo[0] = bSize * bCount;
                sdCardInfo[1] = bSize * availBlocks;
            } else {
                long bSize = sf.getBlockSize();
                long bCount = sf.getBlockCount();
                long availBlocks = sf.getAvailableBlocks();
                sdCardInfo[0] = bSize * bCount;
                sdCardInfo[1] = bSize * availBlocks;
            }
        }
        return sdCardInfo;
    }

    private static String disk() {
        long[] info = getSdCardMemory();
        long total = info[0];
        long avail = info[1];
        if (total <= 0) {
            return "--";
        } else {
            float ratio = (float) ((avail * 100) / total);
            return String.format(Locale.US, "%.01f%% [%s]", ratio, getSizeWithUnit(total));
        }
    }

    private static String ram(Context mContext) {
        long total = getTotalMemory(mContext);
        long avail = getAvailMemory(mContext);
        if (total <= 0) {
            return "--";
        } else {
            float ratio = (float) ((avail * 100) / total);
            return String.format(Locale.US, "%.01f%% [%s]", ratio, getSizeWithUnit(total));
        }
    }

    private static String getSizeWithUnit(long size) {
        if (size >= 1073741824) {
            float i = (float) (size / 1073741824);
            return String.format(Locale.US, "%.02f GB", i);
        } else if (size >= 1048576) {
            float i = (float) (size / 1048576);
            return String.format(Locale.US, "%.02f MB", i);
        } else {
            float i = (float) (size / 1024);
            return String.format(Locale.US, "%.02f KB", i);
        }
    }

    private static String loadVersionInfo(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (pi != null) {
                return pi.versionName;
            } else {
                return "";
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String getPhoneModelWithManufacturer() {
        return Build.MANUFACTURER + " " + android.os.Build.MODEL;
    }

    private static String getOsInfo() {
        return android.os.Build.VERSION.RELEASE;
    }

    private static String getNetworkInfo(Context context) {
        String info = "";
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo activeNetInfo = connectivity.getActiveNetworkInfo();
            if (activeNetInfo != null) {
                if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    info = activeNetInfo.getTypeName();
                } else {
                    StringBuilder sb = new StringBuilder();
                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    sb.append(activeNetInfo.getTypeName());
                    sb.append(" [");
                    if (tm != null) {
                        // Result may be unreliable on CDMA networks
                        sb.append(tm.getNetworkOperatorName());
                        sb.append("#");
                    }
                    sb.append(activeNetInfo.getSubtypeName());
                    sb.append("]");
                    info = sb.toString();
                }
            }
        }
        return info;
    }

    public static String snapshot(Context context) {
        Map<String, String> info = new LinkedHashMap<String, String>();
        info.put("Device Name: ", getPhoneModelWithManufacturer());
        info.put("Android Version: ", getOsInfo());
        info.put("Device System: ", Build.DISPLAY);
        info.put("Device Battery: ", battery(context));
        info.put("Device Rooted: ", isRooted(context) ? "yes" : "no");
        info.put("Device Ram: ", ram(context));
        info.put("Device Disk: ", disk());
        info.put("App Verion: ", String.format("%s", loadVersionInfo(context)));
        info.put("Device Network: ", getNetworkInfo(context));
        Iterator<Map.Entry<String, String>> iterator = info.entrySet().iterator();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            if (entry != null) {
                sb.append(entry.getKey()).append(entry.getValue());
                sb.append(System.getProperty("line.separator"));
            }
        }
        return sb.toString();
    }

}
