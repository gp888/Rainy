package com.gp.rainy.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.gp.rainy.App;
import com.gp.rainy.Constants;
import com.gp.rainy.MyLogUtil;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;
import static com.gp.rainy.App.globalContext;

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
            PackageInfo pi = globalContext.getPackageManager().getPackageInfo(globalContext.getPackageName(), 0);
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
        String uniqueId = PreferenceUtils.getPreferenceString(globalContext, Constants.SHARE_DEVICE_ID, "");
        if ("".equals(uniqueId)) {
            uniqueId = UUID.randomUUID().toString();
            PreferenceUtils.setPreferenceString(globalContext, Constants.SHARE_DEVICE_ID, uniqueId);
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

    public static void phoneCall(Context context, String num) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + num));
        context.startActivity(intent);
    }

    public static void vibrateShort(Context context, Long millisecodes) {
        Vibrator vibrator = (Vibrator)context.getSystemService(VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            VibrationEffect vibrationEffect = VibrationEffect.createOneShot(millisecodes, DEFAULT_AMPLITUDE);
            if (vibrator != null) {
                vibrator.vibrate(vibrationEffect);
            }
        } else {
            vibrator.vibrate(millisecodes);
        }
    }

    public static void vibrateLong(Context context){
        Vibrator vibrator = (Vibrator)context.getSystemService(VIBRATOR_SERVICE);
        long[] patter = {1000, 1000, 1000, 1000};
        vibrator.vibrate(patter, 0);
    }

    public static void stopVibrate(Context context) {
        Vibrator vibrator = (Vibrator)context.getSystemService(VIBRATOR_SERVICE);
//        assert vibrator != null;
        vibrator.cancel();
    }

    public static boolean isMainProcess(Context context) {
        try {
            ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
            String mainProcessName = context.getPackageName();
            int myPid = android.os.Process.myPid();
            MyLogUtil.i("myPid -> " + myPid + ", mainProcessName -> " + mainProcessName);
            List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
            if (processInfos != null) {
                for (ActivityManager.RunningAppProcessInfo info : processInfos) {
                    MyLogUtil.i("info.pid -> " + info.pid + ", info.processName -> " + info.processName);
                    if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public static void openCamera(Context mContext) {
        String filename = "rainy_camera_" + System.currentTimeMillis() + ".jpg";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            File file = new File(Constants.DIR_IMAGE_TEMP, filename);
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            Uri imageUri = getUriForFile(mContext, file);
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);//设置Action为拍照
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//将拍取的照片保存到指定URI
            Constants.PHOTOFILEPATH = Constants.DIR_IMAGE_TEMP + "/" + filename;
            ((AppCompatActivity) mContext).startActivityForResult(intent, Constants.CHOICE_CMARE);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Constants.DIR_IMAGE_TEMP, filename)));
            Constants.PHOTOFILEPATH = Constants.DIR_IMAGE_TEMP + "/" + filename;
            ((AppCompatActivity) mContext).startActivityForResult(intent, Constants.CHOICE_CMARE);
        }
    }

    /**
     * 创建一个用于拍照图片输出路径的Uri (FileProvider)
     * @param context
     * @return
     */
    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context, Constants.FILE_PROVIDER_AUTHORITY, file);
    }

    public static void openPhotos(Context mContext) {
        try {
            //Intent intent_photo = new Intent(Intent.ACTION_GET_CONTENT); 错误
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            ((AppCompatActivity) mContext).startActivityForResult(intent, Constants.CHOICE_PHOTO);
        } catch (Exception ext) {
            ext.printStackTrace();
        }
    }

    public static final String CROP_TEMP_IMAGE_NAME = "crop_image.jpg";

    public static void cropImage(Context context, Uri uri) {
        try {
            if (uri != null) {
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
                    intent.setDataAndType(uri, "image/*");
                } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
                    String path = getImageAbsolutePath(context, uri);
                    if (!TextUtils.isEmpty(path)) {
                        File file = new File(path);
                        if (file.isFile() && file.exists()) {
                            intent.setDataAndType(Uri.fromFile(new File(path)), "image/*");
                        }
                    }
                } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    String path = getImageAbsolutePath(context, uri);
                    intent.setDataAndType(DeviceUtil.getUriForFile(context, new File(path)), "image/*");
                }
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("outputX", 600);
                intent.putExtra("outputY", 600);
                intent.putExtra("scale", true);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Constants.DIR_IMAGE_TEMP + CROP_TEMP_IMAGE_NAME)));
                intent.putExtra("return-data", false);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                intent.putExtra("noFaceDetection", true); // no face detection
                ((AppCompatActivity) context).startActivityForResult(intent, Constants.CHOICE_MEDIA);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取拍照或从图库选择图片后图片的真实路径(适用所有机型,解决Android4.4以上版本Uri转换)
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static synchronized String getImageAbsolutePath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean getUrlIsHttp(String url) {
        boolean flag = false;
        if (url.toLowerCase().indexOf("http") == 0 || url.toLowerCase().indexOf("https") == 0) {
            flag = true;
        }
        return flag;
    }

    /**
     * 空格
     */
    private static final Pattern STRING_SPACE_PATTERN = Pattern.compile("\\s*|\\t|\\r|\\n");

    public static String replaceBlank(String str) {
        String dest = "";
        try {
            if (str != null) {
                Matcher m = STRING_SPACE_PATTERN.matcher(str);
                dest = m.replaceAll("");
            }
        } catch (Exception ext) {
            ext.printStackTrace();
            dest = str;
        }
        return dest;
    }

    //获取屏幕宽度
    public static int getScreenWidth() {
        return PreferenceUtils.getPreferenceInt(globalContext, Constants.SHARE_SCREEN_WIDTH, 720);
    }

    //获取屏幕高度
    public static int getScreenHeight() {
        return PreferenceUtils.getPreferenceInt(globalContext, Constants.SHARE_SCREEN_HEIGHT, 1280);
    }

    public static boolean isApkDebugable() {
        try {
            ApplicationInfo info = globalContext.getApplicationInfo();
            if (info != null) {
                return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void loadApps(Context mContext) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps = mContext.getPackageManager().queryIntentActivities(intent, 0);
        //排序
        Collections.sort(apps, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo a, ResolveInfo b) {
                return String.CASE_INSENSITIVE_ORDER.compare(
                        a.loadLabel(mContext.getPackageManager()).toString(),
                        b.loadLabel(mContext.getPackageManager()).toString()
                );
            }
        });
        //for循环遍历ResolveInfo对象获取包名和类名
        for (int i = 0; i < apps.size(); i++) {
            ResolveInfo info = apps.get(i);
            String packageName = info.activityInfo.packageName;
            CharSequence cls = info.activityInfo.name;
            CharSequence name = info.activityInfo.loadLabel(mContext.getPackageManager());
            MyLogUtil.d("--packageName--" + cls);
        }
    }

    public static void getRunningApp(Context mContext) {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        ActivityManager.RunningAppProcessInfo processInfo = processes.get(0);
        String appPackageName = processInfo.processName.toString();
        MyLogUtil.d("--packageName--" + appPackageName);
    }

    /**
     * 获取当前网络类型
     * 0—无网络 1—wifi 2—3g/4g网络
     */
    public static int getNetworkType(Context context) {
        String netType = "NETWORK_NONE";
        int status = 0;
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                netType = "NETWORK_WIFI";
                status = 1;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (info.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        /** 2G网络 */
//                        return "NETWORK_2G";
                        return 2;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        /** 3G网络 */
//                        return "NETWORK_3G";
                        return 2;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        /** 4G网络 */
//                        return "NETWORK_4G";
                        return 2;
                    default:
                        break;
                }
                netType = "NETWORK_MOBILE";
                status = 2;
            }
        }
//        return netType;
        return status;
    }

    @SuppressLint("MissingPermission")
    public static String getImei() {
        String imei = "";
        TelephonyManager telephonyManager = (TelephonyManager) App.globalContext.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            if (telephonyManager != null) {
                imei = telephonyManager.getDeviceId();
                if (imei == null || imei.equals("")) {
//                    imei = getMac();
                }
            }

        } catch (Exception ext) {
            ext.printStackTrace();
        }

        return imei;
    }

    private static String toMD5(String text) throws NoSuchAlgorithmException {
        //获取摘要器MessageDigest
         MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        // 通过摘要器对字符串的二进制字节数组进行hash计算
        byte[] digest = messageDigest.digest(text.getBytes());
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < digest.length; i++) {
         //循环每个字符 将计算结果转化为正整数;
         int digestInt = digest[i] & 0xff;
//         将10进制转化为较短的16进制
         String hexString = Integer.toHexString(digestInt);
//        转化结果如果是个位数会省略0,因此判断并补0
         if (hexString.length() < 2) {
             sb.append(0);
         }
//       将循环结果添加到缓冲区
         sb.append(hexString); }
//       返回整个结果
        return sb.toString();
    }
}
