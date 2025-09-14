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