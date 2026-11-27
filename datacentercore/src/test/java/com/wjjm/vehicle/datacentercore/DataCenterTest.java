package com.wjjm.vehicle.datacentercore;

import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import com.wjjm.vehicle.datacentercore.db.DataBean;

@RunWith(RobolectricTestRunner.class)
public class DataCenterTest {

    private static void resetDataCenter() {
        try {
            java.lang.reflect.Field field = DataCenter.class.getDeclaredField("dataBase");
            field.setAccessible(true);
            field.set(DataCenter.getInstance(), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetDaoBeforeInitThrows() {
        resetDataCenter();
        try {
            DataCenter.getInstance().getDao();
            fail("getDao before init should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("not initialized"));
        }
    }

    @Test
    public void testInitThenGetDaoWorks() {
        resetDataCenter();
        DataCenter.getInstance().init(RuntimeEnvironment.getApplication());
        assertEquals(0, DataCenter.getInstance().getDao().queryAllData().length);
    }

    @Test
    public void testInitIsIdempotent() {
        resetDataCenter();
        DataCenter.getInstance().init(RuntimeEnvironment.getApplication());
        DataCenter.getInstance().init(RuntimeEnvironment.getApplication());
        DataCenter.getInstance().getDao().insertData(new DataBean("k", "v"));
        assertEquals(1, DataCenter.getInstance().getDao().queryAllData().length);
    }

    @Test
    public void testInitWithNullContextIsSafe() {
        resetDataCenter();
        DataCenter.getInstance().init(null);
    }
}
