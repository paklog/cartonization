package com.paklog.cartonization.infrastructure.cache;

import com.paklog.cartonization.application.port.out.CartonRepository;
import com.paklog.cartonization.application.port.out.ProductCatalogClient;
import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.valueobject.SKU;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class CacheWarmupService {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmupService.class);

    private final CartonRepository cartonRepository;
    private final ProductCatalogClient productCatalogClient;
    private final CacheService cacheService;
    
    private boolean warmupEnabled;
    
    private List<String> commonSkus;

    public CacheWarmupService(CartonRepository cartonRepository,
                            ProductCatalogClient productCatalogClient,
                            CacheService cacheService) {
        this.cartonRepository = cartonRepository;
        this.productCatalogClient = productCatalogClient;
        this.cacheService = cacheService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void warmupCachesOnStartup() {
        if (!warmupEnabled) {
            log.info("Cache warmup is disabled");
            return;
        }

        log.info("Starting cache warmup process...");
        
        CompletableFuture<Void> cartonWarmup = warmupCartonCache();
        CompletableFuture<Void> productWarmup = warmupProductCache();
        
        CompletableFuture.allOf(cartonWarmup, productWarmup)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Cache warmup completed with errors", throwable);
                } else {
                    log.info("Cache warmup completed successfully");
                }
            });
    }

    @Async
    public CompletableFuture<Void> warmupCartonCache() {
        try {
            log.debug("Warming up carton cache...");
            
            // Preload active cartons
            List<Carton> activeCartons = cartonRepository.findAllActive();
            log.debug("Preloaded {} active cartons", activeCartons.size());
            
            // Preload all cartons
            List<Carton> allCartons = cartonRepository.findAll();
            log.debug("Preloaded {} total cartons", allCartons.size());
            
            log.info("Carton cache warmup completed - {} active cartons, {} total cartons", 
                    activeCartons.size(), allCartons.size());
            
            return CompletableFuture.completedFuture(null);
            
        } catch (Exception e) {
            log.error("Error during carton cache warmup", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public CompletableFuture<Void> warmupProductCache() {
        try {
            log.debug("Warming up product cache...");
            
            if (commonSkus == null || commonSkus.isEmpty()) {
                log.debug("No common SKUs configured for product cache warmup");
                return CompletableFuture.completedFuture(null);
            }
            
            List<SKU> skus = commonSkus.stream()
                .map(SKU::of)
                .toList();
            
            // Warm up product info cache
            skus.parallelStream()
                .forEach(sku -> {
                    try {
                        productCatalogClient.getProductInfo(sku);
                        productCatalogClient.getProductDimensions(sku);
                        log.debug("Warmed up product cache for SKU: {}", sku.getValue());
                    } catch (Exception e) {
                        log.warn("Failed to warm up product cache for SKU: {}", sku.getValue(), e);
                    }
                });
            
            log.info("Product cache warmup completed for {} SKUs", skus.size());
            
            return CompletableFuture.completedFuture(null);
            
        } catch (Exception e) {
            log.error("Error during product cache warmup", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public CompletableFuture<Void> refreshCache(String cacheName) {
        try {
            log.info("Refreshing cache: {}", cacheName);
            
            cacheService.clear(cacheName);
            
            switch (cacheName) {
                case "cartons", "active-cartons", "carton-by-id" -> {
                    warmupCartonCache().get();
                }
                case "products", "product-by-sku", "product-dimensions" -> {
                    warmupProductCache().get();
                }
                case "packing-solutions", "packing-cache" -> {
                    // Packing solution cache is populated on-demand
                    log.debug("Packing solution cache cleared, will be populated on-demand");
                }
                default -> {
                    log.warn("Unknown cache name for refresh: {}", cacheName);
                }
            }
            
            log.info("Cache refresh completed for: {}", cacheName);
            return CompletableFuture.completedFuture(null);
            
        } catch (Exception e) {
            log.error("Error refreshing cache: {}", cacheName, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    public void refreshAllCaches() {
        log.info("Refreshing all caches...");
        
        cacheService.clearAll();
        warmupCachesOnStartup();
    }

    public CacheWarmupStatus getWarmupStatus() {
        boolean cartonsCached = cacheService.exists("active-cartons", "warmup-marker") ||
                              !cacheService.get("active-cartons", "test-key", Object.class).isEmpty();
        
        boolean productsCached = commonSkus != null && !commonSkus.isEmpty() &&
                               cacheService.exists("product-by-sku", commonSkus.get(0));
        
        return new CacheWarmupStatus(
            warmupEnabled,
            cartonsCached,
            productsCached,
            cacheService.getCacheNames()
        );
    }

    public record CacheWarmupStatus(
        boolean enabled,
        boolean cartonsCached,
        boolean productsCached,
        java.util.Set<String> availableCaches
    ) {}
}