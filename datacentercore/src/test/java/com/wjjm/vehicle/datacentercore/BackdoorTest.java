package com.wjjm.vehicle.datacentercore;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static com.wjjm.vehicle.datacentercore.Constant.KEY;
import static com.wjjm.vehicle.datacentercore.Constant.VALUE;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class BackdoorTest {

    private Context context;
    private Backdoor backdoor;

    @Before
    public void setUp() {
        resetDataCenter();
        context = RuntimeEnvironment.getApplication();
        DataCenter.getInstance().init(context);
        backdoor = new Backdoor();
    }

    private static void resetDataCenter() {
        try {
            java.lang.reflect.Field field = DataCenter.class.getDeclaredField("dataBase");
            field.setAccessible(true);
            field.set(DataCenter.getInstance(), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Intent intent(int action, String key, String value) {
        Intent intent = new Intent();
        intent.putExtra("action", action);
        intent.putExtra(KEY, key);
        intent.putExtra(VALUE, value);
        return intent;
    }

    private String queryValue(String key) {
        Cursor cursor = DataCenter.getInstance().getDao().queryData(new String[]{key});
        try {
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getString(cursor.getColumnIndex(VALUE));
            }
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    @Test
    public void testSetEnabledTogglesComponentState() {
        ComponentName component = new ComponentName(context, Backdoor.class);
        Backdoor.setEnabled(context, true);
        assertEquals(PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                context.getPackageManager().getComponentEnabledSetting(component));
        Backdoor.setEnabled(context, false);
        assertEquals(PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                context.getPackageManager().getComponentEnabledSetting(component));
    }

    @Test
    public void testInsertAction() {
        backdoor.onReceive(context, intent(1, "a", "1"));
        assertEquals("1", queryValue("a"));
    }

    @Test
    public void testUpdateActionExisting() {
        backdoor.onReceive(context, intent(1, "a", "1"));
        backdoor.onReceive(context, intent(3, "a", "2"));
        assertEquals("2", queryValue("a"));
    }

    @Test
    public void testUpdateActionMissingInserts() {
        backdoor.onReceive(context, intent(3, "a", "9"));
        assertEquals("9", queryValue("a"));
    }

    @Test
    public void testDeleteAction() {
        backdoor.onReceive(context, intent(1, "a", "1"));
        backdoor.onReceive(context, intent(2, "a", null));
        assertNull(queryValue("a"));
    }

    @Test
    public void testQueryActionMissingKeyDoesNotCrash() {
        backdoor.onReceive(context, intent(4, "missing", null));
    }

    @Test
    public void testDefaultActionDumpsAllWithoutCrash() {
        backdoor.onReceive(context, intent(1, "a", "1"));
        backdoor.onReceive(context, intent(1, "b", "2"));
        backdoor.onReceive(context, intent(0, null, null));
    }

    @Test
    public void testNullExtrasDoNotCrash() {
        backdoor.onReceive(context, intent(1, null, null));
        backdoor.onReceive(context, intent(2, null, null));
        backdoor.onReceive(context, intent(3, null, null));
    }
}
