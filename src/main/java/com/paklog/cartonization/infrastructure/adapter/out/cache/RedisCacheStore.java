package com.paklog.cartonization.infrastructure.adapter.out.cache;

import com.paklog.cartonization.application.port.out.CacheStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Component
public class RedisCacheStore implements CacheStore {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheStore.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public <T> void put(String key, T value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Stored value in cache with key: {}", key);
        } catch (Exception e) {
            log.error("Failed to store value in cache with key: {}", key, e);
            throw new RuntimeException("Cache store operation failed", e);
        }
    }

    @Override
    public <T> void put(String key, T value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Stored value in cache with key: {} and TTL: {}", key, ttl);
        } catch (Exception e) {
            log.error("Failed to store value in cache with key: {} and TTL: {}", key, ttl, e);
            throw new RuntimeException("Cache store operation failed", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.debug("No value found in cache for key: {}", key);
                return Optional.empty();
            }
            
            if (type.isAssignableFrom(value.getClass())) {
                log.debug("Retrieved value from cache for key: {}", key);
                return Optional.of((T) value);
            } else {
                log.warn("Type mismatch for cached value with key: {}. Expected: {}, Actual: {}", 
                        key, type.getSimpleName(), value.getClass().getSimpleName());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Failed to retrieve value from cache with key: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            boolean result = exists != null && exists;
            log.debug("Key existence check for {}: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to check key existence for: {}", key, e);
            return false;
        }
    }

    @Override
    public void delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            boolean result = deleted != null && deleted;
            log.debug("Deleted key from cache: {} - Success: {}", key, result);
        } catch (Exception e) {
            log.error("Failed to delete key from cache: {}", key, e);
            throw new RuntimeException("Cache delete operation failed", e);
        }
    }

    @Override
    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keys);
                log.debug("Deleted {} keys matching pattern: {}", deletedCount, pattern);
            } else {
                log.debug("No keys found matching pattern: {}", pattern);
            }
        } catch (Exception e) {
            log.error("Failed to delete keys by pattern: {}", pattern, e);
            throw new RuntimeException("Cache delete by pattern operation failed", e);
        }
    }

    @Override
    public void clear() {
        try {
            // Note: This clears ALL keys in the current database, use with caution
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            log.info("Cleared all keys from cache database");
        } catch (Exception e) {
            log.error("Failed to clear cache database", e);
            throw new RuntimeException("Cache clear operation failed", e);
        }
    }

    @Override
    public long size() {
        try {
            Long dbSize = redisTemplate.getConnectionFactory().getConnection().dbSize();
            long result = dbSize != null ? dbSize : 0L;
            log.debug("Cache database size: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get cache database size", e);
            return 0L;
        }
    }
}