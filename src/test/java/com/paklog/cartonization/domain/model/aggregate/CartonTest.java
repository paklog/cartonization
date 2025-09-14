package com.paklog.cartonization.domain.model.aggregate;

import com.paklog.cartonization.domain.model.valueobject.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartonTest {

    @Test
    void shouldCreateCartonSuccessfully() {
        // Given
        String name = "Test Box";
        DimensionSet dimensions = new DimensionSet(
            BigDecimal.valueOf(12), BigDecimal.valueOf(8), BigDecimal.valueOf(6),
            DimensionUnit.INCHES
        );
        Weight maxWeight = new Weight(BigDecimal.valueOf(25), WeightUnit.POUNDS);

        // When
        Carton carton = Carton.create(name, dimensions, maxWeight);

        // Then
        assertThat(carton).isNotNull();
        assertThat(carton.getName()).isEqualTo(name);
        assertThat(carton.getDimensions()).isEqualTo(dimensions);
        assertThat(carton.getMaxWeight()).isEqualTo(maxWeight);
        assertThat(carton.getStatus()).isEqualTo(CartonStatus.ACTIVE);
        assertThat(carton.getId()).isNotNull();
        assertThat(carton.getCreatedAt()).isNotNull();
        assertThat(carton.pullDomainEvents()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenCreatingCartonWithInvalidData() {
        // Given
        String invalidName = "";
        DimensionSet dimensions = new DimensionSet(
            BigDecimal.valueOf(12), BigDecimal.valueOf(8), BigDecimal.valueOf(6),
            DimensionUnit.INCHES
        );
        Weight maxWeight = new Weight(BigDecimal.valueOf(25), WeightUnit.POUNDS);

        // When & Then
        assertThatThrownBy(() -> Carton.create(invalidName, dimensions, maxWeight))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Carton name cannot be empty");
    }

    @Test
    void shouldDeactivateCarton() {
        // Given
        Carton carton = createTestCarton();

        // When
        carton.deactivate();

        // Then
        assertThat(carton.getStatus()).isEqualTo(CartonStatus.INACTIVE);
        assertThat(carton.getUpdatedAt()).isAfterOrEqualTo(carton.getCreatedAt());
        assertThat(carton.pullDomainEvents()).hasSize(1);
    }

    @Test
    void shouldFitItemWithinDimensions() {
        // Given
        Carton carton = createTestCarton();
        DimensionSet itemDimensions = new DimensionSet(
            BigDecimal.valueOf(10), BigDecimal.valueOf(6), BigDecimal.valueOf(4),
            DimensionUnit.INCHES
        );
        Weight itemWeight = new Weight(BigDecimal.valueOf(10), WeightUnit.POUNDS);

        // When
        boolean canFit = carton.canFitItem(itemDimensions, itemWeight);

        // Then
        assertThat(canFit).isTrue();
    }

    @Test
    void shouldNotFitItemThatIsTooLarge() {
        // Given
        Carton carton = createTestCarton();
        DimensionSet itemDimensions = new DimensionSet(
            BigDecimal.valueOf(20), BigDecimal.valueOf(10), BigDecimal.valueOf(8),
            DimensionUnit.INCHES
        );
        Weight itemWeight = new Weight(BigDecimal.valueOf(10), WeightUnit.POUNDS);

        // When
        boolean canFit = carton.canFitItem(itemDimensions, itemWeight);

        // Then
        assertThat(canFit).isFalse();
    }

    @Test
    void shouldNotFitItemThatIsTooHeavy() {
        // Given
        Carton carton = createTestCarton();
        DimensionSet itemDimensions = new DimensionSet(
            BigDecimal.valueOf(10), BigDecimal.valueOf(6), BigDecimal.valueOf(4),
            DimensionUnit.INCHES
        );
        Weight itemWeight = new Weight(BigDecimal.valueOf(50), WeightUnit.POUNDS);

        // When
        boolean canFit = carton.canFitItem(itemDimensions, itemWeight);

        // Then
        assertThat(canFit).isFalse();
    }

    private Carton createTestCarton() {
        return Carton.create(
            "Test Box",
            new DimensionSet(
                BigDecimal.valueOf(12), BigDecimal.valueOf(8), BigDecimal.valueOf(6),
                DimensionUnit.INCHES
            ),
            new Weight(BigDecimal.valueOf(25), WeightUnit.POUNDS)
        );
    }
}