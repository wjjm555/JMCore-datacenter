package com.wjjm.vehicle.datacentersdk.interfaces;

import com.wjjm.vehicle.datacentersdk.util.JsonUtil;

public abstract class ClassOnChangedListener<T> implements OnChangedListener {

    private final Class<T> tClass;

    public ClassOnChangedListener(Class<T> tClass) {
        this.tClass = tClass;
    }

    protected abstract void onDataChanged(String key, T t);

    @Override
    public final void onChanged(String key, String value) {
        onDataChanged(key, JsonUtil.getInstance().fromJson(value, tClass));
    }
}
