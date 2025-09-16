package com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Document(collection = "packing_solutions")
public class PackingSolutionDocument {

    @Id
    private String solutionId;

    @Indexed
    private String requestId;

    @Indexed
    private String orderId;

    private List<PackageDocument> packages;

    @Indexed
    private Instant createdAt;

    public PackingSolutionDocument() {
    }

    public PackingSolutionDocument(String solutionId, String requestId, String orderId, List<PackageDocument> packages, Instant createdAt) {
        this.solutionId = solutionId;
        this.requestId = requestId;
        this.orderId = orderId;
        this.packages = packages;
        this.createdAt = createdAt;
    }

    public String getSolutionId() {
        return solutionId;
    }

    public void setSolutionId(String solutionId) {
        this.solutionId = solutionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<PackageDocument> getPackages() {
        return packages;
    }

    public void setPackages(List<PackageDocument> packages) {
        this.packages = packages;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackingSolutionDocument that = (PackingSolutionDocument) o;
        return Objects.equals(solutionId, that.solutionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(solutionId);
    }

    @Override
    public String toString() {
        return "PackingSolutionDocument{" +
               "solutionId='" + solutionId + '\'' +
               ", requestId='" + requestId + '\'' +
               ", orderId='" + orderId + '\'' +
               ", packagesCount=" + (packages != null ? packages.size() : 0) +
               ", createdAt=" + createdAt +
               '}';
    }

    public static class PackageDocument {
        private CartonDocument carton;
        private List<ItemDocument> items;
        private String currentWeight;
        private String usedVolume;

        public PackageDocument() {
        }

        public PackageDocument(CartonDocument carton, List<ItemDocument> items, String currentWeight, String usedVolume) {
            this.carton = carton;
            this.items = items;
            this.currentWeight = currentWeight;
            this.usedVolume = usedVolume;
        }

        public CartonDocument getCarton() {
            return carton;
        }

        public void setCarton(CartonDocument carton) {
            this.carton = carton;
        }

        public List<ItemDocument> getItems() {
            return items;
        }

        public void setItems(List<ItemDocument> items) {
            this.items = items;
        }

        public String getCurrentWeight() {
            return currentWeight;
        }

        public void setCurrentWeight(String currentWeight) {
            this.currentWeight = currentWeight;
        }

        public String getUsedVolume() {
            return usedVolume;
        }

        public void setUsedVolume(String usedVolume) {
            this.usedVolume = usedVolume;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PackageDocument that = (PackageDocument) o;
            return Objects.equals(carton, that.carton) &&
                   Objects.equals(items, that.items) &&
                   Objects.equals(currentWeight, that.currentWeight) &&
                   Objects.equals(usedVolume, that.usedVolume);
        }

        @Override
        public int hashCode() {
            return Objects.hash(carton, items, currentWeight, usedVolume);
        }

        @Override
        public String toString() {
            return "PackageDocument{" +
                   "carton=" + carton +
                   ", itemsCount=" + (items != null ? items.size() : 0) +
                   ", currentWeight='" + currentWeight + '\'' +
                   ", usedVolume='" + usedVolume + '\'' +
                   '}';
        }
    }

    public static class ItemDocument {
        private String sku;
        private Integer quantity;
        private CartonDocument.DimensionDocument dimensions;
        private CartonDocument.WeightDocument weight;
        private String category;
        private Boolean fragile;

        public ItemDocument() {
        }

        public ItemDocument(String sku, Integer quantity, CartonDocument.DimensionDocument dimensions, CartonDocument.WeightDocument weight, String category, Boolean fragile) {
            this.sku = sku;
            this.quantity = quantity;
            this.dimensions = dimensions;
            this.weight = weight;
            this.category = category;
            this.fragile = fragile;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public CartonDocument.DimensionDocument getDimensions() {
            return dimensions;
        }

        public void setDimensions(CartonDocument.DimensionDocument dimensions) {
            this.dimensions = dimensions;
        }

        public CartonDocument.WeightDocument getWeight() {
            return weight;
        }

        public void setWeight(CartonDocument.WeightDocument weight) {
            this.weight = weight;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Boolean getFragile() {
            return fragile;
        }

        public void setFragile(Boolean fragile) {
            this.fragile = fragile;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemDocument that = (ItemDocument) o;
            return Objects.equals(sku, that.sku) &&
                   Objects.equals(quantity, that.quantity) &&
                   Objects.equals(dimensions, that.dimensions) &&
                   Objects.equals(weight, that.weight) &&
                   Objects.equals(category, that.category) &&
                   Objects.equals(fragile, that.fragile);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sku, quantity, dimensions, weight, category, fragile);
        }

        @Override
        public String toString() {
            return "ItemDocument{" +
                   "sku='" + sku + '\'' +
                   ", quantity=" + quantity +
                   ", dimensions=" + dimensions +
                   ", weight=" + weight +
                   ", category='" + category + '\'' +
                   ", fragile=" + fragile +
                   '}';
        }
    }
}