package com.paklog.cartonization.domain.model.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

public final class ItemWithDimensions {
    private final SKU sku;
    private final Integer quantity;
    private final DimensionSet dimensions;
    private final Weight weight;
    private final String category;
    private final Boolean fragile;

    private ItemWithDimensions(SKU sku, Integer quantity, DimensionSet dimensions, Weight weight, String category, Boolean fragile) {
        this.sku = sku;
        this.quantity = quantity;
        this.dimensions = dimensions;
        this.weight = weight;
        this.category = category;
        this.fragile = fragile;
    }

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

    public SKU getSku() {
        return sku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public DimensionSet getDimensions() {
        return dimensions;
    }

    public Weight getWeight() {
        return weight;
    }

    public String getCategory() {
        return category;
    }

    public Boolean getFragile() {
        return fragile;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemWithDimensions that = (ItemWithDimensions) o;
        return Objects.equals(sku, that.sku) &&
               Objects.equals(quantity, that.quantity) &&
               Objects.equals(dimensions, that.dimensions) &&
               Objects.equals(weight, that.weight) &&
               Objects.equals(category, that.category) &&
               Objects.equals(fragile, that.fragile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku, quantity, dimensions, weight, category, fragile);
    }

    @Override
    public String toString() {
        return "ItemWithDimensions{" +
               "sku=" + sku +
               ", quantity=" + quantity +
               ", dimensions=" + dimensions +
               ", weight=" + weight +
               ", category='" + category + '\'' +
               ", fragile=" + fragile +
               '}';
    }

    public static class Builder {
        private SKU sku;
        private Integer quantity;
        private DimensionSet dimensions;
        private Weight weight;
        private String category;
        private Boolean fragile;

        public Builder sku(SKU sku) {
            this.sku = sku;
            return this;
        }

        public Builder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder dimensions(DimensionSet dimensions) {
            this.dimensions = dimensions;
            return this;
        }

        public Builder weight(Weight weight) {
            this.weight = weight;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder fragile(Boolean fragile) {
            this.fragile = fragile;
            return this;
        }

        public ItemWithDimensions build() {
            return new ItemWithDimensions(sku, quantity, dimensions, weight, category, fragile);
        }
    }
}