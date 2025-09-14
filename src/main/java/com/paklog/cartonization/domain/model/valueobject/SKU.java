package com.paklog.cartonization.domain.model.valueobject;

import lombok.Value;

@Value
public class SKU {
    String value;

    public SKU(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
        this.value = value;
    }

    public static SKU of(String value) {
        return new SKU(value);
    }

    @Override
    public String toString() {
        return value;
    }
}