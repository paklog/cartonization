package com.paklog.cartonization.domain.model.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

public final class PackingRules {
    private final Boolean optimizeForMinimumBoxes;
    private final Boolean allowMixedCategories;
    private final Boolean separateFragileItems;
    private final BigDecimal maxUtilizationThreshold;

    private PackingRules(Boolean optimizeForMinimumBoxes, Boolean allowMixedCategories, Boolean separateFragileItems, BigDecimal maxUtilizationThreshold) {
        this.optimizeForMinimumBoxes = optimizeForMinimumBoxes;
        this.allowMixedCategories = allowMixedCategories;
        this.separateFragileItems = separateFragileItems;
        this.maxUtilizationThreshold = maxUtilizationThreshold;
    }

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

    public Boolean getOptimizeForMinimumBoxes() {
        return optimizeForMinimumBoxes;
    }

    public Boolean getAllowMixedCategories() {
        return allowMixedCategories;
    }

    public Boolean getSeparateFragileItems() {
        return separateFragileItems;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackingRules that = (PackingRules) o;
        return Objects.equals(optimizeForMinimumBoxes, that.optimizeForMinimumBoxes) &&
               Objects.equals(allowMixedCategories, that.allowMixedCategories) &&
               Objects.equals(separateFragileItems, that.separateFragileItems) &&
               Objects.equals(maxUtilizationThreshold, that.maxUtilizationThreshold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optimizeForMinimumBoxes, allowMixedCategories, separateFragileItems, maxUtilizationThreshold);
    }

    @Override
    public String toString() {
        return "PackingRules{" +
               "optimizeForMinimumBoxes=" + optimizeForMinimumBoxes +
               ", allowMixedCategories=" + allowMixedCategories +
               ", separateFragileItems=" + separateFragileItems +
               ", maxUtilizationThreshold=" + maxUtilizationThreshold +
               '}';
    }

    public static class Builder {
        private Boolean optimizeForMinimumBoxes;
        private Boolean allowMixedCategories;
        private Boolean separateFragileItems;
        private BigDecimal maxUtilizationThreshold;

        public Builder optimizeForMinimumBoxes(Boolean optimizeForMinimumBoxes) {
            this.optimizeForMinimumBoxes = optimizeForMinimumBoxes;
            return this;
        }

        public Builder allowMixedCategories(Boolean allowMixedCategories) {
            this.allowMixedCategories = allowMixedCategories;
            return this;
        }

        public Builder separateFragileItems(Boolean separateFragileItems) {
            this.separateFragileItems = separateFragileItems;
            return this;
        }

        public Builder maxUtilizationThreshold(BigDecimal maxUtilizationThreshold) {
            this.maxUtilizationThreshold = maxUtilizationThreshold;
            return this;
        }

        public PackingRules build() {
            return new PackingRules(optimizeForMinimumBoxes, allowMixedCategories, separateFragileItems, maxUtilizationThreshold);
        }
    }
}