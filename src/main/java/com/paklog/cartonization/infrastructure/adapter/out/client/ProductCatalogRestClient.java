package com.paklog.cartonization.infrastructure.adapter.out.client;

import com.paklog.cartonization.application.port.out.ProductCatalogClient;
import com.paklog.cartonization.domain.model.valueobject.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductCatalogRestClient implements ProductCatalogClient {

    private static final Logger log = LoggerFactory.getLogger(ProductCatalogRestClient.class);
    private static final String CIRCUIT_BREAKER_NAME = "productCatalog";
    
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ProductCatalogRestClient(RestTemplate restTemplate, 
                                  @Value("${integration.product-catalog.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getProductInfoFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    @Cacheable(value = "product-by-sku", key = "#sku.value")
    public Optional<ProductInfo> getProductInfo(SKU sku) {
        try {
            log.debug("Fetching product info for SKU: {}", sku.getValue());
            
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/v1/products/{sku}")
                .buildAndExpand(sku.getValue())
                .toUriString();

            ResponseEntity<ProductCatalogResponse> response = restTemplate.getForEntity(url, ProductCatalogResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ProductInfo productInfo = mapToProductInfo(response.getBody());
                log.debug("Successfully fetched product info for SKU: {}", sku.getValue());
                return Optional.of(productInfo);
            }
            
            log.warn("Product not found for SKU: {}", sku.getValue());
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Failed to fetch product info for SKU: {}", sku.getValue(), e);
            throw new RuntimeException("Failed to fetch product info", e);
        }
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getProductsInfoFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public List<ProductInfo> getProductsInfo(List<SKU> skus) {
        try {
            log.debug("Fetching product info for {} SKUs", skus.size());
            
            String skuList = skus.stream()
                .map(SKU::getValue)
                .collect(Collectors.joining(","));
                
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/v1/products/batch")
                .queryParam("skus", skuList)
                .toUriString();

            ResponseEntity<ProductCatalogResponse[]> response = restTemplate.getForEntity(url, ProductCatalogResponse[].class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<ProductInfo> products = List.of(response.getBody()).stream()
                    .map(this::mapToProductInfo)
                    .collect(Collectors.toList());
                    
                log.debug("Successfully fetched {} product infos", products.size());
                return products;
            }
            
            log.warn("No products found for provided SKUs");
            return List.of();
            
        } catch (Exception e) {
            log.error("Failed to fetch products info", e);
            throw new RuntimeException("Failed to fetch products info", e);
        }
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getProductDimensionsFallback")
    @Cacheable(value = "product-dimensions", key = "#sku.value")
    public Optional<DimensionSet> getProductDimensions(SKU sku) {
        return getProductInfo(sku).map(ProductInfo::dimensions);
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getProductWeightFallback")
    public Optional<Weight> getProductWeight(SKU sku) {
        return getProductInfo(sku).map(ProductInfo::weight);
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getProductCategoryFallback")
    public Optional<String> getProductCategory(SKU sku) {
        return getProductInfo(sku).map(ProductInfo::category);
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "isProductFragileFallback")
    public boolean isProductFragile(SKU sku) {
        return getProductInfo(sku)
            .map(ProductInfo::fragile)
            .orElse(false);
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "isProductActiveFallback")
    public boolean isProductActive(SKU sku) {
        return getProductInfo(sku)
            .map(ProductInfo::active)
            .orElse(false);
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "findProductsBySimilarDimensionsFallback")
    public List<SKU> findProductsBySimilarDimensions(DimensionSet dimensions, double tolerancePercentage) {
        try {
            log.debug("Finding products with similar dimensions to: {}", dimensions);
            
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/v1/products/similar-dimensions")
                .queryParam("length", dimensions.getLength())
                .queryParam("width", dimensions.getWidth())
                .queryParam("height", dimensions.getHeight())
                .queryParam("unit", dimensions.getUnit())
                .queryParam("tolerance", tolerancePercentage)
                .toUriString();

            ResponseEntity<String[]> response = restTemplate.getForEntity(url, String[].class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<SKU> similarProducts = List.of(response.getBody()).stream()
                    .map(SKU::of)
                    .collect(Collectors.toList());
                    
                log.debug("Found {} products with similar dimensions", similarProducts.size());
                return similarProducts;
            }
            
            return List.of();
            
        } catch (Exception e) {
            log.error("Failed to find products with similar dimensions", e);
            return List.of();
        }
    }

    // Fallback methods
    public Optional<ProductInfo> getProductInfoFallback(SKU sku, Exception ex) {
        log.warn("Using fallback for getProductInfo, SKU: {}, error: {}", sku.getValue(), ex.getMessage());
        return Optional.empty();
    }

    public List<ProductInfo> getProductsInfoFallback(List<SKU> skus, Exception ex) {
        log.warn("Using fallback for getProductsInfo, SKUs count: {}, error: {}", skus.size(), ex.getMessage());
        return List.of();
    }

    public Optional<DimensionSet> getProductDimensionsFallback(SKU sku, Exception ex) {
        log.warn("Using fallback for getProductDimensions, SKU: {}, error: {}", sku.getValue(), ex.getMessage());
        return Optional.empty();
    }

    public Optional<Weight> getProductWeightFallback(SKU sku, Exception ex) {
        log.warn("Using fallback for getProductWeight, SKU: {}, error: {}", sku.getValue(), ex.getMessage());
        return Optional.empty();
    }

    public Optional<String> getProductCategoryFallback(SKU sku, Exception ex) {
        log.warn("Using fallback for getProductCategory, SKU: {}, error: {}", sku.getValue(), ex.getMessage());
        return Optional.empty();
    }

    public boolean isProductFragileFallback(SKU sku, Exception ex) {
        log.warn("Using fallback for isProductFragile, SKU: {}, error: {}", sku.getValue(), ex.getMessage());
        return false; // Safe default
    }

    public boolean isProductActiveFallback(SKU sku, Exception ex) {
        log.warn("Using fallback for isProductActive, SKU: {}, error: {}", sku.getValue(), ex.getMessage());
        return true; // Optimistic default
    }

    public List<SKU> findProductsBySimilarDimensionsFallback(DimensionSet dimensions, double tolerancePercentage, Exception ex) {
        log.warn("Using fallback for findProductsBySimilarDimensions, error: {}", ex.getMessage());
        return List.of();
    }

    private ProductInfo mapToProductInfo(ProductCatalogResponse response) {
        DimensionSet dimensions = new DimensionSet(
            response.dimensions().length(),
            response.dimensions().width(),
            response.dimensions().height(),
            DimensionUnit.valueOf(response.dimensions().unit().toUpperCase())
        );
        
        Weight weight = new Weight(
            response.weight().value(),
            WeightUnit.valueOf(response.weight().unit().toUpperCase())
        );
        
        return new ProductInfo(
            SKU.of(response.sku()),
            response.name(),
            response.description(),
            dimensions,
            weight,
            response.category(),
            response.fragile(),
            response.active(),
            response.barcode()
        );
    }

    // DTOs for external API communication
    public record ProductCatalogResponse(
        String sku,
        String name,
        String description,
        DimensionResponse dimensions,
        WeightResponse weight,
        String category,
        boolean fragile,
        boolean active,
        String barcode
    ) {}

    public record DimensionResponse(
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        String unit
    ) {}

    public record WeightResponse(
        BigDecimal value,
        String unit
    ) {}
}