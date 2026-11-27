package com.wjjm.vehicle.datacentersdk.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class OrgJsonConverterTest {

    private final OrgJsonConverter converter = new OrgJsonConverter();

    public static class Pojo {
        public String name;
        public int age;
        public List<String> tags;
        public static String staticField = "s";
        public transient String transientField = "t";
    }

    public static class Outer {
        public Inner inner;
    }

    public static class Inner {
        public int x;
    }

    public enum Color {
        RED, BLUE
    }

    @Test
    public void testToJsonNull() {
        assertEquals("null", converter.toJson(null));
    }

    @Test
    public void testToJsonString() {
        assertEquals("\"hello\"", converter.toJson("hello"));
        assertEquals("\"a\\\"b\\\\c\"", converter.toJson("a\"b\\c"));
    }

    @Test
    public void testToJsonNumbersAndBooleans() {
        assertEquals("5", converter.toJson(5));
        assertEquals("1234567890123", converter.toJson(1234567890123L));
        assertEquals("1.5", converter.toJson(1.5));
        assertEquals("true", converter.toJson(true));
    }

    @Test
    public void testToJsonEnum() {
        assertEquals("\"RED\"", converter.toJson(Color.RED));
    }

    @Test
    public void testToJsonPojo() {
        Pojo pojo = new Pojo();
        pojo.name = "wjjm";
        pojo.age = 27;
        String json = converter.toJson(pojo);
        assertTrue(json.contains("\"name\":\"wjjm\""));
        assertTrue(json.contains("\"age\":27"));
        assertFalse(json.contains("staticField"));
        assertFalse(json.contains("transientField"));
    }

    @Test
    public void testToJsonNestedPojoAndNullField() {
        Outer outer = new Outer();
        String json = converter.toJson(outer);
        assertTrue(json.contains("\"inner\":null"));
        outer.inner = new Inner();
        outer.inner.x = 3;
        assertTrue(converter.toJson(outer).contains("\"x\":3"));
    }

    @Test
    public void testToJsonListAndMap() {
        assertEquals("[1,2,3]", converter.toJson(Arrays.asList(1, 2, 3)));
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", "x");
        String json = converter.toJson(map);
        assertTrue(json.contains("\"a\":1"));
        assertTrue(json.contains("\"b\":\"x\""));
    }

    @Test
    public void testToJsonArrays() {
        assertEquals("[1,2]", converter.toJson(new int[]{1, 2}));
        assertEquals("[\"a\",\"b\"]", converter.toJson(new String[]{"a", "b"}));
    }

    @Test
    public void testFromJsonNullAndEmpty() {
        assertNull(converter.fromJson(null, int.class));
        assertNull(converter.fromJson("", int.class));
    }

    @Test
    public void testFromJsonPrimitives() {
        assertEquals(Integer.valueOf(5), converter.fromJson("5", int.class));
        assertEquals(Long.valueOf(1234567890123L), converter.fromJson("1234567890123", long.class));
        assertEquals(Double.valueOf(1.5), converter.fromJson("1.5", double.class));
        assertEquals(Boolean.TRUE, converter.fromJson("true", boolean.class));
        assertNull(converter.fromJson("null", int.class));
    }

    @Test
    public void testFromJsonQuotedStringPassthrough() {
        // String 类型原样透传,不做 JSON 反转义
        assertEquals("\"hi\"", converter.fromJson("\"hi\"", String.class));
    }

    @Test
    public void testFromJsonRawStringPassthrough() {
        assertEquals("hello world", converter.fromJson("hello world", String.class));
        assertEquals("123", converter.fromJson("123", String.class));
        assertNull(converter.fromJson("null", String.class));
    }

    @Test
    public void testFromJsonEnum() {
        assertEquals(Color.BLUE, converter.fromJson("\"BLUE\"", Color.class));
    }

    @Test
    public void testFromJsonPojo() {
        Pojo pojo = converter.fromJson("{\"name\":\"wjjm\",\"age\":27,\"tags\":[\"a\",\"b\"]}", Pojo.class);
        assertNotNull(pojo);
        assertEquals("wjjm", pojo.name);
        assertEquals(27, pojo.age);
        assertEquals(Arrays.asList("a", "b"), pojo.tags);
    }

    @Test
    public void testFromJsonPojoMissingFieldsKeepDefault() {
        Pojo pojo = converter.fromJson("{\"name\":\"wjjm\"}", Pojo.class);
        assertNotNull(pojo);
        assertEquals(0, pojo.age);
    }

    @Test
    public void testFromJsonTypedList() {
        Type type = new TypeToken<List<Integer>>() {
        }.getType();
        assertEquals(Arrays.asList(1, 2, 3), converter.fromJson("[1,2,3]", type));
    }

    @Test
    public void testFromJsonTypedListOfPojo() {
        Type type = new TypeToken<List<Inner>>() {
        }.getType();
        List<Inner> list = converter.fromJson("[{\"x\":1},{\"x\":2}]", type);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(1, list.get(0).x);
        assertEquals(2, list.get(1).x);
    }

    @Test
    public void testFromJsonTypedMap() {
        Type type = new TypeToken<Map<String, Integer>>() {
        }.getType();
        Map<String, Integer> map = converter.fromJson("{\"a\":1,\"b\":2}", type);
        assertNotNull(map);
        assertEquals(Integer.valueOf(1), map.get("a"));
        assertEquals(Integer.valueOf(2), map.get("b"));
    }

    @Test
    public void testFromJsonNestedGenerics() {
        Type type = new TypeToken<Map<String, List<Integer>>>() {
        }.getType();
        Map<String, List<Integer>> map = converter.fromJson("{\"a\":[1,2],\"b\":[3]}", type);
        assertNotNull(map);
        assertEquals(Arrays.asList(1, 2), map.get("a"));
        assertEquals(Arrays.asList(3), map.get("b"));
    }

    @Test
    public void testFromJsonRawList() {
        assertEquals(Arrays.asList(1, 2, 3), converter.fromJson("[1,2,3]", List.class));
        assertEquals(Arrays.asList(1.5, 2.5), converter.fromJson("[1.5,2.5]", List.class));
    }

    @Test
    public void testFromJsonMalformedReturnsNull() {
        assertNull(converter.fromJson("{bad json", Pojo.class));
        assertNull(converter.fromJson("[1,", List.class));
    }

    @Test
    public void testFromJsonStringTokenToNumber() {
        // 带引号的数字字符串转数值
        assertEquals(Integer.valueOf(5), converter.fromJson("\"5\"", int.class));
    }

    @Test
    public void testRoundtripPojo() {
        Pojo pojo = new Pojo();
        pojo.name = "round trip \"ok\"";
        pojo.age = 30;
        pojo.tags = Arrays.asList("x", "y");
        Pojo result = converter.fromJson(converter.toJson(pojo), Pojo.class);
        assertEquals(pojo.name, result.name);
        assertEquals(pojo.age, result.age);
        assertEquals(pojo.tags, result.tags);
    }
}
