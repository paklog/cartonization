package com.paklog.cartonization.domain.model.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Weight {
    private final BigDecimal value;
    private final WeightUnit unit;

    private static final BigDecimal POUNDS_TO_KG = new BigDecimal("0.453592");

    public Weight(BigDecimal value, WeightUnit unit) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Weight value must be positive");
        }
        this.value = value;
        this.unit = unit;
    }

    public Weight convertTo(WeightUnit targetUnit) {
        if (this.unit == targetUnit) {
            return this;
        }

        BigDecimal conversionFactor = getConversionFactor(this.unit, targetUnit);
        return new Weight(value.multiply(conversionFactor), targetUnit);
    }

    public boolean isGreaterThan(Weight other) {
        Weight normalizedOther = other.convertTo(this.unit);
        return this.value.compareTo(normalizedOther.value) > 0;
    }

    public boolean isZeroOrNegative() {
        return value.compareTo(BigDecimal.ZERO) <= 0;
    }

    private BigDecimal getConversionFactor(WeightUnit from, WeightUnit to) {
        if (from == WeightUnit.POUNDS && to == WeightUnit.KILOGRAMS) {
            return POUNDS_TO_KG;
        } else if (from == WeightUnit.KILOGRAMS && to == WeightUnit.POUNDS) {
            return BigDecimal.ONE.divide(POUNDS_TO_KG, 6, RoundingMode.HALF_UP);
        }
        return BigDecimal.ONE;
    }

    public BigDecimal getValue() {
        return value;
    }

    public WeightUnit getUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Weight weight = (Weight) o;
        return Objects.equals(value, weight.value) && unit == weight.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }

    @Override
    public String toString() {
        return "Weight{" +
               "value=" + value +
               ", unit=" + unit +
               '}';
    }
}