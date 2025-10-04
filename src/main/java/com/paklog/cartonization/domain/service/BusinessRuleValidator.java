package com.paklog.cartonization.domain.service;

import com.paklog.cartonization.domain.model.valueobject.ItemWithDimensions;
import com.paklog.cartonization.domain.model.valueobject.PackingRules;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BusinessRuleValidator {

    public void validatePackingRequest(List<ItemWithDimensions> items, PackingRules rules) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Packing request must contain at least one item");
        }

        validateItemConstraints(items);
        validatePackingRules(rules);
        validateItemCompatibility(items, rules);
    }

    public boolean canPackTogether(ItemWithDimensions item1, ItemWithDimensions item2, PackingRules rules) {
        // Check category mixing rules
        if (!rules.shouldAllowMixedCategories()) {
            if (!item1.getCategory().equals(item2.getCategory())) {
                return false;
            }
        }

        // Check fragile item separation rules
        if (rules.shouldSeparateFragileItems()) {
            if (item1.isFragile() && !item2.isFragile()) {
                return false;
            }
            if (!item1.isFragile() && item2.isFragile()) {
                return false;
            }
        }

        return true;
    }

    public boolean isItemValid(ItemWithDimensions item) {
        if (item == null) {
            return false;
        }

        if (item.getSku() == null || item.getSku().getValue().trim().isEmpty()) {
            return false;
        }

        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            return false;
        }

        if (item.getDimensions() == null || item.getDimensions().hasZeroOrNegativeValues()) {
            return false;
        }

        if (item.getWeight() == null || item.getWeight().isZeroOrNegative()) {
            return false;
        }

        return true;
    }

    public boolean exceedsWeightThreshold(BigDecimal totalWeight, BigDecimal maxWeight, BigDecimal threshold) {
        if (totalWeight == null || maxWeight == null || threshold == null) {
            return true;
        }

        BigDecimal utilization = totalWeight.divide(maxWeight, 4, RoundingMode.HALF_UP);
        return utilization.compareTo(threshold) > 0;
    }

    public boolean exceedsVolumeThreshold(BigDecimal usedVolume, BigDecimal totalVolume, BigDecimal threshold) {
        if (usedVolume == null || totalVolume == null || threshold == null) {
            return true;
        }

        if (totalVolume.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }

        BigDecimal utilization = usedVolume.divide(totalVolume, 4, RoundingMode.HALF_UP);
        return utilization.compareTo(threshold) > 0;
    }

    private void validateItemConstraints(List<ItemWithDimensions> items) {
        for (ItemWithDimensions item : items) {
            if (!isItemValid(item)) {
                throw new IllegalArgumentException("Invalid item in packing request: " + item);
            }

            // Check for reasonable size limits
            BigDecimal maxDimension = item.getDimensions().getLength()
                .max(item.getDimensions().getWidth())
                .max(item.getDimensions().getHeight());

            if (maxDimension.compareTo(BigDecimal.valueOf(1000)) > 0) {
                throw new IllegalArgumentException("Item dimensions exceed maximum allowed size: " + item.getSku());
            }

            // Check for reasonable weight limits
            if (item.getWeight().getValue().compareTo(BigDecimal.valueOf(1000)) > 0) {
                throw new IllegalArgumentException("Item weight exceeds maximum allowed weight: " + item.getSku());
            }
        }
    }

    private void validatePackingRules(PackingRules rules) {
        if (rules == null) {
            throw new IllegalArgumentException("Packing rules cannot be null");
        }

        BigDecimal threshold = rules.getMaxUtilizationThreshold();
        if (threshold == null || threshold.compareTo(BigDecimal.ZERO) <= 0 || threshold.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Max utilization threshold must be between 0 and 1");
        }
    }

    private void validateItemCompatibility(List<ItemWithDimensions> items, PackingRules rules) {
        if (!rules.shouldAllowMixedCategories()) {
            String firstCategory = items.get(0).getCategory();
            boolean hasMultipleCategories = items.stream()
                .anyMatch(item -> !item.getCategory().equals(firstCategory));

            if (hasMultipleCategories) {
                throw new IllegalArgumentException("Mixed categories not allowed according to packing rules");
            }
        }

        if (rules.shouldSeparateFragileItems()) {
            boolean hasFragile = items.stream().anyMatch(ItemWithDimensions::isFragile);
            boolean hasNonFragile = items.stream().anyMatch(item -> !item.isFragile());

            if (hasFragile && hasNonFragile) {
                throw new IllegalArgumentException("Fragile and non-fragile items cannot be packed together according to packing rules");
            }
        }
    }
}
