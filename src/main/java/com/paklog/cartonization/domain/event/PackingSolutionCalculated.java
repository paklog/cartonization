package com.paklog.cartonization.domain.event;

import com.paklog.cartonization.domain.model.entity.PackingSolution;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event published when a packing solution has been calculated.
 * This event is part of the domain layer and represents a significant business event.
 */
public class PackingSolutionCalculated implements DomainEvent {
    private final String solutionId;
    private final String requestId;
    private final String orderId;
    private final Integer packageCount;
    private final BigDecimal totalWeight;
    private final BigDecimal averageUtilization;
    private final Instant occurredOn;

    public PackingSolutionCalculated(String solutionId, String requestId, String orderId,
                                    Integer packageCount, BigDecimal totalWeight,
                                    BigDecimal averageUtilization, Instant occurredOn) {
        this.solutionId = solutionId;
        this.requestId = requestId;
        this.orderId = orderId;
        this.packageCount = packageCount;
        this.totalWeight = totalWeight;
        this.averageUtilization = averageUtilization;
        this.occurredOn = occurredOn;
    }

    public static PackingSolutionCalculated from(PackingSolution solution) {
        return new PackingSolutionCalculated(
            solution.getSolutionId(),
            solution.getRequestId(),
            solution.getOrderId(),
            solution.getTotalPackages(),
            solution.getTotalWeight(),
            solution.getAverageUtilization(),
            solution.getCreatedAt()
        );
    }

    public String getSolutionId() {
        return solutionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getOrderId() {
        return orderId;
    }

    public Integer getPackageCount() {
        return packageCount;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    public BigDecimal getAverageUtilization() {
        return averageUtilization;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackingSolutionCalculated that = (PackingSolutionCalculated) o;
        return Objects.equals(solutionId, that.solutionId) &&
               Objects.equals(requestId, that.requestId) &&
               Objects.equals(orderId, that.orderId) &&
               Objects.equals(packageCount, that.packageCount) &&
               Objects.equals(totalWeight, that.totalWeight) &&
               Objects.equals(averageUtilization, that.averageUtilization) &&
               Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(solutionId, requestId, orderId, packageCount, totalWeight, averageUtilization, occurredOn);
    }

    @Override
    public String toString() {
        return "PackingSolutionCalculated{" +
               "solutionId='" + solutionId + '\'' +
               ", requestId='" + requestId + '\'' +
               ", orderId='" + orderId + '\'' +
               ", packageCount=" + packageCount +
               ", totalWeight=" + totalWeight +
               ", averageUtilization=" + averageUtilization +
               ", occurredOn=" + occurredOn +
               '}';
    }
}
