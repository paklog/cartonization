#!/bin/bash

# Docker Helper Script for Cartonization Service
# This script provides convenient commands for managing the Docker environment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default configuration
COMPOSE_FILE="docker-compose.yml"
ENV_FILE=".env"

# Help function
show_help() {
    echo -e "${BLUE}🐳 Cartonization Service Docker Helper${NC}"
    echo -e "${BLUE}======================================${NC}"
    echo ""
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo -e "${YELLOW}Commands:${NC}"
    echo "  start [dev|prod]     Start the services (default: dev)"
    echo "  stop                 Stop all services"
    echo "  restart [service]    Restart services or specific service"
    echo "  logs [service]       Show logs for all services or specific service"
    echo "  status               Show status of all services"
    echo "  health               Check health of all services"
    echo "  clean                Clean up containers, networks, and volumes"
    echo "  reset                Reset everything (⚠️  destroys data)"
    echo "  build                Build the application image"
    echo "  shell [service]      Open shell in service container"
    echo "  backup               Backup MongoDB data"
    echo "  restore [file]       Restore MongoDB data from backup"
    echo "  setup                Initial setup - copy env files and build"
    echo "  test-api             Test API endpoints"
    echo ""
    echo -e "${YELLOW}Options:${NC}"
    echo "  -f, --follow         Follow logs (for logs command)"
    echo "  -v, --verbose        Verbose output"
    echo "  -h, --help           Show this help message"
    echo ""
    echo -e "${YELLOW}Examples:${NC}"
    echo "  $0 start dev         Start development environment"
    echo "  $0 logs -f           Follow all logs"
    echo "  $0 shell mongodb     Open MongoDB shell"
    echo "  $0 backup            Create MongoDB backup"
}

# Logging functions
log_info() {
    echo -e "${GREEN}ℹ️  $1${NC}"
}

log_warn() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# Check if Docker is running
check_docker() {
    if ! docker info >/dev/null 2>&1; then
        log_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
}

# Setup environment
setup_env() {
    log_info "Setting up environment..."
    
    if [ ! -f "$ENV_FILE" ]; then
        if [ -f ".env.example" ]; then
            cp .env.example "$ENV_FILE"
            log_success "Created $ENV_FILE from .env.example"
        else
            log_warn "No .env.example found. Please create $ENV_FILE manually."
        fi
    else
        log_info "$ENV_FILE already exists"
    fi
    
    # Create necessary directories
    mkdir -p docker/{mongodb,redis,nginx,mockserver}
    log_success "Environment setup complete"
}

# Start services
start_services() {
    local environment=${1:-dev}
    
    check_docker
    setup_env
    
    case $environment in
        "prod"|"production")
            COMPOSE_FILE="docker-compose.prod.yml"
            ENV_FILE=".env.prod"
            log_info "Starting production environment..."
            ;;
        "dev"|"development"|*)
            COMPOSE_FILE="docker-compose.yml"
            log_info "Starting development environment..."
            ;;
    esac
    
    if [ ! -f "$ENV_FILE" ]; then
        log_warn "Environment file $ENV_FILE not found. Using defaults."
    fi
    
    docker-compose -f "$COMPOSE_FILE" up -d
    
    log_success "Services started successfully!"
    log_info "Waiting for services to be ready..."
    sleep 10
    
    # Check if main service is healthy
    if curl -sf http://localhost:8080/actuator/health >/dev/null 2>&1; then
        log_success "Cartonization service is healthy! 🎉"
        echo ""
        echo -e "${BLUE}📋 Quick Access:${NC}"
        echo "  • API: http://localhost:8080"
        echo "  • Swagger UI: http://localhost:8080/swagger-ui.html"
        echo "  • Health: http://localhost:8080/api/v1/health"
        if [ "$environment" = "dev" ]; then
            echo "  • MongoDB Express: http://localhost:8086 (admin/admin123)"
            echo "  • Redis Commander: http://localhost:8087 (admin/admin123)"
            echo "  • Kafka UI: http://localhost:8085"
        fi
    else
        log_warn "Service may still be starting up. Check logs with: $0 logs"
    fi
}

# Stop services
stop_services() {
    log_info "Stopping services..."
    docker-compose -f docker-compose.yml down 2>/dev/null || true
    docker-compose -f docker-compose.prod.yml down 2>/dev/null || true
    log_success "Services stopped"
}

# Restart services
restart_services() {
    local service=$1
    
    if [ -n "$service" ]; then
        log_info "Restarting service: $service"
        docker-compose restart "$service"
    else
        log_info "Restarting all services..."
        docker-compose restart
    fi
    log_success "Restart complete"
}

# Show logs
show_logs() {
    local service=$1
    local follow_flag=""
    
    if [ "$2" = "-f" ] || [ "$2" = "--follow" ]; then
        follow_flag="-f"
    fi
    
    if [ -n "$service" ]; then
        docker-compose logs $follow_flag "$service"
    else
        docker-compose logs $follow_flag
    fi
}

# Show status
show_status() {
    log_info "Service Status:"
    docker-compose ps
    
    echo ""
    log_info "Resource Usage:"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}"
}

# Health check
health_check() {
    log_info "Checking service health..."
    
    # Main service
    if curl -sf http://localhost:8080/actuator/health >/dev/null 2>&1; then
        log_success "Cartonization Service: Healthy"
    else
        log_error "Cartonization Service: Unhealthy"
    fi
    
    # MongoDB
    if docker-compose exec -T mongodb mongosh --quiet --eval "db.adminCommand('ping')" >/dev/null 2>&1; then
        log_success "MongoDB: Healthy"
    else
        log_error "MongoDB: Unhealthy"
    fi
    
    # Redis
    if docker-compose exec -T redis redis-cli ping >/dev/null 2>&1; then
        log_success "Redis: Healthy"
    else
        log_error "Redis: Unhealthy"
    fi
}

# Clean up
clean_up() {
    log_info "Cleaning up containers and networks..."
    docker-compose down --remove-orphans 2>/dev/null || true
    docker-compose -f docker-compose.prod.yml down --remove-orphans 2>/dev/null || true
    docker system prune -f
    log_success "Cleanup complete"
}

# Reset everything
reset_all() {
    log_warn "This will destroy all data and containers. Are you sure? (y/N)"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        log_info "Resetting everything..."
        docker-compose down -v --remove-orphans 2>/dev/null || true
        docker-compose -f docker-compose.prod.yml down -v --remove-orphans 2>/dev/null || true
        docker system prune -a -f --volumes
        log_success "Reset complete"
    else
        log_info "Reset cancelled"
    fi
}

# Build application
build_app() {
    log_info "Building application image..."
    docker build -t paklog/cartonization-service:latest .
    log_success "Build complete"
}

# Open shell
open_shell() {
    local service=${1:-cartonization-service}
    
    log_info "Opening shell in $service..."
    case $service in
        "mongodb")
            docker-compose exec mongodb mongosh
            ;;
        "redis")
            docker-compose exec redis redis-cli
            ;;
        *)
            docker-compose exec "$service" /bin/sh
            ;;
    esac
}

# Backup MongoDB
backup_mongodb() {
    local backup_dir="./backups/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$backup_dir"
    
    log_info "Creating MongoDB backup..."
    docker-compose exec -T mongodb mongodump --uri="mongodb://admin:admin123@localhost:27017/cartonization" --out=/tmp/backup
    docker cp "$(docker-compose ps -q mongodb)":/tmp/backup/cartonization "$backup_dir/"
    
    log_success "Backup created at: $backup_dir"
}

# Restore MongoDB
restore_mongodb() {
    local backup_file=$1
    
    if [ -z "$backup_file" ]; then
        log_error "Please specify backup file: $0 restore [backup-file]"
        exit 1
    fi
    
    if [ ! -d "$backup_file" ]; then
        log_error "Backup directory not found: $backup_file"
        exit 1
    fi
    
    log_info "Restoring MongoDB from: $backup_file"
    docker cp "$backup_file" "$(docker-compose ps -q mongodb)":/tmp/restore
    docker-compose exec -T mongodb mongorestore --uri="mongodb://admin:admin123@localhost:27017/cartonization" --drop /tmp/restore
    
    log_success "Restore complete"
}

# Test API endpoints
test_api() {
    log_info "Testing API endpoints..."
    
    # Health check
    if curl -sf http://localhost:8080/api/v1/health | jq . >/dev/null 2>&1; then
        log_success "Health endpoint: OK"
    else
        log_error "Health endpoint: FAILED"
    fi
    
    # List cartons
    if curl -sf http://localhost:8080/api/v1/cartons | jq . >/dev/null 2>&1; then
        log_success "Cartons endpoint: OK"
    else
        log_error "Cartons endpoint: FAILED"
    fi
    
    log_info "API test complete"
}

# Main script logic
main() {
    case $1 in
        "start")
            start_services "$2"
            ;;
        "stop")
            stop_services
            ;;
        "restart")
            restart_services "$2"
            ;;
        "logs")
            show_logs "$2" "$3"
            ;;
        "status")
            show_status
            ;;
        "health")
            health_check
            ;;
        "clean")
            clean_up
            ;;
        "reset")
            reset_all
            ;;
        "build")
            build_app
            ;;
        "shell")
            open_shell "$2"
            ;;
        "backup")
            backup_mongodb
            ;;
        "restore")
            restore_mongodb "$2"
            ;;
        "setup")
            setup_env
            ;;
        "test-api")
            test_api
            ;;
        "-h"|"--help"|"help"|"")
            show_help
            ;;
        *)
            log_error "Unknown command: $1"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"