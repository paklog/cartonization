package com.paklog.cartonization.application.service;

import com.paklog.cartonization.application.port.in.PackingSolutionUseCase;
import com.paklog.cartonization.application.port.in.command.CalculatePackingSolutionCommand;
import com.paklog.cartonization.application.port.out.CartonRepository;
import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.entity.PackingSolution;
import com.paklog.cartonization.domain.model.valueobject.*;
import com.paklog.cartonization.domain.service.PackingAlgorithmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PackingSolutionService implements PackingSolutionUseCase {

    private static final Logger log = LoggerFactory.getLogger(PackingSolutionService.class);
    
    private final PackingAlgorithmService packingAlgorithmService;
    private final CartonRepository cartonRepository;
    private final ProductDimensionEnricher productDimensionEnricher;
    
    public PackingSolutionService(PackingAlgorithmService packingAlgorithmService, 
                                CartonRepository cartonRepository,
                                ProductDimensionEnricher productDimensionEnricher) {
        this.packingAlgorithmService = packingAlgorithmService;
        this.cartonRepository = cartonRepository;
        this.productDimensionEnricher = productDimensionEnricher;
    }

    @Override
    public PackingSolution calculate(CalculatePackingSolutionCommand command) {
        log.info("Processing packing solution request: {}", command.getRequestId());

        try {
            // Get available cartons
            List<Carton> availableCartons = cartonRepository.findAllActive();
            if (availableCartons.isEmpty()) {
                throw new IllegalStateException("No active cartons available");
            }

            // Enrich items with dimensions from product catalog
            List<ItemWithDimensions> enrichedItems = productDimensionEnricher.enrichItems(command.getItems());

            // Build packing rules from command
            PackingRules rules = PackingRules.builder()
                .optimizeForMinimumBoxes(command.isOptimizeForMinimumBoxes())
                .allowMixedCategories(command.isAllowMixedCategories())
                .separateFragileItems(true)
                .maxUtilizationThreshold(BigDecimal.valueOf(0.95))
                .build();

            // Calculate optimal packing
            PackingSolution solution = packingAlgorithmService.calculateOptimalPacking(
                enrichedItems,
                availableCartons,
                rules
            );

            // Set metadata
            solution.setRequestId(command.getRequestId());
            solution.setOrderId(command.getOrderId());

            log.info("Successfully calculated packing solution for request: {}", command.getRequestId());
            log.info("Solution uses {} packages with {} total items",
                    solution.getTotalPackages(), solution.getTotalItems());

            return solution;

        } catch (Exception e) {
            log.error("Error calculating packing solution for request: {}", command.getRequestId(), e);
            throw new PackingSolutionException("Failed to calculate packing solution", e);
        }
    }

    // Mock implementation - in real scenario, this would call Product Catalog service
    private List<ItemWithDimensions> enrichItemsWithMockDimensions(List<ItemToPack> items) {
        return items.stream()
            .map(item -> {
                // Mock dimensions based on SKU for demonstration
                // In production, this would be fetched from Product Catalog service
                DimensionSet mockDimensions = createMockDimensions(item.getSku());
                Weight mockWeight = createMockWeight(item.getSku());

                return ItemWithDimensions.builder()
                    .sku(item.getSku())
                    .quantity(item.getQuantity())
                    .dimensions(mockDimensions)
                    .weight(mockWeight)
                    .category("GENERAL") // Mock category
                    .fragile(false) // Mock fragility
                    .build();
            })
            .collect(Collectors.toList());
    }

    // Mock dimension creation - replace with real Product Catalog integration
    private DimensionSet createMockDimensions(SKU sku) {
        String skuValue = sku.getValue().toLowerCase();

        if (skuValue.contains("large")) {
            return new DimensionSet(
                BigDecimal.valueOf(18), BigDecimal.valueOf(12), BigDecimal.valueOf(8),
                DimensionUnit.INCHES
            );
        } else if (skuValue.contains("medium")) {
            return new DimensionSet(
                BigDecimal.valueOf(12), BigDecimal.valueOf(8), BigDecimal.valueOf(6),
                DimensionUnit.INCHES
            );
        } else {
            return new DimensionSet(
                BigDecimal.valueOf(6), BigDecimal.valueOf(4), BigDecimal.valueOf(4),
                DimensionUnit.INCHES
            );
        }
    }

    // Mock weight creation - replace with real Product Catalog integration
    private Weight createMockWeight(SKU sku) {
        String skuValue = sku.getValue().toLowerCase();

        if (skuValue.contains("large")) {
            return new Weight(BigDecimal.valueOf(5.0), WeightUnit.POUNDS);
        } else if (skuValue.contains("medium")) {
            return new Weight(BigDecimal.valueOf(2.5), WeightUnit.POUNDS);
        } else {
            return new Weight(BigDecimal.valueOf(1.0), WeightUnit.POUNDS);
        }
    }

    public static class PackingSolutionException extends RuntimeException {
        public PackingSolutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}