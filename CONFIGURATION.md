# Cartonization Service - Configuration Guide

## MongoDB Connection Configuration

The service supports both **authenticated** and **unauthenticated** MongoDB connections depending on the environment.

### Local Development (No Authentication)

For local development without MongoDB authentication:

**application.yml (default):**
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/cartonization
```

**No environment variables needed** - the service will connect to local MongoDB without credentials.

---

### Docker Environment (With Authentication)

When running with Docker Compose, MongoDB is configured with authentication.

**Docker Compose Setup:**
- MongoDB Root User: `admin` / `admin123`
- Application User: `cartonization_user` / `cartonization_pass`
- Database: `cartonization`

**application-docker.yml:**
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://cartonization_user:cartonization_pass@mongodb:27017/cartonization
```

The `docker/mongodb/init-mongo.js` script automatically creates:
- Application user with readWrite permissions
- Collections with validation schemas
- Performance indexes
- Sample data

---

### Production Environment (With Authentication)

For production deployments, use environment variables:

**Option 1: Individual Variables**
```bash
MONGO_HOST=mongodb-prod.example.com
MONGO_PORT=27017
MONGO_DATABASE=cartonization
MONGO_AUTH_SOURCE=?authSource=admin&authMechanism=SCRAM-SHA-256
```

**Option 2: Full Connection String (Recommended)**
```bash
SPRING_DATA_MONGODB_URI=mongodb://user:password@mongodb-prod.example.com:27017/cartonization?authSource=admin&ssl=true
```

---

## Server Port Configuration

### Current Configuration

**Port:** 8084 (updated from default 8080)

```yaml
server:
  port: 8084
```

### Available Endpoints

- **Application:** http://localhost:8084
- **Health Check:** http://localhost:8084/actuator/health
- **API Docs:** http://localhost:8084/swagger-ui.html
- **Metrics:** http://localhost:8084/actuator/prometheus

---

## Redis Configuration

### Local Development (No Password)

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    # No password for local development
```

**No environment variables needed** - Redis runs without password locally.

### Docker/Production (With Password)

**Docker (application-docker.yml):**
```yaml
spring:
  redis:
    host: redis
    port: 6379
    password: redis123
```

**Production Environment Variables:**
```bash
REDIS_HOST=redis-prod.example.com
REDIS_PORT=6379
REDIS_PASSWORD=your_secure_password

# Or set via Spring property
SPRING_DATA_REDIS_PASSWORD=your_secure_password
```

---

## Kafka Configuration

### Local Development

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

### Docker Environment

```yaml
spring:
  kafka:
    bootstrap-servers: kafka:29092
```

### Production

```bash
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka1.example.com:9092,kafka2.example.com:9092,kafka3.example.com:9092
```

---

## Complete Environment Variable Reference

### Minimal Local Development
```bash
# No variables needed - uses defaults in application.yml
```

### Docker Deployment
```bash
SPRING_PROFILES_ACTIVE=docker
# Other configs in application-docker.yml
```

### Production Deployment
```bash
# MongoDB
SPRING_DATA_MONGODB_URI=mongodb://user:pass@host:port/db?authSource=admin&ssl=true

# Redis
REDIS_HOST=redis-prod.example.com
REDIS_PORT=6379
REDIS_PASSWORD=secure_password

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka1:9092,kafka2:9092,kafka3:9092

# External Services
PRODUCT_CATALOG_BASE_URL=https://product-catalog.example.com

# Server
SERVER_PORT=8084

# Security
SPRING_SECURITY_USER_NAME=admin
SPRING_SECURITY_USER_PASSWORD=secure_admin_password

# Logging
LOGGING_LEVEL_COM_PAKLOG_CARTONIZATION=INFO
```

---

## Profile-Specific Configuration Files

### application.yml (default/local)
- Local development without authentication
- MongoDB: localhost:27017
- Redis: localhost:6379 (no password)
- Kafka: localhost:9092
- Server Port: 8084
- Debug logging enabled

### application-docker.yml
- Docker Compose environment
- MongoDB with authentication
- Redis with password
- Kafka internal networking
- Server Port: 8080
- Info-level logging

### application-test.yml
- Test environment with Testcontainers
- Embedded test databases
- Mock external services

---

## Docker Compose Services

When running `docker-compose up`, the following services are available:

| Service | Port | Description |
|---------|------|-------------|
| cartonization-service | 8080 | Main application |
| mongodb | 27017 | MongoDB database |
| mongodb-express | 8086 | MongoDB web UI |
| redis | 6379 | Redis cache |
| redis-commander | 8087 | Redis web UI |
| kafka | 9092 | Kafka broker |
| kafka-ui | 8085 | Kafka management UI |
| product-catalog-mock | 8082 | Mock product service |

### Access URLs (Docker)
- **Application:** http://localhost:8080
- **MongoDB UI:** http://localhost:8086 (admin/admin123)
- **Redis UI:** http://localhost:8087 (admin/admin123)
- **Kafka UI:** http://localhost:8085
- **Mock Server:** http://localhost:8082

---

## MongoDB Initialization

The `docker/mongodb/init-mongo.js` script creates:

### User
```javascript
user: 'cartonization_user'
password: 'cartonization_pass'
role: readWrite on cartonization database
```

### Collections
1. **cartons** - Carton type definitions with validation
2. **packing_solutions** - Calculated packing solutions

### Indexes
```javascript
// Cartons
{ status: 1 }
{ name: 1 }
{ createdAt: -1 }
{ 'dimensions.length': 1, 'dimensions.width': 1, 'dimensions.height': 1 }

// Packing Solutions
{ requestId: 1 }
{ orderId: 1 }
{ createdAt: -1 }
```

### Sample Data
Three sample cartons are pre-loaded:
- Small Box (20x15x10 cm, 5kg max)
- Medium Box (35x25x20 cm, 15kg max)
- Large Box (50x40x30 cm, 25kg max)

---

## Troubleshooting

### MongoDB Connection Issues

**Problem:** `MongoTimeoutException` or `Authentication failed`

**Solutions:**

1. **Local Development - No Auth:**
   ```bash
   # Start MongoDB without auth
   mongod --dbpath /data/db

   # Or use Docker without auth
   docker run -d -p 27017:27017 mongo:7.0
   ```

2. **Docker - Check Init Script:**
   ```bash
   # View MongoDB logs
   docker logs cartonization-mongodb

   # Should see: "✅ Cartonization database initialized successfully"
   ```

3. **Production - Verify Credentials:**
   ```bash
   # Test connection
   mongosh "mongodb://user:pass@host:port/cartonization?authSource=admin"
   ```

### Redis Connection Issues

**Problem:** `RedisConnectionException`

**Solutions:**

1. **Check Redis is running:**
   ```bash
   redis-cli ping
   # Should return: PONG
   ```

2. **With password:**
   ```bash
   redis-cli -a your_password ping
   ```

3. **Check environment variables:**
   ```bash
   echo $REDIS_HOST
   echo $REDIS_PORT
   echo $REDIS_PASSWORD
   ```

### Port Conflicts

**Problem:** `Address already in use`

**Solutions:**

1. **Find process using port:**
   ```bash
   lsof -i :8084
   ```

2. **Kill process:**
   ```bash
   kill -9 <PID>
   ```

3. **Change port:**
   ```yaml
   server:
     port: 8085  # Use different port
   ```

---

## Security Best Practices

### Development
✅ Use default credentials (documented)
✅ No authentication for local MongoDB
✅ Simple Redis password

### Production
⚠️ **NEVER** commit credentials to version control
✅ Use environment variables or secrets manager
✅ Enable SSL/TLS for MongoDB
✅ Use strong passwords (16+ characters)
✅ Rotate credentials regularly
✅ Use network isolation
✅ Enable authentication on all services

### Example Production Setup
```bash
# Use secrets management
export MONGO_URI=$(aws secretsmanager get-secret-value --secret-id mongo-uri --query SecretString --output text)
export REDIS_PASSWORD=$(vault read -field=password secret/redis/prod)

# Or use Kubernetes secrets
kubectl create secret generic cartonization-secrets \
  --from-literal=mongo-uri='mongodb://...' \
  --from-literal=redis-password='...'
```

---

## Quick Start Commands

### Local Development
```bash
# Start local MongoDB (no auth)
docker run -d -p 27017:27017 --name mongo mongo:7.0

# Start local Redis (no auth)
docker run -d -p 6379:6379 --name redis redis:7.2-alpine

# Start application
mvn spring-boot:run
```

### Docker Compose
```bash
# Start all services
docker-compose up -d

# Check logs
docker-compose logs -f cartonization-service

# Stop all services
docker-compose down
```

### Production
```bash
# Set environment variables
export SPRING_DATA_MONGODB_URI="mongodb://..."
export REDIS_HOST="redis-prod.example.com"
export REDIS_PASSWORD="..."

# Run application
java -jar cartonization-service.jar --spring.profiles.active=prod
```

---

## Security Configuration

### Current Status

⚠️ **Spring Security has been removed** for easier development and testing.

All API endpoints are **publicly accessible** without authentication:
- ✅ `/api/**` - No authentication required
- ✅ `/actuator/**` - No authentication required
- ✅ `/swagger-ui/**` - No authentication required

### For Production

When deploying to production, you should add authentication. Options include:

1. **Add Spring Security back:**
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-security</artifactId>
   </dependency>
   ```

2. **Use API Gateway** (Recommended):
   - Kong, AWS API Gateway, or Azure API Management
   - Handle authentication at the gateway level

3. **Add OAuth2/JWT:**
   - Spring Security OAuth2 Resource Server
   - Keycloak or Auth0 integration

---

**Last Updated:** 2025-10-04
**Port:** 8084
**MongoDB Auth:** Configurable (none for local, required for docker/prod)
**Security:** Disabled (add for production)
