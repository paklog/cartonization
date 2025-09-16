package com.paklog.cartonization.application.service;

import com.paklog.cartonization.application.port.out.CacheStore;
import com.paklog.cartonization.application.port.out.ProductCatalogClient;
import com.paklog.cartonization.domain.model.valueobject.ItemToPack;
import com.paklog.cartonization.domain.model.valueobject.ItemWithDimensions;
import com.paklog.cartonization.domain.model.valueobject.SKU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductDimensionEnricher {

    private static final Logger log = LoggerFactory.getLogger(ProductDimensionEnricher.class);
    private static final String CACHE_PREFIX = "product";
    private static final Duration CACHE_TTL = Duration.ofHours(4);

    private final ProductCatalogClient productCatalogClient;
    private final CacheStore cacheStore;

    public ProductDimensionEnricher(ProductCatalogClient productCatalogClient, CacheStore cacheStore) {
        this.productCatalogClient = productCatalogClient;
        this.cacheStore = cacheStore;
    }

    public List<ItemWithDimensions> enrichItems(List<ItemToPack> items) {
        log.debug("Enriching {} items with product dimensions", items.size());
        
        List<ItemWithDimensions> enrichedItems = new ArrayList<>();
        
        for (ItemToPack item : items) {
            try {
                ItemWithDimensions enrichedItem = enrichItem(item);
                enrichedItems.add(enrichedItem);
            } catch (Exception e) {
                log.error("Failed to enrich item: {}", item.getSku().getValue(), e);
                throw new RuntimeException("Failed to enrich item: " + item.getSku().getValue(), e);
            }
        }
        
        log.info("Successfully enriched {} items", enrichedItems.size());
        return enrichedItems;
    }

    public ItemWithDimensions enrichItem(ItemToPack item) {
        SKU sku = item.getSku();
        
        // Try to get from cache first
        Optional<ProductCatalogClient.ProductInfo> cachedProduct = getCachedProductInfo(sku);
        if (cachedProduct.isPresent()) {
            log.debug("Using cached product info for SKU: {}", sku.getValue());
            return cachedProduct.get().toItemWithDimensions(item.getQuantity());
        }
        
        // Fetch from product catalog service
        Optional<ProductCatalogClient.ProductInfo> productInfo = productCatalogClient.getProductInfo(sku);
        
        if (productInfo.isPresent()) {
            // Cache the product info
            cacheProductInfo(sku, productInfo.get());
            log.debug("Fetched and cached product info for SKU: {}", sku.getValue());
            return productInfo.get().toItemWithDimensions(item.getQuantity());
        } else {
            log.warn("Product info not found for SKU: {}, using default values", sku.getValue());
            return createDefaultItemWithDimensions(item);
        }
    }

    public void preloadProductInfo(List<SKU> skus) {
        log.debug("Preloading product info for {} SKUs", skus.size());
        
        // Filter out SKUs that are already cached
        List<SKU> uncachedSkus = skus.stream()
            .filter(sku -> getCachedProductInfo(sku).isEmpty())
            .toList();
        
        if (uncachedSkus.isEmpty()) {
            log.debug("All SKUs are already cached");
            return;
        }
        
        // Batch fetch uncached products
        List<ProductCatalogClient.ProductInfo> products = productCatalogClient.getProductsInfo(uncachedSkus);
        
        // Cache all fetched products
        for (ProductCatalogClient.ProductInfo product : products) {
            cacheProductInfo(product.sku(), product);
        }
        
        log.info("Preloaded {} product infos", products.size());
    }

    public void invalidateCache(SKU sku) {
        String cacheKey = cacheStore.buildKey(CACHE_PREFIX, sku.getValue());
        cacheStore.delete(cacheKey);
        log.debug("Invalidated cache for SKU: {}", sku.getValue());
    }

    public void invalidateAllCache() {
        cacheStore.deleteByPattern(CACHE_PREFIX + ":*");
        log.info("Invalidated all product cache");
    }

    private Optional<ProductCatalogClient.ProductInfo> getCachedProductInfo(SKU sku) {
        try {
            String cacheKey = cacheStore.buildKey(CACHE_PREFIX, sku.getValue());
            return cacheStore.get(cacheKey, ProductCatalogClient.ProductInfo.class);
        } catch (Exception e) {
            log.warn("Failed to get cached product info for SKU: {}", sku.getValue(), e);
            return Optional.empty();
        }
    }

    private void cacheProductInfo(SKU sku, ProductCatalogClient.ProductInfo productInfo) {
        try {
            String cacheKey = cacheStore.buildKey(CACHE_PREFIX, sku.getValue());
            cacheStore.put(cacheKey, productInfo, CACHE_TTL);
        } catch (Exception e) {
            log.warn("Failed to cache product info for SKU: {}", sku.getValue(), e);
            // Don't throw exception, caching is not critical
        }
    }

    private ItemWithDimensions createDefaultItemWithDimensions(ItemToPack item) {
        // Use default dimensions when product info is not available
        log.warn("Creating default item dimensions for SKU: {}", item.getSku().getValue());
        
        return ItemWithDimensions.builder()
            .sku(item.getSku())
            .quantity(item.getQuantity())
            .dimensions(createDefaultDimensions())
            .weight(createDefaultWeight())
            .category("UNKNOWN")
            .fragile(false)
            .build();
    }

    private com.paklog.cartonization.domain.model.valueobject.DimensionSet createDefaultDimensions() {
        return new com.paklog.cartonization.domain.model.valueobject.DimensionSet(
            java.math.BigDecimal.valueOf(5.0),  // 5 inches length
            java.math.BigDecimal.valueOf(5.0),  // 5 inches width
            java.math.BigDecimal.valueOf(5.0),  // 5 inches height
            com.paklog.cartonization.domain.model.valueobject.DimensionUnit.INCHES
        );
    }

    private com.paklog.cartonization.domain.model.valueobject.Weight createDefaultWeight() {
        return new com.paklog.cartonization.domain.model.valueobject.Weight(
            java.math.BigDecimal.valueOf(1.0),  // 1 pound
            com.paklog.cartonization.domain.model.valueobject.WeightUnit.POUNDS
        );
    }
}