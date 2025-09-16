package com.paklog.cartonization.domain.model.entity;

import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.valueobject.ItemWithDimensions;
import com.paklog.cartonization.domain.model.valueobject.PackingRules;
import com.paklog.cartonization.domain.model.valueobject.Weight;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Package {
    private final Carton carton;
    private final List<ItemWithDimensions> items;
    private BigDecimal currentWeight;
    private BigDecimal usedVolume;

    private Package(Carton carton) {
        this.carton = carton;
        this.items = new ArrayList<>();
        this.currentWeight = BigDecimal.ZERO;
        this.usedVolume = BigDecimal.ZERO;
    }

    public static Package create(Carton carton) {
        return new Package(carton);
    }

    public boolean canAddItem(ItemWithDimensions item, PackingRules rules) {
        // Check if carton can physically fit the item
        if (!carton.canFitItem(item.getDimensions(), item.getWeight())) {
            return false;
        }

        // Check weight capacity
        Weight newTotalWeight = new Weight(
            currentWeight.add(item.getWeight().getValue()),
            item.getWeight().getUnit()
        );
        if (newTotalWeight.isGreaterThan(carton.getMaxWeight())) {
            return false;
        }

        // Check volume utilization
        BigDecimal newUsedVolume = usedVolume.add(item.getDimensions().volume());
        BigDecimal cartonVolume = carton.getDimensions().volume();
        if (newUsedVolume.divide(cartonVolume, 4, RoundingMode.HALF_UP)
                .compareTo(rules.getMaxUtilizationThreshold()) > 0) {
            return false;
        }

        // Check business rules
        if (rules.shouldSeparateFragileItems() && item.isFragile()) {
            // Check if package already contains non-fragile items
            boolean hasNonFragile = items.stream()
                .anyMatch(existing -> !existing.isFragile());
            if (hasNonFragile) {
                return false;
            }
        }

        if (!rules.shouldAllowMixedCategories()) {
            // Check if all items are from same category
            String itemCategory = item.getCategory();
            boolean hasDifferentCategory = items.stream()
                .anyMatch(existing -> !existing.getCategory().equals(itemCategory));
            if (hasDifferentCategory) {
                return false;
            }
        }

        return true;
    }

    public void addItem(ItemWithDimensions item) {
        addItem(item, PackingRules.defaultRules());
    }

    public void addItem(ItemWithDimensions item, PackingRules rules) {
        if (!canAddItem(item, rules)) {
            throw new IllegalArgumentException("Cannot add item to package: " + item.getSku());
        }

        items.add(item);
        currentWeight = currentWeight.add(item.getWeight().getValue());
        usedVolume = usedVolume.add(item.getDimensions().volume());
    }

    public BigDecimal getUtilization() {
        if (carton.getDimensions().volume().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return usedVolume.divide(carton.getDimensions().volume(), 4, RoundingMode.HALF_UP);
    }

    public BigDecimal getRemainingVolume() {
        return carton.getDimensions().volume().subtract(usedVolume);
    }

    public Weight getRemainingWeight() {
        BigDecimal remainingValue = carton.getMaxWeight().getValue().subtract(currentWeight);
        return new Weight(remainingValue, carton.getMaxWeight().getUnit());
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getItemCount() {
        return items.stream().mapToInt(ItemWithDimensions::getQuantity).sum();
    }

    public Carton getCarton() {
        return carton;
    }

    public List<ItemWithDimensions> getItems() {
        return items;
    }

    public BigDecimal getCurrentWeight() {
        return currentWeight;
    }

    public BigDecimal getUsedVolume() {
        return usedVolume;
    }
}