package com.paklog.cartonization.infrastructure.adapter.out.messaging.event;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class PackingSolutionCalculatedEvent {
    String solutionId;
    String requestId;
    String orderId;
    Integer packageCount;
    BigDecimal totalWeight;
    BigDecimal averageUtilization;
    Instant timestamp;
}