package com.paklog.cartonization.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Service to handle idempotency of message processing.
 * Prevents duplicate processing of Kafka messages in case of retries or rebalancing.
 */
@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final RedisTemplate<String, String> redisTemplate;

    public IdempotencyService(@Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Checks if a request with the given ID has already been processed.
     *
     * @param requestId the unique request identifier
     * @return true if the request has been processed, false otherwise
     */
    public boolean isProcessed(String requestId) {
        try {
            String key = IDEMPOTENCY_KEY_PREFIX + requestId;
            Boolean exists = redisTemplate.hasKey(key);

            if (Boolean.TRUE.equals(exists)) {
                log.debug("Request {} has already been processed", requestId);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.warn("Failed to check idempotency for request: {}, assuming not processed", requestId, e);
            // Fail open - if Redis is down, allow processing to prevent blocking
            return false;
        }
    }

    /**
     * Marks a request as processed with the default TTL (24 hours).
     *
     * @param requestId the unique request identifier
     */
    public void markProcessed(String requestId) {
        markProcessed(requestId, DEFAULT_TTL);
    }

    /**
     * Marks a request as processed with a custom TTL.
     *
     * @param requestId the unique request identifier
     * @param ttl the time-to-live for the idempotency record
     */
    public void markProcessed(String requestId, Duration ttl) {
        try {
            String key = IDEMPOTENCY_KEY_PREFIX + requestId;
            String value = Instant.now().toString();
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Marked request {} as processed with TTL {}", requestId, ttl);
        } catch (Exception e) {
            log.error("Failed to mark request {} as processed - duplicate processing may occur", requestId, e);
            // Don't throw - this is a best-effort idempotency check
        }
    }

    /**
     * Attempts to acquire an idempotency lock atomically.
     * Returns true if this is the first time seeing this request ID.
     *
     * @param requestId the unique request identifier
     * @return true if the lock was acquired (first processor), false if already processed
     */
    public boolean tryAcquireIdempotencyLock(String requestId) {
        return tryAcquireIdempotencyLock(requestId, DEFAULT_TTL);
    }

    /**
     * Attempts to acquire an idempotency lock atomically with custom TTL.
     *
     * @param requestId the unique request identifier
     * @param ttl the time-to-live for the idempotency record
     * @return true if the lock was acquired (first processor), false if already processed
     */
    public boolean tryAcquireIdempotencyLock(String requestId, Duration ttl) {
        try {
            String key = IDEMPOTENCY_KEY_PREFIX + requestId;
            String value = Instant.now().toString();

            // SET if not exists (atomic operation)
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, value, ttl);

            if (Boolean.TRUE.equals(acquired)) {
                log.debug("Acquired idempotency lock for request: {}", requestId);
                return true;
            } else {
                log.debug("Request {} already being processed or was processed", requestId);
                return false;
            }
        } catch (Exception e) {
            log.warn("Failed to acquire idempotency lock for request: {}, allowing processing", requestId, e);
            // Fail open - if Redis is down, allow processing
            return true;
        }
    }

    /**
     * Removes the idempotency record for a request.
     * Useful for manual retry scenarios.
     *
     * @param requestId the unique request identifier
     */
    public void clearIdempotency(String requestId) {
        try {
            String key = IDEMPOTENCY_KEY_PREFIX + requestId;
            redisTemplate.delete(key);
            log.debug("Cleared idempotency record for request: {}", requestId);
        } catch (Exception e) {
            log.error("Failed to clear idempotency for request: {}", requestId, e);
        }
    }
}
