package com.wjjm.vehicle.datacentersdk;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.wjjm.vehicle.datacentersdk.interfaces.OnChangedListener;

class MyContentObserver extends ContentObserver {

    private final OnChangedListener listener;

    private final OnChangedCallBack callBack;

    private final String key;

    MyContentObserver(Handler handler, String key, OnChangedListener listener, OnChangedCallBack callBack) {
        super(handler);
        this.listener = listener;
        this.callBack = callBack;
        this.key = key;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        if (uri != null && uri.getPath() != null) {
            String[] path = uri.getPath().split("/");
            String key = path[path.length - 1];

            if (TextUtils.equals(key, this.key) && callBack != null) {
                callBack.onChanged(key, listener);
            }

        }

    }

    public interface OnChangedCallBack {
        void onChanged(String key, OnChangedListener listener);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyContentObserver that = (MyContentObserver) o;
        return listener.equals(that.listener);
    }

    @Override
    public int hashCode() {
        return listener.hashCode();
    }
}
