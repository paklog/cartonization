package com.paklog.cartonization.application.service;

import com.paklog.cartonization.application.port.out.ProductCatalogClient;
import com.paklog.cartonization.domain.model.valueobject.DimensionSet;
import com.paklog.cartonization.domain.model.valueobject.ItemToPack;
import com.paklog.cartonization.domain.model.valueobject.ItemWithDimensions;
import com.paklog.cartonization.domain.model.valueobject.Weight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DefaultProductDimensionEnricher implements ProductDimensionEnricher {

    private static final Logger log = LoggerFactory.getLogger(DefaultProductDimensionEnricher.class);

    private final ProductCatalogClient productCatalogClient;

    public DefaultProductDimensionEnricher(ProductCatalogClient productCatalogClient) {
        this.productCatalogClient = productCatalogClient;
    }

    @Override
    public List<ItemWithDimensions> enrichItems(List<ItemToPack> items) {
        return items.stream()
            .map(this::enrichItem)
            .collect(Collectors.toList());
    }

    private ItemWithDimensions enrichItem(ItemToPack item) {
        try {
            // Fetch product info from catalog
            var productInfo = productCatalogClient.getProductInfo(item.getSku())
                .orElseThrow(() -> new IllegalStateException(
                    "Product not found for SKU: " + item.getSku().getValue()));

            return productInfo.toItemWithDimensions(item.getQuantity());
        } catch (Exception e) {
            log.error("Failed to enrich item with SKU: {}", item.getSku().getValue(), e);
            throw new IllegalStateException("Failed to enrich item dimensions for SKU: " + item.getSku().getValue(), e);
        }
    }
}
