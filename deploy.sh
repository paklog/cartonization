#!/bin/bash

# Cartonization Service Deployment Script
# This script handles the complete deployment process for the Cartonization service

set -e

# Configuration
NAMESPACE="cartonization"
SERVICE_NAME="cartonization-service"
DOCKER_REGISTRY="paklog"
DOCKER_TAG="${DOCKER_TAG:-latest}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."

    # Check if kubectl is installed
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed. Please install it first."
        exit 1
    fi

    # Check if docker is installed
    if ! command -v docker &> /dev/null; then
        log_error "docker is not installed. Please install it first."
        exit 1
    fi

    # Check if gradle is available
    if ! command -v ./gradlew &> /dev/null && ! command -v gradle &> /dev/null; then
        log_error "Gradle is not available. Please install it or use the gradlew wrapper."
        exit 1
    fi

    log_success "Prerequisites check passed"
}

# Function to build the application
build_application() {
    log_info "Building application..."

    # Clean and build
    ./gradlew clean build

    log_success "Application built successfully"
}

# Function to build Docker image
build_docker_image() {
    log_info "Building Docker image..."

    # Build Docker image
    docker build -t ${DOCKER_REGISTRY}/${SERVICE_NAME}:${DOCKER_TAG} .

    # Tag as latest if not already
    if [ "$DOCKER_TAG" != "latest" ]; then
        docker tag ${DOCKER_REGISTRY}/${SERVICE_NAME}:${DOCKER_TAG} ${DOCKER_REGISTRY}/${SERVICE_NAME}:latest
    fi

    log_success "Docker image built: ${DOCKER_REGISTRY}/${SERVICE_NAME}:${DOCKER_TAG}"
}

# Function to push Docker image
push_docker_image() {
    log_info "Pushing Docker image to registry..."

    # Push to registry
    docker push ${DOCKER_REGISTRY}/${SERVICE_NAME}:${DOCKER_TAG}

    if [ "$DOCKER_TAG" != "latest" ]; then
        docker push ${DOCKER_REGISTRY}/${SERVICE_NAME}:latest
    fi

    log_success "Docker image pushed successfully"
}

# Function to create namespace
create_namespace() {
    log_info "Creating namespace: ${NAMESPACE}"

    kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

    log_success "Namespace created"
}

# Function to deploy infrastructure dependencies
deploy_infrastructure() {
    log_info "Deploying infrastructure dependencies..."

    # Note: In a real environment, you would deploy MongoDB, Redis, Kafka separately
    # For this demo, we'll assume they are already running

    log_success "Infrastructure deployment completed"
}

# Function to deploy application
deploy_application() {
    log_info "Deploying application to Kubernetes..."

    # Apply Kubernetes manifests
    kubectl apply -f k8s/configmap.yaml -n ${NAMESPACE}
    kubectl apply -f k8s/secret.yaml -n ${NAMESPACE}
    kubectl apply -f k8s/service.yaml -n ${NAMESPACE}
    kubectl apply -f k8s/deployment.yaml -n ${NAMESPACE}
    kubectl apply -f k8s/ingress.yaml -n ${NAMESPACE}
    kubectl apply -f k8s/hpa.yaml -n ${NAMESPACE}

    log_success "Application deployed successfully"
}

# Function to wait for deployment
wait_for_deployment() {
    log_info "Waiting for deployment to be ready..."

    kubectl wait --for=condition=available --timeout=300s deployment/${SERVICE_NAME} -n ${NAMESPACE}

    log_success "Deployment is ready"
}

# Function to run health checks
run_health_checks() {
    log_info "Running health checks..."

    # Get service URL
    SERVICE_URL=$(kubectl get svc ${SERVICE_NAME} -n ${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
    if [ -z "$SERVICE_URL" ]; then
        SERVICE_URL=$(kubectl get svc ${SERVICE_NAME} -n ${NAMESPACE} -o jsonpath='{.spec.clusterIP}')
    fi

    # Wait for service to be ready
    sleep 30

    # Check health endpoint
    if curl -f http://${SERVICE_URL}/actuator/health; then
        log_success "Health check passed"
    else
        log_error "Health check failed"
        exit 1
    fi
}

# Function to show deployment status
show_status() {
    log_info "Deployment status:"

    echo ""
    echo "Pods:"
    kubectl get pods -n ${NAMESPACE} -l app=${SERVICE_NAME}

    echo ""
    echo "Services:"
    kubectl get svc -n ${NAMESPACE} -l app=${SERVICE_NAME}

    echo ""
    echo "Ingress:"
    kubectl get ingress -n ${NAMESPACE} -l app=${SERVICE_NAME}

    echo ""
    echo "Application URLs:"
    echo "- Health Check: http://$(kubectl get svc ${SERVICE_NAME} -n ${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')/actuator/health"
    echo "- API Docs: http://$(kubectl get svc ${SERVICE_NAME} -n ${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')/swagger-ui.html"
    echo "- Metrics: http://$(kubectl get svc ${SERVICE_NAME} -n ${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')/actuator/prometheus"
}

# Function to cleanup on failure
cleanup() {
    log_warning "Deployment failed. Cleaning up..."

    kubectl delete namespace ${NAMESPACE} --ignore-not-found=true

    log_info "Cleanup completed"
}

# Main deployment function
main() {
    log_info "Starting Cartonization Service deployment..."

    # Set trap for cleanup on error
    trap cleanup ERR

    # Run deployment steps
    check_prerequisites
    build_application
    build_docker_image
    push_docker_image
    create_namespace
    deploy_infrastructure
    deploy_application
    wait_for_deployment
    run_health_checks
    show_status

    log_success "🎉 Cartonization Service deployment completed successfully!"
    log_info "The service is now running and ready to accept requests."
}

# Function to show usage
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Deploy the Cartonization service to Kubernetes"
    echo ""
    echo "Options:"
    echo "  -t, --tag TAG     Docker image tag (default: latest)"
    echo "  -n, --namespace NS Kubernetes namespace (default: cartonization)"
    echo "  -h, --help        Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                          # Deploy with default settings"
    echo "  $0 -t v1.0.0               # Deploy with specific tag"
    echo "  $0 -n production           # Deploy to production namespace"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--tag)
            DOCKER_TAG="$2"
            shift 2
            ;;
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            usage
            exit 1
            ;;
    esac
done

# Run main function
main