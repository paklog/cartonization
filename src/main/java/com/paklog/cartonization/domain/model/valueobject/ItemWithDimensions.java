package com.paklog.cartonization.domain.model.valueobject;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ItemWithDimensions {
    SKU sku;
    Integer quantity;
    DimensionSet dimensions;
    Weight weight;
    String category;
    Boolean fragile;

    public BigDecimal getTotalVolume() {
        return dimensions.volume().multiply(BigDecimal.valueOf(quantity));
    }

    public Weight getTotalWeight() {
        // For simplicity, assume all items of same SKU have same weight
        // In reality, this might need more complex logic
        return new Weight(
            weight.getValue().multiply(BigDecimal.valueOf(quantity)),
            weight.getUnit()
        );
    }

    public boolean isFragile() {
        return Boolean.TRUE.equals(fragile);
    }
}