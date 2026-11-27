package com.wjjm.vehicle.datacentersdk.util;

import java.lang.reflect.Type;

/**
 * JSON 门面。默认使用 {@link OrgJsonConverter}(Android 自带 org.json,零三方依赖),
 * 可通过 {@link #setConverter(JsonConverter)} 全局替换为自定义实现(如 Gson)。
 */
public class JsonUtil {

    private static volatile JsonConverter converter = new OrgJsonConverter();

    public static JsonUtil getInstance() {
        return JsonUtil.InnerHolder.INSTANCE;
    }

    private static class InnerHolder {
        private static final JsonUtil INSTANCE = new JsonUtil();
    }

    private JsonUtil() {
    }

    public static void setConverter(JsonConverter jsonConverter) {
        if (jsonConverter != null) {
            converter = jsonConverter;
        }
    }

    public String toJson(Object src) {
        return converter.toJson(src);
    }

    public <T> T fromJson(String json, Class<T> classOfT) {
        return converter.fromJson(json, classOfT);
    }

    public <T> T fromJson(String json, Type typeOfT) {
        return converter.fromJson(json, typeOfT);
    }
}
