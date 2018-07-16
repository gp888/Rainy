package com.gp.rainy;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebViewManager {
    private Context mContext;

    public WebViewManager(Context context){
        this.mContext = context;
    }

    @JavascriptInterface
    public String getMessage(){
        return "这句话来自安卓";
    }

    @JavascriptInterface
    public void showToast(String s){
        Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
    }
}
