package com.gp.rainy;

import android.content.Intent;
import com.gp.rainy.share.SharePublicAccountModel;

import java.util.Map;

public interface JsInterface {
    void callbackJsFun(String fun);

    void callHandler(int what, String value);

    void callHandler(int what, String value, String m);

    void callHandler(int what, Intent intent);

    void callHandler(int what, Object object);

    void callHandler(int what);

    void callJsShowDialog();

    void callJsDismissDialog();

    void callAlipayHandler(Map<String, String> result);

    void callStartActivityForResult(Intent intent);

    void callFinish();

    void callShareModelHandler(SharePublicAccountModel model);

}