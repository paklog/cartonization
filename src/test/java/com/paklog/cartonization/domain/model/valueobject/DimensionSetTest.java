package com.paklog.cartonization.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class DimensionSetTest {

    @Test
    void shouldCreateDimensionSetSuccessfully() {
        // Given
        BigDecimal length = BigDecimal.valueOf(10);
        BigDecimal width = BigDecimal.valueOf(5);
        BigDecimal height = BigDecimal.valueOf(3);
        DimensionUnit unit = DimensionUnit.CENTIMETERS;

        // When
        DimensionSet dimensions = new DimensionSet(length, width, height, unit);

        // Then
        assertThat(dimensions.getLength()).isEqualTo(length);
        assertThat(dimensions.getWidth()).isEqualTo(width);
        assertThat(dimensions.getHeight()).isEqualTo(height);
        assertThat(dimensions.getUnit()).isEqualTo(unit);
    }

    @Test
    void shouldCalculateVolumeCorrectly() {
        // Given
        DimensionSet dimensions = new DimensionSet(
            BigDecimal.valueOf(10),
            BigDecimal.valueOf(5),
            BigDecimal.valueOf(2),
            DimensionUnit.CENTIMETERS
        );

        // When
        BigDecimal volume = dimensions.volume();

        // Then
        assertThat(volume).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldThrowExceptionForNullDimensions() {
        // When & Then
        assertThatThrownBy(() -> new DimensionSet(null, BigDecimal.ONE, BigDecimal.ONE, DimensionUnit.CENTIMETERS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Length must be positive");

        assertThatThrownBy(() -> new DimensionSet(BigDecimal.ONE, null, BigDecimal.ONE, DimensionUnit.CENTIMETERS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Width must be positive");

        assertThatThrownBy(() -> new DimensionSet(BigDecimal.ONE, BigDecimal.ONE, null, DimensionUnit.CENTIMETERS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Height must be positive");

        // Unit validation is not currently implemented in DimensionSet constructor
    }

    @Test
    void shouldThrowExceptionForNegativeOrZeroDimensions() {
        // When & Then
        assertThatThrownBy(() -> new DimensionSet(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE, DimensionUnit.CENTIMETERS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Length must be positive");

        assertThatThrownBy(() -> new DimensionSet(BigDecimal.valueOf(-1), BigDecimal.ONE, BigDecimal.ONE, DimensionUnit.CENTIMETERS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Length must be positive");
    }

    @ParameterizedTest
    @MethodSource("dimensionComparisonTestCases")
    void shouldCompareDimensionsCorrectly(DimensionSet first, DimensionSet second, boolean expectedFits) {
        // When
        boolean canFit = first.canContain(second);

        // Then
        assertThat(canFit).isEqualTo(expectedFits);
    }

    private static Stream<Arguments> dimensionComparisonTestCases() {
        return Stream.of(
            // Same dimensions should fit
            Arguments.of(
                new DimensionSet(BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(3), DimensionUnit.CENTIMETERS),
                new DimensionSet(BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(3), DimensionUnit.CENTIMETERS),
                true
            ),
            // Smaller dimensions should fit
            Arguments.of(
                new DimensionSet(BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(3), DimensionUnit.CENTIMETERS),
                new DimensionSet(BigDecimal.valueOf(8), BigDecimal.valueOf(4), BigDecimal.valueOf(2), DimensionUnit.CENTIMETERS),
                true
            ),
            // Larger dimensions should not fit
            Arguments.of(
                new DimensionSet(BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(3), DimensionUnit.CENTIMETERS),
                new DimensionSet(BigDecimal.valueOf(12), BigDecimal.valueOf(6), BigDecimal.valueOf(4), DimensionUnit.CENTIMETERS),
                false
            ),
            // One dimension larger should not fit
            Arguments.of(
                new DimensionSet(BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(3), DimensionUnit.CENTIMETERS),
                new DimensionSet(BigDecimal.valueOf(8), BigDecimal.valueOf(4), BigDecimal.valueOf(4), DimensionUnit.CENTIMETERS),
                false
            )
        );
    }

    @Test
    void shouldBeEqualWhenSameDimensions() {
        // Given
        DimensionSet first = new DimensionSet(BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(3), DimensionUnit.CENTIMETERS);
        DimensionSet second = new DimensionSet(BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(3), DimensionUnit.CENTIMETERS);

        // When & Then
        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentDimensions() {
        // Given
        DimensionSet first = new DimensionSet(BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(3), DimensionUnit.CENTIMETERS);
        DimensionSet second = new DimensionSet(BigDecimal.valueOf(8), BigDecimal.valueOf(5), BigDecimal.valueOf(3), DimensionUnit.CENTIMETERS);

        // When & Then
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldNotBeEqualWhenDifferentUnits() {
        // Given
        DimensionSet first = new DimensionSet(BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(3), DimensionUnit.CENTIMETERS);
        DimensionSet second = new DimensionSet(BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(3), DimensionUnit.INCHES);

        // When & Then
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldHaveReadableToString() {
        // Given
        DimensionSet dimensions = new DimensionSet(BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(3), DimensionUnit.CENTIMETERS);

        // When
        String toString = dimensions.toString();

        // Then
        assertThat(toString).contains("10", "5", "3", "CENTIMETERS");
    }
}