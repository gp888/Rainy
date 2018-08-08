package com.gp.rainy;

import android.Manifest;
import android.os.Environment;

import java.io.File;

public class Constants {

    public static final int LOCATION_PERMISSION_REQ_CODE = 100;
    public static final int STORAGE_PERMISSION_REQ_CODE = 101;
    public static final int CAMERA_PERMISSION_REQ_CODE = 102;
    public final static int JSShARENET = 103;
    //从JS调分享面板
    public static final int TO_WEBVIEW_FROM_JS = 104;

    public static File mExternalStorage = Environment.getExternalStorageDirectory();
    public static final String DIR_PROJECT = "/rainy/";
    public static final String DIR_DOWNLOAD = mExternalStorage + DIR_PROJECT + "download/"; //文件下载
    public static final String DIR_MEDIA = mExternalStorage + DIR_PROJECT + "media/"; //语音
    public static final String DIR_VOICE = mExternalStorage + DIR_PROJECT + "voice/"; //讯飞语音合成
    public static final String DIR_IMAGE = mExternalStorage + DIR_PROJECT + "image/"; //图片
    public static final String DIR_VIDEO = mExternalStorage + DIR_PROJECT + "video/"; //视频
    public static final String DIR_FILE = mExternalStorage + DIR_PROJECT + "file/";   //文件
    public static final String DIR_LOG = mExternalStorage + DIR_PROJECT + "log/";     //日志
    public static final String DIR_IMAGE_TEMP = mExternalStorage + DIR_PROJECT + "temp/"; //临时目录
    public static final String DIR_IMAGE_TEMP_NOMEDIA = mExternalStorage + DIR_PROJECT + "temp/.nomedia";
    public static final String SHARE_DEVICE_ID = "a8";
    public static final String SHARE_SCREEN_WIDTH = "a1"; // 屏幕宽度
    public static final String SHARE_SCREEN_HEIGHT = "a2"; // 屏幕高度


    public static final String CacheUserInfo = "CacheUserInfo";
    public static final int cacheUserInfo = 201;
    public static final String GetCacheUserInfo = "GetCacheUserInfo";
    public static final int getCacheUserInfo = 202;

    public static final String Call = "Call";
    public static final String PhoneVibration = "PhoneVibration";
    public static final String FingerPrint = "FingerPrint";
    public static final String ChooseImage = "ChooseImage";
    public static final int chooseImage = 203;

    public static final int OPENGJSALERT = 204;
    public static final int FINGERSUCCESS = 205;
    public static final int FINGERFAIL = 206;

    public static final String Locate = "Locate";
    public static final int locate = 207;
    public static final String Share = "Share";
    public static final int share = 209;
    public static final String WeChatPay = "WeChatPay";
    public static final int weChatPay = 210;
    public static final String Alipay = "Alipay";
    public static final int alipay = 211;

    public static String PHOTOFILEPATH = "";

    public static final int CHOICE_CMARE = 20101;
    public static final int CHOICE_PHOTO = 20102;
    public static final int CHOICE_MEDIA = 20103;

    public static final String FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider";

    public static final String[] camraString = {Manifest.permission.CAMERA};
    public static final String[] audioString = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final String[] contactsString = {Manifest.permission.READ_CONTACTS};

    public static final String[] storgePermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
}
