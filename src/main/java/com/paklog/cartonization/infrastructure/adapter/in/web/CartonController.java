package com.paklog.cartonization.infrastructure.adapter.in.web;

import com.paklog.cartonization.application.port.in.CartonManagementUseCase;
import com.paklog.cartonization.application.port.in.command.CreateCartonCommand;
import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.valueobject.DimensionSet;
import com.paklog.cartonization.domain.model.valueobject.DimensionUnit;
import com.paklog.cartonization.domain.model.valueobject.Weight;
import com.paklog.cartonization.domain.model.valueobject.WeightUnit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Carton Management", description = "API for managing carton types and specifications")
@RestController
@RequestMapping("/api/v1/cartons")
public class CartonController {

    private static final Logger log = LoggerFactory.getLogger(CartonController.class);
    
    private final CartonManagementUseCase cartonManagementUseCase;
    
    public CartonController(CartonManagementUseCase cartonManagementUseCase) {
        this.cartonManagementUseCase = cartonManagementUseCase;
    }

    @Operation(
        summary = "Create new carton type",
        description = "Creates a new carton type with specified dimensions and weight capacity. The carton will be available for packing solutions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Carton created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Carton.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid carton data",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\": \"Validation Failed\", \"message\": \"Carton dimensions must be positive\"}"
                )
            )
        )
    })
    @PostMapping
    public ResponseEntity<Carton> createCarton(@Valid @RequestBody CreateCartonRequest request) {
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

    @Operation(
        summary = "List carton types",
        description = "Retrieves a list of available carton types. Can be filtered to show only active cartons."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "List of cartons retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Carton.class, type = "array")
            )
        )
    })
    @GetMapping
    public ResponseEntity<List<Carton>> listCartons(
            @Parameter(
                description = "Filter to show only active cartons",
                example = "true"
            )
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        log.info("Listing cartons (activeOnly: {})", activeOnly);

        List<Carton> cartons = activeOnly
            ? cartonManagementUseCase.listActiveCartons()
            : cartonManagementUseCase.listAllCartons();

        return ResponseEntity.ok(cartons);
    }

    @Operation(
        summary = "Get carton by ID",
        description = "Retrieves a specific carton type by its unique identifier."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Carton found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Carton.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Carton not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\": \"Not Found\", \"message\": \"Carton with ID 'unknown-id' not found\"}"
                )
            )
        )
    })
    @GetMapping("/{cartonId}")
    public ResponseEntity<Carton> getCartonById(
            @Parameter(
                description = "Unique carton identifier",
                example = "small-box-001"
            )
            @PathVariable String cartonId) {
        log.info("Getting carton by ID: {}", cartonId);

        Carton carton = cartonManagementUseCase.getCartonById(cartonId);
        return ResponseEntity.ok(carton);
    }

    @Operation(
        summary = "Deactivate carton type",
        description = "Deactivates a carton type, making it unavailable for new packing solutions while preserving historical data."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "Carton deactivated successfully"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Carton not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\": \"Not Found\", \"message\": \"Carton with ID 'unknown-id' not found\"}"
                )
            )
        )
    })
    @DeleteMapping("/{cartonId}")
    public ResponseEntity<Void> deactivateCarton(
            @Parameter(
                description = "Unique carton identifier to deactivate",
                example = "small-box-001"
            )
            @PathVariable String cartonId) {
        log.info("Deactivating carton: {}", cartonId);

        cartonManagementUseCase.deactivateCarton(cartonId);
        return ResponseEntity.noContent().build();
    }

    @Schema(
        description = "Request to create a new carton type",
        example = """
            {
              "name": "Medium Box",
              "length": 30.0,
              "width": 20.0,
              "height": 15.0,
              "dimensionUnit": "CM",
              "maxWeight": 5.0,
              "weightUnit": "KG"
            }
            """
    )
    public record CreateCartonRequest(
        @Schema(
            description = "Human-readable carton name",
            example = "Medium Box",
            maxLength = 100
        )
        @NotBlank(message = "Carton name is required")
        @Size(max = 100, message = "Carton name must not exceed 100 characters")
        String name,
        
        @Schema(
            description = "Carton length dimension",
            example = "30.0",
            minimum = "0.1"
        )
        @NotNull(message = "Length is required")
        @DecimalMin(value = "0.1", message = "Length must be at least 0.1")
        BigDecimal length,
        
        @Schema(
            description = "Carton width dimension", 
            example = "20.0",
            minimum = "0.1"
        )
        @NotNull(message = "Width is required")
        @DecimalMin(value = "0.1", message = "Width must be at least 0.1")
        BigDecimal width,
        
        @Schema(
            description = "Carton height dimension",
            example = "15.0", 
            minimum = "0.1"
        )
        @NotNull(message = "Height is required")
        @DecimalMin(value = "0.1", message = "Height must be at least 0.1")
        BigDecimal height,
        
        @Schema(
            description = "Unit of measurement for dimensions",
            example = "CM",
            allowableValues = {"MM", "CM", "M", "IN", "FT"}
        )
        @NotBlank(message = "Dimension unit is required")
        @Pattern(
            regexp = "^(MM|CM|M|IN|FT)$",
            message = "Dimension unit must be one of: MM, CM, M, IN, FT"
        )
        String dimensionUnit,
        
        @Schema(
            description = "Maximum weight capacity of the carton",
            example = "5.0",
            minimum = "0.01"
        )
        @NotNull(message = "Maximum weight is required")
        @DecimalMin(value = "0.01", message = "Maximum weight must be at least 0.01")
        BigDecimal maxWeight,
        
        @Schema(
            description = "Unit of measurement for weight",
            example = "KG", 
            allowableValues = {"G", "KG", "LB", "OZ"}
        )
        @NotBlank(message = "Weight unit is required")
        @Pattern(
            regexp = "^(G|KG|LB|OZ)$",
            message = "Weight unit must be one of: G, KG, LB, OZ"
        )
        String weightUnit
    ) {}
}