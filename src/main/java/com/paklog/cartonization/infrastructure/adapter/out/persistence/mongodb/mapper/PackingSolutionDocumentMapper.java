package com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.mapper;

import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.entity.Package;
import com.paklog.cartonization.domain.model.entity.PackingSolution;
import com.paklog.cartonization.domain.model.valueobject.*;
import com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.document.CartonDocument;
import com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.document.PackingSolutionDocument;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PackingSolutionDocumentMapper {

    private final CartonDocumentMapper cartonMapper;

    public PackingSolutionDocumentMapper(CartonDocumentMapper cartonMapper) {
        this.cartonMapper = cartonMapper;
    }

    public PackingSolutionDocument toDocument(PackingSolution solution) {
        if (solution == null) {
            return null;
        }

        List<PackingSolutionDocument.PackageDocument> packageDocs = solution.getPackages().stream()
            .map(this::toPackageDocument)
            .collect(Collectors.toList());

        return new PackingSolutionDocument(
            solution.getSolutionId(),
            solution.getRequestId(),
            solution.getOrderId(),
            packageDocs,
            solution.getCreatedAt()
        );
    }

    public PackingSolution fromDocument(PackingSolutionDocument document) {
        if (document == null) {
            return null;
        }

        List<Package> packages = document.getPackages().stream()
            .map(this::fromPackageDocument)
            .collect(Collectors.toList());

        PackingSolution solution = PackingSolution.create(packages);
        
        // Use reflection to set private fields since we're reconstructing from persistence
        setField(solution, "solutionId", document.getSolutionId());
        setField(solution, "requestId", document.getRequestId());
        setField(solution, "orderId", document.getOrderId());
        setField(solution, "createdAt", document.getCreatedAt());

        return solution;
    }

    private PackingSolutionDocument.PackageDocument toPackageDocument(Package pkg) {
        CartonDocument cartonDoc = cartonMapper.toDocument(pkg.getCarton());
        
        List<PackingSolutionDocument.ItemDocument> itemDocs = pkg.getItems().stream()
            .map(this::toItemDocument)
            .collect(Collectors.toList());

        return new PackingSolutionDocument.PackageDocument(
            cartonDoc,
            itemDocs,
            pkg.getCurrentWeight().toString(),
            pkg.getUsedVolume().toString()
        );
    }

    private Package fromPackageDocument(PackingSolutionDocument.PackageDocument doc) {
        Carton carton = cartonMapper.toDomain(doc.getCarton());
        Package pkg = Package.create(carton);

        List<ItemWithDimensions> items = doc.getItems().stream()
            .map(this::fromItemDocument)
            .collect(Collectors.toList());

        // Add items to package (this will update currentWeight and usedVolume)
        items.forEach(pkg::addItem);

        return pkg;
    }

    private PackingSolutionDocument.ItemDocument toItemDocument(ItemWithDimensions item) {
        return new PackingSolutionDocument.ItemDocument(
            item.getSku().getValue(),
            item.getQuantity(),
            toDimensionDocument(item.getDimensions()),
            toWeightDocument(item.getWeight()),
            item.getCategory(),
            item.getFragile()
        );
    }

    private ItemWithDimensions fromItemDocument(PackingSolutionDocument.ItemDocument doc) {
        return ItemWithDimensions.builder()
            .sku(SKU.of(doc.getSku()))
            .quantity(doc.getQuantity())
            .dimensions(fromDimensionDocument(doc.getDimensions()))
            .weight(fromWeightDocument(doc.getWeight()))
            .category(doc.getCategory())
            .fragile(doc.getFragile())
            .build();
    }

    private CartonDocument.DimensionDocument toDimensionDocument(DimensionSet dimensions) {
        return new CartonDocument.DimensionDocument(
            dimensions.getLength(),
            dimensions.getWidth(),
            dimensions.getHeight(),
            dimensions.getUnit().toString()
        );
    }

    private DimensionSet fromDimensionDocument(CartonDocument.DimensionDocument doc) {
        return new DimensionSet(
            doc.getLength(),
            doc.getWidth(),
            doc.getHeight(),
            DimensionUnit.valueOf(doc.getUnit())
        );
    }

    private CartonDocument.WeightDocument toWeightDocument(Weight weight) {
        return new CartonDocument.WeightDocument(
            weight.getValue(),
            weight.getUnit().toString()
        );
    }

    private Weight fromWeightDocument(CartonDocument.WeightDocument doc) {
        return new Weight(
            doc.getValue(),
            WeightUnit.valueOf(doc.getUnit())
        );
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}