package com.wjjm.vehicle.datacentercore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.wjjm.vehicle.datacentercore.db.DataBean;

import static com.wjjm.vehicle.datacentercore.Constant.KEY;
import static com.wjjm.vehicle.datacentercore.Constant.VALUE;
import static com.wjjm.vehicle.datacentercore.util.UriUtil.parseUri;


public class Backdoor extends BroadcastReceiver {

    final String TAG = "DataCenter_NSR";

    final String KEY_ACTION = "action";
    final int INSERT = 1, DELETE = 2, UPDATE = 3, QUERY = 4;

    @Override
    public void onReceive(Context context, Intent intent) {

        int action = intent.getIntExtra(KEY_ACTION, 0);
        String key = intent.getStringExtra(KEY);
        String value = intent.getStringExtra(VALUE);

        Log.i(TAG, String.format("onReceive Action:%d " + (TextUtils.isEmpty(key) ? "" : "%s") + (TextUtils.isEmpty(value) ? "" : ":%s"), action, key, value));

        switch (action) {
            case INSERT:
                if (!TextUtils.isEmpty(key) && value != null) {
                    DataCenter.getInstance().getDao().insertData(new DataBean(key, value));
                    notifyChange(context, parseUri(key));
                }

                break;

            case DELETE:
                if (!TextUtils.isEmpty(key)) {
                    DataCenter.getInstance().getDao().deleteData(new String[]{key});
                    notifyChange(context, parseUri(key));
                }
                break;

            case UPDATE:
                if (!TextUtils.isEmpty(key) && value != null) {
                    int count = DataCenter.getInstance().getDao().updateData(value, new String[]{key});
                    if (count <= 0)
                        DataCenter.getInstance().getDao().insertData(new DataBean(key, value));
                    notifyChange(context, parseUri(key));
                }
                break;

            case QUERY:
                if (!TextUtils.isEmpty(key)) {
                    Cursor cursor = DataCenter.getInstance().getDao().queryData(new String[]{key}, KEY);
                    if (cursor.moveToNext())
                        Log.i(TAG, String.format("%s:%s", key, cursor.getString(cursor.getColumnIndex(VALUE))));
                    cursor.close();
                }
                break;

            default:
                for (DataBean bean : DataCenter.getInstance().getDao().queryAllData()) {
                    Log.i(TAG, bean.toString());
                }
                break;
        }


    }

    private void notifyChange(Context context, Uri uri) {
        if (context != null && context.getContentResolver() != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
    }


}
