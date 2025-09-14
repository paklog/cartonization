
# Cartonization Service - Sample Implementation Code

## 1. Domain Layer

### Carton Aggregate Root

```java
package com.paklog.cartonization.domain.model.aggregate;

import com.paklog.cartonization.domain.event.CartonCreatedEvent;
import com.paklog.cartonization.domain.event.CartonDeactivatedEvent;
import com.paklog.cartonization.domain.event.DomainEvent;
import com.paklog.cartonization.domain.model.valueobject.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Getter
public class Carton {
    
    private final CartonId id;
    private String name;
    private DimensionSet dimensions;
    private Weight maxWeight;
    private CartonStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    // Private constructor for factory method pattern
    private Carton(CartonId id, String name, DimensionSet dimensions, 
                   Weight maxWeight, CartonStatus status) {
        this.id = id;
        this.name = name;
        this.dimensions = dimensions;
        this.maxWeight = maxWeight;
        this.status = status;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }
    
    // Factory method for creating new carton
    public static Carton create(String name, DimensionSet dimensions, Weight maxWeight) {
        validateCartonData(name, dimensions, maxWeight);
        
        CartonId cartonId = CartonId.generate();
        Carton carton = new Carton(cartonId, name, dimensions, maxWeight, CartonStatus.ACTIVE);
        
        carton.addDomainEvent(new CartonCreatedEvent(
            cartonId.getValue(),
            name,
            dimensions,
            maxWeight,
            Instant.now()
        ));
        
        log.info("Created new carton with ID: {}", cartonId.getValue());
        return carton;
    }
    
    // Factory method for reconstituting from persistence
    public static Carton reconstitute(CartonId id, String name, DimensionSet dimensions,
                                      Weight maxWeight, CartonStatus status, 
                                      Instant createdAt, Instant updatedAt) {
        Carton carton = new Carton(id, name, dimensions, maxWeight, status);
        carton.updatedAt = updatedAt;
        return carton;
    }
    
    // Business methods
    public void activate() {
        if (this.status == CartonStatus.ACTIVE) {
            log.warn("Carton {} is already active", id.getValue());
            return;
        }
        
        this.status = CartonStatus.ACTIVE;
        this.updatedAt = Instant.now();
        log.info("Activated carton: {}", id.getValue());
    }
    
    public void deactivate() {
        if (this.status == CartonStatus.INACTIVE) {
            log.warn("Carton {} is already inactive", id.getValue());
            return;
        }
        
        this.status = CartonStatus.INACTIVE;
        this.updatedAt = Instant.now();
        
        addDomainEvent(new CartonDeactivatedEvent(
            id.getValue(),
            Instant.now()
        ));
        
        log.info("Deactivated carton: {}", id.getValue());
    }
    
    public boolean canFitItem(DimensionSet itemDimensions, Weight itemWeight) {
        // Check weight constraint
        if (itemWeight.isGreaterThan(maxWeight)) {
            return false;
        }
        
        // Check dimensional constraints (considering rotation)
        return dimensions.canContain(itemDimensions);
    }
    
    public void updateDimensions(DimensionSet newDimensions) {
        validateDimensions(newDimensions);
        this.dimensions = newDimensions;
        this.updatedAt = Instant.now();
        log.info("Updated dimensions for carton: {}", id.getValue());
    }
    
    // Domain event handling
    private void addDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }
    
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }
    
    // Validation methods
    private static void validateCartonData(String name, DimensionSet dimensions, Weight maxWeight) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Carton name cannot be empty");
        }
        validateDimensions(dimensions);
        validateWeight(maxWeight);
    }
    
    private static void validateDimensions(DimensionSet dimensions) {
        if (dimensions == null || dimensions.hasZeroOrNegativeValues()) {
            throw new IllegalArgumentException("Invalid carton dimensions");
        }
    }
    
    private static void validateWeight(Weight weight) {
        if (weight == null || weight.isZeroOrNegative()) {
            throw new IllegalArgumentException("Invalid carton weight capacity");
        }
    }
}
```

### Value Objects

#### CartonId Value Object

```java
package com.paklog.cartonization.domain.model.valueobject;

import lombok.Value;
import java.util.UUID;

@Value
public class CartonId {
    String value;
    
    private CartonId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("CartonId cannot be null or empty");
        }
        this.value = value;
    }
    
    public static CartonId of(String value) {
        return new CartonId(value);
    }
    
    public static CartonId generate() {
        return new CartonId(UUID.randomUUID().toString());
    }
    
    @Override
    public String toString() {
        return value;
    }
}
```

#### DimensionSet Value Object

```java
package com.paklog.cartonization.domain.model.valueobject;

import lombok.Value;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

@Value
public class DimensionSet {
    BigDecimal length;
    BigDecimal width;
    BigDecimal height;
    DimensionUnit unit;
    
    private static final BigDecimal INCHES_TO_CM = new BigDecimal("2.54");
    
    public DimensionSet(BigDecimal length, BigDecimal width, BigDecimal height, DimensionUnit unit) {
        validateDimension(length, "Length");
        validateDimension(width, "Width");
        validateDimension(height, "Height");
        
        this.length = length;
        this.width = width;
        this.height = height;
        this.unit = unit;
    }
    
    public BigDecimal volume() {
        return length.multiply(width).multiply(height)
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    public DimensionSet convertTo(DimensionUnit targetUnit) {
        if (this.unit == targetUnit) {
            return this;
        }
        
        BigDecimal conversionFactor = getConversionFactor(this.unit, targetUnit);
        return new DimensionSet(
            length.multiply(conversionFactor),
            width.multiply(conversionFactor),
            height.multiply(conversionFactor),
            targetUnit
        );
    }
    
    public boolean canContain(DimensionSet item) {
        // Convert to same unit for comparison
        DimensionSet normalizedItem = item.convertTo(this.unit);
        
        // Get sorted dimensions for both carton and item
        List<BigDecimal> cartonDims = getSortedDimensions();
        List<BigDecimal> itemDims = normalizedItem.getSortedDimensions();
        
        // Check if item can fit in any orientation
        for (int i = 0; i < 3; i++) {
            if (itemDims.get(i).compareTo(cartonDims.get(i)) > 0) {
                return false;
            }
        }
        return true;
    }
    
    public boolean hasZeroOrNegativeValues() {
        return length.compareTo(BigDecimal.ZERO) <= 0 ||
               width.compareTo(BigDecimal.ZERO) <= 0 ||
               height.compareTo(BigDecimal.ZERO) <= 0;
    }
    
    private List<BigDecimal> getSortedDimensions() {
        List<BigDecimal> dims = Arrays.asList(length, width, height);
        dims.sort(BigDecimal::compareTo);
        return dims;
    }
    
    private BigDecimal getConversionFactor(DimensionUnit from, DimensionUnit to) {
        if (from == DimensionUnit.INCHES && to == DimensionUnit.CENTIMETERS) {
            return INCHES_TO_CM;
        } else if (from == DimensionUnit.CENTIMETERS && to == DimensionUnit.INCHES) {
            return BigDecimal.ONE.divide(INCHES_TO_CM, 4, RoundingMode.HALF_UP);
        }
        return BigDecimal.ONE;
    }
    
    private void validateDimension(BigDecimal value, String dimensionName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(dimensionName + " must be positive");
        }
    }
}
```

### Domain Service

```java
package com.paklog.cartonization.domain.service;

import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.entity.Package;
import com.paklog.cartonization.domain.model.entity.PackingSolution;
import com.paklog.cartonization.domain.model.valueobject.ItemWithDimensions;
import com.paklog.cartonization.domain.model.valueobject.PackingRules;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PackingAlgorithmService {
    
    public PackingSolution calculateOptimalPacking(
            List<ItemWithDimensions> items,
            List<Carton> availableCartons,
            PackingRules rules) {
        
        log.info("Starting packing calculation for {} items with {} available carton types",
                items.size(), availableCartons.size());
        
        // Sort items by volume (largest first) for better packing
        List<ItemWithDimensions> sortedItems = items.stream()
            .sorted((a, b) -> b.getDimensions().volume()
                .compareTo(a.getDimensions().volume()))
            .collect(Collectors.toList());
        
        // Sort cartons by volume (smallest first) to minimize waste
        List<Carton> sortedCartons = availableCartons.stream()
            .filter(c -> c.getStatus() == CartonStatus.ACTIVE)
            .sorted(Comparator.comparing(c -> c.getDimensions().volume()))
            .collect(Collectors.toList());
        
        // Apply the selected algorithm based on rules
        List<Package> packages = rules.isOptimizeForMinimumBoxes() 
            ? bestFitDecreasing(sortedItems, sortedCartons, rules)
            : firstFitDecreasing(sortedItems, sortedCartons, rules);
        
        PackingSolution solution = PackingSolution.create(packages);
        
        log.info("Packing calculation completed. Solution uses {} packages with {:.2f}% average utilization",
                packages.size(), solution.getAverageUtilization() * 100);
        
        return solution;
    }
    
    private List<Package> bestFitDecreasing(
            List<ItemWithDimensions> items,
            List<Carton> cartons,
            PackingRules rules) {
        
        List<Package> packages = new ArrayList<>();
        Map<String, Package> openPackages = new HashMap<>();
        
        for (ItemWithDimensions item : items) {
            Package bestPackage = null;
            BigDecimal bestRemainingVolume = null;
            
            // Try to find the best existing package
            for (Package pkg : openPackages.values()) {
                if (pkg.canAddItem(item, rules)) {
                    BigDecimal remainingVolume = pkg.getRemainingVolume();
                    if (bestPackage == null || remainingVolume.compareTo(bestRemainingVolume) < 0) {
                        bestPackage = pkg;
                        bestRemainingVolume = remainingVolume;
                    }
                }
            }
            
            // If no suitable package found, create a new one
            if (bestPackage == null) {
                Carton suitableCarton = findSmallestSuitableCarton(item, cartons);
                if (suitableCarton == null) {
                    log.error("No suitable carton found for item: {}", item.getSku());
                    throw new IllegalStateException("Cannot pack item: " + item.getSku());
                }
                
                bestPackage = Package.create(suitableCarton);
                String packageId = UUID.randomUUID().toString();
                openPackages.put(packageId, bestPackage);
                packages.add(bestPackage);
            }
            
            bestPackage.addItem(item);
        }
        
        return packages;
    }
    
    private List<Package> firstFitDecreasing(
            List<ItemWithDimensions> items,
            List<Carton> cartons,
            PackingRules rules) {
        
        List<Package> packages = new ArrayList<>();
        
        for (ItemWithDimensions item : items) {
            boolean packed = false;
            
            // Try to fit in existing packages
            for (Package pkg : packages) {
                if (pkg.canAddItem(item, rules)) {
                    pkg.addItem(item);
                    packed = true;
                    break;
                }
            }
            
            // Create new package if needed
            if (!packed) {
                Carton suitableCarton = findSmallestSuitableCarton(item, cartons);
                if (suitableCarton == null) {
                    log.error("No suitable carton found for item: {}", item.getSku());
                    throw new IllegalStateException("Cannot pack item: " + item.getSku());
                }
                
                Package newPackage = Package.create(suitableCarton);
                newPackage.addItem(item);
                packages.add(newPackage);
            }
        }
        
        return packages;
    }
    
    private Carton findSmallestSuitableCarton(ItemWithDimensions item, List<Carton> cartons) {
        return cartons.stream()
            .filter(carton -> carton.canFitItem(item.getDimensions(), item.getWeight()))
            .findFirst()
            .orElse(null);
    }
}
```

## 2. Application Layer

### Input Port

```java
package com.paklog.cartonization.application.port.in;

import com.paklog.cartonization.application.port.in.command.CalculatePackingSolutionCommand;
import com.paklog.cartonization.domain.model.entity.PackingSolution;

public interface PackingSolutionUseCase {
    PackingSolution calculate(CalculatePackingSolutionCommand command);
}
```

### Command Object

```java
package com.paklog.cartonization.application.port.in.command;

import com.paklog.cartonization.domain.model.valueobject.ItemToPack;
import lombok.Builder;
import lombok.Value;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Value
@Builder
public class CalculatePackingSolutionCommand {
    
    @NotNull(message = "Request ID is required")
    String requestId;
    
    @NotEmpty(message = "At least one item is required for packing")
    @Valid
    List<ItemToPack> items;
    
    String orderId;
    
    boolean optimizeForMinimumBoxes;
    
    boolean allowMixedCategories;
}
```

### Application Service

```java
package com.paklog.cartonization.application.service;

import com.paklog.cartonization.application.port.in.PackingSolutionUseCase;
import com.paklog.cartonization.application.port.in.command.CalculatePackingSolutionCommand;
import com.paklog.cartonization.application.port.out.*;
import com.paklog.cartonization.domain.event.PackingSolutionCalculatedEvent;
import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.entity.PackingSolution;
import com.paklog.cartonization.domain.model.valueobject.*;
import com.paklog.cartonization.domain.service.PackingAlgorithmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PackingSolutionService implements PackingSolutionUseCase {
    
    private final PackingAlgorithmService packingAlgorithmService;
    private final CartonRepository cartonRepository;
    private final PackingSolutionRepository solutionRepository;
    private final ProductCatalogClient productCatalogClient;
    private final EventPublisher eventPublisher;
    private final CacheStore cacheStore;
    
    @Override
    public PackingSolution calculate(CalculatePackingSolutionCommand command) {
        log.info("Processing packing solution request: {}", command.getRequestId());
        
        try {
            // Enrich items with product dimensions
            List<ItemWithDimensions> enrichedItems = enrichItemsWithDimensions(command.getItems());
            
            // Get available cartons
            List<Carton> availableCartons = cartonRepository.findAllActive();
            if (availableCartons.isEmpty()) {
                throw new IllegalStateException("No active cartons available");
            }
            
            // Build packing rules from command
            PackingRules rules = PackingRules.builder()
                .optimizeForMinimumBoxes(command.isOptimizeForMinimumBoxes())
                .allowMixedCategories(command.isAllowMixedCategories())
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
            
            // Persist solution
            solutionRepository.save(solution);
            
            // Cache the solution for quick retrieval
            cacheStore.put("solution:" + command.getRequestId(), solution, 3600);
            
            // Publish domain event
            publishSolutionCalculatedEvent(solution);
            
            log.info("Successfully calculated packing solution for request: {}", 
                    command.getRequestId());
            
            return solution;
            
        } catch (Exception e) {
            log.error("Error calculating packing solution for request: {}", 
                    command.getRequestId(), e);
            throw new PackingSolutionException("Failed to calculate packing solution", e);
        }
    }
    
    private List<ItemWithDimensions> enrichItemsWithDimensions(List<ItemToPack> items) {
        return items.stream()
            .map(item -> {
                // Check cache first
                String cacheKey = "product:" + item.getSku().getValue();
                ProductDimensions cached = cacheStore.get(cacheKey, ProductDimensions.class);
                
                ProductDimensions dimensions;
                if (cached != null) {
                    log.debug("Using cached dimensions for SKU: {}", item.getSku());
                    dimensions = cached;
                } else {
                    // Fetch from Product Catalog service
                    dimensions = productCatalogClient.getProductDimensions(item.getSku());
                    // Cache for future use
                    cacheStore.put(cacheKey, dimensions, 3600);
                }
                
                return ItemWithDimensions.builder()
                    .sku(item.getSku())
                    .quantity(item.getQuantity())
                    .dimensions(dimensions.getDimensions())
                    .weight(dimensions.getWeight())
                    .category(dimensions.getCategory())
                    .fragile(dimensions.isFragile())
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    private void publishSolutionCalculatedEvent(PackingSolution solution) {
        PackingSolutionCalculatedEvent event = PackingSolutionCalculatedEvent.builder()
            .solutionId(solution.getSolutionId())
            .requestId(solution.getRequestId())
            .orderId(solution.getOrderId())
            .packageCount(solution.getPackages().size())
            .totalWeight(solution.getTotalWeight())
            .averageUtilization(solution.getAverageUtilization())
            .timestamp(Instant.now())
            .build();
        
        eventPublisher.publish("cartonization.solutions", event);
    }
}
```

### Output Port

```java
package com.paklog.cartonization.application.port.out;

import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.valueobject.CartonId;

import java.util.List;
import java.util.Optional;

public interface CartonRepository {
    
    Carton save(Carton carton);
    
    Optional<Carton> findById(CartonId id);
    
    List<Carton> findAll();
    
    List<Carton> findAllActive();
    
    void deleteById(CartonId id);
    
    boolean existsById(CartonId id);
    
    long count();
}
```

## 3. Infrastructure Layer

### MongoDB Repository Implementation

```java
package com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.repository;

import com.paklog.cartonization.application.port.out.CartonRepository;
import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.valueobject.CartonId;
import com.paklog.cartonization.domain.model.valueobject.CartonStatus;
import com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.document.CartonDocument;
import com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.mapper.CartonDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MongoCartonRepository implements CartonRepository {
    
    private final MongoTemplate mongoTemplate;
    private final CartonDocumentMapper mapper;
    private final SpringDataMongoCartonRepository springDataRepository;
    
    @Override
    public Carton save(Carton carton) {
        log.debug("Saving carton with ID: {}", carton.getId());
        
        CartonDocument document = mapper.toDocument(carton);
        CartonDocument saved = springDataRepository.save(document);
        
        // Publish domain events after successful save
        carton.pullDomainEvents().forEach(event -> {
            // Event publishing logic here
            log.info("Publishing domain event: {}", event.getClass().getSimpleName());
        });
        
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Carton> findById(CartonId id) {
        log.debug("Finding carton by ID: {}", id);
        return springDataRepository.findById(id.getValue())
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Carton> findAll() {
        return springDataRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Carton> findAllActive() {
        Query query = new Query(Criteria.where("status").is(CartonStatus.ACTIVE.name()));
        return mongoTemplate.find(query, CartonDocument.class).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(CartonId id) {
        log.info("Deleting carton with ID: {}", id);
        springDataRepository.deleteById(id.getValue());
    }
    
    @Override
    public boolean existsById(CartonId id) {
        return springDataRepository.existsById(id.getValue());
    }
    
    @Override
    public long count() {
        return springDataRepository.count();
    }
}
```

### REST Controller

```java
package com.paklog.cartonization.infrastructure.adapter.in.web;

import com.paklog.cartonization.application.port.in.CartonManagementUseCase;
import com.paklog.cartonization.application.port.in.command.CreateCartonCommand;
import com.paklog.cartonization.application.port.in.command.UpdateCartonCommand;
import com.paklog.cartonization.infrastructure.adapter.in.web.dto.CartonRequestDTO;
import com.paklog.cartonization.infrastructure.adapter.in.web.dto.CartonResponseDTO;
import com.paklog.cartonization.infrastructure.adapter.in.web.mapper.CartonWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/cartons")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Cartons", description = "Operations for managing the inventory of available shipping cartons")
public class CartonController {
    
    private final CartonManagementUseCase cartonManagementUseCase;
    private final CartonWebMapper mapper;
    
    @PostMapping
    @Operation(
        summary = "Create a new carton type",
        description = "Adds a new carton specification to the inventory"
    )
    @ApiResponse(
        responseCode = "201",
        description = "Carton created successfully",
        content = @Content(schema = @Schema(implementation = CartonResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid carton data")
    public ResponseEntity<CartonResponseDTO> createCarton(
            @Valid @RequestBody CartonRequestDTO request) {
        
        log.info("Creating new carton: {}", request.getName());
        
        CreateCartonCommand command = mapper.toCreateCommand(request);
        var carton = cartonManagementUseCase.createCarton(command);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapper.toResponseDTO(carton));
    }
    
    @GetMapping
    @Operation(
        summary = "List all carton types",
        description = "Retrieves a list of all available cartons in the inventory"
    )
    @ApiResponse(
        responseCode = "200",
        description = "A list of available cartons",
        content = @Content(schema = @Schema(implementation = CartonResponseDTO.class))
    )
    public ResponseEntity<List<CartonResponseDTO>> listCartons(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        
        log.info("Listing cartons (activeOnly: {})", activeOnly);
        
        var cartons = activeOnly 
            ? cartonManagementUseCase.listActiveCartons()
            : cartonManagementUseCase.listAllCartons();
        
        List<CartonResponseDTO> response = cartons.stream()
            .map(mapper::toResponseDTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{cartonId}")
    @Operation(
        summary = "Get carton by ID",
        description = "Retrieves a specific carton by its unique identifier"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successful operation",
        content = @Content(schema = @Schema(implementation = CartonResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Carton not found")
    public ResponseEntity<CartonResponseDTO> getCartonById(
            @Parameter(description = "Unique identifier of the carton")
            @PathVariable String cartonId) {
        
        log.info("Getting carton by ID: {}", cartonId);
        
        var carton = cartonManagementUseCase.getCartonById(cartonId);
        return ResponseEntity.ok(mapper.toResponseDTO(carton));
    }
    
    @PutMapping("/{cartonId}")
    @Operation(
        summary = "Update carton",
        description = "Updates an existing carton's properties"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Carton updated successfully",
        content = @Content(schema = @Schema(implementation = CartonResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Carton not found")
    public ResponseEntity<CartonResponseDTO> updateCarton(
            @PathVariable String cartonId,
            @Valid @RequestBody CartonRequestDTO request) {
        
        log.info("Updating carton: {}", cartonId);
        
        UpdateCartonCommand command = mapper.toUpdateCommand(cartonId, request);
        var carton = cartonManagementUseCase.updateCarton(command);
        
        return ResponseEntity.ok(mapper.toResponseDTO(carton));
    }
    
    @DeleteMapping("/{cartonId}")
    @Operation(
        summary = "Deactivate carton",
        description = "Deactivates a carton type from the inventory"
    )
    @ApiResponse(responseCode = "204", description = "Carton deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Carton not found")
    public ResponseEntity<Void> deactivateCarton(@PathVariable String cartonId) {
        
        log.info("Deactivating carton: {}", cartonId);
        
        cartonManagementUseCase.deactivateCarton(cartonId);
        return ResponseEntity.noContent().build();
    }
}
```

### Kafka Consumer

```java
package com.paklog.cartonization.infrastructure.adapter.in.messaging;

import com.paklog.cartonization.application.port.in.PackingSolutionUseCase;
import com.paklog.cartonization.application.port.in.command.CalculatePackingSolutionCommand;
import com.paklog.cartonization.infrastructure.adapter.in.messaging.event.CartonizationRequestEvent;
import com.paklog.cartonization.infrastructure.adapter.in.messaging.mapper.EventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartonizationRequestConsumer {
    
    private final PackingSolutionUseCase packingSolutionUseCase;
    private final EventMapper eventMapper;
    
    @KafkaListener(
        topics = "${kafka.topics.cartonization-requests}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleCartonizationRequest(
            @Payload CartonizationRequestEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            ConsumerRecord<String, CartonizationRequestEvent> record,
            Acknowledgment acknowledgment) {
        
        log.info("Received cartonization request: {} from topic: {}, partition: {}, offset: {}",
                event.getRequestId(), topic, partition, offset);
        
        try {
            // Convert event to command
            CalculatePackingSolutionCommand command = eventMapper.toCommand(event);
            
            // Process the request
            packingSolutionUseCase.calculate(command);
            
            // Acknowledge successful processing
            acknowledgment.acknowledge();
            
            log.info("Successfully processed cartonization request: {}", event.getRequestId());
            
        } catch (Exception e) {
            log.error("Error processing cartonization request: {}", event.getRequestId(), e);
            
            // Implement retry logic or send to DLQ
            handleProcessingError(event, e, record);
            
            // Still acknowledge to prevent reprocessing
            acknowledgment.acknowledge();
        }
    }
    
    private void handleProcessingError(
            CartonizationRequestEvent event, 
            Exception exception,
            ConsumerRecord<String, CartonizationRequestEvent> record) {
        
        // Log error details
        log.error("Failed to process cartonization request. " +
                "RequestId: {}, Error: {}", 
                event.getRequestId(), exception.getMessage());
        
        // Send to DLQ or implement retry logic
        // This is where you'd implement your error handling strategy
    }
}
```

### Product Catalog Client with Resilience

```java
package com.paklog.cartonization.infrastructure.adapter.out.client;

import com.paklog.cartonization.application.port.out.ProductCatalogClient;
import com.paklog.cartonization.domain.model.valueobject.ProductDimensions;
import com.paklog.cartonization.domain.model.valueobject.SKU;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCatalogRestClient implements ProductCatalogClient {
    
    private final RestTemplate restTemplate;
    private final ProductCatalogClientConfig config;
    
    @Override
    @Cacheable(value = "product-dimensions", key = "#sku.value")
    @Retry(name = "product-catalog")
    @CircuitBreaker(name = "product-catalog", fallbackMethod = "getProductDimensionsFallback")
    public ProductDimensions getProductDimensions(SKU sku) {
        log.debug("Fetching product dimensions for SKU: {}", sku.getValue());
        
        String url = config.getBaseUrl() + "/products/" + sku.getValue();
        
        try {
            ProductResponse response = restTemplate.getForObject(url, ProductResponse.class);
            
            if (response == null) {
                throw new ProductNotFoundException("Product not found: " + sku.getValue());
            }
            
            return mapToProductDim