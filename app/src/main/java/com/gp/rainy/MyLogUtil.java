package com.gp.rainy;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MyLogUtil {

    private static final String TAG = BuildConfig.FLAVOR;
    private static boolean on = true;

    /**
     * 日志长度
     */
    private static int LOG_MAX_LENGTH = 3 * 1024;

    /**
     * 日志文件保存的天数
     */
    private static int LOG_DAYS = 5;

    /**
     * 格式化日期
     * notice:SimpleDateFormat线程不安全
     */
    private static ThreadLocal<DateFormat> threadFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };


    /**
     * 设置日志开关
     */
    public static void setLogEnable(boolean flag) {
        on = flag;
    }


    public static void e(String msg) {
        if (on) {
            Log.e(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (on) {
            Log.d(TAG, msg);
        }
    }

    public static void v(String msg) {
        if (on) {
            Log.v(TAG, msg);
        }
    }

    public static void i(String msg) {
        if (!on) {
            return;
        }

        int strLength = msg.length();
        int start = 0;
        int end = LOG_MAX_LENGTH;

        // 长度小于等于限制直接打印
        if (strLength <= end) {
            Log.i(TAG, msg);
            return;
        }

        while (strLength > end) {
            Log.i(TAG, msg.substring(start, end));
            start = end;
            end = end + LOG_MAX_LENGTH;
        }
        //打印剩余日志
        String substring = msg.substring(start, strLength);
        Log.i(TAG, substring);

    }


    /**
     * 收集设备参数信息
     */
    private static Map<String, String> collectDeviceInfo() {
        Map<String, String> infoMap = new HashMap<>();
        try {
            PackageManager pm = App.globalContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(App.globalContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infoMap.put("versionName", versionName);
                infoMap.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infoMap.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return infoMap;
    }
}