package com.paklog.cartonization.domain.model.valueobject;

import lombok.Value;

@Value
public class ItemToPack {
    SKU sku;
    Integer quantity;

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
}