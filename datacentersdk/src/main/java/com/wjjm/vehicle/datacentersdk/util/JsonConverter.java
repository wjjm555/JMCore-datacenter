package com.wjjm.vehicle.datacentersdk.util;

import java.lang.reflect.Type;

/**
 * JSON 序列化插件接口。
 * 默认实现为 {@link OrgJsonConverter}(基于 Android 自带 org.json + 反射,零三方依赖),
 * 可通过 {@link JsonUtil#setConverter(JsonConverter)} 替换为 Gson 或其他实现。
 */
public interface JsonConverter {

    String toJson(Object src);

    <T> T fromJson(String json, Class<T> classOfT);

    <T> T fromJson(String json, Type typeOfT);
}
