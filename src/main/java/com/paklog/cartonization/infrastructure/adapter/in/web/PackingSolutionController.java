package com.paklog.cartonization.infrastructure.adapter.in.web;

import com.paklog.cartonization.application.port.in.PackingSolutionUseCase;
import com.paklog.cartonization.application.port.in.command.CalculatePackingSolutionCommand;
import com.paklog.cartonization.domain.model.entity.PackingSolution;
import com.paklog.cartonization.domain.model.valueobject.ItemToPack;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Packing Solutions", description = "API for calculating optimal packing solutions")
@RestController
@RequestMapping("/api/v1/packing-solutions")
public class PackingSolutionController {

    private static final Logger log = LoggerFactory.getLogger(PackingSolutionController.class);
    
    private final PackingSolutionUseCase packingSolutionUseCase;
    
    public PackingSolutionController(PackingSolutionUseCase packingSolutionUseCase) {
        this.packingSolutionUseCase = packingSolutionUseCase;
    }

    @Operation(
        summary = "Calculate optimal packing solution",
        description = """
            Calculates the optimal way to pack items into cartons based on item dimensions, 
            weights, and optimization preferences. The service considers various factors like 
            carton capacity, item fragility, category compatibility, and optimization goals.
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Packing request with items and optimization preferences",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PackingRequest.class),
                examples = @ExampleObject(
                    name = "Standard packing request",
                    value = """
                        {
                          "items": [
                            {
                              "sku": "BOOK-001",
                              "quantity": 2
                            },
                            {
                              "sku": "SHIRT-XL-BLUE",
                              "quantity": 1
                            }
                          ],
                          "orderId": "ORDER-123456",
                          "optimizeForMinimumBoxes": true,
                          "allowMixedCategories": true
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Packing solution calculated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PackingSolution.class),
                examples = @ExampleObject(
                    name = "Successful packing solution",
                    value = """
                        {
                          "solutionId": "sol-789",
                          "requestId": "req-123",
                          "orderId": "ORDER-123456",
                          "packages": [
                            {
                              "packageId": "pkg-001",
                              "carton": {
                                "id": "small-box",
                                "name": "Small Box",
                                "dimensions": {
                                  "length": 20.0,
                                  "width": 15.0,
                                  "height": 10.0,
                                  "unit": "CM"
                                }
                              },
                              "items": [
                                {
                                  "sku": "BOOK-001",
                                  "quantity": 2
                                }
                              ],
                              "utilizationPercentage": 75.5
                            }
                          ],
                          "totalPackages": 1,
                          "totalWeight": "1.2 KG",
                          "efficiency": 85.2
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request data",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation error",
                    value = """
                        {
                          "error": "Validation Failed",
                          "message": "Request validation failed",
                          "details": [
                            "Item SKU is required",
                            "Item quantity must be positive"
                          ],
                          "timestamp": "2024-01-15T10:30:00Z",
                          "path": "/api/v1/packing-solutions"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "422", 
            description = "Business rule violation",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Business rule error",
                    value = """
                        {
                          "error": "Business Rule Violation",
                          "message": "Items cannot be packed with available cartons",
                          "details": [
                            "Item LARGE-ITEM-001 exceeds maximum carton dimensions",
                            "No suitable carton found for fragile items"
                          ],
                          "timestamp": "2024-01-15T10:30:00Z",
                          "path": "/api/v1/packing-solutions"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Server error",
                    value = """
                        {
                          "error": "Internal Server Error",
                          "message": "An unexpected error occurred",
                          "timestamp": "2024-01-15T10:30:00Z",
                          "path": "/api/v1/packing-solutions"
                        }
                        """
                )
            )
        )
    })
    @PostMapping
    public ResponseEntity<PackingSolution> calculatePackingSolution(
            @Valid @RequestBody PackingRequest request,
            @Parameter(
                description = "Optional request ID for tracking. If not provided, one will be generated.",
                example = "req-12345"
            )
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

    @Schema(
        description = "Request to calculate optimal packing solution",
        example = """
            {
              "items": [
                {
                  "sku": "BOOK-001",
                  "quantity": 2
                },
                {
                  "sku": "SHIRT-XL-BLUE", 
                  "quantity": 1
                }
              ],
              "orderId": "ORDER-123456",
              "optimizeForMinimumBoxes": true,
              "allowMixedCategories": true
            }
            """
    )
    public record PackingRequest(
        @Schema(
            description = "List of items to be packed",
            required = true,
            minLength = 1
        )
        @NotEmpty(message = "At least one item is required for packing")
        List<@Valid ItemRequest> items,
        
        @Schema(
            description = "Order identifier for tracking purposes",
            example = "ORDER-123456",
            maxLength = 50
        )
        String orderId,
        
        @Schema(
            description = "Whether to optimize for minimum number of boxes (true) or minimum cost (false)",
            example = "true",
            defaultValue = "true"
        )
        Boolean optimizeForMinimumBoxes,
        
        @Schema(
            description = "Whether to allow items from different categories in the same box",
            example = "true", 
            defaultValue = "true"
        )
        Boolean allowMixedCategories
    ) {}

    @Schema(
        description = "Item to be packed with its quantity",
        example = """
            {
              "sku": "BOOK-001",
              "quantity": 2
            }
            """
    )
    public record ItemRequest(
        @Schema(
            description = "Stock Keeping Unit - unique product identifier",
            example = "BOOK-001",
            pattern = "^[A-Z0-9-_]+$"
        )
        @NotNull(message = "Item SKU is required")
        @Pattern(
            regexp = "^[A-Z0-9-_]+$", 
            message = "Item SKU must contain only uppercase letters, numbers, hyphens, and underscores"
        )
        String sku,
        
        @Schema(
            description = "Quantity of items to pack",
            example = "2",
            minimum = "1"
        )
        @NotNull(message = "Item quantity is required")
        @Positive(message = "Item quantity must be positive")
        Integer quantity
    ) {}
}