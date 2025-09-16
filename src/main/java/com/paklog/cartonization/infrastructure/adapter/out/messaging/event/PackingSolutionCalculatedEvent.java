package com.paklog.cartonization.infrastructure.adapter.out.messaging.event;

import java.util.Objects;

import java.math.BigDecimal;
import java.time.Instant;

public class PackingSolutionCalculatedEvent {
    private final String solutionId;
    private final String requestId;
    private final String orderId;
    private final Integer packageCount;
    private final BigDecimal totalWeight;
    private final BigDecimal averageUtilization;
    private final Instant timestamp;
    
    public PackingSolutionCalculatedEvent(String solutionId, String requestId, String orderId, 
                                        Integer packageCount, BigDecimal totalWeight, 
                                        BigDecimal averageUtilization, Instant timestamp) {
        this.solutionId = solutionId;
        this.requestId = requestId;
        this.orderId = orderId;
        this.packageCount = packageCount;
        this.totalWeight = totalWeight;
        this.averageUtilization = averageUtilization;
        this.timestamp = timestamp;
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
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackingSolutionCalculatedEvent that = (PackingSolutionCalculatedEvent) o;
        return Objects.equals(solutionId, that.solutionId) &&
               Objects.equals(requestId, that.requestId) &&
               Objects.equals(orderId, that.orderId) &&
               Objects.equals(packageCount, that.packageCount) &&
               Objects.equals(totalWeight, that.totalWeight) &&
               Objects.equals(averageUtilization, that.averageUtilization) &&
               Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(solutionId, requestId, orderId, packageCount, totalWeight, averageUtilization, timestamp);
    }
    
    @Override
    public String toString() {
        return "PackingSolutionCalculatedEvent{" +
               "solutionId='" + solutionId + '\'' +
               ", requestId='" + requestId + '\'' +
               ", orderId='" + orderId + '\'' +
               ", packageCount=" + packageCount +
               ", totalWeight=" + totalWeight +
               ", averageUtilization=" + averageUtilization +
               ", timestamp=" + timestamp +
               '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String solutionId;
        private String requestId;
        private String orderId;
        private Integer packageCount;
        private BigDecimal totalWeight;
        private BigDecimal averageUtilization;
        private Instant timestamp;
        
        public Builder solutionId(String solutionId) {
            this.solutionId = solutionId;
            return this;
        }
        
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }
        
        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }
        
        public Builder packageCount(Integer packageCount) {
            this.packageCount = packageCount;
            return this;
        }
        
        public Builder totalWeight(BigDecimal totalWeight) {
            this.totalWeight = totalWeight;
            return this;
        }
        
        public Builder averageUtilization(BigDecimal averageUtilization) {
            this.averageUtilization = averageUtilization;
            return this;
        }
        
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public PackingSolutionCalculatedEvent build() {
            return new PackingSolutionCalculatedEvent(solutionId, requestId, orderId, 
                    packageCount, totalWeight, averageUtilization, timestamp);
        }
    }
}