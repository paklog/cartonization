package com.paklog.cartonization.application.service;

import com.paklog.cartonization.application.port.in.PackingSolutionUseCase;
import com.paklog.cartonization.application.port.in.command.CalculatePackingSolutionCommand;
import com.paklog.cartonization.application.port.out.CartonRepository;
import com.paklog.cartonization.application.port.out.EventPublisher;
import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.entity.PackingSolution;
import com.paklog.cartonization.domain.model.valueobject.*;
import com.paklog.cartonization.domain.service.PackingAlgorithmService;
import com.paklog.cartonization.domain.event.PackingSolutionCalculated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class PackingSolutionService implements PackingSolutionUseCase {

    private static final Logger log = LoggerFactory.getLogger(PackingSolutionService.class);

    private final PackingAlgorithmService packingAlgorithmService;
    private final CartonRepository cartonRepository;
    private final EventPublisher eventPublisher;
    private final ProductDimensionEnricher productDimensionEnricher;

    public PackingSolutionService(PackingAlgorithmService packingAlgorithmService,
                                   CartonRepository cartonRepository,
                                   EventPublisher eventPublisher,
                                   ProductDimensionEnricher productDimensionEnricher) {
        this.packingAlgorithmService = packingAlgorithmService;
        this.cartonRepository = cartonRepository;
        this.eventPublisher = eventPublisher;
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

            // Publish domain event
            PackingSolutionCalculated event = PackingSolutionCalculated.from(solution);
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