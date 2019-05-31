package com.gp.rainy;

import com.zhy.http.okhttp.callback.Callback;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Response;

public abstract class AbstractJsonCallback extends Callback<JSONObject> {
    @Override
    public JSONObject parseNetworkResponse(Response response, int id) throws IOException {
        String string = response.body().string();
        JSONObject object = null;
        try {
            object = new JSONObject(string);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }
}