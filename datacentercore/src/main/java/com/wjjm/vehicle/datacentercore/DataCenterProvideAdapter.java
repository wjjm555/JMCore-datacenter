package com.wjjm.vehicle.datacentercore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public abstract class DataCenterProvideAdapter {

    private Context context;

    public DataCenterProvideAdapter(Context context) {
        this.context = context;
    }

    public abstract String getType(Uri uri);

    public abstract Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);

    public abstract Uri insert(Uri uri, ContentValues values);

    public abstract int delete(Uri uri, String selection, String[] selectionArgs);

    public abstract int update(Uri uri, ContentValues values, String selection, String[] selectionArgs);

    protected Context getContext() {
        return context;
    }
}
