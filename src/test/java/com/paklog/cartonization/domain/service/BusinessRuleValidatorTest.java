package com.paklog.cartonization.domain.service;

import com.paklog.cartonization.domain.model.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class BusinessRuleValidatorTest {

    private BusinessRuleValidator validator;
    private PackingRules defaultRules;

    @BeforeEach
    void setUp() {
        validator = new BusinessRuleValidator();
        setupDefaultRules();
    }

    @Test
    void shouldValidateValidPackingRequest() {
        // Given
        List<ItemWithDimensions> items = Arrays.asList(
            createValidItem("ITEM001", "Electronics"),
            createValidItem("ITEM002", "Electronics")
        );

        // When & Then
        assertThatCode(() -> validator.validatePackingRequest(items, defaultRules))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionForNullItems() {
        // When & Then
        assertThatThrownBy(() -> validator.validatePackingRequest(null, defaultRules))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Packing request must contain at least one item");
    }

    @Test
    void shouldThrowExceptionForEmptyItems() {
        // When & Then
        assertThatThrownBy(() -> validator.validatePackingRequest(Collections.emptyList(), defaultRules))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Packing request must contain at least one item");
    }

    @Test
    void shouldThrowExceptionForNullPackingRules() {
        // Given
        List<ItemWithDimensions> items = Collections.singletonList(createValidItem("ITEM001", "Electronics"));

        // When & Then
        assertThatThrownBy(() -> validator.validatePackingRequest(items, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Packing rules cannot be null");
    }

    @Test
    void shouldThrowExceptionForMixedCategoriesWhenNotAllowed() {
        // Given
        PackingRules restrictiveRules = PackingRules.builder()
            .maxUtilizationThreshold(BigDecimal.valueOf(0.8))
            .optimizeForMinimumBoxes(true)
            .allowMixedCategories(false)
            .separateFragileItems(false)
            .build();

        List<ItemWithDimensions> items = Arrays.asList(
            createValidItem("ITEM001", "Electronics"),
            createValidItem("ITEM002", "Clothing")
        );

        // When & Then
        assertThatThrownBy(() -> validator.validatePackingRequest(items, restrictiveRules))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Mixed categories not allowed according to packing rules");
    }

    @Test
    void shouldThrowExceptionForMixedFragileItemsWhenNotAllowed() {
        // Given
        PackingRules fragileRules = PackingRules.builder()
            .maxUtilizationThreshold(BigDecimal.valueOf(0.8))
            .optimizeForMinimumBoxes(true)
            .allowMixedCategories(true)
            .separateFragileItems(true)
            .build();

        List<ItemWithDimensions> items = Arrays.asList(
            createFragileItem("ITEM001", "Electronics"),
            createValidItem("ITEM002", "Electronics")
        );

        // When & Then
        assertThatThrownBy(() -> validator.validatePackingRequest(items, fragileRules))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Fragile and non-fragile items cannot be packed together according to packing rules");
    }

    @Test
    void shouldAllowFragileItemsTogetherWhenSeparationRequired() {
        // Given
        PackingRules fragileRules = PackingRules.builder()
            .maxUtilizationThreshold(BigDecimal.valueOf(0.8))
            .optimizeForMinimumBoxes(true)
            .allowMixedCategories(true)
            .separateFragileItems(true)
            .build();

        List<ItemWithDimensions> items = Arrays.asList(
            createFragileItem("ITEM001", "Electronics"),
            createFragileItem("ITEM002", "Electronics")
        );

        // When & Then
        assertThatCode(() -> validator.validatePackingRequest(items, fragileRules))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldValidateItemWithValidData() {
        // Given
        ItemWithDimensions validItem = createValidItem("ITEM001", "Electronics");

        // When
        boolean isValid = validator.isItemValid(validItem);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldRejectNullItem() {
        // When
        boolean isValid = validator.isItemValid(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectItemWithNullSku() {
        // Given
        ItemWithDimensions item = ItemWithDimensions.builder()
            .sku(null)
            .quantity(1)
            .dimensions(createValidDimensions())
            .weight(createValidWeight())
            .category("Electronics")
            .fragile(false)
            .build();

        // When
        boolean isValid = validator.isItemValid(item);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectItemWithEmptySku() {
        // When & Then
        assertThatThrownBy(() -> SKU.of("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("SKU cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void shouldRejectItemWithInvalidQuantity(int quantity) {
        // Given
        ItemWithDimensions item = ItemWithDimensions.builder()
            .sku(SKU.of("ITEM001"))
            .quantity(quantity)
            .dimensions(createValidDimensions())
            .weight(createValidWeight())
            .category("Electronics")
            .fragile(false)
            .build();

        // When
        boolean isValid = validator.isItemValid(item);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectItemWithNullDimensions() {
        // Given
        ItemWithDimensions item = ItemWithDimensions.builder()
            .sku(SKU.of("ITEM001"))
            .quantity(1)
            .dimensions(null)
            .weight(createValidWeight())
            .category("Electronics")
            .fragile(false)
            .build();

        // When
        boolean isValid = validator.isItemValid(item);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectItemWithNullWeight() {
        // Given
        ItemWithDimensions item = ItemWithDimensions.builder()
            .sku(SKU.of("ITEM001"))
            .quantity(1)
            .dimensions(createValidDimensions())
            .weight(null)
            .category("Electronics")
            .fragile(false)
            .build();

        // When
        boolean isValid = validator.isItemValid(item);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldAllowCompatibleItemsTogether() {
        // Given
        ItemWithDimensions item1 = createValidItem("ITEM001", "Electronics");
        ItemWithDimensions item2 = createValidItem("ITEM002", "Electronics");

        // When
        boolean canPack = validator.canPackTogether(item1, item2, defaultRules);

        // Then
        assertThat(canPack).isTrue();
    }

    @Test
    void shouldRejectDifferentCategoriesWhenNotAllowed() {
        // Given
        PackingRules restrictiveRules = PackingRules.builder()
            .maxUtilizationThreshold(BigDecimal.valueOf(0.8))
            .optimizeForMinimumBoxes(true)
            .allowMixedCategories(false)
            .separateFragileItems(false)
            .build();

        ItemWithDimensions item1 = createValidItem("ITEM001", "Electronics");
        ItemWithDimensions item2 = createValidItem("ITEM002", "Clothing");

        // When
        boolean canPack = validator.canPackTogether(item1, item2, restrictiveRules);

        // Then
        assertThat(canPack).isFalse();
    }

    @Test
    void shouldRejectFragileWithNonFragileWhenSeparationRequired() {
        // Given
        PackingRules fragileRules = PackingRules.builder()
            .maxUtilizationThreshold(BigDecimal.valueOf(0.8))
            .optimizeForMinimumBoxes(true)
            .allowMixedCategories(true)
            .separateFragileItems(true)
            .build();

        ItemWithDimensions fragileItem = createFragileItem("ITEM001", "Electronics");
        ItemWithDimensions regularItem = createValidItem("ITEM002", "Electronics");

        // When
        boolean canPack = validator.canPackTogether(fragileItem, regularItem, fragileRules);

        // Then
        assertThat(canPack).isFalse();
    }

    @ParameterizedTest
    @MethodSource("weightThresholdTestCases")
    void shouldCheckWeightThreshold(BigDecimal totalWeight, BigDecimal maxWeight, 
                                  BigDecimal threshold, boolean expectedExceeds) {
        // When
        boolean exceeds = validator.exceedsWeightThreshold(totalWeight, maxWeight, threshold);

        // Then
        assertThat(exceeds).isEqualTo(expectedExceeds);
    }

    private static Stream<Arguments> weightThresholdTestCases() {
        return Stream.of(
            Arguments.of(BigDecimal.valueOf(8), BigDecimal.valueOf(10), BigDecimal.valueOf(0.7), true),   // 80% > 70%
            Arguments.of(BigDecimal.valueOf(6), BigDecimal.valueOf(10), BigDecimal.valueOf(0.7), false),  // 60% < 70%
            Arguments.of(BigDecimal.valueOf(7), BigDecimal.valueOf(10), BigDecimal.valueOf(0.7), false),  // 70% = 70% (not exceeds)
            Arguments.of(null, BigDecimal.valueOf(10), BigDecimal.valueOf(0.7), true),                    // null weight
            Arguments.of(BigDecimal.valueOf(8), null, BigDecimal.valueOf(0.7), true),                     // null max
            Arguments.of(BigDecimal.valueOf(8), BigDecimal.valueOf(10), null, true)                       // null threshold
        );
    }

    @ParameterizedTest
    @MethodSource("volumeThresholdTestCases")
    void shouldCheckVolumeThreshold(BigDecimal usedVolume, BigDecimal totalVolume, 
                                  BigDecimal threshold, boolean expectedExceeds) {
        // When
        boolean exceeds = validator.exceedsVolumeThreshold(usedVolume, totalVolume, threshold);

        // Then
        assertThat(exceeds).isEqualTo(expectedExceeds);
    }

    private static Stream<Arguments> volumeThresholdTestCases() {
        return Stream.of(
            Arguments.of(BigDecimal.valueOf(80), BigDecimal.valueOf(100), BigDecimal.valueOf(0.7), true),   // 80% > 70%
            Arguments.of(BigDecimal.valueOf(60), BigDecimal.valueOf(100), BigDecimal.valueOf(0.7), false),  // 60% < 70%
            Arguments.of(BigDecimal.valueOf(70), BigDecimal.valueOf(100), BigDecimal.valueOf(0.7), false),  // 70% = 70%
            Arguments.of(BigDecimal.valueOf(50), BigDecimal.ZERO, BigDecimal.valueOf(0.7), true),           // division by zero
            Arguments.of(null, BigDecimal.valueOf(100), BigDecimal.valueOf(0.7), true),                     // null used
            Arguments.of(BigDecimal.valueOf(80), null, BigDecimal.valueOf(0.7), true),                      // null total
            Arguments.of(BigDecimal.valueOf(80), BigDecimal.valueOf(100), null, true)                       // null threshold
        );
    }

    @Test
    void shouldThrowExceptionForOversizedItems() {
        // Given
        ItemWithDimensions oversizedItem = ItemWithDimensions.builder()
            .sku(SKU.of("OVERSIZED"))
            .quantity(1)
            .dimensions(new DimensionSet(
                BigDecimal.valueOf(1500), // Exceeds max of 1000
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(10),
                DimensionUnit.CENTIMETERS
            ))
            .weight(createValidWeight())
            .category("Electronics")
            .fragile(false)
            .build();

        List<ItemWithDimensions> items = Collections.singletonList(oversizedItem);

        // When & Then
        assertThatThrownBy(() -> validator.validatePackingRequest(items, defaultRules))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Item dimensions exceed maximum allowed size");
    }

    @Test
    void shouldThrowExceptionForOverweightItems() {
        // Given
        ItemWithDimensions overweightItem = ItemWithDimensions.builder()
            .sku(SKU.of("OVERWEIGHT"))
            .quantity(1)
            .dimensions(createValidDimensions())
            .weight(new Weight(BigDecimal.valueOf(1500), WeightUnit.KILOGRAMS)) // Exceeds max of 1000
            .category("Electronics")
            .fragile(false)
            .build();

        List<ItemWithDimensions> items = Collections.singletonList(overweightItem);

        // When & Then
        assertThatThrownBy(() -> validator.validatePackingRequest(items, defaultRules))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Item weight exceeds maximum allowed weight");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 0.0, 1.1, 2.0})
    void shouldThrowExceptionForInvalidUtilizationThreshold(double thresholdValue) {
        // Given
        PackingRules invalidRules = PackingRules.builder()
            .maxUtilizationThreshold(BigDecimal.valueOf(thresholdValue))
            .optimizeForMinimumBoxes(true)
            .allowMixedCategories(true)
            .separateFragileItems(false)
            .build();

        List<ItemWithDimensions> items = Collections.singletonList(createValidItem("ITEM001", "Electronics"));

        // When & Then
        assertThatThrownBy(() -> validator.validatePackingRequest(items, invalidRules))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Max utilization threshold must be between 0 and 1");
    }

    private void setupDefaultRules() {
        defaultRules = PackingRules.builder()
            .maxUtilizationThreshold(BigDecimal.valueOf(0.85))
            .optimizeForMinimumBoxes(true)
            .allowMixedCategories(true)
            .separateFragileItems(false)
            .build();
    }

    private ItemWithDimensions createValidItem(String sku, String category) {
        return ItemWithDimensions.builder()
            .sku(SKU.of(sku))
            .quantity(1)
            .dimensions(createValidDimensions())
            .weight(createValidWeight())
            .category(category)
            .fragile(false)
            .build();
    }

    private ItemWithDimensions createFragileItem(String sku, String category) {
        return ItemWithDimensions.builder()
            .sku(SKU.of(sku))
            .quantity(1)
            .dimensions(createValidDimensions())
            .weight(createValidWeight())
            .category(category)
            .fragile(true)
            .build();
    }

    private DimensionSet createValidDimensions() {
        return new DimensionSet(
            BigDecimal.valueOf(10),
            BigDecimal.valueOf(8),
            BigDecimal.valueOf(6),
            DimensionUnit.CENTIMETERS
        );
    }

    private Weight createValidWeight() {
        return new Weight(BigDecimal.valueOf(2.5), WeightUnit.KILOGRAMS);
    }
}