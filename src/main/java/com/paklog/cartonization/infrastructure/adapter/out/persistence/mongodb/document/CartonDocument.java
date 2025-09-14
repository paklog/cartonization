package com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DimensionDocument {
        private BigDecimal length;
        private BigDecimal width;
        private BigDecimal height;
        private String unit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeightDocument {
        private BigDecimal value;
        private String unit;
    }
}