package com.wjjm.vehicle.datacentercore.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.wjjm.vehicle.datacentercore.DataCenter;
import com.wjjm.vehicle.datacentercore.DataCenterProvideAdapter;

import static com.wjjm.vehicle.datacentercore.Constant.AUTHORITY;
import static com.wjjm.vehicle.datacentercore.Constant.KEY;
import static com.wjjm.vehicle.datacentercore.Constant.VALUE;
import static com.wjjm.vehicle.datacentercore.util.UriUtil.parseUri;

public class RoomDataCenterProvideAdapter extends DataCenterProvideAdapter {

    private static final int MATCH_CODE_DATACENTER = 100;

    private static UriMatcher uriMatcher;

    public RoomDataCenterProvideAdapter(Context context) {
        super(context);
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "datacenter", MATCH_CODE_DATACENTER);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int match = uriMatcher.match(uri);
        if (match == MATCH_CODE_DATACENTER) {
            return DataCenter.getInstance().getDao().queryData(selectionArgs, sortOrder);
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) == MATCH_CODE_DATACENTER) {
            String key = values.getAsString(KEY);
            DataBean dataBean = new DataBean(key, values.getAsString(VALUE));
            DataCenter.getInstance().getDao().insertData(dataBean);
            Uri insertUri = parseUri(key);
            notifyChange(insertUri);
            return insertUri;
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (uriMatcher.match(uri) == MATCH_CODE_DATACENTER) {
            int count = DataCenter.getInstance().getDao().deleteData(selectionArgs);
            if (count > 0) {
                for (String key : selectionArgs) {
                    notifyChange(parseUri(key));
                }
            }
            return count;
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (uriMatcher.match(uri) == MATCH_CODE_DATACENTER) {
            String value = values.getAsString(VALUE);
            int count = DataCenter.getInstance().getDao().updateData(value, selectionArgs);
            if (count <= 0) {
                DataCenter.getInstance().getDao().insertData(new DataBean(selectionArgs[0], value));
            }
            for (String key : selectionArgs) {
                notifyChange(parseUri(key));
            }
            return count;
        }
        return 0;
    }

    private void notifyChange(Uri uri) {
        if (getContext() != null && getContext().getContentResolver() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }
}
