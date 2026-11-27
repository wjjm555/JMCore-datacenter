package com.wjjm.vehicle.datacentersdk.interfaces;

import com.wjjm.vehicle.datacentersdk.util.JsonUtil;

import java.lang.reflect.Type;

public abstract class TypeOnChangedListener<T> implements OnChangedListener {

    private final Type typeOfT;

    public TypeOnChangedListener(Type typeOfT) {
        this.typeOfT = typeOfT;
    }

    protected abstract void onDataChanged(String key, T t);

    @Override
    public final void onChanged(String key, String value) {
        onDataChanged(key, fromJson(value, typeOfT));
    }

    private T fromJson(String json, Type typeOfT) {
        return JsonUtil.getInstance().fromJson(json, typeOfT);
    }
}
