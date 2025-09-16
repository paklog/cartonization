package com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.document;

import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "cartons")
public class CartonDocument {

    @Id
    private String id;

    @Indexed
    private String name;

    private DimensionDocument dimensions;
    private WeightDocument maxWeight;

    @Indexed
    private String status;

    private Instant createdAt;
    private Instant updatedAt;
    
    public CartonDocument() {
    }
    
    public CartonDocument(String id, String name, DimensionDocument dimensions, WeightDocument maxWeight, String status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.dimensions = dimensions;
        this.maxWeight = maxWeight;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public DimensionDocument getDimensions() {
        return dimensions;
    }
    
    public void setDimensions(DimensionDocument dimensions) {
        this.dimensions = dimensions;
    }
    
    public WeightDocument getMaxWeight() {
        return maxWeight;
    }
    
    public void setMaxWeight(WeightDocument maxWeight) {
        this.maxWeight = maxWeight;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartonDocument that = (CartonDocument) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               Objects.equals(dimensions, that.dimensions) &&
               Objects.equals(maxWeight, that.maxWeight) &&
               Objects.equals(status, that.status) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(updatedAt, that.updatedAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name, dimensions, maxWeight, status, createdAt, updatedAt);
    }
    
    @Override
    public String toString() {
        return "CartonDocument{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", dimensions=" + dimensions +
               ", maxWeight=" + maxWeight +
               ", status='" + status + '\'' +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String name;
        private DimensionDocument dimensions;
        private WeightDocument maxWeight;
        private String status;
        private Instant createdAt;
        private Instant updatedAt;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder dimensions(DimensionDocument dimensions) {
            this.dimensions = dimensions;
            return this;
        }
        
        public Builder maxWeight(WeightDocument maxWeight) {
            this.maxWeight = maxWeight;
            return this;
        }
        
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        
        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public CartonDocument build() {
            return new CartonDocument(id, name, dimensions, maxWeight, status, createdAt, updatedAt);
        }
    }

    public static class DimensionDocument {
        private BigDecimal length;
        private BigDecimal width;
        private BigDecimal height;
        private String unit;
        
        public DimensionDocument() {
        }
        
        public DimensionDocument(BigDecimal length, BigDecimal width, BigDecimal height, String unit) {
            this.length = length;
            this.width = width;
            this.height = height;
            this.unit = unit;
        }
        
        public BigDecimal getLength() {
            return length;
        }
        
        public void setLength(BigDecimal length) {
            this.length = length;
        }
        
        public BigDecimal getWidth() {
            return width;
        }
        
        public void setWidth(BigDecimal width) {
            this.width = width;
        }
        
        public BigDecimal getHeight() {
            return height;
        }
        
        public void setHeight(BigDecimal height) {
            this.height = height;
        }
        
        public String getUnit() {
            return unit;
        }
        
        public void setUnit(String unit) {
            this.unit = unit;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DimensionDocument that = (DimensionDocument) o;
            return Objects.equals(length, that.length) &&
                   Objects.equals(width, that.width) &&
                   Objects.equals(height, that.height) &&
                   Objects.equals(unit, that.unit);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(length, width, height, unit);
        }
        
        @Override
        public String toString() {
            return "DimensionDocument{" +
                   "length=" + length +
                   ", width=" + width +
                   ", height=" + height +
                   ", unit='" + unit + '\'' +
                   '}';
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private BigDecimal length;
            private BigDecimal width;
            private BigDecimal height;
            private String unit;
            
            public Builder length(BigDecimal length) {
                this.length = length;
                return this;
            }
            
            public Builder width(BigDecimal width) {
                this.width = width;
                return this;
            }
            
            public Builder height(BigDecimal height) {
                this.height = height;
                return this;
            }
            
            public Builder unit(String unit) {
                this.unit = unit;
                return this;
            }
            
            public DimensionDocument build() {
                return new DimensionDocument(length, width, height, unit);
            }
        }
    }

    public static class WeightDocument {
        private BigDecimal value;
        private String unit;
        
        public WeightDocument() {
        }
        
        public WeightDocument(BigDecimal value, String unit) {
            this.value = value;
            this.unit = unit;
        }
        
        public BigDecimal getValue() {
            return value;
        }
        
        public void setValue(BigDecimal value) {
            this.value = value;
        }
        
        public String getUnit() {
            return unit;
        }
        
        public void setUnit(String unit) {
            this.unit = unit;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WeightDocument that = (WeightDocument) o;
            return Objects.equals(value, that.value) &&
                   Objects.equals(unit, that.unit);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(value, unit);
        }
        
        @Override
        public String toString() {
            return "WeightDocument{" +
                   "value=" + value +
                   ", unit='" + unit + '\'' +
                   '}';
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private BigDecimal value;
            private String unit;
            
            public Builder value(BigDecimal value) {
                this.value = value;
                return this;
            }
            
            public Builder unit(String unit) {
                this.unit = unit;
                return this;
            }
            
            public WeightDocument build() {
                return new WeightDocument(value, unit);
            }
        }
    }
}