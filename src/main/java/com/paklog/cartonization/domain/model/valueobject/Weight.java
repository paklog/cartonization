package com.paklog.cartonization.domain.model.valueobject;

import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Value
public class Weight {
    BigDecimal value;
    WeightUnit unit;

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
}