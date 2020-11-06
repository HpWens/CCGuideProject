package com.mei.guide.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.zhouyou.http.model.ApiResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ParseJsonUtils {

    public static String toJson(Object object) {
        try {
            return new Gson().toJson(object);
        } catch (Exception e) {
        }
        return "";
    }

    public static <T> T parseData(String json, Class<T> t) {
        return new Gson().fromJson(json, t);
    }

    public static <T> Result<T> parseDataToResult(String json, Class<T> clazz) {
        if (TextUtils.isEmpty(json)) {
            return new Result<>();
        }
        try {
            Type type = new ParameterizedTypeImpl(Result.class, new Class[]{clazz});
            return new Gson().fromJson(json, type);
        } catch (Exception e) {
            return new Result<>();
        }
    }

    public static <T> ApiResult<T> parseDataToApiResult(String json, Class<T> clazz) {
        Type type = new ParameterizedTypeImpl(ApiResult.class, new Class[]{clazz});
        return new Gson().fromJson(json, type);
    }

    public static <T> List<T> parseListData(String json, Class<T> clazz) {
        Type listType = new ParameterizedTypeImpl(List.class, new Class[]{clazz});
        try {
            return new Gson().fromJson(json, listType);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static <T> List<T> parseListData(String json, String field, Class<T> clazz) {
        if (TextUtils.isEmpty(json)) {
            return new ArrayList<T>();
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has(field)) {
                return parseListData(jsonObject.optString(field), clazz);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ArrayList<T>();
    }

    public static <T> List<T> parseListData(String json, final String field, final String nextField, Class<T> clazz) {
        if (TextUtils.isEmpty(json)) {
            return new ArrayList<T>();
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has(field)) {
                JSONObject dataObj = new JSONObject(jsonObject.optString(field));
                if (dataObj.has(nextField)) {
                    return parseListData(dataObj.optString(nextField), clazz);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return new ArrayList<T>();
    }

    public static <T> ArrayList<T> parseArrayListData(String json, Class<T> clazz) {
        Type listType = new ParameterizedTypeImpl(List.class, new Class[]{clazz});
        return new Gson().fromJson(json, listType);
    }

    public static String parseFieldData(String json, String fieldName) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has(fieldName)) {
                return jsonObject.optString(fieldName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int parseIntFieldData(String json, String fieldName, String nextFieldName) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has(fieldName)) {
                JSONObject dataObj = new JSONObject(jsonObject.optString(fieldName));
                if (dataObj.has(nextFieldName)) {
                    return dataObj.optInt(nextFieldName);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
