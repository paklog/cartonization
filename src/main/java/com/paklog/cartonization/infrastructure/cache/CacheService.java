package com.paklog.cartonization.infrastructure.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);
    
    private final CacheManager cacheManager;

    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public <T> Optional<T> get(String cacheName, Object key, Class<T> type) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(key);
                if (wrapper != null) {
                    Object value = wrapper.get();
                    if (type.isInstance(value)) {
                        log.debug("Cache hit for cache: {}, key: {}", cacheName, key);
                        return Optional.of(type.cast(value));
                    }
                }
            }
            log.debug("Cache miss for cache: {}, key: {}", cacheName, key);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error retrieving from cache: {}, key: {}", cacheName, key, e);
            return Optional.empty();
        }
    }

    public <T> T get(String cacheName, Object key, Callable<T> valueLoader) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                return cache.get(key, valueLoader);
            }
            return valueLoader.call();
        } catch (Exception e) {
            log.error("Error retrieving/loading cache value for cache: {}, key: {}", cacheName, key, e);
            try {
                return valueLoader.call();
            } catch (Exception ex) {
                log.error("Error executing value loader", ex);
                throw new RuntimeException("Failed to load value", ex);
            }
        }
    }

    public void put(String cacheName, Object key, Object value) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
                log.debug("Cached value for cache: {}, key: {}", cacheName, key);
            }
        } catch (Exception e) {
            log.error("Error putting value to cache: {}, key: {}", cacheName, key, e);
        }
    }

    public void evict(String cacheName, Object key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
                log.debug("Evicted cache entry for cache: {}, key: {}", cacheName, key);
            }
        } catch (Exception e) {
            log.error("Error evicting from cache: {}, key: {}", cacheName, key, e);
        }
    }

    public void clear(String cacheName) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("Cleared cache: {}", cacheName);
            }
        } catch (Exception e) {
            log.error("Error clearing cache: {}", cacheName, e);
        }
    }

    public void clearAll() {
        try {
            cacheManager.getCacheNames().forEach(this::clear);
            log.info("Cleared all caches");
        } catch (Exception e) {
            log.error("Error clearing all caches", e);
        }
    }

    public Set<String> getCacheNames() {
        return Set.copyOf(cacheManager.getCacheNames());
    }

    public boolean exists(String cacheName, Object key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                return cache.get(key) != null;
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking cache existence for cache: {}, key: {}", cacheName, key, e);
            return false;
        }
    }
}