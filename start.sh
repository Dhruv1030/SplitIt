#!/bin/bash

echo "🚀 Starting Splitwise Microservices..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop first."
    exit 1
fi

echo "✓ Docker is running"

# Start all services
echo "📦 Starting all services with Docker Compose..."
docker compose up -d

# Wait a bit for services to start
echo "⏳ Waiting for services to start..."
sleep 10

# Show status
echo ""
echo "📊 Service Status:"
docker compose ps

echo ""
echo "✅ Services started!"
echo ""
echo "Access points:"
echo "  • Eureka Dashboard: http://localhost:8761"
echo "  • API Gateway: http://localhost:8080"
echo "  • Zipkin Tracing: http://localhost:9411"
echo ""
echo "To view logs: docker compose logs -f [service-name]"
echo "To stop all: docker compose down"
