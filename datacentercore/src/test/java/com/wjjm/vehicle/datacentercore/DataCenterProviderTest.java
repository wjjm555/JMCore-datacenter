package com.wjjm.vehicle.datacentercore;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static com.wjjm.vehicle.datacentercore.Constant.AUTHORITY;
import static com.wjjm.vehicle.datacentercore.Constant.KEY;
import static com.wjjm.vehicle.datacentercore.Constant.VALUE;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DataCenterProviderTest {

    private static final Uri URI = Uri.parse("content://" + AUTHORITY + "/datacenter");

    private Context context;
    private ContentResolver resolver;

    @Before
    public void setUp() {
        resetDataCenter();
        context = RuntimeEnvironment.getApplication();
        Robolectric.buildContentProvider(DataCenterProvider.class).create(AUTHORITY);
        resolver = context.getContentResolver();
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

    private ContentValues values(String key, String value) {
        ContentValues values = new ContentValues();
        values.put(KEY, key);
        values.put(VALUE, value);
        return values;
    }

    private String queryValue(String key) {
        Cursor cursor = resolver.query(URI, null, KEY, new String[]{key}, null);
        assertNotNull(cursor);
        try {
            if (cursor.moveToNext()) {
                return cursor.getString(cursor.getColumnIndex(VALUE));
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    private int queryCount() {
        Cursor cursor = resolver.query(URI, null, null, null, null);
        assertNotNull(cursor);
        try {
            return cursor.getCount();
        } finally {
            cursor.close();
        }
    }

    @Test
    public void testInsertAndQuery() {
        assertNotNull(resolver.insert(URI, values("a", "1")));
        assertEquals("1", queryValue("a"));
    }

    @Test
    public void testQueryMissingKey() {
        assertNull(queryValue("missing"));
    }

    @Test
    public void testQueryNullSelectionArgsIsSafe() {
        resolver.insert(URI, values("a", "1"));
        Cursor cursor = resolver.query(URI, null, KEY, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    @Test
    public void testQueryWrongUriReturnsNull() {
        assertNull(resolver.query(Uri.parse("content://" + AUTHORITY + "/other"), null, null, null, null));
    }

    @Test
    public void testUpdateExisting() {
        resolver.insert(URI, values("a", "1"));
        assertEquals(1, resolver.update(URI, values(null, "2"), KEY, new String[]{"a"}));
        assertEquals("2", queryValue("a"));
    }

    @Test
    public void testUpdateMissingInserts() {
        assertEquals(0, resolver.update(URI, values(null, "9"), KEY, new String[]{"new"}));
        assertEquals("9", queryValue("new"));
    }

    @Test
    public void testUpdateNullSelectionArgsIsSafe() {
        assertEquals(0, resolver.update(URI, values(null, "9"), KEY, null));
    }

    @Test
    public void testDelete() {
        resolver.insert(URI, values("a", "1"));
        assertEquals(1, resolver.delete(URI, KEY, new String[]{"a"}));
        assertNull(queryValue("a"));
    }

    @Test
    public void testDeleteNullSelectionArgsIsSafe() {
        resolver.insert(URI, values("a", "1"));
        assertEquals(0, resolver.delete(URI, KEY, null));
        assertEquals("1", queryValue("a"));
    }

    @Test
    public void testInsertWithoutKeyIsSafe() {
        ContentValues values = new ContentValues();
        values.put(VALUE, "1");
        assertNull(resolver.insert(URI, values));
    }

    @Test
    public void testInsertNullValuesIsSafe() {
        assertNull(resolver.insert(URI, null));
    }

    @Test
    public void testDataNotLostAcrossOperations() {
        resolver.insert(URI, values("a", "1"));
        resolver.insert(URI, values("b", "2"));
        resolver.update(URI, values(null, "3"), KEY, new String[]{"a"});
        resolver.delete(URI, KEY, new String[]{"b"});
        assertEquals("3", queryValue("a"));
        assertNull(queryValue("b"));
    }
}
