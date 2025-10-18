# Cartonization Service

3D bin-packing optimization engine for shipping cartons using Spring Boot, Kafka, MongoDB, and hexagonal architecture.

## Overview

The Cartonization Service is responsible for optimizing shipping carton utilization through advanced 3D bin-packing algorithms. Within the Paklog fulfillment platform, this bounded context calculates the most efficient way to pack items into shipping cartons, minimizing shipping costs while ensuring proper fit and safety.

## Domain-Driven Design

### Bounded Context
**Cartonization & Packing Optimization** - Manages carton definitions and calculates optimal packing solutions for fulfillment orders.

### Core Domain Model

#### Aggregates
- **Carton** - Defines available shipping carton types with dimensions, weight limits, and metadata
- **PackingSolution** - Represents the calculated optimal packing strategy for a set of items

#### Entities
- **Package** - Individual package within a packing solution
- **ItemToPack** - Item requiring packing with its dimensions and quantity

#### Value Objects
- **DimensionSet** - Length, width, height with units
- **Weight** - Weight value with unit
- **SKU** - Stock keeping unit identifier
- **CartonId** - Unique carton identifier
- **PackingRules** - Business rules for packing constraints

#### Domain Events
- **CartonCreatedEvent** - New carton type added to system
- **CartonUpdatedEvent** - Carton type modified
- **CartonDeactivatedEvent** - Carton type removed from active use
- **PackingSolutionCalculated** - Optimal packing solution computed

#### Domain Services
- **PackingAlgorithmService** - Implements 3D bin-packing algorithms
- **BusinessRuleValidator** - Validates packing business rules

### Ubiquitous Language
- **Cartonization**: Process of determining optimal carton sizes for shipment
- **Bin Packing**: Algorithm for fitting items into containers
- **Packing Solution**: Calculated arrangement of items in cartons
- **Available to Promise**: Items that can be packed and shipped

## Architecture & Patterns

### Hexagonal Architecture (Ports and Adapters)

```
src/main/java/com/paklog/cartonization/
├── domain/                           # Core business logic
│   ├── model/
│   │   ├── aggregate/               # Carton, PackingSolution
│   │   ├── entity/                  # Package
│   │   └── valueobject/             # DimensionSet, Weight, SKU
│   ├── service/                     # Domain services
│   └── event/                       # Domain events
├── application/                      # Use cases & orchestration
│   ├── port/
│   │   ├── in/                      # Input ports (use cases)
│   │   └── out/                     # Output ports (repositories, clients)
│   └── service/                     # Application services
└── infrastructure/                   # External adapters
    ├── persistence/                 # MongoDB repositories
    ├── messaging/                   # Kafka publishers
    ├── web/                         # REST controllers
    └── config/                      # Configuration
```

### Design Patterns & Principles
- **Hexagonal Architecture** - Clear separation between domain and infrastructure
- **Domain-Driven Design** - Rich domain models with business logic
- **CQRS** - Separation of command and query responsibilities
- **Event-Driven Architecture** - Asynchronous communication via domain events
- **Transactional Outbox Pattern** - Reliable event publishing
- **Repository Pattern** - Abstraction over data persistence
- **Dependency Inversion** - Domain depends on abstractions, not implementations
- **SOLID Principles** - Clean, maintainable, and testable code

## Technology Stack

### Core Framework
- **Java 21** - Programming language
- **Spring Boot 3.2.0** - Application framework
- **Maven** - Build and dependency management

### Data & Persistence
- **MongoDB** - Document database for aggregates
- **Spring Data MongoDB** - Data access layer
- **Redis** - Caching layer for performance

### Messaging & Events
- **Apache Kafka** - Event streaming platform
- **Spring Kafka** - Kafka integration
- **CloudEvents** - Standardized event format

### API & Documentation
- **Spring Web MVC** - REST API framework
- **SpringDoc OpenAPI** - API documentation
- **Bean Validation** - Input validation

### Observability
- **Spring Boot Actuator** - Health checks and metrics
- **Micrometer** - Metrics collection
- **Prometheus** - Metrics aggregation
- **Grafana** - Metrics visualization
- **Loki** - Log aggregation

### Testing
- **JUnit 5** - Unit testing framework
- **Testcontainers** - Integration testing with containers
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Local development environment
- **Kubernetes** - Container orchestration

## Standards Applied

### Architectural Standards
- ✅ Hexagonal Architecture (Ports and Adapters)
- ✅ Domain-Driven Design tactical patterns
- ✅ CQRS for command/query separation
- ✅ Event-Driven Architecture
- ✅ Microservices architecture
- ✅ RESTful API design

### Code Quality Standards
- ✅ SOLID principles
- ✅ Clean Code practices
- ✅ Comprehensive unit and integration testing
- ✅ Test-Driven Development (TDD)
- ✅ Dependency injection
- ✅ Immutable value objects

### Event & Integration Standards
- ✅ CloudEvents specification
- ✅ Transactional Outbox Pattern
- ✅ At-least-once delivery semantics
- ✅ Event versioning strategy
- ✅ Schema evolution support

### Observability Standards
- ✅ Structured logging (JSON)
- ✅ Distributed tracing
- ✅ Health check endpoints
- ✅ Prometheus metrics exposition
- ✅ Correlation ID propagation

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- Docker & Docker Compose

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/paklog/cartonization.git
   cd cartonization
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose up -d mongodb kafka redis
   ```

3. **Build and run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Verify the service is running**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

### Using Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f cartonization

# Stop all services
docker-compose down
```

## API Documentation

Once running, access the interactive API documentation:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

### Key Endpoints

- `POST /api/v1/cartons` - Create new carton type
- `GET /api/v1/cartons` - List all carton types
- `GET /api/v1/cartons/{id}` - Get carton by ID
- `PUT /api/v1/cartons/{id}` - Update carton type
- `DELETE /api/v1/cartons/{id}` - Deactivate carton type
- `POST /api/v1/packing-solutions` - Calculate optimal packing solution

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run tests with coverage
mvn clean verify jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## Configuration

Key configuration properties:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/cartonization
  kafka:
    bootstrap-servers: localhost:9092
  redis:
    host: localhost
    port: 6379

cartonization:
  algorithm:
    default-strategy: BEST_FIT_DECREASING
  cache:
    enabled: true
    ttl: 3600
```

## Event Integration

### Published Events
- `com.paklog.cartonization.carton.created.v1`
- `com.paklog.cartonization.carton.updated.v1`
- `com.paklog.cartonization.carton.deactivated.v1`
- `com.paklog.cartonization.packing.solution.calculated.v1`

### Event Format
All events follow the CloudEvents specification and are published to Kafka.

## Monitoring

- **Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus

## Contributing

1. Follow hexagonal architecture principles
2. Implement domain logic in the domain layer
3. Keep infrastructure concerns separate
4. Write comprehensive tests for all layers
5. Document domain concepts using ubiquitous language
6. Follow existing code style and conventions

## License

Copyright © 2024 Paklog. All rights reserved.
