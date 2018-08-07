package com.gp.rainy.share;

import com.google.gson.JsonObject;


public interface ShareInterface {
    void sendShareHandler(int r, String errorCode, String error, String funName, int what);
    void sendShareHandler(int r, String errorCode, String error, String funName, int what, String data);
    void callShareHttpPost(JsonObject attrsObj);
}
