package com.dongyang.mock.util.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InmemoryCacheManager {
    private final Map<String, Object> cacheStore = new ConcurrentHashMap<>();
    private static final InmemoryCacheManager instance = new InmemoryCacheManager();

    private InmemoryCacheManager() {}

    public static InmemoryCacheManager getInstance() {
        return instance;
    }

    public <V> void put(String key, V value) {
        if (key == null || value == null) {
            System.err.println("Cache Key 또는 Value가 NULL입니다. 저장하지 않습니다.");
            return;
        }
        cacheStore.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <V> V get(String key, Class<V> type) {
        Object value = cacheStore.get(key);
        if (value != null && type.isInstance(value)) {
            return (V) value;
        }
        return null;
    }

    public <T> List<T> getList(String key) {
        Object value = cacheStore.get(key);
        if (value instanceof List) {
            return Collections.unmodifiableList((List<T>) value);
        }
        return Collections.emptyList();
    }
}