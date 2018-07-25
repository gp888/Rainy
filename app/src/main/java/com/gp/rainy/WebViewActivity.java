package com.gp.rainy;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.gson.JsonObject;

public class WebViewActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();
    private WebView webview;
    private String url_load;
    private WebViewManager webViewManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        webview = findViewById(R.id.webview);

        webview = setWebViewConfig(webview, this);
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                if (!isFinishing()) {
                    Toast.makeText(WebViewActivity.this, message, Toast.LENGTH_SHORT).show();
                    result.confirm();
                }
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView arg0, String arg1, String arg2, final JsResult arg3) {
                if (!isFinishing()) {

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
        });

        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

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
        if (MainActivity.isConnected(this)) {
            webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        //支持显示PC宽屏页面的全部内容
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        MyLogUtil.i("userAgent------" + webview.getSettings().getUserAgentString());
        webViewManager = new WebViewManager(this);
        webview.addJavascriptInterface(webViewManager, "oatongJSBridge");
        return webview;
    }
}
