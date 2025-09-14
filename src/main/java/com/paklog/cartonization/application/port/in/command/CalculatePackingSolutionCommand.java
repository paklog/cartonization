package com.paklog.cartonization.application.port.in.command;

import com.paklog.cartonization.domain.model.valueobject.ItemToPack;
import lombok.Builder;
import lombok.Value;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Value
@Builder
public class CalculatePackingSolutionCommand {

    @NotNull(message = "Request ID is required")
    String requestId;

    @NotEmpty(message = "At least one item is required for packing")
    @Valid
    List<ItemToPack> items;

    String orderId;

    boolean optimizeForMinimumBoxes;

    boolean allowMixedCategories;

    public static CalculatePackingSolutionCommand create(String requestId, List<ItemToPack> items) {
        return CalculatePackingSolutionCommand.builder()
            .requestId(requestId)
            .items(items)
            .optimizeForMinimumBoxes(true)
            .allowMixedCategories(true)
            .build();
    }
}