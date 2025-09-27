# 🐳 Docker Setup for Cartonization Service

This guide provides comprehensive instructions for running the Cartonization Service using Docker and Docker Compose.

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Development Environment](#development-environment)
- [Production Environment](#production-environment)
- [Configuration](#configuration)
- [Services Overview](#services-overview)
- [Monitoring & Management](#monitoring--management)
- [Troubleshooting](#troubleshooting)
- [Advanced Usage](#advanced-usage)

## 🔧 Prerequisites

- **Docker**: Version 20.10 or higher
- **Docker Compose**: Version 2.0 or higher
- **System Requirements**:
  - RAM: Minimum 4GB, Recommended 8GB
  - Storage: Minimum 10GB free space
  - CPU: 2+ cores recommended

### Verify Installation

```bash
docker --version
docker-compose --version
```

## 🚀 Quick Start

### 1. Clone and Setup

```bash
git clone <repository-url>
cd cartonization
cp .env.example .env
```

### 2. Start Development Environment

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f cartonization-service

# Check service health
curl http://localhost:8080/api/v1/health
```

### 3. Access Services

- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **MongoDB Express**: http://localhost:8086 (admin/admin123)
- **Redis Commander**: http://localhost:8087 (admin/admin123)
- **Kafka UI**: http://localhost:8085

## 🛠️ Development Environment

The development setup includes all necessary services with debugging capabilities enabled.

### Start Services

```bash
# Start all services in background
docker-compose up -d

# Start with logs visible
docker-compose up

# Start specific service
docker-compose up cartonization-service mongodb redis
```

### Service Management

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (⚠️ destroys data)
docker-compose down -v

# Restart a specific service
docker-compose restart cartonization-service

# View service logs
docker-compose logs -f [service-name]

# Scale a service
docker-compose up -d --scale cartonization-service=2
```

### Development Features

- **Hot Reload**: Code changes require rebuild
- **Debug Logging**: Detailed logs enabled
- **Test Data**: Sample cartons pre-loaded
- **Mock Services**: Product catalog mock included

## 🏭 Production Environment

### Start Production Stack

```bash
# Copy and configure production environment
cp .env.example .env.prod

# Start production services
docker-compose -f docker-compose.prod.yml up -d

# Monitor startup
docker-compose -f docker-compose.prod.yml logs -f
```

### Production Features

- **Security**: Non-root containers, restricted permissions
- **Performance**: Optimized JVM settings and resource limits
- **Monitoring**: Health checks and metrics enabled
- **Load Balancing**: Nginx reverse proxy
- **Persistence**: Data volumes for durability

### Production Configuration

```bash
# Environment variables for production
export MONGODB_ROOT_PASSWORD=secure-password
export REDIS_PASSWORD=secure-redis-password
export PRODUCT_CATALOG_BASE_URL=https://api.yourcompany.com

# Start with production settings
docker-compose -f docker-compose.prod.yml up -d
```

## ⚙️ Configuration

### Environment Variables

Key environment variables in `.env` file:

```bash
# Database
MONGODB_ROOT_PASSWORD=admin123
MONGODB_PASSWORD=cartonization_pass

# Cache
REDIS_PASSWORD=redis123

# External Services
PRODUCT_CATALOG_BASE_URL=http://product-catalog:8080

# Application
SPRING_PROFILES_ACTIVE=docker
LOG_LEVEL=INFO
```

### Application Configuration

The service uses profile-specific configurations:

- `application.yml` - Base configuration
- `application-docker.yml` - Docker-specific overrides
- Environment variables override config files

### Custom Configuration

Create custom configuration files:

```bash
# Create custom application config
mkdir -p docker/config
echo "logging.level.com.paklog: DEBUG" > docker/config/application-custom.yml
```

Add to docker-compose.yml:
```yaml
volumes:
  - ./docker/config/application-custom.yml:/app/config/application-custom.yml:ro
```

## 🏗️ Services Overview

### Core Services

| Service | Port | Description |
|---------|------|-------------|
| **Cartonization Service** | 8080 | Main application service |
| **MongoDB** | 27017 | Primary database |
| **Redis** | 6379 | Caching layer |

### Supporting Services

| Service | Port | Description |
|---------|------|-------------|
| **Kafka** | 9092 | Message broker |
| **Zookeeper** | 2181 | Kafka coordination |

### Development Tools

| Service | Port | Description |
|---------|------|-------------|
| **MongoDB Express** | 8086 | Database admin UI |
| **Redis Commander** | 8087 | Redis admin UI |
| **Kafka UI** | 8085 | Kafka management UI |
| **Product Catalog Mock** | 8081 | Mock external service |

### Production Services

| Service | Port | Description |
|---------|------|-------------|
| **Nginx** | 80/443 | Reverse proxy & load balancer |

## 📊 Monitoring & Management

### Health Checks

```bash
# Application health
curl http://localhost:8080/api/v1/health

# Individual service health
docker-compose ps
docker-compose top
```

### Logs Management

```bash
# View all logs
docker-compose logs

# Follow specific service logs
docker-compose logs -f cartonization-service

# View last N lines
docker-compose logs --tail=100 cartonization-service

# Search logs
docker-compose logs cartonization-service | grep ERROR
```

### Resource Monitoring

```bash
# Container resource usage
docker stats

# Service-specific stats
docker stats cartonization-service-container

# Disk usage
docker system df
```

### Metrics and Observability

Access built-in monitoring endpoints:

- **Health**: `/api/v1/health`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`
- **Info**: `/api/v1/health/info`

## 🔍 Troubleshooting

### Common Issues

#### Service Won't Start

```bash
# Check service status
docker-compose ps

# View error logs
docker-compose logs cartonization-service

# Check resource usage
docker stats

# Verify network connectivity
docker-compose exec cartonization-service ping mongodb
```

#### Database Connection Issues

```bash
# Test MongoDB connection
docker-compose exec mongodb mongosh -u admin -p admin123

# Check MongoDB logs
docker-compose logs mongodb

# Verify network
docker network ls
docker network inspect cartonization_cartonization-network
```

#### Out of Memory Errors

```bash
# Increase JVM memory
export JAVA_OPTS="-Xmx2g -Xms1g"
docker-compose up cartonization-service

# Check system resources
free -h
docker system prune
```

#### Port Conflicts

```bash
# Check port usage
netstat -tlnp | grep :8080
lsof -i :8080

# Change ports in docker-compose.yml
ports:
  - "8081:8080"  # Change host port
```

### Debug Mode

Enable debug logging:

```bash
# Set debug environment
echo "LOGGING_LEVEL_COM_PAKLOG_CARTONIZATION=DEBUG" >> .env

# Restart with debug
docker-compose up -d cartonization-service
docker-compose logs -f cartonization-service
```

### Reset Everything

```bash
# Complete cleanup (⚠️ destroys all data)
docker-compose down -v
docker system prune -a
docker volume prune

# Fresh start
docker-compose up -d
```

## 🚀 Advanced Usage

### Custom Builds

```bash
# Build custom image
docker build -t cartonization-service:custom .

# Use custom image
docker-compose -f docker-compose.custom.yml up
```

### Multi-Environment Setup

```bash
# Development
docker-compose up -d

# Staging
docker-compose -f docker-compose.yml -f docker-compose.staging.yml up -d

# Production
docker-compose -f docker-compose.prod.yml up -d
```

### Performance Tuning

#### JVM Optimization

```bash
# Optimize for container
JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# G1 Garbage Collector
JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC -XX:G1HeapRegionSize=16m"

# String deduplication
JAVA_OPTS="$JAVA_OPTS -XX:+UseStringDeduplication"
```

#### Database Tuning

```yaml
# MongoDB optimization
mongodb:
  command: mongod --wiredTigerCacheSizeGB=1
```

#### Redis Optimization

```yaml
# Redis memory optimization
redis:
  command: redis-server --maxmemory 512mb --maxmemory-policy allkeys-lru
```

### Backup and Recovery

#### MongoDB Backup

```bash
# Create backup
docker-compose exec mongodb mongodump --uri="mongodb://admin:admin123@localhost:27017/cartonization" --out=/backup

# Restore backup
docker-compose exec mongodb mongorestore --uri="mongodb://admin:admin123@localhost:27017/cartonization" /backup/cartonization
```

#### Volume Backup

```bash
# Backup volumes
docker run --rm -v cartonization_mongodb_data:/data -v $(pwd):/backup alpine tar czf /backup/mongodb-backup.tar.gz -C /data .

# Restore volumes
docker run --rm -v cartonization_mongodb_data:/data -v $(pwd):/backup alpine tar xzf /backup/mongodb-backup.tar.gz -C /data
```

### Scaling

#### Horizontal Scaling

```bash
# Scale application instances
docker-compose up -d --scale cartonization-service=3

# Load balancer configuration required for multiple instances
```

#### Vertical Scaling

```yaml
# Increase resource limits
deploy:
  resources:
    limits:
      memory: 2GB
      cpus: "2.0"
```

## 📞 Support

### Getting Help

- **Logs**: Always check service logs first
- **Health Checks**: Use `/api/v1/health` endpoint
- **Documentation**: Refer to API docs at `/swagger-ui.html`
- **Issues**: Check GitHub issues for known problems

### Useful Commands Reference

```bash
# Quick status check
docker-compose ps && curl -s http://localhost:8080/api/v1/health | jq .

# Resource overview
docker stats --no-stream

# Network debugging
docker network inspect cartonization_cartonization-network

# Clean up resources
docker system prune --volumes
```

---

**Happy Dockerizing! 🐳**