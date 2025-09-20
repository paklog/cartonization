package com.paklog.cartonization.application.service;

import com.paklog.cartonization.application.port.in.PackingSolutionUseCase;
import com.paklog.cartonization.application.port.in.command.CalculatePackingSolutionCommand;
import com.paklog.cartonization.application.port.out.CartonRepository;
import com.paklog.cartonization.application.port.out.EventPublisher;
import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.entity.PackingSolution;
import com.paklog.cartonization.domain.model.valueobject.*;
import com.paklog.cartonization.domain.service.PackingAlgorithmService;
import com.paklog.cartonization.infrastructure.adapter.out.messaging.event.PackingSolutionCalculatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PackingSolutionService implements PackingSolutionUseCase {

    private final PackingAlgorithmService packingAlgorithmService;
    private final CartonRepository cartonRepository;
    private final EventPublisher eventPublisher;
    private final ProductDimensionEnricher productDimensionEnricher;

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

            // Publish event
            PackingSolutionCalculatedEvent event = PackingSolutionCalculatedEvent.from(solution);
            eventPublisher.publish("cartonization.packing-solution.calculated", solution.getRequestId(), event);

            return solution;

        } catch (Exception e) {
            log.error("Error calculating packing solution for request: {}", command.getRequestId(), e);
            throw new PackingSolutionException("Failed to calculate packing solution", e);
        }
    }

    public static class PackingSolutionException extends RuntimeException {
        public PackingSolutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}