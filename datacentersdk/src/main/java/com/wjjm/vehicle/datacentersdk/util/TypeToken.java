package com.wjjm.vehicle.datacentersdk.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 捕获泛型类型,等价于 Gson 的 TypeToken。
 * 用法:new TypeToken&lt;List&lt;String&gt;&gt;() {}.getType()
 */
public abstract class TypeToken<T> {

    private final Type type;

    protected TypeToken() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        } else {
            throw new IllegalArgumentException("TypeToken must be created as an anonymous subclass, e.g. new TypeToken<List<String>>() {}");
        }
    }

    public final Type getType() {
        return type;
    }
}
