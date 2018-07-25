package com.gp.rainy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gp.rainy.location.ILocation;
import com.gp.rainy.location.LocationPresenter;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationPresenter lp = new LocationPresenter(MainActivity.this, new ILocation() {
                    @Override
                    public void locationSuccess(String lat, String lon, String location) {
                        Log.d(TAG, "location-lat:" + lat + "-lon:" + lon + "-address:" + location);
                    }

                    @Override
                    public void locationFailed() {
                        Log.d(TAG, "location failed");
                    }
                });
                lp.doLocation();
            }
        });

        findViewById(R.id.write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (writeToSDCardFile("aa", "bb")) {
                    Toast.makeText(MainActivity.this, "file success", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.toWebview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, WebViewActivity.class));
            }
        });

        if (requestStoagePermission()) {
            FileUtils.createProjectSdcardFile();
        }
    }


    private void vibrateShort() {
        Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(1000);
    }

    private void vibrateLong(){
        Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        long[] patter = {1000, 1000, 1000, 1000};
        vibrator.vibrate(patter, 0);
    }

    private void stopVibrate() {
        Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
//        assert vibrator != null;
        vibrator.cancel();
    }

    /**
     * 获取手机Android 版本（4.4、5.0、5.1 ...）
     *
     * @return
     */
    public static String getBuildVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机Android API等级（22、23 ...）
     *
     * @return
     */
    public static int getBuildLevel() {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * 判断网络是否可用
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//        assert cm != null;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
    /**
     * 获取当前网络类型
     */
    public static String getNetworkType(Context context) {
        String netType = "NETWORK_NONE";
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                netType = "NETWORK_WIFI";
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (info.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        /** 2G网络 */
                        return "NETWORK_2G";
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
                        return "NETWORK_3G";
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        /** 4G网络 */
                        return "NETWORK_4G";
                    default:
                        break;
                }

                netType = "NETWORK_MOBILE";
            }
        }
        return netType;
    }

    public void saveMessage(String key, String value) {
        PreferenceUtils.setPreferenceString(this, key, value);
    }

    public void getMessage(String key) {
        PreferenceUtils.getPreferenceString(this, key, "");
    }

    //sdcard 目录
    public boolean writeToSDCardFile(String fileName, String Content) {
        boolean isSuccess = FileUtils.writeSDCardFile(Environment.getExternalStorageDirectory() + Constants.DIR_DOWNLOAD + fileName, Content, true);
        return isSuccess;
    }

    public boolean writeToInnerFile(String fileName, String Content) {
        boolean isSuccess = FileUtils.writeInnerFile( this, Content, fileName);
        return isSuccess;
    }

    public boolean requestStoagePermission() {
        boolean isPermissionAllow = false;
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, permissions, Constants.STORAGE_PERMISSION_REQ_CODE);
        } else {
            isPermissionAllow = true;
        }
        return isPermissionAllow;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.LOCATION_PERMISSION_REQ_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "permission sucess", Toast.LENGTH_SHORT).show();
                } else {
                    // Permission Denied
//                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.STORAGE_PERMISSION_REQ_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FileUtils.createProjectSdcardFile();
                } else {
                    Toast.makeText(this, "需要开启存储权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
