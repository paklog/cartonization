# Cartonization Service

A production-ready microservice for optimizing shipping carton utilization using advanced 3D bin-packing algorithms, built with Spring Boot, Kafka, MongoDB, and hexagonal architecture.

## 🎯 Overview

The Cartonization Service is a sophisticated optimization engine that calculates the most efficient way to pack items into shipping cartons. It uses advanced algorithms to minimize shipping costs while ensuring items fit properly and safely within available carton types.

### Key Features

- **🧮 Advanced Algorithms**: 3D bin-packing with multiple optimization strategies
- **🏗️ Clean Architecture**: Hexagonal architecture with clear separation of concerns
- **📊 Real-time Metrics**: Comprehensive monitoring and performance tracking
- **🔒 Enterprise Security**: Authentication, authorization, and secure communication
- **⚡ High Performance**: Optimized algorithms with caching and async processing
- **🔄 Event-Driven**: Kafka integration for reliable event publishing
- **📈 Auto-Scaling**: Kubernetes deployment with horizontal pod autoscaling
- **🧪 Test Coverage**: Comprehensive testing with Testcontainers

## 🏛️ Architecture

### Hexagonal Architecture (Ports & Adapters)

```
├── Domain Layer (Pure Business Logic)
├── Application Layer (Use Cases & Services)
├── Infrastructure Layer (External Dependencies)
└── Clean separation with dependency inversion
```

### Technology Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Framework** | Spring Boot 3.2.0 | Application framework |
| **Language** | Java 17 | Runtime environment |
| **Database** | MongoDB | Document storage |
| **Messaging** | Apache Kafka | Event streaming |
| **Cache** | Redis | Performance optimization |
| **Security** | Spring Security | Authentication & authorization |
| **Monitoring** | Micrometer + Prometheus | Observability |
| **Container** | Docker + Kubernetes | Deployment |
| **Build** | Gradle | Dependency management |

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Kubernetes cluster (optional for local development)

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/paklog/cartonization-service.git
   cd cartonization-service
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose up -d
   ```

3. **Build and run the application**
   ```bash
   ./gradlew bootRun
   ```

4. **Verify the service is running**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

### API Usage

#### Create a Carton

```bash
curl -X POST http://localhost:8080/api/v1/cartons \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Medium Box",
    "dimensions": {
      "length": {"value": 12, "unit": "INCHES"},
      "width": {"value": 8, "unit": "INCHES"},
      "height": {"value": 6, "unit": "INCHES"}
    },
    "maxWeight": {"value": 25, "unit": "POUNDS"}
  }'
```

#### Calculate Packing Solution

```bash
curl -X POST http://localhost:8080/api/v1/packing-solutions \
  -H "Content-Type: application/json" \
  -H "X-Request-ID: test-123" \
  -d '{
    "items": [
      {"sku": "WIDGET-001", "quantity": 2},
      {"sku": "GADGET-002", "quantity": 1}
    ],
    "orderId": "ORD-123",
    "optimizeForMinimumBoxes": true
  }'
```

## 📚 API Documentation

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/cartons` | Create a new carton type |
| `GET` | `/api/v1/cartons` | List all carton types |
| `GET` | `/api/v1/cartons/{id}` | Get carton by ID |
| `PUT` | `/api/v1/cartons/{id}` | Update carton |
| `DELETE` | `/api/v1/cartons/{id}` | Deactivate carton |
| `POST` | `/api/v1/packing-solutions` | Calculate optimal packing |

### Monitoring Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Health check |
| `/actuator/info` | Application information |
| `/actuator/metrics` | Application metrics |
| `/actuator/prometheus` | Prometheus metrics |
| `/swagger-ui.html` | API documentation |

## 🧪 Testing

### Unit Tests

```bash
./gradlew test
```

### Integration Tests

```bash
./gradlew integrationTest
```

### Performance Tests

```bash
./gradlew performanceTest
```

### Test Coverage Report

```bash
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## 🐳 Docker Deployment

### Build Docker Image

```bash
./gradlew bootBuildImage
```

### Run with Docker Compose

```bash
docker-compose up -d
```

### Kubernetes Deployment

```bash
# Deploy to Kubernetes
./deploy.sh

# Or deploy manually
kubectl apply -f k8s/
```

## 📊 Monitoring & Observability

### Metrics

The service exposes comprehensive metrics:

- **Business Metrics**: Packing requests, success/failure rates
- **Performance Metrics**: Algorithm execution time, memory usage
- **System Metrics**: CPU, memory, disk usage
- **Custom Metrics**: Carton utilization, package efficiency

### Dashboards

- **Grafana**: Pre-configured dashboards for monitoring
- **Prometheus**: Metrics collection and alerting
- **Zipkin**: Distributed tracing

### Health Checks

- **Liveness Probe**: Container health
- **Readiness Probe**: Service availability
- **Startup Probe**: Initial startup time

## 🔒 Security

### Authentication & Authorization

- HTTP Basic Authentication
- JWT token support (configurable)
- Role-based access control
- API key authentication

### Data Protection

- TLS/SSL encryption
- Sensitive data encryption at rest
- Secure credential management
- Audit logging

## ⚡ Performance Optimization

### Algorithm Optimization

- **Best Fit Decreasing**: Minimizes number of boxes
- **First Fit Decreasing**: Faster execution
- **Parallel Processing**: Multi-threaded computation
- **Caching**: Redis-based result caching

### Infrastructure Optimization

- **Horizontal Scaling**: Kubernetes HPA
- **Load Balancing**: NGINX ingress
- **Database Indexing**: Optimized MongoDB queries
- **Connection Pooling**: Efficient resource usage

## 🔄 CI/CD Pipeline

### GitHub Actions Workflow

```yaml
name: CI/CD Pipeline
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Build and Test
        run: ./gradlew build
      - name: Build Docker Image
        run: ./gradlew bootBuildImage
```

### Deployment Strategy

- **Blue-Green Deployment**: Zero-downtime deployments
- **Canary Releases**: Gradual rollout with monitoring
- **Rollback Strategy**: Automated rollback on failures
- **Environment Promotion**: Dev → Staging → Production

## 📈 Scaling Strategy

### Horizontal Scaling

- **Pod Autoscaling**: CPU/memory-based scaling
- **Request-based Scaling**: HTTP request rate scaling
- **Custom Metrics**: Business metric-based scaling

### Database Scaling

- **MongoDB Sharding**: Horizontal data distribution
- **Read Replicas**: Read workload distribution
- **Connection Pooling**: Efficient connection management

### Caching Strategy

- **Redis Cluster**: Distributed caching
- **TTL-based Expiration**: Automatic cache cleanup
- **Cache Warming**: Pre-populated frequently used data

## 🛠️ Development

### Project Structure

```
cartonization-service/
├── src/main/java/com/paklog/cartonization/
│   ├── CartonizationApplication.java
│   ├── domain/                    # Domain layer
│   ├── application/               # Application layer
│   └── infrastructure/            # Infrastructure layer
├── src/test/                      # Test sources
├── k8s/                          # Kubernetes manifests
├── docker-compose.yml            # Local development
├── Dockerfile                    # Container definition
├── deploy.sh                     # Deployment script
└── README.md                     # This file
```

### Development Workflow

1. **Create Feature Branch**
   ```bash
   git checkout -b feature/new-algorithm
   ```

2. **Implement Changes**
   ```bash
   # Follow hexagonal architecture principles
   # Add tests for new functionality
   # Update documentation
   ```

3. **Run Tests**
   ```bash
   ./gradlew test
   ./gradlew integrationTest
   ```

4. **Build and Deploy**
   ```bash
   ./gradlew build
   ./deploy.sh
   ```

## 🤝 Contributing

### Code Standards

- **SOLID Principles**: Single responsibility, open/closed, etc.
- **Clean Code**: Meaningful names, small functions, comprehensive tests
- **Documentation**: Inline comments, API documentation
- **Testing**: Unit tests, integration tests, performance tests

### Pull Request Process

1. Fork the repository
2. Create a feature branch
3. Implement changes with tests
4. Ensure all tests pass
5. Update documentation
6. Submit pull request

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 📞 Support

### Documentation

- [API Documentation](http://localhost:8080/swagger-ui.html)
- [Architecture Diagrams](architecture-diagrams.md)
- [Implementation Plan](cartonization-implementation-plan.md)
- [Development Setup](development-environment-setup.md)

### Monitoring

- **Health Checks**: `/actuator/health`
- **Metrics**: `/actuator/prometheus`
- **Logs**: Application logs with structured format

### Troubleshooting

- **Common Issues**: Check the troubleshooting section in development setup
- **Performance Issues**: Monitor metrics and adjust scaling
- **Deployment Issues**: Check Kubernetes events and pod logs

---

## 🎉 Success Metrics

The Cartonization Service demonstrates:

- **🏆 Enterprise Architecture**: Production-ready microservice
- **🧮 Mathematical Excellence**: Advanced optimization algorithms
- **⚡ Performance Excellence**: Optimized for speed and scalability
- **🔒 Security Excellence**: Enterprise-grade security features
- **📊 Monitoring Excellence**: Comprehensive observability
- **🧪 Testing Excellence**: Thorough test coverage
- **🚀 Deployment Excellence**: Automated CI/CD pipeline

**Ready for production deployment and immediate business value delivery!** 🚀