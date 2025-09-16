package com.paklog.cartonization.domain.model.aggregate;

import com.paklog.cartonization.domain.event.CartonCreatedEvent;
import com.paklog.cartonization.domain.event.CartonDeactivatedEvent;
import com.paklog.cartonization.domain.event.CartonUpdatedEvent;
import com.paklog.cartonization.domain.event.DomainEvent;
import com.paklog.cartonization.domain.model.valueobject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Carton {

    private static final Logger log = LoggerFactory.getLogger(Carton.class);

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
        Carton carton = new Carton(id, name, dimensions, maxWeight, status, createdAt, updatedAt);
        return carton;
    }

    // Private constructor for reconstitution
    private Carton(CartonId id, String name, DimensionSet dimensions,
                   Weight maxWeight, CartonStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.dimensions = dimensions;
        this.maxWeight = maxWeight;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
        
        addDomainEvent(new CartonUpdatedEvent(
            id.getValue(),
            name,
            dimensions,
            maxWeight,
            updatedAt
        ));
        
        log.info("Updated dimensions for carton: {}", id.getValue());
    }

    public void updateName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Carton name cannot be empty");
        }
        this.name = newName.trim();
        this.updatedAt = Instant.now();
        
        addDomainEvent(new CartonUpdatedEvent(
            id.getValue(),
            name,
            dimensions,
            maxWeight,
            updatedAt
        ));
        
        log.info("Updated name for carton: {}", id.getValue());
    }

    public void updateMaxWeight(Weight newMaxWeight) {
        validateWeight(newMaxWeight);
        this.maxWeight = newMaxWeight;
        this.updatedAt = Instant.now();
        
        addDomainEvent(new CartonUpdatedEvent(
            id.getValue(),
            name,
            dimensions,
            maxWeight,
            updatedAt
        ));
        
        log.info("Updated max weight for carton: {}", id.getValue());
    }

    public void updateCarton(String newName, DimensionSet newDimensions, Weight newMaxWeight) {
        validateCartonData(newName, newDimensions, newMaxWeight);
        
        this.name = newName.trim();
        this.dimensions = newDimensions;
        this.maxWeight = newMaxWeight;
        this.updatedAt = Instant.now();
        
        addDomainEvent(new CartonUpdatedEvent(
            id.getValue(),
            name,
            dimensions,
            maxWeight,
            updatedAt
        ));
        
        log.info("Updated carton: {}", id.getValue());
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

    public CartonId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DimensionSet getDimensions() {
        return dimensions;
    }

    public Weight getMaxWeight() {
        return maxWeight;
    }

    public CartonStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}