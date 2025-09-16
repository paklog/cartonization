package com.paklog.cartonization.infrastructure.adapter.in.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class CartonizationResponseEvent {
    private final String requestId;
    private final String orderId;
    private final String solutionId;
    private final String status;
    private final List<PackageResponse> packages;
    private final SolutionMetrics metrics;
    private final String errorMessage;
    private final Instant processedAt;
    private final Long processingTimeMs;

    public CartonizationResponseEvent(String requestId, String orderId, String solutionId, 
                                    String status, List<PackageResponse> packages, 
                                    SolutionMetrics metrics, String errorMessage, 
                                    Instant processedAt, Long processingTimeMs) {
        this.requestId = requestId;
        this.orderId = orderId;
        this.solutionId = solutionId;
        this.status = status;
        this.packages = packages;
        this.metrics = metrics;
        this.errorMessage = errorMessage;
        this.processedAt = processedAt;
        this.processingTimeMs = processingTimeMs;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getSolutionId() {
        return solutionId;
    }

    public String getStatus() {
        return status;
    }

    public List<PackageResponse> getPackages() {
        return packages;
    }

    public SolutionMetrics getMetrics() {
        return metrics;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartonizationResponseEvent that = (CartonizationResponseEvent) o;
        return Objects.equals(requestId, that.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId);
    }

    @Override
    public String toString() {
        return "CartonizationResponseEvent{" +
               "requestId='" + requestId + '\'' +
               ", orderId='" + orderId + '\'' +
               ", solutionId='" + solutionId + '\'' +
               ", status='" + status + '\'' +
               ", packagesCount=" + (packages != null ? packages.size() : 0) +
               ", errorMessage='" + errorMessage + '\'' +
               ", processedAt=" + processedAt +
               ", processingTimeMs=" + processingTimeMs +
               '}';
    }

    public static class PackageResponse {
        private final String cartonId;
        private final String cartonName;
        private final List<String> itemSkus;
        private final Integer totalItems;
        private final BigDecimal utilization;
        private final BigDecimal totalWeight;
        private final BigDecimal usedVolume;

        public PackageResponse(String cartonId, String cartonName, List<String> itemSkus, 
                             Integer totalItems, BigDecimal utilization, BigDecimal totalWeight, 
                             BigDecimal usedVolume) {
            this.cartonId = cartonId;
            this.cartonName = cartonName;
            this.itemSkus = itemSkus;
            this.totalItems = totalItems;
            this.utilization = utilization;
            this.totalWeight = totalWeight;
            this.usedVolume = usedVolume;
        }

        public String getCartonId() {
            return cartonId;
        }

        public String getCartonName() {
            return cartonName;
        }

        public List<String> getItemSkus() {
            return itemSkus;
        }

        public Integer getTotalItems() {
            return totalItems;
        }

        public BigDecimal getUtilization() {
            return utilization;
        }

        public BigDecimal getTotalWeight() {
            return totalWeight;
        }

        public BigDecimal getUsedVolume() {
            return usedVolume;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PackageResponse that = (PackageResponse) o;
            return Objects.equals(cartonId, that.cartonId) &&
                   Objects.equals(itemSkus, that.itemSkus);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cartonId, itemSkus);
        }

        @Override
        public String toString() {
            return "PackageResponse{" +
                   "cartonId='" + cartonId + '\'' +
                   ", cartonName='" + cartonName + '\'' +
                   ", itemsCount=" + (itemSkus != null ? itemSkus.size() : 0) +
                   ", totalItems=" + totalItems +
                   ", utilization=" + utilization +
                   ", totalWeight=" + totalWeight +
                   ", usedVolume=" + usedVolume +
                   '}';
        }
    }

    public static class SolutionMetrics {
        private final Integer totalPackages;
        private final Integer totalItems;
        private final BigDecimal averageUtilization;
        private final BigDecimal totalVolume;
        private final BigDecimal totalWeight;

        public SolutionMetrics(Integer totalPackages, Integer totalItems, 
                             BigDecimal averageUtilization, BigDecimal totalVolume, 
                             BigDecimal totalWeight) {
            this.totalPackages = totalPackages;
            this.totalItems = totalItems;
            this.averageUtilization = averageUtilization;
            this.totalVolume = totalVolume;
            this.totalWeight = totalWeight;
        }

        public Integer getTotalPackages() {
            return totalPackages;
        }

        public Integer getTotalItems() {
            return totalItems;
        }

        public BigDecimal getAverageUtilization() {
            return averageUtilization;
        }

        public BigDecimal getTotalVolume() {
            return totalVolume;
        }

        public BigDecimal getTotalWeight() {
            return totalWeight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SolutionMetrics that = (SolutionMetrics) o;
            return Objects.equals(totalPackages, that.totalPackages) &&
                   Objects.equals(totalItems, that.totalItems) &&
                   Objects.equals(averageUtilization, that.averageUtilization) &&
                   Objects.equals(totalVolume, that.totalVolume) &&
                   Objects.equals(totalWeight, that.totalWeight);
        }

        @Override
        public int hashCode() {
            return Objects.hash(totalPackages, totalItems, averageUtilization, totalVolume, totalWeight);
        }

        @Override
        public String toString() {
            return "SolutionMetrics{" +
                   "totalPackages=" + totalPackages +
                   ", totalItems=" + totalItems +
                   ", averageUtilization=" + averageUtilization +
                   ", totalVolume=" + totalVolume +
                   ", totalWeight=" + totalWeight +
                   '}';
        }
    }
}