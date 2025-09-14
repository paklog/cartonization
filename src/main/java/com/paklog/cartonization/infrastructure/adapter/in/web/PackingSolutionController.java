package com.paklog.cartonization.infrastructure.adapter.in.web;

import com.paklog.cartonization.application.port.in.PackingSolutionUseCase;
import com.paklog.cartonization.application.port.in.command.CalculatePackingSolutionCommand;
import com.paklog.cartonization.domain.model.entity.PackingSolution;
import com.paklog.cartonization.domain.model.valueobject.ItemToPack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/packing-solutions")
@RequiredArgsConstructor
@Slf4j
public class PackingSolutionController {

    private final PackingSolutionUseCase packingSolutionUseCase;

    @PostMapping
    public ResponseEntity<PackingSolution> calculatePackingSolution(
            @RequestBody PackingRequest request,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId) {

        // Generate request ID if not provided
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        log.info("Processing packing solution request. RequestId: {}, Items: {}",
                requestId, request.items().size());

        // Convert request to command
        List<ItemToPack> items = request.items().stream()
            .map(item -> ItemToPack.of(item.sku(), item.quantity()))
            .toList();

        CalculatePackingSolutionCommand command = CalculatePackingSolutionCommand.builder()
            .requestId(requestId)
            .items(items)
            .orderId(request.orderId())
            .optimizeForMinimumBoxes(request.optimizeForMinimumBoxes() != null ? request.optimizeForMinimumBoxes() : true)
            .allowMixedCategories(request.allowMixedCategories() != null ? request.allowMixedCategories() : true)
            .build();

        // Calculate solution
        PackingSolution solution = packingSolutionUseCase.calculate(command);

        log.info("Successfully calculated packing solution. RequestId: {}, Packages: {}",
                requestId, solution.getTotalPackages());

        return ResponseEntity.ok(solution);
    }

    // Simple request DTO
    public record PackingRequest(
        List<ItemRequest> items,
        String orderId,
        Boolean optimizeForMinimumBoxes,
        Boolean allowMixedCategories
    ) {}

    public record ItemRequest(
        String sku,
        Integer quantity
    ) {}
}