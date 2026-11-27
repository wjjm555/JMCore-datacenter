package com.wjjm.vehicle.datacentersdk.interfaces;


import com.google.gson.reflect.TypeToken;

import java.util.Map;

public abstract class MapOnChangedListener<K, V> extends TypeOnChangedListener<Map<K, V>> {
    public MapOnChangedListener() {
        super(new TypeToken<Map<K, V>>() {
        }.getType());
    }
}
