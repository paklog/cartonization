package com.paklog.cartonization.application.port.out;

import com.paklog.cartonization.domain.model.valueobject.DimensionSet;
import com.paklog.cartonization.domain.model.valueobject.ItemWithDimensions;
import com.paklog.cartonization.domain.model.valueobject.SKU;
import com.paklog.cartonization.domain.model.valueobject.Weight;

import java.util.List;
import java.util.Optional;

public interface ProductCatalogClient {

    Optional<ProductInfo> getProductInfo(SKU sku);

    List<ProductInfo> getProductsInfo(List<SKU> skus);

    Optional<DimensionSet> getProductDimensions(SKU sku);

    Optional<Weight> getProductWeight(SKU sku);

    Optional<String> getProductCategory(SKU sku);

    boolean isProductFragile(SKU sku);

    boolean isProductActive(SKU sku);

    List<SKU> findProductsBySimilarDimensions(DimensionSet dimensions, double tolerancePercentage);

    record ProductInfo(
        SKU sku,
        String name,
        String description,
        DimensionSet dimensions,
        Weight weight,
        String category,
        boolean fragile,
        boolean active,
        String barcode
    ) {
        public ItemWithDimensions toItemWithDimensions(Integer quantity) {
            return ItemWithDimensions.builder()
                .sku(sku)
                .quantity(quantity)
                .dimensions(dimensions)
                .weight(weight)
                .category(category)
                .fragile(fragile)
                .build();
        }
    }
}