package com.gp.rainy.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    private static Gson gson = new Gson();

    /**
     * 解析json串为一个Bean对象
     */
    public static <T> T parserJSONObject(String jsonStr, Class<T> resultCls) {
        return gson.fromJson(jsonStr, resultCls);
    }

    /**
     * 解析json串为一个Bean类型对象的集合
     */
    public static <T> List<T> parserJSONArray(String jsonStr) {
        return gson.fromJson(jsonStr, new TypeToken<ArrayList<T>>() {
        }.getType());
    }

    /**
     * 解析json串为一个Type类型对象的集合
     */
    public static <T> List<T> parserJSONArray(String jsonStr, Type type) throws JSONException {
        return gson.fromJson(jsonStr, type);
    }

    public static boolean isJson(String json) {
        if (json == null) {
            return false;
        } else if (json.length() < 2) {
            return false;
        } else return json.startsWith("{") && json.endsWith("}");
    }

    /**
     * map转json
     */
    public static String map2Json(Map map) {
        return gson.toJson(map);
    }

    /**
     * object转json
     */
    public static String object2Json(Object object) {
        return gson.toJson(object);
    }


    /**
     * 把json 字符串转化成list
     */
    public static <T> List<T> jsonToList(String json, Class<T> cls) {
        List<T> list = new ArrayList<>();
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        for (final JsonElement elem : array) {
            list.add(gson.fromJson(elem, cls));
        }
        return list;
    }

    /**
     * 对象转换成json字符串
     *
     * @param obj 需要被转换的对象
     * @return json字符串
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }


    /**
     * json字符串转成对象
     *
     * @param str  json字符串
     * @param type 类型
     * @return 被转换的对象
     */
    public static <T> T fromJson(String str, Class<T> type) {
        return gson.fromJson(str, type);
    }

    /**
     * json字符串转成对象
     *
     * @param str  json字符串
     * @param type 类型
     * @return 被转换的对象
     */
    public static <T> T fromJson(String str, Type type) {
        return gson.fromJson(str, type);
    }

    /**
     * String转List集合
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        try {
            JsonArray array = new JsonParser().parse(json).getAsJsonArray();
            for (final JsonElement elem : array) {
                list.add(gson.fromJson(elem, clazz));
            }
        } catch (Exception e) {
        }
        return list;
    }

    /**
     * List集合转String
     */
    public static <T> String listToJson(List<T> list) {
        String result = "";
        if (list == null || list.size() == 0) {
            return result;
        }

        Type listType = new TypeToken<List<T>>() {
        }.getType();
        result = gson.toJson(list, listType);

        return result;
    }
}
