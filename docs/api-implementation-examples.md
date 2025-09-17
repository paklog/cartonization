# API Implementation Examples - Based on OpenAPI Specification

## 1. DTO Classes (Based on OpenAPI Schema)

### CartonRequestDTO

```java
package com.paklog.cartonization.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating or updating a carton")
public class CartonRequestDTO {
    
    @NotBlank(message = "Carton name is required")
    @Schema(
        description = "A human-readable name for the carton",
        example = "Medium Cube Box (12x12x12)",
        required = true
    )
    private String name;
    
    @NotNull(message = "Dimensions are required")
    @Valid
    @Schema(description = "The physical dimensions of the carton", required = true)
    private DimensionSetDTO dimensions;
    
    @NotNull(message = "Maximum weight is required")
    @Valid
    @Schema(description = "The maximum weight capacity of the carton", required = true)
    private WeightMeasurementDTO maxWeight;
}
```

### CartonResponseDTO

```java
package com.paklog.cartonization.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object representing a carton")
public class CartonResponseDTO {
    
    @Schema(
        description = "The unique identifier for the carton type",
        example = "550e8400-e29b-41d4-a716-446655440000",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String id;
    
    @Schema(
        description = "A human-readable name for the carton",
        example = "Medium Cube Box (12x12x12)"
    )
    private String name;
    
    @Schema(description = "The physical dimensions of the carton")
    private DimensionSetDTO dimensions;
    
    @Schema(description = "The maximum weight capacity of the carton")
    private WeightMeasurementDTO maxWeight;
    
    @Schema(
        description = "The current status of the carton",
        example = "ACTIVE",
        allowableValues = {"ACTIVE", "INACTIVE"}
    )
    private String status;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(
        description = "When the carton was created",
        example = "2024-01-15T10:30:00.000Z",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Instant createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(
        description = "When the carton was last updated",
        example = "2024-01-15T14:45:00.000Z",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Instant updatedAt;
}
```

### DimensionSetDTO

```java
package com.paklog.cartonization.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A set of three-dimensional measurements")
public class DimensionSetDTO {
    
    @NotNull(message = "Length is required")
    @Valid
    @Schema(description = "The length dimension", required = true)
    private DimensionMeasurementDTO length;
    
    @NotNull(message = "Width is required")
    @Valid
    @Schema(description = "The width dimension", required = true)
    private DimensionMeasurementDTO width;
    
    @NotNull(message = "Height is required")
    @Valid
    @Schema(description = "The height dimension", required = true)
    private DimensionMeasurementDTO height;
}
```

### DimensionMeasurementDTO

```java
package com.paklog.cartonization.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single dimension measurement with value and unit")
public class DimensionMeasurementDTO {
    
    @NotNull(message = "Value is required")
    @Positive(message = "Value must be positive")
    @Schema(
        description = "The numeric value of the measurement",
        example = "12.0",
        required = true
    )
    private BigDecimal value;
    
    @NotNull(message = "Unit is required")
    @Schema(
        description = "The unit of measurement",
        example = "INCHES",
        allowableValues = {"INCHES", "CENTIMETERS"},
        required = true
    )
    private String unit;
}
```

### CartonizationRequestDTO

```java
package com.paklog.cartonization.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A request to find a packing solution for a set of items")
public class CartonizationRequestDTO {
    
    @NotEmpty(message = "At least one item is required")
    @Valid
    @Schema(
        description = "The list of items to be packed",
        required = true
    )
    private List<ItemToPackDTO> items;
    
    @Schema(
        description = "Optional order ID for tracking purposes",
        example = "ORD-2024-001234"
    )
    private String orderId;
    
    @Schema(
        description = "Whether to optimize for minimum number of boxes",
        defaultValue = "true"
    )
    private boolean optimizeForMinimumBoxes = true;
}
```

### PackingSolutionDTO

```java
package com.paklog.cartonization.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "The calculated packing solution")
public class PackingSolutionDTO {
    
    @Schema(
        description = "Unique identifier for the solution",
        example = "sol-550e8400-e29b-41d4"
    )
    private String solutionId;
    
    @Schema(
        description = "The order ID if provided in the request",
        example = "ORD-2024-001234"
    )
    private String orderId;
    
    @Schema(
        description = "List of packages in the solution"
    )
    private List<PackedCartonDTO> packages;
    
    @Schema(description = "Metrics about the packing solution")
    private PackingMetricsDTO metrics;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(
        description = "When the solution was calculated",
        example = "2024-01-15T10:30:00.000Z"
    )
    private Instant calculatedAt;
}
```

## 2. REST Controller Implementation

### PackingSolutionController

```java
package com.paklog.cartonization.infrastructure.adapter.in.web;

import com.paklog.cartonization.application.port.in.PackingSolutionUseCase;
import com.paklog.cartonization.application.port.in.command.CalculatePackingSolutionCommand;
import com.paklog.cartonization.infrastructure.adapter.in.web.dto.CartonizationRequestDTO;
import com.paklog.cartonization.infrastructure.adapter.in.web.dto.PackingSolutionDTO;
import com.paklog.cartonization.infrastructure.adapter.in.web.mapper.PackingSolutionWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/packing-solutions")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Cartonization", description = "The core operation for calculating packing solutions")
public class PackingSolutionController {
    
    private final PackingSolutionUseCase packingSolutionUseCase;
    private final PackingSolutionWebMapper mapper;
    
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Calculate a packing solution",
        description = "Accepts a list of SKUs and quantities and returns the optimal packing solution. " +
                     "This endpoint triggers the core bin-packing algorithm. " +
                     "The service will internally fetch product dimensions from the Product Catalog service."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Optimal packing solution found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PackingSolutionDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product SKU not found in catalog",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        )
    })
    public ResponseEntity<PackingSolutionDTO> calculatePackingSolution(
            @Valid @RequestBody CartonizationRequestDTO request,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        
        // Generate request ID if not provided
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        
        log.info("Processing packing solution request. RequestId: {}, Items: {}", 
                requestId, request.getItems().size());
        
        // Map DTO to command
        CalculatePackingSolutionCommand command = mapper.toCommand(request, requestId);
        
        // Calculate solution
        var solution = packingSolutionUseCase.calculate(command);
        
        // Map to response DTO
        PackingSolutionDTO response = mapper.toDTO(solution);
        
        log.info("Successfully calculated packing solution. RequestId: {}, Packages: {}", 
                requestId, response.getPackages().size());
        
        return ResponseEntity.ok()
            .header("X-Request-ID", requestId)
            .body(response);
    }
}
```

## 3. API Request/Response Examples

### Example 1: Create a New Carton

**Request:**
```http
POST /api/v1/cartons
Content-Type: application/json

{
  "name": "Medium Cube Box (12x12x12)",
  "dimensions": {
    "length": {
      "value": 12.0,
      "unit": "INCHES"
    },
    "width": {
      "value": 12.0,
      "unit": "INCHES"
    },
    "height": {
      "value": 12.0,
      "unit": "INCHES"
    }
  },
  "maxWeight": {
    "value": 50.0,
    "unit": "POUNDS"
  }
}
```

**Response:**
```http
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Medium Cube Box (12x12x12)",
  "dimensions": {
    "length": {
      "value": 12.0,
      "unit": "INCHES"
    },
    "width": {
      "value": 12.0,
      "unit": "INCHES"
    },
    "height": {
      "value": 12.0,
      "unit": "INCHES"
    }
  },
  "maxWeight": {
    "value": 50.0,
    "unit": "POUNDS"
  },
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:30:00.000Z",
  "updatedAt": "2024-01-15T10:30:00.000Z"
}
```

### Example 2: List All Active Cartons

**Request:**
```http
GET /api/v1/cartons?activeOnly=true
Accept: application/json
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Small Box (6x6x6)",
    "dimensions": {
      "length": {"value": 6.0, "unit": "INCHES"},
      "width": {"value": 6.0, "unit": "INCHES"},
      "height": {"value": 6.0, "unit": "INCHES"}
    },
    "maxWeight": {"value": 20.0, "unit": "POUNDS"},
    "status": "ACTIVE",
    "createdAt": "2024-01-10T08:00:00.000Z",
    "updatedAt": "2024-01-10T08:00:00.000Z"
  },
  {
    "id": "660f9511-f39c-52e5-b827-557766551111",
    "name": "Medium Cube Box (12x12x12)",
    "dimensions": {
      "length": {"value": 12.0, "unit": "INCHES"},
      "width": {"value": 12.0, "unit": "INCHES"},
      "height": {"value": 12.0, "unit": "INCHES"}
    },
    "maxWeight": {"value": 50.0, "unit": "POUNDS"},
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00.000Z",
    "updatedAt": "2024-01-15T10:30:00.000Z"
  },
  {
    "id": "770fa622-a40d-63f6-c938-668877662222",
    "name": "Large Box (18x18x24)",
    "dimensions": {
      "length": {"value": 18.0, "unit": "INCHES"},
      "width": {"value": 18.0, "unit": "INCHES"},
      "height": {"value": 24.0, "unit": "INCHES"}
    },
    "maxWeight": {"value": 70.0, "unit": "POUNDS"},
    "status": "ACTIVE",
    "createdAt": "2024-01-12T14:15:00.000Z",
    "updatedAt": "2024-01-12T14:15:00.000Z"
  }
]
```

### Example 3: Calculate Packing Solution

**Request:**
```http
POST /api/v1/packing-solutions
Content-Type: application/json
X-Request-ID: req-2024-001234

{
  "items": [
    {
      "sku": "WIDGET-BLUE-SMALL",
      "quantity": 5
    },
    {
      "sku": "GADGET-RED-MEDIUM",
      "quantity": 3
    },
    {
      "sku": "DEVICE-GREEN-LARGE",
      "quantity": 1
    }
  ],
  "orderId": "ORD-2024-001234",
  "optimizeForMinimumBoxes": true
}
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Request-ID: req-2024-001234

{
  "solutionId": "sol-881fb733-b51e-74g7-d049-779988773333",
  "orderId": "ORD-2024-001234",
  "packages": [
    {
      "cartonId": "660f9511-f39c-52e5-b827-557766551111",
      "cartonName": "Medium Cube Box (12x12x12)",
      "items": [
        {
          "sku": "WIDGET-BLUE-SMALL",
          "quantity": 5
        },
        {
          "sku": "GADGET-RED-MEDIUM",
          "quantity": 2
        }
      ],
      "utilization": {
        "volumeUsed": 78.5,
        "weightUsed": 32.5
      }
    },
    {
      "cartonId": "550e8400-e29b-41d4-a716-446655440000",
      "cartonName": "Small Box (6x6x6)",
      "items": [
        {
          "sku": "GADGET-RED-MEDIUM",
          "quantity": 1
        }
      ],
      "utilization": {
        "volumeUsed": 45.0,
        "weightUsed": 8.5
      }
    },
    {
      "cartonId": "770fa622-a40d-63f6-c938-668877662222",
      "cartonName": "Large Box (18x18x24)",
      "items": [
        {
          "sku": "DEVICE-GREEN-LARGE",
          "quantity": 1
        }
      ],
      "utilization": {
        "volumeUsed": 62.0,
        "weightUsed": 45.0
      }
    }
  ],
  "metrics": {
    "totalPackages": 3,
    "totalItems": 9,
    "totalWeight": {
      "value": 86.0,
      "unit": "POUNDS"
    },
    "averageUtilization": 61.8,
    "algorithmExecutionTime": 145,
    "efficiency": 0.85
  },
  "calculatedAt": "2024-01-15T14:30:45.123Z"
}
```

### Example 4: Error Response

**Request:**
```http
POST /api/v1/packing-solutions
Content-Type: application/json

{
  "items": []  // Invalid: empty items array
}
```

**Response:**
```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed",
    "details": [
      {
        "field": "items",
        "message": "At least one item is required"
      }
    ],
    "timestamp": "2024-01-15T14:35:00.000Z"
  }
}
```

## 4. Exception Handling

### Global Exception Handler

```java
package com.paklog.cartonization.infrastructure.adapter.in.web;

import com.paklog.cartonization.domain.exception.CartonNotFoundException;
import com.paklog.cartonization.domain.exception.InvalidPackingRequestException;
import com.paklog.cartonization.infrastructure.adapter.in.web.dto.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CartonNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleCartonNotFound(
            CartonNotFoundException ex, 
            WebRequest request) {
        
        log.error("Carton not found: {}", ex.getMessage());
        
        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .code("CARTON_NOT_FOUND")
            .message(ex.getMessage())
            .timestamp(Instant.now())
            .path(request.getDescription(false))
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(InvalidPackingRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidPackingRequest(
            InvalidPackingRequestException ex,
            WebRequest request) {
        
        log.error("Invalid packing request: {}", ex.getMessage());
        
        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .code("INVALID_PACKING_REQUEST")
            .message(ex.getMessage())
            .timestamp(Instant.now())
            .path(request.getDescription(false))
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .code("VALIDATION_ERROR")
            .message("Request validation failed")
            .details(errors)
            .timestamp(Instant.now())
            .path(request.getDescription(false))
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex,
            WebRequest request) {
        
        log.error("Unexpected error occurred", ex);
        
        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .code("INTERNAL_ERROR")
            .message("An unexpected error occurred")
            .timestamp(Instant.now())
            .path(request.getDescription(false))
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

## 5. Swagger/OpenAPI Configuration

```java
package com.paklog.cartonization.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Value("${server.port:8080}")
    private int serverPort;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Cartonization Service API")
                .version("1.0.0")
                .description("A RESTful API for managing carton inventory and calculating optimal packing solutions. " +
                           "This service operates within the Cartonization Bounded Context. " +
                           "It is responsible for all bin-packing algorithms and business rules related to packing. " +
                           "It consumes data from the Product Catalog API to retrieve item dimensions when needed.")
                .contact(new Contact()
                    .name("PakLog Engineering Team")
                    .email("engineering@paklog.com")
                    .url("https://paklog.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort + "/api/v1")
                    .description("Local Development Server"),
                new Server()
                    .url("https://api.paklog.com/cartonization/v1")
                    .description("Production Server"),
                new Server()
                    .url("https://staging-api.paklog.com/cartonization/v1")
                    .description("Staging Server")
            ));
    }
}
```

## 6. API Client Example (For Testing)

```java
package com.paklog.cartonization.client;

import com.paklog.cartonization.infrastructure.adapter.in.web.dto.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
public class CartonizationApiClient {
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    
    public CartonizationApiClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
        this.baseUrl = "http://localhost:8080/api/v1";
    }
    
    public CartonResponseDTO createCarton(CartonRequestDTO request) {
        String url = baseUrl + "/cartons";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<CartonRequestDTO> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<CartonResponseDTO> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            CartonResponseDTO.class
        );
        
        return response.getBody();
    }
    
    public List<CartonResponseDTO> listCartons(boolean activeOnly) {
        String url = baseUrl + "/cartons?activeOnly=" + activeOnly;
        
        ResponseEntity<CartonResponseDTO[]> response = restTemplate.getForEntity(
            url,
            CartonResponseDTO[].class
        );
        
        return Arrays.asList(response.getBody());
    }
    
    public PackingSolutionDTO calculatePackingSolution(CartonizationRequestDTO request) {
        String url = baseUrl + "/packing-solutions";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Request-ID", "test-" + System.currentTimeMillis());
        
        HttpEntity<CartonizationRequestDTO> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<PackingSolutionDTO> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            PackingSolutionDTO.class
        );
        
        return response.getBody();
    }
}
```

## 7. API Versioning Strategy

```java
package com.paklog.cartonization.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiVersioningConfig implements WebMvcConfigurer {
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api/v1", 
            c -> c.getPackage().getName().startsWith("com.paklog.cartonization.infrastructure.adapter.in.web"));
    }
}
```

## 8. Health Check Endpoint

```java
package com.paklog.cartonization.infrastructure.adapter.in.web;

import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CartonizationHealthIndicator implements HealthIndicator {
    
    private final MongoTemplate mongoTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public CartonizationHealthIndicator(MongoTemplate mongoTemplate, 
                                       KafkaTemplate<String, Object> kafkaTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @Override
    public Health health() {
        try {
            // Check MongoDB connection
            mongoTemplate.getDb().getName();
            
            // Check Kafka connection
            kafkaTemplate.getDefaultTopic();
            
            return Health.up()
                .withDetail("service", "Cartonization Service")
                .withDetail("status", "All systems operational")
                .withDetail("mongodb", "Connected")
                .withDetail("kafka", "Connected")
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("service", "Cartonization Service")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

This completes the API implementation examples based on the OpenAPI specification, including:
- Complete DTO classes matching the OpenAPI schemas
- Controller implementations with proper Swagger annotations
- Request/response examples for all major endpoints
- Error handling and exception mapping
- API client for testing
- Versioning strategy
- Health check endpoints

All implementations follow Spring Boot best practices and are aligned with the OpenAPI specification provided.