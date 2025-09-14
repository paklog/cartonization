package com.paklog.cartonization.application.port.in.command;

import com.paklog.cartonization.domain.model.valueobject.DimensionSet;
import com.paklog.cartonization.domain.model.valueobject.Weight;

public record CreateCartonCommand(
    String name,
    DimensionSet dimensions,
    Weight maxWeight
) {
    public CreateCartonCommand {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Carton name cannot be empty");
        }
        if (dimensions == null) {
            throw new IllegalArgumentException("Dimensions cannot be null");
        }
        if (maxWeight == null) {
            throw new IllegalArgumentException("Max weight cannot be null");
        }
    }
}