package com.wjjm.vehicle.datacentersdk;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.reflect.TypeToken;
import com.wjjm.vehicle.datacentersdk.interfaces.OnChangedListener;
import com.wjjm.vehicle.datacentersdk.util.JsonUtil;
import com.wjjm.vehicle.datacentersdk.util.UriUtil;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.wjjm.vehicle.datacentersdk.Constant.KEY;
import static com.wjjm.vehicle.datacentersdk.Constant.URI;
import static com.wjjm.vehicle.datacentersdk.Constant.VALUE;

class ResolverWrapperImp extends ResolverWrapper implements MyContentObserver.OnChangedCallBack {

    private volatile Handler observerHandler;

    private final Map<OnChangedListener, Map<String, ContentObserver>> observerMap = new ConcurrentHashMap<>();

    ResolverWrapperImp(ContentResolver resolver) {
        super(resolver);
    }

    @Override
    public String query(String key) {
        checkKey(key);
        Cursor cursor = getResolver().query(URI, null, KEY, new String[]{key}, null);
        return cursorToString(cursor);
    }

    @Override
    public <T> T query(String key, Class<T> tClass) {
        checkKey(key);
        try {
            T t = JsonUtil.getInstance().fromJson(queryCursor(key), tClass);
            return t == null ? defaultValue(tClass) : t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue(tClass);
    }

    @Override
    public <T> T query(String key, Type typeOfT) {
        checkKey(key);
        try {
            return JsonUtil.getInstance().fromJson(queryCursor(key), typeOfT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> List<T> queryList(String key) {
        checkKey(key);
        try {
            return JsonUtil.getInstance().fromJson(queryCursor(key), new TypeToken<List<T>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <K, V> Map<K, V> queryMap(String key) {
        checkKey(key);
        try {
            return JsonUtil.getInstance().fromJson(queryCursor(key), new TypeToken<Map<K, V>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void insert(String key, Object value) {
        checkKey(key);
        ContentValues values = new ContentValues();
        values.put(KEY, key);
        values.put(VALUE, (value instanceof String ? value.toString() : JsonUtil.getInstance().toJson(value)));
        getResolver().insert(URI, values);
    }

    @Override
    public void update(String key, Object value) {
        checkKey(key);
        ContentValues values = new ContentValues();
        values.put(VALUE, (value instanceof String ? value.toString() : JsonUtil.getInstance().toJson(value)));
        getResolver().update(URI, values, KEY, new String[]{key});
    }

    @Override
    public void delete(String key) {
        checkKey(key);
        getResolver().delete(URI, KEY, new String[]{key});
    }

    @Override
    public void registerContentObserver(String key, OnChangedListener listener) {
        checkKey(key);
        Map<String, ContentObserver> keyedObservers = observerMap.computeIfAbsent(listener, l -> new ConcurrentHashMap<>());
        ContentObserver observer = keyedObservers.get(key);
        if (observer == null) {
            observer = new MyContentObserver(getObserverHandler(), key, listener, this);
            keyedObservers.put(key, observer);
        }
        getResolver().registerContentObserver(UriUtil.parseUri(key), false, observer);
    }

    @Override
    public void unregisterContentObserver(OnChangedListener listener) {
        Map<String, ContentObserver> keyedObservers = observerMap.remove(listener);
        if (keyedObservers != null) {
            for (ContentObserver observer : keyedObservers.values()) {
                getResolver().unregisterContentObserver(observer);
            }
        }
    }

    @Override
    public void onChanged(String key, OnChangedListener listener) {
        if (listener != null) {
            listener.onChanged(key, query(key));
        }
    }

    private Handler getObserverHandler() {
        if (observerHandler == null) {
            synchronized (this) {
                if (observerHandler == null) {
                    observerHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return observerHandler;
    }

    private String queryCursor(String key) {
        Cursor cursor = getResolver().query(URI, null, KEY, new String[]{key}, null);
        return cursorToString(cursor);
    }

    private String cursorToString(Cursor cursor) {
        String value = "";
        if (cursor != null) {
            if (cursor.moveToNext())
                value = cursor.getString(cursor.getColumnIndex(VALUE));
            cursor.close();
        }
        return value;
    }

    private static void checkKey(String key) {
        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("key must not be empty");
        }
    }

    private <T> T defaultValue(Class<T> tClass) {
        if (tClass.isPrimitive()) {
            if (tClass == boolean.class) return (T) Boolean.FALSE;
            if (tClass == char.class) return (T) Character.valueOf(' ');
            if (tClass == byte.class) return (T) Byte.valueOf((byte) 0);
            if (tClass == short.class) return (T) Short.valueOf((short) 0);
            if (tClass == int.class) return (T) Integer.valueOf(0);
            if (tClass == long.class) return (T) Long.valueOf(0);
            if (tClass == float.class) return (T) Float.valueOf(0);
            if (tClass == double.class) return (T) Double.valueOf(0);
        }
        if (Byte.class.isAssignableFrom(tClass)) {
            return (T) Byte.valueOf((byte) 0);
        } else if (Short.class.isAssignableFrom(tClass)) {
            return (T) Short.valueOf((short) 0);
        } else if (Integer.class.isAssignableFrom(tClass)) {
            return (T) Integer.valueOf(0);
        } else if (Long.class.isAssignableFrom(tClass)) {
            return (T) Long.valueOf(0);
        } else if (Float.class.isAssignableFrom(tClass)) {
            return (T) Float.valueOf(0);
        } else if (Double.class.isAssignableFrom(tClass)) {
            return (T) Double.valueOf(0);
        } else if (Character.class.isAssignableFrom(tClass)) {
            return (T) Character.valueOf(' ');
        } else if (Boolean.class.isAssignableFrom(tClass)) {
            return (T) Boolean.FALSE;
        } else if (String.class.isAssignableFrom(tClass)) {
            return (T) "";
        }
        return null;
    }
}
