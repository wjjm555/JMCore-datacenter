package com.wjjm.vehicle.datacentercore;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.wjjm.vehicle.datacentercore.db.RoomDataCenterProvideAdapter;

public class DataCenterProvider extends ContentProvider {

    private DataCenterProvideAdapter adapter;

    @Override public boolean onCreate() {
        adapter = new RoomDataCenterProvideAdapter(getContext());
        return true;
    }

    @Override public String getType(Uri uri) {
        return adapter.getType(uri);
    }

    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return adapter.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override public Uri insert(Uri uri, ContentValues values) {
        return adapter.insert(uri, values);
    }

    @Override public int delete(Uri uri, String selection, String[] selectionArgs) {
        return adapter.delete(uri, selection, selectionArgs);
    }

    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return adapter.update(uri, values, selection, selectionArgs);
    }


}
