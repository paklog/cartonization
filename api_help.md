# Cartonization Service API: Developer Guide

## 1. Introduction

Welcome to the Cartonization Service API. This service is the central point for all bin-packing and carton management logic. Its primary purpose is to solve two key challenges:

1.  **Carton Inventory Management**: To maintain a digital inventory of all available shipping cartons (boxes), including their names, dimensions, and weight limits.
2.  **Optimal Packing Calculation**: To calculate the most efficient way to pack a given set of items into the available cartons based on dimensions, weight, and other business rules.

This API is designed for internal services that need to prepare items for shipment, such as warehouse management systems or order fulfillment services. It operates by taking a list of items (SKUs) and returning a "packing solution" that details which boxes to use and what items to place in each box.

**Base URL:** All API endpoints described in this document are relative to the following base URL:
`/api/v1`

---

## 2. Getting Started

### Headers

For operations that create or modify data (like `POST`), you can include an idempotency key to safely retry requests without accidentally performing the same operation twice.

*   `X-Request-ID` (optional, string, uuid): A unique identifier for the request.

---

## 3. API Endpoints: Use Cases and Details

The API is divided into two main functional areas: Carton Management and Cartonization.

### 3.1. Carton Management

These endpoints allow you to manage the inventory of available shipping cartons.

#### 3.1.1. Create a New Carton Type

Adds a new carton specification to the system, making it available for future packing calculations.

*   **Endpoint:** `POST /cartons`
*   **Use Case:** When the warehouse procures a new type of box, you use this endpoint to register its physical properties in the system.

**Request Body:**

The request body must be a JSON object describing the new carton.

*   `name` (string, required): A human-readable name for the carton (e.g., "Small Flat Box (10x8x2)").
*   `length`, `width`, `height` (number, required): The dimensions of the carton.
*   `dimensionUnit` (string, required): The unit for the dimensions. Allowed values: `MM`, `CM`, `M`, `IN`, `FT`.
*   `maxWeight` (number, required): The maximum weight the carton can hold.
*   `weightUnit` (string, required): The unit for the weight. Allowed values: `G`, `KG`, `LB`, `OZ`.

**Example Request:**
```json
{
  "name": "Medium Cube Box (12x12x12)",
  "length": 12,
  "width": 12,
  "height": 12,
  "dimensionUnit": "IN",
  "maxWeight": 20,
  "weightUnit": "LB"
}
```

**Responses:**

*   **`201 Created`**: Indicates the carton was created successfully. The response body will contain the full carton object, including its server-generated unique `id`.

**Example Success Response:**
```json
{
  "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
  "name": "Medium Cube Box (12x12x12)",
  "dimensions": {
    "length": { "value": 12.0, "unit": "IN" },
    "width": { "value": 12.0, "unit": "IN" },
    "height": { "value": 12.0, "unit": "IN" }
  },
  "maxWeight": {
    "value": 20.0,
    "unit": "LB"
  }
}
```

---

#### 3.1.2. List All Carton Types

Retrieves a list of all available cartons in the inventory.

*   **Endpoint:** `GET /cartons`
*   **Use Case:** To fetch a list of all usable boxes, for example, to display in an administrative dashboard or to see what options are available to the packing algorithm.

**Query Parameters:**

*   `activeOnly` (boolean, optional, default: `false`): If set to `true`, the list will only include cartons that have not been deactivated.

**Responses:**

*   **`200 OK`**: Returns a JSON array of carton objects.

**Example Success Response:**
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
    "name": "Medium Cube Box (12x12x12)",
    "dimensions": { ... },
    "maxWeight": { ... }
  },
  {
    "id": "b2c3d4e5-f6a7-8901-2345-67890abcdef1",
    "name": "Small Flat Box (10x8x2)",
    "dimensions": { ... },
    "maxWeight": { ... }
  }
]
```

---

#### 3.1.3. Get Carton by ID

Retrieves the full details of a single, specific carton type.

*   **Endpoint:** `GET /cartons/{cartonId}`
*   **Use Case:** When you need to inspect the properties of a specific carton, given its unique ID.

**Path Parameters:**

*   `cartonId` (string, required): The unique identifier of the carton you want to retrieve.

**Responses:**

*   **`200 OK`**: Returns the full carton object.
*   **`404 Not Found`**: Returned if no carton exists with the provided `cartonId`.

---

#### 3.1.4. Deactivate a Carton

Deactivates a carton, making it unavailable for *future* packing solutions. This is a "soft delete".

*   **Endpoint:** `DELETE /cartons/{cartonId}`
*   **Use Case:** When a carton type is being phased out and should no longer be used for new packing jobs. The record is not deleted to maintain the integrity of historical packing solutions that may have used it.

**Path Parameters:**

*   `cartonId` (string, required): The unique identifier of the carton to deactivate.

**Responses:**

*   **`204 No Content`**: Indicates the carton was successfully deactivated. No response body is returned.
*   **`404 Not Found`**: Returned if no carton exists with the provided `cartonId`.

---

### 3.2. Cartonization

This is the core functionality of the service.

#### 3.2.1. Calculate a Packing Solution

Accepts a list of items (SKUs and quantities) and returns the optimal packing solution, detailing which cartons to use and what to put inside them.

*   **Endpoint:** `POST /packing-solutions`
*   **Use Case:** This is the primary operational endpoint. For a given customer order or fulfillment batch, you call this endpoint to determine how the items should be physically packed for shipment. The service internally handles fetching product dimensions from the Product Catalog service.

**Request Body:**

The request body must be a JSON object containing the list of items to be packed.

*   `items` (array, required): An array of `ItemToPack` objects.
    *   `sku` (string): The Stock Keeping Unit of the item.
    *   `quantity` (integer): How many of this item need to be packed.
*   `orderId` (string, optional): An identifier for the order, used for tracking and logging.
*   `optimizeForMinimumBoxes` (boolean, optional): A flag to guide the algorithm. If `true`, the algorithm prioritizes using the fewest boxes possible, even if it means using larger boxes with more empty space.
*   `allowMixedCategories` (boolean, optional): A flag to indicate if items from different product categories can be packed in the same box.

**Example Request:**
```json
{
  "orderId": "ORD-2025-12345",
  "items": [
    {
      "sku": "EXAMPLE-SKU-123",
      "quantity": 2
    },
    {
      "sku": "WIDGET-BLUE-LARGE",
      "quantity": 1
    },
    {
      "sku": "SMALL-GADGET-RED",
      "quantity": 5
    }
  ],
  "optimizeForMinimumBoxes": true
}
```

**Responses:**

*   **`200 OK`**: Indicates a packing solution was successfully calculated. The response body contains the `PackingSolution`.

**Example Success Response:**

The solution consists of a list of `packages`. Each package represents one physical box to be shipped.

```json
{
  "packages": [
    {
      "cartonId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "items": [
        {
          "sku": "EXAMPLE-SKU-123",
          "quantity": 2
        },
        {
          "sku": "WIDGET-BLUE-LARGE",
          "quantity": 1
        }
      ]
    },
    {
      "cartonId": "c3d4e5f6-a7b8-9012-3456-7890abcdef12",
      "items": [
        {
          "sku": "SMALL-GADGET-RED",
          "quantity": 5
        }
      ]
    }
  ]
}
```
**How to interpret this response:**
This solution requires two physical boxes:
1.  The first box should be of type `a1b2...ef` (e.g., "Medium Cube Box"). Inside it, you will place 2 units of `EXAMPLE-SKU-123` and 1 unit of `WIDGET-BLUE-LARGE`.
2.  The second box should be of type `c3d4...12` (e.g., "Extra Small Box"). Inside it, you will place all 5 units of `SMALL-GADGET-RED`.
