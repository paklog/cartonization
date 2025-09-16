package com.paklog.cartonization.infrastructure.adapter.in.messaging.event;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class CartonizationRequestEvent {
    private final String requestId;
    private final String orderId;
    private final String customerId;
    private final List<ItemRequest> items;
    private final PackingPreferences preferences;
    private final String priority;
    private final Instant requestedAt;
    private final String source;

    public CartonizationRequestEvent(String requestId, String orderId, String customerId, 
                                   List<ItemRequest> items, PackingPreferences preferences, 
                                   String priority, Instant requestedAt, String source) {
        this.requestId = requestId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
        this.preferences = preferences;
        this.priority = priority;
        this.requestedAt = requestedAt;
        this.source = source;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<ItemRequest> getItems() {
        return items;
    }

    public PackingPreferences getPreferences() {
        return preferences;
    }

    public String getPriority() {
        return priority;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public String getSource() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartonizationRequestEvent that = (CartonizationRequestEvent) o;
        return Objects.equals(requestId, that.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId);
    }

    @Override
    public String toString() {
        return "CartonizationRequestEvent{" +
               "requestId='" + requestId + '\'' +
               ", orderId='" + orderId + '\'' +
               ", customerId='" + customerId + '\'' +
               ", itemsCount=" + (items != null ? items.size() : 0) +
               ", priority='" + priority + '\'' +
               ", requestedAt=" + requestedAt +
               ", source='" + source + '\'' +
               '}';
    }

    public static class ItemRequest {
        private final String sku;
        private final Integer quantity;
        private final String category;
        private final Boolean fragile;

        public ItemRequest(String sku, Integer quantity, String category, Boolean fragile) {
            this.sku = sku;
            this.quantity = quantity;
            this.category = category;
            this.fragile = fragile;
        }

        public String getSku() {
            return sku;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public String getCategory() {
            return category;
        }

        public Boolean getFragile() {
            return fragile;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemRequest that = (ItemRequest) o;
            return Objects.equals(sku, that.sku) &&
                   Objects.equals(quantity, that.quantity) &&
                   Objects.equals(category, that.category) &&
                   Objects.equals(fragile, that.fragile);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sku, quantity, category, fragile);
        }

        @Override
        public String toString() {
            return "ItemRequest{" +
                   "sku='" + sku + '\'' +
                   ", quantity=" + quantity +
                   ", category='" + category + '\'' +
                   ", fragile=" + fragile +
                   '}';
        }
    }

    public static class PackingPreferences {
        private final Boolean optimizeForMinimumBoxes;
        private final Boolean allowMixedCategories;
        private final Boolean separateFragileItems;
        private final Double maxUtilizationThreshold;
        private final String preferredCartonSize;

        public PackingPreferences(Boolean optimizeForMinimumBoxes, Boolean allowMixedCategories, 
                                Boolean separateFragileItems, Double maxUtilizationThreshold, 
                                String preferredCartonSize) {
            this.optimizeForMinimumBoxes = optimizeForMinimumBoxes;
            this.allowMixedCategories = allowMixedCategories;
            this.separateFragileItems = separateFragileItems;
            this.maxUtilizationThreshold = maxUtilizationThreshold;
            this.preferredCartonSize = preferredCartonSize;
        }

        public Boolean getOptimizeForMinimumBoxes() {
            return optimizeForMinimumBoxes;
        }

        public Boolean getAllowMixedCategories() {
            return allowMixedCategories;
        }

        public Boolean getSeparateFragileItems() {
            return separateFragileItems;
        }

        public Double getMaxUtilizationThreshold() {
            return maxUtilizationThreshold;
        }

        public String getPreferredCartonSize() {
            return preferredCartonSize;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PackingPreferences that = (PackingPreferences) o;
            return Objects.equals(optimizeForMinimumBoxes, that.optimizeForMinimumBoxes) &&
                   Objects.equals(allowMixedCategories, that.allowMixedCategories) &&
                   Objects.equals(separateFragileItems, that.separateFragileItems) &&
                   Objects.equals(maxUtilizationThreshold, that.maxUtilizationThreshold) &&
                   Objects.equals(preferredCartonSize, that.preferredCartonSize);
        }

        @Override
        public int hashCode() {
            return Objects.hash(optimizeForMinimumBoxes, allowMixedCategories, separateFragileItems, 
                              maxUtilizationThreshold, preferredCartonSize);
        }

        @Override
        public String toString() {
            return "PackingPreferences{" +
                   "optimizeForMinimumBoxes=" + optimizeForMinimumBoxes +
                   ", allowMixedCategories=" + allowMixedCategories +
                   ", separateFragileItems=" + separateFragileItems +
                   ", maxUtilizationThreshold=" + maxUtilizationThreshold +
                   ", preferredCartonSize='" + preferredCartonSize + '\'' +
                   '}';
        }
    }
}