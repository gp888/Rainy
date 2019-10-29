package com.gp.rainy.Biometric;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.text.TextUtils;

import com.gp.rainy.App;
import com.gp.rainy.Constants;
import com.gp.rainy.encypt.Encrypt.EncrypMD5;
import com.gp.rainy.utils.JsonUtils;
import com.gp.rainy.utils.PreferenceUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 指纹工具类
 *
 * @author wdp
 */
public class FingerprintUtils {

    /**
     * 获取指纹token
     *
     * @return
     */
    public static String getFingerprintToken(Context context) {
        String fingerprintToken = "";
        try {
            //加密（用户id+设备id）
            String archiveIdAndDevicesId = "userId" + UUID.randomUUID().toString();
            //MD5加密（archiveIdAndDevicesId+业务名称）
            fingerprintToken = EncrypMD5.encryption(archiveIdAndDevicesId + "yewuname");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fingerprintToken;
    }

    /**
     * 保存当前指纹信息
     */
    public static void saveFingerprintInfo() {
        if (!isAboveApi23()) {
            return;
        }

        String fingerprint = JsonUtils.listToJson(getFingerPrintInfo());
        PreferenceUtils.setPreferenceString(App.globalContext, Constants.FINGERPRINT_ID_LIST, fingerprint);
    }

    /**
     * 检测指纹id是否有变化
     *
     * @return true：有变化
     */
    public static boolean checkFingerprintId() {
        if (!isAboveApi23()) {
            return true;
        }
        String fingerprintId = PreferenceUtils.getPreferenceString(App.globalContext, Constants.FINGERPRINT_ID_LIST, "");
        if (TextUtils.isEmpty(fingerprintId)) {
            return false;
        }

        List<String> oldList = JsonUtils.fromJsonToList(fingerprintId, String.class);
        List<String> newList = getFingerPrintInfo();

        if (oldList.size() != newList.size()) {
            return true;
        }

        for (int i = 0; i < newList.size(); i++) {
            String oldId = oldList.get(i);
            String newId = newList.get(i);

            if (oldId == null) {
                return true;
            }

            if (!oldId.equals(newId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 通过反射获取到指纹id，有getFingerId、getName、getGroupId、getDeviceId四种属性可以获取到
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public static List<String> getFingerPrintInfo() {
        List<String> listFingerprints = new ArrayList<>(5);
        try {
            Class clz = Class.forName("android.hardware.fingerprint.FingerprintManager");
            Method method = clz.getDeclaredMethod("getEnrolledFingerprints", new Class[]{});
            method.setAccessible(true);
            FingerprintManager fingerprintManager = (FingerprintManager) App.globalContext.getSystemService(Context.FINGERPRINT_SERVICE);
            Object obj = method.invoke(fingerprintManager, new Object[]{});
            if (obj == null) {
                return listFingerprints;
            }
            Class<?> clazz = Class.forName("android.hardware.fingerprint.Fingerprint");
            Method getFingerId = clazz.getDeclaredMethod("getFingerId");

            for (int i = 0; i < ((List) obj).size(); i++) {
                Object item = ((List) obj).get(i);
                if (null == item) {
                    continue;
                }
                int mFingerId = (int) getFingerId.invoke(item);
                listFingerprints.add(String.valueOf(mFingerId));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return listFingerprints;
        }

        return listFingerprints;
    }

    /**
     * android版本是否大于6.0
     *
     * @return
     */
    public static boolean isAboveApi23() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
