# Cartonization Bounded Context

## Overview

The **Cartonization Bounded Context** is a specific, protected area within your software architecture whose sole purpose is to determine the most optimal way to pack a given set of items into a set of available boxes. It encapsulates all the logic, data, and business rules related to this single, well-defined responsibility.

Think of it as a highly specialized department in a factory, like a "Packing Logistics" office. This office doesn't care about product marketing or customer billing; its entire world revolves around boxes, packing rules, and finding the most efficient way to get products safely into a shipping container.

## Core Responsibilities

This context has a narrow and deep set of responsibilities:

**Carton Inventory Management:** It is the single source of truth for all available shipping cartons. It knows their dimensions, weight limits, names, and any other relevant attributes.

**Packing Algorithm Execution:** It contains the complex logic (e.g., 3D bin-packing algorithms) required to solve the puzzle of fitting items into boxes.

**Business Rule Enforcement:** It applies all rules related to packing. For example: "Fragile items must have 2 inches of padding," "Hazardous materials cannot be mixed with food-grade items," or "Item X must always ship in its original container."

**Solution Generation:** Its primary output is a `PackingSolution`—a clear set of instructions detailing which boxes to use and which items to place in each box for a given order.

Crucially, this context is **not** responsible for product data (like SKU names or prices) or order management (like customer addresses or payment status).

## Ubiquitous Language

To ensure clarity, everyone involved (developers, business stakeholders, etc.) must use a precise, shared vocabulary when discussing this context.

- **Carton:** A specific type of physical box available for packing. It is an **Aggregate Root**.
- **Item:** A product to be packed, identified by its **SKU**. The context only cares about its physical properties (dimensions, weight) and packing attributes (fragility, etc.), which it gets from another context.
- **Cartonization Request:** A command to the system asking, "How should I pack these items?"
- **Packing Solution:** The output of the cartonization process; an immutable set of instructions.
- **Package:** A single packed box within a `PackingSolution`. It contains a specific `Carton` type and a list of `Items`.

## Context Map: Relationships with Other Contexts

A bounded context doesn't live in isolation. It communicates with other contexts in clearly defined ways.

### Product Catalog Context (Upstream)
The Cartonization context is a **downstream** consumer of the Product Catalog.

- **Relationship Type:** **Conformist**. It conforms to the model of the Product Catalog. When it needs product dimensions, it calls the Product Catalog API and uses the data as-is, without translation.
- **Integration:** Via a REST API call. The Cartonization service calls `GET /products/{sku}` to fetch the data it needs to perform its calculations.

### Order Management Context (Upstream)
The Cartonization context is also **downstream** from the Order Management System (OMS).

- **Relationship Type:** **Open Host Service**. The Cartonization context exposes a simple, open API (`POST /packing-solutions`) for any upstream context, like an OMS, to use. It acts as a service provider.
- **Integration:** The OMS initiates the process by sending a `CartonizationRequest` to our context's API.