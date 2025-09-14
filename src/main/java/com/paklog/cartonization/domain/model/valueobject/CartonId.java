package com.paklog.cartonization.domain.model.valueobject;

import lombok.Value;

import java.util.UUID;

@Value
public class CartonId {
    String value;

    private CartonId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("CartonId cannot be null or empty");
        }
        this.value = value;
    }

    public static CartonId of(String value) {
        return new CartonId(value);
    }

    public static CartonId generate() {
        return new CartonId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}