package com.eitraz.automation.scripts;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class LocalCache {
    private Map<String, Object> cache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Supplier<T> supplier) {
        T value = get(key);
        if (value == null) {
            value = supplier.get();
            cache.put(key, value);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) cache.get(key);
    }

    public <T> void set(String key, T value) {
        cache.put(key, value);
    }
}
