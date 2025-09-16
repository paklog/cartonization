package com.paklog.cartonization.infrastructure.adapter.in.messaging.mapper;

import com.paklog.cartonization.application.port.in.command.CalculatePackingSolutionCommand;
import com.paklog.cartonization.domain.model.entity.Package;
import com.paklog.cartonization.domain.model.entity.PackingSolution;
import com.paklog.cartonization.domain.model.valueobject.ItemToPack;
import com.paklog.cartonization.domain.model.valueobject.SKU;
import com.paklog.cartonization.infrastructure.adapter.in.messaging.event.CartonizationRequestEvent;
import com.paklog.cartonization.infrastructure.adapter.in.messaging.event.CartonizationResponseEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartonizationEventMapper {

    public CalculatePackingSolutionCommand toCommand(CartonizationRequestEvent event) {
        if (event == null) {
            return null;
        }

        List<ItemToPack> items = event.getItems().stream()
            .map(this::toItemToPack)
            .collect(Collectors.toList());

        return CalculatePackingSolutionCommand.builder()
            .requestId(event.getRequestId())
            .items(items)
            .orderId(event.getOrderId())
            .optimizeForMinimumBoxes(getOptimizeForMinimumBoxes(event))
            .allowMixedCategories(getAllowMixedCategories(event))
            .build();
    }

    public CartonizationResponseEvent toSuccessResponse(CartonizationRequestEvent request, 
                                                       PackingSolution solution, 
                                                       long processingTimeMs) {
        List<CartonizationResponseEvent.PackageResponse> packages = solution.getPackages().stream()
            .map(this::toPackageResponse)
            .collect(Collectors.toList());

        CartonizationResponseEvent.SolutionMetrics metrics = new CartonizationResponseEvent.SolutionMetrics(
            solution.getTotalPackages(),
            solution.getTotalItems(),
            solution.getAverageUtilization(),
            solution.getTotalVolume(),
            solution.getTotalWeight()
        );

        return new CartonizationResponseEvent(
            request.getRequestId(),
            request.getOrderId(),
            solution.getSolutionId(),
            "SUCCESS",
            packages,
            metrics,
            null,
            Instant.now(),
            processingTimeMs
        );
    }

    public CartonizationResponseEvent toErrorResponse(CartonizationRequestEvent request, 
                                                     String errorMessage, 
                                                     long processingTimeMs) {
        return new CartonizationResponseEvent(
            request.getRequestId(),
            request.getOrderId(),
            null,
            "ERROR",
            null,
            null,
            errorMessage,
            Instant.now(),
            processingTimeMs
        );
    }

    private ItemToPack toItemToPack(CartonizationRequestEvent.ItemRequest itemRequest) {
        return ItemToPack.of(itemRequest.getSku(), itemRequest.getQuantity());
    }

    private CartonizationResponseEvent.PackageResponse toPackageResponse(Package pkg) {
        List<String> itemSkus = pkg.getItems().stream()
            .map(item -> item.getSku().getValue())
            .collect(Collectors.toList());

        return new CartonizationResponseEvent.PackageResponse(
            pkg.getCarton().getId().getValue(),
            pkg.getCarton().getName(),
            itemSkus,
            pkg.getItemCount(),
            pkg.getUtilization(),
            pkg.getCurrentWeight(),
            pkg.getUsedVolume()
        );
    }

    private boolean getOptimizeForMinimumBoxes(CartonizationRequestEvent event) {
        if (event.getPreferences() != null && event.getPreferences().getOptimizeForMinimumBoxes() != null) {
            return event.getPreferences().getOptimizeForMinimumBoxes();
        }
        return true; // Default value
    }

    private boolean getAllowMixedCategories(CartonizationRequestEvent event) {
        if (event.getPreferences() != null && event.getPreferences().getAllowMixedCategories() != null) {
            return event.getPreferences().getAllowMixedCategories();
        }
        return true; // Default value
    }
}