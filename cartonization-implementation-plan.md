# Cartonization Service - Implementation Plan

## 1. Architecture Overview

### Hexagonal Architecture (Ports & Adapters)

The service will follow hexagonal architecture principles to ensure clear separation of concerns and maintainability.

```
┌─────────────────────────────────────────────────────────────────┐
│                        Infrastructure Layer                      │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                      Adapters (Driving)                   │  │
│  │  • REST Controllers  • Kafka Consumers  • Schedulers     │  │
│  └──────────────────────────────────────────────────────────┘  │
│                               ↓                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    Input Ports (Interfaces)               │  │
│  │  • CartonManagementUseCase  • PackingSolutionUseCase     │  │
│  └──────────────────────────────────────────────────────────┘  │
│                               ↓                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    Application Layer                      │  │
│  │           • Use Case Implementations                      │  │
│  │           • Application Services                          │  │
│  │           • Command/Query Handlers                        │  │
│  └──────────────────────────────────────────────────────────┘  │
│                               ↓                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                      Domain Layer                         │  │
│  │  • Aggregates: Carton                                     │  │
│  │  • Entities: PackingSolution, Package                     │  │
│  │  • Value Objects: DimensionSet, Weight, SKU              │  │
│  │  • Domain Services: PackingAlgorithmService               │  │
│  │  • Domain Events: CartonCreated, SolutionCalculated       │  │
│  └──────────────────────────────────────────────────────────┘  │
│                               ↓                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                   Output Ports (Interfaces)               │  │
│  │  • CartonRepository  • EventPublisher                     │  │
│  │  • ProductCatalogClient  • PackingSolutionRepository      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                               ↓                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                     Adapters (Driven)                     │  │
│  │  • MongoDB Repositories  • Kafka Producer                 │  │
│  │  • REST Client (Product Catalog)  • Cache (Redis)         │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## 2. Project Structure

```
cartonization-service/
├── src/main/java/com/paklog/cartonization/
│   ├── domain/                          # Core Business Logic (No Framework Dependencies)
│   │   ├── model/
│   │   │   ├── aggregate/
│   │   │   │   └── Carton.java
│   │   │   ├── entity/
│   │   │   │   ├── PackingSolution.java
│   │   │   │   └── Package.java
│   │   │   └── valueobject/
│   │   │       ├── CartonId.java
│   │   │       ├── DimensionSet.java
│   │   │       ├── Weight.java
│   │   │       ├── SKU.java
│   │   │       └── ItemToPack.java
│   │   ├── service/
│   │   │   ├── PackingAlgorithmService.java
│   │   │   └── BusinessRuleValidator.java
│   │   ├── event/
│   │   │   ├── DomainEvent.java
│   │   │   ├── CartonCreatedEvent.java
│   │   │   ├── CartonUpdatedEvent.java
│   │   │   └── PackingSolutionCalculatedEvent.java
│   │   └── exception/
│   │       ├── DomainException.java
│   │       ├── CartonNotFoundException.java
│   │       └── InvalidPackingRequestException.java
│   │
│   ├── application/                     # Application Services & Use Cases
│   │   ├── port/
│   │   │   ├── in/                     # Input Ports (Driving)
│   │   │   │   ├── CartonManagementUseCase.java
│   │   │   │   ├── PackingSolutionUseCase.java
│   │   │   │   └── command/
│   │   │   │       ├── CreateCartonCommand.java
│   │   │   │       ├── UpdateCartonCommand.java
│   │   │   │       └── CalculatePackingSolutionCommand.java
│   │   │   └── out/                    # Output Ports (Driven)
│   │   │       ├── CartonRepository.java
│   │   │       ├── PackingSolutionRepository.java
│   │   │       ├── ProductCatalogClient.java
│   │   │       ├── EventPublisher.java
│   │   │       └── CacheStore.java
│   │   ├── service/
│   │   │   ├── CartonManagementService.java
│   │   │   ├── PackingSolutionService.java
│   │   │   └── ProductDimensionEnricher.java
│   │   └── dto/
│   │       ├── CartonDTO.java
│   │       ├── PackingSolutionDTO.java
│   │       └── ProductDimensionsDTO.java
│   │
│   └── infrastructure/                  # Framework & External Dependencies
│       ├── adapter/
│       │   ├── in/                     # Driving Adapters
│       │   │   ├── web/
│       │   │   │   ├── CartonController.java
│       │   │   │   ├── PackingSolutionController.java
│       │   │   │   └── mapper/
│       │   │   │       └── RestDTOMapper.java
│       │   │   └── messaging/
│       │   │       ├── CartonizationRequestConsumer.java
│       │   │       └── config/
│       │   │           └── KafkaConsumerConfig.java
│       │   └── out/                    # Driven Adapters
│       │       ├── persistence/
│       │       │   ├── mongodb/
│       │       │   │   ├── repository/
│       │       │   │   │   ├── MongoCartonRepository.java
│       │       │   │   │   └── MongoPackingSolutionRepository.java
│       │       │   │   ├── document/
│       │       │   │   │   ├── CartonDocument.java
│       │       │   │   │   └── PackingSolutionDocument.java
│       │       │   │   └── mapper/
│       │       │   │       └── DocumentMapper.java
│       │       │   └── config/
│       │       │       └── MongoConfig.java
│       │       ├── messaging/
│       │       │   ├── KafkaEventPublisher.java
│       │       │   └── config/
│       │       │       └── KafkaProducerConfig.java
│       │       ├── client/
│       │       │   ├── ProductCatalogRestClient.java
│       │       │   └── config/
│       │       │       └── RestClientConfig.java
│       │       └── cache/
│       │           ├── RedisCartonCache.java
│       │           └── config/
│       │               └── RedisConfig.java
│       └── config/
│           ├── ApplicationConfig.java
│           ├── SecurityConfig.java
│           ├── OpenApiConfig.java
│           └── ActuatorConfig.java
```

## 3. Domain Model Design (DDD)

### Aggregates

#### Carton Aggregate
```java
@Aggregate
public class Carton {
    private CartonId id;
    private String name;
    private DimensionSet dimensions;
    private Weight maxWeight;
    private CartonStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Business methods
    public void activate() { /* domain logic */ }
    public void deactivate() { /* domain logic */ }
    public boolean canFitItem(DimensionSet itemDimensions, Weight itemWeight) { /* domain logic */ }
    
    // Domain events
    public List<DomainEvent> pullDomainEvents() { /* event sourcing */ }
}
```

### Value Objects

#### DimensionSet
```java
@ValueObject
public class DimensionSet {
    private final BigDecimal length;
    private final BigDecimal width;
    private final BigDecimal height;
    private final DimensionUnit unit;
    
    public BigDecimal volume() { /* calculation */ }
    public DimensionSet convertTo(DimensionUnit targetUnit) { /* conversion */ }
}
```

### Domain Services

#### PackingAlgorithmService
```java
@DomainService
public class PackingAlgorithmService {
    // Implements 3D bin-packing algorithm
    public PackingSolution calculateOptimalPacking(
        List<ItemWithDimensions> items,
        List<Carton> availableCartons,
        PackingRules rules
    ) {
        // Complex packing algorithm implementation
    }
}
```

## 4. MongoDB Schema Design

### Collections

#### cartons Collection
```javascript
{
  "_id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Medium Cube Box",
  "status": "ACTIVE",
  "dimensions": {
    "length": { "value": 12.0, "unit": "INCHES" },
    "width": { "value": 12.0, "unit": "INCHES" },
    "height": { "value": 12.0, "unit": "INCHES" }
  },
  "maxWeight": { "value": 50.0, "unit": "POUNDS" },
  "metadata": {
    "createdAt": ISODate("2024-01-01T00:00:00Z"),
    "updatedAt": ISODate("2024-01-01T00:00:00Z"),
    "createdBy": "system",
    "version": 1
  }
}
```

#### packing_solutions Collection
```javascript
{
  "_id": ObjectId("..."),
  "solutionId": "ps-550e8400-e29b-41d4",
  "requestId": "req-123456",
  "packages": [
    {
      "cartonId": "550e8400-e29b-41d4-a716-446655440000",
      "cartonName": "Medium Cube Box",
      "items": [
        { "sku": "WIDGET-001", "quantity": 2 },
        { "sku": "GADGET-002", "quantity": 1 }
      ],
      "utilization": {
        "volumeUsed": 85.5,
        "weightUsed": 45.2
      }
    }
  ],
  "metrics": {
    "totalPackages": 1,
    "totalWeight": 45.2,
    "algorithmExecutionTime": 125,
    "efficiency": 0.855
  },
  "createdAt": ISODate("2024-01-01T00:00:00Z"),
  "ttl": ISODate("2024-01-08T00:00:00Z")  // Auto-expire after 7 days
}
```

### MongoDB Best Practices Implementation

1. **Indexes**
```javascript
// cartons collection indexes
db.cartons.createIndex({ "status": 1 })
db.cartons.createIndex({ "dimensions.volume": -1 })
db.cartons.createIndex({ "name": "text" })

// packing_solutions collection indexes
db.packing_solutions.createIndex({ "solutionId": 1 }, { unique: true })
db.packing_solutions.createIndex({ "requestId": 1 })
db.packing_solutions.createIndex({ "ttl": 1 }, { expireAfterSeconds: 0 })
```

2. **Sharding Strategy** (for scalability)
```javascript
sh.shardCollection("cartonization.packing_solutions", { "requestId": "hashed" })
```

3. **Change Streams** for real-time updates
4. **Read Preference** configurations for read-heavy operations
5. **Write Concern** settings for critical data

## 5. Kafka Integration

### Topics

#### Input Topics
- `cartonization.requests` - Incoming cartonization requests from Order Management
- `product.dimension.updates` - Product dimension changes from Product Catalog

#### Output Topics
- `cartonization.solutions` - Calculated packing solutions
- `cartonization.events` - Domain events (CartonCreated, CartonUpdated, etc.)

### Event Schemas (Avro)

```avsc
{
  "type": "record",
  "name": "CartonizationRequest",
  "namespace": "com.paklog.cartonization.event",
  "fields": [
    {"name": "requestId", "type": "string"},
    {"name": "orderId", "type": "string"},
    {"name": "items", "type": {
      "type": "array",
      "items": {
        "type": "record",
        "name": "Item",
        "fields": [
          {"name": "sku", "type": "string"},
          {"name": "quantity", "type": "int"}
        ]
      }
    }},
    {"name": "timestamp", "type": "long", "logicalType": "timestamp-millis"}
  ]
}
```

### Kafka Configuration

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: cartonization-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: io.confluent.kafka.serializers.KafkaAvroDeserializer
      properties:
        specific.avro.reader: true
        isolation.level: read_committed
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: io.confluent.kafka.serializers.KafkaAvroSerializer
      acks: all
      retries: 3
      properties:
        enable.idempotence: true
    properties:
      schema.registry.url: ${SCHEMA_REGISTRY_URL:http://localhost:8081}
```

## 6. Spring Boot Configuration

### Dependencies (build.gradle)

```gradle
dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-cache'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    
    // Spring Cloud
    implementation 'org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j'
    implementation 'org.springframework.cloud:spring-cloud-starter-sleuth'
    implementation 'org.springframework.cloud:spring-cloud-sleuth-zipkin'
    
    // Kafka
    implementation 'org.springframework.kafka:spring-kafka'
    implementation 'io.confluent:kafka-avro-serializer:7.5.0'
    
    // MongoDB
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb-reactive'
    
    // OpenAPI/Swagger
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'
    
    // Monitoring
    implementation 'io.micrometer:micrometer-registry-prometheus'
    
    // Utilities
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
    testImplementation 'de.flapdoodle.embed:de.flapdoodle.embed.mongo'
    testImplementation 'org.testcontainers:testcontainers'
    testImplementation 'org.testcontainers:kafka'
    testImplementation 'org.testcontainers:mongodb'
}
```

### Application Properties

```yaml
spring:
  application:
    name: cartonization-service
  
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/cartonization}
      auto-index-creation: true
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
  
  cache:
    type: redis
    redis:
      time-to-live: 600000  # 10 minutes
      cache-null-values: false
  
resilience4j:
  circuitbreaker:
    instances:
      product-catalog:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
        
  retry:
    instances:
      product-catalog:
        max-attempts: 3
        wait-duration: 1s
        retry-exceptions:
          - java.io.IOException
          - org.springframework.web.client.RestClientException

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
```

## 7. Integration Patterns

### Product Catalog Integration

```java
@Component
@Slf4j
public class ProductCatalogRestClient implements ProductCatalogClient {
    
    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;
    private final Cache cache;
    
    @Override
    @Cacheable(value = "product-dimensions", key = "#sku")
    @Retry(name = "product-catalog")
    @CircuitBreaker(name = "product-catalog", fallbackMethod = "getProductDimensionsFallback")
    public ProductDimensions getProductDimensions(SKU sku) {
        // Implementation with resilience patterns
    }
    
    private ProductDimensions getProductDimensionsFallback(SKU sku, Exception ex) {
        log.warn("Falling back to cached data for SKU: {}", sku);
        // Return from cache or default values
    }
}
```

### Event-Driven Architecture

```java
@Component
@Slf4j
public class CartonizationRequestConsumer {
    
    @KafkaListener(
        topics = "cartonization.requests",
        groupId = "cartonization-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleCartonizationRequest(
        @Payload CartonizationRequestEvent event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
    ) {
        log.info("Received cartonization request: {} from partition: {}", 
                 event.getRequestId(), partition);
        
        try {
            // Process the request
            PackingSolution solution = packingSolutionUseCase.calculate(event);
            
            // Publish the result
            eventPublisher.publishSolution(solution);
            
        } catch (Exception e) {
            // Handle errors and potentially send to DLQ
            handleError(event, e);
        }
    }
}
```

## 8. Testing Strategy

### Unit Tests
- Domain logic testing (pure Java, no Spring context)
- Service layer testing with mocks
- Mapper testing

### Integration Tests
```java
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Testcontainers
class CartonizationIntegrationTest {
    
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");
    
    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }
    
    @Test
    void shouldCalculatePackingSolution() {
        // Integration test implementation
    }
}
```

### Contract Tests
- Using Spring Cloud Contract for API contracts
- Pact for consumer-driven contracts with Product Catalog

### Performance Tests
- JMeter for load testing
- Gatling for stress testing the packing algorithm

## 9. Monitoring & Observability

### Metrics
- Custom metrics for packing algorithm performance
- Business metrics (solutions per minute, average package count)
- Technical metrics (response times, error rates)

### Logging
```java
@Aspect
@Component
@Slf4j
public class LoggingAspect {
    
    @Around("@annotation(Loggable)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        
        Object proceed = joinPoint.proceed();
        
        long executionTime = System.currentTimeMillis() - start;
        
        log.info("{} executed in {} ms", joinPoint.getSignature(), executionTime);
        
        // Send metric to Micrometer
        meterRegistry.timer("method.execution.time", 
            "method", joinPoint.getSignature().getName())
            .record(executionTime, TimeUnit.MILLISECONDS);
        
        return proceed;
    }
}
```

### Distributed Tracing
- Spring Cloud Sleuth for trace/span generation
- Zipkin for trace visualization

## 10. Security Considerations

### API Security
- OAuth2/JWT for authentication
- Rate limiting per client
- Input validation and sanitization

### Data Security
- MongoDB encryption at rest
- TLS for all network communication
- Sensitive data masking in logs

## 11. Deployment Strategy

### Docker Configuration
```dockerfile
FROM openjdk:17-jdk-slim AS builder
WORKDIR /app
COPY gradle gradle
COPY build.gradle settings.gradle gradlew ./
RUN ./gradlew dependencies --no-daemon
COPY src src
RUN ./gradlew build --no-daemon

FROM openjdk:17-jre-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cartonization-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: cartonization-service
  template:
    metadata:
      labels:
        app: cartonization-service
    spec:
      containers:
      - name: cartonization-service
        image: paklog/cartonization-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: MONGODB_URI
          valueFrom:
            secretKeyRef:
              name: mongodb-secret
              key: uri
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
```

## 12. Implementation Phases

### Phase 1: Foundation (Week 1-2)
- Set up Spring Boot project with hexagonal architecture
- Implement domain model (Carton aggregate, value objects)
- Set up MongoDB with basic repository operations
- Implement basic REST endpoints for Carton management

### Phase 2: Core Algorithm (Week 3-4)
- Implement 3D bin-packing algorithm
- Add business rule validation
- Create packing solution domain service
- Unit test coverage for domain logic

### Phase 3: Integration (Week 5-6)
- Integrate with Product Catalog service
- Implement Kafka consumers and producers
- Add caching layer with Redis
- Circuit breaker implementation

### Phase 4: Production Readiness (Week 7-8)
- Add comprehensive error handling
- Implement monitoring and observability
- Performance optimization
- Security implementation
- Documentation and API specs

### Phase 5: Testing & Deployment (Week 9-10)
- Integration testing with Testcontainers
- Contract testing setup
- Performance testing
- Docker and Kubernetes deployment
- Production deployment and monitoring setup

## 13. Key Design Decisions

1. **Event Sourcing**: Consider implementing event sourcing for the Carton aggregate to maintain audit trail
2. **CQRS**: Separate read and write models for better performance
3. **Saga Pattern**: For managing distributed transactions across bounded contexts
4. **Outbox Pattern**: Ensure reliable event publishing with MongoDB and Kafka
5. **Optimistic Locking**: For concurrent updates to carton inventory

## 14. Performance Optimizations

1. **Algorithm Optimization**:
   - Use dynamic programming for bin-packing
   - Implement parallel processing for large item sets
   - Cache frequently used carton combinations

2. **Database Optimization**:
   - Use MongoDB aggregation pipeline for complex queries
   - Implement read replicas for query operations
   - Use compound indexes for frequent query patterns

3. **Caching Strategy**:
   - Cache product dimensions (TTL: 1 hour)
   - Cache carton inventory (TTL: 10 minutes)
   - Cache recent packing solutions (TTL: 24 hours)

## 15. Error Handling & Resilience

```java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CartonNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCartonNotFound(CartonNotFoundException ex) {
        log.error("Carton not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of("CARTON_NOT_FOUND", ex.getMessage()));
    }
    
    @ExceptionHandler(InvalidPackingRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidPackingRequestException ex) {
        log.error("Invalid packing request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of("INVALID_REQUEST", ex.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
```

## Next Steps

1. Review and approve this implementation plan
2. Set up the development environment
3. Create the Spring Boot project structure
4. Begin implementation following the phases outlined above
5. Set up CI/CD pipeline early in the development process