# Cartonization Service - Architecture Diagrams

## 1. High-Level System Architecture

```mermaid
graph TB
    subgraph External Systems
        OMS[Order Management System]
        PC[Product Catalog Service]
        WH[Warehouse System]
    end
    
    subgraph Cartonization Service
        API[REST API Layer]
        KC[Kafka Consumer]
        APP[Application Layer]
        DOM[Domain Layer]
        INFRA[Infrastructure Layer]
    end
    
    subgraph Data Stores
        MongoDB[(MongoDB)]
        Redis[(Redis Cache)]
        Kafka[Apache Kafka]
    end
    
    OMS -->|REST Request| API
    OMS -->|Async Event| Kafka
    Kafka -->|Consume| KC
    API --> APP
    KC --> APP
    APP --> DOM
    DOM --> APP
    APP --> INFRA
    INFRA -->|Query| PC
    INFRA -->|Store| MongoDB
    INFRA -->|Cache| Redis
    INFRA -->|Publish| Kafka
    Kafka -->|Events| WH
```

## 2. Hexagonal Architecture Detail

```mermaid
graph TB
    subgraph Driving Side - Input Adapters
        REST[REST Controllers]
        KAFKA_IN[Kafka Consumers]
        SCHED[Schedulers]
    end
    
    subgraph Application Core
        subgraph Ports In
            CMP[CartonManagementPort]
            PSP[PackingSolutionPort]
        end
        
        subgraph Use Cases
            CMS[CartonManagementService]
            PSS[PackingSolutionService]
        end
        
        subgraph Domain
            CA[Carton Aggregate]
            PS[PackingSolution Entity]
            VO[Value Objects]
            DS[Domain Services]
            DE[Domain Events]
        end
        
        subgraph Ports Out
            CR[CartonRepository]
            PSR[PackingSolutionRepository]
            PCC[ProductCatalogClient]
            EP[EventPublisher]
            CS[CacheStore]
        end
    end
    
    subgraph Driven Side - Output Adapters
        MONGO[MongoDB Adapter]
        REDIS[Redis Adapter]
        HTTP[HTTP Client Adapter]
        KAFKA_OUT[Kafka Producer]
    end
    
    REST --> CMP
    KAFKA_IN --> PSP
    SCHED --> CMP
    
    CMP --> CMS
    PSP --> PSS
    
    CMS --> CA
    CMS --> VO
    PSS --> PS
    PSS --> DS
    PSS --> DE
    
    CA --> CR
    PS --> PSR
    DS --> PCC
    DE --> EP
    VO --> CS
    
    CR --> MONGO
    PSR --> MONGO
    CS --> REDIS
    PCC --> HTTP
    EP --> KAFKA_OUT
```

## 3. Cartonization Request Flow

```mermaid
sequenceDiagram
    participant Client
    participant API as REST API
    participant Service as PackingSolutionService
    participant Domain as PackingAlgorithm
    participant ProdCat as Product Catalog
    participant Cache as Redis Cache
    participant DB as MongoDB
    participant Kafka
    
    Client->>API: POST /packing-solutions
    API->>Service: calculatePackingSolution(request)
    
    loop For each SKU
        Service->>Cache: Check product dimensions
        alt Cache Miss
            Service->>ProdCat: GET /products/{sku}
            ProdCat-->>Service: Product dimensions
            Service->>Cache: Store dimensions
        else Cache Hit
            Cache-->>Service: Cached dimensions
        end
    end
    
    Service->>Domain: calculateOptimalPacking(items, cartons)
    Note over Domain: Execute 3D bin-packing algorithm
    Domain-->>Service: PackingSolution
    
    Service->>DB: Save solution
    Service->>Kafka: Publish SolutionCalculatedEvent
    Service-->>API: Return solution
    API-->>Client: 200 OK with solution
```

## 4. Event-Driven Flow

```mermaid
graph LR
    subgraph Order Management
        OM[Order Created]
    end
    
    subgraph Kafka Topics
        REQ[cartonization.requests]
        SOL[cartonization.solutions]
        EVT[cartonization.events]
    end
    
    subgraph Cartonization Service
        CON[Request Consumer]
        PROC[Process Request]
        PUB[Solution Publisher]
    end
    
    subgraph Warehouse
        WMS[Pack Order]
    end
    
    OM -->|Publish| REQ
    REQ -->|Consume| CON
    CON --> PROC
    PROC -->|Publish| SOL
    PROC -->|Publish| EVT
    SOL -->|Consume| WMS
```

## 5. Domain Model Class Diagram

```mermaid
classDiagram
    class Carton {
        -CartonId id
        -String name
        -DimensionSet dimensions
        -Weight maxWeight
        -CartonStatus status
        +activate() void
        +deactivate() void
        +canFitItem(dimensions, weight) boolean
    }
    
    class PackingSolution {
        -String solutionId
        -String requestId
        -List~Package~ packages
        -PackingMetrics metrics
        +addPackage(package) void
        +calculateEfficiency() double
    }
    
    class Package {
        -Carton carton
        -List~ItemToPack~ items
        -Utilization utilization
        +addItem(item) void
        +calculateUtilization() Utilization
    }
    
    class DimensionSet {
        -BigDecimal length
        -BigDecimal width
        -BigDecimal height
        -DimensionUnit unit
        +volume() BigDecimal
        +convertTo(unit) DimensionSet
    }
    
    class Weight {
        -BigDecimal value
        -WeightUnit unit
        +convertTo(unit) Weight
        +add(weight) Weight
    }
    
    class ItemToPack {
        -SKU sku
        -Integer quantity
        -DimensionSet dimensions
        -Weight weight
    }
    
    class PackingAlgorithmService {
        +calculateOptimalPacking(items, cartons, rules) PackingSolution
        -firstFitDecreasing(items, cartons) List~Package~
        -bestFit(items, cartons) List~Package~
    }
    
    Carton "1" --* "1" DimensionSet
    Carton "1" --* "1" Weight
    PackingSolution "1" --* "*" Package
    Package "*" --* "1" Carton
    Package "1" --* "*" ItemToPack
    ItemToPack "1" --* "1" DimensionSet
    ItemToPack "1" --* "1" Weight
    PackingAlgorithmService ..> PackingSolution : creates
```

> For a detailed breakdown of each domain object, including its role as an Aggregate, Entity, or Value Object, please see the [Domain Model Documentation](./domain-model.md).

## 6. Data Flow Architecture

```mermaid
graph TB
    subgraph Input Layer
        REST_IN[REST API Request]
        KAFKA_MSG[Kafka Message]
    end
    
    subgraph Processing Layer
        VAL[Request Validation]
        ENR[Data Enrichment]
        ALG[Packing Algorithm]
        RULE[Business Rules]
    end
    
    subgraph Data Access Layer
        CACHE_R[Cache Read]
        DB_R[DB Read]
        EXT_API[External API Call]
    end
    
    subgraph Storage Layer
        CACHE_W[Cache Write]
        DB_W[DB Write]
        EVENT[Event Publishing]
    end
    
    subgraph Output Layer
        REST_OUT[REST Response]
        KAFKA_OUT[Kafka Event]
    end
    
    REST_IN --> VAL
    KAFKA_MSG --> VAL
    
    VAL --> ENR
    ENR --> CACHE_R
    CACHE_R -->|Miss| EXT_API
    EXT_API --> CACHE_W
    
    ENR --> ALG
    ALG --> DB_R
    ALG --> RULE
    
    RULE --> DB_W
    RULE --> EVENT
    
    DB_W --> REST_OUT
    EVENT --> KAFKA_OUT
```

## 7. MongoDB Document Relationships

```mermaid
erDiagram
    CARTON {
        string id PK
        string name
        object dimensions
        object maxWeight
        string status
        datetime createdAt
        datetime updatedAt
    }
    
    PACKING_SOLUTION {
        objectId _id PK
        string solutionId UK
        string requestId FK
        array packages
        object metrics
        datetime createdAt
        datetime ttl
    }
    
    PACKAGE {
        string cartonId FK
        string cartonName
        array items
        object utilization
    }
    
    ITEM_TO_PACK {
        string sku
        integer quantity
    }
    
    CARTON ||--o{ PACKAGE : uses
    PACKING_SOLUTION ||--|{ PACKAGE : contains
    PACKAGE ||--|{ ITEM_TO_PACK : contains
```

## 8. Deployment Architecture

```mermaid
graph TB
    subgraph Kubernetes Cluster
        subgraph Namespace: cartonization
            subgraph Deployment
                POD1[Pod 1]
                POD2[Pod 2]
                POD3[Pod 3]
            end
            
            SVC[Service]
            ING[Ingress]
            
            subgraph ConfigMaps & Secrets
                CM[ConfigMap]
                SEC[Secrets]
            end
        end
        
        subgraph Namespace: monitoring
            PROM[Prometheus]
            GRAF[Grafana]
        end
    end
    
    subgraph External Services
        MONGO[(MongoDB Atlas)]
        REDIS[(Redis Cloud)]
        KAFKA[Confluent Cloud]
    end
    
    subgraph External Systems
        LB[Load Balancer]
        REGISTRY[Container Registry]
    end
    
    LB --> ING
    ING --> SVC
    SVC --> POD1
    SVC --> POD2
    SVC --> POD3
    
    POD1 --> MONGO
    POD2 --> REDIS
    POD3 --> KAFKA
    
    POD1 --> CM
    POD2 --> SEC
    
    PROM --> POD1
    PROM --> POD2
    PROM --> POD3
```

## 9. Circuit Breaker Pattern

```mermaid
stateDiagram-v2
    [*] --> Closed
    
    Closed --> Open : Failure threshold reached
    Closed --> Closed : Success
    Closed --> Closed : Failure below threshold
    
    Open --> HalfOpen : After timeout
    Open --> Open : Call rejected
    
    HalfOpen --> Closed : Success threshold reached
    HalfOpen --> Open : Failure
    
    note right of Closed
        Normal operation
        All requests pass through
    end note
    
    note right of Open
        Service unavailable
        Fast fail all requests
        Use fallback mechanism
    end note
    
    note right of HalfOpen
        Testing recovery
        Limited requests allowed
    end note
```

## 10. Caching Strategy

```mermaid
graph LR
    subgraph Request Flow
        REQ[Request]
        CHECK{Cache Hit?}
        CACHE[(Redis)]
        SERVICE[Product Service]
        UPDATE[Update Cache]
        RESPONSE[Response]
    end
    
    REQ --> CHECK
    CHECK -->|Yes| CACHE
    CACHE --> RESPONSE
    CHECK -->|No| SERVICE
    SERVICE --> UPDATE
    UPDATE --> CACHE
    SERVICE --> RESPONSE
    
    subgraph Cache Policies
        TTL[TTL: 1 hour for products]
        EVICT[LRU Eviction]
        SIZE[Max Size: 1000 entries]
    end
```

## 11. Error Handling Flow

```mermaid
flowchart TB
    START[Request Received] --> VALIDATE{Valid Request?}
    
    VALIDATE -->|No| BADREQ[400 Bad Request]
    VALIDATE -->|Yes| PROCESS[Process Request]
    
    PROCESS --> ENRICH{Enrichment Success?}
    
    ENRICH -->|No| RETRY{Retry Count < 3?}
    RETRY -->|Yes| PROCESS
    RETRY -->|No| FALLBACK[Use Fallback Data]
    
    ENRICH -->|Yes| CALCULATE[Calculate Solution]
    FALLBACK --> CALCULATE
    
    CALCULATE --> ALGO{Algorithm Success?}
    
    ALGO -->|No| ERROR[500 Internal Error]
    ALGO -->|Yes| SAVE[Save Solution]
    
    SAVE --> DB{DB Success?}
    
    DB -->|No| COMPENSATE[Compensate Transaction]
    DB -->|Yes| PUBLISH[Publish Event]
    
    PUBLISH --> KAFKA{Kafka Success?}
    
    KAFKA -->|No| DLQ[Send to DLQ]
    KAFKA -->|Yes| SUCCESS[200 OK]
    
    COMPENSATE --> ERROR
    
    style BADREQ fill:#ff9999
    style ERROR fill:#ff9999
    style SUCCESS fill:#99ff99
```

## 12. Testing Pyramid

```mermaid
graph TB
    subgraph Testing Strategy
        E2E[E2E Tests - 5%]
        INT[Integration Tests - 15%]
        CONTRACT[Contract Tests - 20%]
        UNIT[Unit Tests - 60%]
    end
    
    E2E --> INT
    INT --> CONTRACT
    CONTRACT --> UNIT
    
    style E2E fill:#ff9999
    style INT fill:#ffcc99
    style CONTRACT fill:#ffff99
    style UNIT fill:#99ff99
```

## 13. Development Workflow

```mermaid
gitGraph
    commit id: "Initial setup"
    branch feature/domain-model
    checkout feature/domain-model
    commit id: "Add Carton aggregate"
    commit id: "Add value objects"
    checkout main
    merge feature/domain-model
    
    branch feature/mongodb-integration
    checkout feature/mongodb-integration
    commit id: "Add MongoDB config"
    commit id: "Implement repositories"
    checkout main
    merge feature/mongodb-integration
    
    branch feature/kafka-integration
    checkout feature/kafka-integration
    commit id: "Add Kafka config"
    commit id: "Implement consumers"
    commit id: "Implement producers"
    checkout main
    merge feature/kafka-integration
    
    branch feature/packing-algorithm
    checkout feature/packing-algorithm
    commit id: "Implement 3D bin-packing"
    commit id: "Add business rules"
    checkout main
    merge feature/packing-algorithm
```

## 14. API Request/Response Flow

```mermaid
flowchart LR
    subgraph Client
        REQ[HTTP Request]
        RES[HTTP Response]
    end
    
    subgraph API Gateway
        AUTH[Authentication]
        RATE[Rate Limiting]
        ROUTE[Routing]
    end
    
    subgraph Cartonization Service
        CTRL[Controller]
        VALID[Validation]
        MAP1[DTO → Domain]
        UC[Use Case]
        MAP2[Domain → DTO]
    end
    
    REQ --> AUTH
    AUTH --> RATE
    RATE --> ROUTE
    ROUTE --> CTRL
    CTRL --> VALID
    VALID --> MAP1
    MAP1 --> UC
    UC --> MAP2
    MAP2 --> RES
```

These diagrams provide a comprehensive visual representation of:
- System architecture and component interactions
- Hexagonal architecture implementation
- Request flows and sequence diagrams
- Domain model relationships
- Deployment architecture
- Error handling and resilience patterns
- Testing strategy
- Development workflow

Each diagram is designed to be clear and informative, helping developers understand different aspects of the system at various levels of abstraction.