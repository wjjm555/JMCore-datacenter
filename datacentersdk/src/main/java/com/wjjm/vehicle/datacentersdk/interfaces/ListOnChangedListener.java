package com.wjjm.vehicle.datacentersdk.interfaces;

import com.google.gson.reflect.TypeToken;

import java.util.List;

public abstract class ListOnChangedListener<T> extends TypeOnChangedListener<List<T>> {
    public ListOnChangedListener() {
        super(new TypeToken<List<T>>() {
        }.getType());
    }
}
