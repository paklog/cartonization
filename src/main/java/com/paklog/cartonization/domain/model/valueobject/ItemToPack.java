package com.paklog.cartonization.domain.model.valueobject;

import java.util.Objects;

public final class ItemToPack {
    private final SKU sku;
    private final Integer quantity;

    public ItemToPack(SKU sku, Integer quantity) {
        if (sku == null) {
            throw new IllegalArgumentException("SKU cannot be null");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.sku = sku;
        this.quantity = quantity;
    }

    public static ItemToPack of(String sku, Integer quantity) {
        return new ItemToPack(SKU.of(sku), quantity);
    }

    public SKU getSku() {
        return sku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemToPack that = (ItemToPack) o;
        return Objects.equals(sku, that.sku) && Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku, quantity);
    }

    @Override
    public String toString() {
        return "ItemToPack{" +
               "sku=" + sku +
               ", quantity=" + quantity +
               '}';
    }
}