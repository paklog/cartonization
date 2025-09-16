package com.paklog.cartonization.application.port.in.command;

import com.paklog.cartonization.domain.model.valueobject.ItemToPack;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

public final class CalculatePackingSolutionCommand {

    @NotNull(message = "Request ID is required")
    private final String requestId;

    @NotEmpty(message = "At least one item is required for packing")
    @Valid
    private final List<ItemToPack> items;

    private final String orderId;

    private final boolean optimizeForMinimumBoxes;

    private final boolean allowMixedCategories;

    private CalculatePackingSolutionCommand(String requestId, List<ItemToPack> items, String orderId, boolean optimizeForMinimumBoxes, boolean allowMixedCategories) {
        this.requestId = requestId;
        this.items = items;
        this.orderId = orderId;
        this.optimizeForMinimumBoxes = optimizeForMinimumBoxes;
        this.allowMixedCategories = allowMixedCategories;
    }

    public static CalculatePackingSolutionCommand create(String requestId, List<ItemToPack> items) {
        return CalculatePackingSolutionCommand.builder()
            .requestId(requestId)
            .items(items)
            .optimizeForMinimumBoxes(true)
            .allowMixedCategories(true)
            .build();
    }

    public String getRequestId() {
        return requestId;
    }

    public List<ItemToPack> getItems() {
        return items;
    }

    public String getOrderId() {
        return orderId;
    }

    public boolean isOptimizeForMinimumBoxes() {
        return optimizeForMinimumBoxes;
    }

    public boolean isAllowMixedCategories() {
        return allowMixedCategories;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalculatePackingSolutionCommand that = (CalculatePackingSolutionCommand) o;
        return optimizeForMinimumBoxes == that.optimizeForMinimumBoxes &&
               allowMixedCategories == that.allowMixedCategories &&
               Objects.equals(requestId, that.requestId) &&
               Objects.equals(items, that.items) &&
               Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, items, orderId, optimizeForMinimumBoxes, allowMixedCategories);
    }

    @Override
    public String toString() {
        return "CalculatePackingSolutionCommand{" +
               "requestId='" + requestId + '\'' +
               ", items=" + items +
               ", orderId='" + orderId + '\'' +
               ", optimizeForMinimumBoxes=" + optimizeForMinimumBoxes +
               ", allowMixedCategories=" + allowMixedCategories +
               '}';
    }

    public static class Builder {
        private String requestId;
        private List<ItemToPack> items;
        private String orderId;
        private boolean optimizeForMinimumBoxes;
        private boolean allowMixedCategories;

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder items(List<ItemToPack> items) {
            this.items = items;
            return this;
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder optimizeForMinimumBoxes(boolean optimizeForMinimumBoxes) {
            this.optimizeForMinimumBoxes = optimizeForMinimumBoxes;
            return this;
        }

        public Builder allowMixedCategories(boolean allowMixedCategories) {
            this.allowMixedCategories = allowMixedCategories;
            return this;
        }

        public CalculatePackingSolutionCommand build() {
            return new CalculatePackingSolutionCommand(requestId, items, orderId, optimizeForMinimumBoxes, allowMixedCategories);
        }
    }
}