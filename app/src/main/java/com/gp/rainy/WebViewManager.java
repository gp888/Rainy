package com.gp.rainy;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alipay.sdk.app.PayTask;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.gp.rainy.location.ILocation;
import com.gp.rainy.location.LocationPresenter;
import com.gp.rainy.share.ShareInterface;
import com.gp.rainy.share.ShareManager;
import com.gp.rainy.share.SharePublicAccountModel;
import com.gp.rainy.utils.StatusBarUtil;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.gp.rainy.App.globalContext;

public class WebViewManager {

    private static String TAG = WebViewManager.class.getSimpleName();
    private Context mContext;
    public HashMap<String, String> functionHash = new HashMap<>();
    private JsInterface js_interface;
    private WXPayCallBackBean mWXPayCallBackBean;
    private UMShareAPI mShareAPI;
    private MaterialDialog mProgressDialog;

    public WebViewManager(Context context,UMShareAPI mShareAPI) {
        this.mContext = context;
        this.mShareAPI = mShareAPI;
    }

    @JavascriptInterface
    public void invoke(String request) {
        String m;
        String cmd = "";
        try {
            MyLogUtil.i(TAG + "--invoke-request:" + request);
            ((WebViewActivity)mContext).printLog("请求：" + request);
            JSONObject jsonObjParent = new JSONObject(request);
            m = jsonObjParent.getString("m");
            cmd = jsonObjParent.getString("cmd");

            functionHash.put(cmd, m);

            if (cmd.equals(Constants.CacheUserInfo)) {
                Iterator<String> iterator = jsonObjParent.keys();
                ArrayList<String> keys = new ArrayList<>();
                while(iterator.hasNext()){
                    String key = iterator.next();
                    if (!"cmd".equals(key) && !"m".equals(key)) {
                        String value = jsonObjParent.getString(key);
                        PreferenceUtils.setPreferenceString(mContext, key, value);
                        keys.add(key);
                    }
                }
                if (keys.size() < 1) {
                    sendHandler(0, "-1", "缓存用户数据失败", cmd, Constants.cacheUserInfo);
                } else {
                    PreferenceUtils.setPreferenceString(mContext, Constants.cacheStr, new Gson().toJson(keys));
                    Log.d("gpdata:", new Gson().toJson(keys));
                    sendHandler(1, "", "", cmd, Constants.cacheUserInfo, "缓存用户数据成功");
                }
            } else if (cmd.equals(Constants.GetCacheUserInfo)) {
                Bundle data = new Bundle();

                String result = PreferenceUtils.getPreferenceString(mContext, Constants.cacheStr, "");
                if (!TextUtils.isEmpty(result)) {
                    List<String> items = new Gson().fromJson(result, new TypeToken<List<String>>() {}.getType());
                    if (items != null && items.size() > 0) {
                        for(int i = 0; i < items.size(); i++) {
                            data.putString(items.get(i), PreferenceUtils.getPreferenceString(mContext, items.get(i), ""));
                        }
                    }
                }

                data.putString("jsonStr", PreferenceUtils.getPreferenceString(globalContext, Constants.accountArray, ""));
                if (PreferenceUtils.hasKey(mContext,"account") && !TextUtils.isEmpty(PreferenceUtils.getPreferenceString(mContext, "account", ""))) {
                    sendHandler(1, "", "", cmd, Constants.getCacheUserInfo, data);
                } else {
                    sendHandler(1, "", "", cmd, Constants.getCacheUserInfo, data);
                }
            } else if (cmd.equals(Constants.Call)) {
                String phoneNumber = jsonObjParent.getString("phone");
                DeviceUtil.phoneCall(mContext, phoneNumber);
                removeFunction(cmd);
            } else if (cmd.equals(Constants.PhoneVibration)) {
                DeviceUtil.vibrateShort(mContext, 300L);
                removeFunction(cmd);
            } else if (cmd.equals(Constants.FingerPrint)) {
                ((WebViewActivity)mContext).initFingerPrint();
            } else if (cmd.equals(Constants.SelectImage)) {
                String uploadUrl = jsonObjParent.getString("uploadUrl");
                PreferenceUtils.setPreferenceString(mContext, Constants.UPLOADURL, uploadUrl);
                final String copycmd = cmd;
                if (requestPermission(Constants.camraString, Constants.CAMERA_PERMISSION_REQ_CODE)) {
                    ListPopMenuDialogUtils menuDailogDAL = new ListPopMenuDialogUtils(mContext);
                    menuDailogDAL.showWebviewSelectPhotoDialog(new ListPopMenuDialogUtils.PhotoItemClick() {

                        @Override
                        public void itemclick(int position) {
                            if (position == 2) {
                                sendHandler(0, "-1", "没有选择图片", copycmd, Constants.selectImage);
                            }
                        }
                    });
                } else {
                    removeFunction(cmd);
                }
            } else if (Constants.Locate.equals(cmd)) {
                final String copyCmd = cmd;
                LocationPresenter lp = new LocationPresenter(mContext, new ILocation() {
                    @Override
                    public void locationSuccess(String lat, String lon, String location) {
                        Log.d(TAG, "location-lat:" + lat + "-lon:" + lon + "-address:" + location);
                        Bundle data = new Bundle();
                        data.putString("latitude", lat);
                        data.putString("longitude", lon);
                        data.putString("city", location);
                        sendHandler(1, "", "", copyCmd, Constants.locate, data);
                    }

                    @Override
                    public void locationFailed() {
                        Log.d(TAG, "location failed");
                        sendHandler(0, "-1", "失败", copyCmd, Constants.locate);
                    }
                });
                lp.doLocation();
            } else if (Constants.Share.equals(cmd)) {
                String title = jsonObjParent.getString("title");   //分享标题
                String desc = jsonObjParent.getString("desc");     //分享描述
                String imgUrl = jsonObjParent.getString("imgUrl"); //分享图标
                String link = jsonObjParent.getString("link");     //分享链接
                String platForm = jsonObjParent.getString("platForm");     //分享链接

//                if (TextUtils.isEmpty(title) && TextUtils.isEmpty(title) && TextUtils.isEmpty(title) && TextUtils.isEmpty(title)) {
//                    removeFunction(cmd);
//                    ToastUtil.showToastShort("分享参数错误，所有参数为必填项");
//                    return;
//                } else {
//                    if (!DeviceUtil.getUrlIsHttp(imgUrl)) {
//                        removeFunction(cmd);
//                        ToastUtil.showToastShort("分享参数错误，imgUrl参数必填以http开头");
//                        return;
//                    }
//                }
//                if (TextUtils.isEmpty(desc)) {
//                    desc = title;
//                }
                SharePublicAccountModel shareModel = new SharePublicAccountModel();
                shareModel.setcontent(desc);
                shareModel.settitle(title);
                shareModel.setimgurl(imgUrl);
                shareModel.seturl(link);
//                if (js_interface != null) {
//                    js_interface.callShareModelHandler(shareModel);
//                }


                ShareManager shareManager = new ShareManager(mContext);
                shareManager.setShareListener(new ShareInterface() {

                    @Override
                    public void sendShareHandler(int r, String errorCode, String error, String funName, int what) {
                        sendHandler(r, errorCode, error, funName, what);
                    }

                    @Override
                    public void sendShareHandler(int r, String errorCode, String error, String funName, int what, String data) {
                        sendHandler(r, errorCode, error, funName, what, data);
                    }

                    @Override
                    public void callShareHttpPost(JsonObject attrsObj) {
                    }

                });
                int platformId = 0;
                if ("wx".equals(platForm)) {
                    platformId = 0;
                } else if ("wx_circle".equals(platForm)){
                    platformId = 1;
                } else if ("qq".equals(platForm)) {
                    platformId = 2;
                } else if ("sina".equals(platForm)) {
                    platformId = 4;
                    boolean isIns = mShareAPI.isInstall((WebViewActivity)mContext, SHARE_MEDIA.SINA);
                    if (!isIns) {
                        sendHandler(0, "-1", "没安装微博", Constants.Share, Constants.share);
                    }
                }
                shareModel.setShareType(3);//link
                // 追加APPID
                if (!TextUtils.isEmpty(shareModel.geturl())) {
                    String url = shareModel.geturl();
                    if (!url.contains("?")) {
                        url = url + "?app=" + BuildConfig.APPID;
                    } else {
                        url = url + "&app=" + BuildConfig.APPID;
                    }
                    shareModel.seturl(url);
                }
                //0:wx,1:wx_circle,2:qq,4:sina
                shareManager.performShare(shareModel, platformId, true);

            } else if(Constants.WeChatPay.equals(cmd)) {
                IWXAPI api = WXAPIFactory.createWXAPI(mContext, BuildConfig.THIRDPART_WEIXIN_APPID); // 自己的微信公众号wxappid
                if (api.isWXAppInstalled()) {
                    String appid = "";
                    if (jsonObjParent.has("appid")) {
                        appid = jsonObjParent.get("appid").toString();
                        if (TextUtils.isEmpty(appid)) {
                            sendHandler(0, "-3", String.format(mContext.getString(R.string.js_param_error), "appid"), cmd, Constants.weChatPay);
                            return;
                        }
                    }

                    String partnerid = "";
                    if (jsonObjParent.has("partnerId")) {
                        partnerid = jsonObjParent.get("partnerId").toString();
                        if (TextUtils.isEmpty(partnerid)) {
                            sendHandler(0, "-3", String.format(mContext.getString(R.string.js_param_error), "partnerid"), cmd, Constants.weChatPay);
                            return;
                        }
                    }
                    String prepayid = "";
                    if (jsonObjParent.has("prepayId")) {
                        prepayid = jsonObjParent.get("prepayId").toString();
                        if (TextUtils.isEmpty(prepayid)) {
                            sendHandler(0, "-3", String.format(mContext.getString(R.string.js_param_error), "prepayid"), cmd, Constants.weChatPay);
                            return;
                        }
                    }
                    String mPackage = "";
                    if (jsonObjParent.has("package")) {
                        mPackage = jsonObjParent.get("package").toString();
                        if (TextUtils.isEmpty(mPackage)) {
                            sendHandler(0, "-3", String.format(mContext.getString(R.string.js_param_error), "mPackage"), cmd, Constants.weChatPay);
                        }
                    }
                    String noncestr = "";
                    if (jsonObjParent.has("nonceStr")) {
                        noncestr = jsonObjParent.get("nonceStr").toString();
                        if (TextUtils.isEmpty(noncestr)) {
                            sendHandler(0, "-3", String.format(mContext.getString(R.string.js_param_error), "noncestr"), cmd, Constants.weChatPay);
                            return;
                        }
                    }
                    String timestamp = "";
                    if (jsonObjParent.has("timeStamp")) {
                        timestamp = jsonObjParent.get("timeStamp").toString();
                        if (TextUtils.isEmpty(timestamp)) {
                            sendHandler(0, "-3", String.format(mContext.getString(R.string.js_param_error), "timestamp"), cmd, Constants.weChatPay);
                            return;
                        }
                    }
                    String sign = "";
                    if (jsonObjParent.has("sign")) {
                        sign = jsonObjParent.get("sign").toString();
                        if (TextUtils.isEmpty(sign)) {
                            sendHandler(0, "-3", String.format(mContext.getString(R.string.js_param_error), "sign"), cmd, Constants.weChatPay);
                            return;
                        }
                    }
                    Bundle data = new Bundle();
                    data.putString("partnerid", partnerid);
                    data.putString("prepayid", prepayid);
                    data.putString("mPackage", mPackage);
                    data.putString("noncestr", noncestr);
                    data.putString("timestamp", timestamp);
                    data.putString("sign", sign);
                    sendHandler(1, "", "", Constants.WeChatPay, Constants.weChatPay, data);
                } else {
                    sendHandler(0, "-4", mContext.getString(R.string.js_wx_pay_sendreq_fail), cmd, Constants.weChatPay);
                }
            } else if (Constants.Alipay.equals(cmd)) {
                String orderInfo = jsonObjParent.getString("orderStr");

                try {
                    orderInfo = new String(toBytes(orderInfo), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                String finalOrderInfo = orderInfo;
                Runnable payRunnable = new Runnable() {

                    @Override
                    public void run() {
                        PayTask alipay = new PayTask((AppCompatActivity)mContext);
                        Map<String, String> result = alipay.payV2(finalOrderInfo, true);
                        Log.i("Alipay_msp", result.toString());

                        if (js_interface != null) {
                            js_interface.callAlipayHandler(result);
                        }
                    }
                };
                Thread payThread = new Thread(payRunnable);
                payThread.start();
            } else if (Constants.ThirdLogin.equals(cmd)) {
                String third = jsonObjParent.getString("third");
                if (third.equals("qq")) {
                    if (mShareAPI.isInstall((AppCompatActivity)mContext, SHARE_MEDIA.QQ)) {
                        loginThird(SHARE_MEDIA.QQ, cmd);
                    } else {
                        sendHandler(0, "-1", mContext.getString(R.string.login_no_qq_client_warning), cmd, Constants.thirdLogin);
                    }
                } else if (third.equals("wx")) {
                    if (mShareAPI.isInstall((AppCompatActivity)mContext, SHARE_MEDIA.WEIXIN)) {
                        loginThird(SHARE_MEDIA.WEIXIN, cmd);
                    } else {
                        sendHandler(0, "-1", mContext.getString(R.string.login_no_wx_client_warning), cmd, Constants.thirdLogin);
                    }
                }
            } else if (Constants.Gyro.equals(cmd)) {
                double time = (double) jsonObjParent.get("gyroUpdateInterval");
                if (time <= 0) {
                    time = 0.1;
                }
                ((WebViewActivity)mContext).setGyro(true, time);
            } else if (Constants.CloseGyro.equals(cmd)) {
                removeFunction(cmd);
                ((WebViewActivity)mContext).setGyro(false, 0.1d);
            } else if (Constants.NetworkStatus.equals(cmd)) {
                int status = DeviceUtil.getNetworkType(mContext);
                sendHandler(1, "", "", cmd, Constants.networkStatus, status + "");
            } else if (Constants.Identify.equals(cmd)) {
                String SerialNumber = android.os.Build.SERIAL;
                String ANDROID_ID = Settings.System.getString(mContext.getContentResolver(), Settings.System.ANDROID_ID);
                Bundle data = new Bundle();
                data.putString("equipment", "Android");
                data.putString("uniquelyIdentifies", SerialNumber);
                sendHandler(1, "", "", Constants.Identify, Constants.identify, data);
            } else if (Constants.Logout.equals(cmd)) {
                String result = PreferenceUtils.getPreferenceString(mContext, Constants.cacheStr, "");
                if (!TextUtils.isEmpty(result)) {
                    List<String> items = new Gson().fromJson(result, new TypeToken<List<String>>() {}.getType());
                    if (items != null && items.size() > 0) {
                        for(int i = 0; i < items.size(); i++) {
                            PreferenceUtils.remove(items.get(i));
                        }
                    }
                }
                PreferenceUtils.remove(Constants.cacheStr);

//                PreferenceUtils.remove("account");
                sendHandler(1, "", "", Constants.Logout, Constants.logout, "退出成功");
            } else if (Constants.CacheFile.equals(cmd)) {
                String fileUrl = jsonObjParent.getString("fileUrl");
                String targetFilepath = jsonObjParent.getString("targetFilepath");
                ((WebViewActivity)mContext).patchSource(fileUrl, targetFilepath);
            } else if (Constants.CacheUserAccount.equals(cmd)) {
                String account = jsonObjParent.getString("account");
                String password = jsonObjParent.getString("password");
                PreferenceUtils.setPreferenceString(mContext, account, account + "-" + password);
                AccountHelper.getIns().cache(account, password);
                sendHandler(1, "", "", Constants.CacheUserAccount, Constants.cacheUserAccount, "存储成功");
            } else if (Constants.DeleteUserAccount.equals(cmd)) {
                String account = jsonObjParent.getString("account");
                PreferenceUtils.setPreferenceString(mContext, account, "");
                String jsonStr = AccountHelper.getIns().delete(account);
                Bundle data = new Bundle();
                data.putString("jsonStr", jsonStr);
                sendHandler(1, "", "", Constants.DeleteUserAccount, Constants.deleteUserAccount, data);
            } else if (Constants.CloseApp.equals(cmd)) {
                android.os.Process.killProcess(android.os.Process.myPid());
            } else if (Constants.ScanCode.equals(cmd)) {
                if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)) {
                    ActivityCompat.requestPermissions((WebViewActivity) mContext, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA);
                } else {
                    Intent intent = new Intent(mContext, CaptureActivity.class);
                    ((WebViewActivity) mContext).startActivityForResult(intent, Constants.REQUEST_CODE);
                }
            } else if (Constants.OpenMap.equals(cmd)) {
                openMap(MapNaviUtils.isGdMapInstalled(), MapNaviUtils.isBaiduMapInstalled());
                removeFunction(cmd);
            } else if (Constants.StatusBarStyle.equals(cmd)) {
                String style = jsonObjParent.getString("style");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if ("Default".equals(style)) {
                        StatusBarUtil.setStatusBarColor((WebViewActivity) mContext,  R.color.black1);
                    } else if ("Light".equals(style)) {
                        StatusBarUtil.setStatusBarColor((WebViewActivity) mContext,  Color.WHITE);
//                        StatusBarUtil.setImmersiveStatusBar((WebViewActivity) mContext,true);
//                        setAndroidNativeLightStatusBar((WebViewActivity) mContext, false);
//                        StatusBarUtil.setTranslucentStatus((WebViewActivity) mContext);
                        StatusBarUtil.setStatusBarColor((WebViewActivity) mContext,  R.color.black1);
                    }
                }
                sendHandler(1, "", "", Constants.StatusBarStyle, Constants.statusBarStyle, "修改成功");
            } else if (Constants.PlaySound.equals(cmd)) {
                String soundType = jsonObjParent.getString("soundType");
                String pathUrl = jsonObjParent.getString("pathUrl");
                if ("internet".equals(soundType)) {
                    AudioManager.getInstance(mContext).playMedia("", pathUrl, 0, this);
                } else if ("local".equals(soundType)){
                    String path = Environment.getExternalStorageDirectory() + "/rainy/";
                    AudioManager.getInstance(mContext).playMedia(path + pathUrl, "", 1,this);
                }
            } else if (Constants.ContactList.equals(cmd)) {
                if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS)) {
                    ActivityCompat.requestPermissions((WebViewActivity) mContext, new String[]{Manifest.permission.READ_CONTACTS}, Constants.REQUEST_CONTACT);
                } else {

//                    List<Contact> array = new ArrayList<>();
//                    Contact contact = new Contact();
//                    contact.phone = "123";
//                    contact.name = "456";
//                    array.add(contact);
//                    String str = new Gson().toJson(array);
//                   sendHandler(1, "", "", Constants.ContactList, Constants.contactList, str);
                    queryContactPhoneNumber();
                }
            } else if (Constants.DeleteCacheFile.equals(cmd)) {
                String path = jsonObjParent.getString("path");
                String filePath = Environment.getExternalStorageDirectory() + "/rainy/" + path + "/";
                FileUtils.deleteFileByDirectory(new File(filePath));
                sendHandler(1, "", "", Constants.DeleteCacheFile, Constants.deleteCacheFile, "删除成功");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            //请求数据格式异常
            sendHandler(0, "-1", mContext.getString(R.string.js_error), cmd, Constants.cacheUserInfo);
        }
    }

    public void sendHandler(int r, String errorCode, String error, String funName, int what) {
        Bundle lbd = new Bundle();
        lbd.putString("errorCode", errorCode);
        lbd.putString("error", error);
        lbd.putInt("r", r);
        lbd.putString("f", funName);
        sendHandler(what, lbd);
    }

    public void sendHandler(int r, String errorCode, String error, String funName, int what, String data) {
        Bundle lbd = new Bundle();
        lbd.putString("error", error);
        lbd.putString("errorCode", errorCode);
        lbd.putInt("r", r);
        lbd.putString("f", funName);
        lbd.putString("data", data);
        sendHandler(what, lbd);
    }

    public void sendHandler(int r, String errorCode, String error, String funName, int what, Bundle data) {
        Bundle lbd = data;
        lbd.putString("error", error);
        lbd.putString("errorCode", errorCode);
        lbd.putInt("r", r);
        lbd.putString("f", funName);
        sendHandler(what, lbd);
    }

    private void sendHandler(int what, Bundle data) {
        Message msg = jsHandler.obtainMessage();
        msg.what = what;
        msg.setData(data);
        jsHandler.sendMessage(msg);
    }

    private Handler jsHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundleData = msg.getData();
            String cmd = bundleData.get("f").toString();
            if (!functionHash.containsKey(cmd)) {
                return;
            }

            String fun = functionHash.get(cmd);
            int r = bundleData.getInt("r", -1);
            if (r == 0) {
                //错误情况
                callbackJsFun(fun, getParentJson(0, bundleData.get("error").toString(), bundleData.get("errorCode").toString()).toString());
                removeFunction(cmd);
                return;
            }

            //移除客户端回调方法状态
            JsonObject ParentJson = getParentJson(bundleData);
            ParentJson.addProperty("nonstop", 0);
            switch (msg.what) {
                case Constants.cacheUserInfo: {
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.get("data") != null) {
                        DataJson.addProperty("msg", bundleData.get("data").toString());
                    }
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                break;
                case Constants.getCacheUserInfo: {
                    JsonObject DataJson = new JsonObject();


                    if (!TextUtils.isEmpty(PreferenceUtils.getPreferenceString(mContext, Constants.cacheStr, ""))) {
                        DataJson.addProperty("isLogin", 1);
                    } else {
                        DataJson.addProperty("isLogin", 0);
                    }

                    String str = bundleData.getString("jsonStr");
                    JsonArray array = null;
                    if (!TextUtils.isEmpty(str)) {
                        array = new JsonParser().parse(str).getAsJsonArray();
                    } else {
                        array = new JsonArray();
                    }
                    DataJson.add("userList", array);

                    JsonObject object = new JsonObject();
                    bundleData.remove("jsonStr");
                    String result = PreferenceUtils.getPreferenceString(mContext, Constants.cacheStr, "");
                    if (!TextUtils.isEmpty(result)) {
                        List<String> items = new Gson().fromJson(result, new TypeToken<List<String>>() {}.getType());
                        if (items != null && items.size() > 0) {
                            for(int i = 0; i < items.size(); i++) {
                                object.addProperty(items.get(i), PreferenceUtils.getPreferenceString(mContext, items.get(i), ""));
                            }
                        }
                    }

                    DataJson.add("userInfo", object);

                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                break;
                case Constants.locate:{
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.getString("city") != null) {
                        DataJson.addProperty("city", bundleData.getString("city"));
                        DataJson.addProperty("latitude", bundleData.getString("latitude"));
                        DataJson.addProperty("longitude", bundleData.getString("longitude"));
                    }
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                break;
                case Constants.share: {
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.get("data") != null) {
                        DataJson.addProperty("msg", "分享成功");
                    }
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                break;
                case Constants.weChatPay:
                    IWXAPI api = WXAPIFactory.createWXAPI(mContext, BuildConfig.THIRDPART_WEIXIN_APPID);

                    Bundle data = msg.getData();
                    PayReq request = new PayReq();

                    if (!TextUtils.isEmpty(data.getString("appid"))) {
                        request.appId = data.getString("appid");
                    } else {
                        request.appId = BuildConfig.THIRDPART_WEIXIN_APPID;
                    }
                    request.partnerId = data.getString("partnerid");
                    request.prepayId = data.getString("prepayid");
                    request.packageValue = data.getString("mPackage");
                    request.nonceStr = data.getString("noncestr");
                    request.timeStamp = data.getString("timestamp");
                    request.sign = data.getString("sign");
                    if (api.sendReq(request)) {
                        WXPayCallBackBean bean = new WXPayCallBackBean();
                        bean.fun = fun;
                        bean.appid = request.appId;
                        bean.partnerid = data.getString("partnerid");
                        bean.prepayid = data.getString("prepayid");
                        setmWXPayCallBackBean(bean);
                    } else {
                        callbackJsFun(fun, getParentJson(0, mContext.getString(R.string.js_wx_pay_sendreq_fail), "-4").toString());
                    }
                    break;
                case Constants.alipay:{
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.getString("data") != null) {
                        DataJson.addProperty("resultStatus", bundleData.getString("data"));
                    }
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                    break;
                case Constants.thirdLogin:{
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.getString("nickname") != null) {
                        DataJson.addProperty("nickname", bundleData.getString("nickname"));
                    }
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                    break;
                case Constants.gyro: {
                    JsonObject DataJson = new JsonObject();
                    DataJson.addProperty("x", bundleData.getDouble("x"));
                    DataJson.addProperty("y", bundleData.getDouble("y"));
                    DataJson.addProperty("z", bundleData.getDouble("z"));
                    ParentJson.addProperty("nonstop", 1);
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                break;
                case Constants.selectImage: {
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.getString("data") != null) {
                        DataJson.addProperty("imgUrl", bundleData.get("data").toString());
                    }
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                    break;
                case Constants.fingerPrint: {
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.getString("data") != null) {
                        DataJson.addProperty("msg", bundleData.getString("data"));
                    }
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                    break;
                case Constants.networkStatus:{
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.getString("data") != null) {
                        DataJson.addProperty("networkStatus", bundleData.getString("data"));
                    }
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                    break;
                case Constants.identify:{
                    JsonObject DataJson = new JsonObject();
                    DataJson.addProperty("equipment", bundleData.getString("equipment"));
                    DataJson.addProperty("uniquelyIdentifies", bundleData.getString("uniquelyIdentifies"));
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                    break;
                case Constants.logout:{
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.getString("data") != null) {
                        DataJson.addProperty("msg", bundleData.getString("data"));
                    }
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                break;
                case Constants.cacheFile:{
                    JsonObject DataJson = new JsonObject();
                    String error = bundleData.getString("download");
                    if ("1".equals(error)) {
                        DataJson.addProperty("msg", "下载成功");
                    } else if ("0".equals(error)) {
                        DataJson.addProperty("msg", "下载失败");
                        DataJson.addProperty("errorCode", "0");
                    }
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                break;
                case Constants.cacheUserAccount:{
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.getString("data") != null) {
                        DataJson.addProperty("msg", bundleData.getString("data"));
                    }
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                break;
                case Constants.deleteUserAccount:{
                    JsonObject DataJson = new JsonObject();
                    DataJson.addProperty("msg", "删除成功");
                    if (bundleData.getString("jsonStr") != null) {
                        String str = bundleData.getString("jsonStr");
                        JsonArray array = new JsonParser().parse(str).getAsJsonArray();
                        DataJson.add("userList", array);
                    }
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                break;
                case Constants.scanCode:{
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.getString("data") != null) {
                        DataJson.addProperty("resCode", bundleData.getString("data"));
                    }
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                break;
                case Constants.playSound:{
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.getInt("success")  == 1) {
                        DataJson.addProperty("msg", "播放成功");
                    } else {
                        DataJson.addProperty("msg", "播放失败");
                        DataJson.addProperty("errorCode", "0");
                    }
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                case Constants.statusBarStyle:{
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.getString("data") != null) {
                        DataJson.addProperty("msg", bundleData.getString("data"));
                    }
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                break;
                case Constants.contactList:{
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.getString("data")  != null) {
                        JsonArray array = new JsonParser().parse(bundleData.getString("data")).getAsJsonArray();
                        DataJson.add("list", array);
                        ParentJson.add("data", DataJson);
                        callbackJsFun(fun, ParentJson.toString());
                    }
                }
                break;
                case Constants.deleteCacheFile:{
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.getString("data") != null) {
                        DataJson.addProperty("msg", "删除成功");
                    }
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                    break;
                default: {
                    //默认处理方式
                    JsonObject DataJson = new JsonObject();
                    ParentJson.add("data", DataJson);
                    callbackJsFun(fun, ParentJson.toString());
                }
                break;
            }
            //统一remove
            removeFunction(cmd);
        }
    };

    //回调调用
    public synchronized void callbackJsFun(String fun, String data) {
        try {
            String strData = data;
            strData = strData.replaceAll("\\\\n", "。");
            strData = strData.replaceAll("\\\\r", "。");
            String weburl = "javascript:znt.fn." + fun + "('" + strData + "')";
            if (js_interface != null) {
                js_interface.callbackJsFun(weburl);
            }
        } catch (Exception ext) {
            ext.printStackTrace();
        }
    }

    public void removeFunction(String key) {
        MyLogUtil.i(TAG + "--removeFunction:" + key);
        functionHash.remove(key);
    }

    /**
     * 封装公共返回数据
     */
    private JsonObject getParentJson(Bundle bundleData) {
        int r = 0;
        String error = "";
        String errorCode = "";

        if (bundleData.get("r") != null) {
            r = bundleData.getInt("r", 0);
        }
        if (bundleData.get("error") != null) {
            error = bundleData.get("error").toString();
        }
        if (bundleData.get("errorCode") != null) {
            errorCode = bundleData.get("errorCode").toString();
        }
        JsonObject ParentJson = new JsonObject();
        ParentJson.addProperty("code", r);

        //错误
        JsonObject ErrorJson = new JsonObject();
        ErrorJson.addProperty("errorCode", errorCode);
        ErrorJson.addProperty("msg", error);
        ParentJson.add("error", ErrorJson);

        return ParentJson;
    }

    public JsonObject getParentJson(int r, String error, String errorCode) {
        JsonObject ParentJson = new JsonObject();
        ParentJson.addProperty("code", r);

        //错误
        JsonObject ErrorJson = new JsonObject();
        ErrorJson.addProperty("errorCode", errorCode);
        ErrorJson.addProperty("msg", error);
        ParentJson.add("error", ErrorJson);

        return ParentJson;
    }

    public void setCallFunJSListener(JsInterface js_interface) {
        this.js_interface = js_interface;
    }

    private boolean requestPermission(String[] permissions, int requestCode) {
        boolean isPermissionAllow = false;
        if (ContextCompat.checkSelfPermission(mContext, permissions[0])!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions((AppCompatActivity)mContext, permissions, requestCode);
        } else {
            isPermissionAllow = true;
        }
        return isPermissionAllow;
    }

    public class WXPayCallBackBean {
        public String fun;
        public String appid;
        public String partnerid; //商户号
        public String prepayid;  //预支付交易会话ID
    }

    public WXPayCallBackBean getmWXPayCallBackBean() {
        return mWXPayCallBackBean;
    }

    public void setmWXPayCallBackBean(WXPayCallBackBean mWXPayCallBackBean) {
        this.mWXPayCallBackBean = mWXPayCallBackBean;
    }

    private void loginThird(final SHARE_MEDIA platform, String cmd) {
        mShareAPI.doOauthVerify((AppCompatActivity)mContext, platform, new UMAuthListener() {

            @Override
            public void onStart(SHARE_MEDIA share_media) {
            }

            @Override
            public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
                if (data != null && data.size() > 0) {
                    MyLogUtil.i("authorize info:" + data.toString());
                    getUserInfo(platform, cmd);
                } else {
                    sendHandler(0, "-1", mContext.getString(R.string.login_fail_warning), cmd, Constants.thirdLogin);
                }
            }

            @Override
            public void onError(SHARE_MEDIA platform, int action, Throwable throwable) {
                if (!((AppCompatActivity)mContext).isFinishing()) {
                    if (throwable != null) {
                        MyLogUtil.e(TAG + throwable.getMessage());
                    }
                    sendHandler(0, "-1", mContext.getString(R.string.login_fail_warning), cmd, Constants.thirdLogin);
                }
            }

            @Override
            public void onCancel(SHARE_MEDIA platform, int action) {
                sendHandler(0, "-1", mContext.getString(R.string.login_cancel), cmd, Constants.thirdLogin);
            }
        });
    }

    private void getUserInfo(final SHARE_MEDIA platform, String cmd) {
        mShareAPI.getPlatformInfo((AppCompatActivity)mContext, platform, new UMAuthListener() {

            @Override
            public void onStart(SHARE_MEDIA share_media) {
            }

            @Override
            public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> info) {
                if (info != null && info.size() > 0) {
                    MyLogUtil.i("user info:" + info.toString());
                    Bundle data = new Bundle();
                    data.putString("openid", info.get("openid"));
                    data.putString("nickname", info.get("name"));
                    if (platform == SHARE_MEDIA.WEIXIN) {
                        data.putString("unionid", info.get("unionid"));
                        data.putString("sex", info.get("gender"));
                        data.putString("headimgurl", info.get("iconurl"));
                    } else if (platform == SHARE_MEDIA.QQ) {
                        data.putString("gender", info.get("gender"));
                        data.putString("figureurl_qq_1", info.get("iconurl"));
                        data.putString("figureurl_qq_2", info.get("iconurl"));
                    }
                    sendHandler(1, "", "", Constants.ThirdLogin, Constants.thirdLogin, data);
                }
            }

            @Override
            public void onError(SHARE_MEDIA platform, int action, Throwable throwable) {
                if (!((AppCompatActivity)mContext).isFinishing()) {
                    if (throwable != null) {
                        MyLogUtil.e(TAG + throwable.getMessage());
                    }
                    sendHandler(0, "-1", mContext.getString(R.string.login_fail_warning), cmd, Constants.thirdLogin);
                }
            }

            @Override
            public void onCancel(SHARE_MEDIA platform, int action) {
                sendHandler(0, "-1", mContext.getString(R.string.login_cancel), cmd, Constants.thirdLogin);
            }
        });
    }

    public void uploadPic(File file) {
        String url = PreferenceUtils.getPreferenceString(mContext, Constants.UPLOADURL, Constants.UPLOADPIC);
        OkHttpUtils
                .post()
                .url(url)
                .addFile("file", file.getName(), file)
                .build()
                .execute(new MyStringCallback());
    }

    public class MyStringCallback extends AbstractJsonCallback {
        @Override
        public void onBefore(Request request, int id) {
            mProgressDialog = MaterialDialogUtil.showProgress(mContext, "上传中...");
        }

        @Override
        public void onAfter(int id) {
//            setTitle("Sample-okHttp");
        }

        @Override
        public void onError(Call call, Exception e, int id) {
            mProgressDialog.dismiss();
            MyLogUtil.d(TAG + "-Exception：" + e.toString());
            e.printStackTrace();
            sendHandler(0, "-1", "上传图片失败", Constants.SelectImage, Constants.selectImage);
        }

        @Override
        public void onResponse(JSONObject response, int id) {//id 100 http, 101 https
            //{"msg":"图片上传成功","path":"/upload/project/dynamic/20180816/1534386533740528478.jpg","success":true}
//            {"code":200,"data":{"msg":"图片上传成功","path":"http:\/\/59.110.169.175:8080\/uploadImgs\/upload\/project\/copyright\/20181128\/1543367848097517181.jpg","success":true},"message":"success"}
            mProgressDialog.dismiss();
            if (response != null && response.optJSONObject("data").optBoolean("success")) {
                Log.e(TAG, "onResponse：" + response);
                String body = response.optJSONObject("data").optString("path");
                sendHandler(1, "", "", Constants.SelectImage, Constants.selectImage, Constants.UPLOADPIC2 + body);
            } else {
                sendHandler(0, "-1", "上传图片失败", Constants.SelectImage, Constants.selectImage);
            }
        }

        @Override
        public void inProgress(float progress, long total, int id) {
//            Log.e(TAG, "inProgress:" + progress);
//            mProgressBar.setProgress((int) (100 * progress));
        }
    }

//    RequestCall call = OkHttpUtils.get().url(url).build();
// call.cancel();

    public void queryContactPhoneNumber() {

        Observable<List<Contact>> observable = Observable.create(new Observable.OnSubscribe<List<Contact>>(){

            @Override
            public void call(Subscriber<? super List<Contact>> subscriber) {
                List<Contact> contacts = new ArrayList<>();
                String[] cols = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
                Cursor cursor = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        cols, null, null, null);
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    // 取得联系人名字
                    int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                    int numberFieldColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String name = cursor.getString(nameFieldColumnIndex);
                    String number = cursor.getString(numberFieldColumnIndex);
                    Contact contact = new Contact();
                    contact.name = name;
                    contact.phone = number;
                    contacts.add(contact);
                }

                subscriber.onNext(contacts);
                subscriber.onCompleted();
            }
        });

        Observer<List<Contact>> observer = new Observer<List<Contact>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<Contact> s) {
                String array = new Gson().toJson(s);
                sendHandler(1, "", "", Constants.ContactList, Constants.contactList, array);
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                List<Contact> contacts = new ArrayList<>();
//                String[] cols = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
//                Cursor cursor = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                        cols, null, null, null);
//                for (int i = 0; i < cursor.getCount(); i++) {
//                    cursor.moveToPosition(i);
//                    // 取得联系人名字
//                    int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
//                    int numberFieldColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
//                    String name = cursor.getString(nameFieldColumnIndex);
//                    String number = cursor.getString(numberFieldColumnIndex);
//                    Contact contact = new Contact();
//                    contact.name = name;
//                    contact.phone = number;
//                    contacts.add(contact);
//                }
//            }
//        }).start();

    }

    private void openMap(boolean b1, boolean b2) {
        View view = View.inflate(mContext, R.layout.layout_openmap, null);
        LinearLayout ll = view.findViewById(R.id.llRoot);
        ImageView iv1 = view.findViewById(R.id.iv1);
        ImageView iv2 = view.findViewById(R.id.iv2);
        BottomSheetDialog bsd = new BottomSheetDialog(mContext);
        bsd.setCancelable(true);
        bsd.setCanceledOnTouchOutside(true);
        bsd.setContentView(view);

        if (!b1) {
            ll.getChildAt(0).setVisibility(View.GONE);
        } else {
            iv1.setImageDrawable(getAppIcon(MapNaviUtils.PN_GAODE_MAP));
        }

        if (!b2) {
            ll.getChildAt(1).setVisibility(View.GONE);
        } else {
            iv2.setImageDrawable(getAppIcon(MapNaviUtils.PN_BAIDU_MAP));
        }

        ll.getChildAt(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.VIEW",
                        android.net.Uri.parse("androidamap://showTraffic?sourceApplication=softname&amp;poiid=BGVIS1&amp;lat=36.2&amp;lon=116.1&amp;level=10&amp;dev=0"));
                intent.setPackage("com.autonavi.minimap");
                mContext.startActivity(intent);
            }
        });

        ll.getChildAt(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setData(Uri.parse("baidumap://map?src=andr.baidu.openAPIdemo"));
                mContext.startActivity(intent);
            }
        });
        bsd.show();
    }

    private Drawable getAppIcon(String packageName){
//        PackageManager packageManager = mContext.getPackageManager();
//        ApplicationInfo application=packageManager.getPackageInfo(packageName, 0).applicationInfo;
//        return application.loadIcon(packageManager);
        PackageManager pm = mContext.getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            Drawable appIcon = pm.getApplicationIcon(appInfo);
            return appIcon;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void setAndroidNativeLightStatusBar(Activity activity, boolean dark) {
        View decor = activity.getWindow().getDecorView();
        if (dark) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    /**
     * 将16进制字符串转换为byte[]
     *
     * @param str
     * @return
     */
    public static byte[] toBytes(String str) {
        if(str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for(int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }
}
