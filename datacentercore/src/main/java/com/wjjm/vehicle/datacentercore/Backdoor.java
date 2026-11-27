package com.wjjm.vehicle.datacentercore;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.wjjm.vehicle.datacentercore.db.DataBean;

import static com.wjjm.vehicle.datacentercore.Constant.KEY;
import static com.wjjm.vehicle.datacentercore.Constant.VALUE;
import static com.wjjm.vehicle.datacentercore.util.UriUtil.parseUri;


/**
 * 调试后门:通过广播(adb shell am broadcast)对数据中心执行增删改查。
 * 默认关闭,使用前需调用 {@link #setEnabled(Context, boolean)} 开启。
 */
public class Backdoor extends BroadcastReceiver {

    private static final String TAG = "DataCenter_NSR";

    private static final String KEY_ACTION = "action";
    private static final int INSERT = 1, DELETE = 2, UPDATE = 3, QUERY = 4;

    /**
     * 开启 / 关闭后门接收器。默认关闭,即使开启也仅对 shell / root 的显式广播可达
     * (manifest 中 exported=false,其他 App 永远无法触发)。
     */
    public static void setEnabled(Context context, boolean enabled) {
        context.getPackageManager().setComponentEnabledSetting(
                new ComponentName(context, Backdoor.class),
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        DataCenter.getInstance().init(context.getApplicationContext());

        int action = intent.getIntExtra(KEY_ACTION, 0);
        String key = intent.getStringExtra(KEY);
        String value = intent.getStringExtra(VALUE);

        Log.i(TAG, "onReceive action=" + action + ", key=" + key + ", value=" + value);

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
                    Cursor cursor = DataCenter.getInstance().getDao().queryData(new String[]{key});
                    try {
                        if (cursor != null && cursor.moveToNext())
                            Log.i(TAG, key + ":" + cursor.getString(cursor.getColumnIndex(VALUE)));
                    } finally {
                        if (cursor != null) cursor.close();
                    }
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
