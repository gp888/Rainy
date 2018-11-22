package com.gp.rainy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.JsonObject;
import com.gp.rainy.alipay.PayResult;
import com.gp.rainy.fingerprint.AppUtils;
import com.gp.rainy.fingerprint.FingerPrintException;
import com.gp.rainy.fingerprint.FingerprintManagerUtil;
import com.gp.rainy.share.ShareInterface;
import com.gp.rainy.share.ShareManager;
import com.gp.rainy.share.ShareMenuDialog;
import com.gp.rainy.share.SharePublicAccountModel;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class WebViewActivity extends AppCompatActivity implements SensorEventListener {

    private static String TAG = WebViewActivity.class.getSimpleName();
    private static final int FILECHOOSER_RESULTCODE = 1;
    public final static int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 2;
    public ValueCallback<Uri[]> mUploadMessageForAndroid5;
    private Uri mCapturedImageURI;
    private ValueCallback mUploadMessage;
    private WebView webview;
    private TextView lg;
    private String url_load;
    private WebViewManager webViewManager;
    protected Context mContext;
    private MyHandler mHandler;

    private String mType = "login";
    public final static String LOGIN = "login";
    public final static String SETTING = "setting";
    public final static String LOGIN_SETTING = "login_setting";
    public final static String CLEAR = "clear";
    public static boolean isShow;
    private boolean mIsSupportFingerprint;
    private boolean isInAuth = false;

    private FingerprintManagerUtil mFingerprintManagerUtil;
    private FingerPrintTypeController mFingerPrintTypeController;
    private ArrayList<String> methodOrderArrayList;
    private String mBeginAuthenticateMethodName;
    private Map<String, String> exceptionTipsMappingMap;
    private Map<String, String> mi5TipsMappingMap;
    private AlertDialog fingerDialog;
    private ShareMenuDialog jsShareDialog;
    private ShareManager shareManager;
    private UMShareAPI mShareAPI;
    private boolean isGyro;
    private double time = 0.1;
    private SensorManager sensorManager;
    private String firstLoadUrl = "";
    BottomSheetDialog bsd;
    ListView recyclerView;
    List<String> logArray;
    ArrayAdapter<String> adapter;

    private static class MyHandler extends Handler {

        private WeakReference reference;

        public MyHandler(Context context) {
            reference = new WeakReference<>(context);
        }
        @Override
        public void handleMessage(Message msg) {
            WebViewActivity activity = (WebViewActivity) reference.get();
            if(activity != null){
                switch (msg.what) {
                    case Constants.OPENGJSALERT:
                        MaterialDialogUtil.showConfirm(activity, (String) msg.obj, R.string.confirm);
                        break;
                    case Constants.FINGERSUCCESS:
                        activity.stopAnim(true);
                        activity.webViewManager.sendHandler(1, "", "", Constants.FingerPrint, Constants.fingerPrint, "验证通过");
                        break;
                    case Constants.FINGERFAIL:
                        activity.stopAnim(true);
                        activity.webViewManager.sendHandler(0, "-1", "指纹验证失败", Constants.FingerPrint, Constants.fingerPrint);
                        break;
                    case Constants.JSShARENET:
                        SharePublicAccountModel accountModel = (SharePublicAccountModel) msg.obj;
                        accountModel.setShareType(3);//link
                        // 追加APPID
                        if (!TextUtils.isEmpty(accountModel.geturl())) {
                            String url = accountModel.geturl();
                            if (!url.contains("?")) {
                                url = url + "?app=" + BuildConfig.APPID;
                            } else {
                                url = url + "&app=" + BuildConfig.APPID;
                            }
                            accountModel.seturl(url);
                        }
                        activity.showJsShareDialog(accountModel);
                        break;
                    case Constants.alipay:
                        PayResult payResult = new PayResult((Map<String, String>) msg.obj);

                        String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                        String resultStatus = payResult.getResultStatus();
                        // 判断resultStatus 为9000则代表支付成功
                        if (TextUtils.equals(resultStatus, "9000")) {
                            // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                            Toast.makeText(activity, "支付成功", Toast.LENGTH_SHORT).show();
                           activity.webViewManager.sendHandler(1, "", "", Constants.Alipay, Constants.alipay, "success");
                        } else {
                            Toast.makeText(activity, "支付失败", Toast.LENGTH_SHORT).show();
                            activity.webViewManager.sendHandler(0, "-1", "支付失败", Constants.Alipay, Constants.alipay);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFormat(PixelFormat.RGBA_8888);
        setContentView(R.layout.activity_webview);
        mContext = this;
        webview = findViewById(R.id.webview);
        lg = findViewById(R.id.lg);
        mHandler = new MyHandler(mContext);
        mShareAPI = UMShareAPI.get(this);

        webview = setWebViewConfig(webview, mContext);

        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                MyLogUtil.i("---onPageStarted--");
            }

            @SuppressLint("NewApi")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                MyLogUtil.i("---shouldOverrideUrlLoading--" + url);
                if (url.toLowerCase().startsWith("http") || url.toLowerCase().startsWith("https")) {
                    //兼容8.0以上 点击a标签两次跳转不一致
                    if (TextUtils.isEmpty(firstLoadUrl)) {
                        firstLoadUrl = url;
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && firstLoadUrl.equals(url)) {
                            MyLogUtil.e("  do not load again  ");
                            return false;
                        }
                    }
                    url_load = url;
//                    WebViewUtil.webviewSyncCookie(url_load, webview);
                    view.loadUrl(url_load);
                    return true;
                }
                return false;
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                MyLogUtil.i("---onPageFinished--url:" + url);
                synchronized (this) {
                    if (webview != null) {
                        webview.getSettings().setBlockNetworkImage(false);
                        String title = view.getTitle();
                        if (!TextUtils.isEmpty(title)) {

                        }
                        url_load = url;
                    }
                }
                firstLoadUrl = "";
            }

            @SuppressLint("NewApi")
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                MyLogUtil.d("---onReceivedError--" + error);
                if (request.isForMainFrame()) {
                    //无法与服务器正常连接,断网或者网络连接超时
                    MyLogUtil.d("---error--" + error);
                }
            }

            @Override
            public void onReceivedSslError(WebView arg0, SslErrorHandler arg1, SslError arg2) {
                arg1.proceed();         //忽略证书
                MyLogUtil.e("--onReceivedSslError:" + arg2);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                MyLogUtil.i("--onLoadResource--" + url);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                MyLogUtil.e("---onReceivedHttpError--" + errorResponse);
            }

        });

        webview.setWebChromeClient(new ChromClient());

        webview.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                try {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showToastLong("无法打开");
                }
            }

        });

        webViewManager.setCallFunJSListener(new JsInterface() {

            @Override
            public void callbackJsFun(String fun) {
                try {
                    MyLogUtil.i(TAG + "---callbackJsFun:" + fun);
                    logArray.add("返回：" + fun);
                    adapter.notifyDataSetChanged();
                    if (webview != null &&!TextUtils.isEmpty(fun)) {
                        final String sweburl = fun;
                        webview.post(new Runnable() {

                            @Override
                            public void run() {
                                synchronized (this) {
                                    if (null != webview && sweburl != null) {
                                        webview.loadUrl(sweburl);
                                    }
                                }
                            }
                        });
                    }
                } catch (Exception ext) {
                    ext.printStackTrace();
                }
            }

            @Override
            public void callHandler(int what, String value) {

            }

            @Override
            public void callHandler(int what, String value, String m) {
                switch (what) {

                    default:
                        break;
                }
            }

            @Override
            public void callHandler(int what, Intent intent) {
//            switch (what) {
//                case HandlerComm.JSCLOSEWINCONFIRM:
//                    //传递的退出框文案
//                    String exitDialogTips = intent.getStringExtra("tips");
//                    String exitDialogPositiveBtn = intent.getStringExtra("positiveBtn");
//                    String exitDialogNegativeBtn = intent.getStringExtra("negativeBtn");
//                    exitMap.put(url_load, new ExitBean(exitDialogTips, exitDialogPositiveBtn, exitDialogNegativeBtn));
//                    break;
//            }
            }

            @Override
            public void callHandler(int what, Object object) {
                switch (what) {
//                case HandlerComm.JSQINIUUPLOADVIDEO:
//                    videmodel = (VideoModel) object;
//                    js_manager.openMedia();
//                    break;
                }
            }

            @Override
            public void callHandler(int what) {
                switch (what) {
//                case HandlerComm.JSUPLOADVIDEO: //拍摄视频
//                    handler.sendEmptyMessage(202);
//                    break;
                }
            }

            @Override
            public void callJsShowDialog() {
//            if (dialog == null) {
//                dialog = MaterialDialogUtil.showProgress(mContext, "正在上传...");
//            } else {
//                dialog.show();
//            }
            }

            @Override
            public void callJsDismissDialog() {
//            if (dialog != null) {
//                dialog.dismiss();
//            }
            }

            @Override
            public void callAlipayHandler(Map<String, String> result) {
                mHandler.obtainMessage(Constants.alipay, result).sendToTarget();
            }

            @Override
            public void callStartActivityForResult(android.content.Intent intent) {
//            startActivityForResult(intent, Constants.REQUEST_GET_INFO);
            }

            @Override
            public void callFinish() {
                finish();
            }

            @Override
            public void callShareModelHandler(SharePublicAccountModel model) {
                mHandler.obtainMessage(Constants.JSShARENET, model).sendToTarget();
            }

        });

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        PreferenceUtils.setPreferenceInt(this,Constants.SHARE_SCREEN_WIDTH, dm.widthPixels);
        PreferenceUtils.setPreferenceInt(this, Constants.SHARE_SCREEN_HEIGHT, dm.heightPixels);

        shareManager = new ShareManager(mContext);
        shareManager.setShareListener(new ShareInterface() {

            @Override
            public void sendShareHandler(int r, String errorCode, String error, String funName, int what) {
                webViewManager.sendHandler(r, errorCode, error, funName, what);
            }

            @Override
            public void sendShareHandler(int r, String errorCode, String error, String funName, int what, String data) {
                webViewManager.sendHandler(r, errorCode, error, funName, what, data);
            }

            @Override
            public void callShareHttpPost(JsonObject attrsObj) {
            }

        });
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if(Build.VERSION.SDK_INT>=23){
            String[] mPermissionList = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_LOGS,Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.SET_DEBUG_APP,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.WRITE_APN_SETTINGS, Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this,mPermissionList,123);
        }

        if (BuildConfig.BUILD_TYPE.equals("release")) {
            url_load = Constants.mainUrl1;
        } else {
            url_load = Constants.testUrl;
        }
        webview.loadUrl(url_load);
        lg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = logArray.size();
                recyclerView.setSelection(logArray.size());
                bsd.show();
                adapter.notifyDataSetChanged();
            }
        });

        View view = View.inflate(this, R.layout.layout_log, null);
        recyclerView = view.findViewById(R.id.rvLog);
        logArray = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, android.R.id.text1, logArray);
        recyclerView.setAdapter(adapter);
        bsd = new BottomSheetDialog(this);
        bsd.setCancelable(true);
        bsd.setCanceledOnTouchOutside(true);
        bsd.setContentView(view);
    }

    public void initFingerPrint() {
        mFingerprintManagerUtil = new FingerprintManagerUtil(this, () -> beginAuthAnim(), new MyAuthCallbackListener());
        mIsSupportFingerprint = mFingerprintManagerUtil.isSupportFingerprint();
        mFingerPrintTypeController = new FingerPrintTypeController();

        methodOrderArrayList = new ArrayList<>();

        //普通异常情况提示
        exceptionTipsMappingMap = new HashMap<>();
        exceptionTipsMappingMap.put(SETTING, getString(R.string.fingerprint_no_support_fingerprint_gesture));
        exceptionTipsMappingMap.put(LOGIN_SETTING, getString(R.string.fingerprint_no_support_fingerprint_gesture));
        exceptionTipsMappingMap.put(CLEAR, null);
        exceptionTipsMappingMap.put(LOGIN, getString(R.string.fingerprint_no_support_fingerprint_account));

        //小米5乱回调生命周期的异常情况提示
        mi5TipsMappingMap = new HashMap<>();
        mi5TipsMappingMap.put(SETTING, getString(R.string.tips_mi5_setting_open_close_error));
        mi5TipsMappingMap.put(LOGIN_SETTING, getString(R.string.tips_mi5_login_setting_error));
        mi5TipsMappingMap.put(CLEAR, getString(R.string.tips_mi5_setting_open_close_error));
        mi5TipsMappingMap.put(LOGIN, getString(R.string.tips_mi5_login_auth_error));

        initSetting();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isShow = true;
        if (mFingerprintManagerUtil != null) {
            mIsSupportFingerprint = mFingerprintManagerUtil.isSupportFingerprint();
        }
        //回来的时候自动调起验证
        if (isInAuth) {
            initSetting();
        }
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopFingerprintListen();
        isInAuth = mFingerprintManagerUtil != null && mFingerprintManagerUtil.getIsInAuth();
        if (methodOrderArrayList != null) {
            methodOrderArrayList.add(AppUtils.getMethodName());
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isShow = false;
        if (methodOrderArrayList != null) {
            methodOrderArrayList.clear();
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    private void stopFingerprintListen() {
        if (mFingerprintManagerUtil != null) {
            mFingerprintManagerUtil.stopsFingerprintListen();
        }
    }

    public WebView setWebViewConfig(final WebView webview, Context mContext) {
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setBuiltInZoomControls(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webview.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        if (Integer.parseInt(Build.VERSION.SDK) >= 14) {
            webview.getSettings().setDisplayZoomControls(false);
        }
        webview.getSettings().setUserAgentString(DeviceUtil.getAppVersion() + "/" + webview.getSettings().getUserAgentString());
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webview.getSettings().setBlockNetworkImage(true);
        //开启存储
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        //设置缓冲路径
        String appCachePath = mContext.getCacheDir().getAbsolutePath();
        webview.getSettings().setAppCachePath(appCachePath);
        //开启文件数据缓存
        webview.getSettings().setAllowFileAccess(false);
        //开启APP缓存
        webview.getSettings().setAppCacheEnabled(true);

        //根据网络状态加载缓冲，有网：走默认设置；无网络：走加载缓冲
        if (MainActivity.isConnected(mContext)) {
            webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        //支持显示PC宽屏页面的全部内容
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        MyLogUtil.i("userAgent------" + webview.getSettings().getUserAgentString());
        webViewManager = new WebViewManager(mContext, mShareAPI);
        webview.addJavascriptInterface(webViewManager, "oatongJSBridge");
        return webview;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, intent);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, intent);
                return;
            }
            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == mUploadMessage) {
                    return;
                }
                Uri result = null;
                try {
                    if (resultCode != RESULT_OK) {
                        result = null;
                    } else {
                        //retrieve from the private variable if the intent is null
                        result = intent == null ? mCapturedImageURI : intent.getData();
                    }
                } catch (Exception e) {
                    MyLogUtil.e(TAG + e);
                }
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5) {
             if (null == mUploadMessageForAndroid5) {
                 return;
             }
             Uri result = (intent == null || resultCode != RESULT_OK) ? null : intent.getData();
             if (result != null) {
                 mUploadMessageForAndroid5.onReceiveValue(new Uri[]{result});
             } else {
                 mUploadMessageForAndroid5.onReceiveValue(new Uri[]{});
             }
             mUploadMessageForAndroid5 = null;
        }else if (requestCode == FILECHOOSER_RESULTCODE) {
             if (mUploadMessage == null) {
                 return;
             }
             Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
             mUploadMessage.onReceiveValue(result);
             mUploadMessage = null;
        } else if (requestCode == Constants.CHOICE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null) {
                    Uri originalUri = intent.getData();
                    if (originalUri != null) {
                        String srcPath = DeviceUtil.getImageAbsolutePath(mContext, originalUri);
                        MyLogUtil.d(TAG + "图片：" + srcPath);
                        webViewManager.uploadPic(new File(srcPath));
                    } else {
                        webViewManager.sendHandler(0, "-1", "没有选取图片", Constants.SelectImage, Constants.selectImage);
                    }
                } else {
                    webViewManager.sendHandler(0, "-1", "没有选取图片", Constants.SelectImage, Constants.selectImage);
                }
            } else {
                webViewManager.sendHandler(0, "-1", "没有选取图片", Constants.SelectImage, Constants.selectImage);
            }
        }else if (requestCode == Constants.CHOICE_CMARE) { //相机
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String filepath = Constants.PHOTOFILEPATH;
                    File temp = new File(filepath);
                    Uri tempUri = Uri.fromFile(temp);
                    if (tempUri != null) {
                        String srcPath = DeviceUtil.getImageAbsolutePath(mContext, tempUri);
                        MyLogUtil.d(TAG + "图片：" + srcPath);
                        webViewManager.uploadPic(new File(srcPath));
                    } else {
                        webViewManager.sendHandler(0, "-1", "没有选取图片", Constants.SelectImage, Constants.selectImage);
                    }
                } catch (Exception e) {
                    webViewManager.sendHandler(0, "-1", "没有选取图片", Constants.SelectImage, Constants.selectImage);
                    e.printStackTrace();
                }
            } else {
                webViewManager.sendHandler(0, "-1", "没有选取图片", Constants.SelectImage, Constants.selectImage);
            }
        }else if (requestCode == Constants.REQUEST_CODE) {
            if (null != intent) {
                Bundle bundle = intent.getExtras();
                if (bundle == null) {
                    webViewManager.sendHandler(0, "-1", "解析二维码失败", Constants.ScanCode, Constants.scanCode);
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    webViewManager.sendHandler(1, "", "", Constants.ScanCode, Constants.scanCode, result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    webViewManager.sendHandler(0, "-1", "解析二维码失败", Constants.ScanCode, Constants.scanCode);
                }
            } else {
                webViewManager.sendHandler(0, "-1", "解析二维码失败", Constants.ScanCode, Constants.scanCode);
            }
        }
    }

    private void initSetting() {
        if (!mIsSupportFingerprint) {
            jumpToGesture(mType);
            return;
        }
        beginAuthenticate();
    }


    private void initVerify(String errorContent) {
        if (!mIsSupportFingerprint) {
            logoutAndClearFingerPrint();
            return;
        }
        beginAuthenticate();
    }

    private void beginAuthenticate() {
        mBeginAuthenticateMethodName = AppUtils.getMethodName();
        methodOrderArrayList.add(mBeginAuthenticateMethodName);
        try {
            mFingerprintManagerUtil.beginAuthenticate();
        } catch (FingerPrintException e) {
            onAuthExceptionOrBeIntercept();
        }
    }

    private void beginAuthAnim() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(WebViewActivity.this, R.layout.dialog_fingerprint, null);
        builder.setView(view);
        fingerDialog = builder.create();
        fingerDialog.setCancelable(false);
        fingerDialog.setCanceledOnTouchOutside(false);
        fingerDialog.show();
    }

    /**
     * 回调
     */
    public class MyAuthCallbackListener implements FingerprintManagerUtil.AuthenticationCallbackListener {

        @Override
        public void onAuthenticationSucceeded(boolean isAuthSuccess) {
            methodOrderArrayList.add(AppUtils.getMethodName());
            if (isAuthSuccess) {
                mFingerPrintTypeController.onAuthenticationSucceeded();
            } else {
                onAuthExceptionOrBeIntercept();
            }
        }

        @Override
        public void onAuthenticationError(int errMsgId, String errString) {
            //验证过程中遇到不可恢复的错误
            switch (errMsgId) {
                case FingerprintManagerUtil.MyAuthCallback.ERROR_BEYOND:
                    mFingerPrintTypeController.onAuthenticationError(null);
                    break;
                case FingerprintManagerUtil.MyAuthCallback.ERROR_CANCEL:
                    compatibilityDispose();
                    methodOrderArrayList.clear();
                    break;
                default:
                    break;
            }
        }

        /**
         * 针对小米5的兼容，小米5在验证过程中切到后台再回来时，开启验证会直接回调onAuthenticationError，无法继续验证
         * 所以存储函数调用顺序，判断是否一开启验证马上就回调onAuthenticationError
         */
        private void compatibilityDispose() {
            int size = methodOrderArrayList.size();
            if (size <= 0) {
                return;
            }
            if ("MI 5".equals(DeviceUtil.getMODEL()) && mBeginAuthenticateMethodName.equals(methodOrderArrayList.get(size - 1))) {
                mFingerPrintTypeController.onAuthenticationError(mi5TipsMappingMap.get(mType));
            }
        }

        @Override
        public void onAuthenticationFailed() {
            methodOrderArrayList.add(AppUtils.getMethodName());
            onAuthFail(getString(R.string.fingerprint_auth_fail));
        }

        @Override
        public void onAuthenticationHelp(String helpString) {
            //验证过程中遇到可恢复错误
            methodOrderArrayList.add(AppUtils.getMethodName());
            onAuthFail(helpString);
        }
    }

    /**
     * 验证过程异常 或 验证结果被恶意劫持
     * 该失败场景都会清掉指纹再次登陆引导设置，所以如果是关闭场景按成功来处理
     */
    private void onAuthExceptionOrBeIntercept() {
        if (CLEAR.equals(mType)) {
            mFingerPrintTypeController.onAuthenticationSucceeded();
        } else {
            mFingerPrintTypeController.onAuthenticationError(exceptionTipsMappingMap.get(mType));
            clearFingerPrintSign();
        }
    }

    private void onAuthFail(String text) {
//        ToastUtil.showToastShort(text);
        mHandler.obtainMessage(Constants.FINGERFAIL).sendToTarget();
    }

    private void onAuthSuccess(String text) {
//        ToastUtil.showToastShort(text);
        mHandler.obtainMessage(Constants.FINGERSUCCESS).sendToTarget();
    }

    private void stopAnim(boolean isSuccess){
        if (fingerDialog != null) {
//            TextView status = fingerDialog.findViewById(R.id.fingerprint_status);
            if (isSuccess) {
//                status.setText(R.string.fingerprint_auth_success);
//                status.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                fingerDialog.dismiss();
                fingerDialog = null;
            } else {
                DeviceUtil.vibrateShort(mContext, 300L);
            }
        }
    }


    private void jumpToGesture(String type) {
    }


    private void logoutAndClearFingerPrint() {
    }

    private void clearFingerPrintSign() {
    }

    private interface FingerPrintType {
        void onAuthenticationSucceeded();

        void onAuthenticationError(String content);
    }

    private class LoginAuthType implements FingerPrintType {
        @Override
        public void onAuthenticationSucceeded() {
            onAuthSuccess(getString(R.string.fingerprint_auth_success));
        }

        @Override
        public void onAuthenticationError(String content) {
        }
    }

    private class ClearType implements FingerPrintType {
        @Override
        public void onAuthenticationSucceeded() {
            onAuthSuccess(getString(R.string.fingerprint_close_success));
        }

        @Override
        public void onAuthenticationError(String content) {
        }
    }

    private class LoginSettingType implements FingerPrintType {
        @Override
        public void onAuthenticationSucceeded() {
            onAuthSuccess(getString(R.string.fingerprint_set_success));
        }

        @Override
        public void onAuthenticationError(String content) {
        }
    }

    private class SettingType implements FingerPrintType {
        @Override
        public void onAuthenticationSucceeded() {
            onAuthSuccess(getString(R.string.fingerprint_set_success));
        }

        @Override
        public void onAuthenticationError(String content) {
        }
    }

    private class FingerPrintTypeController implements FingerPrintType {
        private Map<String, FingerPrintType> typeMappingMap = new HashMap<>();

        public FingerPrintTypeController() {
            typeMappingMap.put(SETTING, new SettingType());
            typeMappingMap.put(LOGIN_SETTING, new LoginSettingType());
            typeMappingMap.put(CLEAR, new ClearType());
            typeMappingMap.put(LOGIN, new LoginAuthType());
        }

        @Override
        public void onAuthenticationSucceeded() {
            FingerPrintType fingerPrintType = typeMappingMap.get(mType);
            if (null != fingerPrintType) {
                fingerPrintType.onAuthenticationSucceeded();
            }
        }

        @Override
        public void onAuthenticationError(String content) {
            FingerPrintType fingerPrintType = typeMappingMap.get(mType);
            if (null != fingerPrintType) {
                fingerPrintType.onAuthenticationError(content);
            }
        }
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
            case Constants.REQUEST_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(mContext, CaptureActivity.class);
                    ((WebViewActivity) mContext).startActivityForResult(intent, Constants.REQUEST_CODE);
                }else {
                    Toast.makeText(this, "需要开启相机权限", Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.REQUEST_CONTACT:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    webViewManager.queryContactPhoneNumber();
                }else {
                    Toast.makeText(this, "需要开启电话联系人权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        int sensorType = event.sensor.getType();
        if (sensorType == Sensor.TYPE_ACCELEROMETER && isGyro) {//
            MyLogUtil.d(TAG + "Sensor,speed:" + ":" + x + "," + y + "," + z);
            Bundle data = new Bundle();
            data.putDouble("x", x);
            data.putDouble("y", y);
            data.putDouble("z", z);
            long delayMillis = (long) (time * 1000);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToastShort(x + "");
                    webViewManager.sendHandler(1, "", "", Constants.Gyro, Constants.gyro, data);
                }
            }, delayMillis);
        }
    }

    public void setGyro (boolean isGyro, Double time) {
        this.isGyro = isGyro;
        this.time = time;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //share
    private void showJsShareDialog(final SharePublicAccountModel shareModel) {
        if (isFinishing()) {
            return;
        }

        jsShareDialog = new ShareMenuDialog(mContext, Constants.TO_WEBVIEW_FROM_JS, "",
                mShareAPI.isInstall(this, SHARE_MEDIA.SINA), true, new ShareMenuDialog.OnButtonClickListener() {

                    @Override
                    public void onButtonClick(int type, int id) {
                        if (jsShareDialog != null && jsShareDialog.isShowing()) {
                            jsShareDialog.setOnDismissListener(null);
                            jsShareDialog.dismiss();
                        }
                        if (type == 1) {
                            shareManager.performShare(shareModel, id, true);
                        }
                    }
                });
        jsShareDialog.show();

        jsShareDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                //传给网页取消分享的状态
                ShareInterface shareInterface = shareManager.getShareInterface();
                if (shareInterface != null) {
                    shareInterface.sendShareHandler(0, "-1", "取消分享", Constants.Share, Constants.share);
                }
            }
        });
    }

    /**
     * wechatpay
     */
    public void onEventMainThread(WebviewEvent event) {
        if (event.type == WebviewEvent.TYPE_WX_PAY_RESULT) {
            WebViewManager.WXPayCallBackBean bean = webViewManager.getmWXPayCallBackBean();
            if (bean != null) {
                switch (event.result) {
                    case 0: {
                        JsonObject ParentJson = webViewManager.getParentJson(1, "", "");
                        JsonObject DataJson = new JsonObject();
                        DataJson.addProperty("partnerid", bean.partnerid);
                        DataJson.addProperty("prepayid", bean.prepayid);
                        DataJson.addProperty("msg", "支付成功");
                        ParentJson.add("data", DataJson);
                        webViewManager.callbackJsFun(bean.fun, ParentJson.toString());
                    }
                    break;
                    case -2: {
                        JsonObject ParentJson = webViewManager.getParentJson(0, "用户取消", "-2");
                        webViewManager.callbackJsFun(bean.fun, ParentJson.toString());
                    }
                    break;
                    default: {
                        //其他错误统一提示
                        JsonObject ParentJson = webViewManager.getParentJson(0, event.errStr, "-1");
                        webViewManager.callbackJsFun(bean.fun, ParentJson.toString());
                    }
                    break;
                }
                webViewManager.setmWXPayCallBackBean(null);
            }
        } else if (event.type == WebviewEvent.TYPE_DOWN) {
            Bundle data = new Bundle();
            if ("1".equals(event.errStr)) {
                data.putString("download", "1");
                webViewManager.sendHandler(1, "", "", Constants.CacheFile, Constants.cacheFile, data);
            } else {
                data.putString("download", "0");
                webViewManager.sendHandler(1, "", "", Constants.CacheFile, Constants.cacheFile, data);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack();
        } else {
            finish();
        }
    }

    public void patchSource (String fileUrl, String path) {
        FileUtils.download(fileUrl, path);
    }

    class ChromClient extends WebChromeClient {

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            if (!isFinishing()) {
                mHandler.obtainMessage(Constants.OPENGJSALERT, message).sendToTarget();
                result.confirm();
            }
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView arg0, String arg1, String arg2, final JsResult arg3) {
            if (!isFinishing()) {
                MaterialDialog dialog = MaterialDialogUtil.showAlert(mContext, arg2, R.string.confirm, R.string.cancel,
                        new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                arg3.confirm();
                            }
                        }, new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                arg3.cancel();
                            }
                        });
                dialog.setCancelable(false);
            }
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            MyLogUtil.i(TAG + "---onProgressChanged--newProgress:" + newProgress);
            if (newProgress == 100) {
//                    progress_bar.setVisibility(View.GONE);
            } else {
//                    progress_bar.setVisibility(View.VISIBLE);
//                    progress_bar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }

        /*android 低版本 Desperate*/
//        @Override
//        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
//            Log.i("WebViewLog", String.format("sourceID: %s lineNumber: %n message: %s", sourceID,
//                    lineNumber, message));
//            super.onConsoleMessage(message, lineNumber, sourceID);
//        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            String message = consoleMessage.message();
            int lineNumber = consoleMessage.lineNumber();
            String sourceID = consoleMessage.sourceId();
            String messageLevel = consoleMessage.messageLevel().toString();

            Log.i("WebViewLog", String.format("[%s] sourceID: %s lineNumber: %n message: %s",
                    messageLevel, sourceID, lineNumber, message));
            logArray.add(String.format("[%s] sourceID: %s lineNumber: %n message: %s", messageLevel, sourceID, lineNumber, message));
            adapter.notifyDataSetChanged();
            return super.onConsoleMessage(consoleMessage);
        }


        @Override
        public View getVideoLoadingProgressView() {
            FrameLayout frameLayout = new FrameLayout(mContext);
            frameLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            return frameLayout;
        }

        //Android >= 5.0
        @Override
        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
            openFileChooserImplForAndroid5(filePath);
            return true;
        }

        //openFileChooser Android >= 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            //Create AndroidExampleFolder at sdcard
            File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AndroidExampleFolder");
            if (!imageStorageDir.exists()) {
                imageStorageDir.mkdirs();
            }
            //Create camera captured image file path and name
            File file = new File(imageStorageDir + File.separator + "_" + String.valueOf(System.currentTimeMillis()) + "." + FileUtils.getExtensionName(imageStorageDir.getName()));
            mCapturedImageURI = Uri.fromFile(file);
            //Camera capture image intent
            final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("*/*");
            i.addCategory(Intent.CATEGORY_OPENABLE);
            Intent chooserIntent = Intent.createChooser(i, getString(R.string.webview_file_browser_title));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});
            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
        }

        //Android versions < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

        //Android versions >= 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            openFileChooser(uploadMsg, acceptType);
        }

        /**
         *  >= Android5.0
         **/
        private void openFileChooserImplForAndroid5(ValueCallback<Uri[]> uploadMsg) {
            mUploadMessageForAndroid5 = uploadMsg;
            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("*/*");

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.webview_file_browser_title));

            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
        }
    }

    public void printLog(String log) {
        logArray.add(log);
        adapter.notifyDataSetChanged();
    }
}
