package com.paklog.cartonization.infrastructure.cache;

import com.paklog.cartonization.application.port.in.command.CalculatePackingSolutionCommand;
import com.paklog.cartonization.domain.model.entity.PackingSolution;
import com.paklog.cartonization.domain.model.valueobject.ItemToPack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PackingSolutionCacheService {

    private static final Logger log = LoggerFactory.getLogger(PackingSolutionCacheService.class);
    
    private final CacheService cacheService;

    public PackingSolutionCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Cacheable(value = "packing-cache", key = "#command.requestId")
    public Optional<PackingSolution> getCachedSolution(CalculatePackingSolutionCommand command) {
        // This method will be intercepted by Spring Cache
        return Optional.empty();
    }

    public void cacheSolution(CalculatePackingSolutionCommand command, PackingSolution solution) {
        String cacheKey = generateCacheKey(command);
        cacheService.put("packing-cache", cacheKey, solution);
        
        // Also cache by request ID for quick lookup
        if (command.getRequestId() != null) {
            cacheService.put("packing-cache", command.getRequestId(), solution);
        }
        
        log.debug("Cached packing solution for key: {}", cacheKey);
    }

    public Optional<PackingSolution> findCachedSolution(CalculatePackingSolutionCommand command) {
        String cacheKey = generateCacheKey(command);
        Optional<PackingSolution> solution = cacheService.get("packing-cache", cacheKey, PackingSolution.class);
        
        if (solution.isEmpty() && command.getRequestId() != null) {
            // Try by request ID as fallback
            solution = cacheService.get("packing-cache", command.getRequestId(), PackingSolution.class);
        }
        
        return solution;
    }

    public Optional<PackingSolution> findByRequestId(String requestId) {
        return cacheService.get("packing-cache", requestId, PackingSolution.class);
    }

    @CacheEvict(value = "packing-cache", allEntries = true)
    public void clearAllPackingCache() {
        log.info("Cleared all packing solution cache entries");
    }

    @CacheEvict(value = "packing-cache", key = "#requestId")
    public void evictByRequestId(String requestId) {
        log.debug("Evicted packing cache entry for request ID: {}", requestId);
    }

    public void evictByCommand(CalculatePackingSolutionCommand command) {
        String cacheKey = generateCacheKey(command);
        cacheService.evict("packing-cache", cacheKey);
        
        if (command.getRequestId() != null) {
            cacheService.evict("packing-cache", command.getRequestId());
        }
        
        log.debug("Evicted packing cache entries for command: {}", cacheKey);
    }

    public boolean isCached(CalculatePackingSolutionCommand command) {
        String cacheKey = generateCacheKey(command);
        return cacheService.exists("packing-cache", cacheKey) ||
               (command.getRequestId() != null && cacheService.exists("packing-cache", command.getRequestId()));
    }

    private String generateCacheKey(CalculatePackingSolutionCommand command) {
        // Create a deterministic cache key based on command parameters
        StringBuilder keyBuilder = new StringBuilder();
        
        // Add order ID if available
        if (command.getOrderId() != null) {
            keyBuilder.append("order:").append(command.getOrderId()).append("|");
        }
        
        // Add sorted items for consistency
        String itemsKey = command.getItems().stream()
            .sorted((a, b) -> a.getSku().getValue().compareTo(b.getSku().getValue()))
            .map(item -> item.getSku().getValue() + ":" + item.getQuantity())
            .collect(Collectors.joining(","));
        keyBuilder.append("items:").append(itemsKey);
        
        // Add optimization flags
        keyBuilder.append("|minBoxes:").append(command.isOptimizeForMinimumBoxes());
        keyBuilder.append("|mixedCat:").append(command.isAllowMixedCategories());
        
        String cacheKey = keyBuilder.toString();
        
        // Use hash if key is too long
        if (cacheKey.length() > 250) {
            cacheKey = "hash:" + Math.abs(cacheKey.hashCode());
        }
        
        return cacheKey;
    }

    public void warmUpCache(List<CalculatePackingSolutionCommand> commonCommands) {
        log.info("Starting cache warm-up for {} common packing commands", commonCommands.size());
        
        // This would typically be called during startup or by a scheduled job
        // to pre-populate cache with common packing scenarios
        
        commonCommands.parallelStream()
            .forEach(command -> {
                try {
                    if (!isCached(command)) {
                        // This would trigger actual calculation and caching
                        log.debug("Pre-calculating solution for cache key: {}", generateCacheKey(command));
                        // Actual implementation would call the packing service
                    }
                } catch (Exception e) {
                    log.warn("Failed to warm up cache for command: {}", generateCacheKey(command), e);
                }
            });
        
        log.info("Cache warm-up completed");
    }

    public CacheStats getCacheStats() {
        // This would require access to Redis statistics or cache metrics
        // For now, return basic info
        return new CacheStats(
            cacheService.getCacheNames().contains("packing-cache"),
            "packing-cache",
            0L, // hit count - would need metrics integration
            0L, // miss count - would need metrics integration
            0L  // eviction count - would need metrics integration
        );
    }

    public record CacheStats(
        boolean enabled,
        String cacheName,
        long hitCount,
        long missCount,
        long evictionCount
    ) {}
}