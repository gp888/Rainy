package com.gp.rainy;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import com.google.gson.JsonObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class WebViewManager {

    private static String TAG = MainActivity.class.getSimpleName();
    private Context mContext;
    public HashMap<String, String> functionHash = new HashMap<>();
    private JsInterface js_interface;

    public WebViewManager(Context context) {
        this.mContext = context;
    }

    @JavascriptInterface
    public void invoke(String request) {
        String m;
        String cmd = "";
        try {
            MyLogUtil.i(TAG + "--invoke-request:" + request);
            JSONObject jsonObjParent = new JSONObject(request);
            m = jsonObjParent.getString("m");
            cmd = jsonObjParent.getString("cmd");

            functionHash.put(cmd, m);

            if (cmd.equals(Constants.CacheUserInfo)) {
                String userId = jsonObjParent.getString("userId");
                String token = jsonObjParent.getString("token");
                String account = jsonObjParent.getString("account");
                String nickname = jsonObjParent.getString("nickname");

                PreferenceUtils.setPreferenceString(mContext, "userId", userId);
                if (nickname.equals("呵呵呵") ) {
                    sendHandler(1, "", "", cmd, Constants.cacheUserInfo, "hehe");
                } else {
                    sendHandler(0, "-1", "失败", cmd, Constants.cacheUserInfo);
                }
            } else if (cmd.equals(Constants.GetCacheUserInfo)) {
                String userId = jsonObjParent.getString("userId");
                String result = PreferenceUtils.getPreferenceString(mContext, "userId", "");
                if (!TextUtils.isEmpty(result)) {
                    sendHandler(1, "", "", cmd, Constants.getCacheUserInfo, result);
                } else {
                    sendHandler(0, "-1", "失败", cmd, Constants.getCacheUserInfo);
                }
            } else if (cmd.equals(Constants.Call)) {
                String phoneNumber = jsonObjParent.getString("phone");
                DeviceUtil.phoneCall(mContext, phoneNumber);
                removeFunction(cmd);
            } else if (cmd.equals(Constants.PhoneVibration)) {
                DeviceUtil.vibrateShort(mContext, 800L);
                removeFunction(cmd);
            } else if (cmd.equals(Constants.FingerPrint)) {
                ((WebViewActivity)mContext).initFingerPrint();
                removeFunction(cmd);
            } else if (cmd.equals(Constants.ChooseImage)) {
                final String copycmd = cmd;
                if (requestPermission(Constants.camraString, Constants.CAMERA_PERMISSION_REQ_CODE)) {
                    ListPopMenuDialogUtils menuDailogDAL = new ListPopMenuDialogUtils(mContext);
                    menuDailogDAL.showWebviewSelectPhotoDialog(new ListPopMenuDialogUtils.PhotoItemClick() {

                        @Override
                        public void itemclick(int position) {
                            if (position == 2) {
                                sendHandler(0, "-1", "没有选择图片", copycmd, Constants.chooseImage);
                            }
                        }
                    });
                    removeFunction(cmd);
                } else {
                    removeFunction(cmd);
                }
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
            switch (msg.what) {
                case Constants.cacheUserInfo: {
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.get("data") != null) {
                        DataJson.addProperty("msg", bundleData.get("data").toString());
                    }
                    ParentJson.add("data", DataJson);
                    ParentJson.addProperty("nonstop", 0);
                    callbackJsFun(fun, ParentJson.toString());
                }
                break;
                case Constants.getCacheUserInfo: {
                    JsonObject DataJson = new JsonObject();
                    if (bundleData.get("data") != null) {
                        DataJson.addProperty("nickname", bundleData.get("data").toString());
                    }
                    ParentJson.add("data", DataJson);
                    ParentJson.addProperty("nonstop", 0);
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
}
