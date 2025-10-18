---
layout: default
title: Home
---

# Cartonization Service Documentation

3D bin-packing optimization engine for shipping cartons using Spring Boot, Kafka, MongoDB, and hexagonal architecture.

## Overview

The Cartonization Service is responsible for optimizing shipping carton utilization through advanced 3D bin-packing algorithms. Within the Paklog fulfillment platform, this bounded context calculates the most efficient way to pack items into shipping cartons, minimizing shipping costs while ensuring proper fit and safety.

## Quick Links

- [Architecture Diagrams](architecture-diagrams.md) - System architecture and component diagrams
- [Domain Model](domain-model.md) - Domain-Driven Design model
- [Bounded Context](cartonization-bounded-context.md) - Bounded context definition
- [API Examples](api-implementation-examples.md) - API usage examples
- [Implementation Plan](cartonization-implementation-plan.md) - Implementation roadmap
- [Development Setup](development-environment-setup.md) - Local development guide
- [Docker Guide](DOCKER_README.md) - Docker deployment guide

## Technology Stack

- **Java 21** - Programming language
- **Spring Boot 3.2.0** - Application framework
- **MongoDB** - Document database
- **Apache Kafka** - Event streaming
- **Redis** - Caching layer

## Key Features

- 3D bin-packing algorithms
- Hexagonal architecture
- Event-driven design
- Real-time optimization
- Comprehensive testing

## Getting Started

1. Clone the repository
2. Review the [Development Setup Guide](development-environment-setup.md)
3. Follow the [Docker Guide](DOCKER_README.md) for containerized deployment
4. Explore the [API Examples](api-implementation-examples.md)

## Domain-Driven Design

This service implements Domain-Driven Design principles with:

- **Aggregates**: Carton, PackingSolution
- **Entities**: Package, ItemToPack
- **Value Objects**: DimensionSet, Weight, SKU
- **Domain Events**: CartonCreated, PackingSolutionCalculated
- **Domain Services**: PackingAlgorithmService

For detailed domain model information, see [Domain Model](domain-model.md).

## Architecture

The service follows Hexagonal Architecture (Ports and Adapters) pattern:

- **Domain Layer** - Pure business logic
- **Application Layer** - Use cases and orchestration
- **Infrastructure Layer** - External integrations

See [Architecture Diagrams](architecture-diagrams.md) for visual representations.

## Contributing

For contribution guidelines, please refer to the main [README](../README.md) in the project root.

## Support

- **GitHub Issues**: [Report bugs or request features](https://github.com/paklog/cartonization/issues)
- **Documentation**: Browse the guides in the navigation menu
- **API Reference**: See [API Documentation](api-implementation-examples.md)
