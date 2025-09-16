package com.paklog.cartonization.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class WeightTest {

    @Test
    void shouldCreateWeightSuccessfully() {
        // Given
        BigDecimal value = BigDecimal.valueOf(5.5);
        WeightUnit unit = WeightUnit.KILOGRAMS;

        // When
        Weight weight = new Weight(value, unit);

        // Then
        assertThat(weight.getValue()).isEqualTo(value);
        assertThat(weight.getUnit()).isEqualTo(unit);
    }

    @Test
    void shouldThrowExceptionForNullValue() {
        // When & Then
        assertThatThrownBy(() -> new Weight(null, WeightUnit.KILOGRAMS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Weight value must be positive");
    }

    @Test
    void shouldThrowExceptionForNegativeWeight() {
        // When & Then
        assertThatThrownBy(() -> new Weight(BigDecimal.valueOf(-1), WeightUnit.KILOGRAMS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Weight value must be positive");
    }

    @Test
    void shouldThrowExceptionForZeroWeight() {
        // When & Then
        assertThatThrownBy(() -> new Weight(BigDecimal.ZERO, WeightUnit.KILOGRAMS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Weight value must be positive");
    }

    @Test
    void shouldConvertBetweenUnits() {
        // Given
        Weight weightInKg = new Weight(BigDecimal.valueOf(10), WeightUnit.KILOGRAMS);

        // When
        Weight convertedWeight = weightInKg.convertTo(WeightUnit.POUNDS);

        // Then
        assertThat(convertedWeight.getUnit()).isEqualTo(WeightUnit.POUNDS);
        assertThat(convertedWeight.getValue()).isGreaterThan(weightInKg.getValue()); // Should be larger number in pounds
    }

    @Test
    void shouldNotConvertWhenSameUnit() {
        // Given
        Weight weight = new Weight(BigDecimal.valueOf(5), WeightUnit.KILOGRAMS);

        // When
        Weight convertedWeight = weight.convertTo(WeightUnit.KILOGRAMS);

        // Then
        assertThat(convertedWeight).isSameAs(weight); // Should return same instance
    }

    @Test
    void shouldCompareWeightsCorrectly() {
        // Given
        Weight heavyWeight = new Weight(BigDecimal.valueOf(10), WeightUnit.KILOGRAMS);
        Weight lightWeight = new Weight(BigDecimal.valueOf(5), WeightUnit.KILOGRAMS);

        // When & Then
        assertThat(heavyWeight.isGreaterThan(lightWeight)).isTrue();
        assertThat(lightWeight.isGreaterThan(heavyWeight)).isFalse();
    }

    @Test
    void shouldCompareWeightsWithDifferentUnits() {
        // Given
        Weight weightInKg = new Weight(BigDecimal.valueOf(1), WeightUnit.KILOGRAMS);
        Weight weightInPounds = new Weight(BigDecimal.valueOf(1), WeightUnit.POUNDS);

        // When & Then
        assertThat(weightInKg.isGreaterThan(weightInPounds)).isTrue(); // 1kg > 1lb
    }

    @Test
    void shouldIdentifyZeroOrNegativeWeights() {
        // Given
        Weight positiveWeight = new Weight(BigDecimal.valueOf(5), WeightUnit.KILOGRAMS);

        // When & Then
        assertThat(positiveWeight.isZeroOrNegative()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("conversionTestCases")
    void shouldConvertWeightUnitsCorrectly(BigDecimal inputValue, WeightUnit inputUnit, 
                                          WeightUnit targetUnit, BigDecimal expectedRange) {
        // Given
        Weight weight = new Weight(inputValue, inputUnit);

        // When
        Weight converted = weight.convertTo(targetUnit);

        // Then
        assertThat(converted.getUnit()).isEqualTo(targetUnit);
        assertThat(converted.getValue()).isCloseTo(expectedRange, withinPercentage(5)); // Allow 5% margin for precision
    }

    private static Stream<Arguments> conversionTestCases() {
        return Stream.of(
            // 1 kg = ~2.2 pounds
            Arguments.of(BigDecimal.valueOf(1), WeightUnit.KILOGRAMS, WeightUnit.POUNDS, BigDecimal.valueOf(2.2)),
            // 2.2 pounds = ~1 kg  
            Arguments.of(BigDecimal.valueOf(2.2), WeightUnit.POUNDS, WeightUnit.KILOGRAMS, BigDecimal.valueOf(1)),
            // Same unit conversion
            Arguments.of(BigDecimal.valueOf(5), WeightUnit.KILOGRAMS, WeightUnit.KILOGRAMS, BigDecimal.valueOf(5))
        );
    }

    @Test
    void shouldBeEqualWhenSameValueAndUnit() {
        // Given
        Weight first = new Weight(BigDecimal.valueOf(5.5), WeightUnit.KILOGRAMS);
        Weight second = new Weight(BigDecimal.valueOf(5.5), WeightUnit.KILOGRAMS);

        // When & Then
        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentValue() {
        // Given
        Weight first = new Weight(BigDecimal.valueOf(5.5), WeightUnit.KILOGRAMS);
        Weight second = new Weight(BigDecimal.valueOf(3.5), WeightUnit.KILOGRAMS);

        // When & Then
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldNotBeEqualWhenDifferentUnit() {
        // Given
        Weight first = new Weight(BigDecimal.valueOf(5.5), WeightUnit.KILOGRAMS);
        Weight second = new Weight(BigDecimal.valueOf(5.5), WeightUnit.POUNDS);

        // When & Then
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldHaveReadableToString() {
        // Given
        Weight weight = new Weight(BigDecimal.valueOf(5.5), WeightUnit.KILOGRAMS);

        // When
        String toString = weight.toString();

        // Then
        assertThat(toString).contains("5.5", "KILOGRAMS");
    }
}