package com.paklog.cartonization.application.port.in.command;

import com.paklog.cartonization.domain.model.valueobject.DimensionSet;
import com.paklog.cartonization.domain.model.valueobject.Weight;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public final class UpdateCartonCommand {

    @NotBlank(message = "Carton ID is required")
    private final String cartonId;

    @NotBlank(message = "Carton name is required")
    private final String name;

    @NotNull(message = "Dimensions are required")
    @Valid
    private final DimensionSet dimensions;

    @NotNull(message = "Max weight is required")
    @Valid
    private final Weight maxWeight;

    public UpdateCartonCommand(String cartonId, String name, DimensionSet dimensions, Weight maxWeight) {
        this.cartonId = cartonId;
        this.name = name;
        this.dimensions = dimensions;
        this.maxWeight = maxWeight;
    }

    public String getCartonId() {
        return cartonId;
    }

    public String getName() {
        return name;
    }

    public DimensionSet getDimensions() {
        return dimensions;
    }

    public Weight getMaxWeight() {
        return maxWeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateCartonCommand that = (UpdateCartonCommand) o;
        return Objects.equals(cartonId, that.cartonId) &&
               Objects.equals(name, that.name) &&
               Objects.equals(dimensions, that.dimensions) &&
               Objects.equals(maxWeight, that.maxWeight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartonId, name, dimensions, maxWeight);
    }

    @Override
    public String toString() {
        return "UpdateCartonCommand{" +
               "cartonId='" + cartonId + '\'' +
               ", name='" + name + '\'' +
               ", dimensions=" + dimensions +
               ", maxWeight=" + maxWeight +
               '}';
    }
}