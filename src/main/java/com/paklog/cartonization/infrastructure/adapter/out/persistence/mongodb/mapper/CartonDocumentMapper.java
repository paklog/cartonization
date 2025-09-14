package com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.mapper;

import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.valueobject.*;
import com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.document.CartonDocument;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CartonDocumentMapper {

    public CartonDocument toDocument(Carton carton) {
        return CartonDocument.builder()
            .id(carton.getId().getValue())
            .name(carton.getName())
            .dimensions(toDimensionDocument(carton.getDimensions()))
            .maxWeight(toWeightDocument(carton.getMaxWeight()))
            .status(carton.getStatus().name())
            .createdAt(carton.getCreatedAt())
            .updatedAt(carton.getUpdatedAt())
            .build();
    }

    public Carton toDomain(CartonDocument document) {
        return Carton.reconstitute(
            CartonId.of(document.getId()),
            document.getName(),
            toDimensionSet(document.getDimensions()),
            toWeight(document.getMaxWeight()),
            CartonStatus.valueOf(document.getStatus()),
            document.getCreatedAt(),
            document.getUpdatedAt()
        );
    }

    private CartonDocument.DimensionDocument toDimensionDocument(DimensionSet dimensions) {
        return CartonDocument.DimensionDocument.builder()
            .length(dimensions.getLength())
            .width(dimensions.getWidth())
            .height(dimensions.getHeight())
            .unit(dimensions.getUnit().name())
            .build();
    }

    private DimensionSet toDimensionSet(CartonDocument.DimensionDocument document) {
        return new DimensionSet(
            document.getLength(),
            document.getWidth(),
            document.getHeight(),
            DimensionUnit.valueOf(document.getUnit())
        );
    }

    private CartonDocument.WeightDocument toWeightDocument(Weight weight) {
        return CartonDocument.WeightDocument.builder()
            .value(weight.getValue())
            .unit(weight.getUnit().name())
            .build();
    }

    private Weight toWeight(CartonDocument.WeightDocument document) {
        return new Weight(
            document.getValue(),
            WeightUnit.valueOf(document.getUnit())
        );
    }
}