package com.hazelcast.jet.contrib.socketlistenersource;

import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.jet.function.FunctionEx;
import com.hazelcast.jet.impl.util.ExceptionUtil;
import com.hazelcast.query.impl.getters.AbstractJsonGetter;
import com.hazelcast.query.impl.getters.JsonGetter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class JsonUtil {
    private static final Method GET_VALUE_METHOD = getValueMethod();

    private static Method getValueMethod() {
        try {
            Method getValueMethod = AbstractJsonGetter.class.getDeclaredMethod("getValue", Object.class, String.class);
            getValueMethod.setAccessible(true);
            return getValueMethod;
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    private JsonUtil() {

    }

    public static <T> FunctionEx<HazelcastJsonValue, T> attributeExtractor(String attributePath) {
        return (jsonValue -> extractAttribute(jsonValue, attributePath));
    }

    private static <T> T extractAttribute(HazelcastJsonValue jsonValue, String attributePath) {
        try {
            return (T)GET_VALUE_METHOD.invoke(JsonGetter.INSTANCE, jsonValue, attributePath);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
}
