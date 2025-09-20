# Domain Model

This document provides a detailed description of the core domain objects within the Cartonization Bounded Context. These objects represent the key concepts of the domain and encapsulate the business logic and rules.

The domain model is structured according to the principles of Domain-Driven Design (DDD), and is organized into Aggregates, Entities, and Value Objects.

## Table of Contents

- [Aggregates](#aggregates)
  - [Carton](#carton)
  - [PackingSolution](#packingsolution)
- [Entities](#entities)
  - [Package](#package)
- [Value Objects](#value-objects)
  - [CartonId](#cartonid)
  - [DimensionSet](#dimensionset)
  - [Weight](#weight)
  - [ItemToPack](#itemtopack)
  - [SKU](#sku)
  - [CartonStatus](#cartonstatus)

---

## Aggregates

Aggregates are clusters of domain objects that can be treated as a single unit. Each aggregate has a root entity, known as the Aggregate Root. The Aggregate Root is the only member of the aggregate that outside objects are allowed to hold references to.

### Carton

-   **Role:** Aggregate Root
-   **Description:** Represents a type of physical box available for packing items. It is a central concept in the domain and acts as a consistency boundary for all operations related to carton management. It ensures that any changes to a carton's state are valid and consistent.
-   **Attributes:**
    -   `id` (CartonId): The unique identifier for the carton.
    -   `name` (String): The human-readable name of the carton (e.g., "Small Box", "Large Box").
    -   `dimensions` (DimensionSet): The physical dimensions (length, width, height) of the carton.
    -   `maxWeight` (Weight): The maximum weight the carton can hold.
    -   `status` (CartonStatus): The current status of the carton (e.g., ACTIVE, INACTIVE).
-   **Key Behaviors:**
    -   `create()`: Factory method to create a new carton, ensuring all invariants are met.
    -   `activate()` / `deactivate()`: Manages the lifecycle of the carton.
    -   `canFitItem()`: Checks if an item with given dimensions and weight can be placed in this carton.
    -   `updateDimensions()`, `updateName()`, `updateMaxWeight()`: Methods to modify the carton's properties, ensuring validity.
    -   `pullDomainEvents()`: Collects and clears domain events (e.g., `CartonCreatedEvent`) that occurred on the aggregate.

### PackingSolution

-   **Role:** Aggregate Root
-   **Description:** Represents the complete result of a cartonization request. It contains a list of packages, which are the proposed packing arrangements for a set of items. This aggregate is the primary output of the cartonization process.
-   **Attributes:**
    -   `solutionId` (String): The unique identifier for the packing solution.
    -   `requestId` (String): The ID of the original request this solution is for.
    -   `packages` (List<Package>): The list of packages that make up the solution.
    -   `createdAt` (Instant): The timestamp when the solution was created.
-   **Key Behaviors:**
    -   `create()`: Factory method to create a new packing solution from a list of packages.
    -   `getTotalPackages()`, `getTotalItems()`: Calculates summary metrics for the solution.
    -   `getAverageUtilization()`: Calculates the average volume utilization across all packages.
    -   `isEmpty()`: Checks if the solution is empty.

---

## Entities

Entities are objects that have a distinct identity that runs through time and different states. They are not defined by their attributes, but rather by a thread of continuity and identity.

### Package

-   **Role:** Entity
-   **Description:** Represents a single physical box (a `Carton`) containing a set of packed items. It is a child entity within the `PackingSolution` aggregate. It does not have a global identity and is only meaningful in the context of its parent `PackingSolution`.
-   **Attributes:**
    -   `carton` (Carton): The carton used for this package.
    -   `items` (List<ItemWithDimensions>): The list of items packed into the carton.
    -   `currentWeight` (BigDecimal): The total weight of all items in the package.
    -   `usedVolume` (BigDecimal): The total volume of all items in the package.
-   **Key Behaviors:**
    -   `create()`: Factory method to create a new, empty package with a specific carton.
    -   `canAddItem()`: Checks if a new item can be added to the package, considering weight, volume, and business rules.
    -   `addItem()`: Adds an item to the package if it fits.
    -   `getUtilization()`: Calculates the percentage of the carton's volume that is used.

---

## Value Objects

Value Objects are objects that describe a characteristic or an attribute. They do not have a conceptual identity; they are defined by their attributes. They are immutable and their equality is based on their values.

### CartonId

-   **Role:** Value Object
-   **Description:** A unique identifier for a `Carton` aggregate. It encapsulates the format of the ID.
-   **Attributes:**
    -   `value` (String): The string representation of the ID.

### DimensionSet

-   **Role:** Value Object
-   **Description:** Represents the physical dimensions (length, width, height) of an object. It is immutable and encapsulates logic for volume calculation, unit conversion, and checking if one set of dimensions can contain another.
-   **Attributes:**
    -   `length` (BigDecimal): The length.
    -   `width` (BigDecimal): The width.
    -   `height` (BigDecimal): The height.
    -   `unit` (DimensionUnit): The unit of measurement (e.g., INCHES, CENTIMETERS).

### Weight

-   **Role:** Value Object
-   **Description:** Represents a weight measurement. It is immutable and handles logic for unit conversion and comparison.
-   **Attributes:**
    -   `value` (BigDecimal): The magnitude of the weight.
    -   `unit` (WeightUnit): The unit of measurement (e.g., POUNDS, KILOGRAMS).

### ItemToPack

-   **Role:** Value Object
-   **Description:** Represents a request to pack a certain quantity of a specific item. It is used as input to the cartonization process.
-   **Attributes:**
    -   `sku` (SKU): The SKU of the item.
    -   `quantity` (Integer): The number of units of this item to pack.

### SKU

-   **Role:** Value Object
--   **Description:** Represents a Stock Keeping Unit (SKU), a unique identifier for a product.
-   **Attributes:**
    -   `value` (String): The string representation of the SKU.

### CartonStatus

-   **Role:** Value Object (Enum)
-   **Description:** An enumeration representing the lifecycle status of a `Carton`.
-   **Values:**
    -   `ACTIVE`: The carton is available for use.
    -   `INACTIVE`: The carton is not currently available for use.
