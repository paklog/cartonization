package com.paklog.cartonization.domain.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();
}