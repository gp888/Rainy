package com.gp.rainy;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 设备信息工具类
 */
public class DeviceUtil {

    //获取UUID
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    //获取版本号
    public static String getVersionName() {
        try {
            PackageInfo pi = App.globalContext.getPackageManager().getPackageInfo(App.globalContext.getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    //获取设备型号
    public static String getMODEL() {
        try {
            return Build.MODEL;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    //设备的系统版本
    public static String getSystemRelease() {
        try {
            return Build.VERSION.RELEASE;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    //设备的系统版本
    public static String getSystemSDKVersion() {
        try {
            return Build.VERSION.SDK;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 设备编码
     */
    private static final Pattern DEVICE_CODE_PATTERN = Pattern.compile("[\u0391-\uFFE5]");

    //设备编码
    public static String getAppVersion() {
        String device_model = DeviceUtil.getMODEL(); // 设备型号
        Matcher m = DEVICE_CODE_PATTERN.matcher(device_model);
        if (m.find()) {
            // 如果是汉字和特殊符号
            device_model = "";
        }

        String version_sdk = DeviceUtil.getSystemSDKVersion(); // 设备SDK版本
        String version_release = DeviceUtil.getSystemRelease();// 设备的系统版本
        String uniqueId = getDeviceUUID();
        String OA_APP_VERSION = getAppVersionNumber();
        String appVersion = "com.gp.rainy/" + OA_APP_VERSION + "/" + "(" + device_model
                + ";version_sdk:" + version_sdk + ";version_release:" + version_release + ";deviceId:" + uniqueId + ")";
        MyLogUtil.e(appVersion);
        return appVersion;
    }

    //设备号
    public static synchronized String getDeviceUUID() {
        String uniqueId = PreferenceUtils.getPreferenceString(App.globalContext, Constants.SHARE_DEVICE_ID, "");
        if ("".equals(uniqueId)) {
            uniqueId = UUID.randomUUID().toString();
            PreferenceUtils.setPreferenceString(App.globalContext, Constants.SHARE_DEVICE_ID, uniqueId);
        }
        return uniqueId;
    }

    //获取版本号
    public static String getAppVersionNumber() {
        String OA_APP_VERSION = "";
        try {
            OA_APP_VERSION = DeviceUtil.getVersionName();
            MyLogUtil.d("version-number1:" + OA_APP_VERSION);
            String[] versionList = OA_APP_VERSION.split("\\.");
            MyLogUtil.d("version-number3-versionList-lenght:" + versionList.length);
            OA_APP_VERSION = "";
            for (String versionNumber : versionList) {
                if (OA_APP_VERSION.length() <= 0) {
                    OA_APP_VERSION = versionNumber + ".";
                } else {
                    OA_APP_VERSION += versionNumber;
                }
            }
            MyLogUtil.d("version-number3:" + OA_APP_VERSION);
        } catch (Exception ext) {
            ext.printStackTrace();
        }
        return OA_APP_VERSION;
    }
}
