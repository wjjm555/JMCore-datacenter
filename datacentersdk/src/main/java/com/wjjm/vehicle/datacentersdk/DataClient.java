package com.wjjm.vehicle.datacentersdk;

import android.content.Context;

import com.wjjm.vehicle.datacentersdk.interfaces.OnChangedListener;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public final class DataClient {

    private final ResolverWrapper resolverWrapper;

    public DataClient(Context context) {
        resolverWrapper = new ResolverWrapperImp(context.getContentResolver());
    }

    public void insertToDataCenter(String key, Object value) {
        resolverWrapper.insert(key, value);
    }

    public void updateToDataCenter(String key, Object value) {
        resolverWrapper.update(key, value);
    }

    public <T> T queryFromDataCenter(String key, Class<T> tClass) {
        return resolverWrapper.query(key, tClass);
    }

    public <T> T queryFromDataCenter(String key, Type typeOfT) {
        return resolverWrapper.query(key, typeOfT);
    }

    public String queryFromDataCenter(String key) {
        return resolverWrapper.query(key);
    }

    public int queryIntFromDataCenter(String key) {
        return resolverWrapper.query(key, int.class);
    }

    public long queryLongFromDataCenter(String key) {
        return resolverWrapper.query(key, long.class);
    }

    public double queryDoubleFromDataCenter(String key) {
        return resolverWrapper.query(key, double.class);
    }

    public float queryFloatFromDataCenter(String key) {
        return resolverWrapper.query(key, float.class);
    }

    public boolean queryBooleanFromDataCenter(String key) {
        return resolverWrapper.query(key, boolean.class);
    }

    public <T> List<T> queryListFromDataCenter(String key) {
        return resolverWrapper.queryList(key);
    }

    public <K, V> Map<K, V> queryMapFromDataCenter(String key) {
        return resolverWrapper.queryMap(key);
    }

    public void deleteFromDataCenter(String key) {
        resolverWrapper.delete(key);
    }

    public void addListener(String key, OnChangedListener listener) {
        resolverWrapper.registerContentObserver(key, listener);
    }

    public void removeListener(OnChangedListener listener) {
        resolverWrapper.unregisterContentObserver(listener);
    }


}
