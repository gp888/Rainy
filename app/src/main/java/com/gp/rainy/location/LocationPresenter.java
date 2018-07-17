package com.gp.rainy.location;

import android.content.Context;

public class LocationPresenter {
    private Context mContext;
    private ILocation mLocation;
    private String TAG = LocationPresenter.class.getSimpleName();

    public LocationPresenter(Context mContext, ILocation mLocation) {
        this.mContext = mContext;
        this.mLocation = mLocation;
    }

    //初始化定位监听回调接口
    private LocationManager.onLocationListener mOnLocationListener = new LocationManager.onLocationListener() {
        @Override
        public void onLocationChanged(int code, String lat1, String long1, String location) {
            //0 表示成功
            if (0 == code) {
                mLocation.locationSuccess(lat1, long1, location);
            } else {
                mLocation.locationFailed();
            }
        }
    };

    //定位
    public void doLocation() {
        if (LocationManager.checkLocationPermission(mContext)) {
            //成功返回地理位置信息结果
            boolean success = new LocationManager().getMyLocation(mContext, mOnLocationListener);
            if (!success) {
                mLocation.locationFailed();
            }
        }
    }
}