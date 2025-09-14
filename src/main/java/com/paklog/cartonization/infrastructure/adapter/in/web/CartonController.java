package com.paklog.cartonization.infrastructure.adapter.in.web;

import com.paklog.cartonization.application.port.in.CartonManagementUseCase;
import com.paklog.cartonization.application.port.in.command.CreateCartonCommand;
import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.valueobject.DimensionSet;
import com.paklog.cartonization.domain.model.valueobject.DimensionUnit;
import com.paklog.cartonization.domain.model.valueobject.Weight;
import com.paklog.cartonization.domain.model.valueobject.WeightUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cartons")
@RequiredArgsConstructor
@Slf4j
public class CartonController {

    private final CartonManagementUseCase cartonManagementUseCase;

    @PostMapping
    public ResponseEntity<Carton> createCarton(@RequestBody CreateCartonRequest request) {
        log.info("Creating new carton: {}", request.name());

        CreateCartonCommand command = new CreateCartonCommand(
            request.name(),
            new DimensionSet(
                request.length(),
                request.width(),
                request.height(),
                DimensionUnit.valueOf(request.dimensionUnit().toUpperCase())
            ),
            new Weight(
                request.maxWeight(),
                WeightUnit.valueOf(request.weightUnit().toUpperCase())
            )
        );

        Carton carton = cartonManagementUseCase.createCarton(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(carton);
    }

    @GetMapping
    public ResponseEntity<List<Carton>> listCartons(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        log.info("Listing cartons (activeOnly: {})", activeOnly);

        List<Carton> cartons = activeOnly
            ? cartonManagementUseCase.listActiveCartons()
            : cartonManagementUseCase.listAllCartons();

        return ResponseEntity.ok(cartons);
    }

    @GetMapping("/{cartonId}")
    public ResponseEntity<Carton> getCartonById(@PathVariable String cartonId) {
        log.info("Getting carton by ID: {}", cartonId);

        Carton carton = cartonManagementUseCase.getCartonById(cartonId);
        return ResponseEntity.ok(carton);
    }

    @DeleteMapping("/{cartonId}")
    public ResponseEntity<Void> deactivateCarton(@PathVariable String cartonId) {
        log.info("Deactivating carton: {}", cartonId);

        cartonManagementUseCase.deactivateCarton(cartonId);
        return ResponseEntity.noContent().build();
    }

    // Simple request DTO for initial implementation
    public record CreateCartonRequest(
        String name,
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        String dimensionUnit,
        BigDecimal maxWeight,
        String weightUnit
    ) {}
}