package com.wjjm.vehicle.datacentersdk.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class JsonUtil {

    private final Gson gson;

    public static JsonUtil getInstance() {
        return JsonUtil.InnerHolder.INSTANCE;
    }

    private static class InnerHolder {
        private static final JsonUtil INSTANCE = new JsonUtil();
    }

    private JsonUtil() {
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

    public String toJson(Object src) {
        return gson.toJson(src);
    }

    public <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }
}
