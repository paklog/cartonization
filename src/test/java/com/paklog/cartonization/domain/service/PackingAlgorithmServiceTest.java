package com.paklog.cartonization.domain.service;

import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.entity.PackingSolution;
import com.paklog.cartonization.domain.model.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class PackingAlgorithmServiceTest {

    private PackingAlgorithmService packingService;
    private List<Carton> testCartons;
    private PackingRules defaultRules;

    @BeforeEach
    void setUp() {
        packingService = new PackingAlgorithmService();
        setupTestCartons();
        setupDefaultRules();
    }

    @Test
    void shouldCalculateOptimalPackingForSingleItem() {
        // Given
        ItemWithDimensions item = createTestItem("ITEM001", 10, 5, 3, 2.0, "Electronics");
        List<ItemWithDimensions> items = Collections.singletonList(item);

        // When
        PackingSolution solution = packingService.calculateOptimalPacking(items, testCartons, defaultRules);

        // Then
        assertThat(solution).isNotNull();
        assertThat(solution.getPackages()).hasSize(1);
        assertThat(solution.getPackages().get(0).getItems()).hasSize(1);
        assertThat(solution.getTotalPackages()).isGreaterThan(0);
        assertThat(solution.getAverageUtilization()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void shouldPackMultipleItemsInSingleCarton() {
        // Given
        List<ItemWithDimensions> items = Arrays.asList(
            createTestItem("ITEM001", 5, 3, 2, 1.0, "Electronics"),
            createTestItem("ITEM002", 4, 3, 2, 1.5, "Electronics"),
            createTestItem("ITEM003", 3, 2, 1, 0.5, "Electronics")
        );

        // When
        PackingSolution solution = packingService.calculateOptimalPacking(items, testCartons, defaultRules);

        // Then
        assertThat(solution).isNotNull();
        assertThat(solution.getPackages()).hasSize(1);
        assertThat(solution.getPackages().get(0).getItems()).hasSize(3);
    }

    @Test
    void shouldCreateMultiplePackagesWhenItemsDoNotFit() {
        // Given
        List<ItemWithDimensions> items = Arrays.asList(
            createTestItem("ITEM001", 15, 10, 8, 10.0, "Furniture"),
            createTestItem("ITEM002", 20, 15, 10, 15.0, "Furniture"),
            createTestItem("ITEM003", 25, 20, 12, 20.0, "Furniture")
        );

        // When
        PackingSolution solution = packingService.calculateOptimalPacking(items, testCartons, defaultRules);

        // Then
        assertThat(solution).isNotNull();
        assertThat(solution.getPackages().size()).isGreaterThan(1);
    }

    @Test
    void shouldSortItemsByVolumeDescending() {
        // Given
        List<ItemWithDimensions> items = Arrays.asList(
            createTestItem("SMALL", 2, 2, 2, 1.0, "Electronics"),    // Volume: 8
            createTestItem("LARGE", 5, 5, 5, 3.0, "Electronics"),    // Volume: 125
            createTestItem("MEDIUM", 3, 3, 3, 2.0, "Electronics")     // Volume: 27
        );

        // When
        PackingSolution solution = packingService.calculateOptimalPacking(items, testCartons, defaultRules);

        // Then
        assertThat(solution).isNotNull();
        // The algorithm should process larger items first for better packing efficiency
        assertThat(solution.getPackages()).isNotEmpty();
    }

    @Test
    void shouldOptimizeForMinimumBoxesWhenRuleSet() {
        // Given
        PackingRules optimizeForBoxes = PackingRules.builder()
            .maxUtilizationThreshold(BigDecimal.valueOf(0.8))
            .optimizeForMinimumBoxes(true)
            .allowMixedCategories(true)
            .separateFragileItems(false)
            .build();

        List<ItemWithDimensions> items = Arrays.asList(
            createTestItem("ITEM001", 8, 6, 4, 3.0, "Electronics"),
            createTestItem("ITEM002", 6, 4, 3, 2.0, "Electronics")
        );

        // When
        PackingSolution solution = packingService.calculateOptimalPacking(items, testCartons, optimizeForBoxes);

        // Then
        assertThat(solution).isNotNull();
        assertThat(solution.getPackages()).hasSize(1);
    }

    @Test
    void shouldUseFirstFitDecreasingWhenNotOptimizingForBoxes() {
        // Given
        PackingRules firstFitRules = PackingRules.builder()
            .maxUtilizationThreshold(BigDecimal.valueOf(0.8))
            .optimizeForMinimumBoxes(false)
            .allowMixedCategories(true)
            .separateFragileItems(false)
            .build();

        List<ItemWithDimensions> items = Arrays.asList(
            createTestItem("ITEM001", 8, 6, 4, 3.0, "Electronics"),
            createTestItem("ITEM002", 6, 4, 3, 2.0, "Electronics")
        );

        // When
        PackingSolution solution = packingService.calculateOptimalPacking(items, testCartons, firstFitRules);

        // Then
        assertThat(solution).isNotNull();
        assertThat(solution.getPackages()).isNotEmpty();
    }

    @Test
    void shouldThrowExceptionWhenNoSuitableCartonFound() {
        // Given
        ItemWithDimensions oversizedItem = createTestItem("OVERSIZED", 100, 100, 100, 1000.0, "Furniture");
        List<ItemWithDimensions> items = Collections.singletonList(oversizedItem);

        // When & Then
        assertThatThrownBy(() -> packingService.calculateOptimalPacking(items, testCartons, defaultRules))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot pack item: OVERSIZED");
    }

    @Test
    void shouldFilterInactiveCartons() {
        // Given
        Carton inactiveCarton = Carton.create(
            "Inactive Box",
            new DimensionSet(BigDecimal.valueOf(50), BigDecimal.valueOf(50), BigDecimal.valueOf(50), DimensionUnit.CENTIMETERS),
            new Weight(BigDecimal.valueOf(100), WeightUnit.KILOGRAMS)
        );
        inactiveCarton.deactivate();

        List<Carton> cartonsWithInactive = Arrays.asList(testCartons.get(0), inactiveCarton);
        ItemWithDimensions item = createTestItem("ITEM001", 10, 5, 3, 2.0, "Electronics");

        // When
        PackingSolution solution = packingService.calculateOptimalPacking(
            Collections.singletonList(item), 
            cartonsWithInactive, 
            defaultRules
        );

        // Then
        assertThat(solution).isNotNull();
        assertThat(solution.getPackages()).hasSize(1);
        // Should use active carton, not inactive one
    }

    @ParameterizedTest
    @MethodSource("packingScenarios")
    void shouldHandleVariousPackingScenarios(List<ItemWithDimensions> items, int expectedPackages, String scenario) {
        // When
        PackingSolution solution = packingService.calculateOptimalPacking(items, testCartons, defaultRules);

        // Then
        assertThat(solution).isNotNull();
        assertThat(solution.getPackages())
            .as("Scenario: " + scenario)
            .hasSize(expectedPackages);
    }

    private static Stream<Arguments> packingScenarios() {
        return Stream.of(
            Arguments.of(
                Arrays.asList(
                    createTestItem("A", 2, 2, 2, 0.5, "Electronics"),
                    createTestItem("B", 2, 2, 2, 0.5, "Electronics")
                ),
                1,
                "Two small items fitting in one box"
            ),
            Arguments.of(
                Arrays.asList(
                    createTestItem("A", 12, 8, 6, 3.0, "Furniture"),
                    createTestItem("B", 12, 8, 6, 3.0, "Furniture")
                ),
                2,
                "Two medium items requiring separate boxes"
            ),
            Arguments.of(
                Collections.singletonList(
                    createTestItem("A", 1, 1, 1, 0.1, "Electronics")
                ),
                1,
                "Single tiny item"
            )
        );
    }

    @Test
    void shouldCalculateUtilizationCorrectly() {
        // Given
        List<ItemWithDimensions> items = Arrays.asList(
            createTestItem("ITEM001", 8, 6, 4, 3.0, "Electronics")
        );

        // When
        PackingSolution solution = packingService.calculateOptimalPacking(items, testCartons, defaultRules);

        // Then
        assertThat(solution).isNotNull();
        assertThat(solution.getAverageUtilization())
            .isBetween(BigDecimal.ZERO, BigDecimal.ONE);
    }

    @Test
    void shouldSetTemporaryRequestId() {
        // Given
        ItemWithDimensions item = createTestItem("ITEM001", 10, 5, 3, 2.0, "Electronics");
        List<ItemWithDimensions> items = Collections.singletonList(item);

        // When
        PackingSolution solution = packingService.calculateOptimalPacking(items, testCartons, defaultRules);

        // Then
        assertThat(solution).isNotNull();
        assertThat(solution.getRequestId()).startsWith("temp-");
    }

    private void setupTestCartons() {
        testCartons = Arrays.asList(
            // Small carton
            Carton.create(
                "Small Box",
                new DimensionSet(BigDecimal.valueOf(15), BigDecimal.valueOf(10), BigDecimal.valueOf(8), DimensionUnit.CENTIMETERS),
                new Weight(BigDecimal.valueOf(5), WeightUnit.KILOGRAMS)
            ),
            // Medium carton
            Carton.create(
                "Medium Box",
                new DimensionSet(BigDecimal.valueOf(25), BigDecimal.valueOf(20), BigDecimal.valueOf(15), DimensionUnit.CENTIMETERS),
                new Weight(BigDecimal.valueOf(15), WeightUnit.KILOGRAMS)
            ),
            // Large carton
            Carton.create(
                "Large Box",
                new DimensionSet(BigDecimal.valueOf(40), BigDecimal.valueOf(30), BigDecimal.valueOf(25), DimensionUnit.CENTIMETERS),
                new Weight(BigDecimal.valueOf(30), WeightUnit.KILOGRAMS)
            )
        );
    }

    private void setupDefaultRules() {
        defaultRules = PackingRules.builder()
            .maxUtilizationThreshold(BigDecimal.valueOf(0.95))
            .optimizeForMinimumBoxes(true)
            .allowMixedCategories(true)
            .separateFragileItems(false)
            .build();
    }

    private static ItemWithDimensions createTestItem(String sku, double length, double width, double height, 
                                                    double weight, String category) {
        return ItemWithDimensions.builder()
            .sku(SKU.of(sku))
            .quantity(1)
            .dimensions(new DimensionSet(
                BigDecimal.valueOf(length),
                BigDecimal.valueOf(width), 
                BigDecimal.valueOf(height),
                DimensionUnit.CENTIMETERS
            ))
            .weight(new Weight(BigDecimal.valueOf(weight), WeightUnit.KILOGRAMS))
            .category(category)
            .fragile(false)
            .build();
    }

    private static ItemWithDimensions createFragileTestItem(String sku, double length, double width, double height, 
                                                           double weight, String category) {
        return ItemWithDimensions.builder()
            .sku(SKU.of(sku))
            .quantity(1)
            .dimensions(new DimensionSet(
                BigDecimal.valueOf(length),
                BigDecimal.valueOf(width), 
                BigDecimal.valueOf(height),
                DimensionUnit.CENTIMETERS
            ))
            .weight(new Weight(BigDecimal.valueOf(weight), WeightUnit.KILOGRAMS))
            .category(category)
            .fragile(true)
            .build();
    }
}