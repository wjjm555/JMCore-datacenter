package com.wjjm.vehicle.datacentersdk.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基于 Android 自带 org.json + 反射的 JSON 实现,零三方依赖。
 * 支持:基本类型 / 包装类型 / String / 枚举 / POJO / List / Map / 数组及嵌套泛型;
 * 序列化忽略 static / transient 字段,解析失败返回 null。
 */
public class OrgJsonConverter implements JsonConverter {

    @Override
    public String toJson(Object src) {
        if (src instanceof String || src instanceof Character || src instanceof Enum) {
            return JSONObject.quote(src.toString());
        }
        Object token = toToken(src);
        return token == null ? "null" : token.toString();
    }

    private static Object toToken(Object src) {
        if (src == null) return JSONObject.NULL;
        if (src instanceof JSONObject || src instanceof JSONArray) return src;
        if (src instanceof Map) {
            JSONObject object = new JSONObject();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) src).entrySet()) {
                try {
                    object.put(String.valueOf(entry.getKey()), toToken(entry.getValue()));
                } catch (Exception ignored) {
                }
            }
            return object;
        }
        if (src instanceof Iterable) {
            JSONArray array = new JSONArray();
            for (Object element : (Iterable<?>) src) {
                array.put(toToken(element));
            }
            return array;
        }
        if (src.getClass().isArray()) {
            JSONArray array = new JSONArray();
            int length = Array.getLength(src);
            for (int i = 0; i < length; i++) {
                array.put(toToken(Array.get(src, i)));
            }
            return array;
        }
        if (src instanceof Number || src instanceof Boolean) return src;
        if (src instanceof String || src instanceof Character || src instanceof Enum) return src.toString();

        JSONObject object = new JSONObject();
        for (Field field : src.getClass().getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) continue;
            field.setAccessible(true);
            try {
                object.put(field.getName(), toToken(field.get(src)));
            } catch (Exception ignored) {
            }
        }
        return object;
    }

    @Override
    public <T> T fromJson(String json, Class<T> classOfT) {
        return (T) fromJson(json, (Type) classOfT);
    }

    @Override
    public <T> T fromJson(String json, Type typeOfT) {
        if (json == null || json.length() == 0) return null;
        if (typeOfT == String.class) {
            return (T) ("null".equals(json) ? null : json);
        }
        Object token;
        try {
            token = new JSONTokener(json).nextValue();
        } catch (Exception e) {
            return null;
        }
        return (T) convert(token, typeOfT);
    }

    private static Object convert(Object token, Type type) {
        if (token == null || token == JSONObject.NULL) return null;
        if (type instanceof Class) return convertToClass(token, (Class<?>) type);
        if (type instanceof ParameterizedType) return convertToParameterized(token, (ParameterizedType) type);
        if (type instanceof WildcardType) {
            Type[] upper = ((WildcardType) type).getUpperBounds();
            return convert(token, upper.length > 0 ? upper[0] : Object.class);
        }
        if (type instanceof GenericArrayType) {
            Type component = ((GenericArrayType) type).getGenericComponentType();
            if (token instanceof JSONArray) {
                JSONArray array = (JSONArray) token;
                int length = array.length();
                List<Object> list = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    list.add(convert(array.opt(i), component));
                }
                return list;
            }
            return null;
        }
        return token;
    }

    private static Object convertToClass(Object token, Class<?> cls) {
        if (cls == Object.class) return token;
        if (token instanceof String) return fromStringToken((String) token, cls);
        if (token instanceof Boolean) {
            if (cls == Boolean.class || cls == boolean.class) return token;
            if (cls == String.class) return token.toString();
            return null;
        }
        if (token instanceof Number) return numberTo((Number) token, cls);
        if (token instanceof JSONArray) return arrayTo((JSONArray) token, cls);
        if (token instanceof JSONObject) return objectTo((JSONObject) token, cls);
        return null;
    }

    private static Object fromStringToken(String s, Class<?> cls) {
        if (cls == String.class) return s;
        if (cls == Character.class || cls == char.class) return s.length() > 0 ? s.charAt(0) : null;
        if (cls.isEnum()) {
            try {
                return Enum.valueOf((Class<? extends Enum>) cls, s);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        try {
            if (cls == Byte.class || cls == byte.class) return Byte.valueOf(s);
            if (cls == Short.class || cls == short.class) return Short.valueOf(s);
            if (cls == Integer.class || cls == int.class) return Integer.valueOf(s);
            if (cls == Long.class || cls == long.class) return Long.valueOf(s);
            if (cls == Float.class || cls == float.class) return Float.valueOf(s);
            if (cls == Double.class || cls == double.class) return Double.valueOf(s);
            if (cls == Boolean.class || cls == boolean.class) return Boolean.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    private static Object numberTo(Number number, Class<?> cls) {
        if (cls == Number.class) return number;
        if (cls == Byte.class || cls == byte.class) return number.byteValue();
        if (cls == Short.class || cls == short.class) return number.shortValue();
        if (cls == Integer.class || cls == int.class) return number.intValue();
        if (cls == Long.class || cls == long.class) return number.longValue();
        if (cls == Float.class || cls == float.class) return number.floatValue();
        if (cls == Double.class || cls == double.class) return number.doubleValue();
        if (cls == Character.class || cls == char.class) return (char) number.intValue();
        if (cls == String.class) return number.toString();
        return null;
    }

    private static Object arrayTo(JSONArray array, Class<?> cls) {
        if (cls.isArray()) {
            Class<?> component = cls.getComponentType();
            Object result = Array.newInstance(component, array.length());
            for (int i = 0; i < array.length(); i++) {
                Array.set(result, i, convert(array.opt(i), component));
            }
            return result;
        }
        Collection<Object> collection = Set.class.isAssignableFrom(cls)
                ? new LinkedHashSet<>() : new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            collection.add(convert(array.opt(i), Object.class));
        }
        return collection;
    }

    private static Object objectTo(JSONObject object, Class<?> cls) {
        if (Map.class.isAssignableFrom(cls)) {
            Map<Object, Object> map = new LinkedHashMap<>();
            for (Iterator<String> it = object.keys(); it.hasNext(); ) {
                String key = it.next();
                map.put(key, convert(object.opt(key), Object.class));
            }
            return map;
        }
        try {
            Object instance = cls.getDeclaredConstructor().newInstance();
            for (Field field : cls.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) continue;
                if (!object.has(field.getName())) continue;
                field.setAccessible(true);
                field.set(instance, convert(object.opt(field.getName()), field.getGenericType()));
            }
            return instance;
        } catch (Exception e) {
            return null;
        }
    }

    private static Object convertToParameterized(Object token, ParameterizedType type) {
        Class<?> raw = (Class<?>) type.getRawType();
        Type[] args = type.getActualTypeArguments();
        if (token instanceof JSONArray) {
            if (raw == List.class || Collection.class.isAssignableFrom(raw) || Iterable.class.isAssignableFrom(raw)) {
                Collection<Object> collection = Set.class.isAssignableFrom(raw)
                        ? new LinkedHashSet<>() : new ArrayList<>();
                Type elementType = args.length > 0 ? args[0] : Object.class;
                JSONArray array = (JSONArray) token;
                for (int i = 0; i < array.length(); i++) {
                    collection.add(convert(array.opt(i), elementType));
                }
                return collection;
            }
            if (raw.isArray()) {
                return arrayTo((JSONArray) token, raw);
            }
        }
        if (token instanceof JSONObject) {
            if (Map.class.isAssignableFrom(raw)) {
                Map<Object, Object> map = new LinkedHashMap<>();
                Type keyType = args.length > 0 ? args[0] : Object.class;
                Type valueType = args.length > 1 ? args[1] : Object.class;
                JSONObject object = (JSONObject) token;
                for (Iterator<String> it = object.keys(); it.hasNext(); ) {
                    String key = it.next();
                    map.put(convertKey(key, keyType), convert(object.opt(key), valueType));
                }
                return map;
            }
            return objectTo((JSONObject) token, raw);
        }
        return null;
    }

    private static Object convertKey(String key, Type keyType) {
        if (keyType == String.class || keyType == Object.class) return key;
        if (keyType instanceof Class) return fromStringToken(key, (Class<?>) keyType);
        return key;
    }
}
