# cartonization - Helm Deployment

3D bin-packing optimization engine

## Quick Deploy

Deploy this service only:

```bash
cd deployment/helm/cartonization
helm dependency update
helm install cartonization . -n paklog --create-namespace
```

## Configuration

Edit `values.yaml` to configure:

- Replica count
- Resource limits
- Database connections
- Kafka settings
- Environment variables

## Verify Deployment

```bash
# Check pod status
kubectl get pods -n paklog -l app.kubernetes.io/name=cartonization

# Check service
kubectl get svc -n paklog cartonization

# View logs
kubectl logs -n paklog -l app.kubernetes.io/name=cartonization -f

# Check health
kubectl port-forward -n paklog svc/cartonization 8084:8084
curl http://localhost:8084/actuator/health
```

## Update Deployment

```bash
helm upgrade cartonization . -n paklog
```

## Uninstall

```bash
helm uninstall cartonization -n paklog
```

## Deploy as Part of Platform

To deploy all services together, use the umbrella chart:

```bash
cd ../../../../deployments/helm/paklog-platform
helm dependency update
helm install paklog-platform . -n paklog --create-namespace
```

See [Platform Documentation](../../../../deployments/helm/README.md) for more details.
