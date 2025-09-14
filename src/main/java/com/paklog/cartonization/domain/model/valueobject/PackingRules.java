package com.paklog.cartonization.domain.model.valueobject;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class PackingRules {
    Boolean optimizeForMinimumBoxes;
    Boolean allowMixedCategories;
    Boolean separateFragileItems;
    BigDecimal maxUtilizationThreshold;

    public boolean shouldOptimizeForMinimumBoxes() {
        return Boolean.TRUE.equals(optimizeForMinimumBoxes);
    }

    public boolean shouldAllowMixedCategories() {
        return Boolean.TRUE.equals(allowMixedCategories);
    }

    public boolean shouldSeparateFragileItems() {
        return Boolean.TRUE.equals(separateFragileItems);
    }

    public BigDecimal getMaxUtilizationThreshold() {
        return maxUtilizationThreshold != null ? maxUtilizationThreshold : BigDecimal.valueOf(0.95);
    }

    public static PackingRules defaultRules() {
        return PackingRules.builder()
            .optimizeForMinimumBoxes(true)
            .allowMixedCategories(true)
            .separateFragileItems(true)
            .maxUtilizationThreshold(BigDecimal.valueOf(0.95))
            .build();
    }
}