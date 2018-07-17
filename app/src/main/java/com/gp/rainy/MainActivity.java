package com.gp.rainy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

        findViewById(R.id.toWebview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, WebViewActivity.class));
            }
        });

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
                }

                netType = "NETWORK_MOBILE";
            }
        }
        return netType;
    }

    public boolean getLocationPermission() {
        boolean isPermissionAllow = false;
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 100);
        } else {
            isPermissionAllow = true;
        }
        return isPermissionAllow;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "permission sucess", Toast.LENGTH_SHORT).show();
                //成功
            } else {
                // Permission Denied
                Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
