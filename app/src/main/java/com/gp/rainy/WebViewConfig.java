package com.gp.rainy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.gp.rainy.utils.DeviceUtil;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewConfig {

    private static WebViewConfig instract;

    public static WebViewConfig getInstract() {
        instract = new WebViewConfig();
        return instract;
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
        return webview;
    }

//    public WebView setCookie_JSInterface_Config(final WebView webview, final String url_load, final TencentWebViewJSManager js_manager) {
//        if (js_manager != null) {
//            webview.addJavascriptInterface(js_manager, "oatongJSBridge");
//        }
//        //判断URL地址是否为公司内部域名
//        if (!TextUtils.isEmpty(url_load) && RegexURLUtil.isCompanyURl(url_load)) {
//            WebViewUtil.webviewSyncCookie(url_load,webview);
//
//            webview.removeJavascriptInterface("searchBoxJavaBridge_");
//            webview.removeJavascriptInterface("accessibility");
//            webview.removeJavascriptInterface("accessibilityTraversal");
//        }
//        MyLogUtil.i("Cookie设置:" + CookieManager.getInstance().getCookie(url_load));
////        String s = RegexURLUtil.WEB_COOKIE_DOMAIN[2].replaceFirst(".", "");
////        String http = "https://open.t.nxin.com/api/nxin.usercenter.user.get/1.0";
////        MyLogUtil.i("Cookie设置--key=" + http + ":" + CookieManager.getInstance().getCookie(http));
//        return webview;
//    }
}
