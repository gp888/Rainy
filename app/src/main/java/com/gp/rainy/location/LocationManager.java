package com.gp.rainy.location;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.gp.rainy.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationManager {
    private static String TAG = LocationManager.class.getSimpleName();

    private onLocationListener locationListener;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            List<Map<String, String>> locationMap = (List<Map<String, String>>) msg.obj;
            Map<String, String> latMap = locationMap.get(0);
            Map<String, String> lonMap = locationMap.get(1);
            Map<String, String> addrMap = locationMap.get(2);
            String strAddress = addrMap.get("address");
            if (TextUtils.isEmpty(strAddress)) {
                //定位失败回调
                locationListener.onLocationChanged(-1, "0", "0", strAddress);
            } else {
                //定位成功回调
                locationListener.onLocationChanged(0, latMap.get("latitude"), lonMap.get("longitude"), strAddress);
            }
        }
    };

    //监测定位权限
    public static boolean checkLocationPermission(Context mContext) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions((AppCompatActivity)mContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOCATION_PERMISSION_REQ_CODE);
                return false;
            }
        }
        return true;
    }

    /**
     * 根据经纬度解码地理位置
     * @param mContext
     * @param location
     * @return
     */
    private void getAddressFromLocation(Context mContext, Location location) {
        List<Map<String, String>> locationMap = new ArrayList<>();
        Map<String, String> latMap = new HashMap<>();
        Map<String, String> lonMap = new HashMap<>();
        Map<String, String> addrMap = new HashMap<>();
        Geocoder geocoder = new Geocoder(mContext);
        double latitude = location.getLatitude();
        latMap.put("latitude", latitude + "");
        locationMap.add(latMap);
        double longitude = location.getLongitude();
        lonMap.put("longitude", longitude + "");
        locationMap.add(lonMap);
        Log.d(TAG, "getAddressFromLocation->lat:" + latitude + ", long:" + longitude);
//            if (!Geocoder.isPresent()) {
//                Log.d(TAG, "Geocoder 服务不可用" + Geocoder.isPresent());
//                return "定位失败";
//            }
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Message message = handler.obtainMessage();
        if (addresses != null && addresses.size() > 0) {
            //返回当前位置，精度可调
            Address address = addresses.get(0);
            String sAddress;
            if (!TextUtils.isEmpty(address.getLocality())) {
                if (!TextUtils.isEmpty(address.getFeatureName())) {
                    sAddress = address.getLocality() + " " + address.getFeatureName();
                } else {
                    sAddress = address.getLocality();
                }
            } else {
                sAddress = "";
            }
            addrMap.put("address", sAddress);
            locationMap.add(addrMap);
            message.obj = locationMap;
            handler.sendMessage(message);
            return;
        }
        addrMap.put("address", "");
        locationMap.add(addrMap);
        message.obj = locationMap;
        handler.sendMessage(message);
    }

    /**
     * 获取位置
     * @param mContext
     * @param locationListener
     * @return
     */
    public boolean getMyLocation(final Context mContext, onLocationListener locationListener) {
        this.locationListener = locationListener;
        final android.location.LocationManager locationManager = (android.location.LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        //判断网络定位是否可用
        if (!locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
            //调用定位提示对话框，打开定位功能
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("尚未开启位置定位服务");
            builder.setPositiveButton("开启", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //启动定位Activity
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(intent);
                }
            });

            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            builder.show();
            return false;
        }
        //权限检查
        if (!checkLocationPermission(mContext)) {
            return false;
        }

        // 通过网络获取位置
        final Location curLoc = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
        if (null == curLoc) {
            LocationListener mLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(final Location location) {
                    //获取解码的地理位置
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getAddressFromLocation(mContext, location);
                        }
                    }).start();
                    //关闭 GPS 定位功能
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    //关闭 GPS 定位功能
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onProviderEnabled(String provider) {
                    //关闭 GPS 定位功能
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onProviderDisabled(String provider) {
                    //关闭 GPS 定位功能
                    locationManager.removeUpdates(this);
                }
            };
            //设置监听器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
            locationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER,8000,0,mLocationListener);
        }else {
            //获取解码的地理位置
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getAddressFromLocation(mContext, curLoc);
                }
            }).start();
        }
        return true;
    }

    //自定义定位监听回调接口
    public interface onLocationListener {
        void onLocationChanged(int code, String lat1, String long1, String location);
    }

}
