package com.paklog.cartonization.domain.service;

import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.entity.Package;
import com.paklog.cartonization.domain.model.entity.PackingSolution;
import com.paklog.cartonization.domain.model.valueobject.CartonStatus;
import com.paklog.cartonization.domain.model.valueobject.ItemWithDimensions;
import com.paklog.cartonization.domain.model.valueobject.PackingRules;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PackingAlgorithmService {

    public PackingSolution calculateOptimalPacking(
            List<ItemWithDimensions> items,
            List<Carton> availableCartons,
            PackingRules rules) {

        log.info("Starting packing calculation for {} items with {} available carton types",
                items.size(), availableCartons.size());

        // Sort items by volume (largest first) for better packing
        List<ItemWithDimensions> sortedItems = items.stream()
            .sorted((a, b) -> b.getDimensions().volume()
                .compareTo(a.getDimensions().volume()))
            .collect(Collectors.toList());

        // Sort cartons by volume (smallest first) to minimize waste
        List<Carton> sortedCartons = availableCartons.stream()
            .filter(carton -> carton.getStatus() == CartonStatus.ACTIVE)
            .sorted(Comparator.comparing(c -> c.getDimensions().volume()))
            .collect(Collectors.toList());

        // Apply the selected algorithm based on rules
        List<Package> packages = rules.shouldOptimizeForMinimumBoxes()
            ? bestFitDecreasing(sortedItems, sortedCartons, rules)
            : firstFitDecreasing(sortedItems, sortedCartons, rules);

        PackingSolution solution = PackingSolution.create(packages);
        solution.setRequestId("temp-" + System.currentTimeMillis()); // Will be set by application service

        log.info("Packing calculation completed. Solution uses {} packages with {:.2f}% average utilization",
                packages.size(), solution.getAverageUtilization().multiply(BigDecimal.valueOf(100)));

        return solution;
    }

    private List<Package> bestFitDecreasing(
            List<ItemWithDimensions> items,
            List<Carton> cartons,
            PackingRules rules) {

        List<Package> packages = new ArrayList<>();
        Map<String, Package> openPackages = new HashMap<>();

        for (ItemWithDimensions item : items) {
            Package bestPackage = null;
            BigDecimal bestRemainingVolume = null;

            // Try to find the best existing package
            for (Package pkg : openPackages.values()) {
                if (pkg.canAddItem(item, rules)) {
                    BigDecimal remainingVolume = pkg.getRemainingVolume();
                    if (bestPackage == null || remainingVolume.compareTo(bestRemainingVolume) < 0) {
                        bestPackage = pkg;
                        bestRemainingVolume = remainingVolume;
                    }
                }
            }

            // If no suitable package found, create a new one
            if (bestPackage == null) {
                Carton suitableCarton = findSmallestSuitableCarton(item, cartons);
                if (suitableCarton == null) {
                    log.error("No suitable carton found for item: {}", item.getSku());
                    throw new IllegalStateException("Cannot pack item: " + item.getSku());
                }

                bestPackage = Package.create(suitableCarton);
                String packageId = UUID.randomUUID().toString();
                openPackages.put(packageId, bestPackage);
                packages.add(bestPackage);
            }

            bestPackage.addItem(item);
        }

        return packages;
    }

    private List<Package> firstFitDecreasing(
            List<ItemWithDimensions> items,
            List<Carton> cartons,
            PackingRules rules) {

        List<Package> packages = new ArrayList<>();

        for (ItemWithDimensions item : items) {
            boolean packed = false;

            // Try to fit in existing packages
            for (Package pkg : packages) {
                if (pkg.canAddItem(item, rules)) {
                    pkg.addItem(item);
                    packed = true;
                    break;
                }
            }

            // Create new package if needed
            if (!packed) {
                Carton suitableCarton = findSmallestSuitableCarton(item, cartons);
                if (suitableCarton == null) {
                    log.error("No suitable carton found for item: {}", item.getSku());
                    throw new IllegalStateException("Cannot pack item: " + item.getSku());
                }

                Package newPackage = Package.create(suitableCarton);
                newPackage.addItem(item);
                packages.add(newPackage);
            }
        }

        return packages;
    }

    private Carton findSmallestSuitableCarton(ItemWithDimensions item, List<Carton> cartons) {
        return cartons.stream()
            .filter(carton -> carton.canFitItem(item.getDimensions(), item.getWeight()))
            .findFirst()
            .orElse(null);
    }
}