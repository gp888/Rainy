package com.gp.rainy;

import android.Manifest;
import android.os.Environment;

import java.io.File;

public class Constants {

    public static final String mainUrl = "https://testapp.yglmart.com/#/ceshi";

    public static final String mainUrl1 = "https://apptest.coderplay.top/";
    public static final String testUrl = "file:///android_asset/jssdk/demo.html";
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
    public static final int fingerPrint = 214;
    public static final String SelectImage = "SelectImage";
    public static final int selectImage = 203;

    public static final int OPENGJSALERT = 204;
    public static final int FINGERSUCCESS = 205;
    public static final int FINGERFAIL = 206;

    public static final String GetLocation = "GetLocation";
    public static final int getLocation = 207;
    public static final String Share = "Share";
    public static final int share = 209;
    public static final String WeChatPay = "WeChatPay";
    public static final int weChatPay = 210;
    public static final String Alipay = "Alipay";
    public static final int alipay = 211;
    public static final String ThirdLogin = "ThirdLogin";
    public static final int thirdLogin = 212;
    public static final String Gyro = "Gyro";
    public static final int gyro = 213;
    public static final String CloseGyro = "CloseGyro";
    public static final int closeGyro = 199;
    public static final String NetworkStatus = "NetworkStatus";
    public static final int networkStatus = 215;
    public static final String Identify = "UniquelyIdentifies";
    public static final int identify = 216;
    public static final String Logout = "Logout";
    public static final int logout = 217;
    public static final String CacheFile = "CacheFile";
    public static final int cacheFile = 218;
    public static final String CacheUserAccount = "CacheUserAccount";
    public static final int cacheUserAccount = 219;
    public static final String DeleteUserAccount = "DeleteUserAccount";
    public static final int deleteUserAccount = 230;

    public static final String CloseApp = "CloseApp";
    public static final int closeApp = 231;
    public static final String ScanCode = "ScanCode";
    public static final int scanCode = 232;
    public static final String OpenMap = "OpenMap";
    public static final int openMap = 233;

    public static final String StatusBarStyle = "StatusBarStyle";
    public static final int statusBarStyle = 234;
    public static final String PlaySound = "PlaySound";
    public static final int playSound = 235;


    public static final String ContactList = "ContactList";
    public static final int contactList = 236;
    public static final String DeleteCacheFile = "DeleteCacheFile";
    public static final int deleteCacheFile = 237;

    public static final String JgPushReg = "JgPushReg";
    public static final int jgPushReg = 238;

    public static final String RemoveJgTag = "RemoveJgTag";
    public static final int removeJgTag = 239;

    public static String PHOTOFILEPATH = "";

    public static final int CHOICE_CMARE = 20101;
    public static final int CHOICE_PHOTO = 20102;
    public static final int CHOICE_MEDIA = 20103;
    public static final int REQUEST_CODE = 20104;
    public static final int REQUEST_CAMERA = 20105;
    public static final int REQUEST_CONTACT = 20106;

    public static final String FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider";

    public static final String[] camraString = {Manifest.permission.CAMERA};
    public static final String[] audioString = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final String[] contactsString = {Manifest.permission.READ_CONTACTS};

    public static final String[] storgePermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final String UPLOADPIC = "http://59.110.169.175:8080/admin/dynamic/upload.do";
    public static final String UPLOADPIC1 = "http://test.bjyishubiyeji.com:8080/admin/dynamic/upload.do";
    public static final String UPLOADPIC2 = "http://test.bjyishubiyeji.com:8080/uploadImgs";
    public static final String UPLOADURL = "uploadUrl";
    public static final String AUTHORIZATION = "Authorization";
    public static final String accountArray = "accountArray";
    public static final String DOWNLOAD_ID = "rainy.download_id";
    public static final String cacheStr = "cacheStr";

    public static final String FINGERPRINT_ID_LIST = "fingerprint_id_list";//指纹id集合

}
