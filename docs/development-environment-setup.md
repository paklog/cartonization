# Development Environment Setup Guide

## Prerequisites

### Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| **Java JDK** | 17+ | Runtime environment |
| **Gradle** | 8.0+ | Build automation |
| **Docker** | 24.0+ | Container runtime |
| **Docker Compose** | 2.20+ | Multi-container orchestration |
| **Git** | 2.40+ | Version control |
| **IDE** | IntelliJ IDEA / VS Code | Development environment |
| **Postman** | Latest | API testing |
| **MongoDB Compass** | Latest | MongoDB GUI (optional) |
| **Kafka Manager** | Latest | Kafka GUI (optional) |

## 1. Local Infrastructure Setup

### Docker Compose Configuration

Create a `docker-compose.yml` file in the project root:

```yaml
version: '3.8'

services:
  # MongoDB
  mongodb:
    image: mongo:7.0
    container_name: cartonization-mongodb
    restart: unless-stopped
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: admin123
      MONGO_INITDB_DATABASE: cartonization
    volumes:
      - mongodb-data:/data/db
      - ./init-mongo.js:/docker-entrypoint-initdb.d/init-mongo.js:ro
    networks:
      - cartonization-network

  # Redis
  redis:
    image: redis:7-alpine
    container_name: cartonization-redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis-data:/data
    networks:
      - cartonization-network

  # Zookeeper (for Kafka)
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: cartonization-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
    networks:
      - cartonization-network

  # Kafka
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: cartonization-kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
      - "29092:29092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092,PLAINTEXT_HOST://kafka:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT_HOST
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
    networks:
      - cartonization-network

  # Schema Registry
  schema-registry:
    image: confluentinc/cp-schema-registry:7.5.0
    container_name: cartonization-schema-registry
    depends_on:
      - kafka
    ports:
      - "8081:8081"
    environment:
      SCHEMA_REGISTRY_HOST_NAME: schema-registry
      SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: kafka:29092
      SCHEMA_REGISTRY_LISTENERS: http://0.0.0.0:8081
    networks:
      - cartonization-network

  # Kafka UI (optional but helpful)
  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: cartonization-kafka-ui
    depends_on:
      - kafka
      - schema-registry
    ports:
      - "8090:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092
      KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper:2181
      KAFKA_CLUSTERS_0_SCHEMAREGISTRY: http://schema-registry:8081
    networks:
      - cartonization-network

  # Prometheus
  prometheus:
    image: prom/prometheus:latest
    container_name: cartonization-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
    networks:
      - cartonization-network

  # Grafana
  grafana:
    image: grafana/grafana:latest
    container_name: cartonization-grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_USERS_ALLOW_SIGN_UP=false
    volumes:
      - grafana-data:/var/lib/grafana
      - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources
    networks:
      - cartonization-network

  # Zipkin (for distributed tracing)
  zipkin:
    image: openzipkin/zipkin:latest
    container_name: cartonization-zipkin
    ports:
      - "9411:9411"
    networks:
      - cartonization-network

volumes:
  mongodb-data:
  redis-data:
  prometheus-data:
  grafana-data:

networks:
  cartonization-network:
    driver: bridge
```

### MongoDB Initialization Script

Create `init-mongo.js`:

```javascript
// Switch to the cartonization database
db = db.getSiblingDB('cartonization');

// Create user for the application
db.createUser({
  user: 'cartonization_user',
  pwd: 'cartonization_pass',
  roles: [
    {
      role: 'readWrite',
      db: 'cartonization'
    }
  ]
});

// Create collections with validation
db.createCollection('cartons', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['_id', 'name', 'dimensions', 'maxWeight', 'status'],
      properties: {
        _id: {
          bsonType: 'string',
          description: 'Carton UUID'
        },
        name: {
          bsonType: 'string',
          description: 'Carton name'
        },
        status: {
          enum: ['ACTIVE', 'INACTIVE'],
          description: 'Carton status'
        }
      }
    }
  }
});

// Create indexes
db.cartons.createIndex({ 'status': 1 });
db.cartons.createIndex({ 'name': 'text' });

// Insert sample cartons
db.cartons.insertMany([
  {
    _id: '550e8400-e29b-41d4-a716-446655440000',
    name: 'Small Box (6x6x6)',
    status: 'ACTIVE',
    dimensions: {
      length: { value: 6.0, unit: 'INCHES' },
      width: { value: 6.0, unit: 'INCHES' },
      height: { value: 6.0, unit: 'INCHES' }
    },
    maxWeight: { value: 20.0, unit: 'POUNDS' },
    metadata: {
      createdAt: new Date(),
      updatedAt: new Date(),
      createdBy: 'system',
      version: 1
    }
  },
  {
    _id: '660f9511-f39c-52e5-b827-557766551111',
    name: 'Medium Cube Box (12x12x12)',
    status: 'ACTIVE',
    dimensions: {
      length: { value: 12.0, unit: 'INCHES' },
      width: { value: 12.0, unit: 'INCHES' },
      height: { value: 12.0, unit: 'INCHES' }
    },
    maxWeight: { value: 50.0, unit: 'POUNDS' },
    metadata: {
      createdAt: new Date(),
      updatedAt: new Date(),
      createdBy: 'system',
      version: 1
    }
  }
]);
```

## 2. Project Setup

### Clone and Initialize Project

```bash
# Clone the repository
git clone https://github.com/paklog/cartonization-service.git
cd cartonization-service

# Create project structure
mkdir -p src/main/java/com/paklog/cartonization
mkdir -p src/main/resources
mkdir -p src/test/java/com/paklog/cartonization
mkdir -p src/test/resources
```

### Gradle Build Configuration

Create `build.gradle`:

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'com.google.cloud.tools.jib' version '3.4.0'
    id 'org.sonarqube' version '4.4.1.3373'
    id 'jacoco'
}

group = 'com.paklog'
version = '1.0.0-SNAPSHOT'
sourceCompatibility = '17'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
    maven { url 'https://packages.confluent.io/maven/' }
}

ext {
    set('springCloudVersion', "2023.0.0")
    set('testcontainersVersion', "1.19.3")
    set('mapstructVersion', "1.5.5.Final")
}

dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-cache'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.boot:spring-boot-starter-aop'
    
    // Spring Cloud
    implementation 'org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j'
    implementation 'org.springframework.cloud:spring-cloud-starter-sleuth'
    implementation 'org.springframework.cloud:spring-cloud-sleuth-zipkin'
    
    // Kafka
    implementation 'org.springframework.kafka:spring-kafka'
    implementation 'io.confluent:kafka-avro-serializer:7.5.0'
    implementation 'org.apache.avro:avro:1.11.3'
    
    // OpenAPI/Swagger
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
    
    // Monitoring
    implementation 'io.micrometer:micrometer-registry-prometheus'
    implementation 'io.micrometer:micrometer-tracing-bridge-brave'
    
    // Utilities
    implementation "org.mapstruct:mapstruct:${mapstructVersion}"
    annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
    testImplementation 'org.testcontainers:testcontainers'
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:kafka'
    testImplementation 'org.testcontainers:mongodb'
    testImplementation 'org.mockito:mockito-inline'
    testImplementation 'org.assertj:assertj-core'
    testImplementation 'io.rest-assured:rest-assured'
}

dependencyManagement {
    imports {
        mavenBom "org.testcontainers:testcontainers-bom:${testcontainersVersion}"
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
    }
}

tasks.named('test') {
    useJUnitPlatform()
    finalizedBy jacocoTestReport
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required = true
        html.required = true
    }
}

// Docker image build with Jib
jib {
    from {
        image = 'eclipse-temurin:17-jre'
    }
    to {
        image = 'paklog/cartonization-service'
        tags = ['latest', version]
    }
    container {
        ports = ['8080']
        jvmFlags = ['-Xms512m', '-Xmx1024m']
        environment = [
            'SPRING_PROFILES_ACTIVE': 'docker'
        ]
    }
}
```

### Application Properties

Create `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: cartonization-service

  profiles:
    active: local

  data:
    mongodb:
      uri: mongodb://cartonization_user:cartonization_pass@localhost:27017/cartonization
      auto-index-creation: true

  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

  kafka:
    bootstrap-servers: localhost:9092
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
      schema.registry.url: http://localhost:8081

  cache:
    type: redis
    redis:
      time-to-live: 600000
      cache-null-values: false

kafka:
  topics:
    cartonization-requests: cartonization.requests
    cartonization-solutions: cartonization.solutions
    cartonization-events: cartonization.events

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
  tracing:
    sampling:
      probability: 1.0

logging:
  level:
    com.paklog.cartonization: DEBUG
    org.springframework.data.mongodb.core.MongoTemplate: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

server:
  port: 8080
  error:
    include-message: always
    include-binding-errors: always

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha

product-catalog:
  base-url: http://localhost:8081
  timeout: 5000
```

## 3. Development Workflow

### Starting the Infrastructure

```bash
# Start all infrastructure services
docker-compose up -d

# Verify all services are running
docker-compose ps

# Check logs if needed
docker-compose logs -f kafka
docker-compose logs -f mongodb
```

### Creating Kafka Topics

```bash
# Access Kafka container
docker exec -it cartonization-kafka bash

# Create topics
kafka-topics --create --topic cartonization.requests --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics --create --topic cartonization.solutions --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics --create --topic cartonization.events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# List topics to verify
kafka-topics --list --bootstrap-server localhost:9092
```

### Building and Running the Application

```bash
# Build the project
./gradlew clean build

# Run tests
./gradlew test

# Run the application
./gradlew bootRun

# Or run with specific profile
./gradlew bootRun --args='--spring.profiles.active=local'
```

### IDE Setup

#### IntelliJ IDEA

1. Open IntelliJ IDEA
2. Select "Open" and navigate to the project directory
3. IntelliJ will automatically detect the Gradle project
4. Enable annotation processing:
   - Go to Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"
5. Install recommended plugins:
   - Lombok Plugin
   - Spring Boot Plugin
   - Docker Plugin
   - MongoDB Plugin

#### Visual Studio Code

1. Install extensions:
   ```
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Lombok Annotations Support
   - Docker
   - MongoDB for VS Code
   ```

2. Open the project folder in VS Code

3. Configure Java settings in `.vscode/settings.json`:
   ```json
   {
     "java.configuration.updateBuildConfiguration": "automatic",
     "java.server.launchMode": "Standard",
     "java.compile.nullAnalysis.mode": "automatic"
   }
   ```

## 4. Testing Setup

### Unit Tests

Create test configuration `src/test/resources/application-test.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/cartonization-test

  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      auto-offset-reset: earliest

  cache:
    type: simple

logging:
  level:
    com.paklog.cartonization: DEBUG
```

### Integration Tests with Testcontainers

```java
package com.paklog.cartonization;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class CartonizationIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
    }
}
```

## 5. Monitoring Setup

### Prometheus Configuration

Create `monitoring/prometheus.yml`:

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'cartonization-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8080']
```

### Grafana Dashboard

Create `monitoring/grafana/datasources/prometheus.yml`:

```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
```

## 6. Troubleshooting

### Common Issues and Solutions

#### MongoDB Connection Issues
```bash
# Check MongoDB logs
docker-compose logs mongodb

# Connect to MongoDB directly
docker exec -it cartonization-mongodb mongosh -u admin -p admin123

# Verify database and collections
use cartonization
show collections
```

#### Kafka Connection Issues
```bash
# Check Kafka logs
docker-compose logs kafka

# List topics
docker exec -it cartonization-kafka kafka-topics --list --bootstrap-server localhost:9092

# Check consumer groups
docker exec -it cartonization-kafka kafka-consumer-groups --list --bootstrap-server localhost:9092
```

#### Redis Connection Issues
```bash
# Check Redis logs
docker-compose logs redis

# Connect to Redis CLI
docker exec -it cartonization-redis redis-cli

# Test connection
PING
```

### Performance Tuning

```bash
# JVM options for development
export JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Run with custom JVM options
./gradlew bootRun --args='--spring.profiles.active=local' -Dorg.gradle.jvmargs="$JAVA_OPTS"
```

## 7. API Testing

### Using Postman

1. Import the OpenAPI specification:
   - Open Postman
   - Click "Import" → "File" → Select `openapi.yaml`
   - Postman will generate a collection with all endpoints

2. Set up environment variables:
   ```json
   {
     "baseUrl": "http://localhost:8080/api/v1",
     "requestId": "{{$guid}}",
     "cartonId": "550e8400-e29b-41d4-a716-446655440000"
   }
   ```

### Using cURL

```bash
# Health check
curl http://localhost:8080/actuator/health

# Create a carton
curl -X POST http://localhost:8080/api/v1/cartons \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Box",
    "dimensions": {
      "length": {"value": 10, "unit": "INCHES"},
      "width": {"value": 10, "unit": "INCHES"},
      "height": {"value": 10, "unit": "INCHES"}
    },
    "maxWeight": {"value": 30, "unit": "POUNDS"}
  }'

# List cartons
curl http://localhost:8080/api/v1/cartons

# Calculate packing solution
curl -X POST http://localhost:8080/api/v1/packing-solutions \
  -H "Content-Type: application/json" \
  -H "X-Request-ID: test-123" \
  -d '{
    "items": [
      {"sku": "WIDGET-001", "quantity": 2},
      {"sku": "GADGET-002", "quantity": 1}
    ],
    "orderId": "ORD-123"
  }'
```

## 8. Continuous Integration

### GitHub Actions Workflow

Create `.github/workflows/ci.yml`:

```yaml
name: CI Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      mongodb:
        image: mongo:7.0
        ports:
          - 27017:27017
        options: >-
          --health-cmd="mongosh --eval 'db.adminCommand(\"ping\")'"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=5
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build with Gradle
      run: ./gradlew build
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Test Results
        path: build/test-results/test/*.xml
        reporter: java-junit
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: build/reports/jacoco/test/jacocoTestReport.xml
```

## 9. Useful Commands Reference

```bash
# Development
./gradlew bootRun                    # Run application
./gradlew test                        # Run tests
./gradlew build                       # Build project
./gradlew clean                       # Clean build artifacts

# Docker
docker-compose up -d                  # Start infrastructure
docker-compose down                   # Stop infrastructure
docker-compose logs -f [service]      # View logs
docker-compose restart [service]      # Restart service

# Monitoring
open http://localhost:8080/swagger-ui.html  # Swagger UI
open http://localhost:8090                  # Kafka UI
open http://localhost:9090                  # Prometheus
open http://localhost:3000                  # Grafana (admin/admin)
open http://localhost:9411                  # Zipkin

# Database
docker exec -it cartonization-mongodb mongosh -u admin -p admin123
docker exec -it cartonization-redis redis-cli
```

## Next Steps

After setting up your development environment:

1. ✅ Verify all infrastructure services are running
2. ✅ Run the application locally
3. ✅ Execute the test suite
4. ✅ Test API endpoints using Swagger UI or Postman
5. ✅ Set up your IDE with proper configurations
6. ✅ Configure monitoring dashboards
7. 🚀 Start implementing features following the architecture plan

For any issues or questions, refer to the troubleshooting section or consult the team documentation.