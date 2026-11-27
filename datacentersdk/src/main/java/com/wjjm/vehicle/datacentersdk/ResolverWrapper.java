package com.wjjm.vehicle.datacentersdk;

import android.content.ContentResolver;

import com.wjjm.vehicle.datacentersdk.interfaces.OnChangedListener;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

abstract class ResolverWrapper {

    private final ContentResolver resolver;

    ResolverWrapper(ContentResolver resolver) {
        this.resolver = resolver;
    }

    public abstract String query(String key);

    public abstract <T> T query(String key, Class<T> tClass);

    public abstract <T> T query(String key, Type typeOfT);

    public abstract <T> List<T> queryList(String key);

    public abstract <K, V> Map<K, V> queryMap(String key);

    public abstract void insert(String key, Object value);

    public abstract void update(String key, Object value);

    public abstract void delete(String key);

    public abstract void registerContentObserver(String key, OnChangedListener listener);

    public abstract void unregisterContentObserver(OnChangedListener listener);

    final ContentResolver getResolver() {
        return resolver;
    }
}
