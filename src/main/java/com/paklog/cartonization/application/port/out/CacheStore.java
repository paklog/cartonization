package com.paklog.cartonization.application.port.out;

import java.time.Duration;
import java.util.Optional;

public interface CacheStore {

    <T> void put(String key, T value);

    <T> void put(String key, T value, Duration ttl);

    <T> Optional<T> get(String key, Class<T> type);

    boolean exists(String key);

    void delete(String key);

    void deleteByPattern(String pattern);

    void clear();

    long size();

    default String buildKey(String prefix, Object... parts) {
        StringBuilder keyBuilder = new StringBuilder(prefix);
        for (Object part : parts) {
            keyBuilder.append(":").append(part);
        }
        return keyBuilder.toString();
    }
}