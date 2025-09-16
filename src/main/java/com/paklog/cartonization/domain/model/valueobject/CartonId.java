package com.paklog.cartonization.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class CartonId {
    private final String value;

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

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartonId cartonId = (CartonId) o;
        return Objects.equals(value, cartonId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}