package com.paklog.cartonization.domain.model.valueobject;

import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

@Value
public class DimensionSet {
    BigDecimal length;
    BigDecimal width;
    BigDecimal height;
    DimensionUnit unit;

    private static final BigDecimal INCHES_TO_CM = new BigDecimal("2.54");

    public DimensionSet(BigDecimal length, BigDecimal width, BigDecimal height, DimensionUnit unit) {
        validateDimension(length, "Length");
        validateDimension(width, "Width");
        validateDimension(height, "Height");

        this.length = length;
        this.width = width;
        this.height = height;
        this.unit = unit;
    }

    public BigDecimal volume() {
        return length.multiply(width).multiply(height)
            .setScale(2, RoundingMode.HALF_UP);
    }

    public DimensionSet convertTo(DimensionUnit targetUnit) {
        if (this.unit == targetUnit) {
            return this;
        }

        BigDecimal conversionFactor = getConversionFactor(this.unit, targetUnit);
        return new DimensionSet(
            length.multiply(conversionFactor),
            width.multiply(conversionFactor),
            height.multiply(conversionFactor),
            targetUnit
        );
    }

    public boolean canContain(DimensionSet item) {
        // Convert to same unit for comparison
        DimensionSet normalizedItem = item.convertTo(this.unit);

        // Get sorted dimensions for both carton and item
        List<BigDecimal> cartonDims = getSortedDimensions();
        List<BigDecimal> itemDims = normalizedItem.getSortedDimensions();

        // Check if item can fit in any orientation
        for (int i = 0; i < 3; i++) {
            if (itemDims.get(i).compareTo(cartonDims.get(i)) > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean hasZeroOrNegativeValues() {
        return length.compareTo(BigDecimal.ZERO) <= 0 ||
               width.compareTo(BigDecimal.ZERO) <= 0 ||
               height.compareTo(BigDecimal.ZERO) <= 0;
    }

    private List<BigDecimal> getSortedDimensions() {
        List<BigDecimal> dims = Arrays.asList(length, width, height);
        dims.sort(BigDecimal::compareTo);
        return dims;
    }

    private BigDecimal getConversionFactor(DimensionUnit from, DimensionUnit to) {
        if (from == DimensionUnit.INCHES && to == DimensionUnit.CENTIMETERS) {
            return INCHES_TO_CM;
        } else if (from == DimensionUnit.CENTIMETERS && to == DimensionUnit.INCHES) {
            return BigDecimal.ONE.divide(INCHES_TO_CM, 4, RoundingMode.HALF_UP);
        }
        return BigDecimal.ONE;
    }

    private void validateDimension(BigDecimal value, String dimensionName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(dimensionName + " must be positive");
        }
    }
}