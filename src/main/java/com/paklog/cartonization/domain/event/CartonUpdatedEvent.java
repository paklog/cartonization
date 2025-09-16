package com.paklog.cartonization.domain.event;

import com.paklog.cartonization.domain.model.valueobject.DimensionSet;
import com.paklog.cartonization.domain.model.valueobject.Weight;

import java.time.Instant;
import java.util.Objects;

public final class CartonUpdatedEvent implements DomainEvent {
    private final String cartonId;
    private final String name;
    private final DimensionSet dimensions;
    private final Weight maxWeight;
    private final Instant occurredOn;

    public CartonUpdatedEvent(String cartonId, String name, DimensionSet dimensions, Weight maxWeight, Instant occurredOn) {
        this.cartonId = cartonId;
        this.name = name;
        this.dimensions = dimensions;
        this.maxWeight = maxWeight;
        this.occurredOn = occurredOn;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    public String getCartonId() {
        return cartonId;
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

    public Instant getOccurredOn() {
        return occurredOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartonUpdatedEvent that = (CartonUpdatedEvent) o;
        return Objects.equals(cartonId, that.cartonId) &&
               Objects.equals(name, that.name) &&
               Objects.equals(dimensions, that.dimensions) &&
               Objects.equals(maxWeight, that.maxWeight) &&
               Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartonId, name, dimensions, maxWeight, occurredOn);
    }

    @Override
    public String toString() {
        return "CartonUpdatedEvent{" +
               "cartonId='" + cartonId + '\'' +
               ", name='" + name + '\'' +
               ", dimensions=" + dimensions +
               ", maxWeight=" + maxWeight +
               ", occurredOn=" + occurredOn +
               '}';
    }
}