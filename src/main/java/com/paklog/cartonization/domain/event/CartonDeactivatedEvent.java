package com.paklog.cartonization.domain.event;

import java.time.Instant;

public record CartonDeactivatedEvent(
    String cartonId,
    Instant occurredOn
) implements DomainEvent {

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }
}