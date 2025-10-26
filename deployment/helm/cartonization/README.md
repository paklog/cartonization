# Cartonization Helm Chart

3D bin-packing optimization engine for shipping cartons

## Installation

```bash
helm install cartonization .
```

## Configuration

See `values.yaml` for all configuration options.

### Key Configuration

```yaml
image:
  repository: paklog/cartonization
  tag: latest

service:
  port: 8084
  managementPort: 8081

resources:
  limits:
    cpu: 1000m
    memory: 1Gi
  requests:
    cpu: 500m
    memory: 512Mi

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
```

### Database Configuration

MongoDB is enabled by default.

Redis is enabled for caching.

### Observability

- **Metrics**: Prometheus metrics available at `/actuator/prometheus`
- **Tracing**: OpenTelemetry traces sent to Tempo
- **Logging**: Structured logs sent to Loki

## Uninstallation

```bash
helm uninstall cartonization
```

## Maintainers

- Paklog Team
