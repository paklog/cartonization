package com.paklog.cartonization.domain.event;

import com.paklog.cartonization.domain.model.valueobject.DimensionSet;
import com.paklog.cartonization.domain.model.valueobject.Weight;

import java.time.Instant;

public record CartonCreatedEvent(
    String cartonId,
    String name,
    DimensionSet dimensions,
    Weight maxWeight,
    Instant occurredOn
) implements DomainEvent {

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }
}