package com.wjjm.vehicle.datacentersdk;

import android.content.Context;
import android.os.Looper;

import com.wjjm.vehicle.datacentercore.Constant;
import com.wjjm.vehicle.datacentercore.DataCenter;
import com.wjjm.vehicle.datacentercore.DataCenterProvider;
import com.wjjm.vehicle.datacentersdk.interfaces.IntOnChangedListener;
import com.wjjm.vehicle.datacentersdk.interfaces.SimpleOnChangedListener;
import com.wjjm.vehicle.datacentersdk.util.JsonConverter;
import com.wjjm.vehicle.datacentersdk.util.JsonUtil;
import com.wjjm.vehicle.datacentersdk.util.OrgJsonConverter;
import com.wjjm.vehicle.datacentersdk.util.TypeToken;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DataClientTest {

    public static class Pojo {
        public String name;
        public int age;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pojo pojo = (Pojo) o;
            return age == pojo.age && (name == null ? pojo.name == null : name.equals(pojo.name));
        }
    }

    private Context context;
    private DataClient client;

    @Before
    public void setUp() {
        resetDataCenter();
        context = RuntimeEnvironment.getApplication();
        Robolectric.buildContentProvider(DataCenterProvider.class).create(Constant.AUTHORITY);
        client = new DataClient(context);
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

    private static void flushMainLooper() {
        Shadows.shadowOf(Looper.getMainLooper()).idle();
    }

    @Test
    public void testInsertAndQueryString() {
        client.insertToDataCenter("s", "hello");
        assertEquals("hello", client.queryFromDataCenter("s"));
    }

    @Test
    public void testInsertAndQueryNumbers() {
        client.insertToDataCenter("i", 42);
        client.insertToDataCenter("l", 1234567890123L);
        client.insertToDataCenter("d", 1.5);
        client.insertToDataCenter("f", 2.5f);
        client.insertToDataCenter("b", true);
        assertEquals(42, client.queryIntFromDataCenter("i"));
        assertEquals(1234567890123L, client.queryLongFromDataCenter("l"));
        assertEquals(1.5, client.queryDoubleFromDataCenter("d"), 0.0);
        assertEquals(2.5f, client.queryFloatFromDataCenter("f"), 0.0f);
        assertTrue(client.queryBooleanFromDataCenter("b"));
    }

    @Test
    public void testQueryMissingKeyDefaults() {
        assertEquals("", client.queryFromDataCenter("missing"));
        assertEquals(0, client.queryIntFromDataCenter("missing"));
        assertEquals(0L, client.queryLongFromDataCenter("missing"));
        assertEquals(0.0, client.queryDoubleFromDataCenter("missing"), 0.0);
        assertFalse(client.queryBooleanFromDataCenter("missing"));
    }

    @Test
    public void testPojoRoundtripWithoutExposeAnnotation() {
        Pojo pojo = new Pojo();
        pojo.name = "wjjm";
        pojo.age = 27;
        client.insertToDataCenter("pojo", pojo);

        Pojo byClass = client.queryFromDataCenter("pojo", Pojo.class);
        assertEquals(pojo, byClass);

        Pojo byType = client.queryFromDataCenter("pojo", (Type) Pojo.class);
        assertEquals(pojo, byType);
    }

    @Test
    public void testTypedListRoundtrip() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        client.insertToDataCenter("list", list);
        Type type = new TypeToken<List<Integer>>() {
        }.getType();
        assertEquals(list, client.<List<Integer>>queryFromDataCenter("list", type));
    }

    @Test
    public void testRawListQueryReturnsJsonNumbers() {
        // queryListFromDataCenter 无法获知元素类型,整数按 Integer 解析
        List<Integer> list = Arrays.asList(1, 2, 3);
        client.insertToDataCenter("list", list);
        assertEquals(Arrays.asList(1, 2, 3), client.queryListFromDataCenter("list"));
    }

    @Test
    public void testTypedMapRoundtrip() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        client.insertToDataCenter("map", map);
        Type type = new TypeToken<Map<String, Integer>>() {
        }.getType();
        assertEquals(map, client.<Map<String, Integer>>queryFromDataCenter("map", type));
    }

    @Test
    public void testRawMapQueryReturnsJsonNumbers() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        client.insertToDataCenter("map", map);
        Map<String, Integer> expected = new HashMap<>();
        expected.put("a", 1);
        expected.put("b", 2);
        assertEquals(expected, client.queryMapFromDataCenter("map"));
    }

    @Test
    public void testUpdateExisting() {
        client.insertToDataCenter("u", 1);
        client.updateToDataCenter("u", 2);
        assertEquals(2, client.queryIntFromDataCenter("u"));
    }

    @Test
    public void testUpdateMissingCreates() {
        client.updateToDataCenter("u", 9);
        assertEquals(9, client.queryIntFromDataCenter("u"));
    }

    @Test
    public void testDelete() {
        client.insertToDataCenter("d", "x");
        client.deleteFromDataCenter("d");
        assertEquals("", client.queryFromDataCenter("d"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyKeyThrows() {
        client.insertToDataCenter("", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullKeyThrows() {
        client.insertToDataCenter(null, 1);
    }

    @Test
    public void testSimpleListenerOnInsertAndDelete() {
        List<String> received = new ArrayList<>();
        SimpleOnChangedListener listener = new SimpleOnChangedListener() {
            @Override
            public void onChanged(String value) {
                received.add(value);
            }
        };
        client.addListener("key", listener);
        client.insertToDataCenter("key", "1");
        flushMainLooper();
        client.insertToDataCenter("key", "2");
        flushMainLooper();
        client.deleteFromDataCenter("key");
        flushMainLooper();
        assertEquals(Arrays.asList("1", "2", ""), received);
    }

    @Test
    public void testTypedListenerDeleteDoesNotCrash() {
        List<Integer> received = new ArrayList<>();
        IntOnChangedListener listener = new IntOnChangedListener() {
            @Override
            protected void onDataChanged(String key, Integer integer) {
                received.add(integer);
            }
        };
        client.addListener("key", listener);
        client.insertToDataCenter("key", 5);
        flushMainLooper();
        client.deleteFromDataCenter("key");
        flushMainLooper();
        assertEquals(2, received.size());
        assertEquals(Integer.valueOf(5), received.get(0));
        assertNull(received.get(1));
    }

    @Test
    public void testSameListenerOnTwoKeys() {
        List<String> received = new ArrayList<>();
        SimpleOnChangedListener listener = new SimpleOnChangedListener() {
            @Override
            public void onChanged(String value) {
                received.add(value);
            }
        };
        client.addListener("keyA", listener);
        client.addListener("keyB", listener);
        client.insertToDataCenter("keyA", "a");
        flushMainLooper();
        client.insertToDataCenter("keyB", "b");
        flushMainLooper();
        assertEquals(Arrays.asList("a", "b"), received);
    }

    @Test
    public void testRemoveListenerStopsCallbacks() {
        List<String> received = new ArrayList<>();
        SimpleOnChangedListener listener = new SimpleOnChangedListener() {
            @Override
            public void onChanged(String value) {
                received.add(value);
            }
        };
        client.addListener("key", listener);
        client.insertToDataCenter("key", "1");
        flushMainLooper();
        client.removeListener(listener);
        client.insertToDataCenter("key", "2");
        flushMainLooper();
        assertEquals(Arrays.asList("1"), received);
    }

    @Test
    public void testCustomConverterPlugin() {
        try {
            JsonUtil.setConverter(new JsonConverter() {
                @Override
                public String toJson(Object src) {
                    return "custom|" + src;
                }

                @Override
                public <T> T fromJson(String json, Class<T> classOfT) {
                    if (json == null || !json.startsWith("custom|")) return null;
                    String value = json.substring("custom|".length());
                    if (classOfT == Integer.class) return (T) Integer.valueOf(value);
                    return (T) value;
                }

                @Override
                public <T> T fromJson(String json, Type typeOfT) {
                    return (T) fromJson(json, Object.class);
                }
            });
            client.insertToDataCenter("plugin", 123);
            assertEquals("custom|123", client.queryFromDataCenter("plugin"));
            assertEquals(Integer.valueOf(123), client.queryFromDataCenter("plugin", Integer.class));
        } finally {
            JsonUtil.setConverter(new OrgJsonConverter());
        }
    }
}
