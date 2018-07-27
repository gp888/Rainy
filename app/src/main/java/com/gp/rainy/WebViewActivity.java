package com.gp.rainy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.JsonObject;

import java.io.File;
import java.lang.ref.WeakReference;

public class WebViewActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();
    private static final int FILECHOOSER_RESULTCODE = 1;
    public final static int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 2;
    public ValueCallback<Uri[]> mUploadMessageForAndroid5;
    private Uri mCapturedImageURI;
    private ValueCallback mUploadMessage;
    private WebView webview;
    private String url_load;
    private WebViewManager webViewManager;
    protected Context mContext;
    private MyHandler mHandler;

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
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mContext = this;
        webview = findViewById(R.id.webview);
        mHandler = new MyHandler(mContext);

        webview = setWebViewConfig(webview, mContext);
        webview.setWebChromeClient(new WebChromeClient() {
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

                } else {

                }
                super.onProgressChanged(view, newProgress);
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
        });

        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                super.shouldOverrideUrlLoading(view, request);
                return true;
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

            }

            @Override
            public void onReceivedSslError(WebView arg0, SslErrorHandler arg1, SslError arg2) {
                arg1.proceed();
            }

        });

        webViewManager.setCallFunJSListener(new JsInterface() {

            @Override
            public void callbackJsFun(String fun) {
                try {
                    MyLogUtil.i(TAG + "---callbackJsFun:" + fun);
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
            public void callHttpPost(JsonObject attrsObj) {
//            MyLogUtil.i(initTag() + "-----callHttpPost---");
//            if (isLogin()) {
//                httpPost(REQUEST_CODE_1, null, IDataManager.getIRequest(ConstantNetwork.REQUEST_CODE_SHARE_ARTICLE, 2, attrsObj, null));
//            }
            }

            @Override
            public void callStartActivityForResult(android.content.Intent intent) {
//            startActivityForResult(intent, Constants.REQUEST_GET_INFO);
            }

            @Override
            public void callFinish() {
                finish();
            }

        });

        url_load = "file:///android_asset/jssdk/demo.html";
        webview.loadUrl(url_load);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
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
        webViewManager = new WebViewManager(mContext);
        webview.addJavascriptInterface(webViewManager, "oatongJSBridge");
        return webview;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
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
         }
    }
}
